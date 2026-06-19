package org.spruce.compiler.bootstrap.test.symbol;

import java.util.List;
import java.util.Map;

import org.spruce.compiler.bootstrap.common.CompilerMessage;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.resolution.ResolutionContext;
import org.spruce.compiler.bootstrap.symbol.BasicSymbolCreator;
import org.spruce.compiler.bootstrap.symbol.ChildSymbolTable;
import org.spruce.compiler.bootstrap.symbol.GlobalLookup;
import org.spruce.compiler.bootstrap.symbol.ParameterizedSymbol;
import org.spruce.compiler.bootstrap.symbol.ParentSymbol;
import org.spruce.compiler.bootstrap.symbol.Symbol;
import org.spruce.compiler.bootstrap.symbol.SymbolTable;

import static org.junit.jupiter.api.Assertions.*;
import static org.spruce.compiler.bootstrap.symbol.GlobalLookup.UNNAMED_NAMESPACE_NAME;
import static org.spruce.compiler.bootstrap.symbol.Symbol.FLAG_NONE;

/**
 * Utility methods for symbol creator tests.  No test entry points.
 */
public class SymbolCreatorTestUtility {
    /**
     * Prints the symbol table.  Prints any compiler messages.  Ensures that there are
     * no compiler messages representing an error.
     * @param table A <code>SymbolTable</code>.
     * @param creator A <code>BasicSymbolCreator</code>.
     */
    static void ensureNoErrors(SymbolTable table, BasicSymbolCreator creator) {
        System.out.println(table);
        long errorCount = generalCheckForError(creator);
        if (errorCount != 0) {
            fail("Error message(s) found!");
        }
    }

    /**
     * Prints the symbol table.  Prints any compiler messages.  Ensures that there is
     * exactly one compiler message representing an error.
     * @param table A <code>SymbolTable</code>.
     * @param creator A <code>BasicSymbolCreator</code>.
     */
    static void expectError(SymbolTable table, BasicSymbolCreator creator) {
        expectError(table, creator, 1);
    }

    /**
     * Prints the symbol table.  Prints any compiler messages.  Ensures that there is
     * exactly the specified number of compiler messages representing an error.
     * @param table A <code>SymbolTable</code>.
     * @param creator A <code>BasicSymbolCreator</code>.
     */
    static void expectError(SymbolTable table, BasicSymbolCreator creator, int count) {
        System.out.println(table);
        long errorCount = generalCheckForError(creator);
        if (errorCount != count) {
            fail("Expected " + count + " message(s), got " + errorCount + "!");
        }
    }

    /**
     * Prints the symbol.  Prints any compiler messages.  Ensures that there is
     * exactly one compiler message representing an error.
     * @param symbol A <code>Symbol</code>.
     * @param creator A <code>BasicSymbolCreator</code>.
     */
    static void expectError(Symbol symbol, BasicSymbolCreator creator) {
        expectError(symbol, creator, 1);
    }

    /**
     * Prints the symbol.  Prints any compiler messages.  Ensures that there is
     * exactly the specified number of compiler messages representing an error.
     * @param symbol A <code>Symbol</code>.
     * @param creator A <code>BasicSymbolCreator</code>.
     */
    static void expectError(Symbol symbol, BasicSymbolCreator creator, int count) {
        System.out.println(symbol);
        long errorCount = generalCheckForError(creator);
        if (errorCount != count) {
            fail("Expected " + count + " message(s), got " + errorCount + "!");
        }
    }

    private static long generalCheckForError(BasicSymbolCreator creator) {
        List<CompilerMessage> msgs = creator.getCompilerMessages();
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
     * @param expKind The expected <code>Kind</code>.
     * @param expFlags The expected flags, exactly.
     */
    public static void checkSymbol(Symbol symbol, String expName, Symbol.Kind expKind, long expFlags) {
        assertEquals(expName, symbol.getName());
        assertEquals(expKind, symbol.getKind());
        assertEquals(expFlags, symbol.getFlags());
    }

    /**
     * Checks a <code>Symbol</code> to test if it has the expected name,
     * exactly the expected flags value, and the expected number of children.
     * If not, fails the test.
     * @param symbol The <code>Symbol</code> to test.
     * @param expName The expected name.
     * @param expKind The expected <code>Kind</code>.
     * @param expFlags The expected flags, exactly.
     * @param numExpChildren The number of expected children.
     */
    public static void checkSymbol(ParentSymbol symbol, String expName, Symbol.Kind expKind, long expFlags, int numExpChildren) {
        checkSymbol(symbol, expName, expKind, expFlags);
        assertEquals(numExpChildren, symbol.getTable().size());
    }

    /**
     * Checks a <code>Symbol</code> to test if it has the expected name,
     * exactly the expected flags value, and the expected number of children.
     * If not, fails the test.
     * @param symbol The <code>Symbol</code> to test.
     * @param expName The expected name.
     * @param expKind The expected <code>Kind</code>.
     * @param expFlags The expected flags, exactly.
     * @param numExpChildren The number of expected children.
     * @param numExpParameters The number of expected parameters.
     */
    public static void checkSymbol(ParameterizedSymbol symbol, String expName, Symbol.Kind expKind,
                            long expFlags, int numExpChildren, int numExpParameters) {
        checkSymbol(symbol, expName, expKind, expFlags, numExpChildren);
        assertEquals(numExpParameters, symbol.numParameters());
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
    public static void checkSymbolTable(SymbolTable table, SymbolTable.Scope expScope, int numEntries,
                                 List<String> expSymbolNames) {
        assertEquals(expScope, table.getScope());
        assertEquals(numEntries, table.size());
        for (String expSymbolName : expSymbolNames) {
            assertTrue(table.containsSymbolName(expSymbolName),
                    "Didn't find expected symbol name \"" + expSymbolName + "\".");
        }
    }

    /**
     * Creates a fake <code>ResolutionContext</code> to pass in to
     * TypesSymbolCreator tests.
     * @return A fake <code>ResolutionContext</code>.
     */
    static ResolutionContext createFakeResolutionContext() {
        GlobalLookup global = new GlobalLookup();
        ParentSymbol unnamedNamespace = global.getNamespace(UNNAMED_NAMESPACE_NAME).get();
        ParentSymbol enclosingType = new ParentSymbol(
                new Location("<dummy>", 0, 0, "unavailable"),
                "TestType", Symbol.Kind.CLASS, unnamedNamespace.getTable(), FLAG_NONE);
        ChildSymbolTable table = new ChildSymbolTable(SymbolTable.Scope.TYPE, enclosingType);
        enclosingType.setTable(table);
        return new ResolutionContext(Map.of(), enclosingType);
    }
}


