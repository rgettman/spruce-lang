package org.spruce.compiler.test;

import org.spruce.compiler.ast.*;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.parser.Parser;
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
     * Tests simple enum declaration.
     */
    @Test
    public void testEnumDeclarationSimple()
    {
        Parser parser = new Parser(new Scanner("enum Dummy {DUMMY}"));
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
        Parser parser = new Parser(new Scanner("public shared enum FullEnumTest implements Serializable {QUIZ, TEST, FINAL}"));
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
        Parser parser = new Parser(new Scanner("{\nRED, YELLOW, GREEN\n}"));
        ASTEnumBody node = parser.parseEnumBody();
        checkSimple(node, ASTEnumConstantList.class);
    }

    /**
     * Tests enum body of nothing.
     */
    @Test
    public void testEnumBodyOfNothing()
    {
        Parser parser = new Parser(new Scanner("{}"));
        ASTEnumBody node = parser.parseEnumBody();
        checkEmpty(node, null);
    }

    /**
     * Tests enum body of utility methods.
     */
    @Test
    public void testEnumBodyOfUtility()
    {
        Parser parser = new Parser(new Scanner("{\n;    shared void utility() {\n    out.println(\"Utility!\");\n}\n}"));
        ASTEnumBody node = parser.parseEnumBody();
        checkSimple(node, ASTEnumBodyDeclarations.class);
    }

    /**
     * Tests enum body of constants and class part list.
     */
    @Test
    public void testEnumBodyOfConstantsClassPartList()
    {
        Parser parser = new Parser(new Scanner("{\nRED, YELLOW, GREEN;\nshared void utility() {\n    out.println(\"Utility!\");\n}\n}"));
        ASTEnumBody node = parser.parseEnumBody();
        checkBinary(node, ASTEnumConstantList.class, ASTEnumBodyDeclarations.class);
    }

    /**
     * Tests enum body declarations.
     */
    @Test
    public void testEnumBodyDeclarations()
    {
        Parser parser = new Parser(new Scanner(";\nconstructor() {}"));
        ASTEnumBodyDeclarations node = parser.parseEnumBodyDeclarations();
        checkSimple(node, ASTClassPartList.class, SEMICOLON);
    }

    /**
     * Tests enum constant list of enum constant.
     */
    @Test
    public void testEnumConstantListOfEnumConstant()
    {
        Parser parser = new Parser(new Scanner("SINGLETON"));
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
        Parser parser = new Parser(new Scanner("RED, YELLOW, GREEN"));
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
        Parser parser = new Parser(new Scanner("RED"));
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
        Parser parser = new Parser(new Scanner("RED(\"#F9152F\") { override String toString() { return \"Red Light\"; } }"));
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
        Parser parser = new Parser(new Scanner("class Dummy {}"));
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
        Parser parser = new Parser(new Scanner("public shared class FullTest<T> extends Test<T> implements Serializable, List<T> {}"));
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
        Parser parser = new Parser(new Scanner("implements Copyable"));
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
        Parser parser = new Parser(new Scanner("Serializable"));
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
        Parser parser = new Parser(new Scanner("Serializable, Comparable<T>"));
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
        Parser parser = new Parser(new Scanner("Serializable, Comparable<T>, RandomAccess"));
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
        Parser parser = new Parser(new Scanner("extends Thread"));
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
        Parser parser = new Parser(new Scanner("abstract final shared strictfp"));
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
        Parser parser = new Parser(new Scanner("{}"));
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
        Parser parser = new Parser(new Scanner("{\nprivate Integer i := 1;\nconstructor(Integer i) { this.i := i; }\nInteger getI() {\n    return i;\n}\n}"));
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
        Parser parser = new Parser(new Scanner("private Integer i := 1;"));
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
        Parser parser = new Parser(new Scanner("private Integer i := 1;\nconstructor(Integer i) { this.i := i; }"));
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
        Parser parser = new Parser(new Scanner("private Integer i := 1;\nconstructor(Integer i) { this.i := i; }\nInteger getI() {\n    return i;\n}"));
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
        Parser parser = new Parser(new Scanner("shared constructor() { sharedVar := reallyComplicatedLogic(); }"));
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
        Parser parser = new Parser(new Scanner("public abstract void method();"));
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
        Parser parser = new Parser(new Scanner("public abstract <T> void method(T param);"));
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
        Parser parser = new Parser(new Scanner("public abstract String method();"));
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
        Parser parser = new Parser(new Scanner("public abstract <T> T method(T param);"));
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
        Parser parser = new Parser(new Scanner("const String method(String param);"));
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
        Parser parser = new Parser(new Scanner("private Int myVar := 1, myVar2 := 2;"));
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
        Parser parser = new Parser(new Scanner("constructor(String s) : constructor(s) {}"));
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
        Parser parser = new Parser(new Scanner("public shared class Nested {}"));
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
        Parser parser = new Parser(new Scanner("private enum Light {RED, YELLOW, GREEN}"));
        ASTClassPart node = parser.parseClassPart();
        checkSimple(node, ASTEnumDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests shared constructor.
     */
    @Test
    public void testSharedConstructor()
    {
        Parser parser = new Parser(new Scanner("shared constructor() { sharedVar := reallyComplicatedLogic(); }"));
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
        Parser parser = new Parser(new Scanner("private strictfp constructor(String s) : super(s) { this.s := s; }"));
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
        Parser parser = new Parser(new Scanner("constructor(String s) { this.s := s; }"));
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
        Parser parser = new Parser(new Scanner(": (primary).<T>super()"));
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
        Parser parser = new Parser(new Scanner(": (primary).super()"));
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
        Parser parser = new Parser(new Scanner(": expr.name.<String>super()"));
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
        Parser parser = new Parser(new Scanner(": expr.name.super()"));
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
        Parser parser = new Parser(new Scanner(": <Integer>super(5)"));
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
        Parser parser = new Parser(new Scanner(": <Integer>constructor()"));
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
        Parser parser = new Parser(new Scanner(": constructor()"));
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
        Parser parser = new Parser(new Scanner("strictfp"));
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
        Parser parser = new Parser(new Scanner("constructor()"));
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
        Parser parser = new Parser(new Scanner("<T> constructor(T param)"));
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
        Parser parser = new Parser(new Scanner("public const final String aConstant := \"CONSTANT\";"));
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
        Parser parser = new Parser(new Scanner("String name := \"spruce\";"));
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
        Parser parser = new Parser(new Scanner("const final shared transient volatile"));
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
        Parser parser = new Parser(new Scanner("override"));
        assertThrows(CompileException.class, parser::parseFieldModifierList);
    }

    /**
     * Tests simple method declaration.
     */
    @Test
    public void testMethodDeclarationSimple()
    {
        Parser parser = new Parser(new Scanner("String toString() {\n    return this;\n}"));
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
        Parser parser = new Parser(new Scanner("public abstract Foo abstractMethod();"));
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
        Parser parser = new Parser(new Scanner(";"));
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
        Parser parser = new Parser(new Scanner("{\n    out.println(\"Body!\");\n}"));
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
        Parser parser = new Parser(new Scanner("final"));
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
        Parser parser = new Parser(new Scanner("final abstract shared strictfp"));
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
        Parser parser = new Parser(new Scanner("public"));
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
        Parser parser = new Parser(new Scanner("protected"));
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
        Parser parser = new Parser(new Scanner("internal"));
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
        Parser parser = new Parser(new Scanner("private"));
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
        Parser parser = new Parser(new Scanner("abstract final override shared strictfp"));
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
        Parser parser = new Parser(new Scanner("const"));
        assertThrows(CompileException.class, parser::parseMethodModifierList);
    }

    /**
     * Tests method modifier list of method modifiers.
     */
    @Test
    public void testGeneralModifierListOfMethodModifiers()
    {
        Parser parser = new Parser(new Scanner("abstract const final override shared strictfp transient volatile"));
        ASTGeneralModifierList node = parser.parseGeneralModifierList();
        checkList(node, null, ASTGeneralModifier.class, 8);
        node.collapseThenPrint();
    }

    /**
     * Tests general modifier of abstract.
     */
    @Test
    public void testGeneralModifierOfAbstract()
    {
        Parser parser = new Parser(new Scanner("abstract"));
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
        Parser parser = new Parser(new Scanner("const"));
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
        Parser parser = new Parser(new Scanner("final"));
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
        Parser parser = new Parser(new Scanner("override"));
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
        Parser parser = new Parser(new Scanner("shared"));
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
        Parser parser = new Parser(new Scanner("strictfp"));
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
        Parser parser = new Parser(new Scanner("transient"));
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
        Parser parser = new Parser(new Scanner("volatile"));
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
        Parser parser = new Parser(new Scanner("void toString() const"));
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
        Parser parser = new Parser(new Scanner("<T> T getItem() const"));
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
        Parser parser = new Parser(new Scanner("void"));
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
        Parser parser = new Parser(new Scanner("Map<String, Integer>"));
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
        Parser parser = new Parser(new Scanner("const Map<String, Integer>"));
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
        Parser parser = new Parser(new Scanner("update()"));
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
        Parser parser = new Parser(new Scanner("join(String sep) const)"));
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
        Parser parser = new Parser(new Scanner("const"));
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
        Parser parser = new Parser(new Scanner("const Int a"));
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
        Parser parser = new Parser(new Scanner("String msg, Foo f, Bar b"));
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
        Parser parser = new Parser(new Scanner("Point pt, Double... coordinates"));
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
        Parser parser = new Parser(new Scanner("Double... coordinates, Point pt"));
        assertThrows(CompileException.class, parser::parseFormalParameterList);
    }

    /**
     * Tests formal parameter, no variable modifier list, with ellipsis.
     */
    @Test
    public void testFormalParameterNoVMLEllipsis()
    {
        Parser parser = new Parser(new Scanner("String... args"));
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
        Parser parser = new Parser(new Scanner("final String... args"));
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
        Parser parser = new Parser(new Scanner("String[] args"));
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
        Parser parser = new Parser(new Scanner("final String[] args"));
        ASTFormalParameter node = parser.parseFormalParameter();
        checkTrinary(node, null, ASTVariableModifierList.class, ASTDataType.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }
}
