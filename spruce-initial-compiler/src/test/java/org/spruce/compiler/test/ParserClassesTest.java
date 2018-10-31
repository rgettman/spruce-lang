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
        Parser parser = new Parser(new Scanner("public const final String constant = \"CONSTANT\";"));
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
        Parser parser = new Parser(new Scanner("String name = \"spruce\";"));
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
