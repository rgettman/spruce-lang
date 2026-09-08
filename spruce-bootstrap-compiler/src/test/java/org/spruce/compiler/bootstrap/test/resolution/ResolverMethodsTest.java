package org.spruce.compiler.bootstrap.test.resolution;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.bootstrap.ast.classes.ASTClassDeclaration;
import org.spruce.compiler.bootstrap.ast.classes.ASTMethodDeclaration;
import org.spruce.compiler.bootstrap.ast.expressions.ASTExpression;
import org.spruce.compiler.bootstrap.ast.expressions.ASTMethodInvocation;
import org.spruce.compiler.bootstrap.ast.expressions.ASTPrimary;
import org.spruce.compiler.bootstrap.ast.statements.ASTExpressionStatement;
import org.spruce.compiler.bootstrap.ast.statements.ASTLocalVariableDeclarationStatement;
import org.spruce.compiler.bootstrap.ast.statements.ASTVariableDeclarator;
import org.spruce.compiler.bootstrap.resolution.MethodsResolver;
import org.spruce.compiler.bootstrap.resolution.ResolutionContext;
import org.spruce.compiler.bootstrap.symbol.ChildSymbolTable;
import org.spruce.compiler.bootstrap.symbol.ParameterizedSymbol;
import org.spruce.compiler.bootstrap.symbol.ParentSymbol;
import org.spruce.compiler.bootstrap.symbol.Symbol;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;

import static org.junit.jupiter.api.Assertions.*;
import static org.spruce.compiler.bootstrap.symbol.GlobalLookup.UNNAMED_NAMESPACE_NAME;
import static org.spruce.compiler.bootstrap.test.resolution.ResolverTestUtility.*;
import static org.spruce.compiler.bootstrap.test.util.TestUtility.ensureIsa;

public class ResolverMethodsTest {
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

        ResolverTestUtility.Trio trio = resolveAllButMembers(codes);

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
        MethodsResolver opResolver = trio.resolver().getMethodsResolver();
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
        MethodsResolver opResolver = trio.resolver().getMethodsResolver();
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
        MethodsResolver opResolver = trio.resolver().getMethodsResolver();
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
        MethodsResolver opResolver = trio.resolver().getMethodsResolver();
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
        MethodsResolver opResolver = trio.resolver().getMethodsResolver();
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
        MethodsResolver opResolver = trio.resolver().getMethodsResolver();
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
        MethodsResolver opResolver = trio.resolver().getMethodsResolver();
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
        MethodsResolver opResolver = trio.resolver().getMethodsResolver();
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
        MethodsResolver opResolver = trio.resolver().getMethodsResolver();
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
        MethodsResolver opResolver = trio.resolver().getMethodsResolver();
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
        MethodsResolver opResolver = trio.resolver().getMethodsResolver();
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
        MethodsResolver opResolver = trio.resolver().getMethodsResolver();
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
        MethodsResolver opResolver = trio.resolver().getMethodsResolver();
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
        MethodsResolver opResolver = trio.resolver().getMethodsResolver();
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
        MethodsResolver opResolver = trio.resolver().getMethodsResolver();
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
        MethodsResolver opResolver = trio.resolver().getMethodsResolver();
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
        MethodsResolver opResolver = trio.resolver().getMethodsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        TypeSymbol typeToSearch = optTypeToSearch.get();
        assertSame(test, typeToSearch);
        Set<ParameterizedSymbol> applicableMethods =
                opResolver.getPotentiallyApplicable(methodInvocation, typeToSearch);
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
        MethodsResolver opResolver = trio.resolver().getMethodsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        TypeSymbol typeToSearch = optTypeToSearch.get();
        assertSame(test, typeToSearch);
        Set<ParameterizedSymbol> applicableMethods =
                opResolver.getPotentiallyApplicable(methodInvocation, typeToSearch);
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
        MethodsResolver opResolver = trio.resolver().getMethodsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        TypeSymbol typeToSearch = optTypeToSearch.get();
        assertSame(test, typeToSearch);
        Set<ParameterizedSymbol> applicableMethods =
                opResolver.getPotentiallyApplicable(methodInvocation, typeToSearch);
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
        MethodsResolver opResolver = trio.resolver().getMethodsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        TypeSymbol typeToSearch = optTypeToSearch.get();
        assertSame(test, typeToSearch);
        Set<ParameterizedSymbol> applicableMethods =
                opResolver.getPotentiallyApplicable(methodInvocation, typeToSearch);
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
        MethodsResolver opResolver = trio.resolver().getMethodsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        TypeSymbol typeToSearch = optTypeToSearch.get();
        assertSame(test, typeToSearch);
        Set<ParameterizedSymbol> applicableMethods =
                opResolver.getPotentiallyApplicable(methodInvocation, typeToSearch);
        assertEquals(4, applicableMethods.size());

        List<ParameterizedSymbol> maxSpecificMethods = opResolver.getMaximallySpecific(applicableMethods);
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
        MethodsResolver opResolver = trio.resolver().getMethodsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        TypeSymbol typeToSearch = optTypeToSearch.get();
        assertSame(test, typeToSearch);
        Set<ParameterizedSymbol> applicableMethods =
                opResolver.getPotentiallyApplicable(methodInvocation, typeToSearch);
        assertEquals(6, applicableMethods.size());

        List<ParameterizedSymbol> maxSpecificMethods = opResolver.getMaximallySpecific(applicableMethods);
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
        MethodsResolver opResolver = trio.resolver().getMethodsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        TypeSymbol typeToSearch = optTypeToSearch.get();
        assertSame(test, typeToSearch);
        Set<ParameterizedSymbol> applicableMethods =
                opResolver.getPotentiallyApplicable(methodInvocation, typeToSearch);
        assertEquals(4, applicableMethods.size());

        List<ParameterizedSymbol> maxSpecificMethods = opResolver.getMaximallySpecific(applicableMethods);
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
        MethodsResolver opResolver = trio.resolver().getMethodsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        TypeSymbol typeToSearch = optTypeToSearch.get();
        assertSame(test, typeToSearch);
        Set<ParameterizedSymbol> applicableMethods =
                opResolver.getPotentiallyApplicable(methodInvocation, typeToSearch);
        assertEquals(4, applicableMethods.size());

        List<ParameterizedSymbol> maxSpecificMethods = opResolver.getMaximallySpecific(applicableMethods);
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
        MethodsResolver opResolver = trio.resolver().getMethodsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        TypeSymbol typeToSearch = optTypeToSearch.get();
        assertSame(test, typeToSearch);
        Set<ParameterizedSymbol> applicableMethods =
                opResolver.getPotentiallyApplicable(methodInvocation, typeToSearch);
        assertEquals(3, applicableMethods.size());

        List<ParameterizedSymbol> maxSpecificMethods = opResolver.getMaximallySpecific(applicableMethods);
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
        MethodsResolver opResolver = trio.resolver().getMethodsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        TypeSymbol typeToSearch = optTypeToSearch.get();
        assertSame(test, typeToSearch);
        Set<ParameterizedSymbol> applicableMethods =
                opResolver.getPotentiallyApplicable(methodInvocation, typeToSearch);
        assertEquals(3, applicableMethods.size());

        List<ParameterizedSymbol> maxSpecificMethods = opResolver.getMaximallySpecific(applicableMethods);
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
        MethodsResolver opResolver = trio.resolver().getMethodsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        TypeSymbol typeToSearch = optTypeToSearch.get();
        assertSame(test, typeToSearch);
        Set<ParameterizedSymbol> applicableMethods =
                opResolver.getPotentiallyApplicable(methodInvocation, typeToSearch);
        assertEquals(3, applicableMethods.size());

        List<ParameterizedSymbol> maxSpecificMethods = opResolver.getMaximallySpecific(applicableMethods);
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
        MethodsResolver opResolver = trio.resolver().getMethodsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        TypeSymbol typeToSearch = optTypeToSearch.get();
        assertSame(test, typeToSearch);
        Set<ParameterizedSymbol> applicableMethods =
                opResolver.getPotentiallyApplicable(methodInvocation, typeToSearch);
        assertEquals(3, applicableMethods.size());

        List<ParameterizedSymbol> maxSpecificMethods = opResolver.getMaximallySpecific(applicableMethods);
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
        MethodsResolver opResolver = trio.resolver().getMethodsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        TypeSymbol typeToSearch = optTypeToSearch.get();
        assertSame(test, typeToSearch);
        Set<ParameterizedSymbol> applicableMethods =
                opResolver.getPotentiallyApplicable(methodInvocation, typeToSearch);
        assertEquals(3, applicableMethods.size());

        List<ParameterizedSymbol> maxSpecificMethods = opResolver.getMaximallySpecific(applicableMethods);
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
        MethodsResolver opResolver = trio.resolver().getMethodsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(methodInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        TypeSymbol typeToSearch = optTypeToSearch.get();
        assertSame(test, typeToSearch);
        Set<ParameterizedSymbol> applicableMethods =
                opResolver.getPotentiallyApplicable(methodInvocation, typeToSearch);
        assertEquals(3, applicableMethods.size());

        List<ParameterizedSymbol> maxSpecificMethods = opResolver.getMaximallySpecific(applicableMethods);
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
}
