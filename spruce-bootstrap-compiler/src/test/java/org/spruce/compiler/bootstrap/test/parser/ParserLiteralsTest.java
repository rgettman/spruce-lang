package org.spruce.compiler.bootstrap.test.parser;

import org.spruce.compiler.bootstrap.ast.literals.*;
import org.spruce.compiler.bootstrap.common.BaseMessageProducer;
import org.spruce.compiler.bootstrap.parser.LiteralsParser;
import org.spruce.compiler.bootstrap.parser.Parser;
import org.spruce.compiler.bootstrap.scanner.Scanner;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.bootstrap.test.util.TestUtility;

import static org.junit.jupiter.api.Assertions.*;
import static org.spruce.compiler.bootstrap.test.parser.ParserTestUtility.*;

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
        ensureNoErrors(node, parser);
        ASTIntegerLiteral integerLiteral = TestUtility.ensureIsa(node, ASTIntegerLiteral.class);
        assertEquals(1234, integerLiteral.getNumericValue());
    }

    /**
     * Tests a floating point literal.
     */
    @Test
    public void testLiteralOfFloatingPoint() {
        LiteralsParser parser = getLiteralsParser("1234.5");
        ASTLiteral node = parser.parseLiteral();
        ensureNoErrors(node, parser);
        ASTFloatingPointLiteral floatingPointLiteral = TestUtility.ensureIsa(node, ASTFloatingPointLiteral.class);
        assertEquals(1234.5, floatingPointLiteral.getNumericValue());
    }

    /**
     * Tests a character literal.
     */
    @Test
    public void testLiteralOfCharacter() {
        LiteralsParser parser = getLiteralsParser("'c'");
        ASTLiteral node = parser.parseLiteral();
        ensureNoErrors(node, parser);
        ASTCharacterLiteral charLiteral = TestUtility.ensureIsa(node, ASTCharacterLiteral.class);
        assertEquals('c', charLiteral.getCharacterValue());
    }

    /**
     * Tests a normal string literal.
     */
    @Test
    public void testLiteralOfStringNormal() {
        LiteralsParser parser = getLiteralsParser("\"s\\tring\"");
        ASTLiteral node = parser.parseLiteral();
        ensureNoErrors(node, parser);
        ASTStringLiteral strLiteral = TestUtility.ensureIsa(node, ASTStringLiteral.class);
        assertEquals("s\tring", strLiteral.getStringValue());
    }

    /**
     * Tests an empty string literal.
     */
    @Test
    public void testLiteralOfStringEmpty() {
        LiteralsParser parser = getLiteralsParser("\"\"");
        ASTLiteral node = parser.parseLiteral();
        ensureNoErrors(node, parser);
        ASTStringLiteral strLiteral = TestUtility.ensureIsa(node, ASTStringLiteral.class);
        assertEquals("", strLiteral.getStringValue());
    }

    /**
     * Tests a text block string literal.
     */
    @Test
    public void testLiteralOfStringTextBlock() {
        String str = """
                \"""
                "stri\\ng"\"""
                """;
        LiteralsParser parser = getLiteralsParser(str);
        ASTLiteral node = parser.parseLiteral();
        ensureNoErrors(node, parser);
        ASTStringLiteral strLiteral = TestUtility.ensureIsa(node, ASTStringLiteral.class);
        assertEquals("\"stri\ng\"", strLiteral.getStringValue());
    }

    /**
     * Helper method to get a <code>LiteralsParser</code> directly from code.
     * @param code The code to test.
     * @return A <code>LiteralsParser</code> that will parse the given code.
     */
    private static LiteralsParser getLiteralsParser(String code) {
        return new Parser(new Scanner(code), new BaseMessageProducer()).getLiteralsParser();
    }
}
