package org.spruce.compiler.test;

import org.spruce.compiler.ast.literals.*;
import org.spruce.compiler.parser.LiteralsParser;
import org.spruce.compiler.parser.Parser;
import org.spruce.compiler.scanner.Scanner;
import static org.spruce.compiler.test.ParserTestUtility.*;

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
        LiteralsParser parser = new Parser(new Scanner("1234")).getLiteralsParser();
        ASTLiteral node = parser.parseLiteral();
        checkIs(node, ASTIntegerLiteral.class);
        ASTIntegerLiteral integerLiteral = (ASTIntegerLiteral) node;
        assertEquals(1234, integerLiteral.getNumericValue());
        node.print();
    }

    /**
     * Tests a floating point literal.
     */
    @Test
    public void testLiteralOfFloatingPoint() {
        LiteralsParser parser = new Parser(new Scanner("1234.5")).getLiteralsParser();
        ASTLiteral node = parser.parseLiteral();
        checkIs(node, ASTFloatingPointLiteral.class);
        ASTFloatingPointLiteral floatingPointLiteral = (ASTFloatingPointLiteral) node;
        assertEquals(1234.5, floatingPointLiteral.getNumericValue());
        node.print();
    }

    /**
     * Tests a character literal.
     */
    @Test
    public void testLiteralOfCharacter() {
        LiteralsParser parser = new Parser(new Scanner("'c'")).getLiteralsParser();
        ASTLiteral node = parser.parseLiteral();
        checkIs(node, ASTCharacterLiteral.class);
        ASTCharacterLiteral charLiteral = (ASTCharacterLiteral) node;
        assertEquals('c', charLiteral.getCharacterValue());
        node.print();
    }

    /**
     * Tests a normal string literal.
     */
    @Test
    public void testLiteralOfStringNormal() {
        LiteralsParser parser = new Parser(new Scanner("\"s\\tring\"")).getLiteralsParser();
        ASTLiteral node = parser.parseLiteral();
        checkIs(node, ASTStringLiteral.class);
        ASTStringLiteral strLiteral = (ASTStringLiteral) node;
        assertEquals("s\tring", strLiteral.getStringValue());
        node.print();
    }

    /**
     * Tests an empty string literal.
     */
    @Test
    public void testLiteralOfStringEmpty() {
        LiteralsParser parser = new Parser(new Scanner("\"\"")).getLiteralsParser();
        ASTLiteral node = parser.parseLiteral();
        checkIs(node, ASTStringLiteral.class);
        ASTStringLiteral strLiteral = (ASTStringLiteral) node;
        assertEquals("", strLiteral.getStringValue());
        node.print();
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
        LiteralsParser parser = new Parser(new Scanner(str)).getLiteralsParser();
        ASTLiteral node = parser.parseLiteral();
        checkIs(node, ASTStringLiteral.class);
        ASTStringLiteral strLiteral = (ASTStringLiteral) node;
        assertEquals("\"stri\\ng\"", strLiteral.getStringValue());
        node.print();
    }

    /**
     * Tests a true boolean literal.
     */
    @Test
    public void testLiteralOfBooleanTrue() {
        LiteralsParser parser = new Parser(new Scanner("true")).getLiteralsParser();
        ASTLiteral node = parser.parseLiteral();
        checkIs(node, ASTBooleanLiteral.class);
        ASTBooleanLiteral boolLiteral = (ASTBooleanLiteral) node;
        assertTrue(boolLiteral.getBooleanValue());
        node.print();
    }

    /**
     * Tests a false boolean literal.
     */
    @Test
    public void testLiteralOfBooleanFalse() {
        LiteralsParser parser = new Parser(new Scanner("false")).getLiteralsParser();
        ASTLiteral node = parser.parseLiteral();
        checkIs(node, ASTBooleanLiteral.class);
        ASTBooleanLiteral boolLiteral = (ASTBooleanLiteral) node;
        assertFalse(boolLiteral.getBooleanValue());
        node.print();
    }
}
