package org.spruce.compiler.test;

import java.util.Arrays;

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
    public void testDataTypeOfDataTypeNoArray()
    {
        Parser parser = new Parser(new Scanner("spruce.lang.String"));
        ASTDataType node = parser.parseDataType();
        checkSimple(node, ASTDataTypeNoArray.class);
        node.collapseThenPrint();
    }

    /**
     * Tests data type of array type.
     */
    @Test
    public void testDataTypeOfArrayType()
    {
        Parser parser = new Parser(new Scanner("spruce.lang.String[]"));
        ASTDataType node = parser.parseDataType();
        checkSimple(node, ASTArrayType.class);
        node.collapseThenPrint();
    }

    /**
     * Tests array type.
     */
    @Test
    public void testArrayType()
    {
        Parser parser = new Parser(new Scanner("spruce.lang.String[][]"));
        ASTArrayType node = parser.parseArrayType();
        checkBinary(node, ASTDataTypeNoArray.class, ASTDims.class);
        node.collapseThenPrint();
    }

    /**
     * Tests dims.
     */
    @Test
    public void testDims()
    {
        Parser parser = new Parser(new Scanner("[][][]"));
        ASTDims node = parser.parseDims();
        checkSimple(node, ASTDims.class, OPEN_CLOSE_BRACKET);

        ASTDims child = (ASTDims) node.getChildren().get(0);
        checkSimple(child, ASTDims.class, OPEN_CLOSE_BRACKET);
        child = (ASTDims) child.getChildren().get(0);

        checkEmpty(child, OPEN_CLOSE_BRACKET);
        node.collapseThenPrint();
    }

    /**
     * Tests data type (no array) of simple type.
     */
    @Test
    public void testDataTypeNoArrayOfSimpleType()
    {
        Parser parser = new Parser(new Scanner("List<?>"));
        ASTDataTypeNoArray node = parser.parseDataTypeNoArray();
        checkSimple(node, ASTSimpleType.class);
        node.collapseThenPrint();
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
        node.collapseThenPrint();
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
        node.collapseThenPrint();
    }

    /**
     * Tests simple type of identifier.
     */
    @Test
    public void testSimpleTypeIdentifier()
    {
        Parser parser = new Parser(new Scanner("Simple"));
        ASTSimpleType node = parser.parseSimpleType();
        checkSimple(node, ASTIdentifier.class);
        ASTIdentifier id = (ASTIdentifier) node.getChildren().get(0);
        assertEquals("Simple", id.getValue());
        node.collapseThenPrint();
    }

    /**
     * Tests simple type of identifier and type arguments.
     */
    @Test
    public void testSimpleTypeOfIdentifierTypeArguments()
    {
        Parser parser = new Parser(new Scanner("Map<?, ?>"));
        ASTSimpleType node = parser.parseSimpleType();
        checkBinary(node, ASTIdentifier.class, ASTTypeArguments.class);
        ASTIdentifier id = (ASTIdentifier) node.getChildren().get(0);
        assertEquals("Map", id.getValue());
        node.collapseThenPrint();
    }

    /**
     * Tests intersection type of data type.
     */
    @Test
    public void testIntersectionTypeOfDataType()
    {
        Parser parser = new Parser(new Scanner("Student"));
        ASTIntersectionType node = parser.parseIntersectionType();
        checkSimple(node, ASTDataType.class);
        node.collapseThenPrint();
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
        node.collapseThenPrint();
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
        node.collapseThenPrint();
    }

    /**
     * Tests type arguments of type argument list.
     */
    @Test
    public void testTypeArgumentsOfTypeArgumentList()
    {
        Parser parser = new Parser(new Scanner("<?>"));
        ASTTypeArguments node = parser.parseTypeArguments();
        checkSimple(node, ASTTypeArgumentList.class, LESS_THAN);
        node.collapseThenPrint();
    }

    /**
     * Tests type argument list of type argument.
     */
    @Test
    public void testTypeArgumentListOfTypeArgument()
    {
        Parser parser = new Parser(new Scanner("?"));
        ASTTypeArgumentList node = parser.parseTypeArgumentList();
        checkSimple(node, ASTTypeArgument.class, COMMA);
        node.collapseThenPrint();
    }

    /**
     * Tests argument list of nested argument lists (here, just multiple arguments).
     */
    @Test
    public void testTypeArgumentListNested()
    {
        Parser parser = new Parser(new Scanner("Employee, ?, ? <: Number"));
        ASTTypeArgumentList node = parser.parseTypeArgumentList();
        checkList(node, COMMA, ASTTypeArgument.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests type argument of wildcard.
     */
    @Test
    public void testTypeArgumentOfWildcard()
    {
        Parser parser = new Parser(new Scanner("?"));
        ASTTypeArgument node = parser.parseTypeArgument();
        checkSimple(node, ASTWildcard.class);
        node.collapseThenPrint();
    }

    /**
     * Tests type argument of data type.
     */
    @Test
    public void testTypeArgumentOfDataType()
    {
        Parser parser = new Parser(new Scanner("Employee"));
        ASTTypeArgument node = parser.parseTypeArgument();
        checkSimple(node, ASTDataType.class);
        node.collapseThenPrint();
    }

    /**
     * Tests wildcard by itself.
     */
    @Test
    public void testWildcard()
    {
        Parser parser = new Parser(new Scanner("?"));
        ASTWildcard node = parser.parseWildcard();
        checkEmpty(node, QUESTION_MARK);
        node.collapseThenPrint();
    }

    /**
     * Tests wildcard with bounds.
     */
    @Test
    public void testWildcardBounds()
    {
        Parser parser = new Parser(new Scanner("? <: Employee"));
        ASTWildcard node = parser.parseWildcard();
        checkSimple(node, ASTWildcardBounds.class, QUESTION_MARK);
        node.collapseThenPrint();
    }

    /**
     * Tests wildcard bounds of subtype.
     */
    @Test
    public void testWildcardBoundsOfSubtype()
    {
        Parser parser = new Parser(new Scanner("<: Employee"));
        ASTWildcardBounds node = parser.parseWildcardBounds();
        checkSimple(node, ASTDataType.class, SUBTYPE);
        node.collapseThenPrint();
    }

    /**
     * Tests wildcard bounds of supertype.
     */
    @Test
    public void testWildcardBoundsOfSupertype()
    {
        Parser parser = new Parser(new Scanner(":> Employee"));
        ASTWildcardBounds node = parser.parseWildcardBounds();
        checkSimple(node, ASTDataType.class, SUPERTYPE);
        node.collapseThenPrint();
    }
}
