package org.spruce.compiler.test.parser;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.ast.classes.*;
import org.spruce.compiler.ast.expressions.*;
import org.spruce.compiler.ast.names.*;
import org.spruce.compiler.ast.statements.*;
import org.spruce.compiler.ast.types.*;
import org.spruce.compiler.common.BaseMessageProducer;
import org.spruce.compiler.parser.ClassesParser;
import org.spruce.compiler.parser.Parser;
import org.spruce.compiler.common.Location;
import org.spruce.compiler.scanner.Scanner;
import static org.spruce.compiler.scanner.TokenType.*;
import static org.spruce.compiler.test.parser.ParserTestUtility.*;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.test.util.TestUtility;

import static org.spruce.compiler.ast.ASTListNode.Type.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * All tests for the parser related to classes, methods, etc.
 */
public class ParserClassesTest {
    /**
     * Tests simple annotation declaration.
     */
    @Test
    public void testAnnotationDeclarationSimple() {
        ClassesParser parser = getClassesParser("annotation Dummy {}");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTAnnotationDeclaration node = parser.parseAnnotationDeclaration(loc, annList, null, genModList);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 0);
        assertFalse(node.getAccessMod().isPresent());
        checkList(node.getInterfaceModList(), INTERFACE_MODIFIERS, ASTKeywordNode.class, 0);
        ASTIdentifier name = node.getName();
        assertEquals("Dummy", name.getValue());
        checkList(node.getBody(), ANNOTATION_PARTS, ASTAnnotationPart.class, 0);
    }

    /**
     * Tests full annotation declaration.
     */
    @Test
    public void testAnnotationDeclarationFull() {
        ClassesParser parser = getClassesParser("""
            @Test public shared annotation AFullTest {
                String prop();
            }
            """);
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = accessMod.getLocation();
        ASTAnnotationDeclaration node = parser.parseAnnotationDeclaration(loc, annList, accessMod, genModList);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 1);
        assertTrue(node.getAccessMod().isPresent());
        ASTKeywordNode am = TestUtility.ensureIsa(node.getAccessMod().get(), ASTKeywordNode.class);
        assertEquals(PUBLIC, am.getKeyword());
        checkList(node.getInterfaceModList(), INTERFACE_MODIFIERS, ASTKeywordNode.class, 1);
        ASTIdentifier name = node.getName();
        assertEquals("AFullTest", name.getValue());
        checkList(node.getBody(), ANNOTATION_PARTS, ASTAnnotationPart.class, 1);
    }

    /**
     * Tests bad annotation declaration of bad annotation modifier.
     */
    @Test
    public void testAnnotationDeclarationBadAnnotationMod() {
        ClassesParser parser = getClassesParser("""
            public constant annotation ABadAnnotation {
                String prop();
            }
            """);
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = accessMod.getLocation();
        ASTAnnotationDeclaration node = parser.parseAnnotationDeclaration(loc, annList, accessMod, genModList);
        expectError(node, parser);
    }

    /**
     * Tests empty annotation body.
     */
    @Test
    public void testAnnotationBodyEmpty() {
        ClassesParser parser = getClassesParser("{}");
        ASTAnnotationPartList node = parser.parseAnnotationBody();
        ensureNoErrors(node, parser);
        checkList(node, ANNOTATION_PARTS, ASTAnnotationPart.class, 0);
    }

    /**
     * Tests annotation body.
     */
    @Test
    public void testAnnotationBody() {
        ClassesParser parser = getClassesParser("""
                {
                    constant Integer i = 1;
                    class Inner{}
                    Integer getI() default 1;}
                }
                """);
        ASTAnnotationPartList node = parser.parseAnnotationBody();
        ensureNoErrors(node, parser);
        checkList(node, ANNOTATION_PARTS, ASTAnnotationPart.class, 3);
    }

    /**
     * Tests annotation part list of annotation part.
     */
    @Test
    public void testAnnotationPartListOfAnnotationPart() {
        ClassesParser parser = getClassesParser("constant Integer i = 1;");
        ASTAnnotationPartList node = parser.parseAnnotationPartList();
        ensureNoErrors(node, parser);
        checkList(node, ANNOTATION_PARTS, ASTAnnotationPart.class, 1);
    }

    /**
     * Tests annotation part list of all possible annotation parts.
     */
    @Test
    public void testAnnotationPartListComprehensive() {
        ClassesParser parser = getClassesParser("""
                constant String foo = "Foo!";
                @Test String element() default "Who!";
                class Nested {}
                enum TrafficLight {RED, YELLOW, GREEN}
                interface Helper {}
                annotation InnerAnnotation {}
                record FooRecord(String bar) {}
                adt Foo {
                    Boo() {},
                    Goo() {}
                }
                """);
        ASTAnnotationPartList node = parser.parseAnnotationPartList();
        ensureNoErrors(node, parser);
        checkList(node, ANNOTATION_PARTS, ASTAnnotationPart.class, 8);
    }

    /**
     * Tests annotation part list.
     */
    @Test
    public void testAnnotationPartList() {
        ClassesParser parser = getClassesParser("""
            constant Integer i = 1;
            class Inner {}
            """);
        ASTAnnotationPartList node = parser.parseAnnotationPartList();
        ensureNoErrors(node, parser);
        checkList(node, ANNOTATION_PARTS, ASTAnnotationPart.class, 2);
    }

    /**
     * Tests annotation part lists of multiple annotation parts.
     */
    @Test
    public void testAnnotationPartListMultiple() {
        ClassesParser parser = getClassesParser("""
            constant Integer i = 1;
            class Inner {}
            Integer getI() default 1;
            """);
        ASTAnnotationPartList node = parser.parseAnnotationPartList();
        ensureNoErrors(node, parser);
        checkList(node, ANNOTATION_PARTS, ASTAnnotationPart.class, 3);
    }

    /**
     * Tests annotation part of annotation type element declaration.
     */
    @Test
    public void testAnnotationPartOfATED() {
        ClassesParser parser = getClassesParser("String element() default \"Test\";");
        ASTAnnotationPart node = parser.parseAnnotationPart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTAnnotationTypeElementDeclaration.class, node);
    }

    /**
     * Tests annotation part of constant declaration.
     */
    @Test
    public void testAnnotationPartOfConstantDeclaration() {
        ClassesParser parser = getClassesParser("constant String LANGUAGE = \"Spruce\";");
        ASTAnnotationPart node = parser.parseAnnotationPart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTConstantDeclaration.class, node);
    }

    /**
     * Tests annotation part of class declaration.
     */
    @Test
    public void testAnnotationPartOfClassDeclaration() {
        ClassesParser parser = getClassesParser("public shared class Nested {}");
        ASTAnnotationPart node = parser.parseAnnotationPart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTClassDeclaration.class, node);
    }

    /**
     * Tests annotation part of enum declaration.
     */
    @Test
    public void testAnnotationPartOfEnumDeclaration() {
        ClassesParser parser = getClassesParser("private enum Light {RED, YELLOW, GREEN}");
        ASTAnnotationPart node = parser.parseAnnotationPart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTEnumDeclaration.class, node);
    }

    /**
     * Tests annotation part of interface declaration.
     */
    @Test
    public void testAnnotationPartOfInterfaceDeclaration() {
        ClassesParser parser = getClassesParser("private interface TrafficLight { Light getStatus(); }");
        ASTAnnotationPart node = parser.parseAnnotationPart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTInterfaceDeclaration.class, node);
    }

    /**
     * Tests annotation part of annotation declaration.
     */
    @Test
    public void testAnnotationPartOfAnnotationDeclaration() {
        ClassesParser parser = getClassesParser("public annotation Test { String getStatus() default \"SUCCESS\"; }");
        ASTAnnotationPart node = parser.parseAnnotationPart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTAnnotationDeclaration.class, node);
    }

    /**
     * Tests annotation part of record declaration.
     */
    @Test
    public void testAnnotationPartOfRecordDeclaration() {
        ClassesParser parser = getClassesParser("internal record Redacted(String byWhom) { }");
        ASTAnnotationPart node = parser.parseAnnotationPart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTRecordDeclaration.class, node);
    }

    /**
     * Tests annotation part of adt declaration.
     */
    @Test
    public void testAnnotationPartOfAdtDeclaration() {
        ClassesParser parser = getClassesParser("""
                public adt Optional<T> {
                    None() {},
                    Some(T value) {
                        T getValue() {
                            return value();
                        }
                    }
                }
                """);
        ASTAnnotationPart node = parser.parseAnnotationPart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTAdtDeclaration.class, node);
    }

    /**
     * Tests bad annotation part of annotation type element declaration with type parameters.
     */
    @Test
    public void testAnnotationPartTypeParamsOnATED() {
        ClassesParser parser = getClassesParser("""
                <T> String value();
                """);
        ASTAnnotationPart node = parser.parseAnnotationPart();
        expectError(node, parser);
    }

    /**
     * Tests bad annotation part of annotation type element declaration with a general modifier.
     */
    @Test
    public void testAnnotationPartGenModOnATED() {
        ClassesParser parser = getClassesParser("""
                shared String value();
                """);
        ASTAnnotationPart node = parser.parseAnnotationPart();
        expectError(node, parser);
    }

    /**
     * Tests bad annotation part of annotation type element declaration with an access modifier.
     */
    @Test
    public void testAnnotationPartAccessModOnATED() {
        ClassesParser parser = getClassesParser("""
                private String value();
                """);
        ASTAnnotationPart node = parser.parseAnnotationPart();
        expectError(node, parser);
    }

    /**
     * Tests bad annotation part of constant declaration with a type parameter.
     */
    @Test
    public void testAnnotationPartTypeParamsOnConstantDeclaration() {
        ClassesParser parser = getClassesParser("""
                <T> constant Foo TEST = "test";
                """);
        ASTAnnotationPart node = parser.parseAnnotationPart();
        expectError(node, parser);
    }

    /**
     * Tests annotation type element declaration.
     */
    @Test
    public void testATED() {
        ClassesParser parser = getClassesParser("String element();");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTDataType dataType = parser.getTypesParser().parseDataType();
        ASTAnnotationTypeElementDeclaration node = parser.parseAnnotationTypeElementDeclaration(
                dataType.getLocation(), annList, dataType);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 0);
        assertNotNull(node.getDataType());
        assertEquals("element", node.getName().getValue());
        assertFalse(node.getDefaultValue().isPresent());
    }

    /**
     * Tests annotation type element declaration with annotation.
     */
    @Test
    public void testATEDAnnotation() {
        ClassesParser parser = getClassesParser("@Test String element();");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTDataType dataType = parser.getTypesParser().parseDataType();
        ASTAnnotationTypeElementDeclaration node = parser.parseAnnotationTypeElementDeclaration(
                dataType.getLocation(), annList, dataType);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 1);
        assertNotNull(node.getDataType());
        assertEquals("element", node.getName().getValue());
        assertFalse(node.getDefaultValue().isPresent());
    }

    /**
     * Tests annotation type element declaration with default value.
     */
    @Test
    public void testATEDDefaultValue() {
        ClassesParser parser = getClassesParser("String element() default \"DNE\";");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTDataType dataType = parser.getTypesParser().parseDataType();
        ASTAnnotationTypeElementDeclaration node = parser.parseAnnotationTypeElementDeclaration(
                dataType.getLocation(), annList, dataType);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 0);
        assertNotNull(node.getDataType());
        assertEquals("element", node.getName().getValue());
        assertTrue(node.getDefaultValue().isPresent());
    }

    /**
     * Tests bad annotation type element declaration of no open parenthesis.
     */
    @Test
    public void testATEDNoOpenParen() {
        ClassesParser parser = getClassesParser("String bad default \"DNE\";");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTDataType dataType = parser.getTypesParser().parseDataType();
        ASTAnnotationTypeElementDeclaration node = parser.parseAnnotationTypeElementDeclaration(dataType.getLocation(), annList, dataType);
        expectError(node, parser, 2);
    }

    /**
     * Tests bad annotation type element declaration of no close parenthesis.
     */
    @Test
    public void testATEDNoCloseParen() {
        ClassesParser parser = getClassesParser("String bad( default \"DNE\";");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTDataType dataType = parser.getTypesParser().parseDataType();
        ASTAnnotationTypeElementDeclaration node = parser.parseAnnotationTypeElementDeclaration(dataType.getLocation(), annList, dataType);
        expectError(node, parser);
    }

    /**
     * Tests bad annotation type element declaration of no semicolon.
     */
    @Test
    public void testATEDNoSemicolon() {
        ClassesParser parser = getClassesParser("String bad() default \"DNE\"}");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTDataType dataType = parser.getTypesParser().parseDataType();
        ASTAnnotationTypeElementDeclaration node = parser.parseAnnotationTypeElementDeclaration(dataType.getLocation(), annList, dataType);
        expectError(node, parser);
    }

    /**
     * Tests default value.
     */
    @Test
    public void testDefaultValue() {
        ClassesParser parser = getClassesParser("default {\"default\", \"value\"}");
        ASTElementValue node = parser.parseDefaultValue();
        ensureNoErrors(node, parser);

        ASTElementValueList elementValueArrayInit = TestUtility.ensureIsa(node, ASTElementValueList.class);
        checkList(elementValueArrayInit, ELEMENT_VALUES, ASTElementValue.class, 2);
    }

    /**
     * Tests annotation of marker annotation.
     */
    @Test
    public void testAnnotationOfMarkerAnnotation() {
        ClassesParser parser = getClassesParser("@Test");
        ASTAnnotation node = parser.parseAnnotation();
        ensureNoErrors(node, parser);

        ASTMarkerAnnotation ma = TestUtility.ensureIsa(node, ASTMarkerAnnotation.class);
        assertNotNull(ma.getTypeName());
    }

    /**
     * Tests annotation of single element annotation.
     */
    @Test
    public void testAnnotationOfSingleElementAnnotation() {
        ClassesParser parser = getClassesParser("@Test(\"Test\")");
        ASTAnnotation node = parser.parseAnnotation();
        ensureNoErrors(node, parser);

        ASTSingleElementAnnotation sea = TestUtility.ensureIsa(node, ASTSingleElementAnnotation.class);
        assertNotNull(sea.getTypeName());
        assertNotNull(sea.getElementValue());
    }

    /**
     * Tests bad annotation of bad single element annotation, no close parenthesis.
     */
    @Test
    public void testAnnotationOfSingleElementAnnotationNoCloseParen() {
        ClassesParser parser = getClassesParser("@Test(\"Test\"}");
        ASTAnnotation node = parser.parseAnnotation();
        expectError(node, parser);
    }

    /**
     * Tests annotation of normal annotation, empty.
     */
    @Test
    public void testAnnotationOfNormalAnnotationEmpty() {
        ClassesParser parser = getClassesParser("@Empty()");
        ASTAnnotation node = parser.parseAnnotation();
        ensureNoErrors(node, parser);

        ASTNormalAnnotation na = TestUtility.ensureIsa(node, ASTNormalAnnotation.class);
        assertNotNull(na.getTypeName());
        checkList(na.getElementValuePairList(), ELEMENT_VALUE_PAIRS, ASTElementValuePair.class, 0);
    }

    /**
     * Tests annotation of normal annotation of element pair value list.
     */
    @Test
    public void testAnnotationOfNormalAnnotationOfEVPL() {
        ClassesParser parser = getClassesParser("@Many(one = 1, two = \"two\", three = '3')");
        ASTAnnotation node = parser.parseAnnotation();
        ensureNoErrors(node, parser);

        ASTNormalAnnotation na = TestUtility.ensureIsa(node, ASTNormalAnnotation.class);
        assertNotNull(na.getTypeName());
        checkList(na.getElementValuePairList(), ELEMENT_VALUE_PAIRS, ASTElementValuePair.class, 3);
    }

    /**
     * Tests bad annotation of bad normal annotation, no close parenthesis.
     */
    @Test
    public void testAnnotationOfNormalAnnotationNoCloseParen() {
        ClassesParser parser = getClassesParser("@Test(test = \"Test\"}");
        ASTAnnotation node = parser.parseAnnotation();
        expectError(node, parser);
    }

    /**
     * Tests element value pair list of element value pair.
     */
    @Test
    public void testEVPListOfEVP() {
        ClassesParser parser = getClassesParser("test = \"Test\"");
        ASTElementValuePairList node = parser.parseElementValuePairList();
        ensureNoErrors(node, parser);
        checkList(node, ELEMENT_VALUE_PAIRS, ASTElementValuePair.class, 1);
    }

    /**
     * Tests element value pair list.
     */
    @Test
    public void testEVPList() {
        ClassesParser parser = getClassesParser("one = 1, two = \"two\", three = '3'");
        ASTElementValuePairList node = parser.parseElementValuePairList();
        ensureNoErrors(node, parser);
        checkList(node, ELEMENT_VALUE_PAIRS, ASTElementValuePair.class, 3);
    }

    /**
     * Tests element value pair of element value.
     */
    @Test
    public void testElementValuePairOfElementValue() {
        ClassesParser parser = getClassesParser("prop = \"Value Expression\"");
        ASTElementValuePair node = parser.parseElementValuePair();
        ensureNoErrors(node, parser);
        assertNotNull(node.getElementName());
        assertNotNull(node.getElementValue());
    }

    /**
     * Tests bad element value pair of bad assignment.
     */
    @Test
    public void testElementValuePairOfBadAssignment() {
        ClassesParser parser = getClassesParser("prop -> \"Value Expression\"");
        ASTElementValuePair node = parser.parseElementValuePair();
        expectError(node, parser);
    }

    /**
     * Tests empty element value array initializer.
     */
    @Test
    public void testEVAIEmpty() {
        ClassesParser parser = getClassesParser("{}");
        ASTElementValueList node = parser.parseElementValueArrayInitializer();
        ensureNoErrors(node, parser);
        checkList(node, ELEMENT_VALUES, ASTElementValue.class, 0);
    }

    /**
     * Tests element value array initializer of element value list.
     */
    @Test
    public void testEVAIOfEVList() {
        ClassesParser parser = getClassesParser("{1, \"Two\", '3'}");
        ASTElementValueList node = parser.parseElementValueArrayInitializer();
        ensureNoErrors(node, parser);
        checkList(node, ELEMENT_VALUES, ASTElementValue.class, 3);
    }

    /**
     * Tests element value list of element value.
     */
    @Test
    public void testEVListOfEV() {
        ClassesParser parser = getClassesParser("\"Test\"");
        ASTElementValueList node = parser.parseElementValueList();
        ensureNoErrors(node, parser);
        checkList(node, ELEMENT_VALUES, ASTElementValue.class, 1);
    }

    /**
     * Tests element value list.
     */
    @Test
    public void testEVList() {
        ClassesParser parser = getClassesParser("1, \"two\", '3', @Four");
        ASTElementValueList node = parser.parseElementValueList();
        ensureNoErrors(node, parser);
        checkList(node, ELEMENT_VALUES, ASTElementValue.class, 4);
    }

    /**
     * Tests element value of value expression.
     */
    @Test
    public void testElementValueOfValueExpression() {
        ClassesParser parser = getClassesParser("\"Value Expression\"");
        ASTElementValue node = parser.parseElementValue();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTPrimary.class, node);
    }

    /**
     * Tests element value of element value array initializer.
     */
    @Test
    public void testElementValueOfEVAI() {
        ClassesParser parser = getClassesParser("{\"Value Expression\"}");
        ASTElementValue node = parser.parseElementValue();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTListNode.class, node);
    }

    /**
     * Tests element value of annotation.
     */
    @Test
    public void testElementValueOfAnnotation() {
        ClassesParser parser = getClassesParser("@Foo");
        ASTElementValue node = parser.parseElementValue();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTAnnotation.class, node);
    }

    /**
     * Tests simple interface declaration.
     */
    @Test
    public void testInterfaceDeclarationSimple() {
        ClassesParser parser = getClassesParser("interface Dummy {}");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTInterfaceDeclaration node = parser.parseInterfaceDeclaration(loc, annList, null, genModList);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 0);
        assertFalse(node.getAccessMod().isPresent());
        checkList(node.getInterfaceModifierList(), INTERFACE_MODIFIERS, ASTKeywordNode.class, 0);
        assertEquals("Dummy", node.getName().getValue());
        assertFalse(node.getTypeParams().isPresent());
        assertFalse(node.getExtendsInterfaces().isPresent());
        assertFalse(node.getPermits().isPresent());
        checkList(node.getInterfaceParts(), INTERFACE_PARTS, ASTInterfacePart.class, 0);
    }

    /**
     * Tests full interface declaration.
     */
    @Test
    public void testInterfaceDeclarationFull() {
        ClassesParser parser = getClassesParser("""
            @Test1 @Test2(2) public shared interface IFullTest<T> extends ITest<T>, Serializable, List<T>
                permits FinalTest, UnitTest, Test, Quiz, PopQuiz
            {}
            """);
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = accessMod.getLocation();
        ASTInterfaceDeclaration node = parser.parseInterfaceDeclaration(loc, annList, accessMod, genModList);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 2);
        assertTrue(node.getAccessMod().isPresent());
        checkList(node.getInterfaceModifierList(), INTERFACE_MODIFIERS, ASTKeywordNode.class, 1);
        assertEquals("IFullTest", node.getName().getValue());
        assertTrue(node.getTypeParams().isPresent());
        assertTrue(node.getExtendsInterfaces().isPresent());
        assertTrue(node.getPermits().isPresent());
        checkList(node.getInterfaceParts(), INTERFACE_PARTS, ASTInterfacePart.class, 0);
    }

    /**
     * Tests bad interface declaration of bad modifier.
     */
    @Test
    public void testInterfaceDeclarationBadMod() {
        ClassesParser parser = getClassesParser("""
            public volatile interface Bad {
            }
            """);
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = accessMod.getLocation();
        ASTInterfaceDeclaration node = parser.parseInterfaceDeclaration(loc, annList, accessMod, genModList);
        expectError(node, parser);
    }

    /**
     * Tests extends interfaces (extends clause on interface).
     */
    @Test
    public void testExtendsInterfaces() {
        ClassesParser parser = getClassesParser("extends Copyable, Serializable");
        ASTDataTypeNoArrayList node = parser.parseExtendsInterfaces();
        ensureNoErrors(node, parser);
        checkList(node, DATA_TYPES_NO_ARRAY, ASTDataTypeNoArray.class, 2);
    }

    /**
     * Tests empty interface body.
     */
    @Test
    public void testInterfaceBodyEmpty() {
        ClassesParser parser = getClassesParser("{}");
        ASTInterfacePartList node = parser.parseInterfaceBody();
        ensureNoErrors(node, parser);
        checkList(node, INTERFACE_PARTS, ASTInterfacePart.class, 0);
    }

    /**
     * Tests interface body.
     */
    @Test
    public void testInterfaceBody() {
        ClassesParser parser = getClassesParser("""
                {
                    constant Integer i = 1;
                    class Inner{}
                    default Integer getI() {
                        return i;
                    }
                }
                """);
        ASTInterfacePartList node = parser.parseInterfaceBody();
        ensureNoErrors(node, parser);
        checkList(node, INTERFACE_PARTS, ASTInterfacePart.class, 3);
    }

    /**
     * Tests bad interface body with no open brace.
     */
    @Test
    public void testInterfaceBodyNoOpenBrace() {
        ClassesParser parser = getClassesParser("""
                    constant Integer i = 1;
                    class Inner{}
                    default Integer getI() {
                        return i;
                    }
                }
                """);
        ASTInterfacePartList node = parser.parseInterfaceBody();
        expectError(node, parser);
    }

    /**
     * Tests bad interface body with no close brace.
     */
    @Test
    public void testInterfaceBodyNoCloseBrace() {
        ClassesParser parser = getClassesParser("""
                {
                    constant Integer i = 1;
                    class Inner{}
                    default Integer getI() {
                        return i;
                    }
                """);
        ASTInterfacePartList node = parser.parseInterfaceBody();
        expectError(node, parser);
    }

    /**
     * Tests interface part list of all possible interface parts.
     */
    @Test
    public void testInterfacePartListComprehensive() {
        ClassesParser parser = getClassesParser("""
                constant String foo = "Foo!";
                default String getToo() {
                    return too;
                }
                TrafficLight getStatus();
                class Nested {}
                enum TrafficLight {RED, YELLOW, GREEN}
                @Test interface Helper {}
                annotation InnerAnnotation {}
                record FooRecord(String bar) {}
                adt Foo {
                    Boo() {},
                    Goo() {}
                }
                """);
        ASTInterfacePartList node = parser.parseInterfacePartList();
        ensureNoErrors(node, parser);
        checkList(node, INTERFACE_PARTS, ASTInterfacePart.class, 9);
    }

    /**
     * Tests interface part list of interface part.
     */
    @Test
    public void testInterfacePartListOfInterfacePart() {
        ClassesParser parser = getClassesParser("constant Integer i = 1;");
        ASTInterfacePartList node = parser.parseInterfacePartList();
        ensureNoErrors(node, parser);
        checkList(node, INTERFACE_PARTS, ASTInterfacePart.class, 1);
    }

    /**
     * Tests interface part list.
     */
    @Test
    public void testInterfacePartList() {
        ClassesParser parser = getClassesParser("""
            constant Integer i = 1;
            class Inner {}
            """);
        ASTInterfacePartList node = parser.parseInterfacePartList();
        ensureNoErrors(node, parser);
        checkList(node, INTERFACE_PARTS, ASTInterfacePart.class, 2);
    }

    /**
     * Tests interface part lists of multiple interface parts.
     */
    @Test
    public void testInterfacePartListMultiple() {
        ClassesParser parser = getClassesParser("""
                constant Integer i = 1;
                class Inner {}
                Integer getI();
                """);
        ASTInterfacePartList node = parser.parseInterfacePartList();
        ensureNoErrors(node, parser);
        checkList(node, INTERFACE_PARTS, ASTInterfacePart.class, 3);
    }

    /**
     * Tests interface part of method declaration with void result.
     */
    @Test
    public void testInterfacePartOfMethodDeclarationVoidResult() {
        ClassesParser parser = getClassesParser("public void method();");
        ASTInterfacePart node = parser.parseInterfacePart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTInterfaceMethodDeclaration.class, node);
    }

    /**
     * Tests interface part of method declaration with void result and type parameters.
     */
    @Test
    public void testInterfacePartOfMethodDeclarationVoidResultTypeParameters() {
        ClassesParser parser = getClassesParser("public <T> void method(T param);");
        ASTInterfacePart node = parser.parseInterfacePart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTInterfaceMethodDeclaration.class, node);
    }

    /**
     * Tests interface part of method declaration data type void result.
     */
    @Test
    public void testInterfacePartOfMethodDeclarationDataTypeResult() {
        ClassesParser parser = getClassesParser("public String method();");
        ASTInterfacePart node = parser.parseInterfacePart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTInterfaceMethodDeclaration.class, node);
    }

    /**
     * Tests interface part of method declaration with data type result and type parameters.
     */
    @Test
    public void testInterfacePartOfMethodDeclarationDataTypeResultTypeParameters() {
        ClassesParser parser = getClassesParser("public <T> T method(T param);");
        ASTInterfacePart node = parser.parseInterfacePart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTInterfaceMethodDeclaration.class, node);
    }

    /**
     * Tests interface part of method declaration with mut result.
     */
    @Test
    public void testInterfacePartOfMethodDeclarationConstResult() {
        ClassesParser parser = getClassesParser("mut String method(String param);");
        ASTInterfacePart node = parser.parseInterfacePart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTInterfaceMethodDeclaration.class, node);
    }

    /**
     * Tests interface part of constant declaration.
     */
    @Test
    public void testInterfacePartOfConstantDeclaration() {
        ClassesParser parser = getClassesParser("constant String LANGUAGE = \"Spruce\";");
        ASTInterfacePart node = parser.parseInterfacePart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTConstantDeclaration.class, node);
    }

    /**
     * Tests interface part of class declaration.
     */
    @Test
    public void testInterfacePartOfClassDeclaration() {
        ClassesParser parser = getClassesParser("public shared class Nested {}");
        ASTInterfacePart node = parser.parseInterfacePart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTClassDeclaration.class, node);
    }

    /**
     * Tests interface part of enum declaration.
     */
    @Test
    public void testInterfacePartOfEnumDeclaration() {
        ClassesParser parser = getClassesParser("private enum Light {RED, YELLOW, GREEN}");
        ASTInterfacePart node = parser.parseInterfacePart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTEnumDeclaration.class, node);
    }

    /**
     * Tests interface part of interface declaration.
     */
    @Test
    public void testInterfacePartOfInterfaceDeclaration() {
        ClassesParser parser = getClassesParser("private interface TrafficLight { Light getStatus(); }");
        ASTInterfacePart node = parser.parseInterfacePart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTInterfaceDeclaration.class, node);
    }

    /**
     * Tests interface part of annotation declaration.
     */
    @Test
    public void testInterfacePartOfAnnotationDeclaration() {
        ClassesParser parser = getClassesParser("public annotation Test { String getStatus() default \"SUCCESS\"; }");
        ASTInterfacePart node = parser.parseInterfacePart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTAnnotationDeclaration.class, node);
    }

    /**
     * Tests interface part of record declaration.
     */
    @Test
    public void testInterfacePartOfRecordDeclaration() {
        ClassesParser parser = getClassesParser("internal record Redacted(String byWhom) { }");
        ASTInterfacePart node = parser.parseInterfacePart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTRecordDeclaration.class, node);
    }

    /**
     * Tests interface part of adt declaration.
     */
    @Test
    public void testInterfacePartOfAdtDeclaration() {
        ClassesParser parser = getClassesParser("""
                public adt Optional<T> {
                    None() {},
                    Some(T value) {
                        T getValue() {
                            return value();
                        }
                    }
                }
                """);
        ASTInterfacePart node = parser.parseInterfacePart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTAdtDeclaration.class, node);
    }

    /**
     * Tests bad interface part of bad constant of variable modifier.
     */
    @Test
    public void testInterfacePartBadConstant() {
        ClassesParser parser = getClassesParser("public constant var String BAD_CONSTANT = \"Bad!\"");
        ASTInterfacePart node = parser.parseInterfacePart();
        expectError(node, parser, 2);
    }

    /**
     * Tests simple interface method declaration.
     */
    @Test
    public void testInterfaceMethodDeclarationSimple() {
        ClassesParser parser = getClassesParser("Boolean add(T element);");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTVariableModifierList varModList = parser.getStatementsParser().parseVariableModifierList();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTInterfaceMethodDeclaration node = parser.parseInterfaceMethodDeclaration(loc, annList, null, genModList, varModList, dt);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 0);
        assertFalse(node.getAccessMod().isPresent());
        checkList(node.getModifierList(), INTERFACE_METHOD_MODIFIERS, ASTKeywordNode.class, 0);
        assertNotNull(node.getHeader());
        assertNotNull(node.getBody());
    }

    /**
     * Tests interface method declaration with access modifier and method modifier.
     */
    @Test
    public void testInterfaceMethodDeclarationAccessModifierMethodModifier() {
        ClassesParser parser = getClassesParser("""
            private default void addAll(Collection<T> other) {
                for (T element in other) {
                    add(other);
                }
            }
            """);
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = accessMod.getLocation();
        ASTInterfaceMethodDeclaration node = parser.parseInterfaceMethodDeclaration(loc, annList, accessMod, genModList);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 0);
        assertTrue(node.getAccessMod().isPresent());
        assertEquals(PRIVATE, node.getAccessMod().get().getKeyword());
        checkList(node.getModifierList(), INTERFACE_METHOD_MODIFIERS, ASTKeywordNode.class, 1);
        assertNotNull(node.getHeader());
        assertNotNull(node.getBody());
    }

    /**
     * Tests interface method declaration with annotation.
     */
    @Test
    public void testInterfaceMethodDeclarationAnnotation() {
        ClassesParser parser = getClassesParser("""
            @Baz(3.14) private default void addAll(Collection<T> other) {
                for (T element in other) {
                    add(other);
                }
            }
            """);
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = accessMod.getLocation();
        ASTInterfaceMethodDeclaration node = parser.parseInterfaceMethodDeclaration(loc, annList, accessMod, genModList);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 1);
        assertTrue(node.getAccessMod().isPresent());
        assertEquals(PRIVATE, node.getAccessMod().get().getKeyword());
        checkList(node.getModifierList(), INTERFACE_METHOD_MODIFIERS, ASTKeywordNode.class, 1);
        assertNotNull(node.getHeader());
        assertNotNull(node.getBody());
    }

    /**
     * Tests bad interface method declaration of bad modifier.
     */
    @Test
    public void testInterfaceMethodDeclarationBadMod() {
        ClassesParser parser = getClassesParser("""
            private volatile void addAll(Collection<T> other) {
                for (T element in other) {
                    add(other);
                }
            }d
            """);
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = accessMod.getLocation();
        ASTInterfaceMethodDeclaration node = parser.parseInterfaceMethodDeclaration(loc, annList, accessMod, genModList);
        expectError(node, parser);
    }

    /**
     * Tests bad constant declaration, no "constant".
     */
    @Test
    public void testConstantDeclaration() {
        ClassesParser parser = getClassesParser("String test = \"Test\";");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTConstantDeclaration node = parser.parseConstantDeclaration(loc, annList, null, genModList, dt);
        expectError(node, parser);
    }

    /**
     * Tests constant declaration, no "constant".
     */
    @Test
    public void testConstantDeclarationAccessMod() {
        ClassesParser parser = getClassesParser("public String test = \"Test\";");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = accessMod.getLocation();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTConstantDeclaration node = parser.parseConstantDeclaration(loc, annList, accessMod, genModList, dt);
        expectError(node, parser);
    }

    /**
     * Tests constant declaration with "constant".
     */
    @Test
    public void testConstantDeclarationOfConstant() {
        ClassesParser parser = getClassesParser("constant String test = \"Test\";");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTConstantDeclaration node = parser.parseConstantDeclaration(loc, annList, null, genModList, dt);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 0);
        assertFalse(node.getAccessMod().isPresent());
        assertNotNull(node.getConstantMod());
        assertNotNull(node.getDataType());
        assertNotNull(node.getVarDeclList());
    }

    /**
     * Tests constant declaration with an annotation.
     */
    @Test
    public void testConstantDeclarationOfAnnotation() {
        ClassesParser parser = getClassesParser("@Test constant String test = \"Test\";");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTConstantDeclaration node = parser.parseConstantDeclaration(loc, annList, null, genModList, dt);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 1);
        assertFalse(node.getAccessMod().isPresent());
        assertNotNull(node.getConstantMod());
        assertNotNull(node.getDataType());
        assertNotNull(node.getVarDeclList());
    }

    /**
     * Tests bad constant declaration of no semicolon.
     */
    @Test
    public void testConstantDeclarationNoSemicolon() {
        ClassesParser parser = getClassesParser("constant String noSemicolon = \"Test\"}");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTConstantDeclaration node = parser.parseConstantDeclaration(loc, annList, null, genModList, dt);
        expectError(node, parser);
    }

    /**
     * Tests bad adt declaration no adt body.
     */
    @Test
    public void testAdtDeclarationNoAdtBody() {
        Scanner scanner = new Scanner("adt Bad;");
        ClassesParser parser = new Parser(scanner, new BaseMessageProducer()).getClassesParser();
        ASTAnnotationList annList = parser.parseAnnotationList();
        Location loc = scanner.getCurrToken().getLocation();
        ASTAdtDeclaration node = parser.parseAdtDeclaration(loc, annList, null);
        expectError(node, parser, 2);
    }

    /**
     * Tests bad adt declaration no adt.
     */
    @Test
    public void testAdtDeclarationNoAdt() {
        Scanner scanner = new Scanner("throw Optional { None() {}, Some(Object value) {}}");
        ClassesParser parser = new Parser(scanner, new BaseMessageProducer()).getClassesParser();
        ASTClassPart node = parser.parseClassPart();
        expectError(node, parser, 3);
    }

    /**
     * Tests full adt declaration.
     */
    @Test
    public void testAdtDeclarationFull() {
        Scanner scanner = new Scanner("""
                @Test public adt Optional<T> extends Bar { None() {}, Some(T value) {}}
                """);
        ClassesParser parser = new Parser(scanner, new BaseMessageProducer()).getClassesParser();
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        ASTAdtDeclaration node = parser.parseAdtDeclaration(accessMod.getLocation(), annList, accessMod);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 1);
        assertTrue(node.getAccessMod().isPresent());
        assertEquals("Optional", node.getName().getValue());
        assertTrue(node.getTypeParams().isPresent());
        assertTrue(node.getExtendsInterfaces().isPresent());
        assertNotNull(node.getAdtBody());
    }

    /**
     * Tests simple adt declaration.
     */
    @Test
    public void testAdtDeclarationSimple() {
        Scanner scanner = new Scanner("adt Optional { None() {}, Some(Object value) {}}");
        ClassesParser parser = new Parser(scanner, new BaseMessageProducer()).getClassesParser();
        ASTAnnotationList annList = parser.parseAnnotationList();
        Location loc = scanner.getCurrToken().getLocation();
        ASTAdtDeclaration node = parser.parseAdtDeclaration(loc, annList,null);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 0);
        assertFalse(node.getAccessMod().isPresent());
        assertEquals("Optional", node.getName().getValue());
        assertFalse(node.getTypeParams().isPresent());
        assertFalse(node.getExtendsInterfaces().isPresent());
        assertNotNull(node.getAdtBody());
    }

    /**
     * Tests adt body without close brace.
     */
    @Test
    public void testAdtBodyMissingCloseBrace() {
        ClassesParser parser = getClassesParser("""
                {
                    None() {},
                    Some(T value) {};
                    
                    T getValue();
                """);
        ASTAdtBody node = parser.parseAdtBody();
        expectError(node, parser);
    }

    /**
     * Tests adt body without open brace.
     */
    @Test
    public void testAdtBodyMissingOpenBrace() {
        ClassesParser parser = getClassesParser("""
                    None() {},
                    Some(T value) {};
                    
                    T getValue();
                }
                """);
        ASTAdtBody node = parser.parseAdtBody();
        expectError(node, parser);
    }

    /**
     * Tests adt body.
     */
    @Test
    public void testAdtBody() {
        ClassesParser parser = getClassesParser("""
                {
                    None() {},
                    Some(T value) {};
                    
                    T getValue();
                }
                """);
        ASTAdtBody node = parser.parseAdtBody();
        ensureNoErrors(node, parser);

        assertNotNull(node.getVariantList());
        assertNotNull(node.getBodyDecls());
    }

    /**
     * Tests adt body without body declarations.
     */
    @Test
    public void testAdtBodyNoBodyDeclarations() {
        ClassesParser parser = getClassesParser("""
                {
                    None() {},
                    Some(T value) {}
                }
                """);
        ASTAdtBody node = parser.parseAdtBody();
        ensureNoErrors(node, parser);

        assertNotNull(node.getVariantList());
        assertNotNull(node.getBodyDecls());
    }

    /**
     * Tests variant list.
     */
    @Test
    public void testVariantList() {
        ClassesParser parser = getClassesParser("Here, There(Location l) {}, Anywhere}");
        ASTVariantList node = parser.parseVariantList();
        ensureNoErrors(node, parser);
        checkList(node, VARIANTS, ASTVariant.class, 3);
    }

    /**
     * Tests variant of compact record declaration.
     */
    @Test
    public void testVariantOfCompactRecordDeclaration() {
        ClassesParser parser = getClassesParser("None() {}");
        ASTVariant node = parser.parseVariant();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTCompactRecordDeclaration.class, node);
    }

    /**
     * Tests variant of data type.
     */
    @Test
    public void testVariantOfVariantType() {
        ClassesParser parser = getClassesParser("Elsewhere,");
        ASTVariant node = parser.parseVariant();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTVariantType.class, node);
    }

    /**
     * Tests variant type.
     */
    @Test
    public void testVariantType() {
        ClassesParser parser = getClassesParser("VariantType");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTVariantType node = parser.parseVariantType(annList);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 0);
        assertNotNull(node.getDtna());
    }

    /**
     * Tests variant type with annotation.
     */
    @Test
    public void testVariantTypeAnnotation() {
        ClassesParser parser = getClassesParser("@Abc VariantType");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTVariantType node = parser.parseVariantType(annList);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 1);
        assertNotNull(node.getDtna());
    }

    /**
     * Tests bad compact record declaration.
     */
    @Test
    public void testBadCompactRecordDeclaration() {
        ClassesParser parser = getClassesParser("record None() {}");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTCompactRecordDeclaration node = parser.parseCompactRecordDeclaration(annList);
        expectError(node, parser);
    }

    /**
     * Tests full compact record declaration.
     */
    @Test
    public void testCompactRecordDeclarationFull() {
        ClassesParser parser = getClassesParser("""
                @Test Some<T>(T value) implements Foo {
                    public T get() {
                        return value();
                    }
                }
                """);
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTCompactRecordDeclaration node = parser.parseCompactRecordDeclaration(annList);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 1);
        assertEquals("Some", node.getName().getValue());
        assertTrue(node.getTypeParams().isPresent());
        checkList(node.getRecordCompList(), RECORD_COMPONENTS, ASTRecordComponent.class, 1);
        assertTrue(node.getSuperinterfaces().isPresent());
        checkList(node.getSuperinterfaces().get(), DATA_TYPES_NO_ARRAY, ASTDataTypeNoArray.class, 1);
        checkList(node.getClassParts(), CLASS_PARTS, ASTClassPart.class, 1);
    }

    /**
     * Tests simple compact record declaration.
     */
    @Test
    public void testCompactRecordDeclarationSimple() {
        ClassesParser parser = getClassesParser("None() {}");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTCompactRecordDeclaration node = parser.parseCompactRecordDeclaration(annList);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 0);
        assertEquals("None", node.getName().getValue());
        assertFalse(node.getTypeParams().isPresent());
        checkList(node.getRecordCompList(), RECORD_COMPONENTS, ASTRecordComponent.class, 0);
        assertFalse(node.getSuperinterfaces().isPresent());
        checkList(node.getClassParts(), CLASS_PARTS, ASTClassPart.class, 0);
    }

    /**
     * Tests bad compact record declaration of no identifier.
     */
    @Test
    public void testCompactRecordDeclarationNoIdentifier() {
        ClassesParser parser = getClassesParser("public() {}");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTCompactRecordDeclaration node = parser.parseCompactRecordDeclaration(annList);
        expectError(node, parser, 13);
    }

    /**
     * Tests bad Adt Body Declarations.
     */
    @Test
    public void testBadAdtBodyDeclarations() {
        ClassesParser parser = getClassesParser("public T getValue();");
        ASTInterfacePartList node = parser.parseAdtBodyDeclarations();
        expectError(node, parser);
    }

    /**
     * Tests Adt Body Declarations.
     */
    @Test
    public void testAdtBodyDeclarations() {
        ClassesParser parser = getClassesParser("; public T getValue();");
        ASTInterfacePartList node = parser.parseAdtBodyDeclarations();
        ensureNoErrors(node, parser);
        checkList(node, INTERFACE_PARTS, ASTInterfacePart.class, 1);
    }

    /**
     * Tests a full record declaration.
     */
    @Test
    public void testRecordDeclarationFull() {
        Scanner scanner = new Scanner("""
                @Test(val1 = "one", val2 = "two")
                public record Value<T>(T value) implements Comparable<T> {}
                """);
        ClassesParser parser = new Parser(scanner, new BaseMessageProducer()).getClassesParser();
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTKeywordNode am = parser.parseAccessModifier();
        ASTRecordDeclaration node = parser.parseRecordDeclaration(am.getLocation(), annList, am);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 1);
        assertTrue(node.getAccessMod().isPresent());
        assertEquals("Value", node.getName().getValue());
        assertTrue(node.getTypeParams().isPresent());
        checkList(node.getRecordCompList(), RECORD_COMPONENTS, ASTRecordComponent.class, 1);
        assertTrue(node.getSuperinterfaces().isPresent());
        checkList(node.getSuperinterfaces().get(), DATA_TYPES_NO_ARRAY, ASTDataTypeNoArray.class, 1);
        checkList(node.getClassParts(), CLASS_PARTS, ASTClassPart.class, 0);
    }

    /**
     * Tests a simple record declaration.
     */
    @Test
    public void testRecordDeclarationSimple() {
        Scanner scanner = new Scanner("record Person(String first, String last) {}");
        ClassesParser parser = new Parser(scanner, new BaseMessageProducer()).getClassesParser();
        ASTAnnotationList annList = parser.parseAnnotationList();
        Location loc = scanner.getCurrToken().getLocation();
        ASTRecordDeclaration node = parser.parseRecordDeclaration(loc, annList,null);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 0);
        assertFalse(node.getAccessMod().isPresent());
        assertEquals("Person", node.getName().getValue());
        assertFalse(node.getTypeParams().isPresent());
        checkList(node.getRecordCompList(), RECORD_COMPONENTS, ASTRecordComponent.class, 2);
        assertFalse(node.getSuperinterfaces().isPresent());
        checkList(node.getClassParts(), CLASS_PARTS, ASTClassPart.class, 0);
    }

    /**
     * Tests bad Record Header, missing close parenthesis.
     */
    @Test
    public void testRecordHeaderMissingCloseParen() {
        ClassesParser parser = getClassesParser("(String filename, Int lineNbr");
        ASTRecordComponentList node = parser.parseRecordHeader();
        expectError(node, parser);
    }

    /**
     * Tests bad Record Header, missing open parenthesis.
     */
    @Test
    public void testRecordHeaderMissingOpenParen() {
        ClassesParser parser = getClassesParser("String filename, Int lineNbr)");
        ASTRecordComponentList node = parser.parseRecordHeader();
        expectError(node, parser);
    }

    /**
     * Tests a Record Header.
     */
    @Test
    public void testRecordHeader() {
        ClassesParser parser = getClassesParser("(String filename, Int lineNbr)");
        ASTRecordComponentList node = parser.parseRecordHeader();
        ensureNoErrors(node, parser);
        checkList(node, RECORD_COMPONENTS, ASTRecordComponent.class, 2);
    }

    /**
     * Tests record component list of record component.
     */
    @Test
    public void testRecordComponentListOfFormalParameter() {
        ClassesParser parser = getClassesParser("const Int a");
        ASTRecordComponentList node = parser.parseRecordComponentList();
        ensureNoErrors(node, parser);
        checkList(node, RECORD_COMPONENTS, ASTRecordComponent.class, 1);
    }

    /**
     * Tests record component list.
     */
    @Test
    public void testRecordComponentList() {
        ClassesParser parser = getClassesParser("String msg, Foo f, Bar b");
        ASTRecordComponentList node = parser.parseRecordComponentList();
        ensureNoErrors(node, parser);
        checkList(node, RECORD_COMPONENTS, ASTRecordComponent.class, 3);
    }

    /**
     * Tests record componentr list with varargs component list.
     */
    @Test
    public void testRecordComponentListOfLastVarargs() {
        ClassesParser parser = getClassesParser("Point pt, Double... coordinates");
        ASTRecordComponentList node = parser.parseRecordComponentList();
        ensureNoErrors(node, parser);
        checkList(node, RECORD_COMPONENTS, ASTRecordComponent.class, 2);
    }

    /**
     * Tests bad record component list if varargs not last, compiler error.
     */
    @Test
    public void testRecordComponentListVarargsNotLastError() {
        ClassesParser parser = getClassesParser("Double... coordinates, Point pt");
        ASTRecordComponentList node = parser.parseRecordComponentList();
        expectError(node, parser);
    }

    /**
     * Tests record component list of record components of take and of annotation.
     */
    @Test
    public void testRecordComponentListTakeAnnotation() {
        ClassesParser parser = getClassesParser("take String foo, @Baz Integer bar");
        ASTRecordComponentList node = parser.parseRecordComponentList();
        ensureNoErrors(node, parser);
        checkList(node, RECORD_COMPONENTS, ASTRecordComponent.class, 2);
    }

    /**
     * Tests basic record component.
     */
    @Test
    public void testRecordComponentNoEllipsis() {
        ClassesParser parser = getClassesParser("String filename");
        ASTRecordComponent node = parser.parseRecordComponent();
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 0);
        assertFalse(node.getTakeMod().isPresent());
        assertNotNull(node.getDataType());
        assertFalse(node.getEllipsisMod().isPresent());
        assertEquals("filename", node.getName().getValue());
    }

    /**
     * Tests record component with an annotation, "take", and an ellipsis.
     */
    @Test
    public void testRecordComponentAll() {
        ClassesParser parser = getClassesParser("@Foo take String ... filenames");
        ASTRecordComponent node = parser.parseRecordComponent();
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 1);
        assertTrue(node.getTakeMod().isPresent());
        assertNotNull(node.getDataType());
        assertTrue(node.getEllipsisMod().isPresent());
        assertEquals("filenames", node.getName().getValue());
    }

    /**
     * Tests a compact constructor declaration, with access modifier.
     */
    @Test
    public void testCompactConstructorDeclarationAccessModifier() {
        ClassesParser parser = getClassesParser("""
                public constructor {
                    a *= 2;
                    b /= 2;
                }
                """);
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTKeywordNode am = parser.parseAccessModifier();
        ASTCompactConstructorDeclaration node = parser.parseCompactConstructorDeclaration(am.getLocation(), annList, am);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 0);
        assertTrue(node.getAccessMod().isPresent());
        assertNotNull(node.getBlock());
    }

    /**
     * Tests a simple compact constructor declaration, no access modifier.
     */
    @Test
    public void testCompactConstructorDeclaration() {
        Scanner scanner = new Scanner("""
                constructor {
                    a *= 2;
                    b /= 2;
                }
                """);
        ClassesParser parser = new Parser(scanner, new BaseMessageProducer()).getClassesParser();
        ASTAnnotationList annList = parser.parseAnnotationList();
        Location loc = scanner.getCurrToken().getLocation();
        ASTCompactConstructorDeclaration node = parser.parseCompactConstructorDeclaration(loc, annList,null);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 0);
        assertFalse(node.getAccessMod().isPresent());
        assertNotNull(node.getBlock());
    }

    /**
     * Tests a simple compact constructor declaration, with annotation.
     */
    @Test
    public void testCompactConstructorDeclarationAnnotation() {
        Scanner scanner = new Scanner("""
                @Foo constructor {
                    a *= 2;
                    b /= 2;
                }
                """);
        ClassesParser parser = new Parser(scanner, new BaseMessageProducer()).getClassesParser();
        ASTAnnotationList annList = parser.parseAnnotationList();
        Location loc = scanner.getCurrToken().getLocation();
        ASTCompactConstructorDeclaration node = parser.parseCompactConstructorDeclaration(loc, annList,null);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 1);
        assertFalse(node.getAccessMod().isPresent());
        assertNotNull(node.getBlock());
    }

    /**
     * Tests simple enum declaration.
     */
    @Test
    public void testEnumDeclarationSimple() {
        ClassesParser parser = getClassesParser("enum Dummy {DUMMY}");
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        ASTAnnotationList annList = parser.parseAnnotationList();
        Location loc = genModList.getLocation();
        ASTEnumDeclaration node = parser.parseEnumDeclaration(loc, annList,null, genModList);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 0);
        assertFalse(node.getAccessMod().isPresent());
        checkList(node.getClassModifierList(), CLASS_MODIFIERS, ASTKeywordNode.class, 0);
        assertEquals("Dummy", node.getName().getValue());
        assertFalse(node.getSuperinterfaces().isPresent());
        assertNotNull(node.getEnumBody());
    }

    /**
     * Tests full enum declaration.
     */
    @Test
    public void testEnumDeclarationFull() {
        ClassesParser parser = getClassesParser("@Test public shared enum FullEnumTest implements Serializable {QUIZ, TEST, FINAL}");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = accessMod.getLocation();
        ASTEnumDeclaration node = parser.parseEnumDeclaration(loc, annList, accessMod, genModList);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 1);
        assertTrue(node.getAccessMod().isPresent());
        checkList(node.getClassModifierList(), CLASS_MODIFIERS, ASTKeywordNode.class, 1);
        assertEquals("FullEnumTest", node.getName().getValue());
        assertTrue(node.getSuperinterfaces().isPresent());
        checkList(node.getSuperinterfaces().get(), DATA_TYPES_NO_ARRAY, ASTDataTypeNoArray.class, 1);
        assertNotNull(node.getEnumBody());
    }

    /**
     * Tests bad enum declaration of bad enum modifier.
     */
    @Test
    public void testEnumDeclarationBadGenMod() {
        ClassesParser parser = getClassesParser("public volatile enum BadEnumTest {VOLATILE}");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = accessMod.getLocation();
        ASTEnumDeclaration node = parser.parseEnumDeclaration(loc, annList, accessMod, genModList);
        expectError(node, parser);
    }

    /**
     * Tests simple enum body.
     */
    @Test
    public void testEnumBodySimple() {
        ClassesParser parser = getClassesParser("{\nRED, YELLOW, GREEN\n}");
        ASTEnumBody node = parser.parseEnumBody();
        ensureNoErrors(node, parser);
        checkList(node.getEnumConstants(), ENUM_CONSTANTS, ASTEnumConstant.class, 3);
        checkList(node.getClassParts(), CLASS_PARTS, ASTClassPart.class, 0);
    }

    /**
     * Tests enum body of nothing.
     */
    @Test
    public void testEnumBodyOfNothing() {
        ClassesParser parser = getClassesParser("{}");
        ASTEnumBody node = parser.parseEnumBody();
        ensureNoErrors(node, parser);
        checkList(node.getEnumConstants(), ENUM_CONSTANTS, ASTEnumConstant.class, 0);
        checkList(node.getClassParts(), CLASS_PARTS, ASTClassPart.class, 0);
    }

    /**
     * Tests enum body of utility methods.
     */
    @Test
    public void testEnumBodyOfUtility() {
        ClassesParser parser = getClassesParser("""
                {;
                    shared void utility() {
                        stdout.println("Utility!");
                    }
                }
                """);
        ASTEnumBody node = parser.parseEnumBody();
        ensureNoErrors(node, parser);
        checkList(node.getEnumConstants(), ENUM_CONSTANTS, ASTEnumConstant.class, 0);
        checkList(node.getClassParts(), CLASS_PARTS, ASTClassPart.class, 1);
    }

    /**
     * Tests enum body of constants and class part list.
     */
    @Test
    public void testEnumBodyOfConstantsClassPartList() {
        ClassesParser parser = getClassesParser("""
                {
                    RED, YELLOW, GREEN;
                    shared void utility() {
                        stdout.println("Utility!");
                    }
                }
                """);
        ASTEnumBody node = parser.parseEnumBody();
        ensureNoErrors(node, parser);
        checkList(node.getEnumConstants(), ENUM_CONSTANTS, ASTEnumConstant.class, 3);
        checkList(node.getClassParts(), CLASS_PARTS, ASTClassPart.class, 1);
    }

    /**
     * Tests bad enum body of no open brace.
     */
    @Test
    public void testEnumBodyNoOpenBrace() {
        ClassesParser parser = getClassesParser("SATURDAY, SUNDAY}");
        ASTEnumBody node = parser.parseEnumBody();
        expectError(node, parser);
    }

    /**
     * Tests bad enum body of no close brace.
     */
    @Test
    public void testEnumBodyNoCloseBrace() {
        ClassesParser parser = getClassesParser("{SATURDAY, SUNDAY");
        ASTEnumBody node = parser.parseEnumBody();
        expectError(node, parser);
    }

    /**
     * Tests bad enum body of no semicolon but enum body declarations.
     */
    @Test
    public void testEnumBodyNoSemicolon() {
        ClassesParser parser = getClassesParser("""
                {
                    SATURDAY, SUNDAY
                    constant WeekendDay FUN_DAY = SUNDAY;
                }
                """);
        ASTEnumBody node = parser.parseEnumBody();
        expectError(node, parser);
    }

    /**
     * Tests enum body declarations.
     */
    @Test
    public void testEnumBodyDeclarations() {
        ClassesParser parser = getClassesParser("""
        ;
        constructor() {}
        """);
        ASTClassPartList node = parser.parseEnumBodyDeclarations();
        ensureNoErrors(node, parser);
        checkList(node, CLASS_PARTS, ASTClassPart.class, 1);
    }

    /**
     * Tests enum constant list of enum constant.
     */
    @Test
    public void testEnumConstantListOfEnumConstant() {
        ClassesParser parser = getClassesParser("SINGLETON");
        ASTEnumConstantList node = parser.parseEnumConstantList();
        ensureNoErrors(node, parser);
        checkList(node, ENUM_CONSTANTS, ASTEnumConstant.class, 1);
    }

    /**
     * Tests enum constant list.
     */
    @Test
    public void testEnumConstantList() {
        ClassesParser parser = getClassesParser("RED, YELLOW, GREEN");
        ASTEnumConstantList node = parser.parseEnumConstantList();
        ensureNoErrors(node, parser);
        checkList(node, ENUM_CONSTANTS, ASTEnumConstant.class, 3);
    }

    /**
     * Tests enum constant list with annotations.
     */
    @Test
    public void testEnumConstantListAnnotations() {
        ClassesParser parser = getClassesParser("@Color RED, @Color YELLOW, @Color GREEN");
        ASTEnumConstantList node = parser.parseEnumConstantList();
        ensureNoErrors(node, parser);
        checkList(node, ENUM_CONSTANTS, ASTEnumConstant.class, 3);
    }

    /**
     * Tests simple enum constant.
     */
    @Test
    public void testEnumConstantSimple() {
        ClassesParser parser = getClassesParser("RED");
        ASTEnumConstant node = parser.parseEnumConstant();
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 0);
        assertEquals("RED", node.getName().getValue());
        checkList(node.getArgsList(), ARGUMENTS, ASTGiveExpression.class, 0);
        checkList(node.getClassParts(), CLASS_PARTS, ASTClassPart.class, 0);
    }

    @Test
    public void testEnumConstantAnnotation() {
        ClassesParser parser = getClassesParser("@Color RED");
        ASTEnumConstant node = parser.parseEnumConstant();
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 1);
        assertEquals("RED", node.getName().getValue());
        checkList(node.getArgsList(), ARGUMENTS, ASTGiveExpression.class, 0);
        checkList(node.getClassParts(), CLASS_PARTS, ASTClassPart.class, 0);
    }

    /**
     * Tests full enum constant.
     */
    @Test
    public void testEnumConstantOfArgumentListClassBody() {
        ClassesParser parser = getClassesParser("RED(\"#F9152F\") { override String toString() { return \"Red Light\"; } }");
        ASTEnumConstant node = parser.parseEnumConstant();
        ensureNoErrors(node, parser);

        assertEquals("RED", node.getName().getValue());
        checkList(node.getArgsList(), ARGUMENTS, ASTGiveExpression.class, 1);
        checkList(node.getClassParts(), CLASS_PARTS, ASTClassPart.class, 1);
    }

    /**
     * Tests bad enum constant of no close parenthesis.
     */
    @Test
    public void testEnumConstantNoCloseParen() {
        ClassesParser parser = getClassesParser("RED(\"#F9152F\" { override String toString() { return \"Red Light\"; } }");
        ASTEnumConstant node = parser.parseEnumConstant();
        expectError(node, parser);
    }

    /**
     * Tests simple class declaration.
     */
    @Test
    public void testClassDeclarationSimple() {
        ClassesParser parser = getClassesParser("class Dummy {}");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTClassDeclaration node = parser.parseClassDeclaration(loc, annList,null, genModList);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 0);
        assertFalse(node.getAccessMod().isPresent());
        checkList(node.getClassModifierList(), CLASS_MODIFIERS, ASTKeywordNode.class, 0);
        assertEquals("Dummy", node.getName().getValue());
        assertFalse(node.getTypeParams().isPresent());
        assertFalse(node.getSuperclass().isPresent());
        assertFalse(node.getSuperinterfaces().isPresent());
        assertFalse(node.getPermits().isPresent());
        checkList(node.getClassParts(), CLASS_PARTS, ASTClassPart.class, 0);
    }

    /**
     * Tests full class declaration.
     */
    @Test
    public void testClassDeclarationFull() {
        ClassesParser parser = getClassesParser("""
            @Test1 @Test2 @Test3 public shared class FullTest<T> extends Test<T> implements Serializable, List<T>
                permits FinalTest, UnitTest, Test, Quiz, PopQuiz
            {}
            """);
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = accessMod.getLocation();
        ASTClassDeclaration node = parser.parseClassDeclaration(loc, annList, accessMod, genModList);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 3);
        assertTrue(node.getAccessMod().isPresent());
        checkList(node.getClassModifierList(), CLASS_MODIFIERS, ASTKeywordNode.class, 1);
        assertEquals("FullTest", node.getName().getValue());
        assertTrue(node.getTypeParams().isPresent());
        assertTrue(node.getSuperclass().isPresent());
        checkList(node.getSuperclass().get(), SIMPLE_TYPES, ASTSimpleType.class, 1);
        assertTrue(node.getSuperinterfaces().isPresent());
        checkList(node.getSuperinterfaces().get(), DATA_TYPES_NO_ARRAY, ASTDataTypeNoArray.class, 2);
        assertTrue(node.getPermits().isPresent());
        checkList(node.getPermits().get(), DATA_TYPES_NO_ARRAY, ASTDataTypeNoArray.class, 5);
        checkList(node.getClassParts(), CLASS_PARTS, ASTClassPart.class, 0);
    }

    /**
     * Test final class.
     */
    @Test
    public void testClassFinal() {
        ClassesParser parser = getClassesParser("final class FinalClass {}");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTClassDeclaration node = parser.parseClassDeclaration(loc, annList,null, genModList);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 0);
        assertFalse(node.getAccessMod().isPresent());
        checkList(node.getClassModifierList(), CLASS_MODIFIERS, ASTKeywordNode.class, 1);
        assertEquals("FinalClass", node.getName().getValue());
        assertFalse(node.getTypeParams().isPresent());
        assertFalse(node.getSuperclass().isPresent());
        assertFalse(node.getSuperinterfaces().isPresent());
        assertFalse(node.getPermits().isPresent());
        checkList(node.getClassParts(), CLASS_PARTS, ASTClassPart.class, 0);
    }

    /**
     * Tests bad class declaration of bad class modifier.
     */
    @Test
    public void testClassDeclarationBadClassMod() {
        ClassesParser parser = getClassesParser("""
            public constant class BadClassMod {
            }
            """);
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = accessMod.getLocation();
        ASTClassDeclaration node = parser.parseClassDeclaration(loc, annList, accessMod, genModList);
        expectError(node, parser);
    }

    /**
     * Tests permits (permits clause).
     */
    @Test
    public void testPermits() {
        ClassesParser parser = getClassesParser("permits Dog, Cat, Mouse");
        ASTDataTypeNoArrayList node = parser.parsePermits();
        ensureNoErrors(node, parser);
        checkList(node, DATA_TYPES_NO_ARRAY, ASTDataTypeNoArray.class, 3);
    }

    /**
     * Tests superinterfaces (implements clause).
     */
    @Test
    public void testSuperinterfaces() {
        ClassesParser parser = getClassesParser("implements Copyable");
        ASTDataTypeNoArrayList node = parser.parseSuperinterfaces();
        ensureNoErrors(node, parser);
        checkList(node, DATA_TYPES_NO_ARRAY, ASTDataTypeNoArray.class, 1);
    }

    /**
     * Tests superclass (extends clause).
     */
    @Test
    public void testSuperclass() {
        ClassesParser parser = getClassesParser("extends Thread");
        ASTDataTypeNoArray node = parser.parseSuperclass();
        ensureNoErrors(node, parser);
        checkList(node, SIMPLE_TYPES, ASTSimpleType.class, 1);
    }

    /**
     * Tests empty class body.
     */
    @Test
    public void testClassBodyEmpty() {
        ClassesParser parser = getClassesParser("{}");
        ASTClassPartList node = parser.parseClassBody();
        ensureNoErrors(node, parser);
        checkList(node, CLASS_PARTS, ASTClassPart.class, 0);
    }

    /**
     * Tests class body.
     */
    @Test
    public void testClassBody() {
        ClassesParser parser = getClassesParser("""
                {
                    private Integer i = 1;
                    constructor(Integer i) { self.i = i; }
                    Integer getI() {
                        return i;
                    }
                }
                """);
        ASTClassPartList node = parser.parseClassBody();
        ensureNoErrors(node, parser);
        checkList(node, CLASS_PARTS, ASTClassPart.class, 3);
    }

    /**
     * Tests class part list of all possible class parts.
     */
    @Test
    public void testClassPartListComprehensive() {
        ClassesParser parser = getClassesParser("""
                protected String foo;
                shared constructor() {}
                constructor(String foo) { self.foo = foo; }
                constructor { a++; }
                @Test public String getFoo() {
                    return foo;
                }
                class Nested {}
                enum TrafficLight {RED, YELLOW, GREEN}
                interface Helper {}
                annotation InnerAnnotation {}
                record FooRecord(String bar) {}
                adt Foo {
                    Boo() {},
                    Goo() {}
                }
                """);
        ASTClassPartList node = parser.parseClassPartList();
        ensureNoErrors(node, parser);
        checkList(node, CLASS_PARTS, ASTClassPart.class, 11);
    }

    /**
     * Tests class part list of class part.
     */
    @Test
    public void testClassPartListOfClassPart() {
        ClassesParser parser = getClassesParser("private Integer i = 1;");
        ASTClassPartList node = parser.parseClassPartList();
        ensureNoErrors(node, parser);
        checkList(node, CLASS_PARTS, ASTClassPart.class, 1);
    }

    /**
     * Tests class part list.
     */
    @Test
    public void testClassPartList() {
        ClassesParser parser = getClassesParser("""
            private Integer i = 1;
            constructor(Integer i) { self.i = i; }
            """);
        ASTClassPartList node = parser.parseClassPartList();
        ensureNoErrors(node, parser);
        checkList(node, CLASS_PARTS, ASTClassPart.class, 2);
    }

    /**
     * Tests nested class part lists of multiple class parts.
     */
    @Test
    public void testClassPartListMultiple() {
        ClassesParser parser = getClassesParser("""
            private Integer i = 1;
            constructor(Integer i) { self.i = i; }
            Integer getI() {
                return i;
            }
            """);
        ASTClassPartList node = parser.parseClassPartList();
        ensureNoErrors(node, parser);
        checkList(node, CLASS_PARTS, ASTClassPart.class, 3);
    }

    /**
     * Tests class part of shared constructor.
     */
    @Test
    public void testClassPartOfSharedConstructor() {
        ClassesParser parser = getClassesParser("shared constructor() { sharedVar = reallyComplicatedLogic(); }");
        ASTClassPart node = parser.parseClassPart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTSharedConstructor.class, node);
    }

    /**
     * Tests class part of method declaration with void result.
     */
    @Test
    public void testClassPartOfMethodDeclarationVoidResult() {
        ClassesParser parser = getClassesParser("public abstract void method();");
        ASTClassPart node = parser.parseClassPart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTMethodDeclaration.class, node);
    }

    /**
     * Tests class part of method declaration with void result and type parameters.
     */
    @Test
    public void testClassPartOfMethodDeclarationVoidResultTypeParameters() {
        ClassesParser parser = getClassesParser("public abstract <T> void method(T param);");
        ASTClassPart node = parser.parseClassPart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTMethodDeclaration.class, node);
    }

    /**
     * Tests class part of method declaration data type void result.
     */
    @Test
    public void testClassPartOfMethodDeclarationDataTypeResult() {
        ClassesParser parser = getClassesParser("public abstract String method();");
        ASTClassPart node = parser.parseClassPart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTMethodDeclaration.class, node);
    }

    /**
     * Tests class part of method declaration with data type result and type parameters.
     */
    @Test
    public void testClassPartOfMethodDeclarationDataTypeResultTypeParameters() {
        ClassesParser parser = getClassesParser("public abstract <T> T method(T param);");
        ASTClassPart node = parser.parseClassPart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTMethodDeclaration.class, node);
    }

    /**
     * Tests class part of method declaration with mut result.
     */
    @Test
    public void testClassPartOfMethodDeclarationMutResult() {
        ClassesParser parser = getClassesParser("mut String method(String param);");
        ASTClassPart node = parser.parseClassPart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTMethodDeclaration.class, node);
    }

    /**
     * Tests class part of method declaration with mut result.
     */
    @Test
    public void testClassPartOfMethodDeclarationMutResultShared() {
        ClassesParser parser = getClassesParser("shared mut String method(String param);");
        ASTClassPart node = parser.parseClassPart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTMethodDeclaration.class, node);
    }

    /**
     * Tests class part of field declaration.
     */
    @Test
    public void testClassPartOfFieldDeclaration() {
        ClassesParser parser = getClassesParser("private Int myVar = 1, myVar2 = 2;");
        ASTClassPart node = parser.parseClassPart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTFieldDeclaration.class, node);
    }

    /**
     * Tests class part of constructor declaration.
     */
    @Test
    public void testClassPartOfConstructorDeclaration() {
        ClassesParser parser = getClassesParser("constructor(String s) : constructor(s) {}");
        ASTClassPart node = parser.parseClassPart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTConstructorDeclaration.class, node);
    }

    /**
     * Tests class part of class declaration.
     */
    @Test
    public void testClassPartOfClassDeclaration() {
        ClassesParser parser = getClassesParser("public shared class Nested {}");
        ASTClassPart node = parser.parseClassPart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTClassDeclaration.class, node);
    }

    /**
     * Tests class part of enum declaration.
     */
    @Test
    public void testClassPartOfEnumDeclaration() {
        ClassesParser parser = getClassesParser("private enum Light {RED, YELLOW, GREEN}");
        ASTClassPart node = parser.parseClassPart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTEnumDeclaration.class, node);
    }

    /**
     * Tests class part of interface declaration.
     */
    @Test
    public void testClassPartOfInterfaceDeclaration() {
        ClassesParser parser = getClassesParser("private interface TrafficLight { Light getStatus(); }");
        ASTClassPart node = parser.parseClassPart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTInterfaceDeclaration.class, node);
    }

    /**
     * Tests class part of annotation declaration.
     */
    @Test
    public void testClassPartOfAnnotationDeclaration() {
        ClassesParser parser = getClassesParser("""
                public annotation Test {
                    String getStatus() default "SUCCESS";
                }
                """);
        ASTClassPart node = parser.parseClassPart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTAnnotationDeclaration.class, node);
    }

    /**
     * Tests class part of record declaration.
     */
    @Test
    public void testClassPartOfRecordDeclaration() {
        ClassesParser parser = getClassesParser("""
                public record LineItem(Order order, Int lineNbr, Product p, Int qty) {
                    Double getSubtotal() {
                        return p.getUnitPrice() * qty;
                    }
                }
                """);
        ASTClassPart node = parser.parseClassPart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTRecordDeclaration.class, node);
    }

    /**
     * Tests class part of adt declaration.
     */
    @Test
    public void testClassPartOfAdtDeclaration() {
        ClassesParser parser = getClassesParser("""
                public adt Optional<T> {
                    None() {},
                    Some(T value) {
                        T getValue() {
                            return value();
                        }
                    }
                }
                """);
        ASTClassPart node = parser.parseClassPart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTAdtDeclaration.class, node);
    }

    /**
     * Tests bad class part of constructor with type parameters but a general modifier.
     */
    @Test
    public void testClassPartBadConstructorTypeParamsGeneralModifier() {
        ClassesParser parser = getClassesParser("""
                public override <T> constructor() {
                }
                """);
        ASTClassPart node = parser.parseClassPart();
        expectError(node, parser);
    }

    /**
     * Tests bad class part of constructor but with a general modifier.
     */
    @Test
    public void testClassPartBadConstructorGeneralModifier() {
        ClassesParser parser = getClassesParser("""
                public override constructor() {
                }
                """);
        ASTClassPart node = parser.parseClassPart();
        expectError(node, parser);
    }

    /**
     * Tests shared constructor.
     */
    @Test
    public void testSharedConstructor() {
        ClassesParser parser = getClassesParser("shared constructor() { sharedVar = reallyComplicatedLogic(); }");
        ASTSharedConstructor node = parser.parseSharedConstructor();
        ensureNoErrors(node, parser);
        assertNotNull(node.getBlock());
    }

    /**
     * Tests bad shared constructor of no open parenthesis.
     */
    @Test
    public void testSharedConstructorNoOpenParen() {
        ClassesParser parser = getClassesParser("shared constructor ) { sharedVar = reallyComplicatedLogic(); }");
        ASTSharedConstructor node = parser.parseSharedConstructor();
        expectError(node, parser);
    }

    /**
     * Tests bad shared constructor of no close parenthesis.
     */
    @Test
    public void testSharedConstructorNoCloseParen() {
        ClassesParser parser = getClassesParser("shared constructor( { sharedVar = reallyComplicatedLogic(); }");
        ASTSharedConstructor node = parser.parseSharedConstructor();
        expectError(node, parser);
    }

    /**
     * Tests constructor declaration of access modifier, and constructor invocation.
     */
    @Test
    public void testConstructorDeclarationOfAccessConstructorInvocation() {
        ClassesParser parser = getClassesParser("private constructor(String s) : super(s) { self.s = s; }");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        Location loc = accessMod.getLocation();
        ASTConstructorDeclaration node = parser.parseConstructorDeclaration(loc, annList, accessMod);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 0);
        assertTrue(node.getAccessMod().isPresent());
        assertNotNull(node.getConstructorDecl());
        assertTrue(node.getConstructorInvocation().isPresent());
        assertNotNull(node.getBlock());
    }

    /**
     * Tests simple constructor declaration.
     */
    @Test
    public void testConstructorDeclarationSimple() {
        ClassesParser parser = getClassesParser("constructor(String s) { self.s = s; }");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTConstructorDeclaration node = parser.parseConstructorDeclaration(loc, annList, null);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 0);
        assertFalse(node.getAccessMod().isPresent());
        assertNotNull(node.getConstructorDecl());
        assertFalse(node.getConstructorInvocation().isPresent());
        assertNotNull(node.getBlock());
    }

    /**
     * Tests simple constructor declaration with annotation.
     */
    @Test
    public void testConstructorDeclarationAnnotation() {
        ClassesParser parser = getClassesParser("@Foo constructor(String s) { self.s = s; }");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTConstructorDeclaration node = parser.parseConstructorDeclaration(loc, annList, null);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 1);
        assertFalse(node.getAccessMod().isPresent());
        assertNotNull(node.getConstructorDecl());
        assertFalse(node.getConstructorInvocation().isPresent());
        assertNotNull(node.getBlock());
    }

    /**
     * Tests constructor invocation of super and type arguments.
     */
    @Test
    public void testConstructorInvocationOfSuperTypeArguments() {
        ClassesParser parser = getClassesParser(": <Integer>super(5)");
        ASTConstructorInvocation node = parser.parseConstructorInvocation();
        ensureNoErrors(node, parser);

        assertTrue(node.getTypeArgs().isPresent());
        assertEquals(SUPER, node.getConstructorKeyword().getKeyword());
        checkList(node.getArgsList(), ARGUMENTS, ASTGiveExpression.class, 1);
    }

    /**
     * Tests constructor invocation of constructor and type arguments.
     */
    @Test
    public void testConstructorInvocationOfConstructorTypeArguments() {
        ClassesParser parser = getClassesParser(": <Integer>constructor()");
        ASTConstructorInvocation node = parser.parseConstructorInvocation();
        ensureNoErrors(node, parser);

        assertTrue(node.getTypeArgs().isPresent());
        assertEquals(CONSTRUCTOR, node.getConstructorKeyword().getKeyword());
        checkList(node.getArgsList(), ARGUMENTS, ASTGiveExpression.class, 0);
    }

    /**
     * Tests simple constructor invocation of constructor.
     */
    @Test
    public void testConstructorInvocationOfConstructorSimple() {
        ClassesParser parser = getClassesParser(": constructor()");
        ASTConstructorInvocation node = parser.parseConstructorInvocation();
        ensureNoErrors(node, parser);

        assertFalse(node.getTypeArgs().isPresent());
        assertEquals(CONSTRUCTOR, node.getConstructorKeyword().getKeyword());
        checkList(node.getArgsList(), ARGUMENTS, ASTGiveExpression.class, 0);
    }

    /**
     * Tests bad constructor invocation of not constructor or super.
     */
    @Test
    public void testConstructorInvocationNotConstructorOrSuper() {
        ClassesParser parser = getClassesParser(": class()");
        ASTConstructorInvocation node = parser.parseConstructorInvocation();
        expectError(node, parser);
    }

    /**
     * Tests bad constructor invocation of no open parenthesis.
     */
    @Test
    public void testConstructorInvocationNoOpenParen() {
        ClassesParser parser = getClassesParser(": constructor String str)");
        ASTConstructorInvocation node = parser.parseConstructorInvocation();
        expectError(node, parser, 2);
    }

    /**
     * Tests bad constructor invocation of no close parenthesis.
     */
    @Test
    public void testConstructorInvocationNoCloseParen() {
        ClassesParser parser = getClassesParser(": constructor(String str {");
        ASTConstructorInvocation node = parser.parseConstructorInvocation();
        expectError(node, parser);
    }

    /**
     * Tests simple constructor declarator.
     */
    @Test
    public void testConstructorDeclaratorSimple() {
        ClassesParser parser = getClassesParser("constructor()");
        ASTConstructorDeclarator node = parser.parseConstructorDeclarator();
        ensureNoErrors(node, parser);

        assertFalse(node.getTypeParams().isPresent());
        checkList(node.getFormalParamList(), FORMAL_PARAMETERS, ASTFormalParameter.class, 0);
    }

    /**
     * Tests full constructor declarator.
     */
    @Test
    public void testConstructorDeclaratorFull() {
        ClassesParser parser = getClassesParser("<T> constructor(T param)");
        ASTConstructorDeclarator node = parser.parseConstructorDeclarator();
        ensureNoErrors(node, parser);

        assertTrue(node.getTypeParams().isPresent());
        checkList(node.getFormalParamList(), FORMAL_PARAMETERS, ASTFormalParameter.class, 1);
    }

    /**
     * Tests constructor declarator of no open parenthesis.
     */
    @Test
    public void testConstructorDeclaratorNoOpenParen() {
        ClassesParser parser = getClassesParser("constructor Integer param)");
        ASTConstructorDeclarator node = parser.parseConstructorDeclarator();
        expectError(node, parser);
    }

    /**
     * Tests constructor declarator of no close parenthesis.
     */
    @Test
    public void testConstructorDeclaratorNoCloseParen() {
        ClassesParser parser = getClassesParser("constructor(String param {");
        ASTConstructorDeclarator node = parser.parseConstructorDeclarator();
        expectError(node, parser);
    }

    /**
     * Tests full field declaration.
     */
    @Test
    public void testFieldDeclaration() {
        ClassesParser parser = getClassesParser("public constant String aConstant = \"CONSTANT\";");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = accessMod.getLocation();
        ASTVariableModifierList varModList = parser.getStatementsParser().parseVariableModifierList();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTFieldDeclaration node = parser.parseFieldDeclaration(loc, annList, accessMod, genModList, varModList, dt);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 0);
        assertTrue(node.getAccessMod().isPresent());
        checkList(node.getFieldModList(), FIELD_MODIFIERS, ASTKeywordNode.class, 1);
        assertNotNull(node.getDataType());
        checkList(node.getVarDeclList(), VARIABLE_DECLARATORS, ASTVariableDeclarator.class, 1);
    }

    /**
     * Tests constant field declaration.
     */
    @Test
    public void testFieldDeclarationOfConstant() {
        ClassesParser parser = getClassesParser("public constant String aConstant = \"CONSTANT\";");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = accessMod.getLocation();
        ASTVariableModifierList varModList = parser.getStatementsParser().parseVariableModifierList();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTFieldDeclaration node = parser.parseFieldDeclaration(loc, annList, accessMod, genModList, varModList, dt);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 0);
        assertTrue(node.getAccessMod().isPresent());
        checkList(node.getFieldModList(), FIELD_MODIFIERS, ASTKeywordNode.class, 1);
        checkList(node.getVarModList(), VARIABLE_MODIFIERS, ASTKeywordNode.class, 0);
        assertNotNull(node.getDataType());
        checkList(node.getVarDeclList(), VARIABLE_DECLARATORS, ASTVariableDeclarator.class, 1);
    }

    /**
     * Tests field declaration with variable modifiers.
     */
    @Test
    public void testFieldDeclarationVariableModifiers() {
        ClassesParser parser = getClassesParser("mut String name = \"spruce\";");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTVariableModifierList varModList = parser.getStatementsParser().parseVariableModifierList();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTFieldDeclaration node = parser.parseFieldDeclaration(loc, annList,null, genModList, varModList, dt);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 0);
        assertFalse(node.getAccessMod().isPresent());
        checkList(node.getFieldModList(), FIELD_MODIFIERS, ASTKeywordNode.class, 0);
        checkList(node.getVarModList(), VARIABLE_MODIFIERS, ASTKeywordNode.class, 1);
        assertNotNull(node.getDataType());
        checkList(node.getVarDeclList(), VARIABLE_DECLARATORS, ASTVariableDeclarator.class, 1);
    }

    /**
     * Tests simple field declaration.
     */
    @Test
    public void testFieldDeclarationSimple() {
        ClassesParser parser = getClassesParser("String name = \"spruce\";");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTVariableModifierList varModList = parser.getStatementsParser().parseVariableModifierList();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTFieldDeclaration node = parser.parseFieldDeclaration(loc, annList, null, genModList, varModList, dt);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 0);
        assertFalse(node.getAccessMod().isPresent());
        checkList(node.getFieldModList(), FIELD_MODIFIERS, ASTKeywordNode.class, 0);
        checkList(node.getVarModList(), VARIABLE_MODIFIERS, ASTKeywordNode.class, 0);
        assertNotNull(node.getDataType());
        checkList(node.getVarDeclList(), VARIABLE_DECLARATORS, ASTVariableDeclarator.class, 1);
    }

    /**
     * Tests field declaration with Annotation.
     */
    @Test
    public void testFieldDeclarationAnnotation() {
        ClassesParser parser = getClassesParser("@Test String name = \"spruce\";");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTVariableModifierList varModList = parser.getStatementsParser().parseVariableModifierList();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTFieldDeclaration node = parser.parseFieldDeclaration(loc, annList, null, genModList, varModList, dt);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 1);
        assertFalse(node.getAccessMod().isPresent());
        checkList(node.getFieldModList(), FIELD_MODIFIERS, ASTKeywordNode.class, 0);
        checkList(node.getVarModList(), VARIABLE_MODIFIERS, ASTKeywordNode.class, 0);
        assertNotNull(node.getDataType());
        checkList(node.getVarDeclList(), VARIABLE_DECLARATORS, ASTVariableDeclarator.class, 1);
    }

    /**
     * Tests bad field declaration of bad field modifier.
     */
    @Test
    public void testFieldDeclarationBadModifier() {
        ClassesParser parser = getClassesParser("abstract String name = \"bad\";");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        ASTVariableModifierList varModList = parser.getStatementsParser().parseVariableModifierList();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        Location loc = genModList.getLocation();
        ASTFieldDeclaration node = parser.parseFieldDeclaration(loc, annList,null, genModList,
                        varModList, dt);
        expectError(node, parser);
    }

    /**
     * Tests bad field declaration of no semicolon.
     */
    @Test
    public void testFieldDeclarationNoSemicolon() {
        ClassesParser parser = getClassesParser("shared String name = \"bad\"}");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        ASTVariableModifierList varModList = parser.getStatementsParser().parseVariableModifierList();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        Location loc = genModList.getLocation();
        ASTFieldDeclaration node = parser.parseFieldDeclaration(loc, annList,null, genModList,
                varModList, dt);
        expectError(node, parser);
    }

    /**
     * Tests simple method declaration.
     */
    @Test
    public void testMethodDeclarationSimple() {
        ClassesParser parser = getClassesParser("""
                String toString() {
                    return self;
                }
                """);
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTVariableModifierList varModList = parser.getStatementsParser().parseVariableModifierList();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTMethodDeclaration node = parser.parseMethodDeclaration(loc, annList,null, genModList, varModList, dt);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 0);
        assertFalse(node.getAccessMod().isPresent());
        checkList(node.getMethodModList(), METHOD_MODIFIERS, ASTKeywordNode.class, 0);
        assertNotNull(node.getHeader());
        assertNotNull(node.getBody());
    }

    /**
     * Tests method declaration with access modifier and method modifier.
     */
    @Test
    public void testMethodDeclarationAccessModifierMethodModifier() {
        ClassesParser parser = getClassesParser("public abstract Foo abstractMethod();");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = accessMod.getLocation();
        ASTVariableModifierList varModList = parser.getStatementsParser().parseVariableModifierList();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTMethodDeclaration node = parser.parseMethodDeclaration(loc, annList, accessMod, genModList, varModList, dt);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 0);
        assertTrue(node.getAccessMod().isPresent());
        checkList(node.getMethodModList(), METHOD_MODIFIERS, ASTKeywordNode.class, 1);
        assertNotNull(node.getHeader());
        assertNotNull(node.getBody());
    }

    /**
     * Tests bad method declaration of bad modifier.
     */
    @Test
    public void testMethodDeclarationBadModifier() {
        ClassesParser parser = getClassesParser("public volatile Foo abstractMethod();");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = accessMod.getLocation();
        ASTMethodDeclaration node = parser.parseMethodDeclaration(loc, annList, accessMod, genModList);
        expectError(node, parser);
    }

    /**
     * Tests method declaration with annotation.
     */
    @Test
    public void testMethodDeclarationAnnotation() {
        ClassesParser parser = getClassesParser("@Bar public abstract Foo abstractMethod();");
        ASTAnnotationList annList = parser.parseAnnotationList();
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = accessMod.getLocation();
        ASTVariableModifierList varModList = parser.getStatementsParser().parseVariableModifierList();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTMethodDeclaration node = parser.parseMethodDeclaration(loc, annList, accessMod, genModList, varModList, dt);
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 1);
        assertTrue(node.getAccessMod().isPresent());
        checkList(node.getMethodModList(), METHOD_MODIFIERS, ASTKeywordNode.class, 1);
        assertNotNull(node.getHeader());
        assertNotNull(node.getBody());
    }

    /**
     * Tests method body of semicolon.
     */
    @Test
    public void testMethodBodyOfSemicolon() {
        ClassesParser parser = getClassesParser(";");
        ASTMethodBody node = parser.parseMethodBody();
        ensureNoErrors(node, parser);
        assertFalse(node.getBlock().isPresent());
    }

    /**
     * Tests method body of block.
     */
    @Test
    public void testMethodBodyOfBlock() {
        ClassesParser parser = getClassesParser("{\n    stdout.println(\"Body!\");\n}");
        ASTMethodBody node = parser.parseMethodBody();
        ensureNoErrors(node, parser);
        assertTrue(node.getBlock().isPresent());
    }

    /**
     * Tests bad method body of block or semicolon.
     */
    @Test
    public void testMethodBodyOfNoBlockOrSemicolon() {
        ClassesParser parser = getClassesParser("public");
        ASTMethodBody node = parser.parseMethodBody();
        expectError(node, parser, 7);
    }

    /**
     * Tests method modifier of public.
     */
    @Test
    public void testAccessModifierOfPublic() {
        ClassesParser parser = getClassesParser("public");
        ASTKeywordNode node = parser.parseAccessModifier();
        ensureNoErrors(node, parser);
        assertEquals(PUBLIC, node.getKeyword());
    }

    /**
     * Tests method modifier of protected.
     */
    @Test
    public void testAccessModifierOfProtected() {
        ClassesParser parser = getClassesParser("protected");
        ASTKeywordNode node = parser.parseAccessModifier();
        ensureNoErrors(node, parser);
        assertEquals(PROTECTED, node.getKeyword());
    }

    /**
     * Tests method modifier of abstract.
     */
    @Test
    public void testAccessModifierOfInternal() {
        ClassesParser parser = getClassesParser("internal");
        ASTKeywordNode node = parser.parseAccessModifier();
        ensureNoErrors(node, parser);
        assertEquals(INTERNAL, node.getKeyword());
    }

    /**
     * Tests access modifier of private.
     */
    @Test
    public void testAccessModifierOfPrivate() {
        ClassesParser parser = getClassesParser("private");
        ASTKeywordNode node = parser.parseAccessModifier();
        ensureNoErrors(node, parser);
        assertEquals(PRIVATE, node.getKeyword());
    }

    /**
     * Tests general modifier list of class modifiers.
     */
    @Test
    public void testGeneralModifierListOfClassModifiers() {
        ClassesParser parser = getClassesParser("abstract final sealed shared");
        ASTGeneralModifierList node = parser.parseGeneralModifierList();
        ensureNoErrors(node, parser);
        checkList(node, GENERAL_MODIFIERS, ASTKeywordNode.class, 4);
    }

    /**
     * Tests general modifier list of interface modifiers.
     */
    @Test
    public void testGeneralModifierListOfInterfaceModifiers() {
        ClassesParser parser = getClassesParser("abstract final sealed shared");
        ASTGeneralModifierList node = parser.parseGeneralModifierList();
        ensureNoErrors(node, parser);
        checkList(node, GENERAL_MODIFIERS, ASTKeywordNode.class, 4);
    }

    /**
     * Tests general modifier list of method modifiers.
     */
    @Test
    public void testGeneralModifierListOfMethodModifiers() {
        ClassesParser parser = getClassesParser("final abstract override shared");
        ASTGeneralModifierList node = parser.parseGeneralModifierList();
        ensureNoErrors(node, parser);
        checkList(node, GENERAL_MODIFIERS, ASTKeywordNode.class, 4);
    }

    /**
     * Tests general modifier of abstract.
     */
    @Test
    public void testGeneralModifierOfAbstract() {
        ClassesParser parser = getClassesParser("abstract");
        ASTKeywordNode node = parser.parseGeneralModifier();
        ensureNoErrors(node, parser);
        assertEquals(ABSTRACT, node.getKeyword());
    }

    /**
     * Tests general modifier of constant.
     */
    @Test
    public void testGeneralModifierOfConstant() {
        ClassesParser parser = getClassesParser("constant");
        ASTKeywordNode node = parser.parseGeneralModifier();
        ensureNoErrors(node, parser);
        assertEquals(CONSTANT, node.getKeyword());
    }

    /**
     * Tests general modifier of default.
     */
    @Test
    public void testGeneralModifierOfDefault() {
        ClassesParser parser = getClassesParser("default");
        ASTKeywordNode node = parser.parseGeneralModifier();
        ensureNoErrors(node, parser);
        assertEquals(DEFAULT, node.getKeyword());
    }

    /**
     * Tests general modifier of final.
     */
    @Test
    public void testGeneralModifierOfFinal() {
        ClassesParser parser = getClassesParser("final");
        ASTKeywordNode node = parser.parseGeneralModifier();
        ensureNoErrors(node, parser);
        assertEquals(FINAL, node.getKeyword());
    }

    /**
     * Tests general modifier of override.
     */
    @Test
    public void testGeneralModifierOfOverride() {
        ClassesParser parser = getClassesParser("override");
        ASTKeywordNode node = parser.parseGeneralModifier();
        ensureNoErrors(node, parser);
        assertEquals(OVERRIDE, node.getKeyword());
    }

    /**
     * Tests general modifier of sealed.
     */
    @Test
    public void testGeneralModifierOfSealed() {
        ClassesParser parser = getClassesParser("sealed");
        ASTKeywordNode node = parser.parseGeneralModifier();
        ensureNoErrors(node, parser);
        assertEquals(SEALED, node.getKeyword());
    }

    /**
     * Tests general modifier of shared.
     */
    @Test
    public void testGeneralModifierOfShared() {
        ClassesParser parser = getClassesParser("shared");
        ASTKeywordNode node = parser.parseGeneralModifier();
        ensureNoErrors(node, parser);
        assertEquals(SHARED, node.getKeyword());
    }

    /**
     * Tests general modifier of volatile.
     */
    @Test
    public void testGeneralModifierOfVolatile() {
        ClassesParser parser = getClassesParser("volatile");
        ASTKeywordNode node = parser.parseGeneralModifier();
        ensureNoErrors(node, parser);
        assertEquals(VOLATILE, node.getKeyword());
    }

    /**
     * Tests simple method header.
     */
    @Test
    public void testMethodHeaderSimple() {
        ClassesParser parser = getClassesParser("void toString() const");
        ASTMethodHeader node = parser.parseMethodHeader();
        ensureNoErrors(node, parser);

        assertFalse(node.getTypeParams().isPresent());
        assertNotNull(node.getResult());
        assertNotNull(node.getMethodDecl());
    }

    /**
     * Tests method header with type parameters.
     */
    @Test
    public void testMethodHeaderOfTypeParameters() {
        ClassesParser parser = getClassesParser("<T> T getItem() const");
        ASTMethodHeader node = parser.parseMethodHeader();
        ensureNoErrors(node, parser);

        assertTrue(node.getTypeParams().isPresent());
        assertNotNull(node.getResult());
        assertNotNull(node.getMethodDecl());
    }

    /**
     * Tests result of void.
     */
    @Test
    public void testResultOfVoid() {
        ClassesParser parser = getClassesParser("void");
        ASTResult node = parser.parseResult();
        ensureNoErrors(node, parser);

        assertFalse(node.getMutMod().isPresent());
        assertFalse(node.getDataType().isPresent());
        assertTrue(node.getVoidKeyword().isPresent());
    }

    /**
     * Tests result of data type.
     */
    @Test
    public void testResultOfDataType() {
        ClassesParser parser = getClassesParser("Map<String, Integer>");
        ASTResult node = parser.parseResult();
        ensureNoErrors(node, parser);

        assertFalse(node.getMutMod().isPresent());
        assertTrue(node.getDataType().isPresent());
        assertFalse(node.getVoidKeyword().isPresent());
    }

    /**
     * Tests result of mut modifier and data type.
     */
    @Test
    public void testResultOfMutModifierDataType() {
        ClassesParser parser = getClassesParser("mut Map<String, Integer>");
        ASTResult node = parser.parseResult();
        ensureNoErrors(node, parser);

        assertTrue(node.getMutMod().isPresent());
        assertTrue(node.getDataType().isPresent());
        assertFalse(node.getVoidKeyword().isPresent());
    }

    /**
     * Tests simple method declarator.
     */
    @Test
    public void testMethodDeclaratorSimple() {
        ClassesParser parser = getClassesParser("update()");
        ASTMethodDeclarator node = parser.parseMethodDeclarator();
        ensureNoErrors(node, parser);

        assertEquals("update", node.getName().getValue());
        checkList(node.getFormalParamList(), FORMAL_PARAMETERS, ASTFormalParameter.class, 0);
        assertFalse(node.getMutModifier().isPresent());
    }

    /**
     * Tests method declarator of parameter list and mut modifier.
     */
    @Test
    public void testMethodDeclaratorOfParameterListConstModifier() {
        ClassesParser parser = getClassesParser("join(String sep) mut)");
        ASTMethodDeclarator node = parser.parseMethodDeclarator();
        ensureNoErrors(node, parser);

        assertEquals("join", node.getName().getValue());
        checkList(node.getFormalParamList(), FORMAL_PARAMETERS, ASTFormalParameter.class, 1);
        assertTrue(node.getMutModifier().isPresent());
    }

    /**
     * Tests bad method declarator of no identifier.
     */
    @Test
    public void testMethodDeclaratorNoIdentifier() {
        ClassesParser parser = getClassesParser("(String sep) mut)");
        ASTMethodDeclarator node = parser.parseMethodDeclarator();
        expectError(node, parser);
    }

    /**
     * Tests bad method declarator of no open parenthesis.
     */
    @Test
    public void testMethodDeclaratorNoOpenParen() {
        ClassesParser parser = getClassesParser("test String sep) mut)");
        ASTMethodDeclarator node = parser.parseMethodDeclarator();
        expectError(node, parser);
    }

    /**
     * Tests bad method declarator of no close parenthesis.
     */
    @Test
    public void testMethodDeclaratorNoCloseParen() {
        ClassesParser parser = getClassesParser("test(String sep  mut)");
        ASTMethodDeclarator node = parser.parseMethodDeclarator();
        expectError(node, parser);
    }

    /**
     * Tests mut modifier by itself.
     */
    @Test
    public void testMutModifier() {
        ClassesParser parser = getClassesParser("mut");
        ASTKeywordNode node = parser.parseMutModifier();
        ensureNoErrors(node, parser);
        assertEquals(MUT, node.getKeyword());
    }

    /**
     * Tests formal parameter list of formal parameter.
     */
    @Test
    public void testFormalParameterListOfFormalParameter() {
        ClassesParser parser = getClassesParser("const Int a");
        ASTFormalParameterList node = parser.parseFormalParameterList();
        ensureNoErrors(node, parser);
        checkList(node, FORMAL_PARAMETERS, ASTFormalParameter.class, 1);
    }

    /**
     * Tests formal parameter list.
     */
    @Test
    public void testFormalParameterList() {
        ClassesParser parser = getClassesParser("String msg, Foo f, Bar b");
        ASTFormalParameterList node = parser.parseFormalParameterList();
        ensureNoErrors(node, parser);
        checkList(node, FORMAL_PARAMETERS, ASTFormalParameter.class, 3);
    }

    /**
     * Tests formal parameter list with varargs parameter list.
     */
    @Test
    public void testFormalParameterListOfLastVarargs() {
        ClassesParser parser = getClassesParser("Point pt, Double... coordinates");
        ASTFormalParameterList node = parser.parseFormalParameterList();
        ensureNoErrors(node, parser);
        checkList(node, FORMAL_PARAMETERS, ASTFormalParameter.class, 2);
    }

    /**
     * Tests bad formal parameter list if varargs not last, compiler error.
     */
    @Test
    public void testFormalParameterListVarargsNotLastError() {
        ClassesParser parser = getClassesParser("Double... coordinates, Point pt");
        ASTFormalParameterList node = parser.parseFormalParameterList();
        expectError(node, parser);
    }

    /**
     * Tests formal parameter list of formal parameters of take and of annotation.
     */
    @Test
    public void testFormalParameterListTakeAnnotation() {
        ClassesParser parser = getClassesParser("take String foo, @Baz Integer bar");
        ASTFormalParameterList node = parser.parseFormalParameterList();
        ensureNoErrors(node, parser);
        checkList(node, FORMAL_PARAMETERS, ASTFormalParameter.class, 2);
    }

    /**
     * Tests formal parameter, no variable modifier list, with ellipsis.
     */
    @Test
    public void testFormalParameterNoVMLEllipsis() {
        ClassesParser parser = getClassesParser("String... args");
        ASTFormalParameter node = parser.parseFormalParameter();
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 0);
        assertFalse(node.getTakeMod().isPresent());
        checkList(node.getVarModList(), VARIABLE_MODIFIERS, ASTKeywordNode.class, 0);
        assertNotNull(node.getDataType());
        assertTrue(node.getEllipsisMod().isPresent());
        assertEquals("args", node.getName().getValue());
    }

    /**
     * Tests formal parameter, variable modifier list, with ellipsis.
     */
    @Test
    public void testFormalParameterOfVMLEllipsis() {
        ClassesParser parser = getClassesParser("mut String... args");
        ASTFormalParameter node = parser.parseFormalParameter();
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 0);
        assertFalse(node.getTakeMod().isPresent());
        checkList(node.getVarModList(), VARIABLE_MODIFIERS, ASTKeywordNode.class, 1);
        assertNotNull(node.getDataType());
        assertTrue(node.getEllipsisMod().isPresent());
        assertEquals("args", node.getName().getValue());
    }

    /**
     * Tests formal parameter, no variable modifier list, no ellipsis.
     */
    @Test
    public void testFormalParameterNoVMLNoEllipsis() {
        ClassesParser parser = getClassesParser("String[] args");
        ASTFormalParameter node = parser.parseFormalParameter();
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 0);
        assertFalse(node.getTakeMod().isPresent());
        checkList(node.getVarModList(), VARIABLE_MODIFIERS, ASTKeywordNode.class, 0);
        assertNotNull(node.getDataType());
        assertFalse(node.getEllipsisMod().isPresent());
        assertEquals("args", node.getName().getValue());
    }

    /**
     * Tests formal parameter, variable modifier list, no ellipsis.
     */
    @Test
    public void testFormalParameterOfVMLNoEllipsis() {
        ClassesParser parser = getClassesParser("mut String[] args");
        ASTFormalParameter node = parser.parseFormalParameter();
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 0);
        assertFalse(node.getTakeMod().isPresent());
        checkList(node.getVarModList(), VARIABLE_MODIFIERS, ASTKeywordNode.class, 1);
        assertNotNull(node.getDataType());
        assertFalse(node.getEllipsisMod().isPresent());
        assertEquals("args", node.getName().getValue());
    }

    /**
     * Tests formal parameter of take.
     */
    @Test
    public void testFormalParameterTake() {
        ClassesParser parser = getClassesParser("take State state");
        ASTFormalParameter node = parser.parseFormalParameter();
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 0);
        assertTrue(node.getTakeMod().isPresent());
        checkList(node.getVarModList(), VARIABLE_MODIFIERS, ASTKeywordNode.class, 0);
        assertNotNull(node.getDataType());
        assertFalse(node.getEllipsisMod().isPresent());
        assertEquals("state", node.getName().getValue());
    }

    /**
     * Tests formal parameter of annotation.
     */
    @Test
    public void testFormalParameterAnnotation() {
        ClassesParser parser = getClassesParser("@State State state");
        ASTFormalParameter node = parser.parseFormalParameter();
        ensureNoErrors(node, parser);

        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 1);
        assertFalse(node.getTakeMod().isPresent());
        checkList(node.getVarModList(), VARIABLE_MODIFIERS, ASTKeywordNode.class, 0);
        assertNotNull(node.getDataType());
        assertFalse(node.getEllipsisMod().isPresent());
        assertEquals("state", node.getName().getValue());
    }

    /**
     * Helper method to get a <code>ClassesParser</code> directly from code.
     * @param code The code to test.
     * @return A <code>ClassesParser</code> that will parse the given code.
     */
    public static ClassesParser getClassesParser(String code) {
        return new Parser(new Scanner(code), new BaseMessageProducer()).getClassesParser();
    }
}
