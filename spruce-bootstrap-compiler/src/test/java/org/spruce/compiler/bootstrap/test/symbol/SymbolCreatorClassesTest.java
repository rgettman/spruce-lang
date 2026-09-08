package org.spruce.compiler.bootstrap.test.symbol;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.bootstrap.ast.classes.*;
import org.spruce.compiler.bootstrap.ast.statements.ASTBlock;
import org.spruce.compiler.bootstrap.ast.statements.ASTBlockStatements;
import org.spruce.compiler.bootstrap.ast.statements.ASTConstructorInvocation;
import org.spruce.compiler.bootstrap.ast.statements.ASTLocalVariableDeclarationStatement;
import org.spruce.compiler.bootstrap.ast.toplevel.ASTNamespaceDeclaration;
import org.spruce.compiler.bootstrap.common.BaseMessageProducer;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.parser.TopLevelParser;
import org.spruce.compiler.bootstrap.resolution.ResolutionContext;
import org.spruce.compiler.bootstrap.scanner.TokenType;
import org.spruce.compiler.bootstrap.symbol.ChildSymbolTable;
import org.spruce.compiler.bootstrap.symbol.ClassesSymbolCreator;
import org.spruce.compiler.bootstrap.symbol.ParameterizedSymbol;
import org.spruce.compiler.bootstrap.symbol.ParentSymbol;
import org.spruce.compiler.bootstrap.symbol.Symbol;
import org.spruce.compiler.bootstrap.symbol.SymbolCreator;
import org.spruce.compiler.bootstrap.symbol.SymbolTable;
import org.spruce.compiler.bootstrap.symbol.GlobalLookup;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;
import org.spruce.compiler.bootstrap.symbol.VariableSymbol;
import org.spruce.compiler.bootstrap.test.parser.ParserTopLevelTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.spruce.compiler.bootstrap.symbol.Symbol.*;
import static org.spruce.compiler.bootstrap.symbol.Symbol.Kind.PARAMETER;
import static org.spruce.compiler.bootstrap.symbol.SymbolTable.Scope.*;
import static org.spruce.compiler.bootstrap.symbol.GlobalLookup.UNNAMED_NAMESPACE_NAME;
import static org.spruce.compiler.bootstrap.test.symbol.SymbolCreatorTestUtility.*;
import static org.spruce.compiler.bootstrap.test.util.TestUtility.ensureIsa;

/**
 * All tests for the classes symbol creator.
 */
public class SymbolCreatorClassesTest {

    /**
     * Tests class.  It has a default constructor.
     */
    @Test
    public void testClass() {
        Pair pair = createGlobalLookupNoErrors("class Class {}");
        SymbolTable global = pair.lookup();
        ASTClassDeclaration outer = ensureIsa(pair.typeDecl(), ASTClassDeclaration.class);
        checkSymbolTable(global, GLOBAL, 2, List.of(UNNAMED_NAMESPACE_NAME, "spruce"));

        ParentSymbol unnamedNamespace = ensureIsa(global.get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        checkSymbol(unnamedNamespace, UNNAMED_NAMESPACE_NAME, Kind.NAMESPACE, FLAG_NONE, 1);
        SymbolTable namespaceTable = unnamedNamespace.getTable();

        String expSymbolName = "Class";
        checkSymbolTable(namespaceTable, NAMESPACE, 1, List.of(expSymbolName));
        ParentSymbol symbol = ensureIsa(namespaceTable.get(expSymbolName), ParentSymbol.class);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), expSymbolName, Kind.CLASS,
                FLAG_NONE, 1);
        assertSame(symbol, outer.getDeclSymbol());

        SymbolTable classTable = symbol.getTable();
        checkSymbolTable(classTable, TYPE, 1, List.of(NAME_CONSTRUCTOR + "()"));
    }

    /**
     * Tests abstract class.  It has a default constructor.
     */
    @Test
    public void testAbstractClass() {
        Pair pair = createGlobalLookupNoErrors("abstract class AbstractClass {}");
        SymbolTable global = pair.lookup();
        ASTClassDeclaration outer = ensureIsa(pair.typeDecl(), ASTClassDeclaration.class);
        checkSymbolTable(global, GLOBAL, 2, List.of(UNNAMED_NAMESPACE_NAME, "spruce"));

        ParentSymbol unnamedNamespace = ensureIsa(global.get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        checkSymbol(unnamedNamespace, UNNAMED_NAMESPACE_NAME, Kind.NAMESPACE, FLAG_NONE, 1);
        SymbolTable namespaceTable = unnamedNamespace.getTable();

        String expSymbolName = "AbstractClass";
        checkSymbolTable(namespaceTable, NAMESPACE, 1, List.of(expSymbolName));
        Symbol symbol = namespaceTable.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), expSymbolName, Kind.CLASS,
                FLAG_MOD_ABSTRACT, 1);
        assertSame(symbol, outer.getDeclSymbol());
    }

    /**
     * Tests interface.  No default constructor.
     */
    @Test
    public void testPublicSealedInterface() {
        Pair pair = createGlobalLookupNoErrors("interface Interface {}");
        SymbolTable global = pair.lookup();
        ASTInterfaceDeclaration outer = ensureIsa(pair.typeDecl(), ASTInterfaceDeclaration.class);
        checkSymbolTable(global, GLOBAL, 2, List.of(UNNAMED_NAMESPACE_NAME, "spruce"));

        ParentSymbol unnamedNamespace = ensureIsa(global.get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        checkSymbol(unnamedNamespace, UNNAMED_NAMESPACE_NAME, Kind.NAMESPACE, FLAG_NONE, 1);
        SymbolTable namespaceTable = unnamedNamespace.getTable();

        String expSymbolName = "Interface";
        checkSymbolTable(namespaceTable, NAMESPACE, 1, List.of(expSymbolName));
        Symbol symbol = namespaceTable.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), expSymbolName, Kind.INTERFACE,
                FLAG_MOD_ABSTRACT, 0);
        assertSame(symbol, outer.getDeclSymbol());
    }

    /**
     * Tests member of inner class.  They have default constructors.
     */
    @Test
    public void testMemberInnerClass() {
        Pair pair = createGlobalLookupNoErrors("""
                class Outer {
                    class Inner {}
                }
                """);
        SymbolTable global = pair.lookup();
        ASTClassDeclaration outer = ensureIsa(pair.typeDecl(), ASTClassDeclaration.class);
        checkSymbolTable(global, GLOBAL, 2, List.of(UNNAMED_NAMESPACE_NAME));

        ParentSymbol unnamedNamespace = ensureIsa(global.get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        checkSymbol(unnamedNamespace, UNNAMED_NAMESPACE_NAME, Kind.NAMESPACE, FLAG_NONE, 1);
        SymbolTable namespaceTable = unnamedNamespace.getTable();

        String expSymbolName = "Outer";
        checkSymbolTable(namespaceTable, NAMESPACE, 1, List.of(expSymbolName));
        Symbol symbol = namespaceTable.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), "Outer", Kind.CLASS, FLAG_NONE, 2);
        assertSame(symbol, outer.getDeclSymbol());

        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        ASTClassDeclaration inner = ensureIsa(outer.getMembers().get(0), ASTClassDeclaration.class);
        checkSymbolTable(innerTable, TYPE, 2, Arrays.asList("Inner"));

        Symbol innerSymbol = innerTable.get("Inner");
        checkSymbol(ensureIsa(innerSymbol, ParentSymbol.class), "Inner", Kind.CLASS, FLAG_NONE, 1);
        assertSame(innerSymbol, inner.getDeclSymbol());
    }

    /**
     * Tests member of nested class.  They have default constructors.
     */
    @Test
    public void testMemberNestedClass() {
        Pair pair = createGlobalLookupNoErrors("""
                class Outer {
                    shared class Inner {}
                }
                """);

        SymbolTable global = pair.lookup();
        checkSymbolTable(global, GLOBAL, 2, List.of(UNNAMED_NAMESPACE_NAME, "spruce"));

        ParentSymbol unnamedNamespace = ensureIsa(global.get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        checkSymbol(unnamedNamespace, UNNAMED_NAMESPACE_NAME, Kind.NAMESPACE, FLAG_NONE, 1);
        SymbolTable namespaceTable = unnamedNamespace.getTable();

        String expSymbolName = "Outer";
        checkSymbolTable(namespaceTable, NAMESPACE, 1, List.of(expSymbolName));
        Symbol symbol = namespaceTable.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), "Outer", Kind.CLASS, FLAG_NONE, 2);

        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 2, Arrays.asList("Inner"));

        Symbol inner = innerTable.get("Inner");
        checkSymbol(ensureIsa(inner, ParentSymbol.class), "Inner", Kind.CLASS,
                FLAG_MOD_SHARED, 1);
    }

    /**
     * Tests member of nested interface.  The class has a default constructor,
     * but the interface does not have one.
     */
    @Test
    public void testMemberNestedInterface() {
        Pair pair = createGlobalLookupNoErrors("""
                class Outer {
                    interface Inner {}
                }
                """);
        SymbolTable global = pair.lookup();
        ASTClassDeclaration outer = ensureIsa(pair.typeDecl(), ASTClassDeclaration.class);
        checkSymbolTable(global, GLOBAL, 2, List.of(UNNAMED_NAMESPACE_NAME, "spruce"));

        ParentSymbol unnamedNamespace = ensureIsa(global.get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        checkSymbol(unnamedNamespace, UNNAMED_NAMESPACE_NAME, Kind.NAMESPACE, FLAG_NONE, 1);

        SymbolTable namespaceTable = unnamedNamespace.getTable();

        String expSymbolName = "Outer";
        checkSymbolTable(namespaceTable, NAMESPACE, 1, List.of(expSymbolName));
        Symbol symbol = namespaceTable.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), "Outer", Kind.CLASS, FLAG_NONE, 2);
        assertSame(symbol, outer.getDeclSymbol());

        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        ASTInterfaceDeclaration inner = ensureIsa(outer.getMembers().get(0), ASTInterfaceDeclaration.class);
        checkSymbolTable(innerTable, TYPE, 2, Arrays.asList("Inner"));

        Symbol innerSymbol = innerTable.get("Inner");
        checkSymbol(ensureIsa(innerSymbol, ParentSymbol.class), "Inner", Kind.INTERFACE,
                FLAG_MOD_ABSTRACT | FLAG_MOD_SHARED, 0);
        assertSame(innerSymbol, inner.getDeclSymbol());
    }

    /**
     * Tests member of constructor.  No default constructor.
     */
    @Test
    public void testMemberConstructor() {
        Pair pair = createGlobalLookupNoErrors("""
                class HasMember {
                    constructor() {}
                }
                """);
        SymbolTable global = pair.lookup();
        ASTTypeDeclaration typeDecl = pair.typeDecl();
        checkSymbolTable(global, GLOBAL, 2, List.of(UNNAMED_NAMESPACE_NAME, "spruce"));

        ParentSymbol unnamedNamespace = ensureIsa(global.get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        checkSymbol(unnamedNamespace, UNNAMED_NAMESPACE_NAME, Kind.NAMESPACE, FLAG_NONE, 1);

        SymbolTable namespaceTable = unnamedNamespace.getTable();

        String expSymbolName = "HasMember";
        checkSymbolTable(namespaceTable, NAMESPACE, 1, List.of(expSymbolName));
        Symbol symbol = namespaceTable.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), "HasMember", Kind.CLASS, FLAG_NONE, 1);

        String symbolName = NAME_CONSTRUCTOR + "()";
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 1, Arrays.asList(symbolName));

        Symbol constructorDecl = innerTable.get(symbolName);
        checkSymbol(ensureIsa(constructorDecl, ParentSymbol.class), symbolName, Kind.CONSTRUCTOR, FLAG_NONE,0);

        ASTConstructorDeclaration constructor = ensureIsa(typeDecl.getMembers().get(0), ASTConstructorDeclaration.class);
        assertSame(constructorDecl, constructor.getDeclSymbol());
    }

    /**
     * Tests members of overloaded constructors.  No default constructor.
     */
    @Test
    public void testMemberConstructorOverload() {
        Pair pair = createGlobalLookupNoErrors("""
                class Int {
                    constructor() {}
                    constructor(Int foo) {}
                }
                """);
        SymbolTable global = pair.lookup();
        ASTTypeDeclaration typeDecl = pair.typeDecl();
        checkSymbolTable(global, GLOBAL, 2, List.of(UNNAMED_NAMESPACE_NAME));

        ParentSymbol unnamedNamespace = ensureIsa(global.get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        checkSymbol(unnamedNamespace, UNNAMED_NAMESPACE_NAME, Kind.NAMESPACE, FLAG_NONE, 1);

        SymbolTable namespaceTable = unnamedNamespace.getTable();

        String expSymbolName = "Int";
        checkSymbolTable(namespaceTable, NAMESPACE, 1, List.of(expSymbolName));
        Symbol symbol = namespaceTable.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), expSymbolName, Kind.CLASS, FLAG_NONE, 2);

        String symbolName = NAME_CONSTRUCTOR + "()";
        String symbolName2 = NAME_CONSTRUCTOR + "(Int)";
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 2, Arrays.asList(symbolName, symbolName2));

        Symbol constructorDecl = innerTable.get(symbolName);
        checkSymbol(ensureIsa(constructorDecl, ParameterizedSymbol.class), symbolName, Kind.CONSTRUCTOR,
                FLAG_NONE,0, 0);
        ASTConstructorDeclaration constructor = ensureIsa(typeDecl.getMembers().get(0), ASTConstructorDeclaration.class);
        assertSame(constructorDecl, constructor.getDeclSymbol());

        Symbol constructorDecl2 = innerTable.get(symbolName2);
        checkSymbol(ensureIsa(constructorDecl2, ParameterizedSymbol.class), symbolName2, Kind.CONSTRUCTOR,
                FLAG_NONE,1, 1);
        ASTConstructorDeclaration constructor2 = ensureIsa(typeDecl.getMembers().get(1), ASTConstructorDeclaration.class);
        assertSame(constructorDecl2, constructor2.getDeclSymbol());
    }

    /**
     * Tests that no default constructor is created if an explicit constructor
     * is declared.
     */
    @Test
    public void testMemberConstructorNoDefaultConstructor() {
        Pair pair = createGlobalLookupNoErrors("""
                class HasExplicitConstructor {
                    constructor(String s) {}
                }
                """);
        SymbolTable global = pair.lookup();
        ASTTypeDeclaration typeDecl = pair.typeDecl();
        checkSymbolTable(global, GLOBAL, 2, List.of(UNNAMED_NAMESPACE_NAME, "spruce"));

        ParentSymbol unnamedNamespace = ensureIsa(global.get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        checkSymbol(unnamedNamespace, UNNAMED_NAMESPACE_NAME, Kind.NAMESPACE, FLAG_NONE, 1);
        SymbolTable namespaceTable = unnamedNamespace.getTable();

        String expSymbolName = "HasExplicitConstructor";
        checkSymbolTable(namespaceTable, NAMESPACE, 1, List.of(expSymbolName));
        Symbol symbol = namespaceTable.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), expSymbolName, Kind.CLASS, FLAG_NONE, 1);

        String symbolName = NAME_CONSTRUCTOR + "(spruce.lang.String)";
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 1, Arrays.asList(symbolName));

        Symbol constructorDecl = innerTable.get(symbolName);
        checkSymbol(ensureIsa(constructorDecl, ParameterizedSymbol.class), symbolName, Kind.CONSTRUCTOR,
                FLAG_NONE,1, 1);
        ASTConstructorDeclaration constructor = ensureIsa(typeDecl.getMembers().get(0), ASTConstructorDeclaration.class);
        assertSame(constructorDecl, constructor.getDeclSymbol());
    }

    /**
     * Tests that a class without an explicit constructor still has the default
     * constructor, and its body has a "super" constructor invocation.
     */
    @Test
    public void testDefaultConstructor() {
        Pair pair = createGlobalLookupNoErrors("""
                class HasDefaultConstructor {
                }
                """);
        SymbolTable global = pair.lookup();
        ASTTypeDeclaration typeDecl = pair.typeDecl();
        checkSymbolTable(global, GLOBAL, 2, List.of(UNNAMED_NAMESPACE_NAME, "spruce"));

        ParentSymbol unnamedNamespace = ensureIsa(global.get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        checkSymbol(unnamedNamespace, UNNAMED_NAMESPACE_NAME, Kind.NAMESPACE, FLAG_NONE, 1);
        SymbolTable namespaceTable = unnamedNamespace.getTable();

        String expSymbolName = "HasDefaultConstructor";
        checkSymbolTable(namespaceTable, NAMESPACE, 1, List.of(expSymbolName));
        Symbol symbol = namespaceTable.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), expSymbolName, Kind.CLASS, FLAG_NONE, 1);

        String symbolName = NAME_CONSTRUCTOR + "()";
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 1, Arrays.asList(symbolName));

        Symbol constructorDecl = innerTable.get(symbolName);
        checkSymbol(ensureIsa(constructorDecl, ParameterizedSymbol.class), symbolName, Kind.CONSTRUCTOR,
                FLAG_NONE,0, 0);
        ASTConstructorDeclaration constructor = ensureIsa(typeDecl.getMembers().get(0), ASTConstructorDeclaration.class);
        assertSame(constructorDecl, constructor.getDeclSymbol());

        // Test fabricated default constructor's AST nodes.
        assertEquals(symbolName, constructorDecl.getName());
        ASTConstructorDeclarator declarator = constructor.getConstructorDecl();
        ASTFormalParameterList formalParamList = declarator.getFormalParamList();
        assertEquals(0, formalParamList.getChildren().size());
        assertNotNull(constructor.getBlock());

        ASTBlock block = constructor.getBlock();
        assertNotNull(constructor.getBlock());
        ASTBlockStatements blockStmts = block.getBlockStmts();
        assertEquals(1, blockStmts.getChildren().size());

        ASTConstructorInvocation constrInvocation = ensureIsa(blockStmts.get(0), ASTConstructorInvocation.class);
        assertEquals(TokenType.SUPER, constrInvocation.getConstructorKeyword().getKeyword());
        assertEquals(0, constrInvocation.getArgumentList().getChildren().size());
    }

    /**
     * Tests that the "Any" root class without an explicit constructor still has the default
     * constructor, but the body does NOT have a constructor invocation.
     */
    @Test
    public void testRootClassDefaultConstructorNoConstructorInvocation() {
        // Special situation, bypass "createGlobalLookupNoErrors".
        ClassesSymbolCreator creator = getClassesSymbolCreator();
        TopLevelParser topLevelParser = ParserTopLevelTest.getTopLevelParser("""
                namespace spruce.lang;
                class Any {
                }
                """);
        ASTNamespaceDeclaration namespaceDecl = topLevelParser.parseNamespaceDeclaration();
        assertEquals(2, namespaceDecl.getNamespace().getChildren().size());
        ASTTypeDeclaration typeDecl = topLevelParser.parseTypeDeclaration();
        GlobalLookup global = creator.getGlobalLookup();

        ParentSymbol spruceLang = setupSpruceLangNamespaces(global);
        ChildSymbolTable langTable = spruceLang.getTable();
        creator.createSymbolsForTopLevelTypeDeclarationTypeOnly(typeDecl, spruceLang);
        // Auto-use spruce.lang.
        ResolutionContext fake = new ResolutionContext(new HashMap<>(), typeDecl.getDeclSymbol());
        fake.using().put("lang", spruceLang);
        creator.createSymbolsForTopLevelTypeDeclarationTypeMembers(typeDecl, fake);
        // End of special situation setup.

        String expSymbolName = "Any";
        checkSymbolTable(langTable, NAMESPACE, 1, List.of(expSymbolName));
        Symbol symbol = langTable.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), expSymbolName, Kind.CLASS, FLAG_NONE, 1);

        String symbolName = NAME_CONSTRUCTOR + "()";
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 1, Arrays.asList(symbolName));

        Symbol constructorDecl = innerTable.get(symbolName);
        checkSymbol(ensureIsa(constructorDecl, ParameterizedSymbol.class), symbolName, Kind.CONSTRUCTOR,
                FLAG_NONE,0, 0);
        ASTConstructorDeclaration constructor = ensureIsa(typeDecl.getMembers().get(0), ASTConstructorDeclaration.class);
        assertSame(constructorDecl, constructor.getDeclSymbol());

        // Test fabricated default constructor's AST nodes.
        assertEquals(symbolName, constructorDecl.getName());
        ASTConstructorDeclarator declarator = constructor.getConstructorDecl();
        ASTFormalParameterList formalParamList = declarator.getFormalParamList();
        assertEquals(0, formalParamList.getChildren().size());
        assertNotNull(constructor.getBlock());

        ASTBlock block = constructor.getBlock();
        ASTBlockStatements blockStmts = block.getBlockStmts();
        assertEquals(0, blockStmts.getChildren().size());
    }

    /**
     * Test that an explicit constructor without an explicit constructor
     * invocation gets an implicit super() at the top.
     */
    @Test
    public void testExplicitConstructorImplicitSuper() {
        Pair pair = createGlobalLookupNoErrors("""
                class HasExplicitConstructor {
                    constructor(String s) {
                        String ss = "ss";
                    }
                }
                """);
        SymbolTable global = pair.lookup();
        ASTTypeDeclaration typeDecl = pair.typeDecl();
        checkSymbolTable(global, GLOBAL, 2, List.of(UNNAMED_NAMESPACE_NAME, "spruce"));

        ParentSymbol unnamedNamespace = ensureIsa(global.get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        checkSymbol(unnamedNamespace, UNNAMED_NAMESPACE_NAME, Kind.NAMESPACE, FLAG_NONE, 1);
        SymbolTable namespaceTable = unnamedNamespace.getTable();

        String expSymbolName = "HasExplicitConstructor";
        checkSymbolTable(namespaceTable, NAMESPACE, 1, List.of(expSymbolName));
        Symbol symbol = namespaceTable.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), expSymbolName, Kind.CLASS, FLAG_NONE, 1);

        String symbolName = NAME_CONSTRUCTOR + "(spruce.lang.String)";
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 1, Arrays.asList(symbolName));

        Symbol constructorDecl = innerTable.get(symbolName);
        checkSymbol(ensureIsa(constructorDecl, ParameterizedSymbol.class), symbolName, Kind.CONSTRUCTOR,
                FLAG_NONE,2, 1);
        ASTConstructorDeclaration constructor = ensureIsa(typeDecl.getMembers().get(0), ASTConstructorDeclaration.class);
        ASTBlockStatements blockStmts = constructor.getBlock().getBlockStmts();
        assertEquals(2, blockStmts.getChildren().size());

        ASTConstructorInvocation implicit = ensureIsa(blockStmts.getChildren().get(0), ASTConstructorInvocation.class);
        assertEquals(0, implicit.getArgumentList().getChildren().size());
        assertEquals(TokenType.SUPER, implicit.getConstructorKeyword().getKeyword());

        ASTLocalVariableDeclarationStatement localVarDeclStmt = ensureIsa(blockStmts.getChildren().get(1),
                ASTLocalVariableDeclarationStatement.class);
        assertEquals(1, localVarDeclStmt.getLocalVarDecl().getVarDeclList().getChildren().size());
    }

    /**
     * Test that an explicit constructor with an explicit constructor
     * invocation does NOT get an implicit super() at the top.
     */
    @Test
    public void testExplicitConstructorExplicitSuper() {
        Pair pair = createGlobalLookupNoErrors("""
                class HasExplicitConstructor {
                    constructor(String s) {
                        super();
                        String ss = "ss";
                    }
                }
                """);
        SymbolTable global = pair.lookup();
        ASTTypeDeclaration typeDecl = pair.typeDecl();
        checkSymbolTable(global, GLOBAL, 2, List.of(UNNAMED_NAMESPACE_NAME, "spruce"));

        ParentSymbol unnamedNamespace = ensureIsa(global.get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        checkSymbol(unnamedNamespace, UNNAMED_NAMESPACE_NAME, Kind.NAMESPACE, FLAG_NONE, 1);
        SymbolTable namespaceTable = unnamedNamespace.getTable();

        String expSymbolName = "HasExplicitConstructor";
        checkSymbolTable(namespaceTable, NAMESPACE, 1, List.of(expSymbolName));
        Symbol symbol = namespaceTable.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), expSymbolName, Kind.CLASS, FLAG_NONE, 1);

        String symbolName = NAME_CONSTRUCTOR + "(spruce.lang.String)";
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 1, Arrays.asList(symbolName));

        Symbol constructorDecl = innerTable.get(symbolName);
        checkSymbol(ensureIsa(constructorDecl, ParameterizedSymbol.class), symbolName, Kind.CONSTRUCTOR,
                FLAG_NONE,2, 1);
        ASTConstructorDeclaration constructor = ensureIsa(typeDecl.getMembers().get(0), ASTConstructorDeclaration.class);
        ASTBlockStatements blockStmts = constructor.getBlock().getBlockStmts();
        assertEquals(2, blockStmts.getChildren().size());

        ASTConstructorInvocation implicit = ensureIsa(blockStmts.getChildren().get(0), ASTConstructorInvocation.class);
        assertEquals(0, implicit.getArgumentList().getChildren().size());
        assertEquals(TokenType.SUPER, implicit.getConstructorKeyword().getKeyword());

        ASTLocalVariableDeclarationStatement localVarDeclStmt = ensureIsa(blockStmts.getChildren().get(1),
                ASTLocalVariableDeclarationStatement.class);
        assertEquals(1, localVarDeclStmt.getLocalVarDecl().getVarDeclList().getChildren().size());
    }

    /**
     * Tests that an interface has no default constructor.
     */
    @Test
    public void testInterfaceNoDefaultConstructor() {
        Pair pair = createGlobalLookupNoErrors("""
                interface HasNoDefaultConstructor {
                }
                """);
        SymbolTable global = pair.lookup();
        checkSymbolTable(global, GLOBAL, 2, List.of(UNNAMED_NAMESPACE_NAME, "spruce"));

        ParentSymbol unnamedNamespace = ensureIsa(global.get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        checkSymbol(unnamedNamespace, UNNAMED_NAMESPACE_NAME, Kind.NAMESPACE, FLAG_NONE, 1);
        SymbolTable namespaceTable = unnamedNamespace.getTable();

        String expSymbolName = "HasNoDefaultConstructor";
        checkSymbolTable(namespaceTable, NAMESPACE, 1, List.of(expSymbolName));
        Symbol symbol = namespaceTable.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), expSymbolName, Kind.INTERFACE, FLAG_MOD_ABSTRACT, 0);
    }

    /**
     * Tests members of field declarations with variable modifiers.  The class
     * has a default constructor.
     */
    @Test
    public void testMemberFieldDeclarationVarMods() {
        Pair pair = createGlobalLookupNoErrors("""
                class HasMember {
                    String immutable;
                }
                """);
        SymbolTable global = pair.lookup();
        ASTTypeDeclaration typeDecl = pair.typeDecl();
        checkSymbolTable(global, GLOBAL, 2, List.of(UNNAMED_NAMESPACE_NAME, "spruce"));

        ParentSymbol unnamedNamespace = ensureIsa(global.get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        checkSymbol(unnamedNamespace, UNNAMED_NAMESPACE_NAME, Kind.NAMESPACE, FLAG_NONE, 1);

        SymbolTable namespaceTable = unnamedNamespace.getTable();

        String expSymbolName = "HasMember";
        checkSymbolTable(namespaceTable, NAMESPACE, 1, List.of(expSymbolName));
        Symbol symbol = namespaceTable.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), "HasMember", Kind.CLASS, FLAG_NONE, 2);

        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 2, Arrays.asList("immutable"));

        Symbol immutSymbol = innerTable.get("immutable");
        checkSymbol(immutSymbol, "immutable", Kind.FIELD, FLAG_NONE);
        ASTFieldDeclaration field = ensureIsa(typeDecl.getMembers().get(0), ASTFieldDeclaration.class);
        assertSame(immutSymbol, field.getVarDeclList().get(0).getDeclSymbol());
    }

    /**
     * Tests members of field declarations.  The class has a default constructor.
     */
    @Test
    public void testMemberFieldDeclaration() {
        Pair pair = createGlobalLookupNoErrors("""
                class HasMember {
                    String foo;
                    Integer bar, jazz;
                }
                """);
        SymbolTable global = pair.lookup();
        ASTTypeDeclaration typeDecl = pair.typeDecl();
        checkSymbolTable(global, GLOBAL, 2, List.of(UNNAMED_NAMESPACE_NAME, "spruce"));

        ParentSymbol unnamedNamespace = ensureIsa(global.get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        checkSymbol(unnamedNamespace, UNNAMED_NAMESPACE_NAME, Kind.NAMESPACE, FLAG_NONE, 1);

        SymbolTable namespaceTable = unnamedNamespace.getTable();

        String expSymbolName = "HasMember";
        checkSymbolTable(namespaceTable, NAMESPACE, 1, List.of(expSymbolName));
        Symbol symbol = namespaceTable.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), "HasMember", Kind.CLASS, FLAG_NONE, 4);
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 4, Arrays.asList("foo", "bar", "jazz"));

        Symbol fooSymbol = innerTable.get("foo");
        checkSymbol(fooSymbol, "foo", Kind.FIELD, FLAG_NONE);
        ASTFieldDeclaration fooField = ensureIsa(typeDecl.getMembers().get(0), ASTFieldDeclaration.class);
        assertSame(fooSymbol, fooField.getVarDeclList().get(0).getDeclSymbol());

        Symbol barSymbol = innerTable.get("bar");
        checkSymbol(barSymbol, "bar", Kind.FIELD, FLAG_NONE);
        ASTFieldDeclaration twoField = ensureIsa(typeDecl.getMembers().get(1), ASTFieldDeclaration.class);
        assertSame(barSymbol, twoField.getVarDeclList().get(0).getDeclSymbol());

        Symbol jazzSymbol = innerTable.get("jazz");
        checkSymbol(jazzSymbol, "jazz", Kind.FIELD, FLAG_NONE);
        assertSame(jazzSymbol, twoField.getVarDeclList().get(1).getDeclSymbol());
    }

    /**
     * Tests members of shared field declaration.  The class has a default
     * constructor.
     */
    @Test
    public void testMemberSharedFieldDeclaration() {
        Pair pair = createGlobalLookupNoErrors("""
                class HasMember {
                    shared String foo;
                }
                """);
        SymbolTable global = pair.lookup();
        ASTTypeDeclaration typeDecl = pair.typeDecl();
        checkSymbolTable(global, GLOBAL, 2, List.of(UNNAMED_NAMESPACE_NAME, "spruce"));

        ParentSymbol unnamedNamespace = ensureIsa(global.get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        checkSymbol(unnamedNamespace, UNNAMED_NAMESPACE_NAME, Kind.NAMESPACE, FLAG_NONE, 1);

        SymbolTable namespaceTable = unnamedNamespace.getTable();

        String expSymbolName = "HasMember";
        checkSymbolTable(namespaceTable, NAMESPACE, 1, List.of(expSymbolName));
        Symbol symbol = namespaceTable.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), "HasMember", Kind.CLASS, FLAG_NONE, 2);
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 2, Arrays.asList("foo"));

        Symbol fooSymbol = innerTable.get("foo");
        checkSymbol(fooSymbol, "foo", Kind.FIELD, FLAG_MOD_SHARED);
        ASTFieldDeclaration fooField = ensureIsa(typeDecl.getMembers().get(0), ASTFieldDeclaration.class);
        assertSame(fooSymbol, fooField.getVarDeclList().get(0).getDeclSymbol());
    }

    /**
     * Tests duplicate field names.
     */
    @Test
    public void testMemberFieldsSameName() {
        createGlobalLookupWithErrors("""
                class DupeFieldSymbols {
                    String foo;
                    Integer foo;
                }
                """, 2);
    }

    /**
     * Tests member of constant declaration.  The class has a default constructor.
     */
    @Test
    public void testMemberConstant() {
        Pair pair = createGlobalLookupNoErrors("""
                class HasMember {
                    constant String BAR = "bar";
                }
                """);
        SymbolTable global = pair.lookup();
        ASTTypeDeclaration typeDecl = pair.typeDecl();
        checkSymbolTable(global, GLOBAL, 2, List.of(UNNAMED_NAMESPACE_NAME, "spruce"));

        ParentSymbol unnamedNamespace = ensureIsa(global.get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        checkSymbol(unnamedNamespace, UNNAMED_NAMESPACE_NAME, Kind.NAMESPACE, FLAG_NONE, 1);

        SymbolTable namespaceTable = unnamedNamespace.getTable();
        String expSymbolName = "HasMember";
        checkSymbolTable(namespaceTable, NAMESPACE, 1, List.of(expSymbolName));
        Symbol symbol = namespaceTable.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), "HasMember", Kind.CLASS, FLAG_NONE, 2);
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 2, Arrays.asList("BAR"));

        Symbol constantSymbol = innerTable.get("BAR");
        checkSymbol(constantSymbol, "BAR", Kind.FIELD, FLAG_MOD_SHARED | FLAG_MOD_FINAL);
        ASTFieldDeclaration barField = ensureIsa(typeDecl.getMembers().get(0), ASTFieldDeclaration.class);
        assertSame(constantSymbol, barField.getVarDeclList().get(0).getDeclSymbol());
    }

    /**
     * Tests member of method declaration.  The class has a default constructor.
     */
    @Test
    public void testMemberMethodDeclaration() {
        Pair pair = createGlobalLookupNoErrors("""
               class Stringy {
                    void foo(Stringy bar) {
                        Widget baz;
                    }
               }
               """);
        SymbolTable global = pair.lookup();
        ASTTypeDeclaration typeDecl = pair.typeDecl();
        checkSymbolTable(global, GLOBAL, 2, List.of(UNNAMED_NAMESPACE_NAME, "spruce"));

        ParentSymbol unnamedNamespace = ensureIsa(global.get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        checkSymbol(unnamedNamespace, UNNAMED_NAMESPACE_NAME, Kind.NAMESPACE, FLAG_NONE, 1);

        SymbolTable namespaceTable = unnamedNamespace.getTable();

        String expSymbolName = "Stringy";
        checkSymbolTable(namespaceTable, NAMESPACE, 1, List.of(expSymbolName));
        Symbol symbol = namespaceTable.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), expSymbolName, Kind.CLASS, FLAG_NONE, 2);
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        String symbolName = "foo(Stringy)";
        checkSymbolTable(innerTable, TYPE, 2, Arrays.asList(symbolName));

        Symbol fooSymbol = innerTable.get(symbolName);
        checkSymbol(ensureIsa(fooSymbol, ParameterizedSymbol.class), symbolName, Kind.METHOD,
                FLAG_NONE,2, 1);
        ASTMethodDeclaration fooMethod = ensureIsa(typeDecl.getMembers().get(0), ASTMethodDeclaration.class);
        assertSame(fooSymbol, fooMethod.getDeclSymbol());
    }

    /**
     * Tests bad abstract method with body.
     */
    @Test
    public void testMemberBadAbstractMethodBody() {
        createGlobalLookupWithErrors("""
                class Test {
                    abstract void testMethod() {}
                }
                """, 2);
    }

    /**
     * Tests bad abstract method with class not being abstract.
     */
    @Test
    public void testMemberBadAbstractMethodClassNotAbstract() {
        createGlobalLookupWithErrors("""
                class Test {
                    abstract void testMethod();
                }
                """, 1);
    }

    /**
     * Tests member of abstract method declaration.  The class has a default
     * constructor.
     */
    @Test
    public void testMemberAbstractMethodDeclaration() {
        Pair pair = createGlobalLookupNoErrors("""
               abstract class HasMember {
                    abstract void foo(HasMember bar);
               }
               """);
        SymbolTable global = pair.lookup();
        ASTTypeDeclaration typeDecl = pair.typeDecl();
        checkSymbolTable(global, GLOBAL, 2, List.of(UNNAMED_NAMESPACE_NAME, "spruce"));

        ParentSymbol unnamedNamespace = ensureIsa(global.get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        checkSymbol(unnamedNamespace, UNNAMED_NAMESPACE_NAME, Kind.NAMESPACE, FLAG_NONE, 1);

        SymbolTable namespaceTable = unnamedNamespace.getTable();

        String expSymbolName = "HasMember";
        checkSymbolTable(namespaceTable, NAMESPACE, 1, List.of(expSymbolName));
        Symbol symbol = namespaceTable.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), "HasMember", Kind.CLASS,
                FLAG_MOD_ABSTRACT, 2);
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        String symbolName = "foo(HasMember)";
        checkSymbolTable(innerTable, TYPE, 2, Arrays.asList(symbolName));

        Symbol fooSymbol = innerTable.get(symbolName);
        checkSymbol(ensureIsa(fooSymbol, ParameterizedSymbol.class), symbolName, Kind.METHOD,
                FLAG_MOD_ABSTRACT,1, 1);
        ASTMethodDeclaration fooMethod = ensureIsa(typeDecl.getMembers().get(0), ASTMethodDeclaration.class);
        assertSame(fooSymbol, fooMethod.getDeclSymbol());
    }

    /**
     * Tests bad shared method without body.
     */
    @Test
    public void testMemberBadSharedMethodNoBody() {
        createGlobalLookupWithErrors("""
                class Test {
                    shared void testMethod();
                }
                """, 1);
    }

    /**
     * Tests member of overloaded methods.  The class has a default constructor.
     */
    @Test
    public void testMemberMethodOverloads() {
        Pair pair = createGlobalLookupNoErrors("""
                class Stringy {
                    Stringy foo;
                    void foo() {}
                    void foo(Stringy bar) {}
                }
                """);
        SymbolTable global = pair.lookup();
        ASTTypeDeclaration typeDecl = pair.typeDecl();
        checkSymbolTable(global, GLOBAL, 2, List.of(UNNAMED_NAMESPACE_NAME, "spruce"));

        ParentSymbol unnamedNamespace = ensureIsa(global.get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        checkSymbol(unnamedNamespace, UNNAMED_NAMESPACE_NAME, Kind.NAMESPACE, FLAG_NONE, 1);
        SymbolTable namespaceTable = unnamedNamespace.getTable();
        String expSymbolName = "Stringy";

        checkSymbolTable(namespaceTable, NAMESPACE, 1, List.of(expSymbolName));
        Symbol symbol = namespaceTable.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), expSymbolName, Kind.CLASS, FLAG_NONE, 4);
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        checkSymbolTable(innerTable, TYPE, 4, Arrays.asList("foo", "foo()", "foo(Stringy)"));

        String symbolName = "foo";
        Symbol fooSymbol = innerTable.get(symbolName);
        checkSymbol(fooSymbol, symbolName, Kind.FIELD, FLAG_NONE);
        ASTFieldDeclaration fooField = ensureIsa(typeDecl.getMembers().get(0), ASTFieldDeclaration.class);
        assertSame(fooSymbol, fooField.getVarDeclList().get(0).getDeclSymbol());

        symbolName = "foo()";
        Symbol fooSymbol2 = innerTable.get(symbolName);
        checkSymbol(ensureIsa(fooSymbol2, ParameterizedSymbol.class), symbolName, Kind.METHOD,
                FLAG_NONE,0, 0);
        ASTMethodDeclaration fooMethod = ensureIsa(typeDecl.getMembers().get(1), ASTMethodDeclaration.class);
        assertSame(fooSymbol2, fooMethod.getDeclSymbol());

        symbolName = "foo(Stringy)";
        Symbol fooSymbol3 = innerTable.get(symbolName);
        checkSymbol(ensureIsa(fooSymbol3, ParameterizedSymbol.class), symbolName, Kind.METHOD,
                FLAG_NONE,1, 1);
        ASTMethodDeclaration fooStringMethod = ensureIsa(typeDecl.getMembers().get(2), ASTMethodDeclaration.class);
        assertSame(fooSymbol3, fooStringMethod.getDeclSymbol());
    }

    /**
     * Tests member of interface method declaration.  The interface does not
     * have a default constructor.
     */
    @Test
    public void testMemberInterfaceMethod() {
        Pair pair = createGlobalLookupNoErrors("""
                interface HasMember {
                    void foo(HasMember bar);
                }
                """);
        SymbolTable global = pair.lookup();
        ASTTypeDeclaration typeDecl = pair.typeDecl();
        checkSymbolTable(global, GLOBAL, 2, List.of(UNNAMED_NAMESPACE_NAME, "spruce"));

        ParentSymbol unnamedNamespace = ensureIsa(global.get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        checkSymbol(unnamedNamespace, UNNAMED_NAMESPACE_NAME, Kind.NAMESPACE, FLAG_NONE, 1);
        SymbolTable namespaceTable = unnamedNamespace.getTable();

        String expSymbolName = "HasMember";
        checkSymbolTable(namespaceTable, NAMESPACE, 1, List.of(expSymbolName));
        Symbol symbol = namespaceTable.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), "HasMember", Kind.INTERFACE,
                FLAG_MOD_ABSTRACT, 1);
        assertSame(symbol, typeDecl.getDeclSymbol());

        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        String symbolName = "foo(HasMember)";
        checkSymbolTable(innerTable, TYPE, 1, Arrays.asList(symbolName));

        Symbol fooSymbol = innerTable.get(symbolName);
        checkSymbol(ensureIsa(fooSymbol, ParameterizedSymbol.class), symbolName, Kind.METHOD, FLAG_MOD_ABSTRACT,
                1, 1);
        ASTInterfaceMethodDeclaration fooMethod = ensureIsa(typeDecl.getMembers().get(0),
                ASTInterfaceMethodDeclaration.class);
        assertSame(fooSymbol, fooMethod.getDeclSymbol());

    }

    /**
     * Tests bad interface method with body.
     */
    @Test
    public void testMemberBadInterfaceMethodBody() {
        createGlobalLookupWithErrors("""
                interface Test {
                    void testMethod() {}
                }
                """, 1);
    }

    /**
     * Tests member of shared interface method declaration.  The interface does
     * not have a default constructor.
     */
    @Test
    public void testMemberSharedInterfaceMethod() {
        Pair pair = createGlobalLookupNoErrors("""
                interface HasMember {
                    shared void foo(HasMember bar) {}
                }
                """);
        SymbolTable global = pair.lookup();
        ASTTypeDeclaration typeDecl = pair.typeDecl();
        checkSymbolTable(global, GLOBAL, 2, List.of(UNNAMED_NAMESPACE_NAME, "spruce"));

        ParentSymbol unnamedNamespace = ensureIsa(global.get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        checkSymbol(unnamedNamespace, UNNAMED_NAMESPACE_NAME, Kind.NAMESPACE, FLAG_NONE, 1);
        SymbolTable namespaceTable = unnamedNamespace.getTable();

        String expSymbolName = "HasMember";
        checkSymbolTable(namespaceTable, NAMESPACE, 1, List.of(expSymbolName));
        Symbol symbol = namespaceTable.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), "HasMember", Kind.INTERFACE,
                FLAG_MOD_ABSTRACT, 1);
        assertSame(symbol, typeDecl.getDeclSymbol());

        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        String symbolName = "foo(HasMember)";
        checkSymbolTable(innerTable, TYPE, 1, Arrays.asList(symbolName));

        Symbol fooSymbol = innerTable.get(symbolName);
        checkSymbol(ensureIsa(fooSymbol, ParameterizedSymbol.class), symbolName, Kind.METHOD, FLAG_MOD_SHARED,
                1, 1);
        ASTInterfaceMethodDeclaration fooMethod = ensureIsa(typeDecl.getMembers().get(0),
                ASTInterfaceMethodDeclaration.class);
        assertSame(fooSymbol, fooMethod.getDeclSymbol());
    }

    /**
     * Tests bad shared interface method without body.  The interface does not
     * have a default constructor.
     */
    @Test
    public void testMemberBadSharedInterfaceMethodNoBody() {
        createGlobalLookupWithErrors("""
                interface Test {
                    shared void testMethod();
                }
                """, 1);
    }

    /**
     * Tests formal parameter list in a method.  The class has a default
     * constructor.
     */
    @Test
    public void testFormalParameterList() {
        Pair pair = createGlobalLookupNoErrors("""
                class Stringy {
                    Stringy foo(Stringy first, Stringy last, Stringy age, Stringy ssn) {}
                }
                """);
        SymbolTable global = pair.lookup();
        ASTTypeDeclaration typeDecl = pair.typeDecl();
        checkSymbolTable(global, GLOBAL, 2, List.of(UNNAMED_NAMESPACE_NAME, "spruce"));

        ParentSymbol unnamedNamespace = ensureIsa(global.get(UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        checkSymbol(unnamedNamespace, UNNAMED_NAMESPACE_NAME, Kind.NAMESPACE, FLAG_NONE, 1);

        SymbolTable namespaceTable = unnamedNamespace.getTable();

        String expSymbolName = "Stringy";
        checkSymbolTable(namespaceTable, NAMESPACE, 1, List.of(expSymbolName));
        Symbol symbol = namespaceTable.get(expSymbolName);
        checkSymbol(ensureIsa(symbol, ParentSymbol.class), expSymbolName, Kind.CLASS, FLAG_NONE, 2);
        SymbolTable innerTable = ensureIsa(symbol, ParentSymbol.class).getTable();
        String symbolName = "foo(Stringy, Stringy, Stringy, Stringy)";
        checkSymbolTable(innerTable, TYPE, 2, Arrays.asList(symbolName));

        List<String> expNames = Arrays.asList("first", "last", "age", "ssn");
        ParameterizedSymbol fooSymbol = ensureIsa(innerTable.get(symbolName), ParameterizedSymbol.class);
        checkSymbol(fooSymbol, symbolName, Kind.METHOD, FLAG_NONE, 4, 4);
        SymbolTable paramTable = fooSymbol.getTable();
        checkSymbolTable(paramTable, MEMBER, 4, expNames);
        List<VariableSymbol> parameters = fooSymbol.getParameters();

        assertEquals(4, parameters.size());
        List<Long> expFlags = Arrays.asList(FLAG_NONE, FLAG_NONE, FLAG_NONE, FLAG_NONE);
        ASTMethodDeclaration fooMethod = ensureIsa(typeDecl.getMembers().get(0), ASTMethodDeclaration.class);
        ASTFormalParameterList formalParams = fooMethod.getHeader().getMethodDecl().getFormalParamList();
        for (int i = 0; i < 4; i++) {
            Symbol param = parameters.get(i);
            checkSymbol(param, expNames.get(i), PARAMETER, expFlags.get(i));
            assertSame(param, formalParams.get(i).getDeclSymbol());
        }
    }

    /**
     * Helper method to get a <code>ClassesSymbolCreator</code>.
     * @return A <code>ClassesSymbolCreator</code>.
     */
    public static ClassesSymbolCreator getClassesSymbolCreator() {
        return new SymbolCreator(new BaseMessageProducer(), new GlobalLookup()).getClassesSymbolCreator();
    }

    /**
     * Helper method to get a <code>Pair</code> consisting of a
     * <code>GlobalLookup</code> and <code>SymbolCreator </code>for a top level
     * type declaration directly from code.  Ensures no errors.
     * @param code The code for the top level type declaration.
     * @return A <code>Pair</code> consisting of a <code>GlobalLookup</code> and
     *     a <code>SymbolCreator</code>.
     */
    public static Pair createGlobalLookupNoErrors(String code) {
        ClassesSymbolCreator creator = getClassesSymbolCreator();
        Pair pair = createGlobalLookup(code, creator);
        ensureNoErrors(pair.lookup(), creator);
        return pair;
    }

    /**
     * Helper method to run the symbol creation process for a top level type
     * declaration directly from code.  Expects the given number of errors.
     * @param code The code for the top level type declaration.
     * @param expNumErrors The number of errors expected.
     */
    public static void createGlobalLookupWithErrors(String code, int expNumErrors) {
        ClassesSymbolCreator creator = getClassesSymbolCreator();
        Pair pair = createGlobalLookup(code, creator);
        expectError(pair.lookup(), creator, expNumErrors);
    }

    // Creates some test symbols so early resolution doesn't cause problems here.
    private static Pair createGlobalLookup(String code, ClassesSymbolCreator creator) {
        TopLevelParser topLevelParser = ParserTopLevelTest.getTopLevelParser(code);
        ASTTypeDeclaration typeDecl = topLevelParser.parseTypeDeclaration();
        GlobalLookup global = creator.getGlobalLookup();
        Optional<ParentSymbol> optUnnamedNamespace = global.getNamespace(UNNAMED_NAMESPACE_NAME);
        assertTrue(optUnnamedNamespace.isPresent());
        ParentSymbol unnamedNamespace = optUnnamedNamespace.get();
        ChildSymbolTable table = unnamedNamespace.getTable();
        unnamedNamespace.setTable(table);

        ParentSymbol spruceLang = setupSpruceLangNamespaces(global);
        ChildSymbolTable langTable = spruceLang.getTable();

        langTable.insertSymbol(
                new TypeSymbol(new Location("<fake>", 0, 0, "class Any {}"),
                        "Any", Kind.CLASS, langTable, FLAG_NONE));
        langTable.insertSymbol(
                new TypeSymbol(new Location("<fake>", 0, 0, "class String {}"),
                        "String", Kind.CLASS, langTable, FLAG_NONE));
        langTable.insertSymbol(
                new TypeSymbol(new Location("<fake>", 1, 0, "class Integer {}"),
                        "Integer", Kind.CLASS, langTable, FLAG_NONE));

        creator.createSymbolsForTopLevelTypeDeclarationTypeOnly(typeDecl, unnamedNamespace);
        // Auto-use spruce.lang.
        ResolutionContext fake = new ResolutionContext(new HashMap<>(), typeDecl.getDeclSymbol());
        fake.using().put("lang", spruceLang);
        creator.createSymbolsForTopLevelTypeDeclarationTypeMembers(typeDecl, fake);
        return new Pair(typeDecl, creator.getGlobalLookup());
    }

    private static ParentSymbol setupSpruceLangNamespaces(GlobalLookup global) {
        ParentSymbol spruce = new ParentSymbol(new Location("<fake>", 0, 0, "namespace spruce.lang;"),
                "spruce", Kind.NAMESPACE, global, FLAG_NONE);
        global.insertSymbol(spruce);
        ChildSymbolTable spruceTable = new ChildSymbolTable(NAMESPACE, spruce);
        spruce.setTable(spruceTable);
        ParentSymbol lang = new ParentSymbol(new Location("<fake>", 0, 0, "namespace spruce.lang;"),
                "lang", Kind.NAMESPACE, spruceTable, FLAG_NONE);
        spruceTable.insertSymbol(lang);
        ChildSymbolTable langTable = new ChildSymbolTable(NAMESPACE, lang);
        lang.setTable(langTable);
        return lang;
    }

    public record Pair(ASTTypeDeclaration typeDecl, GlobalLookup lookup) {
    }
}
