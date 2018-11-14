package org.spruce.compiler.test;

import org.spruce.compiler.ast.classes.*;
import org.spruce.compiler.ast.expressions.*;
import org.spruce.compiler.ast.names.*;
import org.spruce.compiler.ast.statements.*;
import org.spruce.compiler.ast.types.*;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.parser.ClassesParser;
import org.spruce.compiler.scanner.Scanner;
import static org.spruce.compiler.scanner.TokenType.*;
import static org.spruce.compiler.test.ParserTestUtility.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * All tests for the parser related to classes, methods, etc.
 */
public class ParserClassesTest
{
    /**
     * Tests simple annotation declaration.
     */
    @Test
    public void testAnnotationDeclarationSimple()
    {
        ClassesParser parser = new ClassesParser(new Scanner("annotation Dummy {}"));
        ASTAnnotationDeclaration node = parser.parseAnnotationDeclaration();
        checkBinary(node, ANNOTATION, ASTIdentifier.class, ASTAnnotationBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests full annotation declaration.
     */
    @Test
    public void testAnnotationDeclarationFull()
    {
        ClassesParser parser = new ClassesParser(new Scanner("public shared annotation AFullTest {String prop();}"));
        ASTAnnotationDeclaration node = parser.parseAnnotationDeclaration();
        checkNary(node, ANNOTATION, ASTAccessModifier.class, ASTInterfaceModifierList.class, ASTIdentifier.class, ASTAnnotationBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests empty annotation body.
     */
    @Test
    public void testAnnotationBodyEmpty()
    {
        ClassesParser parser = new ClassesParser(new Scanner("{}"));
        ASTAnnotationBody node = parser.parseAnnotationBody();
        checkEmpty(node, OPEN_BRACE);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation body.
     */
    @Test
    public void testAnnotationBody()
    {
        ClassesParser parser = new ClassesParser(new Scanner("{\nconstant Integer i := 1;\nclass Inner{}\nInteger getI() default 1;}\n}"));
        ASTAnnotationBody node = parser.parseAnnotationBody();
        checkSimple(node, ASTAnnotationPartList.class, OPEN_BRACE);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation part list of annotation part.
     */
    @Test
    public void testAnnotationPartListOfAnnotationPart()
    {
        ClassesParser parser = new ClassesParser(new Scanner("constant Integer i := 1;"));
        ASTAnnotationPartList node = parser.parseAnnotationPartList();
        checkSimple(node, ASTAnnotationPart.class);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation part list.
     */
    @Test
    public void testAnnotationPartList()
    {
        ClassesParser parser = new ClassesParser(new Scanner("constant Integer i := 1;\nclass Inner {}"));
        ASTAnnotationPartList node = parser.parseAnnotationPartList();
        checkList(node, null, ASTAnnotationPart.class, 2);
        node.collapseThenPrint();
    }

    /**
     * Tests nested annotation part lists.
     */
    @Test
    public void testAnnotationPartListNested()
    {
        ClassesParser parser = new ClassesParser(new Scanner("constant Integer i := 1;\nclass Inner {}\nInteger getI() default 1;"));
        ASTAnnotationPartList node = parser.parseAnnotationPartList();
        checkList(node, null, ASTAnnotationPart.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation part of annotation type element declaration.
     */
    @Test
    public void testAnnotationPartOfATED()
    {
        ClassesParser parser = new ClassesParser(new Scanner("String element() default \"Test\";"));
        ASTAnnotationPart node = parser.parseAnnotationPart();
        checkSimple(node, ASTAnnotationTypeElementDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation part of constant declaration.
     */
    @Test
    public void testAnnotationPartOfConstantDeclaration()
    {
        ClassesParser parser = new ClassesParser(new Scanner("constant String LANGUAGE := \"Spruce\";"));
        ASTAnnotationPart node = parser.parseAnnotationPart();
        checkSimple(node, ASTConstantDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation part of class declaration.
     */
    @Test
    public void testAnnotationPartOfClassDeclaration()
    {
        ClassesParser parser = new ClassesParser(new Scanner("public shared class Nested {}"));
        ASTAnnotationPart node = parser.parseAnnotationPart();
        checkSimple(node, ASTClassDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation part of enum declaration.
     */
    @Test
    public void testAnnotationPartOfEnumDeclaration()
    {
        ClassesParser parser = new ClassesParser(new Scanner("private enum Light {RED, YELLOW, GREEN}"));
        ASTAnnotationPart node = parser.parseAnnotationPart();
        checkSimple(node, ASTEnumDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation part of interface declaration.
     */
    @Test
    public void testAnnotationPartOfInterfaceDeclaration()
    {
        ClassesParser parser = new ClassesParser(new Scanner("private interface TrafficLight { Light getStatus(); }"));
        ASTAnnotationPart node = parser.parseAnnotationPart();
        checkSimple(node, ASTInterfaceDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation part of annotation declaration.
     */
    @Test
    public void testAnnotationPartOfAnnotationDeclaration()
    {
        ClassesParser parser = new ClassesParser(new Scanner("public annotation Test { String getStatus() default \"SUCCESS\"; }"));
        ASTAnnotationPart node = parser.parseAnnotationPart();
        checkSimple(node, ASTAnnotationDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation type element declaration.
     */
    @Test
    public void testATED()
    {
        ClassesParser parser = new ClassesParser(new Scanner("String element();"));
        ASTAnnotationTypeElementDeclaration node = parser.parseAnnotationTypeElementDeclaration();
        checkBinary(node, OPEN_PARENTHESIS, ASTDataType.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation type element declaration with default value.
     */
    @Test
    public void testATEDDefaultValue()
    {
        ClassesParser parser = new ClassesParser(new Scanner("String element() default \"DNE\";"));
        ASTAnnotationTypeElementDeclaration node = parser.parseAnnotationTypeElementDeclaration();
        checkTrinary(node, OPEN_PARENTHESIS, ASTDataType.class, ASTIdentifier.class, ASTDefaultValue.class);
        node.collapseThenPrint();
    }

    /**
     * Tests default value.
     */
    @Test
    public void testDefaultValue()
    {
        ClassesParser parser = new ClassesParser(new Scanner("default {\"default\", \"value\"}"));
        ASTDefaultValue node = parser.parseDefaultValue();
        checkSimple(node, ASTElementValue.class, DEFAULT);
        node.collapseThenPrint();
    }

    /**
     * Tests annotation of marker annotation.
     */
    @Test
    public void testAnnotationOfMarkerAnnotation()
    {
        ClassesParser parser = new ClassesParser(new Scanner("@Test"));
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
    public void testAnnotationOfSingleElementAnnotation()
    {
        ClassesParser parser = new ClassesParser(new Scanner("@Test(\"Test\")"));
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
    public void testAnnotationOfNormalAnnotationEmpty()
    {
        ClassesParser parser = new ClassesParser(new Scanner("@Empty()"));
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
    public void testAnnotationOfNormalAnnotationOfEVPL()
    {
        ClassesParser parser = new ClassesParser(new Scanner("@Many(one := 1, two := \"two\", three := '3')"));
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
    public void testEVPListOfEVP()
    {
        ClassesParser parser = new ClassesParser(new Scanner("test := \"Test\""));
        ASTElementValuePairList node = parser.parseElementValuePairList();
        checkSimple(node, ASTElementValuePair.class, COMMA);
        node.collapseThenPrint();
    }

    /**
     * Tests element value pair list.
     */
    @Test
    public void testEVPList()
    {
        ClassesParser parser = new ClassesParser(new Scanner("one := 1, two := \"two\", three := '3'"));
        ASTElementValuePairList node = parser.parseElementValuePairList();
        checkList(node, COMMA, ASTElementValuePair.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests element value pair of element value.
     */
    @Test
    public void testElementValuePairOfElementValue()
    {
        ClassesParser parser = new ClassesParser(new Scanner("prop := \"Conditional Expression\""));
        ASTElementValuePair node = parser.parseElementValuePair();
        checkBinary(node, ASSIGNMENT, ASTIdentifier.class, ASTElementValue.class);
        node.collapseThenPrint();
    }

    /**
     * Tests empty element value array initializer.
     */
    @Test
    public void testEVAIEmpty()
    {
        ClassesParser parser = new ClassesParser(new Scanner("{}"));
        ASTElementValueArrayInitializer node = parser.parseElementValueArrayInitializer();
        checkEmpty(node, OPEN_BRACE);
        node.collapseThenPrint();
    }

    /**
     * Tests element value array initializer of element value list.
     */
    @Test
    public void testEVAIOfEVList()
    {
        ClassesParser parser = new ClassesParser(new Scanner("{1, \"Two\", '3'}"));
        ASTElementValueArrayInitializer node = parser.parseElementValueArrayInitializer();
        checkSimple(node, ASTElementValueList.class, OPEN_BRACE);
        node.collapseThenPrint();
    }

    /**
     * Tests element value list of element value.
     */
    @Test
    public void testEVListOfEV()
    {
        ClassesParser parser = new ClassesParser(new Scanner("\"Test\""));
        ASTElementValueList node = parser.parseElementValueList();
        checkSimple(node, ASTElementValue.class, COMMA);
        node.collapseThenPrint();
    }

    /**
     * Tests element value list.
     */
    @Test
    public void testEVList()
    {
        ClassesParser parser = new ClassesParser(new Scanner("1, \"two\", '3'"));
        ASTElementValueList node = parser.parseElementValueList();
        checkList(node, COMMA, ASTElementValue.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests element value of conditional expression.
     */
    @Test
    public void testElementValueOfConditionalExpression()
    {
        ClassesParser parser = new ClassesParser(new Scanner("\"Conditional Expression\""));
        ASTElementValue node = parser.parseElementValue();
        checkSimple(node, ASTConditionalExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests element value of element value array initializer.
     */
    @Test
    public void testElementValueOfEVAI()
    {
        ClassesParser parser = new ClassesParser(new Scanner("{\"Conditional Expression\"}"));
        ASTElementValue node = parser.parseElementValue();
        checkSimple(node, ASTElementValueArrayInitializer.class);
        node.collapseThenPrint();
    }

    /**
     * Tests element value of annotation.
     */
    @Test
    public void testElementValueOfAnnotation()
    {
        ClassesParser parser = new ClassesParser(new Scanner("@Foo"));
        ASTElementValue node = parser.parseElementValue();
        checkSimple(node, ASTAnnotation.class);
        node.collapseThenPrint();
    }

    /**
     * Tests simple interface declaration.
     */
    @Test
    public void testInterfaceDeclarationSimple()
    {
        ClassesParser parser = new ClassesParser(new Scanner("interface Dummy {}"));
        ASTInterfaceDeclaration node = parser.parseInterfaceDeclaration();
        checkBinary(node, INTERFACE, ASTIdentifier.class, ASTInterfaceBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests full interface declaration.
     */
    @Test
    public void testInterfaceDeclarationFull()
    {
        ClassesParser parser = new ClassesParser(new Scanner("public shared interface IFullTest<T> extends ITest<T>, Serializable, List<T> {}"));
        ASTInterfaceDeclaration node = parser.parseInterfaceDeclaration();
        checkNary(node, INTERFACE, ASTAccessModifier.class, ASTInterfaceModifierList.class, ASTIdentifier.class,
                ASTTypeParameters.class, ASTExtendsInterfaces.class, ASTInterfaceBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface modifier list.
     */
    @Test
    public void testInterfaceModifierList()
    {
        ClassesParser parser = new ClassesParser(new Scanner("abstract shared strictfp"));
        ASTInterfaceModifierList node = parser.parseInterfaceModifierList();
        checkList(node, null, ASTGeneralModifier.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests extends interfaces (extends clause on interface).
     */
    @Test
    public void testExtendsInterfaces()
    {
        ClassesParser parser = new ClassesParser(new Scanner("extends Copyable, Serializable"));
        ASTExtendsInterfaces node = parser.parseExtendsInterfaces();
        checkSimple(node, ASTDataTypeNoArrayList.class, EXTENDS);
        node.collapseThenPrint();
    }

    /**
     * Tests empty interface body.
     */
    @Test
    public void testInterfaceBodyEmpty()
    {
        ClassesParser parser = new ClassesParser(new Scanner("{}"));
        ASTInterfaceBody node = parser.parseInterfaceBody();
        checkEmpty(node, OPEN_BRACE);
        node.collapseThenPrint();
    }

    /**
     * Tests interface body.
     */
    @Test
    public void testInterfaceBody()
    {
        ClassesParser parser = new ClassesParser(new Scanner("{\nconstant Integer i := 1;\nclass Inner{}\ndefault Integer getI() {\n    return i;\n}\n}"));
        ASTInterfaceBody node = parser.parseInterfaceBody();
        checkSimple(node, ASTInterfacePartList.class, OPEN_BRACE);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part list of interface part.
     */
    @Test
    public void testInterfacePartListOfInterfacePart()
    {
        ClassesParser parser = new ClassesParser(new Scanner("constant Integer i := 1;"));
        ASTInterfacePartList node = parser.parseInterfacePartList();
        checkSimple(node, ASTInterfacePart.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part list.
     */
    @Test
    public void testInterfacePartList()
    {
        ClassesParser parser = new ClassesParser(new Scanner("constant Integer i := 1;\nclass Inner {}"));
        ASTInterfacePartList node = parser.parseInterfacePartList();
        checkList(node, null, ASTInterfacePart.class, 2);
        node.collapseThenPrint();
    }

    /**
     * Tests nested interface part lists.
     */
    @Test
    public void testInterfacePartListNested()
    {
        ClassesParser parser = new ClassesParser(new Scanner("constant Integer i := 1;\nclass Inner {}\nInteger getI();"));
        ASTInterfacePartList node = parser.parseInterfacePartList();
        checkList(node, null, ASTInterfacePart.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part of method declaration with void result.
     */
    @Test
    public void testInterfacePartOfMethodDeclarationVoidResult()
    {
        ClassesParser parser = new ClassesParser(new Scanner("public void method();"));
        ASTInterfacePart node = parser.parseInterfacePart();
        checkSimple(node, ASTInterfaceMethodDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part of method declaration with void result and type parameters.
     */
    @Test
    public void testInterfacePartOfMethodDeclarationVoidResultTypeParameters()
    {
        ClassesParser parser = new ClassesParser(new Scanner("public <T> void method(T param);"));
        ASTInterfacePart node = parser.parseInterfacePart();
        checkSimple(node, ASTInterfaceMethodDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part of method declaration data type void result.
     */
    @Test
    public void testInterfacePartOfMethodDeclarationDataTypeResult()
    {
        ClassesParser parser = new ClassesParser(new Scanner("public String method();"));
        ASTInterfacePart node = parser.parseInterfacePart();
        checkSimple(node, ASTInterfaceMethodDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part of method declaration with data type result and type parameters.
     */
    @Test
    public void testInterfacePartOfMethodDeclarationDataTypeResultTypeParameters()
    {
        ClassesParser parser = new ClassesParser(new Scanner("public <T> T method(T param);"));
        ASTInterfacePart node = parser.parseInterfacePart();
        checkSimple(node, ASTInterfaceMethodDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part of method declaration with const result.
     */
    @Test
    public void testInterfacePartOfMethodDeclarationConstResult()
    {
        ClassesParser parser = new ClassesParser(new Scanner("const String method(String param);"));
        ASTInterfacePart node = parser.parseInterfacePart();
        checkSimple(node, ASTInterfaceMethodDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part of constant declaration.
     */
    @Test
    public void testInterfacePartOfConstantDeclaration()
    {
        ClassesParser parser = new ClassesParser(new Scanner("constant String LANGUAGE := \"Spruce\";"));
        ASTInterfacePart node = parser.parseInterfacePart();
        checkSimple(node, ASTConstantDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part of class declaration.
     */
    @Test
    public void testInterfacePartOfClassDeclaration()
    {
        ClassesParser parser = new ClassesParser(new Scanner("public shared class Nested {}"));
        ASTInterfacePart node = parser.parseInterfacePart();
        checkSimple(node, ASTClassDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part of enum declaration.
     */
    @Test
    public void testInterfacePartOfEnumDeclaration()
    {
        ClassesParser parser = new ClassesParser(new Scanner("private enum Light {RED, YELLOW, GREEN}"));
        ASTInterfacePart node = parser.parseInterfacePart();
        checkSimple(node, ASTEnumDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part of interface declaration.
     */
    @Test
    public void testInterfacePartOfInterfaceDeclaration()
    {
        ClassesParser parser = new ClassesParser(new Scanner("private interface TrafficLight { Light getStatus(); }"));
        ASTInterfacePart node = parser.parseInterfacePart();
        checkSimple(node, ASTInterfaceDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface part of annotation declaration.
     */
    @Test
    public void testInterfacePartOfAnnotationDeclaration()
    {
        ClassesParser parser = new ClassesParser(new Scanner("public annotation Test { String getStatus() default \"SUCCESS\"; }"));
        ASTInterfacePart node = parser.parseInterfacePart();
        checkSimple(node, ASTAnnotationDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests simple interface method declaration.
     */
    @Test
    public void testInterfaceMethodDeclarationSimple()
    {
        ClassesParser parser = new ClassesParser(new Scanner("Boolean add(T element);"));
        ASTInterfaceMethodDeclaration node = parser.parseInterfaceMethodDeclaration();
        checkBinary(node, ASTMethodHeader.class, ASTMethodBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface method declaration with access modifier and method modifier.
     */
    @Test
    public void testInterfaceMethodDeclarationAccessModifierMethodModifier()
    {
        ClassesParser parser = new ClassesParser(new Scanner("private default void addAll(Collection<T> other) {\n    for (T element : other) {\n    add(other);\n}\n}"));
        ASTInterfaceMethodDeclaration node = parser.parseInterfaceMethodDeclaration();
        checkNary(node, null, ASTAccessModifier.class, ASTInterfaceMethodModifierList.class, ASTMethodHeader.class, ASTMethodBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests interface method modifier list.
     */
    @Test
    public void testInterfaceMethodModifierList()
    {
        ClassesParser parser = new ClassesParser(new Scanner("abstract default override shared strictfp"));
        ASTInterfaceMethodModifierList node = parser.parseInterfaceMethodModifierList();
        checkList(node, null, ASTGeneralModifier.class, 5);
        node.collapseThenPrint();
    }

    /**
     * Tests bad interface method modifier list.
     */
    @Test
    public void testErrorInterfaceMethodModifierListOfConst()
    {
        ClassesParser parser = new ClassesParser(new Scanner("final"));
        assertThrows(CompileException.class, parser::parseInterfaceMethodModifierList);
    }

    /**
     * Tests constant declaration, no "constant".
     */
    @Test
    public void testConstantDeclaration()
    {
        ClassesParser parser = new ClassesParser(new Scanner("String test := \"Test\";"));
        ASTConstantDeclaration node = parser.parseConstantDeclaration();
        checkBinary(node, ASTDataType.class, ASTVariableDeclaratorList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests constant declaration with "constant".
     */
    @Test
    public void testConstantDeclarationOfConstant()
    {
        ClassesParser parser = new ClassesParser(new Scanner("constant String test := \"Test\";"));
        ASTConstantDeclaration node = parser.parseConstantDeclaration();
        checkTrinary(node, null, ASTConstantModifier.class, ASTDataType.class, ASTVariableDeclaratorList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests constant modifier by itself.
     */
    @Test
    public void testConstantModifier()
    {
        ClassesParser parser = new ClassesParser(new Scanner("constant"));
        ASTConstantModifier node = parser.parseConstantModifier();
        checkEmpty(node, CONSTANT);
        node.collapseThenPrint();
    }

    /**
     * Tests simple enum declaration.
     */
    @Test
    public void testEnumDeclarationSimple()
    {
        ClassesParser parser = new ClassesParser(new Scanner("enum Dummy {DUMMY}"));
        ASTEnumDeclaration node = parser.parseEnumDeclaration();
        checkBinary(node, ENUM, ASTIdentifier.class, ASTEnumBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests full enum declaration.
     */
    @Test
    public void testEnumDeclarationFull()
    {
        ClassesParser parser = new ClassesParser(new Scanner("public shared enum FullEnumTest implements Serializable {QUIZ, TEST, FINAL}"));
        ASTEnumDeclaration node = parser.parseEnumDeclaration();
        checkNary(node, ENUM, ASTAccessModifier.class, ASTClassModifierList.class, ASTIdentifier.class,
                ASTSuperinterfaces.class, ASTEnumBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests simple enum body.
     */
    @Test
    public void testEnumBodySimple()
    {
        ClassesParser parser = new ClassesParser(new Scanner("{\nRED, YELLOW, GREEN\n}"));
        ASTEnumBody node = parser.parseEnumBody();
        checkSimple(node, ASTEnumConstantList.class);
    }

    /**
     * Tests enum body of nothing.
     */
    @Test
    public void testEnumBodyOfNothing()
    {
        ClassesParser parser = new ClassesParser(new Scanner("{}"));
        ASTEnumBody node = parser.parseEnumBody();
        checkEmpty(node, null);
    }

    /**
     * Tests enum body of utility methods.
     */
    @Test
    public void testEnumBodyOfUtility()
    {
        ClassesParser parser = new ClassesParser(new Scanner("{\n;    shared void utility() {\n    out.println(\"Utility!\");\n}\n}"));
        ASTEnumBody node = parser.parseEnumBody();
        checkSimple(node, ASTEnumBodyDeclarations.class);
    }

    /**
     * Tests enum body of constants and class part list.
     */
    @Test
    public void testEnumBodyOfConstantsClassPartList()
    {
        ClassesParser parser = new ClassesParser(new Scanner("{\nRED, YELLOW, GREEN;\nshared void utility() {\n    out.println(\"Utility!\");\n}\n}"));
        ASTEnumBody node = parser.parseEnumBody();
        checkBinary(node, ASTEnumConstantList.class, ASTEnumBodyDeclarations.class);
    }

    /**
     * Tests enum body declarations.
     */
    @Test
    public void testEnumBodyDeclarations()
    {
        ClassesParser parser = new ClassesParser(new Scanner(";\nconstructor() {}"));
        ASTEnumBodyDeclarations node = parser.parseEnumBodyDeclarations();
        checkSimple(node, ASTClassPartList.class, SEMICOLON);
    }

    /**
     * Tests enum constant list of enum constant.
     */
    @Test
    public void testEnumConstantListOfEnumConstant()
    {
        ClassesParser parser = new ClassesParser(new Scanner("SINGLETON"));
        ASTEnumConstantList node = parser.parseEnumConstantList();
        checkSimple(node, ASTEnumConstant.class, COMMA);
        node.collapseThenPrint();
    }

    /**
     * Tests enum constant list.
     */
    @Test
    public void testEnumConstantList()
    {
        ClassesParser parser = new ClassesParser(new Scanner("RED, YELLOW, GREEN"));
        ASTEnumConstantList node = parser.parseEnumConstantList();
        checkList(node, COMMA, ASTEnumConstant.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests simple enum constant.
     */
    @Test
    public void testEnumConstantSimple()
    {
        ClassesParser parser = new ClassesParser(new Scanner("RED"));
        ASTEnumConstant node = parser.parseEnumConstant();
        checkSimple(node, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests full enum constant.
     */
    @Test
    public void testEnumConstantOfArgumentListClassBody()
    {
        ClassesParser parser = new ClassesParser(new Scanner("RED(\"#F9152F\") { override String toString() { return \"Red Light\"; } }"));
        ASTEnumConstant node = parser.parseEnumConstant();
        checkTrinary(node, null, ASTIdentifier.class, ASTArgumentList.class, ASTClassBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests simple class declaration.
     */
    @Test
    public void testClassDeclarationSimple()
    {
        ClassesParser parser = new ClassesParser(new Scanner("class Dummy {}"));
        ASTClassDeclaration node = parser.parseClassDeclaration();
        checkBinary(node, CLASS, ASTIdentifier.class, ASTClassBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests full class declaration.
     */
    @Test
    public void testClassDeclarationFull()
    {
        ClassesParser parser = new ClassesParser(new Scanner("public shared class FullTest<T> extends Test<T> implements Serializable, List<T> {}"));
        ASTClassDeclaration node = parser.parseClassDeclaration();
        checkNary(node, CLASS, ASTAccessModifier.class, ASTClassModifierList.class, ASTIdentifier.class,
                ASTTypeParameters.class, ASTSuperclass.class, ASTSuperinterfaces.class, ASTClassBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests superinterfaces (implements clause).
     */
    @Test
    public void testSuperinterfaces()
    {
        ClassesParser parser = new ClassesParser(new Scanner("implements Copyable"));
        ASTSuperinterfaces node = parser.parseSuperinterfaces();
        checkSimple(node, ASTDataTypeNoArrayList.class, IMPLEMENTS);
        node.collapseThenPrint();
    }

    /**
     * Tests data type no array of data type no array.
     */
    @Test
    public void testDataTypeNoArrayListOfClassPart()
    {
        ClassesParser parser = new ClassesParser(new Scanner("Serializable"));
        ASTDataTypeNoArrayList node = parser.parseDataTypeNoArrayList();
        checkSimple(node, ASTDataTypeNoArray.class, COMMA);
        node.collapseThenPrint();
    }

    /**
     * Tests data type no array list.
     */
    @Test
    public void testDataTypeNoArrayList()
    {
        ClassesParser parser = new ClassesParser(new Scanner("Serializable, Comparable<T>"));
        ASTDataTypeNoArrayList node = parser.parseDataTypeNoArrayList();
        checkList(node, COMMA, ASTDataTypeNoArray.class, 2);
        node.collapseThenPrint();
    }

    /**
     * Tests nested data type no array lists.
     */
    @Test
    public void testDataTypeNoArrayListNested()
    {
        ClassesParser parser = new ClassesParser(new Scanner("Serializable, Comparable<T>, RandomAccess"));
        ASTDataTypeNoArrayList node = parser.parseDataTypeNoArrayList();
        checkList(node, COMMA, ASTDataTypeNoArray.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests superclass (extends clause).
     */
    @Test
    public void testSuperclass()
    {
        ClassesParser parser = new ClassesParser(new Scanner("extends Thread"));
        ASTSuperclass node = parser.parseSuperclass();
        checkSimple(node, ASTDataTypeNoArray.class, EXTENDS);
        node.collapseThenPrint();
    }

    /**
     * Tests class modifier list.
     */
    @Test
    public void testClassModifierList()
    {
        ClassesParser parser = new ClassesParser(new Scanner("abstract final shared strictfp"));
        ASTClassModifierList node = parser.parseClassModifierList();
        checkList(node, null, ASTGeneralModifier.class, 4);
        node.collapseThenPrint();
    }

    /**
     * Tests empty class body.
     */
    @Test
    public void testClassBodyEmpty()
    {
        ClassesParser parser = new ClassesParser(new Scanner("{}"));
        ASTClassBody node = parser.parseClassBody();
        checkEmpty(node, OPEN_BRACE);
        node.collapseThenPrint();
    }

    /**
     * Tests class body.
     */
    @Test
    public void testClassBody()
    {
        ClassesParser parser = new ClassesParser(new Scanner("{\nprivate Integer i := 1;\nconstructor(Integer i) { this.i := i; }\nInteger getI() {\n    return i;\n}\n}"));
        ASTClassBody node = parser.parseClassBody();
        checkSimple(node, ASTClassPartList.class, OPEN_BRACE);
        node.collapseThenPrint();
    }

    /**
     * Tests class part list of class part.
     */
    @Test
    public void testClassPartListOfClassPart()
    {
        ClassesParser parser = new ClassesParser(new Scanner("private Integer i := 1;"));
        ASTClassPartList node = parser.parseClassPartList();
        checkSimple(node, ASTClassPart.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class part list.
     */
    @Test
    public void testClassPartList()
    {
        ClassesParser parser = new ClassesParser(new Scanner("private Integer i := 1;\nconstructor(Integer i) { this.i := i; }"));
        ASTClassPartList node = parser.parseClassPartList();
        checkList(node, null, ASTClassPart.class, 2);
        node.collapseThenPrint();
    }

    /**
     * Tests nested class part lists.
     */
    @Test
    public void testClassPartListNested()
    {
        ClassesParser parser = new ClassesParser(new Scanner("private Integer i := 1;\nconstructor(Integer i) { this.i := i; }\nInteger getI() {\n    return i;\n}"));
        ASTClassPartList node = parser.parseClassPartList();
        checkList(node, null, ASTClassPart.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of shared constructor.
     */
    @Test
    public void testClassPartOfSharedConstructor()
    {
        ClassesParser parser = new ClassesParser(new Scanner("shared constructor() { sharedVar := reallyComplicatedLogic(); }"));
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTSharedConstructor.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of method declaration with void result.
     */
    @Test
    public void testClassPartOfMethodDeclarationVoidResult()
    {
        ClassesParser parser = new ClassesParser(new Scanner("public abstract void method();"));
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTMethodDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of method declaration with void result and type parameters.
     */
    @Test
    public void testClassPartOfMethodDeclarationVoidResultTypeParameters()
    {
        ClassesParser parser = new ClassesParser(new Scanner("public abstract <T> void method(T param);"));
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTMethodDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of method declaration data type void result.
     */
    @Test
    public void testClassPartOfMethodDeclarationDataTypeResult()
    {
        ClassesParser parser = new ClassesParser(new Scanner("public abstract String method();"));
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTMethodDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of method declaration with data type result and type parameters.
     */
    @Test
    public void testClassPartOfMethodDeclarationDataTypeResultTypeParameters()
    {
        ClassesParser parser = new ClassesParser(new Scanner("public abstract <T> T method(T param);"));
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTMethodDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of method declaration with const result.
     */
    @Test
    public void testClassPartOfMethodDeclarationConstResult()
    {
        ClassesParser parser = new ClassesParser(new Scanner("const String method(String param);"));
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTMethodDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of field declaration.
     */
    @Test
    public void testClassPartOfFieldDeclaration()
    {
        ClassesParser parser = new ClassesParser(new Scanner("private Int myVar := 1, myVar2 := 2;"));
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTFieldDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of constructor declaration.
     */
    @Test
    public void testClassPartOfConstructorDeclaration()
    {
        ClassesParser parser = new ClassesParser(new Scanner("constructor(String s) : constructor(s) {}"));
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTConstructorDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of class declaration.
     */
    @Test
    public void testClassPartOfClassDeclaration()
    {
        ClassesParser parser = new ClassesParser(new Scanner("public shared class Nested {}"));
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTClassDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of enum declaration.
     */
    @Test
    public void testClassPartOfEnumDeclaration()
    {
        ClassesParser parser = new ClassesParser(new Scanner("private enum Light {RED, YELLOW, GREEN}"));
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTEnumDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of interface declaration.
     */
    @Test
    public void testClassPartOfInterfaceDeclaration()
    {
        ClassesParser parser = new ClassesParser(new Scanner("private interface TrafficLight { Light getStatus(); }"));
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTInterfaceDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests class part of annotation declaration.
     */
    @Test
    public void testClassPartOfAnnotationDeclaration()
    {
        ClassesParser parser = new ClassesParser(new Scanner("public annotation Test { String getStatus() default \"SUCCESS\"; }"));
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTAnnotationDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests shared constructor.
     */
    @Test
    public void testSharedConstructor()
    {
        ClassesParser parser = new ClassesParser(new Scanner("shared constructor() { sharedVar := reallyComplicatedLogic(); }"));
        ASTSharedConstructor node = parser.parseSharedConstructor();
        checkSimple(node, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests constructor declaration of access modifier, strictfp, and constructor invocation.
     */
    @Test
    public void testConstructorDeclarationOfAccessStrictfpConstructorInvocation()
    {
        ClassesParser parser = new ClassesParser(new Scanner("private strictfp constructor(String s) : super(s) { this.s := s; }"));
        ASTConstructorDeclaration node = parser.parseConstructorDeclaration();
        checkNary(node, null, ASTAccessModifier.class, ASTStrictfpModifier.class, ASTConstructorDeclarator.class, ASTConstructorInvocation.class, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests simple constructor declaration.
     */
    @Test
    public void testConstructorDeclarationSimple()
    {
        ClassesParser parser = new ClassesParser(new Scanner("constructor(String s) { this.s := s; }"));
        ASTConstructorDeclaration node = parser.parseConstructorDeclaration();
        checkBinary(node, ASTConstructorDeclarator.class, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests constructor invocation of primary, type arguments, and super.
     */
    @Test
    public void testConstructorInvocationOfPrimaryTypeArgumentsSuper()
    {
        ClassesParser parser = new ClassesParser(new Scanner(": (primary).<T>super()"));
        ASTConstructorInvocation node = parser.parseConstructorInvocation();
        checkBinary(node, SUPER, ASTPrimary.class, ASTTypeArguments.class);
        node.collapseThenPrint();
    }

    /**
     * Tests constructor invocation of primary and super.
     */
    @Test
    public void testConstructorInvocationOfPrimarySuper()
    {
        ClassesParser parser = new ClassesParser(new Scanner(": (primary).super()"));
        ASTConstructorInvocation node = parser.parseConstructorInvocation();
        checkSimple(node, ASTPrimary.class, SUPER);
        node.collapseThenPrint();
    }

    /**
     * Tests constructor invocation of expression name, type arguments, and super.
     */
    @Test
    public void testConstructorInvocationOfExpressionNameTypeArgumentsSuper()
    {
        ClassesParser parser = new ClassesParser(new Scanner(": expr.name.<String>super()"));
        ASTConstructorInvocation node = parser.parseConstructorInvocation();
        checkBinary(node, SUPER, ASTExpressionName.class, ASTTypeArguments.class);
        node.collapseThenPrint();
    }

    /**
     * Tests constructor invocation of expression name and super.
     */
    @Test
    public void testConstructorInvocationOfExpressionNameSuper()
    {
        ClassesParser parser = new ClassesParser(new Scanner(": expr.name.super()"));
        ASTConstructorInvocation node = parser.parseConstructorInvocation();
        checkSimple(node, ASTExpressionName.class, SUPER);
        node.collapseThenPrint();
    }

    /**
     * Tests constructor invocation of super and type arguments.
     */
    @Test
    public void testConstructorInvocationOfSuperTypeArguments()
    {
        ClassesParser parser = new ClassesParser(new Scanner(": <Integer>super(5)"));
        ASTConstructorInvocation node = parser.parseConstructorInvocation();
        checkBinary(node, SUPER, ASTTypeArguments.class, ASTArgumentList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests constructor invocation of constructor and type arguments.
     */
    @Test
    public void testConstructorInvocationOfConstructorTypeArguments()
    {
        ClassesParser parser = new ClassesParser(new Scanner(": <Integer>constructor()"));
        ASTConstructorInvocation node = parser.parseConstructorInvocation();
        checkSimple(node, ASTTypeArguments.class, CONSTRUCTOR);
        node.collapseThenPrint();
    }

    /**
     * Tests simple constructor invocation of constructor.
     */
    @Test
    public void testConstructorInvocationOfConstructorSimple()
    {
        ClassesParser parser = new ClassesParser(new Scanner(": constructor()"));
        ASTConstructorInvocation node = parser.parseConstructorInvocation();
        checkEmpty(node, CONSTRUCTOR);
        node.collapseThenPrint();
    }

    /**
     * Tests strictfp modifier by itself.
     */
    @Test
    public void testStrictfpModifier()
    {
        ClassesParser parser = new ClassesParser(new Scanner("strictfp"));
        ASTStrictfpModifier node = parser.parseStrictfpModifier();
        checkEmpty(node, STRICTFP);
        node.collapseThenPrint();
    }

    /**
     * Tests simple constructor declarator.
     */
    @Test
    public void testConstructorDeclaratorSimple()
    {
        ClassesParser parser = new ClassesParser(new Scanner("constructor()"));
        ASTConstructorDeclarator node = parser.parseConstructorDeclarator();
        checkEmpty(node, CONSTRUCTOR);
        node.collapseThenPrint();
    }

    /**
     * Tests full constructor declarator.
     */
    @Test
    public void testConstructorDeclaratorFull()
    {
        ClassesParser parser = new ClassesParser(new Scanner("<T> constructor(T param)"));
        ASTConstructorDeclarator node = parser.parseConstructorDeclarator();
        checkBinary(node, CONSTRUCTOR, ASTTypeParameters.class, ASTFormalParameterList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests full field declaration.
     */
    @Test
    public void testFieldDeclaration()
    {
        ClassesParser parser = new ClassesParser(new Scanner("public const final String aConstant := \"CONSTANT\";"));
        ASTFieldDeclaration node = parser.parseFieldDeclaration();
        checkNary(node, null, ASTAccessModifier.class, ASTFieldModifierList.class, ASTDataType.class, ASTVariableDeclaratorList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests constant field declaration.
     */
    @Test
    public void testFieldDeclarationOfConstant()
    {
        ClassesParser parser = new ClassesParser(new Scanner("public constant String aConstant := \"CONSTANT\";"));
        ASTFieldDeclaration node = parser.parseFieldDeclaration();
        checkNary(node, null, ASTAccessModifier.class, ASTFieldModifierList.class, ASTDataType.class, ASTVariableDeclaratorList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests simple field declaration.
     */
    @Test
    public void testFieldDeclarationSimple()
    {
        ClassesParser parser = new ClassesParser(new Scanner("String name := \"spruce\";"));
        ASTFieldDeclaration node = parser.parseFieldDeclaration();
        checkBinary(node, ASTDataType.class, ASTVariableDeclaratorList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests field modifier list.
     */
    @Test
    public void testFieldModifierList()
    {
        ClassesParser parser = new ClassesParser(new Scanner("const final shared transient volatile"));
        ASTFieldModifierList node = parser.parseFieldModifierList();
        checkList(node, null, ASTGeneralModifier.class, 5);
        node.collapseThenPrint();
    }

    /**
     * Tests bad field modifier list.
     */
    @Test
    public void testErrorFieldModifierListOfOverride()
    {
        ClassesParser parser = new ClassesParser(new Scanner("override"));
        assertThrows(CompileException.class, parser::parseFieldModifierList);
    }

    /**
     * Tests simple method declaration.
     */
    @Test
    public void testMethodDeclarationSimple()
    {
        ClassesParser parser = new ClassesParser(new Scanner("String toString() {\n    return this;\n}"));
        ASTMethodDeclaration node = parser.parseMethodDeclaration();
        checkBinary(node, ASTMethodHeader.class, ASTMethodBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests method declaration with access modifier and method modifier.
     */
    @Test
    public void testMethodDeclarationAccessModifierMethodModifier()
    {
        ClassesParser parser = new ClassesParser(new Scanner("public abstract Foo abstractMethod();"));
        ASTMethodDeclaration node = parser.parseMethodDeclaration();
        checkNary(node, null, ASTAccessModifier.class, ASTMethodModifierList.class, ASTMethodHeader.class, ASTMethodBody.class);
        node.collapseThenPrint();
    }

    /**
     * Tests method body of semicolon.
     */
    @Test
    public void testMethodBodyOfSemicolon()
    {
        ClassesParser parser = new ClassesParser(new Scanner(";"));
        ASTMethodBody node = parser.parseMethodBody();
        checkEmpty(node, SEMICOLON);
        node.collapseThenPrint();
    }

    /**
     * Tests method body of block.
     */
    @Test
    public void testMethodBodyOfBlock()
    {
        ClassesParser parser = new ClassesParser(new Scanner("{\n    out.println(\"Body!\");\n}"));
        ASTMethodBody node = parser.parseMethodBody();
        checkSimple(node, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests access modifier list of access modifier.
     */
    @Test
    public void testAccessModifierListOfAccessModifier()
    {
        ClassesParser parser = new ClassesParser(new Scanner("final"));
        ASTMethodModifierList node = parser.parseMethodModifierList();
        checkSimple(node, ASTGeneralModifier.class);
        node.collapseThenPrint();
    }
    /**
     * Tests access modifier list of access modifiers.
     */
    @Test
    public void testAccessModifierListOfAccessModifiers()
    {
        ClassesParser parser = new ClassesParser(new Scanner("final abstract shared strictfp"));
        ASTMethodModifierList node = parser.parseMethodModifierList();
        checkList(node, null, ASTGeneralModifier.class, 4);
        node.collapseThenPrint();
    }

    /**
     * Tests method modifier of public.
     */
    @Test
    public void testAccessModifierOfPublic()
    {
        ClassesParser parser = new ClassesParser(new Scanner("public"));
        ASTAccessModifier node = parser.parseAccessModifier();
        checkEmpty(node, PUBLIC);
        node.collapseThenPrint();
    }

    /**
     * Tests method modifier of protected.
     */
    @Test
    public void testAccessModifierOfProtected()
    {
        ClassesParser parser = new ClassesParser(new Scanner("protected"));
        ASTAccessModifier node = parser.parseAccessModifier();
        checkEmpty(node, PROTECTED);
        node.collapseThenPrint();
    }

    /**
     * Tests method modifier of abstract.
     */
    @Test
    public void testAccessModifierOfInternal()
    {
        ClassesParser parser = new ClassesParser(new Scanner("internal"));
        ASTAccessModifier node = parser.parseAccessModifier();
        checkEmpty(node, INTERNAL);
        node.collapseThenPrint();
    }

    /**
     * Tests access modifier of private.
     */
    @Test
    public void testAccessModifierOfPrivate()
    {
        ClassesParser parser = new ClassesParser(new Scanner("private"));
        ASTAccessModifier node = parser.parseAccessModifier();
        checkEmpty(node, PRIVATE);
        node.collapseThenPrint();
    }

    /**
     * Tests method modifier list.
     */
    @Test
    public void testMethodModifierList()
    {
        ClassesParser parser = new ClassesParser(new Scanner("abstract final override shared strictfp"));
        ASTMethodModifierList node = parser.parseMethodModifierList();
        checkList(node, null, ASTGeneralModifier.class, 5);
        node.collapseThenPrint();
    }

    /**
     * Tests bad method modifier list.
     */
    @Test
    public void testErrorMethodModifierListOfConst()
    {
        ClassesParser parser = new ClassesParser(new Scanner("const"));
        assertThrows(CompileException.class, parser::parseMethodModifierList);
    }

    /**
     * Tests method modifier list of method modifiers.
     */
    @Test
    public void testGeneralModifierListOfMethodModifiers()
    {
        ClassesParser parser = new ClassesParser(new Scanner("abstract const constant final override shared strictfp transient volatile"));
        ASTGeneralModifierList node = parser.parseGeneralModifierList();
        checkList(node, null, ASTGeneralModifier.class, 9);
        node.collapseThenPrint();
    }

    /**
     * Tests general modifier of abstract.
     */
    @Test
    public void testGeneralModifierOfAbstract()
    {
        ClassesParser parser = new ClassesParser(new Scanner("abstract"));
        ASTGeneralModifier node = parser.parseGeneralModifier();
        checkEmpty(node, ABSTRACT);
        node.collapseThenPrint();
    }

    /**
     * Tests general modifier of const.
     */
    @Test
    public void testGeneralModifierOfConst()
    {
        ClassesParser parser = new ClassesParser(new Scanner("const"));
        ASTGeneralModifier node = parser.parseGeneralModifier();
        checkEmpty(node, CONST);
        node.collapseThenPrint();
    }

    /**
     * Tests general modifier of final.
     */
    @Test
    public void testMethodModifierOfFinal()
    {
        ClassesParser parser = new ClassesParser(new Scanner("final"));
        ASTGeneralModifier node = parser.parseGeneralModifier();
        checkEmpty(node, FINAL);
        node.collapseThenPrint();
    }

    /**
     * Tests general modifier of override.
     */
    @Test
    public void testGeneralModifierOfOverride()
    {
        ClassesParser parser = new ClassesParser(new Scanner("override"));
        ASTGeneralModifier node = parser.parseGeneralModifier();
        checkEmpty(node, OVERRIDE);
        node.collapseThenPrint();
    }

    /**
     * Tests general modifier of shared.
     */
    @Test
    public void testGeneralModifierOfShared()
    {
        ClassesParser parser = new ClassesParser(new Scanner("shared"));
        ASTGeneralModifier node = parser.parseGeneralModifier();
        checkEmpty(node, SHARED);
        node.collapseThenPrint();
    }

    /**
     * Tests general modifier of strictfp.
     */
    @Test
    public void testGeneralModifierOfStrictfp()
    {
        ClassesParser parser = new ClassesParser(new Scanner("strictfp"));
        ASTGeneralModifier node = parser.parseGeneralModifier();
        checkEmpty(node, STRICTFP);
        node.collapseThenPrint();
    }

    /**
     * Tests general modifier of transient.
     */
    @Test
    public void testGeneralModifierOfTransient()
    {
        ClassesParser parser = new ClassesParser(new Scanner("transient"));
        ASTGeneralModifier node = parser.parseGeneralModifier();
        checkEmpty(node, TRANSIENT);
        node.collapseThenPrint();
    }

    /**
     * Tests general modifier of volatile.
     */
    @Test
    public void testGeneralModifierOfVolatile()
    {
        ClassesParser parser = new ClassesParser(new Scanner("volatile"));
        ASTGeneralModifier node = parser.parseGeneralModifier();
        checkEmpty(node, VOLATILE);
        node.collapseThenPrint();
    }

    /**
     * Tests simple method header.
     */
    @Test
    public void testMethodHeaderSimple()
    {
        ClassesParser parser = new ClassesParser(new Scanner("void toString() const"));
        ASTMethodHeader node = parser.parseMethodHeader();
        checkBinary(node, ASTResult.class, ASTMethodDeclarator.class);
        node.collapseThenPrint();
    }

    /**
     * Tests method header with type parameters.
     */
    @Test
    public void testMethodHeaderOfTypeParameters()
    {
        ClassesParser parser = new ClassesParser(new Scanner("<T> T getItem() const"));
        ASTMethodHeader node = parser.parseMethodHeader();
        checkTrinary(node, null, ASTTypeParameters.class, ASTResult.class, ASTMethodDeclarator.class);
        node.collapseThenPrint();
    }

    /**
     * Tests result of void.
     */
    @Test
    public void testResultOfVoid()
    {
        ClassesParser parser = new ClassesParser(new Scanner("void"));
        ASTResult node = parser.parseResult();
        checkEmpty(node, VOID);
        node.collapseThenPrint();
    }

    /**
     * Tests result of data type.
     */
    @Test
    public void testResultOfDataType()
    {
        ClassesParser parser = new ClassesParser(new Scanner("Map<String, Integer>"));
        ASTResult node = parser.parseResult();
        checkSimple(node, ASTDataType.class);
        node.collapseThenPrint();
    }

    /**
     * Tests result of const modifier and data type.
     */
    @Test
    public void testResultOfConstModifierDataType()
    {
        ClassesParser parser = new ClassesParser(new Scanner("const Map<String, Integer>"));
        ASTResult node = parser.parseResult();
        checkBinary(node, ASTConstModifier.class, ASTDataType.class);
        node.collapseThenPrint();
    }

    /**
     * Tests simple method declarator.
     */
    @Test
    public void testMethodDeclaratorSimple()
    {
        ClassesParser parser = new ClassesParser(new Scanner("update()"));
        ASTMethodDeclarator node = parser.parseMethodDeclarator();
        checkSimple(node, ASTIdentifier.class, OPEN_PARENTHESIS);
        node.collapseThenPrint();
    }

    /**
     * Tests method declarator of parameter list and const modifier.
     */
    @Test
    public void testMethodDeclaratorOfParameterListConstModifier()
    {
        ClassesParser parser = new ClassesParser(new Scanner("join(String sep) const)"));
        ASTMethodDeclarator node = parser.parseMethodDeclarator();
        checkTrinary(node, OPEN_PARENTHESIS, ASTIdentifier.class, ASTFormalParameterList.class, ASTConstModifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests const modifier by itself.
     */
    @Test
    public void testConstModifier()
    {
        ClassesParser parser = new ClassesParser(new Scanner("const"));
        ASTConstModifier node = parser.parseConstModifier();
        checkEmpty(node, CONST);
        node.collapseThenPrint();
    }

    /**
     * Tests formal parameter list of formal parameter.
     */
    @Test
    public void testFormalParameterListOfFormalParameter()
    {
        ClassesParser parser = new ClassesParser(new Scanner("const Int a"));
        ASTFormalParameterList node = parser.parseFormalParameterList();
        checkSimple(node, ASTFormalParameter.class, COMMA);
        node.collapseThenPrint();
    }

    /**
     * Tests formal parameter list.
     */
    @Test
    public void testFormalParameterList()
    {
        ClassesParser parser = new ClassesParser(new Scanner("String msg, Foo f, Bar b"));
        ASTFormalParameterList node = parser.parseFormalParameterList();
        checkList(node, COMMA, ASTFormalParameter.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests formal parameter list with varargs parameter list.
     */
    @Test
    public void testFormalParameterListOfLastVarargs()
    {
        ClassesParser parser = new ClassesParser(new Scanner("Point pt, Double... coordinates"));
        ASTFormalParameterList node = parser.parseFormalParameterList();
        checkList(node, COMMA, ASTFormalParameter.class, 2);
        node.collapseThenPrint();
    }

    /**
     * Tests if varargs not last, compiler error.
     */
    @Test
    public void testFormalParameterListVarargsNotLastError()
    {
        ClassesParser parser = new ClassesParser(new Scanner("Double... coordinates, Point pt"));
        assertThrows(CompileException.class, parser::parseFormalParameterList);
    }

    /**
     * Tests formal parameter, no variable modifier list, with ellipsis.
     */
    @Test
    public void testFormalParameterNoVMLEllipsis()
    {
        ClassesParser parser = new ClassesParser(new Scanner("String... args"));
        ASTFormalParameter node = parser.parseFormalParameter();
        checkBinary(node, ELLIPSIS, ASTDataType.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests formal parameter, variable modifier list, with ellipsis.
     */
    @Test
    public void testFormalParameterOfVMLEllipsis()
    {
        ClassesParser parser = new ClassesParser(new Scanner("final String... args"));
        ASTFormalParameter node = parser.parseFormalParameter();
        checkTrinary(node, ELLIPSIS, ASTVariableModifierList.class, ASTDataType.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests formal parameter, no variable modifier list, no ellipsis.
     */
    @Test
    public void testFormalParameterNoVMLNoEllipsis()
    {
        ClassesParser parser = new ClassesParser(new Scanner("String[] args"));
        ASTFormalParameter node = parser.parseFormalParameter();
        checkBinary(node, ASTDataType.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests formal parameter, variable modifier list, no ellipsis.
     */
    @Test
    public void testFormalParameterOfVMLNoEllipsis()
    {
        ClassesParser parser = new ClassesParser(new Scanner("final String[] args"));
        ASTFormalParameter node = parser.parseFormalParameter();
        checkTrinary(node, null, ASTVariableModifierList.class, ASTDataType.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }
}
