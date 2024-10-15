package org.spruce.compiler.test;

import java.util.Arrays;
import java.util.Collections;

import org.spruce.compiler.ast.ASTBinaryNode;
import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.ast.ASTModifierNode;
import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.ASTUnaryNode;
import org.spruce.compiler.ast.classes.ASTFormalParameter;
import org.spruce.compiler.ast.expressions.*;
import org.spruce.compiler.ast.literals.*;
import org.spruce.compiler.ast.names.*;
import org.spruce.compiler.ast.statements.ASTBlock;
import org.spruce.compiler.ast.expressions.ASTSwitchLabel;
import org.spruce.compiler.ast.statements.ASTThrowStatement;
import org.spruce.compiler.ast.types.*;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.parser.ExpressionsParser;
import org.spruce.compiler.parser.Parser;
import org.spruce.compiler.scanner.Scanner;
import static org.spruce.compiler.scanner.TokenType.*;
import static org.spruce.compiler.test.ParserTestUtility.*;

import org.junit.jupiter.api.Test;

import static org.spruce.compiler.ast.ASTListNode.Type.*;
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
        ASTBinaryNode node = parser.parseLambdaExpression();
        node.print();
        checkBinary(node, ARROW, ASTLambdaParameters.class, ASTBinaryNode.class);
        ASTBinaryNode nested = (ASTBinaryNode) node.getSecond();
        checkBinary(nested, ARROW, ASTLambdaParameters.class, ASTPrimary.class);
    }

    /**
     * Tests expression of lambda expression.
     */
    @Test
    public void testExpressionOfLambdaExpression() {
        ExpressionsParser parser = getExpressionsParser("|x, y| -> { use x + y;}");
        ASTNode node = parser.parseExpression();
        node.print();
        ASTBinaryNode lambdaExpr = ensureIsa(node, ASTBinaryNode.class);
        assertEquals(ARROW, lambdaExpr.getOperation());
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
        ASTBinaryNode node = parser.parseLambdaExpression();
        node.print();
        checkBinary(node, ARROW, ASTLambdaParameters.class, ASTBinaryNode.class);
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
        ASTNode node = parser.parseLambdaBody();
        node.print();
        assertInstanceOf(ASTBlock.class, node);
    }

    /**
     * Tests lambda body of expression.
     */
    @Test
    public void testLambdaBodyOfExpression() {
        ExpressionsParser parser = getExpressionsParser("n * 2");
        ASTNode node = parser.parseLambdaBody();
        node.print();
        assertInstanceOf(ASTBinaryNode.class, node);
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
        node.print();
        assertFalse(node.getLambdaParams().isPresent());
        assertTrue(node.getIdentifier().isPresent());
        assertInstanceOf(ASTIdentifier.class, node.getIdentifier().get());
        assertNull(node.getOperation());
    }

    /**
     * Tests lambda parameters of lambda parameter list.
     */
    @Test
    public void testLambdaParametersOfLambdaParameterList() {
        ExpressionsParser parser = getExpressionsParser("|a, b|");
        ASTLambdaParameters node = parser.parseLambdaParameters();
        node.print();
        assertTrue(node.getLambdaParams().isPresent());
        assertInstanceOf(ASTListNode.class, node.getLambdaParams().get());
        assertFalse(node.getIdentifier().isPresent());
        assertEquals(PIPE, node.getOperation());
        ASTListNode inferredParams = node.getLambdaParams().get();
        assertEquals(INFERRED_PARAMETERS, inferredParams.getType());
    }

    /**
     * Tests lambda parameters of just a two separated pipes.
     */
    @Test
    public void testLambdaParametersOfTwoPipes() {
        ExpressionsParser parser = getExpressionsParser("| |");
        ASTLambdaParameters node = parser.parseLambdaParameters();
        node.print();
        assertFalse(node.getLambdaParams().isPresent());
        assertFalse(node.getIdentifier().isPresent());
        assertEquals(PIPE, node.getOperation());
    }

    /**
     * Tests lambda parameters of just a double-pipe.
     */
    @Test
    public void testLambdaParametersOfDoublePipe() {
        ExpressionsParser parser = getExpressionsParser("||");
        ASTLambdaParameters node = parser.parseLambdaParameters();
        node.print();
        assertFalse(node.getLambdaParams().isPresent());
        assertFalse(node.getIdentifier().isPresent());
        assertEquals(PIPE, node.getOperation());
    }

    /**
     * Tests lambda parameter list of formal parameter list.
     */
    @Test
    public void testLambdaParameterListOfFormalParameterList() {
        ExpressionsParser parser = getExpressionsParser("Double x, Double y, Double z");
        ASTListNode node = parser.parseLambdaParameterList();
        node.print();
        checkList(node, FORMAL_PARAMETERS, ASTFormalParameter.class, 3);
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
        ASTListNode node = parser.parseLambdaParameterList();
        node.print();
        checkList(node, INFERRED_PARAMETERS, ASTIdentifier.class, 3);
    }

    /**
     * Tests bad inferred parameter list.
     */
    @Test
    public void testBadInferredParameterList() {
        ExpressionsParser parser = getExpressionsParser("2");
        ASTListNode node = parser.parseInferredParameterList();
        checkList(node, INFERRED_PARAMETERS, ASTIdentifier.class, 0);
    }

    /**
     * Tests inferred parameter list.
     */
    @Test
    public void testInferredParameterList() {
        ExpressionsParser parser = getExpressionsParser("alpha, beta, gamma");
        ASTListNode node = parser.parseInferredParameterList();
        node.print();
        checkList(node, INFERRED_PARAMETERS, ASTIdentifier.class, 3);
    }

    /**
     * Tests expression of other expression.
     */
    @Test
    public void testExpressionOfOtherExpression() {
        ExpressionsParser parser = getExpressionsParser("count == 1");
        ASTNode node = parser.parseExpression();
        node.print();
        checkBinary(ensureIsa(node, ASTBinaryNode.class), DOUBLE_EQUAL, ASTPrimary.class, ASTPrimary.class);
    }

    /**
     * Tests expression of conditional expression.
     */
    @Test
    public void testExpressionOfConditionalExpression() {
        ExpressionsParser parser = getExpressionsParser("a ? b : c");
        ASTNode node = parser.parseExpression();
        node.print();
        assertInstanceOf(ASTConditionalExpression.class, node);
    }

    /**
     * Tests conditional expression of logical or expression.
     */
    @Test
    public void testConditionalExpressionOfLogicalOrExpression() {
        ExpressionsParser parser = getExpressionsParser("a || b");
        ASTNode node = parser.parseConditionalExpression();
        node.print();
        checkBinary(ensureIsa(node, ASTBinaryNode.class), DOUBLE_PIPE, ASTPrimary.class, ASTPrimary.class);
    }

    /**
     * Tests conditional expression of "?" and ":" and logical or expression.
     */
    @Test
    public void testConditionalExpression() {
        ExpressionsParser parser = getExpressionsParser("condition ? valueIfTrue : valueIfFalse");
        ASTNode node = parser.parseConditionalExpression();
        node.print();
        assertInstanceOf(ASTConditionalExpression.class, node);
        ASTConditionalExpression condExpr = (ASTConditionalExpression) node;
        assertEquals(QUESTION_MARK, condExpr.getOperation());
        compareClasses(Arrays.asList(ASTPrimary.class, ASTPrimary.class, ASTPrimary.class),
                Arrays.asList(condExpr.getCondition(), condExpr.getExprIfTrue(), condExpr.getExprIfFalse()));
    }

    /**
     * Tests nested conditional expressions.
     */
    @Test
    public void testConditionalExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("a || b ? \"one\" : c || d ? \"two\" : e || f ? \"three\" : \"four\"");
        ASTNode node = parser.parseConditionalExpression();
        node.print();

        assertInstanceOf(ASTConditionalExpression.class, node);
        ASTConditionalExpression outer = (ASTConditionalExpression) node;
        assertEquals(QUESTION_MARK, outer.getOperation());
        compareClasses(Arrays.asList(ASTBinaryNode.class, ASTPrimary.class, ASTConditionalExpression.class),
                Arrays.asList(outer.getCondition(), outer.getExprIfTrue(), outer.getExprIfFalse()));

        ASTConditionalExpression middle = (ASTConditionalExpression) outer.getExprIfFalse();
        assertEquals(QUESTION_MARK, middle.getOperation());
        compareClasses(Arrays.asList(ASTBinaryNode.class, ASTPrimary.class, ASTConditionalExpression.class),
                Arrays.asList(middle.getCondition(), middle.getExprIfTrue(), middle.getExprIfFalse()));

        ASTConditionalExpression inner = (ASTConditionalExpression) middle.getExprIfFalse();
        assertEquals(QUESTION_MARK, inner.getOperation());
        compareClasses(Arrays.asList(ASTBinaryNode.class, ASTPrimary.class, ASTPrimary.class),
                Arrays.asList(inner.getCondition(), inner.getExprIfTrue(), inner.getExprIfFalse()));
    }

    /**
     * Tests logical or expression of logical xor expression.
     */
    @Test
    public void testLogicalOrExpressionOfLogicalAndExpression() {
        ExpressionsParser parser = getExpressionsParser("a ^: b");
        ASTNode node = parser.parseLogicalOrExpression();
        node.print();
        checkBinary(ensureIsa(node, ASTBinaryNode.class), CARET_COLON, ASTPrimary.class, ASTPrimary.class);
    }

    /**
     * Tests logical or expression of "|:" and logical xor expression.
     */
    @Test
    public void testLogicalOrExpressionOfEager() {
        ExpressionsParser parser = getExpressionsParser("test |: elseThis");
        ASTNode node = parser.parseLogicalOrExpression();
        node.print();
        checkBinaryLeftAssociative(ensureIsa(node, ASTBinaryNode.class), Collections.singletonList(PIPE_COLON), ASTPrimary.class, ASTPrimary.class);
    }

    /**
     * Tests logical or expression of "||" and logical xor expression.
     */
    @Test
    public void testLogicalOrExpressionOfConditional() {
        ExpressionsParser parser = getExpressionsParser("alreadyDone || test");
        ASTNode node = parser.parseLogicalOrExpression();
        node.print();
        checkBinaryLeftAssociative(ensureIsa(node, ASTBinaryNode.class), Collections.singletonList(DOUBLE_PIPE), ASTPrimary.class, ASTPrimary.class);
    }

    /**
     * Tests nested logical or expressions.
     */
    @Test
    public void testLogicalOrExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("a && b |: c ^: d || e &: f");
        ASTNode node = parser.parseLogicalOrExpression();
        node.print();
        checkBinaryPostorder(ensureIsa(node, ASTBinaryNode.class),
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
        ASTNode node = parser.parseLogicalXorExpression();
        node.print();
        ASTBinaryNode binary = ensureIsa(node, ASTBinaryNode.class);
        assertEquals(DOUBLE_AMPERSAND, binary.getOperation());
    }

    /**
     * Tests logical xor expression of "^:" and logical and expression.
     */
    @Test
    public void testLogicalXorExpression() {
        ExpressionsParser parser = getExpressionsParser("test ^: thisAlso");
        ASTNode node = parser.parseLogicalXorExpression();
        node.print();
        checkBinaryLeftAssociative(ensureIsa(node, ASTBinaryNode.class), Collections.singletonList(CARET_COLON), ASTPrimary.class, ASTPrimary.class);
    }

    /**
     * Tests nested logical xor expressions.
     */
    @Test
    public void testLogicalXorExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("a && b ^: c &: d ^: e && f");
        ASTNode node = parser.parseLogicalXorExpression();
        node.print();
        checkBinaryPostorder(ensureIsa(node, ASTBinaryNode.class),
                ASTPrimary.class, ASTPrimary.class, DOUBLE_AMPERSAND, ASTPrimary.class, ASTPrimary.class, AMPERSAND_COLON, CARET_COLON,
                ASTPrimary.class, ASTPrimary.class, DOUBLE_AMPERSAND, CARET_COLON);
    }

    /**
     * Tests logical and expression of relational expression.
     */
    @Test
    public void testLogicalAndExpressionOfRelationalExpression() {
        ExpressionsParser parser = getExpressionsParser("a == b");
        ASTNode node = parser.parseLogicalAndExpression();
        node.print();
        ASTBinaryNode binary = ensureIsa(node, ASTBinaryNode.class);
        assertEquals(DOUBLE_EQUAL, binary.getOperation());
    }

    /**
     * Tests logical and expression of "&&" and relational expression.
     */
    @Test
    public void testLogicalAndExpressionOfConditional() {
        ExpressionsParser parser = getExpressionsParser("test && notDone");
        ASTNode node = parser.parseLogicalAndExpression();
        node.print();
        checkBinaryLeftAssociative(ensureIsa(node, ASTBinaryNode.class), Collections.singletonList(DOUBLE_AMPERSAND), ASTPrimary.class, ASTPrimary.class);
    }

    /**
     * Tests logical and expression of "&:" and relational expression.
     */
    @Test
    public void testLogicalAndExpressionOfEager() {
        ExpressionsParser parser = getExpressionsParser("test &: thisAlso");
        ASTNode node = parser.parseLogicalAndExpression();
        node.print();
        checkBinaryLeftAssociative(ensureIsa(node, ASTBinaryNode.class), Collections.singletonList(AMPERSAND_COLON), ASTPrimary.class, ASTPrimary.class);
    }

    /**
     * Tests nested logical and expressions.
     */
    @Test
    public void testLogicalAndExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("a < b &: c <= d && e > f");
        ASTNode node = parser.parseLogicalAndExpression();
        node.print();
        checkBinaryPostorder(ensureIsa(node, ASTBinaryNode.class),
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
        ASTNode node = parser.parseRelationalExpression();
        node.print();
        ASTBinaryNode binary = ensureIsa(node, ASTBinaryNode.class);
        assertEquals(COMPARISON, binary.getOperation());
    }

    /**
     * Tests relational expression of "&lt;" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfLessThan() {
        ExpressionsParser parser = getExpressionsParser("a.value < b.value");
        ASTNode node = parser.parseRelationalExpression();
        node.print();
        checkBinaryLeftAssociative(ensureIsa(node, ASTBinaryNode.class), Collections.singletonList(LESS_THAN), ASTPrimary.class, ASTPrimary.class);
    }

    /**
     * Tests relational expression of "&lt;=" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfLessThanOrEqual() {
        ExpressionsParser parser = getExpressionsParser("2 <= 2");
        ASTNode node = parser.parseRelationalExpression();
        node.print();
        checkBinaryLeftAssociative(ensureIsa(node, ASTBinaryNode.class), Collections.singletonList(LESS_THAN_OR_EQUAL), ASTPrimary.class, ASTPrimary.class);
    }

    /**
     * Tests relational expression of "&gt;" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfGreaterThan() {
        ExpressionsParser parser = getExpressionsParser("a.value > b.value");
        ASTNode node = parser.parseRelationalExpression();
        node.print();
        checkBinaryLeftAssociative(ensureIsa(node, ASTBinaryNode.class), Collections.singletonList(GREATER_THAN), ASTPrimary.class, ASTPrimary.class);
    }

    /**
     * Tests relational expression of "&gt;=" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfGreaterThanOrEqual() {
        ExpressionsParser parser = getExpressionsParser("2 >= 2");
        ASTNode node = parser.parseRelationalExpression();
        node.print();
        checkBinaryLeftAssociative(ensureIsa(node, ASTBinaryNode.class), Collections.singletonList(GREATER_THAN_OR_EQUAL), ASTPrimary.class, ASTPrimary.class);
    }

    /**
     * Tests relational expression of "==" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfEqual() {
        ExpressionsParser parser = getExpressionsParser("test == SUCCESS");
        ASTNode node = parser.parseRelationalExpression();
        node.print();
        checkBinaryLeftAssociative(ensureIsa(node, ASTBinaryNode.class), Collections.singletonList(DOUBLE_EQUAL), ASTPrimary.class, ASTPrimary.class);
    }

    /**
     * Tests relational expression of "!=" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfNotEqual() {
        ExpressionsParser parser = getExpressionsParser("test != FAILURE");
        ASTNode node = parser.parseRelationalExpression();
        node.print();
        checkBinaryLeftAssociative(ensureIsa(node, ASTBinaryNode.class), Collections.singletonList(NOT_EQUAL), ASTPrimary.class, ASTPrimary.class);
    }

    /**
     * Tests relational expression of "isa" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfIsa() {
        ExpressionsParser parser = getExpressionsParser("node isa ASTRelationalExpression");
        ASTNode node = parser.parseRelationalExpression();
        node.print();
        ASTBinaryNode binary = ensureIsa(node, ASTBinaryNode.class);
        checkBinary(binary, ISA, ASTPrimary.class, ASTDataType.class);
        ASTNode child = binary.getFirst();
        assertInstanceOf(ASTPrimary.class, child);
    }

    /**
     * Tests relational expression of "is" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfIs() {
        ExpressionsParser parser = getExpressionsParser("obj is other");
        ASTNode node = parser.parseRelationalExpression();
        node.print();
        checkBinaryLeftAssociative(ensureIsa(node, ASTBinaryNode.class), Collections.singletonList(IS), ASTPrimary.class, ASTPrimary.class);
    }

    /**
     * Tests relational expression of "isnt" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfIsnt() {
        ExpressionsParser parser = getExpressionsParser("obj isnt somethingElse");
        ASTNode node = parser.parseRelationalExpression();
        node.print();
        checkBinaryLeftAssociative(ensureIsa(node, ASTBinaryNode.class), Collections.singletonList(ISNT), ASTPrimary.class, ASTPrimary.class);
    }

    /**
     * Tests nested relational expressions.
     */
    @Test
    public void testRelationalExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("a < b <=> c <= d");
        ASTNode node = parser.parseRelationalExpression();
        node.print();
        checkBinaryPostorder(ensureIsa(node, ASTBinaryNode.class),
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
        ASTNode node = parser.parseCompareExpression();
        node.print();
        ASTBinaryNode binary = ensureIsa(node, ASTBinaryNode.class);
        assertEquals(PIPE, binary.getOperation());
    }

    /**
     * Tests compare expression of "&lt;=&gt;" and bitwise or expression.
     */
    @Test
    public void testCompareExpression() {
        ExpressionsParser parser = getExpressionsParser("a.value <=> b.value");
        ASTNode node = parser.parseCompareExpression();
        node.print();
        checkBinary(ensureIsa(node, ASTBinaryNode.class), COMPARISON, ASTPrimary.class, ASTPrimary.class);
    }

    /**
     * Tests bitwise or expression of bitwise xor expression.
     */
    @Test
    public void testBitwiseOrExpressionOfBitwiseXorExpression() {
        ExpressionsParser parser = getExpressionsParser("a ^ b");
        ASTNode node = parser.parseBitwiseOrExpression();
        node.print();
        ASTBinaryNode binary = ensureIsa(node, ASTBinaryNode.class);
        assertEquals(CARET, binary.getOperation());
    }

    /**
     * Tests bitwise or expression of "|" and bitwise xor expression.
     */
    @Test
    public void testBitwiseOrExpression() {
        ExpressionsParser parser = getExpressionsParser("color | blueMask");
        ASTNode node = parser.parseBitwiseOrExpression();
        node.print();
        checkBinaryLeftAssociative(ensureIsa(node, ASTBinaryNode.class), Collections.singletonList(PIPE), ASTPrimary.class, ASTPrimary.class);
    }

    /**
     * Tests nested bitwise or expressions.
     */
    @Test
    public void testBitwiseOrExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("red | blue | yellow ^ green");
        ASTNode node = parser.parseBitwiseOrExpression();
        node.print();
        checkBinaryPostorder(ensureIsa(node, ASTBinaryNode.class),
                ASTPrimary.class, ASTPrimary.class, PIPE,
                ASTPrimary.class, ASTPrimary.class, CARET, PIPE);
    }

    /**
     * Tests bitwise xor expression of bitwise and expression.
     */
    @Test
    public void testBitwiseXorExpressionOfBitwiseAndExpression() {
        ExpressionsParser parser = getExpressionsParser("a & b");
        ASTNode node = parser.parseBitwiseXorExpression();
        node.print();
        ASTBinaryNode binary = ensureIsa(node, ASTBinaryNode.class);
        assertEquals(AMPERSAND, binary.getOperation());
    }

    /**
     * Tests bitwise xor expression of "^" and bitwise and expression.
     */
    @Test
    public void testBitwiseXorExpression() {
        ExpressionsParser parser = getExpressionsParser("color ^ blueMask");
        ASTNode node = parser.parseBitwiseXorExpression();
        node.print();
        checkBinaryLeftAssociative(ensureIsa(node, ASTBinaryNode.class), Collections.singletonList(CARET), ASTPrimary.class, ASTPrimary.class);
    }

    /**
     * Tests nested bitwise xor expressions.
     */
    @Test
    public void testBitwiseXorExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("red ^ blue & yellow ^ green");
        ASTNode node = parser.parseBitwiseXorExpression();
        node.print();
        checkBinaryPostorder(ensureIsa(node, ASTBinaryNode.class),
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
        ASTNode node = parser.parseBitwiseAndExpression();
        node.print();
        ASTBinaryNode binary = ensureIsa(node, ASTBinaryNode.class);
        assertEquals(SHIFT_LEFT, binary.getOperation());
    }

    /**
     * Tests bitwise and expression of "&" and shift expression.
     */
    @Test
    public void testBitwiseAndExpression() {
        ExpressionsParser parser = getExpressionsParser("color & blueMask");
        ASTNode node = parser.parseBitwiseAndExpression();
        node.print();
        checkBinaryLeftAssociative(ensureIsa(node, ASTBinaryNode.class), Collections.singletonList(AMPERSAND), ASTPrimary.class, ASTPrimary.class);
    }

    /**
     * Tests nested bitwise and expressions.
     */
    @Test
    public void testBitwiseAndExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("red + blue & blueGreenMask & greenRedMask");
        ASTNode node = parser.parseBitwiseAndExpression();
        node.print();
        checkBinaryPostorder(ensureIsa(node, ASTBinaryNode.class),
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
        ASTNode node = parser.parseShiftExpression();
        node.print();
        ASTBinaryNode binary = ensureIsa(node, ASTBinaryNode.class);
        assertEquals(PLUS, binary.getOperation());
    }

    /**
     * Tests shift expression of "<<" and additive expression.
     */
    @Test
    public void testShiftExpressionOfLeftShift() {
        ExpressionsParser parser = getExpressionsParser("1 << 2");
        ASTNode node = parser.parseShiftExpression();
        node.print();
        checkBinaryLeftAssociative(ensureIsa(node, ASTBinaryNode.class), Collections.singletonList(SHIFT_LEFT), ASTPrimary.class, ASTPrimary.class);
    }

    /**
     * Tests shift expression of ">>" and additive expression.
     */
    @Test
    public void testShiftExpressionOfRightShift() {
        ExpressionsParser parser = getExpressionsParser("2048 >> 2");
        ASTNode node = parser.parseShiftExpression();
        node.print();
        checkBinaryLeftAssociative(ensureIsa(node, ASTBinaryNode.class), Collections.singletonList(SHIFT_RIGHT), ASTPrimary.class, ASTPrimary.class);
    }

    /**
     * Tests nested shift expressions.
     */
    @Test
    public void testShiftExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("-2 << 3 + 4 >> 5 >> 1");
        ASTNode node = parser.parseShiftExpression();
        node.print();
        checkBinaryPostorder(ensureIsa(node, ASTBinaryNode.class),
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
        ASTNode node = parser.parseAdditiveExpression();
        node.print();
        ASTBinaryNode binary = ensureIsa(node, ASTBinaryNode.class);
        assertEquals(STAR, binary.getOperation());
    }

    /**
     * Tests additive expression of "+" and multiplicative expression.
     */
    @Test
    public void testAdditiveExpressionOfPlus() {
        ExpressionsParser parser = getExpressionsParser("-1 + 2");
        ASTNode node = parser.parseAdditiveExpression();
        node.print();
        checkBinary(ensureIsa(node, ASTBinaryNode.class), PLUS, ASTUnaryExpression.class, ASTPrimary.class);
    }

    /**
     * Tests additive expression of "-" and multiplicative expression.
     */
    @Test
    public void testAdditiveExpressionOfMinus() {
        ExpressionsParser parser = getExpressionsParser("finish - start");
        ASTNode node = parser.parseAdditiveExpression();
        node.print();
        checkBinaryLeftAssociative(ensureIsa(node, ASTBinaryNode.class), Collections.singletonList(MINUS), ASTPrimary.class, ASTPrimary.class);
    }

    /**
     * Tests nested additive expressions.
     */
    @Test
    public void testAdditiveExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("-2 + 3 * 4 - 5");
        ASTNode node = parser.parseAdditiveExpression();
        node.print();
        checkBinaryPostorder(ensureIsa(node, ASTBinaryNode.class),
                ASTUnaryExpression.class, ASTPrimary.class, ASTPrimary.class, STAR, PLUS, ASTPrimary.class, MINUS);
    }

    /**
     * Tests multiplicative expression of unary expression.
     */
    @Test
    public void testMultiplicativeExpressionOfUnaryExpression() {
        ExpressionsParser parser = getExpressionsParser("varName");
        ASTNode node = parser.parseMultiplicativeExpression();
        node.print();
        assertInstanceOf(ASTPrimary.class, node);
    }

    /**
     * Tests multiplicative expression of "*" and unary expression.
     */
    @Test
    public void testMultiplicativeExpressionOfTimes() {
        ExpressionsParser parser = getExpressionsParser("a * b");
        ASTNode node = parser.parseMultiplicativeExpression();
        node.print();
        checkBinaryLeftAssociative(ensureIsa(node, ASTBinaryNode.class), Collections.singletonList(STAR), ASTPrimary.class, ASTPrimary.class);
    }

    /**
     * Tests multiplicative expression of "/" and unary expression.
     */
    @Test
    public void testMultiplicativeExpressionOfDivide() {
        ExpressionsParser parser = getExpressionsParser("i / -1");
        ASTNode node = parser.parseMultiplicativeExpression();
        node.print();
        checkBinary(ensureIsa(node, ASTBinaryNode.class), SLASH, ASTPrimary.class, ASTUnaryExpression.class);
    }

    /**
     * Tests multiplicative expression of "%" and unary expression.
     */
    @Test
    public void testMultiplicativeExpressionOfModulus() {
        ExpressionsParser parser = getExpressionsParser("index % len");
        ASTNode node = parser.parseMultiplicativeExpression();
        node.print();
        checkBinaryLeftAssociative(ensureIsa(node, ASTBinaryNode.class), Collections.singletonList(PERCENT), ASTPrimary.class, ASTPrimary.class);
    }

    /**
     * Tests nested multiplicative expressions.
     */
    @Test
    public void testMultiplicativeExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("5 * 6 / 3 % 7");
        ASTNode node = parser.parseMultiplicativeExpression();
        node.print();
        checkBinaryPostorder(ensureIsa(node, ASTBinaryNode.class),
                ASTPrimary.class, ASTPrimary.class, STAR,
                ASTPrimary.class, SLASH, ASTPrimary.class, PERCENT);
    }

    /**
     * Tests parenthesized multiplicative expressions.
     */
    @Test
    public void testMultiplicativeExpressionOfParenthesizedExpressions() {
        ExpressionsParser parser = getExpressionsParser("(x + 1)*(x - 1)");
        ASTNode node = parser.parseMultiplicativeExpression();
        node.print();
        ASTBinaryNode binary = ensureIsa(node, ASTBinaryNode.class);
        checkBinary(binary, STAR, ASTPrimary.class, ASTPrimary.class);
        ASTPrimary left = ensureIsa(binary.getFirst(), ASTPrimary.class);
        checkUnary(left, OPEN_PARENTHESIS, ASTBinaryNode.class);
        checkBinary(ensureIsa(left.getFirst(), ASTBinaryNode.class), PLUS, ASTPrimary.class, ASTPrimary.class);
        ASTPrimary right = ensureIsa(binary.getSecond(), ASTPrimary.class);
        checkUnary(right, OPEN_PARENTHESIS, ASTBinaryNode.class);
        checkBinary(ensureIsa(right.getFirst(), ASTBinaryNode.class), MINUS, ASTPrimary.class, ASTPrimary.class);
    }

    /**
     * Tests cast expression of unary expression.
     */
    @Test
    public void testCastExpressionOfUnaryExpression() {
        ExpressionsParser parser = getExpressionsParser("varName");
        ASTNode node = parser.parseCastExpression();
        node.print();
        assertInstanceOf(ASTPrimary.class, node);
    }

    /**
     * Tests cast expression of unary expression, "as", and an intersection
     * type consisting solely of a data type name.
     */
    @Test
    public void testCastExpressionOfIntersectionType() {
        ExpressionsParser parser = getExpressionsParser("d as Double");
        ASTNode node = parser.parseCastExpression();
        node.print();
        checkBinary(ensureIsa(node, ASTBinaryNode.class), AS, ASTPrimary.class, ASTListNode.class);
    }

    /**
     * Tests nested cast expressions.
     */
    @Test
    public void testCastExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("\"2\" as Object as String & Serializable");
        ASTNode node = parser.parseCastExpression();
        node.print();

        ASTBinaryNode cast = ensureIsa(node, ASTBinaryNode.class);
        checkBinary(cast, AS, ASTBinaryNode.class, ASTListNode.class);
        ASTBinaryNode childNode = ensureIsa(cast.getFirst(), ASTBinaryNode.class);
        checkBinary(childNode, AS, ASTPrimary.class, ASTListNode.class);
    }

    /**
     * Tests unary expression of primary.
     */
    @Test
    public void testUnaryExpressionOfPrimary() {
        ExpressionsParser parser = getExpressionsParser("varName");
        ASTUnaryNode node = parser.parseUnaryExpression();
        node.print();
        checkUnary(node, ASTListNode.class);
    }

    /**
     * Tests unary expression of "-" and unary expression.
     */
    @Test
    public void testUnaryExpressionOfMinusUnary() {
        ExpressionsParser parser = getExpressionsParser("-1");
        ASTUnaryNode node = parser.parseUnaryExpression();
        node.print();
        checkUnary(node, MINUS, ASTPrimary.class, ASTIntegerLiteral.class);
    }

    /**
     * Tests unary expression of "~" and unary expression.
     */
    @Test
    public void testUnaryExpressionOfComplementUnary() {
        ExpressionsParser parser = getExpressionsParser("~bits");
        ASTUnaryNode node = parser.parseUnaryExpression();
        node.print();
        checkUnary(node, TILDE, ASTPrimary.class, ASTListNode.class);
    }

    /**
     * Tests unary expression of "!" and unary expression.
     */
    @Test
    public void testUnaryExpressionOfLogicalComplementUnary() {
        ExpressionsParser parser = getExpressionsParser("!false");
        ASTUnaryNode node = parser.parseUnaryExpression();
        node.print();
        checkUnary(node, EXCLAMATION, ASTPrimary.class, ASTBooleanLiteral.class);
    }

    /**
     * Tests nested unary expressions.
     */
    @Test
    public void testUnaryExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("~ - ~ - bits");
        ASTUnaryNode node = parser.parseUnaryExpression();
        node.print();
        checkUnary(node, TILDE, ASTUnaryExpression.class);

        ASTUnaryExpression childNode = (ASTUnaryExpression) node.getChildren().get(0);
        checkUnary(childNode, MINUS, ASTUnaryExpression.class);

        childNode = (ASTUnaryExpression) childNode.getChildren().get(0);
        checkUnary(childNode, TILDE, ASTUnaryExpression.class);

        childNode = (ASTUnaryExpression) childNode.getChildren().get(0);
        checkUnary(childNode, MINUS, ASTPrimary.class, ASTListNode.class);
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
        ASTUnaryNode node = parser.parseUnaryExpression();
        node.print();
        checkUnary(node, SWITCH, ASTBinaryNode.class);
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
        ASTNode node = parser.parseSwitchExpression();
        node.print();
        checkBinary(ensureIsa(node, ASTBinaryNode.class), SWITCH, ASTPrimary.class, ASTUnaryNode.class);
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
        ASTUnaryNode node = parser.parseSwitchExpressionBlock();
        node.print();
        checkUnary(node, ASTListNode.class);
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
        ASTListNode node = parser.parseSwitchExpressionRules();
        node.print();
        checkList(node, SWITCH_EXPR_RULES, ASTBinaryNode.class, 3);
    }

    /**
     * Tests Switch Expression Rule of Throw Statement.
     */
    @Test
    public void testSwitchExpressionRuleOfThrowStatement() {
        ExpressionsParser parser = getExpressionsParser("case 1 -> throw new TestException();");
        ASTBinaryNode node = parser.parseSwitchExpressionRule();
        node.print();
        checkBinary(node, ARROW, ASTSwitchLabel.class, ASTThrowStatement.class);
    }

    /**
     * Tests Switch Expression Rule of Block.
     */
    @Test
    public void testSwitchExpressionRuleOfBlock() {
        ExpressionsParser parser = getExpressionsParser("case 1 -> { use a + 1; }");
        ASTBinaryNode node = parser.parseSwitchExpressionRule();
        node.print();
        checkBinary(node, ARROW, ASTSwitchLabel.class, ASTBlock.class);
    }

    /**
     * Tests Switch Expression Rule of Expression.
     */
    @Test
    public void testSwitchExpressionRuleOfExpression() {
        ExpressionsParser parser = getExpressionsParser("case 1 -> a + 1;");
        ASTBinaryNode node = parser.parseSwitchExpressionRule();
        node.print();
        checkBinary(node, ARROW, ASTSwitchLabel.class, ASTBinaryNode.class);
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
        node.print();
        assertNull(node.getOperation());
        assertTrue(node.getChild().isPresent());
        assertInstanceOf(ASTRecordPattern.class, node.getChild().get());
        assertTrue(node.getGuard().isPresent());
    }

    /**
     * Tests Switch Label of Pattern.
     */
    @Test
    public void testSwitchLabelOfPattern() {
        ExpressionsParser parser = getExpressionsParser("Month(String name)");
        ASTSwitchLabel node = parser.parseSwitchLabel();
        node.print();
        assertNull(node.getOperation());
        assertTrue(node.getChild().isPresent());
        assertInstanceOf(ASTRecordPattern.class, node.getChild().get());
        assertFalse(node.getGuard().isPresent());
    }

    /**
     * Tests Switch Label of Case Constants.
     */
    @Test
    public void testSwitchLabelOfCaseConstants() {
        ExpressionsParser parser = getExpressionsParser("case MONDAY, TUESDAY, WEDNESDAY");
        ASTSwitchLabel node = parser.parseSwitchLabel();
        node.print();
        assertEquals(CASE, node.getOperation());
        assertTrue(node.getChild().isPresent());
        ASTListNode caseConstants = ensureIsa(node.getChild().get(), ASTListNode.class);
        checkList(caseConstants, CASE_CONSTANTS, ASTPrimary.class, 3);
        assertFalse(node.getGuard().isPresent());
    }

    /**
     * Tests Switch Label of Default.
     */
    @Test
    public void testSwitchLabelOfDefault() {
        ExpressionsParser parser = getExpressionsParser("default");
        ASTSwitchLabel node = parser.parseSwitchLabel();
        node.print();
        assertEquals(DEFAULT, node.getOperation());
        assertFalse(node.getChild().isPresent());
        assertFalse(node.getGuard().isPresent());
    }

    /**
     * Tests Case Constants.
     */
    @Test
    public void testCaseConstants() {
        ExpressionsParser parser = getExpressionsParser("RED, GREEN, RED | GREEN");
        ASTListNode node = parser.parseCaseConstants();
        node.print();
        checkList(node, CASE_CONSTANTS, ASTParentNode.class, 3);
    }

    /**
     * Tests Guard.
     */
    @Test
    public void testGuard() {
        ExpressionsParser parser = getExpressionsParser("when a == b");
        ASTUnaryNode node = parser.parseGuard();
        node.print();
        checkUnary(node, WHEN, ASTBinaryNode.class);
    }

    /**
     * Tests Pattern List.
     */
    @Test
    public void testPatternList() {
        ExpressionsParser parser = getExpressionsParser("Widget w, Sprocket s, XrayMachine x");
        ASTListNode node = parser.parsePatternList();
        node.print();
        checkList(node, PATTERNS, ASTTypePattern.class, 3);
    }

    /**
     * Tests Pattern of Type Pattern.
     */
    @Test
    public void testPatternOfTypePattern() {
        ExpressionsParser parser = getExpressionsParser("Widget w");
        ASTNode node = parser.parsePattern();
        node.print();
        assertInstanceOf(ASTTypePattern.class, node);
    }

    /**
     * Tests Pattern of Record Pattern.
     */
    @Test
    public void testPatternOfRecordPattern() {
        ExpressionsParser parser = getExpressionsParser("Order(Int line, Double amt)");
        ASTNode node = parser.parsePattern();
        node.print();
        assertInstanceOf(ASTRecordPattern.class, node);
    }

    /**
     * Tests nested Record Patterns.
     */
    @Test
    public void testRecordPatternNested() {
        ExpressionsParser parser = getExpressionsParser("Order(LineItem(Integer id, Double amt))");
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTRecordPattern node = parser.parseRecordPattern(dt);
        node.print();

        assertNull(node.getOperation());
        assertInstanceOf(ASTDataType.class, node.getDataType());
        assertTrue(node.getPatternList().isPresent());

        ASTListNode patternList = node.getPatternList().get();
        checkList(patternList, PATTERNS, ASTRecordPattern.class, 1);

        ASTNode innerPattern = patternList.getChildren().get(0);
        ASTRecordPattern innerRp = ensureIsa(innerPattern, ASTRecordPattern.class);
        assertNull(innerRp.getOperation());
        assertInstanceOf(ASTDataType.class, innerRp.getDataType());
        assertTrue(innerRp.getPatternList().isPresent());

        ASTListNode nested = innerRp.getPatternList().get();
        checkList(nested, PATTERNS, ASTTypePattern.class, 2);
    }

    /**
     * Tests record pattern.
     */
    @Test
    public void testRecordPattern() {
        ExpressionsParser parser = getExpressionsParser("Person(String first, String last)");
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTRecordPattern node = parser.parseRecordPattern(dt);
        node.print();

        assertNull(node.getOperation());
        assertInstanceOf(ASTDataType.class, node.getDataType());
        assertTrue(node.getPatternList().isPresent());
        ASTListNode patternList = node.getPatternList().get();
        checkList(patternList, PATTERNS, ASTTypePattern.class, 2);
    }

    /**
     * Tests type pattern with variable modifier.
     */
    @Test
    public void testTypePattern() {
        ExpressionsParser parser = getExpressionsParser("mut DataType id");
        ASTTypePattern node = parser.parseTypePattern();
        node.print();

        assertNull(node.getOperation());
        assertTrue(node.getVarModList().isPresent());
        ASTListNode varModList = node.getVarModList().get();
        checkList(varModList, VARIABLE_MODIFIERS, ASTModifierNode.class, 1);
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
        node.print();
        assertNull(node.getOperation());
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
        ASTListNode node = parser.parseArgumentList();
        node.print();
        checkList(node, ARGUMENTS, ASTUnaryNode.class, 1);
    }

    /**
     * Tests argument list of nested argument lists (here, just multiple arguments).
     */
    @Test
    public void testArgumentListNested() {
        ExpressionsParser parser = getExpressionsParser("a, 1, b + c");
        ASTListNode node = parser.parseArgumentList();
        node.print();
        checkList(node, ARGUMENTS, ASTUnaryNode.class, 3);
    }

    /**
     * Tests a give expression of an expression.
     */
    @Test
    public void testGiveExpressionNoGive() {
        ExpressionsParser parser = getExpressionsParser("x + 1");
        ASTUnaryNode node = parser.parseGiveExpression();
        node.print();
        checkUnary(node, ASTBinaryNode.class);
    }

    /**
     * Tests a give expression of "give" then an expression.
     */
    @Test
    public void testGiveExpressionWithGive() {
        ExpressionsParser parser = getExpressionsParser("give a.b");
        ASTUnaryNode node = parser.parseGiveExpression();
        node.print();
        checkUnary(node, GIVE, ASTPrimary.class);
    }

    /**
     * Tests primary of expression name.
     */
    @Test
    public void testPrimaryOfExpressionName() {
        ExpressionsParser parser = getExpressionsParser("a.b");
        ASTPrimary node = parser.parsePrimary();
        node.print();
        checkUnary(node, ASTListNode.class);
    }

    /**
     * Tests primary of literal.
     */
    @Test
    public void testPrimaryOfLiteral() {
        ExpressionsParser parser = getExpressionsParser("3.14");
        ASTPrimary node = parser.parsePrimary();
        node.print();
        checkUnary(node, ASTLiteral.class);
    }

    /**
     * Tests primary of class literal (data type).
     */
    @Test
    public void testPrimaryOfClassLiteralOfDataType() {
        ExpressionsParser parser = getExpressionsParser("spruce.lang.Comparable<String>[][].class");
        ASTPrimary node = parser.parsePrimary();
        node.print();
        checkUnary(node, ASTUnaryNode.class);
        ASTUnaryNode classLiteral = ensureIsa(node.getFirst(), ASTUnaryNode.class);
        checkUnary(classLiteral, CLASS, ASTDataType.class);
    }

    /**
     * Tests primary of "self".
     */
    @Test
    public void testPrimaryOfSelf() {
        ExpressionsParser parser = getExpressionsParser("self");
        ASTPrimary node = parser.parsePrimary();
        node.print();
        checkUnary(node, ASTSelf.class);
    }

    /**
     * Tests primary of parenthesized expression.
     */
    @Test
    public void testPrimaryOfParenthesizedExpression() {
        ExpressionsParser parser = getExpressionsParser("(a + b)");
        ASTPrimary node = parser.parsePrimary();
        node.print();
        checkUnary(node, OPEN_PARENTHESIS, ASTBinaryNode.class);
    }

    /**
     * Tests primary of element access.
     */
    @Test
    public void testPrimaryOfElementAccess() {
        ExpressionsParser parser = getExpressionsParser("a[1][2][3]");
        ASTPrimary node = parser.parsePrimary();
        node.print();
        checkUnary(node, ASTBinaryNode.class);
        ASTBinaryNode elementAccess = ensureIsa(node.getFirst(), ASTBinaryNode.class);
        checkBinaryPostorder(elementAccess,
                ASTPrimary.class, ASTPrimary.class, OPEN_BRACKET,
                ASTPrimary.class, OPEN_BRACKET,
                ASTPrimary.class, OPEN_BRACKET);
    }

    /**
     * Tests primary of field access of super.
     */
    @Test
    public void testPrimaryOfFieldAccessOfSuper() {
        ExpressionsParser parser = getExpressionsParser("super.superclassField");
        ASTPrimary node = parser.parsePrimary();
        node.print();
        checkUnary(node, ASTFieldAccess.class);
        ASTFieldAccess fa = ensureIsa(node.getFirst(), ASTFieldAccess.class);
        checkFieldAccess(fa, false, true, false);
    }

    /**
     * Tests primary of field access of type name and super.
     */
    @Test
    public void testPrimaryOfFieldAccessOfTypeNameSuper() {
        ExpressionsParser parser = getExpressionsParser("EnclosingClass.super.superclassField");
        ASTPrimary node = parser.parsePrimary();
        node.print();
        checkUnary(node, ASTFieldAccess.class);
        ASTFieldAccess fa = ensureIsa(node.getFirst(), ASTFieldAccess.class);
        checkFieldAccess(fa, true, true, false);
    }

    /**
     * Tests primary of field access of primary.
     */
    @Test
    public void testPrimaryOfFieldAccessOfPrimary() {
        ExpressionsParser parser = getExpressionsParser("method().field");
        ASTPrimary node = parser.parsePrimary();
        node.print();
        checkUnary(node, ASTFieldAccess.class);
        ASTFieldAccess fa = ensureIsa(node.getFirst(), ASTFieldAccess.class);
        checkFieldAccess(fa, false, false, true);
    }

    /**
     * Tests primary of method invocation, expression name.
     */
    @Test
    public void testPrimaryOfMethodInvocationOfExpressionName() {
        ExpressionsParser parser = getExpressionsParser("expr.name.methodName()");
        ASTPrimary node = parser.parsePrimary();
        node.print();

        checkUnary(node, ASTMethodInvocation.class);
        ASTMethodInvocation mi = ensureIsa(node.getFirst(), ASTMethodInvocation.class);
        checkMethodInvocation(mi, false, false, true, 
                false, false, true);
        ASTListNode exprName = mi.getExprName().get();
        checkList(exprName, EXPR_NAME_IDS, ASTIdentifier.class, 2);
    }

    /**
     * Tests primary of method invocation, expression name and type arguments.
     */
    @Test
    public void testPrimaryOfMethodInvocationOfExpressionNameTypeArguments() {
        ExpressionsParser parser = getExpressionsParser("expr.name.<T>methodName(one)");
        ASTPrimary node = parser.parsePrimary();
        node.print();

        checkUnary(node, ASTMethodInvocation.class);
        ASTMethodInvocation mi = ensureIsa(node.getFirst(), ASTMethodInvocation.class);
        checkMethodInvocation(mi, false, false, true,
                false, true, true);
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
        node.print();
        checkUnary(node, ASTMethodInvocation.class);
        ASTMethodInvocation mi = ensureIsa(node.getFirst(), ASTMethodInvocation.class);
        checkMethodInvocation(mi, false, false, false,
                false, false, true);
        node.print();
    }

    /**
     * Tests primary of method invocation starting with <code>super</code>.
     */
    @Test
    public void testPrimaryOfMethodInvocationOfSuper() {
        ExpressionsParser parser = getExpressionsParser("super.<T>inheritedMethod(\"super\")");
        ASTPrimary node = parser.parsePrimary();
        node.print();
        checkUnary(node, ASTMethodInvocation.class);
        ASTMethodInvocation mi = ensureIsa(node.getFirst(), ASTMethodInvocation.class);
        checkMethodInvocation(mi, false, true, false,
                false, true, true);
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
        node.print();
        checkUnary(node, ASTMethodInvocation.class);
        ASTMethodInvocation mi = ensureIsa(node.getFirst(), ASTMethodInvocation.class);
        checkMethodInvocation(mi, true, true, false,
                false, true, true);
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
        node.print();
        checkUnary(node, ASTArrayCreationExpression.class);
    }

    /**
     * Tests primary of class instance creation expression.
     */
    @Test
    public void testPrimaryOfClassInstanceCreationExpression() {
        ExpressionsParser parser = getExpressionsParser("new Team(25, \"Dodgers\")");
        ASTPrimary node = parser.parsePrimary();
        node.print();
        checkUnary(node, ASTUnqualifiedClassInstanceCreationExpression.class);
    }

    /**
     * Tests primary of qualified class instance creation expression.
     */
    @Test
    public void testPrimaryOfClassInstanceCreationExpressionQualified() {
        ExpressionsParser parser = getExpressionsParser("league.new Team(25, \"Dodgers\")");
        ASTPrimary node = parser.parsePrimary();
        node.print();
        checkUnary(node, ASTBinaryNode.class);
    }

    /**
     * Tests primary of type name, ".", and self.
     */
    @Test
    public void testPrimaryOfTypeNameDotSelf() {
        ExpressionsParser parser = getExpressionsParser("qualified.type.self");
        ASTPrimary node = parser.parsePrimary();
        node.print();
        checkUnary(node, ASTBinaryNode.class);
        ASTBinaryNode typeNameSelf = ensureIsa(node.getFirst(), ASTBinaryNode.class);
        checkBinary(typeNameSelf, DOT, ASTListNode.class, ASTSelf.class);
    }

    /**
     * Tests primary of method reference starting with "super".
     */
    @Test
    public void testPrimaryOfMethodReferenceSuper() {
        ExpressionsParser parser = getExpressionsParser("super::<String>methodName");
        ASTPrimary node = parser.parsePrimary();
        node.print();
        checkUnary(node, ASTMethodReference.class);
        ASTMethodReference mRef = ensureIsa(node.getFirst(), ASTMethodReference.class);
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
        node.print();

        checkUnary(node, ASTMethodReference.class);
        ASTMethodReference mRef = ensureIsa(node.getFirst(), ASTMethodReference.class);
        checkMethodReference(mRef, false, false, false,
                true,false, false);
        ASTDataType dt = mRef.getDataType().get();
        checkSimple(dt, ASTListNode.class);
    }

    /**
     * Tests Primary of Method Reference of Expression Name.
     */
    @Test
    public void testPrimaryOfMethodReferenceOfExpressionName() {
        ExpressionsParser parser = getExpressionsParser("spruce.lang.String::size");
        ASTPrimary node = parser.parsePrimary();
        node.print();
        checkUnary(node, ASTMethodReference.class);
        ASTMethodReference mRef = ensureIsa(node.getFirst(), ASTMethodReference.class);
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
        node.print();
        checkUnary(node, ASTMethodReference.class);
        ASTMethodReference mRef = ensureIsa(node.getFirst(), ASTMethodReference.class);
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
        node.print();
        checkUnary(node, ASTMethodReference.class);
        ASTMethodReference mRef = ensureIsa(node.getFirst(), ASTMethodReference.class);
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
        node.print();
        checkUnary(node, ASTMethodReference.class);
        ASTMethodReference mRef = ensureIsa(node.getFirst(), ASTMethodReference.class);
        checkMethodReference(mRef, true, true, false,
                false,false, false);
    }

    /**
     * Tests nested primary expressions, including Class Instance Creation
     * Expressions, Method Invocations, Field Accesses, and Element Accesses.
     */
    @Test
    public void testPrimaryOfNested() {
        ExpressionsParser parser = getExpressionsParser("new Foo()[i].field1.method1()[j].field2.<T>method2(1).new Bar()");
        ASTPrimary node = parser.parsePrimary();
        node.print();

        checkUnary(node, ASTBinaryNode.class);
        ASTBinaryNode cice = ensureIsa(node.getFirst(), ASTBinaryNode.class);
        ASTPrimary pMethod2 = ensureIsa(cice.getFirst(), ASTPrimary.class);
        ASTUnqualifiedClassInstanceCreationExpression outerCice = ensureIsa(cice.getSecond(), ASTUnqualifiedClassInstanceCreationExpression.class);
        assertEquals(NEW, outerCice.getOperation());
        
        checkUnary(pMethod2, ASTMethodInvocation.class);
        ASTMethodInvocation method2 = ensureIsa(pMethod2.getFirst(), ASTMethodInvocation.class);
        checkMethodInvocation(method2, false, false, false,
                true, true, true);
        ASTIdentifier methodName2 = method2.getIdentifier();
        assertEquals("method2", methodName2.getValue());

        ASTPrimary pFieldAccess2 = method2.getPrimary().get();
        checkUnary(pFieldAccess2, ASTFieldAccess.class);
        ASTFieldAccess fieldAccess2 = ensureIsa(pFieldAccess2.getFirst(), ASTFieldAccess.class);
        checkFieldAccess(fieldAccess2, false, false, true);
        ASTIdentifier fieldName2 = fieldAccess2.getIdentifier();
        assertEquals("field2", fieldName2.getValue());

        ASTPrimary pJElementAccess = fieldAccess2.getPrimary().get();
        checkUnary(pJElementAccess, ASTBinaryNode.class);
        ASTBinaryNode jElementAccess = ensureIsa(pJElementAccess.getFirst(), ASTBinaryNode.class);
        checkBinary(jElementAccess, OPEN_BRACKET, ASTPrimary.class, ASTPrimary.class);

        ASTPrimary pMethod1 = ensureIsa(jElementAccess.getFirst(), ASTPrimary.class);
        checkUnary(pMethod1, ASTMethodInvocation.class);
        ASTMethodInvocation method1 = ensureIsa(pMethod1.getFirst(), ASTMethodInvocation.class);
        checkMethodInvocation(method1, false, false, false,
                true, false, true);
        ASTIdentifier methodName1 = method1.getIdentifier();
        assertEquals("method1", methodName1.getValue());

        ASTPrimary pFieldAccess1 = method1.getPrimary().get();
        checkUnary(pFieldAccess1, ASTFieldAccess.class);
        ASTFieldAccess fieldAccess1 = ensureIsa(pFieldAccess1.getFirst(), ASTFieldAccess.class);
        checkFieldAccess(fieldAccess1, false, false, true);
        ASTIdentifier fieldName1 = fieldAccess1.getIdentifier();
        assertEquals("field1", fieldName1.getValue());

        ASTPrimary pIElementAccess = fieldAccess1.getPrimary().get();
        checkUnary(pIElementAccess, ASTBinaryNode.class);
        ASTBinaryNode iElementAccess = ensureIsa(pIElementAccess.getFirst(), ASTBinaryNode.class);
        checkBinary(iElementAccess, OPEN_BRACKET, ASTPrimary.class, ASTPrimary.class);

        ASTPrimary pInnerCice = ensureIsa(iElementAccess.getFirst(), ASTPrimary.class);
        checkUnary(pInnerCice, ASTUnqualifiedClassInstanceCreationExpression.class);
    }

    /**
     * Tests unqualified class instance creation expression of type arguments and type to instantiate.
     */
    @Test
    public void testUnqualifiedClassInstanceCreationExpressionOfTypeArguments() {
        ExpressionsParser parser = getExpressionsParser("new <String> MyClass()");
        ASTUnqualifiedClassInstanceCreationExpression node = parser.parseUnqualifiedClassInstanceCreationExpression();
        node.print();
        checkUcice(node, true);
    }

    /**
     * Tests class instance creation expression of unqualified class instance
     * creation expression.
     */
    @Test
    public void testClassInstanceCreationExpressionOfUCICE() {
        ExpressionsParser parser = getExpressionsParser("new MyClass(1, \"one\")");
        ASTParentNode node = parser.parseClassInstanceCreationExpression();
        node.print();
        assertInstanceOf(ASTUnqualifiedClassInstanceCreationExpression.class, node);
    }

    /**
     * Tests unqualified class instance creation expression of type to instantiate and argument list.
     */
    @Test
    public void testUnqualifiedClassInstanceCreationExpressionOfArgumentList() {
        ExpressionsParser parser = getExpressionsParser("new MyClass(1, \"one\")");
        ASTUnqualifiedClassInstanceCreationExpression node = parser.parseUnqualifiedClassInstanceCreationExpression();
        node.print();
        checkUcice(node, false);
    }

    /**
     * Tests type to instantiate of type name.
     */
    @Test
    public void testTypeToInstantiateOfTypeName() {
        ExpressionsParser parser = getExpressionsParser("MyClass");
        ASTTypeToInstantiate node = parser.parseTypeToInstantiate();
        node.print();
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
        node.print();
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
        node.print();
        checkArrayCreationExpression(node, true, false, false);
    }

    /**
     * Tests array creation expression of dim exprs and dims.
     */
    @Test
    public void testArrayCreationExpressionOfDimExprsDims() {
        ExpressionsParser parser = getExpressionsParser("new String[10][]");
        ASTArrayCreationExpression node = parser.parseArrayCreationExpression();
        node.print();
        checkArrayCreationExpression(node, true, true, false);
    }

    /**
     * Tests array creation expression of dims and array initializer.
     */
    @Test
    public void testArrayCreationExpressionOfDimsArrayInitializer() {
        ExpressionsParser parser = getExpressionsParser("new String[] {\"one\", \"two\", \"three\"}");
        ASTArrayCreationExpression node = parser.parseArrayCreationExpression();
        node.print();
        checkArrayCreationExpression(node, false, true, true);
    }

    /**
     * Tests dim exprs of dim expr.
     */
    @Test
    public void testDimExprsDimExpr() {
        ExpressionsParser parser = getExpressionsParser("[1][2][3]");
        ASTListNode node = parser.parseDimExprs();
        node.print();
        checkList(node, DIM_EXPRS, ASTUnaryNode.class, 3);
    }

    /**
     * Tests dim expr of expression.
     */
    @Test
    public void testDimExprOfExpression() {
        ExpressionsParser parser = getExpressionsParser("[x+y]");
        ASTUnaryNode node = parser.parseDimExpr();
        node.print();
        checkUnary(node, OPEN_BRACKET, ASTBinaryNode.class);
    }

    /**
     * Tests array initializer of just empty braces.
     */
    @Test
    public void testArrayInitializerEmpty() {
        ExpressionsParser parser = getExpressionsParser("{}");
        ASTUnaryNode node = parser.parseArrayInitializer();
        node.print();
        assertEquals(OPEN_BRACE, node.getOperation());
        ASTListNode varInitList = ensureIsa(node.getFirst(), ASTListNode.class);
        checkList(varInitList, VARIABLE_INITIALIZERS, ASTNode.class, 0);
    }

    /**
     * Tests array initializer of a variable initializer list.
     */
    @Test
    public void testArrayInitializerOfVariableInitializerList() {
        ExpressionsParser parser = getExpressionsParser("{x + 1, y - 2}");
        ASTUnaryNode node = parser.parseArrayInitializer();
        node.print();
        assertEquals(OPEN_BRACE, node.getOperation());
        assertInstanceOf(ASTListNode.class, node.getFirst());
    }

    /**
     * Tests variable initializer list of variable initializer.
     */
    @Test
    public void testVariableInitializerListOfVariableInitializer() {
        ExpressionsParser parser = getExpressionsParser("i + 1");
        ASTListNode node = parser.parseVariableInitializerList();
        node.print();
        checkList(node, VARIABLE_INITIALIZERS, ASTBinaryNode.class, 1);
    }

    /**
     * Tests variable initializer list of "," and variable initializer.
     */
    @Test
    public void testVariableInitializerListOfComma() {
        ExpressionsParser parser = getExpressionsParser("x + 1, y - 1");
        ASTListNode node = parser.parseVariableInitializerList();
        node.print();
        checkList(node, VARIABLE_INITIALIZERS, ASTBinaryNode.class, 2);
    }

    /**
     * Tests nested variable initializer lists (here, just multiple variable initializers).
     */
    @Test
    public void testVariableInitializerListNested() {
        ExpressionsParser parser = getExpressionsParser("self, count + 1, sumSoFar + value");
        ASTListNode node = parser.parseVariableInitializerList();
        node.print();
        checkList(node, VARIABLE_INITIALIZERS, ASTNode.class, 3);
    }

    /**
     * Tests variable initializer of expression.
     */
    @Test
    public void testVariableInitializerOfExpression() {
        ExpressionsParser parser = getExpressionsParser("a + b");
        ASTNode node = parser.parseVariableInitializer();
        node.print();
        assertInstanceOf(ASTBinaryNode.class, node);
    }

    /**
     * Tests variable initializer of array initializer.
     */
    @Test
    public void testVariableInitializerOfArrayInitializer() {
        ExpressionsParser parser = getExpressionsParser("{1, 2, 3}");
        ASTNode node = parser.parseVariableInitializer();
        node.print();
        assertInstanceOf(ASTUnaryNode.class, node);
    }

    /**
     * Tests a class literal.  Parses a DataType first.
     */
    @Test
    public void testClassLiteral() {
        ExpressionsParser parser = getExpressionsParser("Outer.Inner.class");
        ASTDataType dataType = parser.getTypesParser().parseDataType();
        ASTUnaryNode node = parser.parseClassLiteral(dataType);
        node.print();
        checkUnary(node, CLASS, ASTDataType.class);
    }

    /**
     * Tests "self".
     */
    @Test
    public void testSelf() {
        ExpressionsParser parser = getExpressionsParser("self");
        ASTSelf node = parser.parseSelf();
        node.print();
        checkIs(node, ASTSelf.class);
    }

    /**
     * Tests "super".
     */
    @Test
    public void testSuper() {
        ExpressionsParser parser = getExpressionsParser("super");
        ASTSuper node = parser.parseSuper();
        node.print();
        checkIs(node, ASTSuper.class);
    }

    /**
     * Helper method to test <code>ASTArrayCreationExpression</code> attributes.
     */
    private static void checkArrayCreationExpression(ASTArrayCreationExpression ace, boolean isDimExprsPresent,
                                                    boolean isDimsPresent, boolean isArrayInitializerPresent) {
        assertEquals(NEW, ace.getOperation());
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
        assertEquals(NEW, ucice.getOperation());
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
        assertEquals(DOT, fa.getOperation());
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
     * @param isArgsListPresent Whether the Argument List should be present.
     */
    private static void checkMethodInvocation(ASTMethodInvocation mi, boolean isTypeNamePresent,
                                              boolean isSuperPresent, boolean isExprNamePresent, boolean isPrimaryPresent,
                                              boolean isTypeArgsPresent, boolean isArgsListPresent) {
        assertEquals(OPEN_PARENTHESIS, mi.getOperation());
        assertEquals(isTypeNamePresent, mi.getTypeName().isPresent());
        assertEquals(isSuperPresent, mi.getSooper().isPresent());
        assertEquals(isExprNamePresent, mi.getExprName().isPresent());
        assertEquals(isPrimaryPresent, mi.getPrimary().isPresent());
        assertEquals(isTypeArgsPresent, mi.getTypeArgs().isPresent());
        assertNotNull(mi.getIdentifier());
        assertEquals(isArgsListPresent, mi.getArgumentList().isPresent());
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
        assertEquals(DOUBLE_COLON, mr.getOperation());
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
}
