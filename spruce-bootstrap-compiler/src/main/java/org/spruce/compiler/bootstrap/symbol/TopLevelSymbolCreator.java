package org.spruce.compiler.bootstrap.symbol;

import java.util.List;

import org.spruce.compiler.bootstrap.ast.classes.ASTTypeDeclaration;
import org.spruce.compiler.bootstrap.ast.names.ASTIdentifier;
import org.spruce.compiler.bootstrap.ast.names.ASTNamespaceName;
import org.spruce.compiler.bootstrap.ast.toplevel.ASTNamespaceDeclaration;
import org.spruce.compiler.bootstrap.ast.toplevel.ASTOrdinaryCompilationUnit;
import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.resolution.ResolutionContext;
import org.spruce.compiler.bootstrap.resolution.Resolver;
import org.spruce.compiler.bootstrap.resolution.TopLevelResolver;
import org.spruce.compiler.bootstrap.symbol.Symbol.Kind;

import static org.spruce.compiler.bootstrap.symbol.Symbol.FLAG_NONE;
import static org.spruce.compiler.bootstrap.symbol.SymbolTable.Scope.NAMESPACE;

/**
 * A <code>TopLevelSymbolCreator</code> is a <code>BasicSymbolCreator</code>
 * that creates symbols belonging to top level AST elements.
 */
public class TopLevelSymbolCreator extends BasicSymbolCreator {
    /**
     * Constructs a <code>TopLevelSymbolCreator</code>.
     * @param symbolCreator A <code>SymbolCreator</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     * @param globalLookup A <code>GlobalLookup</code>.
     */
    public TopLevelSymbolCreator(SymbolCreator symbolCreator, MessageProducer msgProducer, GlobalLookup globalLookup) {
        super(symbolCreator, msgProducer, globalLookup);
    }

    /**
     * Finds or creates a namespace symbol table in the global type lookup for
     * an <code>OrdinaryCompilationUnit</code>.  Populates the namespace symbol
     * hierarchically with symbols representing what's found in the compilation
     * unit.
     * @param ocus A <code>List</code> of <code>ASTOrdinaryCompilationUnit</code>s.
     */
    public void createSymbolTable(List<ASTOrdinaryCompilationUnit> ocus) {
        // 1. Create symbols for only the types first.
        for (ASTOrdinaryCompilationUnit ocu : ocus) {
            createSymbolTableOcuTypeOnly(ocu);
        }

        // 2. Next, resolve all use statement symbols.  Also create
        //    ResolutionContexts to make type resolution possible.
        Resolver resolver = getEarlyResolver();
        TopLevelResolver tlResolver = resolver.getTopLevelResolver();
        tlResolver.resolveUseStatements(ocus);

        // 3. Create symbols for all non-type members.  When creating symbols
        //    for methods and constructors, the formal parameters will be
        //    resolvable to properly distinguish overloads and detect duplicate
        //    signatures.
        for (ASTOrdinaryCompilationUnit ocu : ocus) {
            createSymbolTableOcuTypeMembers(ocu);
        }
    }

    /**
     * 1. Creates symbols in the global lookup for only the types in an
     * <code>OrdinaryCompilationUnit</code>.  Populates the namespace symbol
     * hierarchically with symbols representing what's found in the compilation
     * unit.
     * @param ocu An <code>ASTOrdinaryCompilationUnit</code>.
     */
    public void createSymbolTableOcuTypeOnly(ASTOrdinaryCompilationUnit ocu) {
        ParentSymbol namespace;
        if (ocu.getNamespaceDecl().isPresent()) {
            ASTNamespaceDeclaration namespaceDecl = ocu.getNamespaceDecl().get();
            namespace = createSymbolsForNamespaceDeclaration(namespaceDecl);
        }
        else {
            namespace = getGlobalLookup().getNamespace(GlobalLookup.UNNAMED_NAMESPACE_NAME)
                    .orElseThrow(() -> internalError("Unnamed namespace not found!"));
        }

        List<ASTTypeDeclaration> typeDecls = ocu.getTypeDeclList().getTypedChildren();
        ClassesSymbolCreator classesCreator = getClassesSymbolCreator();
        for (ASTTypeDeclaration typeDecl : typeDecls) {
            classesCreator.createSymbolsForTopLevelTypeDeclarationTypeOnly(typeDecl, namespace);
        }

        ocu.setDeclSymbol(namespace);
    }

    /**
     * 3. Creates symbols in the global lookup for only the non-type members in
     * an <code>OrdinaryCompilationUnit</code>.  Populates the namespace symbol
     * hierarchically with symbols representing what's found in the compilation
     * unit.
     * @param ocu An <code>ASTOrdinaryCompilationUnit</code>.
     */
    public void createSymbolTableOcuTypeMembers(ASTOrdinaryCompilationUnit ocu) {
        List<ASTTypeDeclaration> typeDecls = ocu.getTypeDeclList().getTypedChildren();
        ClassesSymbolCreator classesCreator = getClassesSymbolCreator();
        ResolutionContext ctx = ocu.getCtx();
        for (ASTTypeDeclaration typeDecl : typeDecls) {
            classesCreator.createSymbolsForTopLevelTypeDeclarationTypeMembers(typeDecl, ctx);
        }
    }

    /**
     * Finds or creates a namespace symbol table in the global type lookup for
     * a <code>NamespaceDeclaration</code>.
     * @param namespaceDecl An <code>ASTNamespaceDeclaration</code>.
     * @return The <code>ParentSymbol</code> for the last part of the namespace, e.g.
     *     "concurrent" for "spruce.collections.concurrent".
     */
    public ParentSymbol createSymbolsForNamespaceDeclaration(ASTNamespaceDeclaration namespaceDecl) {
        ASTNamespaceName namespaceName = namespaceDecl.getNamespace();
        return createSymbolsForNamespaceName(namespaceName);
    }

    /**
     * Creates a symbol for a <code>NamespaceName</code> and populates it in
     * the parent symbol table.  It is expected to contain at least one identifier.
     * @param namespaceName An <code>ASTNamespaceName</code>.
     * @return The canonical <code>ParentSymbol</code> for the last part of the
     *     namespace, e.g. "concurrent" for "spruce.collections.concurrent".
     */
    public ParentSymbol createSymbolsForNamespaceName(ASTNamespaceName namespaceName) {
        List<ASTIdentifier> identifiers = namespaceName.getTypedChildren();
        SymbolTable global = getGlobalLookup();
        // First
        if (identifiers.isEmpty()) {
            throw internalError("identifier in namespace");
        }
        ASTIdentifier first = identifiers.get(0);
        String name = first.getValue();
        ParentSymbol curr = new ParentSymbol(first.getLocation(), name, Kind.NAMESPACE, global, FLAG_NONE);
        ChildSymbolTable table = new ChildSymbolTable(NAMESPACE, curr);
        curr.setTable(table);
        ParentSymbol canonical = global.findOrAddSymbol(curr);
        table = canonical.getTable();
        checkNameConflict(curr, canonical);

        // Rest
        for (int i = 1; i < identifiers.size(); i++) {
            ChildSymbolTable currTable = table;
            ASTIdentifier id = identifiers.get(i);
            name = id.getValue();
            curr = new ParentSymbol(id.getLocation(), name, Kind.NAMESPACE, table, FLAG_NONE);
            table = new ChildSymbolTable(NAMESPACE, curr);
            curr.setTable(table);
            canonical = currTable.findOrAddSymbol(curr);
            table = canonical.getTable();
            checkNameConflict(curr, canonical);
        }

        return canonical;
    }

    private void checkNameConflict(Symbol symbol, Symbol canonical) {
        if (canonical.isType()) {
            handleNameConflictError(symbol, canonical);
        }
    }
}
