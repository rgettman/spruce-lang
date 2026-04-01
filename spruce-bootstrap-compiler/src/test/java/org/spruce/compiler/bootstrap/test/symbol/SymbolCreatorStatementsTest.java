package org.spruce.compiler.bootstrap.test.symbol;

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.bootstrap.ast.classes.ASTMember;
import org.spruce.compiler.bootstrap.ast.classes.ASTMethodDeclaration;
import org.spruce.compiler.bootstrap.ast.statements.ASTBasicForStatement;
import org.spruce.compiler.bootstrap.ast.statements.ASTBlock;
import org.spruce.compiler.bootstrap.ast.statements.ASTBlockStatement;
import org.spruce.compiler.bootstrap.ast.statements.ASTBlockStatements;
import org.spruce.compiler.bootstrap.ast.statements.ASTEnhancedForStatement;
import org.spruce.compiler.bootstrap.ast.statements.ASTIfStatement;
import org.spruce.compiler.bootstrap.ast.statements.ASTInit;
import org.spruce.compiler.bootstrap.ast.statements.ASTLocalVariableDeclaration;
import org.spruce.compiler.bootstrap.ast.statements.ASTLocalVariableDeclarationStatement;
import org.spruce.compiler.bootstrap.ast.statements.ASTVariableDeclaratorList;
import org.spruce.compiler.bootstrap.ast.statements.ASTWhileStatement;
import org.spruce.compiler.bootstrap.common.BaseMessageProducer;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.parser.ClassesParser;
import org.spruce.compiler.bootstrap.symbol.ClassesSymbolCreator;
import org.spruce.compiler.bootstrap.symbol.DataType;
import org.spruce.compiler.bootstrap.symbol.ParameterizedSymbol;
import org.spruce.compiler.bootstrap.symbol.ParentSymbol;
import org.spruce.compiler.bootstrap.symbol.StatementsSymbolCreator;
import org.spruce.compiler.bootstrap.symbol.Symbol;
import org.spruce.compiler.bootstrap.symbol.SymbolCreator;
import org.spruce.compiler.bootstrap.symbol.SymbolTable;
import org.spruce.compiler.bootstrap.symbol.TypeLookup;
import org.spruce.compiler.bootstrap.test.parser.ParserClassesTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.spruce.compiler.bootstrap.symbol.Symbol.*;
import static org.spruce.compiler.bootstrap.symbol.SymbolTable.Scope.*;
import static org.spruce.compiler.bootstrap.test.symbol.SymbolCreatorTestUtility.*;
import static org.spruce.compiler.bootstrap.test.util.TestUtility.ensureIsa;

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
                    Int four = 4;
                    Int five = 5;
                    Int six = 6;
                    Int seven = 7;
                }
                """;
        Pair pair = createMemberSymbolTableNoErrors(code, ClassesParser::parseClassPart);
        SymbolTable table = pair.table();
        ASTMethodDeclaration testingMethod = ensureIsa(pair.member(), ASTMethodDeclaration.class);
        assertTrue(testingMethod.getBody().getBlock().isPresent());
        List<ASTLocalVariableDeclarationStatement> varDeclStmts =
                testingMethod.getBody().getBlock().get().getBlockStmts().getTypedChildren().stream()
                        .filter(bs -> bs instanceof ASTLocalVariableDeclarationStatement)
                        .map(ASTLocalVariableDeclarationStatement.class::cast)
                        .toList();

        checkSymbolTable(table, TYPE, 1, List.of("testing(String,String)"));
        Symbol symbol = table.get("testing(String,String)");

        ParameterizedSymbol paraSymbol = ensureIsa(symbol, ParameterizedSymbol.class);
        DataType expDtString = new DataType("", "String");
        DataType expDtInt = new DataType("", "Int");
        checkSymbol(paraSymbol, "testing(String,String)", Kind.METHOD, expDtString, FLAG_NONE,
                7, 2);

        SymbolTable innerTable = paraSymbol.getTable();
        checkSymbolTable(innerTable, MEMBER, 7, List.of("one", "two", "three", "four", "five", "six", "seven"));

        Symbol three = innerTable.get("three");
        checkSymbol(three, "three", Kind.LOCAL, expDtString, FLAG_NONE);
        ASTLocalVariableDeclarationStatement stmt3 = varDeclStmts.get(0);
        assertSame(three, stmt3.getLocalVarDecl().getVarDeclList().get(0).getDeclSymbol());

        Symbol four = innerTable.get("four");
        checkSymbol(four, "four", Kind.LOCAL, expDtInt, FLAG_NONE);
        ASTLocalVariableDeclarationStatement stmt4 = varDeclStmts.get(1);
        assertSame(four, stmt4.getLocalVarDecl().getVarDeclList().get(0).getDeclSymbol());

        Symbol five = innerTable.get("five");
        checkSymbol(five, "five", Kind.LOCAL, expDtInt, FLAG_NONE);
        ASTLocalVariableDeclarationStatement stmt5 = varDeclStmts.get(2);
        assertSame(five, stmt5.getLocalVarDecl().getVarDeclList().get(0).getDeclSymbol());

        Symbol six = innerTable.get("six");
        checkSymbol(six, "six", Kind.LOCAL, expDtInt, FLAG_NONE);
        ASTLocalVariableDeclarationStatement stmt6 = varDeclStmts.get(3);
        assertSame(six, stmt6.getLocalVarDecl().getVarDeclList().get(0).getDeclSymbol());

        Symbol seven = innerTable.get("seven");
        checkSymbol(seven, "seven", Kind.LOCAL, expDtInt, FLAG_NONE);
        ASTLocalVariableDeclarationStatement stmt7 = varDeclStmts.get(4);
        assertSame(seven, stmt7.getLocalVarDecl().getVarDeclList().get(0).getDeclSymbol());
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
        Pair pair = createMemberSymbolTableNoErrors(code, ClassesParser::parseClassPart);
        SymbolTable table = pair.table();
        ASTMethodDeclaration testingMethod = ensureIsa(pair.member(), ASTMethodDeclaration.class);
        assertTrue(testingMethod.getBody().getBlock().isPresent());
        List<ASTBlockStatement> blockStmts = testingMethod.getBody().getBlock().get()
                .getBlockStmts().getTypedChildren();

        DataType expDtString = new DataType("", "String");
        DataType expDtBaz = new DataType("", "Baz");

        checkSymbolTable(table, TYPE, 1, List.of("nestedBlock()"));
        Symbol symbol = table.get("nestedBlock()");

        ParameterizedSymbol paraSymbol = ensureIsa(symbol, ParameterizedSymbol.class);
        checkSymbol(paraSymbol, "nestedBlock()", Kind.METHOD, expDtString, FLAG_NONE,
                3, 0);

        SymbolTable innerTable = paraSymbol.getTable();
        checkSymbolTable(innerTable, MEMBER, 3, List.of("foo", "<block0>", "baz"));

        Symbol foo = innerTable.get("foo");
        checkSymbol(foo, "foo", Kind.LOCAL, expDtString, FLAG_NONE);
        ASTLocalVariableDeclarationStatement stmtFoo = ensureIsa(
                blockStmts.get(0), ASTLocalVariableDeclarationStatement.class);
        assertSame(foo, stmtFoo.getLocalVarDecl().getVarDeclList().get(0).getDeclSymbol());

        ParentSymbol scope0 = ensureIsa(innerTable.get("<block0>"), ParentSymbol.class);
        checkSymbol(scope0, "<block0>", Kind.BLOCK, DataType.NONE, FLAG_NONE, 1);
        ASTBlock innerBlock = ensureIsa(blockStmts.get(1), ASTBlock.class);
        assertSame(scope0, innerBlock.getDeclSymbol());

        SymbolTable scope0Table = scope0.getTable();
        checkSymbolTable(scope0Table, SCOPE, 1, List.of("bar"));

        Symbol bar = scope0Table.get("bar");
        checkSymbol(bar, "bar", Kind.LOCAL, expDtString, FLAG_NONE);
        ASTLocalVariableDeclarationStatement stmtBar = ensureIsa(
                innerBlock.getBlockStmts().get(0), ASTLocalVariableDeclarationStatement.class);
        assertSame(bar, stmtBar.getLocalVarDecl().getVarDeclList().get(0).getDeclSymbol());

        Symbol baz = innerTable.get("baz");
        checkSymbol(baz, "baz", Kind.LOCAL, expDtBaz, FLAG_NONE);
        ASTLocalVariableDeclarationStatement stmtBaz = ensureIsa(
                blockStmts.get(2), ASTLocalVariableDeclarationStatement.class);
        assertSame(baz, stmtBaz.getLocalVarDecl().getVarDeclList().get(0).getDeclSymbol());
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
                void basicForStmt() {
                    for (Int i = 0, j = 0; i < 10; i++, j++) {
                        String output = "i: " + i + ", j: " + j;
                        stdout.println(output);
                    }
                }
                """;
        Pair pair = createMemberSymbolTableNoErrors(code, ClassesParser::parseClassPart);
        SymbolTable table = pair.table();
        ASTMethodDeclaration testingMethod = ensureIsa(pair.member(), ASTMethodDeclaration.class);
        assertTrue(testingMethod.getBody().getBlock().isPresent());
        List<ASTBlockStatement> blockStmts = testingMethod.getBody().getBlock().get()
                .getBlockStmts().getTypedChildren();
        DataType expDtVoid = new DataType("", "void");
        DataType expDtInt = new DataType("", "Int");
        DataType expDtString = new DataType("", "String");

        checkSymbolTable(table, TYPE, 1, List.of("basicForStmt()"));
        Symbol symbol = table.get("basicForStmt()");

        ParameterizedSymbol paraSymbol = ensureIsa(symbol, ParameterizedSymbol.class);
        checkSymbol(paraSymbol, "basicForStmt()", Kind.METHOD, expDtVoid, FLAG_NONE,
                1, 0);

        SymbolTable innerTable = paraSymbol.getTable();
        checkSymbolTable(innerTable, MEMBER, 1, List.of("<for0>"));

        ParentSymbol for0 = ensureIsa(innerTable.get("<for0>"), ParentSymbol.class);
        checkSymbol(for0, "<for0>", Kind.FOR_STMT, DataType.NONE, FLAG_NONE, 3);
        assertTrue(testingMethod.getBody().getBlock().isPresent());
        ASTBasicForStatement basicForStmt = ensureIsa(blockStmts.get(0), ASTBasicForStatement.class);
        assertSame(for0, basicForStmt.getDeclSymbol());

        SymbolTable for0Table = for0.getTable();
        checkSymbolTable(for0Table, SCOPE, 3, List.of("i", "j", "output"));
        assertTrue(basicForStmt.getInit().isPresent());
        ASTInit init = basicForStmt.getInit().get();
        ASTLocalVariableDeclaration initDecl = ensureIsa(init, ASTLocalVariableDeclaration.class);
        ASTVariableDeclaratorList varDeclList = initDecl.getVarDeclList();

        Symbol i = for0Table.get("i");
        checkSymbol(i, "i", Kind.LOCAL, expDtInt, FLAG_NONE);
        assertSame(i, varDeclList.get(0).getDeclSymbol());

        Symbol j = for0Table.get("j");
        checkSymbol(j, "j", Kind.LOCAL, expDtInt, FLAG_NONE);
        assertSame(j, varDeclList.get(1).getDeclSymbol());

        Symbol output = for0Table.get("output");
        checkSymbol(output, "output", Kind.LOCAL, expDtString, FLAG_NONE);
        ASTBlockStatements forBlockStmts = basicForStmt.getBlock().getBlockStmts();
        ASTLocalVariableDeclarationStatement outputVarDeclStmt = ensureIsa(
                forBlockStmts.get(0), ASTLocalVariableDeclarationStatement.class);
        assertSame(output, outputVarDeclStmt.getLocalVarDecl().getVarDeclList().get(0).getDeclSymbol());
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
        Pair pair = createMemberSymbolTableNoErrors(code, ClassesParser::parseClassPart);
        SymbolTable table = pair.table();
        ASTMethodDeclaration testingMethod = ensureIsa(pair.member(), ASTMethodDeclaration.class);
        assertTrue(testingMethod.getBody().getBlock().isPresent());
        List<ASTBlockStatement> blockStmts = testingMethod.getBody().getBlock().get()
                .getBlockStmts().getTypedChildren();
        DataType expDtString = new DataType("", "String");

        checkSymbolTable(table, TYPE, 1, List.of("enhancedForStmt()"));
        Symbol symbol = table.get("enhancedForStmt()");

        ParameterizedSymbol paraSymbol = ensureIsa(symbol, ParameterizedSymbol.class);
        checkSymbol(paraSymbol, "enhancedForStmt()", Kind.METHOD, expDtString,
                FLAG_NONE, 1, 0);

        SymbolTable innerTable = paraSymbol.getTable();
        checkSymbolTable(innerTable, MEMBER, 1, List.of("<for0>"));

        ParentSymbol for0 = ensureIsa(innerTable.get("<for0>"), ParentSymbol.class);
        checkSymbol(for0, "<for0>", Kind.FOR_STMT, DataType.NONE, FLAG_NONE, 2);
        ASTEnhancedForStatement enhancedForStmt = ensureIsa(blockStmts.get(0), ASTEnhancedForStatement.class);
        assertSame(for0, enhancedForStmt.getDeclSymbol());

        SymbolTable for0Table = for0.getTable();
        checkSymbolTable(for0Table, SCOPE, 2, List.of("line", "output"));
        ASTLocalVariableDeclaration initDecl = enhancedForStmt.getLocalVarDecl();
        ASTVariableDeclaratorList varDeclList = initDecl.getVarDeclList();

        Symbol line = for0Table.get("line");
        checkSymbol(line, "line", Kind.LOCAL, expDtString, FLAG_NONE);
        assertSame(line, varDeclList.get(0).getDeclSymbol());

        Symbol output = for0Table.get("output");
        checkSymbol(output, "output", Kind.LOCAL, expDtString, FLAG_NONE);
        ASTBlockStatements forBlockStmts = enhancedForStmt.getBlock().getBlockStmts();
        ASTLocalVariableDeclarationStatement outputVarDeclStmt = ensureIsa(
                forBlockStmts.get(0), ASTLocalVariableDeclarationStatement.class);
        assertSame(output, outputVarDeclStmt.getLocalVarDecl().getVarDeclList().get(0).getDeclSymbol());
    }

    /**
     * Tests if statement.
     */
    @Test
    public void testIfStatement() {
        String code = """
                String ifStmt() {
                    if { String line = nextLine() } line isa Value {
                        String output = "line: " + line;
                        stdout.println(output);
                    }
                }
                """;
        Pair pair = createMemberSymbolTableNoErrors(code, ClassesParser::parseClassPart);
        SymbolTable table = pair.table();
        ASTMethodDeclaration testingMethod = ensureIsa(pair.member(), ASTMethodDeclaration.class);
        assertTrue(testingMethod.getBody().getBlock().isPresent());
        List<ASTBlockStatement> blockStmts = testingMethod.getBody().getBlock().get()
                .getBlockStmts().getTypedChildren();
        DataType expDtString = new DataType("", "String");

        checkSymbolTable(table, TYPE, 1, List.of("ifStmt()"));
        Symbol symbol = table.get("ifStmt()");

        ParameterizedSymbol paraSymbol = ensureIsa(symbol, ParameterizedSymbol.class);
        checkSymbol(paraSymbol, "ifStmt()", Kind.METHOD, expDtString, FLAG_NONE,
                1, 0);

        SymbolTable innerTable = paraSymbol.getTable();
        checkSymbolTable(innerTable, MEMBER, 1, List.of("<if0>"));

        ParentSymbol if0 = ensureIsa(innerTable.get("<if0>"), ParentSymbol.class);
        checkSymbol(if0, "<if0>", Kind.IF_STMT, DataType.NONE, FLAG_NONE, 2);
        ASTIfStatement ifStmt = ensureIsa(blockStmts.get(0), ASTIfStatement.class);
        assertSame(if0, ifStmt.getDeclSymbol());

        SymbolTable if0Table = if0.getTable();
        checkSymbolTable(if0Table, SCOPE, 2, List.of("line", "<if0_block0>"));
        assertTrue(ifStmt.getInit().isPresent());
        ASTInit init = ifStmt.getInit().get();
        ASTLocalVariableDeclaration initDecl = ensureIsa(init, ASTLocalVariableDeclaration.class);
        ASTVariableDeclaratorList varDeclList = initDecl.getVarDeclList();

        Symbol line = if0Table.get("line");
        checkSymbol(line, "line", Kind.LOCAL, expDtString, FLAG_NONE);
        assertSame(line, varDeclList.get(0).getDeclSymbol());

        ParentSymbol if0_block0 = ensureIsa(if0Table.get("<if0_block0>"), ParentSymbol.class);
        checkSymbol(if0_block0, "<if0_block0>", Kind.BLOCK, DataType.NONE, FLAG_NONE);
        ASTBlock ifBlock = ifStmt.getIfBlock();
        assertSame(if0_block0, ifBlock.getDeclSymbol());

        SymbolTable if0_block0Table = if0_block0.getTable();
        checkSymbolTable(if0_block0Table, SCOPE, 1, List.of("output"));

        Symbol output = if0_block0Table.get("output");
        checkSymbol(output, "output", Kind.LOCAL, expDtString, FLAG_NONE);
        List<ASTBlockStatement> ifBlockStmts = ifBlock.getBlockStmts().getTypedChildren();
        ASTLocalVariableDeclarationStatement outputVarDeclStmt = ensureIsa(ifBlockStmts.get(0),
                ASTLocalVariableDeclarationStatement.class);
        assertSame(output, outputVarDeclStmt.getLocalVarDecl().getVarDeclList().get(0).getDeclSymbol());
    }

    /**
     * Tests if-else statement.
     */
    @Test
    public void testIfStatementElse() {
        String code = """
                String ifElseStmt() {
                    if { String line = nextLine() } line isa Value {
                        String output = "line: " + line;
                        stdout.println(output);
                    }
                    else {
                        stdout.println("Line DNE!");
                    }
                }
                """;
        Pair pair = createMemberSymbolTableNoErrors(code, ClassesParser::parseClassPart);
        SymbolTable table = pair.table();
        ASTMethodDeclaration testingMethod = ensureIsa(pair.member(), ASTMethodDeclaration.class);
        assertTrue(testingMethod.getBody().getBlock().isPresent());
        List<ASTBlockStatement> blockStmts = testingMethod.getBody().getBlock().get()
                .getBlockStmts().getTypedChildren();
        DataType expDtString = new DataType("", "String");

        checkSymbolTable(table, TYPE, 1, List.of("ifElseStmt()"));
        Symbol symbol = table.get("ifElseStmt()");

        ParameterizedSymbol paraSymbol = ensureIsa(symbol, ParameterizedSymbol.class);
        checkSymbol(paraSymbol, "ifElseStmt()", Kind.METHOD, expDtString, FLAG_NONE,
                1, 0);

        SymbolTable innerTable = paraSymbol.getTable();
        checkSymbolTable(innerTable, MEMBER, 1, List.of("<if0>"));

        ParentSymbol if0 = ensureIsa(innerTable.get("<if0>"), ParentSymbol.class);
        checkSymbol(if0, "<if0>", Kind.IF_STMT, DataType.NONE, FLAG_NONE, 3);
        ASTIfStatement ifStmt = ensureIsa(blockStmts.get(0), ASTIfStatement.class);
        assertSame(if0, ifStmt.getDeclSymbol());

        SymbolTable if0Table = if0.getTable();
        checkSymbolTable(if0Table, SCOPE, 3, List.of("line", "<if0_block0>", "<if0_block1>"));
        assertTrue(ifStmt.getInit().isPresent());
        ASTInit init = ifStmt.getInit().get();
        ASTLocalVariableDeclaration initDecl = ensureIsa(init, ASTLocalVariableDeclaration.class);
        ASTVariableDeclaratorList varDeclList = initDecl.getVarDeclList();

        Symbol line = if0Table.get("line");
        checkSymbol(line, "line", Kind.LOCAL, expDtString, FLAG_NONE);
        assertSame(line, varDeclList.get(0).getDeclSymbol());

        ParentSymbol if0_block0 = ensureIsa(if0Table.get("<if0_block0>"), ParentSymbol.class);
        checkSymbol(if0_block0, "<if0_block0>", Kind.BLOCK, DataType.NONE, FLAG_NONE);
        ASTBlock ifBlock = ifStmt.getIfBlock();
        assertSame(if0_block0, ifBlock.getDeclSymbol());

        SymbolTable if0_block0Table = if0_block0.getTable();
        checkSymbolTable(if0_block0Table, SCOPE, 1, List.of("output"));

        Symbol output = if0_block0Table.get("output");
        checkSymbol(output, "output", Kind.LOCAL, expDtString, FLAG_NONE);
        List<ASTBlockStatement> ifBlockStmts = ifBlock.getBlockStmts().getTypedChildren();
        ASTLocalVariableDeclarationStatement outputVarDeclStmt = ensureIsa(ifBlockStmts.get(0),
                ASTLocalVariableDeclarationStatement.class);
        assertSame(output, outputVarDeclStmt.getLocalVarDecl().getVarDeclList().get(0).getDeclSymbol());

        ParentSymbol if0_block1 = ensureIsa(if0Table.get("<if0_block1>"), ParentSymbol.class);
        checkSymbol(if0_block1, "<if0_block1>", Kind.BLOCK, DataType.NONE, FLAG_NONE);

        checkSymbolTable(if0_block1.getTable(), SCOPE, 0, List.of());
        assertTrue(ifStmt.getElseBlock().isPresent());
        ASTBlock elseBlock = ifStmt.getElseBlock().get();
        assertSame(if0_block1, elseBlock.getDeclSymbol());
    }

    /**
     * Tests if-else-if statement.
     */
    @Test
    public void testIfStatementElseIf() {
        String code = """
                String ifElseStmt() {
                    if { String line = nextLine() } line isa Value {
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
        Pair pair = createMemberSymbolTableNoErrors(code, ClassesParser::parseClassPart);
        SymbolTable table = pair.table();
        ASTMethodDeclaration testingMethod = ensureIsa(pair.member(), ASTMethodDeclaration.class);
        assertTrue(testingMethod.getBody().getBlock().isPresent());
        List<ASTBlockStatement> blockStmts = testingMethod.getBody().getBlock().get()
                .getBlockStmts().getTypedChildren();
        DataType expDtString = new DataType("", "String");
        DataType expDtInt = new DataType("", "Int");

        checkSymbolTable(table, TYPE, 1, List.of("ifElseStmt()"));
        Symbol symbol = table.get("ifElseStmt()");

        ParameterizedSymbol paraSymbol = ensureIsa(symbol, ParameterizedSymbol.class);
        checkSymbol(paraSymbol, "ifElseStmt()", Kind.METHOD, expDtString, FLAG_NONE,
                1, 0);

        SymbolTable innerTable = paraSymbol.getTable();
        checkSymbolTable(innerTable, MEMBER, 1, List.of("<if0>"));

        // if
        ParentSymbol if0 = ensureIsa(innerTable.get("<if0>"), ParentSymbol.class);
        checkSymbol(if0, "<if0>", Kind.IF_STMT, DataType.NONE, FLAG_NONE, 3);
        ASTIfStatement ifStmt = ensureIsa(blockStmts.get(0), ASTIfStatement.class);
        assertSame(if0, ifStmt.getDeclSymbol());

        SymbolTable if0Table = if0.getTable();
        checkSymbolTable(if0Table, SCOPE, 3, List.of("line", "<if0_block0>", "<if0_if1>"));
        assertTrue(ifStmt.getInit().isPresent());
        ASTInit init = ifStmt.getInit().get();
        ASTLocalVariableDeclaration initDecl = ensureIsa(init, ASTLocalVariableDeclaration.class);
        ASTVariableDeclaratorList varDeclList = initDecl.getVarDeclList();

        Symbol line = if0Table.get("line");
        checkSymbol(line, "line", Kind.LOCAL, expDtString, FLAG_NONE);
        assertSame(line, varDeclList.get(0).getDeclSymbol());

        // if block
        ParentSymbol if0_block0 = ensureIsa(if0Table.get("<if0_block0>"), ParentSymbol.class);
        checkSymbol(if0_block0, "<if0_block0>", Kind.BLOCK, DataType.NONE, FLAG_NONE);
        ASTBlock ifBlock = ifStmt.getIfBlock();
        assertSame(if0_block0, ifBlock.getDeclSymbol());

        SymbolTable if0_block0Table = if0_block0.getTable();
        checkSymbolTable(if0_block0Table, SCOPE, 1, List.of("output"));

        Symbol output = if0_block0Table.get("output");
        checkSymbol(output, "output", Kind.LOCAL, expDtString, FLAG_NONE);
        List<ASTBlockStatement> ifBlockStmts = ifBlock.getBlockStmts().getTypedChildren();
        ASTLocalVariableDeclarationStatement outputVarDeclStmt = ensureIsa(ifBlockStmts.get(0),
                ASTLocalVariableDeclarationStatement.class);
        assertSame(output, outputVarDeclStmt.getLocalVarDecl().getVarDeclList().get(0).getDeclSymbol());

        // else if
        ParentSymbol if0_if1 = ensureIsa(if0Table.get("<if0_if1>"), ParentSymbol.class);
        checkSymbol(if0_if1, "<if0_if1>", Kind.IF_STMT, DataType.NONE, FLAG_NONE);
        assertTrue(ifStmt.getElseIf().isPresent());
        ASTIfStatement elseIfStmt = ifStmt.getElseIf().get();
        assertSame(if0_if1, elseIfStmt.getDeclSymbol());

        SymbolTable if0_if1Table = if0_if1.getTable();
        checkSymbolTable(if0_if1Table, SCOPE, 3, List.of("someOtherCondition", "<if0_if1_block0>", "<if0_if1_block1>"));
        assertTrue(elseIfStmt.getInit().isPresent());
        ASTInit elseIfInit = elseIfStmt.getInit().get();
        ASTLocalVariableDeclaration elseIfInitDecl = ensureIsa(elseIfInit, ASTLocalVariableDeclaration.class);
        ASTVariableDeclaratorList elseIfVarDeclList = elseIfInitDecl.getVarDeclList();

        Symbol if0_if1_someOtherCondition = if0_if1Table.get("someOtherCondition");
        checkSymbol(if0_if1_someOtherCondition, "someOtherCondition", Kind.LOCAL,
                expDtInt, FLAG_NONE);
        assertSame(if0_if1_someOtherCondition, elseIfVarDeclList.get(0).getDeclSymbol());

        // else if block
        ParentSymbol if0_if1_block0 = ensureIsa(if0_if1Table.get("<if0_if1_block0>"), ParentSymbol.class);
        checkSymbol(if0_if1_block0, "<if0_if1_block0>", Kind.BLOCK, DataType.NONE, FLAG_NONE);
        assertSame(if0_if1_block0, elseIfStmt.getIfBlock().getDeclSymbol());

        SymbolTable if0_if1_block0Table = if0_if1_block0.getTable();
        checkSymbolTable(if0_if1_block0Table, SCOPE, 1, List.of("otherCondition"));
        List<ASTBlockStatement> elseIfBlockStmts = elseIfStmt.getIfBlock().getBlockStmts().getTypedChildren();
        ASTLocalVariableDeclarationStatement otherCondVarDeclStmt = ensureIsa(elseIfBlockStmts.get(0),
                ASTLocalVariableDeclarationStatement.class);

        Symbol otherCondition = if0_if1_block0Table.get("otherCondition");
        checkSymbol(otherCondition, "otherCondition", Kind.LOCAL, expDtString, FLAG_NONE);
        assertSame(otherCondition, otherCondVarDeclStmt.getLocalVarDecl().getVarDeclList().get(0).getDeclSymbol());


        // else block
        ParentSymbol if0_if1_block1 = ensureIsa(if0_if1Table.get("<if0_if1_block1>"), ParentSymbol.class);
        checkSymbol(if0_if1_block1, "<if0_if1_block1>", Kind.BLOCK, DataType.NONE, FLAG_NONE);
        assertTrue(elseIfStmt.getElseBlock().isPresent());
        assertSame(if0_if1_block1, elseIfStmt.getElseBlock().get().getDeclSymbol());

        SymbolTable if0_if1_block1Table = if0_if1_block1.getTable();
        checkSymbolTable(if0_if1_block1Table, SCOPE, 1, List.of("dummy"));

        Symbol dummy = if0_if1_block1Table.get("dummy");
        checkSymbol(dummy, "dummy", Kind.LOCAL, expDtString, FLAG_NONE);
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
        Pair pair = createMemberSymbolTableNoErrors(code, ClassesParser::parseClassPart);
        SymbolTable table = pair.table();
        ASTMethodDeclaration testingMethod = ensureIsa(pair.member(), ASTMethodDeclaration.class);
        assertTrue(testingMethod.getBody().getBlock().isPresent());
        List<ASTBlockStatement> blockStmts = testingMethod.getBody().getBlock().get()
                .getBlockStmts().getTypedChildren();
        DataType expDtString = new DataType("", "String");
        DataType expDtInt = new DataType("", "Int");

        checkSymbolTable(table, TYPE, 1, List.of("whileStmt()"));
        Symbol symbol = table.get("whileStmt()");

        ParameterizedSymbol paraSymbol = ensureIsa(symbol, ParameterizedSymbol.class);
        checkSymbol(paraSymbol, "whileStmt()", Kind.METHOD, expDtString, FLAG_NONE,
                1, 0);

        SymbolTable paraTable = paraSymbol.getTable();
        checkSymbolTable(paraTable, MEMBER, 1, List.of("<while0>"));

        ParentSymbol whileSymbol = ensureIsa(paraTable.get("<while0>"), ParentSymbol.class);
        checkSymbol(whileSymbol, "<while0>", Kind.WHILE_STMT, DataType.NONE,
                FLAG_NONE, 2);
        ASTWhileStatement whileStmt = ensureIsa(blockStmts.get(0), ASTWhileStatement.class);
        assertSame(whileSymbol, whileStmt.getDeclSymbol());

        SymbolTable whileTable = whileSymbol.getTable();
        checkSymbolTable(whileTable, SCOPE, 2, List.of("line", "lineNbr"));

        Symbol lineSymbol = whileTable.get("line");
        checkSymbol(lineSymbol, "line", Kind.LOCAL, expDtString, FLAG_NONE);
        assertTrue(whileStmt.getInit().isPresent());
        ASTInit init = whileStmt.getInit().get();
        ASTLocalVariableDeclaration initDecl = ensureIsa(init, ASTLocalVariableDeclaration.class);
        ASTVariableDeclaratorList varDeclList = initDecl.getVarDeclList();
        assertSame(lineSymbol, varDeclList.get(0).getDeclSymbol());

        Symbol lineNbrSymbol = whileTable.get("lineNbr");
        checkSymbol(lineNbrSymbol, "lineNbr", Kind.LOCAL, expDtInt, FLAG_NONE);
        List<ASTBlockStatement> whileBlockStmts = whileStmt.getBlock().getBlockStmts().getTypedChildren();
        ASTLocalVariableDeclarationStatement lineNbrDeclStmt = ensureIsa(whileBlockStmts.get(0),
                ASTLocalVariableDeclarationStatement.class);
        assertSame(lineNbrSymbol, lineNbrDeclStmt.getLocalVarDecl().getVarDeclList().get(0).getDeclSymbol());
    }

    /**
     * Helper method to get a <code>StatementsSymbolCreator</code>.
     * @return A <code>StatementsSymbolCreator</code>.
     */
    public static StatementsSymbolCreator getStatementsSymbolCreator() {
        return new SymbolCreator(new BaseMessageProducer(), new TypeLookup()).getStatementsSymbolCreator();
    }

    /**
     * Helper method to get a <code>SymbolTable</code> for a member declaration
     * directly from code.  Ensures no errors.
     * @param code The code for the member declaration.
     * @param memberParser A <code>Function</code> on a <code>ClassesParser</code>
     *                    that parses a particular member production.
     * @return A <code>SymbolTable</code> for the member declaration.
     */
    public static Pair createMemberSymbolTableNoErrors(String code,
                                                              Function<ClassesParser, ? extends ASTMember> memberParser) {
        ClassesSymbolCreator creator = SymbolCreatorClassesTest.getClassesSymbolCreator();
        Pair pair = createMemberSymbolTable(code, creator, memberParser);
        SymbolTable table = pair.table();
        ensureNoErrors(table, creator);
        return pair;
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
        Pair pair = createMemberSymbolTable(code, creator, memberParser);
        SymbolTable table = pair.table();
        expectError(table, creator, expNumErrors);
    }

    private static Pair createMemberSymbolTable(String code, ClassesSymbolCreator creator,
                                                       Function<ClassesParser, ? extends ASTMember> memberParser) {
        ClassesParser classesParser = ParserClassesTest.getClassesParser(code);
        ASTMember member = memberParser.apply(classesParser);
        SymbolTable parent = new SymbolTable(SymbolTable.Scope.TYPE);
        ParentSymbol enclosingType = new ParentSymbol(
                new Location("<dummy>", 0, 0, "unavailable"),
                "TestType", Kind.CLASS, null, new DataType("", "TestType"),
                FLAG_NONE);

        creator.createSymbolsForMember(parent, member);
        return new Pair(member, parent);
    }

    public record Pair(ASTMember member, SymbolTable table) {
    }
}
