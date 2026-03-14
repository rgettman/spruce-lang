package org.spruce.compiler.bootstrap.test.symbol;

import java.util.Arrays;
import java.util.List;

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
import org.spruce.compiler.bootstrap.symbol.TopLevelSymbolTable;
import org.spruce.compiler.bootstrap.test.parser.ParserTopLevelTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.spruce.compiler.bootstrap.symbol.Symbol.*;
import static org.spruce.compiler.bootstrap.symbol.Symbol.Type.PARAMETER;
import static org.spruce.compiler.bootstrap.symbol.SymbolTable.Scope.*;
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
        Pair pair = createTopLevelSymbolTableNoErrors("""
                class Outer {
                    class Inner {}
                }
                """);
        TopLevelSymbolTable topLevel = pair.tlst();
        ASTClassDeclaration outer = ensureIsa(pair.typeDecl(), ASTClassDeclaration.class);

        String expSymbolName = "Outer";
        checkSymbolTable(topLevel, TOP, 1, List.of(expSymbolName));
        Symbol symbol = topLevel.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), "Outer", Type.CLASS, FLAG_NONE, 1);
        assertSame(symbol, outer.getDeclSymbol());

        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        ASTClassDeclaration inner = ensureIsa(outer.getMembers().get(0), ASTClassDeclaration.class);
        checkSymbolTable(innerTable, TYPE, 1, Arrays.asList("Inner"));

        Symbol innerSymbol = innerTable.get("Inner");
        checkSymbol(ensureIsa(innerSymbol, ParentSymbol.class), "Inner", Type.CLASS, FLAG_NONE, 0);
        assertSame(innerSymbol, inner.getDeclSymbol());
    }

    /**
     * Tests member of constructor.
     */
    @Test
    public void testMemberConstructor() {
        Pair pair = createTopLevelSymbolTableNoErrors("""
                class HasMember {
                    constructor() {}
                }
                """);
        TopLevelSymbolTable topLevel = pair.tlst();
        ASTTypeDeclaration typeDecl = pair.typeDecl();

        String expSymbolName = "HasMember";
        checkSymbolTable(topLevel, TOP, 1, List.of(expSymbolName));
        Symbol symbol = topLevel.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), "HasMember", Type.CLASS, FLAG_NONE, 1);

        String symbolName = NAME_CONSTRUCTOR + "()";
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 1, Arrays.asList(symbolName));

        Symbol constructorDecl = innerTable.get(symbolName);
        checkSymbol(ensureIsa(constructorDecl, ParentSymbol.class), symbolName, Type.CONSTRUCTOR, FLAG_NONE,
                0);

        ASTConstructorDeclaration constructor = ensureIsa(typeDecl.getMembers().get(0), ASTConstructorDeclaration.class);
        assertSame(constructorDecl, constructor.getConstructorDecl().getDeclSymbol());
    }

    /**
     * Tests member of overloaded constructors.
     */
    @Test
    public void testMemberConstructorOverload() {
        Pair pair = createTopLevelSymbolTableNoErrors("""
                class HasMember {
                    constructor() {}
                    constructor(Integer foo) {}
                }
                """);
        TopLevelSymbolTable topLevel = pair.tlst();
        ASTTypeDeclaration typeDecl = pair.typeDecl();

        String expSymbolName = "HasMember";
        checkSymbolTable(topLevel, TOP, 1, List.of(expSymbolName));
        Symbol symbol = topLevel.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), "HasMember", Type.CLASS, FLAG_NONE, 2);

        String symbolName = NAME_CONSTRUCTOR + "()";
        String symbolName2 = NAME_CONSTRUCTOR + "(Integer)";
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 2, Arrays.asList(symbolName, symbolName2));

        Symbol constructorDecl = innerTable.get(symbolName);
        checkSymbol(ensureIsa(constructorDecl, ParameterizedSymbol.class), symbolName, Type.CONSTRUCTOR, FLAG_NONE,
                0, 0);
        ASTConstructorDeclaration constructor = ensureIsa(typeDecl.getMembers().get(0), ASTConstructorDeclaration.class);
        assertSame(constructorDecl, constructor.getConstructorDecl().getDeclSymbol());

        Symbol constructorDecl2 = innerTable.get(symbolName2);
        checkSymbol(ensureIsa(constructorDecl2, ParameterizedSymbol.class), symbolName2, Type.CONSTRUCTOR, FLAG_NONE,
                1, 1);
        ASTConstructorDeclaration constructor2 = ensureIsa(typeDecl.getMembers().get(1), ASTConstructorDeclaration.class);
        assertSame(constructorDecl2, constructor2.getConstructorDecl().getDeclSymbol());
    }

    /**
     * Tests members of field declarations with variable modifiers.
     */
    @Test
    public void testMemberFieldDeclarationVarMods() {
        Pair pair = createTopLevelSymbolTableNoErrors("""
                class HasMember {
                    String immutable;
                }
                """);
        TopLevelSymbolTable topLevel = pair.tlst();
        ASTTypeDeclaration typeDecl = pair.typeDecl();

        String expSymbolName = "HasMember";
        checkSymbolTable(topLevel, TOP, 1, List.of(expSymbolName));
        Symbol symbol = topLevel.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), "HasMember", Type.CLASS, FLAG_NONE, 1);

        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 1, Arrays.asList("immutable"));

        Symbol immutSymbol = innerTable.get("immutable");
        checkSymbol(immutSymbol, "immutable", Type.FIELD, FLAG_NONE);
        ASTFieldDeclaration field = ensureIsa(typeDecl.getMembers().get(0), ASTFieldDeclaration.class);
        assertSame(immutSymbol, field.getVarDeclList().get(0).getDeclSymbol());
    }

    /**
     * Tests members of field declarations.
     */
    @Test
    public void testMemberFieldDeclaration() {
        Pair pair = createTopLevelSymbolTableNoErrors("""
                class HasMember {
                    String foo;
                    Int bar, jazz;
                }
                """);
        TopLevelSymbolTable topLevel = pair.tlst();
        ASTTypeDeclaration typeDecl = pair.typeDecl();

        String expSymbolName = "HasMember";
        checkSymbolTable(topLevel, TOP, 1, List.of(expSymbolName));
        Symbol symbol = topLevel.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), "HasMember", Type.CLASS, FLAG_NONE, 3);
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 3, Arrays.asList("foo", "bar", "jazz"));

        Symbol fooSymbol = innerTable.get("foo");
        checkSymbol(fooSymbol, "foo", Type.FIELD, FLAG_NONE);
        ASTFieldDeclaration fooField = ensureIsa(typeDecl.getMembers().get(0), ASTFieldDeclaration.class);
        assertSame(fooSymbol, fooField.getVarDeclList().get(0).getDeclSymbol());

        Symbol barSymbol = innerTable.get("bar");
        checkSymbol(barSymbol, "bar", Type.FIELD, FLAG_NONE);
        ASTFieldDeclaration twoField = ensureIsa(typeDecl.getMembers().get(1), ASTFieldDeclaration.class);
        assertSame(barSymbol, twoField.getVarDeclList().get(0).getDeclSymbol());

        Symbol jazzSymbol = innerTable.get("jazz");
        checkSymbol(jazzSymbol, "jazz", Type.FIELD, FLAG_NONE);
        assertSame(jazzSymbol, twoField.getVarDeclList().get(1).getDeclSymbol());
    }

    /**
     * Tests duplicate field names.
     */
    @Test
    public void testMemberFieldsSameName() {
        createTopLevelSymbolTableWithErrors("""
                class DupeFieldSymbols {
                    String foo;
                    Int foo;
                }
                """, 1);
    }

    /**
     * Tests member of constant declaration.
     */
    @Test
    public void testMemberConstant() {
        Pair pair = createTopLevelSymbolTableNoErrors("""
                class HasMember {
                    constant String BAR = "bar";
                }
                """);
        TopLevelSymbolTable topLevel = pair.tlst();
        ASTTypeDeclaration typeDecl = pair.typeDecl();

        String expSymbolName = "HasMember";
        checkSymbolTable(topLevel, TOP, 1, List.of(expSymbolName));
        Symbol symbol = topLevel.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), "HasMember", Type.CLASS, FLAG_NONE, 1);
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 1, Arrays.asList("BAR"));

        Symbol constantSymbol = innerTable.get("BAR");
        checkSymbol(constantSymbol, "BAR", Type.FIELD, FLAG_MOD_SHARED);
        ASTFieldDeclaration barField = ensureIsa(typeDecl.getMembers().get(0), ASTFieldDeclaration.class);
        assertSame(constantSymbol, barField.getVarDeclList().get(0).getDeclSymbol());
    }

    /**
     * Tests member of method declaration.
     */
    @Test
    public void testMemberMethodDeclaration() {
        Pair pair = createTopLevelSymbolTableNoErrors("""
               class HasMember {
                    void foo(String bar) {
                        Widget baz;
                    }
               }
               """);
        TopLevelSymbolTable topLevel = pair.tlst();
        ASTTypeDeclaration typeDecl = pair.typeDecl();

        String expSymbolName = "HasMember";
        checkSymbolTable(topLevel, TOP, 1, List.of(expSymbolName));
        Symbol symbol = topLevel.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), "HasMember", Type.CLASS, FLAG_NONE, 1);
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        String symbolName = "foo(String)";
        checkSymbolTable(innerTable, TYPE, 1, Arrays.asList(symbolName));

        Symbol fooSymbol = innerTable.get(symbolName);
        checkSymbol(ensureIsa(fooSymbol, ParameterizedSymbol.class), symbolName, Type.METHOD, FLAG_NONE,
                2, 1);
        ASTMethodDeclaration fooMethod = ensureIsa(typeDecl.getMembers().get(0), ASTMethodDeclaration.class);
        assertSame(fooSymbol, fooMethod.getHeader().getMethodDecl().getDeclSymbol());
    }

    /**
     * Tests member of overloaded methods.
     */
    @Test
    public void testMemberMethodOverloads() {
        Pair pair = createTopLevelSymbolTableNoErrors("""
                class HasMember {
                    String foo;
                    void foo() {}
                    void foo(String bar) {}
                }
                """);
        TopLevelSymbolTable topLevel = pair.tlst();
        ASTTypeDeclaration typeDecl = pair.typeDecl();

        String expSymbolName = "HasMember";
        checkSymbolTable(topLevel, TOP, 1, List.of(expSymbolName));
        Symbol symbol = topLevel.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), "HasMember", Type.CLASS, FLAG_NONE, 3);
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 3, Arrays.asList("foo", "foo()", "foo(String)"));

        String symbolName = "foo";
        Symbol fooSymbol = innerTable.get(symbolName);
        checkSymbol(fooSymbol, symbolName, Type.FIELD, FLAG_NONE);
        ASTFieldDeclaration fooField = ensureIsa(typeDecl.getMembers().get(0), ASTFieldDeclaration.class);
        assertSame(fooSymbol, fooField.getVarDeclList().get(0).getDeclSymbol());

        symbolName = "foo()";
        Symbol fooSymbol2 = innerTable.get(symbolName);
        checkSymbol(ensureIsa(fooSymbol2, ParameterizedSymbol.class), symbolName, Type.METHOD, FLAG_NONE,
                0, 0);
        ASTMethodDeclaration fooMethod = ensureIsa(typeDecl.getMembers().get(1), ASTMethodDeclaration.class);
        assertSame(fooSymbol2, fooMethod.getHeader().getMethodDecl().getDeclSymbol());

        symbolName = "foo(String)";
        Symbol fooSymbol3 = innerTable.get(symbolName);
        checkSymbol(ensureIsa(fooSymbol3, ParameterizedSymbol.class), symbolName, Type.METHOD, FLAG_NONE,
                1, 1);
        ASTMethodDeclaration fooStringMethod = ensureIsa(typeDecl.getMembers().get(2), ASTMethodDeclaration.class);
        assertSame(fooSymbol3, fooStringMethod.getHeader().getMethodDecl().getDeclSymbol());
    }

    /**
     * Tests formal parameter list in a method.
     */
    @Test
    public void testFormalParameterList() {
        Pair pair = createTopLevelSymbolTableNoErrors("""
                class HasMember {
                    String foo(String first, String last, Integer age, String ssn) {}
                }
                """);
        TopLevelSymbolTable topLevel = pair.tlst();
        ASTTypeDeclaration typeDecl = pair.typeDecl();

        String expSymbolName = "HasMember";
        checkSymbolTable(topLevel, TOP, 1, List.of(expSymbolName));
        Symbol symbol = topLevel.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), "HasMember", Type.CLASS, FLAG_NONE, 1);
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        String symbolName = "foo(String,String,Integer,String)";
        checkSymbolTable(innerTable, TYPE, 1, Arrays.asList(symbolName));

        List<String> expNames = Arrays.asList("first", "last", "age", "ssn");
        ParameterizedSymbol fooSymbol = ensureIsa(innerTable.get(symbolName), ParameterizedSymbol.class);
        checkSymbol(fooSymbol, symbolName, Type.METHOD, FLAG_NONE, 4, 4);
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
        return new SymbolCreator(new BaseMessageProducer()).getClassesSymbolCreator();
    }

    /**
     * Helper method to get a <code>TopLevelSymbolTable</code> for a top level
     * type declaration directly from code.  Ensures no errors.
     * @param code The code for the top level type declaration.
     * @return A <code>Pair</code> consisting of an <code>ASTTypeDeclaration</code>
     *     and a <code>TopLevelSymbolTable</code>.
     */
    public static Pair createTopLevelSymbolTableNoErrors(String code) {
        ClassesSymbolCreator creator = getClassesSymbolCreator();
        Pair pair = createTopLevelSymbolTable(code, creator);
        TopLevelSymbolTable table = pair.tlst();
        ensureNoErrors(table, creator);
        return pair;
    }

    /**
     * Helper method to get a <code>TopLevelSymbolTable</code> for a top level
     * type declaration directly from code.  Expects the given number of errors.
     * @param code The code for the top level type declaration.
     * @param expNumErrors The number of errors expected.
     */
    public static void createTopLevelSymbolTableWithErrors(String code, int expNumErrors) {
        ClassesSymbolCreator creator = getClassesSymbolCreator();
        Pair pair = createTopLevelSymbolTable(code, creator);
        TopLevelSymbolTable table = pair.tlst();
        expectError(table, creator, expNumErrors);
    }

    private static Pair createTopLevelSymbolTable(String code, ClassesSymbolCreator creator) {
        TopLevelParser topLevelParser = ParserTopLevelTest.getTopLevelParser(code);
        ASTTypeDeclaration typeDecl = topLevelParser.parseTypeDeclaration();
        TopLevelSymbolTable parent = new TopLevelSymbolTable();
        creator.createSymbolsForTopLevelTypeDeclaration(typeDecl, parent);
        return new Pair(typeDecl, parent);
    }

    public record Pair(ASTTypeDeclaration typeDecl, TopLevelSymbolTable tlst) {
    }
}
