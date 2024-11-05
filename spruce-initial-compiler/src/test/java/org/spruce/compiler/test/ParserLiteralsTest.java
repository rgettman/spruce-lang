package org.spruce.compiler.test;

import org.spruce.compiler.ast.literals.*;
import org.spruce.compiler.parser.LiteralsParser;
import org.spruce.compiler.parser.Parser;
import org.spruce.compiler.scanner.Scanner;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * All tests for the parser related to literals.
 */
public class ParserLiteralsTest {
    /**
     * Tests an integer literal.
     */
    @Test
    public void testLiteralOfInteger() {
        LiteralsParser parser = getLiteralsParser("1234");
        ASTLiteral node = parser.parseLiteral();
        System.out.println(node);
        assertInstanceOf(ASTIntegerLiteral.class, node);
        ASTIntegerLiteral integerLiteral = (ASTIntegerLiteral) node;
        assertEquals(1234, integerLiteral.getNumericValue());
    }

    /**
     * Tests a floating point literal.
     */
    @Test
    public void testLiteralOfFloatingPoint() {
        LiteralsParser parser = getLiteralsParser("1234.5");
        ASTLiteral node = parser.parseLiteral();
        System.out.println(node);
        assertInstanceOf(ASTFloatingPointLiteral.class, node);
        ASTFloatingPointLiteral floatingPointLiteral = (ASTFloatingPointLiteral) node;
        assertEquals(1234.5, floatingPointLiteral.getNumericValue());
    }

    /**
     * Tests a character literal.
     */
    @Test
    public void testLiteralOfCharacter() {
        LiteralsParser parser = getLiteralsParser("'c'");
        ASTLiteral node = parser.parseLiteral();
        System.out.println(node);
        assertInstanceOf(ASTCharacterLiteral.class, node);
        ASTCharacterLiteral charLiteral = (ASTCharacterLiteral) node;
        assertEquals('c', charLiteral.getCharacterValue());
    }

    /**
     * Tests a normal string literal.
     */
    @Test
    public void testLiteralOfStringNormal() {
        LiteralsParser parser = getLiteralsParser("\"s\\tring\"");
        ASTLiteral node = parser.parseLiteral();
        System.out.println(node);
        assertInstanceOf(ASTStringLiteral.class, node);
        ASTStringLiteral strLiteral = (ASTStringLiteral) node;
        assertEquals("s\tring", strLiteral.getStringValue());
    }

    /**
     * Tests an empty string literal.
     */
    @Test
    public void testLiteralOfStringEmpty() {
        LiteralsParser parser = getLiteralsParser("\"\"");
        ASTLiteral node = parser.parseLiteral();
        System.out.println(node);
        assertInstanceOf(ASTStringLiteral.class, node);
        ASTStringLiteral strLiteral = (ASTStringLiteral) node;
        assertEquals("", strLiteral.getStringValue());
    }

    /**
     * Tests a raw string literal.
     */
    @Test
    public void testLiteralOfStringRaw() {
        String str = """
                \"""
                "stri\\ng"\"""
                """;
        LiteralsParser parser = getLiteralsParser(str);
        ASTLiteral node = parser.parseLiteral();
        System.out.println(node);
        assertInstanceOf(ASTStringLiteral.class, node);
        ASTStringLiteral strLiteral = (ASTStringLiteral) node;
        assertEquals("\"stri\\ng\"", strLiteral.getStringValue());
    }

    /**
     * Tests a true boolean literal.
     */
    @Test
    public void testLiteralOfBooleanTrue() {
        LiteralsParser parser = getLiteralsParser("true");
        ASTLiteral node = parser.parseLiteral();
        System.out.println(node);
        assertInstanceOf(ASTBooleanLiteral.class, node);
        ASTBooleanLiteral boolLiteral = (ASTBooleanLiteral) node;
        assertTrue(boolLiteral.getBooleanValue());
    }

    /**
     * Tests a false boolean literal.
     */
    @Test
    public void testLiteralOfBooleanFalse() {
        LiteralsParser parser = getLiteralsParser("false");
        ASTLiteral node = parser.parseLiteral();
        System.out.println(node);
        assertInstanceOf(ASTBooleanLiteral.class, node);
        ASTBooleanLiteral boolLiteral = (ASTBooleanLiteral) node;
        assertFalse(boolLiteral.getBooleanValue());
    }

    /**
     * Helper method to get a <code>LiteralsParser</code> directly from code.
     * @param code The code to test.
     * @return A <code>LiteralsParser</code> that will parse the given code.
     */
    private static LiteralsParser getLiteralsParser(String code) {
        return new Parser(new Scanner(code)).getLiteralsParser();
    }
}
