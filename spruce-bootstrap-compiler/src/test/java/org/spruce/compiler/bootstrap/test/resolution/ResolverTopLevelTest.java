package org.spruce.compiler.bootstrap.test.resolution;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.bootstrap.ast.toplevel.*;
import org.spruce.compiler.bootstrap.symbol.ParentSymbol;
import org.spruce.compiler.bootstrap.symbol.SymbolTable;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;

import static org.junit.jupiter.api.Assertions.*;
import static org.spruce.compiler.bootstrap.test.resolution.ResolverTestUtility.*;
import static org.spruce.compiler.bootstrap.test.util.TestUtility.*;

/**
 * All tests for the top level resolver.
 */
public class ResolverTopLevelTest {
    /**
     * Tests resolution of namespaces with use all declarations.
     */
    @Test
    public void testNamespaceResolutionUseAll() {
        List<String> codes = List.of(
             """
             namespace spruce.time;
             use spruce.collections.+;
             """,
             """
             namespace spruce.collections;
             use spruce.time.+;
             """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        SymbolTable spruceTable = spruce.getTable();
        ParentSymbol time = ensureIsa(spruceTable.get("time"), ParentSymbol.class);
        ParentSymbol collections = ensureIsa(spruceTable.get("collections"), ParentSymbol.class);

        ASTUseAllDeclaration useAllCollections = ensureIsa(trio.ocus().get(0).getUseDeclList().get(0),
                ASTUseAllDeclaration.class);
        assertSame(collections, useAllCollections.getResolvedNamespace());
        ASTUseAllDeclaration useAllTime = ensureIsa(trio.ocus().get(1).getUseDeclList().get(0),
                ASTUseAllDeclaration.class);
        assertSame(time, useAllTime.getResolvedNamespace());
    }

    /**
     * Tests resolution of types in namespaces with use type declarations.
     */
    @Test
    public void testNamespaceResolutionUseType() {
        List<String> codes = List.of(
                """
                namespace spruce.time;
                use spruce.collections.ArrayList;
                class Instant {}
                """,
                """
                namespace spruce.collections;
                use spruce.time.Instant;
                class ArrayList {}
                """,
                """
                namespace spruce.lang;
                class Any {}
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruceNamespace = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        SymbolTable spruceTable = spruceNamespace.getTable();
        ParentSymbol timeNamespace = ensureIsa(spruceTable.get("time"), ParentSymbol.class);
        SymbolTable timeTable = timeNamespace.getTable();
        ParentSymbol instantClass = ensureIsa(timeTable.get("Instant"), ParentSymbol.class);
        ParentSymbol collectionsNamespace = ensureIsa(spruceTable.get("collections"), ParentSymbol.class);
        SymbolTable collectionsTable = collectionsNamespace.getTable();
        ParentSymbol arrayListClass = ensureIsa(collectionsTable.get("ArrayList"), ParentSymbol.class);

        ASTUseTypeDeclaration useTypeArrayList = ensureIsa(trio.ocus().get(0).getUseDeclList().get(0),
                ASTUseTypeDeclaration.class);
        assertSame(arrayListClass, useTypeArrayList.getResolvedDataType());
        ASTUseTypeDeclaration useTypeInstant = ensureIsa(trio.ocus().get(1).getUseDeclList().get(0),
                ASTUseTypeDeclaration.class);
        assertSame(instantClass, useTypeInstant.getResolvedDataType());
    }

    /**
     * Test that resolution of a type fails when it doesn't exist.
     */
    @Test
    public void testUseTypeNotFound() {
        List<String> codes = List.of(
                """
                namespace spruce.collections;
                class ArrayList {}
                """,
                """
                namespace spruce.collections;
                use spruce.collections.HashMap;
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
     * Tests resolution of types in namespaces with use mult declarations.
     */
    @Test
    public void testNamespaceResolutionUseMult() {
        List<String> codes = List.of(
                """
                namespace spruce.time;
                use spruce.collections.{ArrayList, HashMap};
                """,
                """
                namespace spruce.collections;
                class ArrayList {}
                """,
                """
                namespace spruce.collections;
                class HashMap {}
                """,
                """
                namespace spruce.lang;
                class Any {}
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruceNamespace = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        SymbolTable spruceTable = spruceNamespace.getTable();
        ParentSymbol collectionsNamespace = ensureIsa(spruceTable.get("collections"), ParentSymbol.class);
        SymbolTable collectionsTable = collectionsNamespace.getTable();
        ParentSymbol arrayListClass = ensureIsa(collectionsTable.get("ArrayList"), ParentSymbol.class);
        ParentSymbol hashMapClass = ensureIsa(collectionsTable.get("HashMap"), ParentSymbol.class);

        ASTUseMultDeclaration useMultCollections = ensureIsa(trio.ocus().get(0).getUseDeclList().get(0),
                ASTUseMultDeclaration.class);
        Optional<TypeSymbol> optResolvedArrayList = useMultCollections.getResolvedDataType("ArrayList");
        assertTrue(optResolvedArrayList.isPresent());
        assertSame(arrayListClass, optResolvedArrayList.get());
        Optional<TypeSymbol> optResolvedHashMap = useMultCollections.getResolvedDataType("HashMap");
        assertTrue(optResolvedHashMap.isPresent());
        assertSame(hashMapClass, optResolvedHashMap.get());
    }

    /**
     * Tests bad use all declarations of namespace not found.
     */
    @Test
    public void testNamespaceNotFound() {
        List<String> codes = List.of(
                """
                namespace spruce.time;
                class Instant {}
                """,
                """
                use spruce.time.dne.+;
                use spruce.dne.+;
                use dne.+;
                """,
                """
                namespace spruce.lang;
                class Any {}
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 3);
    }

    /**
     * Tests bad use type declarations of type not found.
     */
    @Test
    public void testTypeNotFound() {
        List<String> codes = List.of(
                """
                namespace spruce.time;
                class Instant {}
                """,
                """
                use spruce.time.DoesNotExist;
                use spruce.Instant;
                """,
                """
                namespace spruce.lang;
                class Any {}
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 2);
    }

    /**
     * Tests bad use type declaration of simple name conflict with a type from
     * another namespace.
     */
    @Test
    public void testUseTypeNameConflict() {
        List<String> codes = List.of(
                """
                namespace spruce.collections;
                class List {}
                """,
                """
                namespace spruce.some.other;
                class List {}
                """,
                """
                use spruce.collections.List;
                use spruce.some.other.List;
                """,
                """
                namespace spruce.lang;
                class Any {}
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 1);
    }

    /**
     * Tests use mult declarations using the same simple names from different
     * namespaces.
     */
    @Test
    public void testUseMultDupeNames() {
        List<String> codes = List.of(
                """
                namespace spruce.collections;
                class List {}
                class Map {}
                """,
                """
                namespace spruce.some.other;
                class Map {}
                class List {}
                """,
                """
                use spruce.collections.{List, Map};
                use spruce.some.other.{Map, List};
                """,
                """
                namespace spruce.lang;
                class Any {}
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 2);
    }

    /**
     * Can't use the same simple name as a type declared in the same OCU.
     */
    @Test
    public void testBadUseDeclSameNameAsTypeInOcu() {
        List<String> codes = List.of(
                """
                namespace spruce.collections;
                class List {}
                """,
                """
                use spruce.collections.List;
                class List {}
                """,
                """
                namespace spruce.lang;
                class Any {}
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 1);
    }
}
