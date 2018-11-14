package org.spruce.compiler.test;

import org.spruce.compiler.ast.expressions.*;
import org.spruce.compiler.ast.names.*;
import org.spruce.compiler.ast.statements.*;
import org.spruce.compiler.ast.types.*;
import org.spruce.compiler.parser.Parser;
import org.spruce.compiler.parser.StatementsParser;
import org.spruce.compiler.scanner.Scanner;
import static org.spruce.compiler.scanner.TokenType.*;
import static org.spruce.compiler.test.ParserTestUtility.*;

import org.junit.jupiter.api.Test;

/**
 * All tests for the parser related to statements.
 */
public class ParserStatementsTest
{
    /**
     * Tests block of empty braces.
     */
    @Test
    public void testBlockOfNothing()
    {
        StatementsParser parser = new Parser(new Scanner("{}")).getStatementsParser();
        ASTBlock node = parser.parseBlock();
        checkEmpty(node, OPEN_BRACE);
    }

    /**
     * Tests block of block statements.
     */
    @Test
    public void testBlockOfBlockStatements()
    {
        StatementsParser parser = new Parser(new Scanner("{Integer a := 1;\nInteger b := 2;\nreturn a + b;}")).getStatementsParser();
        ASTBlock node = parser.parseBlock();
        checkSimple(node, ASTBlockStatements.class, OPEN_BRACE);
    }

    /**
     * Test block statements of block statement instances.
     */
    @Test
    public void testBlockStatements()
    {
        StatementsParser parser = new Parser(new Scanner("final String stmt := \"Statement one!\";\nconst Integer stmt2Nbr := 2;\ni++;}")).getStatementsParser();
        ASTBlockStatements node = parser.parseBlockStatements();
        checkList(node, null, ASTBlockStatement.class, 3);
    }

    /**
     * Tests block statement of modifier and local variable declaration.
     */
    @Test
    public void testBlockStatementOfModifierDeclaration()
    {
        StatementsParser parser = new Parser(new Scanner("final Integer i := 1;")).getStatementsParser();
        ASTBlockStatement node = parser.parseBlockStatement();
        checkSimple(node, ASTLocalVariableDeclarationStatement.class);
    }

    /**
     * Tests block statement of local variable declaration.
     */
    @Test
    public void testBlockStatementOfDeclaration()
    {
        StatementsParser parser = new Parser(new Scanner("Integer i := 1;")).getStatementsParser();
        ASTBlockStatement node = parser.parseBlockStatement();
        checkSimple(node, ASTLocalVariableDeclarationStatement.class);
    }

    /**
     * Tests block statement of assignment.
     */
    @Test
    public void testBlockStatementOfAssignment()
    {
        StatementsParser parser = new Parser(new Scanner("i := 1;")).getStatementsParser();
        ASTBlockStatement node = parser.parseBlockStatement();
        checkSimple(node, ASTStatement.class);
    }

    /**
     * Tests block statement of method invocation.
     */
    @Test
    public void testBlockStatementOfMethodInvocation()
    {
        StatementsParser parser = new Parser(new Scanner("i(j);")).getStatementsParser();
        ASTBlockStatement node = parser.parseBlockStatement();
        checkSimple(node, ASTStatement.class);
    }

    /**
     * Tests block statement of qualified class instance creation expression.
     */
    @Test
    public void testBlockStatementOfCICE()
    {
        StatementsParser parser = new Parser(new Scanner("i.new J();")).getStatementsParser();
        ASTBlockStatement node = parser.parseBlockStatement();
        checkSimple(node, ASTStatement.class);
    }

    /**
     * Tests block statement of return statement.
     */
    @Test
    public void testBlockStatementOfReturn()
    {
        StatementsParser parser = new Parser(new Scanner("return true;")).getStatementsParser();
        ASTBlockStatement node = parser.parseBlockStatement();
        checkSimple(node, ASTStatement.class);
    }

    /**
     * Tests local variable declaration statement.
     */
    @Test
    public void testLocalVariableDeclarationStatement()
    {
        StatementsParser parser = new Parser(new Scanner("Integer[] values := {1, 2, 3};")).getStatementsParser();
        ASTLocalVariableDeclarationStatement node = parser.parseLocalVariableDeclarationStatement();
        checkSimple(node, ASTLocalVariableDeclaration.class, SEMICOLON);
    }

    /**
     * Tests local variable declaration without modifiers.
     */
    @Test
    public void testLocalVariableDeclaration()
    {
        StatementsParser parser = new Parser(new Scanner("Boolean result := true, done := false")).getStatementsParser();
        ASTLocalVariableDeclaration node = parser.parseLocalVariableDeclaration();
        checkBinary(node, ASTLocalVariableType.class, ASTVariableDeclaratorList.class);
    }

    /**
     * Tests local variable declaration with modifiers.
     */
    @Test
    public void testLocalVariableDeclarationOfModifiers()
    {
        StatementsParser parser = new Parser(new Scanner("final const Boolean result := true, done := false")).getStatementsParser();
        ASTLocalVariableDeclaration node = parser.parseLocalVariableDeclaration();
        checkTrinary(node, null, ASTVariableModifierList.class, ASTLocalVariableType.class, ASTVariableDeclaratorList.class);
    }

    /**
     * Tests variable modifier list of variable modifier.
     */
    @Test
    public void testVariableModifierListOfVariableModifier()
    {
        StatementsParser parser = new Parser(new Scanner("const")).getStatementsParser();
        ASTVariableModifierList node = parser.parseVariableModifierList();
        checkSimple(node, ASTVariableModifier.class);
    }
    /**
     * Tests variable modifier list of variable modifiers.
     */
    @Test
    public void testVariableModifierListOfVariableModifiers()
    {
        StatementsParser parser = new Parser(new Scanner("final const")).getStatementsParser();
        ASTVariableModifierList node = parser.parseVariableModifierList();
        checkList(node, null, ASTVariableModifier.class, 2);
    }

    /**
     * Tests variable modifier of "const".
     */
    @Test
    public void testVariableModifierOfConst()
    {
        StatementsParser parser = new Parser(new Scanner("const")).getStatementsParser();
        ASTVariableModifier node = parser.parseVariableModifier();
        checkEmpty(node, CONST);
    }

    /**
     * Tests variable modifier of "final".
     */
    @Test
    public void testVariableModifierOfFinal()
    {
        StatementsParser parser = new Parser(new Scanner("final")).getStatementsParser();
        ASTVariableModifier node = parser.parseVariableModifier();
        checkEmpty(node, FINAL);
    }

    /**
     * Tests variable modifier of "constant".
     */
    @Test
    public void testVariableModifierOfConstant()
    {
        StatementsParser parser = new Parser(new Scanner("constant")).getStatementsParser();
        ASTVariableModifier node = parser.parseVariableModifier();
        checkEmpty(node, CONSTANT);
    }

    /**
     * Tests variable declarator list of variable declarator.
     */
    @Test
    public void testVariableDeclaratorListOfVariableDeclarator()
    {
        StatementsParser parser = new Parser(new Scanner("a := b")).getStatementsParser();
        ASTVariableDeclaratorList node = parser.parseVariableDeclaratorList();
        checkSimple(node, ASTVariableDeclarator.class, COMMA);
    }

    /**
     * Tests variable declarator list.
     */
    @Test
    public void testVariableDeclaratorList()
    {
        StatementsParser parser = new Parser(new Scanner("x := 1, y := x")).getStatementsParser();
        ASTVariableDeclaratorList node = parser.parseVariableDeclaratorList();
        checkList(node, COMMA, ASTVariableDeclarator.class, 2);
    }

    /**
     * Tests nested variable declarator lists.
     */
    @Test
    public void testVariableDeclaratorListNested()
    {
        StatementsParser parser = new Parser(new Scanner("a := 1, b := a + 1, c := 2 * b")).getStatementsParser();
        ASTVariableDeclaratorList node = parser.parseVariableDeclaratorList();
        checkList(node, COMMA, ASTVariableDeclarator.class, 3);
    }

    /**
     * Tests variable declarator of identifier.
     */
    @Test
    public void testVariableDeclaratorOfIdentifier()
    {
        StatementsParser parser = new Parser(new Scanner("varName")).getStatementsParser();
        ASTVariableDeclarator node = parser.parseVariableDeclarator();
        checkSimple(node, ASTIdentifier.class);
    }

    /**
     * Tests variable declarator of identifier and variable initializer.
     */
    @Test
    public void testVariableDeclaratorOfIdentifierVariableInitializer()
    {
        StatementsParser parser = new Parser(new Scanner("count := 2")).getStatementsParser();
        ASTVariableDeclarator node = parser.parseVariableDeclarator();
        checkBinary(node, ASSIGNMENT, ASTIdentifier.class, ASTVariableInitializer.class);
    }

    /**
     * Tests local variable type of data type.
     */
    @Test
    public void testLocalVariableTypeOfDataType()
    {
        StatementsParser parser = new Parser(new Scanner("spruce.lang.String[][])")).getStatementsParser();
        ASTLocalVariableType node = parser.parseLocalVariableType();
        checkSimple(node, ASTDataType.class);
    }

    /**
     * Tests local variable type of "auto".
     */
    @Test
    public void testLocalVariableTypeOfAuto()
    {
        StatementsParser parser = new Parser(new Scanner("auto")).getStatementsParser();
        ASTLocalVariableType node = parser.parseLocalVariableType();
        checkEmpty(node, AUTO);
    }

    /**
     * Tests statement of block.
     */
    @Test
    public void testStatementOfBlock()
    {
        StatementsParser parser = new Parser(new Scanner("{x := x + 1;}")).getStatementsParser();
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTBlock.class);
    }

    /**
     * Tests statement of expression statement.
     */
    @Test
    public void testStatementOfExpressionStatement()
    {
        StatementsParser parser = new Parser(new Scanner("x := x + 1;")).getStatementsParser();
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTExpressionStatement.class);
    }

    /**
     * Tests statement of return statement.
     */
    @Test
    public void testStatementOfReturnStatement()
    {
        StatementsParser parser = new Parser(new Scanner("return true;")).getStatementsParser();
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTReturnStatement.class);
    }

    /**
     * Tests statement of throw statement.
     */
    @Test
    public void testStatementOfThrowStatement()
    {
        StatementsParser parser = new Parser(new Scanner("throw new CompileException(\"Error message\");")).getStatementsParser();
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTThrowStatement.class);
    }

    /**
     * Tests statement of break statement.
     */
    @Test
    public void testStatementOfBreakStatement()
    {
        StatementsParser parser = new Parser(new Scanner("break;")).getStatementsParser();
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTBreakStatement.class);
    }

    /**
     * Tests statement of break statement.
     */
    @Test
    public void testStatementOfContinueStatement()
    {
        StatementsParser parser = new Parser(new Scanner("continue;")).getStatementsParser();
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTContinueStatement.class);
    }

    /**
     * Tests statement of break statement.
     */
    @Test
    public void testStatementOfFallthroughStatement()
    {
        StatementsParser parser = new Parser(new Scanner("fallthrough;")).getStatementsParser();
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTFallthroughStatement.class);
    }

    /**
     * Tests statement of assert statement.
     */
    @Test
    public void testStatementOfAssertStatement()
    {
        StatementsParser parser = new Parser(new Scanner("assert status = true;")).getStatementsParser();
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTAssertStatement.class);
    }

    /**
     * Tests statement of if statement.
     */
    @Test
    public void testStatementOfIfStatement()
    {
        StatementsParser parser = new Parser(new Scanner("if (success) { return true; }")).getStatementsParser();
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTIfStatement.class);
    }

    /**
     * Tests statement of while statement.
     */
    @Test
    public void testStatementOfWhileStatement()
    {
        StatementsParser parser = new Parser(new Scanner("while (shouldContinue) doWork();")).getStatementsParser();
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTWhileStatement.class);
    }

    /**
     * Tests statement of do statement.
     */
    @Test
    public void testStatementOfDoStatement()
    {
        StatementsParser parser = new Parser(new Scanner("do { work(); } while (shouldContinue);")).getStatementsParser();
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTDoStatement.class);
    }

    /**
     * Tests statement of synchronized statement.
     */
    @Test
    public void testStatementOfSynchronizedStatement()
    {
        StatementsParser parser = new Parser(new Scanner("synchronized (myLock) {\n    myLock.wait();\n}")).getStatementsParser();
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTSynchronizedStatement.class);
    }

    /**
     * Tests statement of for statement.
     */
    @Test
    public void testStatementOfForStatement()
    {
        StatementsParser parser = new Parser(new Scanner("for (;;) doWork();")).getStatementsParser();
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTForStatement.class);
    }

    /**
     * Tests statement of try statement.
     */
    @Test
    public void testStatementOfTryStatement()
    {
        StatementsParser parser = new Parser(new Scanner("try {\n    br.readLine();\n} catch (IOException e) {\n    out.println(e.getMessage());\n}")).getStatementsParser();
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTTryStatement.class);
    }

    /**
     * Tests statement of switch statement.
     */
    @Test
    public void testStatementOfSwitchStatement()
    {
        StatementsParser parser = new Parser(new Scanner("switch (code) {\ncase 1: out.println(\"One\");\ncase 2: out.println(\"Two\");\ndefault: out.println(\"Unexpected\");\n}")).getStatementsParser();
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTSwitchStatement.class);
    }

    /**
     * Tests switch statement.
     */
    @Test
    public void testSwitchStatement()
    {
        StatementsParser parser = new Parser(new Scanner("switch (code) {\ncase 1: out.println(\"One\");\ncase 2: out.println(\"Two\");\ndefault: out.println(\"Unexpected\");\n}")).getStatementsParser();
        ASTSwitchStatement node = parser.parseSwitchStatement();
        checkBinary(node, SWITCH, ASTExpression.class, ASTSwitchBlock.class);
    }

    /**
     * Tests empty switch block.
     */
    @Test
    public void testSwitchBlockEmpty()
    {
        StatementsParser parser = new Parser(new Scanner("{}")).getStatementsParser();
        ASTSwitchBlock node = parser.parseSwitchBlock();
        checkEmpty(node, OPEN_BRACE);
    }

    /**
     * Tests switch block of switch cases.
     */
    @Test
    public void testSwitchBlockOfSwitchCases()
    {
        StatementsParser parser = new Parser(new Scanner("{case 1: out.println(\"Success\");}")).getStatementsParser();
        ASTSwitchBlock node = parser.parseSwitchBlock();
        checkSimple(node, ASTSwitchCases.class, OPEN_BRACE);
    }

    /**
     * Tests switch cases of switch case.
     */
    @Test
    public void testSwitchCasesOfSwitchCase()
    {
        StatementsParser parser = new Parser(new Scanner("case 1: out.println(\"Success\");}")).getStatementsParser();
        ASTSwitchCases node = parser.parseSwitchCases();
        checkSimple(node, ASTSwitchCase.class);
    }

    /**
     * Tests switch cases of switch case instances (here, just multiple switch cases).
     */
    @Test
    public void testSwitchCasesNested()
    {
        StatementsParser parser = new Parser(new Scanner("case 1: out.println(\"Success\");\ncase 2: out.println(\"Pass\");}")).getStatementsParser();
        ASTSwitchCases node = parser.parseSwitchCases();
        checkList(node, null, ASTSwitchCase.class, 2);
    }

    /**
     * Tests switch case of block statements.
     */
    @Test
    public void testSwitchCaseOfBlockStatements()
    {
        StatementsParser parser = new Parser(new Scanner("case 1, 2, 3:\n    out.println(\"Case found!\");\n    fallthrough;\n}")).getStatementsParser();
        ASTSwitchCase node = parser.parseSwitchCase();
        checkBinary(node, ASTSwitchLabel.class, ASTBlockStatements.class);
    }

    /**
     * Tests switch case of no statements.
     */
    @Test
    public void testSwitchCaseOfEmpty()
    {
        StatementsParser parser = new Parser(new Scanner("case 1, 2, 3:}")).getStatementsParser();
        ASTSwitchCase node = parser.parseSwitchCase();
        checkSimple(node, ASTSwitchLabel.class);
    }

    /**
     * Tests switch label of case and switch values.
     */
    @Test
    public void testSwitchLabelOfCase()
    {
        StatementsParser parser = new Parser(new Scanner("case 1, 2, 3:")).getStatementsParser();
        ASTSwitchLabel node = parser.parseSwitchLabel();
        checkSimple(node, ASTSwitchValues.class, CASE);
    }

    /**
     * Tests switch label of default.
     */
    @Test
    public void testSwitchLabelOfDefault()
    {
        StatementsParser parser = new Parser(new Scanner("default:")).getStatementsParser();
        ASTSwitchLabel node = parser.parseSwitchLabel();
        checkEmpty(node, DEFAULT);
    }

    /**
     * Tests switch values of expression (no incr/decr).
     */
    @Test
    public void testSwitchValuesOfExpressionNoIncrDecr()
    {
        StatementsParser parser = new Parser(new Scanner("1")).getStatementsParser();
        ASTSwitchValues node = parser.parseSwitchValues();
        checkSimple(node, ASTSwitchValue.class, COMMA);
    }

    /**
     * Tests nested switch values (here, just multiple instances of switch value).
     */
    @Test
    public void testSwitchValuesNested()
    {
        StatementsParser parser = new Parser(new Scanner("1, 2, 3")).getStatementsParser();
        ASTSwitchValues node = parser.parseSwitchValues();
        checkList(node, COMMA, ASTSwitchValue.class, 3);
    }

    /**
     * Tests switch value of identifier.
     */
    @Test
    public void testSwitchValueOfIdentifier()
    {
        StatementsParser parser = new Parser(new Scanner("BLUE:")).getStatementsParser();
        ASTSwitchValue node = parser.parseSwitchValue();
        checkSimple(node, ASTIdentifier.class);
    }

    /**
     * Tests switch value of expression (no incr/decr).
     */
    @Test
    public void testSwitchValueOfExpressionNoIncrDecr()
    {
        StatementsParser parser = new Parser(new Scanner("1 + 2")).getStatementsParser();
        ASTSwitchValue node = parser.parseSwitchValue();
        checkSimple(node, ASTExpressionNoIncrDecr.class);
    }

    /**
     * Tests try statement of catch.
     */
    @Test
    public void testTryStatementOfCatch()
    {
        StatementsParser parser = new Parser(new Scanner("try {\n    br.readLine();\n} catch (IOException e) {\n    out.println(e.getMessage());\n}")).getStatementsParser();
        ASTTryStatement node = parser.parseTryStatement();
        checkBinary(node, TRY, ASTBlock.class, ASTCatches.class);
    }

    /**
     * Tests try statement of finally.
     */
    @Test
    public void testTryStatementOfFinally()
    {
        StatementsParser parser = new Parser(new Scanner("try {\n    br.readLine();\n} finally {\n    br.close();\n}")).getStatementsParser();
        ASTTryStatement node = parser.parseTryStatement();
        checkBinary(node, TRY, ASTBlock.class, ASTFinally.class);
    }

    /**
     * Tests try statement of resource specification.
     */
    @Test
    public void testTryStatementOfResourceSpecification()
    {
        StatementsParser parser = new Parser(new Scanner("try (BufferedReader br := new BufferedReader()){\n    br.readLine();\n}")).getStatementsParser();
        ASTTryStatement node = parser.parseTryStatement();
        checkBinary(node, TRY, ASTResourceSpecification.class, ASTBlock.class);
    }

    /**
     * Tests try statement of all optionals.
     */
    @Test
    public void testTryStatementOfAll()
    {
        StatementsParser parser = new Parser(new Scanner("try (BufferedReader br := new BufferedReader()){\n    br.readLine();\n} catch (IOException e) {\n    out.println(e.getMessage());\n} finally {\n    br.close();\n}")).getStatementsParser();
        ASTTryStatement node = parser.parseTryStatement();
        checkNary(node, TRY, ASTResourceSpecification.class, ASTBlock.class, ASTCatches.class, ASTFinally.class);
        node.collapseThenPrint();
    }

    /**
     * Tests resource specification of resource list.
     */
    @Test
    public void testResourceSpecification()
    {
        StatementsParser parser = new Parser(new Scanner("(fr; BufferedReader br := new BufferedReader(fr))")).getStatementsParser();
        ASTResourceSpecification node = parser.parseResourceSpecification();
        checkSimple(node, ASTResourceList.class);
    }

    /**
     * Tests resource specification of resource list and semicolon.
     */
    @Test
    public void testResourceSpecificationSemicolon()
    {
        StatementsParser parser = new Parser(new Scanner("(fr; BufferedReader br := new BufferedReader(fr);)")).getStatementsParser();
        ASTResourceSpecification node = parser.parseResourceSpecification();
        checkSimple(node, ASTResourceList.class);
    }

    /**
     * Tests resource list of resource.
     */
    @Test
    public void testResourceListOfResource()
    {
        StatementsParser parser = new Parser(new Scanner("BufferedReader br := new BufferedReader()")).getStatementsParser();
        ASTResourceList node = parser.parseResourceList();
        checkSimple(node, ASTResource.class, SEMICOLON);
    }

    /**
     * Tests resource list of nested resource lists (here, just multiple resources).
     */
    @Test
    public void testResourceListNested()
    {
        StatementsParser parser = new Parser(new Scanner("fr; BufferedReader br := new BufferedReader(fr)")).getStatementsParser();
        ASTResourceList node = parser.parseResourceList();
        checkList(node, SEMICOLON, ASTResource.class, 2);
    }

    /**
     * Test resource of resource declaration.
     */
    @Test
    public void testResourceOfResourceDeclaration()
    {
        StatementsParser parser = new Parser(new Scanner("BufferedReader br := new BufferedReader()")).getStatementsParser();
        ASTResource node = parser.parseResource();
        checkSimple(node, ASTResourceDeclaration.class);
    }

    /**
     * Test resource of expression name.
     */
    @Test
    public void testResourceOfExpressionName()
    {
        StatementsParser parser = new Parser(new Scanner("br")).getStatementsParser();
        ASTResource node = parser.parseResource();
        checkSimple(node, ASTExpressionName.class);
    }

    /**
     * Test resource of field access.
     */
    @Test
    public void testResourceOfFieldAccess()
    {
        StatementsParser parser = new Parser(new Scanner("super.br")).getStatementsParser();
        ASTResource node = parser.parseResource();
        checkSimple(node, ASTFieldAccess.class);
    }

    /**
     * Test resource declaration, no variable modifiers.
     */
    @Test
    public void testResourceDeclaration()
    {
        StatementsParser parser = new Parser(new Scanner("BufferedReader br := new BufferedReader()")).getStatementsParser();
        ASTResourceDeclaration node = parser.parseResourceDeclaration();
        checkTrinary(node, ASSIGNMENT, ASTLocalVariableType.class, ASTIdentifier.class, ASTExpressionNoIncrDecr.class);
    }

    /**
     * Test resource declaration, with variable modifiers.
     */
    @Test
    public void testResourceDeclarationOfVariableModifier()
    {
        StatementsParser parser = new Parser(new Scanner("final BufferedReader br := new BufferedReader()")).getStatementsParser();
        ASTResourceDeclaration node = parser.parseResourceDeclaration();
        checkNary(node, ASSIGNMENT, ASTVariableModifierList.class, ASTLocalVariableType.class, ASTIdentifier.class, ASTExpressionNoIncrDecr.class);
        node.collapseThenPrint();
    }

    /**
     * Test catches of catch clauses.
     */
    @Test
    public void testCatches()
    {
        StatementsParser parser = new Parser(new Scanner("catch (FileNotFoundException e) { err.println(e.getMessage()); }\ncatch (IOException e) {out.println(e.getMessage()); }")).getStatementsParser();
        ASTCatches node = parser.parseCatches();
        checkList(node, null, ASTCatchClause.class, 2);
    }

    /**
     * Tests catch clause.
     */
    @Test
    public void testCatchClause()
    {
        StatementsParser parser = new Parser(new Scanner("catch (CompileException ce) { out.println(ce.getMessage()); }")).getStatementsParser();
        ASTCatchClause node = parser.parseCatchClause();
        checkBinary(node, CATCH, ASTCatchFormalParameter.class, ASTBlock.class);
    }

    /**
     * Tests catch type of data type.
     */
    @Test
    public void testCatchTypeOfDataType()
    {
        StatementsParser parser = new Parser(new Scanner("Exception")).getStatementsParser();
        ASTCatchType node = parser.parseCatchType();
        checkSimple(node, ASTDataType.class, BITWISE_OR);
    }

    /**
     * Tests catch formal parameter without modifiers.
     */
    @Test
    public void testCatchFormalParameter()
    {
        StatementsParser parser = new Parser(new Scanner("Exception e")).getStatementsParser();
        ASTCatchFormalParameter node = parser.parseCatchFormalParameter();
        checkBinary(node, ASTCatchType.class, ASTIdentifier.class);
    }

    /**
     * Tests catch formal parameter with modifiers.
     */
    @Test
    public void testCatchFormalParameterOfModifiers()
    {
        StatementsParser parser = new Parser(new Scanner("final CustomException ce")).getStatementsParser();
        ASTCatchFormalParameter node = parser.parseCatchFormalParameter();
        checkTrinary(node, null, ASTVariableModifierList.class, ASTCatchType.class, ASTIdentifier.class);
    }

    /**
     * Tests catch type.
     */
    @Test
    public void testCatchType()
    {
        StatementsParser parser = new Parser(new Scanner("IOException | SQLException")).getStatementsParser();
        ASTCatchType node = parser.parseCatchType();
        checkList(node, BITWISE_OR, ASTDataType.class, 2);
    }

    /**
     * Tests nested catch types, here, just a list of data types.
     */
    @Test
    public void testCatchTypeNested()
    {
        StatementsParser parser = new Parser(new Scanner("ArrayIndexOutOfBoundsException | NullPointerException | IllegalArgumentException")).getStatementsParser();
        ASTCatchType node = parser.parseCatchType();
        checkList(node, BITWISE_OR, ASTDataType.class, 3);
    }

    /**
     * Tests finally block.
     */
    @Test
    public void testFinally()
    {
        StatementsParser parser = new Parser(new Scanner("finally { out.println(\"Always executed!\"); }")).getStatementsParser();
        ASTFinally node = parser.parseFinally();
        checkSimple(node, ASTBlock.class, FINALLY);
    }

    /**
     * Tests simple if statement.
     */
    @Test
    public void testIfStatementOfSimple()
    {
        StatementsParser parser = new Parser(new Scanner("if (success) { return true; }")).getStatementsParser();
        ASTIfStatement node = parser.parseIfStatement();
        checkBinary(node, IF, ASTExpressionNoIncrDecr.class, ASTStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests if statement with init.
     */
    @Test
    public void testIfStatementOfInit()
    {
        StatementsParser parser = new Parser(new Scanner("if {String line := br.readLine()} (line != null) out.println(line);")).getStatementsParser();
        ASTIfStatement node = parser.parseIfStatement();
        checkTrinary(node, IF, ASTInit.class, ASTExpressionNoIncrDecr.class, ASTStatement.class);
    }

    /**
     * Tests if statement with else.
     */
    @Test
    public void testIfStatementOfElse()
    {
        StatementsParser parser = new Parser(new Scanner("if (result) {\n    out.println(\"Test passed.\");\n} else {\n    out.println(\"Test FAILED!\");\n}")).getStatementsParser();
        ASTIfStatement node = parser.parseIfStatement();
        checkTrinary(node, IF, ASTExpressionNoIncrDecr.class, ASTStatement.class, ASTStatement.class);
    }

    /**
     * Tests nested if statements (if/else if/else).
     */
    @Test
    public void testIfStatementNested()
    {
        StatementsParser parser = new Parser(new Scanner("if (result) {\n    out.println(\"Test passed.\");\n} else if (DEBUG) {\n    out.println(\"Test failed in debug mode!\");\n} else {\n    out.println(\"Test FAILED!\");\n}")).getStatementsParser();
        ASTIfStatement node = parser.parseIfStatement();
        checkTrinary(node, IF, ASTExpressionNoIncrDecr.class, ASTStatement.class, ASTStatement.class);

        ASTStatement nested = (ASTStatement) node.getChildren().get(2);
        checkSimple(nested, ASTIfStatement.class);
        ASTIfStatement nestedIf = (ASTIfStatement) nested.getChildren().get(0);
        checkTrinary(nestedIf, IF, ASTExpressionNoIncrDecr.class, ASTStatement.class, ASTStatement.class);

        node.collapseThenPrint();
    }

    /**
     * Tests simple while statement.
     */
    @Test
    public void testWhileStatementOfSimple()
    {
        StatementsParser parser = new Parser(new Scanner("while (shouldContinue) { doWork(); }")).getStatementsParser();
        ASTWhileStatement node = parser.parseWhileStatement();
        checkBinary(node, WHILE, ASTExpressionNoIncrDecr.class, ASTStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests while statement with init.
     */
    @Test
    public void testWhileStatementOfInit()
    {
        StatementsParser parser = new Parser(new Scanner("while {String line := br.readLine()} (line != null) out.println(line);")).getStatementsParser();
        ASTWhileStatement node = parser.parseWhileStatement();
        checkTrinary(node, WHILE, ASTInit.class, ASTExpressionNoIncrDecr.class, ASTStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests do statement.
     */
    @Test
    public void testDoStatement()
    {
        StatementsParser parser = new Parser(new Scanner("do { work(); } while (shouldContinue);")).getStatementsParser();
        ASTDoStatement node = parser.parseDoStatement();
        checkBinary(node, DO, ASTStatement.class, ASTExpressionNoIncrDecr.class);
        node.collapseThenPrint();
    }

    /**
     * Tests synchronized statement.
     */
    @Test
    public void testSynchronizedStatement()
    {
        StatementsParser parser = new Parser(new Scanner("synchronized (lock) { lock.notifyAll(); }")).getStatementsParser();
        ASTSynchronizedStatement node = parser.parseSynchronizedStatement();
        checkBinary(node, SYNCHRONIZED, ASTExpressionNoIncrDecr.class, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests for statement of basic for statement of all 3 parts.
     */
    @Test
    public void testForStatementOfBasicForStatementAll3()
    {
        StatementsParser parser = new Parser(new Scanner("for (Int i := 0; i < 10; i++) {\n    out.println(i);\n}")).getStatementsParser();
        ASTForStatement node = parser.parseForStatement();
        checkSimple(node, ASTBasicForStatement.class, FOR);
        ASTBasicForStatement basicForStmt = (ASTBasicForStatement) node.getChildren().get(0);
        checkNary(basicForStmt, SEMICOLON, ASTInit.class, ASTExpressionNoIncrDecr.class, ASTStatementExpressionList.class, ASTStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests for statement of basic for statement of infinite loop.
     */
    @Test
    public void testForStatementOfBasicForStatementInfiniteLoop()
    {
        StatementsParser parser = new Parser(new Scanner("for (;;) {\n    out.println(\"Hello world!\");\n}")).getStatementsParser();
        ASTForStatement node = parser.parseForStatement();
        checkSimple(node, ASTBasicForStatement.class, FOR);
        ASTBasicForStatement basicForStmt = (ASTBasicForStatement) node.getChildren().get(0);
        checkSimple(basicForStmt, ASTStatement.class, SEMICOLON);
        node.collapseThenPrint();
    }

    /**
     * Tests for statement of enhanced for statement.
     */
    @Test
    public void testForStatementOfEnhancedForStatement()
    {
        StatementsParser parser = new Parser(new Scanner("for (Int i : array) {\n    sum += i;\n}")).getStatementsParser();
        ASTForStatement node = parser.parseForStatement();
        checkSimple(node, ASTEnhancedForStatement.class, FOR);
        ASTEnhancedForStatement enhForStmt = (ASTEnhancedForStatement) node.getChildren().get(0);
        checkTrinary(enhForStmt, COLON, ASTLocalVariableDeclaration.class, ASTExpressionNoIncrDecr.class, ASTStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests return statement.
     */
    @Test
    public void testReturnStatement()
    {
        StatementsParser parser = new Parser(new Scanner("return;")).getStatementsParser();
        ASTReturnStatement node = parser.parseReturnStatement();
        checkEmpty(node, RETURN);
        node.collapseThenPrint();
    }

    /**
     * Tests return statement with expression.
     */
    @Test
    public void testReturnStatementOfExpression()
    {
        StatementsParser parser = new Parser(new Scanner("return x.y + 2;")).getStatementsParser();
        ASTReturnStatement node = parser.parseReturnStatement();
        checkSimple(node, ASTExpression.class, RETURN);
        node.collapseThenPrint();
    }

    /**
     * Tests throw statement with expression.
     */
    @Test
    public void testThrowStatementOfExpression()
    {
        StatementsParser parser = new Parser(new Scanner("throw new Exception();")).getStatementsParser();
        ASTThrowStatement node = parser.parseThrowStatement();
        checkSimple(node, ASTExpression.class, THROW);
        node.collapseThenPrint();
    }

    /**
     * Tests break statement.
     */
    @Test
    public void testBreakStatement()
    {
        StatementsParser parser = new Parser(new Scanner("break;")).getStatementsParser();
        ASTBreakStatement node = parser.parseBreakStatement();
        checkEmpty(node, BREAK);
        node.collapseThenPrint();
    }

    /**
     * Tests continue statement.
     */
    @Test
    public void testContinueStatement()
    {
        StatementsParser parser = new Parser(new Scanner("continue;")).getStatementsParser();
        ASTContinueStatement node = parser.parseContinueStatement();
        checkEmpty(node, CONTINUE);
        node.collapseThenPrint();
    }

    /**
     * Tests fallthrough statement.
     */
    @Test
    public void testFallthroughStatement()
    {
        StatementsParser parser = new Parser(new Scanner("fallthrough;")).getStatementsParser();
        ASTFallthroughStatement node = parser.parseFallthroughStatement();
        checkEmpty(node, FALLTHROUGH);
        node.collapseThenPrint();
    }

    /**
     * Tests assert statement of expression.
     */
    @Test
    public void testAssertStatementOfExpression()
    {
        StatementsParser parser = new Parser(new Scanner("assert result = true;")).getStatementsParser();
        ASTAssertStatement node = parser.parseAssertStatement();
        checkSimple(node, ASTExpression.class, ASSERT);
        node.collapseThenPrint();
    }

    /**
     * Tests assert statement of 2 expressions.
     */
    @Test
    public void testAssertStatementOfTwoExpressions()
    {
        StatementsParser parser = new Parser(new Scanner("assert result = true : \"Assertion failed!\";")).getStatementsParser();
        ASTAssertStatement node = parser.parseAssertStatement();
        checkBinary(node, ASSERT, ASTExpression.class, ASTExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests expression statement of statement expression.
     */
    @Test
    public void testExpressionStatementOfStatementExpression()
    {
        StatementsParser parser = new Parser(new Scanner("x++;")).getStatementsParser();
        ASTExpressionStatement node = parser.parseExpressionStatement();
        checkSimple(node, ASTStatementExpression.class, SEMICOLON);
        node.collapseThenPrint();
    }

    /**
     * Tests init of local variable declaration.
     */
    @Test
    public void testInitOfLocalVariableDeclaration()
    {
        StatementsParser parser = new Parser(new Scanner("Int i := 0, j := 0")).getStatementsParser();
        ASTInit node = parser.parseInit();
        checkSimple(node, ASTLocalVariableDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests init of statement expression.
     */
    @Test
    public void testInitOfStatementExpression()
    {
        StatementsParser parser = new Parser(new Scanner("i := 0")).getStatementsParser();
        ASTInit node = parser.parseInit();
        checkSimple(node, ASTStatementExpressionList.class);
        ASTStatementExpressionList list = (ASTStatementExpressionList) node.getChildren().get(0);
        checkSimple(list, ASTStatementExpression.class, COMMA);
        node.collapseThenPrint();
    }

    /**
     * Tests init of statement expression list.
     */
    @Test
    public void testInitOfStatementExpressionList()
    {
        StatementsParser parser = new Parser(new Scanner("i := 0, j := 0, k := 1")).getStatementsParser();
        ASTInit node = parser.parseInit();
        checkSimple(node, ASTStatementExpressionList.class);
        ASTStatementExpressionList list = (ASTStatementExpressionList) node.getChildren().get(0);
        checkList(list, COMMA, ASTStatementExpression.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests statement expression list of statement expression.
     */
    @Test
    public void testStatementExpressionListOfStatementExpression()
    {
        StatementsParser parser = new Parser(new Scanner("i := 0")).getStatementsParser();
        ASTStatementExpressionList node = parser.parseStatementExpressionList();
        checkSimple(node, ASTStatementExpression.class, COMMA);
        node.collapseThenPrint();
    }

    /**
     * Tests statement expression lists of nested statement expression lists
     * (here, just multiple statement expressions).
     */
    @Test
    public void testStatementExpressionListNested()
    {
        StatementsParser parser = new Parser(new Scanner("i := 0, j := 0, k := 1")).getStatementsParser();
        ASTStatementExpressionList node = parser.parseStatementExpressionList();
        checkList(node, COMMA, ASTStatementExpression.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests statement expression of assignment.
     */
    @Test
    public void testStatementExpressionOfAssignment()
    {
        StatementsParser parser = new Parser(new Scanner("x := 0")).getStatementsParser();
        ASTStatementExpression node = parser.parseStatementExpression();
        checkSimple(node, ASTAssignment.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement expression of postfix expression.
     */
    @Test
    public void testStatementExpressionOfPostfixExpression()
    {
        StatementsParser parser = new Parser(new Scanner("x.y++")).getStatementsParser();
        ASTStatementExpression node = parser.parseStatementExpression();
        checkSimple(node, ASTPostfixExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement expression of prefix expression.
     */
    @Test
    public void testStatementExpressionOfPrefixExpression()
    {
        StatementsParser parser = new Parser(new Scanner("--x.y")).getStatementsParser();
        ASTStatementExpression node = parser.parseStatementExpression();
        checkSimple(node, ASTPrefixExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement expression of method invocation.
     */
    @Test
    public void testStatementExpressionOfMethodInvocation()
    {
        StatementsParser parser = new Parser(new Scanner("x.y(2)")).getStatementsParser();
        ASTStatementExpression node = parser.parseStatementExpression();
        checkSimple(node, ASTMethodInvocation.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement expression of class instance creation expression.
     */
    @Test
    public void testStatementExpressionOfClassInstanceCreationExpression()
    {
        StatementsParser parser = new Parser(new Scanner("new SideEffect()")).getStatementsParser();
        ASTStatementExpression node = parser.parseStatementExpression();
        checkSimple(node, ASTClassInstanceCreationExpression.class);
        node.collapseThenPrint();
    }
}
