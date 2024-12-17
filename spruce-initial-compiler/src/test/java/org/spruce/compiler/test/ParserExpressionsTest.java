package org.spruce.compiler.test;

import java.util.Arrays;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.classes.ASTFormalParameter;
import org.spruce.compiler.ast.classes.ASTFormalParameterList;
import org.spruce.compiler.ast.expressions.*;
import org.spruce.compiler.ast.literals.*;
import org.spruce.compiler.ast.names.*;
import org.spruce.compiler.ast.statements.ASTBlock;
import org.spruce.compiler.ast.expressions.ASTSwitchLabel;
import org.spruce.compiler.ast.statements.ASTVariableModifierList;
import org.spruce.compiler.ast.types.*;
import org.spruce.compiler.parser.ExpressionsParser;
import org.spruce.compiler.parser.Parser;
import org.spruce.compiler.scanner.Scanner;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.scanner.TokenType;

import static org.spruce.compiler.ast.ASTListNode.Type.*;
import static org.spruce.compiler.ast.expressions.ASTPrimary.Type.*;
import static org.spruce.compiler.scanner.TokenType.*;
import static org.spruce.compiler.test.ParserTestUtility.*;

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
        ensureNoErrors(node, parser);
        assertNotNull(node.getLambdaParameters());
        ASTLambdaExpression nested = ensureIsa(node.getLambdaBody(), ASTLambdaExpression.class);
        assertNotNull(nested.getLambdaParameters());
    }

    /**
     * Tests expression of lambda expression.
     */
    @Test
    public void testExpressionOfLambdaExpression() {
        ExpressionsParser parser = getExpressionsParser("|x, y| -> { use x + y;}");
        ASTExpression node = parser.parseExpression();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTLambdaExpression.class, node);
    }

    /**
     * Tests bad lambda expression.
     */
    @Test
    public void testBadLambdaExpression() {
        ExpressionsParser parser = getExpressionsParser("|x, y| oops()");
        ASTLambdaExpression node = parser.parseLambdaExpression();
        expectError(node, parser);
    }

    /**
     * Tests lambda expression.
     */
    @Test
    public void testLambdaExpression() {
        ExpressionsParser parser = getExpressionsParser("n -> n * 2");
        ASTLambdaExpression node = parser.parseLambdaExpression();
        ensureNoErrors(node, parser);
        assertNotNull(node.getLambdaParameters());
        assertNotNull(node.getLambdaBody());
    }

    /**
     * Tests bad lambda body.
     */
    @Test
    public void testBadLambdaBody() {
        ExpressionsParser parser = getExpressionsParser("class Bad");
        ASTLambdaBody node = parser.parseLambdaBody();
        expectError(node, parser);
    }

    /**
     * Tests lambda body of block.
     */
    @Test
    public void testLambdaBodyOfBlock() {
        ExpressionsParser parser = getExpressionsParser("{ use n * 2; }");
        ASTLambdaBody node = parser.parseLambdaBody();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTBlock.class, node);
    }

    /**
     * Tests lambda body of expression.
     */
    @Test
    public void testLambdaBodyOfExpression() {
        ExpressionsParser parser = getExpressionsParser("n * 2");
        ASTLambdaBody node = parser.parseLambdaBody();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTBinaryExpression.class, node);
    }

    /**
     * Tests bad lambda parameters.
     */
    @Test
    public void testBadLambdaParameters() {
        ExpressionsParser parser = getExpressionsParser("2, 3");
        ASTLambdaParameters node = parser.parseLambdaParameters();
        expectError(node, parser);
    }

    /**
     * Tests lambda parameters of a bare identifier.
     */
    @Test
    public void testLambdaParametersOfIdentifier() {
        ExpressionsParser parser = getExpressionsParser("n");
        ASTLambdaParameters node = parser.parseLambdaParameters();
        ensureNoErrors(node, parser);
        assertFalse(node.getLambdaParams().isPresent());
        assertTrue(node.getIdentifier().isPresent());
        assertInstanceOf(ASTIdentifier.class, node.getIdentifier().get());
    }

    /**
     * Tests lambda parameters of lambda parameter list.
     */
    @Test
    public void testLambdaParametersOfLambdaParameterList() {
        ExpressionsParser parser = getExpressionsParser("|a, b|");
        ASTLambdaParameters node = parser.parseLambdaParameters();
        ensureNoErrors(node, parser);
        assertTrue(node.getLambdaParams().isPresent());
        assertInstanceOf(ASTListNode.class, node.getLambdaParams().get());
        assertFalse(node.getIdentifier().isPresent());
        assertInstanceOf(ASTInferredParameterList.class, node.getLambdaParams().get());
    }

    /**
     * Tests lambda parameters of just a two separated pipes.
     */
    @Test
    public void testLambdaParametersOfTwoPipes() {
        ExpressionsParser parser = getExpressionsParser("| |");
        ASTLambdaParameters node = parser.parseLambdaParameters();
        ensureNoErrors(node, parser);
        assertFalse(node.getLambdaParams().isPresent());
        assertFalse(node.getIdentifier().isPresent());
    }

    /**
     * Tests lambda parameters of just a double-pipe.
     */
    @Test
    public void testLambdaParametersOfDoublePipe() {
        ExpressionsParser parser = getExpressionsParser("||");
        ASTLambdaParameters node = parser.parseLambdaParameters();
        ensureNoErrors(node, parser);
        assertFalse(node.getLambdaParams().isPresent());
        assertFalse(node.getIdentifier().isPresent());
    }

    /**
     * Tests bad lambda parameters, no second pipe.
     */
    @Test
    public void testLambdaParametersNoSecondPipe() {
        ExpressionsParser parser = getExpressionsParser("|a, b");
        ASTLambdaParameters node = parser.parseLambdaParameters();
        expectError(node, parser);
    }

    /**
     * Tests lambda parameter list of formal parameter list.
     */
    @Test
    public void testLambdaParameterListOfFormalParameterList() {
        ExpressionsParser parser = getExpressionsParser("Double x, Double y, Double z");
        ASTLambdaParameterList node = parser.parseLambdaParameterList();
        ensureNoErrors(node, parser);
        ASTFormalParameterList formalParameters = ensureIsa(node, ASTFormalParameterList.class);
        checkList(formalParameters, FORMAL_PARAMETERS, ASTFormalParameter.class, 3);
    }

    /**
     * Tests bad lambda parameter list.
     */
    @Test
    public void testBadLambdaParameterList() {
        ExpressionsParser parser = getExpressionsParser("take badParameter");
        ASTLambdaParameterList node = parser.parseLambdaParameterList();
        expectError(node, parser);
    }

    /**
     * Tests lambda parameter list of inferred parameter list.
     */
    @Test
    public void testLambdaParameterListOfInferredParameterList() {
        ExpressionsParser parser = getExpressionsParser("alpha, beta, gamma");
        ASTLambdaParameterList node = parser.parseLambdaParameterList();
        ensureNoErrors(node, parser);
        ASTInferredParameterList inferredParameters = ensureIsa(node, ASTInferredParameterList.class);
        checkList(inferredParameters, INFERRED_PARAMETERS, ASTIdentifier.class, 3);
    }

    /**
     * Tests bad inferred parameter list.
     */
    @Test
    public void testBadInferredParameterList() {
        ExpressionsParser parser = getExpressionsParser("2");
        ASTInferredParameterList node = parser.parseInferredParameterList();
        expectError(node, parser);
    }

    /**
     * Tests inferred parameter list.
     */
    @Test
    public void testInferredParameterList() {
        ExpressionsParser parser = getExpressionsParser("alpha, beta, gamma");
        ASTInferredParameterList node = parser.parseInferredParameterList();
        ensureNoErrors(node, parser);
        checkList(node, INFERRED_PARAMETERS, ASTIdentifier.class, 3);
    }

    /**
     * Tests expression of other expression.
     */
    @Test
    public void testExpressionOfOtherExpression() {
        ExpressionsParser parser = getExpressionsParser("count == 1");
        ASTExpression node = parser.parseExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, DOUBLE_EQUAL);
    }

    /**
     * Tests expression of conditional expression.
     */
    @Test
    public void testExpressionOfConditionalExpression() {
        ExpressionsParser parser = getExpressionsParser("if a use b else c");
        ASTExpression node = parser.parseExpression();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTIfExpression.class, node);
    }

    /**
     * Tests value expression of logical or expression.
     */
    @Test
    public void testValueExpressionOfLogicalOrExpression() {
        ExpressionsParser parser = getExpressionsParser("a || b");
        ASTValueExpression node = parser.parseValueExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, DOUBLE_PIPE);
    }

    /**
     * Tests if expression.
     */
    @Test
    public void testConditionalExpression() {
        ExpressionsParser parser = getExpressionsParser("if condition use valueIfTrue else valueIfFalse");
        ASTValueExpression node = parser.parseValueExpression();
        ensureNoErrors(node, parser);
        ASTIfExpression ifExpr = ensureIsa(node, ASTIfExpression.class);
        compareClasses(Arrays.asList(ASTPrimary.class, ASTPrimary.class, ASTPrimary.class),
                Arrays.asList(ifExpr.getCondition(), ifExpr.getExprIfTrue(), ifExpr.getExprIfFalse()));
    }

    /**
     * Tests chained if expressions.
     */
    @Test
    public void testIfExpressionChained() {
        ExpressionsParser parser = getExpressionsParser(
                "if a || b use \"one\" else if c || d use \"two\" else if e || f use \"three\" else \"four\"");
        ASTValueExpression node = parser.parseValueExpression();
        ensureNoErrors(node, parser);

        ASTIfExpression outer = ensureIsa(node, ASTIfExpression.class);
        compareClasses(Arrays.asList(ASTBinaryExpression.class, ASTPrimary.class, ASTIfExpression.class),
                Arrays.asList(outer.getCondition(), outer.getExprIfTrue(), outer.getExprIfFalse()));

        ASTIfExpression middle = ensureIsa(outer.getExprIfFalse(), ASTIfExpression.class);
        compareClasses(Arrays.asList(ASTBinaryExpression.class, ASTPrimary.class, ASTIfExpression.class),
                Arrays.asList(middle.getCondition(), middle.getExprIfTrue(), middle.getExprIfFalse()));

        ASTIfExpression inner = ensureIsa(middle.getExprIfFalse(), ASTIfExpression.class);
        compareClasses(Arrays.asList(ASTBinaryExpression.class, ASTPrimary.class, ASTPrimary.class),
                Arrays.asList(inner.getCondition(), inner.getExprIfTrue(), inner.getExprIfFalse()));
    }

    /**
     * Tests bad if expression with question mark but no else.
     */
    @Test
    public void testIfExpressionNoElse() {
        ExpressionsParser parser = getExpressionsParser("if condition use valueIfTrue valueIfFalse");
        ASTValueExpression node = parser.parseValueExpression();
        expectError(node, parser);
    }

    /**
     * Tests bad if expression with question mark but no use.
     */
    @Test
    public void testIfExpressionNoUse() {
        ExpressionsParser parser = getExpressionsParser("if condition valueIfTrue else valueIfFalse");
        ASTValueExpression node = parser.parseValueExpression();
        expectError(node, parser);
    }

    /**
     * Tests logical or expression of logical xor expression.
     */
    @Test
    public void testLogicalOrExpressionOfLogicalAndExpression() {
        ExpressionsParser parser = getExpressionsParser("a ^: b");
        ASTValueExpression node = parser.parseLogicalOrExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, CARET_COLON);
    }

    /**
     * Tests logical or expression of "|:" and logical xor expression.
     */
    @Test
    public void testLogicalOrExpressionOfEager() {
        ExpressionsParser parser = getExpressionsParser("test |: elseThis");
        ASTValueExpression node = parser.parseLogicalOrExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, PIPE_COLON);
    }

    /**
     * Tests logical or expression of "||" and logical xor expression.
     */
    @Test
    public void testLogicalOrExpressionOfConditional() {
        ExpressionsParser parser = getExpressionsParser("alreadyDone || test");
        ASTValueExpression node = parser.parseLogicalOrExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, DOUBLE_PIPE);
    }

    /**
     * Tests nested logical or expressions.
     */
    @Test
    public void testLogicalOrExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("a && b |: c ^: d || e &: f");
        ASTValueExpression node = parser.parseLogicalOrExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class),
                ASTPrimary.class, ASTPrimary.class, DOUBLE_AMPERSAND,
                ASTPrimary.class, ASTPrimary.class, CARET_COLON, PIPE_COLON,
                ASTPrimary.class, ASTPrimary.class, AMPERSAND_COLON, DOUBLE_PIPE
                );
    }

    /**
     * Tests logical xor expression of logical and expression.
     */
    @Test
    public void testLogicalXorExpressionOfLogicalAndExpression() {
        ExpressionsParser parser = getExpressionsParser("a && b");
        ASTValueExpression node = parser.parseLogicalXorExpression();
        ensureNoErrors(node, parser);
        ASTBinaryExpression binary = ensureIsa(node, ASTBinaryExpression.class);
        assertEquals(DOUBLE_AMPERSAND, binary.getOperation());
    }

    /**
     * Tests logical xor expression of "^:" and logical and expression.
     */
    @Test
    public void testLogicalXorExpression() {
        ExpressionsParser parser = getExpressionsParser("test ^: thisAlso");
        ASTValueExpression node = parser.parseLogicalXorExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, CARET_COLON);
    }

    /**
     * Tests nested logical xor expressions.
     */
    @Test
    public void testLogicalXorExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("a && b ^: c &: d ^: e && f");
        ASTValueExpression node = parser.parseLogicalXorExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class),
                ASTPrimary.class, ASTPrimary.class, DOUBLE_AMPERSAND, ASTPrimary.class, ASTPrimary.class, AMPERSAND_COLON, CARET_COLON,
                ASTPrimary.class, ASTPrimary.class, DOUBLE_AMPERSAND, CARET_COLON);
    }

    /**
     * Tests logical and expression of relational expression.
     */
    @Test
    public void testLogicalAndExpressionOfRelationalExpression() {
        ExpressionsParser parser = getExpressionsParser("a == b");
        ASTValueExpression node = parser.parseLogicalAndExpression();
        ensureNoErrors(node, parser);
        ASTBinaryExpression binary = ensureIsa(node, ASTBinaryExpression.class);
        assertEquals(DOUBLE_EQUAL, binary.getOperation());
    }

    /**
     * Tests logical and expression of "&&" and relational expression.
     */
    @Test
    public void testLogicalAndExpressionOfConditional() {
        ExpressionsParser parser = getExpressionsParser("test && notDone");
        ASTValueExpression node = parser.parseLogicalAndExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, DOUBLE_AMPERSAND);
    }

    /**
     * Tests logical and expression of "&:" and relational expression.
     */
    @Test
    public void testLogicalAndExpressionOfEager() {
        ExpressionsParser parser = getExpressionsParser("test &: thisAlso");
        ASTValueExpression node = parser.parseLogicalAndExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, AMPERSAND_COLON);
    }

    /**
     * Tests nested logical and expressions.
     */
    @Test
    public void testLogicalAndExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("a < b &: c <= d && e > f");
        ASTValueExpression node = parser.parseLogicalAndExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class),
                ASTPrimary.class, ASTPrimary.class, LESS_THAN,
                ASTPrimary.class, ASTPrimary.class, LESS_THAN_OR_EQUAL, AMPERSAND_COLON,
                ASTPrimary.class, ASTPrimary.class, GREATER_THAN, DOUBLE_AMPERSAND
        );
    }

    /**
     * Tests relational expression of compare expression.
     */
    @Test
    public void testRelationalExpressionOfCompareExpression() {
        ExpressionsParser parser = getExpressionsParser("a <=> b");
        ASTValueExpression node = parser.parseRelationalExpression();
        ensureNoErrors(node, parser);
        ASTBinaryExpression binary = ensureIsa(node, ASTBinaryExpression.class);
        assertEquals(COMPARISON, binary.getOperation());
    }

    /**
     * Tests relational expression of "&lt;" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfLessThan() {
        ExpressionsParser parser = getExpressionsParser("a.value < b.value");
        ASTValueExpression node = parser.parseRelationalExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, LESS_THAN);
    }

    /**
     * Tests relational expression of "&lt;=" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfLessThanOrEqual() {
        ExpressionsParser parser = getExpressionsParser("2 <= 2");
        ASTValueExpression node = parser.parseRelationalExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, LESS_THAN_OR_EQUAL);
    }

    /**
     * Tests relational expression of "&gt;" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfGreaterThan() {
        ExpressionsParser parser = getExpressionsParser("a.value > b.value");
        ASTValueExpression node = parser.parseRelationalExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, GREATER_THAN);
    }

    /**
     * Tests relational expression of "&gt;=" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfGreaterThanOrEqual() {
        ExpressionsParser parser = getExpressionsParser("2 >= 2");
        ASTValueExpression node = parser.parseRelationalExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, GREATER_THAN_OR_EQUAL);
    }

    /**
     * Tests relational expression of "==" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfEqual() {
        ExpressionsParser parser = getExpressionsParser("test == SUCCESS");
        ASTValueExpression node = parser.parseRelationalExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, DOUBLE_EQUAL);
    }

    /**
     * Tests relational expression of "!=" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfNotEqual() {
        ExpressionsParser parser = getExpressionsParser("test != FAILURE");
        ASTValueExpression node = parser.parseRelationalExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, NOT_EQUAL);
    }

    /**
     * Tests relational expression of "isa" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfIsa() {
        ExpressionsParser parser = getExpressionsParser("node isa ASTBinaryExpression");
        ASTValueExpression node = parser.parseRelationalExpression();
        ensureNoErrors(node, parser);
        ASTIsaExpression isaExpr = ensureIsa(node, ASTIsaExpression.class);
        assertNotNull(isaExpr.getExpr());
        assertNotNull(isaExpr.getIntersectionType());
    }

    /**
     * Tests relational expression of "is" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfIs() {
        ExpressionsParser parser = getExpressionsParser("obj is other");
        ASTValueExpression node = parser.parseRelationalExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, IS);
    }

    /**
     * Tests relational expression of "isnt" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfIsnt() {
        ExpressionsParser parser = getExpressionsParser("obj isnt somethingElse");
        ASTValueExpression node = parser.parseRelationalExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, ISNT);
    }

    /**
     * Tests nested relational expressions.
     */
    @Test
    public void testRelationalExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("a < b <=> c <= d");
        ASTValueExpression node = parser.parseRelationalExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class),
                ASTPrimary.class, ASTPrimary.class, ASTPrimary.class, COMPARISON, LESS_THAN,
                ASTPrimary.class, LESS_THAN_OR_EQUAL
        );
    }

    /**
     * Tests compare expression of bitwise or expression.
     */
    @Test
    public void testCompareExpressionOfBitwiseOrExpression() {
        ExpressionsParser parser = getExpressionsParser("a | b");
        ASTValueExpression node = parser.parseCompareExpression();
        ensureNoErrors(node, parser);
        ASTBinaryExpression binary = ensureIsa(node, ASTBinaryExpression.class);
        assertEquals(PIPE, binary.getOperation());
    }

    /**
     * Tests compare expression of "&lt;=&gt;" and bitwise or expression.
     */
    @Test
    public void testCompareExpression() {
        ExpressionsParser parser = getExpressionsParser("a.value <=> b.value");
        ASTValueExpression node = parser.parseCompareExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, COMPARISON);
    }

    /**
     * Tests bitwise or expression of bitwise xor expression.
     */
    @Test
    public void testBitwiseOrExpressionOfBitwiseXorExpression() {
        ExpressionsParser parser = getExpressionsParser("a ^ b");
        ASTValueExpression node = parser.parseBitwiseOrExpression();
        ensureNoErrors(node, parser);
        ASTBinaryExpression binary = ensureIsa(node, ASTBinaryExpression.class);
        assertEquals(CARET, binary.getOperation());
    }

    /**
     * Tests bitwise or expression of "|" and bitwise xor expression.
     */
    @Test
    public void testBitwiseOrExpression() {
        ExpressionsParser parser = getExpressionsParser("color | blueMask");
        ASTValueExpression node = parser.parseBitwiseOrExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, PIPE);
    }

    /**
     * Tests nested bitwise or expressions.
     */
    @Test
    public void testBitwiseOrExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("red | blue | yellow ^ green");
        ASTValueExpression node = parser.parseBitwiseOrExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class),
                ASTPrimary.class, ASTPrimary.class, PIPE,
                ASTPrimary.class, ASTPrimary.class, CARET, PIPE);
    }

    /**
     * Tests bitwise xor expression of bitwise and expression.
     */
    @Test
    public void testBitwiseXorExpressionOfBitwiseAndExpression() {
        ExpressionsParser parser = getExpressionsParser("a & b");
        ASTValueExpression node = parser.parseBitwiseXorExpression();
        ensureNoErrors(node, parser);
        ASTBinaryExpression binary = ensureIsa(node, ASTBinaryExpression.class);
        assertEquals(AMPERSAND, binary.getOperation());
    }

    /**
     * Tests bitwise xor expression of "^" and bitwise and expression.
     */
    @Test
    public void testBitwiseXorExpression() {
        ExpressionsParser parser = getExpressionsParser("color ^ blueMask");
        ASTValueExpression node = parser.parseBitwiseXorExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, CARET);
    }

    /**
     * Tests nested bitwise xor expressions.
     */
    @Test
    public void testBitwiseXorExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("red ^ blue & yellow ^ green");
        ASTValueExpression node = parser.parseBitwiseXorExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class),
                ASTPrimary.class, ASTPrimary.class, ASTPrimary.class, AMPERSAND, CARET,
                ASTPrimary.class, CARET
        );
    }

    /**
     * Tests bitwise and expression of shift expression.
     */
    @Test
    public void testBitwiseAndExpressionOfShiftExpression() {
        ExpressionsParser parser = getExpressionsParser("a << b");
        ASTValueExpression node = parser.parseBitwiseAndExpression();
        ensureNoErrors(node, parser);
        ASTBinaryExpression binary = ensureIsa(node, ASTBinaryExpression.class);
        assertEquals(SHIFT_LEFT, binary.getOperation());
    }

    /**
     * Tests bitwise and expression of "&" and shift expression.
     */
    @Test
    public void testBitwiseAndExpression() {
        ExpressionsParser parser = getExpressionsParser("color & blueMask");
        ASTValueExpression node = parser.parseBitwiseAndExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, AMPERSAND);
    }

    /**
     * Tests nested bitwise and expressions.
     */
    @Test
    public void testBitwiseAndExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("red + blue & blueGreenMask & greenRedMask");
        ASTValueExpression node = parser.parseBitwiseAndExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class),
                ASTPrimary.class, ASTPrimary.class, PLUS,
                ASTPrimary.class, AMPERSAND, ASTPrimary.class, AMPERSAND
                );
    }

    /**
     * Tests shift expression of additive expression.
     */
    @Test
    public void testShiftExpressionOfAdditiveExpression() {
        ExpressionsParser parser = getExpressionsParser("a + b");
        ASTValueExpression node = parser.parseShiftExpression();
        ensureNoErrors(node, parser);
        ASTBinaryExpression binary = ensureIsa(node, ASTBinaryExpression.class);
        assertEquals(PLUS, binary.getOperation());
    }

    /**
     * Tests shift expression of "<<" and additive expression.
     */
    @Test
    public void testShiftExpressionOfLeftShift() {
        ExpressionsParser parser = getExpressionsParser("1 << 2");
        ASTValueExpression node = parser.parseShiftExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, SHIFT_LEFT);
    }

    /**
     * Tests shift expression of ">>" and additive expression.
     */
    @Test
    public void testShiftExpressionOfRightShift() {
        ExpressionsParser parser = getExpressionsParser("2048 >> 2");
        ASTValueExpression node = parser.parseShiftExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, SHIFT_RIGHT);
    }

    /**
     * Tests nested shift expressions.
     */
    @Test
    public void testShiftExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("-2 << 3 + 4 >> 5 >> 1");
        ASTValueExpression node = parser.parseShiftExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class),
                ASTUnaryExpression.class,
                ASTPrimary.class, ASTPrimary.class, PLUS, SHIFT_LEFT,
                ASTPrimary.class, SHIFT_RIGHT,
                ASTPrimary.class, SHIFT_RIGHT
                );
    }

    /**
     * Tests additive expression of multiplicative expression.
     */
    @Test
    public void testAdditiveExpressionOfMultiplicativeExpression() {
        ExpressionsParser parser = getExpressionsParser("a * b");
        ASTValueExpression node = parser.parseAdditiveExpression();
        ensureNoErrors(node, parser);
        ASTBinaryExpression binary = ensureIsa(node, ASTBinaryExpression.class);
        assertEquals(STAR, binary.getOperation());
    }

    /**
     * Tests additive expression of "+" and multiplicative expression.
     */
    @Test
    public void testAdditiveExpressionOfPlus() {
        ExpressionsParser parser = getExpressionsParser("-1 + 2");
        ASTValueExpression node = parser.parseAdditiveExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class), ASTUnaryExpression.class, ASTPrimary.class, PLUS);
    }

    /**
     * Tests additive expression of "-" and multiplicative expression.
     */
    @Test
    public void testAdditiveExpressionOfMinus() {
        ExpressionsParser parser = getExpressionsParser("finish - start");
        ASTValueExpression node = parser.parseAdditiveExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, MINUS);
    }

    /**
     * Tests nested additive expressions.
     */
    @Test
    public void testAdditiveExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("-2 + 3 * 4 - 5");
        ASTValueExpression node = parser.parseAdditiveExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class),
                ASTUnaryExpression.class, ASTPrimary.class, ASTPrimary.class, STAR, PLUS, ASTPrimary.class, MINUS);
    }

    /**
     * Tests multiplicative expression of unary expression.
     */
    @Test
    public void testMultiplicativeExpressionOfUnaryExpression() {
        ExpressionsParser parser = getExpressionsParser("varName");
        ASTValueExpression node = parser.parseMultiplicativeExpression();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTPrimary.class, node);
    }

    /**
     * Tests multiplicative expression of "*" and unary expression.
     */
    @Test
    public void testMultiplicativeExpressionOfTimes() {
        ExpressionsParser parser = getExpressionsParser("a * b");
        ASTValueExpression node = parser.parseMultiplicativeExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, STAR);
    }

    /**
     * Tests multiplicative expression of "/" and unary expression.
     */
    @Test
    public void testMultiplicativeExpressionOfDivide() {
        ExpressionsParser parser = getExpressionsParser("i / -1");
        ASTValueExpression node = parser.parseMultiplicativeExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTUnaryExpression.class, SLASH);
    }

    /**
     * Tests multiplicative expression of "%" and unary expression.
     */
    @Test
    public void testMultiplicativeExpressionOfModulus() {
        ExpressionsParser parser = getExpressionsParser("index % len");
        ASTValueExpression node = parser.parseMultiplicativeExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, PERCENT);
    }

    /**
     * Tests nested multiplicative expressions.
     */
    @Test
    public void testMultiplicativeExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("5 * 6 / 3 % 7");
        ASTValueExpression node = parser.parseMultiplicativeExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(ensureIsa(node, ASTBinaryExpression.class),
                ASTPrimary.class, ASTPrimary.class, STAR,
                ASTPrimary.class, SLASH, ASTPrimary.class, PERCENT);
    }

    /**
     * Tests parenthesized multiplicative expressions.
     */
    @Test
    public void testMultiplicativeExpressionOfParenthesizedExpressions() {
        ExpressionsParser parser = getExpressionsParser("(x + 1)*(x - 1)");
        ASTValueExpression node = parser.parseMultiplicativeExpression();
        ensureNoErrors(node, parser);
        ASTBinaryExpression mult = ensureIsa(node, ASTBinaryExpression.class);
        checkBinaryPostorder(mult, ASTPrimary.class, ASTPrimary.class, STAR);
        ASTPrimary left = ensureIsa(mult.getFirst(), ASTPrimary.class);
        checkBinaryPostorder(ensureIsa(left.getChild(), ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, PLUS);
        ASTPrimary right = ensureIsa(mult.getSecond(), ASTPrimary.class);
        checkBinaryPostorder(ensureIsa(right.getChild(), ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, MINUS);
    }

    /**
     * Tests cast expression of unary expression.
     */
    @Test
    public void testCastExpressionOfUnaryExpression() {
        ExpressionsParser parser = getExpressionsParser("varName");
        ASTValueExpression node = parser.parseCastExpression();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTPrimary.class, node);
    }

    /**
     * Tests cast expression of unary expression, "as", and an intersection
     * type consisting solely of a data type name.
     */
    @Test
    public void testCastExpressionOfIntersectionType() {
        ExpressionsParser parser = getExpressionsParser("d as Double");
        ASTValueExpression node = parser.parseCastExpression();
        ensureNoErrors(node, parser);
        ASTCastExpression castExpr = ensureIsa(node, ASTCastExpression.class);
        assertNotNull(castExpr.getExpr());
        assertNotNull(castExpr.getIntersectionType());
    }

    /**
     * Tests nested cast expressions.
     */
    @Test
    public void testCastExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("\"2\" as Object as String & Serializable");
        ASTValueExpression node = parser.parseCastExpression();
        ensureNoErrors(node, parser);

        ASTCastExpression castExpr = ensureIsa(node, ASTCastExpression.class);
        assertNotNull(castExpr.getExpr());
        assertNotNull(castExpr.getIntersectionType());

        ASTCastExpression inner = ensureIsa(castExpr.getExpr(), ASTCastExpression.class);
        assertNotNull(inner.getExpr());
        assertNotNull(inner.getIntersectionType());
    }

    /**
     * Tests unary expression of primary.
     */
    @Test
    public void testUnaryExpressionOfPrimary() {
        ExpressionsParser parser = getExpressionsParser("varName");
        ASTValueExpression node = parser.parseUnaryExpression();
        ensureNoErrors(node, parser);
        ASTPrimary primary = ensureIsa(node, ASTPrimary.class);
        assertEquals(EXPR_NAME, primary.getType());
    }

    /**
     * Tests unary expression of "-" and unary expression.
     */
    @Test
    public void testUnaryExpressionOfMinusUnary() {
        ExpressionsParser parser = getExpressionsParser("-1");
        ASTValueExpression node = parser.parseUnaryExpression();
        ensureNoErrors(node, parser);
        checkUnaryExpr(ensureIsa(node, ASTUnaryExpression.class), MINUS, ASTPrimary.class);
    }

    /**
     * Tests unary expression of "~" and unary expression.
     */
    @Test
    public void testUnaryExpressionOfComplementUnary() {
        ExpressionsParser parser = getExpressionsParser("~bits");
        ASTValueExpression node = parser.parseUnaryExpression();
        ensureNoErrors(node, parser);
        checkUnaryExpr(ensureIsa(node, ASTUnaryExpression.class), TILDE, ASTPrimary.class);
    }

    /**
     * Tests unary expression of "!" and unary expression.
     */
    @Test
    public void testUnaryExpressionOfLogicalComplementUnary() {
        ExpressionsParser parser = getExpressionsParser("!false");
        ASTValueExpression node = parser.parseUnaryExpression();
        ensureNoErrors(node, parser);
        checkUnaryExpr(ensureIsa(node, ASTUnaryExpression.class), EXCLAMATION, ASTPrimary.class);
    }

    /**
     * Tests nested unary expressions.
     */
    @Test
    public void testUnaryExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("~ - ~ - bits");
        ASTValueExpression node = parser.parseUnaryExpression();
        ensureNoErrors(node, parser);
        ASTUnaryExpression unaryExpr = ensureIsa(node, ASTUnaryExpression.class);
        checkUnaryExpr(unaryExpr, TILDE, ASTUnaryExpression.class, ASTUnaryExpression.class);

        ASTUnaryExpression childNode = ensureIsa(unaryExpr.getFirst(), ASTUnaryExpression.class);
        checkUnaryExpr(childNode, MINUS, ASTUnaryExpression.class, ASTUnaryExpression.class);

        childNode = ensureIsa(childNode.getFirst(), ASTUnaryExpression.class);
        checkUnaryExpr(childNode, TILDE, ASTUnaryExpression.class, ASTPrimary.class);

        childNode = ensureIsa(childNode.getFirst(), ASTUnaryExpression.class);
        assertEquals(MINUS, childNode.getOperation());
        ASTValueExpression child = childNode.getFirst();
        assertInstanceOf(ASTPrimary.class, child);
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
        ASTValueExpression node = parser.parseUnaryExpression();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTSwitchExpression.class, node);
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
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTPrimary.class, node.getCondExpr());
        ASTSwitchExpressionRules switchExprRules = node.getSwitchExprRules();
        checkList(switchExprRules, SWITCH_EXPR_RULES, ASTSwitchExpressionRule.class, 3);
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
        ASTSwitchExpression node = parser.parseSwitchExpression();
        expectError(node, parser);
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
        ASTSwitchExpressionRules node = parser.parseSwitchExpressionBlock();
        ensureNoErrors(node, parser);
        checkList(node, SWITCH_EXPR_RULES, ASTSwitchExpressionRule.class, 3);
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
            }
            """);
        ASTSwitchExpressionRules node = parser.parseSwitchExpressionBlock();
        expectError(node, parser);
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
        ASTSwitchExpressionRules node = parser.parseSwitchExpressionBlock();
        expectError(node, parser);
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
        ensureNoErrors(node, parser);
        checkList(node, SWITCH_EXPR_RULES, ASTSwitchExpressionRule.class, 3);
    }

    /**
     * Tests Switch Expression Rule of Throw Statement.
     */
    @Test
    public void testSwitchExpressionRuleOfThrowStatement() {
        ExpressionsParser parser = getExpressionsParser("case 1 -> throw new TestException();");
        ASTSwitchExpressionRule node = parser.parseSwitchExpressionRule();
        ensureNoErrors(node, parser);
        assertNotNull(node.getSwitchLabel());
        assertFalse(node.getExpr().isPresent());
        assertFalse(node.getBlock().isPresent());
        assertTrue(node.getThrowStmt().isPresent());
    }

    /**
     * Tests Switch Expression Rule of Block.
     */
    @Test
    public void testSwitchExpressionRuleOfBlock() {
        ExpressionsParser parser = getExpressionsParser("case 1 -> { use a + 1; }");
        ASTSwitchExpressionRule node = parser.parseSwitchExpressionRule();
        ensureNoErrors(node, parser);
        assertNotNull(node.getSwitchLabel());
        assertFalse(node.getExpr().isPresent());
        assertFalse(node.getThrowStmt().isPresent());
        assertTrue(node.getBlock().isPresent());
    }

    /**
     * Tests Switch Expression Rule of Expression.
     */
    @Test
    public void testSwitchExpressionRuleOfExpression() {
        ExpressionsParser parser = getExpressionsParser("case 1 -> a + 1;");
        ASTSwitchExpressionRule node = parser.parseSwitchExpressionRule();
        ensureNoErrors(node, parser);
        assertNotNull(node.getSwitchLabel());
        assertFalse(node.getThrowStmt().isPresent());
        assertFalse(node.getBlock().isPresent());
        assertTrue(node.getExpr().isPresent());
    }

    /**
     * Tests bad switch expression rule, no arrow.
     */
    @Test
    public void testSwitchExpressionRuleNoArrow() {
        ExpressionsParser parser = getExpressionsParser("case 1 a + 1;");
        ASTSwitchExpressionRule node = parser.parseSwitchExpressionRule();
        expectError(node, parser);
    }

    /**
     * Tests bad switch expression rule, no semicolon after expression.
     */
    @Test
    public void testSwitchExpressionRuleOfExpressionNoSemicolon() {
        ExpressionsParser parser = getExpressionsParser("""
                case 1 -> a + 1
                case 2 -> b + 2
                """);
        ASTSwitchExpressionRule node = parser.parseSwitchExpressionRule();
        expectError(node, parser);
    }

    /**
     * Tests Switch Label of Pattern and Guard.
     */
    @Test
    public void testSwitchLabelOfPatternAndGuard() {
        ExpressionsParser parser = getExpressionsParser("Employee(String name, Double salary) when salary >= 100000");
        ASTSwitchLabel node = parser.parseSwitchLabel();
        ensureNoErrors(node, parser);
        assertTrue(node.getPattern().isPresent());
        assertFalse(node.getCaseConstants().isPresent());
        assertInstanceOf(ASTRecordPattern.class, node.getPattern().get());
        assertTrue(node.getGuard().isPresent());
        assertFalse(node.getKeyword().isPresent());
    }

    /**
     * Tests Switch Label of Pattern.
     */
    @Test
    public void testSwitchLabelOfPattern() {
        ExpressionsParser parser = getExpressionsParser("Month(String name)");
        ASTSwitchLabel node = parser.parseSwitchLabel();
        ensureNoErrors(node, parser);
        assertTrue(node.getPattern().isPresent());
        assertFalse(node.getCaseConstants().isPresent());
        assertInstanceOf(ASTRecordPattern.class, node.getPattern().get());
        assertFalse(node.getGuard().isPresent());
        assertFalse(node.getKeyword().isPresent());
    }

    /**
     * Tests Switch Label of Case Constants.
     */
    @Test
    public void testSwitchLabelOfCaseConstants() {
        ExpressionsParser parser = getExpressionsParser("case MONDAY, TUESDAY, WEDNESDAY");
        ASTSwitchLabel node = parser.parseSwitchLabel();
        ensureNoErrors(node, parser);
        assertFalse(node.getPattern().isPresent());
        assertTrue(node.getCaseConstants().isPresent());
        ASTCaseConstants caseConstants = ensureIsa(node.getCaseConstants().get(), ASTCaseConstants.class);
        checkList(caseConstants, CASE_CONSTANTS, ASTValueExpression.class, 3);
        assertFalse(node.getGuard().isPresent());
        assertTrue(node.getKeyword().isPresent());
        assertEquals(CASE, node.getKeyword().get().getKeyword());
    }



    /**
     * Tests Switch Label of other Case Constants.
     */
    @Test
    public void testSwitchLabelOfOtherCaseConstants() {
        ExpressionsParser parser = getExpressionsParser("case -1, 0, 1");
        ASTSwitchLabel node = parser.parseSwitchLabel();
        ensureNoErrors(node, parser);
        assertFalse(node.getPattern().isPresent());
        assertTrue(node.getCaseConstants().isPresent());
        ASTCaseConstants caseConstants = ensureIsa(node.getCaseConstants().get(), ASTCaseConstants.class);
        checkList(caseConstants, CASE_CONSTANTS, ASTValueExpression.class, 3);
        assertFalse(node.getGuard().isPresent());
        assertTrue(node.getKeyword().isPresent());
        assertEquals(CASE, node.getKeyword().get().getKeyword());
    }

    /**
     * Tests Switch Label of Default.
     */
    @Test
    public void testSwitchLabelOfDefault() {
        ExpressionsParser parser = getExpressionsParser("default");
        ASTSwitchLabel node = parser.parseSwitchLabel();
        ensureNoErrors(node, parser);
        assertFalse(node.getPattern().isPresent());
        assertFalse(node.getCaseConstants().isPresent());
        assertFalse(node.getGuard().isPresent());
        assertTrue(node.getKeyword().isPresent());
        assertEquals(DEFAULT, node.getKeyword().get().getKeyword());
    }

    /**
     * Tests Case Constants.
     */
    @Test
    public void testCaseConstants() {
        ExpressionsParser parser = getExpressionsParser("RED, GREEN, RED | GREEN");
        ASTCaseConstants node = parser.parseCaseConstants();
        ensureNoErrors(node, parser);
        checkList(node, CASE_CONSTANTS, ASTValueExpression.class, 3);
    }

    /**
     * Tests Guard.
     */
    @Test
    public void testGuard() {
        ExpressionsParser parser = getExpressionsParser("when a == b");
        ASTGuard node = parser.parseGuard();
        ensureNoErrors(node, parser);
        assertNotNull(node.getExpr());
    }

    /**
     * Tests Pattern List.
     */
    @Test
    public void testPatternList() {
        ExpressionsParser parser = getExpressionsParser("Widget w, Sprocket s, XrayMachine x");
        ASTPatternList node = parser.parsePatternList();
        ensureNoErrors(node, parser);
        checkList(node, PATTERNS, ASTPattern.class, 3);
    }

    /**
     * Tests Pattern of Type Pattern.
     */
    @Test
    public void testPatternOfTypePattern() {
        ExpressionsParser parser = getExpressionsParser("Widget w");
        ASTPattern node = parser.parsePattern();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTTypePattern.class, node);
    }

    /**
     * Tests Pattern of Record Pattern.
     */
    @Test
    public void testPatternOfRecordPattern() {
        ExpressionsParser parser = getExpressionsParser("Order(Int line, Double amt)");
        ASTPattern node = parser.parsePattern();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTRecordPattern.class, node);
    }

    /**
     * Tests bad pattern.
     */
    @Test
    public void testPatternBad() {
        ExpressionsParser parser = getExpressionsParser("6.76e-11");
        ASTPattern node = parser.parsePattern();
        expectError(node, parser, 2);
    }

    /**
     * Tests nested Record Patterns.
     */
    @Test
    public void testRecordPatternNested() {
        ExpressionsParser parser = getExpressionsParser("Order(LineItem(Integer id, Double amt))");
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTRecordPattern node = parser.parseRecordPattern(dt);
        ensureNoErrors(node, parser);

        assertInstanceOf(ASTDataType.class, node.getDataType());
        assertTrue(node.getPatternList().isPresent());

        ASTPatternList patternList = node.getPatternList().get();
        checkList(patternList, PATTERNS, ASTPattern.class, 1);

        ASTPattern innerPattern = patternList.getTypedChildren().get(0);
        ASTRecordPattern innerRp = ensureIsa(innerPattern, ASTRecordPattern.class);
        assertInstanceOf(ASTDataType.class, innerRp.getDataType());
        assertTrue(innerRp.getPatternList().isPresent());

        ASTPatternList nested = innerRp.getPatternList().get();
        checkList(nested, PATTERNS, ASTPattern.class, 2);
    }

    /**
     * Tests record pattern.
     */
    @Test
    public void testRecordPattern() {
        ExpressionsParser parser = getExpressionsParser("Person(String first, String last)");
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTRecordPattern node = parser.parseRecordPattern(dt);
        ensureNoErrors(node, parser);

        assertInstanceOf(ASTDataType.class, node.getDataType());
        assertTrue(node.getPatternList().isPresent());
        ASTPatternList patternList = node.getPatternList().get();
        checkList(patternList, PATTERNS, ASTPattern.class, 2);
    }

    /**
     * Tests bad record pattern of no open paren.
     */
    @Test
    public void testRecordPatternNoOpenParen() {
        ExpressionsParser parser = getExpressionsParser("Person String first, String last)");
        ASTSwitchExpressionRule node = parser.parseSwitchExpressionRule();
        expectError(node, parser, 2);
    }

    /**
     * Tests bad record pattern of no close paren.
     */
    @Test
    public void testRecordPatternNoCloseParen() {
        ExpressionsParser parser = getExpressionsParser("Person(String first, String last;");
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTRecordPattern node = parser.parseRecordPattern(dt);
        expectError(node, parser);
    }

    /**
     * Tests type pattern with variable modifier.
     */
    @Test
    public void testTypePattern() {
        ExpressionsParser parser = getExpressionsParser("mut DataType id");
        ASTTypePattern node = parser.parseTypePattern();
        ensureNoErrors(node, parser);

        assertTrue(node.getVarModList().isPresent());
        ASTVariableModifierList varModList = node.getVarModList().get();
        checkList(varModList, VARIABLE_MODIFIERS, ASTKeywordNode.class, 1);
        ASTKeywordNode mutKeyword = ensureIsa(varModList.getChildren().get(0), ASTKeywordNode.class);
        assertEquals(MUT, mutKeyword.getKeyword());
        assertInstanceOf(ASTDataType.class, node.getDataType());
        assertInstanceOf(ASTIdentifier.class, node.getIdentifier());
    }

    /**
     * Tests type pattern without variable modifier.
     */
    @Test
    public void testTypePatternNoVariableModifier() {
        ExpressionsParser parser = getExpressionsParser("DataType id");
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTTypePattern node = parser.parseTypePattern(dt);
        ensureNoErrors(node, parser);
        assertFalse(node.getVarModList().isPresent());
        assertInstanceOf(ASTDataType.class, node.getDataType());
        assertInstanceOf(ASTIdentifier.class, node.getIdentifier());
    }

    /**
     * Tests argument list of expression.
     */
    @Test
    public void testArgumentListOfExpression() {
        ExpressionsParser parser = getExpressionsParser("index");
        ASTArgumentList node = parser.parseArgumentList();
        ensureNoErrors(node, parser);
        checkList(node, ARGUMENTS, ASTGiveExpression.class, 1);
    }

    /**
     * Tests argument list of multiple arguments.
     */
    @Test
    public void testArgumentListMultiple() {
        ExpressionsParser parser = getExpressionsParser("a, 1, b + c");
        ASTArgumentList node = parser.parseArgumentList();
        ensureNoErrors(node, parser);
        checkList(node, ARGUMENTS, ASTGiveExpression.class, 3);
    }

    /**
     * Tests a give expression of an expression.
     */
    @Test
    public void testGiveExpressionNoGive() {
        ExpressionsParser parser = getExpressionsParser("x + 1");
        ASTGiveExpression node = parser.parseGiveExpression();
        ensureNoErrors(node, parser);
        assertFalse(node.getGiveKeyword().isPresent());
        assertNotNull(node.getExpr());
    }

    /**
     * Tests a give expression of "give" then an expression.
     */
    @Test
    public void testGiveExpressionWithGive() {
        ExpressionsParser parser = getExpressionsParser("give a.b");
        ASTGiveExpression node = parser.parseGiveExpression();
        ensureNoErrors(node, parser);
        assertTrue(node.getGiveKeyword().isPresent());
        assertEquals(GIVE, node.getGiveKeyword().get().getKeyword());
        assertNotNull(node.getExpr());
    }

    /**
     * Tests primary of expression name.
     */
    @Test
    public void testPrimaryOfExpressionName() {
        ExpressionsParser parser = getExpressionsParser("a.b");
        ASTPrimary node = parser.parsePrimary();
        ensureNoErrors(node, parser);
        checkPrimary(node, EXPR_NAME, ASTListNode.class);
    }

    /**
     * Tests primary of literal.
     */
    @Test
    public void testPrimaryOfLiteral() {
        ExpressionsParser parser = getExpressionsParser("3.14");
        ASTPrimary node = parser.parsePrimary();
        ensureNoErrors(node, parser);
        checkPrimary(node, LITERAL, ASTLiteral.class);
    }

    /**
     * Tests primary of class literal (data type).
     */
    @Test
    public void testPrimaryOfClassLiteralOfDataType() {
        ExpressionsParser parser = getExpressionsParser("spruce.lang.Comparable<String>[][].class");
        ASTPrimary node = parser.parsePrimary();
        ensureNoErrors(node, parser);
        checkPrimary(node, ASTPrimary.Type.CLASS_LITERAL, ASTClassLiteral.class);
        ASTClassLiteral classLiteral = ensureIsa(node.getChild(), ASTClassLiteral.class);
        assertNotNull(classLiteral.getDataType());
    }

    /**
     * Tests primary of "self".
     */
    @Test
    public void testPrimaryOfSelf() {
        ExpressionsParser parser = getExpressionsParser("self");
        ASTPrimary node = parser.parsePrimary();
        ensureNoErrors(node, parser);
        checkPrimary(node, ASTPrimary.Type.SELF, ASTKeywordNode.class);
        ASTKeywordNode self = ensureIsa(node.getChild(), ASTKeywordNode.class);
        assertEquals(TokenType.SELF, self.getKeyword());
    }

    /**
     * Tests primary of parenthesized expression.
     */
    @Test
    public void testPrimaryOfParenthesizedExpression() {
        ExpressionsParser parser = getExpressionsParser("(a + b)");
        ASTPrimary node = parser.parsePrimary();
        ensureNoErrors(node, parser);
        checkPrimary(node, PAREN_EXPR, ASTBinaryExpression.class);
    }

    /**
     * Tests primary of element access.
     */
    @Test
    public void testPrimaryOfElementAccess() {
        ExpressionsParser parser = getExpressionsParser("a[1][2][3]");
        ASTPrimary node = parser.parsePrimary();
        ensureNoErrors(node, parser);
        checkPrimary(node, ASTPrimary.Type.ELEMENT_ACCESS, ASTElementAccess.class);
        ASTElementAccess outer = ensureIsa(node.getChild(), ASTElementAccess.class);
        assertNotNull(outer.getIndexExpr());
        assertTrue(outer.getElementAccess().isPresent());
        ASTElementAccess middle = ensureIsa(outer.getElementAccess().get(), ASTElementAccess.class);
        assertNotNull(middle.getIndexExpr());
        assertTrue(middle.getElementAccess().isPresent());
        ASTElementAccess inner = ensureIsa(middle.getElementAccess().get(), ASTElementAccess.class);
        assertNotNull(inner.getIndexExpr());
        assertTrue(inner.getPrimary().isPresent());
    }

    /**
     * Tests primary of field access of super.
     */
    @Test
    public void testPrimaryOfFieldAccessOfSuper() {
        ExpressionsParser parser = getExpressionsParser("super.superclassField");
        ASTPrimary node = parser.parsePrimary();
        ensureNoErrors(node, parser);
        checkPrimary(node, FIELD_ACCESS, ASTFieldAccess.class);
        ASTFieldAccess fa = ensureIsa(node.getChild(), ASTFieldAccess.class);
        checkFieldAccess(fa, false, true, false);
    }

    /**
     * Tests primary of field access of type name and super.
     */
    @Test
    public void testPrimaryOfFieldAccessOfTypeNameSuper() {
        ExpressionsParser parser = getExpressionsParser("EnclosingClass.super.superclassField");
        ASTPrimary node = parser.parsePrimary();
        ensureNoErrors(node, parser);
        checkPrimary(node, FIELD_ACCESS, ASTFieldAccess.class);
        ASTFieldAccess fa = ensureIsa(node.getChild(), ASTFieldAccess.class);
        checkFieldAccess(fa, true, true, false);
    }

    /**
     * Tests primary of field access of primary.
     */
    @Test
    public void testPrimaryOfFieldAccessOfPrimary() {
        ExpressionsParser parser = getExpressionsParser("method().field");
        ASTPrimary node = parser.parsePrimary();
        ensureNoErrors(node, parser);
        checkPrimary(node, FIELD_ACCESS, ASTFieldAccess.class);
        ASTFieldAccess fa = ensureIsa(node.getChild(), ASTFieldAccess.class);
        checkFieldAccess(fa, false, false, true);
    }

    /**
     * Tests primary of method invocation, expression name.
     */
    @Test
    public void testPrimaryOfMethodInvocationOfExpressionName() {
        ExpressionsParser parser = getExpressionsParser("expr.name.methodName()");
        ASTPrimary node = parser.parsePrimary();
        ensureNoErrors(node, parser);

        checkPrimary(node, METHOD_INVOCATION, ASTMethodInvocation.class);
        ASTMethodInvocation mi = ensureIsa(node.getChild(), ASTMethodInvocation.class);
        checkMethodInvocation(mi, false, false, true, 
                false, false);
        ASTExpressionName exprName = mi.getExprName().orElseThrow();
        checkList(exprName, EXPR_NAME_IDS, ASTIdentifier.class, 2);
    }

    /**
     * Tests bad method invocation, no close paren.
     */
    @Test
    public void testMethodInvocationNoCloseParen() {
        ExpressionsParser parser = getExpressionsParser("methodName(a class");
        ASTIdentifier identifier = parser.getNamesParser().parseIdentifier();
        ASTMethodInvocation node = parser.parseMethodInvocation(identifier);
        expectError(node, parser);
    }

    /**
     * Tests bad method invocation, no open paren.
     */
    @Test
    public void testMethodInvocationNoOpenParen() {
        ExpressionsParser parser = getExpressionsParser("super.methodName arg)");
        ASTPrimary node = parser.parsePrimary();
        checkPrimary(node, FIELD_ACCESS, ASTFieldAccess.class);
    }

    /**
     * Tests primary of method invocation, expression name and type arguments.
     */
    @Test
    public void testPrimaryOfMethodInvocationOfExpressionNameTypeArguments() {
        ExpressionsParser parser = getExpressionsParser("expr.name.<T>methodName(one)");
        ASTPrimary node = parser.parsePrimary();
        ensureNoErrors(node, parser);

        checkPrimary(node, METHOD_INVOCATION, ASTMethodInvocation.class);
        ASTMethodInvocation mi = ensureIsa(node.getChild(), ASTMethodInvocation.class);
        checkMethodInvocation(mi, false, false, true,
                false, true);
        ASTIdentifier methodName = mi.getIdentifier();
        assertEquals("methodName", methodName.getValue());
    }

    /**
     * Tests primary of method invocation, simple name.
     */
    @Test
    public void testPrimaryOfMethodInvocationOfSimpleName() {
        ExpressionsParser parser = getExpressionsParser("methodName(helperMethod(i), (a + b), j + 1)");
        ASTPrimary node = parser.parsePrimary();
        ensureNoErrors(node, parser);
        checkPrimary(node, METHOD_INVOCATION, ASTMethodInvocation.class);
        ASTMethodInvocation mi = ensureIsa(node.getChild(), ASTMethodInvocation.class);
        checkMethodInvocation(mi, false, false, false,
                false, false);
        ensureNoErrors(node, parser);
    }

    /**
     * Tests primary of method invocation starting with <code>super</code>.
     */
    @Test
    public void testPrimaryOfMethodInvocationOfSuper() {
        ExpressionsParser parser = getExpressionsParser("super.<T>inheritedMethod(\"super\")");
        ASTPrimary node = parser.parsePrimary();
        ensureNoErrors(node, parser);
        checkPrimary(node, METHOD_INVOCATION, ASTMethodInvocation.class);
        ASTMethodInvocation mi = ensureIsa(node.getChild(), ASTMethodInvocation.class);
        checkMethodInvocation(mi, false, true, false,
                false, true);
        ASTIdentifier methodName = mi.getIdentifier();
        assertEquals("inheritedMethod", methodName.getValue());
    }

    /**
     * Tests primary of method invocation starting with <code>super</code>.
     */
    @Test
    public void testPrimaryOfMethodInvocationOfTypeNameSuper() {
        ExpressionsParser parser = getExpressionsParser("org.test.EnclosingClass.super.<T>inheritedMethod(\"super\")");
        ASTPrimary node = parser.parsePrimary();
        ensureNoErrors(node, parser);
        checkPrimary(node, METHOD_INVOCATION, ASTMethodInvocation.class);
        ASTMethodInvocation mi = ensureIsa(node.getChild(), ASTMethodInvocation.class);
        checkMethodInvocation(mi, true, true, false,
                false, true);
        ASTIdentifier methodName = mi.getIdentifier();
        assertEquals("inheritedMethod", methodName.getValue());
    }

    /**
     * Tests primary of array creation expression.
     */
    @Test
    public void testPrimaryOfArrayCreationExpression() {
        ExpressionsParser parser = getExpressionsParser("new spruce.lang.String[23]");
        ASTPrimary node = parser.parsePrimary();
        ensureNoErrors(node, parser);
        checkPrimary(node, ARRAY_CREATION_EXPR, ASTArrayCreationExpression.class);
    }

    /**
     * Tests primary of class instance creation expression.
     */
    @Test
    public void testPrimaryOfClassInstanceCreationExpression() {
        ExpressionsParser parser = getExpressionsParser("new Team(25, \"Dodgers\")");
        ASTPrimary node = parser.parsePrimary();
        ensureNoErrors(node, parser);
        checkPrimary(node, ASTPrimary.Type.CLASS_INSTANCE_CREATION_EXPR, ASTClassInstanceCreationExpression.class);
    }

    /**
     * Tests primary of qualified class instance creation expression.
     */
    @Test
    public void testPrimaryOfClassInstanceCreationExpressionQualified() {
        ExpressionsParser parser = getExpressionsParser("league.new Team(25, \"Dodgers\")");
        ASTPrimary node = parser.parsePrimary();
        ensureNoErrors(node, parser);
        checkPrimary(node, ASTPrimary.Type.CLASS_INSTANCE_CREATION_EXPR, ASTClassInstanceCreationExpression.class);
    }

    /**
     * Tests primary of class instance creation expression with type arguments
     * (for a generic constructor).
     */
    @Test
    public void testPrimaryOfClassInstanceCreationExpressionTypeArgs() {
        ExpressionsParser parser = getExpressionsParser("new <String>Foo(\"Bar\")");
        ASTPrimary node = parser.parsePrimary();
        ensureNoErrors(node, parser);
        checkPrimary(node, CLASS_INSTANCE_CREATION_EXPR, ASTClassInstanceCreationExpression.class);
    }

    /**
     * Tests primary of type name, ".", and self.
     */
    @Test
    public void testPrimaryOfTypeNameDotSelf() {
        ExpressionsParser parser = getExpressionsParser("qualified.type.self");
        ASTPrimary node = parser.parsePrimary();
        ensureNoErrors(node, parser);
        checkPrimary(node, ASTPrimary.Type.TYPENAME_SELF, ASTTypenameSelf.class);
        ASTTypenameSelf typeNameSelf = ensureIsa(node.getChild(), ASTTypenameSelf.class);
        assertNotNull(typeNameSelf.getTypename());
        ASTKeywordNode self = ensureIsa(typeNameSelf.getSelfKeyword(), ASTKeywordNode.class);
        assertEquals(TokenType.SELF, self.getKeyword());
    }

    /**
     * Tests primary of method reference starting with "super".
     */
    @Test
    public void testPrimaryOfMethodReferenceSuper() {
        ExpressionsParser parser = getExpressionsParser("super::<String>methodName");
        ASTPrimary node = parser.parsePrimary();
        ensureNoErrors(node, parser);
        checkPrimary(node, METHOD_REFERENCE, ASTMethodReference.class);
        ASTMethodReference mRef = ensureIsa(node.getChild(), ASTMethodReference.class);
        checkMethodReference(mRef, false, true, false, 
                false,false, true);
    }

    /**
     * Tests Primary of Method Reference of "new".
     */
    @Test
    public void testPrimaryOfConstructorReference() {
        ExpressionsParser parser = getExpressionsParser("spruce.lang.String::new");
        ASTPrimary node = parser.parsePrimary();
        ensureNoErrors(node, parser);

        checkPrimary(node, METHOD_REFERENCE, ASTMethodReference.class);
        ASTMethodReference mRef = ensureIsa(node.getChild(), ASTMethodReference.class);
        checkMethodReference(mRef, false, false, false,
                true,false, false);
        ASTDataType dt = mRef.getDataType().orElseThrow();
        assertInstanceOf(ASTDataType.class, dt);
    }

    /**
     * Tests Primary of Method Reference of Expression Name.
     */
    @Test
    public void testPrimaryOfMethodReferenceOfExpressionName() {
        ExpressionsParser parser = getExpressionsParser("spruce.lang.String::size");
        ASTPrimary node = parser.parsePrimary();
        ensureNoErrors(node, parser);
        checkPrimary(node, METHOD_REFERENCE, ASTMethodReference.class);
        ASTMethodReference mRef = ensureIsa(node.getChild(), ASTMethodReference.class);
        checkMethodReference(mRef, false, false, true,
                false,false, false);
    }

    /**
     * Tests Primary of Method Reference of DataType.
     */
    @Test
    public void testPrimaryOfMethodReferenceOfDataType() {
        ExpressionsParser parser = getExpressionsParser("Comparator<String>::compare;");
        ASTPrimary node = parser.parsePrimary();
        ensureNoErrors(node, parser);
        checkPrimary(node, METHOD_REFERENCE, ASTMethodReference.class);
        ASTMethodReference mRef = ensureIsa(node.getChild(), ASTMethodReference.class);
        checkMethodReference(mRef, false, false, false,
                true,false, false);
    }

    /**
     * Tests Primary of Method Reference of Primary.
     */
    @Test
    public void testPrimaryOfMethodReferenceOfPrimary() {
        ExpressionsParser parser = getExpressionsParser("(\"a\" + \"b\")::length;");
        ASTPrimary node = parser.parsePrimary();
        ensureNoErrors(node, parser);
        checkPrimary(node, METHOD_REFERENCE, ASTMethodReference.class);
        ASTMethodReference mRef = ensureIsa(node.getChild(), ASTMethodReference.class);
        checkMethodReference(mRef, false, false, false,
                false,true, false);
    }

    /**
     * Tests Primary of Method Reference of TypeName and super.
     */
    @Test
    public void testPrimaryOfMethodReferenceOfTypeNameSuper() {
        ExpressionsParser parser = getExpressionsParser("type.Name.super::length;");
        ASTPrimary node = parser.parsePrimary();
        ensureNoErrors(node, parser);
        checkPrimary(node, METHOD_REFERENCE, ASTMethodReference.class);
        ASTMethodReference mRef = ensureIsa(node.getChild(), ASTMethodReference.class);
        checkMethodReference(mRef, true, true, false,
                false,false, false);
    }

    /**
     * Tests bad method reference of bad referent.
     */
    @Test
    public void testMethodReferenceBadReferent() {
        ExpressionsParser parser = getExpressionsParser("type.Name::for");
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTMethodReference node = parser.parseMethodReference(dt);
        expectError(node, parser);
    }

    /**
     * Tests nested primary expressions, including Class Instance Creation
     * Expressions, Method Invocations, Field Accesses, and Element Accesses.
     */
    @Test
    public void testPrimaryOfNested() {
        ExpressionsParser parser = getExpressionsParser("new Foo()[i].field1.method1()[j].field2.<T>method2(1).new Bar()");
        ASTPrimary node = parser.parsePrimary();
        ensureNoErrors(node, parser);

        checkPrimary(node, ASTPrimary.Type.CLASS_INSTANCE_CREATION_EXPR, ASTClassInstanceCreationExpression.class);
        ASTClassInstanceCreationExpression outerCice = ensureIsa(node.getChild(), ASTClassInstanceCreationExpression.class);
        assertTrue(outerCice.getPrimary().isPresent());
        ASTPrimary pMethod2 = outerCice.getPrimary().get();

        checkPrimary(pMethod2, METHOD_INVOCATION, ASTMethodInvocation.class);
        ASTMethodInvocation method2 = ensureIsa(pMethod2.getChild(), ASTMethodInvocation.class);
        checkMethodInvocation(method2, false, false, false,
                true, true);
        ASTIdentifier methodName2 = method2.getIdentifier();
        assertEquals("method2", methodName2.getValue());

        ASTPrimary pFieldAccess2 = method2.getPrimary().orElseThrow();
        checkPrimary(pFieldAccess2, FIELD_ACCESS, ASTFieldAccess.class);
        ASTFieldAccess fieldAccess2 = ensureIsa(pFieldAccess2.getChild(), ASTFieldAccess.class);
        checkFieldAccess(fieldAccess2, false, false, true);
        ASTIdentifier fieldName2 = fieldAccess2.getIdentifier();
        assertEquals("field2", fieldName2.getValue());

        ASTPrimary pJElementAccess = fieldAccess2.getPrimary().orElseThrow();
        checkPrimary(pJElementAccess, ASTPrimary.Type.ELEMENT_ACCESS, ASTElementAccess.class);
        ASTElementAccess jElementAccess = ensureIsa(pJElementAccess.getChild(), ASTElementAccess.class);
        assertTrue(jElementAccess.getPrimary().isPresent());

        ASTPrimary pMethod1 = jElementAccess.getPrimary().get();
        checkPrimary(pMethod1, METHOD_INVOCATION, ASTMethodInvocation.class);
        ASTMethodInvocation method1 = ensureIsa(pMethod1.getChild(), ASTMethodInvocation.class);
        checkMethodInvocation(method1, false, false, false,
                true, false);
        ASTIdentifier methodName1 = method1.getIdentifier();
        assertEquals("method1", methodName1.getValue());

        ASTPrimary pFieldAccess1 = method1.getPrimary().orElseThrow();
        checkPrimary(pFieldAccess1, FIELD_ACCESS, ASTFieldAccess.class);
        ASTFieldAccess fieldAccess1 = ensureIsa(pFieldAccess1.getChild(), ASTFieldAccess.class);
        checkFieldAccess(fieldAccess1, false, false, true);
        ASTIdentifier fieldName1 = fieldAccess1.getIdentifier();
        assertEquals("field1", fieldName1.getValue());

        ASTPrimary pIElementAccess = fieldAccess1.getPrimary().orElseThrow();
        checkPrimary(pIElementAccess, ASTPrimary.Type.ELEMENT_ACCESS, ASTElementAccess.class);
        ASTElementAccess iElementAccess = ensureIsa(pIElementAccess.getChild(), ASTElementAccess.class);
        assertTrue(iElementAccess.getPrimary().isPresent());

        ASTPrimary pInnerCice = ensureIsa(iElementAccess.getPrimary().get(), ASTPrimary.class);
        checkPrimary(pInnerCice, ASTPrimary.Type.CLASS_INSTANCE_CREATION_EXPR, ASTClassInstanceCreationExpression.class);
    }

    /**
     * Tests bad primary of something that's not a primary.
     */
    @Test
    public void testPrimaryBad() {
        ExpressionsParser parser = getExpressionsParser("public int x = 2;");
        ASTPrimary node = parser.parsePrimary();
        expectError(node, parser);
    }

    /**
     * Tests bad primary of no dot after super.
     */
    @Test
    public void testPrimarySuperBad() {
        ExpressionsParser parser = getExpressionsParser("super methodName()");
        ASTPrimary node = parser.parsePrimary();
        expectError(node, parser);
    }

    /**
     * Tests bad primary of no close paren after open paren.
     */
    @Test
    public void testPrimaryParenBad() {
        ExpressionsParser parser = getExpressionsParser("(a*a + b*b;");
        ASTPrimary node = parser.parsePrimary();
        expectError(node, parser);
    }

    /**
     * Tests bad primary of new, identifier, malformed instantiation.
     */
    @Test
    public void testPrimaryNewIdentifierBad() {
        ExpressionsParser parser = getExpressionsParser("new Bad 2.71828");
        ASTPrimary node = parser.parsePrimary();
        expectError(node, parser, 2);
    }

    /**
     * Tests bad primary of new not followed by type arguments or identifier.
     */
    @Test
    public void testPrimaryNewBad() {
        ExpressionsParser parser = getExpressionsParser("new interface");
        ASTPrimary node = parser.parsePrimary();
        expectError(node, parser, 4);
    }

    /**
     * Tests bad primary of expression name, dot, super, not followed by double colon or dot.
     */
    @Test
    public void testPrimaryOfExpressionNameSuperDotBad() {
        ExpressionsParser parser = getExpressionsParser("expr.name.super bad");
        ASTPrimary node = parser.parsePrimary();
        expectError(node, parser);
    }

    /**
     * Tests unqualified class instance creation expression of type arguments and type to instantiate.
     */
    @Test
    public void testUnqualifiedClassInstanceCreationExpressionOfTypeArguments() {
        ExpressionsParser parser = getExpressionsParser("new <String> MyClass()");
        ASTUnqualifiedClassInstanceCreationExpression node = parser.parseUnqualifiedClassInstanceCreationExpression();
        ensureNoErrors(node, parser);
        checkUcice(node, true);
    }

    /**
     * Tests class instance creation expression of unqualified class instance
     * creation expression.
     */
    @Test
    public void testCICEOfUCICE() {
        ExpressionsParser parser = getExpressionsParser("new MyClass<String>(1, \"one\")");
        ASTClassInstanceCreationExpression node = parser.parseClassInstanceCreationExpression();
        ensureNoErrors(node, parser);
        assertFalse(node.getPrimary().isPresent());
        assertNotNull(node.getUcice());
    }

    /**
     * Tests class instance creation expression when the type to instantiate
     * is parsed first.
     */
    @Test
    public void testCICEOfTypeToInstantiate() {
        ExpressionsParser parser = getExpressionsParser("MyClass(1, \"one\")");
        ASTTypeToInstantiate tti = parser.parseTypeToInstantiate();
        ASTClassInstanceCreationExpression node = parser.parseClassInstanceCreationExpression(tti);
        ensureNoErrors(node, parser);
        assertFalse(node.getPrimary().isPresent());
        assertNotNull(node.getUcice());
    }

    /**
     * Tests unqualified class instance creation expression of type to instantiate and argument list.
     */
    @Test
    public void testUnqualifiedClassInstanceCreationExpressionOfArgumentList() {
        ExpressionsParser parser = getExpressionsParser("new MyClass(1, \"one\")");
        ASTUnqualifiedClassInstanceCreationExpression node = parser.parseUnqualifiedClassInstanceCreationExpression();
        ensureNoErrors(node, parser);
        checkUcice(node, false);
    }

    /**
     * Tests bad unqualified class instance creation expression of no open paren.
     */
    @Test
    public void testUnqualifiedClassInstanceCreationExpressionNoOpenParen() {
        ExpressionsParser parser = getExpressionsParser("new MyClass 1, \"one\")");
        ASTUnqualifiedClassInstanceCreationExpression node = parser.parseUnqualifiedClassInstanceCreationExpression();
        expectError(node, parser);
    }

    /**
     * Tests bad unqualified class instance creation expression of no close paren.
     */
    @Test
    public void testUnqualifiedClassInstanceCreationExpressionNoCloseParen() {
        ExpressionsParser parser = getExpressionsParser("new MyClass(1, \"one\";");
        ASTUnqualifiedClassInstanceCreationExpression node = parser.parseUnqualifiedClassInstanceCreationExpression();
        expectError(node, parser);
    }

    /**
     * Tests type to instantiate of type name.
     */
    @Test
    public void testTypeToInstantiateOfTypeName() {
        ExpressionsParser parser = getExpressionsParser("MyClass");
        ASTTypeToInstantiate node = parser.parseTypeToInstantiate();
        ensureNoErrors(node, parser);
        assertNotNull(node.getTypeName());
        assertFalse(node.getTaod().isPresent());
    }

    /**
     * Tests type to instantiate of type name and type arguments or diamond.
     */
    @Test
    public void testTypeToInstantiateTypeNameOfTypeArgumentsOrDiamond() {
        ExpressionsParser parser = getExpressionsParser("MyClass<T>");
        ASTTypeToInstantiate node = parser.parseTypeToInstantiate();
        ensureNoErrors(node, parser);
        assertNotNull(node.getTypeName());
        assertTrue(node.getTaod().isPresent());
    }

    /**
     * Tests array creation expression of dim exprs.
     */
    @Test
    public void testArrayCreationExpressionOfDimExprs() {
        ExpressionsParser parser = getExpressionsParser("new String[10]");
        ASTArrayCreationExpression node = parser.parseArrayCreationExpression();
        ensureNoErrors(node, parser);
        checkArrayCreationExpression(node, true, false, false);
    }

    /**
     * Tests bad array creation expression of dim exprs and array initializer.
     */
    @Test
    public void testArrayCreationExpressionDimExprsArrayInitializer() {
        ExpressionsParser parser = getExpressionsParser("new Integer[3][] {1, 2, 3}");
        ASTArrayCreationExpression node = parser.parseArrayCreationExpression();
        expectError(node, parser);
    }

    /**
     * Tests array creation expression of dim exprs and dims.
     */
    @Test
    public void testArrayCreationExpressionOfDimExprsDims() {
        ExpressionsParser parser = getExpressionsParser("new String[10][]");
        ASTArrayCreationExpression node = parser.parseArrayCreationExpression();
        ensureNoErrors(node, parser);
        checkArrayCreationExpression(node, true, true, false);
    }

    /**
     * Tests array creation expression of dims and array initializer.
     */
    @Test
    public void testArrayCreationExpressionOfDimsArrayInitializer() {
        ExpressionsParser parser = getExpressionsParser("new String[] {\"one\", \"two\", \"three\"}");
        ASTArrayCreationExpression node = parser.parseArrayCreationExpression();
        ensureNoErrors(node, parser);
        checkArrayCreationExpression(node, false, true, true);
    }

    /**
     * Tests bad element access, no open bracket.
     */
    @Test
    public void testElementAccessNoOpenBracket() {
        ExpressionsParser parser = getExpressionsParser("element 1]");
        ASTPrimary node = parser.parsePrimary();
        checkPrimary(node, EXPR_NAME, ASTExpressionName.class);
    }
    /**
     * Tests bad element access, no close bracket.
     */
    @Test
    public void testElementAccessNoCloseBracket() {
        ExpressionsParser parser = getExpressionsParser("element[0 = 1");
        ASTPrimary node = parser.parsePrimary();
        expectError(node, parser);
    }

    /**
     * Tests dim exprs of dim expr.
     */
    @Test
    public void testDimExprsDimExpr() {
        ExpressionsParser parser = getExpressionsParser("[1][2][3]");
        ASTDimExprs node = parser.parseDimExprs();
        ensureNoErrors(node, parser);
        checkList(node, DIM_EXPRS, ASTDimExpr.class, 3);
    }

    /**
     * Tests dim exprs of dim expr with dim afterward.
     */
    @Test
    public void testDimExprsOfDimExprWithDimAfter() {
        ExpressionsParser parser = getExpressionsParser("[x+y][]");
        ASTDimExprs node = parser.parseDimExprs();
        ensureNoErrors(node, parser);
        checkList(node, DIM_EXPRS, ASTDimExpr.class, 1);
    }

    /**
     * Tests dim exprs of dim expr with dim afterward.
     */
    @Test
    public void testDimExprsOfDimExprWithDimAfter2() {
        ExpressionsParser parser = getExpressionsParser("[x+y][ ]");
        ASTDimExprs node = parser.parseDimExprs();
        ensureNoErrors(node, parser);
        checkList(node, DIM_EXPRS, ASTDimExpr.class, 1);
    }

    /**
     * Tests dim expr of value expression.
     */
    @Test
    public void testDimExprOfValueExpression() {
        ExpressionsParser parser = getExpressionsParser("[x+y]");
        ASTDimExpr node = parser.parseDimExpr();
        ensureNoErrors(node, parser);
        ASTValueExpression expr = node.getValueExpr();
        assertInstanceOf(ASTBinaryExpression.class, expr);
    }

    /**
     * Tests bad dim expression of no open bracket.
     */
    @Test
    public void testDimExprNoOpenBracket() {
        ExpressionsParser parser = getExpressionsParser("i]");
        ASTDimExpr node = parser.parseDimExpr();
        expectError(node, parser);
    }

    /**
     * Tests bad dim expression of no close bracket.
     */
    @Test
    public void testDimExprNoCloseBracket() {
        ExpressionsParser parser = getExpressionsParser("[i");
        ASTDimExpr node = parser.parseDimExpr();
        expectError(node, parser);
    }

    /**
     * Tests array initializer of just empty braces.
     */
    @Test
    public void testArrayInitializerEmpty() {
        ExpressionsParser parser = getExpressionsParser("{}");
        ASTArrayInitializer node = parser.parseArrayInitializer();
        ensureNoErrors(node, parser);
        assertNotNull(node.getVarInitializers());
        checkList(node.getVarInitializers(), VARIABLE_INITIALIZERS, ASTVariableInitializer.class, 0);
    }

    /**
     * Tests array initializer of a variable initializer list.
     */
    @Test
    public void testArrayInitializerOfVariableInitializerList() {
        ExpressionsParser parser = getExpressionsParser("{x + 1, y - 2}");
        ASTArrayInitializer node = parser.parseArrayInitializer();
        ensureNoErrors(node, parser);
        assertNotNull(node.getVarInitializers());
        checkList(node.getVarInitializers(), VARIABLE_INITIALIZERS, ASTVariableInitializer.class, 2);
    }

    /**
     * Tests bad array initializer, no open brace.
     */
    @Test
    public void testArrayInitializerNoOpenBrace() {
        ExpressionsParser parser = getExpressionsParser("\"Needs\", \"Open\", \"Brace\"}");
        ASTArrayInitializer node = parser.parseArrayInitializer();
        expectError(node, parser);
    }

    /**
     * Tests bad array initializer, no close brace.
     */
    @Test
    public void testArrayInitializerNoCloseBrace() {
        ExpressionsParser parser = getExpressionsParser("{\"Needs\", \"Close\", \"Brace\" class");
        ASTArrayInitializer node = parser.parseArrayInitializer();
        expectError(node, parser);
    }

    /**
     * Tests variable initializer list of variable initializer.
     */
    @Test
    public void testVariableInitializerListOfVariableInitializer() {
        ExpressionsParser parser = getExpressionsParser("i + 1");
        ASTVariableInitializerList node = parser.parseVariableInitializerList();
        ensureNoErrors(node, parser);
        checkList(node, VARIABLE_INITIALIZERS, ASTVariableInitializer.class, 1);
    }

    /**
     * Tests variable initializer list of "," and variable initializer.
     */
    @Test
    public void testVariableInitializerListOfComma() {
        ExpressionsParser parser = getExpressionsParser("x + 1, y - 1");
        ASTVariableInitializerList node = parser.parseVariableInitializerList();
        ensureNoErrors(node, parser);
        checkList(node, VARIABLE_INITIALIZERS, ASTVariableInitializer.class, 2);
    }

    /**
     * Tests nested variable initializer lists (here, just multiple variable initializers).
     */
    @Test
    public void testVariableInitializerListNested() {
        ExpressionsParser parser = getExpressionsParser("self, count + 1, sumSoFar + value");
        ASTVariableInitializerList node = parser.parseVariableInitializerList();
        ensureNoErrors(node, parser);
        checkList(node, VARIABLE_INITIALIZERS, ASTVariableInitializer.class, 3);
    }

    /**
     * Tests variable initializer of expression.
     */
    @Test
    public void testVariableInitializerOfExpression() {
        ExpressionsParser parser = getExpressionsParser("a + b");
        ASTVariableInitializer node = parser.parseVariableInitializer();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTBinaryExpression.class, node);
    }

    /**
     * Tests variable initializer of array initializer.
     */
    @Test
    public void testVariableInitializerOfArrayInitializer() {
        ExpressionsParser parser = getExpressionsParser("{1, 2, 3}");
        ASTVariableInitializer node = parser.parseVariableInitializer();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTArrayInitializer.class, node);
    }

    /**
     * Tests a class literal.  Parses a DataType first.
     */
    @Test
    public void testClassLiteral() {
        ExpressionsParser parser = getExpressionsParser("Outer.Inner.class");
        ASTDataType dataType = parser.getTypesParser().parseDataType();
        ASTClassLiteral node = parser.parseClassLiteral(dataType);
        ensureNoErrors(node, parser);
        assertNotNull(node.getDataType());
    }

    /**
     * Tests "self".
     */
    @Test
    public void testSelf() {
        ExpressionsParser parser = getExpressionsParser("self");
        ASTKeywordNode node = parser.parseSelf();
        ensureNoErrors(node, parser);
        assertEquals(TokenType.SELF, node.getKeyword());
    }

    /**
     * Tests "super".
     */
    @Test
    public void testSuper() {
        ExpressionsParser parser = getExpressionsParser("super");
        ASTKeywordNode node = parser.parseSuper();
        ensureNoErrors(node, parser);
        assertEquals(SUPER, node.getKeyword());
    }

    /**
     * Helper method to test <code>ASTArrayCreationExpression</code> attributes.
     */
    private static void checkArrayCreationExpression(ASTArrayCreationExpression ace, boolean isDimExprsPresent,
                                                    boolean isDimsPresent, boolean isArrayInitializerPresent) {
        assertNotNull(ace.getTypeToInstantiate());
        assertEquals(isDimExprsPresent, ace.getDimExprs().isPresent());
        assertEquals(isDimsPresent, ace.getDims().isPresent());
        assertEquals(isArrayInitializerPresent, ace.getArrayInitializer().isPresent());
    }

    /**
     * Helper method to test <code>ASTUnqualifiedClassInstanceCreation</code> attributes.
     */
    private static void checkUcice(ASTUnqualifiedClassInstanceCreationExpression ucice,
        boolean isTypeArgsPresent)
    {
        assertEquals(isTypeArgsPresent, ucice.getTypeArgs().isPresent());
        assertNotNull(ucice.getTti());
        assertNotNull(ucice.getArgumentList());
    }

    /**
     * Helper method to test <code>ASTFieldAccess</code> attributes.
     * @param isTypeNamePresent Whether the Type Name should be present.
     * @param isSuperPresent Whether <code>super</code>> should be present.
     * @param isPrimaryPresent Whether the Primary should be present.
     */
    private static void checkFieldAccess(ASTFieldAccess fa, boolean isTypeNamePresent, boolean isSuperPresent, boolean isPrimaryPresent) {
        assertEquals(isTypeNamePresent, fa.getTypeName().isPresent());
        assertEquals(isSuperPresent, fa.getSooper().isPresent());
        assertEquals(isPrimaryPresent, fa.getPrimary().isPresent());
        assertNotNull(fa.getIdentifier());
    }

    /**
     * Helper method to test <code>ASTMethodInvocation</code> attributes.
     * @param mi The <code>ASTMethodInvocation</code> to test.
     * @param isTypeNamePresent Whether the Type Name should be present.
     * @param isSuperPresent Whether <code>super</code>> should be present.
     * @param isExprNamePresent Whether the Expression Name should be present.
     * @param isPrimaryPresent Whether the Primary should be present.
     * @param isTypeArgsPresent Whether the Type Args should be present.
     */
    private static void checkMethodInvocation(ASTMethodInvocation mi, boolean isTypeNamePresent,
                                              boolean isSuperPresent, boolean isExprNamePresent, boolean isPrimaryPresent,
                                              boolean isTypeArgsPresent) {
        assertEquals(isTypeNamePresent, mi.getTypeName().isPresent());
        assertEquals(isSuperPresent, mi.getSooper().isPresent());
        assertEquals(isExprNamePresent, mi.getExprName().isPresent());
        assertEquals(isPrimaryPresent, mi.getPrimary().isPresent());
        assertEquals(isTypeArgsPresent, mi.getTypeArgs().isPresent());
        assertNotNull(mi.getIdentifier());
        assertTrue(mi.getArgumentList().isPresent());
    }

    /**
     * Helper method to test <code>ASTMethodReference</code> attributes.
     * @param mr The <code>ASTMethodReference</code> to test.
     * @param isTypeNamePresent Whether the Type Name should be present.
     * @param isSuperPresent Whether <code>super</code>> should be present.
     * @param isExprNamePresent Whether the Expression Name should be present.
     * @param isDataTypePresent Whether the Data Type should be present.
     * @param isPrimaryPresent Whether the Primary should be present.
     * @param isTypeArgsPresent Whether the Type Args should be present.
     */
    private static void checkMethodReference(ASTMethodReference mr, boolean isTypeNamePresent,
                                             boolean isSuperPresent, boolean isExprNamePresent, boolean isDataTypePresent,
                                             boolean isPrimaryPresent, boolean isTypeArgsPresent) {
        assertEquals(isTypeNamePresent, mr.getTypeName().isPresent());
        assertEquals(isSuperPresent, mr.getSooper().isPresent());
        assertEquals(isExprNamePresent, mr.getExprName().isPresent());
        assertEquals(isDataTypePresent, mr.getDataType().isPresent());
        assertEquals(isPrimaryPresent, mr.getPrimary().isPresent());
        assertEquals(isTypeArgsPresent, mr.getTypeArgs().isPresent());
        assertNotNull(mr.getIdentifier());
    }
    
    /**
     * Helper method to get a <code>ExpressionsParser</code> directly from code.
     * @param code The code to test.
     * @return A <code>ExpressionsParser</code> that will parse the given code.
     */
    private static ExpressionsParser getExpressionsParser(String code) {
        return new Parser(new Scanner(code)).getExpressionsParser();
    }

    /**
     * Helper method to test <code>ASTPrimary</code> objects.
     * @param primary The <code>ASTPrimary</code> to test.
     * @param type It must have this <code>ASTPrimary.Type</code>.
     * @param childClass Its child must be of this class.
     */
    private static void checkPrimary(ASTPrimary primary, ASTPrimary.Type type, Class<? extends Node> childClass) {
        assertEquals(type, primary.getType());
        assertInstanceOf(childClass, primary.getChild());
    }
}
