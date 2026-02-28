package org.spruce.compiler.bootstrap.parser;

import java.util.List;

import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.common.CompilerMessage;
import org.spruce.compiler.bootstrap.scanner.Scanner;

/**
 * A <code>Parser</code> is the entry point for parsing Spruce code.  It
 * maintains subclasses of <code>BasicParser</code> that parse the actual
 * productions, each of which have their own reference to the <code>Scanner</code>.
 * They reference other <code>BasicParser</code> subclass instances through
 * this object.
 */
public class Parser {
    private final LiteralsParser myLiteralsParser;
    private final NamesParser myNamesParser;
    private final TypesParser myTypesParser;
    private final ExpressionsParser myExpressionsParser;
    private final StatementsParser myStatementsParser;
    private final ClassesParser myClassesParser;
    private final TopLevelParser myTopLevelParser;
    private final MessageProducer myMsgProducer;

    /**
     * Constructs a <code>Parser</code> given a <code>Scanner</code>.
     * @param scanner A <code>Scanner</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     */
    public Parser(Scanner scanner, MessageProducer msgProducer) {
        myLiteralsParser = new LiteralsParser(scanner, this, msgProducer);
        myNamesParser = new NamesParser(scanner, this, msgProducer);
        myTypesParser = new TypesParser(scanner, this, msgProducer);
        myExpressionsParser = new ExpressionsParser(scanner, this, msgProducer);
        myStatementsParser = new StatementsParser(scanner, this, msgProducer);
        myClassesParser = new ClassesParser(scanner, this, msgProducer);
        myTopLevelParser = new TopLevelParser(scanner, this, msgProducer);

        myMsgProducer = msgProducer;

        scanner.next();
    }

    /**
     * Returns the <code>LiteralsParser</code>.
     * @return The <code>LiteralsParser</code>.
     */
    public LiteralsParser getLiteralsParser() {
        return myLiteralsParser;
    }

    /**
     * Returns the <code>NamesParser</code>.
     * @return The <code>NamesParser</code>.
     */
    public NamesParser getNamesParser() {
        return myNamesParser;
    }

    /**
     * Returns the <code>TypesParser</code>.
     * @return The <code>TypesParser</code>.
     */
    public TypesParser getTypesParser() {
        return myTypesParser;
    }

    /**
     * Returns the <code>ExpressionsParser</code>.
     * @return The <code>ExpressionsParser</code>.
     */
    public ExpressionsParser getExpressionsParser() {
        return myExpressionsParser;
    }

    /**
     * Returns the <code>StatementsParser</code>.
     * @return The <code>StatementsParser</code>.
     */
    public StatementsParser getStatementsParser() {
        return myStatementsParser;
    }

    /**
     * Returns the <code>ClassesParser</code>.
     * @return The <code>ClassesParser</code>.
     */
    public ClassesParser getClassesParser() {
        return myClassesParser;
    }

    /**
     * Returns the <code>TopLevelParser</code>.
     * @return The <code>TopLevelParser</code>.
     */
    public TopLevelParser getTopLevelParser() {
        return myTopLevelParser;
    }

    /**
     * Returns the <code>List</code> of <code>CompilerMessage</code>s.
     * @return The <code>List</code> of <code>CompilerMessage</code>s.
     */
    public List<CompilerMessage> getCompilerMessages() {
        return myMsgProducer.getCompilerMessages();
    }
}
