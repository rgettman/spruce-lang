package org.spruce.compiler.test;

import java.util.Arrays;
import java.util.List;

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
    }

    /**
     * Tests expression of post-increment expression.
     */
    @Test
    public void testExpressionOfPostIncrementExpression()
    {
        Parser parser = new Parser(new Scanner("i.j++"));
        ASTExpression node = parser.parseExpression();

        assertNull(node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTPostfixExpression);
        ASTPostfixExpression childNode = (ASTPostfixExpression) child;
        assertEquals(INCREMENT, childNode.getOperation());

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

        assertNull(node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTPostfixExpression);
        ASTPostfixExpression childNode = (ASTPostfixExpression) child;
        assertEquals(DECREMENT, childNode.getOperation());

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

        assertNull(node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTPrefixExpression);
        ASTPrefixExpression childNode = (ASTPrefixExpression) child;
        assertEquals(INCREMENT, childNode.getOperation());

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

        assertNull(node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTPrefixExpression);
        ASTPrefixExpression childNode = (ASTPrefixExpression) child;
        assertEquals(DECREMENT, childNode.getOperation());

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
    }

    /**
     * Tests nested assignment expressions.
     */
    @Test
    public void testAssignmentExpressionNested()
    {
        Parser parser = new Parser(new Scanner("a := b += c -= d"));
        ASTAssignmentExpression node = parser.parseAssignmentExpression();

        assertNull(node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());
        assertTrue(children.get(0) instanceof ASTAssignment);

        ASTAssignment assignment = (ASTAssignment) children.get(0);
        assertEquals(ASSIGNMENT, assignment.getOperation());
        children = assignment.getChildren();
        assertEquals(2, children.size());
        List<Class<?>> expectedClasses = Arrays.asList(ASTLeftHandSide.class, ASTAssignmentExpression.class);
        compareClasses(expectedClasses, children);

        ASTAssignmentExpression childNode = (ASTAssignmentExpression) children.get(1);
        assertNull(childNode.getOperation());
        children = childNode.getChildren();
        assertEquals(1, children.size());
        assertTrue(children.get(0) instanceof ASTAssignment);

        assignment = (ASTAssignment) children.get(0);
        assertEquals(PLUS_EQUALS, assignment.getOperation());
        children = assignment.getChildren();
        compareClasses(expectedClasses, children);

        childNode = (ASTAssignmentExpression) children.get(1);
        assertNull(childNode.getOperation());
        children = childNode.getChildren();
        assertEquals(1, children.size());
        assertTrue(children.get(0) instanceof ASTAssignment);

        assignment = (ASTAssignment) children.get(0);
        assertEquals(MINUS_EQUALS, assignment.getOperation());
        children = assignment.getChildren();
        compareClasses(expectedClasses, children);

        childNode = (ASTAssignmentExpression) children.get(1);
        assertNull(childNode.getOperation());
        children = childNode.getChildren();
        assertEquals(1, children.size());
        assertEquals(ASTConditionalExpression.class, children.get(0).getClass());

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

        assertNull(lhs.getOperation());
        List<ASTNode> children = lhs.getChildren();
        assertEquals(1, children.size());

        lhs.collapse();
        lhs.print();
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

        assertNull(lhs.getOperation());
        List<ASTNode> children = lhs.getChildren();
        assertEquals(1, children.size());

        lhs.collapse();
        lhs.print();
    }

    /**
     * Tests prefix expression of decrement and left hand side.
     */
    @Test
    public void testPrefixExpressionOfDecrement()
    {
        Parser parser = new Parser(new Scanner("--i"));
        ASTPrefixExpression node = parser.parsePrefixExpression();

        assertEquals(DECREMENT, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTLeftHandSide);

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

        assertEquals(INCREMENT, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTLeftHandSide);

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

        assertNull(node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTExpressionName);

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
    }

    /**
     * Tests conditional expression of "?" and ":" and logical or expression.
     */
    @Test
    public void testConditionalExpression()
    {
        Parser parser = new Parser(new Scanner("condition ? valueIfTrue : valueIfFalse"));
        ASTConditionalExpression node = parser.parseConditionalExpression();

        assertEquals(QUESTION_MARK, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(3, children.size());
        List<Class<?>> expectedClasses = Arrays.asList(ASTLogicalOrExpression.class, ASTLogicalOrExpression.class, ASTConditionalExpression.class);
        compareClasses(expectedClasses, children);

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

        assertEquals(QUESTION_MARK, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(3, children.size());
        List<Class<?>> expectedClasses = Arrays.asList(ASTLogicalOrExpression.class, ASTLogicalOrExpression.class, ASTConditionalExpression.class);
        compareClasses(expectedClasses, children);

        ASTConditionalExpression childNode = (ASTConditionalExpression) children.get(2);
        assertEquals(QUESTION_MARK, childNode.getOperation());
        children = childNode.getChildren();
        compareClasses(expectedClasses, children);

        childNode = (ASTConditionalExpression) children.get(2);
        assertEquals(QUESTION_MARK, childNode.getOperation());
        children = childNode.getChildren();
        compareClasses(expectedClasses, children);

        childNode = (ASTConditionalExpression) children.get(2);
        assertNull(childNode.getOperation());
        children = childNode.getChildren();
        assertEquals(1, children.size());

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
    }

    /**
     * Tests relational expression of "instanceof" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfInstanceof()
    {
        Parser parser = new Parser(new Scanner("node instanceof ASTRelationalExpression"));
        ASTRelationalExpression node = parser.parseRelationalExpression();

        ASTParentNode child = node;
        List<ASTNode> children = node.getChildren();

        assertEquals(INSTANCEOF, child.getOperation());
        assertEquals(2, children.size());
        List<Class<?>> expectedClasses = Arrays.asList(ASTRelationalExpression.class, ASTDataType.class);
        compareClasses(expectedClasses, children);

        child = (ASTParentNode) children.get(0);
        assertNull(child.getOperation());
        children = child.getChildren();
        assertEquals(1, children.size());
        assertTrue(children.get(0) instanceof ASTCompareExpression);

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
    }

    /**
     * Tests parenthesized multiplicative expressions.
     */
    @Test
    public void testMultiplicativeExpressionOfParenthesizedExpressions()
    {
        Parser parser = new Parser(new Scanner("(x + 1)*(x - 1)"));
        ASTMultiplicativeExpression node = parser.parseMultiplicativeExpression();

        assertEquals(STAR, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(2, children.size());
        List<Class<?>> expectedClasses = Arrays.asList(ASTMultiplicativeExpression.class, ASTCastExpression.class);
        compareClasses(expectedClasses, children);

        ASTMultiplicativeExpression child = (ASTMultiplicativeExpression) children.get(0);
        assertNull(child.getOperation());
        assertEquals(1, child.getChildren().size());
        assertTrue(child.getChildren().get(0) instanceof ASTCastExpression);

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
    }

    /**
     * Tests nested cast expressions.
     */
    @Test
    public void testCastExpressionNested()
    {
        Parser parser = new Parser(new Scanner("\"2\" as Object as String & Serializable"));
        ASTCastExpression node = parser.parseCastExpression();

        assertEquals(AS, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(2, children.size());
        List<Class<?>> expectedClasses = Arrays.asList(ASTCastExpression.class, ASTIntersectionType.class);
        compareClasses(expectedClasses, children);

        ASTCastExpression childNode = (ASTCastExpression) children.get(0);
        assertEquals(AS, childNode.getOperation());
        children = childNode.getChildren();
        assertEquals(2, children.size());
        expectedClasses = Arrays.asList(ASTUnaryExpression.class, ASTIntersectionType.class);
        compareClasses(expectedClasses, children);

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
    }

    /**
     * Tests nested unary expressions.
     */
    @Test
    public void testUnaryExpressionNested()
    {
        Parser parser = new Parser(new Scanner("~ - ~ - bits"));
        ASTUnaryExpression node = parser.parseUnaryExpression();

        assertEquals(BITWISE_COMPLEMENT, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());
        assertEquals(ASTUnaryExpression.class, children.get(0).getClass());

        ASTUnaryExpression childNode = (ASTUnaryExpression) children.get(0);
        assertEquals(MINUS, childNode.getOperation());
        children = childNode.getChildren();
        assertEquals(1, children.size());
        assertEquals(ASTUnaryExpression.class, children.get(0).getClass());

        childNode = (ASTUnaryExpression) children.get(0);
        assertEquals(BITWISE_COMPLEMENT, childNode.getOperation());
        children = childNode.getChildren();
        assertEquals(1, children.size());
        assertEquals(ASTUnaryExpression.class, children.get(0).getClass());

        childNode = (ASTUnaryExpression) children.get(0);
        assertEquals(MINUS, childNode.getOperation());
        children = childNode.getChildren();
        assertEquals(1, children.size());
        assertEquals(ASTUnaryExpression.class, children.get(0).getClass());

        childNode = (ASTUnaryExpression) children.get(0);
        assertNull(childNode.getOperation());
        children = childNode.getChildren();
        assertEquals(1, children.size());
        assertEquals(ASTPrimary.class, children.get(0).getClass());

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

        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());
        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTExpression);

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
        checkBinaryLeftAssociative(node, Arrays.asList(COMMA, COMMA), ASTArgumentList.class, ASTExpression.class);
    }

    /**
     * Tests primary of expression name.
     */
    @Test
    public void testPrimaryOfExpressionName()
    {
        Parser parser = new Parser(new Scanner("a.b"));
        ASTPrimary node = parser.parsePrimary();

        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());
        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTExpressionName);

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

        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());
        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTLiteral);

        node.collapseThenPrint();
    }

    /**
     * Tests primary of class literal.
     */
    @Test
    public void testPrimaryOfClassLiteral()
    {
        Parser parser = new Parser(new Scanner("spruce.lang.String.class"));
        ASTPrimary node = parser.parsePrimary();

        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());
        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTClassLiteral);

        node.collapseThenPrint();
    }

    /**
     * Tests primary of class literal (array type).
     */
    @Test
    public void testPrimaryOfClassLiteralArrayType()
    {
        Parser parser = new Parser(new Scanner("spruce.lang.String[][].class"));
        ASTPrimary node = parser.parsePrimary();

        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());
        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTClassLiteral);

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

        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());
        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTThis);

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

        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());
        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTExpressionNoIncrDecr);

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

        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());
        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTElementAccess);

        ASTElementAccess ea = (ASTElementAccess) child;
        assertEquals(OPEN_BRACKET, ea.getOperation());
        children = ea.getChildren();
        assertEquals(2, children.size());
        List<Class<?>> expectedClasses = Arrays.asList(ASTElementAccess.class, ASTExpression.class);
        compareClasses(expectedClasses, children);
        child = children.get(0);
        assertTrue(child instanceof ASTElementAccess);

        ea = (ASTElementAccess) child;
        assertEquals(OPEN_BRACKET, ea.getOperation());
        children = ea.getChildren();
        assertEquals(2, children.size());
        compareClasses(expectedClasses, children);
        child = children.get(0);
        assertTrue(child instanceof ASTElementAccess);

        ea = (ASTElementAccess) child;
        assertEquals(OPEN_BRACKET, ea.getOperation());
        children = ea.getChildren();
        assertEquals(2, children.size());
        expectedClasses = Arrays.asList(ASTPrimary.class, ASTExpression.class);
        compareClasses(expectedClasses, children);

        node.collapseThenPrint();
    }

    /**
     * Tests primary of method invocation.
     */
    @Test
    public void testPrimaryOfMethodInvocation()
    {
        Parser parser = new Parser(new Scanner("methodName(helperMethod(i), (a + b), ++j)"));
        ASTPrimary node = parser.parsePrimary();

        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());
        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTMethodInvocation);

        ASTMethodInvocation mi = (ASTMethodInvocation) child;
        checkBinary(mi, OPEN_PARENTHESIS, ASTPrimary.class, ASTArgumentList.class);
    }

    /**
     * Tests primary of array creation expression.
     */
    @Test
    public void testPrimaryOfArrayCreationExpression()
    {
        Parser parser = new Parser(new Scanner("new spruce.lang.String[23]"));
        ASTPrimary node = parser.parsePrimary();

        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());
        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTArrayCreationExpression);

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

        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());
        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTClassInstanceCreationExpression);

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

        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());
        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTClassInstanceCreationExpression);

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
    }

    /**
     * Tests array creation expression of dim exprs and dims.
     */
    @Test
    public void testArrayCreationExpressionOfDimExprsDims()
    {
        Parser parser = new Parser(new Scanner("new String[10][]"));
        ASTArrayCreationExpression node = parser.parseArrayCreationExpression();

        assertEquals(NEW, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(3, children.size());
        List<Class<?>> expectedClasses = Arrays.asList(ASTTypeToInstantiate.class, ASTDimExprs.class, ASTDims.class);
        compareClasses(expectedClasses, children);

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

        assertEquals(NEW, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(3, children.size());
        List<Class<?>> expectedClasses = Arrays.asList(ASTTypeToInstantiate.class, ASTDims.class, ASTArrayInitializer.class);
        compareClasses(expectedClasses, children);

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

        assertNull(node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(2, children.size());
        List<Class<?>> expectedClasses = Arrays.asList(ASTDimExprs.class, ASTDimExpr.class);
        compareClasses(expectedClasses, children);

        ASTDimExprs child = (ASTDimExprs) children.get(0);
        assertNull(child.getOperation());
        children = child.getChildren();
        assertEquals(2, children.size());
        compareClasses(expectedClasses, children);

        child = (ASTDimExprs) children.get(0);
        assertNull(child.getOperation());
        children = child.getChildren();
        assertEquals(1, children.size());
        assertTrue(children.get(0) instanceof ASTDimExpr);

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
    }

    /**
     * Tests variable initializer list of variable initializer.
     */
    @Test
    public void testVariableInitializerListOfVariableInitializer()
    {
        Parser parser = new Parser(new Scanner("i + 1"));
        ASTVariableInitializerList node = parser.parseVariableInitializerList();
        checkSimple(node, ASTVariableInitializer.class);
    }

    /**
     * Tests variable initializer list of "," and variable initializer.
     */
    @Test
    public void testVariableInitializerListOfComma()
    {
        Parser parser = new Parser(new Scanner("x + 1, y - 1"));
        ASTVariableInitializerList node = parser.parseVariableInitializerList();
        checkBinaryLeftAssociative(node, Arrays.asList(COMMA), ASTVariableInitializerList.class, ASTVariableInitializer.class);
    }

    /**
     * Tests nested variable initializer lists (here, just multiple variable initializers).
     */
    @Test
    public void testVariableInitializerListNested()
    {
        Parser parser = new Parser(new Scanner("this, count + 1, sumSoFar + value"));
        ASTVariableInitializerList node = parser.parseVariableInitializerList();
        checkBinaryLeftAssociative(node, Arrays.asList(COMMA, COMMA), ASTVariableInitializerList.class, ASTVariableInitializer.class);
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
    }
}
