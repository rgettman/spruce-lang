package org.spruce.compiler.test;

import org.spruce.compiler.ast.ASTListNode;
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

import static org.spruce.compiler.ast.ASTListNode.Type.*;
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
        TopLevelParser parser = getTopLevelParser("");
        ASTOrdinaryCompilationUnit node = parser.parseOrdinaryCompilationUnit();
        node.print();
        checkEmpty(node, null);
    }

    /**
     * Tests full ordinary compilation unit.
     */
    @Test
    public void testOrdinaryCompilationUnitFull()
    {
        TopLevelParser parser = getTopLevelParser("namespace foo;\nuse project.Bar;\npublic class Baz<T> extends Bar<T> {}\nenum Light {RED, YELLOW, GREEN}");
        ASTOrdinaryCompilationUnit node = parser.parseOrdinaryCompilationUnit();
        node.print();
        checkTrinary(node, null, ASTNamespaceDeclaration.class, ASTListNode.class, ASTListNode.class);
    }

    /**
     * Tests use declaration list of use declaration.
     */
    @Test
    public void testUseDeclarationListOfUseDeclaration()
    {
        TopLevelParser parser = getTopLevelParser("use spruce.collections.List;");
        ASTListNode node = parser.parseUseDeclarationList();
        node.print();
        checkList(node, USE_DECLARATIONS, ASTUseDeclaration.class, 1);
    }

    /**
     * Tests use declaration list of multiple use declarations.
     */
    @Test
    public void testUseDeclarationListOfMultipleUseDeclarations()
    {
        TopLevelParser parser = getTopLevelParser("""
                use spruce.collections.{List, ArrayList};
                use spruce.reflection.*;
                use shared spruce.test.Assertions.*;
                """);
        ASTListNode node = parser.parseUseDeclarationList();
        node.print();
        checkList(node, USE_DECLARATIONS, ASTUseDeclaration.class, 3);
    }

    /**
     * Tests namespace declaration.
     */
    @Test
    public void testNamespaceDeclaration()
    {
        TopLevelParser parser = getTopLevelParser("namespace spruce.test.parser;");
        ASTNamespaceDeclaration node = parser.parseNamespaceDeclaration();
        node.print();
        checkSimple(node, ASTListNode.class, NAMESPACE);
    }

    /**
     * Tests use declaration of use shared all declaration.
     */
    @Test
    public void testUseDeclarationOfUSAD()
    {
        TopLevelParser parser = getTopLevelParser("use shared spruce.test.Assertions.*;");
        ASTUseDeclaration node = parser.parseUseDeclaration();
        node.print();

        checkSimple(node, ASTUseSharedAllDeclaration.class);
        ASTUseSharedAllDeclaration rad = (ASTUseSharedAllDeclaration) node.getChildren().get(0);
        checkSimple(rad, ASTListNode.class, USE);
    }

    /**
     * Tests use declaration of use shared type declaration.
     */
    @Test
    public void testUseDeclarationOfUSTD()
    {
        TopLevelParser parser = getTopLevelParser("use shared spruce.test.Assertions.assertEquals;");
        ASTUseDeclaration node = parser.parseUseDeclaration();
        node.print();

        checkSimple(node, ASTUseSharedTypeDeclaration.class);
        ASTUseSharedTypeDeclaration rtd = (ASTUseSharedTypeDeclaration) node.getChildren().get(0);
        checkBinary(rtd, USE, ASTListNode.class, ASTIdentifier.class);
    }

    /**
     * Tests use declaration of use shared multiple declaration.
     */
    @Test
    public void testUseDeclarationOfUSMD()
    {
        TopLevelParser parser = getTopLevelParser("use shared spruce.test.Assertions.{assertEquals, assertTrue, assertFalse};");
        ASTUseDeclaration node = parser.parseUseDeclaration();
        node.print();

        checkSimple(node, ASTUseSharedMultDeclaration.class);
        ASTUseSharedMultDeclaration rmd = (ASTUseSharedMultDeclaration) node.getChildren().get(0);
        checkBinary(rmd, USE, ASTListNode.class, ASTListNode.class);
    }

    /**
     * Tests use declaration of use all declaration.
     */
    @Test
    public void testUseDeclarationOfUAD()
    {
        TopLevelParser parser = getTopLevelParser("use spruce.collections.*;");
        ASTUseDeclaration node = parser.parseUseDeclaration();
        node.print();

        checkSimple(node, ASTUseAllDeclaration.class);
        ASTUseAllDeclaration rad = (ASTUseAllDeclaration) node.getChildren().get(0);
        checkSimple(rad, ASTListNode.class, USE);
    }

    /**
     * Tests use declaration of use type declaration.
     */
    @Test
    public void testUseDeclarationOfUTD()
    {
        TopLevelParser parser = getTopLevelParser("use spruce.collections.ArrayList;");
        ASTUseDeclaration node = parser.parseUseDeclaration();
        node.print();

        checkSimple(node, ASTUseTypeDeclaration.class);
        ASTUseTypeDeclaration rtd = (ASTUseTypeDeclaration) node.getChildren().get(0);
        checkSimple(rtd, ASTListNode.class, USE);
    }

    /**
     * Tests use declaration of use multiple declaration.
     */
    @Test
    public void testUseDeclarationOfUMD()
    {
        TopLevelParser parser = getTopLevelParser("use spruce.collections.{List, ArrayList, LinkedList};");
        ASTUseDeclaration node = parser.parseUseDeclaration();
        node.print();

        checkSimple(node, ASTUseMultDeclaration.class);
        ASTUseMultDeclaration rmd = (ASTUseMultDeclaration) node.getChildren().get(0);
        checkBinary(rmd, USE, ASTListNode.class, ASTListNode.class);
    }

    /**
     * Tests type declaration list of type declaration.
     */
    @Test
    public void testTypeDeclarationListOfTypeDeclaration()
    {
        TopLevelParser parser = getTopLevelParser("class Foo {}");
        ASTListNode node = parser.parseTypeDeclarationList();
        node.print();
        checkList(node, TYPE_DECLARATIONS, ASTTypeDeclaration.class, 1);
    }

    /**
     * Tests type declaration list of multiple type declarations.
     */
    @Test
    public void testTypeDeclarationListOfMultipleTypeDeclarations()
    {
        TopLevelParser parser = getTopLevelParser("""
                class Foo {}
                enum Bar {CHOCOLATE, EXAM, SAND}
                interface Baz {}
                """);
        ASTListNode node = parser.parseTypeDeclarationList();
        node.print();
        checkList(node, TYPE_DECLARATIONS, ASTTypeDeclaration.class, 3);
    }

    /**
     * Tests type declaration of class declaration.
     */
    @Test
    public void testTypeDeclarationOfClassDeclaration()
    {
        TopLevelParser parser = getTopLevelParser("public abstract class Dummy<T> { abstract void test(); }");
        ASTTypeDeclaration node = parser.parseTypeDeclaration();
        node.print();
        checkSimple(node, ASTClassDeclaration.class);
    }

    /**
     * Tests type declaration of enum declaration.
     */
    @Test
    public void testTypeDeclarationOfEnumDeclaration()
    {
        TopLevelParser parser = getTopLevelParser("public shared enum TrafficLight {RED, YELLOW, GREEN}");
        ASTTypeDeclaration node = parser.parseTypeDeclaration();
        node.print();
        checkSimple(node, ASTEnumDeclaration.class);
    }

    /**
     * Tests type declaration of interface declaration.
     */
    @Test
    public void testTypeDeclarationOfInterfaceDeclaration()
    {
        TopLevelParser parser = getTopLevelParser("protected shared interface Dummy { public void run();}");
        ASTTypeDeclaration node = parser.parseTypeDeclaration();
        node.print();
        checkSimple(node, ASTInterfaceDeclaration.class);
    }

    /**
     * Tests type declaration of annotation declaration.
     */
    @Test
    public void testTypeDeclarationOfAnnotationDeclaration()
    {
        TopLevelParser parser = getTopLevelParser("public shared annotation Spruce { String language();}");
        ASTTypeDeclaration node = parser.parseTypeDeclaration();
        node.print();
        checkSimple(node, ASTAnnotationDeclaration.class);
    }

    /**
     * Tests type declaration of record declaration.
     */
    @Test
    public void testTypeDeclarationOfRecordDeclaration()
    {
        TopLevelParser parser = getTopLevelParser("internal record Redacted(String byWhom) { }");
        ASTTypeDeclaration node = parser.parseTypeDeclaration();
        node.print();
        checkSimple(node, ASTRecordDeclaration.class);
    }

    /**
     * Helper method to get a <code>TopLevelParser</code> directly from code.
     * @param code The code to test.
     * @return A <code>TopLevelParser</code> that will parse the given code.
     */
    private static TopLevelParser getTopLevelParser(String code) {
        return new Parser(new Scanner(code)).getTopLevelParser();
    }
}
