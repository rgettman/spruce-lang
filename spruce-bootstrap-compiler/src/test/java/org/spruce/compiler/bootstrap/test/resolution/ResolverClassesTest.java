package org.spruce.compiler.bootstrap.test.resolution;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.bootstrap.ast.classes.*;

import org.spruce.compiler.bootstrap.ast.types.ASTDataTypeNoArray;
import org.spruce.compiler.bootstrap.ast.types.ASTDataTypeNoArrayList;
import org.spruce.compiler.bootstrap.symbol.ParentSymbol;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;

import static org.junit.jupiter.api.Assertions.*;
import static org.spruce.compiler.bootstrap.test.resolution.ResolverTestUtility.*;
import static org.spruce.compiler.bootstrap.test.util.TestUtility.ensureIsa;

/**
 * All tests for the classes resolver.
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
                class Any {}
                class Number {}
                """,
                """
                namespace spruce.lang;
                class Integer extends Number {}
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        ParentSymbol number = ensureIsa(lang.getTable().get("Number"), ParentSymbol.class);

        ASTClassDeclaration integer = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        assertTrue(integer.getSuperclass().isPresent());
        ASTDataTypeNoArray superclass = integer.getSuperclass().get();
        assertEquals(number, superclass.getResolvedDataType());
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
                """,
                """
                namespace spruce.lang;
                class Any {}
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol collections = ensureIsa(spruce.getTable().get("collections"), ParentSymbol.class);
        ParentSymbol map = ensureIsa(collections.getTable().get("Map"), ParentSymbol.class);
        ParentSymbol entry = ensureIsa(map.getTable().get("Entry"), ParentSymbol.class);

        ASTClassDeclaration mapDecl = ensureIsa(trio.ocus().get(0).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTClassDeclaration subclassEntry = ensureIsa(mapDecl.getClassParts().get(1), ASTClassDeclaration.class);
        assertTrue(subclassEntry.getSuperclass().isPresent());
        ASTDataTypeNoArray superclass = subclassEntry.getSuperclass().get();
        assertEquals(entry, superclass.getResolvedDataType());
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
                """,
                """
                namespace spruce.lang;
                class Any {}
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol collections = ensureIsa(spruce.getTable().get("collections"), ParentSymbol.class);
        ParentSymbol hashMap = ensureIsa(collections.getTable().get("HashMap"), ParentSymbol.class);

        ASTClassDeclaration concurrentHashMapDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        assertTrue(concurrentHashMapDecl.getSuperclass().isPresent());
        ASTDataTypeNoArray superclass = concurrentHashMapDecl.getSuperclass().get();
        assertEquals(hashMap, superclass.getResolvedDataType());
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
                """,
                """
                namespace spruce.lang;
                class Any {}
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol collections = ensureIsa(spruce.getTable().get("collections"), ParentSymbol.class);
        ParentSymbol hashMap = ensureIsa(collections.getTable().get("HashMap"), ParentSymbol.class);

        ASTClassDeclaration concurrentHashMapDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        assertTrue(concurrentHashMapDecl.getSuperclass().isPresent());
        ASTDataTypeNoArray superclass = concurrentHashMapDecl.getSuperclass().get();
        assertEquals(hashMap, superclass.getResolvedDataType());
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
                """,
                """
                namespace spruce.lang;
                class Any {}
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
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
                """,
                """
                namespace spruce.lang;
                class Any {}
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol test = ensureIsa(trio.global().get("test"), ParentSymbol.class);
        ParentSymbol nesting = ensureIsa(test.getTable().get("Nesting"), ParentSymbol.class);
        ParentSymbol nested = ensureIsa(nesting.getTable().get("Nested"), ParentSymbol.class);
        ParentSymbol dn = ensureIsa(nested.getTable().get("DeepNested"), ParentSymbol.class);
        ParentSymbol ddn = ensureIsa(dn.getTable().get("DeepDeepNested"), ParentSymbol.class);

        ASTClassDeclaration nestingDecl = ensureIsa(trio.ocus().get(0).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTClassDeclaration nestedDecl = ensureIsa(nestingDecl.getClassParts().get(0), ASTClassDeclaration.class);
        ASTClassDeclaration dummyDecl = ensureIsa(nestedDecl.getClassParts().get(1), ASTClassDeclaration.class);
        assertTrue(dummyDecl.getSuperclass().isPresent());
        ASTDataTypeNoArray superclass = dummyDecl.getSuperclass().get();
        assertEquals(ddn, superclass.getResolvedDataType());
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
                """,
                """
                namespace spruce.lang;
                class Any {}
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol test = ensureIsa(trio.global().get("test"), ParentSymbol.class);
        ParentSymbol nesting = ensureIsa(test.getTable().get("Nesting"), ParentSymbol.class);
        ParentSymbol nested = ensureIsa(nesting.getTable().get("Nested"), ParentSymbol.class);
        ParentSymbol dummy = ensureIsa(nested.getTable().get("Dummy"), ParentSymbol.class);

        ASTClassDeclaration nestingDecl = ensureIsa(trio.ocus().get(0).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTClassDeclaration nestedDecl = ensureIsa(nestingDecl.getClassParts().get(0), ASTClassDeclaration.class);
        ASTClassDeclaration dnDecl = ensureIsa(nestedDecl.getClassParts().get(0), ASTClassDeclaration.class);
        ASTClassDeclaration ddnDecl = ensureIsa(dnDecl.getClassParts().get(0), ASTClassDeclaration.class);
        ASTClassDeclaration dddnDecl = ensureIsa(ddnDecl.getClassParts().get(0), ASTClassDeclaration.class);
        assertTrue(dddnDecl.getSuperclass().isPresent());
        ASTDataTypeNoArray superclass = dddnDecl.getSuperclass().get();
        assertEquals(dummy, superclass.getResolvedDataType());
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
                """,
                """
                namespace spruce.lang;
                class Any {}
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol test = ensureIsa(trio.global().get("test"), ParentSymbol.class);
        ParentSymbol nesting = ensureIsa(test.getTable().get("Nesting"), ParentSymbol.class);
        ParentSymbol nested = ensureIsa(nesting.getTable().get("Nested"), ParentSymbol.class);

        ASTClassDeclaration targetDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        assertTrue(targetDecl.getSuperclass().isPresent());
        ASTDataTypeNoArray superclass = targetDecl.getSuperclass().get();
        assertEquals(nested, superclass.getResolvedDataType());
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
                """,
                """
                namespace spruce.lang;
                class Any {}
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol test = ensureIsa(trio.global().get("test"), ParentSymbol.class);
        ParentSymbol nesting = ensureIsa(test.getTable().get("Nesting"), ParentSymbol.class);
        ParentSymbol nested = ensureIsa(nesting.getTable().get("Nested"), ParentSymbol.class);

        ASTClassDeclaration targetDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        assertTrue(targetDecl.getSuperclass().isPresent());
        ASTDataTypeNoArray superclass = targetDecl.getSuperclass().get();
        assertEquals(nested, superclass.getResolvedDataType());
    }

    /**
     * Test qualified name data type resolution, fully qualified name in trio.global() symbol table.
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
                """,
                """
                namespace spruce.lang;
                class Any {}
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol test = ensureIsa(trio.global().get("test"), ParentSymbol.class);
        ParentSymbol nesting = ensureIsa(test.getTable().get("Nesting"), ParentSymbol.class);
        ParentSymbol nested = ensureIsa(nesting.getTable().get("Nested"), ParentSymbol.class);

        ASTClassDeclaration targetDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        assertTrue(targetDecl.getSuperclass().isPresent());
        ASTDataTypeNoArray superclass = targetDecl.getSuperclass().get();
        assertEquals(nested, superclass.getResolvedDataType());
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
                """,
                """
                namespace spruce.lang;
                class Any {}
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
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
                """,
                """
                namespace spruce.lang;
                class Any {}
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
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
                """,
                """
                namespace spruce.lang;
                class Any {}
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Test class superinterface resolution.
     */
    @Test
    public void testClassSuperinterfaceResolution() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                interface Copyable {}
                """,
                """
                namespace spruce.lang;
                class String implements Copyable {}
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        ParentSymbol copyable = ensureIsa(lang.getTable().get("Copyable"), ParentSymbol.class);

        ASTClassDeclaration string = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        assertFalse(string.getSuperclass().isPresent());
        Optional<ASTDataTypeNoArrayList> optDtnaSuperinterfaces = string.getSuperinterfaces();
        assertTrue(optDtnaSuperinterfaces.isPresent());
        ASTDataTypeNoArrayList dtnaSuperinterfaces = optDtnaSuperinterfaces.get();
        assertEquals(1, dtnaSuperinterfaces.getTypedChildren().size());
        ASTDataTypeNoArray dtnaSuperinterface = dtnaSuperinterfaces.get(0);
        assertEquals(copyable, dtnaSuperinterface.getResolvedDataType());
    }

    /**
     * Test bad duplicate superinterfaces.
     */
    @Test
    public void testBadInterfaceSuperinterfaceDuplicate() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                interface Copyable {}
                """,
                """
                namespace spruce.lang;
                class String implements Copyable, Copyable {}
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests bad class extending an interface.
     */
    @Test
    public void testBadClassExtendsInterface() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                interface Fooable {}
                """,
                """
                namespace spruce.lang;
                class Test extends Fooable {}
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests bad class implementing another class.
     */
    @Test
    public void testBadClassImplementsClass() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class SomethingElse {}
                """,
                """
                namespace spruce.lang;
                class Test implements SomethingElse {}
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Test interface superinterface resolution.
     */
    @Test
    public void testInterfaceSuperinterfaceResolution() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                interface Fooable {}
                interface Barable {}
                """,
                """
                namespace spruce.lang;
                interface Bazable extends Fooable, Barable {}
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        ParentSymbol fooable = ensureIsa(lang.getTable().get("Fooable"), ParentSymbol.class);
        ParentSymbol barable = ensureIsa(lang.getTable().get("Barable"), ParentSymbol.class);

        ASTInterfaceDeclaration bazable = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTInterfaceDeclaration.class);
        Optional<ASTDataTypeNoArrayList> optDtnaSuperinterfaces = bazable.getExtendsInterfaces();
        assertTrue(optDtnaSuperinterfaces.isPresent());
        ASTDataTypeNoArrayList dtnaSuperinterfaces = optDtnaSuperinterfaces.get();
        assertEquals(2, dtnaSuperinterfaces.getTypedChildren().size());
        ASTDataTypeNoArray dtnaSi0 = dtnaSuperinterfaces.get(0);
        assertEquals(fooable, dtnaSi0.getResolvedDataType());
        ASTDataTypeNoArray dtnaSi1 = dtnaSuperinterfaces.get(1);
        assertEquals(barable, dtnaSi1.getResolvedDataType());
    }

    /**
     * Tests bad interface extending a class.
     */
    @Test
    public void testBadInterfaceImplementsClass() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class SomeClass {}
                """,
                """
                interface Test extends SomeClass {}
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests bad class extends itself.
     */
    @Test
    public void testBadClassExtendsItself() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                """,
                """
                class Test extends Test {}
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests bad class extends itself indirectly.
     */
    @Test
    public void testBadClassExtendsItselfIndirectly() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                """,
                """
                class A extends B {}
                class B extends A {}
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 2);
    }

    /**
     * Tests bad interface extends itself.
     */
    @Test
    public void testBadInterfaceExtendsItself() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                """,
                """
                interface Test extends Test {}
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests bad interface extends itself indirectly.
     */
    @Test
    public void testBadInterfaceExtendsItselfIndirectly() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                """,
                """
                interface Other {}
                interface A extends Other, B {}
                interface B extends A, Other {}
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 2);
    }

    /**
     * Tests bad type dependent on itself through an enclosing type.
     */
    @Test
    public void testBadTypeDependencyThroughEnclosingType() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                """,
                """
                class A implements B.InnerB {
                    class InnerA {}
                }
                class B extends A.InnerA {
                    interface InnerB {}
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 2);
    }

    //
    // Types above, members below.
    //

    /**
     * Tests field data type resolution.
     */
    @Test
    public void testFieldDataTypeResolution() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                """,
                """
                use spruce.lang.Integer;
                class Test {
                    Integer i = 1;
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        ParentSymbol integer = ensureIsa(lang.getTable().get("Integer"), ParentSymbol.class);

        ASTClassDeclaration testDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTFieldDeclaration integerField = ensureIsa(testDecl.getClassParts().getTypedChildren().get(0),
                ASTFieldDeclaration.class);
        assertEquals(integer, integerField.getVarDeclList().get(0).getDeclSymbol().getDataType());
    }

    /**
     * Tests field bad data type resolution.
     */
    @Test
    public void testFieldBadDataTypeResolution() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                """,
                """
                use spruce.lang.Integer;
                class Test {
                    DoesNotExist i = 1;
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests method result data type resolution.
     */
    @Test
    public void testMethodResultDataTypeResolution() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
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
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        ParentSymbol integer = ensureIsa(lang.getTable().get("Integer"), ParentSymbol.class);

        ASTClassDeclaration testDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethod = ensureIsa(testDecl.getClassParts().getTypedChildren().get(0),
                ASTMethodDeclaration.class);
        assertEquals(integer, testMethod.getHeader().getResult().getResolvedDataType());
    }

    /**
     * Tests method void result data type resolution.
     */
    @Test
    public void testMethodVoidResultDataTypeResolution() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                """,
                """
                use spruce.lang.Integer;
                class Test {
                    void test() {}
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ASTClassDeclaration testDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethod = ensureIsa(testDecl.getClassParts().getTypedChildren().get(0),
                ASTMethodDeclaration.class);
        assertEquals(TypeSymbol.VOID, testMethod.getHeader().getResult().getResolvedDataType());
    }

    /**
     * Tests method bad result data type resolution.
     */
    @Test
    public void testMethodBadResultDataTypeResolution() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
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
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 2);
    }

    /**
     * Tests formal parameter data type resolution.
     */
    @Test
    public void testFormalParameterDataTypeResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                use spruce.lang.{Integer, String};
                class Test {
                    String test(Integer x, String y) {
                        return x + y;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        ParentSymbol integer = ensureIsa(lang.getTable().get("Integer"), ParentSymbol.class);
        ParentSymbol string = ensureIsa(lang.getTable().get("String"), ParentSymbol.class);

        ASTClassDeclaration testDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethod = ensureIsa(testDecl.getClassParts().getTypedChildren().get(0),
                ASTMethodDeclaration.class);
        ASTFormalParameterList params = testMethod.getHeader().getMethodDecl().getFormalParamList();
        assertEquals(integer, params.get(0).getDataType().getResolvedDataType());
        assertEquals(string, params.get(1).getDataType().getResolvedDataType());
    }

    /**
     * Tests formal parameter bad data type resolution.
     */
    @Test
    public void testFormalParameterBadDataTypeResolution() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
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
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 2);
    }

    /**
     * Tests formal parameter data type resolution.
     */
    @Test
    public void testConstructorFormalParameterDataTypeResolution() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
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
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        ParentSymbol integer = ensureIsa(lang.getTable().get("Integer"), ParentSymbol.class);
        ParentSymbol string = ensureIsa(lang.getTable().get("String"), ParentSymbol.class);

        ASTClassDeclaration testDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTConstructorDeclaration constr = ensureIsa(testDecl.getClassParts().getTypedChildren().get(0),
                ASTConstructorDeclaration.class);
        ASTFormalParameterList params = constr.getConstructorDecl().getFormalParamList();
        assertEquals(integer, params.get(0).getDataType().getResolvedDataType());
        assertEquals(string, params.get(1).getDataType().getResolvedDataType());
    }

    /**
     * Tests implicit use-all for spruce.lang.
     */
    @Test
    public void testImplicitUseAllSpruceLang() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                class String {}
                """,
                """
                class Test {
                    void test(Integer x, String y) {
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        ParentSymbol integer = ensureIsa(lang.getTable().get("Integer"), ParentSymbol.class);
        ParentSymbol string = ensureIsa(lang.getTable().get("String"), ParentSymbol.class);

        ASTClassDeclaration testDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethod = ensureIsa(testDecl.getClassParts().getTypedChildren().get(0),
                ASTMethodDeclaration.class);
        ASTFormalParameterList params = testMethod.getHeader().getMethodDecl().getFormalParamList();
        assertEquals(integer, params.get(0).getDataType().getResolvedDataType());
        assertEquals(string, params.get(1).getDataType().getResolvedDataType());
    }

    // TODO: Semantic analysis testing: superclass cycles,
    // constructor resolution, method resolution, etc.
}
