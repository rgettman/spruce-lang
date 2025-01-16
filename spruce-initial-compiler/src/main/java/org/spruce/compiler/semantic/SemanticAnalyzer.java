package org.spruce.compiler.semantic;

import java.util.List;

import org.spruce.compiler.ast.toplevel.ASTOrdinaryCompilationUnit;
import org.spruce.compiler.common.MessageProducer;
import org.spruce.compiler.common.CompilerMessage;
import org.spruce.compiler.symbol.TopLevelSymbolTable;

/**
 * A <code>SemanticAnalyzer</code> builds a <code>SymbolTable</code> using an
 * abstract syntax tree with an <code>ASTOrdinaryCompilationUnit</code> as the
 * root.
 */
public class SemanticAnalyzer {
    private final NamesAnalyzer myNamesAnalyzer;
    private final TopLevelAnalyzer myTopLevelAnalyzer;
    private final MessageProducer myMsgProducer;

    /**
     * Constructs a <code>SemanticAnalyzer</code> given a <code>MessageProducer</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     */
    public SemanticAnalyzer(MessageProducer msgProducer) {
        myNamesAnalyzer = new NamesAnalyzer(this, msgProducer);
        myTopLevelAnalyzer = new TopLevelAnalyzer(this, msgProducer);

        myMsgProducer = msgProducer;
    }

    /**
     * Returns the <code>NamesAnalyzer</code>.
     * @return The <code>NamesAnalyzer</code>.
     */
    public NamesAnalyzer getNamesAnalyzer() {
        return myNamesAnalyzer;
    }

    /**
     * Returns the <code>TopLevelAnalyzer</code>.
     * @return The <code>TopLevelAnalyzer</code>.
     */
    public TopLevelAnalyzer getTopLevelAnalyzer() {
        return myTopLevelAnalyzer;
    }

    /**
     * Creates a <code>TopLevelSymbolTable</code> and populates it with data
     * from the given <code>ASTOrdinaryCompilationUnit</code>.
     * @param ocu An <code>ASTOrdinaryCompilationUnit</code>.
     * @return A <code>TopLevelSymbolTable</code>.
     */
    public TopLevelSymbolTable createSymbolTable(ASTOrdinaryCompilationUnit ocu) {
        TopLevelSymbolTable topLevel = new TopLevelSymbolTable();
        getTopLevelAnalyzer().analyzeCompUnit(ocu, topLevel);
        return topLevel;
    }

    /**
     * Returns the <code>List</code> of <code>CompilerMessage</code>s.
     * @return The <code>List</code> of <code>CompilerMessage</code>s.
     */
    public List<CompilerMessage> getCompilerMessages() {
        return myMsgProducer.getCompilerMessages();
    }
}
