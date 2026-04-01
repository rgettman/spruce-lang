package org.spruce.compiler.bootstrap.test.symbol;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.bootstrap.ast.types.*;
import org.spruce.compiler.bootstrap.common.BaseMessageProducer;
import org.spruce.compiler.bootstrap.parser.TypesParser;
import org.spruce.compiler.bootstrap.symbol.SymbolCreator;
import org.spruce.compiler.bootstrap.symbol.TypeLookup;
import org.spruce.compiler.bootstrap.symbol.TypesSymbolCreator;
import org.spruce.compiler.bootstrap.test.parser.ParserTypesTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.spruce.compiler.bootstrap.test.util.TestUtility.ensureIsa;

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
     * Tests data type no array.
     */
    @Test
    public void testDataTypeNoArray() {
        TypesParser parser = ParserTypesTest.getTypesParser("fully.qualified.SimpleType");
        ASTBaseDataType bdt = parser.parseBaseDataType();

        TypesSymbolCreator creator = getClassesSymbolCreator();
        String name = creator.getNameForDataTypeNoArray(ensureIsa(bdt, ASTDataTypeNoArray.class));
        assertEquals("fully.qualified.SimpleType", name);
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
     * Helper method to get a <code>TypesSymbolCreator</code>.
     * @return A <code>TypesSymbolCreator</code>.
     */
    public static TypesSymbolCreator getClassesSymbolCreator() {
        return new SymbolCreator(new BaseMessageProducer(), new TypeLookup()).getTypesSymbolCreator();
    }
}
