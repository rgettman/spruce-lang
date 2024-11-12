package org.spruce.compiler.test;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.types.*;
import org.spruce.compiler.exception.CompileException;
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
        ASTDataTypeNoArrayList node = parser.parseDataTypeNoArrayList();
        System.out.println(node);
        checkList(node, DATA_TYPES_NO_ARRAY, ASTDataTypeNoArray.class, 1);
    }

    /**
     * Tests data type no array list.
     */
    @Test
    public void testDataTypeNoArrayList() {
        TypesParser parser = getTypesParser("Serializable, Comparable<T>");
        ASTDataTypeNoArrayList node = parser.parseDataTypeNoArrayList();
        System.out.println(node);
        checkList(node, DATA_TYPES_NO_ARRAY, ASTDataTypeNoArray.class, 2);
    }
    
    /**
     * Tests nested data type no array lists.
     */
    @Test
    public void testDataTypeNoArrayListNested() {
        TypesParser parser = getTypesParser("Serializable, Comparable<T>, RandomAccess");
        ASTDataTypeNoArrayList node = parser.parseDataTypeNoArrayList();
        System.out.println(node);
        checkList(node, DATA_TYPES_NO_ARRAY, ASTDataTypeNoArray.class, 3);
    }
    
    /**
     * Tests data type of data type (no array).
     */
    @Test
    public void testDataTypeOfDataTypeNoArray() {
        TypesParser parser = getTypesParser("spruce.lang.String");
        ASTDataType node = parser.parseDataType();
        System.out.println(node);
        assertInstanceOf(ASTDataTypeNoArray.class, node);
    }

    /**
     * Tests data type of array type.
     */
    @Test
    public void testDataTypeOfArrayType() {
        TypesParser parser = getTypesParser("spruce.lang.String[]");
        ASTDataType node = parser.parseDataType();
        System.out.println(node);
        assertInstanceOf(ASTArrayType.class, node);
    }

    /**
     * Tests array type.
     */
    @Test
    public void testArrayType() {
        TypesParser parser = getTypesParser("spruce.lang.String[][]");
        ASTArrayType node = parser.parseArrayType();
        System.out.println(node);
        assertNotNull(node.getDataTypeNoArray());
        assertNotNull(node.getDims());
    }

    /**
     * Tests dims.
     */
    @Test
    public void testDims() {
        TypesParser parser = getTypesParser("[][][]");
        ASTDims node = parser.parseDims();
        System.out.println(node);
        checkList(node, DIMS, ASTKeywordNode.class, 3);
    }

    /**
     * Tests dims of separate open bracket and close bracket.
     */
    @Test
    public void testDimsSeparateTokens() {
        TypesParser parser = getTypesParser("[ ][ ][ ]");
        ASTDims node = parser.parseDims();
        System.out.println(node);
        checkList(node, DIMS, ASTKeywordNode.class, 3);
    }

    /**
     * Tests bad dims of expression before close bracket.
     */
    @Test
    public void testDimsBad() {
        TypesParser parser = getTypesParser("[0]");
        assertThrows(CompileException.class, parser::parseDims, "Expected '[]'.");
    }

    /**
     * Tests data type (no array) of simple type.
     */
    @Test
    public void testDataTypeNoArrayOfSimpleType() {
        TypesParser parser = getTypesParser("List<?>");
        ASTDataTypeNoArray node = parser.parseDataTypeNoArray();
        System.out.println(node);
        checkList(node, SIMPLE_TYPES, ASTSimpleType.class, 1);
    }

    /**
     * Tests data type (no array) of "." and simple types.
     */
    @Test
    public void testDataTypeNoArray() {
        TypesParser parser = getTypesParser("A<?>.B<?>");
        ASTDataTypeNoArray node = parser.parseDataTypeNoArray();
        System.out.println(node);
        checkList(node, SIMPLE_TYPES, ASTSimpleType.class, 2);
    }

    /**
     * Tests nested data type (no array) expressions.
     */
    @Test
    public void testDataTypeNoArrayNested() {
        TypesParser parser = getTypesParser("spruce.collections.List<?>");
        ASTDataTypeNoArray node = parser.parseDataTypeNoArray();
        System.out.println(node);
        checkList(node, SIMPLE_TYPES, ASTSimpleType.class, 3);
    }

    /**
     * Tests simple type of identifier.
     */
    @Test
    public void testSimpleTypeOfIdentifier() {
        TypesParser parser = getTypesParser("Simple");
        ASTSimpleType node = parser.parseSimpleType();
        System.out.println(node);
        ASTIdentifier id = node.getName();
        assertEquals("Simple", id.getValue());
        assertFalse(node.getTypeArgs().isPresent());
    }

    /**
     * Tests simple type of identifier and type arguments.
     */
    @Test
    public void testSimpleTypeOfIdentifierTypeArguments() {
        TypesParser parser = getTypesParser("Map<?, ?>");
        ASTSimpleType node = parser.parseSimpleType();
        System.out.println(node);
        //checkNary(node, null, ASTIdentifier.class, ASTTypeArguments.class);
        ASTIdentifier id = node.getName();
        assertEquals("Map", id.getValue());
        assertTrue(node.getTypeArgs().isPresent());
        checkList(node.getTypeArgs().get(), TYPE_ARGUMENTS, ASTTypeArgument.class, 2);
    }

    /**
     * Tests type parameters of type parameter list.
     */
    @Test
    public void testTypeParametersOfTypeParameterList() {
        TypesParser parser = getTypesParser("<K, V>");
        ASTTypeParameterList node = parser.parseTypeParameters();
        System.out.println(node);
        checkList(node, TYPE_PARAMETERS, ASTTypeParameter.class, 2);
    }

    /**
     * Tests type parameter list of type parameter.
     */
    @Test
    public void testTypeParameterListOfTypeParameter() {
        TypesParser parser = getTypesParser("E");
        ASTTypeParameterList node = parser.parseTypeParameterList();
        System.out.println(node);
        checkList(node, TYPE_PARAMETERS, ASTTypeParameter.class, 1);
    }

    /**
     * Tests parameter list of nested parameter lists (here, just multiple parameters).
     */
    @Test
    public void testTypeParameterListNested() {
        TypesParser parser = getTypesParser("K, V, T <: Map<K, V>");
        ASTTypeParameterList node = parser.parseTypeParameterList();
        System.out.println(node);
        checkList(node, TYPE_PARAMETERS, ASTTypeParameter.class, 3);
    }

    /**
     * Tests simple type parameter.
     */
    @Test
    public void testTypeParameterSimple() {
        TypesParser parser = getTypesParser("T");
        ASTTypeParameter node = parser.parseTypeParameter();
        System.out.println(node);
        assertFalse(node.getTypeBound().isPresent());
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
        System.out.println(node);
        ASTIdentifier id = node.getName();
        assertEquals("N", id.getValue());
        assertTrue(node.getTypeBound().isPresent());
    }

    /**
     * Tests type bound of intersection type.
     */
    @Test
    public void testTypeBoundOfIntersectionType() {
        TypesParser parser = getTypesParser("<: Student & Serializable");
        ASTIntersectionType node = parser.parseTypeBound();
        System.out.println(node);
        checkList(node, DATA_TYPES, ASTDataType.class, 2);
    }

    /**
     * Tests intersection type of data type.
     */
    @Test
    public void testIntersectionTypeOfDataType() {
        TypesParser parser = getTypesParser("Student");
        ASTIntersectionType node = parser.parseIntersectionType();
        System.out.println(node);
        checkList(node, DATA_TYPES, ASTDataType.class, 1);
    }

    /**
     * Tests intersection type.
     */
    @Test
    public void testIntersectionType() {
        TypesParser parser = getTypesParser("Student & Person");
        ASTIntersectionType node = parser.parseIntersectionType();
        System.out.println(node);
        checkList(node, DATA_TYPES, ASTDataType.class, 2);
    }

    /**
     * Tests many intersection types.
     */
    @Test
    public void testIntersectionTypeMany() {
        TypesParser parser = getTypesParser("Student & Person & Learner");
        ASTIntersectionType node = parser.parseIntersectionType();
        System.out.println(node);
        checkList(node, DATA_TYPES, ASTDataType.class, 3);
    }

    /**
     * Tests type arguments of type argument list.
     */
    @Test
    public void testTypeArgumentsOfTypeArgumentList() {
        TypesParser parser = getTypesParser("<?>");
        ASTTypeArgumentList node = parser.parseTypeArguments();
        System.out.println(node);
        checkList(node, TYPE_ARGUMENTS, ASTTypeArgument.class, 1);
    }

    /**
     * Tests type argument list of type argument.
     */
    @Test
    public void testTypeArgumentListOfTypeArgument() {
        TypesParser parser = getTypesParser("?");
        ASTTypeArgumentList node = parser.parseTypeArgumentList();
        System.out.println(node);
        checkList(node, TYPE_ARGUMENTS, ASTTypeArgument.class, 1);
    }

    /**
     * Tests type arguments or diamond of type arguments.
     */
    @Test
    public void testTypeArgumentsOrDiamondOfTypeArguments() {
        TypesParser parser = getTypesParser("<T, U>");
        ASTTypeArgumentsOrDiamond node = parser.parseTypeArgumentsOrDiamond();
        System.out.println(node);
        assertTrue(node.getTypeArgs().isPresent());
    }

    /**
     * Tests type arguments or diamond of diamond.
     */
    @Test
    public void testTypeArgumentsOrDiamondOfDiamond() {
        TypesParser parser = getTypesParser("<>");
        ASTTypeArgumentsOrDiamond node = parser.parseTypeArgumentsOrDiamond();
        System.out.println(node);
        assertFalse(node.getTypeArgs().isPresent());
    }

    /**
     * Tests argument list of nested argument lists (here, just multiple arguments).
     */
    @Test
    public void testTypeArgumentListNested() {
        TypesParser parser = getTypesParser("Employee, ?, ? <: Number");
        ASTTypeArgumentList node = parser.parseTypeArgumentList();
        System.out.println(node);
        checkList(node, TYPE_ARGUMENTS, ASTTypeArgument.class, 3);
    }

    /**
     * Tests type argument of wildcard.
     */
    @Test
    public void testTypeArgumentOfWildcard() {
        TypesParser parser = getTypesParser("?");
        ASTTypeArgument node = parser.parseTypeArgument();
        System.out.println(node);
        assertInstanceOf(ASTWildcard.class, node);
    }

    /**
     * Tests type argument of data type.
     */
    @Test
    public void testTypeArgumentOfDataType() {
        TypesParser parser = getTypesParser("Employee");
        ASTTypeArgument node = parser.parseTypeArgument();
        System.out.println(node);
        assertInstanceOf(ASTDataType.class, node);
    }

    /**
     * Tests bad type argument.
     */
    @Test
    public void testTypeArgumentBad() {
        TypesParser parser = getTypesParser("20");
        assertThrows(CompileException.class, parser::parseTypeArgument, "Expected wildcard or data type.");
    }

    /**
     * Tests wildcard by itself.
     */
    @Test
    public void testWildcard() {
        TypesParser parser = getTypesParser("?");
        ASTWildcard node = parser.parseWildcard();
        System.out.println(node);
        assertEquals(QUESTION_MARK, node.getWildcard().getKeyword());
        assertFalse(node.getBounds().isPresent());
    }

    /**
     * Tests wildcard with bounds.
     */
    @Test
    public void testWildcardBounds() {
        TypesParser parser = getTypesParser("? <: Employee");
        ASTWildcard node = parser.parseWildcard();
        System.out.println(node);
        assertEquals(QUESTION_MARK, node.getWildcard().getKeyword());
        assertTrue(node.getBounds().isPresent());
    }

    /**
     * Tests wildcard bounds of subtype.
     */
    @Test
    public void testWildcardBoundsOfSubtype() {
        TypesParser parser = getTypesParser("<: Employee");
        ASTWildcardBounds node = parser.parseWildcardBounds();
        System.out.println(node);
        assertEquals(SUBTYPE, node.getBoundKeyword().getKeyword());
        assertNotNull(node.getDataType());
    }

    /**
     * Tests wildcard bounds of supertype.
     */
    @Test
    public void testWildcardBoundsOfSupertype() {
        TypesParser parser = getTypesParser(":> Employee");
        ASTWildcardBounds node = parser.parseWildcardBounds();
        System.out.println(node);
        assertEquals(SUPERTYPE, node.getBoundKeyword().getKeyword());
        assertNotNull(node.getDataType());
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
