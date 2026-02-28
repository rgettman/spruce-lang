package org.spruce.compiler.bootstrap.test.parser;

//import org.spruce.compiler.bootstrap.ast.ASTKeywordNode;
//import org.spruce.compiler.bootstrap.ast.classes.ASTAnnotation;
import org.spruce.compiler.bootstrap.ast.names.ASTIdentifier;
import org.spruce.compiler.bootstrap.ast.types.*;
import org.spruce.compiler.bootstrap.common.BaseMessageProducer;
import org.spruce.compiler.bootstrap.parser.Parser;
import org.spruce.compiler.bootstrap.parser.TypesParser;
import org.spruce.compiler.bootstrap.scanner.Scanner;

import static org.spruce.compiler.bootstrap.ast.ASTListNode.Type.*;
//import static org.spruce.compiler.bootstrap.scanner.TokenType.*;
import static org.spruce.compiler.bootstrap.test.parser.ParserTestUtility.*;

import org.junit.jupiter.api.Test;
//import org.spruce.compiler.bootstrap.test.util.TestUtility;

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
        TypesParser parser = getTypesParser("Serializable, Comparable, RandomAccess");
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
     * Tests data type (no array) of simple type.
     */
    @Test
    public void testDataTypeNoArrayOfSimpleType() {
        TypesParser parser = getTypesParser("List");
        ASTDataTypeNoArray node = parser.parseDataTypeNoArray();
        ensureNoErrors(node, parser);
        checkList(node, SIMPLE_TYPES, ASTSimpleType.class, 1);
    }

    /**
     * Tests data type (no array) of "." and simple types.
     */
    @Test
    public void testDataTypeNoArray() {
        TypesParser parser = getTypesParser("A.B");
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
        //assertFalse(node.getTypeArgs().isPresent());
    }

    /**
     * Helper method to get a <code>TypesParser</code> directly from code.
     * @param code The code to test.
     * @return A <code>TypesParser</code> that will parse the given code.
     */
    public static TypesParser getTypesParser(String code) {
        return new Parser(new Scanner(code), new BaseMessageProducer()).getTypesParser();
    }
}
