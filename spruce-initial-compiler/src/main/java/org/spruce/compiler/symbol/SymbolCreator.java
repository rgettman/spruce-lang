package org.spruce.compiler.symbol;

import java.util.List;

import org.spruce.compiler.ast.toplevel.ASTOrdinaryCompilationUnit;
import org.spruce.compiler.common.CompilerMessage;

import org.spruce.compiler.common.MessageProducer;

/**
 * A <code>SymbolCreator</code> creates a <code>TopLevelSymbolTable</code> that
 * contains all declared symbols within a Compilation Unit.
 */
public class SymbolCreator {
    private final ClassesSymbolCreator myClassesSymbolCreator;
    private final TopLevelSymbolCreator myTopLevelSymbolCreator;
    private final TypesSymbolCreator myTypesSymbolCreator;
    private final MessageProducer myMsgProducer;

    /**
     * Constructs a <code>SymbolCreator</code> with the given
     * <code>MessageProducer</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     */
    public SymbolCreator(MessageProducer msgProducer) {
        myClassesSymbolCreator = new ClassesSymbolCreator(this, msgProducer);
        myTopLevelSymbolCreator = new TopLevelSymbolCreator(this, msgProducer);
        myTypesSymbolCreator = new TypesSymbolCreator(this, msgProducer);

        myMsgProducer = msgProducer;
    }

    /**
     * Returns the <code>ClassesSymbolCreator</code>.
     * @return The <code>ClassesSymbolCreator</code>.
     */
    public ClassesSymbolCreator getClassesSymbolCreator() {
        return myClassesSymbolCreator;
    }

    /**
     * Returns the <code>TopLevelSymbolCreator</code>.
     * @return The <code>TopLevelSymbolCreator</code>.
     */
    public TopLevelSymbolCreator getTopLevelSymbolCreator() {
        return myTopLevelSymbolCreator;
    }

    /**
     * Returns the <code>TypesSymbolCreator</code>.
     * @return The <code>TypesSymbolCreator</code>.
     */
    public TypesSymbolCreator getTypesSymbolCreator() {
        return myTypesSymbolCreator;
    }

    /**
     * Creates a <code>TopLevelSymbolTable</code> from the given
     * <code>ASTOrdinaryCompilationUnit</code>.  Populates the symbol table
     * hierarchically with all declared symbols found within the compilation
     * unit.
     * @param ocu An <code>ASTOrdinaryCompilationUnit</code>.
     * @return A <code>TopLevelSymbolTable</code>.
     */
    public TopLevelSymbolTable createSymbolsFrom(ASTOrdinaryCompilationUnit ocu) {
        TopLevelSymbolTable topLevel = new TopLevelSymbolTable();
        getTopLevelSymbolCreator().createSymbolsForCompUnit(ocu, topLevel);
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
