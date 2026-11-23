package org.spruce.compiler.bootstrap.analysis;

import java.util.List;

import org.spruce.compiler.bootstrap.ast.toplevel.ASTOrdinaryCompilationUnit;
import org.spruce.compiler.bootstrap.common.CompilerMessage;
import org.spruce.compiler.bootstrap.common.MessageProducer;

/**
 * A <code>Analyzer</code> performs flow analysis, analyzing the AST to ensure
 * that all local variables are initialized before they are read, that every
 * statement is reachable, and for non-void methods, that every execution path
 * returns something.
 */
public class Analyzer {
    private final TopLevelAnalyzer myTopLevelAnalyzer;
    private final ClassesAnalyzer myClassesAnalyzer;
    private final StatementsAnalyzer myStatementsAnalyzer;
    private final ExpressionsAnalyzer myExpressionsAnalyzer;
    private final MessageProducer myMsgProducer;

    /**
     * Constructs an <code>Resolver</code> with the given
     * <code>MessageProducer</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     */
    public Analyzer(MessageProducer msgProducer) {
        myTopLevelAnalyzer = new TopLevelAnalyzer(this, msgProducer);
        myClassesAnalyzer = new ClassesAnalyzer(this, msgProducer);
        myStatementsAnalyzer = new StatementsAnalyzer(this, msgProducer);
        myExpressionsAnalyzer = new ExpressionsAnalyzer(this, msgProducer);

        myMsgProducer = msgProducer;
    }

    /**
     * Returns the <code>TopLevelAnalyzer</code>.
     * @return The <code>TopLevelAnalyzer</code>.
     */
    public TopLevelAnalyzer getTopLevelAnalyzer() {
        return myTopLevelAnalyzer;
    }

    /**
     * Returns the <code>ClassesAnalyzer</code>.
     * @return The <code>ClassesAnalyzer</code>.
     */
    public ClassesAnalyzer getClassesAnalyzer() {
        return myClassesAnalyzer;
    }

    /**
     * Returns the <code>StatementsAnalyzer</code>.
     * @return The <code>StatementsAnalyzer</code>.
     */
    public StatementsAnalyzer getStatementsAnalyzer() {
        return myStatementsAnalyzer;
    }

    /**
     * Returns the <code>ExpressionsAnalyzer</code>.
     * @return The <code>ExpressionsAnalyzer</code>.
     */
    public ExpressionsAnalyzer getExpressionsAnalyzer() {
        return myExpressionsAnalyzer;
    }

    /**
     * Analyze the flow of all <code>OrdinaryCompilationUnit</code>s.
     * @param ocus A <code>List</code> of <code>ASTOrdinaryCompilationUnit</code>s.
     */
    public void analyze(List<ASTOrdinaryCompilationUnit> ocus) {
        getTopLevelAnalyzer().analyzeOrdinaryCompilationUnits(ocus);
    }

    /**
     * Returns the <code>List</code> of <code>CompilerMessage</code>s.
     * @return The <code>List</code> of <code>CompilerMessage</code>s.
     */
    public List<CompilerMessage> getCompilerMessages() {
        return myMsgProducer.getCompilerMessages();
    }
}
