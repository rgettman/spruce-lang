package org.spruce.compiler.bootstrap.test.resolution;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.bootstrap.ast.classes.ASTClassDeclaration;
import org.spruce.compiler.bootstrap.ast.classes.ASTConstructorDeclaration;
import org.spruce.compiler.bootstrap.ast.statements.ASTConstructorInvocation;
import org.spruce.compiler.bootstrap.resolution.ConstructorsResolver;
import org.spruce.compiler.bootstrap.resolution.InvocationResolver;
import org.spruce.compiler.bootstrap.resolution.ResolutionContext;
import org.spruce.compiler.bootstrap.symbol.ParameterizedSymbol;
import org.spruce.compiler.bootstrap.symbol.ParentSymbol;
import org.spruce.compiler.bootstrap.symbol.Symbol;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;

import static org.junit.jupiter.api.Assertions.*;
import static org.spruce.compiler.bootstrap.symbol.GlobalLookup.UNNAMED_NAMESPACE_NAME;
import static org.spruce.compiler.bootstrap.test.resolution.ResolverTestUtility.*;
import static org.spruce.compiler.bootstrap.test.util.TestUtility.ensureIsa;

public class ResolverConstructorsTest {
    /**
     * Tests bad argument list in constructor invocation.
     */
    @Test
    public void testConstructorInvocationBadArguments() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Super {
                    constructor(Integer i) {}
                }
                class Test extends Super {
                    constructor() {
                        super(i);
                    }
                }
                """
        );
        ResolverTestUtility.Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 1);
    }

    /**
     * Tests constructor invocation step 1 - type to search.  Self case (1a),
     * find the type.
     */
    @Test
    public void testConstructorInvocationTypeToSearchSelf() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    constructor() {
                        self(1);
                    }
                    constructor(Integer i) {}
                }
                """
        );

        ResolverTestUtility.Trio trio = resolveAllButMembers(codes);

        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol test = ensureIsa(unnamed.getTable().get("Test"), TypeSymbol.class);

        ASTClassDeclaration testDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0),
                ASTClassDeclaration.class);
        ASTConstructorDeclaration constrDecl = ensureIsa(testDecl.getClassParts().get(0), ASTConstructorDeclaration.class);
        ASTConstructorInvocation constrInvocation = ensureIsa(
                getBlockStatement(constrDecl, 0), ASTConstructorInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(1).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(constrDecl.getDeclSymbol());
        ConstructorsResolver opResolver = trio.resolver().getConstructorsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(constrInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        assertSame(test, optTypeToSearch.get());
    }

    /**
     * Tests constructor invocation step 1 - type to search.  Super case (1b),
     * find the superclass.
     */
    @Test
    public void testConstructorInvocationTypeToSearchSuper() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Super {
                    constructor() {}
                }
                class Test extends Super {
                    constructor() {
                        super();
                    }
                }
                """
        );

        Trio trio = resolveAllButMembers(codes);

        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol sooper = ensureIsa(unnamed.getTable().get("Super"), TypeSymbol.class);

        ASTClassDeclaration testDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(1),
                ASTClassDeclaration.class);
        ASTConstructorDeclaration constrDecl = ensureIsa(testDecl.getClassParts().get(0), ASTConstructorDeclaration.class);
        ASTConstructorInvocation constrInvocation = ensureIsa(
                getBlockStatement(constrDecl, 0), ASTConstructorInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(1).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(constrDecl.getDeclSymbol());
        ConstructorsResolver opResolver = trio.resolver().getConstructorsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(constrInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        assertSame(sooper, optTypeToSearch.get());
    }

    /**
     * Tests constructor invocation step 1 - type to search.  Super case (1b),
     * but there is no superclass.
     */
    @Test
    public void testConstructorInvocationTypeToSearchSuperNoSuperclass() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {
                    constructor() {
                        super();
                    }
                }
                """
        );

        Trio trio = resolveAllButMembers(codes);

        ASTConstructorDeclaration constrDecl = getConstructor(trio, 0, 0, 0);
        ASTConstructorInvocation constrInvocation = ensureIsa(
                getBlockStatement(constrDecl, 0), ASTConstructorInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(0).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(constrDecl.getDeclSymbol());
        ConstructorsResolver opResolver = trio.resolver().getConstructorsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(constrInvocation, testMethodCtx);
        assertFalse(optTypeToSearch.isPresent(), "Should not have found type to search!");
    }

    /**
     * Tests constructor invocation step 2 - applicable constructors.
     */
    @Test
    public void testConstructorInvocationApplicableConstructors() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    constructor() {
                        self(2);
                    }
                    constructor(String s) {}
                    constructor(Integer i, Integer j) {}
                    constructor(Integer i) {}
                    void bar(Integer i) {}
                    void foo(Integer i) {}
                }
                """
        );

        Trio trio = compileSoFar(codes);
        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol test = ensureIsa(unnamed.getTable().get("Test"), TypeSymbol.class);

        ASTConstructorDeclaration constrDecl = getConstructor(trio, 1, 0, 0);
        ASTConstructorInvocation constrInvocation = ensureIsa(
                getBlockStatement(constrDecl, 0), ASTConstructorInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(1).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(constrDecl.getDeclSymbol());
        ConstructorsResolver opResolver = trio.resolver().getConstructorsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(constrInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        TypeSymbol typeToSearch = optTypeToSearch.get();
        assertSame(test, typeToSearch);
        Set<ParameterizedSymbol> applicableConstructors =
                opResolver.getPotentiallyApplicable(constrInvocation, typeToSearch);
        assertEquals(1, applicableConstructors.size());
        List<String> names = applicableConstructors.stream().map(Symbol::getName).toList();
        assertEquals(Symbol.NAME_CONSTRUCTOR + "(spruce.lang.Integer)", names.get(0));
    }

    /**
     * Tests constructor invocation step 2 - many applicable constructors.
     */
    @Test
    public void testConstructorInvocationManyApplicableConstructors() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    constructor(Sub s) {
                        self(s, s);
                    }
                    constructor(Sub s, Super t) {}
                    constructor(Super t, Sub s) {}
                    constructor(Super t1, Super t2) {}
                    constructor(Sub s1, Sub s2) {}
                    constructor(Integer i) {}
                }
                class Super {}
                class Sub extends Super {}
                """
        );

        Trio trio = compileSoFar(codes);
        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol test = ensureIsa(unnamed.getTable().get("Test"), TypeSymbol.class);

        ASTConstructorDeclaration constrDecl = getConstructor(trio, 1, 0, 0);
        ASTConstructorInvocation constrInvocation = ensureIsa(
                getBlockStatement(constrDecl, 0), ASTConstructorInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(1).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(constrDecl.getDeclSymbol());
        ConstructorsResolver opResolver = trio.resolver().getConstructorsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(constrInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        TypeSymbol typeToSearch = optTypeToSearch.get();
        assertSame(test, typeToSearch);
        Set<ParameterizedSymbol> applicableConstructors =
                opResolver.getPotentiallyApplicable(constrInvocation, typeToSearch);
        assertEquals(4, applicableConstructors.size());
        List<String> names = applicableConstructors.stream().map(Symbol::getName).toList();
        assertTrue(names.contains(Symbol.NAME_CONSTRUCTOR + "(Sub, Super)"), "(Sub, Super) not found!");
        assertTrue(names.contains(Symbol.NAME_CONSTRUCTOR + "(Super, Sub)"), "(Super, Sub) not found!");
        assertTrue(names.contains(Symbol.NAME_CONSTRUCTOR + "(Sub, Sub)"), "(Sub, Sub) not found!");
        assertTrue(names.contains(Symbol.NAME_CONSTRUCTOR + "(Super, Super)"), "(Super, Super) not found!");
    }

    /**
     * Tests constructor invocation step 2 - no applicable constructors.
     */
    @Test
    public void testConstructorInvocationNoApplicableConstructors() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    constructor() {
                        self(2);
                    }
                    constructor(Sub s, Super t) {}
                    constructor(Super t, Sub s) {}
                    constructor(Super t1, Super t2) {}
                    constructor(Sub s1, Sub s2) {}
                    constructor(String s) {}
                }
                class Super {}
                class Sub extends Super {}
                """
        );

        Trio trio = compileSoFar(codes);
        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol test = ensureIsa(unnamed.getTable().get("Test"), TypeSymbol.class);

        ASTConstructorDeclaration constrDecl = getConstructor(trio, 1, 0, 0);
        ASTConstructorInvocation constrInvocation = ensureIsa(
                getBlockStatement(constrDecl, 0), ASTConstructorInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(1).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(constrDecl.getDeclSymbol());
        ConstructorsResolver opResolver = trio.resolver().getConstructorsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(constrInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        TypeSymbol typeToSearch = optTypeToSearch.get();
        assertSame(test, typeToSearch);
        Set<ParameterizedSymbol> applicableConstructors =
                opResolver.getPotentiallyApplicable(constrInvocation, typeToSearch);
        assertEquals(0, applicableConstructors.size());
    }

    /**
     * Tests constructor invocation step 3 - many maximally applicable constructors.
     */
    @Test
    public void testConstructorInvocationMultipleMaximallySpecific() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    constructor(Sub s) {
                        self(s, s);
                    }
                    constructor(Sub s, Super t) {}
                    constructor(Super t, Sub s) {}
                    constructor(String s) {}
                }
                class Super {}
                class Sub extends Super {}
                """
        );

        Trio trio = compileSoFar(codes);
        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol test = ensureIsa(unnamed.getTable().get("Test"), TypeSymbol.class);

        ASTConstructorDeclaration constrDecl = getConstructor(trio, 1, 0, 0);
        ASTConstructorInvocation constrInvocation = ensureIsa(
                getBlockStatement(constrDecl, 0), ASTConstructorInvocation.class);

        ResolutionContext ocuCtx = trio.ocus().get(1).getCtx();
        ResolutionContext testMethodCtx = ocuCtx.withEnclosingSymbol(constrDecl.getDeclSymbol());
        ConstructorsResolver opResolver = trio.resolver().getConstructorsResolver();
        Optional<TypeSymbol> optTypeToSearch = opResolver.getTypeToSearch(constrInvocation, testMethodCtx);
        assertTrue(optTypeToSearch.isPresent(), "Type to search not found!");
        TypeSymbol typeToSearch = optTypeToSearch.get();
        assertSame(test, typeToSearch);
        Set<ParameterizedSymbol> applicableConstructors =
                opResolver.getPotentiallyApplicable(constrInvocation, typeToSearch);
        assertEquals(2, applicableConstructors.size());
        List<ParameterizedSymbol> maxSpecificConstructors =
                opResolver.getMaximallySpecific(applicableConstructors);
        assertEquals(2, maxSpecificConstructors.size());
        ParameterizedSymbol first = maxSpecificConstructors.get(0);
        ParameterizedSymbol second = maxSpecificConstructors.get(1);
        // Could be in any order.
        if ((Symbol.NAME_CONSTRUCTOR + "(Sub, Super)").equals(first.getName())) {
            assertEquals(Symbol.NAME_CONSTRUCTOR + "(Super, Sub)", second.getName());
        }
        else {
            assertEquals(Symbol.NAME_CONSTRUCTOR + "(Super, Sub)", first.getName());
            assertEquals(Symbol.NAME_CONSTRUCTOR + "(Sub, Super)", second.getName());
        }
    }

    // When testing overall constructor resolution, only test the high-level results
    // instead of each individual case, which should already be tested above.

    /**
     * Tests constructor resolution, one match.
     */
    @Test
    public void testConstructorInvocationResolutionMatch() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test extends Superclass {
                    constructor(SubSub s) {
                        super(s, s);
                    }
                }
                abstract class Superclass {
                    constructor(SubSub ss, SubSub tt) {}
                }
                class Super {}
                class Sub extends Super {}
                class SubSub extends Sub {}
                """
        );

        Trio trio = compileSoFar(codes);
        ParentSymbol unnamed = ensureIsa(trio.global().get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol superclass = ensureIsa(unnamed.getTable().get("Superclass"), TypeSymbol.class);
        ParameterizedSymbol superConstructor = ensureIsa(superclass.getTable().get(Symbol.NAME_CONSTRUCTOR + "(SubSub, SubSub)"),
                ParameterizedSymbol.class);

        ASTConstructorDeclaration methodDecl = getConstructor(trio, 1, 0, 0);
        ASTConstructorInvocation constrInvocation = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTConstructorInvocation.class);
        ParameterizedSymbol resolved = ensureIsa(constrInvocation.getResolvedEntity(),
                ParameterizedSymbol.class);
        assertSame(superConstructor, resolved);
        assertSame(superclass, resolved.getDataType());
    }

    /**
     * Test bad constructor invocation of does not exist.
     */
    @Test
    public void testConstructorInvocationResolutionDNE() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    constructor(SubSub s) {
                        self(s, s);
                    }
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
     * Test bad constructor invocation of ambiguous resolution due to multiple
     * applicable constructors not being override equivalent.
     */
    @Test
    public void testConstructorInvocationResolutionAmbiguous() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    constructor(SubSub s) {
                        foo(s, s);
                    }
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
