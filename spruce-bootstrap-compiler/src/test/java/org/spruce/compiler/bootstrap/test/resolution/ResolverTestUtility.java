package org.spruce.compiler.bootstrap.test.resolution;

import java.util.ArrayList;
import java.util.List;

import org.spruce.compiler.bootstrap.ast.toplevel.ASTOrdinaryCompilationUnit;
import org.spruce.compiler.bootstrap.common.BaseMessageProducer;
import org.spruce.compiler.bootstrap.common.CompilerMessage;
import org.spruce.compiler.bootstrap.parser.TopLevelParser;
import org.spruce.compiler.bootstrap.resolution.BasicResolver;
import org.spruce.compiler.bootstrap.symbol.SymbolCreator;
import org.spruce.compiler.bootstrap.symbol.SymbolTable;
import org.spruce.compiler.bootstrap.symbol.TypeLookup;
import org.spruce.compiler.bootstrap.test.parser.ParserTopLevelTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Utility methods for semantic Resolver tests.  No test entry points.
 */
public class ResolverTestUtility {

    /**
     * Helper method to parse a bunch of code units at once.
     * @param codeUnits A <code>List</code> of strings, each representing code
     *                  for one compilation unit.
     * @return A <code>List</code> of <code>ASTOrdinaryCompilationUnit</code>s.
     */
    static List<ASTOrdinaryCompilationUnit> parseCodes(List<String> codeUnits) {
        List<ASTOrdinaryCompilationUnit> ocus = new ArrayList<>(codeUnits.size());
        for (String codeUnit : codeUnits) {
            TopLevelParser parser = ParserTopLevelTest.getTopLevelParser(codeUnit);
            ocus.add(parser.parseOrdinaryCompilationUnit());
            assertEquals(0, parser.getCompilerMessages().size());
        }
        return ocus;
    }

    /**
     * Helper method to create the global symbol table using the given
     * <code>OrdinaryCompilationUnit</code>s.
     * @param ocus A <code>List</code> of <code>ASTOrdinaryCompilationUnit</code>s.
     * @return A <code>TypeLookup</code> representing the global symbol table.
     */
    static TypeLookup createGlobalSymbolTable(List<ASTOrdinaryCompilationUnit> ocus) {
        SymbolCreator creator = new SymbolCreator(new BaseMessageProducer(), new TypeLookup());
        for (ASTOrdinaryCompilationUnit ocu : ocus) {
            creator.createSymbolTableForOcu(ocu);
        }
        return creator.getTypeLookup();
    }

    /**
     * Prints any compiler messages.  Ensures that there are no compiler
     * messages representing an error.
     * @param table A <code>SymbolTable</code>.
     * @param Resolver A <code>BasicResolver</code>.
     */
    static void ensureNoErrors(SymbolTable table, BasicResolver Resolver) {
        System.out.println(table);
        long errorCount = generalCheckForError(Resolver);
        if (errorCount != 0) {
            fail("Error message(s) found!");
        }
    }

    /**
     * Prints any compiler messages.  Ensures that there is exactly one
     * compiler message representing an error.
     * @param table A <code>SymbolTable</code>.
     * @param Resolver A <code>BasicResolver</code>.
     */
    static void expectError(SymbolTable table, BasicResolver Resolver) {
        expectError(table, Resolver, 1);
    }

    /**
     * Prints any compiler messages.  Ensures that there is exactly the
     * specified number of compiler messages representing an error.
     * @param table A <code>SymbolTable</code>.
     * @param Resolver A <code>BasicResolver</code>.
     */
    static void expectError(SymbolTable table, BasicResolver Resolver, int count) {
        System.out.println(table);
        long errorCount = generalCheckForError(Resolver);
        if (errorCount != count) {
            fail("Expected " + count + " message(s), got " + errorCount + "!");
        }
    }

    private static long generalCheckForError(BasicResolver Resolver) {
        List<CompilerMessage> msgs = Resolver.getCompilerMessages();
        for (CompilerMessage msg : msgs) {
            System.out.println(msg);
        }
        return msgs.stream()
                .filter(cm -> cm.getLevel() == CompilerMessage.Level.ERROR)
                .count();
    }
}
