package org.spruce.compiler.bootstrap.test.resolution;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.bootstrap.ast.toplevel.*;
import org.spruce.compiler.bootstrap.common.BaseMessageProducer;
import org.spruce.compiler.bootstrap.resolution.Resolver;
import org.spruce.compiler.bootstrap.resolution.TopLevelResolver;
import org.spruce.compiler.bootstrap.symbol.ParentSymbol;
import org.spruce.compiler.bootstrap.symbol.SymbolTable;
import org.spruce.compiler.bootstrap.symbol.TypeLookup;

import static org.junit.jupiter.api.Assertions.*;
import static org.spruce.compiler.bootstrap.test.resolution.ResolverTestUtility.*;
import static org.spruce.compiler.bootstrap.test.util.TestUtility.*;

/**
 * All tests for the top level semantic Resolver.
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
        List<ASTOrdinaryCompilationUnit> ocus = parseCodes(codes);
        TypeLookup global = createGlobalSymbolTable(ocus);
        TopLevelResolver Resolver = getTopLevelResolver(global);
        Resolver.resolveOrdinaryCompilationUnits(ocus);
        ensureNoErrors(global, Resolver);

        ParentSymbol spruce = ensureIsa(global.get("spruce"), ParentSymbol.class);
        SymbolTable spruceTable = spruce.getTable();
        ParentSymbol time = ensureIsa(spruceTable.get("time"), ParentSymbol.class);
        ParentSymbol collections = ensureIsa(spruceTable.get("collections"), ParentSymbol.class);

        ASTUseAllDeclaration useAllCollections = ensureIsa(ocus.get(0).getUseDeclList().get(0),
                ASTUseAllDeclaration.class);
        assertSame(collections, useAllCollections.getResolvedSymbol());
        ASTUseAllDeclaration useAllTime = ensureIsa(ocus.get(1).getUseDeclList().get(0),
                ASTUseAllDeclaration.class);
        assertSame(time, useAllTime.getResolvedSymbol());
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
                """
        );
        List<ASTOrdinaryCompilationUnit> ocus = parseCodes(codes);
        TypeLookup global = createGlobalSymbolTable(ocus);
        TopLevelResolver Resolver = getTopLevelResolver(global);
        Resolver.resolveOrdinaryCompilationUnits(ocus);
        ensureNoErrors(global, Resolver);

        ParentSymbol spruceNamespace = ensureIsa(global.get("spruce"), ParentSymbol.class);
        SymbolTable spruceTable = spruceNamespace.getTable();
        ParentSymbol timeNamespace = ensureIsa(spruceTable.get("time"), ParentSymbol.class);
        SymbolTable timeTable = timeNamespace.getTable();
        ParentSymbol instantClass = ensureIsa(timeTable.get("Instant"), ParentSymbol.class);
        ParentSymbol collectionsNamespace = ensureIsa(spruceTable.get("collections"), ParentSymbol.class);
        SymbolTable collectionsTable = collectionsNamespace.getTable();
        ParentSymbol arrayListClass = ensureIsa(collectionsTable.get("ArrayList"), ParentSymbol.class);

        ASTUseTypeDeclaration useTypeArrayList = ensureIsa(ocus.get(0).getUseDeclList().get(0),
                ASTUseTypeDeclaration.class);
        assertSame(arrayListClass, useTypeArrayList.getResolvedSymbol());
        ASTUseTypeDeclaration useTypeInstant = ensureIsa(ocus.get(1).getUseDeclList().get(0),
                ASTUseTypeDeclaration.class);
        assertSame(instantClass, useTypeInstant.getResolvedSymbol());
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
                """
        );
        List<ASTOrdinaryCompilationUnit> ocus = parseCodes(codes);
        TypeLookup global = createGlobalSymbolTable(ocus);
        TopLevelResolver Resolver = getTopLevelResolver(global);
        Resolver.resolveOrdinaryCompilationUnits(ocus);
        expectError(global, Resolver);
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
                """
        );
        List<ASTOrdinaryCompilationUnit> ocus = parseCodes(codes);
        TypeLookup global = createGlobalSymbolTable(ocus);
        TopLevelResolver Resolver = getTopLevelResolver(global);
        Resolver.resolveOrdinaryCompilationUnits(ocus);
        ensureNoErrors(global, Resolver);

        ParentSymbol spruceNamespace = ensureIsa(global.get("spruce"), ParentSymbol.class);
        SymbolTable spruceTable = spruceNamespace.getTable();
        ParentSymbol collectionsNamespace = ensureIsa(spruceTable.get("collections"), ParentSymbol.class);
        SymbolTable collectionsTable = collectionsNamespace.getTable();
        ParentSymbol arrayListClass = ensureIsa(collectionsTable.get("ArrayList"), ParentSymbol.class);
        ParentSymbol hashMapClass = ensureIsa(collectionsTable.get("HashMap"), ParentSymbol.class);

        ASTUseMultDeclaration useMultCollections = ensureIsa(ocus.get(0).getUseDeclList().get(0),
                ASTUseMultDeclaration.class);
        Optional<ParentSymbol> optResolvedArrayList = useMultCollections.getResolvedSymbol("ArrayList");
        assertTrue(optResolvedArrayList.isPresent());
        assertSame(arrayListClass, optResolvedArrayList.get());
        Optional<ParentSymbol> optResolvedHashMap = useMultCollections.getResolvedSymbol("HashMap");
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
                """
        );
        List<ASTOrdinaryCompilationUnit> ocus = parseCodes(codes);
        TypeLookup global = createGlobalSymbolTable(ocus);
        TopLevelResolver Resolver = getTopLevelResolver(global);
        Resolver.resolveOrdinaryCompilationUnits(ocus);
        expectError(global, Resolver, 3);
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
                """
        );
        List<ASTOrdinaryCompilationUnit> ocus = parseCodes(codes);
        TypeLookup global = createGlobalSymbolTable(ocus);
        TopLevelResolver Resolver = getTopLevelResolver(global);
        Resolver.resolveOrdinaryCompilationUnits(ocus);
        expectError(global, Resolver, 2);
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
                """
        );
        List<ASTOrdinaryCompilationUnit> ocus = parseCodes(codes);
        TypeLookup global = createGlobalSymbolTable(ocus);
        TopLevelResolver Resolver = getTopLevelResolver(global);
        Resolver.resolveOrdinaryCompilationUnits(ocus);
        expectError(global, Resolver, 1);
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
                """
        );
        List<ASTOrdinaryCompilationUnit> ocus = parseCodes(codes);
        TypeLookup global = createGlobalSymbolTable(ocus);
        TopLevelResolver Resolver = getTopLevelResolver(global);
        Resolver.resolveOrdinaryCompilationUnits(ocus);
        expectError(global, Resolver, 2);
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
                """
        );
        List<ASTOrdinaryCompilationUnit> ocus = parseCodes(codes);
        TypeLookup global = createGlobalSymbolTable(ocus);
        TopLevelResolver Resolver = getTopLevelResolver(global);
        Resolver.resolveOrdinaryCompilationUnits(ocus);
        expectError(global, Resolver, 1);
    }

    /**
     * Can't use a type with the same name as a namespace.  Should already be
     * caught at the symbol creation phase with a double error; can't declare a
     * type with the same name as a namespace or a namespace with the same name
     * as a type.
     */
    @Test
    public void testObscuredNamespace() {
        List<String> codes = List.of(
                """
                namespace spruce.obscured;
                class Dummy {}
                """,
                """
                namespace spruce;
                class obscured {}
                """,
                """
                use spruce.obscured;
                use spruce.obscured.Dummy;
                """
        );
        List<ASTOrdinaryCompilationUnit> ocus = parseCodes(codes);
        TypeLookup global = createGlobalSymbolTable(ocus);
        TopLevelResolver Resolver = getTopLevelResolver(global);
        Resolver.resolveOrdinaryCompilationUnits(ocus);
        expectError(global, Resolver, 1);
    }

    /**
     * Helper method to get a <code>TopLevelResolver</code>.
     * @param global The global <code>TypeLookup</code>.
     * @return A <code>TopLevelResolver</code>.
     */
    public static TopLevelResolver getTopLevelResolver(TypeLookup global) {
        return new Resolver(new BaseMessageProducer(), global).getTopLevelResolver();
    }
}
