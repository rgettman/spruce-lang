package org.spruce.compiler.test;

import org.spruce.compiler.ast.expressions.*;
import org.spruce.compiler.ast.names.*;
import org.spruce.compiler.ast.statements.*;
import org.spruce.compiler.ast.types.*;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.parser.Parser;
import org.spruce.compiler.parser.StatementsParser;
import org.spruce.compiler.scanner.Scanner;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.spruce.compiler.scanner.TokenType.*;
import static org.spruce.compiler.test.ParserTestUtility.*;

import org.junit.jupiter.api.Test;

/**
 * All tests for the parser related to statements.
 */
public class ParserStatementsTest {

    @Test
    public void testNestedBlocks() {
        StatementsParser parser = getStatementsParser("""
            {
                {
                    Int a = 1;
                }
            }
            """);
        ASTBlock node = parser.parseBlock();
        checkSimple(node, ASTBlockStatements.class, OPEN_BRACE);
        ASTBlockStatements blockStmts = (ASTBlockStatements) node.getChildren().get(0);
        checkSimple(blockStmts, ASTBlockStatement.class);
        ASTBlockStatement blockStmt = (ASTBlockStatement) blockStmts.getChildren().get(0);
        checkSimple(blockStmt, ASTStatement.class);
        ASTStatement stmt = (ASTStatement) blockStmt.getChildren().get(0);
        checkSimple(stmt, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests block of empty braces.
     */
    @Test
    public void testBlockOfNothing() {
        StatementsParser parser = getStatementsParser("{}");
        ASTBlock node = parser.parseBlock();
        checkEmpty(node, OPEN_BRACE);
        node.collapseThenPrint();
    }

    /**
     * Tests block of block statements.
     */
    @Test
    public void testBlockOfBlockStatements() {
        StatementsParser parser = getStatementsParser("""
            {
                Integer a = 1;
                Integer b = 2;
                return a + b;
            }
            """);
        ASTBlock node = parser.parseBlock();
        checkSimple(node, ASTBlockStatements.class, OPEN_BRACE);
        node.collapseThenPrint();
    }

    /**
     * Test block statements of block statement instances.
     */
    @Test
    public void testBlockStatements() {
        StatementsParser parser = getStatementsParser("""
            String stmt = "Statement one!";
            Integer stmt2Nbr = 2;
            i++;}
            """);
        ASTBlockStatements node = parser.parseBlockStatements();
        checkList(node, null, ASTBlockStatement.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests block statement of modifier and local variable declaration.
     */
    @Test
    public void testBlockStatementOfModifierDeclaration() {
        StatementsParser parser = getStatementsParser("Integer i = 1;");
        ASTBlockStatement node = parser.parseBlockStatement();
        checkSimple(node, ASTLocalVariableDeclarationStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests block statement of local variable declaration.
     */
    @Test
    public void testBlockStatementOfDeclaration() {
        StatementsParser parser = getStatementsParser("Integer i = 1;");
        ASTBlockStatement node = parser.parseBlockStatement();
        checkSimple(node, ASTLocalVariableDeclarationStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests block statement of assignment.
     */
    @Test
    public void testBlockStatementOfAssignment() {
        StatementsParser parser = getStatementsParser("i = 1;");
        ASTBlockStatement node = parser.parseBlockStatement();
        checkSimple(node, ASTStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests block statement of method invocation.
     */
    @Test
    public void testBlockStatementOfMethodInvocation() {
        StatementsParser parser = getStatementsParser("i(j);");
        ASTBlockStatement node = parser.parseBlockStatement();
        checkSimple(node, ASTStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests block statement of qualified class instance creation expression.
     */
    @Test
    public void testBlockStatementOfCICE() {
        StatementsParser parser = getStatementsParser("i.new J();");
        ASTBlockStatement node = parser.parseBlockStatement();
        checkSimple(node, ASTStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests block statement of return statement.
     */
    @Test
    public void testBlockStatementOfReturn() {
        StatementsParser parser = getStatementsParser("return true;");
        ASTBlockStatement node = parser.parseBlockStatement();
        checkSimple(node, ASTStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests local variable declaration statement.
     */
    @Test
    public void testLocalVariableDeclarationStatement() {
        StatementsParser parser = getStatementsParser("Integer[] values = {1, 2, 3};");
        ASTLocalVariableDeclarationStatement node = parser.parseLocalVariableDeclarationStatement();
        checkSimple(node, ASTLocalVariableDeclaration.class, SEMICOLON);
        node.collapseThenPrint();
    }

    /**
     * Tests bad local variable declaration statement.
     */
    @Test
    public void testLocalVariableDeclarationStatementBad() {
        StatementsParser parser = getStatementsParser("Integer[] values := {1, 2, 3};");
        assertThrows(CompileException.class, parser::parseLocalVariableDeclarationStatement, "Error: Use '=' for assignment, not ':='.");
    }

    /**
     * Tests local variable declaration without modifiers.
     */
    @Test
    public void testLocalVariableDeclaration() {
        StatementsParser parser = getStatementsParser("Boolean result = true, done = false");
        ASTLocalVariableDeclaration node = parser.parseLocalVariableDeclaration();
        checkBinary(node, ASTLocalVariableType.class, ASTVariableDeclaratorList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests local variable declaration with modifiers.
     */
    @Test
    public void testLocalVariableDeclarationOfModifiers() {
        StatementsParser parser = getStatementsParser("mut Boolean result = true, var done = false");
        ASTLocalVariableDeclaration node = parser.parseLocalVariableDeclaration();
        checkTrinary(node, null, ASTVariableModifierList.class, ASTLocalVariableType.class, ASTVariableDeclaratorList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests variable modifier list of variable modifier.
     */
    @Test
    public void testVariableModifierListOfVariableModifier() {
        StatementsParser parser = getStatementsParser("var");
        ASTVariableModifierList node = parser.parseVariableModifierList();
        checkSimple(node, ASTVariableModifier.class);
        node.collapseThenPrint();
    }
    /**
     * Tests variable modifier list of variable modifiers.
     */
    @Test
    public void testVariableModifierListOfVariableModifiers() {
        StatementsParser parser = getStatementsParser("var mut");
        ASTVariableModifierList node = parser.parseVariableModifierList();
        checkList(node, null, ASTVariableModifier.class, 2);
        node.collapseThenPrint();
    }

    /**
     * Tests variable modifier of "var".
     */
    @Test
    public void testVariableModifierOfVar() {
        StatementsParser parser = getStatementsParser("var");
        ASTVariableModifier node = parser.parseVariableModifier();
        checkEmpty(node, VAR);
        node.collapseThenPrint();
    }

    /**
     * Tests variable modifier of "mut".
     */
    @Test
    public void testVariableModifierOfMut() {
        StatementsParser parser = getStatementsParser("mut");
        ASTVariableModifier node = parser.parseVariableModifier();
        checkEmpty(node, MUT);
        node.collapseThenPrint();
    }

    /**
     * Tests variable declarator list of variable declarator.
     */
    @Test
    public void testVariableDeclaratorListOfVariableDeclarator() {
        StatementsParser parser = getStatementsParser("a = b");
        ASTVariableDeclaratorList node = parser.parseVariableDeclaratorList();
        checkSimple(node, ASTVariableDeclarator.class, COMMA);
        node.collapseThenPrint();
    }

    /**
     * Tests variable declarator list.
     */
    @Test
    public void testVariableDeclaratorList() {
        StatementsParser parser = getStatementsParser("x = 1, y = x");
        ASTVariableDeclaratorList node = parser.parseVariableDeclaratorList();
        checkList(node, COMMA, ASTVariableDeclarator.class, 2);
        node.collapseThenPrint();
    }

    /**
     * Tests nested variable declarator lists.
     */
    @Test
    public void testVariableDeclaratorListNested() {
        StatementsParser parser = getStatementsParser("a = 1, b = a + 1, c = 2 * b");
        ASTVariableDeclaratorList node = parser.parseVariableDeclaratorList();
        checkList(node, COMMA, ASTVariableDeclarator.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests variable declarator of identifier.
     */
    @Test
    public void testVariableDeclaratorOfIdentifier() {
        StatementsParser parser = getStatementsParser("varName");
        ASTVariableDeclarator node = parser.parseVariableDeclarator();
        checkSimple(node, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests variable declarator of identifier and variable initializer.
     */
    @Test
    public void testVariableDeclaratorOfIdentifierVariableInitializer() {
        StatementsParser parser = getStatementsParser("count = 2");
        ASTVariableDeclarator node = parser.parseVariableDeclarator();
        checkBinary(node, EQUAL, ASTIdentifier.class, ASTVariableInitializer.class);
        node.collapseThenPrint();
    }

    /**
     * Tests local variable type of data type.
     */
    @Test
    public void testLocalVariableTypeOfDataType() {
        StatementsParser parser = getStatementsParser("spruce.lang.String[][])");
        ASTLocalVariableType node = parser.parseLocalVariableType();
        checkSimple(node, ASTDataType.class);
        node.collapseThenPrint();
    }

    /**
     * Tests local variable type of "auto".
     */
    @Test
    public void testLocalVariableTypeOfAuto() {
        StatementsParser parser = getStatementsParser("auto");
        ASTLocalVariableType node = parser.parseLocalVariableType();
        checkEmpty(node, AUTO);
        node.collapseThenPrint();
    }

    /**
     * Tests statement of block.
     */
    @Test
    public void testStatementOfBlock() {
        StatementsParser parser = getStatementsParser("{x = x + 1;}");
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement of expression statement.
     */
    @Test
    public void testStatementOfExpressionStatement() {
        StatementsParser parser = getStatementsParser("x = x + 1;");
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTExpressionStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement of return statement.
     */
    @Test
    public void testStatementOfReturnStatement() {
        StatementsParser parser = getStatementsParser("return true;");
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTReturnStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement of throw statement.
     */
    @Test
    public void testStatementOfThrowStatement() {
        StatementsParser parser = getStatementsParser("throw new CompileException(\"Error message\");");
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTThrowStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement of break statement.
     */
    @Test
    public void testStatementOfBreakStatement() {
        StatementsParser parser = getStatementsParser("break;");
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTBreakStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement of break statement.
     */
    @Test
    public void testStatementOfContinueStatement() {
        StatementsParser parser = getStatementsParser("continue;");
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTContinueStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement of break statement.
     */
    @Test
    public void testStatementOfFallthroughStatement() {
        StatementsParser parser = getStatementsParser("fallthrough;");
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTFallthroughStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement of assert statement.
     */
    @Test
    public void testStatementOfAssertStatement() {
        StatementsParser parser = getStatementsParser("assert status == true;");
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTAssertStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement of if statement.
     */
    @Test
    public void testStatementOfIfStatement() {
        StatementsParser parser = getStatementsParser("if (success) { return true; }");
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTIfStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement of while statement without braces.
     */
    @Test
    public void testStatementOfWhileStatementNoBlock() {
        StatementsParser parser = getStatementsParser("while (shouldContinue) doWork();");
        assertThrows(CompileException.class, parser::parseStatement, "Error: Expected '{'.");
    }

    /**
     * Tests statement of while statement.
     */
    @Test
    public void testStatementOfWhileStatement() {
        StatementsParser parser = getStatementsParser("""
            while (shouldContinue) {
                doWork();
            }
            """);
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTWhileStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement of do statement.
     */
    @Test
    public void testStatementOfDoStatement() {
        StatementsParser parser = getStatementsParser("""
            do {
                work();
            } while shouldContinue;
            """);
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTDoStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement of critical statement.
     */
    @Test
    public void testStatementOfCriticalStatement() {
        StatementsParser parser = getStatementsParser("""
            critical myLock {
                myLock.wait();
            }
        """);
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTCriticalStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement of for statement, no block.
     */
    @Test
    public void testStatementOfForStatementNoBlock() {
        StatementsParser parser = getStatementsParser("for (;;) doWork();");
        assertThrows(CompileException.class, parser::parseStatement, "Expected '{'.");
    }

    /**
     * Tests statement of for statement.
     */
    @Test
    public void testStatementOfForStatement() {
        StatementsParser parser = getStatementsParser("""
                for (;;) {
                    doWork();
                }
                """);
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTForStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement of try statement.
     */
    @Test
    public void testStatementOfTryStatement() {
        StatementsParser parser = getStatementsParser("""
            try {
                br.readLine();
            } catch (IOException e) {
                out.println(e.getMessage());
            }
            """);
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTTryStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement of switch statement.
     */
    @Test
    public void testStatementOfSwitchStatement() {
        StatementsParser parser = getStatementsParser("""
                switch code {
                case 1 -> out.println("One");
                case 2 -> out.println("Two");
                default -> out.println("Unexpected");
                }
                """);
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTSwitchStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests switch statement.
     */
    @Test
    public void testSwitchStatement() {
        StatementsParser parser = getStatementsParser("""
                switch code {
                case 1 -> out.println("One");
                case 2 -> out.println("Two");
                default -> out.println("Unexpected");
                }
                """);
        ASTSwitchStatement node = parser.parseSwitchStatement();
        checkBinary(node, SWITCH, ASTExpression.class, ASTSwitchStatementBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests empty switch block.
     */
    @Test
    public void testSwitchBlockEmpty() {
        StatementsParser parser = getStatementsParser("{}");
        assertThrows(CompileException.class, parser::parseSwitchStatementBlock, "Error at code \"{}\".");
    }

    /**
     * Tests try statement of catch.
     */
    @Test
    public void testTryStatementOfCatch() {
        StatementsParser parser = getStatementsParser("""
            try {
                br.readLine();
            } catch (IOException e) {
                out.println(e.getMessage());
            }
            """);
        ASTTryStatement node = parser.parseTryStatement();
        checkBinary(node, TRY, ASTBlock.class, ASTCatches.class);
        node.collapseThenPrint();
    }

    /**
     * Tests try statement of finally.
     */
    @Test
    public void testTryStatementOfFinally() {
        StatementsParser parser = getStatementsParser("""
                try {
                    br.readLine();
                } finally {
                    br.close();
                }
                """);
        ASTTryStatement node = parser.parseTryStatement();
        checkBinary(node, TRY, ASTBlock.class, ASTFinally.class);
        node.collapseThenPrint();
    }

    /**
     * Tests try statement of resource specification.
     */
    @Test
    public void testTryStatementOfResourceSpecification() {
        StatementsParser parser = getStatementsParser("""
            try (BufferedReader br = new BufferedReader()) {
                br.readLine();
            }
            """);
        ASTTryStatement node = parser.parseTryStatement();
        checkBinary(node, TRY, ASTResourceSpecification.class, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests try statement of all optionals.
     */
    @Test
    public void testTryStatementOfAll() {
        StatementsParser parser = getStatementsParser("""
            try (BufferedReader br = new BufferedReader()) {
                br.readLine();
            } catch (IOException e) {
                out.println(e.getMessage());
            } finally {
                br.close();
            }
            """);
        ASTTryStatement node = parser.parseTryStatement();
        checkNary(node, TRY, ASTResourceSpecification.class, ASTBlock.class, ASTCatches.class, ASTFinally.class);
        node.collapseThenPrint();
    }

    /**
     * Tests resource specification of resource list.
     */
    @Test
    public void testResourceSpecification() {
        StatementsParser parser = getStatementsParser("(fr; BufferedReader br = new BufferedReader(fr))");
        ASTResourceSpecification node = parser.parseResourceSpecification();
        checkSimple(node, ASTResourceList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests resource specification of resource list and semicolon.
     */
    @Test
    public void testResourceSpecificationSemicolon() {
        StatementsParser parser = getStatementsParser("(fr; BufferedReader br = new BufferedReader(fr);)");
        ASTResourceSpecification node = parser.parseResourceSpecification();
        checkSimple(node, ASTResourceList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests resource list of resource.
     */
    @Test
    public void testResourceListOfResource() {
        StatementsParser parser = getStatementsParser("BufferedReader br = new BufferedReader()");
        ASTResourceList node = parser.parseResourceList();
        checkSimple(node, ASTResource.class, SEMICOLON);
        node.collapseThenPrint();
    }

    /**
     * Tests resource list of nested resource lists (here, just multiple resources).
     */
    @Test
    public void testResourceListNested() {
        StatementsParser parser = getStatementsParser("fr; BufferedReader br = new BufferedReader(fr)");
        ASTResourceList node = parser.parseResourceList();
        checkList(node, SEMICOLON, ASTResource.class, 2);
        node.collapseThenPrint();
    }

    /**
     * Test resource of resource declaration.
     */
    @Test
    public void testResourceOfResourceDeclaration() {
        StatementsParser parser = getStatementsParser("BufferedReader br = new BufferedReader()");
        ASTResource node = parser.parseResource();
        checkSimple(node, ASTResourceDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Test resource of expression name.
     */
    @Test
    public void testResourceOfExpressionName() {
        StatementsParser parser = getStatementsParser("br");
        ASTResource node = parser.parseResource();
        checkSimple(node, ASTExpressionName.class);
        node.collapseThenPrint();
    }

    /**
     * Test resource of field access.
     */
    @Test
    public void testResourceOfFieldAccess() {
        StatementsParser parser = getStatementsParser("super.br");
        ASTResource node = parser.parseResource();
        checkSimple(node, ASTFieldAccess.class);
        node.collapseThenPrint();
    }

    /**
     * Test resource declaration, no variable modifiers.
     */
    @Test
    public void testResourceDeclaration() {
        StatementsParser parser = getStatementsParser("BufferedReader br = new BufferedReader()");
        ASTResourceDeclaration node = parser.parseResourceDeclaration();
        checkTrinary(node, EQUAL, ASTLocalVariableType.class, ASTIdentifier.class, ASTExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Test resource declaration, with variable modifiers.
     */
    @Test
    public void testResourceDeclarationOfVariableModifier() {
        StatementsParser parser = getStatementsParser("var BufferedReader br = new BufferedReader()");
        ASTResourceDeclaration node = parser.parseResourceDeclaration();
        checkNary(node, EQUAL, ASTVariableModifierList.class, ASTLocalVariableType.class, ASTIdentifier.class, ASTExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Test catches of catch clauses.
     */
    @Test
    public void testCatches() {
        StatementsParser parser = getStatementsParser("""
            catch (FileNotFoundException e) {
                err.println(e.getMessage());
            }
            catch (IOException e) {
                out.println(e.getMessage());
            }
            """);
        ASTCatches node = parser.parseCatches();
        checkList(node, null, ASTCatchClause.class, 2);
        node.collapseThenPrint();
    }

    /**
     * Tests catch clause.
     */
    @Test
    public void testCatchClause() {
        StatementsParser parser = getStatementsParser("catch (CompileException ce) { out.println(ce.getMessage()); }");
        ASTCatchClause node = parser.parseCatchClause();
        checkBinary(node, CATCH, ASTCatchFormalParameter.class, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests catch type of data type.
     */
    @Test
    public void testCatchTypeOfDataType() {
        StatementsParser parser = getStatementsParser("Exception");
        ASTCatchType node = parser.parseCatchType();
        checkSimple(node, ASTDataType.class, PIPE);
        node.collapseThenPrint();
    }

    /**
     * Tests catch formal parameter without modifiers.
     */
    @Test
    public void testCatchFormalParameter() {
        StatementsParser parser = getStatementsParser("Exception e");
        ASTCatchFormalParameter node = parser.parseCatchFormalParameter();
        checkBinary(node, ASTCatchType.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests catch formal parameter with modifiers.
     */
    @Test
    public void testCatchFormalParameterOfModifiers() {
        StatementsParser parser = getStatementsParser("var CustomException ce");
        ASTCatchFormalParameter node = parser.parseCatchFormalParameter();
        checkTrinary(node, null, ASTVariableModifierList.class, ASTCatchType.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests catch type.
     */
    @Test
    public void testCatchType() {
        StatementsParser parser = getStatementsParser("IOException | SQLException");
        ASTCatchType node = parser.parseCatchType();
        checkList(node, PIPE, ASTDataType.class, 2);
        node.collapseThenPrint();
    }

    /**
     * Tests nested catch types, here, just a list of data types.
     */
    @Test
    public void testCatchTypeNested() {
        StatementsParser parser = getStatementsParser("ArrayIndexOutOfBoundsException | NullPointerException | IllegalArgumentException");
        ASTCatchType node = parser.parseCatchType();
        checkList(node, PIPE, ASTDataType.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests finally block.
     */
    @Test
    public void testFinally() {
        StatementsParser parser = getStatementsParser("""
                finally {
                    out.println("Always executed!");
                }
                """);
        ASTFinally node = parser.parseFinally();
        checkSimple(node, ASTBlock.class, FINALLY);
        node.collapseThenPrint();
    }

    /**
     * Tests simple if statement.
     */
    @Test
    public void testIfStatementOfSimple() {
        StatementsParser parser = getStatementsParser("if (success) { return true; }");
        ASTIfStatement node = parser.parseIfStatement();
        checkBinary(node, IF, ASTConditionalExpression.class, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests if statement with init no block.
     */
    @Test
    public void testIfStatementOfInitNoBlock() {
        StatementsParser parser = getStatementsParser("""
                if {String line = br.readLine()} (line != null) out.println(line);
                """);
        assertThrows(CompileException.class, parser::parseIfStatement, "Expected '{'.");
    }

    /**
     * Tests if statement with init.
     */
    @Test
    public void testIfStatementOfInit() {
        StatementsParser parser = getStatementsParser("""
                if {String line = br.readLine()} line != null {
                    out.println(line);
                }
                """);
        ASTIfStatement node = parser.parseIfStatement();
        checkTrinary(node, IF, ASTInit.class, ASTConditionalExpression.class, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests if statement with else.
     */
    @Test
    public void testIfStatementOfElse() {
        StatementsParser parser = getStatementsParser("""
            if result {
                out.println("Test passed.");
            } else {
                out.println("Test FAILED!");
            }
            """);
        ASTIfStatement node = parser.parseIfStatement();
        checkTrinary(node, IF, ASTConditionalExpression.class, ASTBlock.class, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested if statements (if/else if/else).
     */
    @Test
    public void testIfStatementNested() {
        StatementsParser parser = getStatementsParser("""
            if result {
                out.println("Test passed.");
            } else if DEBUG {
                out.println("Test failed in debug mode!");
            } else {
                out.println("Test FAILED!");
            }
            """);
        ASTIfStatement node = parser.parseIfStatement();
        checkTrinary(node, IF, ASTConditionalExpression.class, ASTBlock.class, ASTIfStatement.class);

        ASTIfStatement nestedIf = (ASTIfStatement) node.getChildren().get(2);
        checkTrinary(nestedIf, IF, ASTConditionalExpression.class, ASTBlock.class, ASTBlock.class);

        node.collapseThenPrint();
    }

    /**
     * Tests simple while statement.
     */
    @Test
    public void testWhileStatementOfSimple() {
        StatementsParser parser = getStatementsParser("while shouldContinue { doWork(); }");
        ASTWhileStatement node = parser.parseWhileStatement();
        checkBinary(node, WHILE, ASTConditionalExpression.class, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests while statement with init without block.
     */
    @Test
    public void testWhileStatementOfInitNoBlock() {
        StatementsParser parser = getStatementsParser("""
                while {String line = br.readLine()} line != null out.println(line);
                """);
        assertThrows(CompileException.class, parser::parseWhileStatement, "Expected '{'.");
    }

    /**
     * Tests while statement with init.
     */
    @Test
    public void testWhileStatementOfInit() {
        StatementsParser parser = getStatementsParser("""
                while {String line = br.readLine()} line != null {
                    out.println(line);
                }
                """);
        ASTWhileStatement node = parser.parseWhileStatement();
        checkTrinary(node, WHILE, ASTInit.class, ASTConditionalExpression.class, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests do statement.
     */
    @Test
    public void testDoStatement() {
        StatementsParser parser = getStatementsParser("do { work(); } while shouldContinue;");
        ASTDoStatement node = parser.parseDoStatement();
        checkBinary(node, DO, ASTBlock.class, ASTConditionalExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests for statement of basic for statement of all 3 parts.
     */
    @Test
    public void testForStatementOfBasicForStatementAll3() {
        StatementsParser parser = getStatementsParser("""
                for (Int i = 0; i < 10; i++) {
                    out.println(i);
                }
                """);
        ASTForStatement node = parser.parseForStatement();
        checkSimple(node, ASTBasicForStatement.class, FOR);
        ASTBasicForStatement basicForStmt = (ASTBasicForStatement) node.getChildren().get(0);
        checkNary(basicForStmt, SEMICOLON, ASTInit.class, ASTConditionalExpression.class, ASTStatementExpressionList.class, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests for statement of basic for statement of infinite loop.
     */
    @Test
    public void testForStatementOfBasicForStatementInfiniteLoop() {
        StatementsParser parser = getStatementsParser("""
        for (;;) {
            out.println("Hello world!");
        }
        """);
        ASTForStatement node = parser.parseForStatement();
        checkSimple(node, ASTBasicForStatement.class, FOR);
        ASTBasicForStatement basicForStmt = (ASTBasicForStatement) node.getChildren().get(0);
        checkSimple(basicForStmt, ASTBlock.class, SEMICOLON);
        node.collapseThenPrint();
    }

    /**
     * Tests for statement of enhanced for statement.
     */
    @Test
    public void testForStatementOfEnhancedForStatement() {
        StatementsParser parser = getStatementsParser("""
                for (Int i : array) {
                    sum += i;
                }
                """);
        ASTForStatement node = parser.parseForStatement();
        checkSimple(node, ASTEnhancedForStatement.class, FOR);
        ASTEnhancedForStatement enhForStmt = (ASTEnhancedForStatement) node.getChildren().get(0);
        checkTrinary(enhForStmt, COLON, ASTLocalVariableDeclaration.class, ASTConditionalExpression.class, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests yield statement.
     */
    @Test
    public void testYieldStatement() {
        StatementsParser parser = getStatementsParser("yield x.y + 2;");
        ASTYieldStatement node = parser.parseYieldStatement();
        checkSimple(node, ASTExpression.class, YIELD);
        node.collapseThenPrint();
    }

    /**
     * Tests use statement.
     */
    @Test
    public void testUseStatement() {
        StatementsParser parser = getStatementsParser("use x.y + 2;");
        ASTUseStatement node = parser.parseUseStatement();
        checkSimple(node, ASTExpression.class, USE);
        node.collapseThenPrint();
    }

    /**
     * Tests return statement.
     */
    @Test
    public void testReturnStatement() {
        StatementsParser parser = getStatementsParser("return;");
        ASTReturnStatement node = parser.parseReturnStatement();
        checkEmpty(node, RETURN);
        node.collapseThenPrint();
    }

    /**
     * Tests return statement with expression.
     */
    @Test
    public void testReturnStatementOfExpression() {
        StatementsParser parser = getStatementsParser("return x.y + 2;");
        ASTReturnStatement node = parser.parseReturnStatement();
        checkSimple(node, ASTExpression.class, RETURN);
        node.collapseThenPrint();
    }

    /**
     * Tests throw statement with expression.
     */
    @Test
    public void testThrowStatementOfExpression() {
        StatementsParser parser = getStatementsParser("throw new Exception();");
        ASTThrowStatement node = parser.parseThrowStatement();
        checkSimple(node, ASTExpression.class, THROW);
        node.collapseThenPrint();
    }

    /**
     * Tests break statement.
     */
    @Test
    public void testBreakStatement() {
        StatementsParser parser = getStatementsParser("break;");
        ASTBreakStatement node = parser.parseBreakStatement();
        checkEmpty(node, BREAK);
        node.collapseThenPrint();
    }

    /**
     * Tests continue statement.
     */
    @Test
    public void testContinueStatement() {
        StatementsParser parser = getStatementsParser("continue;");
        ASTContinueStatement node = parser.parseContinueStatement();
        checkEmpty(node, CONTINUE);
        node.collapseThenPrint();
    }

    /**
     * Tests fallthrough statement.
     */
    @Test
    public void testFallthroughStatement() {
        StatementsParser parser = getStatementsParser("fallthrough;");
        ASTFallthroughStatement node = parser.parseFallthroughStatement();
        checkEmpty(node, FALLTHROUGH);
        node.collapseThenPrint();
    }

    /**
     * Tests assert statement of expression.
     */
    @Test
    public void testAssertStatementOfExpression() {
        StatementsParser parser = getStatementsParser("assert result == true;");
        ASTAssertStatement node = parser.parseAssertStatement();
        checkSimple(node, ASTExpression.class, ASSERT);
        node.collapseThenPrint();
    }

    /**
     * Tests assert statement of 2 expressions.
     */
    @Test
    public void testAssertStatementOfTwoExpressions() {
        StatementsParser parser = getStatementsParser("assert result == true : \"Assertion failed!\";");
        ASTAssertStatement node = parser.parseAssertStatement();
        checkBinary(node, ASSERT, ASTExpression.class, ASTExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests expression statement of statement expression.
     */
    @Test
    public void testExpressionStatementOfStatementExpression() {
        StatementsParser parser = getStatementsParser("x++;");
        ASTExpressionStatement node = parser.parseExpressionStatement();
        checkSimple(node, ASTStatementExpression.class, SEMICOLON);
        node.collapseThenPrint();
    }

    /**
     * Tests init of local variable declaration.
     */
    @Test
    public void testInitOfLocalVariableDeclaration() {
        StatementsParser parser = getStatementsParser("Int i = 0, j = 0");
        ASTInit node = parser.parseInit();
        checkSimple(node, ASTLocalVariableDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests init of statement expression.
     */
    @Test
    public void testInitOfStatementExpression() {
        StatementsParser parser = getStatementsParser("i = 0");
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
    public void testInitOfStatementExpressionList() {
        StatementsParser parser = getStatementsParser("i = 0, j = 0, k = 1");
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
    public void testStatementExpressionListOfStatementExpression() {
        StatementsParser parser = getStatementsParser("i = 0");
        ASTStatementExpressionList node = parser.parseStatementExpressionList();
        checkSimple(node, ASTStatementExpression.class, COMMA);
        node.collapseThenPrint();
    }

    /**
     * Tests statement expression lists of nested statement expression lists
     * (here, just multiple statement expressions).
     */
    @Test
    public void testStatementExpressionListNested() {
        StatementsParser parser = getStatementsParser("i = 0, j = 0, k = 1");
        ASTStatementExpressionList node = parser.parseStatementExpressionList();
        checkList(node, COMMA, ASTStatementExpression.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests statement expression of assignment.
     */
    @Test
    public void testStatementExpressionOfAssignment() {
        StatementsParser parser = getStatementsParser("x = 0");
        ASTStatementExpression node = parser.parseStatementExpression();
        checkSimple(node, ASTAssignment.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement expression of postfix expression.
     */
    @Test
    public void testStatementExpressionOfPostfixExpression() {
        StatementsParser parser = getStatementsParser("x.y++");
        ASTStatementExpression node = parser.parseStatementExpression();
        checkSimple(node, ASTPostfix.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement expression of method invocation.
     */
    @Test
    public void testStatementExpressionOfMethodInvocation() {
        StatementsParser parser = getStatementsParser("x.y(2)");
        ASTStatementExpression node = parser.parseStatementExpression();
        checkSimple(node, ASTMethodInvocation.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement expression of class instance creation expression.
     */
    @Test
    public void testStatementExpressionOfClassInstanceCreationExpression() {
        StatementsParser parser = getStatementsParser("new SideEffect()");
        ASTStatementExpression node = parser.parseStatementExpression();
        checkSimple(node, ASTClassInstanceCreationExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests assignment expression of assignment made of lhs, "+=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfPlusEquals() {
        StatementsParser parser = getStatementsParser("a += 1");
        ASTLeftHandSide lhs = parser.getExpressionsParser().parseLeftHandSide();
        ASTAssignment node = parser.parseAssignment(lhs.getLocation(), lhs);
        checkBinary(node, PLUS_EQUALS, ASTLeftHandSide.class, ASTExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests assignment expression of assignment made of lhs, "-=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfMinusEquals() {
        StatementsParser parser = getStatementsParser("a -= 1");
        ASTLeftHandSide lhs = parser.getExpressionsParser().parseLeftHandSide();
        ASTAssignment node = parser.parseAssignment(lhs.getLocation(), lhs);
        checkBinary(node, MINUS_EQUALS, ASTLeftHandSide.class, ASTExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests assignment expression of assignment made of lhs, "*=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfStarEquals() {
        StatementsParser parser = getStatementsParser("a *= 1");
        ASTLeftHandSide lhs = parser.getExpressionsParser().parseLeftHandSide();
        ASTAssignment node = parser.parseAssignment(lhs.getLocation(), lhs);
        checkBinary(node, STAR_EQUALS, ASTLeftHandSide.class, ASTExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests assignment expression of assignment made of lhs, "/=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfSlashEquals() {
        StatementsParser parser = getStatementsParser("a /= 1");
        ASTLeftHandSide lhs = parser.getExpressionsParser().parseLeftHandSide();
        ASTAssignment node = parser.parseAssignment(lhs.getLocation(), lhs);
        checkBinary(node, SLASH_EQUALS, ASTLeftHandSide.class, ASTExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests assignment expression of assignment made of lhs, "%=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfPercentEquals() {
        StatementsParser parser = getStatementsParser("a %= 1");
        ASTLeftHandSide lhs = parser.getExpressionsParser().parseLeftHandSide();
        ASTAssignment node = parser.parseAssignment(lhs.getLocation(), lhs);
        checkBinary(node, PERCENT_EQUALS, ASTLeftHandSide.class, ASTExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests assignment expression of assignment made of lhs, "<<=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfShiftLeftEquals() {
        StatementsParser parser = getStatementsParser("a <<= 1");
        ASTLeftHandSide lhs = parser.getExpressionsParser().parseLeftHandSide();
        ASTAssignment node = parser.parseAssignment(lhs.getLocation(), lhs);
        checkBinary(node, SHIFT_LEFT_EQUALS, ASTLeftHandSide.class, ASTExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests assignment expression of assignment made of lhs, ">>=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfShiftRightEquals() {
        StatementsParser parser = getStatementsParser("a >>= 1");
        ASTLeftHandSide lhs = parser.getExpressionsParser().parseLeftHandSide();
        ASTAssignment node = parser.parseAssignment(lhs.getLocation(), lhs);
        checkBinary(node, SHIFT_RIGHT_EQUALS, ASTLeftHandSide.class, ASTExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests assignment expression of assignment made of lhs, "|=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfOrEquals() {
        StatementsParser parser = getStatementsParser("a |= 1");
        ASTLeftHandSide lhs = parser.getExpressionsParser().parseLeftHandSide();
        ASTAssignment node = parser.parseAssignment(lhs.getLocation(), lhs);
        checkBinary(node, PIPE_EQUALS, ASTLeftHandSide.class, ASTExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests assignment expression of assignment made of lhs, "&=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfAndEquals() {
        StatementsParser parser = getStatementsParser("a &= 1");
        ASTLeftHandSide lhs = parser.getExpressionsParser().parseLeftHandSide();
        ASTAssignment node = parser.parseAssignment(lhs.getLocation(), lhs);
        checkBinary(node, AMPERSAND_EQUALS, ASTLeftHandSide.class, ASTExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests assignment expression of assignment made of lhs, "^=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfXorEquals() {
        StatementsParser parser = getStatementsParser("a ^= 1");
        ASTLeftHandSide lhs = parser.getExpressionsParser().parseLeftHandSide();
        ASTAssignment node = parser.parseAssignment(lhs.getLocation(), lhs);
        checkBinary(node, CARET_EQUALS, ASTLeftHandSide.class, ASTExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Helper method to get a <code>StatementsParser</code> directly from code.
     * @param code The code to test.
     * @return A <code>StatementsParser</code> that will parse the given code.
     */
    private static StatementsParser getStatementsParser(String code) {
        return new Parser(new Scanner(code)).getStatementsParser();
    }
}
