package org.spruce.compiler.bootstrap.resolution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.spruce.compiler.bootstrap.ast.classes.ASTTypeDeclaration;
import org.spruce.compiler.bootstrap.ast.names.ASTIdentifier;
import org.spruce.compiler.bootstrap.ast.toplevel.ASTOrdinaryCompilationUnit;
import org.spruce.compiler.bootstrap.ast.toplevel.ASTTypeDeclarationList;
import org.spruce.compiler.bootstrap.ast.toplevel.ASTUseAllDeclaration;
import org.spruce.compiler.bootstrap.ast.toplevel.ASTUseDeclaration;
import org.spruce.compiler.bootstrap.ast.toplevel.ASTUseDeclarationList;
import org.spruce.compiler.bootstrap.ast.toplevel.ASTUseMultDeclaration;
import org.spruce.compiler.bootstrap.ast.toplevel.ASTUseTypeDeclaration;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.symbol.ParentSymbol;
import org.spruce.compiler.bootstrap.symbol.Symbol;
import org.spruce.compiler.bootstrap.symbol.SymbolTable;
import org.spruce.compiler.bootstrap.symbol.GlobalLookup;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;

/**
 * A <code>TopLevelResolver</code> is a <code>BasicResolver</code> that resolves
 * data types in the top-level portion of an AST and Symbol Table: use type
 * declarations.
 */
public class TopLevelResolver extends BasicResolver {

    /**
     * Constructs a <code>TopLevelResolver</code>.
     * @param resolver An <code>Resolver</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     * @param global The <code>GlobalLookup</code>.
     */
    public TopLevelResolver(Resolver resolver, MessageProducer msgProducer, GlobalLookup global) {
        super(resolver, msgProducer, global);
    }

    /**
     * Resolve all use statement symbols in all <code>OrdinaryCompilationUnit</code>s
     * along with the <code>GlobalLookup</code>.  Creates <code>ResolutionContext</code>s
     * for each <code>OrdinaryCompilationUnit</code> for use later.
     * @param units A <code>List</code> of <code>ASTOrdinaryCompilationUnit</code>s.
     */
    public void resolveUseStatements(List<ASTOrdinaryCompilationUnit> units) {
        for (ASTOrdinaryCompilationUnit ocu : units) {
            // Keep track of used simple names being brought into scope.
            Map<String, ParentSymbol> using = new HashMap<>();
            // Keep track of parent symbol tables of simple names to detect a
            // potential conflict.
            Map<String, SymbolTable> namesUsed = new HashMap<>();

            // Preload this OCU's type declarations, whose simple names cannot
            // be reused.
            SymbolTable table = ocu.getDeclSymbol().getTable();
            for (ASTTypeDeclaration typeDecl : ocu.getTypeDeclList().getTypedChildren()) {
                namesUsed.put(typeDecl.getName().getValue(), table);
            }

            ASTUseDeclarationList useDeclList = ocu.getUseDeclList();
            for (ASTUseDeclaration useDecl : useDeclList.getTypedChildren()) {
                resolveUseDeclaration(useDecl, namesUsed, using);
            }

            ensureImplicitUseAll(using);

            // Create the ResolutionContexts.
            ParentSymbol namespace = ocu.getDeclSymbol();
            ResolutionContext ctx = new ResolutionContext(using, namespace);
            ocu.setCtx(ctx);
        }
    }

    /**
     * Resolve all remaining symbols in all <code>OrdinaryCompilationUnit</code>s
     * along with the <code>GlobalLookup</code>.
     * @param units A <code>List</code> of <code>ASTOrdinaryCompilationUnit</code>s.
     */
    public void resolveOrdinaryCompilationUnits(List<ASTOrdinaryCompilationUnit> units) {
        ClassesResolver classesResolver = getClassesResolver();

        // 1. Resolve all superclass and superinterface symbols first.
        for (ASTOrdinaryCompilationUnit ocu : units) {
            ASTTypeDeclarationList typeDeclList = ocu.getTypeDeclList();
            ResolutionContext ctx = ocu.getCtx();
            classesResolver.resolveTypeDeclarationListExtends(typeDeclList, ctx);
        }

        // 2. Detect dependency cycles.
        boolean cycleDetected = false;
        for (ASTOrdinaryCompilationUnit ocu : units) {
            cycleDetected |= classesResolver.detectDependencyCycles(ocu.getTypeDeclList());
        }

        // 3. Resolve all member symbols, some of which rely on there being no
        //    dependency cycles.
        if (!cycleDetected) {
            for (ASTOrdinaryCompilationUnit ocu : units) {
                ResolutionContext ctx = ocu.getCtx();
                classesResolver.resolveTypeDeclarationListMembers(ocu.getTypeDeclList(), ctx);
            }
        }
    }

    /**
     * Resolve all symbols in a <code>UseDeclaration</code>.
     * @param useDecl An <code>ASTUseDeclaration</code>.
     * @param namesUsed A <code>Map</code> of simple names to
     *                    <code>Location</code>s used to detect a conflict with
     *                    the same simple name in a different namespace.
     * @param using A <code>Map</code> of simple names to <code>ParentSymbols</code>
     *              brought into scope by a Use Declaration.  This will be
     *              referenced later during data type symbol resolution.
     */
    public void resolveUseDeclaration(ASTUseDeclaration useDecl, Map<String, SymbolTable> namesUsed,
                                      Map<String, ParentSymbol> using) {
        switch(useDecl) {
        case ASTUseTypeDeclaration utd -> resolveUseTypeDeclaration(utd, namesUsed, using);
        case ASTUseMultDeclaration umd -> resolveUseMultDeclaration(umd, namesUsed, using);
        case ASTUseAllDeclaration uad -> resolveUseAllDeclaration(uad, using);
        default -> throw internalError("Unsupported use declaration: " + useDecl.getClass().getSimpleName());
        }
    }

    /**
     * Resolve all symbols in a <code>UseTypeDeclaration</code>.
     * @param utd An <code>ASTUseTypeDeclaration</code>.
     * @param simpleNames A <code>Map</code> of simple names to parent
     *                    <code>SymbolTable</code>s used to detect a conflict with
     *                    the same simple name in a different namespace.
     * @param using A <code>Map</code> of simple names to <code>ParentSymbols</code>
     *              brought into scope by a Use Declaration.  This will be
     *              referenced later during data type symbol resolution.
     */
    public void resolveUseTypeDeclaration(ASTUseTypeDeclaration utd, Map<String, SymbolTable> simpleNames,
                                          Map<String, ParentSymbol> using) {
        Optional<TypeSymbol> optResolved = getNamesResolver().resolveUseTypeName(utd.getTypename());
        if (optResolved.isPresent()) {
            TypeSymbol resolved = optResolved.get();
            List<ASTIdentifier> typeIds = utd.getTypename().getTypedChildren();
            ASTIdentifier typeId = typeIds.getLast();
            if (ensureIsTypeAndInsert(typeId.getLocation(), resolved, simpleNames)) {
                utd.setResolvedDataType(resolved);
                using.put(resolved.getName(), resolved);
            }
        }
    }

    /**
     * Resolve all symbols in a <code>UseMultDeclaration</code>.
     * @param umd An <code>ASTUseMultDeclaration</code>.
     * @param simpleNames A <code>Map</code> of simple names to parent
     *                    <code>SymbolTable</code>s used to detect a conflict with
     *                    the same simple name in a different namespace.
     * @param using A <code>Map</code> of simple names to <code>ParentSymbols</code>
     *              brought into scope by a Use Declaration.  This will be
     *              referenced later during data type symbol resolution.
     */
    public void resolveUseMultDeclaration(ASTUseMultDeclaration umd, Map<String, SymbolTable> simpleNames,
                                          Map<String, ParentSymbol> using) {
        Optional<ParentSymbol> optResolved = getNamesResolver().resolveNamespaceOrTypeName(
                umd.getNamespaceOrTypeName());
        if (optResolved.isEmpty()) {
            return;
        }

        ParentSymbol base = optResolved.get();
        // Resolve the identifiers within the braces.
        SymbolTable table = base.getTable();
        List<ASTIdentifier> typeIds = umd.getIdentifierList().getTypedChildren();
        for (ASTIdentifier typeId : typeIds) {
            String name = typeId.getValue();
            if (table.containsSymbolName(name)) {
                ParentSymbol resolved = (ParentSymbol) table.get(name);
                if (ensureIsTypeAndInsert(typeId.getLocation(), resolved, simpleNames)) {
                    umd.addResolvedDataType((TypeSymbol) resolved);
                    using.put(resolved.getName(), resolved);
                }
            }
            else {
                errorSymbolNotFound(typeId.getLocation(), name);
            }
        }
    }

    /**
     * Resolve all symbols in a <code>UseAllDeclaration</code>.
     * @param uad An <code>ASTUseAllTypeDeclaration</code>.
     * @param using A <code>Map</code> of simple names to <code>ParentSymbols</code>
     *              brought into scope by a Use Declaration.  This will be
     *              referenced later during data type symbol resolution.
     */
    public void resolveUseAllDeclaration(ASTUseAllDeclaration uad, Map<String, ParentSymbol> using) {
        Optional<ParentSymbol> optResolved = getNamesResolver().resolveNamespaceOrTypeName(
                uad.getNamespaceOrTypeName());
        // Legal to resolve a namespace or a type here.
        optResolved.ifPresent(resolved -> {
            uad.setResolvedNamespace(resolved);
            using.put(resolved.getName(), resolved);
        });
    }

    // Return false - error, don't set resolved symbol.
    // Return true - validated, set resolved symbol.
    private boolean ensureIsTypeAndInsert(Location loc, Symbol resolved, Map<String, SymbolTable> simpleNames) {
        String name = resolved.getName();
        // The resolved symbol must be a type.
        if (resolved.getKind() == Symbol.Kind.NAMESPACE) {
            error(loc, "Type '" + name + "' has the same name as an existing namespace.");
            return false;
        }

        if (simpleNames.containsKey(name)) {
            SymbolTable dupeParent = simpleNames.get(name);
            SymbolTable resolvedParent = resolved.getParent();
            if (dupeParent != resolvedParent) {
                error(loc, "Symbol '" + name + "' already being used.");
            }
            // Duplicate use statement for the same symbol is ok but ignored.
            return false;
        }
        else {
            simpleNames.put(name, resolved.getParent());
            return true;
        }
    }

    /**
     * Ensures that the "spruce.lang" namespace is being used.  If it's not
     * already being used explicitly, then add it implicitly.
     * @param using A <code>Map</code> of simple names to <code>ParentSymbol</code>s
     *              representing all explicit use declarations on an OCU.
     */
    public void ensureImplicitUseAll(Map<String, ParentSymbol> using) {
        final String spruceName = "spruce";
        final String langName = "lang";
        // Find spruce.lang in the Type Lookup.
        Optional<ParentSymbol> optSpruce = getGlobalLookup().getNamespace(spruceName);
        if (optSpruce.isEmpty()) {
            // No spruce namespace declared; can't use what doesn't exist.
            return;
        }
        ParentSymbol spruce = optSpruce.get();
        if (!spruce.getTable().containsNamespace(langName)) {
            // No spruce.lang namespace declared; can't use what doesn't exist.
            return;
        }
        ParentSymbol lang = (ParentSymbol) spruce.getTable().get(langName);

        // If it exists, determine if it's already (explicitly) being used.
        if (using.containsKey(langName)) {
            ParentSymbol namespace = using.get(langName);
            if (namespace == lang) {
                return;
            }
        }

        // If it isn't already being used, create an implicit use-all
        // declaration on spruce.lang.
        using.put(langName, lang);
    }
}
