package org.spruce.compiler.test;

import org.spruce.compiler.ast.literals.*;
import org.spruce.compiler.parser.LiteralsParser;
import org.spruce.compiler.scanner.Scanner;
import static org.spruce.compiler.test.ParserTestUtility.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * All tests for the parser related to literals.
 */
public class ParserLiteralsTest
{
    /**
     * Tests an integer literal.
     */
    @Test
    public void testLiteralOfInteger()
    {
        LiteralsParser parser = new LiteralsParser(new Scanner("1234"));
        ASTLiteral node = parser.parseLiteral();
        checkSimple(node, ASTIntegerLiteral.class);
        ASTIntegerLiteral integerLiteral = (ASTIntegerLiteral) node.getChildren().get(0);
        assertEquals(1234, integerLiteral.getNumericValue());
        node.collapseThenPrint();
    }

    /**
     * Tests a floating point literal.
     */
    @Test
    public void testLiteralOfFloatingPoint()
    {
        LiteralsParser parser = new LiteralsParser(new Scanner("1234.5"));
        ASTLiteral node = parser.parseLiteral();
        checkSimple(node, ASTFloatingPointLiteral.class);
        ASTFloatingPointLiteral floatingPointLiteral = (ASTFloatingPointLiteral) node.getChildren().get(0);
        assertEquals(1234.5, floatingPointLiteral.getNumericValue());
        node.collapseThenPrint();
    }

    /**
     * Tests a character literal.
     */
    @Test
    public void testLiteralOfCharacter()
    {
        LiteralsParser parser = new LiteralsParser(new Scanner("'c'"));
        ASTLiteral node = parser.parseLiteral();
        checkSimple(node, ASTCharacterLiteral.class);
        ASTCharacterLiteral charLiteral = (ASTCharacterLiteral) node.getChildren().get(0);
        assertEquals('c', charLiteral.getCharacterValue());
        node.collapseThenPrint();
    }

    /**
     * Tests a normal string literal.
     */
    @Test
    public void testLiteralOfStringNormal()
    {
        LiteralsParser parser = new LiteralsParser(new Scanner("\"s\\tring\""));
        ASTLiteral node = parser.parseLiteral();
        checkSimple(node, ASTStringLiteral.class);
        ASTStringLiteral strLiteral = (ASTStringLiteral) node.getChildren().get(0);
        assertEquals("s\tring", strLiteral.getStringValue());
        node.collapseThenPrint();
    }

    /**
     * Tests an empty string literal.
     */
    @Test
    public void testLiteralOfStringEmpty()
    {
        LiteralsParser parser = new LiteralsParser(new Scanner("\"\""));
        ASTLiteral node = parser.parseLiteral();
        checkSimple(node, ASTStringLiteral.class);
        ASTStringLiteral strLiteral = (ASTStringLiteral) node.getChildren().get(0);
        assertEquals("", strLiteral.getStringValue());
        node.collapseThenPrint();
    }

    /**
     * Tests a raw string literal.
     */
    @Test
    public void testLiteralOfStringRaw()
    {
        LiteralsParser parser = new LiteralsParser(new Scanner("\"\"\"\"stri\\ng\"\"\"\""));
        ASTLiteral node = parser.parseLiteral();
        checkSimple(node, ASTStringLiteral.class);
        ASTStringLiteral strLiteral = (ASTStringLiteral) node.getChildren().get(0);
        assertEquals("\"stri\\ng\"", strLiteral.getStringValue());
        node.collapseThenPrint();
    }

    /**
     * Tests a true boolean literal.
     */
    @Test
    public void testLiteralOfBooleanTrue()
    {
        LiteralsParser parser = new LiteralsParser(new Scanner("true"));
        ASTLiteral node = parser.parseLiteral();
        checkSimple(node, ASTBooleanLiteral.class);
        ASTBooleanLiteral boolLiteral = (ASTBooleanLiteral) node.getChildren().get(0);
        assertTrue(boolLiteral.getBooleanValue());
        node.collapseThenPrint();
    }

    /**
     * Tests a false boolean literal.
     */
    @Test
    public void testLiteralOfBooleanFalse()
    {
        LiteralsParser parser = new LiteralsParser(new Scanner("false"));
        ASTLiteral node = parser.parseLiteral();
        checkSimple(node, ASTBooleanLiteral.class);
        ASTBooleanLiteral boolLiteral = (ASTBooleanLiteral) node.getChildren().get(0);
        assertFalse(boolLiteral.getBooleanValue());
        node.collapseThenPrint();
    }

    /**
     * Tests a null literal.
     */
    @Test
    public void testLiteralOfNull()
    {
        LiteralsParser parser = new LiteralsParser(new Scanner("null"));
        ASTLiteral node = parser.parseLiteral();
        checkSimple(node, ASTNullLiteral.class);
        ASTNullLiteral nullLiteral = (ASTNullLiteral) node.getChildren().get(0);
        assertNull(nullLiteral.getNullValue());
        node.collapseThenPrint();
    }
}
