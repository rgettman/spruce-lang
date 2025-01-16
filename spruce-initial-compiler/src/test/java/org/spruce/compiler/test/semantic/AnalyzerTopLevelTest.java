package org.spruce.compiler.test.semantic;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.ast.toplevel.ASTOrdinaryCompilationUnit;
import org.spruce.compiler.common.BaseMessageProducer;
import org.spruce.compiler.parser.TopLevelParser;
import org.spruce.compiler.semantic.SemanticAnalyzer;
import org.spruce.compiler.semantic.TopLevelAnalyzer;
import org.spruce.compiler.symbol.SymbolTable;
import org.spruce.compiler.symbol.TopLevelSymbolTable;
import org.spruce.compiler.test.parser.ParserTopLevelTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.spruce.compiler.symbol.SymbolTable.Scope.*;
import static org.spruce.compiler.test.semantic.AnalyzerTestUtility.*;

/**
 * All tests for the semantic analyzer related to top level symbols.
 */
public class AnalyzerTopLevelTest {
    /**
     * Tests ordinary compilation unit of only a namespace.
     */
    @Test
    public void testOcuNamespaceOnly() {
        TopLevelParser parser = ParserTopLevelTest.getTopLevelParser("namespace one.two.three;");
        ASTOrdinaryCompilationUnit ocu = parser.parseOrdinaryCompilationUnit();

        TopLevelSymbolTable topLevel = new TopLevelSymbolTable();
        TopLevelAnalyzer analyzer = getTopLevelAnalyzer();
        analyzer.analyzeCompUnit(ocu, topLevel);
        ensureNoErrors(topLevel, analyzer);

        checkSymbolTable(topLevel, TOP);

        assertTrue(topLevel.getNamespace().isPresent());
        SymbolTable namespace = topLevel.getNamespace().get();
        checkSymbolTable(namespace, NAMESPACE, 1, Arrays.asList("one"));
    }

    /**
     * Helper method to get a <code>TopLevelAnalyzer</code>.
     * @return A <code>TopLevelAnalyzer</code>.
     */
    public static TopLevelAnalyzer getTopLevelAnalyzer() {
        return new SemanticAnalyzer(new BaseMessageProducer()).getTopLevelAnalyzer();
    }
}
