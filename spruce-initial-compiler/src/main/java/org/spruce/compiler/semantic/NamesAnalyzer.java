package org.spruce.compiler.semantic;

import org.spruce.compiler.ast.names.ASTNamespaceName;
import org.spruce.compiler.common.MessageProducer;
import org.spruce.compiler.symbol.TopLevelSymbolTable;

/**
 * A <code>NamesAnalyzer</code> is a <code>BasicAnalyzer</code> that analyzes
 * symbols belonging to AST elements having to do with naming.
 */
public class NamesAnalyzer extends BasicAnalyzer {
    /**
     * Constructs a <code>BasicAnalyzer</code> using a <code>MessageProducer</code>.
     * @param analyzer A <code>SemanticAnalyzer</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     */
    public NamesAnalyzer(SemanticAnalyzer analyzer, MessageProducer msgProducer) {
        super(analyzer, msgProducer);
    }

    /**
     * Analyzes a <code>NamespaceName</code>, which is expected to contain at
     * least one identifier.
     * @param namespaceName An <code>ASTNamespaceName</code>.
     */
    public void analyzeNamespaceName(ASTNamespaceName namespaceName, TopLevelSymbolTable topLevel) {

    }
}
