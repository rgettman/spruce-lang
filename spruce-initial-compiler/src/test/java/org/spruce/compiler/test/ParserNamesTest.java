package org.spruce.compiler.test;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.ast.names.*;
import org.spruce.compiler.parser.NamesParser;
import org.spruce.compiler.parser.Parser;
import org.spruce.compiler.scanner.Scanner;

import static org.spruce.compiler.ast.ASTListNode.Type.*;
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
        ASTListNode node = parser.parseNamespaceName();
        checkList(node, NAMESPACE_IDS, ASTIdentifier.class, 1);
        ASTIdentifier identifier = (ASTIdentifier) node.getChildren().get(0);
        assertEquals("simple", identifier.getValue());
        node.print();
    }

    /**
     * Tests namespace name as two identifiers separated by ".".
     */
    @Test
    public void testNamespaceNameOfTwoIdentifiers() {
        NamesParser parser = getNamesParser("one.two");
        ASTListNode node = parser.parseNamespaceName();
        checkList(node, NAMESPACE_IDS, ASTIdentifier.class, 2);
        node.print();
    }

    /**
     * Tests namespace name as three identifiers separated by ".".
     */
    @Test
    public void testNamespaceNameOfThreeIdentifiers() {
        NamesParser parser = getNamesParser("one.two.three");
        ASTListNode node = parser.parseNamespaceName();
        checkList(node, NAMESPACE_IDS, ASTIdentifier.class, 3);
        node.print();
    }

    /**
     * Tests type name as a simple identifier.
     */
    @Test
    public void testTypeNameOfIdentifier() {
        NamesParser parser = getNamesParser("simple");
        ASTListNode node = parser.parseTypeName();
        checkList(node, TYPENAME_IDS, ASTIdentifier.class, 1);
        ASTIdentifier identifier = (ASTIdentifier) node.getChildren().get(0);
        assertEquals("simple", identifier.getValue());
        node.print();
    }

    /**
     * Tests type name as two identifiers separated by ".".
     */
    @Test
    public void testTypeNameOfTwoIdentifiers() {
        NamesParser parser = getNamesParser("one.two");
        ASTListNode node = parser.parseTypeName();
        checkList(node, TYPENAME_IDS, ASTIdentifier.class, 2);
        node.print();
    }

    /**
     * Tests type name as three identifiers separated by ".".
     */
    @Test
    public void testTypeNameOfThreeIdentifiers() {
        NamesParser parser = getNamesParser("one.two.three");
        ASTListNode node = parser.parseTypeName();
        checkList(node, TYPENAME_IDS, ASTIdentifier.class, 3);
        node.print();
    }

    /**
     * Tests namespace/type name as a simple identifier.
     */
    @Test
    public void testNamespaceOrTypeNameOfIdentifier() {
        NamesParser parser = getNamesParser("simple");
        ASTListNode node = parser.parseNamespaceOrTypeName();
        checkList(node, NAMESPACE_OR_TYPENAME_IDS, ASTIdentifier.class, 1);
        ASTIdentifier identifier = (ASTIdentifier) node.getChildren().get(0);
        assertEquals("simple", identifier.getValue());
        node.print();
    }

    /**
     * Tests namespace/type name as two identifiers separated by ".".
     */
    @Test
    public void testNamespaceOrTypeNameOfTwoIdentifiers() {
        NamesParser parser = getNamesParser("one.two");
        ASTListNode node = parser.parseNamespaceOrTypeName();
        checkList(node, NAMESPACE_OR_TYPENAME_IDS, ASTIdentifier.class, 2);
        node.print();
    }

    /**
     * Tests namespace/type name as three identifiers separated by ".".
     */
    @Test
    public void testNamespaceOrTypeNameOfThreeIdentifiers() {
        NamesParser parser = getNamesParser("one.two.three");
        ASTListNode node = parser.parseNamespaceOrTypeName();
        checkList(node, NAMESPACE_OR_TYPENAME_IDS, ASTIdentifier.class, 3);
        node.print();
    }

    /**
     * Tests expression name as a simple identifier.
     */
    @Test
    public void testExpressionNameOfIdentifier() {
        NamesParser parser = getNamesParser("simple");
        ASTListNode node = parser.parseExpressionName();
        checkList(node, EXPR_NAME_IDS, ASTIdentifier.class, 1);
        ASTIdentifier identifier = (ASTIdentifier) node.getChildren().get(0);
        assertEquals("simple", identifier.getValue());
        node.print();
    }

    /**
     * Tests expression name as two identifiers separated by ".".
     */
    @Test
    public void testExpressionNameOfTwoIdentifiers() {
        NamesParser parser = getNamesParser("one.two");
        ASTListNode node = parser.parseExpressionName();
        checkList(node, EXPR_NAME_IDS, ASTIdentifier.class, 2);
        node.print();
    }

    /**
     * Tests expression name as three identifiers separated by ".".
     */
    @Test
    public void testExpressionNameOfThreeIdentifiers() {
        NamesParser parser = getNamesParser("one.two.three");
        ASTListNode node = parser.parseExpressionName();
        checkList(node, EXPR_NAME_IDS, ASTIdentifier.class, 3);
        node.print();
    }

    /**
     * Tests ambiguous name as a simple identifier.
     */
    @Test
    public void testAmbiguousNameOfIdentifier() {
        NamesParser parser = getNamesParser("simple");
        ASTListNode node = parser.parseAmbiguousName();
        checkList(node, AMBIGUOUS_NAME_IDS, ASTIdentifier.class, 1);
        ASTIdentifier identifier = (ASTIdentifier) node.getChildren().get(0);
        assertEquals("simple", identifier.getValue());
        node.print();
    }

    /**
     * Tests ambiguous name as two identifiers separated by ".".
     */
    @Test
    public void testAmbiguousNameOfTwoIdentifiers() {
        NamesParser parser = getNamesParser("one.two");
        ASTListNode node = parser.parseAmbiguousName();
        checkList(node, AMBIGUOUS_NAME_IDS, ASTIdentifier.class, 2);
        node.print();
    }

    /**
     * Tests ambiguous name as three identifiers separated by ".".
     */
    @Test
    public void testAmbiguousNameOfThreeIdentifiers() {
        NamesParser parser = getNamesParser("one.two.three");
        ASTListNode node = parser.parseAmbiguousName();
        checkList(node, AMBIGUOUS_NAME_IDS, ASTIdentifier.class, 3);
        node.print();
    }

    /**
     * Tests identifier list of identifier.
     */
    @Test
    public void testIdentifierListOfIdentifier() {
        NamesParser parser = getNamesParser("ArrayList");
        ASTListNode node = parser.parseIdentifierList();
        checkList(node, IDENTIFIERS, ASTIdentifier.class, 1);
        ASTIdentifier id = (ASTIdentifier) node.getChildren().get(0);
        assertEquals("ArrayList", id.getValue());
        node.print();
    }

    /**
     * Tests identifier list of several identifiers.
     */
    @Test
    public void testIdentifierListNested() {
        NamesParser parser = getNamesParser("List, ArrayList, LinkedList");
        ASTListNode node = parser.parseIdentifierList();
        checkList(node, IDENTIFIERS, ASTIdentifier.class, 3);
        List<String> expectedValues = Arrays.asList("List", "ArrayList", "LinkedList");
        for (int i = 0; i < expectedValues.size(); i++) {
            ASTIdentifier id = (ASTIdentifier) node.getChildren().get(i);
            assertEquals(expectedValues.get(i), id.getValue(), "Mismatch on child " + i);
        }
        node.print();
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
