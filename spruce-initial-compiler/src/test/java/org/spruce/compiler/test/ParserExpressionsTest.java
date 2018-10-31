package org.spruce.compiler.test;

import java.util.Arrays;

import org.spruce.compiler.ast.*;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.parser.Parser;
import org.spruce.compiler.scanner.Scanner;
import static org.spruce.compiler.scanner.TokenType.*;
import static org.spruce.compiler.test.ParserTestUtility.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * All tests for the parser related to expressions.
 */
public class ParserExpressionsTest
{
    /**
     * Tests expression of expression (no incr/decr).
     */
    @Test
    public void testExpressionOfExpressionNoIncrDecr()
    {
        Parser parser = new Parser(new Scanner("count := 1"));
        ASTExpression node = parser.parseExpression();
        checkSimple(node, ASTExpressionNoIncrDecr.class);
        node.collapseThenPrint();
    }

    /**
     * Tests expression of post-increment expression.
     */
    @Test
    public void testExpressionOfPostIncrementExpression()
    {
        Parser parser = new Parser(new Scanner("i.j++"));
        ASTExpression node = parser.parseExpression();
        checkSimple(node, ASTPostfixExpression.class);
        ASTPostfixExpression childNode = (ASTPostfixExpression) node.getChildren().get(0);
        checkSimple(childNode, ASTLeftHandSide.class, INCREMENT);
        node.collapseThenPrint();
    }

    /**
     * Tests expression of post-decrement expression.
     */
    @Test
    public void testExpressionOfPostDecrementExpression()
    {
        Parser parser = new Parser(new Scanner("i.j--"));
        ASTExpression node = parser.parseExpression();
        checkSimple(node, ASTPostfixExpression.class);
        ASTPostfixExpression childNode = (ASTPostfixExpression) node.getChildren().get(0);
        checkSimple(childNode, ASTLeftHandSide.class, DECREMENT);
        node.collapseThenPrint();
    }

    /**
     * Tests expression of pre-increment expression.
     */
    @Test
    public void testExpressionOfPreIncrementExpression()
    {
        Parser parser = new Parser(new Scanner("++i.j"));
        ASTExpression node = parser.parseExpression();
        checkSimple(node, ASTPrefixExpression.class);
        ASTPrefixExpression childNode = (ASTPrefixExpression) node.getChildren().get(0);
        checkSimple(childNode, ASTLeftHandSide.class, INCREMENT);
        node.collapseThenPrint();
    }

    /**
     * Tests expression of pre-decrement expression.
     */
    @Test
    public void testExpressionOfPreDecrementExpression()
    {
        Parser parser = new Parser(new Scanner("--i.j"));
        ASTExpression node = parser.parseExpression();
        checkSimple(node, ASTPrefixExpression.class);
        ASTPrefixExpression childNode = (ASTPrefixExpression) node.getChildren().get(0);
        checkSimple(childNode, ASTLeftHandSide.class, DECREMENT);
        node.collapseThenPrint();
    }

    /**
     * Tests expression (no incr/decr) of assignment expression.
     */
    @Test
    public void testExpressionNoIncrDecrOfAssignmentExpression()
    {
        Parser parser = new Parser(new Scanner("x := 1"));
        ASTExpressionNoIncrDecr node = parser.parseExpressionNoIncrDecr();
        checkSimple(node, ASTAssignmentExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests assignment expression of conditional expression.
     */
    @Test
    public void testAssignmentExpressionOfConditionalExpression()
    {
        Parser parser = new Parser(new Scanner("a ? b : c"));
        ASTAssignmentExpression node = parser.parseAssignmentExpression();
        checkSimple(node, ASTConditionalExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests assignment expression of assignment made of lhs, ":=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfAssignment()
    {
        Parser parser = new Parser(new Scanner("a := 1"));
        ASTAssignmentExpression node = parser.parseAssignmentExpression();
        checkAssignmentExpressionLeftAssociative(node, ASSIGNMENT);
        node.collapseThenPrint();
    }

    /**
     * Tests assignment expression of assignment made of lhs, "+=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfPlusEquals()
    {
        Parser parser = new Parser(new Scanner("a += 1"));
        ASTAssignmentExpression node = parser.parseAssignmentExpression();
        checkAssignmentExpressionLeftAssociative(node, PLUS_EQUALS);
        node.collapseThenPrint();
    }

    /**
     * Tests assignment expression of assignment made of lhs, "-=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfMinusEquals()
    {
        Parser parser = new Parser(new Scanner("a -= 1"));
        ASTAssignmentExpression node = parser.parseAssignmentExpression();
        checkAssignmentExpressionLeftAssociative(node, MINUS_EQUALS);
        node.collapseThenPrint();
    }

    /**
     * Tests assignment expression of assignment made of lhs, "*=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfStarEquals()
    {
        Parser parser = new Parser(new Scanner("a *= 1"));
        ASTAssignmentExpression node = parser.parseAssignmentExpression();
        checkAssignmentExpressionLeftAssociative(node, STAR_EQUALS);
        node.collapseThenPrint();
    }

    /**
     * Tests assignment expression of assignment made of lhs, "/=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfSlashEquals()
    {
        Parser parser = new Parser(new Scanner("a /= 1"));
        ASTAssignmentExpression node = parser.parseAssignmentExpression();
        checkAssignmentExpressionLeftAssociative(node, SLASH_EQUALS);
        node.collapseThenPrint();
    }

    /**
     * Tests assignment expression of assignment made of lhs, "%=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfPercentEquals()
    {
        Parser parser = new Parser(new Scanner("a %= 1"));
        ASTAssignmentExpression node = parser.parseAssignmentExpression();
        checkAssignmentExpressionLeftAssociative(node, PERCENT_EQUALS);
        node.collapseThenPrint();
    }

    /**
     * Tests assignment expression of assignment made of lhs, "<<=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfShiftLeftEquals()
    {
        Parser parser = new Parser(new Scanner("a <<= 1"));
        ASTAssignmentExpression node = parser.parseAssignmentExpression();
        checkAssignmentExpressionLeftAssociative(node, SHIFT_LEFT_EQUALS);
        node.collapseThenPrint();
    }

    /**
     * Tests assignment expression of assignment made of lhs, ">>=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfShiftRightEquals()
    {
        Parser parser = new Parser(new Scanner("a >>= 1"));
        ASTAssignmentExpression node = parser.parseAssignmentExpression();
        checkAssignmentExpressionLeftAssociative(node, SHIFT_RIGHT_EQUALS);
        node.collapseThenPrint();
    }

    /**
     * Tests assignment expression of assignment made of lhs, "|=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfOrEquals()
    {
        Parser parser = new Parser(new Scanner("a |= 1"));
        ASTAssignmentExpression node = parser.parseAssignmentExpression();
        checkAssignmentExpressionLeftAssociative(node, OR_EQUALS);
        node.collapseThenPrint();
    }

    /**
     * Tests assignment expression of assignment made of lhs, "&=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfAndEquals()
    {
        Parser parser = new Parser(new Scanner("a &= 1"));
        ASTAssignmentExpression node = parser.parseAssignmentExpression();
        checkAssignmentExpressionLeftAssociative(node, AND_EQUALS);
        node.collapseThenPrint();
    }

    /**
     * Tests assignment expression of assignment made of lhs, "^=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfXorEquals()
    {
        Parser parser = new Parser(new Scanner("a ^= 1"));
        ASTAssignmentExpression node = parser.parseAssignmentExpression();
        checkAssignmentExpressionLeftAssociative(node, XOR_EQUALS);
        node.collapseThenPrint();
    }

    /**
     * Tests assignment expression of assignment made of lhs, ">>>=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfUnsignedRightShiftEquals()
    {
        Parser parser = new Parser(new Scanner("a >>>= 1"));
        ASTAssignmentExpression node = parser.parseAssignmentExpression();
        checkAssignmentExpressionLeftAssociative(node, UNSIGNED_SHIFT_RIGHT_EQUALS);
        node.collapseThenPrint();
    }

    /**
     * Tests nested assignment expressions.
     */
    @Test
    public void testAssignmentExpressionNested()
    {
        Parser parser = new Parser(new Scanner("a := b += c -= d"));
        ASTAssignmentExpression node = parser.parseAssignmentExpression();
        checkSimple(node, ASTAssignment.class);
        ASTAssignment assignment = (ASTAssignment) node.getChildren().get(0);
        checkBinary(assignment, ASSIGNMENT, ASTLeftHandSide.class, ASTAssignmentExpression.class);

        ASTAssignmentExpression childNode = (ASTAssignmentExpression) assignment.getChildren().get(1);
        checkSimple(childNode, ASTAssignment.class);
        assignment = (ASTAssignment) childNode.getChildren().get(0);
        checkBinary(assignment, PLUS_EQUALS, ASTLeftHandSide.class, ASTAssignmentExpression.class);

        childNode = (ASTAssignmentExpression) assignment.getChildren().get(1);
        checkSimple(childNode, ASTAssignment.class);
        assignment = (ASTAssignment) childNode.getChildren().get(0);
        checkBinary(assignment, MINUS_EQUALS, ASTLeftHandSide.class, ASTAssignmentExpression.class);

        childNode = (ASTAssignmentExpression) assignment.getChildren().get(1);
        checkSimple(childNode, ASTConditionalExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests the Conditional#ToLeftHandSide method with primary expressions
     * that aren't left hand sides: literal, <code>this</code>.
     */
    @Test
    public void testConditionalToLeftHandSideOfLiteral()
    {
        for (String code : Arrays.asList("1", "this"))
        {
            Parser parser = new Parser(new Scanner(code));
            ASTConditionalExpression condExpr = parser.parseConditionalExpression();
            assertThrows(CompileException.class, condExpr::getLeftHandSide, "Error at code \"" + code + "\".");
        }
    }

    /**
     * Tests the Conditional#ToLeftHandSide method with primary expressions
     * that are left hand sides: expression name.
     */
    @Test
    public void testConditionalToLeftHandSideOfExpressionName()
    {
        Parser parser = new Parser(new Scanner("expr.name"));
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
    public void testConditionalToLeftHandSideOfElementAccess()
    {
        Parser parser = new Parser(new Scanner("getArray()[1]"));
        ASTConditionalExpression condExpr = parser.parseConditionalExpression();
        ASTLeftHandSide lhs = condExpr.getLeftHandSide();
        checkSimple(lhs, ASTElementAccess.class);
        lhs.collapseThenPrint();
    }

    /**
     * Tests prefix expression of decrement and left hand side.
     */
    @Test
    public void testPrefixExpressionOfDecrement()
    {
        Parser parser = new Parser(new Scanner("--i"));
        ASTPrefixExpression node = parser.parsePrefixExpression();
        checkSimple(node, ASTLeftHandSide.class, DECREMENT);
        node.collapseThenPrint();
    }

    /**
     * Tests prefix expression of increment and left hand side.
     */
    @Test
    public void testPrefixExpressionOfIncrement()
    {
        Parser parser = new Parser(new Scanner("++i"));
        ASTPrefixExpression node = parser.parsePrefixExpression();
        checkSimple(node, ASTLeftHandSide.class, INCREMENT);
        node.collapseThenPrint();
    }

    /**
     * Tests left hand side of expression name.
     */
    @Test
    public void testLeftHandSideOfExpressionName()
    {
        Parser parser = new Parser(new Scanner("expr.name"));
        ASTLeftHandSide node = parser.parseLeftHandSide();
        checkSimple(node, ASTExpressionName.class);
        node.collapseThenPrint();
    }

    /**
     * Tests conditional expression of logical or expression.
     */
    @Test
    public void testConditionalExpressionOfLogicalOrExpression()
    {
        Parser parser = new Parser(new Scanner("a || b"));
        ASTConditionalExpression node = parser.parseConditionalExpression();
        checkSimple(node, ASTLogicalOrExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests conditional expression of "?" and ":" and logical or expression.
     */
    @Test
    public void testConditionalExpression()
    {
        Parser parser = new Parser(new Scanner("condition ? valueIfTrue : valueIfFalse"));
        ASTConditionalExpression node = parser.parseConditionalExpression();
        checkTrinary(node, QUESTION_MARK, ASTLogicalOrExpression.class, ASTLogicalOrExpression.class, ASTConditionalExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested conditional expressions.
     */
    @Test
    public void testConditionalExpressionNested()
    {
        Parser parser = new Parser(new Scanner("a || b ? \"one\" : c || d ? \"two\" : e || f ? \"three\" : \"four\""));
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
    public void testLogicalOrExpressionOfLogicalAndExpression()
    {
        Parser parser = new Parser(new Scanner("a ^: b"));
        ASTLogicalOrExpression node = parser.parseLogicalOrExpression();
        checkSimple(node, ASTLogicalXorExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests logical or expression of "|:" and logical xor expression.
     */
    @Test
    public void testLogicalOrExpressionOfEager()
    {
        Parser parser = new Parser(new Scanner("test |: elseThis"));
        ASTLogicalOrExpression node = parser.parseLogicalOrExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(LOGICAL_OR), ASTLogicalOrExpression.class, ASTLogicalXorExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests logical or expression of "||" and logical xor expression.
     */
    @Test
    public void testLogicalOrExpressionOfConditional()
    {
        Parser parser = new Parser(new Scanner("alreadyDone || test"));
        ASTLogicalOrExpression node = parser.parseLogicalOrExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(CONDITIONAL_OR), ASTLogicalOrExpression.class, ASTLogicalXorExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested logical or expressions.
     */
    @Test
    public void testLogicalOrExpressionNested()
    {
        Parser parser = new Parser(new Scanner("a && b |: c ^: d || e &: f"));
        ASTLogicalOrExpression node = parser.parseLogicalOrExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(CONDITIONAL_OR, LOGICAL_OR), ASTLogicalOrExpression.class, ASTLogicalXorExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests logical xor expression of logical and expression.
     */
    @Test
    public void testLogicalXorExpressionOfLogicalAndExpression()
    {
        Parser parser = new Parser(new Scanner("a && b"));
        ASTLogicalXorExpression node = parser.parseLogicalXorExpression();
        checkSimple(node, ASTLogicalAndExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests logical xor expression of "^:" and logical and expression.
     */
    @Test
    public void testLogicalXorExpression()
    {
        Parser parser = new Parser(new Scanner("test ^: thisAlso"));
        ASTLogicalXorExpression node = parser.parseLogicalXorExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(LOGICAL_XOR), ASTLogicalXorExpression.class, ASTLogicalAndExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested logical xor expressions.
     */
    @Test
    public void testLogicalXorExpressionNested()
    {
        Parser parser = new Parser(new Scanner("a && b ^: c &: d ^: e && f"));
        ASTLogicalXorExpression node = parser.parseLogicalXorExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(LOGICAL_XOR, LOGICAL_XOR), ASTLogicalXorExpression.class, ASTLogicalAndExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests logical and expression of relational expression.
     */
    @Test
    public void testLogicalAndExpressionOfRelationalExpression()
    {
        Parser parser = new Parser(new Scanner("a = b"));
        ASTLogicalAndExpression node = parser.parseLogicalAndExpression();
        checkSimple(node, ASTRelationalExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests logical and expression of "&&" and relational expression.
     */
    @Test
    public void testLogicalAndExpressionOfConditional()
    {
        Parser parser = new Parser(new Scanner("test && notDone"));
        ASTLogicalAndExpression node = parser.parseLogicalAndExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(CONDITIONAL_AND), ASTLogicalAndExpression.class, ASTRelationalExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests logical and expression of "&:" and relational expression.
     */
    @Test
    public void testLogicalAndExpressionOfEager()
    {
        Parser parser = new Parser(new Scanner("test &: thisAlso"));
        ASTLogicalAndExpression node = parser.parseLogicalAndExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(LOGICAL_AND), ASTLogicalAndExpression.class, ASTRelationalExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested logical and expressions.
     */
    @Test
    public void testLogicalAndExpressionNested()
    {
        Parser parser = new Parser(new Scanner("a < b &: c <= d && e > f"));
        ASTLogicalAndExpression node = parser.parseLogicalAndExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(CONDITIONAL_AND, LOGICAL_AND), ASTLogicalAndExpression.class, ASTRelationalExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests relational expression of compare expression.
     */
    @Test
    public void testRelationalExpressionOfCompareExpression()
    {
        Parser parser = new Parser(new Scanner("a <=> b"));
        ASTRelationalExpression node = parser.parseRelationalExpression();
        checkSimple(node, ASTCompareExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests relational expression of "&lt;" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfLessThan()
    {
        Parser parser = new Parser(new Scanner("a.value < b.value"));
        ASTRelationalExpression node = parser.parseRelationalExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(LESS_THAN), ASTRelationalExpression.class, ASTCompareExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests relational expression of "&lt;=" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfLessThanOrEqual()
    {
        Parser parser = new Parser(new Scanner("2 <= 2"));
        ASTRelationalExpression node = parser.parseRelationalExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(LESS_THAN_OR_EQUAL), ASTRelationalExpression.class, ASTCompareExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests relational expression of "&gt;" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfGreaterThan()
    {
        Parser parser = new Parser(new Scanner("a.value > b.value"));
        ASTRelationalExpression node = parser.parseRelationalExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(GREATER_THAN), ASTRelationalExpression.class, ASTCompareExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests relational expression of "&gt;=" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfGreaterThanOrEqual()
    {
        Parser parser = new Parser(new Scanner("2 >= 2"));
        ASTRelationalExpression node = parser.parseRelationalExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(GREATER_THAN_OR_EQUAL), ASTRelationalExpression.class, ASTCompareExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests relational expression of "=" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfEqual()
    {
        Parser parser = new Parser(new Scanner("test = SUCCESS"));
        ASTRelationalExpression node = parser.parseRelationalExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(EQUAL), ASTRelationalExpression.class, ASTCompareExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests relational expression of "!=" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfNotEqual()
    {
        Parser parser = new Parser(new Scanner("test != FAILURE"));
        ASTRelationalExpression node = parser.parseRelationalExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(NOT_EQUAL), ASTRelationalExpression.class, ASTCompareExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests relational expression of "instanceof" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfInstanceof()
    {
        Parser parser = new Parser(new Scanner("node instanceof ASTRelationalExpression"));
        ASTRelationalExpression node = parser.parseRelationalExpression();
        checkBinary(node, INSTANCEOF, ASTRelationalExpression.class, ASTDataType.class);
        ASTRelationalExpression child = (ASTRelationalExpression) node.getChildren().get(0);
        checkSimple(child, ASTCompareExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests relational expression of "is" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfIs()
    {
        Parser parser = new Parser(new Scanner("obj is other"));
        ASTRelationalExpression node = parser.parseRelationalExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(IS), ASTRelationalExpression.class, ASTCompareExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests relational expression of "isnt" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfIsnt()
    {
        Parser parser = new Parser(new Scanner("obj isnt null"));
        ASTRelationalExpression node = parser.parseRelationalExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(ISNT), ASTRelationalExpression.class, ASTCompareExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested relational expressions.
     */
    @Test
    public void testRelationalExpressionNested()
    {
        Parser parser = new Parser(new Scanner("a < b <=> c <= d"));
        ASTRelationalExpression node = parser.parseRelationalExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(LESS_THAN_OR_EQUAL, LESS_THAN), ASTRelationalExpression.class, ASTCompareExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests compare expression of bitwise or expression.
     */
    @Test
    public void testCompareExpressionOfBitwiseOrExpression()
    {
        Parser parser = new Parser(new Scanner("a | b"));
        ASTCompareExpression node = parser.parseCompareExpression();
        checkSimple(node, ASTBitwiseOrExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests compare expression of "&lt;=&gt;" and bitwise or expression.
     */
    @Test
    public void testCompareExpression()
    {
        Parser parser = new Parser(new Scanner("a.value <=> b.value"));
        ASTCompareExpression node = parser.parseCompareExpression();
        checkBinary(node, COMPARISON, ASTBitwiseOrExpression.class, ASTBitwiseOrExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests bitwise or expression of bitwise xor expression.
     */
    @Test
    public void testBitwiseOrExpressionOfBitwiseXorExpression()
    {
        Parser parser = new Parser(new Scanner("a ^ b"));
        ASTBitwiseOrExpression node = parser.parseBitwiseOrExpression();
        checkSimple(node, ASTBitwiseXorExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests bitwise or expression of "|" and bitwise xor expression.
     */
    @Test
    public void testBitwiseOrExpression()
    {
        Parser parser = new Parser(new Scanner("color | blueMask"));
        ASTBitwiseOrExpression node = parser.parseBitwiseOrExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(BITWISE_OR), ASTBitwiseOrExpression.class, ASTBitwiseXorExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested bitwise or expressions.
     */
    @Test
    public void testBitwiseOrExpressionNested()
    {
        Parser parser = new Parser(new Scanner("red | blue | yellow ^ green"));
        ASTBitwiseOrExpression node = parser.parseBitwiseOrExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(BITWISE_OR, BITWISE_OR), ASTBitwiseOrExpression.class, ASTBitwiseXorExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests bitwise xor expression of bitwise and expression.
     */
    @Test
    public void testBitwiseXorExpressionOfBitwiseAndExpression()
    {
        Parser parser = new Parser(new Scanner("a & b"));
        ASTBitwiseXorExpression node = parser.parseBitwiseXorExpression();
        checkSimple(node, ASTBitwiseAndExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests bitwise xor expression of "^" and bitwise and expression.
     */
    @Test
    public void testBitwiseXorExpression()
    {
        Parser parser = new Parser(new Scanner("color ^ blueMask"));
        ASTBitwiseXorExpression node = parser.parseBitwiseXorExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(BITWISE_XOR), ASTBitwiseXorExpression.class, ASTBitwiseAndExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested bitwise xor expressions.
     */
    @Test
    public void testBitwiseXorExpressionNested()
    {
        Parser parser = new Parser(new Scanner("red ^ blue & yellow ^ green"));
        ASTBitwiseXorExpression node = parser.parseBitwiseXorExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(BITWISE_XOR, BITWISE_XOR), ASTBitwiseXorExpression.class, ASTBitwiseAndExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests bitwise and expression of shift expression.
     */
    @Test
    public void testBitwiseAndExpressionOfShiftExpression()
    {
        Parser parser = new Parser(new Scanner("a << b"));
        ASTBitwiseAndExpression node = parser.parseBitwiseAndExpression();
        checkSimple(node, ASTShiftExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests bitwise and expression of "&" and shift expression.
     */
    @Test
    public void testBitwiseAndExpression()
    {
        Parser parser = new Parser(new Scanner("color & blueMask"));
        ASTBitwiseAndExpression node = parser.parseBitwiseAndExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(BITWISE_AND), ASTBitwiseAndExpression.class, ASTShiftExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested bitwise and expressions.
     */
    @Test
    public void testBitwiseAndExpressionNested()
    {
        Parser parser = new Parser(new Scanner("red + blue & blueGreenMask & greenRedMask"));
        ASTBitwiseAndExpression node = parser.parseBitwiseAndExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(BITWISE_AND, BITWISE_AND), ASTBitwiseAndExpression.class, ASTShiftExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests shift expression of additive expression.
     */
    @Test
    public void testShiftExpressionOfAdditiveExpression()
    {
        Parser parser = new Parser(new Scanner("a + b"));
        ASTShiftExpression node = parser.parseShiftExpression();
        checkSimple(node, ASTAdditiveExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests shift expression of "<<" and additive expression.
     */
    @Test
    public void testShiftExpressionOfLeftShift()
    {
        Parser parser = new Parser(new Scanner("1 << 2"));
        ASTShiftExpression node = parser.parseShiftExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(SHIFT_LEFT), ASTShiftExpression.class, ASTAdditiveExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests shift expression of ">>" and additive expression.
     */
    @Test
    public void testShiftExpressionOfRightShift()
    {
        Parser parser = new Parser(new Scanner("2048 >> 2"));
        ASTShiftExpression node = parser.parseShiftExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(SHIFT_RIGHT), ASTShiftExpression.class, ASTAdditiveExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests shift expression of ">>>" and additive expression.
     */
    @Test
    public void testShiftExpressionOfUnsignedRightShift()
    {
        Parser parser = new Parser(new Scanner("unsigned >>> amount"));
        ASTShiftExpression node = parser.parseShiftExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(UNSIGNED_SHIFT_RIGHT), ASTShiftExpression.class, ASTAdditiveExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested shift expressions.
     */
    @Test
    public void testShiftExpressionNested()
    {
        Parser parser = new Parser(new Scanner("-2 << 3 + 4 >> 5 >>> 1"));
        ASTShiftExpression node = parser.parseShiftExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(UNSIGNED_SHIFT_RIGHT, SHIFT_RIGHT, SHIFT_LEFT), ASTShiftExpression.class, ASTAdditiveExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests additive expression of multiplicative expression.
     */
    @Test
    public void testAdditiveExpressionOfMultiplicativeExpression()
    {
        Parser parser = new Parser(new Scanner("a * b"));
        ASTAdditiveExpression node = parser.parseAdditiveExpression();
        checkSimple(node, ASTMultiplicativeExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests additive expression of "+" and multiplicative expression.
     */
    @Test
    public void testAdditiveExpressionOfPlus()
    {
        Parser parser = new Parser(new Scanner("-1 + 2"));
        ASTAdditiveExpression node = parser.parseAdditiveExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(PLUS), ASTAdditiveExpression.class, ASTMultiplicativeExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests additive expression of "-" and multiplicative expression.
     */
    @Test
    public void testAdditiveExpressionOfMinus()
    {
        Parser parser = new Parser(new Scanner("finish - start"));
        ASTAdditiveExpression node = parser.parseAdditiveExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(MINUS), ASTAdditiveExpression.class, ASTMultiplicativeExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested additive expressions.
     */
    @Test
    public void testAdditiveExpressionNested()
    {
        Parser parser = new Parser(new Scanner("-2 + 3 * 4 - 5"));
        ASTAdditiveExpression node = parser.parseAdditiveExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(MINUS, PLUS), ASTAdditiveExpression.class, ASTMultiplicativeExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests multiplicative expression of unary expression.
     */
    @Test
    public void testMultiplicativeExpressionOfUnaryExpression()
    {
        Parser parser = new Parser(new Scanner("varName"));
        ASTMultiplicativeExpression node = parser.parseMultiplicativeExpression();
        checkSimple(node, ASTCastExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests multiplicative expression of "*" and unary expression.
     */
    @Test
    public void testMultiplicativeExpressionOfTimes()
    {
        Parser parser = new Parser(new Scanner("a * b"));
        ASTMultiplicativeExpression node = parser.parseMultiplicativeExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(STAR), ASTMultiplicativeExpression.class, ASTCastExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests multiplicative expression of "/" and unary expression.
     */
    @Test
    public void testMultiplicativeExpressionOfDivide()
    {
        Parser parser = new Parser(new Scanner("i / -1"));
        ASTMultiplicativeExpression node = parser.parseMultiplicativeExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(SLASH), ASTMultiplicativeExpression.class, ASTCastExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests multiplicative expression of "%" and unary expression.
     */
    @Test
    public void testMultiplicativeExpressionOfModulus()
    {
        Parser parser = new Parser(new Scanner("index % len"));
        ASTMultiplicativeExpression node = parser.parseMultiplicativeExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(PERCENT), ASTMultiplicativeExpression.class, ASTCastExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested multiplicative expressions.
     */
    @Test
    public void testMultiplicativeExpressionNested()
    {
        Parser parser = new Parser(new Scanner("5 * 6 / 3 % 7"));
        ASTMultiplicativeExpression node = parser.parseMultiplicativeExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(PERCENT, SLASH, STAR), ASTMultiplicativeExpression.class, ASTCastExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests parenthesized multiplicative expressions.
     */
    @Test
    public void testMultiplicativeExpressionOfParenthesizedExpressions()
    {
        Parser parser = new Parser(new Scanner("(x + 1)*(x - 1)"));
        ASTMultiplicativeExpression node = parser.parseMultiplicativeExpression();
        checkBinaryLeftAssociative(node, Arrays.asList(STAR), ASTMultiplicativeExpression.class, ASTCastExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests cast expression of unary expression.
     */
    @Test
    public void testCastExpressionOfUnaryExpression()
    {
        Parser parser = new Parser(new Scanner("varName"));
        ASTCastExpression node = parser.parseCastExpression();
        checkSimple(node, ASTUnaryExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests cast expression of unary expression, "as", and an intersection
     * type consisting solely of a data type name.
     */
    @Test
    public void testCastExpressionOfIntersectionType()
    {
        Parser parser = new Parser(new Scanner("d as Double"));
        ASTCastExpression node = parser.parseCastExpression();
        checkBinary(node, AS, ASTUnaryExpression.class, ASTIntersectionType.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested cast expressions.
     */
    @Test
    public void testCastExpressionNested()
    {
        Parser parser = new Parser(new Scanner("\"2\" as Object as String & Serializable"));
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
    public void testUnaryExpressionOfPrimary()
    {
        Parser parser = new Parser(new Scanner("varName"));
        ASTUnaryExpression node = parser.parseUnaryExpression();
        checkSimple(node, ASTPrimary.class);
        node.collapseThenPrint();
    }

    /**
     * Tests unary expression of "-" and unary expression.
     */
    @Test
    public void testUnaryExpressionOfMinusUnary()
    {
        Parser parser = new Parser(new Scanner("-1"));
        ASTUnaryExpression node = parser.parseUnaryExpression();
        checkUnary(node, MINUS, ASTUnaryExpression.class, ASTPrimary.class);
        node.collapseThenPrint();
    }

    /**
     * Tests unary expression of "~" and unary expression.
     */
    @Test
    public void testUnaryExpressionOfComplementUnary()
    {
        Parser parser = new Parser(new Scanner("~bits"));
        ASTUnaryExpression node = parser.parseUnaryExpression();
        checkUnary(node, BITWISE_COMPLEMENT, ASTUnaryExpression.class, ASTPrimary.class);
        node.collapseThenPrint();
    }

    /**
     * Tests unary expression of "!" and unary expression.
     */
    @Test
    public void testUnaryExpressionOfLogicalComplementUnary()
    {
        Parser parser = new Parser(new Scanner("!false"));
        ASTUnaryExpression node = parser.parseUnaryExpression();
        checkUnary(node, LOGICAL_COMPLEMENT, ASTUnaryExpression.class, ASTPrimary.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested unary expressions.
     */
    @Test
    public void testUnaryExpressionNested()
    {
        Parser parser = new Parser(new Scanner("~ - ~ - bits"));
        ASTUnaryExpression node = parser.parseUnaryExpression();
        checkSimple(node, ASTUnaryExpression.class, BITWISE_COMPLEMENT);

        ASTUnaryExpression childNode = (ASTUnaryExpression) node.getChildren().get(0);
        checkSimple(node, ASTUnaryExpression.class, BITWISE_COMPLEMENT);

        childNode = (ASTUnaryExpression) childNode.getChildren().get(0);
        checkSimple(node, ASTUnaryExpression.class, BITWISE_COMPLEMENT);

        childNode = (ASTUnaryExpression) childNode.getChildren().get(0);
        checkUnary(childNode, MINUS, ASTUnaryExpression.class, ASTPrimary.class);
        node.collapseThenPrint();
    }

    /**
     * Tests argument list of expression.
     */
    @Test
    public void testArgumentListOfExpression()
    {
        Parser parser = new Parser(new Scanner("i++"));
        ASTArgumentList node = parser.parseArgumentList();
        checkSimple(node, ASTExpression.class, COMMA);
        node.collapseThenPrint();
    }

    /**
     * Tests argument list of nested argument lists (here, just multiple arguments).
     */
    @Test
    public void testArgumentListNested()
    {
        Parser parser = new Parser(new Scanner("a, 1, b + c"));
        ASTArgumentList node = parser.parseArgumentList();
        checkList(node, COMMA, ASTExpression.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests primary of expression name.
     */
    @Test
    public void testPrimaryOfExpressionName()
    {
        Parser parser = new Parser(new Scanner("a.b"));
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTExpressionName.class);
        node.collapseThenPrint();
    }

    /**
     * Tests primary of literal.
     */
    @Test
    public void testPrimaryOfLiteral()
    {
        Parser parser = new Parser(new Scanner("3.14"));
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTLiteral.class);
        node.collapseThenPrint();
    }

    /**
     * Tests primary of class literal (data type).
     */
    @Test
    public void testPrimaryOfClassLiteralOfDataType()
    {
        Parser parser = new Parser(new Scanner("spruce.lang.Comparable<String>[][].class"));
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTClassLiteral.class);
        ASTClassLiteral classLiteral = (ASTClassLiteral) node.getChildren().get(0);
        checkSimple(classLiteral, ASTDataType.class, CLASS);
        node.collapseThenPrint();
    }

    /**
     * Tests primary of "this".
     */
    @Test
    public void testPrimaryOfThis()
    {
        Parser parser = new Parser(new Scanner("this"));
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTThis.class);
        node.collapseThenPrint();
    }

    /**
     * Tests primary of parenthesized expression.
     */
    @Test
    public void testPrimaryOfParenthesizedExpression()
    {
        Parser parser = new Parser(new Scanner("(a + b)"));
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTExpressionNoIncrDecr.class, OPEN_PARENTHESIS);
        node.collapseThenPrint();
    }

    /**
     * Tests primary of element access.
     */
    @Test
    public void testPrimaryOfElementAccess()
    {
        Parser parser = new Parser(new Scanner("a[1][2][3]"));
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
    public void testPrimaryOfFieldAccessOfSuper()
    {
        Parser parser = new Parser(new Scanner("super.superclassField"));
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
    public void testPrimaryOfFieldAccessOfTypeNameSuper()
    {
        Parser parser = new Parser(new Scanner("EnclosingClass.super.superclassField"));
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
    public void testPrimaryOfFieldAccessOfPrimary()
    {
        Parser parser = new Parser(new Scanner("method().field"));
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
    public void testPrimaryOfMethodInvocationOfExpressionName()
    {
        Parser parser = new Parser(new Scanner("expr.name.methodName()"));
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
    public void testPrimaryOfMethodInvocationOfExpressionNameTypeArguments()
    {
        Parser parser = new Parser(new Scanner("expr.name.<T>methodName(one)"));
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTMethodInvocation.class);
        ASTMethodInvocation mi = (ASTMethodInvocation) node.getChildren().get(0);
        checkNary(mi, OPEN_PARENTHESIS,ASTExpressionName.class, ASTTypeArguments.class, ASTIdentifier.class, ASTArgumentList.class);
        ASTIdentifier methodName = (ASTIdentifier) mi.getChildren().get(2);
        assertEquals("methodName", methodName.getValue());
        node.collapseThenPrint();
    }

    /**
     * Tests primary of method invocation, simple name.
     */
    @Test
    public void testPrimaryOfMethodInvocationOfSimpleName()
    {
        Parser parser = new Parser(new Scanner("methodName(helperMethod(i), (a + b), ++j)"));
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
    public void testPrimaryOfMethodInvocationOfSuper()
    {
        Parser parser = new Parser(new Scanner("super.<T>inheritedMethod(\"super\")"));
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
    public void testPrimaryOfMethodInvocationOfTypeNameSuper()
    {
        Parser parser = new Parser(new Scanner("org.test.EnclosingClass.super.<T>inheritedMethod(\"super\")"));
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
    public void testPrimaryOfArrayCreationExpression()
    {
        Parser parser = new Parser(new Scanner("new spruce.lang.String[23]"));
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTArrayCreationExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests primary of class instance creation expression.
     */
    @Test
    public void testPrimaryOfClassInstanceCreationExpression()
    {
        Parser parser = new Parser(new Scanner("new Team(25, \"Dodgers\")"));
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTClassInstanceCreationExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests primary of qualified class instance creation expression.
     */
    @Test
    public void testPrimaryOfClassInstanceCreationExpressionQualified()
    {
        Parser parser = new Parser(new Scanner("league.new Team(25, \"Dodgers\")"));
        ASTPrimary node = parser.parsePrimary();
        checkSimple(node, ASTClassInstanceCreationExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests primary of type name, ".", and this.
     */
    @Test
    public void testPrimaryOfTypeNameDotThis()
    {
        Parser parser = new Parser(new Scanner("qualified.type.this"));
        ASTPrimary node = parser.parsePrimary();
        checkBinary(node, DOT, ASTTypeName.class, ASTThis.class);
        node.collapseThenPrint();
    }

    /**
     * Tests primary of method reference starting with "super".
     */
    @Test
    public void testPrimaryOfMethodReferenceSuper()
    {
        Parser parser = new Parser(new Scanner("super::<String>methodName"));
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
    public void testPrimaryOfConstructorReference()
    {
        Parser parser = new Parser(new Scanner("spruce.lang.String::new"));
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
    public void testPrimaryOfMethodReferenceOfExpressionName()
    {
        Parser parser = new Parser(new Scanner("spruce.lang.String::size"));
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
    public void testPrimaryOfMethodReferenceOfDataType()
    {
        Parser parser = new Parser(new Scanner("Comparator<String>::compare;"));
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
    public void testPrimaryOfMethodReferenceOfPrimary()
    {
        Parser parser = new Parser(new Scanner("(\"a\" + \"b\")::length;"));
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
    public void testPrimaryOfMethodReferenceOfTypeNameSuper()
    {
        Parser parser = new Parser(new Scanner("type.Name.super::length;"));
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
    public void testPrimaryOfNested()
    {
        Parser parser = new Parser(new Scanner("new Foo()[i].field1.method1()[j].field2.<T>method2(1).new Bar()"));
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
    public void testUnqualifiedClassInstanceCreationExpressionOfTypeArguments()
    {
        Parser parser = new Parser(new Scanner("new <String> MyClass()"));
        ASTUnqualifiedClassInstanceCreationExpression node = parser.parseUnqualifiedClassInstanceCreationExpression();
        checkBinary(node, NEW, ASTTypeArguments.class, ASTTypeToInstantiate.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class instance creation expression of unqualified cice.
     */
    @Test
    public void testClassInstanceCreationExpressionOfUCICE()
    {
        Parser parser = new Parser(new Scanner("new MyClass(1, \"one\")"));
        ASTClassInstanceCreationExpression node = parser.parseClassInstanceCreationExpression();
        checkSimple(node, ASTUnqualifiedClassInstanceCreationExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests unqualified class instance creation expression of type to instantiate and argument list.
     */
    @Test
    public void testUnqualifiedClassInstanceCreationExpressionOfArgumentList()
    {
        Parser parser = new Parser(new Scanner("new MyClass(1, \"one\")"));
        ASTUnqualifiedClassInstanceCreationExpression node = parser.parseUnqualifiedClassInstanceCreationExpression();
        checkBinary(node, NEW, ASTTypeToInstantiate.class, ASTArgumentList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests type to instantiate of type name.
     */
    @Test
    public void testTypeToInstantiateOfTypeName()
    {
        Parser parser = new Parser(new Scanner("MyClass"));
        ASTTypeToInstantiate node = parser.parseTypeToInstantiate();
        checkSimple(node, ASTTypeName.class);
        node.collapseThenPrint();
    }

    /**
     * Tests type to instantiate of type name and type arguments or diamond.
     */
    @Test
    public void testTypeToInstantiateTypeNameOfTypeArgumentsOrDiamond()
    {
        Parser parser = new Parser(new Scanner("MyClass<T>"));
        ASTTypeToInstantiate node = parser.parseTypeToInstantiate();
        checkBinary(node, ASTTypeName.class, ASTTypeArgumentsOrDiamond.class);
        node.collapseThenPrint();
    }

    /**
     * Tests type arguments or diamond of type arguments.
     */
    @Test
    public void testTypeArgumentsOrDiamondOfTypeArguments()
    {
        Parser parser = new Parser(new Scanner("<T, U>"));
        ASTTypeArgumentsOrDiamond node = parser.parseTypeArgumentsOrDiamond();
        checkSimple(node, ASTTypeArguments.class);
        node.collapseThenPrint();
    }

    /**
     * Tests type arguments or diamond of diamond.
     */
    @Test
    public void testTypeArgumentsOrDiamondOfDiamond()
    {
        Parser parser = new Parser(new Scanner("<>"));
        ASTTypeArgumentsOrDiamond node = parser.parseTypeArgumentsOrDiamond();
        checkEmpty(node, LESS_THAN);
        node.collapseThenPrint();
    }

    /**
     * Tests array creation expression of dim exprs.
     */
    @Test
    public void testArrayCreationExpressionOfDimExprs()
    {
        Parser parser = new Parser(new Scanner("new String[10]"));
        ASTArrayCreationExpression node = parser.parseArrayCreationExpression();
        checkBinary(node, NEW, ASTTypeToInstantiate.class, ASTDimExprs.class);
        node.collapseThenPrint();
    }

    /**
     * Tests array creation expression of dim exprs and dims.
     */
    @Test
    public void testArrayCreationExpressionOfDimExprsDims()
    {
        Parser parser = new Parser(new Scanner("new String[10][]"));
        ASTArrayCreationExpression node = parser.parseArrayCreationExpression();
        checkTrinary(node, NEW, ASTTypeToInstantiate.class, ASTDimExprs.class, ASTDims.class);
        node.collapseThenPrint();
    }

    /**
     * Tests array creation expression of dims and array initializer.
     */
    @Test
    public void testArrayCreationExpressionOfDimsArrayInitializer()
    {
        Parser parser = new Parser(new Scanner("new String[] {\"one\", \"two\", \"three\"}"));
        ASTArrayCreationExpression node = parser.parseArrayCreationExpression();
        checkTrinary(node, NEW, ASTTypeToInstantiate.class, ASTDims.class, ASTArrayInitializer.class);
        node.collapseThenPrint();
    }

    /**
     * Tests dim exprs of dim expr.
     */
    @Test
    public void testDimExprsDimExpr()
    {
        Parser parser = new Parser(new Scanner("[1][2][3]"));
        ASTDimExprs node = parser.parseDimExprs();
        checkList(node, null, ASTDimExpr.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests dim expr of expression.
     */
    @Test
    public void testDimExprOfExpression()
    {
        Parser parser = new Parser(new Scanner("[x++]"));
        ASTDimExpr node = parser.parseDimExpr();
        checkSimple(node, ASTExpression.class, OPEN_BRACKET);
        node.collapseThenPrint();
    }

    /**
     * Tests array initializer of just empty braces.
     */
    @Test
    public void testArrayInitializerEmpty()
    {
        Parser parser = new Parser(new Scanner("{}"));
        ASTArrayInitializer node = parser.parseArrayInitializer();
        checkEmpty(node, OPEN_BRACE);
        node.collapseThenPrint();
    }

    /**
     * Tests array initializer of a variable initializer list.
     */
    @Test
    public void testArrayInitializerOfVariableInitializerList()
    {
        Parser parser = new Parser(new Scanner("{x + 1, y - 2}"));
        ASTArrayInitializer node = parser.parseArrayInitializer();
        checkSimple(node, ASTVariableInitializerList.class, OPEN_BRACE);
        node.collapseThenPrint();
    }

    /**
     * Tests variable initializer list of variable initializer.
     */
    @Test
    public void testVariableInitializerListOfVariableInitializer()
    {
        Parser parser = new Parser(new Scanner("i + 1"));
        ASTVariableInitializerList node = parser.parseVariableInitializerList();
        checkSimple(node, ASTVariableInitializer.class, COMMA);
        node.collapseThenPrint();
    }

    /**
     * Tests variable initializer list of "," and variable initializer.
     */
    @Test
    public void testVariableInitializerListOfComma()
    {
        Parser parser = new Parser(new Scanner("x + 1, y - 1"));
        ASTVariableInitializerList node = parser.parseVariableInitializerList();
        checkList(node, COMMA, ASTVariableInitializer.class, 2);
        node.collapseThenPrint();
    }

    /**
     * Tests nested variable initializer lists (here, just multiple variable initializers).
     */
    @Test
    public void testVariableInitializerListNested()
    {
        Parser parser = new Parser(new Scanner("this, count + 1, sumSoFar + value"));
        ASTVariableInitializerList node = parser.parseVariableInitializerList();
        checkList(node, COMMA, ASTVariableInitializer.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests variable initializer of expression (no incr/decr).
     */
    @Test
    public void testVariableInitializerOfExpressionNoIncrDecr()
    {
        Parser parser = new Parser(new Scanner("a + b"));
        ASTVariableInitializer node = parser.parseVariableInitializer();
        checkSimple(node, ASTExpressionNoIncrDecr.class);
        node.collapseThenPrint();
    }

    /**
     * Tests variable initializer of array initializer.
     */
    @Test
    public void testVariableInitializerOfArrayInitializer()
    {
        Parser parser = new Parser(new Scanner("{1, 2, 3}"));
        ASTVariableInitializer node = parser.parseVariableInitializer();
        checkSimple(node, ASTArrayInitializer.class);
        node.collapseThenPrint();
    }
}
