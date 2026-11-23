package org.spruce.compiler.bootstrap.analysis;

import java.util.List;

import org.spruce.compiler.bootstrap.ast.toplevel.ASTOrdinaryCompilationUnit;
import org.spruce.compiler.bootstrap.ast.toplevel.ASTTypeDeclarationList;
import org.spruce.compiler.bootstrap.common.MessageProducer;

/**
 * A <code>TopLevelAnalyzer</code> is a <code>BasicAnalyzer</code> that starts
 * the data flow analysis by drilling down into AST nodes that require such
 * analysis.
 */
public class TopLevelAnalyzer extends BasicAnalyzer {
    /**
     * Constructs a <code>TopLevelAnalyzer</code>.
     * @param analyzer An <code>Analyzer</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     */
    public TopLevelAnalyzer(Analyzer analyzer, MessageProducer msgProducer) {
        super(analyzer, msgProducer);
    }

    /**
     * Analyze the flow of all <code>OrdinaryCompilationUnit</code>s.
     * @param ocus A <code>List</code> of <code>ASTOrdinaryCompilationUnit</code>s.
     */
    public void analyzeOrdinaryCompilationUnits(List<ASTOrdinaryCompilationUnit> ocus) {
        for (ASTOrdinaryCompilationUnit ocu : ocus) {
            ASTTypeDeclarationList typeDeclList = ocu.getTypeDeclList();
            getClassesAnalyzer().analyzeTypeDeclarationList(typeDeclList);
        }
    }
}
