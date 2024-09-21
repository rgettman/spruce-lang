package org.spruce.compiler.test;

import org.spruce.compiler.ast.classes.ASTAnnotationDeclaration;
import org.spruce.compiler.ast.classes.ASTClassDeclaration;
import org.spruce.compiler.ast.classes.ASTEnumDeclaration;
import org.spruce.compiler.ast.classes.ASTInterfaceDeclaration;
import org.spruce.compiler.ast.classes.ASTRecordDeclaration;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.names.ASTIdentifierList;
import org.spruce.compiler.ast.names.ASTNamespaceName;
import org.spruce.compiler.ast.names.ASTNamespaceOrTypeName;
import org.spruce.compiler.ast.names.ASTTypeName;
import org.spruce.compiler.ast.toplevel.*;
import org.spruce.compiler.parser.Parser;
import org.spruce.compiler.parser.TopLevelParser;
import org.spruce.compiler.scanner.Scanner;
import static org.spruce.compiler.scanner.TokenType.*;
import static org.spruce.compiler.test.ParserTestUtility.*;

import org.junit.jupiter.api.Test;

/**
 * All tests for the parser related to top level productions.
 */
public class ParserTopLevelTest
{
    /**
     * Tests empty ordinary compilation unit.
     */
    @Test
    public void testOrdinaryCompilationUnitEmpty()
    {
        TopLevelParser parser = new Parser(new Scanner("")).getTopLevelParser();
        ASTOrdinaryCompilationUnit node = parser.parseOrdinaryCompilationUnit();
        checkEmpty(node, null);
        node.collapseThenPrint();
    }

    /**
     * Tests full ordinary compilation unit.
     */
    @Test
    public void testOrdinaryCompilationUnitFull()
    {
        TopLevelParser parser = new Parser(new Scanner("namespace foo;\nuse project.Bar;\npublic class Baz<T> extends Bar<T> {}\nenum Light {RED, YELLOW, GREEN}")).getTopLevelParser();
        ASTOrdinaryCompilationUnit node = parser.parseOrdinaryCompilationUnit();
        checkTrinary(node, null, ASTNamespaceDeclaration.class, ASTUseDeclarationList.class, ASTTypeDeclarationList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests use declaration list of use declaration.
     */
    @Test
    public void testUseDeclarationListOfUseDeclaration()
    {
        TopLevelParser parser = new Parser(new Scanner("use spruce.collections.List;")).getTopLevelParser();
        ASTUseDeclarationList node = parser.parseUseDeclarationList();
        checkSimple(node, ASTUseDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests use declaration list of multiple use declarations.
     */
    @Test
    public void testUseDeclarationListOfMultipleUseDeclarations()
    {
        TopLevelParser parser = new Parser(new Scanner("use spruce.collections.{List, ArrayList};\nuse spruce.reflection.*;\nuse shared spruce.test.Assertions.*;")).getTopLevelParser();
        ASTUseDeclarationList node = parser.parseUseDeclarationList();
        checkList(node, null, ASTUseDeclaration.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests namespace declaration.
     */
    @Test
    public void testNamespaceDeclaration()
    {
        TopLevelParser parser = new Parser(new Scanner("namespace spruce.test.parser;")).getTopLevelParser();
        ASTNamespaceDeclaration node = parser.parseNamespaceDeclaration();
        checkSimple(node, ASTNamespaceName.class, NAMESPACE);
        node.collapseThenPrint();
    }

    /**
     * Tests use declaration of use shared all declaration.
     */
    @Test
    public void testUseDeclarationOfUSAD()
    {
        TopLevelParser parser = new Parser(new Scanner("use shared spruce.test.Assertions.*;")).getTopLevelParser();
        ASTUseDeclaration node = parser.parseUseDeclaration();
        checkSimple(node, ASTUseSharedAllDeclaration.class);

        ASTUseSharedAllDeclaration rad = (ASTUseSharedAllDeclaration) node.getChildren().get(0);
        checkSimple(rad, ASTTypeName.class, USE);
        node.collapseThenPrint();
    }

    /**
     * Tests use declaration of use shared type declaration.
     */
    @Test
    public void testUseDeclarationOfUSTD()
    {
        TopLevelParser parser = new Parser(new Scanner("use shared spruce.test.Assertions.assertEquals;")).getTopLevelParser();
        ASTUseDeclaration node = parser.parseUseDeclaration();
        checkSimple(node, ASTUseSharedTypeDeclaration.class);

        ASTUseSharedTypeDeclaration rtd = (ASTUseSharedTypeDeclaration) node.getChildren().get(0);
        checkBinary(rtd, USE, ASTTypeName.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests use declaration of use shared multiple declaration.
     */
    @Test
    public void testUseDeclarationOfUSMD()
    {
        TopLevelParser parser = new Parser(new Scanner("use shared spruce.test.Assertions.{assertEquals, assertTrue, assertFalse};")).getTopLevelParser();
        ASTUseDeclaration node = parser.parseUseDeclaration();
        checkSimple(node, ASTUseSharedMultDeclaration.class);

        ASTUseSharedMultDeclaration rmd = (ASTUseSharedMultDeclaration) node.getChildren().get(0);
        checkBinary(rmd, USE, ASTTypeName.class, ASTIdentifierList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests use declaration of use all declaration.
     */
    @Test
    public void testUseDeclarationOfUAD()
    {
        TopLevelParser parser = new Parser(new Scanner("use spruce.collections.*;")).getTopLevelParser();
        ASTUseDeclaration node = parser.parseUseDeclaration();
        checkSimple(node, ASTUseAllDeclaration.class);

        ASTUseAllDeclaration rad = (ASTUseAllDeclaration) node.getChildren().get(0);
        checkSimple(rad, ASTNamespaceOrTypeName.class, USE);
        node.collapseThenPrint();
    }

    /**
     * Tests use declaration of use type declaration.
     */
    @Test
    public void testUseDeclarationOfUTD()
    {
        TopLevelParser parser = new Parser(new Scanner("use spruce.collections.ArrayList;")).getTopLevelParser();
        ASTUseDeclaration node = parser.parseUseDeclaration();
        checkSimple(node, ASTUseTypeDeclaration.class);

        ASTUseTypeDeclaration rtd = (ASTUseTypeDeclaration) node.getChildren().get(0);
        checkSimple(rtd, ASTTypeName.class, USE);
        node.collapseThenPrint();
    }

    /**
     * Tests use declaration of use multiple declaration.
     */
    @Test
    public void testUseDeclarationOfUMD()
    {
        TopLevelParser parser = new Parser(new Scanner("use spruce.collections.{List, ArrayList, LinkedList};")).getTopLevelParser();
        ASTUseDeclaration node = parser.parseUseDeclaration();
        checkSimple(node, ASTUseMultDeclaration.class);

        ASTUseMultDeclaration rmd = (ASTUseMultDeclaration) node.getChildren().get(0);
        checkBinary(rmd, USE, ASTNamespaceOrTypeName.class, ASTIdentifierList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests type declaration list of type declaration.
     */
    @Test
    public void testTypeDeclarationListOfTypeDeclaration()
    {
        TopLevelParser parser = new Parser(new Scanner("class Foo {}")).getTopLevelParser();
        ASTTypeDeclarationList node = parser.parseTypeDeclarationList();
        checkSimple(node, ASTTypeDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests type declaration list of multiple type declarations.
     */
    @Test
    public void testTypeDeclarationListOfMultipleTypeDeclarations()
    {
        TopLevelParser parser = new Parser(new Scanner("class Foo {}\nenum Bar {CHOCOLATE, EXAM, SAND}\ninterface Baz {}")).getTopLevelParser();
        ASTTypeDeclarationList node = parser.parseTypeDeclarationList();
        checkList(node, null, ASTTypeDeclaration.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests type declaration of class declaration.
     */
    @Test
    public void testTypeDeclarationOfClassDeclaration()
    {
        TopLevelParser parser = new Parser(new Scanner("public abstract class Dummy<T> { abstract void test(); }")).getTopLevelParser();
        ASTTypeDeclaration node = parser.parseTypeDeclaration();
        checkSimple(node, ASTClassDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests type declaration of enum declaration.
     */
    @Test
    public void testTypeDeclarationOfEnumDeclaration()
    {
        TopLevelParser parser = new Parser(new Scanner("public shared enum TrafficLight {RED, YELLOW, GREEN}")).getTopLevelParser();
        ASTTypeDeclaration node = parser.parseTypeDeclaration();
        checkSimple(node, ASTEnumDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests type declaration of interface declaration.
     */
    @Test
    public void testTypeDeclarationOfInterfaceDeclaration()
    {
        TopLevelParser parser = new Parser(new Scanner("protected shared interface Dummy { public void run();}")).getTopLevelParser();
        ASTTypeDeclaration node = parser.parseTypeDeclaration();
        checkSimple(node, ASTInterfaceDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests type declaration of annotation declaration.
     */
    @Test
    public void testTypeDeclarationOfAnnotationDeclaration()
    {
        TopLevelParser parser = new Parser(new Scanner("public shared annotation Spruce { String language();}")).getTopLevelParser();
        ASTTypeDeclaration node = parser.parseTypeDeclaration();
        checkSimple(node, ASTAnnotationDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests type declaration of record declaration.
     */
    @Test
    public void testTypeDeclarationOfRecordDeclaration()
    {
        TopLevelParser parser = new Parser(new Scanner("internal record Redacted(String byWhom) { }")).getTopLevelParser();
        ASTTypeDeclaration node = parser.parseTypeDeclaration();
        checkSimple(node, ASTRecordDeclaration.class);
        node.collapseThenPrint();
    }
}
