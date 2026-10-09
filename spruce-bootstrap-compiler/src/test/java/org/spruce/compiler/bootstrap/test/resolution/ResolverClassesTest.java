package org.spruce.compiler.bootstrap.test.resolution;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.bootstrap.ast.classes.*;

import org.spruce.compiler.bootstrap.ast.types.ASTDataTypeNoArray;
import org.spruce.compiler.bootstrap.ast.types.ASTDataTypeNoArrayList;
import org.spruce.compiler.bootstrap.resolution.ClassesResolver;

import org.spruce.compiler.bootstrap.symbol.ChildSymbolTable;
import org.spruce.compiler.bootstrap.symbol.ParameterizedSymbol;
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

    /**
     * Tests bad nested type declaration of same simple name as enclosing type.
     */
    @Test
    public void testBadNestedTypeSameSimpleName() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                """,
                """
                class Foo  {
                    class Foo {}
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 1);
    }

    /**
     * Tests inheritance/override of type methods, step 1 - get all inherited
     * and declared methods.
     */
    @Test
    public void testInheritanceOverrideAllMethods() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {
                    String toString() { return ""; }
                }
                class Integer {}
                class Double {}
                class String {}
                """,
                """
                class Foo extends Superclass implements SomeOtherInterface, MarkerInterface {
                    constructor() {}
                    override String toString() { return "Foo"; }
                    override Integer getCount() { return 1; }
                    override Double getAverage() { return 9.0; }
                }
                abstract class Superclass implements Superinterface {
                    override String toString() { return "Superclass"; }
                }
                interface Superinterface {
                    Integer getCount();
                }
                interface SomeOtherInterface {
                    Double getAverage();
                }
                interface MarkerInterface {}
                """
        );
        Trio trio = compileSoFar(codes);

        ASTClassDeclaration fooDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ClassesResolver resolver = trio.resolver().getClassesResolver();
        List<ParameterizedSymbol> methods = resolver.getInheritedAndDeclaredMethods(fooDecl.getDeclSymbol());
        assertEquals(7, methods.size());

        List<String> expectedSymbolNames = List.of("toString()", "getCount()", "getAverage()", "toString()", "toString()",
                "getCount()", "getAverage()");
        List<String> expectedTypeNames = List.of("Foo", "Foo", "Foo", "Superclass", "spruce.lang.Any", "Superinterface",
                "SomeOtherInterface");
        List<String> expectedResultTypeNames = List.of("spruce.lang.String", "spruce.lang.Integer", "spruce.lang.Double",
                "spruce.lang.String", "spruce.lang.String", "spruce.lang.Integer", "spruce.lang.Double");

        for (int i = 0; i < methods.size(); i++) {
            ParameterizedSymbol method = methods.get(i);
            assertEquals(expectedSymbolNames.get(i), method.getName(),
                    "Failed method index " + i + " for method name.");
            ChildSymbolTable table = ensureIsa(method.getParent(), ChildSymbolTable.class);
            assertEquals(expectedTypeNames.get(i), table.getParent().getFullyQualifiedName(),
                    "Failed method index " + i + " for type name.");
            assertEquals(expectedResultTypeNames.get(i), method.getDataType().getFullyQualifiedName(),
                    "Failed method index " + i + " for type result name.");
        }
    }

    /**
     * Tests inheritance/override of type methods, step 2 - group all methods
     * that are override-equivalent with each other.
     */
    @Test
    public void testInheritanceOverrideMethodGrouping() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {
                    String toString() { return ""; }
                }
                class Integer {}
                class Double {}
                class String {}
                """,
                """
                class Foo extends Superclass implements SomeOtherInterface, MarkerInterface {
                    override void coolMethod(Sub s, Super t) {}
                    void coolMethod(Super s, Sub t) {}
                    override void coolMethod(Super s1, Super s2) {}
                    Integer getCount() { return 0; }
                    override Integer getCount(Super s, Sub t) { return 2; }
                    override Double getAverage() { return 10.0; }
                }
                abstract class Superclass implements Superinterface {
                    void coolMethod(Sub s, Super t) {}
                    abstract void coolMethod(Super s1, Super s2);
                    String someOtherMethod(Sub s, Super t) { return "Superclass"; }
                }
                interface Superinterface {
                    Integer getCount(Super s, Sub t);
                    String someOtherMethod(Sub s, Super t);
                }
                interface SomeOtherInterface {
                    Double getAverage();
                }
                interface MarkerInterface {}
                class Sub extends Super {}
                class Super {}
                """
        );
        Trio trio = compileSoFar(codes);

        ASTClassDeclaration fooDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ClassesResolver resolver = trio.resolver().getClassesResolver();
        List<ParameterizedSymbol> methods = resolver.getInheritedAndDeclaredMethods(fooDecl.getDeclSymbol());
        assertEquals(13, methods.size());

        List<List<ParameterizedSymbol>> overrideEquivalentGroups = resolver.groupOverrideEquivalentMethods(methods);
        assertEquals(8, overrideEquivalentGroups.size());

        List<String> expectedSymbolNames = List.of("coolMethod(Sub, Super)", "coolMethod(Super, Sub)",
                "coolMethod(Super, Super)", "getCount()", "getCount(Super, Sub)", "getAverage()",
                "someOtherMethod(Sub, Super)", "toString()");
        int[] expectedSizes = new int[] {2, 1, 2, 1, 2, 2, 2, 1};

        for (int i = 0; i < overrideEquivalentGroups.size(); i++) {
            List<ParameterizedSymbol> group = overrideEquivalentGroups.get(i);
            ParameterizedSymbol method = group.get(0);
            assertEquals(expectedSymbolNames.get(i), method.getName(),
                    "Failed method index " + i + " for method name.");
            assertEquals(expectedSizes[i], group.size());
        }
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
        compileSoFar(codes, 1);
    }

    /**
     * Tests bad constant that wasn't initialized.
     */
    @Test
    public void testConstantBadUninitialized() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                """,
                """
                use spruce.lang.Integer;
                interface Test {
                    constant Integer UNINITIALIZED;
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
        compileSoFar(codes, 1);
    }

    /**
     * Tests method without override, doesn't override method.
     */
    @Test
    public void testMethodNoOverrideDoesNotOverride() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                """,
                """
                use spruce.lang.Integer;
                class Test extends Superclass {
                    void test() {}
                }
                class Superclass {
                    Integer someOtherMethod() { return 2; }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ASTClassDeclaration testDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethod = ensureIsa(testDecl.getClassParts().getTypedChildren().get(0),
                ASTMethodDeclaration.class);
        assertTrue(testMethod.getDeclSymbol().getOverridden().isEmpty());
    }

    /**
     * Tests method with override, does override method in superclass hierarchy.  Return type matches.
     */
    @Test
    public void testMethodOverrideDoesOverrideFromSuperclass() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                """,
                """
                use spruce.lang.Integer;
                class Test extends Superclass {
                    override Integer test() { return 3; }
                }
                class Superclass {
                    Integer test() { return 2; }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ASTClassDeclaration testDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethod = ensureIsa(testDecl.getClassParts().getTypedChildren().get(0),
                ASTMethodDeclaration.class);
        ASTClassDeclaration superclassDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(1), ASTClassDeclaration.class);
        ASTMethodDeclaration overriddenMethod = ensureIsa(superclassDecl.getClassParts().getTypedChildren().get(0),
                ASTMethodDeclaration.class);
        assertEquals(1, testMethod.getDeclSymbol().getOverridden().size());
        ParameterizedSymbol overridden = testMethod.getDeclSymbol().getOverridden().get(0);
        assertSame(overridden, overriddenMethod.getDeclSymbol());
    }

    /**
     * Tests method with override, does override method in superinterface hierarchy of superclass.  Return type matches.
     */
    @Test
    public void testMethodOverrideDoesOverrideFromSuperInterfaceOfSuperclass() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                """,
                """
                use spruce.lang.Integer;
                class Test extends Superclass {
                    override Integer test() { return 3; }
                }
                abstract class Superclass implements SomeInterface {
                }
                interface SomeInterface {
                    Integer test();
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ASTClassDeclaration testDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethod = ensureIsa(testDecl.getClassParts().getTypedChildren().get(0),
                ASTMethodDeclaration.class);
        ASTInterfaceDeclaration interfaceDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(2), ASTInterfaceDeclaration.class);
        ASTInterfaceMethodDeclaration overriddenMethod = ensureIsa(interfaceDecl.getInterfaceParts().getTypedChildren().get(0),
                ASTInterfaceMethodDeclaration.class);
        assertEquals(1, testMethod.getDeclSymbol().getOverridden().size());
        assertSame(testMethod.getDeclSymbol().getOverridden().get(0), overriddenMethod.getDeclSymbol());
        assertSame(testMethod.getDeclSymbol().getDataType(), overriddenMethod.getDeclSymbol().getDataType());
    }

    /**
     * Tests method with override, does override method in superinterface hierarchy.  Return type matches.
     */
    @Test
    public void testMethodOverrideDoesOverrideFromSuperInterface() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                """,
                """
                use spruce.lang.Integer;
                class Test implements SomeInterface {
                    override Integer test() { return 3; }
                }
                interface SomeInterface {
                    Integer test();
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ASTClassDeclaration testDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethod = ensureIsa(testDecl.getClassParts().getTypedChildren().get(0),
                ASTMethodDeclaration.class);
        ASTInterfaceDeclaration interfaceDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(1), ASTInterfaceDeclaration.class);
        ASTInterfaceMethodDeclaration overriddenMethod = ensureIsa(interfaceDecl.getInterfaceParts().getTypedChildren().get(0),
                ASTInterfaceMethodDeclaration.class);
        assertEquals(1, testMethod.getDeclSymbol().getOverridden().size());
        assertSame(testMethod.getDeclSymbol().getOverridden().get(0), overriddenMethod.getDeclSymbol());
        assertSame(testMethod.getDeclSymbol().getDataType(), overriddenMethod.getDeclSymbol().getDataType());
    }

    /**
     * Tests bad method with no override, but overrides method.
     */
    @Test
    public void testBadMethodNoOverrideDoesOverrideFromSuperclass() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                """,
                """
                use spruce.lang.Integer;
                class Test extends Superclass {
                    Integer test() { return 3; }
                }
                class Superclass {
                    Integer test() { return 2; }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Test bad method with override, but doesn't override method.  Signature match, name mismatch.
     */
    @Test
    public void testBadMethodOverrideDoesNotOverrideNameMismatch() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                """,
                """
                use spruce.lang.Integer;
                class Test extends Superclass {
                    override Integer test(Integer i) { return 3; }
                }
                class Superclass {
                    Integer testing(Integer i) { return 2; }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests bad method with override, but doesn't override method.  Overloaded instead.
     */
    @Test
    public void testBadMethodOverrideDoesNotOverrideDoesOverload() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                class String {}
                """,
                """
                use spruce.lang.Integer;
                class Test extends Superclass {
                    override Integer test(Integer i) { return 3; }
                }
                class Superclass {
                    Integer test(String s) { return 2; }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests method that overrides correctly, return type is subtype (covariant return type).
     */
    @Test
    public void testMethodOverrideCovariantReturnType() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                class String {}
                """,
                """
                use spruce.lang.Integer;
                class Test extends Superclass {
                    override Sub test(String i) { return 3; }
                }
                class Superclass {
                    Super test(String s) { return 2; }
                }
                class Super {}
                class Sub extends Super {}
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ASTClassDeclaration testDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethod = ensureIsa(testDecl.getClassParts().getTypedChildren().get(0),
                ASTMethodDeclaration.class);
        ASTClassDeclaration superclassDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(1), ASTClassDeclaration.class);
        ASTMethodDeclaration overriddenMethod = ensureIsa(superclassDecl.getClassParts().getTypedChildren().get(0),
                ASTMethodDeclaration.class);
        assertEquals(1, testMethod.getDeclSymbol().getOverridden().size());
        ParameterizedSymbol overridden = testMethod.getDeclSymbol().getOverridden().get(0);
        assertSame(overridden, overriddenMethod.getDeclSymbol());
        assertTrue(testMethod.getDeclSymbol().getDataType().isSubTypeOf(overriddenMethod.getDeclSymbol().getDataType()));
    }

    /**
     * Test method overrides correctly, return types are void.
     */
    @Test
    public void testMethodOverrideReturnTypesVoid() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                class String {}
                """,
                """
                use spruce.lang.Integer;
                class Test extends Superclass {
                    override void test(String i) {}
                }
                class Superclass {
                    void test(String s) {}
                }
                class Super {}
                class Sub extends Super {}
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ASTClassDeclaration testDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethod = ensureIsa(testDecl.getClassParts().getTypedChildren().get(0),
                ASTMethodDeclaration.class);
        ASTClassDeclaration superclassDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(1), ASTClassDeclaration.class);
        ASTMethodDeclaration overriddenMethod = ensureIsa(superclassDecl.getClassParts().getTypedChildren().get(0),
                ASTMethodDeclaration.class);
        assertEquals(1, testMethod.getDeclSymbol().getOverridden().size());
        ParameterizedSymbol overridden = testMethod.getDeclSymbol().getOverridden().get(0);
        assertSame(overridden, overriddenMethod.getDeclSymbol());
        assertTrue(testMethod.getDeclSymbol().getDataType().isSubTypeOf(overriddenMethod.getDeclSymbol().getDataType()));
    }

    /**
     * Test bad method override, does override method, but return types mismatch.
     */
    @Test
    public void testBadMethodOverrideReturnTypeMismatch() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                class String {}
                """,
                """
                use spruce.lang.Integer;
                class Test extends Superclass {
                    override Super test(String i) { return 3; }
                }
                class Superclass {
                    Sub test(String s) { return 2; }
                }
                class Super {}
                class Sub extends Super {}
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests bad shared method attempting to override non-shared method.
     */
    @Test
    public void testBadMethodSharedOverrideOfNonShared() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                class String {}
                """,
                """
                use spruce.lang.Integer;
                class Test extends Superclass {
                    override shared Integer test(String i) { return 3; }
                }
                class Superclass {
                    Integer test(String s) { return 2; }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 2);
    }

    /**
     * Tests bad non-shared method attempting to override shared method.  Error.
     */
    @Test
    public void testBadMethodOverrideOfShared() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                class String {}
                """,
                """
                use spruce.lang.Integer;
                class Test extends Superclass {
                    override Integer test(String i) { return 3; }
                }
                class Superclass {
                    shared Integer test(String s) { return 2; }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 1);
    }

    /**
     * Tests shared method, would have hidden shared method.
     */
    @Test
    public void testMethodSharedDoesNotHideShared() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                class String {}
                """,
                """
                use spruce.lang.Integer;
                class Test extends Superclass {
                    shared Integer test(String i) { return 3; }
                }
                class Superclass {
                    shared Integer test(String s) { return 2; }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ASTClassDeclaration testDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethod = ensureIsa(testDecl.getClassParts().getTypedChildren().get(0),
                ASTMethodDeclaration.class);
        assertTrue(testMethod.getDeclSymbol().getOverridden().isEmpty());
    }

    /**
     * Tests interface method that doesn't interfere with anything in the root
     * class, Any.
     */
    @Test
    public void testInterfaceMethodNoRootInterference() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {
                    String toString() {
                        return "Any";
                    }
                }
                class Integer {}
                class String {}
                """,
                """
                interface Test {
                    Integer test(String i);
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());
    }

    /**
     * Tests bad shared interface method that interferes with a method in the
     * root class, Any.
     */
    @Test
    public void testBadInterfaceMethodSharedRootInterference() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {
                    String toString() {
                        return "Any";
                    }
                }
                class Integer {}
                class String {}
                """,
                """
                interface Test {
                    shared String toString() {}
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests bad interface method that interferes with a method in the
     * root class, Any, with a different return type.
     */
    @Test
    public void testBadInterfaceMethodRootInterferenceReturnType() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {
                    String toString() {
                        return "Any";
                    }
                }
                class Integer {}
                class String {}
                """,
                """
                interface Test {
                    Integer toString();
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests that at least one inherited method is return type substitutable
     * for all other methods in an override-equivalent group
     */
    @Test
    public void testInheritedMethodsReturnTypeSubstitutable() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                class String {}
                """,
                """
                class Test implements Foo, Bar, Baz {}
                interface Foo {
                    Super test();
                }
                interface Bar {
                    Sub test();
                }
                interface Baz {
                    SubSub test();
                }
                class Super {}
                class Sub extends Super {}
                class SubSub extends Sub {}
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());
    }

    /**
     * Test bad inherited methods where one method is not return type
     * substitutable with all others in an override-equivalent group.
     */
    @Test
    public void testBadInheritedMethodsNotReturnTypeSubstitutable() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                class String {}
                """,
                """
                class Test implements Foo, Bar, Baz {}
                interface Foo {
                    Super test();
                }
                interface Bar {
                    Sub test();
                }
                interface Baz {
                    Integer test();
                }
                class Super {}
                class Sub extends Super {}
                class SubSub extends Sub {}
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests overriding method that is return type substitutable for all
     * methods in the override-equivalent group.
     */
    @Test
    public void testOverridingMethodReturnTypeSubstitutable() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                class String {}
                """,
                """
                class Test implements Foo, Bar, Baz {
                    override SubSub test() {
                        return new SubSub();
                    }
                }
                interface Foo {
                    Super test();
                }
                interface Bar {
                    Sub test();
                }
                interface Baz {
                    SubSub test();
                }
                class Super {}
                class Sub extends Super {}
                class SubSub extends Sub {}
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());
    }

    @Test
    public void testBadOverridingMethodNotReturnTypeSubstitutable() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                class String {}
                """,
                """
                class Test implements Foo, Bar, Baz {
                    override void test() {}
                }
                interface Foo {
                    Super test();
                }
                interface Bar {
                    Sub test();
                }
                interface Baz {
                    SubSub test();
                }
                class Super {}
                class Sub extends Super {}
                class SubSub extends Sub {}
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
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
        compileSoFar(codes, 2);
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
