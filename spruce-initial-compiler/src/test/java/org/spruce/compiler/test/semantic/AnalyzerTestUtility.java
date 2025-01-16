package org.spruce.compiler.test.semantic;

import java.util.List;

import org.spruce.compiler.common.CompilerMessage;
import org.spruce.compiler.semantic.BasicAnalyzer;
import org.spruce.compiler.symbol.ParentSymbol;
import org.spruce.compiler.symbol.Symbol;
import org.spruce.compiler.symbol.SymbolTable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Utility methods for analyzer tests.  No test entry points.
 */
public class AnalyzerTestUtility {
    /**
     * Prints the symbol table.  Prints any compiler messages.  Ensures that there are
     * no compiler messages representing an error.
     * @param table A <code>SymbolTable</code>.
     * @param analyzer A <code>BasicAnalyzer</code>.
     */
    static void ensureNoErrors(SymbolTable table, BasicAnalyzer analyzer) {
        System.out.println(table);
        long errorCount = generalCheckForError(analyzer);
        if (errorCount != 0) {
            fail("Error message(s) found!");
        }
    }

    /**
     * Prints the symbol table.  Prints any compiler messages.  Ensures that there is
     * exactly one compiler message representing an error.
     * @param table A <code>SymbolTable</code>.
     * @param analyzer A <code>BasicAnalyzer</code>.
     */
    static void expectError(SymbolTable table, BasicAnalyzer analyzer) {
        expectError(table, analyzer, 1);
    }

    /**
     * Prints the symbol table.  Prints any compiler messages.  Ensures that there is
     * exactly the specified number of compiler messages representing an error.
     * @param symbol A <code>Symbol</code>.
     * @param analyzer A <code>BasicAnalyzer</code>.
     */
    static void expectError(Symbol symbol, BasicAnalyzer analyzer, int count) {
        System.out.println(symbol);
        long errorCount = generalCheckForError(analyzer);
        if (errorCount != count) {
            fail("Expected " + count + " message(s), got " + errorCount + "!");
        }
    }

    /**
     * Prints the symbol table.  Prints any compiler messages.  Ensures that there are
     * no compiler messages representing an error.
     * @param symbol A <code>Symbol</code>.
     * @param analyzer A <code>BasicAnalyzer</code>.
     */
    static void ensureNoErrors(Symbol symbol, BasicAnalyzer analyzer) {
        System.out.println(symbol);
        long errorCount = generalCheckForError(analyzer);
        if (errorCount != 0) {
            fail("Error message(s) found!");
        }
    }

    /**
     * Prints the symbol table.  Prints any compiler messages.  Ensures that there is
     * exactly one compiler message representing an error.
     * @param symbol A <code>Symbol</code>.
     * @param analyzer A <code>BasicAnalyzer</code>.
     */
    static void expectError(Symbol symbol, BasicAnalyzer analyzer) {
        expectError(symbol, analyzer, 1);
    }

    /**
     * Prints the symbol table.  Prints any compiler messages.  Ensures that there is
     * exactly the specified number of compiler messages representing an error.
     * @param table A <code>SymbolTable</code>.
     * @param analyzer A <code>BasicAnalyzer</code>.
     */
    static void expectError(SymbolTable table, BasicAnalyzer analyzer, int count) {
        System.out.println(table);
        long errorCount = generalCheckForError(analyzer);
        if (errorCount != count) {
            fail("Expected " + count + " message(s), got " + errorCount + "!");
        }
    }

    private static long generalCheckForError(BasicAnalyzer analyzer) {
        List<CompilerMessage> msgs = analyzer.getCompilerMessages();
        for (CompilerMessage msg : msgs) {
            System.out.println(msg);
        }
        return msgs.stream()
                .filter(cm -> cm.getLevel() == CompilerMessage.Level.ERROR)
                .count();
    }

    /**
     * Checks a <code>Symbol</code> to test if it has the expected name,
     * exactly the expected flags value, and the expected number of children.
     * If not, fails the test.
     * @param symbol The <code>Symbol</code> to test.
     * @param expName The expected name.
     * @param expFlags The expected flags, exactly.
     */
    static void checkSymbol(Symbol symbol, String expName, long expFlags, int numExpChildren) {
        assertEquals(expName, symbol.getName());
        assertEquals(expFlags, symbol.getFlags());
        switch (symbol) {
            case ParentSymbol ps -> assertEquals(numExpChildren, ps.getTable().size());
            case Symbol s -> assertEquals(numExpChildren, 0);
        }
    }

    /**
     * Checks a <code>SymbolTable</code> to test if it has the expected
     * <code>Scope</code>, the expected number of symbol entries, and that all
     * the given symbol names are present.
     * @param table The <code>SymbolTable</code> to test.
     * @param expScope The expected <code>Scope</code>.
     * @param numEntries The expected number of entries.
     * @param expSymbolNames A <code>List</code> of expected symbol names, all
     *                       of which must be present.
     */
    static void checkSymbolTable(SymbolTable table, SymbolTable.Scope expScope, int numEntries,
                                 List<String> expSymbolNames) {
        assertEquals(expScope, table.getScope());
        assertEquals(numEntries, table.size());
        for (String expSymbolName : expSymbolNames) {
            assertTrue(table.containsSymbolName(expSymbolName));
        }
    }

    /**
     * Checks a <code>SymbolTable</code> to test if it has the expected
     * <code>Scope</code> and has no symbol entries.
     * @param table The <code>SymbolTable</code> to test.
     * @param expScope The expected <code>Scope</code>.
     */
    static void checkSymbolTable(SymbolTable table, SymbolTable.Scope expScope) {
        assertEquals(expScope, table.getScope());
        assertEquals(0, table.size());
    }
}
