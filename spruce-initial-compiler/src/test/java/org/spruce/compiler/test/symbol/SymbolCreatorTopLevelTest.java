package org.spruce.compiler.test.symbol;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.ast.names.ASTNamespaceName;
import org.spruce.compiler.ast.toplevel.ASTOrdinaryCompilationUnit;
import org.spruce.compiler.common.BaseMessageProducer;
import org.spruce.compiler.parser.NamesParser;
import org.spruce.compiler.parser.TopLevelParser;
import org.spruce.compiler.symbol.SymbolCreator;
import org.spruce.compiler.symbol.ChildSymbolTable;
import org.spruce.compiler.symbol.ParentSymbol;
import org.spruce.compiler.symbol.Symbol;
import org.spruce.compiler.symbol.SymbolTable;
import org.spruce.compiler.symbol.TopLevelSymbolCreator;
import org.spruce.compiler.symbol.TopLevelSymbolTable;
import org.spruce.compiler.test.parser.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.spruce.compiler.symbol.SymbolTable.Scope.*;
import static org.spruce.compiler.test.symbol.SymbolCreatorTestUtility.*;
import static org.spruce.compiler.test.util.TestUtility.ensureIsa;

/**
 * All tests for the top level symbol creator.
 */
public class SymbolCreatorTopLevelTest {
    /**
     * Tests ordinary compilation unit of only a namespace.
     */
    @Test
    public void testOcuNamespaceOnly() {
        TopLevelParser parser = ParserTopLevelTest.getTopLevelParser("namespace one.two.three;");
        ASTOrdinaryCompilationUnit ocu = parser.parseOrdinaryCompilationUnit();
        SymbolCreator creator = new SymbolCreator(new BaseMessageProducer());
        TopLevelSymbolTable topLevel = creator.createSymbolsFrom(ocu);
        ensureNoErrors(topLevel, creator.getTopLevelSymbolCreator());

        checkSymbolTable(topLevel, TOP);

        assertTrue(topLevel.getNamespace().isPresent());
        SymbolTable namespace = topLevel.getNamespace().get();
        checkSymbolTable(namespace, NAMESPACE, 1, Arrays.asList("one"));
    }

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
        TopLevelSymbolCreator creator = getTopLevelSymbolCreator();
        Symbol symbol = creator.createSymbolsForNamespaceName(namespaceName, topLevel);
        ensureNoErrors(symbol, creator);

        checkSymbol(symbol, "spruce", Symbol.Type.NAMESPACE, 0, 1);

        ChildSymbolTable child = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(child, NAMESPACE, 1, Arrays.asList("collections"));

        Symbol base = child.get("collections");
        checkSymbol(ensureIsa(base, Symbol.class), "collections", Symbol.Type.NAMESPACE, 0, 1);

        child = ensureIsa(base, ParentSymbol.class).getTable();
        checkSymbolTable(child, NAMESPACE, 1, Arrays.asList("concurrent"));

        base = child.get("concurrent");
        checkSymbol(ensureIsa(base, Symbol.class), "concurrent", Symbol.Type.NAMESPACE, 0, 0);
    }

    /**
     * Tests ordinary compilation unit of only type declarations.
     */
    @Test
    public void testOcuTypeDeclarationListOnly() {
        TopLevelParser parser = ParserTopLevelTest.getTopLevelParser("""
                class TestClass {}
                interface TestInterface {}
                annotation TestAnnotation {}
                enum TestEnum {FOO}
                record TestRecord(String foo) {}
                adt TestAdt { Foo(), Bar() }
                """);
        ASTOrdinaryCompilationUnit ocu = parser.parseOrdinaryCompilationUnit();
        SymbolCreator creator = new SymbolCreator(new BaseMessageProducer());
        TopLevelSymbolTable topLevel = creator.createSymbolsFrom(ocu);
        ensureNoErrors(topLevel, creator.getTopLevelSymbolCreator());

        assertFalse(topLevel.getNamespace().isPresent());
        checkSymbolTable(topLevel, TOP, 6,
                Arrays.asList("TestClass", "TestInterface", "TestAnnotation", "TestEnum", "TestRecord", "TestAdt"));
    }

    /**
     * Tests bad classes - same name.
     */
    @Test
    public void testClassesSameName() {
        TopLevelParser parser = ParserTopLevelTest.getTopLevelParser("""
                public class SameName {}
                public class SameName {}
                """);
        ASTOrdinaryCompilationUnit ocu = parser.parseOrdinaryCompilationUnit();
        SymbolCreator creator = new SymbolCreator(new BaseMessageProducer());
        TopLevelSymbolTable topLevel = creator.createSymbolsFrom(ocu);
        expectError(topLevel, creator.getTopLevelSymbolCreator());
    }

    /**
     * Helper method to get a <code>TopLevelSymbolCreator</code>.
     * @return A <code>TopLevelSymbolCreator</code>.
     */
    public static TopLevelSymbolCreator getTopLevelSymbolCreator() {
        return new SymbolCreator(new BaseMessageProducer()).getTopLevelSymbolCreator();
    }
}
