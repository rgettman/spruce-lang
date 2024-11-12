package org.spruce.compiler.test;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.names.*;
import org.spruce.compiler.exception.CompileException;
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
        ASTNamespaceName node = parser.parseNamespaceName();
        System.out.println(node);
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
        System.out.println(node);
        checkList(node, NAMESPACE_IDS, ASTIdentifier.class, 2);
    }

    /**
     * Tests namespace name as three identifiers separated by ".".
     */
    @Test
    public void testNamespaceNameOfThreeIdentifiers() {
        NamesParser parser = getNamesParser("one.two.three");
        ASTNamespaceName node = parser.parseNamespaceName();
        System.out.println(node);
        checkList(node, NAMESPACE_IDS, ASTIdentifier.class, 3);
    }

    /**
     * Tests type name as a simple identifier.
     */
    @Test
    public void testTypeNameOfIdentifier() {
        NamesParser parser = getNamesParser("simple");
        ASTTypeName node = parser.parseTypeName();
        System.out.println(node);
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
        System.out.println(node);
        checkList(node, TYPENAME_IDS, ASTIdentifier.class, 2);
    }

    /**
     * Tests type name as three identifiers separated by ".".
     */
    @Test
    public void testTypeNameOfThreeIdentifiers() {
        NamesParser parser = getNamesParser("one.two.three");
        ASTTypeName node = parser.parseTypeName();
        System.out.println(node);
        checkList(node, TYPENAME_IDS, ASTIdentifier.class, 3);
    }

    /**
     * Tests namespace/type name as a simple identifier.
     */
    @Test
    public void testNamespaceOrTypeNameOfIdentifier() {
        NamesParser parser = getNamesParser("simple");
        ASTNamespaceOrTypeName node = parser.parseNamespaceOrTypeName();
        System.out.println(node);
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
        ASTNamespaceOrTypeName node = parser.parseNamespaceOrTypeName();
        System.out.println(node);
        checkList(node, NAMESPACE_OR_TYPENAME_IDS, ASTIdentifier.class, 2);
    }

    /**
     * Tests namespace/type name as three identifiers separated by ".".
     */
    @Test
    public void testNamespaceOrTypeNameOfThreeIdentifiers() {
        NamesParser parser = getNamesParser("one.two.three");
        ASTNamespaceOrTypeName node = parser.parseNamespaceOrTypeName();
        System.out.println(node);
        checkList(node, NAMESPACE_OR_TYPENAME_IDS, ASTIdentifier.class, 3);
    }

    /**
     * Tests expression name as a simple identifier.
     */
    @Test
    public void testExpressionNameOfIdentifier() {
        NamesParser parser = getNamesParser("simple");
        ASTExpressionName node = parser.parseExpressionName();
        System.out.println(node);
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
        ASTExpressionName node = parser.parseExpressionName();
        System.out.println(node);
        checkList(node, EXPR_NAME_IDS, ASTIdentifier.class, 2);
    }

    /**
     * Tests expression name as three identifiers separated by ".".
     */
    @Test
    public void testExpressionNameOfThreeIdentifiers() {
        NamesParser parser = getNamesParser("one.two.three");
        ASTExpressionName node = parser.parseExpressionName();
        System.out.println(node);
        checkList(node, EXPR_NAME_IDS, ASTIdentifier.class, 3);
    }

    /**
     * Tests ambiguous name as a simple identifier.
     */
    @Test
    public void testAmbiguousNameOfIdentifier() {
        NamesParser parser = getNamesParser("simple");
        ASTAmbiguousName node = parser.parseAmbiguousName();
        System.out.println(node);
        checkList(node, AMBIGUOUS_NAME_IDS, ASTIdentifier.class, 1);
        ASTIdentifier identifier = (ASTIdentifier) node.getChildren().get(0);
        assertEquals("simple", identifier.getValue());
    }

    /**
     * Tests ambiguous name as two identifiers separated by ".".
     */
    @Test
    public void testAmbiguousNameOfTwoIdentifiers() {
        NamesParser parser = getNamesParser("one.two");
        ASTAmbiguousName node = parser.parseAmbiguousName();
        System.out.println(node);
        checkList(node, AMBIGUOUS_NAME_IDS, ASTIdentifier.class, 2);
    }

    /**
     * Tests ambiguous name as three identifiers separated by ".".
     */
    @Test
    public void testAmbiguousNameOfThreeIdentifiers() {
        NamesParser parser = getNamesParser("one.two.three");
        ASTAmbiguousName node = parser.parseAmbiguousName();
        System.out.println(node);
        checkList(node, AMBIGUOUS_NAME_IDS, ASTIdentifier.class, 3);
    }

    /**
     * Tests identifier list of identifier.
     */
    @Test
    public void testIdentifierListOfIdentifier() {
        NamesParser parser = getNamesParser("ArrayList");
        ASTIdentifierList node = parser.parseIdentifierList();
        System.out.println(node);
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
        System.out.println(node);
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
        assertEquals("x", node.getValue());
    }

    /**
     * Tests bad identifier.
     */
    @Test
    public void testIdentifierBad() {
        NamesParser parser = getNamesParser("12x");
        assertThrows(CompileException.class, parser::parseIdentifier, "Expected an identifier.");
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
