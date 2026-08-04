package org.spruce.compiler.bootstrap.test.resolution;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.bootstrap.ast.classes.ASTClassDeclaration;
import org.spruce.compiler.bootstrap.ast.classes.ASTMethodDeclaration;
import org.spruce.compiler.bootstrap.ast.expressions.*;
import org.spruce.compiler.bootstrap.ast.statements.ASTExpressionStatement;
import org.spruce.compiler.bootstrap.ast.statements.ASTLocalVariableDeclarationStatement;
import org.spruce.compiler.bootstrap.ast.statements.ASTVariableDeclarator;
import org.spruce.compiler.bootstrap.resolution.OperationsResolver;
import org.spruce.compiler.bootstrap.resolution.ResolutionContext;
import org.spruce.compiler.bootstrap.symbol.ChildSymbolTable;
import org.spruce.compiler.bootstrap.symbol.ParameterizedSymbol;
import org.spruce.compiler.bootstrap.symbol.ParentSymbol;
import org.spruce.compiler.bootstrap.symbol.Symbol;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;
import org.spruce.compiler.bootstrap.symbol.VariableSymbol;

import static org.junit.jupiter.api.Assertions.*;
import static org.spruce.compiler.bootstrap.symbol.GlobalLookup.UNNAMED_NAMESPACE_NAME;
import static org.spruce.compiler.bootstrap.test.resolution.ResolverTestUtility.*;
import static org.spruce.compiler.bootstrap.test.util.TestUtility.ensureIsa;

/**
 * All tests for the operations resolver.
 */
public class ResolverOperationsTest {

    //
    // Methods
    //

    /**
     * Tests bad argument list in method invocation.
     */
    @Test
    public void testMethodInvocationBadArguments() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Super {
                    void foo(Integer i) {}
                }
                class Test extends Super {
                    void testMethod() {
                        foo(i);
                    }
                }
                """
        );
        ResolverTestUtility.Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 1);
    }

    /**
     * Tests method invocation step 1 - type to search.  Typename super case
     * (1a), find superclass of enclosing type.
     */
    @Test
    public void testMethodInvocationTypeToSearchTypenameSuper() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Super {
                    void foo() {}
                }
                class Enclosing extends Super {
                    class Test {
                        void testMethod() {
                            Enclosing.super.foo();
                        }
                    }
                }
                """
        );
        
        Trio trio = resolveAllButMembers(codes);

        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol sooper = ensureIsa(unnamed.getTable().get("Super"), TypeSymbol.class);

        ASTClassDeclaration enclosingDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(1),
                ASTClassDeclaration.class);
        ASTClassDeclaration testDecl = ensureIsa(enclosingDecl.getClassParts().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration methodDecl = ensureIsa(testDecl.getClassParts().get(0), ASTMethodDeclaration.class);
        ASTExpressionStatement exprStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTExpressionStatement.class);
        ASTMethodInvocation methodInvocation = ensureIsa(exprStmt.getStmtExpr(), ASTMethodInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(1).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(methodDecl.getDeclSymbol());
        OperationsResolver opResolver = trio.resolver().getOperationsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        assertSame(sooper, optTypeToSearch.get());
        assertFalse(methodInvocation.isSharedContextOnly(), "Should not be limited to shared!");
        assertTrue(methodInvocation.isNonsharedContextOnly(), "Should be limited to non-shared!");
    }

    /**
     * Tests bad method invocation step 1 - type to search.  Typename super case
     * (1a), find superclass of enclosing type.  Not resolved.
     */
    @Test
    public void testMethodInvocationTypeToSearchTypenameSuperDne() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Super {
                    void foo() {}
                }
                class Enclosing extends Super {
                    class Test {
                        void testMethod() {
                            DoesNotExist.super.foo();
                        }
                    }
                }
                """
        );

        Trio trio = resolveAllButMembers(codes);

        ASTClassDeclaration enclosingDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(1),
                ASTClassDeclaration.class);
        ASTClassDeclaration testDecl = ensureIsa(enclosingDecl.getClassParts().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration methodDecl = ensureIsa(testDecl.getClassParts().get(0), ASTMethodDeclaration.class);
        ASTExpressionStatement exprStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTExpressionStatement.class);
        ASTMethodInvocation methodInvocation = ensureIsa(exprStmt.getStmtExpr(), ASTMethodInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(1).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(methodDecl.getDeclSymbol());
        OperationsResolver opResolver = trio.resolver().getOperationsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        expectError(trio.global(), opResolver);
        assertFalse(optTypeToSearch.isPresent(), "Should not have found type to search!");
    }

    /**
     * Tests bad method invocation step 1 - type to search.  Typename super case
     * (1a), find superclass of enclosing type.  Not an enclosing type.
     */
    @Test
    public void testMethodInvocationTypeToSearchTypenameSuperNotEnclosing() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Super {
                    void foo() {}
                }
                class Enclosing extends Super {
                    class Test {
                        void testMethod() {
                            Dummy.super.foo();
                        }
                    }
                }
                class Dummy {}
                """
        );

        Trio trio = resolveAllButMembers(codes);

        ASTClassDeclaration enclosingDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(1),
                ASTClassDeclaration.class);
        ASTClassDeclaration testDecl = ensureIsa(enclosingDecl.getClassParts().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration methodDecl = ensureIsa(testDecl.getClassParts().get(0), ASTMethodDeclaration.class);
        ASTExpressionStatement exprStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTExpressionStatement.class);
        ASTMethodInvocation methodInvocation = ensureIsa(exprStmt.getStmtExpr(), ASTMethodInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(1).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(methodDecl.getDeclSymbol());
        OperationsResolver opResolver = trio.resolver().getOperationsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        expectError(trio.global(), opResolver);
        assertFalse(optTypeToSearch.isPresent(), "Should not have found type to search!");
    }

    /**
     * Tests bad method invocation step 1 - type to search.  Typename super case
     * (1a), find superclass of enclosing type.  Enclosing type has no superclass.
     */
    @Test
    public void testMethodInvocationTypeToSearchTypenameSuperEnclosingNoSuperclass() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {
                    class Test {
                        void testMethod() {
                            Any.super.foo();
                        }
                    }
                }
                """
        );

        Trio trio = resolveAllButMembers(codes);

        ASTClassDeclaration enclosingDecl = ensureIsa(trio.ocus().get(0).getTypeDeclList().get(0),
                ASTClassDeclaration.class);
        ASTClassDeclaration testDecl = ensureIsa(enclosingDecl.getClassParts().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration methodDecl = ensureIsa(testDecl.getClassParts().get(0), ASTMethodDeclaration.class);
        ASTExpressionStatement exprStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTExpressionStatement.class);
        ASTMethodInvocation methodInvocation = ensureIsa(exprStmt.getStmtExpr(), ASTMethodInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(0).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(methodDecl.getDeclSymbol());
        OperationsResolver opResolver = trio.resolver().getOperationsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        expectError(trio.global(), opResolver);
        assertFalse(optTypeToSearch.isPresent(), "Should not have found type to search!");
    }

    /**
     * Tests method invocation step 1 - type to search.  Super case (1b),
     * find superclass.
     */
    @Test
    public void testMethodInvocationTypeToSearchSuper() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Super {
                    void foo() {}
                }
                class Test extends Super {
                    void testMethod() {
                        super.foo();
                    }
                }
                """
        );

        Trio trio = resolveAllButMembers(codes);

        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol sooper = ensureIsa(unnamed.getTable().get("Super"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 1, 0);
        ASTExpressionStatement exprStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTExpressionStatement.class);
        ASTMethodInvocation methodInvocation = ensureIsa(exprStmt.getStmtExpr(), ASTMethodInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(1).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(methodDecl.getDeclSymbol());
        OperationsResolver opResolver = trio.resolver().getOperationsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        assertSame(sooper, optTypeToSearch.get());
        assertFalse(methodInvocation.isSharedContextOnly(), "Should not be limited to shared!");
        assertTrue(methodInvocation.isNonsharedContextOnly(), "Should be limited to non-shared!");
    }

    /**
     * Tests bad method invocation step 1 - type to search.  Super case (1b),
     * find superclass.  No superclass.
     */
    @Test
    public void testMethodInvocationTypeToSearchSuperNoSuperclass() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {
                    void testMethod() {
                        super.foo();
                    }
                }
                """
        );

        Trio trio = resolveAllButMembers(codes);

        ASTMethodDeclaration methodDecl = getMethod(trio, 0, 0, 0);
        ASTExpressionStatement exprStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTExpressionStatement.class);
        ASTMethodInvocation methodInvocation = ensureIsa(exprStmt.getStmtExpr(), ASTMethodInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(0).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(methodDecl.getDeclSymbol());
        OperationsResolver opResolver = trio.resolver().getOperationsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertFalse(optTypeToSearch.isPresent(), "Should not have found type to search!");
    }

    /**
     * Tests method invocation step 1 - type to search.  Primary case (1c),
     * type of primary.
     */
    @Test
    public void testMethodInvocationTypeToSearchPrimary() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Clock {
                    String getTime() {
                        return "17:18";
                    }
                }
                """,
                """
                class Test {
                    void testMethod(Clock c) {
                        String time = (c).getTime();
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);

        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol clock = ensureIsa(unnamed.getTable().get("Clock"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 2, 0, 0);
        ASTLocalVariableDeclarationStatement localVarDeclStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDecl = localVarDeclStmt.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInit = varDecl.getVarInitializer();
        assertTrue(optInit.isPresent(), "Initializer not present!");

        ASTPrimary primary = ensureIsa(optInit.get(), ASTPrimary.class);
        assertEquals(ASTPrimary.Type.METHOD_INVOCATION, primary.getType());
        ASTMethodInvocation methodInvocation = ensureIsa(primary.getChild(), ASTMethodInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(2).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(methodDecl.getDeclSymbol());
        OperationsResolver opResolver = trio.resolver().getOperationsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        assertSame(clock, optTypeToSearch.get());
        assertFalse(methodInvocation.isSharedContextOnly(), "Should not be limited to shared!");
        assertTrue(methodInvocation.isNonsharedContextOnly(), "Should be limited to non-shared!");
    }

    /**
     * Tests bad method invocation step 1 - type to search.  Primary case (1c),
     * bad type of primary.
     */
    @Test
    public void testMethodInvocationTypeToSearchPrimaryBad() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Clock {
                    String getTime() {
                        return "17:18";
                    }
                }
                """,
                """
                class Test {
                    void testMethod(Clock c) {
                        String time = (dne).getTime();
                    }
                }
                """
        );
        Trio trio = resolveAllButMembers(codes);

        ASTMethodDeclaration methodDecl = getMethod(trio, 2, 0, 0);
        ASTLocalVariableDeclarationStatement localVarDeclStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDecl = localVarDeclStmt.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInit = varDecl.getVarInitializer();
        assertTrue(optInit.isPresent(), "Initializer not present!");

        ASTPrimary primary = ensureIsa(optInit.get(), ASTPrimary.class);
        assertEquals(ASTPrimary.Type.METHOD_INVOCATION, primary.getType());
        ASTMethodInvocation methodInvocation = ensureIsa(primary.getChild(), ASTMethodInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(2).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(methodDecl.getDeclSymbol());
        OperationsResolver opResolver = trio.resolver().getOperationsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        expectError(trio.global(), opResolver);
        assertFalse(optTypeToSearch.isPresent(), "Should not have found type to search!");
    }

    /**
     * Tests method invocation step 1 - type to search.  Expression name case
     * (1d), expression name.
     */
    @Test
    public void testMethodInvocationTypeToSearchExprName() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Clock {
                    String getTime() {
                        return "17:18";
                    }
                }
                """,
                """
                class Test {
                    void testMethod(Clock c) {
                        String time = c.getTime();
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);

        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol clock = ensureIsa(unnamed.getTable().get("Clock"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 2, 0, 0);
        ASTLocalVariableDeclarationStatement localVarDeclStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDecl = localVarDeclStmt.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInit = varDecl.getVarInitializer();
        assertTrue(optInit.isPresent(), "Initializer not present!");

        ASTPrimary primary = ensureIsa(optInit.get(), ASTPrimary.class);
        assertEquals(ASTPrimary.Type.METHOD_INVOCATION, primary.getType());
        ASTMethodInvocation methodInvocation = ensureIsa(primary.getChild(), ASTMethodInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(2).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(methodDecl.getDeclSymbol());
        OperationsResolver opResolver = trio.resolver().getOperationsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        assertSame(clock, optTypeToSearch.get());
        assertFalse(methodInvocation.isSharedContextOnly(), "Should not be limited to shared!");
        assertTrue(methodInvocation.isNonsharedContextOnly(), "Should be limited to non-shared!");
    }

    /**
     * Tests method invocation step 1 - type to search.  Expression name case
     * (1d), type name.
     */
    @Test
    public void testMethodInvocationTypeToSearchExprNameAsTypeName() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Clock {
                    shared String getType() {
                        return "Clock";
                    }
                }
                """,
                """
                class Test {
                    void testMethod() {
                        String time = Clock.getType();
                    }
                }
                """
        );
        Trio trio = resolveAllButMembers(codes);

        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol clock = ensureIsa(unnamed.getTable().get("Clock"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 2, 0, 0);
        ASTLocalVariableDeclarationStatement localVarDeclStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDecl = localVarDeclStmt.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInit = varDecl.getVarInitializer();
        assertTrue(optInit.isPresent(), "Initializer not present!");

        ASTPrimary primary = ensureIsa(optInit.get(), ASTPrimary.class);
        assertEquals(ASTPrimary.Type.METHOD_INVOCATION, primary.getType());
        ASTMethodInvocation methodInvocation = ensureIsa(primary.getChild(), ASTMethodInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(2).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(methodDecl.getDeclSymbol());
        OperationsResolver opResolver = trio.resolver().getOperationsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        assertSame(clock, optTypeToSearch.get());
        assertTrue(methodInvocation.isSharedContextOnly(), "Should be limited to shared!");
        assertFalse(methodInvocation.isNonsharedContextOnly(), "Should not be limited to non-shared!");
    }

    /**
     * Tests bad method invocation step 1 - type to search.  Expression name case
     * (1d), bad expression name.
     */
    @Test
    public void testMethodInvocationTypeToSearchExprNameBad() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Clock {
                    String getTime() {
                        return "17:18";
                    }
                }
                """,
                """
                class Test {
                    void testMethod(Clock c) {
                        String time = dne.getTime();
                    }
                }
                """
        );
        Trio trio = resolveAllButMembers(codes);

        ASTMethodDeclaration methodDecl = getMethod(trio, 2, 0, 0);
        ASTLocalVariableDeclarationStatement localVarDeclStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDecl = localVarDeclStmt.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInit = varDecl.getVarInitializer();
        assertTrue(optInit.isPresent(), "Initializer not present!");

        ASTPrimary primary = ensureIsa(optInit.get(), ASTPrimary.class);
        assertEquals(ASTPrimary.Type.METHOD_INVOCATION, primary.getType());
        ASTMethodInvocation methodInvocation = ensureIsa(primary.getChild(), ASTMethodInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(2).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(methodDecl.getDeclSymbol());
        OperationsResolver opResolver = trio.resolver().getOperationsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        expectError(trio.global(), opResolver);
        assertFalse(optTypeToSearch.isPresent(), "Should not have found type to search!");
    }

    /**
     * Tests method invocation step 1 - type to search.  Bare method name case,
     * (1e), find method in enclosing type.
     */
    @Test
    public void testMethodInvocationTypeToSearchBareNameEnclosingType() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Enclosing {
                    void foo() {}
                    class Test {
                        void testMethod() {
                            foo();
                        }
                    }
                }
                """
        );

        Trio trio = resolveAllButMembers(codes);

        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol enclosing = ensureIsa(unnamed.getTable().get("Enclosing"), TypeSymbol.class);

        ASTClassDeclaration enclosingDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0),
                ASTClassDeclaration.class);
        ASTClassDeclaration testDecl = ensureIsa(enclosingDecl.getClassParts().get(1), ASTClassDeclaration.class);
        ASTMethodDeclaration methodDecl = ensureIsa(testDecl.getClassParts().get(0), ASTMethodDeclaration.class);
        ASTExpressionStatement exprStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTExpressionStatement.class);
        ASTMethodInvocation methodInvocation = ensureIsa(exprStmt.getStmtExpr(), ASTMethodInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(1).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(methodDecl.getDeclSymbol());
        OperationsResolver opResolver = trio.resolver().getOperationsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        assertSame(enclosing, optTypeToSearch.get());
        assertFalse(methodInvocation.isSharedContextOnly(), "Should not be limited to shared!");
        assertFalse(methodInvocation.isNonsharedContextOnly(), "Should not be limited to non-shared!");
    }

    /**
     * Tests method invocation step 1 - type to search.  Bare method name case,
     * (1e), find method in superinterface.
     */
    @Test
    public void testMethodInvocationTypeToSearchBareNameInterface() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                interface Super {
                    void foo();
                }
                class Test implements Super {
                    void testMethod() {
                        foo();
                    }
                }
                """
        );

        Trio trio = resolveAllButMembers(codes);

        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol test = ensureIsa(unnamed.getTable().get("Test"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 1, 0);
        ASTExpressionStatement exprStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTExpressionStatement.class);
        ASTMethodInvocation methodInvocation = ensureIsa(exprStmt.getStmtExpr(), ASTMethodInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(1).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(methodDecl.getDeclSymbol());
        OperationsResolver opResolver = trio.resolver().getOperationsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        assertSame(test, optTypeToSearch.get());
        assertFalse(methodInvocation.isSharedContextOnly(), "Should not be limited to shared!");
        assertFalse(methodInvocation.isNonsharedContextOnly(), "Should not be limited to non-shared!");
    }

    /**
     * Tests method invocation step 1 - type to search.  Bare method name case,
     * (1e), find method in superclass.
     */
    @Test
    public void testMethodInvocationTypeToSearchBareNameSuperclass() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Super {
                    void foo() {}
                }
                class Test extends Super {
                    void testMethod() {
                        foo();
                    }
                }
                """
        );
        Trio trio = resolveAllButMembers(codes);

        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol test = ensureIsa(unnamed.getTable().get("Test"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 1, 0);
        ASTExpressionStatement exprStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTExpressionStatement.class);
        ASTMethodInvocation methodInvocation = ensureIsa(exprStmt.getStmtExpr(), ASTMethodInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(1).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(methodDecl.getDeclSymbol());
        OperationsResolver opResolver = trio.resolver().getOperationsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        assertSame(test, optTypeToSearch.get());
        assertFalse(methodInvocation.isSharedContextOnly(), "Should not be limited to shared!");
        assertFalse(methodInvocation.isNonsharedContextOnly(), "Should not be limited to non-shared!");
    }

    /**
     * Tests method invocation step 1 - type to search.  Bare method name case,
     * (1e), find method in same class.
     */
    @Test
    public void testMethodInvocationTypeToSearchBareNameSameClass() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void foo() {}
                    void testMethod() {
                        foo();
                    }
                }
                """
        );

        Trio trio = resolveAllButMembers(codes);

        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol test = ensureIsa(unnamed.getTable().get("Test"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 1);
        ASTExpressionStatement exprStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTExpressionStatement.class);
        ASTMethodInvocation methodInvocation = ensureIsa(exprStmt.getStmtExpr(), ASTMethodInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(1).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(methodDecl.getDeclSymbol());
        OperationsResolver opResolver = trio.resolver().getOperationsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        assertSame(test, optTypeToSearch.get());
        assertFalse(methodInvocation.isSharedContextOnly(), "Should not be limited to shared!");
        assertFalse(methodInvocation.isNonsharedContextOnly(), "Should not be limited to non-shared!");
    }

    /**
     * Tests bad method invocation step 1 - type to search.  Bare method name case,
     * (1e), method not found.
     */
    @Test
    public void testMethodInvocationTypeToSearchBareNameDoesNotExist() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod() {
                        doesNotExist();
                    }
                }
                """
        );

        Trio trio = resolveAllButMembers(codes);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTExpressionStatement exprStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTExpressionStatement.class);
        ASTMethodInvocation methodInvocation = ensureIsa(exprStmt.getStmtExpr(), ASTMethodInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(1).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(methodDecl.getDeclSymbol());
        OperationsResolver opResolver = trio.resolver().getOperationsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        expectError(trio.global(), opResolver);
        assertFalse(optTypeToSearch.isPresent(), "Should not have found type to search!");
    }

    /**
     * Tests method invocation step 2 - applicable methods.
     */
    @Test
    public void testMethodInvocationApplicableMethods() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void foo() {}
                    void foo(String s) {}
                    void foo(Integer i, Integer j) {}
                    void foot(Integer i) {}
                    void bar(Integer i) {}
                    void testMethod() {
                        foo(2);
                    }
                    void foo(Integer i) {}
                }
                """
        );

        Trio trio = compileSoFar(codes);
        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol test = ensureIsa(unnamed.getTable().get("Test"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 5);
        ASTExpressionStatement exprStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTExpressionStatement.class);
        ASTMethodInvocation methodInvocation = ensureIsa(exprStmt.getStmtExpr(), ASTMethodInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(1).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(methodDecl.getDeclSymbol());
        OperationsResolver opResolver = trio.resolver().getOperationsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        TypeSymbol typeToSearch = optTypeToSearch.get();
        assertSame(test, typeToSearch);
        Set<ParameterizedSymbol> applicableMethods =
                opResolver.getPotentiallyApplicableMethods(methodInvocation, typeToSearch);
        assertEquals(1, applicableMethods.size());
        List<String> names = applicableMethods.stream().map(Symbol::getName).toList();
        assertEquals("foo(spruce.lang.Integer)", names.get(0));
    }

    /**
     * Tests method invocation step 2 - applicable methods.
     * Find methods in superclass hierarchy, interface hierarchy, interface
     * hierarchy of a superclass.
     */
    @Test
    public void testMethodInvocationApplicableMethodsHierarchy() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test extends Super implements Fooable {
                    void foo() {}
                    void foo(String s) {}
                    void foo(Integer i, Integer j) {}
                    void foo(Integer i) {}
                    void bar(Integer i) {}
                    void testMethod() {
                        foo(2);
                    }
                }
                class Super implements Fooable {
                    void foo(Integer i) {}
                }
                interface Fooable {
                    void foo(Integer i);
                }
                """
        );

        Trio trio = compileSoFar(codes);
        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol test = ensureIsa(unnamed.getTable().get("Test"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 5);
        ASTExpressionStatement exprStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTExpressionStatement.class);
        ASTMethodInvocation methodInvocation = ensureIsa(exprStmt.getStmtExpr(), ASTMethodInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(1).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(methodDecl.getDeclSymbol());
        OperationsResolver opResolver = trio.resolver().getOperationsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        TypeSymbol typeToSearch = optTypeToSearch.get();
        assertSame(test, typeToSearch);
        Set<ParameterizedSymbol> applicableMethods =
                opResolver.getPotentiallyApplicableMethods(methodInvocation, typeToSearch);
        assertEquals(3, applicableMethods.size());
        List<String> names = applicableMethods.stream().map(Symbol::getName).toList();
        for (int i = 0; i < applicableMethods.size(); i++) {
            assertEquals("foo(spruce.lang.Integer)", names.get(i));
        }
    }

    /**
     * Tests method invocation step 2 - applicable methods.  Find methods where
     * all args are subtypes of the corresponding formal parameter types.
     */
    @Test
    public void testMethodInvocationApplicableMethodsWiden() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Sub sub) {
                        foo(sub, sub);
                    }
                    void foo() {}
                    void foo(Sub s, Sub t) {}
                    void foo(Super s, Super t) {}
                    void foo(Sub s, Super t) {}
                    void foo(Super s, Sub t) {}
                    void foo(Sub s, Integer i) {}
                    void foo(SubSub ss, Sub s) {}
                    void foo(Sub s) {}
                    void foo(Sub s, Sub t, Sub u) {}
                }
                class Super {}
                class Sub extends Super {}
                class SubSub extends Sub {}
                """
        );

        Trio trio = compileSoFar(codes);
        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol test = ensureIsa(unnamed.getTable().get("Test"), TypeSymbol.class);
        ParameterizedSymbol subSub = ensureIsa(test.getTable().get("foo(Sub, Sub)"), ParameterizedSymbol.class);
        ParameterizedSymbol superSuper = ensureIsa(test.getTable().get("foo(Super, Super)"), ParameterizedSymbol.class);
        ParameterizedSymbol subSuper = ensureIsa(test.getTable().get("foo(Sub, Super)"), ParameterizedSymbol.class);
        ParameterizedSymbol superSub = ensureIsa(test.getTable().get("foo(Super, Sub)"), ParameterizedSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTExpressionStatement exprStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTExpressionStatement.class);
        ASTMethodInvocation methodInvocation = ensureIsa(exprStmt.getStmtExpr(), ASTMethodInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(1).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(methodDecl.getDeclSymbol());
        OperationsResolver opResolver = trio.resolver().getOperationsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        TypeSymbol typeToSearch = optTypeToSearch.get();
        assertSame(test, typeToSearch);
        Set<ParameterizedSymbol> applicableMethods =
                opResolver.getPotentiallyApplicableMethods(methodInvocation, typeToSearch);
        assertEquals(4, applicableMethods.size());
        assertTrue(applicableMethods.contains(subSub));
        assertTrue(applicableMethods.contains(superSuper));
        assertTrue(applicableMethods.contains(subSuper));
        assertTrue(applicableMethods.contains(superSub));
    }

    /**
     * Tests method invocation step 2 - applicable methods.  No potentially
     * applicable methods.
     */
    @Test
    public void testMethodInvocationApplicableMethodsNone() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                        class Test {
                            void testMethod(Super s) {
                                foo(s, s);
                            }
                            void foo() {}
                            void foo(Sub s, Sub t) {}
                            void foo(Sub s, Super t) {}
                            void foo(Super s, Sub t) {}
                            void foo(Sub s, Integer i) {}
                            void foo(SubSub ss, Sub s) {}
                            void foo(Sub s) {}
                            void foo(Sub s, Sub t, Sub u) {}
                        }
                        class Super {}
                        class Sub extends Super {}
                        class SubSub extends Sub {}
                        """
        );

        Trio trio = compileSoFar(codes);
        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol test = ensureIsa(unnamed.getTable().get("Test"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTExpressionStatement exprStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTExpressionStatement.class);
        ASTMethodInvocation methodInvocation = ensureIsa(exprStmt.getStmtExpr(), ASTMethodInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(1).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(methodDecl.getDeclSymbol());
        OperationsResolver opResolver = trio.resolver().getOperationsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        TypeSymbol typeToSearch = optTypeToSearch.get();
        assertSame(test, typeToSearch);
        Set<ParameterizedSymbol> applicableMethods =
                opResolver.getPotentiallyApplicableMethods(methodInvocation, typeToSearch);
        assertEquals(0, applicableMethods.size());
    }

    /**
     * Tests method invocation step 3 - maximally specific methods.
     * Basic happy case - 1 is maximally specific.
     */
    @Test
    public void testMethodInvocationMaximallySpecificBasic() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(SubSub s) {
                        foo(s, s);
                    }
                    void foo(Sub s, Sub t) {}
                    void foo(Sub s, Super t) {}
                    void foo(Super s, Sub t) {}
                    void foo(SubSub ss, Sub s) {}
                }
                class Super {}
                class Sub extends Super {}
                class SubSub extends Sub {}
                """
        );

        Trio trio = compileSoFar(codes);
        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol test = ensureIsa(unnamed.getTable().get("Test"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTExpressionStatement exprStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTExpressionStatement.class);
        ASTMethodInvocation methodInvocation = ensureIsa(exprStmt.getStmtExpr(), ASTMethodInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(1).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(methodDecl.getDeclSymbol());
        OperationsResolver opResolver = trio.resolver().getOperationsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        TypeSymbol typeToSearch = optTypeToSearch.get();
        assertSame(test, typeToSearch);
        Set<ParameterizedSymbol> applicableMethods =
                opResolver.getPotentiallyApplicableMethods(methodInvocation, typeToSearch);
        assertEquals(4, applicableMethods.size());

        List<ParameterizedSymbol> maxSpecificMethods = opResolver.getMaximallySpecificMethods(applicableMethods);
        assertEquals(1, maxSpecificMethods.size());
        ParameterizedSymbol maxSpecificMethod = maxSpecificMethods.get(0);
        assertEquals("foo(SubSub, Sub)", maxSpecificMethod.getName());
        ChildSymbolTable table = ensureIsa(maxSpecificMethod.getParent(), ChildSymbolTable.class);
        TypeSymbol type = ensureIsa(table.getParent(), TypeSymbol.class);
        assertSame(test, type);
    }

    /**
     * Tests method invocation step 3 - maximally specific methods.
     * 1 is maximally specific, but only because it is found to override
     * another override-equivalent method.
     */
    @Test
    public void testMethodInvocationMaximallySpecificOverride() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test extends Superclass {
                    void testMethod(SubSub s) {
                        foo(s, s);
                    }
                    void foo(Sub s, Sub t) {}
                    void foo(Sub s, Super t) {}
                    void foo(Super s, Sub t) {}
                    void foo(SubSub ss, Sub s) {}
                    override void foo(SubSub ss, SubSub tt) {}
                }
                class Superclass {
                    void foo(SubSub ss, SubSub tt) {}
                }
                class Super {}
                class Sub extends Super {}
                class SubSub extends Sub {}
                """
        );

        Trio trio = compileSoFar(codes);
        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol test = ensureIsa(unnamed.getTable().get("Test"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTExpressionStatement exprStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTExpressionStatement.class);
        ASTMethodInvocation methodInvocation = ensureIsa(exprStmt.getStmtExpr(), ASTMethodInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(1).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(methodDecl.getDeclSymbol());
        OperationsResolver opResolver = trio.resolver().getOperationsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        TypeSymbol typeToSearch = optTypeToSearch.get();
        assertSame(test, typeToSearch);
        Set<ParameterizedSymbol> applicableMethods =
                opResolver.getPotentiallyApplicableMethods(methodInvocation, typeToSearch);
        assertEquals(6, applicableMethods.size());

        List<ParameterizedSymbol> maxSpecificMethods = opResolver.getMaximallySpecificMethods(applicableMethods);
        assertEquals(1, maxSpecificMethods.size());
        ParameterizedSymbol maxSpecificMethod = maxSpecificMethods.get(0);
        assertEquals("foo(SubSub, SubSub)", maxSpecificMethod.getName());
        ChildSymbolTable table = ensureIsa(maxSpecificMethod.getParent(), ChildSymbolTable.class);
        TypeSymbol type = ensureIsa(table.getParent(), TypeSymbol.class);
        assertSame(test, type);
    }

    /**
     * Tests method invocation step 3 - maximally specific methods.
     * Multiple methods are maximally specific, but not all are override
     * equivalent.
     */
    @Test
    public void testMethodInvocationMaximallySpecificNotAllOverrideEquivalent() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(SubSub s) {
                        foo(s, s);
                    }
                    void foo(Sub s, Sub t) {}
                    void foo(Sub s, Super t) {}
                    void foo(Sub s, SubSub tt) {}
                    void foo(SubSub ss, Sub s) {}
                }
                class Super {}
                class Sub extends Super {}
                class SubSub extends Sub {}
                """
        );

        Trio trio = compileSoFar(codes);
        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol test = ensureIsa(unnamed.getTable().get("Test"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTExpressionStatement exprStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTExpressionStatement.class);
        ASTMethodInvocation methodInvocation = ensureIsa(exprStmt.getStmtExpr(), ASTMethodInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(1).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(methodDecl.getDeclSymbol());
        OperationsResolver opResolver = trio.resolver().getOperationsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        TypeSymbol typeToSearch = optTypeToSearch.get();
        assertSame(test, typeToSearch);
        Set<ParameterizedSymbol> applicableMethods =
                opResolver.getPotentiallyApplicableMethods(methodInvocation, typeToSearch);
        assertEquals(4, applicableMethods.size());

        List<ParameterizedSymbol> maxSpecificMethods = opResolver.getMaximallySpecificMethods(applicableMethods);
        assertEquals(2, maxSpecificMethods.size());
        ParameterizedSymbol first = maxSpecificMethods.get(0);
        ParameterizedSymbol second = maxSpecificMethods.get(1);
        // Could be in any order.
        if ("foo(Sub, SubSub)".equals(first.getName())) {
            assertEquals("foo(SubSub, Sub)", second.getName());
        }
        else {
            assertEquals("foo(SubSub, Sub)", first.getName());
            assertEquals("foo(Sub, SubSub)", second.getName());
        }
    }

    /**
     * Tests method invocation step 3 - maximally specific methods.
     * Multiple methods are maximally specific, and they are override
     * equivalent, but only one is concrete and it's overriding.
     */
    @Test
    public void testMethodInvocationMaximallySpecificOneConcrete() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test extends Superclass implements BarOne, BarTwo {
                    void testMethod(SubSub s) {
                        foo(s, s);
                    }
                    override void foo(SubSub ss, SubSub tt) {}
                }
                class Superclass {
                    void foo(SubSub ss, SubSub tt) {}
                }
                interface BarOne {
                    void foo(SubSub ss, SubSub tt);
                }
                interface BarTwo {
                    void foo(SubSub ss, SubSub tt);
                }
                class Super {}
                class Sub extends Super {}
                class SubSub extends Sub {}
                """
        );

        Trio trio = compileSoFar(codes);
        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol test = ensureIsa(unnamed.getTable().get("Test"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTExpressionStatement exprStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTExpressionStatement.class);
        ASTMethodInvocation methodInvocation = ensureIsa(exprStmt.getStmtExpr(), ASTMethodInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(1).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(methodDecl.getDeclSymbol());
        OperationsResolver opResolver = trio.resolver().getOperationsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        TypeSymbol typeToSearch = optTypeToSearch.get();
        assertSame(test, typeToSearch);
        Set<ParameterizedSymbol> applicableMethods =
                opResolver.getPotentiallyApplicableMethods(methodInvocation, typeToSearch);
        assertEquals(4, applicableMethods.size());

        List<ParameterizedSymbol> maxSpecificMethods = opResolver.getMaximallySpecificMethods(applicableMethods);
        assertEquals(1, maxSpecificMethods.size());
        ParameterizedSymbol first = maxSpecificMethods.get(0);
        assertEquals("foo(SubSub, SubSub)", first.getName());

        ChildSymbolTable table = ensureIsa(first.getParent(), ChildSymbolTable.class);
        TypeSymbol type = ensureIsa(table.getParent(), TypeSymbol.class);
        assertSame(test, type);
    }

    /**
     * Tests method invocation step 3 - maximally specific methods.
     * Multiple methods are maximally specific, and they are override
     * equivalent, but only one is concrete and it's inherited.
     */
    @Test
    public void testMethodInvocationMaximallySpecificOneConcreteInherited() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test extends Superclass implements BarOne, BarTwo {
                    void testMethod(SubSub s) {
                        foo(s, s);
                    }
                }
                class Superclass {
                    void foo(SubSub ss, SubSub tt) {}
                }
                interface BarOne {
                    void foo(SubSub ss, SubSub tt);
                }
                interface BarTwo {
                    void foo(SubSub ss, SubSub tt);
                }
                class Super {}
                class Sub extends Super {}
                class SubSub extends Sub {}
                """
        );

        Trio trio = compileSoFar(codes);
        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol test = ensureIsa(unnamed.getTable().get("Test"), TypeSymbol.class);
        TypeSymbol superclass = ensureIsa(unnamed.getTable().get("Superclass"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTExpressionStatement exprStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTExpressionStatement.class);
        ASTMethodInvocation methodInvocation = ensureIsa(exprStmt.getStmtExpr(), ASTMethodInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(1).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(methodDecl.getDeclSymbol());
        OperationsResolver opResolver = trio.resolver().getOperationsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        TypeSymbol typeToSearch = optTypeToSearch.get();
        assertSame(test, typeToSearch);
        Set<ParameterizedSymbol> applicableMethods =
                opResolver.getPotentiallyApplicableMethods(methodInvocation, typeToSearch);
        assertEquals(3, applicableMethods.size());

        List<ParameterizedSymbol> maxSpecificMethods = opResolver.getMaximallySpecificMethods(applicableMethods);
        assertEquals(1, maxSpecificMethods.size());
        ParameterizedSymbol first = maxSpecificMethods.get(0);
        assertEquals("foo(SubSub, SubSub)", first.getName());

        ChildSymbolTable table = ensureIsa(first.getParent(), ChildSymbolTable.class);
        TypeSymbol type = ensureIsa(table.getParent(), TypeSymbol.class);
        assertSame(superclass, type);
    }

    /**
     * Tests method invocation step 3 - maximally specific methods.
     * Multiple methods are maximally specific, and they are override
     * equivalent, but all are abstract.
     */
    @Test
    public void testMethodInvocationMaximallySpecificAllAbstract() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test extends Superclass implements BarOne, BarTwo {
                    void testMethod(SubSub s) {
                        foo(s, s);
                    }
                }
                abstract class Superclass {
                    abstract void foo(SubSub ss, SubSub tt);
                }
                interface BarOne {
                    void foo(SubSub ss, SubSub tt);
                }
                interface BarTwo {
                    void foo(SubSub ss, SubSub tt);
                }
                class Super {}
                class Sub extends Super {}
                class SubSub extends Sub {}
                """
        );

        Trio trio = compileSoFar(codes);
        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol test = ensureIsa(unnamed.getTable().get("Test"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTExpressionStatement exprStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTExpressionStatement.class);
        ASTMethodInvocation methodInvocation = ensureIsa(exprStmt.getStmtExpr(), ASTMethodInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(1).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(methodDecl.getDeclSymbol());
        OperationsResolver opResolver = trio.resolver().getOperationsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        TypeSymbol typeToSearch = optTypeToSearch.get();
        assertSame(test, typeToSearch);
        Set<ParameterizedSymbol> applicableMethods =
                opResolver.getPotentiallyApplicableMethods(methodInvocation, typeToSearch);
        assertEquals(3, applicableMethods.size());

        List<ParameterizedSymbol> maxSpecificMethods = opResolver.getMaximallySpecificMethods(applicableMethods);
        assertEquals(1, maxSpecificMethods.size());
        ParameterizedSymbol first = maxSpecificMethods.get(0);
        assertEquals("foo(SubSub, SubSub)", first.getName());
        // Don't test the enclosing type symbol.  Any of the abstract methods
        // could be chosen.
    }

    // How can we have 2 maximally specific, override equivalent methods that
    // are both concrete?  For now, Spruce can't.  In Java, it's possible in 2 ways:
    // 1. Default methods are concrete, adding to the type to search or superclass
    //    set of concrete methods.
    // 2. Type erasure on generic methods when both type arguments are the same
    //    can yield 2 concrete methods that are override equivalent but both concrete.
    // But Spruce's initial compiler won't support either feature.
    // So for this initial compiler, only test the 0 or 1 concrete method cases.

    /**
     * Tests method invocation step 3 - maximally specific methods.
     * Multiple methods are all maximally specific, override equivalent, and
     * abstract, but one is preferred.
     */
    @Test
    public void testMethodInvocationMaximallySpecificPreferred() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test extends Superclass implements BarOne, BarTwo {
                    void testMethod(SubSub s) {
                        foo(s, s);
                    }
                }
                abstract class Superclass {
                    abstract SubSub foo(SubSub ss, SubSub tt);
                }
                interface BarOne {
                    Sub foo(SubSub ss, SubSub tt);
                }
                interface BarTwo {
                    Super foo(SubSub ss, SubSub tt);
                }
                class Super {}
                class Sub extends Super {}
                class SubSub extends Sub {}
                """
        );

        Trio trio = compileSoFar(codes);
        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol test = ensureIsa(unnamed.getTable().get("Test"), TypeSymbol.class);
        TypeSymbol superclass = ensureIsa(unnamed.getTable().get("Superclass"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTExpressionStatement exprStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTExpressionStatement.class);
        ASTMethodInvocation methodInvocation = ensureIsa(exprStmt.getStmtExpr(), ASTMethodInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(1).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(methodDecl.getDeclSymbol());
        OperationsResolver opResolver = trio.resolver().getOperationsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        TypeSymbol typeToSearch = optTypeToSearch.get();
        assertSame(test, typeToSearch);
        Set<ParameterizedSymbol> applicableMethods =
                opResolver.getPotentiallyApplicableMethods(methodInvocation, typeToSearch);
        assertEquals(3, applicableMethods.size());

        List<ParameterizedSymbol> maxSpecificMethods = opResolver.getMaximallySpecificMethods(applicableMethods);
        assertEquals(1, maxSpecificMethods.size());
        ParameterizedSymbol first = maxSpecificMethods.get(0);
        assertEquals("foo(SubSub, SubSub)", first.getName());

        ChildSymbolTable table = ensureIsa(first.getParent(), ChildSymbolTable.class);
        TypeSymbol type = ensureIsa(table.getParent(), TypeSymbol.class);
        assertSame(superclass, type);
    }

    /**
     * Tests method invocation step 3 - maximally specific methods.
     * Multiple methods are all maximally specific, override equivalent, and
     * abstract, and multiple are preferred, so one is picked.
     */
    @Test
    public void testMethodInvocationMaximallySpecificPreferredMultiple() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test extends Superclass implements BarOne, BarTwo {
                    void testMethod(SubSub s) {
                        foo(s, s);
                    }
                }
                abstract class Superclass {
                    abstract Super foo(SubSub ss, SubSub tt);
                }
                interface BarOne {
                    Sub foo(SubSub ss, SubSub tt);
                }
                interface BarTwo {
                    Sub foo(SubSub ss, SubSub tt);
                }
                class Super {}
                class Sub extends Super {}
                class SubSub extends Sub {}
                """
        );

        Trio trio = compileSoFar(codes);
        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol test = ensureIsa(unnamed.getTable().get("Test"), TypeSymbol.class);
        TypeSymbol superclass = ensureIsa(unnamed.getTable().get("Superclass"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTExpressionStatement exprStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTExpressionStatement.class);
        ASTMethodInvocation methodInvocation = ensureIsa(exprStmt.getStmtExpr(), ASTMethodInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(1).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(methodDecl.getDeclSymbol());
        OperationsResolver opResolver = trio.resolver().getOperationsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        TypeSymbol typeToSearch = optTypeToSearch.get();
        assertSame(test, typeToSearch);
        Set<ParameterizedSymbol> applicableMethods =
                opResolver.getPotentiallyApplicableMethods(methodInvocation, typeToSearch);
        assertEquals(3, applicableMethods.size());

        List<ParameterizedSymbol> maxSpecificMethods = opResolver.getMaximallySpecificMethods(applicableMethods);
        assertEquals(1, maxSpecificMethods.size());
        ParameterizedSymbol first = maxSpecificMethods.get(0);
        assertEquals("foo(SubSub, SubSub)", first.getName());

        ChildSymbolTable table = ensureIsa(first.getParent(), ChildSymbolTable.class);
        TypeSymbol type = ensureIsa(table.getParent(), TypeSymbol.class);
        assertNotSame(superclass, type);
    }

    /**
     * Tests method invocation step 3 - maximally specific methods.
     * Multiple methods are all maximally specific, override equivalent, and
     * abstract, and multiple are preferred returning void, so one is picked.
     */
    @Test
    public void testMethodInvocationMaximallySpecificPreferredMultipleVoid() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test extends Superclass implements BarOne, BarTwo {
                    void testMethod(SubSub s) {
                        foo(s, s);
                    }
                }
                abstract class Superclass {
                    abstract void foo(SubSub ss, SubSub tt);
                }
                interface BarOne {
                    void foo(SubSub ss, SubSub tt);
                }
                interface BarTwo {
                    void foo(SubSub ss, SubSub tt);
                }
                class Super {}
                class Sub extends Super {}
                class SubSub extends Sub {}
                """
        );

        Trio trio = compileSoFar(codes);
        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol test = ensureIsa(unnamed.getTable().get("Test"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTExpressionStatement exprStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTExpressionStatement.class);
        ASTMethodInvocation methodInvocation = ensureIsa(exprStmt.getStmtExpr(), ASTMethodInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(1).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(methodDecl.getDeclSymbol());
        OperationsResolver opResolver = trio.resolver().getOperationsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        TypeSymbol typeToSearch = optTypeToSearch.get();
        assertSame(test, typeToSearch);
        Set<ParameterizedSymbol> applicableMethods =
                opResolver.getPotentiallyApplicableMethods(methodInvocation, typeToSearch);
        assertEquals(3, applicableMethods.size());

        List<ParameterizedSymbol> maxSpecificMethods = opResolver.getMaximallySpecificMethods(applicableMethods);
        assertEquals(1, maxSpecificMethods.size());
        ParameterizedSymbol first = maxSpecificMethods.get(0);
        assertEquals("foo(SubSub, SubSub)", first.getName());

        // Don't test the enclosing type symbol.  Any of the abstract methods
        // could be chosen.
    }

    /**
     * Tests method invocation step 3 - maximally specific methods.
     * Multiple methods are all maximally specific, override equivalent, and
     * abstract, but none are preferred.
     */
    @Test
    public void testMethodInvocationMaximallySpecificPreferredNone() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test extends Superclass implements BarOne, BarTwo {
                    void testMethod(SubSub s) {
                        foo(s, s);
                    }
                }
                abstract class Superclass {
                    abstract Super foo(SubSub ss, SubSub tt);
                }
                interface BarOne {
                    Sub foo(SubSub ss, SubSub tt);
                }
                interface BarTwo {
                    Integer foo(SubSub ss, SubSub tt);
                }
                class Super {}
                class Sub extends Super {}
                class SubSub extends Sub {}
                """
        );

        Trio trio = compileSoFar(codes);
        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol test = ensureIsa(unnamed.getTable().get("Test"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTExpressionStatement exprStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTExpressionStatement.class);
        ASTMethodInvocation methodInvocation = ensureIsa(exprStmt.getStmtExpr(), ASTMethodInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(1).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(methodDecl.getDeclSymbol());
        OperationsResolver opResolver = trio.resolver().getOperationsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        TypeSymbol typeToSearch = optTypeToSearch.get();
        assertSame(test, typeToSearch);
        Set<ParameterizedSymbol> applicableMethods =
                opResolver.getPotentiallyApplicableMethods(methodInvocation, typeToSearch);
        assertEquals(3, applicableMethods.size());

        List<ParameterizedSymbol> maxSpecificMethods = opResolver.getMaximallySpecificMethods(applicableMethods);
        assertEquals(3, maxSpecificMethods.size());
        ParameterizedSymbol first = maxSpecificMethods.get(0);
        assertEquals("foo(SubSub, SubSub)", first.getName());
    }

    // When testing overall method resolution, only test the high-level results
    // instead of each individual case, which should already be tested above.

    /**
     * Tests method resolution, one match.
     */
    @Test
    public void testMethodInvocationResolutionMatch() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test extends Superclass {
                    void testMethod(SubSub s) {
                        foo(s, s);
                    }
                }
                abstract class Superclass {
                    abstract Super foo(SubSub ss, SubSub tt);
                }
                class Super {}
                class Sub extends Super {}
                class SubSub extends Sub {}
                """
        );

        Trio trio = compileSoFar(codes);
        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol sooper = ensureIsa(unnamed.getTable().get("Super"), TypeSymbol.class);
        TypeSymbol superclass = ensureIsa(unnamed.getTable().get("Superclass"), TypeSymbol.class);
        ParameterizedSymbol foo = ensureIsa(superclass.getTable().get("foo(SubSub, SubSub)"),
                ParameterizedSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTExpressionStatement exprStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTExpressionStatement.class);
        ASTMethodInvocation methodInvocation = ensureIsa(exprStmt.getStmtExpr(), ASTMethodInvocation.class);
        ParameterizedSymbol resolved = ensureIsa(methodInvocation.getResolvedEntity(),
                ParameterizedSymbol.class);
        assertSame(foo, resolved);
        assertSame(sooper, resolved.getDataType());
    }

    /**
     * Test bad method invocation of bad name.
     */
    @Test
    public void testMethodInvocationResolutionBadName() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(SubSub s) {
                        foo(s, s);
                    }
                    void bar(SubSub ss, SubSub tt) {}
                }
                class Super {}
                class Sub extends Super {}
                class SubSub extends Sub {}
                """
        );

        ResolverTestUtility.Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 1);
    }

    /**
     * Test bad method invocation of bad signature.
     */
    @Test
    public void testMethodInvocationResolutionBadSignature() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(SubSub s) {
                        foo(s, s);
                    }
                    void foo(Integer i, Double d) {}
                }
                class Super {}
                class Sub extends Super {}
                class SubSub extends Sub {}
                """
        );

        ResolverTestUtility.Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 1);
    }

    /**
     * Test bad method invocation of ambiguous resolution due to multiple
     * applicable methods not being override equivalent.
     */
    @Test
    public void testMethodInvocationResolutionAmbiguous() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(SubSub s) {
                        foo(s, s);
                    }
                    void foo(Sub s, Sub t) {}
                    void foo(Sub s, Super t) {}
                    void foo(Sub s, SubSub tt) {}
                    void foo(SubSub ss, Sub s) {}
                }
                class Super {}
                class Sub extends Super {}
                class SubSub extends Sub {}
                """
        );

        ResolverTestUtility.Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 1);
    }

    /**
     * Test method invocation of shared method by simple name.
     */
    @Test
    public void testMethodInvocationResolutionShared() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(SubSub s) {
                        foo(s, s);
                    }
                    shared void foo(SubSub ss, SubSub tt) {}
                }
                class Super {}
                class Sub extends Super {}
                class SubSub extends Sub {}
                """
        );

        ResolverTestUtility.Trio trio = compileSoFar(codes);
        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol test = ensureIsa(unnamed.getTable().get("Test"), TypeSymbol.class);
        ParameterizedSymbol foo = ensureIsa(test.getTable().get("foo(SubSub, SubSub)"),
                ParameterizedSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTExpressionStatement exprStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTExpressionStatement.class);
        ASTMethodInvocation methodInvocation = ensureIsa(exprStmt.getStmtExpr(), ASTMethodInvocation.class);
        ParameterizedSymbol resolved = ensureIsa(methodInvocation.getResolvedEntity(),
                ParameterizedSymbol.class);
        assertSame(foo, resolved);
        assertTrue(foo.isShared());
    }

    /**
     * Test bad method invocation of shared method from non-shared context.
     */
    @Test
    public void testMethodInvocationResolutionSharedCtxNonShared() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Test t, SubSub s) {
                        t.testMethod(s, s);
                    }
                    shared void testMethod(SubSub ss, SubSub tt) {}
                }
                class Super {}
                class Sub extends Super {}
                class SubSub extends Sub {}
                """
        );

        ResolverTestUtility.Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 1);
    }

    /**
     * Test bad method invocation of shared method not directly on type to search.
     */
    @Test
    public void testMethodInvocationResolutionSharedNotOnTypeToSearch() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test extends Superclass {
                    void testMethod(SubSub s) {
                        super.testMethod(s, s);
                    }
                }
                class Superclass {
                    shared void testMethod(SubSub ss, SubSub tt) {}
                }
                class Super {}
                class Sub extends Super {}
                class SubSub extends Sub {}
                """
        );

        ResolverTestUtility.Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 1);
    }

    //  Error non-shared method resolved but context is shared only.
    @Test
    public void testMethodInvocationResolutionNonSharedCtxShared() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    shared void testMethod(Test t, SubSub s) {
                        testMethod(s, s);
                    }
                    void testMethod(SubSub ss, SubSub tt) {}
                }
                class Super {}
                class Sub extends Super {}
                class SubSub extends Sub {}
                """
        );

        ResolverTestUtility.Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 1);
    }

    //
    // Operators
    //

    /**
     * Test binary expression of the plus operator.
     */
    @Test
    public void testBinaryExpressionPlusResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Character c, Integer i, Double d) {
                        Character c1 = c + 'c';
                        Integer i1 = i + '0';
                        Integer i2 = 10 + i;
                        Double d1 = d + 3.14159;
                        Double d2 = 2.71828 + i;
                        Double d3 = d + 'a';
                    }
                }
                """
        );

        ResolverTestUtility.Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol character = ensureIsa(lang.getTable().get("Character"), TypeSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);
        TypeSymbol duble = ensureIsa(lang.getTable().get("Double"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        VariableSymbol c = getFormalParameterSymbol(methodDecl, 0);
        assertSame(character, c.getDataType());
        VariableSymbol i = getFormalParameterSymbol(methodDecl, 1);
        assertSame(integer, i.getDataType());
        VariableSymbol d = getFormalParameterSymbol(methodDecl, 2);
        assertSame(duble, d.getDataType());

        ASTExpression expr0 = getVarInitializerFromVarDecl(methodDecl, 0);
        ASTBinaryExpression binaryExpr0 = ensureIsa(expr0, ASTBinaryExpression.class);
        ASTPrimary primary00 = ensureIsa(binaryExpr0.getFirst(), ASTPrimary.class);
        ASTPrimary primary01 = ensureIsa(binaryExpr0.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary00, primary01, binaryExpr0, character, character, character);

        ASTExpression expr1 = getVarInitializerFromVarDecl(methodDecl, 1);
        ASTBinaryExpression binaryExpr1 = ensureIsa(expr1, ASTBinaryExpression.class);
        ASTPrimary primary10 = ensureIsa(binaryExpr1.getFirst(), ASTPrimary.class);
        ASTPrimary primary11 = ensureIsa(binaryExpr1.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary10, primary11, binaryExpr1, integer, character, integer);

        ASTExpression expr2 = getVarInitializerFromVarDecl(methodDecl, 2);
        ASTBinaryExpression binaryExpr2 = ensureIsa(expr2, ASTBinaryExpression.class);
        ASTPrimary primary20 = ensureIsa(binaryExpr2.getFirst(), ASTPrimary.class);
        ASTPrimary primary21 = ensureIsa(binaryExpr2.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary20, primary21, binaryExpr2, integer, integer, integer);

        ASTExpression expr3 = getVarInitializerFromVarDecl(methodDecl, 3);
        ASTBinaryExpression binaryExpr3 = ensureIsa(expr3, ASTBinaryExpression.class);
        ASTPrimary primary30 = ensureIsa(binaryExpr3.getFirst(), ASTPrimary.class);
        ASTPrimary primary31 = ensureIsa(binaryExpr3.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary30, primary31, binaryExpr3, duble, duble, duble);

        ASTExpression expr4 = getVarInitializerFromVarDecl(methodDecl, 4);
        ASTBinaryExpression binaryExpr4 = ensureIsa(expr4, ASTBinaryExpression.class);
        ASTPrimary primary40 = ensureIsa(binaryExpr4.getFirst(), ASTPrimary.class);
        ASTPrimary primary41 = ensureIsa(binaryExpr4.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary40, primary41, binaryExpr4, duble, integer, duble);

        ASTExpression expr5 = getVarInitializerFromVarDecl(methodDecl, 5);
        ASTBinaryExpression binaryExpr5 = ensureIsa(expr5, ASTBinaryExpression.class);
        ASTPrimary primary50 = ensureIsa(binaryExpr5.getFirst(), ASTPrimary.class);
        ASTPrimary primary51 = ensureIsa(binaryExpr5.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary50, primary51, binaryExpr5, duble, character, duble);
    }

    /**
     * Test binary expression of the string plus operator.
     */
    @Test
    public void testBinaryExpressionStringPlusResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class MacGuffin {}
                """,
                """
                class Test {
                    void testMethod(Integer i, MacGuffin m) {
                        String s1 = "test" + i;
                        String s2 = "test" + 'i';
                        String s3 = "test" + "ing";
                        String s4 = m + "test";
                        String s5 = i + ". test";
                        String s6 = '1' + ". test";
                    }
                }
                """
        );

        ResolverTestUtility.Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol character = ensureIsa(lang.getTable().get("Character"), TypeSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);
        TypeSymbol string = ensureIsa(lang.getTable().get("String"), TypeSymbol.class);
        TypeSymbol macGuffin = ensureIsa(unnamed.getTable().get("MacGuffin"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 2, 0);

        ASTExpression expr0 = getVarInitializerFromVarDecl(methodDecl, 0);
        ASTBinaryExpression binaryExpr0 = ensureIsa(expr0, ASTBinaryExpression.class);
        ASTPrimary primary00 = ensureIsa(binaryExpr0.getFirst(), ASTPrimary.class);
        ASTPrimary primary01 = ensureIsa(binaryExpr0.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary00, primary01, binaryExpr0, string, integer, string);

        ASTExpression expr1 = getVarInitializerFromVarDecl(methodDecl, 1);
        ASTBinaryExpression binaryExpr1 = ensureIsa(expr1, ASTBinaryExpression.class);
        ASTPrimary primary10 = ensureIsa(binaryExpr1.getFirst(), ASTPrimary.class);
        ASTPrimary primary11 = ensureIsa(binaryExpr1.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary10, primary11, binaryExpr1, string, character, string);

        ASTExpression expr2 = getVarInitializerFromVarDecl(methodDecl, 2);
        ASTBinaryExpression binaryExpr2 = ensureIsa(expr2, ASTBinaryExpression.class);
        ASTPrimary primary20 = ensureIsa(binaryExpr2.getFirst(), ASTPrimary.class);
        ASTPrimary primary21 = ensureIsa(binaryExpr2.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary20, primary21, binaryExpr2, string, string, string);

        ASTExpression expr3 = getVarInitializerFromVarDecl(methodDecl, 3);
        ASTBinaryExpression binaryExpr3 = ensureIsa(expr3, ASTBinaryExpression.class);
        ASTPrimary primary30 = ensureIsa(binaryExpr3.getFirst(), ASTPrimary.class);
        ASTPrimary primary31 = ensureIsa(binaryExpr3.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary30, primary31, binaryExpr3, macGuffin, string, string);

        ASTExpression expr4 = getVarInitializerFromVarDecl(methodDecl, 4);
        ASTBinaryExpression binaryExpr4 = ensureIsa(expr4, ASTBinaryExpression.class);
        ASTPrimary primary40 = ensureIsa(binaryExpr4.getFirst(), ASTPrimary.class);
        ASTPrimary primary41 = ensureIsa(binaryExpr4.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary40, primary41, binaryExpr4, integer, string, string);

        ASTExpression expr5 = getVarInitializerFromVarDecl(methodDecl, 5);
        ASTBinaryExpression binaryExpr5 = ensureIsa(expr5, ASTBinaryExpression.class);
        ASTPrimary primary50 = ensureIsa(binaryExpr5.getFirst(), ASTPrimary.class);
        ASTPrimary primary51 = ensureIsa(binaryExpr5.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary50, primary51, binaryExpr5, character, string, string);
    }

    /**
     * Test bad binary expression of the plus operator.
     */
    @Test
    public void testBadBinaryExpressionPlusResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class MacGuffin {}
                """,
                """
                class Test {
                    void testMethod(MacGuffin m, Any a) {
                        String bad1 = m + a;
                        String bad2 = m + m;
                        String bad3 = a + a;
                    }
                }
                """
        );

        ResolverTestUtility.Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 3);
    }

    /**
     * Test binary expression of the minus operator.
     */
    @Test
    public void testBinaryExpressionMinusResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Character c, Integer i, Double d) {
                        Character c1 = c - 'c';
                        Integer i1 = i - '0';
                        Integer i2 = 10 - i;
                        Double d1 = d - 3.14159;
                        Double d2 = 2.71828 - i;
                        Double d3 = d - 'a';
                    }
                }
                """
        );

        ResolverTestUtility.Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol character = ensureIsa(lang.getTable().get("Character"), TypeSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);
        TypeSymbol duble = ensureIsa(lang.getTable().get("Double"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        VariableSymbol c = getFormalParameterSymbol(methodDecl, 0);
        assertSame(character, c.getDataType());
        VariableSymbol i = getFormalParameterSymbol(methodDecl, 1);
        assertSame(integer, i.getDataType());
        VariableSymbol d = getFormalParameterSymbol(methodDecl, 2);
        assertSame(duble, d.getDataType());

        ASTExpression expr0 = getVarInitializerFromVarDecl(methodDecl, 0);
        ASTBinaryExpression binaryExpr0 = ensureIsa(expr0, ASTBinaryExpression.class);
        ASTPrimary primary00 = ensureIsa(binaryExpr0.getFirst(), ASTPrimary.class);
        ASTPrimary primary01 = ensureIsa(binaryExpr0.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary00, primary01, binaryExpr0, character, character, character);

        ASTExpression expr1 = getVarInitializerFromVarDecl(methodDecl, 1);
        ASTBinaryExpression binaryExpr1 = ensureIsa(expr1, ASTBinaryExpression.class);
        ASTPrimary primary10 = ensureIsa(binaryExpr1.getFirst(), ASTPrimary.class);
        ASTPrimary primary11 = ensureIsa(binaryExpr1.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary10, primary11, binaryExpr1, integer, character, integer);

        ASTExpression expr2 = getVarInitializerFromVarDecl(methodDecl, 2);
        ASTBinaryExpression binaryExpr2 = ensureIsa(expr2, ASTBinaryExpression.class);
        ASTPrimary primary20 = ensureIsa(binaryExpr2.getFirst(), ASTPrimary.class);
        ASTPrimary primary21 = ensureIsa(binaryExpr2.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary20, primary21, binaryExpr2, integer, integer, integer);

        ASTExpression expr3 = getVarInitializerFromVarDecl(methodDecl, 3);
        ASTBinaryExpression binaryExpr3 = ensureIsa(expr3, ASTBinaryExpression.class);
        ASTPrimary primary30 = ensureIsa(binaryExpr3.getFirst(), ASTPrimary.class);
        ASTPrimary primary31 = ensureIsa(binaryExpr3.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary30, primary31, binaryExpr3, duble, duble, duble);

        ASTExpression expr4 = getVarInitializerFromVarDecl(methodDecl, 4);
        ASTBinaryExpression binaryExpr4 = ensureIsa(expr4, ASTBinaryExpression.class);
        ASTPrimary primary40 = ensureIsa(binaryExpr4.getFirst(), ASTPrimary.class);
        ASTPrimary primary41 = ensureIsa(binaryExpr4.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary40, primary41, binaryExpr4, duble, integer, duble);

        ASTExpression expr5 = getVarInitializerFromVarDecl(methodDecl, 5);
        ASTBinaryExpression binaryExpr5 = ensureIsa(expr5, ASTBinaryExpression.class);
        ASTPrimary primary50 = ensureIsa(binaryExpr5.getFirst(), ASTPrimary.class);
        ASTPrimary primary51 = ensureIsa(binaryExpr5.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary50, primary51, binaryExpr5, duble, character, duble);
    }

    /**
     * Tests bad binary expression resolution of the minus operator.
     */
    @Test
    public void testBadBinaryExpressionMinusResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Character c, String s, Integer i) {
                        Double bad0 = s - c;
                        Double bad1 = i - s;
                        Double bad2 = s - "bad";
                    }
                }
                """
        );
        ResolverTestUtility.Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 3);
    }

    /**
     * Tests binary expression resolution of relational operators.
     */
    @Test
    public void testBinaryExpressionRelationalResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Character c, Integer i, Double d) {
                        Boolean lt = c < c;
                        Boolean le = c <= i;
                        Boolean gt = c > d;
                        Boolean ge = i >= i;
                        Boolean eq = i == d;
                        Boolean ne = d != d;
                    }
                }
                """
        );
        ResolverTestUtility.Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol bool = ensureIsa(lang.getTable().get("Boolean"), TypeSymbol.class);
        TypeSymbol character = ensureIsa(lang.getTable().get("Character"), TypeSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);
        TypeSymbol duble = ensureIsa(lang.getTable().get("Double"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        VariableSymbol c = getFormalParameterSymbol(methodDecl, 0);
        assertSame(character, c.getDataType());
        VariableSymbol i = getFormalParameterSymbol(methodDecl, 1);
        assertSame(integer, i.getDataType());
        VariableSymbol d = getFormalParameterSymbol(methodDecl, 2);
        assertSame(duble, d.getDataType());

        ASTExpression expr0 = getVarInitializerFromVarDecl(methodDecl, 0);
        ASTBinaryExpression binaryExpr0 = ensureIsa(expr0, ASTBinaryExpression.class);
        ASTPrimary primary00 = ensureIsa(binaryExpr0.getFirst(), ASTPrimary.class);
        ASTPrimary primary01 = ensureIsa(binaryExpr0.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary00, primary01, binaryExpr0, character, character, bool);

        ASTExpression expr1 = getVarInitializerFromVarDecl(methodDecl, 1);
        ASTBinaryExpression binaryExpr1 = ensureIsa(expr1, ASTBinaryExpression.class);
        ASTPrimary primary10 = ensureIsa(binaryExpr1.getFirst(), ASTPrimary.class);
        ASTPrimary primary11 = ensureIsa(binaryExpr1.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary10, primary11, binaryExpr1, character, integer, bool);

        ASTExpression expr2 = getVarInitializerFromVarDecl(methodDecl, 2);
        ASTBinaryExpression binaryExpr2 = ensureIsa(expr2, ASTBinaryExpression.class);
        ASTPrimary primary20 = ensureIsa(binaryExpr2.getFirst(), ASTPrimary.class);
        ASTPrimary primary21 = ensureIsa(binaryExpr2.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary20, primary21, binaryExpr2, character, duble, bool);

        ASTExpression expr3 = getVarInitializerFromVarDecl(methodDecl, 3);
        ASTBinaryExpression binaryExpr3 = ensureIsa(expr3, ASTBinaryExpression.class);
        ASTPrimary primary30 = ensureIsa(binaryExpr3.getFirst(), ASTPrimary.class);
        ASTPrimary primary31 = ensureIsa(binaryExpr3.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary30, primary31, binaryExpr3, integer, integer, bool);

        ASTExpression expr4 = getVarInitializerFromVarDecl(methodDecl, 4);
        ASTBinaryExpression binaryExpr4 = ensureIsa(expr4, ASTBinaryExpression.class);
        ASTPrimary primary40 = ensureIsa(binaryExpr4.getFirst(), ASTPrimary.class);
        ASTPrimary primary41 = ensureIsa(binaryExpr4.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary40, primary41, binaryExpr4, integer, duble, bool);

        ASTExpression expr5 = getVarInitializerFromVarDecl(methodDecl, 5);
        ASTBinaryExpression binaryExpr5 = ensureIsa(expr5, ASTBinaryExpression.class);
        ASTPrimary primary50 = ensureIsa(binaryExpr5.getFirst(), ASTPrimary.class);
        ASTPrimary primary51 = ensureIsa(binaryExpr5.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary50, primary51, binaryExpr5, duble, duble, bool);
    }

    /**
     * Tests bad binary expression resolution of relational operators.
     */
    @Test
    public void testBadBinaryExpressionRelationalResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Boolean b, String s, Integer i) {
                        Boolean bad0 = b < i;
                        Boolean bad1 = s < i;
                        Boolean bad2 = b < s;
                    }
                }
                """
        );
        ResolverTestUtility.Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 3);
    }

    /**
     * Tests binary expression resolution of logical operators.
     */
    @Test
    public void testBinaryExpressionLogicalResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Boolean b0, Boolean b1) {
                        Boolean and = b0 && b1;
                        Boolean or = b0 || b1;
                    }
                }
                """
        );
        ResolverTestUtility.Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol bool = ensureIsa(lang.getTable().get("Boolean"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        VariableSymbol b0 = getFormalParameterSymbol(methodDecl, 0);
        assertSame(bool, b0.getDataType());
        VariableSymbol b1 = getFormalParameterSymbol(methodDecl, 0);
        assertSame(bool, b1.getDataType());

        ASTExpression expr0 = getVarInitializerFromVarDecl(methodDecl, 0);
        ASTBinaryExpression binaryExpr0 = ensureIsa(expr0, ASTBinaryExpression.class);
        ASTPrimary primary00 = ensureIsa(binaryExpr0.getFirst(), ASTPrimary.class);
        ASTPrimary primary01 = ensureIsa(binaryExpr0.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary00, primary01, binaryExpr0, bool, bool, bool);

        ASTExpression expr1 = getVarInitializerFromVarDecl(methodDecl, 1);
        ASTBinaryExpression binaryExpr1 = ensureIsa(expr1, ASTBinaryExpression.class);
        ASTPrimary primary10 = ensureIsa(binaryExpr1.getFirst(), ASTPrimary.class);
        ASTPrimary primary11 = ensureIsa(binaryExpr1.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary10, primary11, binaryExpr1, bool, bool, bool);
    }

    /**
     * Tests bad binary expression resolution of logical operators.
     */
    @Test
    public void testBinaryExpressionBadLogicalResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Boolean b, String s, Integer i) {
                        Boolean bad0 = b && s;
                        Boolean bad1 = i || b;
                        Boolean bad2 = s || i;
                    }
                }
                """
        );
        ResolverTestUtility.Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 3);
    }

    /**
     * Tests unary expression of minus resolution.
     */
    @Test
    public void testUnaryExpressionMinusResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Character c, Integer i, Double d) {
                        Character minusC = -c;
                        Integer minusI = -i;
                        Double minusD = -d;
                    }
                }
                """
        );
        ResolverTestUtility.Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol character = ensureIsa(lang.getTable().get("Character"), TypeSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);
        TypeSymbol duble = ensureIsa(lang.getTable().get("Double"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        VariableSymbol c = getFormalParameterSymbol(methodDecl, 0);
        VariableSymbol i = getFormalParameterSymbol(methodDecl, 1);
        VariableSymbol d = getFormalParameterSymbol(methodDecl, 2);
        assertSame(character, c.getDataType());
        assertSame(integer, i.getDataType());
        assertSame(duble, d.getDataType());

        ASTExpression expr0 = getVarInitializerFromVarDecl(methodDecl, 0);
        ASTUnaryExpression unaryExpr0 = ensureIsa(expr0, ASTUnaryExpression.class);
        ASTPrimary primary0 = ensureIsa(unaryExpr0.getFirst(), ASTPrimary.class);
        checkUnaryExpression(primary0, unaryExpr0, character, character);

        ASTExpression expr1 = getVarInitializerFromVarDecl(methodDecl, 1);
        ASTUnaryExpression unaryExpr1 = ensureIsa(expr1, ASTUnaryExpression.class);
        ASTPrimary primary1 = ensureIsa(unaryExpr1.getFirst(), ASTPrimary.class);
        checkUnaryExpression(primary1, unaryExpr1, integer, integer);

        ASTExpression expr2 = getVarInitializerFromVarDecl(methodDecl, 2);
        ASTUnaryExpression unaryExpr2 = ensureIsa(expr2, ASTUnaryExpression.class);
        ASTPrimary primary2 = ensureIsa(unaryExpr2.getFirst(), ASTPrimary.class);
        checkUnaryExpression(primary2, unaryExpr2, duble, duble);
    }

    /**
     * Tests bad unary expression of minus not numeric.
     */
    @Test
    public void testBadUnaryExpressionMinusResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Boolean b, String s) {
                        Boolean minusB = -b;
                        String minusS = -s;
                    }
                }
                """
        );
        ResolverTestUtility.Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 2);
    }

    /**
     * Tests unary expression of exclamation resolution.
     */
    @Test
    public void testUnaryExpressionBooleanResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Boolean b) {
                        Boolean not = !b;
                    }
                }
                """
        );
        ResolverTestUtility.Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol bool = ensureIsa(lang.getTable().get("Boolean"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        VariableSymbol b = getFormalParameterSymbol(methodDecl, 0);
        assertSame(bool, b.getDataType());

        ASTExpression expr0 = getVarInitializerFromVarDecl(methodDecl, 0);
        ASTUnaryExpression unaryExpr = ensureIsa(expr0, ASTUnaryExpression.class);
        ASTPrimary primary = ensureIsa(unaryExpr.getFirst(), ASTPrimary.class);
        assertSame(bool, primary.getResolvedDataType());
        assertSame(bool, unaryExpr.getResolvedDataType());
        checkUnaryExpression(primary, unaryExpr, bool, bool);
    }

    /**
     * Tests bad unary expression of exclamation not boolean.
     */
    @Test
    public void testBadUnaryExpressionBooleanResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Integer i, String s) {
                        Boolean notI = !i;
                        Boolean notS = !s;
                    }
                }
                """
        );
        ResolverTestUtility.Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 2);
    }

    private void checkBinaryExpression(ASTValueExpression first, ASTValueExpression second,
                                       ASTBinaryExpression binaryExpr, TypeSymbol tsFirst,
                                       TypeSymbol tsSecond, TypeSymbol tsBinary) {
        assertSame(tsFirst, first.getResolvedDataType());
        assertSame(tsSecond, second.getResolvedDataType());
        assertSame(tsBinary, binaryExpr.getResolvedDataType());
    }

    private void checkUnaryExpression(ASTValueExpression first, ASTUnaryExpression unaryExpr,
                                      TypeSymbol tsFirst, TypeSymbol tsUnary) {
        assertSame(tsFirst, first.getResolvedDataType());
        assertSame(tsUnary, unaryExpr.getResolvedDataType());
    }

    private ASTExpression getVarInitializerFromVarDecl(ASTMethodDeclaration methodDecl, int idx) {
        ASTLocalVariableDeclarationStatement localVarDeclStmt = ensureIsa(
                getBlockStatement(methodDecl, idx), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDecl = localVarDeclStmt.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInit = varDecl.getVarInitializer();
        assertTrue(optInit.isPresent(), "Initializer not present!");
        return optInit.get();
    }
}
