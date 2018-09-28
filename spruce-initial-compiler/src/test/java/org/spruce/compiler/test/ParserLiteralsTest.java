package org.spruce.compiler.test;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.*;
import org.spruce.compiler.parser.Parser;
import org.spruce.compiler.scanner.Scanner;
import static org.spruce.compiler.scanner.TokenType.*;
import static org.spruce.compiler.test.ParserTestUtility.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * All tests for the parser related to literals.
 */
public class ParserLiteralsTest
{
    /**
     * Tests class literal of type name.
     */
    @Test
    public void testClassLiteralTypeName()
    {
        Parser parser = new Parser(new Scanner("String.class"));
        ASTClassLiteral node = parser.parseClassLiteral();

        assertEquals(CLASS, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTTypeName);

        node.collapse();
        node.print();
    }

    /**
     * Tests class literal of type name and bracket pairs.
     */
    @Test
    public void testClassLiteralTypeNameBracketPairs()
    {
        Parser parser = new Parser(new Scanner("Int[][].class"));
        ASTClassLiteral node = parser.parseClassLiteral();
        node.print();
        assertEquals(CLASS, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(2, children.size());
        List<Class<?>> expectedClasses = Arrays.asList(ASTTypeName.class, ASTDims.class);
        compareClasses(expectedClasses, children);
        ASTNode child = children.get(1);
        assertTrue(child instanceof ASTDims);
        ASTDims dims = (ASTDims) child;

        assertEquals(OPEN_CLOSE_BRACKET, dims.getOperation());
        children = dims.getChildren();
        assertEquals(1, children.size());
        child = children.get(0);
        assertTrue(child instanceof ASTDims);
        dims = (ASTDims) child;

        assertEquals(OPEN_CLOSE_BRACKET, dims.getOperation());
        children = dims.getChildren();
        assertEquals(0, children.size());

        node.collapse();
        node.print();
    }

    /**
     * Tests an integer literal.
     */
    @Test
    public void testLiteralInteger()
    {
        Parser parser = new Parser(new Scanner("1234"));
        ASTLiteral node = parser.parseLiteral();

        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTIntegerLiteral);
        ASTIntegerLiteral integerLiteral = (ASTIntegerLiteral) child;
        assertEquals(1234, integerLiteral.getNumericValue());

        node.collapse();
        node.print();
    }

    /**
     * Tests a floating point literal.
     */
    @Test
    public void testLiteralFloatingPoint()
    {
        Parser parser = new Parser(new Scanner("1234.5"));
        ASTLiteral node = parser.parseLiteral();

        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTFloatingPointLiteral);
        ASTFloatingPointLiteral floatingPointLiteral = (ASTFloatingPointLiteral) child;
        assertEquals(1234.5, floatingPointLiteral.getNumericValue());

        node.collapse();
        node.print();
    }

    /**
     * Tests a character literal.
     */
    @Test
    public void testLiteralCharacter()
    {
        Parser parser = new Parser(new Scanner("'c'"));
        ASTLiteral node = parser.parseLiteral();

        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTCharacterLiteral);
        ASTCharacterLiteral charLiteral = (ASTCharacterLiteral) child;
        assertEquals('c', charLiteral.getCharacterValue());

        node.collapse();
        node.print();
    }

    /**
     * Tests a normal string literal.
     */
    @Test
    public void testLiteralStringNormal()
    {
        Parser parser = new Parser(new Scanner("\"s\\tring\""));
        ASTLiteral node = parser.parseLiteral();

        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTStringLiteral);
        ASTStringLiteral strLiteral = (ASTStringLiteral) child;
        assertEquals("s\tring", strLiteral.getStringValue());

        node.collapse();
        node.print();
    }

    /**
     * Tests a raw string literal.
     */
    @Test
    public void testLiteralStringRaw()
    {
        Parser parser = new Parser(new Scanner("\"\"\"\"stri\\ng\"\"\"\""));
        ASTLiteral node = parser.parseLiteral();

        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTStringLiteral);
        ASTStringLiteral strLiteral = (ASTStringLiteral) child;
        assertEquals("\"stri\\ng\"", strLiteral.getStringValue());

        node.collapse();
        node.print();
    }

    /**
     * Tests a true boolean literal.
     */
    @Test
    public void testLiteralBooleanTrue()
    {
        Parser parser = new Parser(new Scanner("true"));
        ASTLiteral node = parser.parseLiteral();

        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTBooleanLiteral);
        ASTBooleanLiteral boolLiteral = (ASTBooleanLiteral) child;
        assertTrue(boolLiteral.getBooleanValue());

        node.collapse();
        node.print();
    }

    /**
     * Tests a false boolean literal.
     */
    @Test
    public void testLiteralBooleanFalse()
    {
        Parser parser = new Parser(new Scanner("false"));
        ASTLiteral node = parser.parseLiteral();

        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTBooleanLiteral);
        ASTBooleanLiteral boolLiteral = (ASTBooleanLiteral) child;
        assertFalse(boolLiteral.getBooleanValue());

        node.collapse();
        node.print();
    }

    /**
     * Tests a null literal.
     */
    @Test
    public void testLiteralNull()
    {
        Parser parser = new Parser(new Scanner("null"));
        ASTLiteral node = parser.parseLiteral();

        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTNullLiteral);
        ASTNullLiteral nullLiteral = (ASTNullLiteral) child;
        assertNull(nullLiteral.getNullValue());

        node.collapse();
        node.print();
    }
}
