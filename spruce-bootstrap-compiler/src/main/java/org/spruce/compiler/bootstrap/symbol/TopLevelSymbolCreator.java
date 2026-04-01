package org.spruce.compiler.bootstrap.symbol;

import java.util.List;

import org.spruce.compiler.bootstrap.ast.classes.ASTTypeDeclaration;
import org.spruce.compiler.bootstrap.ast.names.ASTIdentifier;
import org.spruce.compiler.bootstrap.ast.names.ASTNamespaceName;
import org.spruce.compiler.bootstrap.ast.toplevel.ASTNamespaceDeclaration;
import org.spruce.compiler.bootstrap.ast.toplevel.ASTOrdinaryCompilationUnit;
import org.spruce.compiler.bootstrap.common.MessageProducer;
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
     * @param typeLookup A <code>TypeLookup</code>.
     */
    public TopLevelSymbolCreator(SymbolCreator symbolCreator, MessageProducer msgProducer, TypeLookup typeLookup) {
        super(symbolCreator, msgProducer, typeLookup);
    }

    /**
     * Finds or creates a namespace symbol table in the global type lookup for
     * an <code>OrdinaryCompilationUnit</code>.  Populates the namespace symbol
     * hierarchically with symbols representing what's found in the compilation
     * unit.
     * @param ocu An <code>ASTOrdinaryCompilationUnit</code>.
     */
    public void createSymbolTableForCompUnit(ASTOrdinaryCompilationUnit ocu) {
        ParentSymbol namespace;
        if (ocu.getNamespaceDecl().isPresent()) {
            ASTNamespaceDeclaration namespaceDecl = ocu.getNamespaceDecl().get();
            namespace = createSymbolsForNamespaceDeclaration(namespaceDecl);
        }
        else {
            namespace = getTypeLookup().getNamespace(TypeLookup.UNNAMED_NAMESPACE_NAME)
                    .orElseThrow(() -> internalError("Unnamed namespace not found!"));
        }

        List<ASTTypeDeclaration> typeDecls = ocu.getTypeDeclList().getTypedChildren();
        ClassesSymbolCreator classesCreator = getClassesSymbolCreator();
        for (ASTTypeDeclaration typeDecl : typeDecls) {
            classesCreator.createSymbolsForTopLevelTypeDeclaration(typeDecl, namespace.getTable());
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
        SymbolTable parent = getTypeLookup();

        // First
        if (identifiers.isEmpty()) {
            throw internalError("identifier in namespace");
        }
        ASTIdentifier first = identifiers.get(0);
        String name = first.getValue();
        ParentSymbol curr = new ParentSymbol(first.getLocation(), name, Kind.NAMESPACE, parent,
                DataType.NONE, FLAG_NONE);
        ChildSymbolTable table = new ChildSymbolTable(NAMESPACE, parent);
        curr.setTable(table);
        ParentSymbol canonical = parent.findOrAddSymbol(curr);

        // Rest
        for (int i = 1; i < identifiers.size(); i++) {
            parent = canonical.getTable();
            ASTIdentifier id = identifiers.get(i);
            name = id.getValue();
            curr = new ParentSymbol(id.getLocation(), name, Kind.NAMESPACE, table,
                    DataType.NONE, FLAG_NONE);
            table = new ChildSymbolTable(NAMESPACE, table);
            curr.setTable(table);
            canonical = parent.findOrAddSymbol(curr);
        }

        return canonical;
    }
}
