package org.spruce.compiler.test.symbol;

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.ast.classes.ASTMember;
import org.spruce.compiler.common.BaseMessageProducer;
import org.spruce.compiler.parser.ClassesParser;
import org.spruce.compiler.symbol.ClassesSymbolCreator;
import org.spruce.compiler.symbol.ParameterizedSymbol;
import org.spruce.compiler.symbol.ParentSymbol;
import org.spruce.compiler.symbol.StatementsSymbolCreator;
import org.spruce.compiler.symbol.Symbol;
import org.spruce.compiler.symbol.SymbolCreator;
import org.spruce.compiler.symbol.SymbolTable;
import org.spruce.compiler.test.parser.ParserClassesTest;

import static org.spruce.compiler.symbol.Symbol.*;
import static org.spruce.compiler.symbol.SymbolTable.Scope.*;
import static org.spruce.compiler.test.symbol.SymbolCreatorTestUtility.*;
import static org.spruce.compiler.test.util.TestUtility.ensureIsa;

/**
 * All tests for the statements symbol creator.
 */
public class SymbolCreatorStatementsTest {

    /**
     * Tests local variable declarations in a method.
     */
    @Test
    public void testMethodBlockLocalVarDecls() {
        String code = """
                String testing(String one, String two) {
                    String three = one + two;
                    mut Int four = 4;
                    var Int five = 5;
                    var mut Int six = 6;
                    Int seven = 7;
                }
                """;
        SymbolTable table = createMemberSymbolTableNoErrors(code, ClassesParser::parseClassPart);
        checkSymbolTable(table, TYPE, 1, List.of("testing(String,String)"));
        Symbol symbol = table.get("testing(String,String)");

        ParameterizedSymbol paraSymbol = ensureIsa(symbol, ParameterizedSymbol.class);
        checkSymbol(paraSymbol, "testing(String,String)", Type.METHOD, FLAG_ACCESS_PUBLIC,
                7, 2);

        SymbolTable innerTable = paraSymbol.getTable();
        checkSymbolTable(innerTable, MEMBER, 7, List.of("one", "two", "three", "four", "five", "six", "seven"));

        Symbol three = innerTable.get("three");
        checkSymbol(three, "three", Type.LOCAL, FLAG_NONE);

        Symbol four = innerTable.get("four");
        checkSymbol(four, "four", Type.LOCAL, FLAG_VARIABLE_MUT);

        Symbol five = innerTable.get("five");
        checkSymbol(five, "five", Type.LOCAL, FLAG_VARIABLE_VAR);

        Symbol six = innerTable.get("six");
        checkSymbol(six, "six", Type.LOCAL, FLAG_VARIABLE_MUT | FLAG_VARIABLE_VAR);

        Symbol seven = innerTable.get("seven");
        checkSymbol(seven, "seven", Type.LOCAL, FLAG_NONE);
    }

    /**
     * Tests nested block.
     */
    @Test
    public void testNestedBlock() {
        String code = """
                String nestedBlock() {
                    String foo = "foo";
                    {
                        String bar = "bar";
                    }
                    Baz baz = new Baz();
                }
                """;
        SymbolTable table = createMemberSymbolTableNoErrors(code, ClassesParser::parseClassPart);
        checkSymbolTable(table, TYPE, 1, List.of("nestedBlock()"));
        Symbol symbol = table.get("nestedBlock()");

        ParameterizedSymbol paraSymbol = ensureIsa(symbol, ParameterizedSymbol.class);
        checkSymbol(paraSymbol, "nestedBlock()", Type.METHOD, FLAG_ACCESS_PUBLIC,
                3, 0);

        SymbolTable innerTable = paraSymbol.getTable();
        checkSymbolTable(innerTable, MEMBER, 3, List.of("foo", "<block0>", "baz"));

        Symbol foo = innerTable.get("foo");
        checkSymbol(foo, "foo", Type.LOCAL, FLAG_NONE);

        ParentSymbol scope0 = ensureIsa(innerTable.get("<block0>"), ParentSymbol.class);
        checkSymbol(scope0, "<block0>", Type.BLOCK, FLAG_NONE, 1);

        SymbolTable scope0Table = scope0.getTable();
        checkSymbolTable(scope0Table, SCOPE, 1, List.of("bar"));

        Symbol bar = scope0Table.get("bar");
        checkSymbol(bar, "bar", Type.LOCAL, FLAG_NONE);

        Symbol baz = innerTable.get("baz");
        checkSymbol(baz, "baz", Type.LOCAL, FLAG_NONE);
    }

    /**
     * Tests nested block with variable redeclaration errors.
     */
    @Test
    public void testNestedBlockRedeclarationError() {
        String code = """
                void nestedBlock(Int bar) {
                    String foo = "foo";
                    {
                        String foo = "error";
                    }
                    Bar bar = new Bar();
                }
                """;
        createMemberSymbolTableWithErrors(code, ClassesParser::parseClassPart, 2);
    }

    /**
     * Tests basic for statement.
     */
    @Test
    public void testBasicForStatement() {
        String code = """
                String basicForStmt() {
                    for (mut Int i = 0, j = 0; i < 10; i++, j++) {
                        String output = "i: " + i + ", j: " + j;
                        stdout.println(output);
                    }
                }
                """;
        SymbolTable table = createMemberSymbolTableNoErrors(code, ClassesParser::parseClassPart);
        checkSymbolTable(table, TYPE, 1, List.of("basicForStmt()"));
        Symbol symbol = table.get("basicForStmt()");

        ParameterizedSymbol paraSymbol = ensureIsa(symbol, ParameterizedSymbol.class);
        checkSymbol(paraSymbol, "basicForStmt()", Type.METHOD, FLAG_ACCESS_PUBLIC,
                1, 0);

        SymbolTable innerTable = paraSymbol.getTable();
        checkSymbolTable(innerTable, MEMBER, 1, List.of("<for0>"));

        ParentSymbol for0 = ensureIsa(innerTable.get("<for0>"), ParentSymbol.class);
        checkSymbol(for0, "<for0>", Type.FOR_STMT, FLAG_NONE, 3);

        SymbolTable for0Table = for0.getTable();
        checkSymbolTable(for0Table, SCOPE, 3, List.of("i", "j", "output"));

        Symbol i = for0Table.get("i");
        checkSymbol(i, "i", Type.LOCAL, FLAG_VARIABLE_MUT);

        Symbol j = for0Table.get("j");
        checkSymbol(j, "j", Type.LOCAL, FLAG_VARIABLE_MUT);

        Symbol output = for0Table.get("output");
        checkSymbol(output, "output", Type.LOCAL, FLAG_NONE);
    }

    /**
     * Tests enhanced for statement.
     */
    @Test
    public void testEnhancedForStatement() {
        String code = """
                String enhancedForStmt() {
                    for (String line in getLines()) {
                        String output = "line: " + line;
                        stdout.println(output);
                    }
                }
                """;
        SymbolTable table = createMemberSymbolTableNoErrors(code, ClassesParser::parseClassPart);
        checkSymbolTable(table, TYPE, 1, List.of("enhancedForStmt()"));
        Symbol symbol = table.get("enhancedForStmt()");

        ParameterizedSymbol paraSymbol = ensureIsa(symbol, ParameterizedSymbol.class);
        checkSymbol(paraSymbol, "enhancedForStmt()", Type.METHOD, FLAG_ACCESS_PUBLIC,
                1, 0);

        SymbolTable innerTable = paraSymbol.getTable();
        checkSymbolTable(innerTable, MEMBER, 1, List.of("<for0>"));

        ParentSymbol for0 = ensureIsa(innerTable.get("<for0>"), ParentSymbol.class);
        checkSymbol(for0, "<for0>", Type.FOR_STMT, FLAG_NONE, 2);

        SymbolTable for0Table = for0.getTable();
        checkSymbolTable(for0Table, SCOPE, 2, List.of("line", "output"));

        Symbol line = for0Table.get("line");
        checkSymbol(line, "line", Type.LOCAL, FLAG_NONE);

        Symbol output = for0Table.get("output");
        checkSymbol(output, "output", Type.LOCAL, FLAG_NONE);
    }

    /**
     * Tests if statement.
     */
    @Test
    public void testIfStatement() {
        String code = """
                String ifStmt() {
                    if { String? line = nextLine() } line isa Value {
                        String output = "line: " + line;
                        stdout.println(output);
                    }
                }
                """;
        SymbolTable table = createMemberSymbolTableNoErrors(code, ClassesParser::parseClassPart);
        checkSymbolTable(table, TYPE, 1, List.of("ifStmt()"));
        Symbol symbol = table.get("ifStmt()");

        ParameterizedSymbol paraSymbol = ensureIsa(symbol, ParameterizedSymbol.class);
        checkSymbol(paraSymbol, "ifStmt()", Type.METHOD, FLAG_ACCESS_PUBLIC,
                1, 0);

        SymbolTable innerTable = paraSymbol.getTable();
        checkSymbolTable(innerTable, MEMBER, 1, List.of("<if0>"));

        ParentSymbol if0 = ensureIsa(innerTable.get("<if0>"), ParentSymbol.class);
        checkSymbol(if0, "<if0>", Type.IF_STMT, FLAG_NONE, 2);

        SymbolTable if0Table = if0.getTable();
        checkSymbolTable(if0Table, SCOPE, 2, List.of("line", "<if0_block0>"));

        Symbol line = if0Table.get("line");
        checkSymbol(line, "line", Type.LOCAL, FLAG_NONE);

        ParentSymbol if0_block0 = ensureIsa(if0Table.get("<if0_block0>"), ParentSymbol.class);
        checkSymbol(if0_block0, "<if0_block0>", Type.BLOCK, FLAG_NONE);

        SymbolTable if0_block0Table = if0_block0.getTable();
        checkSymbolTable(if0_block0Table, SCOPE, 1, List.of("output"));

        Symbol output = if0_block0Table.get("output");
        checkSymbol(output, "output", Type.LOCAL, FLAG_NONE);
    }

    /**
     * Tests if-else statement.
     */
    @Test
    public void testIfStatementElse() {
        String code = """
                String ifElseStmt() {
                    if { String? line = nextLine() } line isa Value {
                        String output = "line: " + line;
                        stdout.println(output);
                    }
                    else {
                        stdout.println("Line DNE!");
                    }
                }
                """;
        SymbolTable table = createMemberSymbolTableNoErrors(code, ClassesParser::parseClassPart);
        checkSymbolTable(table, TYPE, 1, List.of("ifElseStmt()"));
        Symbol symbol = table.get("ifElseStmt()");

        ParameterizedSymbol paraSymbol = ensureIsa(symbol, ParameterizedSymbol.class);
        checkSymbol(paraSymbol, "ifElseStmt()", Type.METHOD, FLAG_ACCESS_PUBLIC,
                1, 0);

        SymbolTable innerTable = paraSymbol.getTable();
        checkSymbolTable(innerTable, MEMBER, 1, List.of("<if0>"));

        ParentSymbol if0 = ensureIsa(innerTable.get("<if0>"), ParentSymbol.class);
        checkSymbol(if0, "<if0>", Type.IF_STMT, FLAG_NONE, 3);

        SymbolTable if0Table = if0.getTable();
        checkSymbolTable(if0Table, SCOPE, 3, List.of("line", "<if0_block0>", "<if0_block1>"));

        Symbol line = if0Table.get("line");
        checkSymbol(line, "line", Type.LOCAL, FLAG_NONE);

        ParentSymbol if0_block0 = ensureIsa(if0Table.get("<if0_block0>"), ParentSymbol.class);
        checkSymbol(if0_block0, "<if0_block0>", Type.BLOCK, FLAG_NONE);

        SymbolTable if0_block0Table = if0_block0.getTable();
        checkSymbolTable(if0_block0Table, SCOPE, 1, List.of("output"));

        Symbol output = if0_block0Table.get("output");
        checkSymbol(output, "output", Type.LOCAL, FLAG_NONE);

        ParentSymbol if0_block1 = ensureIsa(if0Table.get("<if0_block1>"), ParentSymbol.class);
        checkSymbol(if0_block1, "<if0_block1>", Type.BLOCK, FLAG_NONE);

        checkSymbolTable(if0_block1.getTable(), SCOPE, 0, List.of());
    }

    /**
     * Tests if-else-if statement.
     */
    @Test
    public void testIfStatementElseIf() {
        String code = """
                String ifElseStmt() {
                    if { String? line = nextLine() } line isa Value {
                        String output = "line: " + line;
                        stdout.println(output);
                    }
                    else if { Int someOtherCondition = get() } someOtherCondition == 1 {
                        String otherCondition = "otherCondition";
                        stdout.println(otherCondition);
                    }
                    else {
                        String dummy = "dummy";
                        stdout.println("Line DNE!");
                    }
                }
                """;
        SymbolTable table = createMemberSymbolTableNoErrors(code, ClassesParser::parseClassPart);
        checkSymbolTable(table, TYPE, 1, List.of("ifElseStmt()"));
        Symbol symbol = table.get("ifElseStmt()");

        ParameterizedSymbol paraSymbol = ensureIsa(symbol, ParameterizedSymbol.class);
        checkSymbol(paraSymbol, "ifElseStmt()", Type.METHOD, FLAG_ACCESS_PUBLIC,
                1, 0);

        SymbolTable innerTable = paraSymbol.getTable();
        checkSymbolTable(innerTable, MEMBER, 1, List.of("<if0>"));

        // if
        ParentSymbol if0 = ensureIsa(innerTable.get("<if0>"), ParentSymbol.class);
        checkSymbol(if0, "<if0>", Type.IF_STMT, FLAG_NONE, 3);

        SymbolTable if0Table = if0.getTable();
        checkSymbolTable(if0Table, SCOPE, 3, List.of("line", "<if0_block0>", "<if0_if1>"));

        Symbol line = if0Table.get("line");
        checkSymbol(line, "line", Type.LOCAL, FLAG_NONE);

        // if block
        ParentSymbol if0_block0 = ensureIsa(if0Table.get("<if0_block0>"), ParentSymbol.class);
        checkSymbol(if0_block0, "<if0_block0>", Type.BLOCK, FLAG_NONE);

        SymbolTable if0_block0Table = if0_block0.getTable();
        checkSymbolTable(if0_block0Table, SCOPE, 1, List.of("output"));

        Symbol output = if0_block0Table.get("output");
        checkSymbol(output, "output", Type.LOCAL, FLAG_NONE);

        // else if
        ParentSymbol if0_if1 = ensureIsa(if0Table.get("<if0_if1>"), ParentSymbol.class);
        checkSymbol(if0_if1, "<if0_if1>", Type.IF_STMT, FLAG_NONE);

        SymbolTable if0_if1Table = if0_if1.getTable();
        checkSymbolTable(if0_if1Table, SCOPE, 3, List.of("someOtherCondition", "<if0_if1_block0>", "<if0_if1_block1>"));

        Symbol if0_if1_someOtherCondition = if0_if1Table.get("someOtherCondition");
        checkSymbol(if0_if1_someOtherCondition, "someOtherCondition", Type.LOCAL, FLAG_NONE);

        // else if block
        ParentSymbol if0_if1_block0 = ensureIsa(if0_if1Table.get("<if0_if1_block0>"), ParentSymbol.class);
        checkSymbol(if0_if1_block0, "<if0_if1_block0>", Type.BLOCK, FLAG_NONE);

        SymbolTable if0_if1_block0Table = if0_if1_block0.getTable();
        checkSymbolTable(if0_if1_block0Table, SCOPE, 1, List.of("otherCondition"));

        Symbol otherCondition = if0_if1_block0Table.get("otherCondition");
        checkSymbol(otherCondition, "otherCondition", Type.LOCAL, FLAG_NONE);

        // else block
        ParentSymbol if0_if1_block1 = ensureIsa(if0_if1Table.get("<if0_if1_block1>"), ParentSymbol.class);
        checkSymbol(if0_if1_block1, "<if0_if1_block1>", Type.BLOCK, FLAG_NONE);

        SymbolTable if0_if1_block1Table = if0_if1_block1.getTable();
        checkSymbolTable(if0_if1_block1Table, SCOPE, 1, List.of("dummy"));

        Symbol dummy = if0_if1_block1Table.get("dummy");
        checkSymbol(dummy, "dummy", Type.LOCAL, FLAG_NONE);
    }

    /**
     * Tests switch statement of case constants and default.
     */
    @Test
    public void testSwitchStatementCaseAndDefault() {
        String code = """
                String switchStmt() {
                    switch (light) {
                        case RED -> {
                            String color = "red";
                            stop(color);
                        }
                        case YELLOW -> slowDown();
                        case GREEN -> go();
                        default -> throw new RuntimeException("Unexpected case!");
                    }
                }
                """;
        SymbolTable table = createMemberSymbolTableNoErrors(code, ClassesParser::parseClassPart);
        checkSymbolTable(table, TYPE, 1, List.of("switchStmt()"));
        Symbol symbol = table.get("switchStmt()");

        ParameterizedSymbol paraSymbol = ensureIsa(symbol, ParameterizedSymbol.class);
        checkSymbol(paraSymbol, "switchStmt()", Type.METHOD, FLAG_ACCESS_PUBLIC,
                4, 0);

        SymbolTable innerTable = paraSymbol.getTable();
        checkSymbolTable(innerTable, MEMBER, 4,
                List.of("<switch0_rule0>", "<switch0_rule1>", "<switch0_rule2>", "<switch0_rule3>"));

        // RED
        ParentSymbol rule0 = ensureIsa(innerTable.get("<switch0_rule0>"), ParentSymbol.class);
        checkSymbol(rule0, "<switch0_rule0>", Type.BLOCK, FLAG_NONE, 1);

        SymbolTable rule0Table = rule0.getTable();
        checkSymbolTable(rule0Table, SCOPE, 1, List.of("color"));
        checkSymbol(rule0Table.get("color"), "color", Type.LOCAL, FLAG_NONE);

        // YELLOW
        ParentSymbol rule1 = ensureIsa(innerTable.get("<switch0_rule1>"), ParentSymbol.class);
        checkSymbol(rule1, "<switch0_rule1>", Type.BLOCK, FLAG_NONE, 0);
        checkSymbolTable(rule1.getTable(), SCOPE, 0, List.of());

        // GREEN
        ParentSymbol rule2 = ensureIsa(innerTable.get("<switch0_rule2>"), ParentSymbol.class);
        checkSymbol(rule2, "<switch0_rule2>", Type.BLOCK, FLAG_NONE, 0);
        checkSymbolTable(rule2.getTable(), SCOPE, 0, List.of());

        // default
        ParentSymbol rule3 = ensureIsa(innerTable.get("<switch0_rule3>"), ParentSymbol.class);
        checkSymbol(rule3, "<switch0_rule3>", Type.BLOCK, FLAG_NONE, 0);
        checkSymbolTable(rule3.getTable(), SCOPE, 0, List.of());
    }

    /**
     * Tests switch statement of patterns.
     */
    @Test
    public void testSwitchStatementPatterns() {
        String code = """
                String switchStmt() {
                    switch (obj) {
                        Person(String first, String last, Int age) -> {
                            String event = "Person";
                            stdout.println(event + ": " + last + ", " + first + "(" + age + ")");
                        }
                        Employee(Person person, Department(String deptName, BusinessUnit unit) -> {
                            String event = "Employee";
                            stdout.println(event + ": " + person + " in " + deptName + "(" + unit + ")");
                        }
                    }
                }
                """;
        SymbolTable table = createMemberSymbolTableNoErrors(code, ClassesParser::parseClassPart);
        checkSymbolTable(table, TYPE, 1, List.of("switchStmt()"));
        Symbol symbol = table.get("switchStmt()");

        ParameterizedSymbol paraSymbol = ensureIsa(symbol, ParameterizedSymbol.class);
        checkSymbol(paraSymbol, "switchStmt()", Type.METHOD, FLAG_ACCESS_PUBLIC,
                2, 0);

        SymbolTable innerTable = paraSymbol.getTable();
        checkSymbolTable(innerTable, MEMBER, 2, List.of("<switch0_rule0>", "<switch0_rule1>"));

        // Person
        ParentSymbol rule0 = ensureIsa(innerTable.get("<switch0_rule0>"), ParentSymbol.class);
        checkSymbol(rule0, "<switch0_rule0>", Type.BLOCK, FLAG_NONE);

        SymbolTable rule0Table = rule0.getTable();
        checkSymbolTable(rule0Table, SCOPE, 4, List.of("first", "last", "age", "event"));
        checkSymbol(rule0Table.get("first"), "first", Type.PATTERN, FLAG_NONE);
        checkSymbol(rule0Table.get("last"), "last", Type.PATTERN, FLAG_NONE);
        checkSymbol(rule0Table.get("age"), "age", Type.PATTERN, FLAG_NONE);
        checkSymbol(rule0Table.get("event"), "event", Type.LOCAL, FLAG_NONE);

        // Employee
        ParentSymbol rule1 = ensureIsa(innerTable.get("<switch0_rule1>"), ParentSymbol.class);
        checkSymbol(rule1, "<switch0_rule1>", Type.BLOCK, FLAG_NONE);

        SymbolTable rule1Table = rule1.getTable();
        checkSymbolTable(rule1Table, SCOPE, 4, List.of("person", "deptName", "unit", "event"));
        checkSymbol(rule1Table.get("person"), "person", Type.PATTERN, FLAG_NONE);
        checkSymbol(rule1Table.get("deptName"), "deptName", Type.PATTERN, FLAG_NONE);
        checkSymbol(rule1Table.get("unit"), "unit", Type.PATTERN, FLAG_NONE);
        checkSymbol(rule1Table.get("event"), "event", Type.LOCAL, FLAG_NONE);
    }

    /**
     * Tests try statement with resources.
     */
    @Test
    public void testTryStatement() {
        String code = """
                String tryStmt() {
                    try (Connection conn = getConnection()) {
                        Statement stmt = conn.createStatement();
                        String foo = "foo1";
                    }
                    catch (IOException e) {
                        String foo = "foo2";
                    }
                    catch (Exception e) {
                        String foo = "foo3";
                    }
                    finally {
                        String foo = "foo4";
                    }
                }
                """;
        SymbolTable table = createMemberSymbolTableNoErrors(code, ClassesParser::parseClassPart);
        checkSymbolTable(table, TYPE, 1, List.of("tryStmt()"));
        Symbol symbol = table.get("tryStmt()");

        ParameterizedSymbol paraSymbol = ensureIsa(symbol, ParameterizedSymbol.class);
        checkSymbol(paraSymbol, "tryStmt()", Type.METHOD, FLAG_ACCESS_PUBLIC,
                1, 0);

        SymbolTable paraTable = paraSymbol.getTable();
        checkSymbolTable(paraTable, MEMBER, 1, List.of("<try0>"));

        ParentSymbol trySymbol = ensureIsa(paraTable.get("<try0>"), ParentSymbol.class);
        checkSymbol(trySymbol, "<try0>", Type.TRY_STMT, FLAG_NONE, 4);

        SymbolTable tryTable = trySymbol.getTable();
        checkSymbolTable(tryTable, SCOPE, 4,
                List.of("<try0_block0>", "<try0_catch1>", "<try0_catch2>", "<try0_block3>"));

        ParentSymbol block0 = ensureIsa(tryTable.get("<try0_block0>"), ParentSymbol.class);
        checkSymbol(block0, "<try0_block0>", Type.BLOCK, FLAG_NONE, 3);

        SymbolTable block0Table = block0.getTable();
        checkSymbolTable(block0Table, SCOPE, 3, List.of("conn", "stmt", "foo"));

        checkSymbol(block0Table.get("conn"), "conn", Type.LOCAL, FLAG_NONE);
        checkSymbol(block0Table.get("stmt"), "stmt", Type.LOCAL, FLAG_NONE);
        checkSymbol(block0Table.get("foo"), "foo", Type.LOCAL, FLAG_NONE);

        ParameterizedSymbol catch1 = ensureIsa(tryTable.get("<try0_catch1>"), ParameterizedSymbol.class);
        checkSymbol(catch1, "<try0_catch1>", Type.CATCH, FLAG_NONE, 2, 1);

        SymbolTable catch1Table = catch1.getTable();
        checkSymbolTable(catch1Table, SCOPE, 2, List.of("e", "foo"));
        checkSymbol(catch1Table.get("e"), "e", Type.PARAMETER, FLAG_NONE);
        checkSymbol(catch1Table.get("foo"), "foo", Type.LOCAL, FLAG_NONE);

        ParameterizedSymbol catch2 = ensureIsa(tryTable.get("<try0_catch2>"), ParameterizedSymbol.class);
        checkSymbol(catch2, "<try0_catch2>", Type.CATCH, FLAG_NONE, 2, 1);

        SymbolTable catch2Table = catch2.getTable();
        checkSymbolTable(catch2Table, SCOPE, 2, List.of("e", "foo"));
        checkSymbol(catch2Table.get("e"), "e", Type.PARAMETER, FLAG_NONE);
        checkSymbol(catch2Table.get("foo"), "foo", Type.LOCAL, FLAG_NONE);

        ParentSymbol block3 = ensureIsa(tryTable.get("<try0_block3>"), ParentSymbol.class);
        checkSymbol(block3, "<try0_block3>", Type.FINALLY, FLAG_NONE);

        SymbolTable block3Table = block3.getTable();
        checkSymbolTable(block3Table, SCOPE, 1, List.of("foo"));
        checkSymbol(block3Table.get("foo"), "foo", Type.LOCAL, FLAG_NONE);
    }

    /**
     * Tests while statement.
     */
    @Test
    public void testWhileStatement() {
        String code = """
                String whileStmt() {
                    while {String line = br.readLine()} (!(line isa None)) {
                        Int lineNbr = 0;
                        stdout.println(line);
                    }
                }
                """;
        SymbolTable table = createMemberSymbolTableNoErrors(code, ClassesParser::parseClassPart);
        checkSymbolTable(table, TYPE, 1, List.of("whileStmt()"));
        Symbol symbol = table.get("whileStmt()");

        ParameterizedSymbol paraSymbol = ensureIsa(symbol, ParameterizedSymbol.class);
        checkSymbol(paraSymbol, "whileStmt()", Type.METHOD, FLAG_ACCESS_PUBLIC,
                1, 0);

        SymbolTable paraTable = paraSymbol.getTable();
        checkSymbolTable(paraTable, MEMBER, 1, List.of("<while0>"));

        ParentSymbol whileSymbol = ensureIsa(paraTable.get("<while0>"), ParentSymbol.class);
        checkSymbol(whileSymbol, "<while0>", Type.WHILE_STMT, FLAG_NONE, 2);

        SymbolTable whileTable = whileSymbol.getTable();
        checkSymbolTable(whileTable, SCOPE, 2, List.of("line", "lineNbr"));

        checkSymbol(whileTable.get("line"), "line", Type.LOCAL, FLAG_NONE);
        checkSymbol(whileTable.get("lineNbr"), "lineNbr", Type.LOCAL, FLAG_NONE);
    }

    /**
     * Helper method to get a <code>StatementsSymbolCreator</code>.
     * @return A <code>StatementsSymbolCreator</code>.
     */
    public static StatementsSymbolCreator getStatementsSymbolCreator() {
        return new SymbolCreator(new BaseMessageProducer()).getStatementsSymbolCreator();
    }

    /**
     * Helper method to get a <code>SymbolTable</code> for a member declaration
     * directly from code.  Ensures no errors.
     * @param code The code for the member declaration.
     * @param memberParser A <code>Function</code> on a <code>ClassesParser</code>
     *                    that parses a particular member production.
     * @return A <code>SymbolTable</code> for the member declaration.
     */
    public static SymbolTable createMemberSymbolTableNoErrors(String code,
                                                              Function<ClassesParser, ? extends ASTMember> memberParser) {
        ClassesSymbolCreator creator = SymbolCreatorClassesTest.getClassesSymbolCreator();
        SymbolTable table = createMemberSymbolTable(code, creator, memberParser);
        ensureNoErrors(table, creator);
        return table;
    }

    /**
     * Helper method to get a <code>SymbolTable</code> for a member declaration
     * directly from code.  Expects the given number of errors.
     * @param code The code for the member declaration.
     * @param memberParser A <code>Function</code> on a <code>ClassesParser</code>
     *                    that parses a particular member production.
     * @param expNumErrors The number of errors expected.
     */
    public static void createMemberSymbolTableWithErrors(String code,
                                                         Function<ClassesParser, ? extends ASTMember> memberParser,
                                                         int expNumErrors) {
        ClassesSymbolCreator creator = SymbolCreatorClassesTest.getClassesSymbolCreator();
        SymbolTable table = createMemberSymbolTable(code, creator, memberParser);
        expectError(table, creator, expNumErrors);
    }

    private static SymbolTable createMemberSymbolTable(String code, ClassesSymbolCreator creator,
                                                       Function<ClassesParser, ? extends ASTMember> memberParser) {
        ClassesParser classesParser = ParserClassesTest.getClassesParser(code);
        ASTMember member = memberParser.apply(classesParser);
        SymbolTable parent = new SymbolTable(SymbolTable.Scope.TYPE);
        // Only one symbol expected --
        creator.createSymbolsForMember(parent, member);
        return parent;
    }
}
