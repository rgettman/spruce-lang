package org.spruce.compiler.test;

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
        ASTAnnotationDeclaration node = parser.parseAnnotationDeclaration();
        checkBinary(node, ANNOTATION, ASTIdentifier.class, ASTAnnotationBody.class);
        node.collapseThenPrint();
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
        ASTAnnotationDeclaration node = parser.parseAnnotationDeclaration();
        checkNary(node, ANNOTATION, ASTAccessModifier.class, ASTInterfaceModifierList.class, ASTIdentifier.class, ASTAnnotationBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests empty annotation body.
     */
    @Test
    public void testAnnotationBodyEmpty() {
        ClassesParser parser = getClassesParser("{}");
        ASTAnnotationBody node = parser.parseAnnotationBody();
        checkEmpty(node, OPEN_BRACE);
        node.collapseThenPrint();
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
        ASTAnnotationBody node = parser.parseAnnotationBody();
        checkSimple(node, ASTAnnotationPartList.class, OPEN_BRACE);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation part list of annotation part.
     */
    @Test
    public void testAnnotationPartListOfAnnotationPart() {
        ClassesParser parser = getClassesParser("constant Integer i = 1;");
        ASTAnnotationPartList node = parser.parseAnnotationPartList();
        checkSimple(node, ASTAnnotationPart.class);
        node.collapseThenPrint();
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
        checkList(node, null, ASTAnnotationPart.class, 8);
        node.collapseThenPrint();
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
        checkList(node, null, ASTAnnotationPart.class, 2);
        node.collapseThenPrint();
    }

    /**
     * Tests nested annotation part lists.
     */
    @Test
    public void testAnnotationPartListNested() {
        ClassesParser parser = getClassesParser("""
            constant Integer i = 1;
            class Inner {}
            Integer getI() default 1;
            """);
        ASTAnnotationPartList node = parser.parseAnnotationPartList();
        checkList(node, null, ASTAnnotationPart.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation part of annotation type element declaration.
     */
    @Test
    public void testAnnotationPartOfATED() {
        ClassesParser parser = getClassesParser("String element() default \"Test\";");
        ASTAnnotationPart node = parser.parseAnnotationPart();
        checkSimple(node, ASTAnnotationTypeElementDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation part of constant declaration.
     */
    @Test
    public void testAnnotationPartOfConstantDeclaration() {
        ClassesParser parser = getClassesParser("constant String LANGUAGE = \"Spruce\";");
        ASTAnnotationPart node = parser.parseAnnotationPart();
        checkSimple(node, ASTConstantDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation part of class declaration.
     */
    @Test
    public void testAnnotationPartOfClassDeclaration() {
        ClassesParser parser = getClassesParser("public shared class Nested {}");
        ASTAnnotationPart node = parser.parseAnnotationPart();
        checkSimple(node, ASTClassDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation part of enum declaration.
     */
    @Test
    public void testAnnotationPartOfEnumDeclaration() {
        ClassesParser parser = getClassesParser("private enum Light {RED, YELLOW, GREEN}");
        ASTAnnotationPart node = parser.parseAnnotationPart();
        checkSimple(node, ASTEnumDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation part of interface declaration.
     */
    @Test
    public void testAnnotationPartOfInterfaceDeclaration() {
        ClassesParser parser = getClassesParser("private interface TrafficLight { Light getStatus(); }");
        ASTAnnotationPart node = parser.parseAnnotationPart();
        checkSimple(node, ASTInterfaceDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation part of annotation declaration.
     */
    @Test
    public void testAnnotationPartOfAnnotationDeclaration() {
        ClassesParser parser = getClassesParser("public annotation Test { String getStatus() default \"SUCCESS\"; }");
        ASTAnnotationPart node = parser.parseAnnotationPart();
        checkSimple(node, ASTAnnotationDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation part of record declaration.
     */
    @Test
    public void testAnnotationPartOfRecordDeclaration() {
        ClassesParser parser = getClassesParser("internal record Redacted(String byWhom) { }");
        ASTAnnotationPart node = parser.parseAnnotationPart();
        checkSimple(node, ASTRecordDeclaration.class);
        node.collapseThenPrint();
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
        checkSimple(node, ASTAdtDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation type element declaration.
     */
    @Test
    public void testATED() {
        ClassesParser parser = getClassesParser("String element();");
        ASTAnnotationTypeElementDeclaration node = parser.parseAnnotationTypeElementDeclaration();
        checkBinary(node, OPEN_PARENTHESIS, ASTDataType.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation type element declaration with default value.
     */
    @Test
    public void testATEDDefaultValue() {
        ClassesParser parser = getClassesParser("String element() default \"DNE\";");
        ASTAnnotationTypeElementDeclaration node = parser.parseAnnotationTypeElementDeclaration();
        checkTrinary(node, OPEN_PARENTHESIS, ASTDataType.class, ASTIdentifier.class, ASTDefaultValue.class);
        node.collapseThenPrint();
    }

    /**
     * Tests default value.
     */
    @Test
    public void testDefaultValue() {
        ClassesParser parser = getClassesParser("default {\"default\", \"value\"}");
        ASTDefaultValue node = parser.parseDefaultValue();
        checkSimple(node, ASTElementValue.class, DEFAULT);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation of marker annotation.
     */
    @Test
    public void testAnnotationOfMarkerAnnotation() {
        ClassesParser parser = getClassesParser("@Test");
        ASTAnnotation node = parser.parseAnnotation();
        checkSimple(node, ASTMarkerAnnotation.class, AT_SIGN);

        ASTMarkerAnnotation ma = (ASTMarkerAnnotation) node.getChildren().get(0);
        checkSimple(ma, ASTTypeName.class, AT_SIGN);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation of single element annotation.
     */
    @Test
    public void testAnnotationOfSingleElementAnnotation() {
        ClassesParser parser = getClassesParser("@Test(\"Test\")");
        ASTAnnotation node = parser.parseAnnotation();
        checkSimple(node, ASTSingleElementAnnotation.class, AT_SIGN);

        ASTSingleElementAnnotation sea = (ASTSingleElementAnnotation) node.getChildren().get(0);
        checkBinary(sea, AT_SIGN, ASTTypeName.class, ASTElementValue.class);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation of normal annotation, empty.
     */
    @Test
    public void testAnnotationOfNormalAnnotationEmpty() {
        ClassesParser parser = getClassesParser("@Empty()");
        ASTAnnotation node = parser.parseAnnotation();
        checkSimple(node, ASTNormalAnnotation.class, AT_SIGN);

        ASTNormalAnnotation na = (ASTNormalAnnotation) node.getChildren().get(0);
        checkSimple(na, ASTTypeName.class, AT_SIGN);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation of normal annotation of element pair value list.
     */
    @Test
    public void testAnnotationOfNormalAnnotationOfEVPL() {
        ClassesParser parser = getClassesParser("@Many(one = 1, two = \"two\", three = '3')");
        ASTAnnotation node = parser.parseAnnotation();
        checkSimple(node, ASTNormalAnnotation.class, AT_SIGN);

        ASTNormalAnnotation na = (ASTNormalAnnotation) node.getChildren().get(0);
        checkBinary(na, AT_SIGN, ASTTypeName.class, ASTElementValuePairList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests element value pair list of element value pair.
     */
    @Test
    public void testEVPListOfEVP() {
        ClassesParser parser = getClassesParser("test = \"Test\"");
        ASTElementValuePairList node = parser.parseElementValuePairList();
        checkSimple(node, ASTElementValuePair.class, COMMA);
        node.collapseThenPrint();
    }

    /**
     * Tests element value pair list.
     */
    @Test
    public void testEVPList() {
        ClassesParser parser = getClassesParser("one = 1, two = \"two\", three = '3'");
        ASTElementValuePairList node = parser.parseElementValuePairList();
        checkList(node, COMMA, ASTElementValuePair.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests element value pair of element value.
     */
    @Test
    public void testElementValuePairOfElementValue() {
        ClassesParser parser = getClassesParser("prop = \"Conditional Expression\"");
        ASTElementValuePair node = parser.parseElementValuePair();
        checkBinary(node, EQUAL, ASTIdentifier.class, ASTElementValue.class);
        node.collapseThenPrint();
    }

    /**
     * Tests empty element value array initializer.
     */
    @Test
    public void testEVAIEmpty() {
        ClassesParser parser = getClassesParser("{}");
        ASTElementValueArrayInitializer node = parser.parseElementValueArrayInitializer();
        checkEmpty(node, OPEN_BRACE);
        node.collapseThenPrint();
    }

    /**
     * Tests element value array initializer of element value list.
     */
    @Test
    public void testEVAIOfEVList() {
        ClassesParser parser = getClassesParser("{1, \"Two\", '3'}");
        ASTElementValueArrayInitializer node = parser.parseElementValueArrayInitializer();
        checkSimple(node, ASTElementValueList.class, OPEN_BRACE);
        node.collapseThenPrint();
    }

    /**
     * Tests element value list of element value.
     */
    @Test
    public void testEVListOfEV() {
        ClassesParser parser = getClassesParser("\"Test\"");
        ASTElementValueList node = parser.parseElementValueList();
        checkSimple(node, ASTElementValue.class, COMMA);
        node.collapseThenPrint();
    }

    /**
     * Tests element value list.
     */
    @Test
    public void testEVList() {
        ClassesParser parser = getClassesParser("1, \"two\", '3'");
        ASTElementValueList node = parser.parseElementValueList();
        checkList(node, COMMA, ASTElementValue.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests element value of conditional expression.
     */
    @Test
    public void testElementValueOfConditionalExpression() {
        ClassesParser parser = getClassesParser("\"Conditional Expression\"");
        ASTElementValue node = parser.parseElementValue();
        checkSimple(node, ASTConditionalExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests element value of element value array initializer.
     */
    @Test
    public void testElementValueOfEVAI() {
        ClassesParser parser = getClassesParser("{\"Conditional Expression\"}");
        ASTElementValue node = parser.parseElementValue();
        checkSimple(node, ASTElementValueArrayInitializer.class);
        node.collapseThenPrint();
    }

    /**
     * Tests element value of annotation.
     */
    @Test
    public void testElementValueOfAnnotation() {
        ClassesParser parser = getClassesParser("@Foo");
        ASTElementValue node = parser.parseElementValue();
        checkSimple(node, ASTAnnotation.class);
        node.collapseThenPrint();
    }

    /**
     * Tests simple interface declaration.
     */
    @Test
    public void testInterfaceDeclarationSimple() {
        ClassesParser parser = getClassesParser("interface Dummy {}");
        ASTInterfaceDeclaration node = parser.parseInterfaceDeclaration();
        checkBinary(node, INTERFACE, ASTIdentifier.class, ASTInterfaceBody.class);
        node.collapseThenPrint();
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
        ASTInterfaceDeclaration node = parser.parseInterfaceDeclaration();
        checkNary(node, INTERFACE, ASTAccessModifier.class, ASTInterfaceModifierList.class, ASTIdentifier.class,
                ASTTypeParameters.class, ASTExtendsInterfaces.class, ASTPermits.class, ASTInterfaceBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface modifier list.
     */
    @Test
    public void testInterfaceModifierList() {
        ClassesParser parser = getClassesParser("shared sealed");
        ASTInterfaceModifierList node = parser.parseInterfaceModifierList();
        checkList(node, null, ASTGeneralModifier.class, 2);
        node.collapseThenPrint();
    }

    /**
     * Tests extends interfaces (extends clause on interface).
     */
    @Test
    public void testExtendsInterfaces() {
        ClassesParser parser = getClassesParser("extends Copyable, Serializable");
        ASTExtendsInterfaces node = parser.parseExtendsInterfaces();
        checkSimple(node, ASTDataTypeNoArrayList.class, EXTENDS);
        node.collapseThenPrint();
    }

    /**
     * Tests empty interface body.
     */
    @Test
    public void testInterfaceBodyEmpty() {
        ClassesParser parser = getClassesParser("{}");
        ASTInterfaceBody node = parser.parseInterfaceBody();
        checkEmpty(node, OPEN_BRACE);
        node.collapseThenPrint();
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
        ASTInterfaceBody node = parser.parseInterfaceBody();
        checkSimple(node, ASTInterfacePartList.class, OPEN_BRACE);
        node.collapseThenPrint();
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
        checkList(node, null, ASTInterfacePart.class, 9);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part list of interface part.
     */
    @Test
    public void testInterfacePartListOfInterfacePart() {
        ClassesParser parser = getClassesParser("constant Integer i = 1;");
        ASTInterfacePartList node = parser.parseInterfacePartList();
        checkSimple(node, ASTInterfacePart.class);
        node.collapseThenPrint();
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
        checkList(node, null, ASTInterfacePart.class, 2);
        node.collapseThenPrint();
    }

    /**
     * Tests nested interface part lists.
     */
    @Test
    public void testInterfacePartListNested() {
        ClassesParser parser = getClassesParser("""
                constant Integer i = 1;
                class Inner {}
                Integer getI();
                """);
        ASTInterfacePartList node = parser.parseInterfacePartList();
        checkList(node, null, ASTInterfacePart.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part of method declaration with void result.
     */
    @Test
    public void testInterfacePartOfMethodDeclarationVoidResult() {
        ClassesParser parser = getClassesParser("public void method();");
        ASTInterfacePart node = parser.parseInterfacePart();
        checkSimple(node, ASTInterfaceMethodDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part of method declaration with void result and type parameters.
     */
    @Test
    public void testInterfacePartOfMethodDeclarationVoidResultTypeParameters() {
        ClassesParser parser = getClassesParser("public <T> void method(T param);");
        ASTInterfacePart node = parser.parseInterfacePart();
        checkSimple(node, ASTInterfaceMethodDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part of method declaration data type void result.
     */
    @Test
    public void testInterfacePartOfMethodDeclarationDataTypeResult() {
        ClassesParser parser = getClassesParser("public String method();");
        ASTInterfacePart node = parser.parseInterfacePart();
        checkSimple(node, ASTInterfaceMethodDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part of method declaration with data type result and type parameters.
     */
    @Test
    public void testInterfacePartOfMethodDeclarationDataTypeResultTypeParameters() {
        ClassesParser parser = getClassesParser("public <T> T method(T param);");
        ASTInterfacePart node = parser.parseInterfacePart();
        checkSimple(node, ASTInterfaceMethodDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part of method declaration with mut result.
     */
    @Test
    public void testInterfacePartOfMethodDeclarationConstResult() {
        ClassesParser parser = getClassesParser("mut String method(String param);");
        ASTInterfacePart node = parser.parseInterfacePart();
        checkSimple(node, ASTInterfaceMethodDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part of constant declaration.
     */
    @Test
    public void testInterfacePartOfConstantDeclaration() {
        ClassesParser parser = getClassesParser("constant String LANGUAGE = \"Spruce\";");
        ASTInterfacePart node = parser.parseInterfacePart();
        checkSimple(node, ASTConstantDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part of class declaration.
     */
    @Test
    public void testInterfacePartOfClassDeclaration() {
        ClassesParser parser = getClassesParser("public shared class Nested {}");
        ASTInterfacePart node = parser.parseInterfacePart();
        checkSimple(node, ASTClassDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part of enum declaration.
     */
    @Test
    public void testInterfacePartOfEnumDeclaration() {
        ClassesParser parser = getClassesParser("private enum Light {RED, YELLOW, GREEN}");
        ASTInterfacePart node = parser.parseInterfacePart();
        checkSimple(node, ASTEnumDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part of interface declaration.
     */
    @Test
    public void testInterfacePartOfInterfaceDeclaration() {
        ClassesParser parser = getClassesParser("private interface TrafficLight { Light getStatus(); }");
        ASTInterfacePart node = parser.parseInterfacePart();
        checkSimple(node, ASTInterfaceDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part of annotation declaration.
     */
    @Test
    public void testInterfacePartOfAnnotationDeclaration() {
        ClassesParser parser = getClassesParser("public annotation Test { String getStatus() default \"SUCCESS\"; }");
        ASTInterfacePart node = parser.parseInterfacePart();
        checkSimple(node, ASTAnnotationDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part of record declaration.
     */
    @Test
    public void testInterfacePartOfRecordDeclaration() {
        ClassesParser parser = getClassesParser("internal record Redacted(String byWhom) { }");
        ASTInterfacePart node = parser.parseInterfacePart();
        checkSimple(node, ASTRecordDeclaration.class);
        node.collapseThenPrint();
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
        checkSimple(node, ASTAdtDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests simple interface method declaration.
     */
    @Test
    public void testInterfaceMethodDeclarationSimple() {
        ClassesParser parser = getClassesParser("Boolean add(T element);");
        ASTInterfaceMethodDeclaration node = parser.parseInterfaceMethodDeclaration();
        checkBinary(node, ASTMethodHeader.class, ASTMethodBody.class);
        node.collapseThenPrint();
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
        ASTInterfaceMethodDeclaration node = parser.parseInterfaceMethodDeclaration();
        checkNary(node, null, ASTAccessModifier.class, ASTInterfaceMethodModifierList.class, ASTMethodHeader.class, ASTMethodBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface method modifier list.
     */
    @Test
    public void testInterfaceMethodModifierList() {
        ClassesParser parser = getClassesParser("default override shared");
        ASTInterfaceMethodModifierList node = parser.parseInterfaceMethodModifierList();
        checkList(node, null, ASTGeneralModifier.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests bad interface method modifier list.
     */
    @Test
    public void testErrorInterfaceMethodModifierListOfConst() {
        ClassesParser parser = getClassesParser("final");
        assertThrows(CompileException.class, parser::parseInterfaceMethodModifierList);
    }

    /**
     * Tests constant declaration, no "constant".
     */
    @Test
    public void testConstantDeclaration() {
        ClassesParser parser = getClassesParser("String test = \"Test\";");
        ASTConstantDeclaration node = parser.parseConstantDeclaration();
        checkBinary(node, ASTDataType.class, ASTVariableDeclaratorList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests constant declaration with "constant".
     */
    @Test
    public void testConstantDeclarationOfConstant() {
        ClassesParser parser = getClassesParser("constant String test = \"Test\";");
        ASTConstantDeclaration node = parser.parseConstantDeclaration();
        checkTrinary(node, null, ASTConstantModifier.class, ASTDataType.class, ASTVariableDeclaratorList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests constant modifier by itself.
     */
    @Test
    public void testConstantModifier() {
        ClassesParser parser = getClassesParser("constant");
        ASTConstantModifier node = parser.parseConstantModifier();
        checkEmpty(node, CONSTANT);
        node.collapseThenPrint();
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
        ASTAccessModifier am = parser.parseAccessModifier();
        ASTAdtDeclaration node = parser.parseAdtDeclaration(am.getLocation(), am);
        checkNary(node, ADT, ASTAccessModifier.class, ASTIdentifier.class, ASTTypeParameters.class,
                ASTExtendsInterfaces.class, ASTAdtBody.class);
        node.collapseThenPrint();
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
        checkBinary(node, ADT, ASTIdentifier.class, ASTAdtBody.class);
        node.collapseThenPrint();
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
        checkBinary(node, ASTVariantList.class, ASTAdtBodyDeclarations.class);
        node.collapseThenPrint();
    }

    /**
     * Tests variant list.
     */
    @Test
    public void testVariantList() {
        ClassesParser parser = getClassesParser("Here, There(Location l) {}, Anywhere}");
        ASTVariantList node = parser.parseVariantList();
        checkList(node, COMMA, ASTVariant.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests variant of compact record declaration.
     */
    @Test
    public void testVariantOfCompactRecordDeclaration() {
        ClassesParser parser = getClassesParser("None() {}");
        ASTVariant node = parser.parseVariant();
        checkSimple(node, ASTCompactRecordDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests variant of data type.
     */
    @Test
    public void testVariantOfDataType() {
        ClassesParser parser = getClassesParser("Elsewhere,");
        ASTVariant node = parser.parseVariant();
        checkSimple(node, ASTDataType.class);
        node.collapseThenPrint();
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
        checkNary(node, null, ASTIdentifier.class, ASTTypeParameters.class, ASTRecordHeader.class,
                ASTSuperinterfaces.class, ASTClassBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests simple compact record declaration.
     */
    @Test
    public void testCompactRecordDeclarationSimple() {
        ClassesParser parser = getClassesParser("None() {}");
        ASTCompactRecordDeclaration node = parser.parseCompactRecordDeclaration();
        checkTrinary(node, null, ASTIdentifier.class, ASTRecordHeader.class, ASTClassBody.class);
        node.collapseThenPrint();
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
        ASTAdtBodyDeclarations node = parser.parseAdtBodyDeclarations();
        checkSimple(node, ASTInterfacePartList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests a full record declaration.
     */
    @Test
    public void testRecordDeclarationFull() {
        Scanner scanner = new Scanner("public record Value<T>(T value) implements Comparable<T> {}");
        ClassesParser parser = new Parser(scanner).getClassesParser();
        ASTAccessModifier am = parser.parseAccessModifier();
        ASTRecordDeclaration node = parser.parseRecordDeclaration(am.getLocation(), am);
        checkNary(node, RECORD, ASTAccessModifier.class, ASTIdentifier.class, ASTTypeParameters.class,
                ASTRecordHeader.class, ASTSuperinterfaces.class, ASTClassBody.class);
        node.collapseThenPrint();
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
        checkTrinary(node, RECORD, ASTIdentifier.class, ASTRecordHeader.class, ASTClassBody.class);
        node.collapseThenPrint();
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
        ASTRecordHeader node = parser.parseRecordHeader();
        checkSimple(node, ASTFormalParameterList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests bad compact constructor declaration.
     */
    @Test
    public void testCompactConstructorDeclarationBad() {
        ClassesParser parser = getClassesParser("private { }");
        ASTAccessModifier am = parser.parseAccessModifier();
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
        ASTAccessModifier am = parser.parseAccessModifier();
        ASTCompactConstructorDeclaration node = parser.parseCompactConstructorDeclaration(am.getLocation(), am);
        checkBinary(node, CONSTRUCTOR, ASTAccessModifier.class, ASTBlock.class);
        node.collapseThenPrint();
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
        checkSimple(node, ASTBlock.class, CONSTRUCTOR);
        node.collapseThenPrint();
    }

    /**
     * Tests simple enum declaration.
     */
    @Test
    public void testEnumDeclarationSimple() {
        ClassesParser parser = getClassesParser("enum Dummy {DUMMY}");
        ASTEnumDeclaration node = parser.parseEnumDeclaration();
        checkBinary(node, ENUM, ASTIdentifier.class, ASTEnumBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests full enum declaration.
     */
    @Test
    public void testEnumDeclarationFull() {
        ClassesParser parser = getClassesParser("public shared enum FullEnumTest implements Serializable {QUIZ, TEST, FINAL}");
        ASTEnumDeclaration node = parser.parseEnumDeclaration();
        checkNary(node, ENUM, ASTAccessModifier.class, ASTClassModifierList.class, ASTIdentifier.class,
                ASTSuperinterfaces.class, ASTEnumBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests simple enum body.
     */
    @Test
    public void testEnumBodySimple() {
        ClassesParser parser = getClassesParser("{\nRED, YELLOW, GREEN\n}");
        ASTEnumBody node = parser.parseEnumBody();
        checkSimple(node, ASTEnumConstantList.class);
    }

    /**
     * Tests enum body of nothing.
     */
    @Test
    public void testEnumBodyOfNothing() {
        ClassesParser parser = getClassesParser("{}");
        ASTEnumBody node = parser.parseEnumBody();
        checkEmpty(node, null);
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
        checkSimple(node, ASTEnumBodyDeclarations.class);
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
        checkBinary(node, ASTEnumConstantList.class, ASTEnumBodyDeclarations.class);
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
        ASTEnumBodyDeclarations node = parser.parseEnumBodyDeclarations();
        checkSimple(node, ASTClassPartList.class, SEMICOLON);
    }

    /**
     * Tests enum constant list of enum constant.
     */
    @Test
    public void testEnumConstantListOfEnumConstant() {
        ClassesParser parser = getClassesParser("SINGLETON");
        ASTEnumConstantList node = parser.parseEnumConstantList();
        checkSimple(node, ASTEnumConstant.class, COMMA);
        node.collapseThenPrint();
    }

    /**
     * Tests enum constant list.
     */
    @Test
    public void testEnumConstantList() {
        ClassesParser parser = getClassesParser("RED, YELLOW, GREEN");
        ASTEnumConstantList node = parser.parseEnumConstantList();
        checkList(node, COMMA, ASTEnumConstant.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests simple enum constant.
     */
    @Test
    public void testEnumConstantSimple() {
        ClassesParser parser = getClassesParser("RED");
        ASTEnumConstant node = parser.parseEnumConstant();
        checkSimple(node, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests full enum constant.
     */
    @Test
    public void testEnumConstantOfArgumentListClassBody() {
        ClassesParser parser = getClassesParser("RED(\"#F9152F\") { override String toString() { return \"Red Light\"; } }");
        ASTEnumConstant node = parser.parseEnumConstant();
        checkTrinary(node, null, ASTIdentifier.class, ASTArgumentList.class, ASTClassBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests simple class declaration.
     */
    @Test
    public void testClassDeclarationSimple() {
        ClassesParser parser = getClassesParser("class Dummy {}");
        ASTClassDeclaration node = parser.parseClassDeclaration();
        checkBinary(node, CLASS, ASTIdentifier.class, ASTClassBody.class);
        node.collapseThenPrint();
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
        ASTClassDeclaration node = parser.parseClassDeclaration();
        checkNary(node, CLASS, ASTAccessModifier.class, ASTClassModifierList.class, ASTIdentifier.class,
                ASTTypeParameters.class, ASTSuperclass.class, ASTSuperinterfaces.class, ASTPermits.class, ASTClassBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests permits (permits clause).
     */
    @Test
    public void testPermits() {
        ClassesParser parser = getClassesParser("permits Dog, Cat, Mouse");
        ASTPermits node = parser.parsePermits();
        checkSimple(node, ASTDataTypeNoArrayList.class, PERMITS);
        node.collapseThenPrint();
    }

    /**
     * Tests superinterfaces (implements clause).
     */
    @Test
    public void testSuperinterfaces() {
        ClassesParser parser = getClassesParser("implements Copyable");
        ASTSuperinterfaces node = parser.parseSuperinterfaces();
        checkSimple(node, ASTDataTypeNoArrayList.class, IMPLEMENTS);
        node.collapseThenPrint();
    }

    /**
     * Tests data type no array of data type no array.
     */
    @Test
    public void testDataTypeNoArrayListOfClassPart() {
        ClassesParser parser = getClassesParser("Serializable");
        ASTDataTypeNoArrayList node = parser.parseDataTypeNoArrayList();
        checkSimple(node, ASTDataTypeNoArray.class, COMMA);
        node.collapseThenPrint();
    }

    /**
     * Tests data type no array list.
     */
    @Test
    public void testDataTypeNoArrayList() {
        ClassesParser parser = getClassesParser("Serializable, Comparable<T>");
        ASTDataTypeNoArrayList node = parser.parseDataTypeNoArrayList();
        checkList(node, COMMA, ASTDataTypeNoArray.class, 2);
        node.collapseThenPrint();
    }

    /**
     * Tests nested data type no array lists.
     */
    @Test
    public void testDataTypeNoArrayListNested() {
        ClassesParser parser = getClassesParser("Serializable, Comparable<T>, RandomAccess");
        ASTDataTypeNoArrayList node = parser.parseDataTypeNoArrayList();
        checkList(node, COMMA, ASTDataTypeNoArray.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests superclass (extends clause).
     */
    @Test
    public void testSuperclass() {
        ClassesParser parser = getClassesParser("extends Thread");
        ASTSuperclass node = parser.parseSuperclass();
        checkSimple(node, ASTDataTypeNoArray.class, EXTENDS);
        node.collapseThenPrint();
    }

    /**
     * Tests class modifier list.
     */
    @Test
    public void testClassModifierList() {
        ClassesParser parser = getClassesParser("abstract final shared sealed");
        ASTClassModifierList node = parser.parseClassModifierList();
        checkList(node, null, ASTGeneralModifier.class, 4);
        node.collapseThenPrint();
    }

    /**
     * Tests empty class body.
     */
    @Test
    public void testClassBodyEmpty() {
        ClassesParser parser = getClassesParser("{}");
        ASTClassBody node = parser.parseClassBody();
        checkEmpty(node, OPEN_BRACE);
        node.collapseThenPrint();
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
        ASTClassBody node = parser.parseClassBody();
        checkSimple(node, ASTClassPartList.class, OPEN_BRACE);
        node.collapseThenPrint();
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
        checkList(node, null, ASTClassPart.class, 11);
        node.collapseThenPrint();
    }

    /**
     * Tests class part list of class part.
     */
    @Test
    public void testClassPartListOfClassPart() {
        ClassesParser parser = getClassesParser("private Integer i = 1;");
        ASTClassPartList node = parser.parseClassPartList();
        checkSimple(node, ASTClassPart.class);
        node.collapseThenPrint();
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
        checkList(node, null, ASTClassPart.class, 2);
        node.collapseThenPrint();
    }

    /**
     * Tests nested class part lists.
     */
    @Test
    public void testClassPartListNested() {
        ClassesParser parser = getClassesParser("""
            private Integer i = 1;
            constructor(Integer i) { self.i = i; }
            Integer getI() {
                return i;
            }
            """);
        ASTClassPartList node = parser.parseClassPartList();
        checkList(node, null, ASTClassPart.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of shared constructor.
     */
    @Test
    public void testClassPartOfSharedConstructor() {
        ClassesParser parser = getClassesParser("shared constructor() { sharedVar = reallyComplicatedLogic(); }");
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTSharedConstructor.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of method declaration with void result.
     */
    @Test
    public void testClassPartOfMethodDeclarationVoidResult() {
        ClassesParser parser = getClassesParser("public abstract void method();");
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTMethodDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of method declaration with void result and type parameters.
     */
    @Test
    public void testClassPartOfMethodDeclarationVoidResultTypeParameters() {
        ClassesParser parser = getClassesParser("public abstract <T> void method(T param);");
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTMethodDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of method declaration data type void result.
     */
    @Test
    public void testClassPartOfMethodDeclarationDataTypeResult() {
        ClassesParser parser = getClassesParser("public abstract String method();");
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTMethodDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of method declaration with data type result and type parameters.
     */
    @Test
    public void testClassPartOfMethodDeclarationDataTypeResultTypeParameters() {
        ClassesParser parser = getClassesParser("public abstract <T> T method(T param);");
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTMethodDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of method declaration with mut result.
     */
    @Test
    public void testClassPartOfMethodDeclarationMutResult() {
        ClassesParser parser = getClassesParser("mut String method(String param);");
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTMethodDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of field declaration.
     */
    @Test
    public void testClassPartOfFieldDeclaration() {
        ClassesParser parser = getClassesParser("private Int myVar = 1, myVar2 = 2;");
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTFieldDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of constructor declaration.
     */
    @Test
    public void testClassPartOfConstructorDeclaration() {
        ClassesParser parser = getClassesParser("constructor(String s) : constructor(s) {}");
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTConstructorDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of class declaration.
     */
    @Test
    public void testClassPartOfClassDeclaration() {
        ClassesParser parser = getClassesParser("public shared class Nested {}");
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTClassDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of enum declaration.
     */
    @Test
    public void testClassPartOfEnumDeclaration() {
        ClassesParser parser = getClassesParser("private enum Light {RED, YELLOW, GREEN}");
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTEnumDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of interface declaration.
     */
    @Test
    public void testClassPartOfInterfaceDeclaration() {
        ClassesParser parser = getClassesParser("private interface TrafficLight { Light getStatus(); }");
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTInterfaceDeclaration.class);
        node.collapseThenPrint();
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
        checkSimple(node, ASTAnnotationDeclaration.class);
        node.collapseThenPrint();
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
        checkSimple(node, ASTRecordDeclaration.class);
        node.collapseThenPrint();
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
        checkSimple(node, ASTAdtDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests shared constructor.
     */
    @Test
    public void testSharedConstructor() {
        ClassesParser parser = getClassesParser("shared constructor() { sharedVar = reallyComplicatedLogic(); }");
        ASTSharedConstructor node = parser.parseSharedConstructor();
        checkSimple(node, ASTBlock.class, CONSTRUCTOR);
        node.collapseThenPrint();
    }

    /**
     * Tests constructor declaration of access modifier, and constructor invocation.
     */
    @Test
    public void testConstructorDeclarationOfAccessConstructorInvocation() {
        ClassesParser parser = getClassesParser("private constructor(String s) : super(s) { self.s = s; }");
        ASTConstructorDeclaration node = parser.parseConstructorDeclaration();
        checkNary(node, CONSTRUCTOR, ASTAccessModifier.class,  ASTConstructorDeclarator.class, ASTConstructorInvocation.class, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests simple constructor declaration.
     */
    @Test
    public void testConstructorDeclarationSimple() {
        ClassesParser parser = getClassesParser("constructor(String s) { self.s = s; }");
        ASTConstructorDeclaration node = parser.parseConstructorDeclaration();
        checkBinary(node, CONSTRUCTOR, ASTConstructorDeclarator.class, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests constructor invocation of primary, type arguments, and super.
     */
    @Test
    public void testConstructorInvocationOfPrimaryTypeArgumentsSuper() {
        ClassesParser parser = getClassesParser(": (primary).<T>super()");
        ASTConstructorInvocation node = parser.parseConstructorInvocation();
        checkBinary(node, SUPER, ASTPrimary.class, ASTTypeArguments.class);
        node.collapseThenPrint();
    }

    /**
     * Tests constructor invocation of primary and super.
     */
    @Test
    public void testConstructorInvocationOfPrimarySuper() {
        ClassesParser parser = getClassesParser(": (primary).super()");
        ASTConstructorInvocation node = parser.parseConstructorInvocation();
        checkSimple(node, ASTPrimary.class, SUPER);
        node.collapseThenPrint();
    }

    /**
     * Tests constructor invocation of expression name, type arguments, and super.
     */
    @Test
    public void testConstructorInvocationOfExpressionNameTypeArgumentsSuper() {
        ClassesParser parser = getClassesParser(": expr.name.<String>super()");
        ASTConstructorInvocation node = parser.parseConstructorInvocation();
        checkBinary(node, SUPER, ASTExpressionName.class, ASTTypeArguments.class);
        node.collapseThenPrint();
    }

    /**
     * Tests constructor invocation of expression name and super.
     */
    @Test
    public void testConstructorInvocationOfExpressionNameSuper() {
        ClassesParser parser = getClassesParser(": expr.name.super()");
        ASTConstructorInvocation node = parser.parseConstructorInvocation();
        checkSimple(node, ASTExpressionName.class, SUPER);
        node.collapseThenPrint();
    }

    /**
     * Tests constructor invocation of super and type arguments.
     */
    @Test
    public void testConstructorInvocationOfSuperTypeArguments() {
        ClassesParser parser = getClassesParser(": <Integer>super(5)");
        ASTConstructorInvocation node = parser.parseConstructorInvocation();
        checkBinary(node, SUPER, ASTTypeArguments.class, ASTArgumentList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests constructor invocation of constructor and type arguments.
     */
    @Test
    public void testConstructorInvocationOfConstructorTypeArguments() {
        ClassesParser parser = getClassesParser(": <Integer>constructor()");
        ASTConstructorInvocation node = parser.parseConstructorInvocation();
        checkSimple(node, ASTTypeArguments.class, CONSTRUCTOR);
        node.collapseThenPrint();
    }

    /**
     * Tests simple constructor invocation of constructor.
     */
    @Test
    public void testConstructorInvocationOfConstructorSimple() {
        ClassesParser parser = getClassesParser(": constructor()");
        ASTConstructorInvocation node = parser.parseConstructorInvocation();
        checkEmpty(node, CONSTRUCTOR);
        node.collapseThenPrint();
    }

    /**
     * Tests simple constructor declarator.
     */
    @Test
    public void testConstructorDeclaratorSimple() {
        ClassesParser parser = getClassesParser("constructor()");
        ASTConstructorDeclarator node = parser.parseConstructorDeclarator();
        checkEmpty(node, CONSTRUCTOR);
        node.collapseThenPrint();
    }

    /**
     * Tests full constructor declarator.
     */
    @Test
    public void testConstructorDeclaratorFull() {
        ClassesParser parser = getClassesParser("<T> constructor(T param)");
        ASTConstructorDeclarator node = parser.parseConstructorDeclarator();
        checkBinary(node, CONSTRUCTOR, ASTTypeParameters.class, ASTFormalParameterList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests full field declaration.
     */
    @Test
    public void testFieldDeclaration() {
        ClassesParser parser = getClassesParser("public constant String aConstant = \"CONSTANT\";");
        ASTFieldDeclaration node = parser.parseFieldDeclaration();
        checkNary(node, null, ASTAccessModifier.class, ASTFieldModifierList.class, ASTDataType.class, ASTVariableDeclaratorList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests constant field declaration.
     */
    @Test
    public void testFieldDeclarationOfConstant() {
        ClassesParser parser = getClassesParser("public constant String aConstant = \"CONSTANT\";");
        ASTFieldDeclaration node = parser.parseFieldDeclaration();
        checkNary(node, null, ASTAccessModifier.class, ASTFieldModifierList.class, ASTDataType.class, ASTVariableDeclaratorList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests simple field declaration.
     */
    @Test
    public void testFieldDeclarationSimple() {
        ClassesParser parser = getClassesParser("String name = \"spruce\";");
        ASTFieldDeclaration node = parser.parseFieldDeclaration();
        checkBinary(node, ASTDataType.class, ASTVariableDeclaratorList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests field modifier list.
     */
    @Test
    public void testFieldModifierList() {
        ClassesParser parser = getClassesParser("constant var mut shared volatile");
        ASTFieldModifierList node = parser.parseFieldModifierList();
        checkList(node, null, ASTGeneralModifier.class, 5);
        node.collapseThenPrint();
    }

    /**
     * Tests bad field modifier list.
     */
    @Test
    public void testErrorFieldModifierListOfOverride() {
        ClassesParser parser = getClassesParser("override");
        assertThrows(CompileException.class, parser::parseFieldModifierList);
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
        ASTMethodDeclaration node = parser.parseMethodDeclaration();
        checkBinary(node, ASTMethodHeader.class, ASTMethodBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests method declaration with access modifier and method modifier.
     */
    @Test
    public void testMethodDeclarationAccessModifierMethodModifier() {
        ClassesParser parser = getClassesParser("public abstract Foo abstractMethod();");
        ASTMethodDeclaration node = parser.parseMethodDeclaration();
        checkNary(node, null, ASTAccessModifier.class, ASTMethodModifierList.class, ASTMethodHeader.class, ASTMethodBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests method body of semicolon.
     */
    @Test
    public void testMethodBodyOfSemicolon() {
        ClassesParser parser = getClassesParser(";");
        ASTMethodBody node = parser.parseMethodBody();
        checkEmpty(node, SEMICOLON);
        node.collapseThenPrint();
    }

    /**
     * Tests method body of block.
     */
    @Test
    public void testMethodBodyOfBlock() {
        ClassesParser parser = getClassesParser("{\n    out.println(\"Body!\");\n}");
        ASTMethodBody node = parser.parseMethodBody();
        checkSimple(node, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests access modifier list of access modifier.
     */
    @Test
    public void testAccessModifierListOfAccessModifier() {
        ClassesParser parser = getClassesParser("final");
        ASTMethodModifierList node = parser.parseMethodModifierList();
        checkSimple(node, ASTGeneralModifier.class);
        node.collapseThenPrint();
    }
    /**
     * Tests access modifier list of access modifiers.
     */
    @Test
    public void testAccessModifierListOfAccessModifiers() {
        ClassesParser parser = getClassesParser("final abstract shared");
        ASTMethodModifierList node = parser.parseMethodModifierList();
        checkList(node, null, ASTGeneralModifier.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests method modifier of public.
     */
    @Test
    public void testAccessModifierOfPublic() {
        ClassesParser parser = getClassesParser("public");
        ASTAccessModifier node = parser.parseAccessModifier();
        checkEmpty(node, PUBLIC);
        node.collapseThenPrint();
    }

    /**
     * Tests method modifier of protected.
     */
    @Test
    public void testAccessModifierOfProtected() {
        ClassesParser parser = getClassesParser("protected");
        ASTAccessModifier node = parser.parseAccessModifier();
        checkEmpty(node, PROTECTED);
        node.collapseThenPrint();
    }

    /**
     * Tests method modifier of abstract.
     */
    @Test
    public void testAccessModifierOfInternal() {
        ClassesParser parser = getClassesParser("internal");
        ASTAccessModifier node = parser.parseAccessModifier();
        checkEmpty(node, INTERNAL);
        node.collapseThenPrint();
    }

    /**
     * Tests access modifier of private.
     */
    @Test
    public void testAccessModifierOfPrivate() {
        ClassesParser parser = getClassesParser("private");
        ASTAccessModifier node = parser.parseAccessModifier();
        checkEmpty(node, PRIVATE);
        node.collapseThenPrint();
    }

    /**
     * Tests method modifier list.
     */
    @Test
    public void testMethodModifierList() {
        ClassesParser parser = getClassesParser("abstract final override shared");
        ASTMethodModifierList node = parser.parseMethodModifierList();
        checkList(node, null, ASTGeneralModifier.class, 4);
        node.collapseThenPrint();
    }

    /**
     * Tests bad method modifier list.
     */
    @Test
    public void testErrorMethodModifierListOfConst() {
        ClassesParser parser = getClassesParser("const");
        assertThrows(CompileException.class, parser::parseMethodModifierList);
    }

    /**
     * Tests method modifier list of method modifiers.
     */
    @Test
    public void testGeneralModifierListOfMethodModifiers() {
        ClassesParser parser = getClassesParser("abstract mut var override shared volatile");
        ASTGeneralModifierList node = parser.parseGeneralModifierList();
        checkList(node, null, ASTGeneralModifier.class, 6);
        node.collapseThenPrint();
    }

    /**
     * Tests general modifier of abstract.
     */
    @Test
    public void testGeneralModifierOfAbstract() {
        ClassesParser parser = getClassesParser("abstract");
        ASTGeneralModifier node = parser.parseGeneralModifier();
        checkEmpty(node, ABSTRACT);
        node.collapseThenPrint();
    }

    /**
     * Tests general modifier of mut.
     */
    @Test
    public void testGeneralModifierOfConst() {
        ClassesParser parser = getClassesParser("mut");
        ASTGeneralModifier node = parser.parseGeneralModifier();
        checkEmpty(node, MUT);
        node.collapseThenPrint();
    }

    /**
     * Tests general modifier of var.
     */
    @Test
    public void testMethodModifierOfVar() {
        ClassesParser parser = getClassesParser("var");
        ASTGeneralModifier node = parser.parseGeneralModifier();
        checkEmpty(node, VAR);
        node.collapseThenPrint();
    }

    /**
     * Tests general modifier of override.
     */
    @Test
    public void testGeneralModifierOfOverride() {
        ClassesParser parser = getClassesParser("override");
        ASTGeneralModifier node = parser.parseGeneralModifier();
        checkEmpty(node, OVERRIDE);
        node.collapseThenPrint();
    }

    /**
     * Tests general modifier of shared.
     */
    @Test
    public void testGeneralModifierOfShared() {
        ClassesParser parser = getClassesParser("shared");
        ASTGeneralModifier node = parser.parseGeneralModifier();
        checkEmpty(node, SHARED);
        node.collapseThenPrint();
    }

    /**
     * Tests general modifier of volatile.
     */
    @Test
    public void testGeneralModifierOfVolatile() {
        ClassesParser parser = getClassesParser("volatile");
        ASTGeneralModifier node = parser.parseGeneralModifier();
        checkEmpty(node, VOLATILE);
        node.collapseThenPrint();
    }

    /**
     * Tests simple method header.
     */
    @Test
    public void testMethodHeaderSimple() {
        ClassesParser parser = getClassesParser("void toString() const");
        ASTMethodHeader node = parser.parseMethodHeader();
        checkBinary(node, ASTResult.class, ASTMethodDeclarator.class);
        node.collapseThenPrint();
    }

    /**
     * Tests method header with type parameters.
     */
    @Test
    public void testMethodHeaderOfTypeParameters() {
        ClassesParser parser = getClassesParser("<T> T getItem() const");
        ASTMethodHeader node = parser.parseMethodHeader();
        checkTrinary(node, null, ASTTypeParameters.class, ASTResult.class, ASTMethodDeclarator.class);
        node.collapseThenPrint();
    }

    /**
     * Tests result of void.
     */
    @Test
    public void testResultOfVoid() {
        ClassesParser parser = getClassesParser("void");
        ASTResult node = parser.parseResult();
        checkEmpty(node, VOID);
        node.collapseThenPrint();
    }

    /**
     * Tests result of data type.
     */
    @Test
    public void testResultOfDataType() {
        ClassesParser parser = getClassesParser("Map<String, Integer>");
        ASTResult node = parser.parseResult();
        checkSimple(node, ASTDataType.class);
        node.collapseThenPrint();
    }

    /**
     * Tests result of var modifier and data type.
     */
    @Test
    public void testResultOfConstModifierDataType() {
        ClassesParser parser = getClassesParser("mut Map<String, Integer>");
        ASTResult node = parser.parseResult();
        checkBinary(node, ASTMutModifier.class, ASTDataType.class);
        node.collapseThenPrint();
    }

    /**
     * Tests simple method declarator.
     */
    @Test
    public void testMethodDeclaratorSimple() {
        ClassesParser parser = getClassesParser("update()");
        ASTMethodDeclarator node = parser.parseMethodDeclarator();
        checkSimple(node, ASTIdentifier.class, OPEN_PARENTHESIS);
        node.collapseThenPrint();
    }

    /**
     * Tests method declarator of parameter list and mut modifier.
     */
    @Test
    public void testMethodDeclaratorOfParameterListConstModifier() {
        ClassesParser parser = getClassesParser("join(String sep) mut)");
        ASTMethodDeclarator node = parser.parseMethodDeclarator();
        checkTrinary(node, OPEN_PARENTHESIS, ASTIdentifier.class, ASTFormalParameterList.class, ASTMutModifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests mut modifier by itself.
     */
    @Test
    public void testMutModifier() {
        ClassesParser parser = getClassesParser("mut");
        ASTMutModifier node = parser.parseMutModifier();
        checkEmpty(node, MUT);
        node.collapseThenPrint();
    }

    /**
     * Tests formal parameter list of formal parameter.
     */
    @Test
    public void testFormalParameterListOfFormalParameter() {
        ClassesParser parser = getClassesParser("const Int a");
        ASTFormalParameterList node = parser.parseFormalParameterList();
        checkSimple(node, ASTFormalParameter.class, COMMA);
        node.collapseThenPrint();
    }

    /**
     * Tests formal parameter list.
     */
    @Test
    public void testFormalParameterList() {
        ClassesParser parser = getClassesParser("String msg, Foo f, Bar b");
        ASTFormalParameterList node = parser.parseFormalParameterList();
        checkList(node, COMMA, ASTFormalParameter.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests formal parameter list with varargs parameter list.
     */
    @Test
    public void testFormalParameterListOfLastVarargs() {
        ClassesParser parser = getClassesParser("Point pt, Double... coordinates");
        ASTFormalParameterList node = parser.parseFormalParameterList();
        checkList(node, COMMA, ASTFormalParameter.class, 2);
        node.collapseThenPrint();
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
        checkBinary(node, THREE_DOTS, ASTDataType.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests formal parameter, variable modifier list, with ellipsis.
     */
    @Test
    public void testFormalParameterOfVMLEllipsis() {
        ClassesParser parser = getClassesParser("mut String... args");
        ASTFormalParameter node = parser.parseFormalParameter();
        checkTrinary(node, THREE_DOTS, ASTVariableModifierList.class, ASTDataType.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests formal parameter, no variable modifier list, no ellipsis.
     */
    @Test
    public void testFormalParameterNoVMLNoEllipsis() {
        ClassesParser parser = getClassesParser("String[] args");
        ASTFormalParameter node = parser.parseFormalParameter();
        checkBinary(node, ASTDataType.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests formal parameter, variable modifier list, no ellipsis.
     */
    @Test
    public void testFormalParameterOfVMLNoEllipsis() {
        ClassesParser parser = getClassesParser("mut String[] args");
        ASTFormalParameter node = parser.parseFormalParameter();
        checkTrinary(node, null, ASTVariableModifierList.class, ASTDataType.class, ASTIdentifier.class);
        node.collapseThenPrint();
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
