package org.spruce.compiler.test;

import java.util.Arrays;
import java.util.List;

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
public class ParserNamesTest {
    /**
     * Tests namespace name as a simple identifier.
     */
    @Test
    public void testNamespaceNameOfIdentifier() {
        NamesParser parser = new Parser(new Scanner("simple")).getNamesParser();
        ASTNamespaceName node = parser.parseNamespaceName();
        checkSimple(node, ASTIdentifier.class);
        ASTIdentifier identifier = (ASTIdentifier) node.getChildren().get(0);
        assertEquals("simple", identifier.getValue());
        node.collapseThenPrint();
    }

    /**
     * Tests namespace name as two identifiers separated by ".".
     */
    @Test
    public void testNamespaceNameOfTwoIdentifiers() {
        NamesParser parser = new Parser(new Scanner("one.two")).getNamesParser();
        ASTNamespaceName node = parser.parseNamespaceName();
        checkBinaryLeftAssociative(node, Arrays.asList(DOT), Arrays.asList("two", "one"),
                ASTNamespaceName.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests namespace name as three identifiers separated by ".".
     */
    @Test
    public void testNamespaceNameOfThreeIdentifiers() {
        NamesParser parser = new Parser(new Scanner("one.two.three")).getNamesParser();
        ASTNamespaceName node = parser.parseNamespaceName();
        checkBinaryLeftAssociative(node, Arrays.asList(DOT, DOT), Arrays.asList("three", "two", "one"),
                ASTNamespaceName.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests type name as a simple identifier.
     */
    @Test
    public void testTypeNameOfIdentifier() {
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
    public void testTypeNameOfTwoIdentifiers() {
        NamesParser parser = new Parser(new Scanner("one.two")).getNamesParser();
        ASTTypeName node = parser.parseTypeName();
        checkBinaryLeftAssociative(node, Arrays.asList(DOT), Arrays.asList("two", "one"),
                ASTNamespaceOrTypeName.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests type name as three identifiers separated by ".".
     */
    @Test
    public void testTypeNameOfThreeIdentifiers() {
        NamesParser parser = new Parser(new Scanner("one.two.three")).getNamesParser();
        ASTTypeName node = parser.parseTypeName();
        checkBinaryLeftAssociative(node, Arrays.asList(DOT, DOT), Arrays.asList("three", "two", "one"),
                ASTNamespaceOrTypeName.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests namespace/type name as a simple identifier.
     */
    @Test
    public void testNamespaceOrTypeNameOfIdentifier() {
        NamesParser parser = new Parser(new Scanner("simple")).getNamesParser();
        ASTNamespaceOrTypeName node = parser.parseNamespaceOrTypeName();
        checkSimple(node, ASTIdentifier.class);
        ASTIdentifier identifier = (ASTIdentifier) node.getChildren().get(0);
        assertEquals("simple", identifier.getValue());
        node.collapseThenPrint();
    }

    /**
     * Tests namespace/type name as two identifiers separated by ".".
     */
    @Test
    public void testNamespaceOrTypeNameOfTwoIdentifiers() {
        NamesParser parser = new Parser(new Scanner("one.two")).getNamesParser();
        ASTNamespaceOrTypeName node = parser.parseNamespaceOrTypeName();
        checkBinaryLeftAssociative(node, Arrays.asList(DOT), Arrays.asList("two", "one"),
                ASTNamespaceOrTypeName.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests namespace/type name as three identifiers separated by ".".
     */
    @Test
    public void testNamespaceOrTypeNameOfThreeIdentifiers() {
        NamesParser parser = new Parser(new Scanner("one.two.three")).getNamesParser();
        ASTNamespaceOrTypeName node = parser.parseNamespaceOrTypeName();
        checkBinaryLeftAssociative(node, Arrays.asList(DOT, DOT), Arrays.asList("three", "two", "one"),
                ASTNamespaceOrTypeName.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests expression name as a simple identifier.
     */
    @Test
    public void testExpressionNameOfIdentifier() {
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
    public void testExpressionNameOfTwoIdentifiers() {
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
    public void testExpressionNameOfThreeIdentifiers() {
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
    public void testAmbiguousNameOfIdentifier() {
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
    public void testAmbiguousNameOfTwoIdentifiers() {
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
    public void testAmbiguousNameOfThreeIdentifiers() {
        NamesParser parser = new Parser(new Scanner("one.two.three")).getNamesParser();
        ASTAmbiguousName node = parser.parseAmbiguousName();
        checkBinaryLeftAssociative(node, Arrays.asList(DOT, DOT), Arrays.asList("three", "two", "one"),
                ASTAmbiguousName.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests identifier list of identifier.
     */
    @Test
    public void testIdentifierListOfIdentifier() {
        NamesParser parser = new Parser(new Scanner("ArrayList")).getNamesParser();
        ASTIdentifierList node = parser.parseIdentifierList();
        checkSimple(node, ASTIdentifier.class, COMMA);
        ASTIdentifier id = (ASTIdentifier) node.getChildren().get(0);
        assertEquals("ArrayList", id.getValue());
        node.collapseThenPrint();
    }

    /**
     * Tests identifier list of several identifiers.
     */
    @Test
    public void testIdentifierListNested() {
        NamesParser parser = new Parser(new Scanner("List, ArrayList, LinkedList")).getNamesParser();
        ASTIdentifierList node = parser.parseIdentifierList();
        checkList(node, COMMA, ASTIdentifier.class, 3);
        List<String> expectedValues = Arrays.asList("List", "ArrayList", "LinkedList");
        for (int i = 0; i < expectedValues.size(); i++) {
            ASTIdentifier id = (ASTIdentifier) node.getChildren().get(i);
            assertEquals(expectedValues.get(i), id.getValue(), "Mismatch on child " + i);
        }
        node.collapseThenPrint();
    }
}
