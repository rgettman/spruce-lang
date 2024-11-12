package org.spruce.compiler.test;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.ast.classes.*;
import org.spruce.compiler.ast.expressions.*;
import org.spruce.compiler.ast.names.*;
import org.spruce.compiler.ast.statements.*;
import org.spruce.compiler.ast.types.*;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.parser.ClassesParser;
import org.spruce.compiler.parser.Parser;
import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.Scanner;
import static org.spruce.compiler.scanner.TokenType.*;
import static org.spruce.compiler.test.ParserTestUtility.*;

import org.junit.jupiter.api.Test;

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
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTAnnotationDeclaration node = parser.parseAnnotationDeclaration(loc, null, genModList);
        System.out.println(node);

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
            public shared annotation AFullTest {
                String prop();
            }
            """);
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = accessMod.getLocation();
        ASTAnnotationDeclaration node = parser.parseAnnotationDeclaration(loc, accessMod, genModList);
        System.out.println(node);

        assertTrue(node.getAccessMod().isPresent());
        ASTKeywordNode am = ensureIsa(node.getAccessMod().get(), ASTKeywordNode.class);
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
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = accessMod.getLocation();
        assertThrows(CompileException.class,
                () -> parser.parseAnnotationDeclaration(loc, accessMod, genModList), "Unexpected annotation modifier.");
    }

    /**
     * Tests empty annotation body.
     */
    @Test
    public void testAnnotationBodyEmpty() {
        ClassesParser parser = getClassesParser("{}");
        ASTAnnotationPartList node = parser.parseAnnotationBody();
        System.out.println(node);
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
        System.out.println(node);
        checkList(node, ANNOTATION_PARTS, ASTAnnotationPart.class, 3);
    }

    /**
     * Tests annotation part list of annotation part.
     */
    @Test
    public void testAnnotationPartListOfAnnotationPart() {
        ClassesParser parser = getClassesParser("constant Integer i = 1;");
        ASTAnnotationPartList node = parser.parseAnnotationPartList();
        System.out.println(node);
        checkList(node, ANNOTATION_PARTS, ASTAnnotationPart.class, 1);
    }

    /**
     * Tests annotation part list of all possible annotation parts.
     */
    @Test
    public void testAnnotationPartListComprehensive() {
        ClassesParser parser = getClassesParser("""
                constant String foo = "Foo!";
                String element() default "Who!";
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
        System.out.println(node);
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
        System.out.println(node);
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
        System.out.println(node);
        checkList(node, ANNOTATION_PARTS, ASTAnnotationPart.class, 3);
    }

    /**
     * Tests annotation part of annotation type element declaration.
     */
    @Test
    public void testAnnotationPartOfATED() {
        ClassesParser parser = getClassesParser("String element() default \"Test\";");
        ASTAnnotationPart node = parser.parseAnnotationPart();
        System.out.println(node);
        assertInstanceOf(ASTAnnotationTypeElementDeclaration.class, node);
    }

    /**
     * Tests annotation part of constant declaration.
     */
    @Test
    public void testAnnotationPartOfConstantDeclaration() {
        ClassesParser parser = getClassesParser("constant String LANGUAGE = \"Spruce\";");
        ASTAnnotationPart node = parser.parseAnnotationPart();
        System.out.println(node);
        assertInstanceOf(ASTConstantDeclaration.class, node);
    }

    /**
     * Tests annotation part of class declaration.
     */
    @Test
    public void testAnnotationPartOfClassDeclaration() {
        ClassesParser parser = getClassesParser("public shared class Nested {}");
        ASTAnnotationPart node = parser.parseAnnotationPart();
        System.out.println(node);
        assertInstanceOf(ASTClassDeclaration.class, node);
    }

    /**
     * Tests annotation part of enum declaration.
     */
    @Test
    public void testAnnotationPartOfEnumDeclaration() {
        ClassesParser parser = getClassesParser("private enum Light {RED, YELLOW, GREEN}");
        ASTAnnotationPart node = parser.parseAnnotationPart();
        System.out.println(node);
        assertInstanceOf(ASTEnumDeclaration.class, node);
    }

    /**
     * Tests annotation part of interface declaration.
     */
    @Test
    public void testAnnotationPartOfInterfaceDeclaration() {
        ClassesParser parser = getClassesParser("private interface TrafficLight { Light getStatus(); }");
        ASTAnnotationPart node = parser.parseAnnotationPart();
        System.out.println(node);
        assertInstanceOf(ASTInterfaceDeclaration.class, node);
    }

    /**
     * Tests annotation part of annotation declaration.
     */
    @Test
    public void testAnnotationPartOfAnnotationDeclaration() {
        ClassesParser parser = getClassesParser("public annotation Test { String getStatus() default \"SUCCESS\"; }");
        ASTAnnotationPart node = parser.parseAnnotationPart();
        System.out.println(node);
        assertInstanceOf(ASTAnnotationDeclaration.class, node);
    }

    /**
     * Tests annotation part of record declaration.
     */
    @Test
    public void testAnnotationPartOfRecordDeclaration() {
        ClassesParser parser = getClassesParser("internal record Redacted(String byWhom) { }");
        ASTAnnotationPart node = parser.parseAnnotationPart();
        System.out.println(node);
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
        System.out.println(node);
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
        assertThrows(CompileException.class, parser::parseAnnotationPart,
                "Type parameters not allowed on annotation element declaration.");
    }

    /**
     * Tests bad annotation part of annotation type element declaration with a general modifier.
     */
    @Test
    public void testAnnotationPartGenModOnATED() {
        ClassesParser parser = getClassesParser("""
                shared String value();
                """);
        assertThrows(CompileException.class, parser::parseAnnotationPart,
                "Method modifiers not allowed on annotation element declaration.");
    }

    /**
     * Tests bad annotation part of annotation type element declaration with an access modifier.
     */
    @Test
    public void testAnnotationPartAccessModOnATED() {
        ClassesParser parser = getClassesParser("""
                private String value();
                """);
        assertThrows(CompileException.class, parser::parseAnnotationPart,
                "Access modifiers not allowed on annotation element declaration.");
    }

    /**
     * Tests bad annotation part of constant declaration with a type parameter.
     */
    @Test
    public void testAnnotationPartTypeParamsOnConstantDeclaration() {
        ClassesParser parser = getClassesParser("""
                <T> constant TEST = "test";
                """);
        assertThrows(CompileException.class, parser::parseAnnotationPart,
                "Type parameters not allowed on constant declaration.");
    }

    /**
     * Tests annotation type element declaration.
     */
    @Test
    public void testATED() {
        ClassesParser parser = getClassesParser("String element();");
        ASTDataType dataType = parser.getTypesParser().parseDataType();
        ASTAnnotationTypeElementDeclaration node = parser.parseAnnotationTypeElementDeclaration(dataType.getLocation(), dataType);
        System.out.println(node);

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
        ASTDataType dataType = parser.getTypesParser().parseDataType();
        ASTAnnotationTypeElementDeclaration node = parser.parseAnnotationTypeElementDeclaration(dataType.getLocation(), dataType);
        System.out.println(node);

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
        ASTDataType dataType = parser.getTypesParser().parseDataType();
        assertThrows(CompileException.class,
                () -> parser.parseAnnotationTypeElementDeclaration(dataType.getLocation(), dataType),
                "Expected '('");
    }

    /**
     * Tests bad annotation type element declaration of no close parenthesis.
     */
    @Test
    public void testATEDNoCloseParen() {
        ClassesParser parser = getClassesParser("String bad( default \"DNE\";");
        ASTDataType dataType = parser.getTypesParser().parseDataType();
        assertThrows(CompileException.class,
                () -> parser.parseAnnotationTypeElementDeclaration(dataType.getLocation(), dataType),
                "Expected ')'");
    }

    /**
     * Tests bad annotation type element declaration of no semicolon.
     */
    @Test
    public void testATEDNoSemicolon() {
        ClassesParser parser = getClassesParser("String bad() default \"DNE\"}");
        ASTDataType dataType = parser.getTypesParser().parseDataType();
        assertThrows(CompileException.class,
                () -> parser.parseAnnotationTypeElementDeclaration(dataType.getLocation(), dataType),
                "Expected ';'");
    }

    /**
     * Tests default value.
     */
    @Test
    public void testDefaultValue() {
        ClassesParser parser = getClassesParser("default {\"default\", \"value\"}");
        ASTElementValue node = parser.parseDefaultValue();
        System.out.println(node);

        ASTElementValueList elementValueArrayInit = ensureIsa(node, ASTElementValueList.class);
        checkList(elementValueArrayInit, ELEMENT_VALUES, ASTElementValue.class, 2);
    }

    /**
     * Tests annotation of marker annotation.
     */
    @Test
    public void testAnnotationOfMarkerAnnotation() {
        ClassesParser parser = getClassesParser("@Test");
        ASTAnnotation node = parser.parseAnnotation();
        System.out.println(node);

        ASTMarkerAnnotation ma = ensureIsa(node, ASTMarkerAnnotation.class);
        assertNotNull(ma.getTypeName());
    }

    /**
     * Tests annotation of single element annotation.
     */
    @Test
    public void testAnnotationOfSingleElementAnnotation() {
        ClassesParser parser = getClassesParser("@Test(\"Test\")");
        ASTAnnotation node = parser.parseAnnotation();
        System.out.println(node);

        ASTSingleElementAnnotation sea = ensureIsa(node, ASTSingleElementAnnotation.class);
        assertNotNull(sea.getTypeName());
        assertNotNull(sea.getElementValue());
    }

    /**
     * Tests bad annotation of bad single element annotation, no close parenthesis.
     */
    @Test
    public void testAnnotationOfSingleElementAnnotationNoCloseParen() {
        ClassesParser parser = getClassesParser("@Test(\"Test\"}");
        assertThrows(CompileException.class, parser::parseAnnotation, "Expected ')'.");
    }

    /**
     * Tests annotation of normal annotation, empty.
     */
    @Test
    public void testAnnotationOfNormalAnnotationEmpty() {
        ClassesParser parser = getClassesParser("@Empty()");
        ASTAnnotation node = parser.parseAnnotation();
        System.out.println(node);

        ASTNormalAnnotation na = ensureIsa(node, ASTNormalAnnotation.class);
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
        System.out.println(node);

        ASTNormalAnnotation na = ensureIsa(node, ASTNormalAnnotation.class);
        assertNotNull(na.getTypeName());
        checkList(na.getElementValuePairList(), ELEMENT_VALUE_PAIRS, ASTElementValuePair.class, 3);
    }

    /**
     * Tests bad annotation of bad normal annotation, no close parenthesis.
     */
    @Test
    public void testAnnotationOfNormalAnnotationNoCloseParen() {
        ClassesParser parser = getClassesParser("@Test(test = \"Test\"}");
        assertThrows(CompileException.class, parser::parseAnnotation, "Expected ')'.");
    }

    /**
     * Tests element value pair list of element value pair.
     */
    @Test
    public void testEVPListOfEVP() {
        ClassesParser parser = getClassesParser("test = \"Test\"");
        ASTElementValuePairList node = parser.parseElementValuePairList();
        System.out.println(node);
        checkList(node, ELEMENT_VALUE_PAIRS, ASTElementValuePair.class, 1);
    }

    /**
     * Tests element value pair list.
     */
    @Test
    public void testEVPList() {
        ClassesParser parser = getClassesParser("one = 1, two = \"two\", three = '3'");
        ASTElementValuePairList node = parser.parseElementValuePairList();
        System.out.println(node);
        checkList(node, ELEMENT_VALUE_PAIRS, ASTElementValuePair.class, 3);
    }

    /**
     * Tests element value pair of element value.
     */
    @Test
    public void testElementValuePairOfElementValue() {
        ClassesParser parser = getClassesParser("prop = \"Value Expression\"");
        ASTElementValuePair node = parser.parseElementValuePair();
        System.out.println(node);
        assertNotNull(node.getElementName());
        assertNotNull(node.getElementValue());
    }

    /**
     * Tests bad element value pair of bad assignment.
     */
    @Test
    public void testElementValuePairOfBadAssignment() {
        ClassesParser parser = getClassesParser("prop -> \"Value Expression\"");
        assertThrows(CompileException.class, parser::parseElementValuePair, "Expected assignment operator '='.");
    }

    /**
     * Tests empty element value array initializer.
     */
    @Test
    public void testEVAIEmpty() {
        ClassesParser parser = getClassesParser("{}");
        ASTElementValueList node = parser.parseElementValueArrayInitializer();
        System.out.println(node);
        checkList(node, ELEMENT_VALUES, ASTElementValue.class, 0);
    }

    /**
     * Tests element value array initializer of element value list.
     */
    @Test
    public void testEVAIOfEVList() {
        ClassesParser parser = getClassesParser("{1, \"Two\", '3'}");
        ASTElementValueList node = parser.parseElementValueArrayInitializer();
        System.out.println(node);
        checkList(node, ELEMENT_VALUES, ASTElementValue.class, 3);
    }

    /**
     * Tests element value list of element value.
     */
    @Test
    public void testEVListOfEV() {
        ClassesParser parser = getClassesParser("\"Test\"");
        ASTElementValueList node = parser.parseElementValueList();
        System.out.println(node);
        checkList(node, ELEMENT_VALUES, ASTElementValue.class, 1);
    }

    /**
     * Tests element value list.
     */
    @Test
    public void testEVList() {
        ClassesParser parser = getClassesParser("1, \"two\", '3'");
        ASTElementValueList node = parser.parseElementValueList();
        System.out.println(node);
        checkList(node, ELEMENT_VALUES, ASTElementValue.class, 3);
    }

    /**
     * Tests element value of value expression.
     */
    @Test
    public void testElementValueOfValueExpression() {
        ClassesParser parser = getClassesParser("\"Value Expression\"");
        ASTElementValue node = parser.parseElementValue();
        System.out.println(node);
        assertInstanceOf(ASTPrimary.class, node);
    }

    /**
     * Tests element value of element value array initializer.
     */
    @Test
    public void testElementValueOfEVAI() {
        ClassesParser parser = getClassesParser("{\"Value Expression\"}");
        ASTElementValue node = parser.parseElementValue();
        System.out.println(node);
        assertInstanceOf(ASTListNode.class, node);
    }

    /**
     * Tests element value of annotation.
     */
    @Test
    public void testElementValueOfAnnotation() {
        ClassesParser parser = getClassesParser("@Foo");
        ASTElementValue node = parser.parseElementValue();
        System.out.println(node);
        assertInstanceOf(ASTAnnotation.class, node);
    }

    /**
     * Tests simple interface declaration.
     */
    @Test
    public void testInterfaceDeclarationSimple() {
        ClassesParser parser = getClassesParser("interface Dummy {}");
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTInterfaceDeclaration node = parser.parseInterfaceDeclaration(loc, null, genModList);
        System.out.println(node);

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
            public shared interface IFullTest<T> extends ITest<T>, Serializable, List<T>
                permits FinalTest, UnitTest, Test, Quiz, PopQuiz
            {}
            """);
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = accessMod.getLocation();
        ASTInterfaceDeclaration node = parser.parseInterfaceDeclaration(loc, accessMod, genModList);
        System.out.println(node);

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
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = accessMod.getLocation();
        assertThrows(CompileException.class, () -> parser.parseInterfaceDeclaration(loc, accessMod, genModList),
                "Unexpected interface modifier.");
    }

    /**
     * Tests extends interfaces (extends clause on interface).
     */
    @Test
    public void testExtendsInterfaces() {
        ClassesParser parser = getClassesParser("extends Copyable, Serializable");
        ASTDataTypeNoArrayList node = parser.parseExtendsInterfaces();
        System.out.println(node);
        checkList(node, DATA_TYPES_NO_ARRAY, ASTDataTypeNoArray.class, 2);
    }

    /**
     * Tests empty interface body.
     */
    @Test
    public void testInterfaceBodyEmpty() {
        ClassesParser parser = getClassesParser("{}");
        ASTInterfacePartList node = parser.parseInterfaceBody();
        System.out.println(node);
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
        System.out.println(node);
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
        assertThrows(CompileException.class, parser::parseInterfaceBody, "Expected '{'.");
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
        assertThrows(CompileException.class, parser::parseInterfaceBody, "Expected '}'.");
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
                interface Helper {}
                annotation InnerAnnotation {}
                record FooRecord(String bar) {}
                adt Foo {
                    Boo() {},
                    Goo() {}
                }
                """);
        ASTInterfacePartList node = parser.parseInterfacePartList();
        System.out.println(node);
        checkList(node, INTERFACE_PARTS, ASTInterfacePart.class, 9);
    }

    /**
     * Tests interface part list of interface part.
     */
    @Test
    public void testInterfacePartListOfInterfacePart() {
        ClassesParser parser = getClassesParser("constant Integer i = 1;");
        ASTInterfacePartList node = parser.parseInterfacePartList();
        System.out.println(node);
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
        System.out.println(node);
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
        System.out.println(node);
        checkList(node, INTERFACE_PARTS, ASTInterfacePart.class, 3);
    }

    /**
     * Tests interface part of method declaration with void result.
     */
    @Test
    public void testInterfacePartOfMethodDeclarationVoidResult() {
        ClassesParser parser = getClassesParser("public void method();");
        ASTInterfacePart node = parser.parseInterfacePart();
        System.out.println(node);
        assertInstanceOf(ASTInterfaceMethodDeclaration.class, node);
    }

    /**
     * Tests interface part of method declaration with void result and type parameters.
     */
    @Test
    public void testInterfacePartOfMethodDeclarationVoidResultTypeParameters() {
        ClassesParser parser = getClassesParser("public <T> void method(T param);");
        ASTInterfacePart node = parser.parseInterfacePart();
        System.out.println(node);
        assertInstanceOf(ASTInterfaceMethodDeclaration.class, node);
    }

    /**
     * Tests interface part of method declaration data type void result.
     */
    @Test
    public void testInterfacePartOfMethodDeclarationDataTypeResult() {
        ClassesParser parser = getClassesParser("public String method();");
        ASTInterfacePart node = parser.parseInterfacePart();
        System.out.println(node);
        assertInstanceOf(ASTInterfaceMethodDeclaration.class, node);
    }

    /**
     * Tests interface part of method declaration with data type result and type parameters.
     */
    @Test
    public void testInterfacePartOfMethodDeclarationDataTypeResultTypeParameters() {
        ClassesParser parser = getClassesParser("public <T> T method(T param);");
        ASTInterfacePart node = parser.parseInterfacePart();
        System.out.println(node);
        assertInstanceOf(ASTInterfaceMethodDeclaration.class, node);
    }

    /**
     * Tests interface part of method declaration with mut result.
     */
    @Test
    public void testInterfacePartOfMethodDeclarationConstResult() {
        ClassesParser parser = getClassesParser("mut String method(String param);");
        ASTInterfacePart node = parser.parseInterfacePart();
        System.out.println(node);
        assertInstanceOf(ASTInterfaceMethodDeclaration.class, node);
    }

    /**
     * Tests interface part of constant declaration.
     */
    @Test
    public void testInterfacePartOfConstantDeclaration() {
        ClassesParser parser = getClassesParser("constant String LANGUAGE = \"Spruce\";");
        ASTInterfacePart node = parser.parseInterfacePart();
        System.out.println(node);
        assertInstanceOf(ASTConstantDeclaration.class, node);
    }

    /**
     * Tests interface part of class declaration.
     */
    @Test
    public void testInterfacePartOfClassDeclaration() {
        ClassesParser parser = getClassesParser("public shared class Nested {}");
        ASTInterfacePart node = parser.parseInterfacePart();
        System.out.println(node);
        assertInstanceOf(ASTClassDeclaration.class, node);
    }

    /**
     * Tests interface part of enum declaration.
     */
    @Test
    public void testInterfacePartOfEnumDeclaration() {
        ClassesParser parser = getClassesParser("private enum Light {RED, YELLOW, GREEN}");
        ASTInterfacePart node = parser.parseInterfacePart();
        System.out.println(node);
        assertInstanceOf(ASTEnumDeclaration.class, node);
    }

    /**
     * Tests interface part of interface declaration.
     */
    @Test
    public void testInterfacePartOfInterfaceDeclaration() {
        ClassesParser parser = getClassesParser("private interface TrafficLight { Light getStatus(); }");
        ASTInterfacePart node = parser.parseInterfacePart();
        System.out.println(node);
        assertInstanceOf(ASTInterfaceDeclaration.class, node);
    }

    /**
     * Tests interface part of annotation declaration.
     */
    @Test
    public void testInterfacePartOfAnnotationDeclaration() {
        ClassesParser parser = getClassesParser("public annotation Test { String getStatus() default \"SUCCESS\"; }");
        ASTInterfacePart node = parser.parseInterfacePart();
        System.out.println(node);
        assertInstanceOf(ASTAnnotationDeclaration.class, node);
    }

    /**
     * Tests interface part of record declaration.
     */
    @Test
    public void testInterfacePartOfRecordDeclaration() {
        ClassesParser parser = getClassesParser("internal record Redacted(String byWhom) { }");
        ASTInterfacePart node = parser.parseInterfacePart();
        System.out.println(node);
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
        System.out.println(node);
        assertInstanceOf(ASTAdtDeclaration.class, node);
    }

    /**
     * Tests bad interface part of bad constant of variable modifier.
     */
    @Test
    public void testInterfacePartBadConstant() {
        ClassesParser parser = getClassesParser("public constant var String BAD_CONSTANT = \"Bad!\"");
        assertThrows(CompileException.class, parser::parseInterfacePart, "Unexpected variable modifier.");
    }

    /**
     * Tests simple interface method declaration.
     */
    @Test
    public void testInterfaceMethodDeclarationSimple() {
        ClassesParser parser = getClassesParser("Boolean add(T element);");
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTVariableModifierList varModList = parser.getStatementsParser().parseVariableModifierList();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTInterfaceMethodDeclaration node = parser.parseInterfaceMethodDeclaration(loc, null, genModList, varModList, dt);
        System.out.println(node);

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
                for (T element : other) {
                    add(other);
                }
            }
            """);
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = accessMod.getLocation();
        ASTInterfaceMethodDeclaration node = parser.parseInterfaceMethodDeclaration(loc, accessMod, genModList);
        System.out.println(node);

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
                for (T element : other) {
                    add(other);
                }
            }
            """);
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = accessMod.getLocation();
        assertThrows(CompileException.class, () -> parser.parseInterfaceMethodDeclaration(loc, accessMod, genModList),
                "Unexpected interface method modifier.");
    }

    /**
     * Tests bad constant declaration, no "constant".
     */
    @Test
    public void testConstantDeclaration() {
        ClassesParser parser = getClassesParser("String test = \"Test\";");
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        assertThrows(CompileException.class, () -> parser.parseConstantDeclaration(loc, null, genModList, dt),
                "Expected 'constant'.");
    }

    /**
     * Tests constant declaration, no "constant".
     */
    @Test
    public void testConstantDeclarationAccessMod() {
        ClassesParser parser = getClassesParser("public String test = \"Test\";");
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = accessMod.getLocation();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        assertThrows(CompileException.class, () -> parser.parseConstantDeclaration(loc, accessMod, genModList, dt),
                "Expected 'constant'.");
    }

    /**
     * Tests constant declaration with "constant".
     */
    @Test
    public void testConstantDeclarationOfConstant() {
        ClassesParser parser = getClassesParser("constant String test = \"Test\";");
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTConstantDeclaration node = parser.parseConstantDeclaration(loc, null, genModList, dt);
        System.out.println(node);

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
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        assertThrows(CompileException.class,
                () -> parser.parseConstantDeclaration(loc, null, genModList, dt),
                "Expected semicolon.");
    }

    /**
     * Tests constant modifier by itself.
     */
    @Test
    public void testConstantModifier() {
        ClassesParser parser = getClassesParser("constant");
        ASTKeywordNode node = parser.parseConstantModifier();
        System.out.println(node);
        assertEquals(CONSTANT, node.getKeyword());
    }

    /**
     * Tests bad adt declaration no adt body.
     */
    @Test
    public void testAdtDeclarationNoAdtBody() {
        Scanner scanner = new Scanner("adt Bad;");
        ClassesParser parser = new Parser(scanner).getClassesParser();
        Location loc = scanner.getCurrToken().getLocation();
        assertThrows(CompileException.class, () -> parser.parseAdtDeclaration(loc, null), "Expected '{'.");
    }

    /**
     * Tests bad adt declaration no adt.
     */
    @Test
    public void testAdtDeclarationNoAdt() {
        Scanner scanner = new Scanner("throw Optional { None() {}, Some(Object value) {}}");
        ClassesParser parser = new Parser(scanner).getClassesParser();
        Location loc = scanner.getCurrToken().getLocation();
        assertThrows(CompileException.class, () -> parser.parseAdtDeclaration(loc, null), "Expected adt.");
    }

    /**
     * Tests full adt declaration.
     */
    @Test
    public void testAdtDeclarationFull() {
        Scanner scanner = new Scanner("""
                public adt Optional<T> extends Bar { None() {}, Some(T value) {}}
                """);
        ClassesParser parser = new Parser(scanner).getClassesParser();
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        ASTAdtDeclaration node = parser.parseAdtDeclaration(accessMod.getLocation(), accessMod);
        System.out.println(node);

        assertTrue(node.getAccessModifier().isPresent());
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
        ClassesParser parser = new Parser(scanner).getClassesParser();
        Location loc = scanner.getCurrToken().getLocation();
        ASTAdtDeclaration node = parser.parseAdtDeclaration(loc, null);
        System.out.println(node);

        assertFalse(node.getAccessModifier().isPresent());
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
        assertThrows(CompileException.class, parser::parseAdtBody, "Expected '}'.'");
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
        assertThrows(CompileException.class, parser::parseAdtBody, "Expected '{'.'");
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
        System.out.println(node);

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
        System.out.println(node);

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
        System.out.println(node);
        checkList(node, VARIANTS, ASTVariant.class, 3);
    }

    /**
     * Tests variant of compact record declaration.
     */
    @Test
    public void testVariantOfCompactRecordDeclaration() {
        ClassesParser parser = getClassesParser("None() {}");
        ASTVariant node = parser.parseVariant();
        System.out.println(node);
        assertInstanceOf(ASTCompactRecordDeclaration.class, node);
    }

    /**
     * Tests variant of data type.
     */
    @Test
    public void testVariantOfDataType() {
        ClassesParser parser = getClassesParser("Elsewhere,");
        ASTVariant node = parser.parseVariant();
        System.out.println(node);
        assertInstanceOf(ASTDataType.class, node);
    }

    /**
     * Tests bad variant of no identifier.
     */
    @Test
    public void testVariantNoIdentifier() {
        ClassesParser parser = getClassesParser("public");
        assertThrows(CompileException.class, parser::parseVariant, "Expected an identifier.");
    }

    /**
     * Tests bad compact record declaration.
     */
    @Test
    public void testBadCompactRecordDeclaration() {
        ClassesParser parser = getClassesParser("record None() {}");
        assertThrows(CompileException.class, parser::parseCompactRecordDeclaration, "Expected identifier.");
    }

    /**
     * Tests full compact record declaration.
     */
    @Test
    public void testCompactRecordDeclarationFull() {
        ClassesParser parser = getClassesParser("""
                Some<T>(T value) implements Foo {
                    public T get() {
                        return value();
                    }
                }
                """);
        ASTCompactRecordDeclaration node = parser.parseCompactRecordDeclaration();
        System.out.println(node);

        assertEquals("Some", node.getName().getValue());
        assertTrue(node.getTypeParams().isPresent());
        checkList(node.getFormalParamList(), FORMAL_PARAMETERS, ASTFormalParameter.class, 1);
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
        ASTCompactRecordDeclaration node = parser.parseCompactRecordDeclaration();
        System.out.println(node);
        assertEquals("None", node.getName().getValue());
        assertFalse(node.getTypeParams().isPresent());
        checkList(node.getFormalParamList(), FORMAL_PARAMETERS, ASTFormalParameter.class, 0);
        assertFalse(node.getSuperinterfaces().isPresent());
        checkList(node.getClassParts(), CLASS_PARTS, ASTClassPart.class, 0);
    }

    /**
     * Tests bad compact record declaration of no identifier.
     */
    @Test
    public void testCompactRecordDeclarationNoIdentifier() {
        ClassesParser parser = getClassesParser("public() {}");
        assertThrows(CompileException.class, parser::parseCompactRecordDeclaration, "Expected an identifier.");
    }

    /**
     * Tests bad Adt Body Declarations.
     */
    @Test
    public void testBadAdtBodyDeclarations() {
        ClassesParser parser = getClassesParser("public T getValue();");
        assertThrows(CompileException.class, parser::parseAdtBodyDeclarations, "Expected ';'.");
    }

    /**
     * Tests Adt Body Declarations.
     */
    @Test
    public void testAdtBodyDeclarations() {
        ClassesParser parser = getClassesParser("; public T getValue();");
        ASTInterfacePartList node = parser.parseAdtBodyDeclarations();
        System.out.println(node);
        checkList(node, INTERFACE_PARTS, ASTInterfacePart.class, 1);
    }

    /**
     * Tests a full record declaration.
     */
    @Test
    public void testRecordDeclarationFull() {
        Scanner scanner = new Scanner("public record Value<T>(T value) implements Comparable<T> {}");
        ClassesParser parser = new Parser(scanner).getClassesParser();
        ASTKeywordNode am = parser.parseAccessModifier();
        ASTRecordDeclaration node = parser.parseRecordDeclaration(am.getLocation(), am);
        System.out.println(node);

        assertTrue(node.getAccessMod().isPresent());
        assertEquals("Value", node.getName().getValue());
        assertTrue(node.getTypeParams().isPresent());
        checkList(node.getFormalParamList(), FORMAL_PARAMETERS, ASTFormalParameter.class, 1);
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
        ClassesParser parser = new Parser(scanner).getClassesParser();
        Location loc = scanner.getCurrToken().getLocation();
        ASTRecordDeclaration node = parser.parseRecordDeclaration(loc, null);
        System.out.println(node);

        assertFalse(node.getAccessMod().isPresent());
        assertEquals("Person", node.getName().getValue());
        assertFalse(node.getTypeParams().isPresent());
        checkList(node.getFormalParamList(), FORMAL_PARAMETERS, ASTFormalParameter.class, 2);
        assertFalse(node.getSuperinterfaces().isPresent());
        checkList(node.getClassParts(), CLASS_PARTS, ASTClassPart.class, 0);
    }

    /**
     * Tests bad Record Header, missing close parenthesis.
     */
    @Test
    public void testRecordHeaderMissingCloseParen() {
        ClassesParser parser = getClassesParser("(String filename, Int lineNbr");
        assertThrows(CompileException.class, parser::parseRecordHeader, "Missing ')'.");
    }

    /**
     * Tests bad Record Header, missing open parenthesis.
     */
    @Test
    public void testRecordHeaderMissingOpenParen() {
        ClassesParser parser = getClassesParser("String filename, Int lineNbr)");
        assertThrows(CompileException.class, parser::parseRecordHeader, "Missing '('.");
    }

    /**
     * Tests a Record Header.
     */
    @Test
    public void testRecordHeader() {
        ClassesParser parser = getClassesParser("(String filename, Int lineNbr)");
        ASTFormalParameterList node = parser.parseRecordHeader();
        System.out.println(node);
        checkList(node, FORMAL_PARAMETERS, ASTFormalParameter.class, 2);
    }

    /**
     * Tests bad compact constructor declaration.
     */
    @Test
    public void testCompactConstructorDeclarationBad() {
        ClassesParser parser = getClassesParser("private { }");
        ASTKeywordNode am = parser.parseAccessModifier();
        assertThrows(CompileException.class, () -> parser.parseCompactConstructorDeclaration(am.getLocation(), am));
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
        ASTKeywordNode am = parser.parseAccessModifier();
        ASTCompactConstructorDeclaration node = parser.parseCompactConstructorDeclaration(am.getLocation(), am);
        System.out.println(node);

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
        ClassesParser parser = new Parser(scanner).getClassesParser();
        Location loc = scanner.getCurrToken().getLocation();
        ASTCompactConstructorDeclaration node = parser.parseCompactConstructorDeclaration(loc, null);
        System.out.println(node);

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
        Location loc = genModList.getLocation();
        ASTEnumDeclaration node = parser.parseEnumDeclaration(loc, null, genModList);
        System.out.println(node);

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
        ClassesParser parser = getClassesParser("public shared enum FullEnumTest implements Serializable {QUIZ, TEST, FINAL}");
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = accessMod.getLocation();
        ASTEnumDeclaration node = parser.parseEnumDeclaration(loc, accessMod, genModList);
        System.out.println(node);

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
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = accessMod.getLocation();
        assertThrows(CompileException.class, () -> parser.parseEnumDeclaration(loc, accessMod, genModList),
                "Unexpected enum modifier.");
    }

    /**
     * Tests simple enum body.
     */
    @Test
    public void testEnumBodySimple() {
        ClassesParser parser = getClassesParser("{\nRED, YELLOW, GREEN\n}");
        ASTEnumBody node = parser.parseEnumBody();
        System.out.println(node);
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
        System.out.println(node);
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
                        out.println("Utility!");
                    }
                }
                """);
        ASTEnumBody node = parser.parseEnumBody();
        System.out.println(node);
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
                        out.println("Utility!");
                    }
                }
                """);
        ASTEnumBody node = parser.parseEnumBody();
        System.out.println(node);
        checkList(node.getEnumConstants(), ENUM_CONSTANTS, ASTEnumConstant.class, 3);
        checkList(node.getClassParts(), CLASS_PARTS, ASTClassPart.class, 1);
    }

    /**
     * Tests bad enum body of no open brace.
     */
    @Test
    public void testEnumBodyNoOpenBrace() {
        ClassesParser parser = getClassesParser("SATURDAY, SUNDAY}");
        assertThrows(CompileException.class, parser::parseEnumBody, "Expected '{'.");
    }

    /**
     * Tests bad enum body of no close brace.
     */
    @Test
    public void testEnumBodyNoCloseBrace() {
        ClassesParser parser = getClassesParser("{SATURDAY, SUNDAY");
        assertThrows(CompileException.class, parser::parseEnumBody, "Expected '}'.");
    }

    /**
     * Tests bad enum body of no semicolon but enum body declarations.
     */
    @Test
    public void testEnumBodyNoSemicolon() {
        ClassesParser parser = getClassesParser("""
                {
                    SATURDAY, SUNDAY
                    constant FUN_DAY = SUNDAY;
                }
                """);
        assertThrows(CompileException.class, parser::parseEnumBody, "Expected semicolon.");
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
        System.out.println(node);
        checkList(node, CLASS_PARTS, ASTClassPart.class, 1);
    }

    /**
     * Tests enum constant list of enum constant.
     */
    @Test
    public void testEnumConstantListOfEnumConstant() {
        ClassesParser parser = getClassesParser("SINGLETON");
        ASTEnumConstantList node = parser.parseEnumConstantList();
        System.out.println(node);
        checkList(node, ENUM_CONSTANTS, ASTEnumConstant.class, 1);
    }

    /**
     * Tests enum constant list.
     */
    @Test
    public void testEnumConstantList() {
        ClassesParser parser = getClassesParser("RED, YELLOW, GREEN");
        ASTEnumConstantList node = parser.parseEnumConstantList();
        System.out.println(node);
        checkList(node, ENUM_CONSTANTS, ASTEnumConstant.class, 3);
    }

    /**
     * Tests simple enum constant.
     */
    @Test
    public void testEnumConstantSimple() {
        ClassesParser parser = getClassesParser("RED");
        ASTEnumConstant node = parser.parseEnumConstant();
        System.out.println(node);

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
        System.out.println(node);

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
        assertThrows(CompileException.class, parser::parseEnumConstant, "Expected ')'.");
    }

    /**
     * Tests simple class declaration.
     */
    @Test
    public void testClassDeclarationSimple() {
        ClassesParser parser = getClassesParser("class Dummy {}");
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTClassDeclaration node = parser.parseClassDeclaration(loc, null, genModList);
        System.out.println(node);

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
            public shared class FullTest<T> extends Test<T> implements Serializable, List<T>
                permits FinalTest, UnitTest, Test, Quiz, PopQuiz
            {}
            """);
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = accessMod.getLocation();
        ASTClassDeclaration node = parser.parseClassDeclaration(loc, accessMod, genModList);
        System.out.println(node);

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
     * Tests bad class declaration of bad class modifier.
     */
    @Test
    public void testClassDeclarationBadClassMod() {
        ClassesParser parser = getClassesParser("""
            public constant class BadClassMod {
            }
            """);
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = accessMod.getLocation();
        assertThrows(CompileException.class, () -> parser.parseClassDeclaration(loc, accessMod, genModList),
                "Unexpected class modifier.");
    }

    /**
     * Tests permits (permits clause).
     */
    @Test
    public void testPermits() {
        ClassesParser parser = getClassesParser("permits Dog, Cat, Mouse");
        ASTDataTypeNoArrayList node = parser.parsePermits();
        System.out.println(node);
        checkList(node, DATA_TYPES_NO_ARRAY, ASTDataTypeNoArray.class, 3);
    }

    /**
     * Tests superinterfaces (implements clause).
     */
    @Test
    public void testSuperinterfaces() {
        ClassesParser parser = getClassesParser("implements Copyable");
        ASTDataTypeNoArrayList node = parser.parseSuperinterfaces();
        System.out.println(node);
        checkList(node, DATA_TYPES_NO_ARRAY, ASTDataTypeNoArray.class, 1);
    }

    /**
     * Tests superclass (extends clause).
     */
    @Test
    public void testSuperclass() {
        ClassesParser parser = getClassesParser("extends Thread");
        ASTDataTypeNoArray node = parser.parseSuperclass();
        System.out.println(node);
        checkList(node, SIMPLE_TYPES, ASTSimpleType.class, 1);
    }

    /**
     * Tests empty class body.
     */
    @Test
    public void testClassBodyEmpty() {
        ClassesParser parser = getClassesParser("{}");
        ASTClassPartList node = parser.parseClassBody();
        System.out.println(node);
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
        System.out.println(node);
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
                public String getFoo() {
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
        System.out.println(node);
        checkList(node, CLASS_PARTS, ASTClassPart.class, 11);
    }

    /**
     * Tests class part list of class part.
     */
    @Test
    public void testClassPartListOfClassPart() {
        ClassesParser parser = getClassesParser("private Integer i = 1;");
        ASTClassPartList node = parser.parseClassPartList();
        System.out.println(node);
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
        System.out.println(node);
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
        System.out.println(node);
        checkList(node, CLASS_PARTS, ASTClassPart.class, 3);
    }

    /**
     * Tests class part of shared constructor.
     */
    @Test
    public void testClassPartOfSharedConstructor() {
        ClassesParser parser = getClassesParser("shared constructor() { sharedVar = reallyComplicatedLogic(); }");
        ASTClassPart node = parser.parseClassPart();
        System.out.println(node);
        assertInstanceOf(ASTSharedConstructor.class, node);
    }

    /**
     * Tests class part of method declaration with void result.
     */
    @Test
    public void testClassPartOfMethodDeclarationVoidResult() {
        ClassesParser parser = getClassesParser("public abstract void method();");
        ASTClassPart node = parser.parseClassPart();
        System.out.println(node);
        assertInstanceOf(ASTMethodDeclaration.class, node);
    }

    /**
     * Tests class part of method declaration with void result and type parameters.
     */
    @Test
    public void testClassPartOfMethodDeclarationVoidResultTypeParameters() {
        ClassesParser parser = getClassesParser("public abstract <T> void method(T param);");
        ASTClassPart node = parser.parseClassPart();
        System.out.println(node);
        assertInstanceOf(ASTMethodDeclaration.class, node);
    }

    /**
     * Tests class part of method declaration data type void result.
     */
    @Test
    public void testClassPartOfMethodDeclarationDataTypeResult() {
        ClassesParser parser = getClassesParser("public abstract String method();");
        ASTClassPart node = parser.parseClassPart();
        System.out.println(node);
        assertInstanceOf(ASTMethodDeclaration.class, node);
    }

    /**
     * Tests class part of method declaration with data type result and type parameters.
     */
    @Test
    public void testClassPartOfMethodDeclarationDataTypeResultTypeParameters() {
        ClassesParser parser = getClassesParser("public abstract <T> T method(T param);");
        ASTClassPart node = parser.parseClassPart();
        System.out.println(node);
        assertInstanceOf(ASTMethodDeclaration.class, node);
    }

    /**
     * Tests class part of method declaration with mut result.
     */
    @Test
    public void testClassPartOfMethodDeclarationMutResult() {
        ClassesParser parser = getClassesParser("mut String method(String param);");
        ASTClassPart node = parser.parseClassPart();
        System.out.println(node);
        assertInstanceOf(ASTMethodDeclaration.class, node);
    }

    /**
     * Tests class part of method declaration with mut result.
     */
    @Test
    public void testClassPartOfMethodDeclarationMutResultShared() {
        ClassesParser parser = getClassesParser("shared mut String method(String param);");
        ASTClassPart node = parser.parseClassPart();
        System.out.println(node);
        assertInstanceOf(ASTMethodDeclaration.class, node);
    }

    /**
     * Tests class part of field declaration.
     */
    @Test
    public void testClassPartOfFieldDeclaration() {
        ClassesParser parser = getClassesParser("private Int myVar = 1, myVar2 = 2;");
        ASTClassPart node = parser.parseClassPart();
        System.out.println(node);
        assertInstanceOf(ASTFieldDeclaration.class, node);
    }

    /**
     * Tests class part of constructor declaration.
     */
    @Test
    public void testClassPartOfConstructorDeclaration() {
        ClassesParser parser = getClassesParser("constructor(String s) : constructor(s) {}");
        ASTClassPart node = parser.parseClassPart();
        System.out.println(node);
        assertInstanceOf(ASTConstructorDeclaration.class, node);
    }

    /**
     * Tests class part of class declaration.
     */
    @Test
    public void testClassPartOfClassDeclaration() {
        ClassesParser parser = getClassesParser("public shared class Nested {}");
        ASTClassPart node = parser.parseClassPart();
        System.out.println(node);
        assertInstanceOf(ASTClassDeclaration.class, node);
    }

    /**
     * Tests class part of enum declaration.
     */
    @Test
    public void testClassPartOfEnumDeclaration() {
        ClassesParser parser = getClassesParser("private enum Light {RED, YELLOW, GREEN}");
        ASTClassPart node = parser.parseClassPart();
        System.out.println(node);
        assertInstanceOf(ASTEnumDeclaration.class, node);
    }

    /**
     * Tests class part of interface declaration.
     */
    @Test
    public void testClassPartOfInterfaceDeclaration() {
        ClassesParser parser = getClassesParser("private interface TrafficLight { Light getStatus(); }");
        ASTClassPart node = parser.parseClassPart();
        System.out.println(node);
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
        System.out.println(node);
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
        System.out.println(node);
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
        System.out.println(node);
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
        assertThrows(CompileException.class, parser::parseClassPart, "Unexpected modifier: 'override'.");
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
        assertThrows(CompileException.class, parser::parseClassPart, "Unexpected modifier: 'override'.");
    }

    /**
     * Tests shared constructor.
     */
    @Test
    public void testSharedConstructor() {
        ClassesParser parser = getClassesParser("shared constructor() { sharedVar = reallyComplicatedLogic(); }");
        ASTSharedConstructor node = parser.parseSharedConstructor();
        System.out.println(node);
        assertNotNull(node.getBlock());
    }

    /**
     * Tests bad shared constructor of no open parenthesis.
     */
    @Test
    public void testSharedConstructorNoOpenParen() {
        ClassesParser parser = getClassesParser("shared constructor ) { sharedVar = reallyComplicatedLogic(); }");
        assertThrows(CompileException.class, parser::parseSharedConstructor, "Expected '('.");
    }

    /**
     * Tests bad shared constructor of no close parenthesis.
     */
    @Test
    public void testSharedConstructorNoCloseParen() {
        ClassesParser parser = getClassesParser("shared constructor( { sharedVar = reallyComplicatedLogic(); }");
        assertThrows(CompileException.class, parser::parseSharedConstructor, "Expected '('.");
    }

    /**
     * Tests constructor declaration of access modifier, and constructor invocation.
     */
    @Test
    public void testConstructorDeclarationOfAccessConstructorInvocation() {
        ClassesParser parser = getClassesParser("private constructor(String s) : super(s) { self.s = s; }");
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        Location loc = accessMod.getLocation();
        ASTConstructorDeclaration node = parser.parseConstructorDeclaration(loc, accessMod);
        System.out.println(node);

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
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTConstructorDeclaration node = parser.parseConstructorDeclaration(loc, null);
        System.out.println(node);

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
        System.out.println(node);

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
        System.out.println(node);

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
        System.out.println(node);

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
        assertThrows(CompileException.class, parser::parseConstructorInvocation, "Expected 'constructor' or 'super'.");
    }

    /**
     * Tests bad constructor invocation of no open parenthesis.
     */
    @Test
    public void testConstructorInvocationNoOpenParen() {
        ClassesParser parser = getClassesParser(": constructor String str)");
        assertThrows(CompileException.class, parser::parseConstructorInvocation, "Expected '('.");
    }

    /**
     * Tests bad constructor invocation of no close parenthesis.
     */
    @Test
    public void testConstructorInvocationNoCloseParen() {
        ClassesParser parser = getClassesParser(": constructor(String str {");
        assertThrows(CompileException.class, parser::parseConstructorInvocation, "Expected ')'.");
    }

    /**
     * Tests simple constructor declarator.
     */
    @Test
    public void testConstructorDeclaratorSimple() {
        ClassesParser parser = getClassesParser("constructor()");
        ASTConstructorDeclarator node = parser.parseConstructorDeclarator();
        System.out.println(node);

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
        System.out.println(node);

        assertTrue(node.getTypeParams().isPresent());
        checkList(node.getFormalParamList(), FORMAL_PARAMETERS, ASTFormalParameter.class, 1);
    }

    /**
     * Tests constructor declarator of no open parenthesis.
     */
    @Test
    public void testConstructorDeclaratorNoOpenParen() {
        ClassesParser parser = getClassesParser("constructor Integer param)");
        assertThrows(CompileException.class, parser::parseConstructorDeclarator, "Expected '('.");
    }

    /**
     * Tests constructor declarator of no close parenthesis.
     */
    @Test
    public void testConstructorDeclaratorNoCloseParen() {
        ClassesParser parser = getClassesParser("constructor(String param {");
        assertThrows(CompileException.class, parser::parseConstructorDeclarator, "Expected ')'.");
    }

    /**
     * Tests full field declaration.
     */
    @Test
    public void testFieldDeclaration() {
        ClassesParser parser = getClassesParser("public constant String aConstant = \"CONSTANT\";");
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = accessMod.getLocation();
        ASTVariableModifierList varModList = parser.getStatementsParser().parseVariableModifierList();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTFieldDeclaration node = parser.parseFieldDeclaration(loc, accessMod, genModList, varModList, dt);
        System.out.println(node);

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
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = accessMod.getLocation();
        ASTVariableModifierList varModList = parser.getStatementsParser().parseVariableModifierList();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTFieldDeclaration node = parser.parseFieldDeclaration(loc, accessMod, genModList, varModList, dt);
        System.out.println(node);

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
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTVariableModifierList varModList = parser.getStatementsParser().parseVariableModifierList();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTFieldDeclaration node = parser.parseFieldDeclaration(loc, null, genModList, varModList, dt);
        System.out.println(node);

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
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTVariableModifierList varModList = parser.getStatementsParser().parseVariableModifierList();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTFieldDeclaration node = parser.parseFieldDeclaration(loc, null, genModList, varModList, dt);
        System.out.println(node);

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
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        ASTVariableModifierList varModList = parser.getStatementsParser().parseVariableModifierList();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        Location loc = genModList.getLocation();
        assertThrows(CompileException.class, () -> parser.parseFieldDeclaration(loc, null, genModList, varModList, dt),
                "Unexpected field modifier.");
    }

    /**
     * Tests bad field declaration of no semicolon.
     */
    @Test
    public void testFieldDeclarationNoSemicolon() {
        ClassesParser parser = getClassesParser("shared String name = \"bad\"}");
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        ASTVariableModifierList varModList = parser.getStatementsParser().parseVariableModifierList();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        Location loc = genModList.getLocation();
        assertThrows(CompileException.class, () -> parser.parseFieldDeclaration(loc, null, genModList, varModList, dt),
                "Expected semicolon.");
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
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTVariableModifierList varModList = parser.getStatementsParser().parseVariableModifierList();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTMethodDeclaration node = parser.parseMethodDeclaration(loc, null, genModList, varModList, dt);
        System.out.println(node);

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
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = accessMod.getLocation();
        ASTVariableModifierList varModList = parser.getStatementsParser().parseVariableModifierList();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTMethodDeclaration node = parser.parseMethodDeclaration(loc, accessMod, genModList, varModList, dt);
        System.out.println(node);

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
        ASTKeywordNode accessMod = parser.parseAccessModifier();
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = accessMod.getLocation();
        assertThrows(CompileException.class, () -> parser.parseMethodDeclaration(loc, accessMod, genModList),
                "");
    }

    /**
     * Tests method body of semicolon.
     */
    @Test
    public void testMethodBodyOfSemicolon() {
        ClassesParser parser = getClassesParser(";");
        ASTMethodBody node = parser.parseMethodBody();
        System.out.println(node);
        assertFalse(node.getBlock().isPresent());
    }

    /**
     * Tests method body of block.
     */
    @Test
    public void testMethodBodyOfBlock() {
        ClassesParser parser = getClassesParser("{\n    out.println(\"Body!\");\n}");
        ASTMethodBody node = parser.parseMethodBody();
        System.out.println(node);
        assertTrue(node.getBlock().isPresent());
    }

    /**
     * Tests bad method body of block or semicolon.
     */
    @Test
    public void testMethodBodyOfNoBlockOrSemicolon() {
        ClassesParser parser = getClassesParser("public");
        assertThrows(CompileException.class, parser::parseMethodBody, "Expected block for method body.");
    }

    /**
     * Tests method modifier of public.
     */
    @Test
    public void testAccessModifierOfPublic() {
        ClassesParser parser = getClassesParser("public");
        ASTKeywordNode node = parser.parseAccessModifier();
        System.out.println(node);
        assertEquals(PUBLIC, node.getKeyword());
    }

    /**
     * Tests method modifier of protected.
     */
    @Test
    public void testAccessModifierOfProtected() {
        ClassesParser parser = getClassesParser("protected");
        ASTKeywordNode node = parser.parseAccessModifier();
        System.out.println(node);
        assertEquals(PROTECTED, node.getKeyword());
    }

    /**
     * Tests method modifier of abstract.
     */
    @Test
    public void testAccessModifierOfInternal() {
        ClassesParser parser = getClassesParser("internal");
        ASTKeywordNode node = parser.parseAccessModifier();
        System.out.println(node);
        assertEquals(INTERNAL, node.getKeyword());
    }

    /**
     * Tests access modifier of private.
     */
    @Test
    public void testAccessModifierOfPrivate() {
        ClassesParser parser = getClassesParser("private");
        ASTKeywordNode node = parser.parseAccessModifier();
        System.out.println(node);
        assertEquals(PRIVATE, node.getKeyword());
    }

    /**
     * Tests method modifier list of method modifiers.
     */
    @Test
    public void testGeneralModifierListOfMethodModifiers() {
        ClassesParser parser = getClassesParser("final abstract override shared");
        ASTGeneralModifierList node = parser.parseGeneralModifierList();
        System.out.println(node);
        checkList(node, GENERAL_MODIFIERS, ASTKeywordNode.class, 4);
    }

    /**
     * Tests general modifier of abstract.
     */
    @Test
    public void testGeneralModifierOfAbstract() {
        ClassesParser parser = getClassesParser("abstract");
        ASTKeywordNode node = parser.parseGeneralModifier();
        System.out.println(node);
        assertEquals(ABSTRACT, node.getKeyword());
    }

    /**
     * Tests general modifier of mut.
     */
    @Test
    public void testGeneralModifierOfConst() {
        ClassesParser parser = getClassesParser("mut");
        ASTKeywordNode node = parser.parseGeneralModifier();
        System.out.println(node);
        assertEquals(MUT, node.getKeyword());
    }

    /**
     * Tests general modifier of var.
     */
    @Test
    public void testMethodModifierOfVar() {
        ClassesParser parser = getClassesParser("var");
        ASTKeywordNode node = parser.parseGeneralModifier();
        System.out.println(node);
        assertEquals(VAR, node.getKeyword());
    }

    /**
     * Tests general modifier of override.
     */
    @Test
    public void testGeneralModifierOfOverride() {
        ClassesParser parser = getClassesParser("override");
        ASTKeywordNode node = parser.parseGeneralModifier();
        System.out.println(node);
        assertEquals(OVERRIDE, node.getKeyword());
    }

    /**
     * Tests general modifier of shared.
     */
    @Test
    public void testGeneralModifierOfShared() {
        ClassesParser parser = getClassesParser("shared");
        ASTKeywordNode node = parser.parseGeneralModifier();
        System.out.println(node);
        assertEquals(SHARED, node.getKeyword());
    }

    /**
     * Tests general modifier of volatile.
     */
    @Test
    public void testGeneralModifierOfVolatile() {
        ClassesParser parser = getClassesParser("volatile");
        ASTKeywordNode node = parser.parseGeneralModifier();
        System.out.println(node);
        assertEquals(VOLATILE, node.getKeyword());
    }

    /**
     * Tests simple method header.
     */
    @Test
    public void testMethodHeaderSimple() {
        ClassesParser parser = getClassesParser("void toString() const");
        ASTMethodHeader node = parser.parseMethodHeader();
        System.out.println(node);

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
        System.out.println(node);

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
        System.out.println(node);

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
        System.out.println(node);

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
        System.out.println(node);

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
        System.out.println(node);

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
        System.out.println(node);

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
        assertThrows(CompileException.class, parser::parseMethodDeclarator, "Identifier expected.");
    }

    /**
     * Tests bad method declarator of no open parenthesis.
     */
    @Test
    public void testMethodDeclaratorNoOpenParen() {
        ClassesParser parser = getClassesParser("test String sep) mut)");
        assertThrows(CompileException.class, parser::parseMethodDeclarator, "Expected '('.");
    }

    /**
     * Tests bad method declarator of no close parenthesis.
     */
    @Test
    public void testMethodDeclaratorNoCloseParen() {
        ClassesParser parser = getClassesParser("test(String sep  mut)");
        assertThrows(CompileException.class, parser::parseMethodDeclarator, "Expected ')'.");
    }

    /**
     * Tests mut modifier by itself.
     */
    @Test
    public void testMutModifier() {
        ClassesParser parser = getClassesParser("mut");
        ASTKeywordNode node = parser.parseMutModifier();
        System.out.println(node);
        assertEquals(MUT, node.getKeyword());
    }

    /**
     * Tests formal parameter list of formal parameter.
     */
    @Test
    public void testFormalParameterListOfFormalParameter() {
        ClassesParser parser = getClassesParser("const Int a");
        ASTFormalParameterList node = parser.parseFormalParameterList();
        System.out.println(node);
        checkList(node, FORMAL_PARAMETERS, ASTFormalParameter.class, 1);
    }

    /**
     * Tests formal parameter list.
     */
    @Test
    public void testFormalParameterList() {
        ClassesParser parser = getClassesParser("String msg, Foo f, Bar b");
        ASTFormalParameterList node = parser.parseFormalParameterList();
        System.out.println(node);
        checkList(node, FORMAL_PARAMETERS, ASTFormalParameter.class, 3);
    }

    /**
     * Tests formal parameter list with varargs parameter list.
     */
    @Test
    public void testFormalParameterListOfLastVarargs() {
        ClassesParser parser = getClassesParser("Point pt, Double... coordinates");
        ASTFormalParameterList node = parser.parseFormalParameterList();
        System.out.println(node);
        checkList(node, FORMAL_PARAMETERS, ASTFormalParameter.class, 2);
    }

    /**
     * Tests if varargs not last, compiler error.
     */
    @Test
    public void testFormalParameterListVarargsNotLastError() {
        ClassesParser parser = getClassesParser("Double... coordinates, Point pt");
        assertThrows(CompileException.class, parser::parseFormalParameterList);
    }

    /**
     * Tests formal parameter, no variable modifier list, with ellipsis.
     */
    @Test
    public void testFormalParameterNoVMLEllipsis() {
        ClassesParser parser = getClassesParser("String... args");
        ASTFormalParameter node = parser.parseFormalParameter();
        System.out.println(node);

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
        System.out.println(node);

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
        System.out.println(node);

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
        System.out.println(node);

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
        System.out.println(node);

        assertTrue(node.getTakeMod().isPresent());
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
    private static ClassesParser getClassesParser(String code) {
        return new Parser(new Scanner(code)).getClassesParser();
    }
}
