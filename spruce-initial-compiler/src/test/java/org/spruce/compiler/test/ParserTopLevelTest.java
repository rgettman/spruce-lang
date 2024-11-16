package org.spruce.compiler.test;

import org.spruce.compiler.ast.classes.ASTAdtDeclaration;
import org.spruce.compiler.ast.classes.ASTAnnotation;
import org.spruce.compiler.ast.classes.ASTAnnotationDeclaration;
import org.spruce.compiler.ast.classes.ASTAnnotationList;
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
public class ParserTopLevelTest {
    /**
     * Tests empty ordinary compilation unit.
     */
    @Test
    public void testOrdinaryCompilationUnitEmpty() {
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
    public void testOrdinaryCompilationUnitFull() {
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
     * Tests full ordinary compilation unit with annotations.
     */
    @Test
    public void testOrdinaryCompilationUnitFullWithAnnotations() {
        TopLevelParser parser = getTopLevelParser("""
                @Doc namespace foo;
                use project.Bar;
                @Test1 public class Baz<T> extends Bar<T> {}
                @Test2 enum Light {RED, YELLOW, GREEN}
                """);
        ASTOrdinaryCompilationUnit node = parser.parseOrdinaryCompilationUnit();
        System.out.println(node);
        assertTrue(node.getNamespaceDecl().isPresent());
        checkList(node.getUseDeclList(), USE_DECLARATIONS, ASTUseDeclaration.class, 1);
        checkList(node.getTypeDeclList(), TYPE_DECLARATIONS, ASTTypeDeclaration.class, 2);
    }

    /**
     * Tests full ordinary compilation unit of only type declarations with annotations.
     */
    @Test
    public void testOrdinaryCompilationUnitTypeDeclarationsWithAnnotations() {
        TopLevelParser parser = getTopLevelParser("""
                @Test1 public class Baz<T> extends Bar<T> {}
                @Test2 enum Light {RED, YELLOW, GREEN}
                """);
        ASTOrdinaryCompilationUnit node = parser.parseOrdinaryCompilationUnit();
        System.out.println(node);
        assertFalse(node.getNamespaceDecl().isPresent());
        checkList(node.getUseDeclList(), USE_DECLARATIONS, ASTUseDeclaration.class, 0);
        checkList(node.getTypeDeclList(), TYPE_DECLARATIONS, ASTTypeDeclaration.class, 2);
    }

    /**
     * Tests full ordinary compilation unit of use declarations then type declarations with annotations.
     */
    @Test
    public void testOrdinaryCompilationUnitUseDeclarationsTypeDeclarationsWithAnnotations() {
        TopLevelParser parser = getTopLevelParser("""
                use project.Bar;
                @Test1 public class Baz<T> extends Bar<T> {}
                @Test2 enum Light {RED, YELLOW, GREEN}
                """);
        ASTOrdinaryCompilationUnit node = parser.parseOrdinaryCompilationUnit();
        System.out.println(node);
        assertFalse(node.getNamespaceDecl().isPresent());
        checkList(node.getUseDeclList(), USE_DECLARATIONS, ASTUseDeclaration.class, 1);
        checkList(node.getTypeDeclList(), TYPE_DECLARATIONS, ASTTypeDeclaration.class, 2);
    }

    /**
     * Tests bad ordinary compilation of bad use declarations with annotations.
     */
    @Test
    public void testOrdinaryCompilationUnitBadUseDeclListWithAnnotations() {
        TopLevelParser parser = getTopLevelParser("""
                namespace foo;
                @Bad use project.Bar;
                public class Baz<T> extends Bar<T> {}
                enum Light {RED, YELLOW, GREEN}
                """);
        assertThrows(CompileException.class, parser::parseOrdinaryCompilationUnit,
                "Annotations are not allowed on use declarations.");
    }

    /**
     * Tests use declaration list of use declaration.
     */
    @Test
    public void testUseDeclarationListOfUseDeclaration() {
        TopLevelParser parser = getTopLevelParser("use spruce.collections.List;");
        ASTUseDeclarationList node = parser.parseUseDeclarationList();
        System.out.println(node);
        checkList(node, USE_DECLARATIONS, ASTUseDeclaration.class, 1);
    }

    /**
     * Tests use declaration list of multiple use declarations.
     */
    @Test
    public void testUseDeclarationListOfMultipleUseDeclarations() {
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
    public void testNamespaceDeclaration() {
        TopLevelParser parser = getTopLevelParser("namespace spruce.test.parser;");
        ASTAnnotationList annList = parser.getClassesParser().parseAnnotationList();
        ASTNamespaceDeclaration node = parser.parseNamespaceDeclaration(annList);
        System.out.println(node);
        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 0);
        checkList(node.getNamespace(), NAMESPACE_IDS, ASTIdentifier.class, 3);
    }

    /**
     * Tests namespace declaration with annotation.
     */
    @Test
    public void testNamespaceDeclarationAnnotation() {
        TopLevelParser parser = getTopLevelParser("@Doc namespace spruce.test.parser;");
        ASTAnnotationList annList = parser.getClassesParser().parseAnnotationList();
        ASTNamespaceDeclaration node = parser.parseNamespaceDeclaration(annList);
        System.out.println(node);
        checkList(node.getAnnList(), ANNOTATIONS, ASTAnnotation.class, 1);
        checkList(node.getNamespace(), NAMESPACE_IDS, ASTIdentifier.class, 3);
    }

    /**
     * Tests bad namespace declaration of no semicolon.
     */
    @Test
    public void testNamespaceDeclarationNoSemicolon() {
        TopLevelParser parser = getTopLevelParser("namespace spruce.test.parser use");
        ASTAnnotationList annList = parser.getClassesParser().parseAnnotationList();
        assertThrows(CompileException.class, () -> parser.parseNamespaceDeclaration(annList), "Missing semicolon.");
    }

    /**
     * Tests use declaration of use shared all declaration.
     */
    @Test
    public void testUseDeclarationOfUSAD() {
        TopLevelParser parser = getTopLevelParser("use shared spruce.test.Assertions.*;");
        ASTUseDeclaration node = parser.parseUseDeclaration();
        System.out.println(node);

        ASTUseSharedAllDeclaration usad = ensureIsa(node, ASTUseSharedAllDeclaration.class);
        checkList(usad.getTypename(), TYPENAME_IDS, ASTIdentifier.class, 3);
    }

    /**
     * Tests bad use declaration of bad use shared all declaration of no semicolon.
     */
    @Test
    public void testUseDeclarationOfUSADNoSemicolon() {
        TopLevelParser parser = getTopLevelParser("use shared spruce.test.Assertions.*");
        assertThrows(CompileException.class, parser::parseUseDeclaration, "Expected semicolon.");
    }

    /**
     * Tests use declaration of use shared type declaration.
     */
    @Test
    public void testUseDeclarationOfUSTD() {
        TopLevelParser parser = getTopLevelParser("use shared spruce.test.Assertions.assertEquals;");
        ASTUseDeclaration node = parser.parseUseDeclaration();
        System.out.println(node);

        ASTUseSharedTypeDeclaration ustd = ensureIsa(node, ASTUseSharedTypeDeclaration.class);
        ASTIdentifier identifier = ustd.getIdentifier();
        assertEquals("assertEquals", identifier.getValue());
        checkList(ustd.getTypeName(), TYPENAME_IDS, ASTIdentifier.class, 3);
    }

    /**
     * Tests bad use declaration of bad use shared type declaration of no semicolon.
     */
    @Test
    public void testUseDeclarationOfUSTDNoSemicolon() {
        TopLevelParser parser = getTopLevelParser("use shared spruce.test.Assertions.assertEquals use");
        assertThrows(CompileException.class, parser::parseUseDeclaration, "Expected semicolon.");
    }

    /**
     * Tests use declaration of use shared multiple declaration.
     */
    @Test
    public void testUseDeclarationOfUSMD() {
        TopLevelParser parser = getTopLevelParser("use shared spruce.test.Assertions.{assertEquals, assertTrue, assertFalse};");
        ASTUseDeclaration node = parser.parseUseDeclaration();
        System.out.println(node);

        ASTUseSharedMultDeclaration usmd = ensureIsa(node, ASTUseSharedMultDeclaration.class);
        checkList(usmd.getIdentifierList(), IDENTIFIERS, ASTIdentifier.class, 3);
        checkList(usmd.getTypeName(), TYPENAME_IDS, ASTIdentifier.class, 3);
    }

    /**
     * Tests bad use declaration of bad use shared mult declaration of no semicolon.
     */
    @Test
    public void testUseDeclarationOfUSMDNoSemicolon() {
        TopLevelParser parser = getTopLevelParser("use shared spruce.test.Assertions.{assertEquals, assertTrue, assertFalse} use");
        assertThrows(CompileException.class, parser::parseUseDeclaration, "Expected semicolon.");
    }

    /**
     * Tests bad use declaration of bad use shared mult declaration of no close brace.
     */
    @Test
    public void testUseDeclarationOfUSMDNoCloseBrace() {
        TopLevelParser parser = getTopLevelParser("use shared spruce.test.Assertions.{assertEquals, assertTrue, assertFalse;");
        assertThrows(CompileException.class, parser::parseUseDeclaration, "Expected '}'");
    }

    /**
     * Tests use declaration of use all declaration.
     */
    @Test
    public void testUseDeclarationOfUAD() {
        TopLevelParser parser = getTopLevelParser("use spruce.collections.*;");
        ASTUseDeclaration node = parser.parseUseDeclaration();
        System.out.println(node);

        ASTUseAllDeclaration uad = ensureIsa(node, ASTUseAllDeclaration.class);
        assertNotNull(uad.getNamespaceOrTypeName());
    }

    /**
     * Tests bad use declaration of bad use all declaration of no semicolon.
     */
    @Test
    public void testUseDeclarationOfUADNoSemicolon() {
        TopLevelParser parser = getTopLevelParser("use spruce.collections.*");
        assertThrows(CompileException.class, parser::parseUseDeclaration, "Expected semicolon.");
    }

    /**
     * Tests use declaration of use type declaration.
     */
    @Test
    public void testUseDeclarationOfUTD() {
        TopLevelParser parser = getTopLevelParser("use spruce.collections.ArrayList;");
        ASTUseDeclaration node = parser.parseUseDeclaration();
        System.out.println(node);

        ASTUseTypeDeclaration utd = ensureIsa(node, ASTUseTypeDeclaration.class);
        checkList(utd.getTypename(), TYPENAME_IDS, ASTIdentifier.class, 3);
    }

    /**
     * Tests bad use declaration of bad use type declaration of no semicolon.
     */
    @Test
    public void testUseDeclarationOfUTDNoSemicolon() {
        TopLevelParser parser = getTopLevelParser("use spruce.collections.ArrayList use");
        assertThrows(CompileException.class, parser::parseUseDeclaration, "Expected semicolon.");
    }

    /**
     * Tests use declaration of use multiple declaration.
     */
    @Test
    public void testUseDeclarationOfUMD() {
        TopLevelParser parser = getTopLevelParser("use spruce.collections.{List, ArrayList, LinkedList};");
        ASTUseDeclaration node = parser.parseUseDeclaration();
        System.out.println(node);

        ASTUseMultDeclaration umd = ensureIsa(node, ASTUseMultDeclaration.class);
        checkList(umd.getIdentifierList(), IDENTIFIERS, ASTIdentifier.class, 3);
        checkList(umd.getNamespaceOrTypeName(), NAMESPACE_OR_TYPENAME_IDS, ASTIdentifier.class, 2);
    }

    /**
     * Tests bad use declaration of bad use mult declaration of no semicolon.
     */
    @Test
    public void testUseDeclarationOfUMDNoSemicolon() {
        TopLevelParser parser = getTopLevelParser("use shared spruce.collections.{List, ArrayList, LinkedList} use");
        assertThrows(CompileException.class, parser::parseUseDeclaration, "Expected semicolon.");
    }

    /**
     * Tests bad use declaration of bad use mult declaration of no close brace.
     */
    @Test
    public void testUseDeclarationOfUMDNoCloseBrace() {
        TopLevelParser parser = getTopLevelParser("use shared spruce.collections.{List, ArrayList, LinkedList;");
        assertThrows(CompileException.class, parser::parseUseDeclaration, "Expected '}'");
    }

    /**
     * Tests type declaration list of type declaration.
     */
    @Test
    public void testTypeDeclarationListOfTypeDeclaration() {
        TopLevelParser parser = getTopLevelParser("class Foo {}");
        ASTAnnotationList annList = parser.getClassesParser().parseAnnotationList();
        ASTTypeDeclarationList node = parser.parseTypeDeclarationList(annList);
        System.out.println(node);
        checkList(node, TYPE_DECLARATIONS, ASTTypeDeclaration.class, 1);
    }

    /**
     * Tests type declaration list of multiple type declarations.
     */
    @Test
    public void testTypeDeclarationListOfMultipleTypeDeclarations() {
        TopLevelParser parser = getTopLevelParser("""
                class Foo {}
                @Test enum Bar {CHOCOLATE, EXAM, SAND}
                interface Baz {}
                """);
        ASTAnnotationList annList = parser.getClassesParser().parseAnnotationList();
        ASTTypeDeclarationList node = parser.parseTypeDeclarationList(annList);
        System.out.println(node);
        checkList(node, TYPE_DECLARATIONS, ASTTypeDeclaration.class, 3);
    }

    /**
     * Tests type declaration list of multiple type declarations and annotations.
     */
    @Test
    public void testTypeDeclarationListOfMultipleTypeDeclarationsAndAnnotations() {
        TopLevelParser parser = getTopLevelParser("""
                @Test1 class Foo {}
                @Test2 enum Bar {CHOCOLATE, EXAM, SAND}
                @Test3 interface Baz {}
                """);
        ASTAnnotationList annList = parser.getClassesParser().parseAnnotationList();
        ASTTypeDeclarationList node = parser.parseTypeDeclarationList(annList);
        System.out.println(node);
        checkList(node, TYPE_DECLARATIONS, ASTTypeDeclaration.class, 3);
    }

    /**
     * Tests type declaration of class declaration.
     */
    @Test
    public void testTypeDeclarationOfClassDeclaration() {
        TopLevelParser parser = getTopLevelParser("public abstract class Dummy<T> { abstract void test(); }");
        ASTAnnotationList annList = parser.getClassesParser().parseAnnotationList();
        ASTTypeDeclaration node = parser.parseTypeDeclaration(annList);
        System.out.println(node);
        assertInstanceOf(ASTClassDeclaration.class, node);
    }

    /**
     * Tests type declaration of enum declaration.
     */
    @Test
    public void testTypeDeclarationOfEnumDeclaration() {
        TopLevelParser parser = getTopLevelParser("public shared enum TrafficLight {RED, YELLOW, GREEN}");
        ASTAnnotationList annList = parser.getClassesParser().parseAnnotationList();
        ASTTypeDeclaration node = parser.parseTypeDeclaration(annList);
        System.out.println(node);
        assertInstanceOf(ASTEnumDeclaration.class, node);
    }

    /**
     * Tests type declaration of interface declaration.
     */
    @Test
    public void testTypeDeclarationOfInterfaceDeclaration() {
        TopLevelParser parser = getTopLevelParser("protected shared interface Dummy { public void run();}");
        ASTAnnotationList annList = parser.getClassesParser().parseAnnotationList();
        ASTTypeDeclaration node = parser.parseTypeDeclaration(annList);
        System.out.println(node);
        assertInstanceOf(ASTInterfaceDeclaration.class, node);
    }

    /**
     * Tests type declaration of annotation declaration.
     */
    @Test
    public void testTypeDeclarationOfAnnotationDeclaration() {
        TopLevelParser parser = getTopLevelParser("public shared annotation Spruce { String language();}");
        ASTAnnotationList annList = parser.getClassesParser().parseAnnotationList();
        ASTTypeDeclaration node = parser.parseTypeDeclaration(annList);
        System.out.println(node);
        assertInstanceOf(ASTAnnotationDeclaration.class, node);
    }

    /**
     * Tests type declaration of record declaration.
     */
    @Test
    public void testTypeDeclarationOfRecordDeclarationBadModifier() {
        TopLevelParser parser = getTopLevelParser("internal shared record Redacted(String byWhom) { }");
        ASTAnnotationList annList = parser.getClassesParser().parseAnnotationList();
        assertThrows(CompileException.class, () -> parser.parseTypeDeclaration(annList),
                "General modifier not allowed here.");
    }

    /**
     * Tests type declaration of record declaration.
     */
    @Test
    public void testTypeDeclarationOfRecordDeclaration() {
        TopLevelParser parser = getTopLevelParser("internal record Redacted(String byWhom) { }");
        ASTAnnotationList annList = parser.getClassesParser().parseAnnotationList();
        ASTTypeDeclaration node = parser.parseTypeDeclaration(annList);
        System.out.println(node);
        assertInstanceOf(ASTRecordDeclaration.class, node);
    }

    /**
     * Tests type declaration of adt declaration.
     */
    @Test
    public void testTypeDeclarationOfAdtDeclaration() {
        TopLevelParser parser = getTopLevelParser("@Preview adt Test { Test1, Test2 }");
        ASTAnnotationList annList = parser.getClassesParser().parseAnnotationList();
        ASTTypeDeclaration node = parser.parseTypeDeclaration(annList);
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
