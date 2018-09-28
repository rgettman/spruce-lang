package org.spruce.compiler.test;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.ast.*;
import org.spruce.compiler.parser.Parser;
import org.spruce.compiler.scanner.Scanner;
import static org.spruce.compiler.scanner.TokenType.*;
import static org.spruce.compiler.test.ParserTestUtility.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * All tests for the parser related to types.
 */
public class ParserTypesTest
{
    /**
     * Tests data type of data type (no array).
     */
    @Test
    public void testDataTypeDataTypeNoArray()
    {
        Parser parser = new Parser(new Scanner("spruce.lang.String"));
        ASTDataType node = parser.parseDataType();
        checkSimple(node, ASTDataTypeNoArray.class);
    }

    /**
     * Tests data type of array type.
     */
    @Test
    public void testDataTypeArrayType()
    {
        Parser parser = new Parser(new Scanner("spruce.lang.String[]"));
        ASTDataType node = parser.parseDataType();
        checkSimple(node, ASTArrayType.class);
    }

    /**
     * Tests array type.
     */
    @Test
    public void testArrayType()
    {
        Parser parser = new Parser(new Scanner("spruce.lang.String[][]"));
        ASTArrayType node = parser.parseArrayType();

        assertNull(node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(2, children.size());
        List<Class<?>> expectedClasses = Arrays.asList(ASTDataTypeNoArray.class, ASTDims.class);
        compareClasses(expectedClasses, children);

        node.collapse();
        node.print();
    }

    /**
     * Tests dims.
     */
    @Test
    public void testDims()
    {
        Parser parser = new Parser(new Scanner("[][][]"));
        ASTDims node = parser.parseDims(), curr = node;

        assertEquals(OPEN_CLOSE_BRACKET, curr.getOperation());
        List<ASTNode> children = curr.getChildren();
        assertEquals(1, children.size());
        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTDims);
        curr = (ASTDims) child;

        assertEquals(OPEN_CLOSE_BRACKET, curr.getOperation());
        children = curr.getChildren();
        assertEquals(1, children.size());
        child = children.get(0);
        assertTrue(child instanceof ASTDims);
        curr = (ASTDims) child;

        assertEquals(OPEN_CLOSE_BRACKET, curr.getOperation());
        children = curr.getChildren();
        assertEquals(0, children.size());

        node.collapse();
        node.print();
    }

    /**
     * Tests data type (no array) of simple type.
     */
    @Test
    public void testDataTypeNoArraySimpleType()
    {
        Parser parser = new Parser(new Scanner("List<?>"));
        ASTDataTypeNoArray node = parser.parseDataTypeNoArray();
        checkSimple(node, ASTSimpleType.class);
    }

    /**
     * Tests data type (no array) of "." and simple types.
     */
    @Test
    public void testDataTypeNoArray()
    {
        Parser parser = new Parser(new Scanner("A<?>.B<?>"));
        ASTDataTypeNoArray node = parser.parseDataTypeNoArray();
        checkBinaryLeftAssociative(node, Arrays.asList(DOT), ASTDataTypeNoArray.class, ASTSimpleType.class);
    }

    /**
     * Tests nested data type (no array) expressions.
     */
    @Test
    public void testDataTypeNoArrayNested()
    {
        Parser parser = new Parser(new Scanner("spruce.collections.List<?>"));
        ASTDataTypeNoArray node = parser.parseDataTypeNoArray();
        checkBinaryLeftAssociative(node, Arrays.asList(DOT, DOT), ASTDataTypeNoArray.class, ASTSimpleType.class);
    }

    /**
     * Tests simple type of identifier.
     */
    @Test
    public void testSimpleTypeIdentifier()
    {
        Parser parser = new Parser(new Scanner("Simple"));
        ASTSimpleType node = parser.parseSimpleType();

        assertNull(node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());
        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTIdentifier);
        ASTIdentifier id = (ASTIdentifier) child;
        assertEquals("Simple", id.getValue());
    }

    /**
     * Tests simple type of identifier and type arguments.
     */
    @Test
    public void testSimpleTypeIdentifierTypeArguments()
    {
        Parser parser = new Parser(new Scanner("Map<?, ?>"));
        ASTSimpleType node = parser.parseSimpleType();

        assertNull(node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(2, children.size());

        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTIdentifier);
        ASTIdentifier id = (ASTIdentifier) child;
        assertEquals("Map", id.getValue());

        child = children.get(1);
        assertTrue(child instanceof ASTTypeArguments);
    }

    /**
     * Tests intersection type of data type.
     */
    @Test
    public void testIntersectionTypeDataType()
    {
        Parser parser = new Parser(new Scanner("Student"));
        ASTIntersectionType node = parser.parseIntersectionType();
        checkSimple(node, ASTDataType.class);
    }

    /**
     * Tests intersection type.
     */
    @Test
    public void testIntersectionType()
    {
        Parser parser = new Parser(new Scanner("Student & Person"));
        ASTIntersectionType node = parser.parseIntersectionType();
        checkBinaryLeftAssociative(node, Arrays.asList(BITWISE_AND), ASTIntersectionType.class, ASTDataType.class);
    }

    /**
     * Tests nested intersection types.
     */
    @Test
    public void testIntersectionTypeNested()
    {
        Parser parser = new Parser(new Scanner("Student & Person & Learner"));
        ASTIntersectionType node = parser.parseIntersectionType();
        checkBinaryLeftAssociative(node, Arrays.asList(BITWISE_AND, BITWISE_AND), ASTIntersectionType.class, ASTDataType.class);
    }

    /**
     * Tests type arguments of type argument list.
     */
    @Test
    public void testTypeArgumentsTypeArgumentList()
    {
        Parser parser = new Parser(new Scanner("<?>"));
        ASTTypeArguments node = parser.parseTypeArguments();
        checkSimple(node, ASTTypeArgumentList.class, LESS_THAN);
    }

    /**
     * Tests type argument list of type argument.
     */
    @Test
    public void testTypeArgumentListTypeArgument()
    {
        Parser parser = new Parser(new Scanner("?"));
        ASTTypeArgumentList node = parser.parseTypeArgumentList();
        checkSimple(node, ASTTypeArgument.class);
    }

    /**
     * Tests argument list of nested argument lists (here, just multiple arguments).
     */
    @Test
    public void testTypeArgumentListNested()
    {
        Parser parser = new Parser(new Scanner("Employee, ?, ? <: Number"));
        ASTTypeArgumentList node = parser.parseTypeArgumentList();
        checkBinaryLeftAssociative(node, Arrays.asList(COMMA, COMMA), ASTTypeArgumentList.class, ASTTypeArgument.class);
    }

    /**
     * Tests type argument of wildcard.
     */
    @Test
    public void testTypeArgumentWildcard()
    {
        Parser parser = new Parser(new Scanner("?"));
        ASTTypeArgument node = parser.parseTypeArgument();
        checkSimple(node, ASTWildcard.class);
    }

    /**
     * Tests type argument of data type.
     */
    @Test
    public void testTypeArgumentDataType()
    {
        Parser parser = new Parser(new Scanner("Employee"));
        ASTTypeArgument node = parser.parseTypeArgument();
        checkSimple(node, ASTDataType.class);
    }

    /**
     * Tests wildcard by itself.
     */
    @Test
    public void testWildcard()
    {
        Parser parser = new Parser(new Scanner("?"));
        ASTWildcard node = parser.parseWildcard();

        assertEquals(QUESTION_MARK, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(0, children.size());

        node.collapse();
        node.print();
    }

    /**
     * Tests wildcard with bounds.
     */
    @Test
    public void testWildcardBounds()
    {
        Parser parser = new Parser(new Scanner("? <: Employee"));
        ASTWildcard node = parser.parseWildcard();

        assertEquals(QUESTION_MARK, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());
        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTWildcardBounds);

        node.collapse();
        node.print();
    }

    /**
     * Tests wildcard bounds of subtype.
     */
    @Test
    public void testWildcardBoundsSubtype()
    {
        Parser parser = new Parser(new Scanner("<: Employee"));
        ASTWildcardBounds node = parser.parseWildcardBounds();

        assertEquals(SUBTYPE, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());
        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTDataType);

        node.collapse();
        node.print();
    }

    /**
     * Tests wildcard bounds of supertype.
     */
    @Test
    public void testWildcardBoundsSupertype()
    {
        Parser parser = new Parser(new Scanner(":> Employee"));
        ASTWildcardBounds node = parser.parseWildcardBounds();

        assertEquals(SUPERTYPE, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());
        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTDataType);

        node.collapse();
        node.print();
    }
}
