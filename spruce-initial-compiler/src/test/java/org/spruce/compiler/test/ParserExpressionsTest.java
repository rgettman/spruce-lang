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
        ExpressionsParser parser = getExpressionsParser("p -> q -> p.foo(q)");
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
        ExpressionsParser parser = getExpressionsParser("|x, y| -> { use x + y;}");
        ASTExpression node = parser.parseExpression();
        checkSimple(node, ASTLambdaExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests bad lambda expression.
     */
    @Test
    public void testBadLambdaExpression() {
        ExpressionsParser parser = getExpressionsParser("|x, y| oops()");
        assertThrows(CompileException.class, parser::parseLambdaExpression, "Expected \"->\".");
    }

    /**
     * Tests lambda expression.
     */
    @Test
    public void testLambdaExpression() {
        ExpressionsParser parser = getExpressionsParser("n -> n * 2");
        ASTLambdaExpression node = parser.parseLambdaExpression();
        checkBinary(node, ARROW, ASTLambdaParameters.class, ASTLambdaBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests bad lambda body.
     */
    @Test
    public void testBadLambdaBody() {
        ExpressionsParser parser = getExpressionsParser("class Bad");
        assertThrows(CompileException.class, parser::parseLambdaBody, "Expected expression or block.");
    }

    /**
     * Tests lambda body of block.
     */
    @Test
    public void testLambdaBodyOfBlock() {
        ExpressionsParser parser = getExpressionsParser("{ use n * 2; }");
        ASTLambdaBody node = parser.parseLambdaBody();
        checkSimple(node, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests lambda body of expression.
     */
    @Test
    public void testLambdaBodyOfExpression() {
        ExpressionsParser parser = getExpressionsParser("n * 2");
        ASTLambdaBody node = parser.parseLambdaBody();
        checkSimple(node, ASTExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests bad lambda parameters.
     */
    @Test
    public void testBadLambdaParameters() {
        ExpressionsParser parser = getExpressionsParser("2, 3");
        assertThrows(CompileException.class, parser::parseLambdaParameters, "Expected lambda parameters.");
    }

    /**
     * Tests lambda parameters of a bare identifier.
     */
    @Test
    public void testLambdaParametersOfIdentifier() {
        ExpressionsParser parser = getExpressionsParser("n");
        ASTLambdaParameters node = parser.parseLambdaParameters();
        checkSimple(node, ASTIdentifier.class);
        node.collapseThenPrint();
    }
    /**
     * Tests lambda parameters of lambda parameter list.
     */
    @Test
    public void testLambdaParametersOfLambdaParameterList() {
        ExpressionsParser parser = getExpressionsParser("|a, b|");
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
        ExpressionsParser parser = getExpressionsParser("| |");
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
        ExpressionsParser parser = getExpressionsParser("||");
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
        ExpressionsParser parser = getExpressionsParser("Double x, Double y, Double z");
        ASTLambdaParameterList node = parser.parseLambdaParameterList();
        checkSimple(node, ASTFormalParameterList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests bad lambda parameter list.
     */
    @Test
    public void testBadLambdaParameterList() {
        ExpressionsParser parser = getExpressionsParser("give badParameter");
        assertThrows(CompileException.class, parser::parseLambdaParameterList, "Expected lambda parameter(s).");
    }

    /**
     * Tests lambda parameter list of inferred parameter list.
     */
    @Test
    public void testLambdaParameterListOfInferredParameterList() {
        ExpressionsParser parser = getExpressionsParser("alpha, beta, gamma");
        ASTLambdaParameterList node = parser.parseLambdaParameterList();
        checkSimple(node, ASTInferredParameterList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests bad inferred parameter list.
     */
    @Test
    public void testBadInferredParameterList() {
        ExpressionsParser parser = getExpressionsParser("2");
        assertThrows(CompileException.class, parser::parseInferredParameterList, "Expected a lambda parameter.");
    }

    /**
     * Tests inferred parameter list.
     */
    @Test
    public void testInferredParameterList() {
        ExpressionsParser parser = getExpressionsParser("alpha, beta, gamma");
        ASTInferredParameterList node = parser.parseInferredParameterList();
        checkList(node, COMMA, ASTIdentifier.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests expression of other expression.
     */
    @Test
    public void testExpressionOfOtherExpression() {
        ExpressionsParser parser = getExpressionsParser("count == 1");
        ASTExpression node = parser.parseExpression();
        checkSimple(node, ASTConditionalExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests expression of conditional expression.
     */
    @Test
    public void testExpressionOfConditionalExpression() {
        ExpressionsParser parser = getExpressionsParser("a ? b : c");
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
            ExpressionsParser parser = getExpressionsParser(code);
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
        ExpressionsParser parser = getExpressionsParser("expr.name");
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
        ExpressionsParser parser = getExpressionsParser("getArray()[1]");
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
        ExpressionsParser parser = getExpressionsParser("expr.name");
        ASTLeftHandSide node = parser.parseLeftHandSide();
        checkSimple(node, ASTExpressionName.class);
        node.collapseThenPrint();
    }

    /**
     * Tests left hand side of element access.
     */
    @Test
    public void testLeftHandSideOfElementAccess() {
        ExpressionsParser parser = getExpressionsParser("array[i]");
        ASTLeftHandSide node = parser.parseLeftHandSide();
        checkSimple(node, ASTElementAccess.class);
        node.collapseThenPrint();
    }

    /**
     * Tests left hand side of field access.
     */
    @Test
    public void testLeftHandSideOfFieldAccess() {
        ExpressionsParser parser = getExpressionsParser("self.x");
        ASTLeftHandSide node = parser.parseLeftHandSide();
        checkSimple(node, ASTFieldAccess.class);
        node.collapseThenPrint();
    }

    /**
     * Tests conditional expression of logical or expression.
     */
    @Test
    public void testConditionalExpressionOfLogicalOrExpression() {
        ExpressionsParser parser = getExpressionsParser("a || b");
        ASTConditionalExpression node = parser.parseConditionalExpression();
        checkSimple(node, ASTLogicalOrExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests conditional expression of "?" and ":" and logical or expression.
     */
    @Test
    public void testConditionalExpression() {
        ExpressionsParser parser = getExpressionsParser("condition ? valueIfTrue : valueIfFalse");
        ASTConditionalExpression node = parser.parseConditionalExpression();
        checkTrinary(node, QUESTION_MARK, ASTLogicalOrExpression.class, ASTLogicalOrExpression.class, ASTConditionalExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested conditional expressions.
     */
    @Test
    public void testConditionalExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("a || b ? \"one\" : c || d ? \"two\" : e || f ? \"three\" : \"four\"");
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
        ExpressionsParser parser = getExpressionsParser("a ^: b");
        ASTLogicalOrExpression node = parser.parseLogicalOrExpression();
        checkSimple(node, ASTLogicalXorExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests logical or expression of "|:" and logical xor expression.
     */
    @Test
    public void testLogicalOrExpressionOfEager() {
        ExpressionsParser parser = getExpressionsParser("test |: elseThis");
        ASTLogicalOrExpression node = parser.parseLogicalOrExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(PIPE_COLON), ASTLogicalOrExpression.class, ASTLogicalXorExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests logical or expression of "||" and logical xor expression.
     */
    @Test
    public void testLogicalOrExpressionOfConditional() {
        ExpressionsParser parser = getExpressionsParser("alreadyDone || test");
        ASTLogicalOrExpression node = parser.parseLogicalOrExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(DOUBLE_PIPE), ASTLogicalOrExpression.class, ASTLogicalXorExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested logical or expressions.
     */
    @Test
    public void testLogicalOrExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("a && b |: c ^: d || e &: f");
        ASTLogicalOrExpression node = parser.parseLogicalOrExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(DOUBLE_PIPE, PIPE_COLON), ASTLogicalOrExpression.class, ASTLogicalXorExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests logical xor expression of logical and expression.
     */
    @Test
    public void testLogicalXorExpressionOfLogicalAndExpression() {
        ExpressionsParser parser = getExpressionsParser("a && b");
        ASTLogicalXorExpression node = parser.parseLogicalXorExpression();
        checkSimple(node, ASTLogicalAndExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests logical xor expression of "^:" and logical and expression.
     */
    @Test
    public void testLogicalXorExpression() {
        ExpressionsParser parser = getExpressionsParser("test ^: thisAlso");
        ASTLogicalXorExpression node = parser.parseLogicalXorExpression();
        checkBinaryLeftAssociative(node,Collections.singletonList(CARET_COLON), ASTLogicalXorExpression.class, ASTLogicalAndExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested logical xor expressions.
     */
    @Test
    public void testLogicalXorExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("a && b ^: c &: d ^: e && f");
        ASTLogicalXorExpression node = parser.parseLogicalXorExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(CARET_COLON, CARET_COLON), ASTLogicalXorExpression.class, ASTLogicalAndExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests logical and expression of relational expression.
     */
    @Test
    public void testLogicalAndExpressionOfRelationalExpression() {
        ExpressionsParser parser = getExpressionsParser("a = b");
        ASTLogicalAndExpression node = parser.parseLogicalAndExpression();
        checkSimple(node, ASTRelationalExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests logical and expression of "&&" and relational expression.
     */
    @Test
    public void testLogicalAndExpressionOfConditional() {
        ExpressionsParser parser = getExpressionsParser("test && notDone");
        ASTLogicalAndExpression node = parser.parseLogicalAndExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(DOUBLE_AMPERSAND), ASTLogicalAndExpression.class, ASTRelationalExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests logical and expression of "&:" and relational expression.
     */
    @Test
    public void testLogicalAndExpressionOfEager() {
        ExpressionsParser parser = getExpressionsParser("test &: thisAlso");
        ASTLogicalAndExpression node = parser.parseLogicalAndExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(AMPERSAND_COLON), ASTLogicalAndExpression.class, ASTRelationalExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested logical and expressions.
     */
    @Test
    public void testLogicalAndExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("a < b &: c <= d && e > f");
        ASTLogicalAndExpression node = parser.parseLogicalAndExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(DOUBLE_AMPERSAND, AMPERSAND_COLON), ASTLogicalAndExpression.class, ASTRelationalExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests relational expression of compare expression.
     */
    @Test
    public void testRelationalExpressionOfCompareExpression() {
        ExpressionsParser parser = getExpressionsParser("a <=> b");
        ASTRelationalExpression node = parser.parseRelationalExpression();
        checkSimple(node, ASTCompareExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests relational expression of "&lt;" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfLessThan() {
        ExpressionsParser parser = getExpressionsParser("a.value < b.value");
        ASTRelationalExpression node = parser.parseRelationalExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(LESS_THAN), ASTRelationalExpression.class, ASTCompareExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests relational expression of "&lt;=" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfLessThanOrEqual() {
        ExpressionsParser parser = getExpressionsParser("2 <= 2");
        ASTRelationalExpression node = parser.parseRelationalExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(LESS_THAN_OR_EQUAL), ASTRelationalExpression.class, ASTCompareExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests relational expression of "&gt;" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfGreaterThan() {
        ExpressionsParser parser = getExpressionsParser("a.value > b.value");
        ASTRelationalExpression node = parser.parseRelationalExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(GREATER_THAN), ASTRelationalExpression.class, ASTCompareExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests relational expression of "&gt;=" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfGreaterThanOrEqual() {
        ExpressionsParser parser = getExpressionsParser("2 >= 2");
        ASTRelationalExpression node = parser.parseRelationalExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(GREATER_THAN_OR_EQUAL), ASTRelationalExpression.class, ASTCompareExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests relational expression of "=" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfEqual() {
        ExpressionsParser parser = getExpressionsParser("test == SUCCESS");
        ASTRelationalExpression node = parser.parseRelationalExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(DOUBLE_EQUAL), ASTRelationalExpression.class, ASTCompareExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests relational expression of "!=" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfNotEqual() {
        ExpressionsParser parser = getExpressionsParser("test != FAILURE");
        ASTRelationalExpression node = parser.parseRelationalExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(NOT_EQUAL), ASTRelationalExpression.class, ASTCompareExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests relational expression of "isa" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfIsa() {
        ExpressionsParser parser = getExpressionsParser("node isa ASTRelationalExpression");
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
        ExpressionsParser parser = getExpressionsParser("obj is other");
        ASTRelationalExpression node = parser.parseRelationalExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(IS), ASTRelationalExpression.class, ASTCompareExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests relational expression of "isnt" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfIsnt() {
        ExpressionsParser parser = getExpressionsParser("obj isnt somethingElse");
        ASTRelationalExpression node = parser.parseRelationalExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(ISNT), ASTRelationalExpression.class, ASTCompareExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested relational expressions.
     */
    @Test
    public void testRelationalExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("a < b <=> c <= d");
        ASTRelationalExpression node = parser.parseRelationalExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(LESS_THAN_OR_EQUAL, LESS_THAN), ASTRelationalExpression.class, ASTCompareExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests compare expression of bitwise or expression.
     */
    @Test
    public void testCompareExpressionOfBitwiseOrExpression() {
        ExpressionsParser parser = getExpressionsParser("a | b");
        ASTCompareExpression node = parser.parseCompareExpression();
        checkSimple(node, ASTBitwiseOrExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests compare expression of "&lt;=&gt;" and bitwise or expression.
     */
    @Test
    public void testCompareExpression() {
        ExpressionsParser parser = getExpressionsParser("a.value <=> b.value");
        ASTCompareExpression node = parser.parseCompareExpression();
        checkBinary(node, COMPARISON, ASTBitwiseOrExpression.class, ASTBitwiseOrExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests bitwise or expression of bitwise xor expression.
     */
    @Test
    public void testBitwiseOrExpressionOfBitwiseXorExpression() {
        ExpressionsParser parser = getExpressionsParser("a ^ b");
        ASTBitwiseOrExpression node = parser.parseBitwiseOrExpression();
        checkSimple(node, ASTBitwiseXorExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests bitwise or expression of "|" and bitwise xor expression.
     */
    @Test
    public void testBitwiseOrExpression() {
        ExpressionsParser parser = getExpressionsParser("color | blueMask");
        ASTBitwiseOrExpression node = parser.parseBitwiseOrExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(PIPE), ASTBitwiseOrExpression.class, ASTBitwiseXorExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested bitwise or expressions.
     */
    @Test
    public void testBitwiseOrExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("red | blue | yellow ^ green");
        ASTBitwiseOrExpression node = parser.parseBitwiseOrExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(PIPE, PIPE), ASTBitwiseOrExpression.class, ASTBitwiseXorExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests bitwise xor expression of bitwise and expression.
     */
    @Test
    public void testBitwiseXorExpressionOfBitwiseAndExpression() {
        ExpressionsParser parser = getExpressionsParser("a & b");
        ASTBitwiseXorExpression node = parser.parseBitwiseXorExpression();
        checkSimple(node, ASTBitwiseAndExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests bitwise xor expression of "^" and bitwise and expression.
     */
    @Test
    public void testBitwiseXorExpression() {
        ExpressionsParser parser = getExpressionsParser("color ^ blueMask");
        ASTBitwiseXorExpression node = parser.parseBitwiseXorExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(CARET), ASTBitwiseXorExpression.class, ASTBitwiseAndExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested bitwise xor expressions.
     */
    @Test
    public void testBitwiseXorExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("red ^ blue & yellow ^ green");
        ASTBitwiseXorExpression node = parser.parseBitwiseXorExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(CARET, CARET), ASTBitwiseXorExpression.class, ASTBitwiseAndExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests bitwise and expression of shift expression.
     */
    @Test
    public void testBitwiseAndExpressionOfShiftExpression() {
        ExpressionsParser parser = getExpressionsParser("a << b");
        ASTBitwiseAndExpression node = parser.parseBitwiseAndExpression();
        checkSimple(node, ASTShiftExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests bitwise and expression of "&" and shift expression.
     */
    @Test
    public void testBitwiseAndExpression() {
        ExpressionsParser parser = getExpressionsParser("color & blueMask");
        ASTBitwiseAndExpression node = parser.parseBitwiseAndExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(AMPERSAND), ASTBitwiseAndExpression.class, ASTShiftExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested bitwise and expressions.
     */
    @Test
    public void testBitwiseAndExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("red + blue & blueGreenMask & greenRedMask");
        ASTBitwiseAndExpression node = parser.parseBitwiseAndExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(AMPERSAND, AMPERSAND), ASTBitwiseAndExpression.class, ASTShiftExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests shift expression of additive expression.
     */
    @Test
    public void testShiftExpressionOfAdditiveExpression() {
        ExpressionsParser parser = getExpressionsParser("a + b");
        ASTShiftExpression node = parser.parseShiftExpression();
        checkSimple(node, ASTAdditiveExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests shift expression of "<<" and additive expression.
     */
    @Test
    public void testShiftExpressionOfLeftShift() {
        ExpressionsParser parser = getExpressionsParser("1 << 2");
        ASTShiftExpression node = parser.parseShiftExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(SHIFT_LEFT), ASTShiftExpression.class, ASTAdditiveExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests shift expression of ">>" and additive expression.
     */
    @Test
    public void testShiftExpressionOfRightShift() {
        ExpressionsParser parser = getExpressionsParser("2048 >> 2");
        ASTShiftExpression node = parser.parseShiftExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(SHIFT_RIGHT), ASTShiftExpression.class, ASTAdditiveExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested shift expressions.
     */
    @Test
    public void testShiftExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("-2 << 3 + 4 >> 5 >>> 1");
        ASTShiftExpression node = parser.parseShiftExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(SHIFT_RIGHT, SHIFT_LEFT), ASTShiftExpression.class, ASTAdditiveExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests additive expression of multiplicative expression.
     */
    @Test
    public void testAdditiveExpressionOfMultiplicativeExpression() {
        ExpressionsParser parser = getExpressionsParser("a * b");
        ASTAdditiveExpression node = parser.parseAdditiveExpression();
        checkSimple(node, ASTMultiplicativeExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests additive expression of "+" and multiplicative expression.
     */
    @Test
    public void testAdditiveExpressionOfPlus() {
        ExpressionsParser parser = getExpressionsParser("-1 + 2");
        ASTAdditiveExpression node = parser.parseAdditiveExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(PLUS), ASTAdditiveExpression.class, ASTMultiplicativeExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests additive expression of "-" and multiplicative expression.
     */
    @Test
    public void testAdditiveExpressionOfMinus() {
        ExpressionsParser parser = getExpressionsParser("finish - start");
        ASTAdditiveExpression node = parser.parseAdditiveExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(MINUS), ASTAdditiveExpression.class, ASTMultiplicativeExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested additive expressions.
     */
    @Test
    public void testAdditiveExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("-2 + 3 * 4 - 5");
        ASTAdditiveExpression node = parser.parseAdditiveExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(MINUS, PLUS), ASTAdditiveExpression.class, ASTMultiplicativeExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests multiplicative expression of unary expression.
     */
    @Test
    public void testMultiplicativeExpressionOfUnaryExpression() {
        ExpressionsParser parser = getExpressionsParser("varName");
        ASTMultiplicativeExpression node = parser.parseMultiplicativeExpression();
        checkSimple(node, ASTCastExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests multiplicative expression of "*" and unary expression.
     */
    @Test
    public void testMultiplicativeExpressionOfTimes() {
        ExpressionsParser parser = getExpressionsParser("a * b");
        ASTMultiplicativeExpression node = parser.parseMultiplicativeExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(STAR), ASTMultiplicativeExpression.class, ASTCastExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests multiplicative expression of "/" and unary expression.
     */
    @Test
    public void testMultiplicativeExpressionOfDivide() {
        ExpressionsParser parser = getExpressionsParser("i / -1");
        ASTMultiplicativeExpression node = parser.parseMultiplicativeExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(SLASH), ASTMultiplicativeExpression.class, ASTCastExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests multiplicative expression of "%" and unary expression.
     */
    @Test
    public void testMultiplicativeExpressionOfModulus() {
        ExpressionsParser parser = getExpressionsParser("index % len");
        ASTMultiplicativeExpression node = parser.parseMultiplicativeExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(PERCENT), ASTMultiplicativeExpression.class, ASTCastExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested multiplicative expressions.
     */
    @Test
    public void testMultiplicativeExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("5 * 6 / 3 % 7");
        ASTMultiplicativeExpression node = parser.parseMultiplicativeExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(PERCENT, SLASH, STAR), ASTMultiplicativeExpression.class, ASTCastExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests parenthesized multiplicative expressions.
     */
    @Test
    public void testMultiplicativeExpressionOfParenthesizedExpressions() {
        ExpressionsParser parser = getExpressionsParser("(x + 1)*(x - 1)");
        ASTMultiplicativeExpression node = parser.parseMultiplicativeExpression();
        checkBinaryLeftAssociative(node, Collections.singletonList(STAR), ASTMultiplicativeExpression.class, ASTCastExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests cast expression of unary expression.
     */
    @Test
    public void testCastExpressionOfUnaryExpression() {
        ExpressionsParser parser = getExpressionsParser("varName");
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
        ExpressionsParser parser = getExpressionsParser("d as Double");
        ASTCastExpression node = parser.parseCastExpression();
        checkBinary(node, AS, ASTUnaryExpression.class, ASTIntersectionType.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested cast expressions.
     */
    @Test
    public void testCastExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("\"2\" as Object as String & Serializable");
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
        ExpressionsParser parser = getExpressionsParser("varName");
        ASTUnaryExpression node = parser.parseUnaryExpression();
        checkSimple(node, ASTPrimary.class);
        node.collapseThenPrint();
    }

    /**
     * Tests unary expression of "-" and unary expression.
     */
    @Test
    public void testUnaryExpressionOfMinusUnary() {
        ExpressionsParser parser = getExpressionsParser("-1");
        ASTUnaryExpression node = parser.parseUnaryExpression();
        checkUnary(node, MINUS, ASTUnaryExpression.class, ASTPrimary.class);
        node.collapseThenPrint();
    }

    /**
     * Tests unary expression of "~" and unary expression.
     */
    @Test
    public void testUnaryExpressionOfComplementUnary() {
        ExpressionsParser parser = getExpressionsParser("~bits");
        ASTUnaryExpression node = parser.parseUnaryExpression();
        checkUnary(node, TILDE, ASTUnaryExpression.class, ASTPrimary.class);
        node.collapseThenPrint();
    }

    /**
     * Tests unary expression of "!" and unary expression.
     */
    @Test
    public void testUnaryExpressionOfLogicalComplementUnary() {
        ExpressionsParser parser = getExpressionsParser("!false");
        ASTUnaryExpression node = parser.parseUnaryExpression();
        checkUnary(node, EXCLAMATION, ASTUnaryExpression.class, ASTPrimary.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested unary expressions.
     */
    @Test
    public void testUnaryExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("~ - ~ - bits");
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
        ExpressionsParser parser = getExpressionsParser("""
            switch testValue {
            case 1 -> throw new TestException();
            case 2 -> { use a + 2; }
            case 3 -> b + 3;
            }""");
        ASTUnaryExpression node = parser.parseUnaryExpression();
        checkSimple(node, ASTSwitchExpression.class, SWITCH);
        node.collapseThenPrint();
    }

    /**
     * Tests Switch Expression.
     */
    @Test
    public void testSwitchExpression() {
        ExpressionsParser parser = getExpressionsParser("""
            switch testValue {
            case 1 -> throw new TestException();
            case 2 -> { use a + 2; }
            case 3 -> b + 3;
            }""");
        ASTSwitchExpression node = parser.parseSwitchExpression();
        checkBinary(node, SWITCH, ASTConditionalExpression.class, ASTSwitchExpressionBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests bad expression for a switch expression.
     */
    @Test
    public void testSwitchExpressionBadExpression() {
        ExpressionsParser parser = getExpressionsParser("""
            switch class {
            case 1 -> throw new TestException();
            case 2 -> { use a + 2; }
            case 3 -> b + 3;
            }""");
        assertThrows(CompileException.class, parser::parseSwitchExpression, "Expected expression.");
    }

    /**
     * Parses a Switch Expression Block.
     */
    @Test
    public void testSwitchExpressionBlock() {
        ExpressionsParser parser = getExpressionsParser("""
            {
            case 1 -> throw new TestException();
            case 2 -> { use a + 2; }
            case 3 -> b + 3;
            }""");
        ASTSwitchExpressionBlock node = parser.parseSwitchExpressionBlock();
        checkSimple(node, ASTSwitchExpressionRules.class);
        node.collapseThenPrint();
    }

    /**
     * Tests no opening brace in switch expression block.
     */
    @Test
    public void testSwitchExpressionBlockNoOpenBrace() {
        ExpressionsParser parser = getExpressionsParser("""
            case 1 -> throw new TestException();
            case 2 -> { use a + 2; }
            case 3 -> b + 3;
            """);
        assertThrows(CompileException.class, parser::parseSwitchExpressionBlock, "Error: Expected '{'.");
    }

    /**
     * Tests no closing brace in switch expression block.
     */
    @Test
    public void testSwitchExpressionBlockNoCloseBrace() {
        ExpressionsParser parser = getExpressionsParser("""
            {
            case 1 -> throw new TestException();
            case 2 -> { use a + 2; }
            case 3 -> b + 3;
            """);
        assertThrows(CompileException.class, parser::parseSwitchExpressionBlock, "Error: Expected '{'.");
    }

    /**
     * Parses Switch Expression Rules.
     */
    @Test
    public void testSwitchExpressionRules() {
        ExpressionsParser parser = getExpressionsParser("""
            case 1 -> throw new TestException();
            case 2 -> { use a + 2; }
            case 3 -> b + 3;
            """);
        ASTSwitchExpressionRules node = parser.parseSwitchExpressionRules();
        checkList(node, null, ASTSwitchExpressionRule.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests Switch Expression Rule of Throw Statement.
     */
    @Test
    public void testSwitchExpressionRuleOfThrowStatement() {
        ExpressionsParser parser = getExpressionsParser("case 1 -> throw new TestException();");
        ASTSwitchExpressionRule node = parser.parseSwitchExpressionRule();
        checkBinary(node, ARROW, ASTSwitchLabel.class, ASTThrowStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests Switch Expression Rule of Block.
     */
    @Test
    public void testSwitchExpressionRuleOfBlock() {
        ExpressionsParser parser = getExpressionsParser("case 1 -> { use a + 1; }");
        ASTSwitchExpressionRule node = parser.parseSwitchExpressionRule();
        checkBinary(node, ARROW, ASTSwitchLabel.class, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests Switch Expression Rule of Expression.
     */
    @Test
    public void testSwitchExpressionRuleOfExpression() {
        ExpressionsParser parser = getExpressionsParser("case 1 -> a + 1;");
        ASTSwitchExpressionRule node = parser.parseSwitchExpressionRule();
        checkBinary(node, ARROW, ASTSwitchLabel.class, ASTExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests bad switch expression rule, no arrow.
     */
    @Test
    public void testSwitchExpressionRuleNoArrow() {
        ExpressionsParser parser = getExpressionsParser("case 1 a + 1;");
        assertThrows(CompileException.class, parser::parseSwitchExpressionRule, "Expected '->'.");
    }

    /**
     * Tests Switch Label of Pattern and Guard.
     */
    @Test
    public void testSwitchLabelOfPatternAndGuard() {
        ExpressionsParser parser = getExpressionsParser("Employee(String name, Double salary) when salary >= 100000");
        ASTSwitchLabel node = parser.parseSwitchLabel();
        checkBinary(node, ASTPattern.class, ASTGuard.class);
        node.collapseThenPrint();
    }

    /**
     * Tests Switch Label of Pattern.
     */
    @Test
    public void testSwitchLabelOfPattern() {
        ExpressionsParser parser = getExpressionsParser("Month(String name)");
        ASTSwitchLabel node = parser.parseSwitchLabel();
        checkSimple(node, ASTPattern.class);
        node.collapseThenPrint();
    }

    /**
     * Tests Switch Label of Case Constants.
     */
    @Test
    public void testSwitchLabelOfCaseConstants() {
        ExpressionsParser parser = getExpressionsParser("case MONDAY, TUESDAY, WEDNESDAY");
        ASTSwitchLabel node = parser.parseSwitchLabel();
        checkSimple(node, ASTCaseConstants.class, CASE);
        node.collapseThenPrint();
    }

    /**
     * Tests Switch Label of Default.
     */
    @Test
    public void testSwitchLabelOfDefault() {
        ExpressionsParser parser = getExpressionsParser("default");
        ASTSwitchLabel node = parser.parseSwitchLabel();
        checkEmpty(node, DEFAULT);
        node.collapseThenPrint();
    }

    /**
     * Tests Case Constants.
     */
    @Test
    public void testCaseConstants() {
        ExpressionsParser parser = getExpressionsParser("RED, GREEN, RED | GREEN");
        ASTCaseConstants node = parser.parseCaseConstants();
        checkList(node, COMMA, ASTConditionalExpression.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests Guard.
     */
    @Test
    public void testGuard() {
        ExpressionsParser parser = getExpressionsParser("when a == b");
        ASTGuard node = parser.parseGuard();
        checkSimple(node, ASTConditionalExpression.class, WHEN);
        node.collapseThenPrint();
    }

    /**
     * Tests Pattern List.
     */
    @Test
    public void testPatternList() {
        ExpressionsParser parser = getExpressionsParser("Widget w, Sprocket s, XrayMachine x");
        ASTPatternList node = parser.parsePatternList();
        checkList(node, COMMA, ASTPattern.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests Pattern of Type Pattern.
     */
    @Test
    public void testPatternOfTypePattern() {
        ExpressionsParser parser = getExpressionsParser("Widget w");
        ASTPattern node = parser.parsePattern();
        checkSimple(node, ASTTypePattern.class);
        node.collapseThenPrint();
    }

    /**
     * Tests Pattern of Record Pattern.
     */
    @Test
    public void testPatternOfRecordPattern() {
        ExpressionsParser parser = getExpressionsParser("Order(Int line, Double amt)");
        ASTPattern node = parser.parsePattern();
        checkSimple(node, ASTRecordPattern.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested Record Patterns.
     */
    @Test
    public void testRecordPatternNested() {
        ExpressionsParser parser = getExpressionsParser("Order(LineItem(Integer id, Double amt))");
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
        ExpressionsParser parser = getExpressionsParser("Person(String first, String last)");
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
        ExpressionsParser parser = getExpressionsParser("mut DataType id");
        ASTTypePattern node = parser.parseTypePattern();
        checkTrinary(node, null, ASTVariableModifierList.class, ASTDataType.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests type pattern without variable modifier.
     */
    @Test
    public void testTypePatternNoVariableModifier() {
        ExpressionsParser parser = getExpressionsParser("DataType id");
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
        ExpressionsParser parser = getExpressionsParser("index");
        ASTArgumentList node = parser.parseArgumentList();
        checkSimple(node, ASTGiveExpression.class, COMMA);
        node.collapseThenPrint();
    }

    /**
     * Tests argument list of nested argument lists (here, just multiple arguments).
     */
    @Test
    public void testArgumentListNested() {
        ExpressionsParser parser = getExpressionsParser("a, 1, b + c");
        ASTArgumentList node = parser.parseArgumentList();
        checkList(node, COMMA, ASTGiveExpression.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests a give expression of an expression.
     */
    @Test
    public void testGiveExpressionNoGive() {
        ExpressionsParser parser = getExpressionsParser("x + 1");
        ASTGiveExpression node = parser.parseGiveExpression();
        checkSimple(node, ASTExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests a give expression of "give" then an expression.
     */
    @Test
    public void testGiveExpressionWithGive() {
        ExpressionsParser parser = getExpressionsParser("give a.b");
        ASTGiveExpression node = parser.parseGiveExpression();
        checkSimple(node, ASTExpression.class, GIVE);
        node.collapseThenPrint();
    }

    /**
     * Tests primary of expression name.
     */
    @Test
    public void testPrimaryOfExpressionName() {
        ExpressionsParser parser = getExpressionsParser("a.b");
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTExpressionName.class);
        node.collapseThenPrint();
    }

    /**
     * Tests primary of literal.
     */
    @Test
    public void testPrimaryOfLiteral() {
        ExpressionsParser parser = getExpressionsParser("3.14");
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTLiteral.class);
        node.collapseThenPrint();
    }

    /**
     * Tests primary of class literal (data type).
     */
    @Test
    public void testPrimaryOfClassLiteralOfDataType() {
        ExpressionsParser parser = getExpressionsParser("spruce.lang.Comparable<String>[][].class");
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
        ExpressionsParser parser = getExpressionsParser("self");
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTSelf.class);
        node.collapseThenPrint();
    }

    /**
     * Tests primary of parenthesized expression.
     */
    @Test
    public void testPrimaryOfParenthesizedExpression() {
        ExpressionsParser parser = getExpressionsParser("(a + b)");
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTExpression.class, OPEN_PARENTHESIS);
        node.collapseThenPrint();
    }

    /**
     * Tests primary of element access.
     */
    @Test
    public void testPrimaryOfElementAccess() {
        ExpressionsParser parser = getExpressionsParser("a[1][2][3]");
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
        ExpressionsParser parser = getExpressionsParser("super.superclassField");
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
        ExpressionsParser parser = getExpressionsParser("EnclosingClass.super.superclassField");
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
        ExpressionsParser parser = getExpressionsParser("method().field");
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
        ExpressionsParser parser = getExpressionsParser("expr.name.methodName()");
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
        ExpressionsParser parser = getExpressionsParser("expr.name.<T>methodName(one)");
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
        ExpressionsParser parser = getExpressionsParser("methodName(helperMethod(i), (a + b), j + 1)");
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
        ExpressionsParser parser = getExpressionsParser("super.<T>inheritedMethod(\"super\")");
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
        ExpressionsParser parser = getExpressionsParser("org.test.EnclosingClass.super.<T>inheritedMethod(\"super\")");
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
        ExpressionsParser parser = getExpressionsParser("new spruce.lang.String[23]");
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTArrayCreationExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests primary of class instance creation expression.
     */
    @Test
    public void testPrimaryOfClassInstanceCreationExpression() {
        ExpressionsParser parser = getExpressionsParser("new Team(25, \"Dodgers\")");
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTClassInstanceCreationExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests primary of qualified class instance creation expression.
     */
    @Test
    public void testPrimaryOfClassInstanceCreationExpressionQualified() {
        ExpressionsParser parser = getExpressionsParser("league.new Team(25, \"Dodgers\")");
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTClassInstanceCreationExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests primary of type name, ".", and self.
     */
    @Test
    public void testPrimaryOfTypeNameDotSelf() {
        ExpressionsParser parser = getExpressionsParser("qualified.type.self");
        ASTPrimary node = parser.parsePrimary();
        checkBinary(node, DOT, ASTTypeName.class, ASTSelf.class);
        node.collapseThenPrint();
    }

    /**
     * Tests primary of method reference starting with "super".
     */
    @Test
    public void testPrimaryOfMethodReferenceSuper() {
        ExpressionsParser parser = getExpressionsParser("super::<String>methodName");
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
        ExpressionsParser parser = getExpressionsParser("spruce.lang.String::new");
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
        ExpressionsParser parser = getExpressionsParser("spruce.lang.String::size");
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
        ExpressionsParser parser = getExpressionsParser("Comparator<String>::compare;");
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
        ExpressionsParser parser = getExpressionsParser("(\"a\" + \"b\")::length;");
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
        ExpressionsParser parser = getExpressionsParser("type.Name.super::length;");
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
        ExpressionsParser parser = getExpressionsParser("new Foo()[i].field1.method1()[j].field2.<T>method2(1).new Bar()");
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
        ExpressionsParser parser = getExpressionsParser("new <String> MyClass()");
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
        ExpressionsParser parser = getExpressionsParser("new MyClass(1, \"one\")");
        ASTClassInstanceCreationExpression node = parser.parseClassInstanceCreationExpression();
        checkSimple(node, ASTUnqualifiedClassInstanceCreationExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests unqualified class instance creation expression of type to instantiate and argument list.
     */
    @Test
    public void testUnqualifiedClassInstanceCreationExpressionOfArgumentList() {
        ExpressionsParser parser = getExpressionsParser("new MyClass(1, \"one\")");
        ASTUnqualifiedClassInstanceCreationExpression node = parser.parseUnqualifiedClassInstanceCreationExpression();
        checkBinary(node, NEW, ASTTypeToInstantiate.class, ASTArgumentList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests type to instantiate of type name.
     */
    @Test
    public void testTypeToInstantiateOfTypeName() {
        ExpressionsParser parser = getExpressionsParser("MyClass");
        ASTTypeToInstantiate node = parser.parseTypeToInstantiate();
        checkSimple(node, ASTTypeName.class);
        node.collapseThenPrint();
    }

    /**
     * Tests type to instantiate of type name and type arguments or diamond.
     */
    @Test
    public void testTypeToInstantiateTypeNameOfTypeArgumentsOrDiamond() {
        ExpressionsParser parser = getExpressionsParser("MyClass<T>");
        ASTTypeToInstantiate node = parser.parseTypeToInstantiate();
        checkBinary(node, ASTTypeName.class, ASTTypeArgumentsOrDiamond.class);
        node.collapseThenPrint();
    }

    /**
     * Tests array creation expression of dim exprs.
     */
    @Test
    public void testArrayCreationExpressionOfDimExprs() {
        ExpressionsParser parser = getExpressionsParser("new String[10]");
        ASTArrayCreationExpression node = parser.parseArrayCreationExpression();
        checkBinary(node, NEW, ASTTypeToInstantiate.class, ASTDimExprs.class);
        node.collapseThenPrint();
    }

    /**
     * Tests array creation expression of dim exprs and dims.
     */
    @Test
    public void testArrayCreationExpressionOfDimExprsDims() {
        ExpressionsParser parser = getExpressionsParser("new String[10][]");
        ASTArrayCreationExpression node = parser.parseArrayCreationExpression();
        checkTrinary(node, NEW, ASTTypeToInstantiate.class, ASTDimExprs.class, ASTDims.class);
        node.collapseThenPrint();
    }

    /**
     * Tests array creation expression of dims and array initializer.
     */
    @Test
    public void testArrayCreationExpressionOfDimsArrayInitializer() {
        ExpressionsParser parser = getExpressionsParser("new String[] {\"one\", \"two\", \"three\"}");
        ASTArrayCreationExpression node = parser.parseArrayCreationExpression();
        checkTrinary(node, NEW, ASTTypeToInstantiate.class, ASTDims.class, ASTArrayInitializer.class);
        node.collapseThenPrint();
    }

    /**
     * Tests dim exprs of dim expr.
     */
    @Test
    public void testDimExprsDimExpr() {
        ExpressionsParser parser = getExpressionsParser("[1][2][3]");
        ASTDimExprs node = parser.parseDimExprs();
        checkList(node, null, ASTDimExpr.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests dim expr of expression.
     */
    @Test
    public void testDimExprOfExpression() {
        ExpressionsParser parser = getExpressionsParser("[x+y]");
        ASTDimExpr node = parser.parseDimExpr();
        checkSimple(node, ASTExpression.class, OPEN_BRACKET);
        node.collapseThenPrint();
    }

    /**
     * Tests array initializer of just empty braces.
     */
    @Test
    public void testArrayInitializerEmpty() {
        ExpressionsParser parser = getExpressionsParser("{}");
        ASTArrayInitializer node = parser.parseArrayInitializer();
        checkEmpty(node, OPEN_BRACE);
        node.collapseThenPrint();
    }

    /**
     * Tests array initializer of a variable initializer list.
     */
    @Test
    public void testArrayInitializerOfVariableInitializerList() {
        ExpressionsParser parser = getExpressionsParser("{x + 1, y - 2}");
        ASTArrayInitializer node = parser.parseArrayInitializer();
        checkSimple(node, ASTVariableInitializerList.class, OPEN_BRACE);
        node.collapseThenPrint();
    }

    /**
     * Tests variable initializer list of variable initializer.
     */
    @Test
    public void testVariableInitializerListOfVariableInitializer() {
        ExpressionsParser parser = getExpressionsParser("i + 1");
        ASTVariableInitializerList node = parser.parseVariableInitializerList();
        checkSimple(node, ASTVariableInitializer.class, COMMA);
        node.collapseThenPrint();
    }

    /**
     * Tests variable initializer list of "," and variable initializer.
     */
    @Test
    public void testVariableInitializerListOfComma() {
        ExpressionsParser parser = getExpressionsParser("x + 1, y - 1");
        ASTVariableInitializerList node = parser.parseVariableInitializerList();
        checkList(node, COMMA, ASTVariableInitializer.class, 2);
        node.collapseThenPrint();
    }

    /**
     * Tests nested variable initializer lists (here, just multiple variable initializers).
     */
    @Test
    public void testVariableInitializerListNested() {
        ExpressionsParser parser = getExpressionsParser("self, count + 1, sumSoFar + value");
        ASTVariableInitializerList node = parser.parseVariableInitializerList();
        checkList(node, COMMA, ASTVariableInitializer.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests variable initializer of expression.
     */
    @Test
    public void testVariableInitializerOfExpression() {
        ExpressionsParser parser = getExpressionsParser("a + b");
        ASTVariableInitializer node = parser.parseVariableInitializer();
        checkSimple(node, ASTExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests variable initializer of array initializer.
     */
    @Test
    public void testVariableInitializerOfArrayInitializer() {
        ExpressionsParser parser = getExpressionsParser("{1, 2, 3}");
        ASTVariableInitializer node = parser.parseVariableInitializer();
        checkSimple(node, ASTArrayInitializer.class);
        node.collapseThenPrint();
    }

    /**
     * Tests a class literal.  Parses a DataType first.
     */
    @Test
    public void testClassLiteral() {
        ExpressionsParser parser = getExpressionsParser("Outer.Inner.class");
        ASTDataType dataType = parser.getTypesParser().parseDataType();
        ASTClassLiteral node = parser.parseClassLiteral(dataType);
        checkSimple(node, ASTDataType.class, CLASS);
    }

    /**
     * Tests "self".
     */
    @Test
    public void testSelf() {
        ExpressionsParser parser = getExpressionsParser("self");
        ASTSelf node = parser.parseSelf();
        checkIs(node, ASTSelf.class);
        node.print();
    }

    /**
     * Tests "super".
     */
    @Test
    public void testSuper() {
        ExpressionsParser parser = getExpressionsParser("super");
        ASTSuper node = parser.parseSuper();
        checkIs(node, ASTSuper.class);
        node.print();
    }

    /**
     * Helper method to get a <code>ExpressionsParser</code> directly from code.
     * @param code The code to test.
     * @return A <code>ExpressionsParser</code> that will parse the given code.
     */
    private static ExpressionsParser getExpressionsParser(String code) {
        return new Parser(new Scanner(code)).getExpressionsParser();
    }
}
