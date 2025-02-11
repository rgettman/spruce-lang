package org.spruce.compiler.symbol;

import java.util.List;

import org.spruce.compiler.ast.classes.ASTTypeDeclaration;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.names.ASTNamespaceName;
import org.spruce.compiler.ast.toplevel.ASTNamespaceDeclaration;
import org.spruce.compiler.ast.toplevel.ASTOrdinaryCompilationUnit;
import org.spruce.compiler.common.MessageProducer;

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
     * Creates symbols for an <code>OrdinaryCompilationUnit</code>.  Populates
     * the given <code>TopLevelSymbolTable</code> with any namespace
     * declaration symbols, any use declarations, and any type declarations.
     * @param ocu An <code>ASTOrdinaryCompilationUnit</code>.
     * @param topLevel A <code>TopLevelSymbolTable</code>.
     */
    public void createSymbolsForCompUnit(ASTOrdinaryCompilationUnit ocu, TopLevelSymbolTable topLevel) {
        if (ocu.getNamespaceDecl().isPresent()) {
            ASTNamespaceDeclaration namespaceDecl = ocu.getNamespaceDecl().get();
            createSymbolsForNamespaceDeclaration(namespaceDecl, topLevel);
        }
        List<ASTTypeDeclaration> typeDecls = ocu.getTypeDeclList().getTypedChildren();
        ClassesSymbolCreator classesCreator = getClassesSymbolCreator();
        for (ASTTypeDeclaration typeDecl : typeDecls) {
            insertSymbol(topLevel, classesCreator.createSymbolsForTopLevelTypeDeclaration(typeDecl, topLevel));
        }
    }

    /**
     * Creates symbols for a <code>NamespaceDeclaration</code>.  Adds a
     * <code>NAMESPACE</code> symbol table to the given <code>TopLevelSymbolTable</code>.
     * @param namespaceDecl An <code>ASTNamespaceDeclaration</code>.
     * @param topLevel A <code>TopLevelSymbolTable</code>.
     */
    public void createSymbolsForNamespaceDeclaration(ASTNamespaceDeclaration namespaceDecl, TopLevelSymbolTable topLevel) {
        ChildSymbolTable namespace = new ChildSymbolTable(NAMESPACE, topLevel);
        topLevel.addNamespace(namespace);

        ASTNamespaceName namespaceName = namespaceDecl.getNamespace();
        insertSymbol(namespace, createSymbolsForNamespaceName(namespaceName, topLevel));
    }

    /**
     * Creates symbols for a <code>NamespaceName</code>, which is expected to
     * contain at least one identifier.
     * @param namespaceName An <code>ASTNamespaceName</code>.
     * @return A <code>Symbol</code> for the namespace name.
     */
    public Symbol createSymbolsForNamespaceName(ASTNamespaceName namespaceName, TopLevelSymbolTable topLevel) {
        List<ASTIdentifier> identifiers = namespaceName.getTypedChildren();

        // First
        SymbolTable parent = topLevel;
        if (identifiers.isEmpty()) {
            throw internalError("identifier in namespace");
        }
        ASTIdentifier first = identifiers.get(0);
        ParentSymbol curr = new ParentSymbol(first.getLocation(), first.getValue(), Symbol.Type.NAMESPACE, parent, 0, NAMESPACE);
        Symbol symbol = curr;

        // Rest
        for (int i = 1; i < identifiers.size(); i++) {
            parent = curr.getTable();
            ASTIdentifier id = identifiers.get(i);
            curr = new ParentSymbol(id.getLocation(), id.getValue(), Symbol.Type.NAMESPACE, parent, 0, NAMESPACE);
            parent.insertSymbol(curr);
        }

        return symbol;
    }
}
