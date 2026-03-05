package org.spruce.compiler.bootstrap.symbol;

import java.util.List;

import org.spruce.compiler.bootstrap.ast.toplevel.ASTOrdinaryCompilationUnit;
import org.spruce.compiler.bootstrap.common.CompilerMessage;
import org.spruce.compiler.bootstrap.common.MessageProducer;

/**
 * A <code>SymbolCreator</code> creates a <code>TopLevelSymbolTable</code> that
 * contains all declared symbols within a Compilation Unit.
 */
public class SymbolCreator {
    private final ClassesSymbolCreator myClassesSymbolCreator;
    private final StatementsSymbolCreator myStatementsSymbolCreator;
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
        myStatementsSymbolCreator = new StatementsSymbolCreator(this, msgProducer);
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
     * Returns the <code>StatementsSymbolCreator</code>.
     * @return The <code>StatementsSymbolCreator</code>.
     */
    public StatementsSymbolCreator getStatementsSymbolCreator() {
        return myStatementsSymbolCreator;
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
    public TopLevelSymbolTable createSymbolTableForOcu(ASTOrdinaryCompilationUnit ocu) {
        return getTopLevelSymbolCreator().createSymbolTableForCompUnit(ocu);
    }

    /**
     * Returns the <code>List</code> of <code>CompilerMessage</code>s.
     * @return The <code>List</code> of <code>CompilerMessage</code>s.
     */
    public List<CompilerMessage> getCompilerMessages() {
        return myMsgProducer.getCompilerMessages();
    }
}
