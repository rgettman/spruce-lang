package org.spruce.compiler.bootstrap.test.symbol;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.bootstrap.ast.toplevel.ASTOrdinaryCompilationUnit;
import org.spruce.compiler.bootstrap.common.BaseMessageProducer;
import org.spruce.compiler.bootstrap.parser.TopLevelParser;
import org.spruce.compiler.bootstrap.symbol.*;
import org.spruce.compiler.bootstrap.test.parser.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.spruce.compiler.bootstrap.symbol.Symbol.FLAG_NONE;
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
        SymbolCreator creator = new SymbolCreator(new BaseMessageProducer(), new TypeLookup());
        creator.createSymbolTableForOcu(ocu);

        TypeLookup lookup = creator.getTypeLookup();
        ensureNoErrors(lookup, creator.getTopLevelSymbolCreator());
        checkSymbolTable(lookup, GLOBAL, 2, Arrays.asList("one", TypeLookup.UNNAMED_NAMESPACE_NAME));

        Optional<ParentSymbol> optNamespaceOne = lookup.getNamespace("one");
        assertTrue(optNamespaceOne.isPresent(), "Namespace one is not present!");
        ParentSymbol namespaceOne = optNamespaceOne.get();
        checkSymbol(namespaceOne, "one", Symbol.Kind.NAMESPACE, DataType.NONE,
                FLAG_NONE, 1);
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
        SymbolCreator creator = new SymbolCreator(new BaseMessageProducer(), new TypeLookup());
        creator.createSymbolTableForOcu(ocu);

        TypeLookup lookup = creator.getTypeLookup();
        ensureNoErrors(lookup, creator.getTopLevelSymbolCreator());
        checkSymbolTable(lookup, GLOBAL, 2, Arrays.asList("spruce", TypeLookup.UNNAMED_NAMESPACE_NAME));

        Optional<ParentSymbol> optFirstNamespace = lookup.getNamespace("spruce");
        assertTrue(optFirstNamespace.isPresent(), "Namespace spruce is not found!");
        ParentSymbol spruceSymbol = optFirstNamespace.get();
        checkSymbol(spruceSymbol, "spruce", Symbol.Kind.NAMESPACE, DataType.NONE,
                FLAG_NONE, 1);

        ChildSymbolTable child = spruceSymbol.getTable();
        checkSymbolTable(child, NAMESPACE, 1, Arrays.asList("collections"));

        ParentSymbol collectionsSymbol = ensureIsa(child.get("collections"), ParentSymbol.class);
        checkSymbol(collectionsSymbol, "collections", Symbol.Kind.NAMESPACE,
                DataType.NONE, FLAG_NONE, 1);

        child = ensureIsa(collectionsSymbol, ParentSymbol.class).getTable();
        checkSymbolTable(child, NAMESPACE, 1, Arrays.asList("concurrent"));

        ParentSymbol concurrentSymbol = ensureIsa(child.get("concurrent"), ParentSymbol.class);
        checkSymbol(concurrentSymbol, "concurrent", Symbol.Kind.NAMESPACE,
                DataType.NONE, FLAG_NONE, 0);
    }

    /**
     * Tests ordinary compilation unit of only type declarations.
     */
    @Test
    public void testOcuTypeDeclarationListOnly() {
        TopLevelParser parser = ParserTopLevelTest.getTopLevelParser("""
                class TestClass1 {}
                class TestClass2 {}
                class TestClass3 {}
                class TestClass4 {}
                class TestClass5 {}
                class TestClass6 {}
                """);
        ASTOrdinaryCompilationUnit ocu = parser.parseOrdinaryCompilationUnit();
        SymbolCreator creator = new SymbolCreator(new BaseMessageProducer(), new TypeLookup());
        creator.createSymbolTableForOcu(ocu);

        TypeLookup lookup = creator.getTypeLookup();
        ensureNoErrors(lookup, creator.getTopLevelSymbolCreator());
        checkSymbolTable(lookup, GLOBAL, 1, Arrays.asList(TypeLookup.UNNAMED_NAMESPACE_NAME));

        Optional<ParentSymbol> optUnnamedNamespace = lookup.getNamespace(TypeLookup.UNNAMED_NAMESPACE_NAME);
        assertTrue(optUnnamedNamespace.isPresent(), "Unnamed namespace is not found!");
        ParentSymbol unnamedNamespace = optUnnamedNamespace.get();
        checkSymbol(unnamedNamespace, TypeLookup.UNNAMED_NAMESPACE_NAME, Symbol.Kind.NAMESPACE, DataType.NONE,
                FLAG_NONE, 6);

        ChildSymbolTable childTable = unnamedNamespace.getTable();
        List<String> expSymbolNames =
                Arrays.asList("TestClass1", "TestClass2", "TestClass3", "TestClass4", "TestClass5", "TestClass6");
        checkSymbolTable(childTable, NAMESPACE, 6, expSymbolNames);
    }

    /**
     * Tests namespace symbol overlap between multiple OCUs.
     */
    @Test
    public void testNamespaceOverlap() {
        TopLevelParser parser1 = ParserTopLevelTest.getTopLevelParser("""
                namespace spruce.lang;
                """);
        TopLevelParser parser2 = ParserTopLevelTest.getTopLevelParser("""
                namespace spruce.collections.concurrent;
                """);
        ASTOrdinaryCompilationUnit ocu1 = parser1.parseOrdinaryCompilationUnit();
        ASTOrdinaryCompilationUnit ocu2 = parser2.parseOrdinaryCompilationUnit();
        SymbolCreator creator = new SymbolCreator(new BaseMessageProducer(), new TypeLookup());
        creator.createSymbolTableForOcu(ocu1);
        creator.createSymbolTableForOcu(ocu2);

        TypeLookup lookup = creator.getTypeLookup();
        ensureNoErrors(lookup, creator.getTopLevelSymbolCreator());
        checkSymbolTable(lookup, GLOBAL, 2, Arrays.asList("spruce", TypeLookup.UNNAMED_NAMESPACE_NAME));

        Optional<ParentSymbol> optSpruceNamespace = lookup.getNamespace("spruce");
        assertTrue(optSpruceNamespace.isPresent(), "Namespace spruce is not found!");
        ParentSymbol spruceNamespace = optSpruceNamespace.get();
        checkSymbol(spruceNamespace, "spruce", Symbol.Kind.NAMESPACE, DataType.NONE,
                FLAG_NONE, 2);

        ChildSymbolTable spruceTable = spruceNamespace.getTable();
        checkSymbolTable(spruceTable, NAMESPACE, 2, Arrays.asList("lang", "collections"));

        ParentSymbol langNamespace = ensureIsa(spruceTable.get("lang"), ParentSymbol.class);
        checkSymbol(langNamespace, "lang", Symbol.Kind.NAMESPACE, DataType.NONE,
                FLAG_NONE, 0);

        ParentSymbol collectionsNamespace = ensureIsa(spruceTable.get("collections"), ParentSymbol.class);
        checkSymbol(collectionsNamespace, "collections", Symbol.Kind.NAMESPACE, DataType.NONE,
                FLAG_NONE, 1);

        ChildSymbolTable collectionsTable = collectionsNamespace.getTable();
        checkSymbolTable(collectionsTable, NAMESPACE, 1, Arrays.asList("concurrent"));

        ParentSymbol concurrentNamespace = ensureIsa(collectionsTable.get("concurrent"), ParentSymbol.class);
        checkSymbol(concurrentNamespace, "concurrent", Symbol.Kind.NAMESPACE, DataType.NONE,
                FLAG_NONE, 0);
    }

    /**
     * Tests bad classes - same name.
     */
    @Test
    public void testClassesSameName() {
        TopLevelParser parser = ParserTopLevelTest.getTopLevelParser("""
                class SameName {}
                class SameName {}
                """);
        ASTOrdinaryCompilationUnit ocu = parser.parseOrdinaryCompilationUnit();
        SymbolCreator creator = new SymbolCreator(new BaseMessageProducer(), new TypeLookup());
        creator.createSymbolTableForOcu(ocu);
        expectError(creator.getTypeLookup(), creator.getTopLevelSymbolCreator());
    }

    /**
     * Helper method to get a <code>TopLevelSymbolCreator</code>.
     * @return A <code>TopLevelSymbolCreator</code>.
     */
    public static TopLevelSymbolCreator getTopLevelSymbolCreator() {
        return new SymbolCreator(new BaseMessageProducer(), new TypeLookup()).getTopLevelSymbolCreator();
    }
}
