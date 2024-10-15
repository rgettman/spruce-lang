package org.spruce.compiler.test;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.types.*;
import org.spruce.compiler.parser.Parser;
import org.spruce.compiler.parser.TypesParser;
import org.spruce.compiler.scanner.Scanner;

import static org.spruce.compiler.ast.ASTListNode.Type.*;
import static org.spruce.compiler.scanner.TokenType.*;
import static org.spruce.compiler.test.ParserTestUtility.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * All tests for the parser related to types.
 */
public class ParserTypesTest {
    /**
     * Tests data type no array of data type no array.
     */
    @Test
    public void testDataTypeNoArrayListOfClassPart() {
        TypesParser parser = getTypesParser("Serializable");
        ASTListNode node = parser.parseDataTypeNoArrayList();
        node.print();
        checkList(node, DATA_TYPES_NO_ARRAY, ASTListNode.class, 1);
    }

    /**
     * Tests data type no array list.
     */
    @Test
    public void testDataTypeNoArrayList() {
        TypesParser parser = getTypesParser("Serializable, Comparable<T>");
        ASTListNode node = parser.parseDataTypeNoArrayList();
        node.print();
        checkList(node, DATA_TYPES_NO_ARRAY, ASTListNode.class, 2);
    }
    
    /**
     * Tests nested data type no array lists.
     */
    @Test
    public void testDataTypeNoArrayListNested() {
        TypesParser parser = getTypesParser("Serializable, Comparable<T>, RandomAccess");
        ASTListNode node = parser.parseDataTypeNoArrayList();
        node.print();
        checkList(node, DATA_TYPES_NO_ARRAY, ASTListNode.class, 3);
    }
    
    /**
     * Tests data type of data type (no array).
     */
    @Test
    public void testDataTypeOfDataTypeNoArray() {
        TypesParser parser = getTypesParser("spruce.lang.String");
        ASTDataType node = parser.parseDataType();
        node.print();
        checkSimple(node, ASTListNode.class);
    }

    /**
     * Tests data type of array type.
     */
    @Test
    public void testDataTypeOfArrayType() {
        TypesParser parser = getTypesParser("spruce.lang.String[]");
        ASTDataType node = parser.parseDataType();
        node.print();
        checkSimple(node, ASTArrayType.class);
    }

    /**
     * Tests array type.
     */
    @Test
    public void testArrayType() {
        TypesParser parser = getTypesParser("spruce.lang.String[][]");
        ASTArrayType node = parser.parseArrayType();
        node.print();
        checkBinary(node, ASTListNode.class, ASTDims.class);
    }

    /**
     * Tests dims.
     */
    @Test
    public void testDims() {
        TypesParser parser = getTypesParser("[][][]");
        ASTDims node = parser.parseDims();
        node.print();
        checkSimple(node, ASTDims.class, OPEN_CLOSE_BRACKET);

        ASTDims child = (ASTDims) node.getChildren().get(0);
        checkSimple(child, ASTDims.class, OPEN_CLOSE_BRACKET);
        child = (ASTDims) child.getChildren().get(0);

        checkEmpty(child, OPEN_CLOSE_BRACKET);
    }

    /**
     * Tests data type (no array) of simple type.
     */
    @Test
    public void testDataTypeNoArrayOfSimpleType() {
        TypesParser parser = getTypesParser("List<?>");
        ASTListNode node = parser.parseDataTypeNoArray();
        node.print();
        checkList(node, SIMPLE_TYPES, ASTSimpleType.class, 1);
    }

    /**
     * Tests data type (no array) of "." and simple types.
     */
    @Test
    public void testDataTypeNoArray() {
        TypesParser parser = getTypesParser("A<?>.B<?>");
        ASTListNode node = parser.parseDataTypeNoArray();
        node.print();
        checkList(node, SIMPLE_TYPES, ASTSimpleType.class, 2);
    }

    /**
     * Tests nested data type (no array) expressions.
     */
    @Test
    public void testDataTypeNoArrayNested() {
        TypesParser parser = getTypesParser("spruce.collections.List<?>");
        ASTListNode node = parser.parseDataTypeNoArray();
        node.print();
        checkList(node, SIMPLE_TYPES, ASTSimpleType.class, 3);
    }

    /**
     * Tests simple type of identifier.
     */
    @Test
    public void testSimpleTypeIdentifier() {
        TypesParser parser = getTypesParser("Simple");
        ASTSimpleType node = parser.parseSimpleType();
        node.print();
        checkSimple(node, ASTIdentifier.class);
        ASTIdentifier id = (ASTIdentifier) node.getChildren().get(0);
        assertEquals("Simple", id.getValue());
    }

    /**
     * Tests simple type of identifier and type arguments.
     */
    @Test
    public void testSimpleTypeOfIdentifierTypeArguments() {
        TypesParser parser = getTypesParser("Map<?, ?>");
        ASTSimpleType node = parser.parseSimpleType();
        node.print();
        checkBinary(node, ASTIdentifier.class, ASTTypeArguments.class);
        ASTIdentifier id = (ASTIdentifier) node.getChildren().get(0);
        assertEquals("Map", id.getValue());
    }

    /**
     * Tests type parameters of type parameter list.
     */
    @Test
    public void testTypeParametersOfTypeParameterList() {
        TypesParser parser = getTypesParser("<K, V>");
        ASTTypeParameters node = parser.parseTypeParameters();
        node.print();
        checkSimple(node, ASTListNode.class, LESS_THAN);
    }

    /**
     * Tests type parameter list of type parameter.
     */
    @Test
    public void testTypeParameterListOfTypeParameter() {
        TypesParser parser = getTypesParser("E");
        ASTListNode node = parser.parseTypeParameterList();
        node.print();
        checkList(node, TYPE_PARAMETERS, ASTTypeParameter.class, 1);
    }

    /**
     * Tests parameter list of nested parameter lists (here, just multiple parameters).
     */
    @Test
    public void testTypeParameterListNested() {
        TypesParser parser = getTypesParser("K, V, T <: Map<K, V>");
        ASTListNode node = parser.parseTypeParameterList();
        node.print();
        checkList(node, TYPE_PARAMETERS, ASTTypeParameter.class, 3);
    }

    /**
     * Tests simple type parameter.
     */
    @Test
    public void testTypeParameterSimple() {
        TypesParser parser = getTypesParser("T");
        ASTTypeParameter node = parser.parseTypeParameter();
        node.print();
        checkSimple(node, ASTIdentifier.class);
        ASTIdentifier id = (ASTIdentifier) node.getChildren().get(0);
        assertEquals("T", id.getValue());
    }

    /**
     * Tests type parameter of bounds (intersection type).
     */
    @Test
    public void testTypeParameterOfBounds() {
        TypesParser parser = getTypesParser("N <: Number");
        ASTTypeParameter node = parser.parseTypeParameter();
        node.print();
        checkBinary(node, ASTIdentifier.class, ASTTypeBound.class);
        ASTIdentifier id = (ASTIdentifier) node.getChildren().get(0);
        assertEquals("N", id.getValue());
    }

    /**
     * Tests type bound of intersection type.
     */
    @Test
    public void testTypeBoundOfIntersectionType() {
        TypesParser parser = getTypesParser("<: Student & Serializable");
        ASTTypeBound node = parser.parseTypeBound();
        node.print();
        checkSimple(node, ASTListNode.class, SUBTYPE);
    }

    /**
     * Tests intersection type of data type.
     */
    @Test
    public void testIntersectionTypeOfDataType() {
        TypesParser parser = getTypesParser("Student");
        ASTListNode node = parser.parseIntersectionType();
        node.print();
        checkList(node, INTERSECTION_TYPES, ASTDataType.class, 1);
    }

    /**
     * Tests intersection type.
     */
    @Test
    public void testIntersectionType() {
        TypesParser parser = getTypesParser("Student & Person");
        ASTListNode node = parser.parseIntersectionType();
        node.print();
        checkList(node, INTERSECTION_TYPES, ASTDataType.class, 2);
    }

    /**
     * Tests nested intersection types.
     */
    @Test
    public void testIntersectionTypeNested() {
        TypesParser parser = getTypesParser("Student & Person & Learner");
        ASTListNode node = parser.parseIntersectionType();
        node.print();
        checkList(node, INTERSECTION_TYPES, ASTDataType.class, 3);
    }

    /**
     * Tests type arguments of type argument list.
     */
    @Test
    public void testTypeArgumentsOfTypeArgumentList() {
        TypesParser parser = getTypesParser("<?>");
        ASTTypeArguments node = parser.parseTypeArguments();
        node.print();
        checkSimple(node, ASTListNode.class, LESS_THAN);
    }

    /**
     * Tests type argument list of type argument.
     */
    @Test
    public void testTypeArgumentListOfTypeArgument() {
        TypesParser parser = getTypesParser("?");
        ASTListNode node = parser.parseTypeArgumentList();
        node.print();
        checkList(node, TYPE_ARGUMENTS, ASTTypeArgument.class, 1);
    }

    /**
     * Tests type arguments or diamond of type arguments.
     */
    @Test
    public void testTypeArgumentsOrDiamondOfTypeArguments() {
        TypesParser parser = getTypesParser("<T, U>");
        ASTTypeArgumentsOrDiamond node = parser.parseTypeArgumentsOrDiamond();
        node.print();
        checkSimple(node, ASTTypeArguments.class);
    }

    /**
     * Tests type arguments or diamond of diamond.
     */
    @Test
    public void testTypeArgumentsOrDiamondOfDiamond() {
        TypesParser parser = getTypesParser("<>");
        ASTTypeArgumentsOrDiamond node = parser.parseTypeArgumentsOrDiamond();
        node.print();
        checkEmpty(node, LESS_THAN);
    }

    /**
     * Tests argument list of nested argument lists (here, just multiple arguments).
     */
    @Test
    public void testTypeArgumentListNested() {
        TypesParser parser = getTypesParser("Employee, ?, ? <: Number");
        ASTListNode node = parser.parseTypeArgumentList();
        node.print();
        checkList(node, TYPE_ARGUMENTS, ASTTypeArgument.class, 3);
    }

    /**
     * Tests type argument of wildcard.
     */
    @Test
    public void testTypeArgumentOfWildcard() {
        TypesParser parser = getTypesParser("?");
        ASTTypeArgument node = parser.parseTypeArgument();
        node.print();
        checkSimple(node, ASTWildcard.class);
    }

    /**
     * Tests type argument of data type.
     */
    @Test
    public void testTypeArgumentOfDataType() {
        TypesParser parser = getTypesParser("Employee");
        ASTTypeArgument node = parser.parseTypeArgument();
        node.print();
        checkSimple(node, ASTDataType.class);
    }

    /**
     * Tests wildcard by itself.
     */
    @Test
    public void testWildcard() {
        TypesParser parser = getTypesParser("?");
        ASTWildcard node = parser.parseWildcard();
        node.print();
        checkEmpty(node, QUESTION_MARK);
    }

    /**
     * Tests wildcard with bounds.
     */
    @Test
    public void testWildcardBounds() {
        TypesParser parser = getTypesParser("? <: Employee");
        ASTWildcard node = parser.parseWildcard();
        node.print();
        checkSimple(node, ASTWildcardBounds.class, QUESTION_MARK);
    }

    /**
     * Tests wildcard bounds of subtype.
     */
    @Test
    public void testWildcardBoundsOfSubtype() {
        TypesParser parser = getTypesParser("<: Employee");
        ASTWildcardBounds node = parser.parseWildcardBounds();
        node.print();
        checkSimple(node, ASTDataType.class, SUBTYPE);
    }

    /**
     * Tests wildcard bounds of supertype.
     */
    @Test
    public void testWildcardBoundsOfSupertype() {
        TypesParser parser = getTypesParser(":> Employee");
        ASTWildcardBounds node = parser.parseWildcardBounds();
        node.print();
        checkSimple(node, ASTDataType.class, SUPERTYPE);
    }
    
    /**
     * Helper method to get a <code>TypesParser</code> directly from code.
     * @param code The code to test.
     * @return A <code>TypesParser</code> that will parse the given code.
     */
    private static TypesParser getTypesParser(String code) {
        return new Parser(new Scanner(code)).getTypesParser();
    }
}
