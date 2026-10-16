package org.spruce.compiler.bootstrap.test.resolution;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.bootstrap.ast.classes.ASTClassDeclaration;
import org.spruce.compiler.bootstrap.ast.classes.ASTMethodDeclaration;
import org.spruce.compiler.bootstrap.ast.expressions.ASTExpression;
import org.spruce.compiler.bootstrap.ast.statements.*;
import org.spruce.compiler.bootstrap.ast.types.ASTDataType;
import org.spruce.compiler.bootstrap.symbol.GlobalLookup;
import org.spruce.compiler.bootstrap.symbol.ParentSymbol;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;

import static org.junit.jupiter.api.Assertions.*;
import static org.spruce.compiler.bootstrap.test.resolution.ResolverTestUtility.*;
import static org.spruce.compiler.bootstrap.test.util.TestUtility.ensureIsa;

/**
 * All tests for the statements resolver.
 */
public class ResolverStatementsTest {
    /**
     * Tests data type resolution on a local variable declaration.
     */
    @Test
    public void testLocalVariableDeclarationTypeResolution() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                """,
                """
                use spruce.lang.Integer;
                class Test {
                    void testMethod() {
                        Integer testInt = 3;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);

        ASTClassDeclaration test = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethod = ensureIsa(test.getClassParts().get(0), ASTMethodDeclaration.class);
        assertTrue(testMethod.getBody().getBlock().isPresent());
        ASTBlock block = testMethod.getBody().getBlock().get();
        ASTLocalVariableDeclarationStatement testIntStmt =
                ensureIsa(block.getBlockStmts().get(0), ASTLocalVariableDeclarationStatement.class);
        assertTrue(testIntStmt.getLocalVarDecl().getLocalVarType().getDataType().isPresent());
        ASTDataType dtInteger = testIntStmt.getLocalVarDecl().getLocalVarType().getDataType().get();
        TypeSymbol target = dtInteger.getResolvedDataType();
        assertSame(integer, target);

        assertTrue(testIntStmt.getLocalVarDecl().getVarDeclList().get(0).getVarInitializer().isPresent());
        ASTExpression expression = testIntStmt.getLocalVarDecl().getVarDeclList().get(0).getVarInitializer().get();
        TypeSymbol expr = expression.getResolvedDataType();
        assertSame(integer, expr);
        assertTrue(trio.resolver().getStatementsResolver().isAssignmentConvertible(expr, target));
    }

    /**
     * Tests data type resolution on a bad local variable declaration.
     */
    @Test
    public void testLocalVariableDeclarationBadTypeResolution() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                """,
                """
                use spruce.lang.Integer;
                class Test {
                    void testMethod() {
                        DoesNotExist dne;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Test local variable declaration, initializer type matches declared type.
     */
    @Test
    public void testLocalVariableDeclarationTypeMatches() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                use spruce.lang.Integer;
                class Test {
                    void testMethod(Integer i) {
                        Integer j = i - 1;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);

        ASTClassDeclaration test = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethod = ensureIsa(test.getClassParts().get(0), ASTMethodDeclaration.class);
        assertTrue(testMethod.getBody().getBlock().isPresent());
        ASTBlock block = testMethod.getBody().getBlock().get();
        ASTLocalVariableDeclarationStatement testIntStmt =
                ensureIsa(block.getBlockStmts().get(0), ASTLocalVariableDeclarationStatement.class);
        assertTrue(testIntStmt.getLocalVarDecl().getLocalVarType().getDataType().isPresent());
        ASTDataType dtInteger = testIntStmt.getLocalVarDecl().getLocalVarType().getDataType().get();
        TypeSymbol target = dtInteger.getResolvedDataType();
        assertSame(integer, target);

        assertTrue(testIntStmt.getLocalVarDecl().getVarDeclList().get(0).getVarInitializer().isPresent());
        ASTExpression expression = testIntStmt.getLocalVarDecl().getVarDeclList().get(0).getVarInitializer().get();
        TypeSymbol expr = expression.getResolvedDataType();
        assertSame(integer, expr);
        assertTrue(trio.resolver().getStatementsResolver().isAssignmentConvertible(expr, target));
    }

    /**
     * Test local variable declaration, initializer type is subtype of declared
     * type.
     */
    @Test
    public void testLocalVariableDeclarationInitSubtype() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                use spruce.lang.Integer;
                class Test {
                    void testMethod(Sub s) {
                        Super t = s;
                    }
                }
                class Super {}
                class Sub extends Super {}
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol unnamed = ensureIsa(trio.global().get(GlobalLookup.UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol sooper = ensureIsa(unnamed.getTable().get("Super"), TypeSymbol.class);
        TypeSymbol sub = ensureIsa(unnamed.getTable().get("Sub"), TypeSymbol.class);

        ASTClassDeclaration test = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethod = ensureIsa(test.getClassParts().get(0), ASTMethodDeclaration.class);
        assertTrue(testMethod.getBody().getBlock().isPresent());
        ASTBlock block = testMethod.getBody().getBlock().get();
        ASTLocalVariableDeclarationStatement localVarDecl = ensureIsa(block.getBlockStmts().get(0),
                ASTLocalVariableDeclarationStatement.class);
        assertTrue(localVarDecl.getLocalVarDecl().getLocalVarType().getDataType().isPresent());
        TypeSymbol target = localVarDecl.getLocalVarDecl().getLocalVarType().getDataType().get().getResolvedDataType();
        assertSame(sooper, target);
        assertTrue(localVarDecl.getLocalVarDecl().getVarDeclList().get(0).getVarInitializer().isPresent());
        TypeSymbol expr = localVarDecl.getLocalVarDecl().getVarDeclList().get(0).getVarInitializer().get().getResolvedDataType();
        assertSame(sub, expr);
        assertTrue(trio.resolver().getStatementsResolver().isAssignmentConvertible(expr, target));
    }

    /**
     * Test local variable declaration, bad initializer, type mismatch.
     */
    @Test
    public void testBadLocalVariableDeclarationTypeMismatch() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                use spruce.lang.Integer;
                class Test {
                    void testMethod(String s) {
                        Integer i = s;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);
        TypeSymbol string = ensureIsa(lang.getTable().get("String"), TypeSymbol.class);

        ASTClassDeclaration test = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethod = ensureIsa(test.getClassParts().get(0), ASTMethodDeclaration.class);
        assertTrue(testMethod.getBody().getBlock().isPresent());
        ASTBlock block = testMethod.getBody().getBlock().get();
        ASTLocalVariableDeclarationStatement testIntStmt =
                ensureIsa(block.getBlockStmts().get(0), ASTLocalVariableDeclarationStatement.class);
        assertTrue(testIntStmt.getLocalVarDecl().getLocalVarType().getDataType().isPresent());
        ASTDataType dtInteger = testIntStmt.getLocalVarDecl().getLocalVarType().getDataType().get();
        TypeSymbol target = dtInteger.getResolvedDataType();
        assertSame(integer, target);

        assertTrue(testIntStmt.getLocalVarDecl().getVarDeclList().get(0).getVarInitializer().isPresent());
        ASTExpression expression = testIntStmt.getLocalVarDecl().getVarDeclList().get(0).getVarInitializer().get();
        TypeSymbol expr = expression.getResolvedDataType();
        assertSame(string, expr);
        assertFalse(trio.resolver().getStatementsResolver().isAssignmentConvertible(expr, target));
    }

    /**
     * Tests data type resolution on a local variable declaration in an
     * enhanced for statement.
     */
    @Test
    public void testEnhancedForStmtLocalVarDeclTypeResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(List list) {
                        for (Integer i in list) {
                        }
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        ParentSymbol integer = ensureIsa(lang.getTable().get("Integer"), ParentSymbol.class);

        ASTClassDeclaration test = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethod = ensureIsa(test.getClassParts().get(0), ASTMethodDeclaration.class);
        assertTrue(testMethod.getBody().getBlock().isPresent());
        ASTBlock block = testMethod.getBody().getBlock().get();
        ASTEnhancedForStatement enhancedForStmt =
                ensureIsa(block.getBlockStmts().get(0), ASTEnhancedForStatement.class);
        assertTrue(enhancedForStmt.getLocalVarDecl().getLocalVarType().getDataType().isPresent());
        ASTDataType dtInteger = enhancedForStmt.getLocalVarDecl().getLocalVarType().getDataType().get();
        assertSame(integer, dtInteger.getResolvedDataType());
    }

    /**
     * Tests enhanced for statement with bad expression that isn't Iterable.
     */
    @Test
    public void testEnhancedForStatementBadExpressionNotIterable() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod() {
                        for (Integer i in "list") {
                        }
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests data type resolution on a local variable declaration in a
     * basic for statement.
     */
    @Test
    public void testBasicForStatementInitLocalVarDeclTypeResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(List list) {
                        for (Integer i = 0; i < list.size(); i = i + 1) {
                        }
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        ParentSymbol integer = ensureIsa(lang.getTable().get("Integer"), ParentSymbol.class);

        ASTClassDeclaration test = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethod = ensureIsa(test.getClassParts().get(0), ASTMethodDeclaration.class);
        assertTrue(testMethod.getBody().getBlock().isPresent());
        ASTBlock block = testMethod.getBody().getBlock().get();
        ASTBasicForStatement basicForStmt =
                ensureIsa(block.getBlockStmts().get(0), ASTBasicForStatement.class);
        assertTrue(basicForStmt.getInit().isPresent());
        ASTLocalVariableDeclaration localVarDecl = ensureIsa(
                basicForStmt.getInit().get(), ASTLocalVariableDeclaration.class);
        assertTrue(localVarDecl.getLocalVarType().getDataType().isPresent());
        ASTDataType dtInteger = localVarDecl.getLocalVarType().getDataType().get();
        assertSame(integer, dtInteger.getResolvedDataType());
    }

    /**
     * Tests data type resolution on a local variable declaration in if
     * statements.
     */
    @Test
    public void testIfStatementInitLocalVarDeclTypeResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                use spruce.lang.{Integer, List};
                class Test {
                    void testMethod(List list) {
                        if {Integer size = list.size()} (size > 10) {
                            //stdout.println("Big");
                        }
                        else if {Integer first = list.get(0) as Integer} (first > 10) {
                            //stdout.println("First is big");
                        }
                        else {
                            //stdout.println("Else");
                        }
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        ParentSymbol integer = ensureIsa(lang.getTable().get("Integer"), ParentSymbol.class);

        ASTClassDeclaration test = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethod = ensureIsa(test.getClassParts().get(0), ASTMethodDeclaration.class);
        assertTrue(testMethod.getBody().getBlock().isPresent());
        ASTBlock block = testMethod.getBody().getBlock().get();

        ASTIfStatement ifStmt = ensureIsa(block.getBlockStmts().get(0), ASTIfStatement.class);
        assertTrue(ifStmt.getInit().isPresent());
        ASTLocalVariableDeclaration localVarDecl = ensureIsa(
                ifStmt.getInit().get(), ASTLocalVariableDeclaration.class);
        assertTrue(localVarDecl.getLocalVarType().getDataType().isPresent());
        ASTDataType dtInteger = localVarDecl.getLocalVarType().getDataType().get();
        assertSame(integer, dtInteger.getResolvedDataType());

        assertTrue(ifStmt.getElseIf().isPresent());
        ASTIfStatement elseIfStmt = ifStmt.getElseIf().get();
        assertTrue(elseIfStmt.getInit().isPresent());
        ASTLocalVariableDeclaration localVarDecl2 = ensureIsa(
                elseIfStmt.getInit().get(), ASTLocalVariableDeclaration.class);
        assertTrue(localVarDecl2.getLocalVarType().getDataType().isPresent());
        ASTDataType dtInteger2 = localVarDecl.getLocalVarType().getDataType().get();
        assertSame(integer, dtInteger2.getResolvedDataType());
    }

    /**
     * Tests data type resolution on a local variable declaration in a while
     * statement.
     */
    @Test
    public void testWhileStatementInitLocalVarDeclTypeResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                use spruce.lang.{Integer, List};
                class Test {
                    void testMethod(List list) {
                        while {Integer i = 0} (i < list.size()) {
                            i = i + 1;
                        }
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        ParentSymbol integer = ensureIsa(lang.getTable().get("Integer"), ParentSymbol.class);

        ASTClassDeclaration test = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethod = ensureIsa(test.getClassParts().get(0), ASTMethodDeclaration.class);
        assertTrue(testMethod.getBody().getBlock().isPresent());
        ASTBlock block = testMethod.getBody().getBlock().get();

        ASTWhileStatement whileStmt = ensureIsa(block.getBlockStmts().get(0), ASTWhileStatement.class);
        assertTrue(whileStmt.getInit().isPresent());
        ASTLocalVariableDeclaration localVarDecl = ensureIsa(
                whileStmt.getInit().get(), ASTLocalVariableDeclaration.class);
        assertTrue(localVarDecl.getLocalVarType().getDataType().isPresent());
        ASTDataType dtInteger = localVarDecl.getLocalVarType().getDataType().get();
        assertSame(integer, dtInteger.getResolvedDataType());
    }

    /**
     * Tests that an if statement's expression is boolean.
     */
    @Test
    public void testIfStatementExprBoolean() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(List list, Integer i) {
                        if (i < list.size()) {
                            i = i + 1;
                        }
                    }
                }
                """
        );

        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());
    }

    /**
     * Tests if statement of bad non-boolean expression.
     */
    @Test
    public void testIfStatementBadExprNotBoolean() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(List list, Integer i) {
                        if (i) {
                            i = i + 1;
                        }
                    }
                }
                """
        );

        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests that a while statement's expression is boolean.
     */
    @Test
    public void testWhileStatementExprBoolean() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(List list) {
                        while {Integer i = 0} (i < list.size()) {
                            i = i + 1;
                        }
                    }
                }
                """
        );

        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());
    }

    /**
     * Tests while statement bad non-boolean expression.
     */
    @Test
    public void testWhileStatementBadExprNotBoolean() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(List list) {
                        while {Integer i = 0} ("bad") {
                            i = i + 1;
                        }
                    }
                }
                """
        );

        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests that a basic for statement's expression is boolean.
     */
    @Test
    public void testBasicForStatementExprBoolean() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(List list) {
                        for (Integer i = 0; i < list.size(); i = i + 1) {
                            Integer j = i + 1;
                        }
                    }
                }
                """
        );

        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());
    }

    /**
     * Tests basic for statement with bad non-boolean expression.
     */
    @Test
    public void testBasicForStatementBadExprNotBoolean() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(List list) {
                        for (Integer i = 0; list; i = i + 1) {
                            Integer j = i + 1;
                        }
                    }
                }
                """
        );

        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests return statement resolution with an expression.
     */
    @Test
    public void testReturnStatementExpressionResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    Integer testMethod(List list) {
                        return 4;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);

        ASTClassDeclaration test = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethod = ensureIsa(test.getClassParts().get(0), ASTMethodDeclaration.class);
        TypeSymbol target = testMethod.getHeader().getResult().getResolvedDataType();
        assertSame(integer, target);

        assertTrue(testMethod.getBody().getBlock().isPresent());
        ASTBlock block = testMethod.getBody().getBlock().get();
        ASTReturnStatement returnStmt = ensureIsa(block.getBlockStmts().get(0), ASTReturnStatement.class);
        assertTrue(returnStmt.getExpr().isPresent());
        TypeSymbol expr = returnStmt.getExpr().get().getResolvedDataType();
        assertSame(integer, expr);
        assertTrue(trio.resolver().getStatementsResolver().isAssignmentConvertible(expr, target));
    }

    /**
     * Tests return statement resolution with no expression.
     */
    @Test
    public void testReturnStatementResolution() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                class List {}
                """,
                """
                class Test {
                    void testMethod(List list) {
                        return;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ASTClassDeclaration test = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethod = ensureIsa(test.getClassParts().get(0), ASTMethodDeclaration.class);
        assertSame(TypeSymbol.VOID, testMethod.getHeader().getResult().getResolvedDataType());

        assertTrue(testMethod.getBody().getBlock().isPresent());
        ASTBlock block = testMethod.getBody().getBlock().get();
        ASTReturnStatement returnStmt = ensureIsa(block.getBlockStmts().get(0), ASTReturnStatement.class);
        assertFalse(returnStmt.getExpr().isPresent());
        assertSame(TypeSymbol.VOID, testMethod.getHeader().getResult().getResolvedDataType());
    }

    /**
     * Tests return statement type matches method return type.
     */
    @Test
    public void testReturnStatementMatchesMethodReturnType() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                class List {
                    Integer size() { return 0; }
                }
                """,
                """
                class Test {
                    Integer testMethod(List list) {
                        return list.size();
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);

        ASTClassDeclaration test = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethod = ensureIsa(test.getClassParts().get(0), ASTMethodDeclaration.class);
        TypeSymbol target = testMethod.getHeader().getResult().getResolvedDataType();
        assertSame(integer, target);

        assertTrue(testMethod.getBody().getBlock().isPresent());
        ASTBlock block = testMethod.getBody().getBlock().get();
        ASTReturnStatement returnStmt = ensureIsa(block.getBlockStmts().get(0), ASTReturnStatement.class);
        assertTrue(returnStmt.getExpr().isPresent());
        TypeSymbol expr = returnStmt.getExpr().get().getResolvedDataType();
        assertSame(integer, expr);
        assertTrue(trio.resolver().getStatementsResolver().isAssignmentConvertible(expr, target));
    }

    /**
     * Tests return statement type is subtype of method's return type.
     */
    @Test
    public void testReturnStatementTypeSubTypeOfMethodReturnType() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                """,
                """
                class Test {
                    Super testMethod(Sub s) {
                        return s;
                    }
                }
                class Super {}
                class Sub extends Super {}
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol unnamed = ensureIsa(trio.global().get(GlobalLookup.UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol sooper = ensureIsa(unnamed.getTable().get("Super"), TypeSymbol.class);
        TypeSymbol sub = ensureIsa(unnamed.getTable().get("Sub"), TypeSymbol.class);

        ASTClassDeclaration test = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethod = ensureIsa(test.getClassParts().get(0), ASTMethodDeclaration.class);
        TypeSymbol target = testMethod.getHeader().getResult().getResolvedDataType();
        assertSame(sooper, target);

        assertTrue(testMethod.getBody().getBlock().isPresent());
        ASTBlock block = testMethod.getBody().getBlock().get();
        ASTReturnStatement returnStmt = ensureIsa(block.getBlockStmts().get(0), ASTReturnStatement.class);
        assertTrue(returnStmt.getExpr().isPresent());
        TypeSymbol expr = returnStmt.getExpr().get().getResolvedDataType();
        assertSame(sub, expr);
        assertTrue(trio.resolver().getStatementsResolver().isAssignmentConvertible(expr, target));
    }

    /**
     * Tests bad return statement type that doesn't match the method's return
     * type.
     */
    @Test
    public void testReturnStatementTypeDoesNotMatchMethodReturnType() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    String testMethod(Double d) {
                        return d;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol duble = ensureIsa(lang.getTable().get("Double"), TypeSymbol.class);
        TypeSymbol string = ensureIsa(lang.getTable().get("String"), TypeSymbol.class);

        ASTClassDeclaration test = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethod = ensureIsa(test.getClassParts().get(0), ASTMethodDeclaration.class);
        TypeSymbol target = testMethod.getHeader().getResult().getResolvedDataType();
        assertSame(string, target);

        assertTrue(testMethod.getBody().getBlock().isPresent());
        ASTBlock block = testMethod.getBody().getBlock().get();
        ASTReturnStatement returnStmt = ensureIsa(block.getBlockStmts().get(0), ASTReturnStatement.class);
        assertTrue(returnStmt.getExpr().isPresent());
        TypeSymbol expr = returnStmt.getExpr().get().getResolvedDataType();
        assertSame(duble, expr);
        assertFalse(trio.resolver().getStatementsResolver().isAssignmentConvertible(expr, target));
    }

    /**
     * Tests bad return statement with expression in void method.
     */
    @Test
    public void testBadReturnStatementExprInVoidMethod() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Double d) {
                        return d;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests bad return statement without expression in non-void method.
     */
    @Test
    public void testBadReturnStatementNoExprInNonVoidMethod() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    Integer testMethod() {
                        return;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests that return type matching occurs with basic for statements.
     */
    @Test
    public void testBadReturnInBasicForStatement() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    Integer testMethod() {
                        for (Integer i = 0; i < 10; i = i + 1) {
                            return;
                        }
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests that return type matching occurs with enhanced for statements.
     */
    @Test
    public void testBadReturnInEnhancedForStatement() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    Integer testMethod(List list) {
                        for (Integer i in list) {
                            return;
                        }
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests that return type matching occurs with if statements.
     */
    @Test
    public void testBadReturnInIfStatement() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    Integer testMethod(Integer i) {
                        if i < 5 {
                            return;
                        }
                        else if i < 10 {
                            return;
                        }
                        else {
                            return;
                        }
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 3);
    }

    /**
     * Tests that return type matching occurs with while statements.
     */
    @Test
    public void testBadReturnInWhileStatement() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    Integer testMethod(Integer i) {
                        while i < 5 {
                            return;
                        }
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests that return type matching occurs with subblocks.
     */
    @Test
    public void testBadReturnInSubblock() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    Integer testMethod(Integer i) {
                        {
                            return;
                        }
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests assignment of expression matching declared type.
     */
    @Test
    public void testAssignmentMatches() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Integer i, Integer j) {
                        j = i - 1;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);

        ASTClassDeclaration test = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethod = ensureIsa(test.getClassParts().get(0), ASTMethodDeclaration.class);
        assertTrue(testMethod.getBody().getBlock().isPresent());
        ASTBlock block = testMethod.getBody().getBlock().get();
        ASTExpressionStatement testAssignmentStmt =
                ensureIsa(block.getBlockStmts().get(0), ASTExpressionStatement.class);
        ASTAssignment assignment = ensureIsa(testAssignmentStmt.getStmtExpr(), ASTAssignment.class);
        TypeSymbol target = assignment.getLeftHandSide().getResolvedDataType();
        assertSame(integer, target);
        TypeSymbol expr = assignment.getExpr().getResolvedDataType();
        assertSame(integer, expr);
        assertTrue(trio.resolver().getStatementsResolver().isAssignmentConvertible(expr, target));
    }

    /**
     * Tests assignment of expression that is a subtype of the declared type.
     */
    @Test
    public void testAssignmentSubtype() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Sub s, Super t) {
                        t = s;
                    }
                }
                class Super {}
                class Sub extends Super {}
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol unnamed = ensureIsa(trio.global().get(GlobalLookup.UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol sooper = ensureIsa(unnamed.getTable().get("Super"), TypeSymbol.class);
        TypeSymbol sub = ensureIsa(unnamed.getTable().get("Sub"), TypeSymbol.class);

        ASTClassDeclaration test = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethod = ensureIsa(test.getClassParts().get(0), ASTMethodDeclaration.class);
        assertTrue(testMethod.getBody().getBlock().isPresent());
        ASTBlock block = testMethod.getBody().getBlock().get();
        ASTExpressionStatement testAssignmentStmt =
                ensureIsa(block.getBlockStmts().get(0), ASTExpressionStatement.class);
        ASTAssignment assignment = ensureIsa(testAssignmentStmt.getStmtExpr(), ASTAssignment.class);
        TypeSymbol target = assignment.getLeftHandSide().getResolvedDataType();
        assertSame(sooper, target);
        TypeSymbol expr = assignment.getExpr().getResolvedDataType();
        assertSame(sub, expr);
        assertTrue(trio.resolver().getStatementsResolver().isAssignmentConvertible(expr, target));
    }

    /**
     * Tests bad assignment of expression that doesn't match the declared type.
     */
    @Test
    public void testBadAssignmentMismatch() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Sub s) {
                        String t;
                        t = s;
                    }
                }
                class Super {}
                class Sub extends Super {}
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol string = ensureIsa(lang.getTable().get("String"), TypeSymbol.class);
        ParentSymbol unnamed = ensureIsa(trio.global().get(GlobalLookup.UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol sub = ensureIsa(unnamed.getTable().get("Sub"), TypeSymbol.class);

        ASTClassDeclaration test = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethod = ensureIsa(test.getClassParts().get(0), ASTMethodDeclaration.class);
        assertTrue(testMethod.getBody().getBlock().isPresent());
        ASTBlock block = testMethod.getBody().getBlock().get();
        ASTExpressionStatement testAssignmentStmt =
                ensureIsa(block.getBlockStmts().get(1), ASTExpressionStatement.class);
        ASTAssignment assignment = ensureIsa(testAssignmentStmt.getStmtExpr(), ASTAssignment.class);
        TypeSymbol target = assignment.getLeftHandSide().getResolvedDataType();
        assertSame(string, target);
        TypeSymbol expr = assignment.getExpr().getResolvedDataType();
        assertSame(sub, expr);
        assertFalse(trio.resolver().getStatementsResolver().isAssignmentConvertible(expr, target));
    }
}
