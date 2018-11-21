package org.spruce.compiler.test;

import org.spruce.compiler.ast.classes.ASTAnnotationDeclaration;
import org.spruce.compiler.ast.classes.ASTClassDeclaration;
import org.spruce.compiler.ast.classes.ASTEnumDeclaration;
import org.spruce.compiler.ast.classes.ASTInterfaceDeclaration;
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
        TopLevelParser parser = new Parser(new Scanner("namespace foo;\nrecognize project.Bar;\npublic class Baz<T> extends Bar<T> {}\nenum Light {RED, YELLOW, GREEN}")).getTopLevelParser();
        ASTOrdinaryCompilationUnit node = parser.parseOrdinaryCompilationUnit();
        checkTrinary(node, null, ASTNamespaceDeclaration.class, ASTRecognizeDeclarationList.class, ASTTypeDeclarationList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests recognize declaration list of recognize declaration.
     */
    @Test
    public void testRecognizeDeclarationListOfRecognizeDeclaration()
    {
        TopLevelParser parser = new Parser(new Scanner("recognize spruce.collections.List;")).getTopLevelParser();
        ASTRecognizeDeclarationList node = parser.parseRecognizeDeclarationList();
        checkSimple(node, ASTRecognizeDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests recognize declaration list of multiple recognize declarations.
     */
    @Test
    public void testRecognizeDeclarationListOfMultipleRecognizeDeclarations()
    {
        TopLevelParser parser = new Parser(new Scanner("recognize spurce.collections.{List, ArrayList};\nrecognize spruce.reflection.*;\nrecognize shared spruce.test.Assertions.*;")).getTopLevelParser();
        ASTRecognizeDeclarationList node = parser.parseRecognizeDeclarationList();
        checkList(node, null, ASTRecognizeDeclaration.class, 3);
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
     * Tests recognize declaration of recognize shared all declaration.
     */
    @Test
    public void testRecognizeDeclarationOfRSAD()
    {
        TopLevelParser parser = new Parser(new Scanner("recognize shared spruce.test.Assertions.*;")).getTopLevelParser();
        ASTRecognizeDeclaration node = parser.parseRecognizeDeclaration();
        checkSimple(node, ASTRecognizeSharedAllDeclaration.class);

        ASTRecognizeSharedAllDeclaration rad = (ASTRecognizeSharedAllDeclaration) node.getChildren().get(0);
        checkSimple(rad, ASTTypeName.class, RECOGNIZE);
        node.collapseThenPrint();
    }

    /**
     * Tests recognize declaration of recognize shared type declaration.
     */
    @Test
    public void testRecognizeDeclarationOfRSTD()
    {
        TopLevelParser parser = new Parser(new Scanner("recognize shared spruce.test.Assertions.assertEquals;")).getTopLevelParser();
        ASTRecognizeDeclaration node = parser.parseRecognizeDeclaration();
        checkSimple(node, ASTRecognizeSharedTypeDeclaration.class);

        ASTRecognizeSharedTypeDeclaration rtd = (ASTRecognizeSharedTypeDeclaration) node.getChildren().get(0);
        checkBinary(rtd, RECOGNIZE, ASTTypeName.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests recognize declaration of recognize shared multiple declaration.
     */
    @Test
    public void testRecognizeDeclarationOfRSMD()
    {
        TopLevelParser parser = new Parser(new Scanner("recognize shared spruce.test.Assertions.{assertEquals, assertTrue, assertFalse};")).getTopLevelParser();
        ASTRecognizeDeclaration node = parser.parseRecognizeDeclaration();
        checkSimple(node, ASTRecognizeSharedMultDeclaration.class);

        ASTRecognizeSharedMultDeclaration rmd = (ASTRecognizeSharedMultDeclaration) node.getChildren().get(0);
        checkBinary(rmd, RECOGNIZE, ASTTypeName.class, ASTIdentifierList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests recognize declaration of recognize all declaration.
     */
    @Test
    public void testRecognizeDeclarationOfRAD()
    {
        TopLevelParser parser = new Parser(new Scanner("recognize spruce.collections.*;")).getTopLevelParser();
        ASTRecognizeDeclaration node = parser.parseRecognizeDeclaration();
        checkSimple(node, ASTRecognizeAllDeclaration.class);

        ASTRecognizeAllDeclaration rad = (ASTRecognizeAllDeclaration) node.getChildren().get(0);
        checkSimple(rad, ASTNamespaceOrTypeName.class, RECOGNIZE);
        node.collapseThenPrint();
    }

    /**
     * Tests recognize declaration of recognize type declaration.
     */
    @Test
    public void testRecognizeDeclarationOfRTD()
    {
        TopLevelParser parser = new Parser(new Scanner("recognize spruce.collections.ArrayList;")).getTopLevelParser();
        ASTRecognizeDeclaration node = parser.parseRecognizeDeclaration();
        checkSimple(node, ASTRecognizeTypeDeclaration.class);

        ASTRecognizeTypeDeclaration rtd = (ASTRecognizeTypeDeclaration) node.getChildren().get(0);
        checkSimple(rtd, ASTTypeName.class, RECOGNIZE);
        node.collapseThenPrint();
    }

    /**
     * Tests recognize declaration of recognize multiple declaration.
     */
    @Test
    public void testRecognizeDeclarationOfRMD()
    {
        TopLevelParser parser = new Parser(new Scanner("recognize spruce.collections.{List, ArrayList, LinkedList};")).getTopLevelParser();
        ASTRecognizeDeclaration node = parser.parseRecognizeDeclaration();
        checkSimple(node, ASTRecognizeMultDeclaration.class);

        ASTRecognizeMultDeclaration rmd = (ASTRecognizeMultDeclaration) node.getChildren().get(0);
        checkBinary(rmd, RECOGNIZE, ASTNamespaceOrTypeName.class, ASTIdentifierList.class);
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
}
