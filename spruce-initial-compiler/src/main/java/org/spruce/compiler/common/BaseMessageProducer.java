package org.spruce.compiler.common;

import java.util.ArrayList;
import java.util.List;

import static org.spruce.compiler.common.CompilerMessage.Level.*;

/**
 * A <code>BaseMessageProducer</code> is a <code>MessageProducer</code> that
 * stores them for later retrieval.
 */
public class BaseMessageProducer implements MessageProducer {
    private final List<CompilerMessage> myMessages;

    /**
     * Constructs a <code>BaseMessageProducer</code>.
     */
    public BaseMessageProducer() {
        myMessages = new ArrayList<>();
    }

    @Override
    public void error(Location loc, String msg) {
        addMessage(loc, msg, ERROR);
    }

    @Override
    public void warning(Location loc, String msg) {
        addMessage(loc, msg, WARNING);
    }

    @Override
    public void note(Location loc, String msg) {
        addMessage(loc, msg, NOTE);
    }

    @Override
    public List<CompilerMessage> getCompilerMessages() {
        return myMessages;
    }

    @Override
    public void addCompilerMessage(CompilerMessage cm) {
        myMessages.add(cm);
    }

    private void addMessage(Location loc, String msg, CompilerMessage.Level level) {
        CompilerMessage cm = new CompilerMessage(loc, level, msg);
        myMessages.add(cm);
    }
}
