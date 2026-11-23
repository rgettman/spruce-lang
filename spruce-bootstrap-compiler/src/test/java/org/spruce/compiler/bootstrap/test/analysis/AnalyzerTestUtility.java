package org.spruce.compiler.bootstrap.test.analysis;

import java.util.List;

import org.spruce.compiler.bootstrap.analysis.Analyzer;
import org.spruce.compiler.bootstrap.analysis.BasicAnalyzer;
import org.spruce.compiler.bootstrap.analysis.TopLevelAnalyzer;
import org.spruce.compiler.bootstrap.ast.toplevel.ASTOrdinaryCompilationUnit;
import org.spruce.compiler.bootstrap.common.BaseMessageProducer;
import org.spruce.compiler.bootstrap.common.CompilerMessage;
import org.spruce.compiler.bootstrap.resolution.Resolver;
import org.spruce.compiler.bootstrap.resolution.TopLevelResolver;
import org.spruce.compiler.bootstrap.symbol.GlobalLookup;
import org.spruce.compiler.bootstrap.test.resolution.ResolverTestUtility;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Utility methods for analyzer tests.  No test entry points.
 */
public class AnalyzerTestUtility {

    /**
     * Helper method to parse all codes, create the symbols, resolve all
     * symbols, and analyze all <code>ASTOrdinaryCompilationUnit</code>s.
     * Expect no errors prior to any analysis.
     * @param codes A <code>List</code> of string codes, one per compilation unit.
     * @return A <code>Pair</code> consisting of a <code>List</code> of
     *     <code>ASTOrdinaryCompilationUnit</code>s and a <code>TopLevelAnalyzer</code>.
     */
    static Pair compileSoFar(List<String> codes) {
        List<ASTOrdinaryCompilationUnit> ocus = ResolverTestUtility.parseCodes(codes);
        GlobalLookup global = ResolverTestUtility.createGlobalSymbolTable(ocus, 0);
        TopLevelResolver resolver = new Resolver(new BaseMessageProducer(), global).getTopLevelResolver();
        resolver.resolveOrdinaryCompilationUnits(ocus);
        TopLevelAnalyzer analyzer = new Analyzer(new BaseMessageProducer()).getTopLevelAnalyzer();
        analyzer.analyzeOrdinaryCompilationUnits(ocus);
        return new Pair(ocus, analyzer);
    }

    /**
     * Prints any compiler messages.  Ensures that there are no compiler
     * messages representing an error.
     * @param analyzer A <code>BasicAnalyzer</code>.
     */
    static void ensureNoErrors(BasicAnalyzer analyzer) {
        long errorCount = generalCheckForError(analyzer);
        if (errorCount != 0) {
            fail("Error message(s) found!");
        }
    }

    /**
     * Prints any compiler messages.  Ensures that there is exactly one
     * compiler message representing an error.
     * @param analyzer A <code>BasicAnalyzer</code>.
     */
    static void expectError(BasicAnalyzer analyzer) {
        expectError(analyzer, 1);
    }

    /**
     * Prints any compiler messages.  Ensures that there is exactly the
     * specified number of compiler messages representing an error.
     * @param analyzer A <code>BasicAnalyzer</code>.
     */
    static void expectError(BasicAnalyzer analyzer, int count) {
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

    public record Pair(List<ASTOrdinaryCompilationUnit> ocus, TopLevelAnalyzer analyzer) {
    }
}
