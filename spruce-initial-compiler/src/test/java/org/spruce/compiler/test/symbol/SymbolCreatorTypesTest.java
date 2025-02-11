package org.spruce.compiler.test.symbol;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.ast.types.*;
import org.spruce.compiler.common.BaseMessageProducer;
import org.spruce.compiler.parser.TypesParser;
import org.spruce.compiler.symbol.SymbolCreator;
import org.spruce.compiler.symbol.TypesSymbolCreator;
import org.spruce.compiler.test.parser.ParserTypesTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.spruce.compiler.test.util.TestUtility.ensureIsa;

/**
 * All tests for the types symbol creator.
 */
public class SymbolCreatorTypesTest {

    /**
     * Tests data type.
     */
    @Test
    public void testDataType() {
        TypesParser parser = ParserTypesTest.getTypesParser("WidgetProcessor");
        ASTDataType dt = parser.parseDataType();

        TypesSymbolCreator creator = getClassesSymbolCreator();
        String name = creator.getNameForDataType(dt);
        assertEquals("WidgetProcessor", name);
    }

    /**
     * Tests data type with exclamation suffix.
     */
    @Test
    public void testDataTypeExclamation() {
        TypesParser parser = ParserTypesTest.getTypesParser("WidgetProcessor !");
        ASTDataType dt = parser.parseDataType();

        TypesSymbolCreator creator = getClassesSymbolCreator();
        String name = creator.getNameForDataType(dt);
        assertEquals("WidgetProcessor!", name);
    }

    /**
     * Tests data type with question mark suffix.
     */
    @Test
    public void testDataTypeQuestionMark() {
        TypesParser parser = ParserTypesTest.getTypesParser("WidgetProcessor ?");
        ASTDataType dt = parser.parseDataType();

        TypesSymbolCreator creator = getClassesSymbolCreator();
        String name = creator.getNameForDataType(dt);
        assertEquals("WidgetProcessor?", name);
    }

    /**
     * Tests array type.
     */
    @Test
    public void testArrayType() {
        TypesParser parser = ParserTypesTest.getTypesParser("Double [] [] []");
        ASTBaseDataType bdt = parser.parseBaseDataType();

        TypesSymbolCreator creator = getClassesSymbolCreator();
        String name = creator.getNameForArrayType(ensureIsa(bdt, ASTArrayType.class));
        assertEquals("Double[][][]", name);
    }

    /**
     * Tests data type no array.
     */
    @Test
    public void testDataTypeNoArray() {
        TypesParser parser = ParserTypesTest.getTypesParser("fully.qualified.SimpleType<E>");
        ASTBaseDataType bdt = parser.parseBaseDataType();

        TypesSymbolCreator creator = getClassesSymbolCreator();
        String name = creator.getNameForDataTypeNoArray(ensureIsa(bdt, ASTDataTypeNoArray.class));
        assertEquals("fully.qualified.SimpleType<E>", name);
    }

    /**
     * Tests simple type.
     */
    @Test
    public void testSimpleType() {
        TypesParser parser = ParserTypesTest.getTypesParser("SimpleType");
        ASTSimpleType simpleType = parser.parseSimpleType();

        TypesSymbolCreator creator = getClassesSymbolCreator();
        String name = creator.getNameForSimpleType(simpleType);
        assertEquals("SimpleType", name);
    }

    /**
     * Tests simple type with type arguments.
     */
    @Test
    public void testSimpleTypeTypeArgs() {
        TypesParser parser = ParserTypesTest.getTypesParser("List<T>");
        ASTSimpleType simpleType = parser.parseSimpleType();

        TypesSymbolCreator creator = getClassesSymbolCreator();
        String name = creator.getNameForSimpleType(simpleType);
        assertEquals("List<T>", name);
    }

    /**
     * Tests type arguments.
     */
    @Test
    public void testTypeArguments() {
        TypesParser parser = ParserTypesTest.getTypesParser("<in T, out R>");
        ASTTypeArgumentList typeArgs = parser.parseTypeArguments();

        TypesSymbolCreator creator = getClassesSymbolCreator();
        String name = creator.getNameForTypeArgumentList(typeArgs);
        assertEquals("<in T, out R>", name);
    }

    /**
     * Tests type argument bounds, no "in" or "out".
     */
    @Test
    public void testTypeArgumentBoundsNoInOut() {
        TypesParser parser = ParserTypesTest.getTypesParser("T");
        ASTTypeArgumentBounds tab = parser.parseTypeArgumentBounds();

        TypesSymbolCreator creator = getClassesSymbolCreator();
        String name = creator.getNameForTypeArgument(tab);
        assertEquals("T", name);
    }

    /**
     * Tests type argument bounds with "in".
     */
    @Test
    public void testTypeArgumentBoundsIn() {
        TypesParser parser = ParserTypesTest.getTypesParser("in T");
        ASTTypeArgumentBounds tab = parser.parseTypeArgumentBounds();

        TypesSymbolCreator creator = getClassesSymbolCreator();
        String name = creator.getNameForTypeArgument(tab);
        assertEquals("in T", name);
    }

    /**
     * Tests type argument bounds with "out".
     */
    @Test
    public void testTypeArgumentBoundsOut() {
        TypesParser parser = ParserTypesTest.getTypesParser("out T");
        ASTTypeArgumentBounds tab = parser.parseTypeArgumentBounds();

        TypesSymbolCreator creator = getClassesSymbolCreator();
        String name = creator.getNameForTypeArgument(tab);
        assertEquals("out T", name);
    }

    /**
     * Tests wildcard.
     */
    @Test
    public void testWildcard() {
        TypesParser parser = ParserTypesTest.getTypesParser("_");
        ASTWildcard wildcard = parser.parseWildcard();

        TypesSymbolCreator creator = getClassesSymbolCreator();
        String name = creator.getNameForTypeArgument(wildcard);
        assertEquals("_", name);
    }

    /**
     * Helper method to get a <code>TypesSymbolCreator</code>.
     * @return A <code>TypesSymbolCreator</code>.
     */
    public static TypesSymbolCreator getClassesSymbolCreator() {
        return new SymbolCreator(new BaseMessageProducer()).getTypesSymbolCreator();
    }
}
