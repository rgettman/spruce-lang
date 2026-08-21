package org.spruce.compiler.bootstrap.test.parser;

import org.spruce.compiler.bootstrap.ast.ASTKeywordNode;
import org.spruce.compiler.bootstrap.ast.ASTListNode;
import org.spruce.compiler.bootstrap.ast.Node;
//import org.spruce.compiler.ast.classes.ASTFormalParameter;
//import org.spruce.compiler.ast.classes.ASTFormalParameterList;
import org.spruce.compiler.bootstrap.ast.expressions.*;
import org.spruce.compiler.bootstrap.ast.literals.*;
import org.spruce.compiler.bootstrap.ast.names.*;
//import org.spruce.compiler.ast.statements.ASTBlock;
//import org.spruce.compiler.bootstrap.ast.statements.ASTVariableModifierList;
import org.spruce.compiler.bootstrap.ast.types.*;
import org.spruce.compiler.bootstrap.common.BaseMessageProducer;
import org.spruce.compiler.bootstrap.parser.ExpressionsParser;
import org.spruce.compiler.bootstrap.parser.Parser;
import org.spruce.compiler.bootstrap.scanner.Scanner;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.bootstrap.scanner.TokenType;
import org.spruce.compiler.bootstrap.test.util.TestUtility;

import static org.spruce.compiler.bootstrap.ast.ASTListNode.Type.*;
import static org.spruce.compiler.bootstrap.ast.expressions.ASTPrimary.Type.*;
import static org.spruce.compiler.bootstrap.scanner.TokenType.*;
import static org.spruce.compiler.bootstrap.test.parser.ParserTestUtility.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * All tests for the parser related to expressions.
 */
public class ParserExpressionsTest {

    /**
     * Tests expression of other expression.
     */
    @Test
    public void testExpressionOfOtherExpression() {
        ExpressionsParser parser = getExpressionsParser("count == 1");
        ASTExpression node = parser.parseExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(TestUtility.ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, DOUBLE_EQUAL);
    }

    /**
     * Tests value expression of logical or expression.
     */
    @Test
    public void testValueExpressionOfLogicalOrExpression() {
        ExpressionsParser parser = getExpressionsParser("a || b");
        ASTValueExpression node = parser.parseValueExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(TestUtility.ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, DOUBLE_PIPE);
    }

    /**
     * Tests logical or expression of logical xor expression.
     */
    @Test
    public void testLogicalOrExpressionOfLogicalAndExpression() {
        ExpressionsParser parser = getExpressionsParser("a && b");
        ASTValueExpression node = parser.parseLogicalOrExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(TestUtility.ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, DOUBLE_AMPERSAND);
    }

    /**
     * Tests logical or expression of "||" and logical xor expression.
     */
    @Test
    public void testLogicalOrExpressionOfConditional() {
        ExpressionsParser parser = getExpressionsParser("alreadyDone || test");
        ASTValueExpression node = parser.parseLogicalOrExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(TestUtility.ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, DOUBLE_PIPE);
    }

    /**
     * Tests nested logical or expressions.
     */
    @Test
    public void testLogicalOrExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("a && b || c && d || e || f");
        ASTValueExpression node = parser.parseLogicalOrExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(TestUtility.ensureIsa(node, ASTBinaryExpression.class),
                ASTPrimary.class, ASTPrimary.class, DOUBLE_AMPERSAND,
                ASTPrimary.class, ASTPrimary.class, DOUBLE_AMPERSAND, DOUBLE_PIPE,
                ASTPrimary.class, DOUBLE_PIPE, ASTPrimary.class, DOUBLE_PIPE
                );
    }

    /**
     * Tests logical and expression of relational expression.
     */
    @Test
    public void testLogicalAndExpressionOfRelationalExpression() {
        ExpressionsParser parser = getExpressionsParser("a == b");
        ASTValueExpression node = parser.parseLogicalAndExpression();
        ensureNoErrors(node, parser);
        ASTBinaryExpression binary = TestUtility.ensureIsa(node, ASTBinaryExpression.class);
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
        checkBinaryPostorder(TestUtility.ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, DOUBLE_AMPERSAND);
    }

    /**
     * Tests nested logical and expressions.
     */
    @Test
    public void testLogicalAndExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("a < b && c <= d && e > f");
        ASTValueExpression node = parser.parseLogicalAndExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(TestUtility.ensureIsa(node, ASTBinaryExpression.class),
                ASTPrimary.class, ASTPrimary.class, LESS_THAN,
                ASTPrimary.class, ASTPrimary.class, LESS_THAN_OR_EQUAL, DOUBLE_AMPERSAND,
                ASTPrimary.class, ASTPrimary.class, GREATER_THAN, DOUBLE_AMPERSAND
        );
    }

    /**
     * Tests relational expression of "&lt;" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfLessThan() {
        ExpressionsParser parser = getExpressionsParser("a.value < b.value");
        ASTValueExpression node = parser.parseRelationalExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(TestUtility.ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, LESS_THAN);
    }

    /**
     * Tests relational expression of "&lt;=" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfLessThanOrEqual() {
        ExpressionsParser parser = getExpressionsParser("2 <= 2");
        ASTValueExpression node = parser.parseRelationalExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(TestUtility.ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, LESS_THAN_OR_EQUAL);
    }

    /**
     * Tests relational expression of "&gt;" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfGreaterThan() {
        ExpressionsParser parser = getExpressionsParser("a.value > b.value");
        ASTValueExpression node = parser.parseRelationalExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(TestUtility.ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, GREATER_THAN);
    }

    /**
     * Tests relational expression of "&gt;=" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfGreaterThanOrEqual() {
        ExpressionsParser parser = getExpressionsParser("2 >= 2");
        ASTValueExpression node = parser.parseRelationalExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(TestUtility.ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, GREATER_THAN_OR_EQUAL);
    }

    /**
     * Tests relational expression of "==" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfEqual() {
        ExpressionsParser parser = getExpressionsParser("test == SUCCESS");
        ASTValueExpression node = parser.parseRelationalExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(TestUtility.ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, DOUBLE_EQUAL);
    }

    /**
     * Tests relational expression of "!=" and compare expression.
     */
    @Test
    public void testRelationalExpressionOfNotEqual() {
        ExpressionsParser parser = getExpressionsParser("test != FAILURE");
        ASTValueExpression node = parser.parseRelationalExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(TestUtility.ensureIsa(node, ASTBinaryExpression.class), ASTPrimary.class, ASTPrimary.class, EXCLAMATION_EQUAL);
    }

    /**
     * Tests relational expression of "isa" and compare expression with a DataType.
     */
    @Test
    public void testRelationalExpressionOfIsaDataType() {
        ExpressionsParser parser = getExpressionsParser("node isa ASTBinaryExpression");
        ASTValueExpression node = parser.parseRelationalExpression();
        ensureNoErrors(node, parser);
        ASTIsaExpression isaExpr = TestUtility.ensureIsa(node, ASTIsaExpression.class);
        assertNotNull(isaExpr.getExpr());
        assertNotNull(isaExpr.getIsaTarget());
        assertInstanceOf(ASTDataType.class, isaExpr.getIsaTarget());
    }

    /**
     * Tests additive expression of "+" and multiplicative expression.
     */
    @Test
    public void testAdditiveExpressionOfPlus() {
        ExpressionsParser parser = getExpressionsParser("-1 + 2");
        ASTValueExpression node = parser.parseAdditiveExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(TestUtility.ensureIsa(node, ASTBinaryExpression.class), ASTUnaryExpression.class, ASTPrimary.class, PLUS);
    }

    /**
     * Tests nested additive expressions.
     */
    @Test
    public void testAdditiveExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("-2 + -3");
        ASTValueExpression node = parser.parseAdditiveExpression();
        ensureNoErrors(node, parser);
        checkBinaryPostorder(TestUtility.ensureIsa(node, ASTBinaryExpression.class),
                ASTUnaryExpression.class, ASTUnaryExpression.class, PLUS);
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
        ASTCastExpression castExpr = TestUtility.ensureIsa(node, ASTCastExpression.class);
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

        ASTCastExpression castExpr = TestUtility.ensureIsa(node, ASTCastExpression.class);
        assertNotNull(castExpr.getExpr());
        assertNotNull(castExpr.getIntersectionType());

        ASTCastExpression inner = TestUtility.ensureIsa(castExpr.getExpr(), ASTCastExpression.class);
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
        ASTPrimary primary = TestUtility.ensureIsa(node, ASTPrimary.class);
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
        checkUnaryExpr(TestUtility.ensureIsa(node, ASTUnaryExpression.class), MINUS, ASTPrimary.class);
    }

    /**
     * Tests unary expression of "!" and unary expression.
     */
    @Test
    public void testUnaryExpressionOfLogicalComplementUnary() {
        ExpressionsParser parser = getExpressionsParser("!false");
        ASTValueExpression node = parser.parseUnaryExpression();
        ensureNoErrors(node, parser);
        checkUnaryExpr(TestUtility.ensureIsa(node, ASTUnaryExpression.class), EXCLAMATION, ASTPrimary.class);
    }

    /**
     * Tests nested unary expressions.
     */
    @Test
    public void testUnaryExpressionNested() {
        ExpressionsParser parser = getExpressionsParser("! - ! - bits");
        ASTValueExpression node = parser.parseUnaryExpression();
        ensureNoErrors(node, parser);
        ASTUnaryExpression unaryExpr = TestUtility.ensureIsa(node, ASTUnaryExpression.class);
        checkUnaryExpr(unaryExpr, EXCLAMATION, ASTUnaryExpression.class, ASTUnaryExpression.class);

        ASTUnaryExpression childNode = TestUtility.ensureIsa(unaryExpr.getFirst(), ASTUnaryExpression.class);
        checkUnaryExpr(childNode, MINUS, ASTUnaryExpression.class, ASTUnaryExpression.class);

        childNode = TestUtility.ensureIsa(childNode.getFirst(), ASTUnaryExpression.class);
        checkUnaryExpr(childNode, EXCLAMATION, ASTUnaryExpression.class, ASTPrimary.class);

        childNode = TestUtility.ensureIsa(childNode.getFirst(), ASTUnaryExpression.class);
        assertEquals(MINUS, childNode.getOperation());
        ASTValueExpression child = childNode.getFirst();
        assertInstanceOf(ASTPrimary.class, child);
    }

    /**
     * Tests argument list that is empty.
     */
    @Test
    public void testArgumentListEmpty() {
        ExpressionsParser parser = getExpressionsParser(")");
        ASTArgumentList node = parser.parseArgumentList();
        ensureNoErrors(node, parser);
        checkList(node, ARGUMENTS, ASTExpression.class, 0);
    }

    /**
     * Tests argument list of expression.
     */
    @Test
    public void testArgumentListOfExpression() {
        ExpressionsParser parser = getExpressionsParser("index");
        ASTArgumentList node = parser.parseArgumentList();
        ensureNoErrors(node, parser);
        checkList(node, ARGUMENTS, ASTExpression.class, 1);
    }

    /**
     * Tests argument list of multiple arguments.
     */
    @Test
    public void testArgumentListMultiple() {
        ExpressionsParser parser = getExpressionsParser("a, 1, b + c");
        ASTArgumentList node = parser.parseArgumentList();
        ensureNoErrors(node, parser);
        checkList(node, ARGUMENTS, ASTExpression.class, 3);
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
        ExpressionsParser parser = getExpressionsParser("spruce.lang.Comparable.class");
        ASTPrimary node = parser.parsePrimary();
        ensureNoErrors(node, parser);
        checkPrimary(node, ASTPrimary.Type.CLASS_LITERAL, ASTClassLiteral.class);
        ASTClassLiteral classLiteral = TestUtility.ensureIsa(node.getChild(), ASTClassLiteral.class);
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
        checkPrimary(node, ASTPrimary.Type.SELF, ASTSelf.class);
        ASTSelf self = TestUtility.ensureIsa(node.getChild(), ASTSelf.class);
        ASTKeywordNode selfKeyword = TestUtility.ensureIsa(self.getSelfKeyword(), ASTKeywordNode.class);
        assertEquals(TokenType.SELF, selfKeyword.getKeyword());
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
     * Tests primary of field access of super.
     */
    @Test
    public void testPrimaryOfFieldAccessOfSuper() {
        ExpressionsParser parser = getExpressionsParser("super.superclassField");
        ASTPrimary node = parser.parsePrimary();
        ensureNoErrors(node, parser);
        checkPrimary(node, FIELD_ACCESS, ASTFieldAccess.class);
        ASTFieldAccess fa = TestUtility.ensureIsa(node.getChild(), ASTFieldAccess.class);
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
        ASTFieldAccess fa = TestUtility.ensureIsa(node.getChild(), ASTFieldAccess.class);
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
        ASTFieldAccess fa = TestUtility.ensureIsa(node.getChild(), ASTFieldAccess.class);
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
        ASTMethodInvocation mi = TestUtility.ensureIsa(node.getChild(), ASTMethodInvocation.class);
        checkMethodInvocation(mi, false, false, true, 
                false);
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
        ExpressionsParser parser = getExpressionsParser("expr.name.methodName(one)");
        ASTPrimary node = parser.parsePrimary();
        ensureNoErrors(node, parser);

        checkPrimary(node, METHOD_INVOCATION, ASTMethodInvocation.class);
        ASTMethodInvocation mi = TestUtility.ensureIsa(node.getChild(), ASTMethodInvocation.class);
        checkMethodInvocation(mi, false, false, true,
                false);
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
        ASTMethodInvocation mi = TestUtility.ensureIsa(node.getChild(), ASTMethodInvocation.class);
        checkMethodInvocation(mi, false, false, false,
                false);
        ensureNoErrors(node, parser);
    }

    /**
     * Tests primary of method invocation starting with <code>super</code>.
     */
    @Test
    public void testPrimaryOfMethodInvocationOfSuper() {
        ExpressionsParser parser = getExpressionsParser("super.inheritedMethod(\"super\")");
        ASTPrimary node = parser.parsePrimary();
        ensureNoErrors(node, parser);
        checkPrimary(node, METHOD_INVOCATION, ASTMethodInvocation.class);
        ASTMethodInvocation mi = TestUtility.ensureIsa(node.getChild(), ASTMethodInvocation.class);
        checkMethodInvocation(mi, false, true, false,
                false);
        ASTIdentifier methodName = mi.getIdentifier();
        assertEquals("inheritedMethod", methodName.getValue());
    }

    /**
     * Tests primary of method invocation starting with <code>super</code>.
     */
    @Test
    public void testPrimaryOfMethodInvocationOfTypeNameSuper() {
        ExpressionsParser parser = getExpressionsParser("org.test.EnclosingClass.super.inheritedMethod(\"super\")");
        ASTPrimary node = parser.parsePrimary();
        ensureNoErrors(node, parser);
        checkPrimary(node, METHOD_INVOCATION, ASTMethodInvocation.class);
        ASTMethodInvocation mi = TestUtility.ensureIsa(node.getChild(), ASTMethodInvocation.class);
        checkMethodInvocation(mi, true, true, false,
                false);
        ASTIdentifier methodName = mi.getIdentifier();
        assertEquals("inheritedMethod", methodName.getValue());
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
        ExpressionsParser parser = getExpressionsParser("new Foo(\"Bar\")");
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
        ASTTypenameSelf typeNameSelf = TestUtility.ensureIsa(node.getChild(), ASTTypenameSelf.class);
        assertNotNull(typeNameSelf.getTypename());
        ASTKeywordNode self = TestUtility.ensureIsa(typeNameSelf.getSelfKeyword(), ASTKeywordNode.class);
        assertEquals(TokenType.SELF, self.getKeyword());
    }

    /**
     * Tests nested primary expressions, including Class Instance Creation
     * Expressions, Method Invocations, and Field Accesses.
     */
    @Test
    public void testPrimaryOfNested() {
        ExpressionsParser parser = getExpressionsParser("new Foo().field1.method1().field2.method2(1).new Bar()");
        ASTPrimary node = parser.parsePrimary();
        ensureNoErrors(node, parser);

        checkPrimary(node, ASTPrimary.Type.CLASS_INSTANCE_CREATION_EXPR, ASTClassInstanceCreationExpression.class);
        ASTClassInstanceCreationExpression outerCice = TestUtility.ensureIsa(node.getChild(), ASTClassInstanceCreationExpression.class);
        assertTrue(outerCice.getPrimary().isPresent());
        ASTPrimary pMethod2 = outerCice.getPrimary().get();

        checkPrimary(pMethod2, METHOD_INVOCATION, ASTMethodInvocation.class);
        ASTMethodInvocation method2 = TestUtility.ensureIsa(pMethod2.getChild(), ASTMethodInvocation.class);
        checkMethodInvocation(method2, false, false, false,
                true);
        ASTIdentifier methodName2 = method2.getIdentifier();
        assertEquals("method2", methodName2.getValue());

        ASTPrimary pFieldAccess2 = method2.getPrimary().orElseThrow();
        checkPrimary(pFieldAccess2, FIELD_ACCESS, ASTFieldAccess.class);
        ASTFieldAccess fieldAccess2 = TestUtility.ensureIsa(pFieldAccess2.getChild(), ASTFieldAccess.class);
        checkFieldAccess(fieldAccess2, false, false, true);
        ASTIdentifier fieldName2 = fieldAccess2.getIdentifier();
        assertEquals("field2", fieldName2.getValue());

        ASTPrimary pMethod1 = fieldAccess2.getPrimary().orElseThrow();
        checkPrimary(pMethod1, METHOD_INVOCATION, ASTMethodInvocation.class);
        ASTMethodInvocation method1 = TestUtility.ensureIsa(pMethod1.getChild(), ASTMethodInvocation.class);
        checkMethodInvocation(method1, false, false, false,
                true);
        ASTIdentifier methodName1 = method1.getIdentifier();
        assertEquals("method1", methodName1.getValue());

        ASTPrimary pFieldAccess1 = method1.getPrimary().orElseThrow();
        checkPrimary(pFieldAccess1, FIELD_ACCESS, ASTFieldAccess.class);
        ASTFieldAccess fieldAccess1 = TestUtility.ensureIsa(pFieldAccess1.getChild(), ASTFieldAccess.class);
        checkFieldAccess(fieldAccess1, false, false, true);
        ASTIdentifier fieldName1 = fieldAccess1.getIdentifier();
        assertEquals("field1", fieldName1.getValue());

        ASTPrimary pInnerCice = TestUtility.ensureIsa(fieldAccess1.getPrimary().orElseThrow(), ASTPrimary.class);
        checkPrimary(pInnerCice, ASTPrimary.Type.CLASS_INSTANCE_CREATION_EXPR, ASTClassInstanceCreationExpression.class);
    }

    /**
     * Tests bad primary of something that's not a primary.
     */
    @Test
    public void testPrimaryBad() {
        ExpressionsParser parser = getExpressionsParser("class int x = 2;");
        ASTPrimary node = parser.parsePrimary();
        expectError(node, parser);
    }

    /**
     * Tests bad primary with keyword void.
     */
    @Test
    public void testPrimaryBadVoid() {
        ExpressionsParser parser = getExpressionsParser("void param");
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
        ExpressionsParser parser = getExpressionsParser("new class");
        ASTPrimary node = parser.parsePrimary();
        expectError(node, parser, 3);
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
        ExpressionsParser parser = getExpressionsParser("new MyClass()");
        ASTUnqualifiedClassInstanceCreationExpression node = parser.parseUnqualifiedClassInstanceCreationExpression();
        ensureNoErrors(node, parser);
        checkUcice(node);
    }

    /**
     * Tests class instance creation expression of unqualified class instance
     * creation expression.
     */
    @Test
    public void testCICEOfUCICE() {
        ExpressionsParser parser = getExpressionsParser("new MyClass(1, \"one\")");
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
        checkUcice(node);
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
     * Helper method to test <code>ASTUnqualifiedClassInstanceCreationExpression</code> attributes.
     * @param ucice The <code>ASTUnqualifiedClassInstanceCreationExpression</code> to test.
     */
    private static void checkUcice(ASTUnqualifiedClassInstanceCreationExpression ucice)
    {
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
     */
    private static void checkMethodInvocation(ASTMethodInvocation mi, boolean isTypeNamePresent,
                                              boolean isSuperPresent, boolean isExprNamePresent,
                                              boolean isPrimaryPresent) {
        assertEquals(isTypeNamePresent, mi.getTypeName().isPresent());
        assertEquals(isSuperPresent, mi.getSooper().isPresent());
        assertEquals(isExprNamePresent, mi.getExprName().isPresent());
        assertEquals(isPrimaryPresent, mi.getPrimary().isPresent());
        assertNotNull(mi.getIdentifier());
        //assertTrue(mi.getArgumentList().isPresent());
    }
    
    /**
     * Helper method to get a <code>ExpressionsParser</code> directly from code.
     * @param code The code to test.
     * @return A <code>ExpressionsParser</code> that will parse the given code.
     */
    private static ExpressionsParser getExpressionsParser(String code) {
        return new Parser(new Scanner(code), new BaseMessageProducer()).getExpressionsParser();
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
