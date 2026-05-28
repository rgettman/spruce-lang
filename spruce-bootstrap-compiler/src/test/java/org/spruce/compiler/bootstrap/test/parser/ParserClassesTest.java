package org.spruce.compiler.bootstrap.test.parser;

import org.spruce.compiler.bootstrap.ast.ASTKeywordNode;
import org.spruce.compiler.bootstrap.ast.classes.*;
import org.spruce.compiler.bootstrap.ast.expressions.*;
import org.spruce.compiler.bootstrap.ast.names.*;
import org.spruce.compiler.bootstrap.ast.statements.*;
import org.spruce.compiler.bootstrap.ast.types.*;
import org.spruce.compiler.bootstrap.common.BaseMessageProducer;
import org.spruce.compiler.bootstrap.parser.ClassesParser;
import org.spruce.compiler.bootstrap.parser.Parser;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.scanner.Scanner;
import static org.spruce.compiler.bootstrap.scanner.TokenType.*;
import static org.spruce.compiler.bootstrap.test.parser.ParserTestUtility.*;

import org.junit.jupiter.api.Test;

import static org.spruce.compiler.bootstrap.ast.ASTListNode.Type.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * All tests for the parser related to classes, methods, etc.
 */
public class ParserClassesTest {

    /**
     * Tests simple interface declaration.
     */
    @Test
    public void testInterfaceDeclarationSimple() {
        ClassesParser parser = getClassesParser("interface Dummy {}");
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTInterfaceDeclaration node = parser.parseInterfaceDeclaration(loc, genModList);
        ensureNoErrors(node, parser);

        assertEquals("Dummy", node.getName().getValue());
        assertFalse(node.getExtendsInterfaces().isPresent());
        checkList(node.getInterfaceParts(), INTERFACE_PARTS, ASTInterfacePart.class, 0);
    }

    /**
     * Tests full interface declaration.
     */
    @Test
    public void testInterfaceDeclarationFull() {
        ClassesParser parser = getClassesParser("""
            interface FullTest extends Test, Serializable, List
            {}
            """);
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTInterfaceDeclaration node = parser.parseInterfaceDeclaration(loc, genModList);
        ensureNoErrors(node, parser);

        assertEquals("FullTest", node.getName().getValue());
        assertTrue(node.getExtendsInterfaces().isPresent());
        checkList(node.getInterfaceParts(), INTERFACE_PARTS, ASTInterfacePart.class, 0);
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
                    Integer getI();
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
                    Integer getI();
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
                    Integer getI();
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
                String getToo();
                TrafficLight getStatus();
                class Nested {}
                interface Helper {}
                """);
        ASTInterfacePartList node = parser.parseInterfacePartList();
        ensureNoErrors(node, parser);
        checkList(node, INTERFACE_PARTS, ASTInterfacePart.class, 5);
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
        ClassesParser parser = getClassesParser("void method();");
        ASTInterfacePart node = parser.parseInterfacePart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTInterfaceMethodDeclaration.class, node);
    }

    /**
     * Tests interface part of method declaration data type void result.
     */
    @Test
    public void testInterfacePartOfMethodDeclarationDataTypeResult() {
        ClassesParser parser = getClassesParser("String method();");
        ASTInterfacePart node = parser.parseInterfacePart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTInterfaceMethodDeclaration.class, node);
    }

    /**
     * Tests interface part of method declaration with mut result.
     */
    @Test
    public void testInterfacePartOfMethodDeclarationConstResult() {
        ClassesParser parser = getClassesParser("String method(String param);");
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
        ClassesParser parser = getClassesParser("class Nested {}");
        ASTInterfacePart node = parser.parseInterfacePart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTClassDeclaration.class, node);
    }

    /**
     * Tests interface part of interface declaration.
     */
    @Test
    public void testInterfacePartOfInterfaceDeclaration() {
        ClassesParser parser = getClassesParser("interface TrafficLight { Light getStatus(); }");
        ASTInterfacePart node = parser.parseInterfacePart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTInterfaceDeclaration.class, node);
    }

    /**
     * Tests simple interface method declaration.
     */
    @Test
    public void testInterfaceMethodDeclarationSimple() {
        ClassesParser parser = getClassesParser("Boolean add(T element);");
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTInterfaceMethodDeclaration node = parser.parseInterfaceMethodDeclaration(loc, genModList, dt);
        ensureNoErrors(node, parser);

        checkList(node.getModifierList(), INTERFACE_METHOD_MODIFIERS, ASTKeywordNode.class, 0);
        assertNotNull(node.getHeader());
        assertNotNull(node.getBody());
    }

    /**
     * Tests bad interface method declaration of bad modifier.
     */
    @Test
    public void testInterfaceMethodDeclarationBadMod() {
        ClassesParser parser = getClassesParser("""
            abstract void addAll(Collection other) {
                for (Any element in other) {
                    add(other);
                }
            }
            """);
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTInterfaceMethodDeclaration node = parser.parseInterfaceMethodDeclaration(loc, genModList);
        expectError(node, parser);
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
        ASTConstantDeclaration node = parser.parseConstantDeclaration(loc, genModList, dt);
        expectError(node, parser);
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
        ASTConstantDeclaration node = parser.parseConstantDeclaration(loc, genModList, dt);
        ensureNoErrors(node, parser);

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
        ASTConstantDeclaration node = parser.parseConstantDeclaration(loc, genModList, dt);
        expectError(node, parser);
    }

    /**
     * Tests full class declaration.
     */
    @Test
    public void testClassDeclarationFull() {
        ClassesParser parser = getClassesParser("""
            class FullTest extends Test implements Serializable, List
            {}
            """);
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTClassDeclaration node = parser.parseClassDeclaration(loc, genModList);
        ensureNoErrors(node, parser);

        assertEquals("FullTest", node.getName().getValue());
        assertTrue(node.getSuperclass().isPresent());
        checkList(node.getSuperclass().get(), SIMPLE_TYPES, ASTSimpleType.class, 1);
        assertTrue(node.getSuperinterfaces().isPresent());
        checkList(node.getSuperinterfaces().get(), DATA_TYPES_NO_ARRAY, ASTDataTypeNoArray.class, 2);
        checkList(node.getClassParts(), CLASS_PARTS, ASTClassPart.class, 0);
    }

    /**
     * Test abstract class.
     */
    @Test
    public void testClassAbstract() {
        ClassesParser parser = getClassesParser("abstract class AbstractClass {}");
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTClassDeclaration node = parser.parseClassDeclaration(loc, genModList);
        ensureNoErrors(node, parser);

        checkList(node.getClassModifierList(), CLASS_MODIFIERS, ASTKeywordNode.class, 1);
        assertEquals("AbstractClass", node.getName().getValue());
        assertFalse(node.getSuperclass().isPresent());
        assertFalse(node.getSuperinterfaces().isPresent());
        checkList(node.getClassParts(), CLASS_PARTS, ASTClassPart.class, 0);
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
     * Test bare class.
     */
    @Test
    public void testClassBare() {
        ClassesParser parser = getClassesParser("class SomeClass {}");
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTClassDeclaration node = parser.parseClassDeclaration(loc, genModList);
        ensureNoErrors(node, parser);

        assertEquals("SomeClass", node.getName().getValue());
        assertFalse(node.getSuperclass().isPresent());
        assertFalse(node.getSuperinterfaces().isPresent());
        checkList(node.getClassParts(), CLASS_PARTS, ASTClassPart.class, 0);
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
                    Integer i = 1;
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
                String foo;
                constructor(String foo) { self.foo = foo; }
                String getFoo() {
                    return foo;
                }
                class Nested {}
                """);
        ASTClassPartList node = parser.parseClassPartList();
        ensureNoErrors(node, parser);
        checkList(node, CLASS_PARTS, ASTClassPart.class, 4);
    }

    /**
     * Tests class part list of class part.
     */
    @Test
    public void testClassPartListOfClassPart() {
        ClassesParser parser = getClassesParser("Integer i = 1;");
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
            Integer i = 1;
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
            Integer i = 1;
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
     * Tests class part of method declaration with void result.
     */
    @Test
    public void testClassPartOfMethodDeclarationVoidResult() {
        ClassesParser parser = getClassesParser("abstract void method();");
        ASTClassPart node = parser.parseClassPart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTMethodDeclaration.class, node);
    }

    /**
     * Tests class part of method declaration data type void result.
     */
    @Test
    public void testClassPartOfMethodDeclarationDataTypeResult() {
        ClassesParser parser = getClassesParser("String method();");
        ASTClassPart node = parser.parseClassPart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTMethodDeclaration.class, node);
    }

    /**
     * Tests class part of method declaration with data type result and type parameters.
     */
    @Test
    public void testClassPartOfMethodDeclarationDataTypeResultTypeParameters() {
        ClassesParser parser = getClassesParser("T method(T param);");
        ASTClassPart node = parser.parseClassPart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTMethodDeclaration.class, node);
    }

    /**
     * Tests class part of field declaration.
     */
    @Test
    public void testClassPartOfFieldDeclaration() {
        ClassesParser parser = getClassesParser("Int myVar = 1, myVar2 = 2;");
        ASTClassPart node = parser.parseClassPart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTFieldDeclaration.class, node);
    }

    /**
     * Tests class part of constructor declaration.
     */
    @Test
    public void testClassPartOfConstructorDeclaration() {
        ClassesParser parser = getClassesParser("constructor(String s) {}");
        ASTClassPart node = parser.parseClassPart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTConstructorDeclaration.class, node);
    }

    /**
     * Tests class part of class declaration.
     */
    @Test
    public void testClassPartOfClassDeclaration() {
        ClassesParser parser = getClassesParser("class Nested {}");
        ASTClassPart node = parser.parseClassPart();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTClassDeclaration.class, node);
    }

    /**
     * Tests bad class part of constructor but with a general modifier.
     */
    @Test
    public void testClassPartBadConstructorGeneralModifier() {
        ClassesParser parser = getClassesParser("""
                override constructor() {
                }
                """);
        ASTClassPart node = parser.parseClassPart();
        expectError(node, parser);
    }

    /**
     * Tests simple constructor declaration.
     */
    @Test
    public void testConstructorDeclarationSimple() {
        ClassesParser parser = getClassesParser("constructor(String s) { self.s = s; }");
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTConstructorDeclaration node = parser.parseConstructorDeclaration(loc);
        ensureNoErrors(node, parser);

        assertNotNull(node.getConstructorDecl());
        assertNotNull(node.getBlock());
    }

    /**
     * Tests simple constructor declarator.
     */
    @Test
    public void testConstructorDeclaratorSimple() {
        ClassesParser parser = getClassesParser("constructor()");
        ASTConstructorDeclarator node = parser.parseConstructorDeclarator();
        ensureNoErrors(node, parser);

        checkList(node.getFormalParamList(), FORMAL_PARAMETERS, ASTFormalParameter.class, 0);
    }

    /**
     * Tests full constructor declarator.
     */
    @Test
    public void testConstructorDeclaratorFull() {
        ClassesParser parser = getClassesParser("constructor(T param)");
        ASTConstructorDeclarator node = parser.parseConstructorDeclarator();
        ensureNoErrors(node, parser);

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
        ClassesParser parser = getClassesParser("constant String aConstant = \"CONSTANT\";");
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTFieldDeclaration node = parser.parseFieldDeclaration(loc, genModList, dt);
        ensureNoErrors(node, parser);

        checkList(node.getFieldModList(), FIELD_MODIFIERS, ASTKeywordNode.class, 1);
        assertNotNull(node.getDataType());
        checkList(node.getVarDeclList(), VARIABLE_DECLARATORS, ASTVariableDeclarator.class, 1);
    }

    /**
     * Tests constant field declaration.
     */
    @Test
    public void testFieldDeclarationOfConstant() {
        ClassesParser parser = getClassesParser("constant String aConstant = \"CONSTANT\";");
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTFieldDeclaration node = parser.parseFieldDeclaration(loc, genModList, dt);
        ensureNoErrors(node, parser);

        checkList(node.getFieldModList(), FIELD_MODIFIERS, ASTKeywordNode.class, 1);
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
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTFieldDeclaration node = parser.parseFieldDeclaration(loc, genModList, dt);
        ensureNoErrors(node, parser);

        checkList(node.getFieldModList(), FIELD_MODIFIERS, ASTKeywordNode.class, 0);
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
        ASTDataType dt = parser.getTypesParser().parseDataType();
        Location loc = genModList.getLocation();
        ASTFieldDeclaration node = parser.parseFieldDeclaration(loc,genModList, dt);
        expectError(node, parser);
    }

    /**
     * Tests bad field declaration of no semicolon.
     */
    @Test
    public void testFieldDeclarationNoSemicolon() {
        ClassesParser parser = getClassesParser("constant String name = \"bad\"}");
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        Location loc = genModList.getLocation();
        ASTFieldDeclaration node = parser.parseFieldDeclaration(loc, genModList, dt);
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
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTMethodDeclaration node = parser.parseMethodDeclaration(loc, genModList, dt);
        ensureNoErrors(node, parser);

        checkList(node.getMethodModList(), METHOD_MODIFIERS, ASTKeywordNode.class, 0);
        assertNotNull(node.getHeader());
        assertNotNull(node.getBody());
    }


    /**
     * Tests bad method declaration of bad modifier.
     */
    @Test
    public void testMethodDeclarationBadModifier() {
        ClassesParser parser = getClassesParser("constant Foo abstractMethod();");
        ASTGeneralModifierList genModList = parser.parseGeneralModifierList();
        Location loc = genModList.getLocation();
        ASTMethodDeclaration node = parser.parseMethodDeclaration(loc, genModList);
        expectError(node, parser);
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
        ClassesParser parser = getClassesParser("""
            {
                stdout.println("Body!");
            }
        """);
        ASTMethodBody node = parser.parseMethodBody();
        ensureNoErrors(node, parser);
        assertTrue(node.getBlock().isPresent());
    }

    /**
     * Tests bad method body of block or semicolon.
     */
    @Test
    public void testMethodBodyOfNoBlockOrSemicolon() {
        ClassesParser parser = getClassesParser("constant");
        ASTMethodBody node = parser.parseMethodBody();
        expectError(node, parser, 7);
    }

    /**
     * Tests general modifier list of class modifiers.
     */
    @Test
    public void testGeneralModifierListOfClassModifiers() {
        ClassesParser parser = getClassesParser("abstract");
        ASTGeneralModifierList node = parser.parseGeneralModifierList();
        ensureNoErrors(node, parser);
        checkList(node, GENERAL_MODIFIERS, ASTKeywordNode.class, 1);
    }

    /**
     * Tests general modifier list of method modifiers.
     */
    @Test
    public void testGeneralModifierListOfMethodModifiers() {
        ClassesParser parser = getClassesParser("abstract override");
        ASTGeneralModifierList node = parser.parseGeneralModifierList();
        ensureNoErrors(node, parser);
        checkList(node, GENERAL_MODIFIERS, ASTKeywordNode.class, 2);
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
     * Tests simple method header.
     */
    @Test
    public void testMethodHeaderSimple() {
        ClassesParser parser = getClassesParser("void toString() const");
        ASTMethodHeader node = parser.parseMethodHeader();
        ensureNoErrors(node, parser);

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

        assertFalse(node.getDataType().isPresent());
        assertTrue(node.getVoidKeyword().isPresent());
    }

    /**
     * Tests result of data type.
     */
    @Test
    public void testResultOfDataType() {
        ClassesParser parser = getClassesParser("Map");
        ASTResult node = parser.parseResult();
        ensureNoErrors(node, parser);

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
    }

    /**
     * Tests bad method declarator of no identifier.
     */
    @Test
    public void testMethodDeclaratorNoIdentifier() {
        ClassesParser parser = getClassesParser("(String sep))");
        ASTMethodDeclarator node = parser.parseMethodDeclarator();
        expectError(node, parser);
    }

    /**
     * Tests bad method declarator of no open parenthesis.
     */
    @Test
    public void testMethodDeclaratorNoOpenParen() {
        ClassesParser parser = getClassesParser("test String sep))");
        ASTMethodDeclarator node = parser.parseMethodDeclarator();
        expectError(node, parser);
    }

    /**
     * Tests bad method declarator of no close parenthesis.
     */
    @Test
    public void testMethodDeclaratorNoCloseParen() {
        ClassesParser parser = getClassesParser("test(String sep  test2");
        ASTMethodDeclarator node = parser.parseMethodDeclarator();
        expectError(node, parser);
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
     * Tests formal parameter, no variable modifier list, no ellipsis.
     */
    @Test
    public void testFormalParameterNoVMLNoEllipsis() {
        ClassesParser parser = getClassesParser("String args");
        ASTFormalParameter node = parser.parseFormalParameter();
        ensureNoErrors(node, parser);

        assertNotNull(node.getDataType());
        assertEquals("args", node.getName().getValue());
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
