package org.spruce.compiler.test;

import org.spruce.compiler.ast.expressions.*;
import org.spruce.compiler.ast.names.*;
import org.spruce.compiler.ast.statements.*;
import org.spruce.compiler.ast.types.*;
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
        StatementsParser parser = new StatementsParser(new Scanner("{}"));
        ASTBlock node = parser.parseBlock();
        checkEmpty(node, OPEN_BRACE);
    }

    /**
     * Tests block of block statements.
     */
    @Test
    public void testBlockOfBlockStatements()
    {
        StatementsParser parser = new StatementsParser(new Scanner("{Integer a := 1;\nInteger b := 2;\nreturn a + b;}"));
        ASTBlock node = parser.parseBlock();
        checkSimple(node, ASTBlockStatements.class, OPEN_BRACE);
    }

    /**
     * Test block statements of block statement instances.
     */
    @Test
    public void testBlockStatements()
    {
        StatementsParser parser = new StatementsParser(new Scanner("final String stmt := \"Statement one!\";\nconst Integer stmt2Nbr := 2;\ni++;}"));
        ASTBlockStatements node = parser.parseBlockStatements();
        checkList(node, null, ASTBlockStatement.class, 3);
    }

    /**
     * Tests block statement of modifier and local variable declaration.
     */
    @Test
    public void testBlockStatementOfModifierDeclaration()
    {
        StatementsParser parser = new StatementsParser(new Scanner("final Integer i := 1;"));
        ASTBlockStatement node = parser.parseBlockStatement();
        checkSimple(node, ASTLocalVariableDeclarationStatement.class);
    }

    /**
     * Tests block statement of local variable declaration.
     */
    @Test
    public void testBlockStatementOfDeclaration()
    {
        StatementsParser parser = new StatementsParser(new Scanner("Integer i := 1;"));
        ASTBlockStatement node = parser.parseBlockStatement();
        checkSimple(node, ASTLocalVariableDeclarationStatement.class);
    }

    /**
     * Tests block statement of assignment.
     */
    @Test
    public void testBlockStatementOfAssignment()
    {
        StatementsParser parser = new StatementsParser(new Scanner("i := 1;"));
        ASTBlockStatement node = parser.parseBlockStatement();
        checkSimple(node, ASTStatement.class);
    }

    /**
     * Tests block statement of method invocation.
     */
    @Test
    public void testBlockStatementOfMethodInvocation()
    {
        StatementsParser parser = new StatementsParser(new Scanner("i(j);"));
        ASTBlockStatement node = parser.parseBlockStatement();
        checkSimple(node, ASTStatement.class);
    }

    /**
     * Tests block statement of qualified class instance creation expression.
     */
    @Test
    public void testBlockStatementOfCICE()
    {
        StatementsParser parser = new StatementsParser(new Scanner("i.new J();"));
        ASTBlockStatement node = parser.parseBlockStatement();
        checkSimple(node, ASTStatement.class);
    }

    /**
     * Tests block statement of return statement.
     */
    @Test
    public void testBlockStatementOfReturn()
    {
        StatementsParser parser = new StatementsParser(new Scanner("return true;"));
        ASTBlockStatement node = parser.parseBlockStatement();
        checkSimple(node, ASTStatement.class);
    }

    /**
     * Tests local variable declaration statement.
     */
    @Test
    public void testLocalVariableDeclarationStatement()
    {
        StatementsParser parser = new StatementsParser(new Scanner("Integer[] values := {1, 2, 3};"));
        ASTLocalVariableDeclarationStatement node = parser.parseLocalVariableDeclarationStatement();
        checkSimple(node, ASTLocalVariableDeclaration.class, SEMICOLON);
    }

    /**
     * Tests local variable declaration without modifiers.
     */
    @Test
    public void testLocalVariableDeclaration()
    {
        StatementsParser parser = new StatementsParser(new Scanner("Boolean result := true, done := false"));
        ASTLocalVariableDeclaration node = parser.parseLocalVariableDeclaration();
        checkBinary(node, ASTLocalVariableType.class, ASTVariableDeclaratorList.class);
    }

    /**
     * Tests local variable declaration with modifiers.
     */
    @Test
    public void testLocalVariableDeclarationOfModifiers()
    {
        StatementsParser parser = new StatementsParser(new Scanner("final const Boolean result := true, done := false"));
        ASTLocalVariableDeclaration node = parser.parseLocalVariableDeclaration();
        checkTrinary(node, null, ASTVariableModifierList.class, ASTLocalVariableType.class, ASTVariableDeclaratorList.class);
    }

    /**
     * Tests variable modifier list of variable modifier.
     */
    @Test
    public void testVariableModifierListOfVariableModifier()
    {
        StatementsParser parser = new StatementsParser(new Scanner("const"));
        ASTVariableModifierList node = parser.parseVariableModifierList();
        checkSimple(node, ASTVariableModifier.class);
    }
    /**
     * Tests variable modifier list of variable modifiers.
     */
    @Test
    public void testVariableModifierListOfVariableModifiers()
    {
        StatementsParser parser = new StatementsParser(new Scanner("final const"));
        ASTVariableModifierList node = parser.parseVariableModifierList();
        checkList(node, null, ASTVariableModifier.class, 2);
    }

    /**
     * Tests variable modifier of "const".
     */
    @Test
    public void testVariableModifierOfConst()
    {
        StatementsParser parser = new StatementsParser(new Scanner("const"));
        ASTVariableModifier node = parser.parseVariableModifier();
        checkEmpty(node, CONST);
    }

    /**
     * Tests variable modifier of "final".
     */
    @Test
    public void testVariableModifierOfFinal()
    {
        StatementsParser parser = new StatementsParser(new Scanner("final"));
        ASTVariableModifier node = parser.parseVariableModifier();
        checkEmpty(node, FINAL);
    }

    /**
     * Tests variable modifier of "constant".
     */
    @Test
    public void testVariableModifierOfConstant()
    {
        StatementsParser parser = new StatementsParser(new Scanner("constant"));
        ASTVariableModifier node = parser.parseVariableModifier();
        checkEmpty(node, CONSTANT);
    }

    /**
     * Tests variable declarator list of variable declarator.
     */
    @Test
    public void testVariableDeclaratorListOfVariableDeclarator()
    {
        StatementsParser parser = new StatementsParser(new Scanner("a := b"));
        ASTVariableDeclaratorList node = parser.parseVariableDeclaratorList();
        checkSimple(node, ASTVariableDeclarator.class, COMMA);
    }

    /**
     * Tests variable declarator list.
     */
    @Test
    public void testVariableDeclaratorList()
    {
        StatementsParser parser = new StatementsParser(new Scanner("x := 1, y := x"));
        ASTVariableDeclaratorList node = parser.parseVariableDeclaratorList();
        checkList(node, COMMA, ASTVariableDeclarator.class, 2);
    }

    /**
     * Tests nested variable declarator lists.
     */
    @Test
    public void testVariableDeclaratorListNested()
    {
        StatementsParser parser = new StatementsParser(new Scanner("a := 1, b := a + 1, c := 2 * b"));
        ASTVariableDeclaratorList node = parser.parseVariableDeclaratorList();
        checkList(node, COMMA, ASTVariableDeclarator.class, 3);
    }

    /**
     * Tests variable declarator of identifier.
     */
    @Test
    public void testVariableDeclaratorOfIdentifier()
    {
        StatementsParser parser = new StatementsParser(new Scanner("varName"));
        ASTVariableDeclarator node = parser.parseVariableDeclarator();
        checkSimple(node, ASTIdentifier.class);
    }

    /**
     * Tests variable declarator of identifier and variable initializer.
     */
    @Test
    public void testVariableDeclaratorOfIdentifierVariableInitializer()
    {
        StatementsParser parser = new StatementsParser(new Scanner("count := 2"));
        ASTVariableDeclarator node = parser.parseVariableDeclarator();
        checkBinary(node, ASSIGNMENT, ASTIdentifier.class, ASTVariableInitializer.class);
    }

    /**
     * Tests local variable type of data type.
     */
    @Test
    public void testLocalVariableTypeOfDataType()
    {
        StatementsParser parser = new StatementsParser(new Scanner("spruce.lang.String[][])"));
        ASTLocalVariableType node = parser.parseLocalVariableType();
        checkSimple(node, ASTDataType.class);
    }

    /**
     * Tests local variable type of "auto".
     */
    @Test
    public void testLocalVariableTypeOfAuto()
    {
        StatementsParser parser = new StatementsParser(new Scanner("auto"));
        ASTLocalVariableType node = parser.parseLocalVariableType();
        checkEmpty(node, AUTO);
    }

    /**
     * Tests statement of block.
     */
    @Test
    public void testStatementOfBlock()
    {
        StatementsParser parser = new StatementsParser(new Scanner("{x := x + 1;}"));
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTBlock.class);
    }

    /**
     * Tests statement of expression statement.
     */
    @Test
    public void testStatementOfExpressionStatement()
    {
        StatementsParser parser = new StatementsParser(new Scanner("x := x + 1;"));
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTExpressionStatement.class);
    }

    /**
     * Tests statement of return statement.
     */
    @Test
    public void testStatementOfReturnStatement()
    {
        StatementsParser parser = new StatementsParser(new Scanner("return true;"));
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTReturnStatement.class);
    }

    /**
     * Tests statement of throw statement.
     */
    @Test
    public void testStatementOfThrowStatement()
    {
        StatementsParser parser = new StatementsParser(new Scanner("throw new CompileException(\"Error message\");"));
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTThrowStatement.class);
    }

    /**
     * Tests statement of break statement.
     */
    @Test
    public void testStatementOfBreakStatement()
    {
        StatementsParser parser = new StatementsParser(new Scanner("break;"));
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTBreakStatement.class);
    }

    /**
     * Tests statement of break statement.
     */
    @Test
    public void testStatementOfContinueStatement()
    {
        StatementsParser parser = new StatementsParser(new Scanner("continue;"));
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTContinueStatement.class);
    }

    /**
     * Tests statement of break statement.
     */
    @Test
    public void testStatementOfFallthroughStatement()
    {
        StatementsParser parser = new StatementsParser(new Scanner("fallthrough;"));
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTFallthroughStatement.class);
    }

    /**
     * Tests statement of assert statement.
     */
    @Test
    public void testStatementOfAssertStatement()
    {
        StatementsParser parser = new StatementsParser(new Scanner("assert status = true;"));
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTAssertStatement.class);
    }

    /**
     * Tests statement of if statement.
     */
    @Test
    public void testStatementOfIfStatement()
    {
        StatementsParser parser = new StatementsParser(new Scanner("if (success) { return true; }"));
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTIfStatement.class);
    }

    /**
     * Tests statement of while statement.
     */
    @Test
    public void testStatementOfWhileStatement()
    {
        StatementsParser parser = new StatementsParser(new Scanner("while (shouldContinue) doWork();"));
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTWhileStatement.class);
    }

    /**
     * Tests statement of do statement.
     */
    @Test
    public void testStatementOfDoStatement()
    {
        StatementsParser parser = new StatementsParser(new Scanner("do { work(); } while (shouldContinue);"));
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTDoStatement.class);
    }

    /**
     * Tests statement of synchronized statement.
     */
    @Test
    public void testStatementOfSynchronizedStatement()
    {
        StatementsParser parser = new StatementsParser(new Scanner("synchronized (myLock) {\n    myLock.wait();\n}"));
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTSynchronizedStatement.class);
    }

    /**
     * Tests statement of for statement.
     */
    @Test
    public void testStatementOfForStatement()
    {
        StatementsParser parser = new StatementsParser(new Scanner("for (;;) doWork();"));
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTForStatement.class);
    }

    /**
     * Tests statement of try statement.
     */
    @Test
    public void testStatementOfTryStatement()
    {
        StatementsParser parser = new StatementsParser(new Scanner("try {\n    br.readLine();\n} catch (IOException e) {\n    out.println(e.getMessage());\n}"));
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTTryStatement.class);
    }

    /**
     * Tests statement of switch statement.
     */
    @Test
    public void testStatementOfSwitchStatement()
    {
        StatementsParser parser = new StatementsParser(new Scanner("switch (code) {\ncase 1: out.println(\"One\");\ncase 2: out.println(\"Two\");\ndefault: out.println(\"Unexpected\");\n}"));
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTSwitchStatement.class);
    }

    /**
     * Tests switch statement.
     */
    @Test
    public void testSwitchStatement()
    {
        StatementsParser parser = new StatementsParser(new Scanner("switch (code) {\ncase 1: out.println(\"One\");\ncase 2: out.println(\"Two\");\ndefault: out.println(\"Unexpected\");\n}"));
        ASTSwitchStatement node = parser.parseSwitchStatement();
        checkBinary(node, SWITCH, ASTExpression.class, ASTSwitchBlock.class);
    }

    /**
     * Tests empty switch block.
     */
    @Test
    public void testSwitchBlockEmpty()
    {
        StatementsParser parser = new StatementsParser(new Scanner("{}"));
        ASTSwitchBlock node = parser.parseSwitchBlock();
        checkEmpty(node, OPEN_BRACE);
    }

    /**
     * Tests switch block of switch cases.
     */
    @Test
    public void testSwitchBlockOfSwitchCases()
    {
        StatementsParser parser = new StatementsParser(new Scanner("{case 1: out.println(\"Success\");}"));
        ASTSwitchBlock node = parser.parseSwitchBlock();
        checkSimple(node, ASTSwitchCases.class, OPEN_BRACE);
    }

    /**
     * Tests switch cases of switch case.
     */
    @Test
    public void testSwitchCasesOfSwitchCase()
    {
        StatementsParser parser = new StatementsParser(new Scanner("case 1: out.println(\"Success\");}"));
        ASTSwitchCases node = parser.parseSwitchCases();
        checkSimple(node, ASTSwitchCase.class);
    }

    /**
     * Tests switch cases of switch case instances (here, just multiple switch cases).
     */
    @Test
    public void testSwitchCasesNested()
    {
        StatementsParser parser = new StatementsParser(new Scanner("case 1: out.println(\"Success\");\ncase 2: out.println(\"Pass\");}"));
        ASTSwitchCases node = parser.parseSwitchCases();
        checkList(node, null, ASTSwitchCase.class, 2);
    }

    /**
     * Tests switch case of block statements.
     */
    @Test
    public void testSwitchCaseOfBlockStatements()
    {
        StatementsParser parser = new StatementsParser(new Scanner("case 1, 2, 3:\n    out.println(\"Case found!\");\n    fallthrough;\n}"));
        ASTSwitchCase node = parser.parseSwitchCase();
        checkBinary(node, ASTSwitchLabel.class, ASTBlockStatements.class);
    }

    /**
     * Tests switch case of no statements.
     */
    @Test
    public void testSwitchCaseOfEmpty()
    {
        StatementsParser parser = new StatementsParser(new Scanner("case 1, 2, 3:}"));
        ASTSwitchCase node = parser.parseSwitchCase();
        checkSimple(node, ASTSwitchLabel.class);
    }

    /**
     * Tests switch label of case and switch values.
     */
    @Test
    public void testSwitchLabelOfCase()
    {
        StatementsParser parser = new StatementsParser(new Scanner("case 1, 2, 3:"));
        ASTSwitchLabel node = parser.parseSwitchLabel();
        checkSimple(node, ASTSwitchValues.class, CASE);
    }

    /**
     * Tests switch label of default.
     */
    @Test
    public void testSwitchLabelOfDefault()
    {
        StatementsParser parser = new StatementsParser(new Scanner("default:"));
        ASTSwitchLabel node = parser.parseSwitchLabel();
        checkEmpty(node, DEFAULT);
    }

    /**
     * Tests switch values of expression (no incr/decr).
     */
    @Test
    public void testSwitchValuesOfExpressionNoIncrDecr()
    {
        StatementsParser parser = new StatementsParser(new Scanner("1"));
        ASTSwitchValues node = parser.parseSwitchValues();
        checkSimple(node, ASTSwitchValue.class, COMMA);
    }

    /**
     * Tests nested switch values (here, just multiple instances of switch value).
     */
    @Test
    public void testSwitchValuesNested()
    {
        StatementsParser parser = new StatementsParser(new Scanner("1, 2, 3"));
        ASTSwitchValues node = parser.parseSwitchValues();
        checkList(node, COMMA, ASTSwitchValue.class, 3);
    }

    /**
     * Tests switch value of identifier.
     */
    @Test
    public void testSwitchValueOfIdentifier()
    {
        StatementsParser parser = new StatementsParser(new Scanner("BLUE:"));
        ASTSwitchValue node = parser.parseSwitchValue();
        checkSimple(node, ASTIdentifier.class);
    }

    /**
     * Tests switch value of expression (no incr/decr).
     */
    @Test
    public void testSwitchValueOfExpressionNoIncrDecr()
    {
        StatementsParser parser = new StatementsParser(new Scanner("1 + 2"));
        ASTSwitchValue node = parser.parseSwitchValue();
        checkSimple(node, ASTExpressionNoIncrDecr.class);
    }

    /**
     * Tests try statement of catch.
     */
    @Test
    public void testTryStatementOfCatch()
    {
        StatementsParser parser = new StatementsParser(new Scanner("try {\n    br.readLine();\n} catch (IOException e) {\n    out.println(e.getMessage());\n}"));
        ASTTryStatement node = parser.parseTryStatement();
        checkBinary(node, TRY, ASTBlock.class, ASTCatches.class);
    }

    /**
     * Tests try statement of finally.
     */
    @Test
    public void testTryStatementOfFinally()
    {
        StatementsParser parser = new StatementsParser(new Scanner("try {\n    br.readLine();\n} finally {\n    br.close();\n}"));
        ASTTryStatement node = parser.parseTryStatement();
        checkBinary(node, TRY, ASTBlock.class, ASTFinally.class);
    }

    /**
     * Tests try statement of resource specification.
     */
    @Test
    public void testTryStatementOfResourceSpecification()
    {
        StatementsParser parser = new StatementsParser(new Scanner("try (BufferedReader br := new BufferedReader()){\n    br.readLine();\n}"));
        ASTTryStatement node = parser.parseTryStatement();
        checkBinary(node, TRY, ASTResourceSpecification.class, ASTBlock.class);
    }

    /**
     * Tests try statement of all optionals.
     */
    @Test
    public void testTryStatementOfAll()
    {
        StatementsParser parser = new StatementsParser(new Scanner("try (BufferedReader br := new BufferedReader()){\n    br.readLine();\n} catch (IOException e) {\n    out.println(e.getMessage());\n} finally {\n    br.close();\n}"));
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
        StatementsParser parser = new StatementsParser(new Scanner("(fr; BufferedReader br := new BufferedReader(fr))"));
        ASTResourceSpecification node = parser.parseResourceSpecification();
        checkSimple(node, ASTResourceList.class);
    }

    /**
     * Tests resource specification of resource list and semicolon.
     */
    @Test
    public void testResourceSpecificationSemicolon()
    {
        StatementsParser parser = new StatementsParser(new Scanner("(fr; BufferedReader br := new BufferedReader(fr);)"));
        ASTResourceSpecification node = parser.parseResourceSpecification();
        checkSimple(node, ASTResourceList.class);
    }

    /**
     * Tests resource list of resource.
     */
    @Test
    public void testResourceListOfResource()
    {
        StatementsParser parser = new StatementsParser(new Scanner("BufferedReader br := new BufferedReader()"));
        ASTResourceList node = parser.parseResourceList();
        checkSimple(node, ASTResource.class, SEMICOLON);
    }

    /**
     * Tests resource list of nested resource lists (here, just multiple resources).
     */
    @Test
    public void testResourceListNested()
    {
        StatementsParser parser = new StatementsParser(new Scanner("fr; BufferedReader br := new BufferedReader(fr)"));
        ASTResourceList node = parser.parseResourceList();
        checkList(node, SEMICOLON, ASTResource.class, 2);
    }

    /**
     * Test resource of resource declaration.
     */
    @Test
    public void testResourceOfResourceDeclaration()
    {
        StatementsParser parser = new StatementsParser(new Scanner("BufferedReader br := new BufferedReader()"));
        ASTResource node = parser.parseResource();
        checkSimple(node, ASTResourceDeclaration.class);
    }

    /**
     * Test resource of expression name.
     */
    @Test
    public void testResourceOfExpressionName()
    {
        StatementsParser parser = new StatementsParser(new Scanner("br"));
        ASTResource node = parser.parseResource();
        checkSimple(node, ASTExpressionName.class);
    }

    /**
     * Test resource of field access.
     */
    @Test
    public void testResourceOfFieldAccess()
    {
        StatementsParser parser = new StatementsParser(new Scanner("super.br"));
        ASTResource node = parser.parseResource();
        checkSimple(node, ASTFieldAccess.class);
    }

    /**
     * Test resource declaration, no variable modifiers.
     */
    @Test
    public void testResourceDeclaration()
    {
        StatementsParser parser = new StatementsParser(new Scanner("BufferedReader br := new BufferedReader()"));
        ASTResourceDeclaration node = parser.parseResourceDeclaration();
        checkTrinary(node, ASSIGNMENT, ASTLocalVariableType.class, ASTIdentifier.class, ASTExpressionNoIncrDecr.class);
    }

    /**
     * Test resource declaration, with variable modifiers.
     */
    @Test
    public void testResourceDeclarationOfVariableModifier()
    {
        StatementsParser parser = new StatementsParser(new Scanner("final BufferedReader br := new BufferedReader()"));
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
        StatementsParser parser = new StatementsParser(new Scanner("catch (FileNotFoundException e) { err.println(e.getMessage()); }\ncatch (IOException e) {out.println(e.getMessage()); }"));
        ASTCatches node = parser.parseCatches();
        checkList(node, null, ASTCatchClause.class, 2);
    }

    /**
     * Tests catch clause.
     */
    @Test
    public void testCatchClause()
    {
        StatementsParser parser = new StatementsParser(new Scanner("catch (CompileException ce) { out.println(ce.getMessage()); }"));
        ASTCatchClause node = parser.parseCatchClause();
        checkBinary(node, CATCH, ASTCatchFormalParameter.class, ASTBlock.class);
    }

    /**
     * Tests catch type of data type.
     */
    @Test
    public void testCatchTypeOfDataType()
    {
        StatementsParser parser = new StatementsParser(new Scanner("Exception"));
        ASTCatchType node = parser.parseCatchType();
        checkSimple(node, ASTDataType.class, BITWISE_OR);
    }

    /**
     * Tests catch formal parameter without modifiers.
     */
    @Test
    public void testCatchFormalParameter()
    {
        StatementsParser parser = new StatementsParser(new Scanner("Exception e"));
        ASTCatchFormalParameter node = parser.parseCatchFormalParameter();
        checkBinary(node, ASTCatchType.class, ASTIdentifier.class);
    }

    /**
     * Tests catch formal parameter with modifiers.
     */
    @Test
    public void testCatchFormalParameterOfModifiers()
    {
        StatementsParser parser = new StatementsParser(new Scanner("final CustomException ce"));
        ASTCatchFormalParameter node = parser.parseCatchFormalParameter();
        checkTrinary(node, null, ASTVariableModifierList.class, ASTCatchType.class, ASTIdentifier.class);
    }

    /**
     * Tests catch type.
     */
    @Test
    public void testCatchType()
    {
        StatementsParser parser = new StatementsParser(new Scanner("IOException | SQLException"));
        ASTCatchType node = parser.parseCatchType();
        checkList(node, BITWISE_OR, ASTDataType.class, 2);
    }

    /**
     * Tests nested catch types, here, just a list of data types.
     */
    @Test
    public void testCatchTypeNested()
    {
        StatementsParser parser = new StatementsParser(new Scanner("ArrayIndexOutOfBoundsException | NullPointerException | IllegalArgumentException"));
        ASTCatchType node = parser.parseCatchType();
        checkList(node, BITWISE_OR, ASTDataType.class, 3);
    }

    /**
     * Tests finally block.
     */
    @Test
    public void testFinally()
    {
        StatementsParser parser = new StatementsParser(new Scanner("finally { out.println(\"Always executed!\"); }"));
        ASTFinally node = parser.parseFinally();
        checkSimple(node, ASTBlock.class, FINALLY);
    }

    /**
     * Tests simple if statement.
     */
    @Test
    public void testIfStatementOfSimple()
    {
        StatementsParser parser = new StatementsParser(new Scanner("if (success) { return true; }"));
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
        StatementsParser parser = new StatementsParser(new Scanner("if {String line := br.readLine()} (line != null) out.println(line);"));
        ASTIfStatement node = parser.parseIfStatement();
        checkTrinary(node, IF, ASTInit.class, ASTExpressionNoIncrDecr.class, ASTStatement.class);
    }

    /**
     * Tests if statement with else.
     */
    @Test
    public void testIfStatementOfElse()
    {
        StatementsParser parser = new StatementsParser(new Scanner("if (result) {\n    out.println(\"Test passed.\");\n} else {\n    out.println(\"Test FAILED!\");\n}"));
        ASTIfStatement node = parser.parseIfStatement();
        checkTrinary(node, IF, ASTExpressionNoIncrDecr.class, ASTStatement.class, ASTStatement.class);
    }

    /**
     * Tests nested if statements (if/else if/else).
     */
    @Test
    public void testIfStatementNested()
    {
        StatementsParser parser = new StatementsParser(new Scanner("if (result) {\n    out.println(\"Test passed.\");\n} else if (DEBUG) {\n    out.println(\"Test failed in debug mode!\");\n} else {\n    out.println(\"Test FAILED!\");\n}"));
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
        StatementsParser parser = new StatementsParser(new Scanner("while (shouldContinue) { doWork(); }"));
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
        StatementsParser parser = new StatementsParser(new Scanner("while {String line := br.readLine()} (line != null) out.println(line);"));
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
        StatementsParser parser = new StatementsParser(new Scanner("do { work(); } while (shouldContinue);"));
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
        StatementsParser parser = new StatementsParser(new Scanner("synchronized (lock) { lock.notifyAll(); }"));
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
        StatementsParser parser = new StatementsParser(new Scanner("for (Int i := 0; i < 10; i++) {\n    out.println(i);\n}"));
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
        StatementsParser parser = new StatementsParser(new Scanner("for (;;) {\n    out.println(\"Hello world!\");\n}"));
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
        StatementsParser parser = new StatementsParser(new Scanner("for (Int i : array) {\n    sum += i;\n}"));
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
        StatementsParser parser = new StatementsParser(new Scanner("return;"));
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
        StatementsParser parser = new StatementsParser(new Scanner("return x.y + 2;"));
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
        StatementsParser parser = new StatementsParser(new Scanner("throw new Exception();"));
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
        StatementsParser parser = new StatementsParser(new Scanner("break;"));
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
        StatementsParser parser = new StatementsParser(new Scanner("continue;"));
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
        StatementsParser parser = new StatementsParser(new Scanner("fallthrough;"));
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
        StatementsParser parser = new StatementsParser(new Scanner("assert result = true;"));
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
        StatementsParser parser = new StatementsParser(new Scanner("assert result = true : \"Assertion failed!\";"));
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
        StatementsParser parser = new StatementsParser(new Scanner("x++;"));
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
        StatementsParser parser = new StatementsParser(new Scanner("Int i := 0, j := 0"));
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
        StatementsParser parser = new StatementsParser(new Scanner("i := 0"));
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
        StatementsParser parser = new StatementsParser(new Scanner("i := 0, j := 0, k := 1"));
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
        StatementsParser parser = new StatementsParser(new Scanner("i := 0"));
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
        StatementsParser parser = new StatementsParser(new Scanner("i := 0, j := 0, k := 1"));
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
        StatementsParser parser = new StatementsParser(new Scanner("x := 0"));
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
        StatementsParser parser = new StatementsParser(new Scanner("x.y++"));
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
        StatementsParser parser = new StatementsParser(new Scanner("--x.y"));
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
        StatementsParser parser = new StatementsParser(new Scanner("x.y(2)"));
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
        StatementsParser parser = new StatementsParser(new Scanner("new SideEffect()"));
        ASTStatementExpression node = parser.parseStatementExpression();
        checkSimple(node, ASTClassInstanceCreationExpression.class);
        node.collapseThenPrint();
    }
}
