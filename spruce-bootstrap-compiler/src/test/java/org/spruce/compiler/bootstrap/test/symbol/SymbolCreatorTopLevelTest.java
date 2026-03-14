package org.spruce.compiler.bootstrap.test.symbol;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.bootstrap.ast.toplevel.ASTOrdinaryCompilationUnit;
import org.spruce.compiler.bootstrap.common.BaseMessageProducer;
import org.spruce.compiler.bootstrap.parser.TopLevelParser;
import org.spruce.compiler.bootstrap.symbol.*;
import org.spruce.compiler.bootstrap.test.parser.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.spruce.compiler.bootstrap.symbol.SymbolTable.Scope.*;
import static org.spruce.compiler.bootstrap.test.symbol.SymbolCreatorTestUtility.*;
import static org.spruce.compiler.bootstrap.test.util.TestUtility.ensureIsa;

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
        TopLevelSymbolTable topLevel = creator.createSymbolTableForOcu(ocu);
        ensureNoErrors(topLevel, creator.getTopLevelSymbolCreator());

        checkSymbolTable(topLevel, TOP);

        assertTrue(topLevel.getNamespace().isPresent(), "Namespace expected but not found!");
        SymbolTable namespace = topLevel.getNamespace().get();
        checkSymbolTable(namespace, NAMESPACE, 1, Arrays.asList("one"));
    }

    /**
     * Tests namespace name symbols.
     */
    @Test
    public void testNamespaceName() {
        TopLevelParser parser = ParserTopLevelTest.getTopLevelParser("""
                namespace spruce.collections.concurrent;
                """);
        ASTOrdinaryCompilationUnit ocu = parser.parseOrdinaryCompilationUnit();

        SymbolCreator creator = new SymbolCreator(new BaseMessageProducer());
        TopLevelSymbolTable topLevel = creator.createSymbolTableForOcu(ocu);
        ensureNoErrors(topLevel, creator.getTopLevelSymbolCreator());

        assertTrue(topLevel.getNamespace().isPresent(), "Namespace expected but not found!");
        SymbolTable namespaceTable = topLevel.getNamespace().get();
        checkSymbolTable(namespaceTable, NAMESPACE, 1, List.of("spruce"));

        Symbol spruceSymbol = namespaceTable.get("spruce");
        checkSymbol(ensureIsa(spruceSymbol, ParentSymbol.class), "spruce", Symbol.Type.NAMESPACE,
                0, 1);

        ChildSymbolTable child = ensureIsa(spruceSymbol, ParentSymbol.class).getTable();
        checkSymbolTable(child, NAMESPACE, 1, Arrays.asList("collections"));

        Symbol collectionsSymbol = child.get("collections");
        checkSymbol(ensureIsa(collectionsSymbol, ParentSymbol.class), "collections", Symbol.Type.NAMESPACE,
                0, 1);

        child = ensureIsa(collectionsSymbol, ParentSymbol.class).getTable();
        checkSymbolTable(child, NAMESPACE, 1, Arrays.asList("concurrent"));

        Symbol concurrentSymbol = child.get("concurrent");
        checkSymbol(ensureIsa(concurrentSymbol, ParentSymbol.class), "concurrent", Symbol.Type.NAMESPACE, 0, 0);

        // AST -> Symbol
        assertSame(spruceSymbol, ocu.getNamespaceDecl().get().getNamespace().getDeclSymbol());
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
        TopLevelSymbolTable topLevel = creator.createSymbolTableForOcu(ocu);
        ensureNoErrors(topLevel, creator.getTopLevelSymbolCreator());

        assertFalse(topLevel.getNamespace().isPresent(), "Unexpected namespace found!");
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
        TopLevelSymbolTable topLevel = creator.createSymbolTableForOcu(ocu);
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
