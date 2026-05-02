package org.spruce.compiler.bootstrap.test.resolution;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.bootstrap.ast.classes.*;
import org.spruce.compiler.bootstrap.ast.toplevel.ASTOrdinaryCompilationUnit;
import org.spruce.compiler.bootstrap.ast.types.ASTDataTypeNoArray;
import org.spruce.compiler.bootstrap.resolution.TopLevelResolver;
import org.spruce.compiler.bootstrap.symbol.ParentSymbol;
import org.spruce.compiler.bootstrap.symbol.TypeLookup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spruce.compiler.bootstrap.test.resolution.ResolverTestUtility.*;
import static org.spruce.compiler.bootstrap.test.resolution.ResolverTopLevelTest.getTopLevelResolver;
import static org.spruce.compiler.bootstrap.test.util.TestUtility.ensureIsa;

/**
 * All tests for the classes Resolver.
 */
public class ResolverClassesTest {
    /**
     * Test simple name data type resolution, same namespace.
     */
    @Test
    public void testSimpleTypeResolutionSimple() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Number {}
                """,
                """
                namespace spruce.lang;
                class Integer extends Number {}
                """
        );
        List<ASTOrdinaryCompilationUnit> ocus = parseCodes(codes);
        TypeLookup global = createGlobalSymbolTable(ocus);
        TopLevelResolver resolver = getTopLevelResolver(global);
        resolver.resolveOrdinaryCompilationUnits(ocus);
        ensureNoErrors(global, resolver);

        ParentSymbol spruce = ensureIsa(global.get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        ParentSymbol number = ensureIsa(lang.getTable().get("Number"), ParentSymbol.class);

        ASTClassDeclaration integer = ensureIsa(ocus.get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        assertTrue(integer.getSuperclass().isPresent());
        ASTDataTypeNoArray superclass = integer.getSuperclass().get();
        assertEquals(number, superclass.getResolvedSymbol());
    }

    /**
     * Test simple name data type resolution, child of ancestor.
     */
    @Test
    public void testSimpleDataTypeResolutionChildOfAncestor() {
        List<String> codes = List.of(
                """
                namespace spruce.collections;
                class Map {
                    class Entry {}
                    class SubclassEntry extends Entry {}
                }
                """
        );
        List<ASTOrdinaryCompilationUnit> ocus = parseCodes(codes);
        TypeLookup global = createGlobalSymbolTable(ocus);
        TopLevelResolver resolver = getTopLevelResolver(global);
        resolver.resolveOrdinaryCompilationUnits(ocus);
        ensureNoErrors(global, resolver);

        ParentSymbol spruce = ensureIsa(global.get("spruce"), ParentSymbol.class);
        ParentSymbol collections = ensureIsa(spruce.getTable().get("collections"), ParentSymbol.class);
        ParentSymbol map = ensureIsa(collections.getTable().get("Map"), ParentSymbol.class);
        ParentSymbol entry = ensureIsa(map.getTable().get("Entry"), ParentSymbol.class);

        ASTClassDeclaration mapDecl = ensureIsa(ocus.get(0).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTClassDeclaration subclassEntry = ensureIsa(mapDecl.getClassParts().get(1), ASTClassDeclaration.class);
        assertTrue(subclassEntry.getSuperclass().isPresent());
        ASTDataTypeNoArray superclass = subclassEntry.getSuperclass().get();
        assertEquals(entry, superclass.getResolvedSymbol());
    }

    /**
     * Test simple name data type resolution, use type declaration.
     */
    @Test
    public void testSimpleDataTypeResolutionUseTypeDecl() {
        List<String> codes = List.of(
                """
                namespace spruce.collections;
                class HashMap {}
                """,
                """
                namespace spruce.collections.concurrent;
                use spruce.collections.HashMap;
                class ConcurrentHashMap extends HashMap {}
                """
        );
        List<ASTOrdinaryCompilationUnit> ocus = parseCodes(codes);
        TypeLookup global = createGlobalSymbolTable(ocus);
        TopLevelResolver resolver = getTopLevelResolver(global);
        resolver.resolveOrdinaryCompilationUnits(ocus);
        ensureNoErrors(global, resolver);

        ParentSymbol spruce = ensureIsa(global.get("spruce"), ParentSymbol.class);
        ParentSymbol collections = ensureIsa(spruce.getTable().get("collections"), ParentSymbol.class);
        ParentSymbol hashMap = ensureIsa(collections.getTable().get("HashMap"), ParentSymbol.class);

        ASTClassDeclaration concurrentHashMapDecl = ensureIsa(ocus.get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        assertTrue(concurrentHashMapDecl.getSuperclass().isPresent());
        ASTDataTypeNoArray superclass = concurrentHashMapDecl.getSuperclass().get();
        assertEquals(hashMap, superclass.getResolvedSymbol());
    }

    /**
     * Test simple name data type resolution, use all declaration.
     */
    @Test
    public void testSimpleDataTypeResolutionUseAllDecl() {
        List<String> codes = List.of(
                """
                namespace spruce.collections;
                class HashMap {}
                """,
                """
                namespace spruce.collections.concurrent;
                use spruce.collections.+;
                class ConcurrentHashMap extends HashMap {}
                """
        );
        List<ASTOrdinaryCompilationUnit> ocus = parseCodes(codes);
        TypeLookup global = createGlobalSymbolTable(ocus);
        TopLevelResolver resolver = getTopLevelResolver(global);
        resolver.resolveOrdinaryCompilationUnits(ocus);
        ensureNoErrors(global, resolver);

        ParentSymbol spruce = ensureIsa(global.get("spruce"), ParentSymbol.class);
        ParentSymbol collections = ensureIsa(spruce.getTable().get("collections"), ParentSymbol.class);
        ParentSymbol hashMap = ensureIsa(collections.getTable().get("HashMap"), ParentSymbol.class);

        ASTClassDeclaration concurrentHashMapDecl = ensureIsa(ocus.get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        assertTrue(concurrentHashMapDecl.getSuperclass().isPresent());
        ASTDataTypeNoArray superclass = concurrentHashMapDecl.getSuperclass().get();
        assertEquals(hashMap, superclass.getResolvedSymbol());
    }

    /**
     * Test simple name data type resolution, not found.
     */
    @Test
    public void testSimpleDataTypeResolutionNotFound() {
        List<String> codes = List.of(
                """
                namespace spruce.collections;
                class HashMap {}
                """,
                """
                namespace spruce.collections.concurrent;
                use spruce.collections.+;
                class ConcurrentHashMap extends DoesNotExist {}
                """
        );
        List<ASTOrdinaryCompilationUnit> ocus = parseCodes(codes);
        TypeLookup global = createGlobalSymbolTable(ocus);
        TopLevelResolver resolver = getTopLevelResolver(global);
        resolver.resolveOrdinaryCompilationUnits(ocus);
        expectError(global, resolver);
    }

    /**
     * Test qualified name data type resolution, first name is child of ancestor.
     */
    @Test
    public void testQualifiedDataTypeResolutionChildOfAncestor() {
        List<String> codes = List.of(
                """
                namespace test;
                class Nesting {
                  class Nested {
                    class DeepNested {
                      class DeepDeepNested {
                        class DeepDeepDeepNested {
                        }
                      }
                    }
                    class Dummy extends DeepNested.DeepDeepNested {
                    }
                  }
                }
                """
        );
        List<ASTOrdinaryCompilationUnit> ocus = parseCodes(codes);
        TypeLookup global = createGlobalSymbolTable(ocus);
        TopLevelResolver resolver = getTopLevelResolver(global);
        resolver.resolveOrdinaryCompilationUnits(ocus);
        ensureNoErrors(global, resolver);

        ParentSymbol test = ensureIsa(global.get("test"), ParentSymbol.class);
        ParentSymbol nesting = ensureIsa(test.getTable().get("Nesting"), ParentSymbol.class);
        ParentSymbol nested = ensureIsa(nesting.getTable().get("Nested"), ParentSymbol.class);
        ParentSymbol dn = ensureIsa(nested.getTable().get("DeepNested"), ParentSymbol.class);
        ParentSymbol ddn = ensureIsa(dn.getTable().get("DeepDeepNested"), ParentSymbol.class);

        ASTClassDeclaration nestingDecl = ensureIsa(ocus.get(0).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTClassDeclaration nestedDecl = ensureIsa(nestingDecl.getClassParts().get(0), ASTClassDeclaration.class);
        ASTClassDeclaration dummyDecl = ensureIsa(nestedDecl.getClassParts().get(1), ASTClassDeclaration.class);
        assertTrue(dummyDecl.getSuperclass().isPresent());
        ASTDataTypeNoArray superclass = dummyDecl.getSuperclass().get();
        assertEquals(ddn, superclass.getResolvedSymbol());
    }

    /**
     * Test qualified name data type resolution, first name is an enclosing type of target.
     */
    @Test
    public void testQualifiedDataTypeResolutionEnclosingType() {
        List<String> codes = List.of(
                """
                namespace test;
                class Nesting {
                  class Nested {
                    class DeepNested {
                      class DeepDeepNested {
                        class DeepDeepDeepNested extends Nested.Dummy {
                        }
                      }
                    }
                    class Dummy {
                    }
                  }
                }
                """
        );
        List<ASTOrdinaryCompilationUnit> ocus = parseCodes(codes);
        TypeLookup global = createGlobalSymbolTable(ocus);
        TopLevelResolver resolver = getTopLevelResolver(global);
        resolver.resolveOrdinaryCompilationUnits(ocus);
        ensureNoErrors(global, resolver);

        ParentSymbol test = ensureIsa(global.get("test"), ParentSymbol.class);
        ParentSymbol nesting = ensureIsa(test.getTable().get("Nesting"), ParentSymbol.class);
        ParentSymbol nested = ensureIsa(nesting.getTable().get("Nested"), ParentSymbol.class);
        ParentSymbol dummy = ensureIsa(nested.getTable().get("Dummy"), ParentSymbol.class);

        ASTClassDeclaration nestingDecl = ensureIsa(ocus.get(0).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTClassDeclaration nestedDecl = ensureIsa(nestingDecl.getClassParts().get(0), ASTClassDeclaration.class);
        ASTClassDeclaration dnDecl = ensureIsa(nestedDecl.getClassParts().get(0), ASTClassDeclaration.class);
        ASTClassDeclaration ddnDecl = ensureIsa(dnDecl.getClassParts().get(0), ASTClassDeclaration.class);
        ASTClassDeclaration dddnDecl = ensureIsa(ddnDecl.getClassParts().get(0), ASTClassDeclaration.class);
        assertTrue(dddnDecl.getSuperclass().isPresent());
        ASTDataTypeNoArray superclass = dddnDecl.getSuperclass().get();
        assertEquals(dummy, superclass.getResolvedSymbol());
    }

    /**
     * Test qualified name data type resolution, first name is in use type declaration.
     */
    @Test
    public void testQualifiedDataTypeResolutionUseType() {
        List<String> codes = List.of(
                """
                namespace test;
                class Nesting {
                  class Nested {
                  }
                }
                """,
                """
                namespace other;
                use test.Nesting;
                class Target extends Nesting.Nested {
                }
                """
        );
        List<ASTOrdinaryCompilationUnit> ocus = parseCodes(codes);
        TypeLookup global = createGlobalSymbolTable(ocus);
        TopLevelResolver resolver = getTopLevelResolver(global);
        resolver.resolveOrdinaryCompilationUnits(ocus);
        ensureNoErrors(global, resolver);

        ParentSymbol test = ensureIsa(global.get("test"), ParentSymbol.class);
        ParentSymbol nesting = ensureIsa(test.getTable().get("Nesting"), ParentSymbol.class);
        ParentSymbol nested = ensureIsa(nesting.getTable().get("Nested"), ParentSymbol.class);

        ASTClassDeclaration targetDecl = ensureIsa(ocus.get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        assertTrue(targetDecl.getSuperclass().isPresent());
        ASTDataTypeNoArray superclass = targetDecl.getSuperclass().get();
        assertEquals(nested, superclass.getResolvedSymbol());
    }

    // Test qualified name data type resolution, first name is in use all declaration.
    @Test
    public void testQualifiedDataTypeResolutionUseAll() {
        List<String> codes = List.of(
                """
                namespace test;
                class Nesting {
                  class Nested {
                  }
                }
                """,
                """
                namespace other;
                use test.+;
                class Target extends Nesting.Nested {
                }
                """
        );
        List<ASTOrdinaryCompilationUnit> ocus = parseCodes(codes);
        TypeLookup global = createGlobalSymbolTable(ocus);
        TopLevelResolver resolver = getTopLevelResolver(global);
        resolver.resolveOrdinaryCompilationUnits(ocus);
        ensureNoErrors(global, resolver);

        ParentSymbol test = ensureIsa(global.get("test"), ParentSymbol.class);
        ParentSymbol nesting = ensureIsa(test.getTable().get("Nesting"), ParentSymbol.class);
        ParentSymbol nested = ensureIsa(nesting.getTable().get("Nested"), ParentSymbol.class);

        ASTClassDeclaration targetDecl = ensureIsa(ocus.get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        assertTrue(targetDecl.getSuperclass().isPresent());
        ASTDataTypeNoArray superclass = targetDecl.getSuperclass().get();
        assertEquals(nested, superclass.getResolvedSymbol());
    }

    /**
     * Test qualified name data type resolution, fully qualified name in global symbol table.
     */
    @Test
    public void testQualifiedDataTypeResolutionFullyQualified() {
        List<String> codes = List.of(
                """
                namespace test;
                class Nesting {
                  class Nested {
                  }
                }
                """,
                """
                namespace other;
                class Target extends test.Nesting.Nested {
                }
                """
        );
        List<ASTOrdinaryCompilationUnit> ocus = parseCodes(codes);
        TypeLookup global = createGlobalSymbolTable(ocus);
        TopLevelResolver resolver = getTopLevelResolver(global);
        resolver.resolveOrdinaryCompilationUnits(ocus);
        ensureNoErrors(global, resolver);

        ParentSymbol test = ensureIsa(global.get("test"), ParentSymbol.class);
        ParentSymbol nesting = ensureIsa(test.getTable().get("Nesting"), ParentSymbol.class);
        ParentSymbol nested = ensureIsa(nesting.getTable().get("Nested"), ParentSymbol.class);

        ASTClassDeclaration targetDecl = ensureIsa(ocus.get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        assertTrue(targetDecl.getSuperclass().isPresent());
        ASTDataTypeNoArray superclass = targetDecl.getSuperclass().get();
        assertEquals(nested, superclass.getResolvedSymbol());
    }

    /**
     * Test qualified name data type resolution, first name is not found.
     */
    @Test
    public void testQualifiedDataTypeResolutionFirstNotFound() {
        List<String> codes = List.of(
                """
                namespace test;
                class Nesting {
                  class Nested {
                  }
                }
                """,
                """
                namespace other;
                class Target extends dne.Nesting.Nested {
                }
                """
        );
        List<ASTOrdinaryCompilationUnit> ocus = parseCodes(codes);
        TypeLookup global = createGlobalSymbolTable(ocus);
        TopLevelResolver resolver = getTopLevelResolver(global);
        resolver.resolveOrdinaryCompilationUnits(ocus);
        expectError(global, resolver);
    }

    /**
     * Test qualified name data type resolution, subsequent name not found.
     */
    @Test
    public void testQualifiedDataTypeResolutionSubsequentNotFound() {
        List<String> codes = List.of(
                """
                namespace test;
                class Nesting {
                  class Nested {
                  }
                }
                """,
                """
                namespace other;
                class Target extends test.Nesting.DoesNotExist {
                }
                """
        );
        List<ASTOrdinaryCompilationUnit> ocus = parseCodes(codes);
        TypeLookup global = createGlobalSymbolTable(ocus);
        TopLevelResolver resolver = getTopLevelResolver(global);
        resolver.resolveOrdinaryCompilationUnits(ocus);
        expectError(global, resolver);
    }

    /**
     * Test qualified name data type resolution, but resolved to a namespace, not a type.
     */
    @Test
    public void testQualifiedDataTypeResolutionNamespaceNotType() {
        List<String> codes = List.of(
                """
                namespace test.testing;
                class Nesting {
                }
                """,
                """
                namespace other;
                class Target extends test.testing {
                }
                """
        );
        List<ASTOrdinaryCompilationUnit> ocus = parseCodes(codes);
        TypeLookup global = createGlobalSymbolTable(ocus);
        TopLevelResolver resolver = getTopLevelResolver(global);
        resolver.resolveOrdinaryCompilationUnits(ocus);
        expectError(global, resolver);
    }

    // DataTypeNoArray resolution should be fully tested with all cases above.
    // DataType resolution, tested below, uses DTNA resolution, so just make
    // sure things get resolved or give an error as expected.

    /**
     * Tests field data type resolution.
     */
    @Test
    public void testFieldDataTypeResolution() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Integer {}
                """,
                """
                use spruce.lang.Integer;
                class Test {
                    Integer i = 1;
                }
                """
        );
        List<ASTOrdinaryCompilationUnit> ocus = parseCodes(codes);
        TypeLookup global = createGlobalSymbolTable(ocus);
        TopLevelResolver resolver = getTopLevelResolver(global);
        resolver.resolveOrdinaryCompilationUnits(ocus);
        ensureNoErrors(global, resolver);

        ParentSymbol spruce = ensureIsa(global.get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        ParentSymbol integer = ensureIsa(lang.getTable().get("Integer"), ParentSymbol.class);

        ASTClassDeclaration testDecl = ensureIsa(ocus.get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTFieldDeclaration integerField = ensureIsa(testDecl.getClassParts().getTypedChildren().get(0),
                ASTFieldDeclaration.class);
        assertEquals(integer, integerField.getVarDeclList().get(0).getResolvedSymbol());
    }

    /**
     * Tests field bad data type resolution.
     */
    @Test
    public void testFieldBadDataTypeResolution() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Integer {}
                """,
                """
                use spruce.lang.Integer;
                class Test {
                    DoesNotExist i = 1;
                }
                """
        );
        List<ASTOrdinaryCompilationUnit> ocus = parseCodes(codes);
        TypeLookup global = createGlobalSymbolTable(ocus);
        TopLevelResolver resolver = getTopLevelResolver(global);
        resolver.resolveOrdinaryCompilationUnits(ocus);
        expectError(global, resolver);
    }

    /**
     * Tests method result data type resolution.
     */
    @Test
    public void testMethodResultDataTypeResolution() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Integer {}
                """,
                """
                use spruce.lang.Integer;
                class Test {
                    Integer test() {
                        return 1;
                    }
                }
                """
        );
        List<ASTOrdinaryCompilationUnit> ocus = parseCodes(codes);
        TypeLookup global = createGlobalSymbolTable(ocus);
        TopLevelResolver resolver = getTopLevelResolver(global);
        resolver.resolveOrdinaryCompilationUnits(ocus);
        ensureNoErrors(global, resolver);

        ParentSymbol spruce = ensureIsa(global.get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        ParentSymbol integer = ensureIsa(lang.getTable().get("Integer"), ParentSymbol.class);

        ASTClassDeclaration testDecl = ensureIsa(ocus.get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethod = ensureIsa(testDecl.getClassParts().getTypedChildren().get(0),
                ASTMethodDeclaration.class);
        assertEquals(integer, testMethod.getHeader().getResult().getResolvedSymbol());
    }

    /**
     * Tests method void result data type resolution.
     */
    @Test
    public void testMethodVoidResultDataTypeResolution() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Integer {}
                """,
                """
                use spruce.lang.Integer;
                class Test {
                    void test() {}
                }
                """
        );
        List<ASTOrdinaryCompilationUnit> ocus = parseCodes(codes);
        TypeLookup global = createGlobalSymbolTable(ocus);
        TopLevelResolver resolver = getTopLevelResolver(global);
        resolver.resolveOrdinaryCompilationUnits(ocus);
        ensureNoErrors(global, resolver);

        ASTClassDeclaration testDecl = ensureIsa(ocus.get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethod = ensureIsa(testDecl.getClassParts().getTypedChildren().get(0),
                ASTMethodDeclaration.class);
        assertEquals(ParentSymbol.VOID, testMethod.getHeader().getResult().getResolvedSymbol());
    }

    /**
     * Tests method bad result data type resolution.
     */
    @Test
    public void testMethodBadResultDataTypeResolution() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Integer {}
                """,
                """
                use spruce.lang.Integer;
                class Test {
                    DoesNotExist test() {
                        return new DoesNotExist();
                    }
                }
                """
        );
        List<ASTOrdinaryCompilationUnit> ocus = parseCodes(codes);
        TypeLookup global = createGlobalSymbolTable(ocus);
        TopLevelResolver resolver = getTopLevelResolver(global);
        resolver.resolveOrdinaryCompilationUnits(ocus);
        expectError(global, resolver);
    }

    /**
     * Tests formal parameter data type resolution.
     */
    @Test
    public void testFormalParameterDataTypeResolution() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Integer {}
                class String {}
                """,
                """
                use spruce.lang.{Integer, String};
                class Test {
                    String test(Integer x, String y) {
                        return x + y;
                    }
                }
                """
        );
        List<ASTOrdinaryCompilationUnit> ocus = parseCodes(codes);
        TypeLookup global = createGlobalSymbolTable(ocus);
        TopLevelResolver resolver = getTopLevelResolver(global);
        resolver.resolveOrdinaryCompilationUnits(ocus);
        ensureNoErrors(global, resolver);

        ParentSymbol spruce = ensureIsa(global.get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        ParentSymbol integer = ensureIsa(lang.getTable().get("Integer"), ParentSymbol.class);
        ParentSymbol string = ensureIsa(lang.getTable().get("String"), ParentSymbol.class);

        ASTClassDeclaration testDecl = ensureIsa(ocus.get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethod = ensureIsa(testDecl.getClassParts().getTypedChildren().get(0),
                ASTMethodDeclaration.class);
        ASTFormalParameterList params = testMethod.getHeader().getMethodDecl().getFormalParamList();
        assertEquals(integer, params.get(0).getDataType().getResolvedSymbol());
        assertEquals(string, params.get(1).getDataType().getResolvedSymbol());
    }

    /**
     * Tests formal parameter bad data type resolution.
     */
    @Test
    public void testFormalParameterBadDataTypeResolution() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Integer {}
                class String {}
                """,
                """
                use spruce.lang.{Integer, String};
                class Test {
                    constructor(DNE x) {
                    }
                    void test(DoesNotExist y) {
                    }
                }
                """
        );
        List<ASTOrdinaryCompilationUnit> ocus = parseCodes(codes);
        TypeLookup global = createGlobalSymbolTable(ocus);
        TopLevelResolver resolver = getTopLevelResolver(global);
        resolver.resolveOrdinaryCompilationUnits(ocus);
        expectError(global, resolver, 2);
    }

    /**
     * Tests formal parameter data type resolution.
     */
    @Test
    public void testConstructorFormalParameterDataTypeResolution() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Integer {}
                class String {}
                """,
                """
                use spruce.lang.{Integer, String};
                class Test {
                    constructor(Integer x, String y) {
                    }
                }
                """
        );
        List<ASTOrdinaryCompilationUnit> ocus = parseCodes(codes);
        TypeLookup global = createGlobalSymbolTable(ocus);
        TopLevelResolver resolver = getTopLevelResolver(global);
        resolver.resolveOrdinaryCompilationUnits(ocus);
        ensureNoErrors(global, resolver);

        ParentSymbol spruce = ensureIsa(global.get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        ParentSymbol integer = ensureIsa(lang.getTable().get("Integer"), ParentSymbol.class);
        ParentSymbol string = ensureIsa(lang.getTable().get("String"), ParentSymbol.class);

        ASTClassDeclaration testDecl = ensureIsa(ocus.get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTConstructorDeclaration constr = ensureIsa(testDecl.getClassParts().getTypedChildren().get(0),
                ASTConstructorDeclaration.class);
        ASTFormalParameterList params = constr.getConstructorDecl().getFormalParamList();
        assertEquals(integer, params.get(0).getDataType().getResolvedSymbol());
        assertEquals(string, params.get(1).getDataType().getResolvedSymbol());
    }

    // TODO: Semantic analysis testing: superclass cycles,
    // constructor resolution, method resolution, etc.
}
