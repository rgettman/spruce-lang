package org.spruce.compiler.test;

import org.spruce.compiler.ast.classes.ASTAdtDeclaration;
import org.spruce.compiler.ast.classes.ASTAnnotationDeclaration;
import org.spruce.compiler.ast.classes.ASTClassDeclaration;
import org.spruce.compiler.ast.classes.ASTEnumDeclaration;
import org.spruce.compiler.ast.classes.ASTInterfaceDeclaration;
import org.spruce.compiler.ast.classes.ASTRecordDeclaration;
import org.spruce.compiler.ast.classes.ASTTypeDeclaration;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.toplevel.*;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.parser.Parser;
import org.spruce.compiler.parser.TopLevelParser;
import org.spruce.compiler.scanner.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.spruce.compiler.ast.ASTListNode.Type.*;
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
        System.out.println(node);
        assertFalse(node.getNamespaceDecl().isPresent());
        checkList(node.getUseDeclList(), USE_DECLARATIONS, ASTUseDeclaration.class, 0);
        checkList(node.getTypeDeclList(), TYPE_DECLARATIONS, ASTTypeDeclaration.class, 0);
    }

    /**
     * Tests full ordinary compilation unit.
     */
    @Test
    public void testOrdinaryCompilationUnitFull()
    {
        TopLevelParser parser = getTopLevelParser("""
                namespace foo;
                use project.Bar;
                public class Baz<T> extends Bar<T> {}
                enum Light {RED, YELLOW, GREEN}
                """);
        ASTOrdinaryCompilationUnit node = parser.parseOrdinaryCompilationUnit();
        System.out.println(node);
        assertTrue(node.getNamespaceDecl().isPresent());
        checkList(node.getUseDeclList(), USE_DECLARATIONS, ASTUseDeclaration.class, 1);
        checkList(node.getTypeDeclList(), TYPE_DECLARATIONS, ASTTypeDeclaration.class, 2);
    }

    /**
     * Tests use declaration list of use declaration.
     */
    @Test
    public void testUseDeclarationListOfUseDeclaration()
    {
        TopLevelParser parser = getTopLevelParser("use spruce.collections.List;");
        ASTUseDeclarationList node = parser.parseUseDeclarationList();
        System.out.println(node);
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
        ASTUseDeclarationList node = parser.parseUseDeclarationList();
        System.out.println(node);
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
        System.out.println(node);
        checkList(node.getNamespace(), NAMESPACE_IDS, ASTIdentifier.class, 3);
    }

    /**
     * Tests use declaration of use shared all declaration.
     */
    @Test
    public void testUseDeclarationOfUSAD()
    {
        TopLevelParser parser = getTopLevelParser("use shared spruce.test.Assertions.*;");
        ASTUseDeclaration node = parser.parseUseDeclaration();
        System.out.println(node);

        ASTUseSharedAllDeclaration usad = ensureIsa(node, ASTUseSharedAllDeclaration.class);
        checkList(usad.getTypename(), TYPENAME_IDS, ASTIdentifier.class, 3);
    }

    /**
     * Tests use declaration of use shared type declaration.
     */
    @Test
    public void testUseDeclarationOfUSTD()
    {
        TopLevelParser parser = getTopLevelParser("use shared spruce.test.Assertions.assertEquals;");
        ASTUseDeclaration node = parser.parseUseDeclaration();
        System.out.println(node);

        ASTUseSharedTypeDeclaration ustd = ensureIsa(node, ASTUseSharedTypeDeclaration.class);
        ASTIdentifier identifier = ustd.getIdentifier();
        assertEquals("assertEquals", identifier.getValue());
        checkList(ustd.getTypeName(), TYPENAME_IDS, ASTIdentifier.class, 3);
    }

    /**
     * Tests use declaration of use shared multiple declaration.
     */
    @Test
    public void testUseDeclarationOfUSMD()
    {
        TopLevelParser parser = getTopLevelParser("use shared spruce.test.Assertions.{assertEquals, assertTrue, assertFalse};");
        ASTUseDeclaration node = parser.parseUseDeclaration();
        System.out.println(node);

        ASTUseSharedMultDeclaration usmd = ensureIsa(node, ASTUseSharedMultDeclaration.class);
        checkList(usmd.getIdentifierList(), IDENTIFIERS, ASTIdentifier.class, 3);
        checkList(usmd.getTypeName(), TYPENAME_IDS, ASTIdentifier.class, 3);
    }

    /**
     * Tests use declaration of use all declaration.
     */
    @Test
    public void testUseDeclarationOfUAD()
    {
        TopLevelParser parser = getTopLevelParser("use spruce.collections.*;");
        ASTUseDeclaration node = parser.parseUseDeclaration();
        System.out.println(node);

        ASTUseAllDeclaration uad = ensureIsa(node, ASTUseAllDeclaration.class);
        assertNotNull(uad.getNamespaceOrTypeName());
    }

    /**
     * Tests use declaration of use type declaration.
     */
    @Test
    public void testUseDeclarationOfUTD()
    {
        TopLevelParser parser = getTopLevelParser("use spruce.collections.ArrayList;");
        ASTUseDeclaration node = parser.parseUseDeclaration();
        System.out.println(node);

        ASTUseTypeDeclaration utd = ensureIsa(node, ASTUseTypeDeclaration.class);
        checkList(utd.getTypename(), TYPENAME_IDS, ASTIdentifier.class, 3);
    }

    /**
     * Tests use declaration of use multiple declaration.
     */
    @Test
    public void testUseDeclarationOfUMD()
    {
        TopLevelParser parser = getTopLevelParser("use spruce.collections.{List, ArrayList, LinkedList};");
        ASTUseDeclaration node = parser.parseUseDeclaration();
        System.out.println(node);

        ASTUseMultDeclaration umd = ensureIsa(node, ASTUseMultDeclaration.class);
        checkList(umd.getIdentifierList(), IDENTIFIERS, ASTIdentifier.class, 3);
        checkList(umd.getNamespaceOrTypeName(), NAMESPACE_OR_TYPENAME_IDS, ASTIdentifier.class, 2);
    }

    /**
     * Tests type declaration list of type declaration.
     */
    @Test
    public void testTypeDeclarationListOfTypeDeclaration()
    {
        TopLevelParser parser = getTopLevelParser("class Foo {}");
        ASTTypeDeclarationList node = parser.parseTypeDeclarationList();
        System.out.println(node);
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
        ASTTypeDeclarationList node = parser.parseTypeDeclarationList();
        System.out.println(node);
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
        System.out.println(node);
        assertInstanceOf(ASTClassDeclaration.class, node);
    }

    /**
     * Tests type declaration of enum declaration.
     */
    @Test
    public void testTypeDeclarationOfEnumDeclaration()
    {
        TopLevelParser parser = getTopLevelParser("public shared enum TrafficLight {RED, YELLOW, GREEN}");
        ASTTypeDeclaration node = parser.parseTypeDeclaration();
        System.out.println(node);
        assertInstanceOf(ASTEnumDeclaration.class, node);
    }

    /**
     * Tests type declaration of interface declaration.
     */
    @Test
    public void testTypeDeclarationOfInterfaceDeclaration()
    {
        TopLevelParser parser = getTopLevelParser("protected shared interface Dummy { public void run();}");
        ASTTypeDeclaration node = parser.parseTypeDeclaration();
        System.out.println(node);
        assertInstanceOf(ASTInterfaceDeclaration.class, node);
    }

    /**
     * Tests type declaration of annotation declaration.
     */
    @Test
    public void testTypeDeclarationOfAnnotationDeclaration()
    {
        TopLevelParser parser = getTopLevelParser("public shared annotation Spruce { String language();}");
        ASTTypeDeclaration node = parser.parseTypeDeclaration();
        System.out.println(node);
        assertInstanceOf(ASTAnnotationDeclaration.class, node);
    }

    /**
     * Tests type declaration of record declaration.
     */
    @Test
    public void testTypeDeclarationOfRecordDeclarationBadModifier()
    {
        TopLevelParser parser = getTopLevelParser("internal shared record Redacted(String byWhom) { }");
        assertThrows(CompileException.class, parser::parseTypeDeclaration, "General modifier not allowed here.");
    }

    /**
     * Tests type declaration of record declaration.
     */
    @Test
    public void testTypeDeclarationOfRecordDeclaration()
    {
        TopLevelParser parser = getTopLevelParser("internal record Redacted(String byWhom) { }");
        ASTTypeDeclaration node = parser.parseTypeDeclaration();
        System.out.println(node);
        assertInstanceOf(ASTRecordDeclaration.class, node);
    }

    /**
     * Tests type declaration of adt declaration.
     */
    @Test
    public void testTypeDeclarationOfAdtDeclaration()
    {
        TopLevelParser parser = getTopLevelParser("adt Test { Test1, Test2 }");
        ASTTypeDeclaration node = parser.parseTypeDeclaration();
        System.out.println(node);
        assertInstanceOf(ASTAdtDeclaration.class, node);
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
