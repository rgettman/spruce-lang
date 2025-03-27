package org.spruce.compiler.symbol;

import java.util.List;

import org.spruce.compiler.ast.classes.ASTTypeDeclaration;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.names.ASTNamespaceName;
import org.spruce.compiler.ast.toplevel.ASTNamespaceDeclaration;
import org.spruce.compiler.ast.toplevel.ASTOrdinaryCompilationUnit;
import org.spruce.compiler.common.MessageProducer;
import org.spruce.compiler.symbol.Symbol.Type;

import static org.spruce.compiler.symbol.Symbol.FLAG_NONE;
import static org.spruce.compiler.symbol.SymbolTable.Scope.NAMESPACE;

/**
 * A <code>TopLevelSymbolCreator</code> is a <code>BasicSymbolCreator</code>
 * that creates symbols belonging to top level AST elements.
 */
public class TopLevelSymbolCreator extends BasicSymbolCreator {
    /**
     * Constructs a <code>TopLevelSymbolCreator</code>.
     * @param symbolCreator A <code>SymbolCreator</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     */
    public TopLevelSymbolCreator(SymbolCreator symbolCreator, MessageProducer msgProducer) {
        super(symbolCreator, msgProducer);
    }

    /**
     * Creates and returns a top level symbol table for an <code>OrdinaryCompilationUnit</code>.
     * Populates the <code>TopLevelSymbolTable</code> with any namespace
     * declaration symbols, any use declarations, and any type declarations.
     * @param ocu An <code>ASTOrdinaryCompilationUnit</code>.
     */
    public TopLevelSymbolTable createSymbolTableForCompUnit(ASTOrdinaryCompilationUnit ocu) {
        TopLevelSymbolTable topLevel = new TopLevelSymbolTable();
        if (ocu.getNamespaceDecl().isPresent()) {
            ASTNamespaceDeclaration namespaceDecl = ocu.getNamespaceDecl().get();
            ChildSymbolTable namespaceTable = createSymbolTableForNamespaceDeclaration(namespaceDecl, topLevel);
            topLevel.addNamespace(namespaceTable);
        }
        List<ASTTypeDeclaration> typeDecls = ocu.getTypeDeclList().getTypedChildren();
        ClassesSymbolCreator classesCreator = getClassesSymbolCreator();
        for (ASTTypeDeclaration typeDecl : typeDecls) {
            classesCreator.createSymbolsForTopLevelTypeDeclaration(typeDecl, topLevel);
        }
        return topLevel;
    }

    /**
     * Creates and populates a symbol table for a <code>NamespaceDeclaration</code>.
     * @param namespaceDecl An <code>ASTNamespaceDeclaration</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the symbol table.
     * @return A <code>ChildSymbolTable</code>.
     */
    public ChildSymbolTable createSymbolTableForNamespaceDeclaration(ASTNamespaceDeclaration namespaceDecl,
                                                                     SymbolTable parent) {
        ChildSymbolTable table = new ChildSymbolTable(NAMESPACE, parent);
        ASTNamespaceName namespaceName = namespaceDecl.getNamespace();
        createSymbolsForNamespaceName(namespaceName, table);
        return table;
    }

    /**
     * Creates a symbol for a <code>NamespaceName</code> and populates it in
     * the parent symbol table.  It is expected to contain at least one identifier.
     * @param namespaceName An <code>ASTNamespaceName</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the symbol.
     */
    public void createSymbolsForNamespaceName(ASTNamespaceName namespaceName, SymbolTable parent) {
        List<ASTIdentifier> identifiers = namespaceName.getTypedChildren();

        // First
        if (identifiers.isEmpty()) {
            throw internalError("identifier in namespace");
        }
        ASTIdentifier first = identifiers.get(0);
        ParentSymbol curr = new ParentSymbol(first.getLocation(), first.getValue(), Type.NAMESPACE, parent, FLAG_NONE);
        insertSymbol(parent, curr);
        ChildSymbolTable table = new ChildSymbolTable(NAMESPACE, parent);
        curr.setTable(table);

        // Rest
        for (int i = 1; i < identifiers.size(); i++) {
            ASTIdentifier id = identifiers.get(i);
            curr = new ParentSymbol(id.getLocation(), id.getValue(), Type.NAMESPACE, table, FLAG_NONE);
            insertSymbol(table, curr);
            table = new ChildSymbolTable(NAMESPACE, table);
            curr.setTable(table);
        }
    }
}
