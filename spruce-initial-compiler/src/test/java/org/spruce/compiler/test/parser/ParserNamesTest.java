package org.spruce.compiler.test.parser;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.names.*;
import org.spruce.compiler.common.BaseMessageProducer;
import org.spruce.compiler.parser.NamesParser;
import org.spruce.compiler.parser.Parser;
import org.spruce.compiler.scanner.Scanner;

import static org.spruce.compiler.ast.ASTListNode.Type.*;
import static org.spruce.compiler.test.parser.ParserTestUtility.*;

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
        ensureNoErrors(node, parser);
        checkList(node, NAMESPACE_IDS, ASTIdentifier.class, 1);
        ASTIdentifier identifier = (ASTIdentifier) node.getChildren().get(0);
        assertEquals("simple", identifier.getValue());
    }

    /**
     * Tests namespace name as two identifiers separated by ".".
     */
    @Test
    public void testNamespaceNameOfTwoIdentifiers() {
        NamesParser parser = getNamesParser("one.two");
        ASTNamespaceName node = parser.parseNamespaceName();
        ensureNoErrors(node, parser);
        checkList(node, NAMESPACE_IDS, ASTIdentifier.class, 2);
    }

    /**
     * Tests namespace name as three identifiers separated by ".".
     */
    @Test
    public void testNamespaceNameOfThreeIdentifiers() {
        NamesParser parser = getNamesParser("one.two.three");
        ASTNamespaceName node = parser.parseNamespaceName();
        ensureNoErrors(node, parser);
        checkList(node, NAMESPACE_IDS, ASTIdentifier.class, 3);
    }

    /**
     * Tests type name as a simple identifier.
     */
    @Test
    public void testTypeNameOfIdentifier() {
        NamesParser parser = getNamesParser("simple");
        ASTTypeName node = parser.parseTypeName();
        ensureNoErrors(node, parser);
        checkList(node, TYPENAME_IDS, ASTIdentifier.class, 1);
        ASTIdentifier identifier = (ASTIdentifier) node.getChildren().get(0);
        assertEquals("simple", identifier.getValue());
    }

    /**
     * Tests type name as two identifiers separated by ".".
     */
    @Test
    public void testTypeNameOfTwoIdentifiers() {
        NamesParser parser = getNamesParser("one.two");
        ASTTypeName node = parser.parseTypeName();
        ensureNoErrors(node, parser);
        checkList(node, TYPENAME_IDS, ASTIdentifier.class, 2);
    }

    /**
     * Tests type name as three identifiers separated by ".".
     */
    @Test
    public void testTypeNameOfThreeIdentifiers() {
        NamesParser parser = getNamesParser("one.two.three");
        ASTTypeName node = parser.parseTypeName();
        ensureNoErrors(node, parser);
        checkList(node, TYPENAME_IDS, ASTIdentifier.class, 3);
    }

    /**
     * Tests namespace/type name as a simple identifier.
     */
    @Test
    public void testNamespaceOrTypeNameOfIdentifier() {
        NamesParser parser = getNamesParser("simple");
        ASTNamespaceOrTypeName node = parser.parseTypeName().convertToNamespaceOrTypeName();
        ensureNoErrors(node, parser);
        checkList(node, NAMESPACE_OR_TYPENAME_IDS, ASTIdentifier.class, 1);
        ASTIdentifier identifier = (ASTIdentifier) node.getChildren().get(0);
        assertEquals("simple", identifier.getValue());
    }

    /**
     * Tests namespace/type name as two identifiers separated by ".".
     */
    @Test
    public void testNamespaceOrTypeNameOfTwoIdentifiers() {
        NamesParser parser = getNamesParser("one.two");
        ASTNamespaceOrTypeName node = parser.parseTypeName().convertToNamespaceOrTypeName();
        ensureNoErrors(node, parser);
        checkList(node, NAMESPACE_OR_TYPENAME_IDS, ASTIdentifier.class, 2);
    }

    /**
     * Tests namespace/type name as three identifiers separated by ".".
     */
    @Test
    public void testNamespaceOrTypeNameOfThreeIdentifiers() {
        NamesParser parser = getNamesParser("one.two.three");
        ASTNamespaceOrTypeName node = parser.parseTypeName().convertToNamespaceOrTypeName();
        ensureNoErrors(node, parser);
        checkList(node, NAMESPACE_OR_TYPENAME_IDS, ASTIdentifier.class, 3);
    }

    /**
     * Tests expression name as a simple identifier.
     */
    @Test
    public void testExpressionNameOfIdentifier() {
        NamesParser parser = getNamesParser("simple");
        ASTExpressionName node = parser.getTypesParser().parseDataType().convertToExpressionName();
        ensureNoErrors(node, parser);
        checkList(node, EXPR_NAME_IDS, ASTIdentifier.class, 1);
        ASTIdentifier identifier = (ASTIdentifier) node.getChildren().get(0);
        assertEquals("simple", identifier.getValue());
    }

    /**
     * Tests expression name as two identifiers separated by ".".
     */
    @Test
    public void testExpressionNameOfTwoIdentifiers() {
        NamesParser parser = getNamesParser("one.two");
        ASTExpressionName node = parser.getTypesParser().parseDataType().convertToExpressionName();
        ensureNoErrors(node, parser);
        checkList(node, EXPR_NAME_IDS, ASTIdentifier.class, 2);
    }

    /**
     * Tests expression name as three identifiers separated by ".".
     */
    @Test
    public void testExpressionNameOfThreeIdentifiers() {
        NamesParser parser = getNamesParser("one.two.three");
        ASTExpressionName node = parser.getTypesParser().parseDataType().convertToExpressionName();
        ensureNoErrors(node, parser);
        checkList(node, EXPR_NAME_IDS, ASTIdentifier.class, 3);
    }

    /**
     * Tests identifier list of identifier.
     */
    @Test
    public void testIdentifierListOfIdentifier() {
        NamesParser parser = getNamesParser("ArrayList");
        ASTIdentifierList node = parser.parseIdentifierList();
        ensureNoErrors(node, parser);
        checkList(node, IDENTIFIERS, ASTIdentifier.class, 1);
        ASTIdentifier id = (ASTIdentifier) node.getChildren().get(0);
        assertEquals("ArrayList", id.getValue());
    }

    /**
     * Tests identifier list of several identifiers.
     */
    @Test
    public void testIdentifierListNested() {
        NamesParser parser = getNamesParser("List, ArrayList, LinkedList");
        ASTIdentifierList node = parser.parseIdentifierList();
        ensureNoErrors(node, parser);
        checkList(node, IDENTIFIERS, ASTIdentifier.class, 3);
        List<String> expectedValues = Arrays.asList("List", "ArrayList", "LinkedList");
        for (int i = 0; i < expectedValues.size(); i++) {
            ASTIdentifier id = (ASTIdentifier) node.getChildren().get(i);
            assertEquals(expectedValues.get(i), id.getValue(), "Mismatch on child " + i);
        }
    }

    /**
     * Tests identifier.
     */
    @Test
    public void testIdentifier() {
        NamesParser parser = getNamesParser("x");
        ASTIdentifier node = parser.parseIdentifier();
        ensureNoErrors(node, parser);
        assertEquals("x", node.getValue());
    }

    /**
     * Tests bad identifier.
     */
    @Test
    public void testIdentifierBad() {
        NamesParser parser = getNamesParser("12x");
        ASTIdentifier node = parser.parseIdentifier();
        expectError(node, parser);
    }

    /**
     * Helper method to get a <code>NamesParser</code> directly from code.
     * @param code The code to test.
     * @return A <code>NamesParser</code> that will parse the given code.
     */
    public static NamesParser getNamesParser(String code) {
        return new Parser(new Scanner(code), new BaseMessageProducer()).getNamesParser();
    }
}
