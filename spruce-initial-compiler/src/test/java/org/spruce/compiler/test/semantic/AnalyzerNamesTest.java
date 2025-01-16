package org.spruce.compiler.test.semantic;

import java.util.Arrays;

import org.spruce.compiler.ast.names.ASTNamespaceName;
import org.spruce.compiler.common.BaseMessageProducer;
import org.spruce.compiler.parser.NamesParser;
import org.spruce.compiler.semantic.NamesAnalyzer;
import org.spruce.compiler.semantic.SemanticAnalyzer;
import org.spruce.compiler.symbol.ChildSymbolTable;
import org.spruce.compiler.symbol.ParentSymbol;
import org.spruce.compiler.symbol.Symbol;
import org.spruce.compiler.symbol.TopLevelSymbolTable;
import org.spruce.compiler.test.parser.ParserNamesTest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.spruce.compiler.symbol.SymbolTable.Scope.*;
import static org.spruce.compiler.test.semantic.AnalyzerTestUtility.*;
import static org.spruce.compiler.test.util.TestUtility.ensureIsa;

/**
 * All tests for the semantic analyzer related to name symbols.
 */
public class AnalyzerNamesTest {
    /**
     * Tests namespace name symbols.
     */
    @Test
    public void testNamespaceName() {
        NamesParser parser = ParserNamesTest.getNamesParser("""
                spruce.collections.concurrent;
                """);
        ASTNamespaceName namespaceName = parser.parseNamespaceName();

        TopLevelSymbolTable topLevel = new TopLevelSymbolTable();
        NamesAnalyzer analyzer = getNamesAnalyzer();
        Symbol symbol = analyzer.analyzeNamespaceName(namespaceName, topLevel);
        ensureNoErrors(symbol, analyzer);

        checkSymbol(symbol, "spruce", 0, 1);

        ChildSymbolTable child = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(child, NAMESPACE, 1, Arrays.asList("collections"));

        symbol = child.get("collections");
        checkSymbol(symbol, "collections", 0, 1);

        child = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(child, NAMESPACE, 1, Arrays.asList("concurrent"));

        symbol = child.get("concurrent");
        checkSymbol(symbol, "concurrent", 0, 0);
    }

    /**
     * Helper method to get a <code>NamesAnalyzer</code>.
     * @return A <code>NamesAnalyzer</code>.
     */
    public static NamesAnalyzer getNamesAnalyzer() {
        return new SemanticAnalyzer(new BaseMessageProducer()).getNamesAnalyzer();
    }
}
