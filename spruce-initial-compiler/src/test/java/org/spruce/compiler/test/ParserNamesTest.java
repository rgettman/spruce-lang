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
        NamesParser parser = getNamesParser("simple");
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
        NamesParser parser = getNamesParser("one.two");
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
        NamesParser parser = getNamesParser("one.two.three");
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
        NamesParser parser = getNamesParser("simple");
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
        NamesParser parser = getNamesParser("one.two");
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
        NamesParser parser = getNamesParser("one.two.three");
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
        NamesParser parser = getNamesParser("simple");
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
        NamesParser parser = getNamesParser("one.two");
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
        NamesParser parser = getNamesParser("one.two.three");
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
        NamesParser parser = getNamesParser("simple");
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
        NamesParser parser = getNamesParser("one.two");
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
        NamesParser parser = getNamesParser("one.two.three");
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
        NamesParser parser = getNamesParser("simple");
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
        NamesParser parser = getNamesParser("one.two");
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
        NamesParser parser = getNamesParser("one.two.three");
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
        NamesParser parser = getNamesParser("ArrayList");
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
        NamesParser parser = getNamesParser("List, ArrayList, LinkedList");
        ASTIdentifierList node = parser.parseIdentifierList();
        checkList(node, COMMA, ASTIdentifier.class, 3);
        List<String> expectedValues = Arrays.asList("List", "ArrayList", "LinkedList");
        for (int i = 0; i < expectedValues.size(); i++) {
            ASTIdentifier id = (ASTIdentifier) node.getChildren().get(i);
            assertEquals(expectedValues.get(i), id.getValue(), "Mismatch on child " + i);
        }
        node.collapseThenPrint();
    }

    /**
     * Helper method to get a <code>NamesParser</code> directly from code.
     * @param code The code to test.
     * @return A <code>NamesParser</code> that will parse the given code.
     */
    private static NamesParser getNamesParser(String code) {
        return new Parser(new Scanner(code)).getNamesParser();
    }
}
