package org.spruce.compiler.test;

import java.util.Arrays;
import java.util.Collections;

import org.spruce.compiler.ast.classes.ASTFormalParameterList;
import org.spruce.compiler.ast.expressions.*;
import org.spruce.compiler.ast.literals.*;
import org.spruce.compiler.ast.names.*;
import org.spruce.compiler.ast.statements.ASTBlock;
import org.spruce.compiler.ast.expressions.ASTSwitchLabel;
import org.spruce.compiler.ast.statements.ASTThrowStatement;
import org.spruce.compiler.ast.statements.ASTVariableModifierList;
import org.spruce.compiler.ast.types.*;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.parser.ExpressionsParser;
import org.spruce.compiler.parser.Parser;
import org.spruce.compiler.scanner.Scanner;
import static org.spruce.compiler.scanner.TokenType.*;
import static org.spruce.compiler.test.ParserTestUtility.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * All tests for the parser related to expressions.
 */
public class ParserExpressionsTest {

    /**
     * Tests nested lambda expressions.
     */
    @Test
    public void testNestedLambdaExpressions() {
        ExpressionsParser parser = new Parser(new Scanner("p -> q -> p.foo(q)")).getExpressionsParser();
        ASTLambdaExpression node = parser.parseLambdaExpression();
        checkBinary(node, ARROW, ASTLambdaParameters.class, ASTLambdaBody.class);
        ASTLambdaBody body = (ASTLambdaBody) node.getChildren().get(1);
        checkSimple(body, ASTExpression.class);
        ASTExpression expr = (ASTExpression) body.getChildren().get(0);
        checkSimple(expr, ASTLambdaExpression.class);
        ASTLambdaExpression nested = (ASTLambdaExpression) expr.getChildren().get(0);
        checkBinary(nested, ARROW, ASTLambdaParameters.class, ASTLambdaBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests expression of lambda expression.
     */
    @Test
    public void testExpressionOfLambdaExpression() {
        ExpressionsParser parser = new Parser(new Scanner("|x, y| -> { use x + y;}")).getExpressionsParser();
        ASTExpression node = parser.parseExpression();
        checkSimple(node, ASTLambdaExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests bad lambda expression.
     */
    @Test
    public void testBadLambdaExpression() {
        ExpressionsParser parser = new Parser(new Scanner("|x, y| oops()")).getExpressionsParser();
        assertThrows(CompileException.class, parser::parseLambdaExpression, "Expected \"->\".");
    }

    /**
     * Tests lambda expression.
     */
    @Test
    public void testLambdaExpression() {
        ExpressionsParser parser = new Parser(new Scanner("n -> n * 2")).getExpressionsParser();
        ASTLambdaExpression node = parser.parseLambdaExpression();
        checkBinary(node, ARROW, ASTLambdaParameters.class, ASTLambdaBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests bad lambda body.
     */
    @Test
    public void testBadLambdaBody() {
        ExpressionsParser parser = new Parser(new Scanner("class Bad")).getExpressionsParser();
        assertThrows(CompileException.class, parser::parseLambdaBody, "Expected expression or block.");
    }

    /**
     * Tests lambda body of block.
     */
    @Test
    public void testLambdaBodyOfBlock() {
        ExpressionsParser parser = new Parser(new Scanner("{ use n * 2; }")).getExpressionsParser();
        ASTLambdaBody node = parser.parseLambdaBody();
        checkSimple(node, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests lambda body of expression.
     */
    @Test
    public void testLambdaBodyOfExpression() {
        ExpressionsParser parser = new Parser(new Scanner("n * 2")).getExpressionsParser();
        ASTLambdaBody node = parser.parseLambdaBody();
        checkSimple(node, ASTExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests bad lambda parameters.
     */
    @Test
    public void testBadLambdaParameters() {
        ExpressionsParser parser = new Parser(new Scanner("2, 3")).getExpressionsParser();
        assertThrows(CompileException.class, parser::parseLambdaParameters, "Expected lambda parameters.");
    }

    /**
     * Tests lambda parameters of a bare identifier.
     */
    @Test
    public void testLambdaParametersOfIdentifier() {
        ExpressionsParser parser = new Parser(new Scanner("n")).getExpressionsParser();
        ASTLambdaParameters node = parser.parseLambdaParameters();
        checkSimple(node, ASTIdentifier.class);
        node.collapseThenPrint();
    }
    /**
     * Tests lambda parameters of lambda parameter list.
     */
    @Test
    public void testLambdaParametersOfLambdaParameterList() {
        ExpressionsParser parser = new Parser(new Scanner("|a, b|")).getExpressionsParser();
        ASTLambdaParameters node = parser.parseLambdaParameters();
        checkSimple(node, ASTLambdaParameterList.class, PIPE);
        ASTLambdaParameterList list = (ASTLambdaParameterList) node.getChildren().get(0);
        checkSimple(list, ASTInferredParameterList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests lambda parameters of just a two separated pipes.
     */
    @Test
    public void testLambdaParametersOfTwoPipes() {
        ExpressionsParser parser = new Parser(new Scanner("| |")).getExpressionsParser();
        ASTLambdaParameters node = parser.parseLambdaParameters();
        checkSimple(node, ASTLambdaParameterList.class, PIPE);
        ASTLambdaParameterList list = (ASTLambdaParameterList) node.getChildren().get(0);
        checkEmpty(list, null);
        node.collapseThenPrint();
    }

    /**
     * Tests lambda parameters of just a double-pipe.
     */
    @Test
    public void testLambdaParametersOfDoublePipe() {
        ExpressionsParser parser = new Parser(new Scanner("||")).getExpressionsParser();
        ASTLambdaParameters node = parser.parseLambdaParameters();
        checkSimple(node, ASTLambdaParameterList.class, PIPE);
        ASTLambdaParameterList list = (ASTLambdaParameterList) node.getChildren().get(0);
        checkEmpty(list, null);
        node.collapseThenPrint();
    }

    /**
     * Tests lambda parameter list of formal parameter list.
     */
    @Test
    public void testLambdaParameterListOfFormalParameterList() {
        ExpressionsParser parser = new Parser(new Scanner("Double x, Double y, Double z")).getExpressionsParser();
        ASTLambdaParameterList node = parser.parseLambdaParameterList();
        checkSimple(node, ASTFormalParameterList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests bad lambda parameter list.
     */
    @Test
    public void testBadLambdaParameterList() {
        ExpressionsParser parser = new Parser(new Scanner("give badParameter")).getExpressionsParser();
        assertThrows(CompileException.class, parser::parseLambdaParameterList, "Expected lambda parameter(s).");
    }

    /**
     * Tests lambda parameter list of inferred parameter list.
     */
    @Test
    public void testLambdaParameterListOfInferredParameterList() {
        ExpressionsParser parser = new Parser(new Scanner("alpha, beta, gamma")).getExpressionsParser();
        ASTLambdaParameterList node = parser.parseLambdaParameterList();
        checkSimple(node, ASTInferredParameterList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests bad inferred parameter list.
     */
    @Test
    public void testBadInferredParameterList() {
        ExpressionsParser parser = new Parser(new Scanner("2")).getExpressionsParser();
        assertThrows(CompileException.class, parser::parseInferredParameterList, "Expected a lambda parameter.");
    }

    /**
     * Tests inferred parameter list.
     */
    @Test
    public void testInferredParameterList() {
        ExpressionsParser parser = new Parser(new Scanner("alpha, beta, gamma")).getExpressionsParser();
        ASTInferredParameterList node = parser.parseInferredParameterList();
        checkList(node, COMMA, ASTIdentifier.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests expression of other expression.
     */
    @Test
    public void testExpressionOfOtherExpression() {
        ExpressionsParser parser = new Parser(new Scanner("count == 1")).getExpressionsParser();
        ASTExpression node = parser.parseExpression();
        checkSimple(node, ASTConditionalExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests expression of conditional expression.
     */
    @Test
    public void testExpressionOfConditionalExpression() {
        ExpressionsParser parser = new Parser(new Scanner("a ? b : c")).getExpressionsParser();
        ASTExpression node = parser.parseExpression();
        checkSimple(node, ASTConditionalExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests the Conditional#ToLeftHandSide method with primary expressions
     * that aren't left hand sides: literal, <code>self</code>.
     */
    @Test
    public void testConditionalToLeftHandSideOfLiteral() {
        for (String code : Arrays.asList("1", "self")) {
            ExpressionsParser parser = new Parser(new Scanner(code)).getExpressionsParser();
            ASTConditionalExpression condExpr = parser.parseConditionalExpression();
            assertThrows(CompileException.class, condExpr::getLeftHandSide, "Error at code \"" + code + "\".");
        }
    }

    /**
     * Tests the Conditional#ToLeftHandSide method with primary expressions
     * that are left hand sides: expression name.
     */
    @Test
    public void testConditionalToLeftHandSideOfExpressionName() {
        ExpressionsParser parser = new Parser(new Scanner("expr.name")).getExpressionsParser();
        ASTConditionalExpression condExpr = parser.parseConditionalExpression();
        ASTLeftHandSide lhs = condExpr.getLeftHandSide();
        checkSimple(lhs, ASTExpressionName.class);
        lhs.collapseThenPrint();
    }

    /**
     * Tests the Conditional#ToLeftHandSide method with primary expressions
     * that are left hand sides: element access.
     */
    @Test
    public void testConditionalToLeftHandSideOfElementAccess() {
        ExpressionsParser parser = new Parser(new Scanner("getArray()[1]")).getExpressionsParser();
        ASTConditionalExpression condExpr = parser.parseConditionalExpression();
        ASTLeftHandSide lhs = condExpr.getLeftHandSide();
        checkSimple(lhs, ASTElementAccess.class);
        lhs.collapseThenPrint();
    }

    /**
     * Tests left hand side of expression name.
     */
    @Test
    public void testLeftHandSideOfExpressionName() {
        ExpressionsParser parser = new Parser(new Scanner("expr.name")).getExpressionsParser();
        ASTLeftHandSide node = parser.parseLeftHandSide();
        checkSimple(node, ASTExpressionName.class);
        node.collapseThenPrint();
    }

    /**
     * Tests left hand side of element access.
     */
    @Test
    public void testLeftHandSideOfElementAccess() {
        ExpressionsParser parser = new Parser(new Scanner("array[i]")).getExpressionsParser();
        ASTLeftHandSide node = parser.parseLeftHandSide();
        checkSimple(node, ASTElementAccess.class);
        node.collapseThenPrint();
    }

    /**
     * Tests left hand side of field access.
     */
    @Test
    public void testLeftHandSideOfFieldAccess() {
        ExpressionsParser parser = new Parser(new Scanner("self.x")).getExpressionsParser();
        ASTLeftHandSide node = parser.parseLeftHandSide();
        checkSimple(node, ASTFieldAccess.class);
        node.collapseThenPrint();
    }

    /**
     * Tests conditional expression of logical or expression.
     */
    @Test
    public void testConditionalExpressionOfLogicalOrExpression() {
        ExpressionsParser parser = new Parser(new Scanner("a || b")).getExpressionsParser();
        ASTConditionalExpression node = parser.parseConditionalExpression();
        checkSimple(node, ASTLogicalOrExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests conditional expression of "?" and ":" and logical or expression.
     */
    @Test
    public void testConditionalExpression() {
        ExpressionsParser parser = new Parser(new Scanner("condition ? valueIfTrue : valueIfFalse")).getExpressionsParser();
        ASTConditionalExpression node = parser.parseConditionalExpression();
        checkTrinary(node, QUESTION_MARK, ASTLogicalOrExpression.class, ASTLogicalOrExpression.class, ASTConditionalExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested conditional expressions.
     */
    @Test
    public void testConditionalExpressionNested() {
        ExpressionsParser parser = new Parser(new Scanner("a || b ? \"one\" : c || d ? \"two\" : e || f ? \"three\" : \"four\"")).getExpressionsParser();
        ASTConditionalExpression node = parser.parseConditionalExpression();
        checkTrinary(node, QUESTION_MARK, ASTLogicalOrExpression.class, ASTLogicalOrExpression.class, ASTConditionalExpression.class);
        ASTConditionalExpression childNode = (ASTConditionalExpression) node.getChildren().get(2);
        checkTrinary(childNode, QUESTION_MARK, ASTLogicalOrExpression.class, ASTLogicalOrExpression.class, ASTConditionalExpression.class);
        childNode = (ASTConditionalExpression) childNode.getChildren().get(2);
        checkTrinary(childNode, QUESTION_MARK, ASTLogicalOrExpression.class, ASTLogicalOrExpression.class, ASTConditionalExpression.class);
        childNode = (ASTConditionalExpression) childNode.getChildren().get(2);
        checkSimple(childNode, ASTLogicalOrExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests logical or expression of logical xor expression.
     */
    @Test
    public void testLogicalOrExpressionOfLogicalAndExpression() {
        ExpressionsParser parser = new Parser(new Scanner("a ^: b")).getExpressionsParser();
        ASTLogicalOrExpression node = parser.parseLogicalOrExpression();
        checkSimple(node, ASTLogicalXorExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests logical or expression of "|:" and logical xor expression.
     */
    @Test
    public void testLogicalOrExpressionOfEager() {
        ExpressionsParser parser = new Parser(new Scanner("test |: elseThis")).getExpressionsParser();
        ASTLogicalOrExpression node = parser.parseLogicalOrExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(PIPE_COLON), ASTLogicalOrExpression.class, ASTLogicalXorExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests logical or expression of "||" and logical xor expression.
     */
    @Test
    public void testLogicalOrExpressionOfConditional() {
        ExpressionsParser parser = new Parser(new Scanner("alreadyDone || test")).getExpressionsParser();
        ASTLogicalOrExpression node = parser.parseLogicalOrExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(DOUBLE_PIPE), ASTLogicalOrExpression.class, ASTLogicalXorExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested logical or expressions.
     */
    @Test
    public void testLogicalOrExpressionNested() {
        ExpressionsParser parser = new Parser(new Scanner("a && b |: c ^: d || e &: f")).getExpressionsParser();
        ASTLogicalOrExpression node = parser.parseLogicalOrExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(DOUBLE_PIPE, PIPE_COLON), ASTLogicalOrExpression.class, ASTLogicalXorExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests logical xor expression of logical and expression.
     */
    @Test
    public void testLogicalXorExpressionOfLogicalAndExpression() {
        ExpressionsParser parser = new Parser(new Scanner("a && b")).getExpressionsParser();
        ASTLogicalXorExpression node = parser.parseLogicalXorExpression();
        checkSimple(node, ASTLogicalAndExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests logical xor expression of "^:" and logical and expression.
     */
    @Test
    public void testLogicalXorExpression() {
        ExpressionsParser parser = new Parser(new Scanner("test ^: thisAlso")).getExpressionsParser();
        ASTLogicalXorExpression node = parser.parseLogicalXorExpression();
        checkBinaryLeftAssociative(node,Collections.singletonList(CARET_COLON), ASTLogicalXorExpression.class, ASTLogicalAndExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested logical xor expressions.
     */
    @Test
    public void testLogicalXorExpressionNested() {
        ExpressionsParser parser = new Parser(new Scanner("a && b ^: c &: d ^: e && f")).getExpressionsParser();
        ASTLogicalXorExpression node = parser.parseLogicalXorExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(CARET_COLON, CARET_COLON), ASTLogicalXorExpression.class, ASTLogicalAndExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests logical and expression of relational expression.
     */
    @Test
    public void testLogicalAndExpressionOfRelationalExpression() {
        ExpressionsParser parser = new Parser(new Scanner("a = b")).getExpressionsParser();
        ASTLogicalAndExpression node = parser.parseLogicalAndExpression();
        checkSimple(node, ASTRelationalExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests logical and expression of "&&" and relational expression.
     */
    @Test
    public void testLogicalAndExpressionOfConditional() {
        ExpressionsParser parser = new Parser(new Scanner("test && notDone")).getExpressionsParser();
        ASTLogicalAndExpression node = parser.parseLogicalAndExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(DOUBLE_AMPERSAND), ASTLogicalAndExpression.class, ASTRelationalExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests logical and expression of "&:" and relational expression.
     */
    @Test
    public void testLogicalAndExpressionOfEager() {
        ExpressionsParser parser = new Parser(new Scanner("test &: thisAlso")).getExpressionsParser();
        ASTLogicalAndExpression node = parser.parseLogicalAndExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(AMPERSAND_COLON), ASTLogicalAndExpression.class, ASTRelationalExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested logical and expressions.
     */
    @Test
    public void testLogicalAndExpressionNested() {
        ExpressionsParser parser = new Parser(new Scanner("a < b &: c <= d && e > f")).getExpressionsParser();
        ASTLogicalAndExpression node = parser.parseLogicalAndExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(DOUBLE_AMPERSAND, AMPERSAND_COLON), ASTLogicalAndExpression.class, ASTRelationalExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests relational expression of compare expression.
     */
    @Test
    public void testRelationalExpressionOfCompareExpression() {
        ExpressionsParser parser = new Parser(new Scanner("a <=> b")).getExpressionsParser();
        ASTRelationalExpression node = parser.parseRelationalExpression();
        checkSimple(node, ASTCompareExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests relational expression of "&lt;" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfLessThan() {
        ExpressionsParser parser = new Parser(new Scanner("a.value < b.value")).getExpressionsParser();
        ASTRelationalExpression node = parser.parseRelationalExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(LESS_THAN), ASTRelationalExpression.class, ASTCompareExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests relational expression of "&lt;=" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfLessThanOrEqual() {
        ExpressionsParser parser = new Parser(new Scanner("2 <= 2")).getExpressionsParser();
        ASTRelationalExpression node = parser.parseRelationalExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(LESS_THAN_OR_EQUAL), ASTRelationalExpression.class, ASTCompareExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests relational expression of "&gt;" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfGreaterThan() {
        ExpressionsParser parser = new Parser(new Scanner("a.value > b.value")).getExpressionsParser();
        ASTRelationalExpression node = parser.parseRelationalExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(GREATER_THAN), ASTRelationalExpression.class, ASTCompareExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests relational expression of "&gt;=" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfGreaterThanOrEqual() {
        ExpressionsParser parser = new Parser(new Scanner("2 >= 2")).getExpressionsParser();
        ASTRelationalExpression node = parser.parseRelationalExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(GREATER_THAN_OR_EQUAL), ASTRelationalExpression.class, ASTCompareExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests relational expression of "=" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfEqual() {
        ExpressionsParser parser = new Parser(new Scanner("test == SUCCESS")).getExpressionsParser();
        ASTRelationalExpression node = parser.parseRelationalExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(DOUBLE_EQUAL), ASTRelationalExpression.class, ASTCompareExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests relational expression of "!=" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfNotEqual() {
        ExpressionsParser parser = new Parser(new Scanner("test != FAILURE")).getExpressionsParser();
        ASTRelationalExpression node = parser.parseRelationalExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(NOT_EQUAL), ASTRelationalExpression.class, ASTCompareExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests relational expression of "isa" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfIsa() {
        ExpressionsParser parser = new Parser(new Scanner("node isa ASTRelationalExpression")).getExpressionsParser();
        ASTRelationalExpression node = parser.parseRelationalExpression();
        checkBinary(node, ISA, ASTRelationalExpression.class, ASTDataType.class);
        ASTRelationalExpression child = (ASTRelationalExpression) node.getChildren().get(0);
        checkSimple(child, ASTCompareExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests relational expression of "is" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfIs() {
        ExpressionsParser parser = new Parser(new Scanner("obj is other")).getExpressionsParser();
        ASTRelationalExpression node = parser.parseRelationalExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(IS), ASTRelationalExpression.class, ASTCompareExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests relational expression of "isnt" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfIsnt() {
        ExpressionsParser parser = new Parser(new Scanner("obj isnt somethingElse")).getExpressionsParser();
        ASTRelationalExpression node = parser.parseRelationalExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(ISNT), ASTRelationalExpression.class, ASTCompareExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested relational expressions.
     */
    @Test
    public void testRelationalExpressionNested() {
        ExpressionsParser parser = new Parser(new Scanner("a < b <=> c <= d")).getExpressionsParser();
        ASTRelationalExpression node = parser.parseRelationalExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(LESS_THAN_OR_EQUAL, LESS_THAN), ASTRelationalExpression.class, ASTCompareExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests compare expression of bitwise or expression.
     */
    @Test
    public void testCompareExpressionOfBitwiseOrExpression() {
        ExpressionsParser parser = new Parser(new Scanner("a | b")).getExpressionsParser();
        ASTCompareExpression node = parser.parseCompareExpression();
        checkSimple(node, ASTBitwiseOrExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests compare expression of "&lt;=&gt;" and bitwise or expression.
     */
    @Test
    public void testCompareExpression() {
        ExpressionsParser parser = new Parser(new Scanner("a.value <=> b.value")).getExpressionsParser();
        ASTCompareExpression node = parser.parseCompareExpression();
        checkBinary(node, COMPARISON, ASTBitwiseOrExpression.class, ASTBitwiseOrExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests bitwise or expression of bitwise xor expression.
     */
    @Test
    public void testBitwiseOrExpressionOfBitwiseXorExpression() {
        ExpressionsParser parser = new Parser(new Scanner("a ^ b")).getExpressionsParser();
        ASTBitwiseOrExpression node = parser.parseBitwiseOrExpression();
        checkSimple(node, ASTBitwiseXorExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests bitwise or expression of "|" and bitwise xor expression.
     */
    @Test
    public void testBitwiseOrExpression() {
        ExpressionsParser parser = new Parser(new Scanner("color | blueMask")).getExpressionsParser();
        ASTBitwiseOrExpression node = parser.parseBitwiseOrExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(PIPE), ASTBitwiseOrExpression.class, ASTBitwiseXorExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested bitwise or expressions.
     */
    @Test
    public void testBitwiseOrExpressionNested() {
        ExpressionsParser parser = new Parser(new Scanner("red | blue | yellow ^ green")).getExpressionsParser();
        ASTBitwiseOrExpression node = parser.parseBitwiseOrExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(PIPE, PIPE), ASTBitwiseOrExpression.class, ASTBitwiseXorExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests bitwise xor expression of bitwise and expression.
     */
    @Test
    public void testBitwiseXorExpressionOfBitwiseAndExpression() {
        ExpressionsParser parser = new Parser(new Scanner("a & b")).getExpressionsParser();
        ASTBitwiseXorExpression node = parser.parseBitwiseXorExpression();
        checkSimple(node, ASTBitwiseAndExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests bitwise xor expression of "^" and bitwise and expression.
     */
    @Test
    public void testBitwiseXorExpression() {
        ExpressionsParser parser = new Parser(new Scanner("color ^ blueMask")).getExpressionsParser();
        ASTBitwiseXorExpression node = parser.parseBitwiseXorExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(CARET), ASTBitwiseXorExpression.class, ASTBitwiseAndExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested bitwise xor expressions.
     */
    @Test
    public void testBitwiseXorExpressionNested() {
        ExpressionsParser parser = new Parser(new Scanner("red ^ blue & yellow ^ green")).getExpressionsParser();
        ASTBitwiseXorExpression node = parser.parseBitwiseXorExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(CARET, CARET), ASTBitwiseXorExpression.class, ASTBitwiseAndExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests bitwise and expression of shift expression.
     */
    @Test
    public void testBitwiseAndExpressionOfShiftExpression() {
        ExpressionsParser parser = new Parser(new Scanner("a << b")).getExpressionsParser();
        ASTBitwiseAndExpression node = parser.parseBitwiseAndExpression();
        checkSimple(node, ASTShiftExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests bitwise and expression of "&" and shift expression.
     */
    @Test
    public void testBitwiseAndExpression() {
        ExpressionsParser parser = new Parser(new Scanner("color & blueMask")).getExpressionsParser();
        ASTBitwiseAndExpression node = parser.parseBitwiseAndExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(AMPERSAND), ASTBitwiseAndExpression.class, ASTShiftExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested bitwise and expressions.
     */
    @Test
    public void testBitwiseAndExpressionNested() {
        ExpressionsParser parser = new Parser(new Scanner("red + blue & blueGreenMask & greenRedMask")).getExpressionsParser();
        ASTBitwiseAndExpression node = parser.parseBitwiseAndExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(AMPERSAND, AMPERSAND), ASTBitwiseAndExpression.class, ASTShiftExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests shift expression of additive expression.
     */
    @Test
    public void testShiftExpressionOfAdditiveExpression() {
        ExpressionsParser parser = new Parser(new Scanner("a + b")).getExpressionsParser();
        ASTShiftExpression node = parser.parseShiftExpression();
        checkSimple(node, ASTAdditiveExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests shift expression of "<<" and additive expression.
     */
    @Test
    public void testShiftExpressionOfLeftShift() {
        ExpressionsParser parser = new Parser(new Scanner("1 << 2")).getExpressionsParser();
        ASTShiftExpression node = parser.parseShiftExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(SHIFT_LEFT), ASTShiftExpression.class, ASTAdditiveExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests shift expression of ">>" and additive expression.
     */
    @Test
    public void testShiftExpressionOfRightShift() {
        ExpressionsParser parser = new Parser(new Scanner("2048 >> 2")).getExpressionsParser();
        ASTShiftExpression node = parser.parseShiftExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(SHIFT_RIGHT), ASTShiftExpression.class, ASTAdditiveExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested shift expressions.
     */
    @Test
    public void testShiftExpressionNested() {
        ExpressionsParser parser = new Parser(new Scanner("-2 << 3 + 4 >> 5 >>> 1")).getExpressionsParser();
        ASTShiftExpression node = parser.parseShiftExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(SHIFT_RIGHT, SHIFT_LEFT), ASTShiftExpression.class, ASTAdditiveExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests additive expression of multiplicative expression.
     */
    @Test
    public void testAdditiveExpressionOfMultiplicativeExpression() {
        ExpressionsParser parser = new Parser(new Scanner("a * b")).getExpressionsParser();
        ASTAdditiveExpression node = parser.parseAdditiveExpression();
        checkSimple(node, ASTMultiplicativeExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests additive expression of "+" and multiplicative expression.
     */
    @Test
    public void testAdditiveExpressionOfPlus() {
        ExpressionsParser parser = new Parser(new Scanner("-1 + 2")).getExpressionsParser();
        ASTAdditiveExpression node = parser.parseAdditiveExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(PLUS), ASTAdditiveExpression.class, ASTMultiplicativeExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests additive expression of "-" and multiplicative expression.
     */
    @Test
    public void testAdditiveExpressionOfMinus() {
        ExpressionsParser parser = new Parser(new Scanner("finish - start")).getExpressionsParser();
        ASTAdditiveExpression node = parser.parseAdditiveExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(MINUS), ASTAdditiveExpression.class, ASTMultiplicativeExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested additive expressions.
     */
    @Test
    public void testAdditiveExpressionNested() {
        ExpressionsParser parser = new Parser(new Scanner("-2 + 3 * 4 - 5")).getExpressionsParser();
        ASTAdditiveExpression node = parser.parseAdditiveExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(MINUS, PLUS), ASTAdditiveExpression.class, ASTMultiplicativeExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests multiplicative expression of unary expression.
     */
    @Test
    public void testMultiplicativeExpressionOfUnaryExpression() {
        ExpressionsParser parser = new Parser(new Scanner("varName")).getExpressionsParser();
        ASTMultiplicativeExpression node = parser.parseMultiplicativeExpression();
        checkSimple(node, ASTCastExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests multiplicative expression of "*" and unary expression.
     */
    @Test
    public void testMultiplicativeExpressionOfTimes() {
        ExpressionsParser parser = new Parser(new Scanner("a * b")).getExpressionsParser();
        ASTMultiplicativeExpression node = parser.parseMultiplicativeExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(STAR), ASTMultiplicativeExpression.class, ASTCastExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests multiplicative expression of "/" and unary expression.
     */
    @Test
    public void testMultiplicativeExpressionOfDivide() {
        ExpressionsParser parser = new Parser(new Scanner("i / -1")).getExpressionsParser();
        ASTMultiplicativeExpression node = parser.parseMultiplicativeExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(SLASH), ASTMultiplicativeExpression.class, ASTCastExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests multiplicative expression of "%" and unary expression.
     */
    @Test
    public void testMultiplicativeExpressionOfModulus() {
        ExpressionsParser parser = new Parser(new Scanner("index % len")).getExpressionsParser();
        ASTMultiplicativeExpression node = parser.parseMultiplicativeExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(PERCENT), ASTMultiplicativeExpression.class, ASTCastExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested multiplicative expressions.
     */
    @Test
    public void testMultiplicativeExpressionNested() {
        ExpressionsParser parser = new Parser(new Scanner("5 * 6 / 3 % 7")).getExpressionsParser();
        ASTMultiplicativeExpression node = parser.parseMultiplicativeExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(PERCENT, SLASH, STAR), ASTMultiplicativeExpression.class, ASTCastExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests parenthesized multiplicative expressions.
     */
    @Test
    public void testMultiplicativeExpressionOfParenthesizedExpressions() {
        ExpressionsParser parser = new Parser(new Scanner("(x + 1)*(x - 1)")).getExpressionsParser();
        ASTMultiplicativeExpression node = parser.parseMultiplicativeExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(STAR), ASTMultiplicativeExpression.class, ASTCastExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests cast expression of unary expression.
     */
    @Test
    public void testCastExpressionOfUnaryExpression() {
        ExpressionsParser parser = new Parser(new Scanner("varName")).getExpressionsParser();
        ASTCastExpression node = parser.parseCastExpression();
        checkSimple(node, ASTUnaryExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests cast expression of unary expression, "as", and an intersection
     * type consisting solely of a data type name.
     */
    @Test
    public void testCastExpressionOfIntersectionType() {
        ExpressionsParser parser = new Parser(new Scanner("d as Double")).getExpressionsParser();
        ASTCastExpression node = parser.parseCastExpression();
        checkBinary(node, AS, ASTUnaryExpression.class, ASTIntersectionType.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested cast expressions.
     */
    @Test
    public void testCastExpressionNested() {
        ExpressionsParser parser = new Parser(new Scanner("\"2\" as Object as String & Serializable")).getExpressionsParser();
        ASTCastExpression node = parser.parseCastExpression();
        checkBinary(node, AS, ASTCastExpression.class, ASTIntersectionType.class);
        ASTCastExpression childNode = (ASTCastExpression) node.getChildren().get(0);
        checkBinary(childNode, AS, ASTUnaryExpression.class, ASTIntersectionType.class);
        node.collapseThenPrint();
    }

    /**
     * Tests unary expression of primary.
     */
    @Test
    public void testUnaryExpressionOfPrimary() {
        ExpressionsParser parser = new Parser(new Scanner("varName")).getExpressionsParser();
        ASTUnaryExpression node = parser.parseUnaryExpression();
        checkSimple(node, ASTPrimary.class);
        node.collapseThenPrint();
    }

    /**
     * Tests unary expression of "-" and unary expression.
     */
    @Test
    public void testUnaryExpressionOfMinusUnary() {
        ExpressionsParser parser = new Parser(new Scanner("-1")).getExpressionsParser();
        ASTUnaryExpression node = parser.parseUnaryExpression();
        checkUnary(node, MINUS, ASTUnaryExpression.class, ASTPrimary.class);
        node.collapseThenPrint();
    }

    /**
     * Tests unary expression of "~" and unary expression.
     */
    @Test
    public void testUnaryExpressionOfComplementUnary() {
        ExpressionsParser parser = new Parser(new Scanner("~bits")).getExpressionsParser();
        ASTUnaryExpression node = parser.parseUnaryExpression();
        checkUnary(node, TILDE, ASTUnaryExpression.class, ASTPrimary.class);
        node.collapseThenPrint();
    }

    /**
     * Tests unary expression of "!" and unary expression.
     */
    @Test
    public void testUnaryExpressionOfLogicalComplementUnary() {
        ExpressionsParser parser = new Parser(new Scanner("!false")).getExpressionsParser();
        ASTUnaryExpression node = parser.parseUnaryExpression();
        checkUnary(node, EXCLAMATION, ASTUnaryExpression.class, ASTPrimary.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested unary expressions.
     */
    @Test
    public void testUnaryExpressionNested() {
        ExpressionsParser parser = new Parser(new Scanner("~ - ~ - bits")).getExpressionsParser();
        ASTUnaryExpression node = parser.parseUnaryExpression();
        checkSimple(node, ASTUnaryExpression.class, TILDE);

        ASTUnaryExpression childNode = (ASTUnaryExpression) node.getChildren().get(0);
        checkSimple(node, ASTUnaryExpression.class, TILDE);

        childNode = (ASTUnaryExpression) childNode.getChildren().get(0);
        checkSimple(node, ASTUnaryExpression.class, TILDE);

        childNode = (ASTUnaryExpression) childNode.getChildren().get(0);
        checkUnary(childNode, MINUS, ASTUnaryExpression.class, ASTPrimary.class);
        node.collapseThenPrint();
    }

    /**
     * Test Unary Expression of Switch Expression.
     */
    @Test
    public void testUnaryExpressionOfSwitchExpression() {
        ExpressionsParser parser = new Parser(new Scanner("""
            switch testValue {
            case 1 -> throw new TestException();
            case 2 -> { use a + 2; }
            case 3 -> b + 3;
            }""")).getExpressionsParser();
        ASTUnaryExpression node = parser.parseUnaryExpression();
        checkSimple(node, ASTSwitchExpression.class, SWITCH);
        node.collapseThenPrint();
    }

    /**
     * Tests Switch Expression.
     */
    @Test
    public void testSwitchExpression() {
        ExpressionsParser parser = new Parser(new Scanner("""
            switch testValue {
            case 1 -> throw new TestException();
            case 2 -> { use a + 2; }
            case 3 -> b + 3;
            }""")).getExpressionsParser();
        ASTSwitchExpression node = parser.parseSwitchExpression();
        checkBinary(node, SWITCH, ASTConditionalExpression.class, ASTSwitchExpressionBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests bad expression for a switch expression.
     */
    @Test
    public void testSwitchExpressionBadExpression() {
        ExpressionsParser parser = new Parser(new Scanner("""
            switch class {
            case 1 -> throw new TestException();
            case 2 -> { use a + 2; }
            case 3 -> b + 3;
            }""")).getExpressionsParser();
        assertThrows(CompileException.class, parser::parseSwitchExpression, "Expected expression.");
    }

    /**
     * Parses a Switch Expression Block.
     */
    @Test
    public void testSwitchExpressionBlock() {
        ExpressionsParser parser = new Parser(new Scanner("""
            {
            case 1 -> throw new TestException();
            case 2 -> { use a + 2; }
            case 3 -> b + 3;
            }""")).getExpressionsParser();
        ASTSwitchExpressionBlock node = parser.parseSwitchExpressionBlock();
        checkSimple(node, ASTSwitchExpressionRules.class);
        node.collapseThenPrint();
    }

    /**
     * Tests no opening brace in switch expression block.
     */
    @Test
    public void testSwitchExpressionBlockNoOpenBrace() {
        ExpressionsParser parser = new Parser(new Scanner("""
            case 1 -> throw new TestException();
            case 2 -> { use a + 2; }
            case 3 -> b + 3;
            """)).getExpressionsParser();
        assertThrows(CompileException.class, parser::parseSwitchExpressionBlock, "Error: Expected '{'.");
    }

    /**
     * Tests no closing brace in switch expression block.
     */
    @Test
    public void testSwitchExpressionBlockNoCloseBrace() {
        ExpressionsParser parser = new Parser(new Scanner("""
            {
            case 1 -> throw new TestException();
            case 2 -> { use a + 2; }
            case 3 -> b + 3;
            """)).getExpressionsParser();
        assertThrows(CompileException.class, parser::parseSwitchExpressionBlock, "Error: Expected '{'.");
    }

    /**
     * Parses Switch Expression Rules.
     */
    @Test
    public void testSwitchExpressionRules() {
        ExpressionsParser parser = new Parser(new Scanner("""
            case 1 -> throw new TestException();
            case 2 -> { use a + 2; }
            case 3 -> b + 3;
            """)).getExpressionsParser();
        ASTSwitchExpressionRules node = parser.parseSwitchExpressionRules();
        checkList(node, null, ASTSwitchExpressionRule.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests Switch Expression Rule of Throw Statement.
     */
    @Test
    public void testSwitchExpressionRuleOfThrowStatement() {
        ExpressionsParser parser = new Parser(new Scanner("case 1 -> throw new TestException();")).getExpressionsParser();
        ASTSwitchExpressionRule node = parser.parseSwitchExpressionRule();
        checkBinary(node, ARROW, ASTSwitchLabel.class, ASTThrowStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests Switch Expression Rule of Block.
     */
    @Test
    public void testSwitchExpressionRuleOfBlock() {
        ExpressionsParser parser = new Parser(new Scanner("case 1 -> { use a + 1; }")).getExpressionsParser();
        ASTSwitchExpressionRule node = parser.parseSwitchExpressionRule();
        checkBinary(node, ARROW, ASTSwitchLabel.class, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests Switch Expression Rule of Expression.
     */
    @Test
    public void testSwitchExpressionRuleOfExpression() {
        ExpressionsParser parser = new Parser(new Scanner("case 1 -> a + 1;")).getExpressionsParser();
        ASTSwitchExpressionRule node = parser.parseSwitchExpressionRule();
        checkBinary(node, ARROW, ASTSwitchLabel.class, ASTExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests bad switch expression rule, no arrow.
     */
    @Test
    public void testSwitchExpressionRuleNoArrow() {
        ExpressionsParser parser = new Parser(new Scanner("case 1 a + 1;")).getExpressionsParser();
        assertThrows(CompileException.class, parser::parseSwitchExpressionRule, "Expected '->'.");
    }

    /**
     * Tests Switch Label of Pattern and Guard.
     */
    @Test
    public void testSwitchLabelOfPatternAndGuard() {
        ExpressionsParser parser = new Parser(new Scanner("Employee(String name, Double salary) when salary >= 100000")).getExpressionsParser();
        ASTSwitchLabel node = parser.parseSwitchLabel();
        checkBinary(node, ASTPattern.class, ASTGuard.class);
        node.collapseThenPrint();
    }

    /**
     * Tests Switch Label of Pattern.
     */
    @Test
    public void testSwitchLabelOfPattern() {
        ExpressionsParser parser = new Parser(new Scanner("Month(String name)")).getExpressionsParser();
        ASTSwitchLabel node = parser.parseSwitchLabel();
        checkSimple(node, ASTPattern.class);
        node.collapseThenPrint();
    }

    /**
     * Tests Switch Label of Case Constants.
     */
    @Test
    public void testSwitchLabelOfCaseConstants() {
        ExpressionsParser parser = new Parser(new Scanner("case MONDAY, TUESDAY, WEDNESDAY")).getExpressionsParser();
        ASTSwitchLabel node = parser.parseSwitchLabel();
        checkSimple(node, ASTCaseConstants.class, CASE);
        node.collapseThenPrint();
    }

    /**
     * Tests Switch Label of Default.
     */
    @Test
    public void testSwitchLabelOfDefault() {
        ExpressionsParser parser = new Parser(new Scanner("default")).getExpressionsParser();
        ASTSwitchLabel node = parser.parseSwitchLabel();
        checkEmpty(node, DEFAULT);
        node.collapseThenPrint();
    }

    /**
     * Tests Case Constants.
     */
    @Test
    public void testCaseConstants() {
        ExpressionsParser parser = new Parser(new Scanner("RED, GREEN, RED | GREEN")).getExpressionsParser();
        ASTCaseConstants node = parser.parseCaseConstants();
        checkList(node, COMMA, ASTConditionalExpression.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests Guard.
     */
    @Test
    public void testGuard() {
        ExpressionsParser parser = new Parser(new Scanner("when a == b")).getExpressionsParser();
        ASTGuard node = parser.parseGuard();
        checkSimple(node, ASTConditionalExpression.class, WHEN);
        node.collapseThenPrint();
    }

    /**
     * Tests Pattern List.
     */
    @Test
    public void testPatternList() {
        ExpressionsParser parser = new Parser(new Scanner("Widget w, Sprocket s, XrayMachine x")).getExpressionsParser();
        ASTPatternList node = parser.parsePatternList();
        checkList(node, COMMA, ASTPattern.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests Pattern of Type Pattern.
     */
    @Test
    public void testPatternOfTypePattern() {
        ExpressionsParser parser = new Parser(new Scanner("Widget w")).getExpressionsParser();
        ASTPattern node = parser.parsePattern();
        checkSimple(node, ASTTypePattern.class);
        node.collapseThenPrint();
    }

    /**
     * Tests Pattern of Record Pattern.
     */
    @Test
    public void testPatternOfRecordPattern() {
        ExpressionsParser parser = new Parser(new Scanner("Order(Int line, Double amt)")).getExpressionsParser();
        ASTPattern node = parser.parsePattern();
        checkSimple(node, ASTRecordPattern.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested Record Patterns.
     */
    @Test
    public void testRecordPatternNested() {
        ExpressionsParser parser = new Parser(new Scanner("Order(LineItem(Integer id, Double amt))")).getExpressionsParser();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTRecordPattern node = parser.parseRecordPattern(dt);
        checkBinary(node, ASTDataType.class, ASTPatternList.class);
        ASTPatternList pl = (ASTPatternList) node.getChildren().get(1);
        checkList(pl, COMMA, ASTPattern.class, 1);
        ASTPattern inner = (ASTPattern) pl.getChildren().get(0);
        checkSimple(inner, ASTRecordPattern.class);
        ASTRecordPattern innerRp = (ASTRecordPattern) inner.getChildren().get(0);
        checkBinary(innerRp, ASTDataType.class, ASTPatternList.class);
        ASTPatternList nested = (ASTPatternList) innerRp.getChildren().get(1);
        checkList(nested, COMMA, ASTPattern.class, 2);
        node.collapseThenPrint();
    }

    /**
     * Tests record pattern.
     */
    @Test
    public void testRecordPattern() {
        ExpressionsParser parser = new Parser(new Scanner("Person(String first, String last)")).getExpressionsParser();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTRecordPattern node = parser.parseRecordPattern(dt);
        checkBinary(node, ASTDataType.class, ASTPatternList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests type pattern with variable modifier.
     */
    @Test
    public void testTypePattern() {
        ExpressionsParser parser = new Parser(new Scanner("mut DataType id")).getExpressionsParser();
        ASTTypePattern node = parser.parseTypePattern();
        checkTrinary(node, null, ASTVariableModifierList.class, ASTDataType.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests type pattern without variable modifier.
     */
    @Test
    public void testTypePatternNoVariableModifier() {
        ExpressionsParser parser = new Parser(new Scanner("DataType id")).getExpressionsParser();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTTypePattern node = parser.parseTypePattern(dt);
        checkBinary(node, ASTDataType.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests argument list of expression.
     */
    @Test
    public void testArgumentListOfExpression() {
        ExpressionsParser parser = new Parser(new Scanner("index")).getExpressionsParser();
        ASTArgumentList node = parser.parseArgumentList();
        checkSimple(node, ASTGiveExpression.class, COMMA);
        node.collapseThenPrint();
    }

    /**
     * Tests argument list of nested argument lists (here, just multiple arguments).
     */
    @Test
    public void testArgumentListNested() {
        ExpressionsParser parser = new Parser(new Scanner("a, 1, b + c")).getExpressionsParser();
        ASTArgumentList node = parser.parseArgumentList();
        checkList(node, COMMA, ASTGiveExpression.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests a give expression of an expression.
     */
    @Test
    public void testGiveExpressionNoGive() {
        ExpressionsParser parser = new Parser(new Scanner("x + 1")).getExpressionsParser();
        ASTGiveExpression node = parser.parseGiveExpression();
        checkSimple(node, ASTExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests a give expression of "give" then an expression.
     */
    @Test
    public void testGiveExpressionWithGive() {
        ExpressionsParser parser = new Parser(new Scanner("give a.b")).getExpressionsParser();
        ASTGiveExpression node = parser.parseGiveExpression();
        checkSimple(node, ASTExpression.class, GIVE);
        node.collapseThenPrint();
    }

    /**
     * Tests primary of expression name.
     */
    @Test
    public void testPrimaryOfExpressionName() {
        ExpressionsParser parser = new Parser(new Scanner("a.b")).getExpressionsParser();
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTExpressionName.class);
        node.collapseThenPrint();
    }

    /**
     * Tests primary of literal.
     */
    @Test
    public void testPrimaryOfLiteral() {
        ExpressionsParser parser = new Parser(new Scanner("3.14")).getExpressionsParser();
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTLiteral.class);
        node.collapseThenPrint();
    }

    /**
     * Tests primary of class literal (data type).
     */
    @Test
    public void testPrimaryOfClassLiteralOfDataType() {
        ExpressionsParser parser = new Parser(new Scanner("spruce.lang.Comparable<String>[][].class")).getExpressionsParser();
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTClassLiteral.class);
        ASTClassLiteral classLiteral = (ASTClassLiteral) node.getChildren().get(0);
        checkSimple(classLiteral, ASTDataType.class, CLASS);
        node.collapseThenPrint();
    }

    /**
     * Tests primary of "self".
     */
    @Test
    public void testPrimaryOfSelf() {
        ExpressionsParser parser = new Parser(new Scanner("self")).getExpressionsParser();
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTSelf.class);
        node.collapseThenPrint();
    }

    /**
     * Tests primary of parenthesized expression.
     */
    @Test
    public void testPrimaryOfParenthesizedExpression() {
        ExpressionsParser parser = new Parser(new Scanner("(a + b)")).getExpressionsParser();
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTExpression.class, OPEN_PARENTHESIS);
        node.collapseThenPrint();
    }

    /**
     * Tests primary of element access.
     */
    @Test
    public void testPrimaryOfElementAccess() {
        ExpressionsParser parser = new Parser(new Scanner("a[1][2][3]")).getExpressionsParser();
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTElementAccess.class);

        ASTElementAccess ea = (ASTElementAccess) node.getChildren().get(0);
        checkBinary(ea, OPEN_BRACKET, ASTElementAccess.class, ASTExpression.class);

        ea = (ASTElementAccess) ea.getChildren().get(0);
        checkBinary(ea, OPEN_BRACKET, ASTElementAccess.class, ASTExpression.class);

        ea = (ASTElementAccess) ea.getChildren().get(0);
        checkBinary(ea, OPEN_BRACKET, ASTPrimary.class, ASTExpression.class);

        node.collapseThenPrint();
    }

    /**
     * Tests primary of field access of super.
     */
    @Test
    public void testPrimaryOfFieldAccessOfSuper() {
        ExpressionsParser parser = new Parser(new Scanner("super.superclassField")).getExpressionsParser();
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTFieldAccess.class);
        ASTFieldAccess fa = (ASTFieldAccess) node.getChildren().get(0);
        checkBinary(fa, DOT, ASTSuper.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests primary of field access of type name and super.
     */
    @Test
    public void testPrimaryOfFieldAccessOfTypeNameSuper() {
        ExpressionsParser parser = new Parser(new Scanner("EnclosingClass.super.superclassField")).getExpressionsParser();
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTFieldAccess.class);
        ASTFieldAccess fa = (ASTFieldAccess) node.getChildren().get(0);
        checkTrinary(fa, DOT, ASTTypeName.class, ASTSuper.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests primary of field access of primary.
     */
    @Test
    public void testPrimaryOfFieldAccessOfPrimary() {
        ExpressionsParser parser = new Parser(new Scanner("method().field")).getExpressionsParser();
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTFieldAccess.class);
        ASTFieldAccess fa = (ASTFieldAccess) node.getChildren().get(0);
        checkBinary(fa, DOT, ASTPrimary.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests primary of method invocation, expression name.
     */
    @Test
    public void testPrimaryOfMethodInvocationOfExpressionName() {
        ExpressionsParser parser = new Parser(new Scanner("expr.name.methodName()")).getExpressionsParser();
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTMethodInvocation.class);
        ASTMethodInvocation mi = (ASTMethodInvocation) node.getChildren().get(0);
        checkBinary(mi, OPEN_PARENTHESIS, ASTExpressionName.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests primary of method invocation, expression name and type arguments.
     */
    @Test
    public void testPrimaryOfMethodInvocationOfExpressionNameTypeArguments() {
        ExpressionsParser parser = new Parser(new Scanner("expr.name.<T>methodName(one)")).getExpressionsParser();
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTMethodInvocation.class);
        ASTMethodInvocation mi = (ASTMethodInvocation) node.getChildren().get(0);
        checkNary(mi, OPEN_PARENTHESIS, ASTExpressionName.class, ASTTypeArguments.class, ASTIdentifier.class, ASTArgumentList.class);
        ASTIdentifier methodName = (ASTIdentifier) mi.getChildren().get(2);
        assertEquals("methodName", methodName.getValue());
        node.collapseThenPrint();
    }

    /**
     * Tests primary of method invocation, simple name.
     */
    @Test
    public void testPrimaryOfMethodInvocationOfSimpleName() {
        ExpressionsParser parser = new Parser(new Scanner("methodName(helperMethod(i), (a + b), j + 1)")).getExpressionsParser();
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTMethodInvocation.class);
        ASTMethodInvocation mi = (ASTMethodInvocation) node.getChildren().get(0);
        checkBinary(mi, OPEN_PARENTHESIS, ASTIdentifier.class, ASTArgumentList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests primary of method invocation starting with <code>super</code>.
     */
    @Test
    public void testPrimaryOfMethodInvocationOfSuper() {
        ExpressionsParser parser = new Parser(new Scanner("super.<T>inheritedMethod(\"super\")")).getExpressionsParser();
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTMethodInvocation.class);
        ASTMethodInvocation mi = (ASTMethodInvocation) node.getChildren().get(0);
        checkNary(mi, OPEN_PARENTHESIS, ASTSuper.class, ASTTypeArguments.class, ASTIdentifier.class, ASTArgumentList.class);
        ASTIdentifier methodName = (ASTIdentifier) mi.getChildren().get(2);
        assertEquals("inheritedMethod", methodName.getValue());
        node.collapseThenPrint();
    }

    /**
     * Tests primary of method invocation starting with <code>super</code>.
     */
    @Test
    public void testPrimaryOfMethodInvocationOfTypeNameSuper() {
        ExpressionsParser parser = new Parser(new Scanner("org.test.EnclosingClass.super.<T>inheritedMethod(\"super\")")).getExpressionsParser();
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTMethodInvocation.class);
        ASTMethodInvocation mi = (ASTMethodInvocation) node.getChildren().get(0);
        checkNary(mi, OPEN_PARENTHESIS, ASTTypeName.class, ASTSuper.class, ASTTypeArguments.class, ASTIdentifier.class, ASTArgumentList.class);
        ASTIdentifier methodName = (ASTIdentifier) mi.getChildren().get(3);
        assertEquals("inheritedMethod", methodName.getValue());
        node.collapseThenPrint();
    }

    /**
     * Tests primary of array creation expression.
     */
    @Test
    public void testPrimaryOfArrayCreationExpression() {
        ExpressionsParser parser = new Parser(new Scanner("new spruce.lang.String[23]")).getExpressionsParser();
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTArrayCreationExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests primary of class instance creation expression.
     */
    @Test
    public void testPrimaryOfClassInstanceCreationExpression() {
        ExpressionsParser parser = new Parser(new Scanner("new Team(25, \"Dodgers\")")).getExpressionsParser();
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTClassInstanceCreationExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests primary of qualified class instance creation expression.
     */
    @Test
    public void testPrimaryOfClassInstanceCreationExpressionQualified() {
        ExpressionsParser parser = new Parser(new Scanner("league.new Team(25, \"Dodgers\")")).getExpressionsParser();
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTClassInstanceCreationExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests primary of type name, ".", and self.
     */
    @Test
    public void testPrimaryOfTypeNameDotSelf() {
        ExpressionsParser parser = new Parser(new Scanner("qualified.type.self")).getExpressionsParser();
        ASTPrimary node = parser.parsePrimary();
        checkBinary(node, DOT, ASTTypeName.class, ASTSelf.class);
        node.collapseThenPrint();
    }

    /**
     * Tests primary of method reference starting with "super".
     */
    @Test
    public void testPrimaryOfMethodReferenceSuper() {
        ExpressionsParser parser = new Parser(new Scanner("super::<String>methodName")).getExpressionsParser();
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTMethodReference.class);
        ASTMethodReference mRef = (ASTMethodReference) node.getChildren().get(0);
        checkTrinary(mRef, DOUBLE_COLON, ASTSuper.class, ASTTypeArguments.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests Primary of Method Reference of "new".
     */
    @Test
    public void testPrimaryOfConstructorReference() {
        ExpressionsParser parser = new Parser(new Scanner("spruce.lang.String::new")).getExpressionsParser();
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTMethodReference.class);
        ASTMethodReference mRef = (ASTMethodReference) node.getChildren().get(0);
        checkSimple(mRef, ASTDataType.class, DOUBLE_COLON);
        node.collapseThenPrint();
    }

    /**
     * Tests Primary of Method Reference of Expression Name.
     */
    @Test
    public void testPrimaryOfMethodReferenceOfExpressionName() {
        ExpressionsParser parser = new Parser(new Scanner("spruce.lang.String::size")).getExpressionsParser();
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTMethodReference.class);
        ASTMethodReference mRef = (ASTMethodReference) node.getChildren().get(0);
        checkBinary(mRef, DOUBLE_COLON, ASTExpressionName.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests Primary of Method Reference of DataType.
     */
    @Test
    public void testPrimaryOfMethodReferenceOfDataType() {
        ExpressionsParser parser = new Parser(new Scanner("Comparator<String>::compare;")).getExpressionsParser();
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTMethodReference.class);
        ASTMethodReference mRef = (ASTMethodReference) node.getChildren().get(0);
        checkBinary(mRef, DOUBLE_COLON, ASTDataType.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests Primary of Method Reference of Primary.
     */
    @Test
    public void testPrimaryOfMethodReferenceOfPrimary() {
        ExpressionsParser parser = new Parser(new Scanner("(\"a\" + \"b\")::length;")).getExpressionsParser();
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTMethodReference.class);
        ASTMethodReference mRef = (ASTMethodReference) node.getChildren().get(0);
        checkBinary(mRef, DOUBLE_COLON, ASTPrimary.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests Primary of Method Reference of TypeName and super.
     */
    @Test
    public void testPrimaryOfMethodReferenceOfTypeNameSuper() {
        ExpressionsParser parser = new Parser(new Scanner("type.Name.super::length;")).getExpressionsParser();
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTMethodReference.class);
        ASTMethodReference mRef = (ASTMethodReference) node.getChildren().get(0);
        checkTrinary(mRef, DOUBLE_COLON, ASTTypeName.class, ASTSuper.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested primary expressions, including Class Instance Creation
     * Expressions, Method Invocations, Field Accesses, and Element Accesses.
     */
    @Test
    public void testPrimaryOfNested() {
        ExpressionsParser parser = new Parser(new Scanner("new Foo()[i].field1.method1()[j].field2.<T>method2(1).new Bar()")).getExpressionsParser();
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTClassInstanceCreationExpression.class);
        ASTClassInstanceCreationExpression outerCice = (ASTClassInstanceCreationExpression) node.getChildren().get(0);
        checkBinary(outerCice, ASTPrimary.class, ASTUnqualifiedClassInstanceCreationExpression.class);

        ASTPrimary pMethod2 = (ASTPrimary) outerCice.getChildren().get(0);
        checkSimple(pMethod2, ASTMethodInvocation.class);
        ASTMethodInvocation method2 = (ASTMethodInvocation) pMethod2.getChildren().get(0);
        checkNary(method2, OPEN_PARENTHESIS, ASTPrimary.class, ASTTypeArguments.class, ASTIdentifier.class, ASTArgumentList.class);
        ASTIdentifier methodName2 = (ASTIdentifier) method2.getChildren().get(2);
        assertEquals("method2", methodName2.getValue());

        ASTPrimary pFieldAccess2 = (ASTPrimary) method2.getChildren().get(0);
        checkSimple(pFieldAccess2, ASTFieldAccess.class);
        ASTFieldAccess fieldAccess2 = (ASTFieldAccess) pFieldAccess2.getChildren().get(0);
        checkBinary(fieldAccess2, DOT, ASTPrimary.class, ASTIdentifier.class);
        ASTIdentifier fieldName2 = (ASTIdentifier) fieldAccess2.getChildren().get(1);
        assertEquals("field2", fieldName2.getValue());

        ASTPrimary pJElementAccess = (ASTPrimary) fieldAccess2.getChildren().get(0);
        checkSimple(pJElementAccess, ASTElementAccess.class);
        ASTElementAccess jElementAccess = (ASTElementAccess) pJElementAccess.getChildren().get(0);
        checkBinary(jElementAccess, OPEN_BRACKET, ASTPrimary.class, ASTExpression.class);

        ASTPrimary pMethod1 = (ASTPrimary) jElementAccess.getChildren().get(0);
        checkSimple(pMethod1, ASTMethodInvocation.class);
        ASTMethodInvocation method1 = (ASTMethodInvocation) pMethod1.getChildren().get(0);
        checkBinary(method1, OPEN_PARENTHESIS, ASTPrimary.class, ASTIdentifier.class);
        ASTIdentifier methodName1 = (ASTIdentifier) method1.getChildren().get(1);
        assertEquals("method1", methodName1.getValue());

        ASTPrimary pFieldAccess1 = (ASTPrimary) method1.getChildren().get(0);
        checkSimple(pFieldAccess1, ASTFieldAccess.class);
        ASTFieldAccess fieldAccess1 = (ASTFieldAccess) pFieldAccess1.getChildren().get(0);
        checkBinary(fieldAccess1, DOT, ASTPrimary.class, ASTIdentifier.class);
        ASTIdentifier fieldName1 = (ASTIdentifier) fieldAccess1.getChildren().get(1);
        assertEquals("field1", fieldName1.getValue());

        ASTPrimary pIElementAccess = (ASTPrimary) fieldAccess1.getChildren().get(0);
        checkSimple(pIElementAccess, ASTElementAccess.class);
        ASTElementAccess iElementAccess = (ASTElementAccess) pIElementAccess.getChildren().get(0);
        checkBinary(iElementAccess, OPEN_BRACKET, ASTPrimary.class, ASTExpression.class);

        ASTPrimary pInnerCice = (ASTPrimary) iElementAccess.getChildren().get(0);
        checkSimple(pInnerCice, ASTClassInstanceCreationExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests unqualified class instance creation expression of type arguments and type to instantiate.
     */
    @Test
    public void testUnqualifiedClassInstanceCreationExpressionOfTypeArguments() {
        ExpressionsParser parser = new Parser(new Scanner("new <String> MyClass()")).getExpressionsParser();
        ASTUnqualifiedClassInstanceCreationExpression node = parser.parseUnqualifiedClassInstanceCreationExpression();
        checkBinary(node, NEW, ASTTypeArguments.class, ASTTypeToInstantiate.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class instance creation expression of unqualified class instance
     * creation expression.
     */
    @Test
    public void testClassInstanceCreationExpressionOfUCICE() {
        ExpressionsParser parser = new Parser(new Scanner("new MyClass(1, \"one\")")).getExpressionsParser();
        ASTClassInstanceCreationExpression node = parser.parseClassInstanceCreationExpression();
        checkSimple(node, ASTUnqualifiedClassInstanceCreationExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests unqualified class instance creation expression of type to instantiate and argument list.
     */
    @Test
    public void testUnqualifiedClassInstanceCreationExpressionOfArgumentList() {
        ExpressionsParser parser = new Parser(new Scanner("new MyClass(1, \"one\")")).getExpressionsParser();
        ASTUnqualifiedClassInstanceCreationExpression node = parser.parseUnqualifiedClassInstanceCreationExpression();
        checkBinary(node, NEW, ASTTypeToInstantiate.class, ASTArgumentList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests type to instantiate of type name.
     */
    @Test
    public void testTypeToInstantiateOfTypeName() {
        ExpressionsParser parser = new Parser(new Scanner("MyClass")).getExpressionsParser();
        ASTTypeToInstantiate node = parser.parseTypeToInstantiate();
        checkSimple(node, ASTTypeName.class);
        node.collapseThenPrint();
    }

    /**
     * Tests type to instantiate of type name and type arguments or diamond.
     */
    @Test
    public void testTypeToInstantiateTypeNameOfTypeArgumentsOrDiamond() {
        ExpressionsParser parser = new Parser(new Scanner("MyClass<T>")).getExpressionsParser();
        ASTTypeToInstantiate node = parser.parseTypeToInstantiate();
        checkBinary(node, ASTTypeName.class, ASTTypeArgumentsOrDiamond.class);
        node.collapseThenPrint();
    }

    /**
     * Tests array creation expression of dim exprs.
     */
    @Test
    public void testArrayCreationExpressionOfDimExprs() {
        ExpressionsParser parser = new Parser(new Scanner("new String[10]")).getExpressionsParser();
        ASTArrayCreationExpression node = parser.parseArrayCreationExpression();
        checkBinary(node, NEW, ASTTypeToInstantiate.class, ASTDimExprs.class);
        node.collapseThenPrint();
    }

    /**
     * Tests array creation expression of dim exprs and dims.
     */
    @Test
    public void testArrayCreationExpressionOfDimExprsDims() {
        ExpressionsParser parser = new Parser(new Scanner("new String[10][]")).getExpressionsParser();
        ASTArrayCreationExpression node = parser.parseArrayCreationExpression();
        checkTrinary(node, NEW, ASTTypeToInstantiate.class, ASTDimExprs.class, ASTDims.class);
        node.collapseThenPrint();
    }

    /**
     * Tests array creation expression of dims and array initializer.
     */
    @Test
    public void testArrayCreationExpressionOfDimsArrayInitializer() {
        ExpressionsParser parser = new Parser(new Scanner("new String[] {\"one\", \"two\", \"three\"}")).getExpressionsParser();
        ASTArrayCreationExpression node = parser.parseArrayCreationExpression();
        checkTrinary(node, NEW, ASTTypeToInstantiate.class, ASTDims.class, ASTArrayInitializer.class);
        node.collapseThenPrint();
    }

    /**
     * Tests dim exprs of dim expr.
     */
    @Test
    public void testDimExprsDimExpr() {
        ExpressionsParser parser = new Parser(new Scanner("[1][2][3]")).getExpressionsParser();
        ASTDimExprs node = parser.parseDimExprs();
        checkList(node, null, ASTDimExpr.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests dim expr of expression.
     */
    @Test
    public void testDimExprOfExpression() {
        ExpressionsParser parser = new Parser(new Scanner("[x+y]")).getExpressionsParser();
        ASTDimExpr node = parser.parseDimExpr();
        checkSimple(node, ASTExpression.class, OPEN_BRACKET);
        node.collapseThenPrint();
    }

    /**
     * Tests array initializer of just empty braces.
     */
    @Test
    public void testArrayInitializerEmpty() {
        ExpressionsParser parser = new Parser(new Scanner("{}")).getExpressionsParser();
        ASTArrayInitializer node = parser.parseArrayInitializer();
        checkEmpty(node, OPEN_BRACE);
        node.collapseThenPrint();
    }

    /**
     * Tests array initializer of a variable initializer list.
     */
    @Test
    public void testArrayInitializerOfVariableInitializerList() {
        ExpressionsParser parser = new Parser(new Scanner("{x + 1, y - 2}")).getExpressionsParser();
        ASTArrayInitializer node = parser.parseArrayInitializer();
        checkSimple(node, ASTVariableInitializerList.class, OPEN_BRACE);
        node.collapseThenPrint();
    }

    /**
     * Tests variable initializer list of variable initializer.
     */
    @Test
    public void testVariableInitializerListOfVariableInitializer() {
        ExpressionsParser parser = new Parser(new Scanner("i + 1")).getExpressionsParser();
        ASTVariableInitializerList node = parser.parseVariableInitializerList();
        checkSimple(node, ASTVariableInitializer.class, COMMA);
        node.collapseThenPrint();
    }

    /**
     * Tests variable initializer list of "," and variable initializer.
     */
    @Test
    public void testVariableInitializerListOfComma() {
        ExpressionsParser parser = new Parser(new Scanner("x + 1, y - 1")).getExpressionsParser();
        ASTVariableInitializerList node = parser.parseVariableInitializerList();
        checkList(node, COMMA, ASTVariableInitializer.class, 2);
        node.collapseThenPrint();
    }

    /**
     * Tests nested variable initializer lists (here, just multiple variable initializers).
     */
    @Test
    public void testVariableInitializerListNested() {
        ExpressionsParser parser = new Parser(new Scanner("self, count + 1, sumSoFar + value")).getExpressionsParser();
        ASTVariableInitializerList node = parser.parseVariableInitializerList();
        checkList(node, COMMA, ASTVariableInitializer.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests variable initializer of expression.
     */
    @Test
    public void testVariableInitializerOfExpression() {
        ExpressionsParser parser = new Parser(new Scanner("a + b")).getExpressionsParser();
        ASTVariableInitializer node = parser.parseVariableInitializer();
        checkSimple(node, ASTExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests variable initializer of array initializer.
     */
    @Test
    public void testVariableInitializerOfArrayInitializer() {
        ExpressionsParser parser = new Parser(new Scanner("{1, 2, 3}")).getExpressionsParser();
        ASTVariableInitializer node = parser.parseVariableInitializer();
        checkSimple(node, ASTArrayInitializer.class);
        node.collapseThenPrint();
    }

    /**
     * Tests a class literal.  Parses a DataType first.
     */
    @Test
    public void testClassLiteral() {
        ExpressionsParser parser = new Parser(new Scanner("Outer.Inner.class")).getExpressionsParser();
        ASTDataType dataType = parser.getTypesParser().parseDataType();
        ASTClassLiteral node = parser.parseClassLiteral(dataType);
        checkSimple(node, ASTDataType.class, CLASS);
    }

    /**
     * Tests "self".
     */
    @Test
    public void testSelf() {
        ExpressionsParser parser = new Parser(new Scanner("self")).getExpressionsParser();
        ASTSelf node = parser.parseSelf();
        checkIs(node, ASTSelf.class);
        node.print();
    }

    /**
     * Tests "super".
     */
    @Test
    public void testSuper() {
        ExpressionsParser parser = new Parser(new Scanner("super")).getExpressionsParser();
        ASTSuper node = parser.parseSuper();
        checkIs(node, ASTSuper.class);
        node.print();
    }
}
