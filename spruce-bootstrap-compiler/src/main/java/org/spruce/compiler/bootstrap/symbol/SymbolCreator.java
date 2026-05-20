package org.spruce.compiler.bootstrap.symbol;

import java.util.List;

import org.spruce.compiler.bootstrap.ast.toplevel.ASTOrdinaryCompilationUnit;
import org.spruce.compiler.bootstrap.common.CompilerMessage;
import org.spruce.compiler.bootstrap.common.MessageProducer;

/**
 * A <code>SymbolCreator</code> creates <code>Symbol</code>s for all
 * declarations being compiled - namespaces, types, type members, parameters,
 * and local variables.  Populates a <code>GlobalLookup</code> for all
 * namespaces and types for use by a semantic Resolver.
 */
public class SymbolCreator {
    private final ClassesSymbolCreator myClassesSymbolCreator;
    private final StatementsSymbolCreator myStatementsSymbolCreator;
    private final TopLevelSymbolCreator myTopLevelSymbolCreator;
    private final TypesSymbolCreator myTypesSymbolCreator;

    private final MessageProducer myMsgProducer;
    private final GlobalLookup myGlobalLookup;

    /**
     * Constructs a <code>SymbolCreator</code> with the given
     * <code>MessageProducer</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     */
    public SymbolCreator(MessageProducer msgProducer, GlobalLookup globalLookup) {
        myClassesSymbolCreator = new ClassesSymbolCreator(this, msgProducer, globalLookup);
        myStatementsSymbolCreator = new StatementsSymbolCreator(this, msgProducer, globalLookup);
        myTopLevelSymbolCreator = new TopLevelSymbolCreator(this, msgProducer, globalLookup);
        myTypesSymbolCreator = new TypesSymbolCreator(this, msgProducer, globalLookup);

        myMsgProducer = msgProducer;
        myGlobalLookup = globalLookup;
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
     * Returns the <code>GlobalLookup</code>.
     * @return The <code>GlobalLookup</code>.
     */
    public GlobalLookup getGlobalLookup() {
        return myGlobalLookup;
    }
}
