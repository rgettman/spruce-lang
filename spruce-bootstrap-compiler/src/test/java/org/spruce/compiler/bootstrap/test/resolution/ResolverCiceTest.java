package org.spruce.compiler.bootstrap.test.resolution;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.bootstrap.ast.classes.ASTClassDeclaration;
import org.spruce.compiler.bootstrap.ast.classes.ASTMethodDeclaration;
import org.spruce.compiler.bootstrap.ast.expressions.ASTClassInstanceCreationExpression;
import org.spruce.compiler.bootstrap.ast.expressions.ASTPrimary;
import org.spruce.compiler.bootstrap.ast.statements.ASTConstructorInvocation;
import org.spruce.compiler.bootstrap.ast.statements.ASTLocalVariableDeclarationStatement;
import org.spruce.compiler.bootstrap.resolution.CiceResolver;
import org.spruce.compiler.bootstrap.resolution.ResolutionContext;
import org.spruce.compiler.bootstrap.symbol.ParameterizedSymbol;
import org.spruce.compiler.bootstrap.symbol.ParentSymbol;
import org.spruce.compiler.bootstrap.symbol.Symbol;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spruce.compiler.bootstrap.symbol.GlobalLookup.UNNAMED_NAMESPACE_NAME;
import static org.spruce.compiler.bootstrap.test.resolution.ResolverTestUtility.*;
import static org.spruce.compiler.bootstrap.test.util.TestUtility.ensureIsa;

/**
 * All tests for the CICE (class instance creation expressions) resolver.
 */
public class ResolverCiceTest {
    /**
     * Tests bad argument list in CICE.
     */
    @Test
    public void testCiceBadArguments() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Foo {
                    constructor(Integer i) {}
                }
                class Test {
                    void test() {
                        Foo foo = new Foo("foo");
                    }
                }
                """
        );
        ResolverTestUtility.Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 1);
    }

    /**
     * Tests CICE step 1 - type to search.  Qualified by a primary case (1a),
     * find the type.
     */
    @Test
    public void testCiceTypeToSearchQualifiedPrimary() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Outer {
                    class Inner {
                        constructor(Integer i) {}
                    }
                }
                class Test {
                    void test(Outer outer) {
                        Outer.Inner inner = outer.new Inner();
                    }
                }
                """
        );

        ResolverTestUtility.Trio trio = resolveAllButMembers(codes);

        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol outer = ensureIsa(unnamed.getTable().get("Outer"), TypeSymbol.class);
        TypeSymbol inner = ensureIsa(outer.getTable().get("Inner"), TypeSymbol.class);

        ASTClassDeclaration testDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(1), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethodDecl = ensureIsa(testDecl.getClassParts().get(0), ASTMethodDeclaration.class);
        ASTLocalVariableDeclarationStatement innerStmt = ensureIsa(
                getBlockStatement(testMethodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        assertTrue(innerStmt.getLocalVarDecl().getVarDeclList().get(0).getVarInitializer().isPresent());
        ASTPrimary primary = ensureIsa(innerStmt.getLocalVarDecl().getVarDeclList().get(0).getVarInitializer().get(),
                ASTPrimary.class);
        ASTClassInstanceCreationExpression cice = ensureIsa(primary.getChild(), ASTClassInstanceCreationExpression.class);

        ResolutionContext ocuCtx = trio.ocus().get(1).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(testMethodDecl.getDeclSymbol());
        CiceResolver opResolver = trio.resolver().getCiceResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(cice, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        assertSame(inner, optTypeToSearch.get());
    }

    /**
     * Tests CICE step 1 - type to search.  Qualified by a primary case (1a),
     * bad primary.
     */
    @Test
    public void testCiceTypeToSearchQualifiedBadPrimary() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Outer {
                    class Inner {
                        constructor(Integer i) {}
                    }
                }
                class Test {
                    void test(Outer outer) {
                        Outer.Inner inner = dne.new Inner();
                    }
                }
                """
        );

        ResolverTestUtility.Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 1);
    }

    /**
     * Tests CICE step 1 - type to search.  Qualified by a primary case (1a),
     * does not exist within the enclosing type.
     */
    @Test
    public void testCiceTypeToSearchQualifiedPrimaryDNE() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Outer {
                    class Something {}
                }
                class Inner {
                    constructor(Integer i) {}
                }
                class Test {
                    void test(Outer outer) {
                        Outer.Inner inner = outer.new Inner();
                    }
                }
                """
        );

        ResolverTestUtility.Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 2);
    }

    /**
     * Tests CICE step 1 - type to search.  Unqualified case (1b),
     * find the type.
     */
    @Test
    public void testCiceTypeToSearchUnqualified() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Outer {
                    constructor(Integer i) {}
                }
                class Test {
                    void test() {
                        Outer outer = new Outer();
                    }
                }
                """
        );

        ResolverTestUtility.Trio trio = resolveAllButMembers(codes);

        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol outer = ensureIsa(unnamed.getTable().get("Outer"), TypeSymbol.class);

        ASTClassDeclaration testDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(1), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethodDecl = ensureIsa(testDecl.getClassParts().get(0), ASTMethodDeclaration.class);
        ASTLocalVariableDeclarationStatement innerStmt = ensureIsa(
                getBlockStatement(testMethodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        assertTrue(innerStmt.getLocalVarDecl().getVarDeclList().get(0).getVarInitializer().isPresent());
        ASTPrimary primary = ensureIsa(innerStmt.getLocalVarDecl().getVarDeclList().get(0).getVarInitializer().get(),
                ASTPrimary.class);
        ASTClassInstanceCreationExpression cice = ensureIsa(primary.getChild(), ASTClassInstanceCreationExpression.class);

        ResolutionContext ocuCtx = trio.ocus().get(1).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(testMethodDecl.getDeclSymbol());
        CiceResolver opResolver = trio.resolver().getCiceResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(cice, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        assertSame(outer, optTypeToSearch.get());
    }

    //
    // Step 2 and step 3 test cases are inherited from AbstractConstructorsResolver
    // to both CiceResolver and ConstructorsResolver.  Test cases for step 2
    // and step 3 are in ResolverConstructorsTest.java.
    //

    // When testing overall CICE resolution, only test the high-level results
    // instead of each individual case, which should already be tested above.

    /**
     * Tests CICE resolution, one constructor match.
     */
    @Test
    public void testCiceResolutionMatch() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void test() {
                        Target t = new Target("target");
                    }
                }
                class Target {
                    constructor(String s) {}
                }
                """
        );

        Trio trio = compileSoFar(codes);
        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol target = ensureIsa(unnamed.getTable().get("Target"), TypeSymbol.class);
        ParameterizedSymbol targetConstructor = ensureIsa(target.getTable().get(Symbol.NAME_CONSTRUCTOR + "(spruce.lang.String)"),
                ParameterizedSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0, 0);
        ASTLocalVariableDeclarationStatement localVarDeclStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        assertTrue(localVarDeclStmt.getLocalVarDecl().getVarDeclList().get(0).getVarInitializer().isPresent());
        ASTPrimary primary = ensureIsa(localVarDeclStmt.getLocalVarDecl().getVarDeclList().get(0).getVarInitializer().get(),
                ASTPrimary.class);
        ASTClassInstanceCreationExpression cice = ensureIsa(primary.getChild(), ASTClassInstanceCreationExpression.class);
        ParameterizedSymbol resolved = ensureIsa(cice.getResolvedEntity(),
                ParameterizedSymbol.class);
        assertSame(targetConstructor, resolved);
        assertSame(target, resolved.getDataType());
    }

    /**
     * Test bad CICE of constructor does not exist.
     */
    @Test
    public void testCiceResolutionDNE() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void test() {
                        Target t = new Target("target");
                    }
                }
                class Target {
                    constructor() {}
                }
                """
        );

        ResolverTestUtility.Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 1);
    }

    /**
     * Test bad CICE of ambiguous resolution due to multiple
     * applicable constructors not being override equivalent.
     */
    @Test
    public void testConstructorInvocationResolutionAmbiguous() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void test(SubSub s) {
                        Target t = new Target(s, s);
                    }
                }
                class Target {
                    constructor(Sub s, Sub t) {}
                    constructor(Sub s, Super t) {}
                    constructor(Sub s, SubSub tt) {}
                    constructor(SubSub ss, Sub s) {}
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
