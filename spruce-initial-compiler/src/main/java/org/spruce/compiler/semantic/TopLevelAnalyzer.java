package org.spruce.compiler.semantic;

import org.spruce.compiler.ast.names.ASTNamespaceName;
import org.spruce.compiler.ast.toplevel.ASTNamespaceDeclaration;
import org.spruce.compiler.ast.toplevel.ASTOrdinaryCompilationUnit;
import org.spruce.compiler.common.MessageProducer;
import org.spruce.compiler.symbol.ChildSymbolTable;
import org.spruce.compiler.symbol.TopLevelSymbolTable;

import static org.spruce.compiler.symbol.SymbolTable.Scope.*;

/**
 * A <code>TopLevelAnalyzer</code> is a <code>BasicAnalyzer</code> that analyzes
 * symbols belonging to top level AST elements.
 */
public class TopLevelAnalyzer extends BasicAnalyzer {
    /**
     * Constructs a <code>TopLevelAnalyzer</code>.
     * @param analyzer A <code>SemanticAnalyzer</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     */
    public TopLevelAnalyzer(SemanticAnalyzer analyzer, MessageProducer msgProducer) {
        super(analyzer, msgProducer);
    }

    /**
     * Analyzes an <code>OrdinaryCompilationUnit</code>.  Populates the given
     * <code>TopLevelSymbolTable</code> with any namespace declaration symbols,
     * any use declarations, and any type declarations.
     * @param ocu An <code>ASTOrdinaryCompilationUnit</code>.
     * @param topLevel A <code>TopLevelSymbolTable</code>.
     */
    public void analyzeCompUnit(ASTOrdinaryCompilationUnit ocu, TopLevelSymbolTable topLevel) {
        if (ocu.getNamespaceDecl().isPresent()) {
            ASTNamespaceDeclaration namespaceDecl = ocu.getNamespaceDecl().get();
            analyzeNamespaceDeclaration(namespaceDecl, topLevel);
        }
    }

    /**
     * Analyzes a <code>NamespaceDeclaration</code>.  Adds a <code>NAMESPACE</code>
     * symbol table to the given <code>TopLevelSymbolTable</code>.
     * @param namespaceDecl An <code>ASTNamespaceDeclaration</code>.
     * @param topLevel A <code>TopLevelSymbolTable</code>.
     */
    public void analyzeNamespaceDeclaration(ASTNamespaceDeclaration namespaceDecl, TopLevelSymbolTable topLevel) {
        ChildSymbolTable namespace = new ChildSymbolTable(NAMESPACE, topLevel);
        topLevel.addNamespace(namespace);

        ASTNamespaceName namespaceName = namespaceDecl.getNamespace();
        insertSymbol(namespace, getNamesAnalyzer().analyzeNamespaceName(namespaceName, topLevel));
    }
}
