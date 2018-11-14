package org.spruce.compiler.test;

import java.util.Arrays;

import org.spruce.compiler.ast.names.*;
import org.spruce.compiler.parser.NamesParser;
import org.spruce.compiler.parser.Parser;
import org.spruce.compiler.scanner.Scanner;
import static org.spruce.compiler.scanner.TokenType.*;
import static org.spruce.compiler.test.ParserTestUtility.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * All tests for the parser related to names.
 */
public class ParserNamesTest
{
    /**
     * Tests type name as a simple identifier.
     */
    @Test
    public void testTypeNameOfIdentifier()
    {
        NamesParser parser = new Parser(new Scanner("simple")).getNamesParser();
        ASTTypeName node = parser.parseTypeName();
        checkSimple(node, ASTIdentifier.class);
        ASTIdentifier identifier = (ASTIdentifier) node.getChildren().get(0);
        assertEquals("simple", identifier.getValue());
        node.collapseThenPrint();
    }

    /**
     * Tests type name as two identifiers separated by ".".
     */
    @Test
    public void testTypeNameOfTwoIdentifiers()
    {
        NamesParser parser = new Parser(new Scanner("one.two")).getNamesParser();
        ASTTypeName node = parser.parseTypeName();
        checkBinaryLeftAssociative(node, Arrays.asList(DOT), Arrays.asList("two", "one"),
                ASTPackageOrTypeName.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests type name as three identifiers separated by ".".
     */
    @Test
    public void testTypeNameOfThreeIdentifiers()
    {
        NamesParser parser = new Parser(new Scanner("one.two.three")).getNamesParser();
        ASTTypeName node = parser.parseTypeName();
        checkBinaryLeftAssociative(node, Arrays.asList(DOT, DOT), Arrays.asList("three", "two", "one"),
                ASTPackageOrTypeName.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests package/type name as a simple identifier.
     */
    @Test
    public void testPackageOrTypeNameOfIdentifier()
    {
        NamesParser parser = new Parser(new Scanner("simple")).getNamesParser();
        ASTPackageOrTypeName node = parser.parsePackageOrTypeName();
        checkSimple(node, ASTIdentifier.class);
        ASTIdentifier identifier = (ASTIdentifier) node.getChildren().get(0);
        assertEquals("simple", identifier.getValue());
        node.collapseThenPrint();
    }

    /**
     * Tests package/type name as two identifiers separated by ".".
     */
    @Test
    public void testPackageOrTypeNameOfTwoIdentifiers()
    {
        NamesParser parser = new Parser(new Scanner("one.two")).getNamesParser();
        ASTPackageOrTypeName node = parser.parsePackageOrTypeName();
        checkBinaryLeftAssociative(node, Arrays.asList(DOT), Arrays.asList("two", "one"),
                ASTPackageOrTypeName.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests package/type name as three identifiers separated by ".".
     */
    @Test
    public void testPackageOrTypeNameOfThreeIdentifiers()
    {
        NamesParser parser = new Parser(new Scanner("one.two.three")).getNamesParser();
        ASTPackageOrTypeName node = parser.parsePackageOrTypeName();
        checkBinaryLeftAssociative(node, Arrays.asList(DOT, DOT), Arrays.asList("three", "two", "one"),
                ASTPackageOrTypeName.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests expression name as a simple identifier.
     */
    @Test
    public void testExpressionNameOfIdentifier()
    {
        NamesParser parser = new Parser(new Scanner("simple")).getNamesParser();
        ASTExpressionName node = parser.parseExpressionName();
        checkSimple(node, ASTIdentifier.class);
        ASTIdentifier identifier = (ASTIdentifier) node.getChildren().get(0);
        assertEquals("simple", identifier.getValue());
        node.collapseThenPrint();
    }

    /**
     * Tests expression name as two identifiers separated by ".".
     */
    @Test
    public void testExpressionNameOfTwoIdentifiers()
    {
        NamesParser parser = new Parser(new Scanner("one.two")).getNamesParser();
        ASTExpressionName node = parser.parseExpressionName();
        checkBinaryLeftAssociative(node, Arrays.asList(DOT), Arrays.asList("two", "one"),
                ASTAmbiguousName.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests expression name as three identifiers separated by ".".
     */
    @Test
    public void testExpressionNameOfThreeIdentifiers()
    {
        NamesParser parser = new Parser(new Scanner("one.two.three")).getNamesParser();
        ASTExpressionName node = parser.parseExpressionName();
        checkBinaryLeftAssociative(node, Arrays.asList(DOT, DOT), Arrays.asList("three", "two", "one"),
                ASTAmbiguousName.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests ambiguous name as a simple identifier.
     */
    @Test
    public void testAmbiguousNameOfIdentifier()
    {
        NamesParser parser = new Parser(new Scanner("simple")).getNamesParser();
        ASTAmbiguousName node = parser.parseAmbiguousName();
        checkSimple(node, ASTIdentifier.class);
        ASTIdentifier identifier = (ASTIdentifier) node.getChildren().get(0);
        assertEquals("simple", identifier.getValue());
        node.collapseThenPrint();
    }

    /**
     * Tests ambiguous name as two identifiers separated by ".".
     */
    @Test
    public void testAmbiguousNameOfTwoIdentifiers()
    {
        NamesParser parser = new Parser(new Scanner("one.two")).getNamesParser();
        ASTAmbiguousName node = parser.parseAmbiguousName();
        checkBinaryLeftAssociative(node, Arrays.asList(DOT), Arrays.asList("two", "one"),
                ASTAmbiguousName.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests ambiguous name as three identifiers separated by ".".
     */
    @Test
    public void testAmbiguousNameOfThreeIdentifiers()
    {
        NamesParser parser = new Parser(new Scanner("one.two.three")).getNamesParser();
        ASTAmbiguousName node = parser.parseAmbiguousName();
        checkBinaryLeftAssociative(node, Arrays.asList(DOT, DOT), Arrays.asList("three", "two", "one"),
                ASTAmbiguousName.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }
}
