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
 * All tests for the parser related to names.
 */
public class ParserNamesTest
{
    /**
     * Tests type name as a simple identifier.
     */
    @Test
    public void testTypeNameIdentifier()
    {
        Parser parser = new Parser(new Scanner("simple"));
        ASTTypeName node = parser.parseTypeName();

        assertNull(node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTIdentifier);
        ASTIdentifier identifier = (ASTIdentifier) child;
        assertEquals("simple", identifier.getValue());

        node.collapse();
        node.print();
    }

    /**
     * Tests type name as two identifiers separated by ".".
     */
    @Test
    public void testTypeNameTwoIdentifiers()
    {
        Parser parser = new Parser(new Scanner("one.two"));
        ASTTypeName node = parser.parseTypeName();
        checkBinaryLeftAssociative(node, Arrays.asList(DOT), Arrays.asList("two", "one"),
                ASTPackageOrTypeName.class, ASTIdentifier.class);
    }

    /**
     * Tests type name as three identifiers separated by ".".
     */
    @Test
    public void testTypeNameThreeIdentifiers()
    {
        Parser parser = new Parser(new Scanner("one.two.three"));
        ASTTypeName node = parser.parseTypeName();
        checkBinaryLeftAssociative(node, Arrays.asList(DOT, DOT), Arrays.asList("three", "two", "one"),
                ASTPackageOrTypeName.class, ASTIdentifier.class);
    }

    /**
     * Tests package/type name as a simple identifier.
     */
    @Test
    public void testPackageOrTypeNameIdentifier()
    {
        Parser parser = new Parser(new Scanner("simple"));
        ASTPackageOrTypeName node = parser.parsePackageOrTypeName();

        assertNull(node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTIdentifier);
        ASTIdentifier identifier = (ASTIdentifier) child;
        assertEquals("simple", identifier.getValue());

        node.collapse();
        node.print();
    }

    /**
     * Tests package/type name as two identifiers separated by ".".
     */
    @Test
    public void testPackageOrTypeNameTwoIdentifiers()
    {
        Parser parser = new Parser(new Scanner("one.two"));
        ASTPackageOrTypeName node = parser.parsePackageOrTypeName();
        checkBinaryLeftAssociative(node, Arrays.asList(DOT), Arrays.asList("two", "one"),
                ASTPackageOrTypeName.class, ASTIdentifier.class);
    }

    /**
     * Tests package/type name as three identifiers separated by ".".
     */
    @Test
    public void testPackageOrTypeNameThreeIdentifiers()
    {
        Parser parser = new Parser(new Scanner("one.two.three"));
        ASTPackageOrTypeName node = parser.parsePackageOrTypeName();
        checkBinaryLeftAssociative(node, Arrays.asList(DOT, DOT), Arrays.asList("three", "two", "one"),
                ASTPackageOrTypeName.class, ASTIdentifier.class);
    }

    /**
     * Tests expression name as a simple identifier.
     */
    @Test
    public void testExpressionNameIdentifier()
    {
        Parser parser = new Parser(new Scanner("simple"));
        ASTExpressionName node = parser.parseExpressionName();

        assertNull(node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTIdentifier);
        ASTIdentifier identifier = (ASTIdentifier) child;
        assertEquals("simple", identifier.getValue());

        node.collapse();
        node.print();
    }

    /**
     * Tests expression name as two identifiers separated by ".".
     */
    @Test
    public void testExpressionNameTwoIdentifiers()
    {
        Parser parser = new Parser(new Scanner("one.two"));
        ASTExpressionName node = parser.parseExpressionName();
        checkBinaryLeftAssociative(node, Arrays.asList(DOT), Arrays.asList("two", "one"),
                ASTAmbiguousName.class, ASTIdentifier.class);
    }

    /**
     * Tests expression name as three identifiers separated by ".".
     */
    @Test
    public void testExpressionNameThreeIdentifiers()
    {
        Parser parser = new Parser(new Scanner("one.two.three"));
        ASTExpressionName node = parser.parseExpressionName();
        checkBinaryLeftAssociative(node, Arrays.asList(DOT, DOT), Arrays.asList("three", "two", "one"),
                ASTAmbiguousName.class, ASTIdentifier.class);
    }

    /**
     * Tests ambiguous name as a simple identifier.
     */
    @Test
    public void testAmbiguousNameIdentifier()
    {
        Parser parser = new Parser(new Scanner("simple"));
        ASTAmbiguousName node = parser.parseAmbiguousName();

        assertNull(node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTIdentifier);
        ASTIdentifier identifier = (ASTIdentifier) child;
        assertEquals("simple", identifier.getValue());

        node.collapse();
        node.print();
    }

    /**
     * Tests ambiguous name as two identifiers separated by ".".
     */
    @Test
    public void testAmbiguousNameTwoIdentifiers()
    {
        Parser parser = new Parser(new Scanner("one.two"));
        ASTAmbiguousName node = parser.parseAmbiguousName();
        checkBinaryLeftAssociative(node, Arrays.asList(DOT), Arrays.asList("two", "one"),
                ASTAmbiguousName.class, ASTIdentifier.class);
    }

    /**
     * Tests ambiguous name as three identifiers separated by ".".
     */
    @Test
    public void testAmbiguousNameThreeIdentifiers()
    {
        Parser parser = new Parser(new Scanner("one.two.three"));
        ASTAmbiguousName node = parser.parseAmbiguousName();
        checkBinaryLeftAssociative(node, Arrays.asList(DOT, DOT), Arrays.asList("three", "two", "one"),
                ASTAmbiguousName.class, ASTIdentifier.class);
    }
}
