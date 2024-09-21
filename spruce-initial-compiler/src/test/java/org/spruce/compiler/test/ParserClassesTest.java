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
        ClassesParser parser = new Parser(new Scanner("annotation Dummy {}")).getClassesParser();
        ASTAnnotationDeclaration node = parser.parseAnnotationDeclaration();
        checkBinary(node, ANNOTATION, ASTIdentifier.class, ASTAnnotationBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests full annotation declaration.
     */
    @Test
    public void testAnnotationDeclarationFull() {
        ClassesParser parser = new Parser(new Scanner("""
            public shared annotation AFullTest {
                String prop();
            }
            """)).getClassesParser();
        ASTAnnotationDeclaration node = parser.parseAnnotationDeclaration();
        checkNary(node, ANNOTATION, ASTAccessModifier.class, ASTInterfaceModifierList.class, ASTIdentifier.class, ASTAnnotationBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests empty annotation body.
     */
    @Test
    public void testAnnotationBodyEmpty() {
        ClassesParser parser = new Parser(new Scanner("{}")).getClassesParser();
        ASTAnnotationBody node = parser.parseAnnotationBody();
        checkEmpty(node, OPEN_BRACE);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation body.
     */
    @Test
    public void testAnnotationBody() {
        ClassesParser parser = new Parser(new Scanner("""
                {
                    constant Integer i = 1;
                    class Inner{}
                    Integer getI() default 1;}
                }
                """)).getClassesParser();
        ASTAnnotationBody node = parser.parseAnnotationBody();
        checkSimple(node, ASTAnnotationPartList.class, OPEN_BRACE);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation part list of annotation part.
     */
    @Test
    public void testAnnotationPartListOfAnnotationPart() {
        ClassesParser parser = new Parser(new Scanner("constant Integer i = 1;")).getClassesParser();
        ASTAnnotationPartList node = parser.parseAnnotationPartList();
        checkSimple(node, ASTAnnotationPart.class);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation part list.
     */
    @Test
    public void testAnnotationPartList() {
        ClassesParser parser = new Parser(new Scanner("""
            constant Integer i = 1;
            class Inner {}
            """)).getClassesParser();
        ASTAnnotationPartList node = parser.parseAnnotationPartList();
        checkList(node, null, ASTAnnotationPart.class, 2);
        node.collapseThenPrint();
    }

    /**
     * Tests nested annotation part lists.
     */
    @Test
    public void testAnnotationPartListNested() {
        ClassesParser parser = new Parser(new Scanner("""
            constant Integer i = 1;
            class Inner {}
            Integer getI() default 1;
            """)).getClassesParser();
        ASTAnnotationPartList node = parser.parseAnnotationPartList();
        checkList(node, null, ASTAnnotationPart.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation part of annotation type element declaration.
     */
    @Test
    public void testAnnotationPartOfATED() {
        ClassesParser parser = new Parser(new Scanner("String element() default \"Test\";")).getClassesParser();
        ASTAnnotationPart node = parser.parseAnnotationPart();
        checkSimple(node, ASTAnnotationTypeElementDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation part of constant declaration.
     */
    @Test
    public void testAnnotationPartOfConstantDeclaration() {
        ClassesParser parser = new Parser(new Scanner("constant String LANGUAGE = \"Spruce\";")).getClassesParser();
        ASTAnnotationPart node = parser.parseAnnotationPart();
        checkSimple(node, ASTConstantDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation part of class declaration.
     */
    @Test
    public void testAnnotationPartOfClassDeclaration() {
        ClassesParser parser = new Parser(new Scanner("public shared class Nested {}")).getClassesParser();
        ASTAnnotationPart node = parser.parseAnnotationPart();
        checkSimple(node, ASTClassDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation part of enum declaration.
     */
    @Test
    public void testAnnotationPartOfEnumDeclaration() {
        ClassesParser parser = new Parser(new Scanner("private enum Light {RED, YELLOW, GREEN}")).getClassesParser();
        ASTAnnotationPart node = parser.parseAnnotationPart();
        checkSimple(node, ASTEnumDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation part of interface declaration.
     */
    @Test
    public void testAnnotationPartOfInterfaceDeclaration() {
        ClassesParser parser = new Parser(new Scanner("private interface TrafficLight { Light getStatus(); }")).getClassesParser();
        ASTAnnotationPart node = parser.parseAnnotationPart();
        checkSimple(node, ASTInterfaceDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation part of annotation declaration.
     */
    @Test
    public void testAnnotationPartOfAnnotationDeclaration() {
        ClassesParser parser = new Parser(new Scanner("public annotation Test { String getStatus() default \"SUCCESS\"; }")).getClassesParser();
        ASTAnnotationPart node = parser.parseAnnotationPart();
        checkSimple(node, ASTAnnotationDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation part of record declaration.
     */
    @Test
    public void testAnnotationPartOfRecordDeclaration() {
        ClassesParser parser = new Parser(new Scanner("internal record Redacted(String byWhom) { }")).getClassesParser();
        ASTAnnotationPart node = parser.parseAnnotationPart();
        checkSimple(node, ASTRecordDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation type element declaration.
     */
    @Test
    public void testATED() {
        ClassesParser parser = new Parser(new Scanner("String element();")).getClassesParser();
        ASTAnnotationTypeElementDeclaration node = parser.parseAnnotationTypeElementDeclaration();
        checkBinary(node, OPEN_PARENTHESIS, ASTDataType.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation type element declaration with default value.
     */
    @Test
    public void testATEDDefaultValue() {
        ClassesParser parser = new Parser(new Scanner("String element() default \"DNE\";")).getClassesParser();
        ASTAnnotationTypeElementDeclaration node = parser.parseAnnotationTypeElementDeclaration();
        checkTrinary(node, OPEN_PARENTHESIS, ASTDataType.class, ASTIdentifier.class, ASTDefaultValue.class);
        node.collapseThenPrint();
    }

    /**
     * Tests default value.
     */
    @Test
    public void testDefaultValue() {
        ClassesParser parser = new Parser(new Scanner("default {\"default\", \"value\"}")).getClassesParser();
        ASTDefaultValue node = parser.parseDefaultValue();
        checkSimple(node, ASTElementValue.class, DEFAULT);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation of marker annotation.
     */
    @Test
    public void testAnnotationOfMarkerAnnotation() {
        ClassesParser parser = new Parser(new Scanner("@Test")).getClassesParser();
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
        ClassesParser parser = new Parser(new Scanner("@Test(\"Test\")")).getClassesParser();
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
        ClassesParser parser = new Parser(new Scanner("@Empty()")).getClassesParser();
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
        ClassesParser parser = new Parser(new Scanner("@Many(one = 1, two = \"two\", three = '3')")).getClassesParser();
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
        ClassesParser parser = new Parser(new Scanner("test = \"Test\"")).getClassesParser();
        ASTElementValuePairList node = parser.parseElementValuePairList();
        checkSimple(node, ASTElementValuePair.class, COMMA);
        node.collapseThenPrint();
    }

    /**
     * Tests element value pair list.
     */
    @Test
    public void testEVPList() {
        ClassesParser parser = new Parser(new Scanner("one = 1, two = \"two\", three = '3'")).getClassesParser();
        ASTElementValuePairList node = parser.parseElementValuePairList();
        checkList(node, COMMA, ASTElementValuePair.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests element value pair of element value.
     */
    @Test
    public void testElementValuePairOfElementValue() {
        ClassesParser parser = new Parser(new Scanner("prop = \"Conditional Expression\"")).getClassesParser();
        ASTElementValuePair node = parser.parseElementValuePair();
        checkBinary(node, EQUAL, ASTIdentifier.class, ASTElementValue.class);
        node.collapseThenPrint();
    }

    /**
     * Tests empty element value array initializer.
     */
    @Test
    public void testEVAIEmpty() {
        ClassesParser parser = new Parser(new Scanner("{}")).getClassesParser();
        ASTElementValueArrayInitializer node = parser.parseElementValueArrayInitializer();
        checkEmpty(node, OPEN_BRACE);
        node.collapseThenPrint();
    }

    /**
     * Tests element value array initializer of element value list.
     */
    @Test
    public void testEVAIOfEVList() {
        ClassesParser parser = new Parser(new Scanner("{1, \"Two\", '3'}")).getClassesParser();
        ASTElementValueArrayInitializer node = parser.parseElementValueArrayInitializer();
        checkSimple(node, ASTElementValueList.class, OPEN_BRACE);
        node.collapseThenPrint();
    }

    /**
     * Tests element value list of element value.
     */
    @Test
    public void testEVListOfEV() {
        ClassesParser parser = new Parser(new Scanner("\"Test\"")).getClassesParser();
        ASTElementValueList node = parser.parseElementValueList();
        checkSimple(node, ASTElementValue.class, COMMA);
        node.collapseThenPrint();
    }

    /**
     * Tests element value list.
     */
    @Test
    public void testEVList() {
        ClassesParser parser = new Parser(new Scanner("1, \"two\", '3'")).getClassesParser();
        ASTElementValueList node = parser.parseElementValueList();
        checkList(node, COMMA, ASTElementValue.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests element value of conditional expression.
     */
    @Test
    public void testElementValueOfConditionalExpression() {
        ClassesParser parser = new Parser(new Scanner("\"Conditional Expression\"")).getClassesParser();
        ASTElementValue node = parser.parseElementValue();
        checkSimple(node, ASTConditionalExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests element value of element value array initializer.
     */
    @Test
    public void testElementValueOfEVAI() {
        ClassesParser parser = new Parser(new Scanner("{\"Conditional Expression\"}")).getClassesParser();
        ASTElementValue node = parser.parseElementValue();
        checkSimple(node, ASTElementValueArrayInitializer.class);
        node.collapseThenPrint();
    }

    /**
     * Tests element value of annotation.
     */
    @Test
    public void testElementValueOfAnnotation() {
        ClassesParser parser = new Parser(new Scanner("@Foo")).getClassesParser();
        ASTElementValue node = parser.parseElementValue();
        checkSimple(node, ASTAnnotation.class);
        node.collapseThenPrint();
    }

    /**
     * Tests simple interface declaration.
     */
    @Test
    public void testInterfaceDeclarationSimple() {
        ClassesParser parser = new Parser(new Scanner("interface Dummy {}")).getClassesParser();
        ASTInterfaceDeclaration node = parser.parseInterfaceDeclaration();
        checkBinary(node, INTERFACE, ASTIdentifier.class, ASTInterfaceBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests full interface declaration.
     */
    @Test
    public void testInterfaceDeclarationFull() {
        ClassesParser parser = new Parser(new Scanner("""
            public shared interface IFullTest<T> extends ITest<T>, Serializable, List<T>
                permits FinalTest, UnitTest, Test, Quiz, PopQuiz
            {}
            """)).getClassesParser();
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
        ClassesParser parser = new Parser(new Scanner("shared sealed")).getClassesParser();
        ASTInterfaceModifierList node = parser.parseInterfaceModifierList();
        checkList(node, null, ASTGeneralModifier.class, 2);
        node.collapseThenPrint();
    }

    /**
     * Tests extends interfaces (extends clause on interface).
     */
    @Test
    public void testExtendsInterfaces() {
        ClassesParser parser = new Parser(new Scanner("extends Copyable, Serializable")).getClassesParser();
        ASTExtendsInterfaces node = parser.parseExtendsInterfaces();
        checkSimple(node, ASTDataTypeNoArrayList.class, EXTENDS);
        node.collapseThenPrint();
    }

    /**
     * Tests empty interface body.
     */
    @Test
    public void testInterfaceBodyEmpty() {
        ClassesParser parser = new Parser(new Scanner("{}")).getClassesParser();
        ASTInterfaceBody node = parser.parseInterfaceBody();
        checkEmpty(node, OPEN_BRACE);
        node.collapseThenPrint();
    }

    /**
     * Tests interface body.
     */
    @Test
    public void testInterfaceBody() {
        ClassesParser parser = new Parser(new Scanner("""
                {
                    constant Integer i = 1;
                    class Inner{}
                    default Integer getI() {
                        return i;
                    }
                }
                """)).getClassesParser();
        ASTInterfaceBody node = parser.parseInterfaceBody();
        checkSimple(node, ASTInterfacePartList.class, OPEN_BRACE);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part list of interface part.
     */
    @Test
    public void testInterfacePartListOfInterfacePart() {
        ClassesParser parser = new Parser(new Scanner("constant Integer i = 1;")).getClassesParser();
        ASTInterfacePartList node = parser.parseInterfacePartList();
        checkSimple(node, ASTInterfacePart.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part list.
     */
    @Test
    public void testInterfacePartList() {
        ClassesParser parser = new Parser(new Scanner("""
            constant Integer i = 1;
            class Inner {}
            """)).getClassesParser();
        ASTInterfacePartList node = parser.parseInterfacePartList();
        checkList(node, null, ASTInterfacePart.class, 2);
        node.collapseThenPrint();
    }

    /**
     * Tests nested interface part lists.
     */
    @Test
    public void testInterfacePartListNested() {
        ClassesParser parser = new Parser(new Scanner("""
                constant Integer i = 1;
                class Inner {}
                Integer getI();
                """)).getClassesParser();
        ASTInterfacePartList node = parser.parseInterfacePartList();
        checkList(node, null, ASTInterfacePart.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part of method declaration with void result.
     */
    @Test
    public void testInterfacePartOfMethodDeclarationVoidResult() {
        ClassesParser parser = new Parser(new Scanner("public void method();")).getClassesParser();
        ASTInterfacePart node = parser.parseInterfacePart();
        checkSimple(node, ASTInterfaceMethodDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part of method declaration with void result and type parameters.
     */
    @Test
    public void testInterfacePartOfMethodDeclarationVoidResultTypeParameters() {
        ClassesParser parser = new Parser(new Scanner("public <T> void method(T param);")).getClassesParser();
        ASTInterfacePart node = parser.parseInterfacePart();
        checkSimple(node, ASTInterfaceMethodDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part of method declaration data type void result.
     */
    @Test
    public void testInterfacePartOfMethodDeclarationDataTypeResult() {
        ClassesParser parser = new Parser(new Scanner("public String method();")).getClassesParser();
        ASTInterfacePart node = parser.parseInterfacePart();
        checkSimple(node, ASTInterfaceMethodDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part of method declaration with data type result and type parameters.
     */
    @Test
    public void testInterfacePartOfMethodDeclarationDataTypeResultTypeParameters() {
        ClassesParser parser = new Parser(new Scanner("public <T> T method(T param);")).getClassesParser();
        ASTInterfacePart node = parser.parseInterfacePart();
        checkSimple(node, ASTInterfaceMethodDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part of method declaration with mut result.
     */
    @Test
    public void testInterfacePartOfMethodDeclarationConstResult() {
        ClassesParser parser = new Parser(new Scanner("mut String method(String param);")).getClassesParser();
        ASTInterfacePart node = parser.parseInterfacePart();
        checkSimple(node, ASTInterfaceMethodDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part of constant declaration.
     */
    @Test
    public void testInterfacePartOfConstantDeclaration() {
        ClassesParser parser = new Parser(new Scanner("constant String LANGUAGE = \"Spruce\";")).getClassesParser();
        ASTInterfacePart node = parser.parseInterfacePart();
        checkSimple(node, ASTConstantDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part of class declaration.
     */
    @Test
    public void testInterfacePartOfClassDeclaration() {
        ClassesParser parser = new Parser(new Scanner("public shared class Nested {}")).getClassesParser();
        ASTInterfacePart node = parser.parseInterfacePart();
        checkSimple(node, ASTClassDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part of enum declaration.
     */
    @Test
    public void testInterfacePartOfEnumDeclaration() {
        ClassesParser parser = new Parser(new Scanner("private enum Light {RED, YELLOW, GREEN}")).getClassesParser();
        ASTInterfacePart node = parser.parseInterfacePart();
        checkSimple(node, ASTEnumDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part of interface declaration.
     */
    @Test
    public void testInterfacePartOfInterfaceDeclaration() {
        ClassesParser parser = new Parser(new Scanner("private interface TrafficLight { Light getStatus(); }")).getClassesParser();
        ASTInterfacePart node = parser.parseInterfacePart();
        checkSimple(node, ASTInterfaceDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part of annotation declaration.
     */
    @Test
    public void testInterfacePartOfAnnotationDeclaration() {
        ClassesParser parser = new Parser(new Scanner("public annotation Test { String getStatus() default \"SUCCESS\"; }")).getClassesParser();
        ASTInterfacePart node = parser.parseInterfacePart();
        checkSimple(node, ASTAnnotationDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part of record declaration.
     */
    @Test
    public void testInterfacePartOfRecordDeclaration() {
        ClassesParser parser = new Parser(new Scanner("internal record Redacted(String byWhom) { }")).getClassesParser();
        ASTInterfacePart node = parser.parseInterfacePart();
        checkSimple(node, ASTRecordDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests simple interface method declaration.
     */
    @Test
    public void testInterfaceMethodDeclarationSimple() {
        ClassesParser parser = new Parser(new Scanner("Boolean add(T element);")).getClassesParser();
        ASTInterfaceMethodDeclaration node = parser.parseInterfaceMethodDeclaration();
        checkBinary(node, ASTMethodHeader.class, ASTMethodBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface method declaration with access modifier and method modifier.
     */
    @Test
    public void testInterfaceMethodDeclarationAccessModifierMethodModifier() {
        ClassesParser parser = new Parser(new Scanner("""
            private default void addAll(Collection<T> other) {
                for (T element : other) {
                    add(other);
                }
            }
            """)).getClassesParser();
        ASTInterfaceMethodDeclaration node = parser.parseInterfaceMethodDeclaration();
        checkNary(node, null, ASTAccessModifier.class, ASTInterfaceMethodModifierList.class, ASTMethodHeader.class, ASTMethodBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface method modifier list.
     */
    @Test
    public void testInterfaceMethodModifierList() {
        ClassesParser parser = new Parser(new Scanner("default override shared")).getClassesParser();
        ASTInterfaceMethodModifierList node = parser.parseInterfaceMethodModifierList();
        checkList(node, null, ASTGeneralModifier.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests bad interface method modifier list.
     */
    @Test
    public void testErrorInterfaceMethodModifierListOfConst() {
        ClassesParser parser = new Parser(new Scanner("final")).getClassesParser();
        assertThrows(CompileException.class, parser::parseInterfaceMethodModifierList);
    }

    /**
     * Tests constant declaration, no "constant".
     */
    @Test
    public void testConstantDeclaration() {
        ClassesParser parser = new Parser(new Scanner("String test = \"Test\";")).getClassesParser();
        ASTConstantDeclaration node = parser.parseConstantDeclaration();
        checkBinary(node, ASTDataType.class, ASTVariableDeclaratorList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests constant declaration with "constant".
     */
    @Test
    public void testConstantDeclarationOfConstant() {
        ClassesParser parser = new Parser(new Scanner("constant String test = \"Test\";")).getClassesParser();
        ASTConstantDeclaration node = parser.parseConstantDeclaration();
        checkTrinary(node, null, ASTConstantModifier.class, ASTDataType.class, ASTVariableDeclaratorList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests constant modifier by itself.
     */
    @Test
    public void testConstantModifier() {
        ClassesParser parser = new Parser(new Scanner("constant")).getClassesParser();
        ASTConstantModifier node = parser.parseConstantModifier();
        checkEmpty(node, CONSTANT);
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
        ClassesParser parser = new Parser(new Scanner("(String filename, Int lineNbr")).getClassesParser();
        assertThrows(CompileException.class, parser::parseRecordHeader, "Missing ')'.");
    }

    /**
     * Tests bad Record Header, missing open parenthesis.
     */
    @Test
    public void testRecordHeaderMissingOpenParen() {
        ClassesParser parser = new Parser(new Scanner("String filename, Int lineNbr)")).getClassesParser();
        assertThrows(CompileException.class, parser::parseRecordHeader, "Missing '('.");
    }

    /**
     * Tests a Record Header.
     */
    @Test
    public void testRecordHeader() {
        ClassesParser parser = new Parser(new Scanner("(String filename, Int lineNbr)")).getClassesParser();
        ASTRecordHeader node = parser.parseRecordHeader();
        checkSimple(node, ASTFormalParameterList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests bad compact constructor declaration.
     */
    @Test
    public void testCompactConstructorDeclarationBad() {
        ClassesParser parser = new Parser(new Scanner("private { }")).getClassesParser();
        ASTAccessModifier am = parser.parseAccessModifier();
        assertThrows(CompileException.class, () -> parser.parseCompactConstructorDeclaration(am.getLocation(), am));
    }

    /**
     * Tests a compact constructor declaration, with access modifier.
     */
    @Test
    public void testCompactConstructorDeclarationAccessModifier() {
        ClassesParser parser = new Parser(new Scanner("""
                public constructor {
                    a *= 2;
                    b /= 2;
                }
                """)).getClassesParser();
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
        ClassesParser parser = new Parser(new Scanner("enum Dummy {DUMMY}")).getClassesParser();
        ASTEnumDeclaration node = parser.parseEnumDeclaration();
        checkBinary(node, ENUM, ASTIdentifier.class, ASTEnumBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests full enum declaration.
     */
    @Test
    public void testEnumDeclarationFull() {
        ClassesParser parser = new Parser(new Scanner("public shared enum FullEnumTest implements Serializable {QUIZ, TEST, FINAL}")).getClassesParser();
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
        ClassesParser parser = new Parser(new Scanner("{\nRED, YELLOW, GREEN\n}")).getClassesParser();
        ASTEnumBody node = parser.parseEnumBody();
        checkSimple(node, ASTEnumConstantList.class);
    }

    /**
     * Tests enum body of nothing.
     */
    @Test
    public void testEnumBodyOfNothing() {
        ClassesParser parser = new Parser(new Scanner("{}")).getClassesParser();
        ASTEnumBody node = parser.parseEnumBody();
        checkEmpty(node, null);
    }

    /**
     * Tests enum body of utility methods.
     */
    @Test
    public void testEnumBodyOfUtility() {
        ClassesParser parser = new Parser(new Scanner("""
                {;
                    shared void utility() {
                        out.println("Utility!");
                    }
                }
                """)).getClassesParser();
        ASTEnumBody node = parser.parseEnumBody();
        checkSimple(node, ASTEnumBodyDeclarations.class);
    }

    /**
     * Tests enum body of constants and class part list.
     */
    @Test
    public void testEnumBodyOfConstantsClassPartList() {
        ClassesParser parser = new Parser(new Scanner("""
                {
                    RED, YELLOW, GREEN;
                    shared void utility() {
                        out.println("Utility!");
                    }
                }
                """)).getClassesParser();
        ASTEnumBody node = parser.parseEnumBody();
        checkBinary(node, ASTEnumConstantList.class, ASTEnumBodyDeclarations.class);
    }

    /**
     * Tests enum body declarations.
     */
    @Test
    public void testEnumBodyDeclarations() {
        ClassesParser parser = new Parser(new Scanner("""
        ;
        constructor() {}
        """)).getClassesParser();
        ASTEnumBodyDeclarations node = parser.parseEnumBodyDeclarations();
        checkSimple(node, ASTClassPartList.class, SEMICOLON);
    }

    /**
     * Tests enum constant list of enum constant.
     */
    @Test
    public void testEnumConstantListOfEnumConstant() {
        ClassesParser parser = new Parser(new Scanner("SINGLETON")).getClassesParser();
        ASTEnumConstantList node = parser.parseEnumConstantList();
        checkSimple(node, ASTEnumConstant.class, COMMA);
        node.collapseThenPrint();
    }

    /**
     * Tests enum constant list.
     */
    @Test
    public void testEnumConstantList() {
        ClassesParser parser = new Parser(new Scanner("RED, YELLOW, GREEN")).getClassesParser();
        ASTEnumConstantList node = parser.parseEnumConstantList();
        checkList(node, COMMA, ASTEnumConstant.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests simple enum constant.
     */
    @Test
    public void testEnumConstantSimple() {
        ClassesParser parser = new Parser(new Scanner("RED")).getClassesParser();
        ASTEnumConstant node = parser.parseEnumConstant();
        checkSimple(node, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests full enum constant.
     */
    @Test
    public void testEnumConstantOfArgumentListClassBody() {
        ClassesParser parser = new Parser(new Scanner("RED(\"#F9152F\") { override String toString() { return \"Red Light\"; } }")).getClassesParser();
        ASTEnumConstant node = parser.parseEnumConstant();
        checkTrinary(node, null, ASTIdentifier.class, ASTArgumentList.class, ASTClassBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests simple class declaration.
     */
    @Test
    public void testClassDeclarationSimple() {
        ClassesParser parser = new Parser(new Scanner("class Dummy {}")).getClassesParser();
        ASTClassDeclaration node = parser.parseClassDeclaration();
        checkBinary(node, CLASS, ASTIdentifier.class, ASTClassBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests full class declaration.
     */
    @Test
    public void testClassDeclarationFull() {
        ClassesParser parser = new Parser(new Scanner("""
            public shared class FullTest<T> extends Test<T> implements Serializable, List<T>
                permits FinalTest, UnitTest, Test, Quiz, PopQuiz
            {}
            """)).getClassesParser();
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
        ClassesParser parser = new Parser(new Scanner("permits Dog, Cat, Mouse")).getClassesParser();
        ASTPermits node = parser.parsePermits();
        checkSimple(node, ASTDataTypeNoArrayList.class, PERMITS);
        node.collapseThenPrint();
    }

    /**
     * Tests superinterfaces (implements clause).
     */
    @Test
    public void testSuperinterfaces() {
        ClassesParser parser = new Parser(new Scanner("implements Copyable")).getClassesParser();
        ASTSuperinterfaces node = parser.parseSuperinterfaces();
        checkSimple(node, ASTDataTypeNoArrayList.class, IMPLEMENTS);
        node.collapseThenPrint();
    }

    /**
     * Tests data type no array of data type no array.
     */
    @Test
    public void testDataTypeNoArrayListOfClassPart() {
        ClassesParser parser = new Parser(new Scanner("Serializable")).getClassesParser();
        ASTDataTypeNoArrayList node = parser.parseDataTypeNoArrayList();
        checkSimple(node, ASTDataTypeNoArray.class, COMMA);
        node.collapseThenPrint();
    }

    /**
     * Tests data type no array list.
     */
    @Test
    public void testDataTypeNoArrayList() {
        ClassesParser parser = new Parser(new Scanner("Serializable, Comparable<T>")).getClassesParser();
        ASTDataTypeNoArrayList node = parser.parseDataTypeNoArrayList();
        checkList(node, COMMA, ASTDataTypeNoArray.class, 2);
        node.collapseThenPrint();
    }

    /**
     * Tests nested data type no array lists.
     */
    @Test
    public void testDataTypeNoArrayListNested() {
        ClassesParser parser = new Parser(new Scanner("Serializable, Comparable<T>, RandomAccess")).getClassesParser();
        ASTDataTypeNoArrayList node = parser.parseDataTypeNoArrayList();
        checkList(node, COMMA, ASTDataTypeNoArray.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests superclass (extends clause).
     */
    @Test
    public void testSuperclass() {
        ClassesParser parser = new Parser(new Scanner("extends Thread")).getClassesParser();
        ASTSuperclass node = parser.parseSuperclass();
        checkSimple(node, ASTDataTypeNoArray.class, EXTENDS);
        node.collapseThenPrint();
    }

    /**
     * Tests class modifier list.
     */
    @Test
    public void testClassModifierList() {
        ClassesParser parser = new Parser(new Scanner("abstract final shared sealed")).getClassesParser();
        ASTClassModifierList node = parser.parseClassModifierList();
        checkList(node, null, ASTGeneralModifier.class, 4);
        node.collapseThenPrint();
    }

    /**
     * Tests empty class body.
     */
    @Test
    public void testClassBodyEmpty() {
        ClassesParser parser = new Parser(new Scanner("{}")).getClassesParser();
        ASTClassBody node = parser.parseClassBody();
        checkEmpty(node, OPEN_BRACE);
        node.collapseThenPrint();
    }

    /**
     * Tests class body.
     */
    @Test
    public void testClassBody() {
        ClassesParser parser = new Parser(new Scanner("""
                {
                    private Integer i = 1;
                    constructor(Integer i) { self.i = i; }
                    Integer getI() {
                        return i;
                    }
                }
                """)).getClassesParser();
        ASTClassBody node = parser.parseClassBody();
        checkSimple(node, ASTClassPartList.class, OPEN_BRACE);
        node.collapseThenPrint();
    }

    /**
     * Tests class part list of all possible class parts.
     */
    @Test
    public void testClassPartListComprehensive() {
        ClassesParser parser = new Parser(new Scanner("""
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
                """)).getClassesParser();
        ASTClassPartList node = parser.parseClassPartList();
        checkList(node, null, ASTClassPart.class, 10);
        node.collapseThenPrint();
    }

    /**
     * Tests class part list of class part.
     */
    @Test
    public void testClassPartListOfClassPart() {
        ClassesParser parser = new Parser(new Scanner("private Integer i = 1;")).getClassesParser();
        ASTClassPartList node = parser.parseClassPartList();
        checkSimple(node, ASTClassPart.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class part list.
     */
    @Test
    public void testClassPartList() {
        ClassesParser parser = new Parser(new Scanner("""
            private Integer i = 1;
            constructor(Integer i) { self.i = i; }
            """)).getClassesParser();
        ASTClassPartList node = parser.parseClassPartList();
        checkList(node, null, ASTClassPart.class, 2);
        node.collapseThenPrint();
    }

    /**
     * Tests nested class part lists.
     */
    @Test
    public void testClassPartListNested() {
        ClassesParser parser = new Parser(new Scanner("""
            private Integer i = 1;
            constructor(Integer i) { self.i = i; }
            Integer getI() {
                return i;
            }
            """)).getClassesParser();
        ASTClassPartList node = parser.parseClassPartList();
        checkList(node, null, ASTClassPart.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of shared constructor.
     */
    @Test
    public void testClassPartOfSharedConstructor() {
        ClassesParser parser = new Parser(new Scanner("shared constructor() { sharedVar = reallyComplicatedLogic(); }")).getClassesParser();
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTSharedConstructor.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of method declaration with void result.
     */
    @Test
    public void testClassPartOfMethodDeclarationVoidResult() {
        ClassesParser parser = new Parser(new Scanner("public abstract void method();")).getClassesParser();
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTMethodDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of method declaration with void result and type parameters.
     */
    @Test
    public void testClassPartOfMethodDeclarationVoidResultTypeParameters() {
        ClassesParser parser = new Parser(new Scanner("public abstract <T> void method(T param);")).getClassesParser();
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTMethodDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of method declaration data type void result.
     */
    @Test
    public void testClassPartOfMethodDeclarationDataTypeResult() {
        ClassesParser parser = new Parser(new Scanner("public abstract String method();")).getClassesParser();
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTMethodDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of method declaration with data type result and type parameters.
     */
    @Test
    public void testClassPartOfMethodDeclarationDataTypeResultTypeParameters() {
        ClassesParser parser = new Parser(new Scanner("public abstract <T> T method(T param);")).getClassesParser();
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTMethodDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of method declaration with mut result.
     */
    @Test
    public void testClassPartOfMethodDeclarationMutResult() {
        ClassesParser parser = new Parser(new Scanner("mut String method(String param);")).getClassesParser();
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTMethodDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of field declaration.
     */
    @Test
    public void testClassPartOfFieldDeclaration() {
        ClassesParser parser = new Parser(new Scanner("private Int myVar = 1, myVar2 = 2;")).getClassesParser();
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTFieldDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of constructor declaration.
     */
    @Test
    public void testClassPartOfConstructorDeclaration() {
        ClassesParser parser = new Parser(new Scanner("constructor(String s) : constructor(s) {}")).getClassesParser();
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTConstructorDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of class declaration.
     */
    @Test
    public void testClassPartOfClassDeclaration() {
        ClassesParser parser = new Parser(new Scanner("public shared class Nested {}")).getClassesParser();
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTClassDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of enum declaration.
     */
    @Test
    public void testClassPartOfEnumDeclaration() {
        ClassesParser parser = new Parser(new Scanner("private enum Light {RED, YELLOW, GREEN}")).getClassesParser();
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTEnumDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of interface declaration.
     */
    @Test
    public void testClassPartOfInterfaceDeclaration() {
        ClassesParser parser = new Parser(new Scanner("private interface TrafficLight { Light getStatus(); }")).getClassesParser();
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTInterfaceDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of annotation declaration.
     */
    @Test
    public void testClassPartOfAnnotationDeclaration() {
        ClassesParser parser = new Parser(new Scanner("""
                public annotation Test {
                    String getStatus() default "SUCCESS";
                }
                """)).getClassesParser();
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTAnnotationDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of record declaration.
     */
    @Test
    public void testClassPartOfRecordDeclaration() {
        ClassesParser parser = new Parser(new Scanner("""
                public record LineItem(Order order, Int lineNbr, Product p, Int qty) {
                    Double getSubtotal() {
                        return p.getUnitPrice() * qty;
                    }
                }
                """)).getClassesParser();
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTRecordDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests shared constructor.
     */
    @Test
    public void testSharedConstructor() {
        ClassesParser parser = new Parser(new Scanner("shared constructor() { sharedVar = reallyComplicatedLogic(); }")).getClassesParser();
        ASTSharedConstructor node = parser.parseSharedConstructor();
        checkSimple(node, ASTBlock.class, CONSTRUCTOR);
        node.collapseThenPrint();
    }

    /**
     * Tests constructor declaration of access modifier, and constructor invocation.
     */
    @Test
    public void testConstructorDeclarationOfAccessConstructorInvocation() {
        ClassesParser parser = new Parser(new Scanner("private constructor(String s) : super(s) { self.s = s; }")).getClassesParser();
        ASTConstructorDeclaration node = parser.parseConstructorDeclaration();
        checkNary(node, CONSTRUCTOR, ASTAccessModifier.class,  ASTConstructorDeclarator.class, ASTConstructorInvocation.class, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests simple constructor declaration.
     */
    @Test
    public void testConstructorDeclarationSimple() {
        ClassesParser parser = new Parser(new Scanner("constructor(String s) { self.s = s; }")).getClassesParser();
        ASTConstructorDeclaration node = parser.parseConstructorDeclaration();
        checkBinary(node, CONSTRUCTOR, ASTConstructorDeclarator.class, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests constructor invocation of primary, type arguments, and super.
     */
    @Test
    public void testConstructorInvocationOfPrimaryTypeArgumentsSuper() {
        ClassesParser parser = new Parser(new Scanner(": (primary).<T>super()")).getClassesParser();
        ASTConstructorInvocation node = parser.parseConstructorInvocation();
        checkBinary(node, SUPER, ASTPrimary.class, ASTTypeArguments.class);
        node.collapseThenPrint();
    }

    /**
     * Tests constructor invocation of primary and super.
     */
    @Test
    public void testConstructorInvocationOfPrimarySuper() {
        ClassesParser parser = new Parser(new Scanner(": (primary).super()")).getClassesParser();
        ASTConstructorInvocation node = parser.parseConstructorInvocation();
        checkSimple(node, ASTPrimary.class, SUPER);
        node.collapseThenPrint();
    }

    /**
     * Tests constructor invocation of expression name, type arguments, and super.
     */
    @Test
    public void testConstructorInvocationOfExpressionNameTypeArgumentsSuper() {
        ClassesParser parser = new Parser(new Scanner(": expr.name.<String>super()")).getClassesParser();
        ASTConstructorInvocation node = parser.parseConstructorInvocation();
        checkBinary(node, SUPER, ASTExpressionName.class, ASTTypeArguments.class);
        node.collapseThenPrint();
    }

    /**
     * Tests constructor invocation of expression name and super.
     */
    @Test
    public void testConstructorInvocationOfExpressionNameSuper() {
        ClassesParser parser = new Parser(new Scanner(": expr.name.super()")).getClassesParser();
        ASTConstructorInvocation node = parser.parseConstructorInvocation();
        checkSimple(node, ASTExpressionName.class, SUPER);
        node.collapseThenPrint();
    }

    /**
     * Tests constructor invocation of super and type arguments.
     */
    @Test
    public void testConstructorInvocationOfSuperTypeArguments() {
        ClassesParser parser = new Parser(new Scanner(": <Integer>super(5)")).getClassesParser();
        ASTConstructorInvocation node = parser.parseConstructorInvocation();
        checkBinary(node, SUPER, ASTTypeArguments.class, ASTArgumentList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests constructor invocation of constructor and type arguments.
     */
    @Test
    public void testConstructorInvocationOfConstructorTypeArguments() {
        ClassesParser parser = new Parser(new Scanner(": <Integer>constructor()")).getClassesParser();
        ASTConstructorInvocation node = parser.parseConstructorInvocation();
        checkSimple(node, ASTTypeArguments.class, CONSTRUCTOR);
        node.collapseThenPrint();
    }

    /**
     * Tests simple constructor invocation of constructor.
     */
    @Test
    public void testConstructorInvocationOfConstructorSimple() {
        ClassesParser parser = new Parser(new Scanner(": constructor()")).getClassesParser();
        ASTConstructorInvocation node = parser.parseConstructorInvocation();
        checkEmpty(node, CONSTRUCTOR);
        node.collapseThenPrint();
    }

    /**
     * Tests simple constructor declarator.
     */
    @Test
    public void testConstructorDeclaratorSimple() {
        ClassesParser parser = new Parser(new Scanner("constructor()")).getClassesParser();
        ASTConstructorDeclarator node = parser.parseConstructorDeclarator();
        checkEmpty(node, CONSTRUCTOR);
        node.collapseThenPrint();
    }

    /**
     * Tests full constructor declarator.
     */
    @Test
    public void testConstructorDeclaratorFull() {
        ClassesParser parser = new Parser(new Scanner("<T> constructor(T param)")).getClassesParser();
        ASTConstructorDeclarator node = parser.parseConstructorDeclarator();
        checkBinary(node, CONSTRUCTOR, ASTTypeParameters.class, ASTFormalParameterList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests full field declaration.
     */
    @Test
    public void testFieldDeclaration() {
        ClassesParser parser = new Parser(new Scanner("public constant String aConstant = \"CONSTANT\";")).getClassesParser();
        ASTFieldDeclaration node = parser.parseFieldDeclaration();
        checkNary(node, null, ASTAccessModifier.class, ASTFieldModifierList.class, ASTDataType.class, ASTVariableDeclaratorList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests constant field declaration.
     */
    @Test
    public void testFieldDeclarationOfConstant() {
        ClassesParser parser = new Parser(new Scanner("public constant String aConstant = \"CONSTANT\";")).getClassesParser();
        ASTFieldDeclaration node = parser.parseFieldDeclaration();
        checkNary(node, null, ASTAccessModifier.class, ASTFieldModifierList.class, ASTDataType.class, ASTVariableDeclaratorList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests simple field declaration.
     */
    @Test
    public void testFieldDeclarationSimple() {
        ClassesParser parser = new Parser(new Scanner("String name = \"spruce\";")).getClassesParser();
        ASTFieldDeclaration node = parser.parseFieldDeclaration();
        checkBinary(node, ASTDataType.class, ASTVariableDeclaratorList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests field modifier list.
     */
    @Test
    public void testFieldModifierList() {
        ClassesParser parser = new Parser(new Scanner("constant var mut shared volatile")).getClassesParser();
        ASTFieldModifierList node = parser.parseFieldModifierList();
        checkList(node, null, ASTGeneralModifier.class, 5);
        node.collapseThenPrint();
    }

    /**
     * Tests bad field modifier list.
     */
    @Test
    public void testErrorFieldModifierListOfOverride() {
        ClassesParser parser = new Parser(new Scanner("override")).getClassesParser();
        assertThrows(CompileException.class, parser::parseFieldModifierList);
    }

    /**
     * Tests simple method declaration.
     */
    @Test
    public void testMethodDeclarationSimple() {
        ClassesParser parser = new Parser(new Scanner("""
                String toString() {
                    return self;
                }
                """)).getClassesParser();
        ASTMethodDeclaration node = parser.parseMethodDeclaration();
        checkBinary(node, ASTMethodHeader.class, ASTMethodBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests method declaration with access modifier and method modifier.
     */
    @Test
    public void testMethodDeclarationAccessModifierMethodModifier() {
        ClassesParser parser = new Parser(new Scanner("public abstract Foo abstractMethod();")).getClassesParser();
        ASTMethodDeclaration node = parser.parseMethodDeclaration();
        checkNary(node, null, ASTAccessModifier.class, ASTMethodModifierList.class, ASTMethodHeader.class, ASTMethodBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests method body of semicolon.
     */
    @Test
    public void testMethodBodyOfSemicolon() {
        ClassesParser parser = new Parser(new Scanner(";")).getClassesParser();
        ASTMethodBody node = parser.parseMethodBody();
        checkEmpty(node, SEMICOLON);
        node.collapseThenPrint();
    }

    /**
     * Tests method body of block.
     */
    @Test
    public void testMethodBodyOfBlock() {
        ClassesParser parser = new Parser(new Scanner("{\n    out.println(\"Body!\");\n}")).getClassesParser();
        ASTMethodBody node = parser.parseMethodBody();
        checkSimple(node, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests access modifier list of access modifier.
     */
    @Test
    public void testAccessModifierListOfAccessModifier() {
        ClassesParser parser = new Parser(new Scanner("final")).getClassesParser();
        ASTMethodModifierList node = parser.parseMethodModifierList();
        checkSimple(node, ASTGeneralModifier.class);
        node.collapseThenPrint();
    }
    /**
     * Tests access modifier list of access modifiers.
     */
    @Test
    public void testAccessModifierListOfAccessModifiers() {
        ClassesParser parser = new Parser(new Scanner("final abstract shared")).getClassesParser();
        ASTMethodModifierList node = parser.parseMethodModifierList();
        checkList(node, null, ASTGeneralModifier.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests method modifier of public.
     */
    @Test
    public void testAccessModifierOfPublic() {
        ClassesParser parser = new Parser(new Scanner("public")).getClassesParser();
        ASTAccessModifier node = parser.parseAccessModifier();
        checkEmpty(node, PUBLIC);
        node.collapseThenPrint();
    }

    /**
     * Tests method modifier of protected.
     */
    @Test
    public void testAccessModifierOfProtected() {
        ClassesParser parser = new Parser(new Scanner("protected")).getClassesParser();
        ASTAccessModifier node = parser.parseAccessModifier();
        checkEmpty(node, PROTECTED);
        node.collapseThenPrint();
    }

    /**
     * Tests method modifier of abstract.
     */
    @Test
    public void testAccessModifierOfInternal() {
        ClassesParser parser = new Parser(new Scanner("internal")).getClassesParser();
        ASTAccessModifier node = parser.parseAccessModifier();
        checkEmpty(node, INTERNAL);
        node.collapseThenPrint();
    }

    /**
     * Tests access modifier of private.
     */
    @Test
    public void testAccessModifierOfPrivate() {
        ClassesParser parser = new Parser(new Scanner("private")).getClassesParser();
        ASTAccessModifier node = parser.parseAccessModifier();
        checkEmpty(node, PRIVATE);
        node.collapseThenPrint();
    }

    /**
     * Tests method modifier list.
     */
    @Test
    public void testMethodModifierList() {
        ClassesParser parser = new Parser(new Scanner("abstract final override shared")).getClassesParser();
        ASTMethodModifierList node = parser.parseMethodModifierList();
        checkList(node, null, ASTGeneralModifier.class, 4);
        node.collapseThenPrint();
    }

    /**
     * Tests bad method modifier list.
     */
    @Test
    public void testErrorMethodModifierListOfConst() {
        ClassesParser parser = new Parser(new Scanner("const")).getClassesParser();
        assertThrows(CompileException.class, parser::parseMethodModifierList);
    }

    /**
     * Tests method modifier list of method modifiers.
     */
    @Test
    public void testGeneralModifierListOfMethodModifiers() {
        ClassesParser parser = new Parser(new Scanner("abstract mut var override shared volatile")).getClassesParser();
        ASTGeneralModifierList node = parser.parseGeneralModifierList();
        checkList(node, null, ASTGeneralModifier.class, 6);
        node.collapseThenPrint();
    }

    /**
     * Tests general modifier of abstract.
     */
    @Test
    public void testGeneralModifierOfAbstract() {
        ClassesParser parser = new Parser(new Scanner("abstract")).getClassesParser();
        ASTGeneralModifier node = parser.parseGeneralModifier();
        checkEmpty(node, ABSTRACT);
        node.collapseThenPrint();
    }

    /**
     * Tests general modifier of mut.
     */
    @Test
    public void testGeneralModifierOfConst() {
        ClassesParser parser = new Parser(new Scanner("mut")).getClassesParser();
        ASTGeneralModifier node = parser.parseGeneralModifier();
        checkEmpty(node, MUT);
        node.collapseThenPrint();
    }

    /**
     * Tests general modifier of var.
     */
    @Test
    public void testMethodModifierOfVar() {
        ClassesParser parser = new Parser(new Scanner("var")).getClassesParser();
        ASTGeneralModifier node = parser.parseGeneralModifier();
        checkEmpty(node, VAR);
        node.collapseThenPrint();
    }

    /**
     * Tests general modifier of override.
     */
    @Test
    public void testGeneralModifierOfOverride() {
        ClassesParser parser = new Parser(new Scanner("override")).getClassesParser();
        ASTGeneralModifier node = parser.parseGeneralModifier();
        checkEmpty(node, OVERRIDE);
        node.collapseThenPrint();
    }

    /**
     * Tests general modifier of shared.
     */
    @Test
    public void testGeneralModifierOfShared() {
        ClassesParser parser = new Parser(new Scanner("shared")).getClassesParser();
        ASTGeneralModifier node = parser.parseGeneralModifier();
        checkEmpty(node, SHARED);
        node.collapseThenPrint();
    }

    /**
     * Tests general modifier of volatile.
     */
    @Test
    public void testGeneralModifierOfVolatile() {
        ClassesParser parser = new Parser(new Scanner("volatile")).getClassesParser();
        ASTGeneralModifier node = parser.parseGeneralModifier();
        checkEmpty(node, VOLATILE);
        node.collapseThenPrint();
    }

    /**
     * Tests simple method header.
     */
    @Test
    public void testMethodHeaderSimple() {
        ClassesParser parser = new Parser(new Scanner("void toString() const")).getClassesParser();
        ASTMethodHeader node = parser.parseMethodHeader();
        checkBinary(node, ASTResult.class, ASTMethodDeclarator.class);
        node.collapseThenPrint();
    }

    /**
     * Tests method header with type parameters.
     */
    @Test
    public void testMethodHeaderOfTypeParameters() {
        ClassesParser parser = new Parser(new Scanner("<T> T getItem() const")).getClassesParser();
        ASTMethodHeader node = parser.parseMethodHeader();
        checkTrinary(node, null, ASTTypeParameters.class, ASTResult.class, ASTMethodDeclarator.class);
        node.collapseThenPrint();
    }

    /**
     * Tests result of void.
     */
    @Test
    public void testResultOfVoid() {
        ClassesParser parser = new Parser(new Scanner("void")).getClassesParser();
        ASTResult node = parser.parseResult();
        checkEmpty(node, VOID);
        node.collapseThenPrint();
    }

    /**
     * Tests result of data type.
     */
    @Test
    public void testResultOfDataType() {
        ClassesParser parser = new Parser(new Scanner("Map<String, Integer>")).getClassesParser();
        ASTResult node = parser.parseResult();
        checkSimple(node, ASTDataType.class);
        node.collapseThenPrint();
    }

    /**
     * Tests result of var modifier and data type.
     */
    @Test
    public void testResultOfConstModifierDataType() {
        ClassesParser parser = new Parser(new Scanner("mut Map<String, Integer>")).getClassesParser();
        ASTResult node = parser.parseResult();
        checkBinary(node, ASTMutModifier.class, ASTDataType.class);
        node.collapseThenPrint();
    }

    /**
     * Tests simple method declarator.
     */
    @Test
    public void testMethodDeclaratorSimple() {
        ClassesParser parser = new Parser(new Scanner("update()")).getClassesParser();
        ASTMethodDeclarator node = parser.parseMethodDeclarator();
        checkSimple(node, ASTIdentifier.class, OPEN_PARENTHESIS);
        node.collapseThenPrint();
    }

    /**
     * Tests method declarator of parameter list and mut modifier.
     */
    @Test
    public void testMethodDeclaratorOfParameterListConstModifier() {
        ClassesParser parser = new Parser(new Scanner("join(String sep) mut)")).getClassesParser();
        ASTMethodDeclarator node = parser.parseMethodDeclarator();
        checkTrinary(node, OPEN_PARENTHESIS, ASTIdentifier.class, ASTFormalParameterList.class, ASTMutModifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests mut modifier by itself.
     */
    @Test
    public void testMutModifier() {
        ClassesParser parser = new Parser(new Scanner("mut")).getClassesParser();
        ASTMutModifier node = parser.parseMutModifier();
        checkEmpty(node, MUT);
        node.collapseThenPrint();
    }

    /**
     * Tests formal parameter list of formal parameter.
     */
    @Test
    public void testFormalParameterListOfFormalParameter() {
        ClassesParser parser = new Parser(new Scanner("const Int a")).getClassesParser();
        ASTFormalParameterList node = parser.parseFormalParameterList();
        checkSimple(node, ASTFormalParameter.class, COMMA);
        node.collapseThenPrint();
    }

    /**
     * Tests formal parameter list.
     */
    @Test
    public void testFormalParameterList() {
        ClassesParser parser = new Parser(new Scanner("String msg, Foo f, Bar b")).getClassesParser();
        ASTFormalParameterList node = parser.parseFormalParameterList();
        checkList(node, COMMA, ASTFormalParameter.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests formal parameter list with varargs parameter list.
     */
    @Test
    public void testFormalParameterListOfLastVarargs() {
        ClassesParser parser = new Parser(new Scanner("Point pt, Double... coordinates")).getClassesParser();
        ASTFormalParameterList node = parser.parseFormalParameterList();
        checkList(node, COMMA, ASTFormalParameter.class, 2);
        node.collapseThenPrint();
    }

    /**
     * Tests if varargs not last, compiler error.
     */
    @Test
    public void testFormalParameterListVarargsNotLastError() {
        ClassesParser parser = new Parser(new Scanner("Double... coordinates, Point pt")).getClassesParser();
        assertThrows(CompileException.class, parser::parseFormalParameterList);
    }

    /**
     * Tests formal parameter, no variable modifier list, with ellipsis.
     */
    @Test
    public void testFormalParameterNoVMLEllipsis() {
        ClassesParser parser = new Parser(new Scanner("String... args")).getClassesParser();
        ASTFormalParameter node = parser.parseFormalParameter();
        checkBinary(node, THREE_DOTS, ASTDataType.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests formal parameter, variable modifier list, with ellipsis.
     */
    @Test
    public void testFormalParameterOfVMLEllipsis() {
        ClassesParser parser = new Parser(new Scanner("mut String... args")).getClassesParser();
        ASTFormalParameter node = parser.parseFormalParameter();
        checkTrinary(node, THREE_DOTS, ASTVariableModifierList.class, ASTDataType.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests formal parameter, no variable modifier list, no ellipsis.
     */
    @Test
    public void testFormalParameterNoVMLNoEllipsis() {
        ClassesParser parser = new Parser(new Scanner("String[] args")).getClassesParser();
        ASTFormalParameter node = parser.parseFormalParameter();
        checkBinary(node, ASTDataType.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests formal parameter, variable modifier list, no ellipsis.
     */
    @Test
    public void testFormalParameterOfVMLNoEllipsis() {
        ClassesParser parser = new Parser(new Scanner("mut String[] args")).getClassesParser();
        ASTFormalParameter node = parser.parseFormalParameter();
        checkTrinary(node, null, ASTVariableModifierList.class, ASTDataType.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }
}
