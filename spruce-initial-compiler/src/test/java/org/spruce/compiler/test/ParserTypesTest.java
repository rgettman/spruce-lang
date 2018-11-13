package org.spruce.compiler.test;

import java.util.Arrays;

import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.types.*;
import org.spruce.compiler.parser.TypesParser;
import org.spruce.compiler.scanner.Scanner;
import static org.spruce.compiler.scanner.TokenType.*;
import static org.spruce.compiler.test.ParserTestUtility.*;

import org.junit.jupiter.api.Test;
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
        TypesParser parser = new TypesParser(new Scanner("spruce.lang.String"));
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
        TypesParser parser = new TypesParser(new Scanner("spruce.lang.String[]"));
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
        TypesParser parser = new TypesParser(new Scanner("spruce.lang.String[][]"));
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
        TypesParser parser = new TypesParser(new Scanner("[][][]"));
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
        TypesParser parser = new TypesParser(new Scanner("List<?>"));
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
        TypesParser parser = new TypesParser(new Scanner("A<?>.B<?>"));
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
        TypesParser parser = new TypesParser(new Scanner("spruce.collections.List<?>"));
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
        TypesParser parser = new TypesParser(new Scanner("Simple"));
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
        TypesParser parser = new TypesParser(new Scanner("Map<?, ?>"));
        ASTSimpleType node = parser.parseSimpleType();
        checkBinary(node, ASTIdentifier.class, ASTTypeArguments.class);
        ASTIdentifier id = (ASTIdentifier) node.getChildren().get(0);
        assertEquals("Map", id.getValue());
        node.collapseThenPrint();
    }

    /**
     * Tests type parameters of type parameter list.
     */
    @Test
    public void testTypeParametersOfTypeParameterList()
    {
        TypesParser parser = new TypesParser(new Scanner("<K, V>"));
        ASTTypeParameters node = parser.parseTypeParameters();
        checkSimple(node, ASTTypeParameterList.class, LESS_THAN);
        node.collapseThenPrint();
    }

    /**
     * Tests type parameter list of type parameter.
     */
    @Test
    public void testTypeParameterListOfTypeParameter()
    {
        TypesParser parser = new TypesParser(new Scanner("E"));
        ASTTypeParameterList node = parser.parseTypeParameterList();
        checkSimple(node, ASTTypeParameter.class, COMMA);
        node.collapseThenPrint();
    }

    /**
     * Tests parameter list of nested parameter lists (here, just multiple parameters).
     */
    @Test
    public void testTypeParameterListNested()
    {
        TypesParser parser = new TypesParser(new Scanner("K, V, T <: Map<K, V>"));
        ASTTypeParameterList node = parser.parseTypeParameterList();
        checkList(node, COMMA, ASTTypeParameter.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests simple type parameter.
     */
    @Test
    public void testTypeParameterSimple()
    {
        TypesParser parser = new TypesParser(new Scanner("T"));
        ASTTypeParameter node = parser.parseTypeParameter();
        checkSimple(node, ASTIdentifier.class);
        ASTIdentifier id = (ASTIdentifier) node.getChildren().get(0);
        assertEquals("T", id.getValue());
        node.collapseThenPrint();
    }

    /**
     * Tests type parameter of bounds (intersection type).
     */
    @Test
    public void testTypeParameterOfBounds()
    {
        TypesParser parser = new TypesParser(new Scanner("N <: Number"));
        ASTTypeParameter node = parser.parseTypeParameter();
        checkBinary(node, ASTIdentifier.class, ASTTypeBound.class);
        ASTIdentifier id = (ASTIdentifier) node.getChildren().get(0);
        assertEquals("N", id.getValue());
        node.collapseThenPrint();
    }

    /**
     * Tests type bound of intersection type.
     */
    @Test
    public void testTypeBoundOfIntersectionType()
    {
        TypesParser parser = new TypesParser(new Scanner("<: Student & Serializable"));
        ASTTypeBound node = parser.parseTypeBound();
        checkSimple(node, ASTIntersectionType.class, SUBTYPE);
    }

    /**
     * Tests intersection type of data type.
     */
    @Test
    public void testIntersectionTypeOfDataType()
    {
        TypesParser parser = new TypesParser(new Scanner("Student"));
        ASTIntersectionType node = parser.parseIntersectionType();
        checkSimple(node, ASTDataType.class, BITWISE_AND);
        node.collapseThenPrint();
    }

    /**
     * Tests intersection type.
     */
    @Test
    public void testIntersectionType()
    {
        TypesParser parser = new TypesParser(new Scanner("Student & Person"));
        ASTIntersectionType node = parser.parseIntersectionType();
        checkList(node, BITWISE_AND, ASTDataType.class, 2);
        node.collapseThenPrint();
    }

    /**
     * Tests nested intersection types.
     */
    @Test
    public void testIntersectionTypeNested()
    {
        TypesParser parser = new TypesParser(new Scanner("Student & Person & Learner"));
        ASTIntersectionType node = parser.parseIntersectionType();
        checkList(node, BITWISE_AND, ASTDataType.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests type arguments of type argument list.
     */
    @Test
    public void testTypeArgumentsOfTypeArgumentList()
    {
        TypesParser parser = new TypesParser(new Scanner("<?>"));
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
        TypesParser parser = new TypesParser(new Scanner("?"));
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
        TypesParser parser = new TypesParser(new Scanner("Employee, ?, ? <: Number"));
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
        TypesParser parser = new TypesParser(new Scanner("?"));
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
        TypesParser parser = new TypesParser(new Scanner("Employee"));
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
        TypesParser parser = new TypesParser(new Scanner("?"));
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
        TypesParser parser = new TypesParser(new Scanner("? <: Employee"));
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
        TypesParser parser = new TypesParser(new Scanner("<: Employee"));
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
        TypesParser parser = new TypesParser(new Scanner(":> Employee"));
        ASTWildcardBounds node = parser.parseWildcardBounds();
        checkSimple(node, ASTDataType.class, SUPERTYPE);
        node.collapseThenPrint();
    }
}
