package org.spruce.compiler.bootstrap.test.resolution;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.bootstrap.ast.classes.ASTClassDeclaration;
import org.spruce.compiler.bootstrap.ast.classes.ASTMethodDeclaration;
import org.spruce.compiler.bootstrap.ast.statements.*;
import org.spruce.compiler.bootstrap.ast.types.ASTDataType;
import org.spruce.compiler.bootstrap.symbol.ParentSymbol;

import static org.junit.jupiter.api.Assertions.*;
import static org.spruce.compiler.bootstrap.test.resolution.ResolverTestUtility.*;
import static org.spruce.compiler.bootstrap.test.util.TestUtility.ensureIsa;

/**
 * All tests for the statements Resolver.
 */
public class ResolverStatementsTest {
    /**
     * Tests data type resolution on a local variable declaration.
     */
    @Test
    public void testLocalVariableDeclarationTypeResolution() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                """,
                """
                use spruce.lang.Integer;
                class Test {
                    void testMethod() {
                        Integer testInt = 3;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        ParentSymbol integer = ensureIsa(lang.getTable().get("Integer"), ParentSymbol.class);

        ASTClassDeclaration test = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethod = ensureIsa(test.getClassParts().get(0), ASTMethodDeclaration.class);
        assertTrue(testMethod.getBody().getBlock().isPresent());
        ASTBlock block = testMethod.getBody().getBlock().get();
        ASTLocalVariableDeclarationStatement testIntStmt =
                ensureIsa(block.getBlockStmts().get(0), ASTLocalVariableDeclarationStatement.class);
        assertTrue(testIntStmt.getLocalVarDecl().getLocalVarType().getDataType().isPresent());
        ASTDataType dtInteger = testIntStmt.getLocalVarDecl().getLocalVarType().getDataType().get();
        assertSame(integer, dtInteger.getResolvedDataType());
    }

    /**
     * Tests data type resolution on a bad local variable declaration.
     */
    @Test
    public void testLocalVariableDeclarationBadTypeResolution() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                """,
                """
                use spruce.lang.Integer;
                class Test {
                    void testMethod() {
                        DoesNotExist dne;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests data type resolution on a local variable declaration in an
     * enhanced for statement.
     */
    @Test
    public void testEnhancedForStmtLocalVarDeclTypeResolution() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                class List {}
                """,
                """
                use spruce.lang.{Integer, List};
                class Test {
                    void testMethod(List list) {
                        for (Integer i in list) {
                        }
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        ParentSymbol integer = ensureIsa(lang.getTable().get("Integer"), ParentSymbol.class);

        ASTClassDeclaration test = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethod = ensureIsa(test.getClassParts().get(0), ASTMethodDeclaration.class);
        assertTrue(testMethod.getBody().getBlock().isPresent());
        ASTBlock block = testMethod.getBody().getBlock().get();
        ASTEnhancedForStatement enhancedForStmt =
                ensureIsa(block.getBlockStmts().get(0), ASTEnhancedForStatement.class);
        assertTrue(enhancedForStmt.getLocalVarDecl().getLocalVarType().getDataType().isPresent());
        ASTDataType dtInteger = enhancedForStmt.getLocalVarDecl().getLocalVarType().getDataType().get();
        assertSame(integer, dtInteger.getResolvedDataType());
    }

    /**
     * Tests data type resolution on a local variable declaration in a
     * basic for statement.
     */
    @Test
    public void testBasicForStatementInitLocalVarDeclTypeResolution() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                class List {}
                """,
                """
                use spruce.lang.{Integer, List};
                class Test {
                    void testMethod(List list) {
                        for (Integer i = 0; i < list.size(); i = i + 1) {
                        }
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        ParentSymbol integer = ensureIsa(lang.getTable().get("Integer"), ParentSymbol.class);

        ASTClassDeclaration test = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethod = ensureIsa(test.getClassParts().get(0), ASTMethodDeclaration.class);
        assertTrue(testMethod.getBody().getBlock().isPresent());
        ASTBlock block = testMethod.getBody().getBlock().get();
        ASTBasicForStatement basicForStmt =
                ensureIsa(block.getBlockStmts().get(0), ASTBasicForStatement.class);
        assertTrue(basicForStmt.getInit().isPresent());
        ASTLocalVariableDeclaration localVarDecl = ensureIsa(
                basicForStmt.getInit().get(), ASTLocalVariableDeclaration.class);
        assertTrue(localVarDecl.getLocalVarType().getDataType().isPresent());
        ASTDataType dtInteger = localVarDecl.getLocalVarType().getDataType().get();
        assertSame(integer, dtInteger.getResolvedDataType());
    }

    /**
     * Tests data type resolution on a local variable declaration in if
     * statements.
     */
    @Test
    public void testIfStatementInitLocalVarDeclTypeResolution() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                class List {}
                """,
                """
                use spruce.lang.{Integer, List};
                class Test {
                    void testMethod(List list) {
                        if {Integer size = list.size()} (size > 10) {
                            stdout.println("Big");
                        }
                        else if {Integer first = list.get(0)} (first > 10) {
                            stdout.println("First is big");
                        }
                        else {
                            stdout.println("Else");
                        }
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        ParentSymbol integer = ensureIsa(lang.getTable().get("Integer"), ParentSymbol.class);

        ASTClassDeclaration test = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethod = ensureIsa(test.getClassParts().get(0), ASTMethodDeclaration.class);
        assertTrue(testMethod.getBody().getBlock().isPresent());
        ASTBlock block = testMethod.getBody().getBlock().get();

        ASTIfStatement ifStmt = ensureIsa(block.getBlockStmts().get(0), ASTIfStatement.class);
        assertTrue(ifStmt.getInit().isPresent());
        ASTLocalVariableDeclaration localVarDecl = ensureIsa(
                ifStmt.getInit().get(), ASTLocalVariableDeclaration.class);
        assertTrue(localVarDecl.getLocalVarType().getDataType().isPresent());
        ASTDataType dtInteger = localVarDecl.getLocalVarType().getDataType().get();
        assertSame(integer, dtInteger.getResolvedDataType());

        assertTrue(ifStmt.getElseIf().isPresent());
        ASTIfStatement elseIfStmt = ifStmt.getElseIf().get();
        assertTrue(elseIfStmt.getInit().isPresent());
        ASTLocalVariableDeclaration localVarDecl2 = ensureIsa(
                elseIfStmt.getInit().get(), ASTLocalVariableDeclaration.class);
        assertTrue(localVarDecl2.getLocalVarType().getDataType().isPresent());
        ASTDataType dtInteger2 = localVarDecl.getLocalVarType().getDataType().get();
        assertSame(integer, dtInteger2.getResolvedDataType());
    }

    /**
     * Tests data type resolution on a local variable declaration in a while
     * statement.
     */
    @Test
    public void testWhileStatementInitLocalVarDeclTypeResolution() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                class List {}
                """,
                """
                use spruce.lang.{Integer, List};
                class Test {
                    void testMethod(List list) {
                        while {Integer i = 0} (i < list.size()) {
                            i = i + 1;
                        }
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        ParentSymbol integer = ensureIsa(lang.getTable().get("Integer"), ParentSymbol.class);

        ASTClassDeclaration test = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethod = ensureIsa(test.getClassParts().get(0), ASTMethodDeclaration.class);
        assertTrue(testMethod.getBody().getBlock().isPresent());
        ASTBlock block = testMethod.getBody().getBlock().get();

        ASTWhileStatement whileStmt = ensureIsa(block.getBlockStmts().get(0), ASTWhileStatement.class);
        assertTrue(whileStmt.getInit().isPresent());
        ASTLocalVariableDeclaration localVarDecl = ensureIsa(
                whileStmt.getInit().get(), ASTLocalVariableDeclaration.class);
        assertTrue(localVarDecl.getLocalVarType().getDataType().isPresent());
        ASTDataType dtInteger = localVarDecl.getLocalVarType().getDataType().get();
        assertSame(integer, dtInteger.getResolvedDataType());
    }
}
