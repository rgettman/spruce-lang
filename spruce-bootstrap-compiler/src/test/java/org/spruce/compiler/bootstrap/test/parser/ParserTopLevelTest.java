package org.spruce.compiler.bootstrap.test.parser;

import org.spruce.compiler.bootstrap.ast.classes.*;
import org.spruce.compiler.bootstrap.ast.names.ASTIdentifier;
import org.spruce.compiler.bootstrap.ast.toplevel.*;
import org.spruce.compiler.bootstrap.common.BaseMessageProducer;
import org.spruce.compiler.bootstrap.parser.Parser;
import org.spruce.compiler.bootstrap.parser.TopLevelParser;
import org.spruce.compiler.bootstrap.scanner.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.spruce.compiler.bootstrap.ast.ASTListNode.Type.*;
import static org.spruce.compiler.bootstrap.test.parser.ParserTestUtility.*;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.bootstrap.test.util.TestUtility;

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
        ensureNoErrors(node, parser);
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
                class Baz extends Bar {}
                class Light {
                    constant Light RED = new Light();
                    constant Light YELLOW = new Light();
                    constant Light GREEN = new Light();
                }
                """);
        ASTOrdinaryCompilationUnit node = parser.parseOrdinaryCompilationUnit();
        ensureNoErrors(node, parser);
        assertTrue(node.getNamespaceDecl().isPresent());
        checkList(node.getUseDeclList(), USE_DECLARATIONS, ASTUseDeclaration.class, 1);
        checkList(node.getTypeDeclList(), TYPE_DECLARATIONS, ASTTypeDeclaration.class, 2);
    }

    /**
     * Tests use declaration list of use declaration.
     */
    @Test
    public void testUseDeclarationListOfUseDeclaration() {
        TopLevelParser parser = getTopLevelParser("use spruce.collections.List;");
        ASTUseDeclarationList node = parser.parseUseDeclarationList();
        ensureNoErrors(node, parser);
        checkList(node, USE_DECLARATIONS, ASTUseDeclaration.class, 1);
    }

    /**
     * Tests use declaration list of multiple use declarations.
     */
    @Test
    public void testUseDeclarationListOfMultipleUseDeclarations() {
        TopLevelParser parser = getTopLevelParser("""
                use spruce.collections.{List, ArrayList};
                use spruce.reflection.+;
                use spruce.test.Assertions.+;
                """);
        ASTUseDeclarationList node = parser.parseUseDeclarationList();
        ensureNoErrors(node, parser);
        checkList(node, USE_DECLARATIONS, ASTUseDeclaration.class, 3);
    }

    /**
     * Tests namespace declaration.
     */
    @Test
    public void testNamespaceDeclaration() {
        TopLevelParser parser = getTopLevelParser("namespace spruce.test.parser;");
        ASTNamespaceDeclaration node = parser.parseNamespaceDeclaration();
        ensureNoErrors(node, parser);
        checkList(node.getNamespace(), NAMESPACE_IDS, ASTIdentifier.class, 3);
    }

    /**
     * Tests bad namespace declaration of no semicolon.
     */
    @Test
    public void testNamespaceDeclarationNoSemicolon() {
        TopLevelParser parser = getTopLevelParser("namespace spruce.test.parser use");
        ASTNamespaceDeclaration node = parser.parseNamespaceDeclaration();
        expectError(node, parser);
    }

    /**
     * Tests use declaration of use all declaration.
     */
    @Test
    public void testUseDeclarationOfUAD() {
        TopLevelParser parser = getTopLevelParser("use spruce.collections.+;");
        ASTUseDeclaration node = parser.parseUseDeclaration();
        ensureNoErrors(node, parser);

        ASTUseAllDeclaration uad = TestUtility.ensureIsa(node, ASTUseAllDeclaration.class);
        assertNotNull(uad.getNamespaceOrTypeName());
    }

    /**
     * Tests bad use declaration of bad use all declaration of no semicolon.
     */
    @Test
    public void testUseDeclarationOfUADNoSemicolon() {
        TopLevelParser parser = getTopLevelParser("use spruce.collections.+");
        ASTUseDeclaration node = parser.parseUseDeclaration();
        expectError(node, parser);

    }

    /**
     * Tests use declaration of use type declaration.
     */
    @Test
    public void testUseDeclarationOfUTD() {
        TopLevelParser parser = getTopLevelParser("use spruce.collections.ArrayList;");
        ASTUseDeclaration node = parser.parseUseDeclaration();
        ensureNoErrors(node, parser);

        ASTUseTypeDeclaration utd = TestUtility.ensureIsa(node, ASTUseTypeDeclaration.class);
        checkList(utd.getTypename(), TYPENAME_IDS, ASTIdentifier.class, 3);
    }

    /**
     * Tests bad use type declaration that is empty.
     */
    @Test
    public void testUseDeclarationEmpty() {
        TopLevelParser parser = getTopLevelParser("use ;");
        ASTUseDeclaration node = parser.parseUseDeclaration();
        expectError(node, parser);
    }

    /**
     * Tests bad use declaration of bad use type declaration of no semicolon.
     */
    @Test
    public void testUseDeclarationOfUTDNoSemicolon() {
        TopLevelParser parser = getTopLevelParser("use spruce.collections.ArrayList use");
        ASTUseDeclaration node = parser.parseUseDeclaration();
        expectError(node, parser);
    }

    /**
     * Tests use declaration of use multiple declaration.
     */
    @Test
    public void testUseDeclarationOfUMD() {
        TopLevelParser parser = getTopLevelParser("use spruce.collections.{List, ArrayList, LinkedList};");
        ASTUseDeclaration node = parser.parseUseDeclaration();
        ensureNoErrors(node, parser);

        ASTUseMultDeclaration umd = TestUtility.ensureIsa(node, ASTUseMultDeclaration.class);
        checkList(umd.getIdentifierList(), IDENTIFIERS, ASTIdentifier.class, 3);
        checkList(umd.getNamespaceOrTypeName(), NAMESPACE_OR_TYPENAME_IDS, ASTIdentifier.class, 2);
    }

    /**
     * Tests bad use declaration of bad use mult declaration of no close brace.
     */
    @Test
    public void testUseDeclarationOfUMDNoCloseBrace() {
        TopLevelParser parser = getTopLevelParser("use shared spruce.collections.{List, ArrayList, LinkedList;");
        ASTUseDeclaration node = parser.parseUseDeclaration();
        expectError(node, parser);
    }

    /**
     * Tests type declaration list of type declaration.
     */
    @Test
    public void testTypeDeclarationListOfTypeDeclaration() {
        TopLevelParser parser = getTopLevelParser("class Foo {}");
        ASTTypeDeclarationList node = parser.parseTypeDeclarationList();
        ensureNoErrors(node, parser);
        checkList(node, TYPE_DECLARATIONS, ASTTypeDeclaration.class, 1);
    }

    /**
     * Tests type declaration list of multiple type declarations.
     */
    @Test
    public void testTypeDeclarationListOfMultipleTypeDeclarations() {
        TopLevelParser parser = getTopLevelParser("""
                class Foo {}
                class Bar {
                   constant Bar CHOCOLATE = new Bar();
                   constant Bar EXAM = new Bar();
                   constant Bar SAND = new Bar();
                }
                class Baz {}
                """);
        ASTTypeDeclarationList node = parser.parseTypeDeclarationList();
        ensureNoErrors(node, parser);
        checkList(node, TYPE_DECLARATIONS, ASTTypeDeclaration.class, 3);
    }

    /**
     * Tests type declaration of class declaration.
     */
    @Test
    public void testTypeDeclarationOfClassDeclaration() {
        TopLevelParser parser = getTopLevelParser("class Dummy { void test(){ } }");
        ASTTypeDeclaration node = parser.parseTypeDeclaration();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTClassDeclaration.class, node);
    }

    /**
     * Helper method to get a <code>TopLevelParser</code> directly from code.
     * @param code The code to test.
     * @return A <code>TopLevelParser</code> that will parse the given code.
     */
    public static TopLevelParser getTopLevelParser(String code) {
        return new Parser(new Scanner(code), new BaseMessageProducer()).getTopLevelParser();
    }
}
