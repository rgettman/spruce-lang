package org.spruce.compiler.test;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.classes.ASTAnnotation;
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
        ASTDataTypeNoArrayList node = parser.parseDataTypeNoArrayList();
        ensureNoErrors(node, parser);
        checkList(node, DATA_TYPES_NO_ARRAY, ASTDataTypeNoArray.class, 1);
    }

    /**
     * Tests data type no array list.
     */
    @Test
    public void testDataTypeNoArrayList() {
        TypesParser parser = getTypesParser("Serializable, Comparable<T>");
        ASTDataTypeNoArrayList node = parser.parseDataTypeNoArrayList();
        ensureNoErrors(node, parser);
        checkList(node, DATA_TYPES_NO_ARRAY, ASTDataTypeNoArray.class, 2);
    }
    
    /**
     * Tests nested data type no array lists.
     */
    @Test
    public void testDataTypeNoArrayListNested() {
        TypesParser parser = getTypesParser("Serializable, Comparable<T>, RandomAccess");
        ASTDataTypeNoArrayList node = parser.parseDataTypeNoArrayList();
        ensureNoErrors(node, parser);
        checkList(node, DATA_TYPES_NO_ARRAY, ASTDataTypeNoArray.class, 3);
    }
    
    /**
     * Tests base data type of data type (no array).
     */
    @Test
    public void testBaseDataTypeOfDataTypeNoArray() {
        TypesParser parser = getTypesParser("spruce.lang.String");
        ASTBaseDataType node = parser.parseBaseDataType();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTDataTypeNoArray.class, node);
    }

    /**
     * Tests base data type of array type.
     */
    @Test
    public void testBaseDataTypeOfArrayType() {
        TypesParser parser = getTypesParser("spruce.lang.String[]");
        ASTBaseDataType node = parser.parseBaseDataType();
        ensureNoErrors(node, parser);
        ASTArrayType arrayType = ensureIsa(node, ASTArrayType.class);
        assertNotNull(arrayType.getDataTypeNoArray());
        assertNotNull(arrayType.getDims());
    }

    /**
     * Tests data type of base data type.
     */
    @Test
    public void testDataTypeOfBaseDataType() {
        TypesParser parser = getTypesParser("spruce.lang.String");
        ASTDataType node = parser.parseDataType();
        ensureNoErrors(node, parser);
        assertFalse(node.getSuffixOperator().isPresent());
        assertNotNull(node.getBaseDataType());
        assertTrue(node.canConvertToExpressionName());
        assertNotNull(node.convertToExpressionName());
    }

    /**
     * Tests data type of base data type with exclamation point.
     */
    @Test
    public void testDataTypeOfExclamation() {
        TypesParser parser = getTypesParser("Connection!");
        ASTDataType node = parser.parseDataType();
        ensureNoErrors(node, parser);
        assertTrue(node.getSuffixOperator().isPresent());
        assertEquals(EXCLAMATION, node.getSuffixOperator().get().getKeyword());
        assertNotNull(node.getBaseDataType());
        assertFalse(node.canConvertToExpressionName());
    }

    /**
     * Tests data type of base data type with question mark.
     */
    @Test
    public void testDataTypeOfQuestionMark() {
        TypesParser parser = getTypesParser("stdDev?");
        ASTDataType node = parser.parseDataType();
        ensureNoErrors(node, parser);
        assertTrue(node.getSuffixOperator().isPresent());
        assertEquals(QUESTION_MARK, node.getSuffixOperator().get().getKeyword());
        assertNotNull(node.getBaseDataType());
        assertFalse(node.canConvertToExpressionName());
    }

    /**
     * Tests dims.
     */
    @Test
    public void testDims() {
        TypesParser parser = getTypesParser("[][][]");
        ASTDims node = parser.parseDims();
        ensureNoErrors(node, parser);
        checkList(node, DIMS, ASTKeywordNode.class, 3);
    }

    /**
     * Tests dims of separate open bracket and close bracket.
     */
    @Test
    public void testDimsSeparateTokens() {
        TypesParser parser = getTypesParser("[ ][ ][ ]");
        ASTDims node = parser.parseDims();
        ensureNoErrors(node, parser);
        checkList(node, DIMS, ASTKeywordNode.class, 3);
    }

    /**
     * Tests bad dims of expression before close bracket.
     */
    @Test
    public void testDimsBad() {
        TypesParser parser = getTypesParser("[0]");
        ASTDims node = parser.parseDims();
        expectError(node, parser);
    }

    /**
     * Tests data type (no array) of simple type.
     */
    @Test
    public void testDataTypeNoArrayOfSimpleType() {
        TypesParser parser = getTypesParser("List<_>");
        ASTDataTypeNoArray node = parser.parseDataTypeNoArray();
        ensureNoErrors(node, parser);
        checkList(node, SIMPLE_TYPES, ASTSimpleType.class, 1);
    }

    /**
     * Tests data type (no array) of "." and simple types.
     */
    @Test
    public void testDataTypeNoArray() {
        TypesParser parser = getTypesParser("A<_>.B<_>");
        ASTDataTypeNoArray node = parser.parseDataTypeNoArray();
        ensureNoErrors(node, parser);
        checkList(node, SIMPLE_TYPES, ASTSimpleType.class, 2);
    }

    /**
     * Tests nested data type (no array) expressions.
     */
    @Test
    public void testDataTypeNoArrayNested() {
        TypesParser parser = getTypesParser("spruce.collections.List<_>");
        ASTDataTypeNoArray node = parser.parseDataTypeNoArray();
        ensureNoErrors(node, parser);
        checkList(node, SIMPLE_TYPES, ASTSimpleType.class, 3);
    }

    /**
     * Tests simple type of identifier.
     */
    @Test
    public void testSimpleTypeOfIdentifier() {
        TypesParser parser = getTypesParser("Simple");
        ASTSimpleType node = parser.parseSimpleType();
        ensureNoErrors(node, parser);
        ASTIdentifier id = node.getName();
        assertEquals("Simple", id.getValue());
        assertFalse(node.getTypeArgs().isPresent());
    }

    /**
     * Tests simple type of identifier and type arguments.
     */
    @Test
    public void testSimpleTypeOfIdentifierTypeArguments() {
        TypesParser parser = getTypesParser("Map<_, _>");
        ASTSimpleType node = parser.parseSimpleType();
        ensureNoErrors(node, parser);
        ASTIdentifier id = node.getName();
        assertEquals("Map", id.getValue());
        assertTrue(node.getTypeArgs().isPresent());
        checkList(node.getTypeArgs().get(), TYPE_ARGUMENTS, ASTTypeArgument.class, 2);
    }

    /**
     * Test simple type of nested type arguments.
     */
    @Test
    public void testSimpleTypeOfNestedTypeArguments() {
        TypesParser parser = getTypesParser("Foo<Map<Integer, String>>");
        ASTSimpleType node = parser.parseSimpleType();
        ensureNoErrors(node, parser);
        ASTIdentifier id = node.getName();
        assertEquals("Foo", id.getValue());

        assertTrue(node.getTypeArgs().isPresent());
        ASTTypeArgumentList outer = node.getTypeArgs().get();
        checkList(outer, TYPE_ARGUMENTS, ASTTypeArgument.class, 1);

        ASTDataType first = ensureIsa(outer.get(0), ASTTypeArgumentBounds.class).getDataType();
        ASTDataTypeNoArray dtna = ensureIsa(first.getBaseDataType(), ASTDataTypeNoArray.class);
        checkList(dtna, SIMPLE_TYPES, ASTSimpleType.class, 1);

        ASTSimpleType simpleType = dtna.get(0);
        ASTIdentifier simpleId = simpleType.getName();
        assertEquals("Map", simpleId.getValue());

        assertTrue(simpleType.getTypeArgs().isPresent());
        ASTTypeArgumentList inner = simpleType.getTypeArgs().get();
        checkList(inner, TYPE_ARGUMENTS, ASTTypeArgument.class, 2);
    }

    /**
     * Tests type parameters of type parameter list.
     */
    @Test
    public void testTypeParametersOfTypeParameterList() {
        TypesParser parser = getTypesParser("<K, V>");
        ASTTypeParameterList node = parser.parseTypeParameters();
        ensureNoErrors(node, parser);
        checkList(node, TYPE_PARAMETERS, ASTTypeParameter.class, 2);
    }

    /**
     * Tests type parameter list of type parameter.
     */
    @Test
    public void testTypeParameterListOfTypeParameter() {
        TypesParser parser = getTypesParser("E");
        ASTTypeParameterList node = parser.parseTypeParameterList();
        ensureNoErrors(node, parser);
        checkList(node, TYPE_PARAMETERS, ASTTypeParameter.class, 1);
    }

    /**
     * Tests parameter list of nested parameter lists (here, just multiple parameters).
     */
    @Test
    public void testTypeParameterListNested() {
        TypesParser parser = getTypesParser("K, V, T : Map<K, V>");
        ASTTypeParameterList node = parser.parseTypeParameterList();
        ensureNoErrors(node, parser);
        checkList(node, TYPE_PARAMETERS, ASTTypeParameter.class, 3);
    }

    /**
     * Tests parameter list of type parameters with annotations.
     */
    @Test
    public void testTypeParameterListTypeParametersAnnotations() {
        TypesParser parser = getTypesParser("@Test1 T, @Test2 U, @Test3 V");
        ASTTypeParameterList node = parser.parseTypeParameterList();
        ensureNoErrors(node, parser);
        checkList(node, TYPE_PARAMETERS, ASTTypeParameter.class, 3);
    }

    /**
     * Tests simple type parameter.
     */
    @Test
    public void testTypeParameterSimple() {
        TypesParser parser = getTypesParser("T");
        ASTTypeParameter node = parser.parseTypeParameter();
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 0);
        assertFalse(node.getTypeBound().isPresent());
        ASTIdentifier id = node.getName();
        assertEquals("T", id.getValue());
    }

    /**
     * Tests type parameter of bounds (intersection type).
     */
    @Test
    public void testTypeParameterOfBounds() {
        TypesParser parser = getTypesParser("N : Number");
        ASTTypeParameter node = parser.parseTypeParameter();
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 0);
        ASTIdentifier id = node.getName();
        assertEquals("N", id.getValue());
        assertTrue(node.getTypeBound().isPresent());
    }

    /**
     * Tests type parameter of annotation.
     */
    @Test
    public void testTypeParameterOfAnnotation() {
        TypesParser parser = getTypesParser("@Test N : Number");
        ASTTypeParameter node = parser.parseTypeParameter();
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 1);
        ASTIdentifier id = node.getName();
        assertEquals("N", id.getValue());
        assertTrue(node.getTypeBound().isPresent());
    }

    /**
     * Tests type bound of intersection type.
     */
    @Test
    public void testTypeBoundOfIntersectionType() {
        TypesParser parser = getTypesParser(": Student & Serializable");
        ASTIntersectionType node = parser.parseTypeBound();
        ensureNoErrors(node, parser);
        checkList(node, DATA_TYPES, ASTDataType.class, 2);
    }

    /**
     * Tests intersection type of data type.
     */
    @Test
    public void testIntersectionTypeOfDataType() {
        TypesParser parser = getTypesParser("Student");
        ASTIntersectionType node = parser.parseIntersectionType();
        ensureNoErrors(node, parser);
        checkList(node, DATA_TYPES, ASTDataType.class, 1);
    }

    /**
     * Tests intersection type.
     */
    @Test
    public void testIntersectionType() {
        TypesParser parser = getTypesParser("Student & Person");
        ASTIntersectionType node = parser.parseIntersectionType();
        ensureNoErrors(node, parser);
        checkList(node, DATA_TYPES, ASTDataType.class, 2);
    }

    /**
     * Tests many intersection types.
     */
    @Test
    public void testIntersectionTypeMany() {
        TypesParser parser = getTypesParser("Student & Person & Learner");
        ASTIntersectionType node = parser.parseIntersectionType();
        ensureNoErrors(node, parser);
        checkList(node, DATA_TYPES, ASTDataType.class, 3);
    }

    /**
     * Tests type arguments of type argument list.
     */
    @Test
    public void testTypeArgumentsOfTypeArgumentList() {
        TypesParser parser = getTypesParser("<T>");
        ASTTypeArgumentList node = parser.parseTypeArguments();
        ensureNoErrors(node, parser);
        checkList(node, TYPE_ARGUMENTS, ASTTypeArgument.class, 1);
    }

    /**
     * Tests type argument list of type argument.
     */
    @Test
    public void testTypeArgumentListOfTypeArgument() {
        TypesParser parser = getTypesParser("T");
        ASTTypeArgumentList node = parser.parseTypeArgumentList();
        ensureNoErrors(node, parser);
        checkList(node, TYPE_ARGUMENTS, ASTTypeArgument.class, 1);
    }

    /**
     * Tests type arguments or diamond of type arguments.
     */
    @Test
    public void testTypeArgumentsOrDiamondOfTypeArguments() {
        TypesParser parser = getTypesParser("<T, U>");
        ASTTypeArgumentsOrDiamond node = parser.parseTypeArgumentsOrDiamond();
        ensureNoErrors(node, parser);
        assertTrue(node.getTypeArgs().isPresent());
    }

    /**
     * Tests type arguments or diamond of diamond.
     */
    @Test
    public void testTypeArgumentsOrDiamondOfDiamond() {
        TypesParser parser = getTypesParser("<>");
        ASTTypeArgumentsOrDiamond node = parser.parseTypeArgumentsOrDiamond();
        ensureNoErrors(node, parser);
        assertFalse(node.getTypeArgs().isPresent());
    }

    /**
     * Tests type argument list of multiple type arguments.
     */
    @Test
    public void testTypeArgumentListNested() {
        TypesParser parser = getTypesParser("Employee, _, out Number");
        ASTTypeArgumentList node = parser.parseTypeArgumentList();
        ensureNoErrors(node, parser);
        checkList(node, TYPE_ARGUMENTS, ASTTypeArgument.class, 3);
    }

    /**
     * Tests type argument of wildcard.
     */
    @Test
    public void testTypeArgumentOfWildcard() {
        TypesParser parser = getTypesParser("_");
        ASTTypeArgument node = parser.parseTypeArgument();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTWildcard.class, node);
    }

    /**
     * Tests type argument of type argument bounds.
     */
    @Test
    public void testTypeArgumentOfTypeArgumentBounds() {
        TypesParser parser = getTypesParser("Employee");
        ASTTypeArgument node = parser.parseTypeArgument();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTTypeArgumentBounds.class, node);
    }

    /**
     * Tests wildcard by itself.
     */
    @Test
    public void testWildcard() {
        TypesParser parser = getTypesParser("_");
        ASTWildcard node = parser.parseWildcard();
        ensureNoErrors(node, parser);
        assertEquals(UNDERSCORE, node.getWildcard().getKeyword());
    }

    /**
     * Tests type argument bounds of subtype.
     */
    @Test
    public void testTypeArgumentBoundsOfSubtype() {
        TypesParser parser = getTypesParser("out Employee");
        ASTTypeArgumentBounds node = parser.parseTypeArgumentBounds();
        ensureNoErrors(node, parser);
        assertTrue(node.getGenericModifier().isPresent());
        assertEquals(OUT, node.getGenericModifier().get().getKeyword());
        assertNotNull(node.getDataType());
    }

    /**
     * Tests type argument bounds of supertype.
     */
    @Test
    public void testTypeArgumentBoundsOfSupertype() {
        TypesParser parser = getTypesParser("in Employee");
        ASTTypeArgumentBounds node = parser.parseTypeArgumentBounds();
        ensureNoErrors(node, parser);
        assertTrue(node.getGenericModifier().isPresent());
        assertEquals(IN, node.getGenericModifier().get().getKeyword());
        assertNotNull(node.getDataType());
    }

    /**
     * Tests type argument bounds of data type.
     */
    @Test
    public void testTypeArgumentBoundsOfDataType() {
        TypesParser parser = getTypesParser("Manager");
        ASTTypeArgumentBounds node = parser.parseTypeArgumentBounds();
        ensureNoErrors(node, parser);
        assertFalse(node.getGenericModifier().isPresent());
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
