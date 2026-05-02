package org.spruce.compiler.bootstrap.symbol;

import java.util.List;

import org.spruce.compiler.bootstrap.ast.toplevel.ASTOrdinaryCompilationUnit;
import org.spruce.compiler.bootstrap.common.CompilerMessage;
import org.spruce.compiler.bootstrap.common.MessageProducer;

/**
 * A <code>SymbolCreator</code> creates <code>Symbol</code>s for all
 * declarations being compiled - namespaces, types, type members, parameters,
 * and local variables.  Populates a global <code>TypeLookup</code> for all
 * namespaces and types for use by a semantic Resolver.
 */
public class SymbolCreator {
    private final ClassesSymbolCreator myClassesSymbolCreator;
    private final StatementsSymbolCreator myStatementsSymbolCreator;
    private final TopLevelSymbolCreator myTopLevelSymbolCreator;
    private final TypesSymbolCreator myTypesSymbolCreator;

    private final MessageProducer myMsgProducer;
    private final TypeLookup myTypeLookup;

    /**
     * Constructs a <code>SymbolCreator</code> with the given
     * <code>MessageProducer</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     */
    public SymbolCreator(MessageProducer msgProducer, TypeLookup typeLookup) {
        myClassesSymbolCreator = new ClassesSymbolCreator(this, msgProducer, typeLookup);
        myStatementsSymbolCreator = new StatementsSymbolCreator(this, msgProducer, typeLookup);
        myTopLevelSymbolCreator = new TopLevelSymbolCreator(this, msgProducer, typeLookup);
        myTypesSymbolCreator = new TypesSymbolCreator(this, msgProducer, typeLookup);

        myMsgProducer = msgProducer;
        myTypeLookup = typeLookup;
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
     * Creates symbols for the given <code>ASTOrdinaryCompilationUnit</code>.
     * Populates symbols hierarchically with all declared symbols found within
     * the compilation unit.
     * @param ocu An <code>ASTOrdinaryCompilationUnit</code>.
     */
    public void createSymbolTableForOcu(ASTOrdinaryCompilationUnit ocu) {
        getTopLevelSymbolCreator().createSymbolTableForCompUnit(ocu);
    }

    /**
     * Returns the <code>List</code> of <code>CompilerMessage</code>s.
     * @return The <code>List</code> of <code>CompilerMessage</code>s.
     */
    public List<CompilerMessage> getCompilerMessages() {
        return myMsgProducer.getCompilerMessages();
    }

    /**
     * Returns the <code>TypeLookup</code>.
     * @return The <code>TypeLookup</code>.
     */
    public TypeLookup getTypeLookup() {
        return myTypeLookup;
    }
}
