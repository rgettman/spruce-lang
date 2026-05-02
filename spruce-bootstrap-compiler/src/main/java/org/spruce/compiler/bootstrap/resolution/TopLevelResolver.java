package org.spruce.compiler.bootstrap.resolution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.spruce.compiler.bootstrap.ast.classes.ASTTypeDeclaration;
import org.spruce.compiler.bootstrap.ast.names.ASTIdentifier;
import org.spruce.compiler.bootstrap.ast.toplevel.ASTOrdinaryCompilationUnit;
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
import org.spruce.compiler.bootstrap.symbol.TypeLookup;

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
     * @param global The global <code>TypeLookup</code>.
     */
    public TopLevelResolver(Resolver resolver, MessageProducer msgProducer, TypeLookup global) {
        super(resolver, msgProducer, global);
    }

    /**
     * Resolve all symbols in all <code>OrdinaryCompilationUnit</code>s along
     * with the global <code>TypeLookup</code>.
     * @param units A <code>List</code> of <code>ASTOrdinaryCompilationUnit</code>s.
     */
    public void resolveOrdinaryCompilationUnits(List<ASTOrdinaryCompilationUnit> units) {
        for (ASTOrdinaryCompilationUnit ocu : units) {
            resolveOrdinaryCompilationUnit(ocu);
        }
    }

    /**
     * Resolve all symbols in an <code>OrdinaryCompilationUnit</code> using the
     * given <code>TypeLookup</code>.
     * @param ocu An <code>ASTOrdinaryCompilationUnit</code>.
     */
    public void resolveOrdinaryCompilationUnit(ASTOrdinaryCompilationUnit ocu) {
        // Keeps track of used namespaces being brought into scope.
        Map<String, ParentSymbol> using = new HashMap<>();
        // Keep track of parent symbol tables of simple names to detect a
        // potential conflict.
        Map<String, SymbolTable> namesUsed = new HashMap<>();

        // Preload this OCU's type declarations, whose simple names cannot be
        // reused.
        SymbolTable table = ocu.getDeclSymbol().getTable();
        for (ASTTypeDeclaration typeDecl : ocu.getTypeDeclList().getTypedChildren()) {
            namesUsed.put(typeDecl.getName().getValue(), table);
        }

        ASTUseDeclarationList useDeclList = ocu.getUseDeclList();
        for (ASTUseDeclaration useDecl : useDeclList.getTypedChildren()) {
            resolveUseDeclaration(useDecl, namesUsed, using);
        }

        // Next: Loop through the Type Declarations.
        ParentSymbol namespace = ocu.getDeclSymbol();
        ResolutionContext ctx = new ResolutionContext(getGlobalLookup(), namespace, using);
        ClassesResolver classesResolver = getClassesResolver();
        for (ASTTypeDeclaration typeDecl : ocu.getTypeDeclList().getTypedChildren()) {
            classesResolver.resolveTypeDeclaration(typeDecl, ctx);
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
     * @param namesUsed A <code>Map</code> of simple names to parent
     *                    <code>SymbolTable</code>s used to detect a conflict with
     *                    the same simple name in a different namespace.
     * @param using A <code>Map</code> of simple names to <code>ParentSymbols</code>
     *              brought into scope by a Use Declaration.  This will be
     *              referenced later during data type symbol resolution.
     */
    public void resolveUseTypeDeclaration(ASTUseTypeDeclaration utd, Map<String, SymbolTable> namesUsed,
                                          Map<String, ParentSymbol> using) {
        List<ASTIdentifier> typeIds = utd.getTypename().getTypedChildren();
        ASTIdentifier typeId = typeIds.getLast();
        SymbolTable currTable = getGlobalLookup();
        Optional<ParentSymbol> optResolved = resolveNamespaceOrTypeNameIds(typeIds, currTable);
        if (optResolved.isEmpty()) {
            return;
        }

        ParentSymbol resolved = optResolved.get();
        if (ensureIsTypeAndInsert(typeId.getLocation(), resolved, namesUsed)) {
            utd.setResolvedSymbol(resolved);
            using.put(resolved.getName(), resolved);
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
        List<ASTIdentifier> namespaceOrTypeIds = umd.getNamespaceOrTypeName().getTypedChildren();
        SymbolTable currTable = getGlobalLookup();
        Optional<ParentSymbol> optResolved = resolveNamespaceOrTypeNameIds(namespaceOrTypeIds, currTable);
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
                    umd.addResolvedSymbol(resolved);
                    using.put(resolved.getName(), resolved);
                }
            }
            else {
                error(typeId.getLocation(), "Symbol '" + name + "' not found.");
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
        List<ASTIdentifier> namespaceOrTypeIds = uad.getNamespaceOrTypeName().getTypedChildren();
        SymbolTable currTable = getGlobalLookup();
        Optional<ParentSymbol> optResolved = resolveNamespaceOrTypeNameIds(namespaceOrTypeIds, currTable);
        // Legal to resolve a namespace or a type here.
        optResolved.ifPresent(resolved -> {
            uad.setResolvedSymbol(resolved);
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

    private Optional<ParentSymbol> resolveNamespaceOrTypeNameIds(List<ASTIdentifier> ids, SymbolTable currTable) {
        // Parser guarantees at least one identifier, which must be a namespace.
        String first = ids.get(0).getValue();
        ParentSymbol resolved;
        if (currTable.containsNamespace(first)) {
            resolved = (ParentSymbol) currTable.get(first);
            currTable = resolved.getTable();
        }
        else {
            error(ids.get(0).getLocation(), "Symbol '" + first + "' not found.");
            return Optional.empty();
        }

        // The remaining identifiers can be namespaces or types, but once a
        // type is found, then all subsequent identifiers must be types until
        // fully resolved.
        boolean typeFound = false;
        for (int i = 1; i < ids.size(); i++) {
            ASTIdentifier id = ids.get(i);
            String name = id.getValue();

            boolean nameFound;
            if (typeFound) {
                nameFound = currTable.containsType(name);
            }
            else {
                nameFound = currTable.containsNamespaceOrType(name);
            }
            if (nameFound) {
                resolved = (ParentSymbol) currTable.get(name);
                currTable = resolved.getTable();
                typeFound = resolved.getKind() != Symbol.Kind.NAMESPACE;
            }
            else {
                error(id.getLocation(), "Symbol '" + name + "' not found.");
                return Optional.empty();
            }
        }

        return Optional.of(resolved);
    }
}
