package org.spruce.compiler.parser;

import org.spruce.compiler.scanner.Scanner;

/**
 * A <code>Parser</code> is the entry point for parsing Spruce code.  It
 * maintains subclasses of <code>BasicParser</code> that parse the actual
 * productions, each of which have their own reference to the <code>Scanner</code>.
 * They reference other <code>BasicParser</code> subclass instances through
 * this object.
 */
public class Parser
{
    private final LiteralsParser myLiteralsParser;
    private final NamesParser myNamesParser;
    private final TypesParser myTypesParser;
    private final ExpressionsParser myExpressionsParser;
    private final StatementsParser myStatementsParser;
    private final ClassesParser myClassesParser;
    private final TopLevelParser myTopLevelParser;

    /**
     * Constructs a <code>Parser</code> given a <code>Scanner</code>.
     * @param scanner A <code>Scanner</code>.
     */
    public Parser(Scanner scanner) {
        myLiteralsParser = new LiteralsParser(scanner, this);
        myNamesParser = new NamesParser(scanner, this);
        myTypesParser = new TypesParser(scanner, this);
        myExpressionsParser = new ExpressionsParser(scanner, this);
        myStatementsParser = new StatementsParser(scanner, this);
        myClassesParser = new ClassesParser(scanner, this);
        myTopLevelParser = new TopLevelParser(scanner, this);
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
}
