package org.spruce.compiler.symbol;

import org.spruce.compiler.common.MessageProducer;

/**
 * An <code>ExpressionsSymbolCreator</code> is a <code>BasicSymbolCreator</code>
 * that creates symbols belonging to expression level AST elements.
 */
public class ExpressionsSymbolCreator extends BasicSymbolCreator {
    /**
     * Constructs a <code>ExpressionsSymbolCreator</code>.
     * @param symbolCreator A <code>SymbolCreator</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     */
    public ExpressionsSymbolCreator(SymbolCreator symbolCreator, MessageProducer msgProducer) {
        super(symbolCreator, msgProducer);
    }


}
