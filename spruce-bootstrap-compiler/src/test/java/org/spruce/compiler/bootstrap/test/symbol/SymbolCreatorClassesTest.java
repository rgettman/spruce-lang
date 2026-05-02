package org.spruce.compiler.bootstrap.test.symbol;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.bootstrap.ast.classes.*;
import org.spruce.compiler.bootstrap.common.BaseMessageProducer;
import org.spruce.compiler.bootstrap.parser.TopLevelParser;
import org.spruce.compiler.bootstrap.symbol.ClassesSymbolCreator;
import org.spruce.compiler.bootstrap.symbol.ParameterizedSymbol;
import org.spruce.compiler.bootstrap.symbol.ParentSymbol;
import org.spruce.compiler.bootstrap.symbol.Symbol;
import org.spruce.compiler.bootstrap.symbol.SymbolCreator;
import org.spruce.compiler.bootstrap.symbol.SymbolTable;
import org.spruce.compiler.bootstrap.symbol.TypeLookup;
import org.spruce.compiler.bootstrap.test.parser.ParserTopLevelTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.spruce.compiler.bootstrap.symbol.Symbol.*;
import static org.spruce.compiler.bootstrap.symbol.Symbol.Kind.PARAMETER;
import static org.spruce.compiler.bootstrap.symbol.SymbolTable.Scope.*;
import static org.spruce.compiler.bootstrap.symbol.TypeLookup.UNNAMED_NAMESPACE_NAME;
import static org.spruce.compiler.bootstrap.test.symbol.SymbolCreatorTestUtility.*;
import static org.spruce.compiler.bootstrap.test.util.TestUtility.ensureIsa;

/**
 * All tests for the classes symbol creator.
 */
public class SymbolCreatorClassesTest {

    /**
     * Tests member of inner class.
     */
    @Test
    public void testMemberInnerClass() {
        Pair pair = createTypeLookupNoErrors("""
                class Outer {
                    class Inner {}
                }
                """);
        SymbolTable global = pair.lookup();
        ASTClassDeclaration outer = ensureIsa(pair.typeDecl(), ASTClassDeclaration.class);
        checkSymbolTable(global, GLOBAL, 1, List.of(UNNAMED_NAMESPACE_NAME));

        ParentSymbol unnamedNamespace = ensureIsa(global.get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        checkSymbol(unnamedNamespace, UNNAMED_NAMESPACE_NAME, Kind.NAMESPACE, FLAG_NONE, 1);

        SymbolTable namespaceTable = unnamedNamespace.getTable();

        String expSymbolName = "Outer";
        checkSymbolTable(namespaceTable, NAMESPACE, 1, List.of(expSymbolName));
        Symbol symbol = namespaceTable.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), "Outer", Kind.CLASS, FLAG_NONE, 1);
        assertSame(symbol, outer.getDeclSymbol());

        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        ASTClassDeclaration inner = ensureIsa(outer.getMembers().get(0), ASTClassDeclaration.class);
        checkSymbolTable(innerTable, TYPE, 1, Arrays.asList("Inner"));

        Symbol innerSymbol = innerTable.get("Inner");
        checkSymbol(ensureIsa(innerSymbol, ParentSymbol.class), "Inner", Kind.CLASS, FLAG_NONE, 0);
        assertSame(innerSymbol, inner.getDeclSymbol());
    }

    /**
     * Tests member of constructor.
     */
    @Test
    public void testMemberConstructor() {
        Pair pair = createTypeLookupNoErrors("""
                class HasMember {
                    constructor() {}
                }
                """);
        SymbolTable global = pair.lookup();
        ASTTypeDeclaration typeDecl = pair.typeDecl();
        checkSymbolTable(global, GLOBAL, 1, List.of(UNNAMED_NAMESPACE_NAME));

        ParentSymbol unnamedNamespace = ensureIsa(global.get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        checkSymbol(unnamedNamespace, UNNAMED_NAMESPACE_NAME, Kind.NAMESPACE, FLAG_NONE, 1);

        SymbolTable namespaceTable = unnamedNamespace.getTable();

        String expSymbolName = "HasMember";
        checkSymbolTable(namespaceTable, NAMESPACE, 1, List.of(expSymbolName));
        Symbol symbol = namespaceTable.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), "HasMember", Kind.CLASS, FLAG_NONE, 1);

        String symbolName = NAME_CONSTRUCTOR + "()";
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 1, Arrays.asList(symbolName));

        Symbol constructorDecl = innerTable.get(symbolName);
        checkSymbol(ensureIsa(constructorDecl, ParentSymbol.class), symbolName, Kind.CONSTRUCTOR, FLAG_NONE,0);

        ASTConstructorDeclaration constructor = ensureIsa(typeDecl.getMembers().get(0), ASTConstructorDeclaration.class);
        assertSame(constructorDecl, constructor.getConstructorDecl().getDeclSymbol());
    }

    /**
     * Tests member of overloaded constructors.
     */
    @Test
    public void testMemberConstructorOverload() {
        Pair pair = createTypeLookupNoErrors("""
                class HasMember {
                    constructor() {}
                    constructor(Integer foo) {}
                }
                """);
        SymbolTable global = pair.lookup();
        ASTTypeDeclaration typeDecl = pair.typeDecl();
        checkSymbolTable(global, GLOBAL, 1, List.of(UNNAMED_NAMESPACE_NAME));

        ParentSymbol unnamedNamespace = ensureIsa(global.get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        checkSymbol(unnamedNamespace, UNNAMED_NAMESPACE_NAME, Kind.NAMESPACE, FLAG_NONE, 1);

        SymbolTable namespaceTable = unnamedNamespace.getTable();

        String expSymbolName = "HasMember";
        checkSymbolTable(namespaceTable, NAMESPACE, 1, List.of(expSymbolName));
        Symbol symbol = namespaceTable.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), "HasMember", Kind.CLASS, FLAG_NONE, 2);

        String symbolName = NAME_CONSTRUCTOR + "()";
        String symbolName2 = NAME_CONSTRUCTOR + "(Integer)";
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 2, Arrays.asList(symbolName, symbolName2));

        Symbol constructorDecl = innerTable.get(symbolName);
        checkSymbol(ensureIsa(constructorDecl, ParameterizedSymbol.class), symbolName, Kind.CONSTRUCTOR,
                FLAG_NONE,0, 0);
        ASTConstructorDeclaration constructor = ensureIsa(typeDecl.getMembers().get(0), ASTConstructorDeclaration.class);
        assertSame(constructorDecl, constructor.getConstructorDecl().getDeclSymbol());

        Symbol constructorDecl2 = innerTable.get(symbolName2);
        checkSymbol(ensureIsa(constructorDecl2, ParameterizedSymbol.class), symbolName2, Kind.CONSTRUCTOR,
                FLAG_NONE,1, 1);
        ASTConstructorDeclaration constructor2 = ensureIsa(typeDecl.getMembers().get(1), ASTConstructorDeclaration.class);
        assertSame(constructorDecl2, constructor2.getConstructorDecl().getDeclSymbol());
    }

    /**
     * Tests members of field declarations with variable modifiers.
     */
    @Test
    public void testMemberFieldDeclarationVarMods() {
        Pair pair = createTypeLookupNoErrors("""
                class HasMember {
                    String immutable;
                }
                """);
        SymbolTable global = pair.lookup();
        ASTTypeDeclaration typeDecl = pair.typeDecl();
        checkSymbolTable(global, GLOBAL, 1, List.of(UNNAMED_NAMESPACE_NAME));

        ParentSymbol unnamedNamespace = ensureIsa(global.get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        checkSymbol(unnamedNamespace, UNNAMED_NAMESPACE_NAME, Kind.NAMESPACE, FLAG_NONE, 1);

        SymbolTable namespaceTable = unnamedNamespace.getTable();

        String expSymbolName = "HasMember";
        checkSymbolTable(namespaceTable, NAMESPACE, 1, List.of(expSymbolName));
        Symbol symbol = namespaceTable.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), "HasMember", Kind.CLASS, FLAG_NONE, 1);

        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 1, Arrays.asList("immutable"));

        Symbol immutSymbol = innerTable.get("immutable");
        checkSymbol(immutSymbol, "immutable", Kind.FIELD, FLAG_NONE);
        ASTFieldDeclaration field = ensureIsa(typeDecl.getMembers().get(0), ASTFieldDeclaration.class);
        assertSame(immutSymbol, field.getVarDeclList().get(0).getDeclSymbol());
    }

    /**
     * Tests members of field declarations.
     */
    @Test
    public void testMemberFieldDeclaration() {
        Pair pair = createTypeLookupNoErrors("""
                class HasMember {
                    String foo;
                    Int bar, jazz;
                }
                """);
        SymbolTable global = pair.lookup();
        ASTTypeDeclaration typeDecl = pair.typeDecl();
        checkSymbolTable(global, GLOBAL, 1, List.of(UNNAMED_NAMESPACE_NAME));

        ParentSymbol unnamedNamespace = ensureIsa(global.get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        checkSymbol(unnamedNamespace, UNNAMED_NAMESPACE_NAME, Kind.NAMESPACE, FLAG_NONE, 1);

        SymbolTable namespaceTable = unnamedNamespace.getTable();

        String expSymbolName = "HasMember";
        checkSymbolTable(namespaceTable, NAMESPACE, 1, List.of(expSymbolName));
        Symbol symbol = namespaceTable.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), "HasMember", Kind.CLASS, FLAG_NONE, 3);
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 3, Arrays.asList("foo", "bar", "jazz"));

        Symbol fooSymbol = innerTable.get("foo");
        checkSymbol(fooSymbol, "foo", Kind.FIELD, FLAG_NONE);
        ASTFieldDeclaration fooField = ensureIsa(typeDecl.getMembers().get(0), ASTFieldDeclaration.class);
        assertSame(fooSymbol, fooField.getVarDeclList().get(0).getDeclSymbol());

        Symbol barSymbol = innerTable.get("bar");
        checkSymbol(barSymbol, "bar", Kind.FIELD, FLAG_NONE);
        ASTFieldDeclaration twoField = ensureIsa(typeDecl.getMembers().get(1), ASTFieldDeclaration.class);
        assertSame(barSymbol, twoField.getVarDeclList().get(0).getDeclSymbol());

        Symbol jazzSymbol = innerTable.get("jazz");
        checkSymbol(jazzSymbol, "jazz", Kind.FIELD, FLAG_NONE);
        assertSame(jazzSymbol, twoField.getVarDeclList().get(1).getDeclSymbol());
    }

    /**
     * Tests duplicate field names.
     */
    @Test
    public void testMemberFieldsSameName() {
        createTypeLookupWithErrors("""
                class DupeFieldSymbols {
                    String foo;
                    Int foo;
                }
                """, 2);
    }

    /**
     * Tests member of constant declaration.
     */
    @Test
    public void testMemberConstant() {
        Pair pair = createTypeLookupNoErrors("""
                class HasMember {
                    constant String BAR = "bar";
                }
                """);
        SymbolTable global = pair.lookup();
        ASTTypeDeclaration typeDecl = pair.typeDecl();
        checkSymbolTable(global, GLOBAL, 1, List.of(UNNAMED_NAMESPACE_NAME));

        ParentSymbol unnamedNamespace = ensureIsa(global.get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        checkSymbol(unnamedNamespace, UNNAMED_NAMESPACE_NAME, Kind.NAMESPACE, FLAG_NONE, 1);

        SymbolTable namespaceTable = unnamedNamespace.getTable();
        String expSymbolName = "HasMember";
        checkSymbolTable(namespaceTable, NAMESPACE, 1, List.of(expSymbolName));
        Symbol symbol = namespaceTable.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), "HasMember", Kind.CLASS, FLAG_NONE, 1);
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 1, Arrays.asList("BAR"));

        Symbol constantSymbol = innerTable.get("BAR");
        checkSymbol(constantSymbol, "BAR", Kind.FIELD, FLAG_MOD_SHARED);
        ASTFieldDeclaration barField = ensureIsa(typeDecl.getMembers().get(0), ASTFieldDeclaration.class);
        assertSame(constantSymbol, barField.getVarDeclList().get(0).getDeclSymbol());
    }

    /**
     * Tests member of method declaration.
     */
    @Test
    public void testMemberMethodDeclaration() {
        Pair pair = createTypeLookupNoErrors("""
               class HasMember {
                    void foo(String bar) {
                        Widget baz;
                    }
               }
               """);
        SymbolTable global = pair.lookup();
        ASTTypeDeclaration typeDecl = pair.typeDecl();
        checkSymbolTable(global, GLOBAL, 1, List.of(UNNAMED_NAMESPACE_NAME));

        ParentSymbol unnamedNamespace = ensureIsa(global.get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        checkSymbol(unnamedNamespace, UNNAMED_NAMESPACE_NAME, Kind.NAMESPACE, -FLAG_NONE, 1);

        SymbolTable namespaceTable = unnamedNamespace.getTable();

        String expSymbolName = "HasMember";
        checkSymbolTable(namespaceTable, NAMESPACE, 1, List.of(expSymbolName));
        Symbol symbol = namespaceTable.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), "HasMember", Kind.CLASS, FLAG_NONE, 1);
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        String symbolName = "foo(String)";
        checkSymbolTable(innerTable, TYPE, 1, Arrays.asList(symbolName));

        Symbol fooSymbol = innerTable.get(symbolName);
        checkSymbol(ensureIsa(fooSymbol, ParameterizedSymbol.class), symbolName, Kind.METHOD,
                FLAG_NONE,2, 1);
        ASTMethodDeclaration fooMethod = ensureIsa(typeDecl.getMembers().get(0), ASTMethodDeclaration.class);
        assertSame(fooSymbol, fooMethod.getHeader().getMethodDecl().getDeclSymbol());
    }

    /**
     * Tests member of overloaded methods.
     */
    @Test
    public void testMemberMethodOverloads() {
        Pair pair = createTypeLookupNoErrors("""
                class HasMember {
                    String foo;
                    void foo() {}
                    void foo(String bar) {}
                }
                """);
        SymbolTable global = pair.lookup();
        ASTTypeDeclaration typeDecl = pair.typeDecl();
        checkSymbolTable(global, GLOBAL, 1, List.of(UNNAMED_NAMESPACE_NAME));

        ParentSymbol unnamedNamespace = ensureIsa(global.get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        checkSymbol(unnamedNamespace, UNNAMED_NAMESPACE_NAME, Kind.NAMESPACE, FLAG_NONE, 1);

        SymbolTable namespaceTable = unnamedNamespace.getTable();

        String expSymbolName = "HasMember";

        checkSymbolTable(namespaceTable, NAMESPACE, 1, List.of(expSymbolName));
        Symbol symbol = namespaceTable.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), "HasMember", Kind.CLASS, FLAG_NONE, 3);
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 3, Arrays.asList("foo", "foo()", "foo(String)"));

        String symbolName = "foo";
        Symbol fooSymbol = innerTable.get(symbolName);
        checkSymbol(fooSymbol, symbolName, Kind.FIELD, FLAG_NONE);
        ASTFieldDeclaration fooField = ensureIsa(typeDecl.getMembers().get(0), ASTFieldDeclaration.class);
        assertSame(fooSymbol, fooField.getVarDeclList().get(0).getDeclSymbol());

        symbolName = "foo()";
        Symbol fooSymbol2 = innerTable.get(symbolName);
        checkSymbol(ensureIsa(fooSymbol2, ParameterizedSymbol.class), symbolName, Kind.METHOD,
                FLAG_NONE,0, 0);
        ASTMethodDeclaration fooMethod = ensureIsa(typeDecl.getMembers().get(1), ASTMethodDeclaration.class);
        assertSame(fooSymbol2, fooMethod.getHeader().getMethodDecl().getDeclSymbol());

        symbolName = "foo(String)";
        Symbol fooSymbol3 = innerTable.get(symbolName);
        checkSymbol(ensureIsa(fooSymbol3, ParameterizedSymbol.class), symbolName, Kind.METHOD,
                FLAG_NONE,1, 1);
        ASTMethodDeclaration fooStringMethod = ensureIsa(typeDecl.getMembers().get(2), ASTMethodDeclaration.class);
        assertSame(fooSymbol3, fooStringMethod.getHeader().getMethodDecl().getDeclSymbol());
    }

    /**
     * Tests formal parameter list in a method.
     */
    @Test
    public void testFormalParameterList() {
        Pair pair = createTypeLookupNoErrors("""
                class HasMember {
                    String foo(String first, String last, Integer age, String ssn) {}
                }
                """);
        SymbolTable global = pair.lookup();
        ASTTypeDeclaration typeDecl = pair.typeDecl();
        checkSymbolTable(global, GLOBAL, 1, List.of(UNNAMED_NAMESPACE_NAME));

        ParentSymbol unnamedNamespace = ensureIsa(global.get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        checkSymbol(unnamedNamespace, UNNAMED_NAMESPACE_NAME, Kind.NAMESPACE, FLAG_NONE, 1);

        SymbolTable namespaceTable = unnamedNamespace.getTable();

        String expSymbolName = "HasMember";
        checkSymbolTable(namespaceTable, NAMESPACE, 1, List.of(expSymbolName));
        Symbol symbol = namespaceTable.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), "HasMember", Kind.CLASS, FLAG_NONE, 1);
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        String symbolName = "foo(String,String,Integer,String)";
        checkSymbolTable(innerTable, TYPE, 1, Arrays.asList(symbolName));

        List<String> expNames = Arrays.asList("first", "last", "age", "ssn");
        ParameterizedSymbol fooSymbol = ensureIsa(innerTable.get(symbolName), ParameterizedSymbol.class);
        checkSymbol(fooSymbol, symbolName, Kind.METHOD, FLAG_NONE, 4, 4);
        SymbolTable paramTable = fooSymbol.getTable();
        checkSymbolTable(paramTable, MEMBER, 4, expNames);
        List<Symbol> parameters = fooSymbol.getParameters();

        assertEquals(4, parameters.size());
        List<Long> expFlags = Arrays.asList(FLAG_NONE, FLAG_NONE, FLAG_NONE, FLAG_NONE);
        ASTMethodDeclaration fooMethod = ensureIsa(typeDecl.getMembers().get(0), ASTMethodDeclaration.class);
        ASTFormalParameterList formalParams = fooMethod.getHeader().getMethodDecl().getFormalParamList();
        for (int i = 0; i < 4; i++) {
            Symbol param = parameters.get(i);
            checkSymbol(param, expNames.get(i), PARAMETER, expFlags.get(i));
            assertSame(param, formalParams.get(i).getDeclSymbol());
        }
    }

    /**
     * Helper method to get a <code>ClassesSymbolCreator</code>.
     * @return A <code>ClassesSymbolCreator</code>.
     */
    public static ClassesSymbolCreator getClassesSymbolCreator() {
        return new SymbolCreator(new BaseMessageProducer(), new TypeLookup()).getClassesSymbolCreator();
    }

    /**
     * Helper method to get a <code>Pair</code> consisting of a
     * <code>TypeLookup</code> and <code>SymbolCreator </code>for a top level
     * type declaration directly from code.  Ensures no errors.
     * @param code The code for the top level type declaration.
     * @return A <code>Pair</code> consisting of a <code>TypeLookup</code> and
     *     a <code>SymbolCreator</code>.
     */
    public static Pair createTypeLookupNoErrors(String code) {
        ClassesSymbolCreator creator = getClassesSymbolCreator();
        Pair pair = createTypeLookup(code, creator);
        ensureNoErrors(pair.lookup(), creator);
        return pair;
    }

    /**
     * Helper method to run the symbol creation process for a top level type
     * declaration directly from code.  Expects the given number of errors.
     * @param code The code for the top level type declaration.
     * @param expNumErrors The number of errors expected.
     */
    public static void createTypeLookupWithErrors(String code, int expNumErrors) {
        ClassesSymbolCreator creator = getClassesSymbolCreator();
        Pair pair = createTypeLookup(code, creator);
        expectError(pair.lookup(), creator, expNumErrors);
    }

    private static Pair createTypeLookup(String code, ClassesSymbolCreator creator) {
        TopLevelParser topLevelParser = ParserTopLevelTest.getTopLevelParser(code);
        ASTTypeDeclaration typeDecl = topLevelParser.parseTypeDeclaration();
        Optional<ParentSymbol> optUnnamedNamespace = creator.getTypeLookup()
                .getNamespace(UNNAMED_NAMESPACE_NAME);
        assertTrue(optUnnamedNamespace.isPresent());
        creator.createSymbolsForTopLevelTypeDeclaration(typeDecl, optUnnamedNamespace.get());
        return new Pair(typeDecl, creator.getTypeLookup());
    }

    public record Pair(ASTTypeDeclaration typeDecl, TypeLookup lookup) {
    }
}
