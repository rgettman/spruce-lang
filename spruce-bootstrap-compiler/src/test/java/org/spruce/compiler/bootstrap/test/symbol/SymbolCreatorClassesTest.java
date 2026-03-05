package org.spruce.compiler.bootstrap.test.symbol;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.bootstrap.ast.classes.*;
import org.spruce.compiler.bootstrap.common.BaseMessageProducer;
import org.spruce.compiler.bootstrap.parser.ClassesParser;
import org.spruce.compiler.bootstrap.parser.TopLevelParser;
import org.spruce.compiler.bootstrap.symbol.ClassesSymbolCreator;
import org.spruce.compiler.bootstrap.symbol.ParameterizedSymbol;
import org.spruce.compiler.bootstrap.symbol.ParentSymbol;
import org.spruce.compiler.bootstrap.symbol.Symbol;
import org.spruce.compiler.bootstrap.symbol.SymbolCreator;
import org.spruce.compiler.bootstrap.symbol.SymbolTable;
import org.spruce.compiler.bootstrap.symbol.TopLevelSymbolTable;
import org.spruce.compiler.bootstrap.test.parser.ParserTopLevelTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        TopLevelSymbolTable topLevel = createTopLevelSymbolTableNoErrors("""
                class Outer {
                    class Inner {}
                }
                """);
        String expSymbolName = "Outer";
        checkSymbolTable(topLevel, TOP, 1, List.of(expSymbolName));
        Symbol symbol = topLevel.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), "Outer", Type.CLASS, FLAG_NONE, 1);

        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 1, Arrays.asList("Inner"));

        Symbol inner = innerTable.get("Inner");
        checkSymbol(ensureIsa(inner, ParentSymbol.class), "Inner", Type.CLASS, FLAG_NONE, 0);
    }

    /**
     * Tests member of nested class.
     */
    @Test
    public void testMemberNestedClass() {
        TopLevelSymbolTable topLevel = createTopLevelSymbolTableNoErrors("""
                class Outer {
                    class Inner {}
                }
                """);
        String expSymbolName = "Outer";
        checkSymbolTable(topLevel, TOP, 1, List.of(expSymbolName));
        Symbol symbol = topLevel.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), "Outer", Type.CLASS, FLAG_NONE, 1);

        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 1, Arrays.asList("Inner"));

        Symbol inner = innerTable.get("Inner");
        checkSymbol(ensureIsa(inner, ParentSymbol.class), "Inner", Type.CLASS,
                FLAG_NONE, 0);
    }

    /**
     * Tests member of constructor.
     */
    @Test
    public void testMemberConstructor() {
        TopLevelSymbolTable topLevel = createTopLevelSymbolTableNoErrors("""
                class HasMember {
                    constructor() {}
                }
                """);
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
    }

    /**
     * Tests member of overloaded constructors.
     */
    @Test
    public void testMemberConstructorOverload() {
        TopLevelSymbolTable topLevel = createTopLevelSymbolTableNoErrors("""
                class HasMember {
                    constructor() {}
                    constructor(Integer foo) {}
                }
                """);
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

        Symbol constructorDecl2 = innerTable.get(symbolName2);
        checkSymbol(ensureIsa(constructorDecl2, ParameterizedSymbol.class), symbolName2, Type.CONSTRUCTOR, FLAG_NONE,
                1, 1);
    }

    /**
     * Tests members of field declarations with variable modifiers.
     */
    @Test
    public void testMemberFieldDeclarationVarMods() {
        TopLevelSymbolTable topLevel = createTopLevelSymbolTableNoErrors("""
                class HasMember {
                    String immutable;
                }
                """);
        String expSymbolName = "HasMember";
        checkSymbolTable(topLevel, TOP, 1, List.of(expSymbolName));
        Symbol symbol = topLevel.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), "HasMember", Type.CLASS, FLAG_NONE, 1);

        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 1, Arrays.asList("immutable"));

        Symbol immutSymbol = innerTable.get("immutable");
        checkSymbol(immutSymbol, "immutable", Type.FIELD, FLAG_NONE);
    }

    /**
     * Tests members of field declarations.
     */
    @Test
    public void testMemberFieldDeclaration() {
        TopLevelSymbolTable topLevel = createTopLevelSymbolTableNoErrors("""
                class HasMember {
                    String foo;
                    Int bar, jazz;
                }
                """);
        String expSymbolName = "HasMember";
        checkSymbolTable(topLevel, TOP, 1, List.of(expSymbolName));
        Symbol symbol = topLevel.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), "HasMember", Type.CLASS, FLAG_NONE, 3);
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 3, Arrays.asList("foo", "bar", "jazz"));

        Symbol fooSymbol = innerTable.get("foo");
        checkSymbol(fooSymbol, "foo", Type.FIELD, FLAG_NONE);

        Symbol barSymbol = innerTable.get("bar");
        checkSymbol(barSymbol, "bar", Type.FIELD, FLAG_NONE);

        Symbol jazzSymbol = innerTable.get("jazz");
        checkSymbol(jazzSymbol, "jazz", Type.FIELD, FLAG_NONE);
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
        TopLevelSymbolTable topLevel = createTopLevelSymbolTableNoErrors("""
                class HasMember {
                    constant String BAR = "bar";
                }
                """);
        String expSymbolName = "HasMember";
        checkSymbolTable(topLevel, TOP, 1, List.of(expSymbolName));
        Symbol symbol = topLevel.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), "HasMember", Type.CLASS, FLAG_NONE, 1);
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 1, Arrays.asList("BAR"));

        Symbol constantSymbol = innerTable.get("BAR");
        checkSymbol(constantSymbol, "BAR", Type.FIELD, FLAG_MOD_SHARED);

    }

    /**
     * Tests member of method declaration.
     */
    @Test
    public void testMemberMethodDeclaration() {
        TopLevelSymbolTable topLevel = createTopLevelSymbolTableNoErrors("""
               class HasMember {
                    void foo(String bar) {
                        Widget baz;
                    }
               }
               """);
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
    }

    /**
     * Tests member of overloaded methods.
     */
    @Test
    public void testMemberMethodOverloads() {
        TopLevelSymbolTable topLevel = createTopLevelSymbolTableNoErrors("""
                class HasMember {
                    String foo;
                    void foo() {}
                    void foo(String bar) {}
                }
                """);
        String expSymbolName = "HasMember";
        checkSymbolTable(topLevel, TOP, 1, List.of(expSymbolName));
        Symbol symbol = topLevel.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), "HasMember", Type.CLASS, FLAG_NONE, 3);
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 3, Arrays.asList("foo", "foo()", "foo(String)"));

        String symbolName = "foo";
        Symbol fooSymbol = innerTable.get(symbolName);
        checkSymbol(fooSymbol, symbolName, Type.FIELD, FLAG_NONE);

        symbolName = "foo()";
        Symbol fooSymbol2 = innerTable.get(symbolName);
        checkSymbol(ensureIsa(fooSymbol2, ParameterizedSymbol.class), symbolName, Type.METHOD, FLAG_NONE,
                0, 0);

        symbolName = "foo(String)";
        Symbol fooSymbol3 = innerTable.get(symbolName);
        checkSymbol(ensureIsa(fooSymbol3, ParameterizedSymbol.class), symbolName, Type.METHOD, FLAG_NONE,
                1, 1);
    }

    /**
     * Tests formal parameter list in a method.
     */
    @Test
    public void testFormalParameterList() {
        TopLevelSymbolTable topLevel = createTopLevelSymbolTableNoErrors("""
                class HasMember {
                    String foo(String first, String last, Integer age, String ssn) {}
                }
                """);
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
        for (int i = 0; i < 4; i++) {
            Symbol param = parameters.get(i);
            checkSymbol(param, expNames.get(i), PARAMETER, expFlags.get(i));
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
     * @return A <code>TopLevelSymbolTable</code>.
     */
    public static TopLevelSymbolTable createTopLevelSymbolTableNoErrors(String code) {
        ClassesSymbolCreator creator = getClassesSymbolCreator();
        TopLevelSymbolTable table = createTopLevelSymbolTable(code, creator);
        ensureNoErrors(table, creator);
        return table;
    }

    /**
     * Helper method to get a <code>TopLevelSymbolTable</code> for a top level
     * type declaration directly from code.  Expects the given number of errors.
     * @param code The code for the top level type declaration.
     * @param expNumErrors The number of errors expected.
     */
    public static void createTopLevelSymbolTableWithErrors(String code, int expNumErrors) {
        ClassesSymbolCreator creator = getClassesSymbolCreator();
        TopLevelSymbolTable table = createTopLevelSymbolTable(code, creator);
        expectError(table, creator, expNumErrors);
    }

    private static TopLevelSymbolTable createTopLevelSymbolTable(String code, ClassesSymbolCreator creator) {
        TopLevelParser topLevelParser = ParserTopLevelTest.getTopLevelParser(code);
        ClassesParser classesParser = topLevelParser.getClassesParser();
        ASTTypeDeclaration typeDecl = topLevelParser.parseTypeDeclaration();
        TopLevelSymbolTable parent = new TopLevelSymbolTable();
        creator.createSymbolsForTopLevelTypeDeclaration(typeDecl, parent);
        return parent;
    }
}
