package org.spruce.compiler.bootstrap.test.resolution;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.bootstrap.ast.classes.ASTClassDeclaration;
import org.spruce.compiler.bootstrap.ast.classes.ASTFieldDeclaration;
import org.spruce.compiler.bootstrap.ast.classes.ASTInterfaceDeclaration;
import org.spruce.compiler.bootstrap.ast.classes.ASTMethodDeclaration;
import org.spruce.compiler.bootstrap.ast.expressions.*;
import org.spruce.compiler.bootstrap.ast.literals.*;
import org.spruce.compiler.bootstrap.ast.names.ASTExpressionName;
import org.spruce.compiler.bootstrap.ast.statements.ASTLocalVariableDeclarationStatement;
import org.spruce.compiler.bootstrap.ast.statements.ASTVariableDeclarator;
import org.spruce.compiler.bootstrap.ast.types.ASTDataType;
import org.spruce.compiler.bootstrap.symbol.EntitySymbol;
import org.spruce.compiler.bootstrap.symbol.GlobalLookup;
import org.spruce.compiler.bootstrap.symbol.ParentSymbol;
import org.spruce.compiler.bootstrap.symbol.VariableSymbol;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;

import static org.junit.jupiter.api.Assertions.*;
import static org.spruce.compiler.bootstrap.test.resolution.ResolverTestUtility.*;
import static org.spruce.compiler.bootstrap.test.util.TestUtility.ensureIsa;

/**
 * All tests for the expressions resolver.
 */
public class ResolverExpressionsTest {

    /**
     * Tests resolving a simple expression name to a parameter and resolving
     * a cast expression.
     */
    @Test
    public void testCastExpressionResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Integer i) {
                        Any a = i as Any;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);
        TypeSymbol any = ensureIsa(lang.getTable().get("Any"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        VariableSymbol i = getFormalParameterSymbol(methodDecl, 0);

        ASTLocalVariableDeclarationStatement localVarDeclStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator a = localVarDeclStmt.getLocalVarDecl().getVarDeclList().get(0);

        Optional<ASTExpression> optInitA = a.getVarInitializer();
        assertTrue(optInitA.isPresent());
        ASTCastExpression castExpr = ensureIsa(optInitA.get(), ASTCastExpression.class);

        ASTPrimary exprPrimary = ensureIsa(castExpr.getExpr(), ASTPrimary.class);
        ASTExpressionName exprName = ensureIsa(exprPrimary.getChild(), ASTExpressionName.class);
        EntitySymbol expr = exprName.getResolvedEntity();
        assertSame(i, expr);
        TypeSymbol exprType = expr.getDataType();
        assertSame(integer, exprType);

        ASTDataType target = castExpr.getIntersectionType().get(0);
        assertSame(any, target.getResolvedDataType());
        assertSame(any, castExpr.getResolvedDataType());
        assertTrue(castExpr.getResolvedDataType("Any").isPresent());
        assertSame(any, castExpr.getResolvedDataType("Any").get());
        assertTrue(trio.resolver().getExpressionsResolver().isCastingConvertible(exprType, target.getResolvedDataType()));
    }

    /**
     * Tests cast expression resolution, narrowing class case.
     */
    @Test
    public void testCastExpressionResolutionNarrowingClass() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Any a) {
                        Integer i = a as Integer;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);
        TypeSymbol any = ensureIsa(lang.getTable().get("Any"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        VariableSymbol a = getFormalParameterSymbol(methodDecl, 0);

        ASTLocalVariableDeclarationStatement localVarDeclStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator i = localVarDeclStmt.getLocalVarDecl().getVarDeclList().get(0);

        Optional<ASTExpression> optInitI = i.getVarInitializer();
        assertTrue(optInitI.isPresent());
        ASTCastExpression castExpr = ensureIsa(optInitI.get(), ASTCastExpression.class);

        ASTPrimary exprPrimary = ensureIsa(castExpr.getExpr(), ASTPrimary.class);
        ASTExpressionName exprName = ensureIsa(exprPrimary.getChild(), ASTExpressionName.class);
        EntitySymbol expr = exprName.getResolvedEntity();
        assertSame(a, expr);
        TypeSymbol exprType = expr.getDataType();
        assertSame(any, exprType);

        ASTDataType target = castExpr.getIntersectionType().get(0);
        assertSame(integer, target.getResolvedDataType());
        assertSame(integer, castExpr.getResolvedDataType());
        assertTrue(castExpr.getResolvedDataType("Integer").isPresent());
        assertSame(integer, castExpr.getResolvedDataType("Integer").get());
        assertTrue(trio.resolver().getExpressionsResolver().isCastingConvertible(exprType, target.getResolvedDataType()));
    }

    /**
     * Tests cast expression resolution, interfaces case.
     */
    @Test
    public void testCastExpressionResolutionInterfaces() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Unrelated1 u1) {
                        Unrelated2 u2 = u1 as Unrelated2;
                    }
                }
                interface Unrelated1 {}
                interface Unrelated2 {}
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol unnamed = ensureIsa(trio.global().get(GlobalLookup.UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol unrelated1 = ensureIsa(unnamed.getTable().get("Unrelated1"), TypeSymbol.class);
        TypeSymbol unrelated2 = ensureIsa(unnamed.getTable().get("Unrelated2"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        VariableSymbol a = getFormalParameterSymbol(methodDecl, 0);

        ASTLocalVariableDeclarationStatement localVarDeclStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator u2 = localVarDeclStmt.getLocalVarDecl().getVarDeclList().get(0);

        Optional<ASTExpression> optInitU2 = u2.getVarInitializer();
        assertTrue(optInitU2.isPresent());
        ASTCastExpression castExpr = ensureIsa(optInitU2.get(), ASTCastExpression.class);

        ASTPrimary exprPrimary = ensureIsa(castExpr.getExpr(), ASTPrimary.class);
        ASTExpressionName exprName = ensureIsa(exprPrimary.getChild(), ASTExpressionName.class);
        EntitySymbol expr = exprName.getResolvedEntity();
        assertSame(a, expr);
        TypeSymbol exprType = expr.getDataType();
        assertSame(unrelated1, exprType);

        ASTDataType target = castExpr.getIntersectionType().get(0);
        assertSame(unrelated2, target.getResolvedDataType());
        assertSame(unrelated2, castExpr.getResolvedDataType());
        assertTrue(castExpr.getResolvedDataType("Unrelated2").isPresent());
        assertSame(unrelated2, castExpr.getResolvedDataType("Unrelated2").get());
        assertTrue(trio.resolver().getExpressionsResolver().isCastingConvertible(exprType, target.getResolvedDataType()));
    }

    /**
     * Tests bad cast expression of unrelated classes.
     */
    @Test
    public void testCastExpressionResolutionBadClasses() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Unrelated1 u1) {
                        Unrelated2 u2 = u1 as Unrelated2;
                    }
                }
                class Unrelated1 {}
                class Unrelated2 {}
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests resolving a simple expression name to a local variable and
     * resolving a cast expression with multiple data types in an intersection
     * type.
     */
    @Test
    public void testCastExpressionIntersectionTypeResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod() {
                        Sub s = new Sub();
                        Super t = s as Sub && Super;
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

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclS = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        VariableSymbol s = varDeclS.getDeclSymbol();

        ASTLocalVariableDeclarationStatement localVarDeclStmt1 = ensureIsa(
                getBlockStatement(methodDecl, 1), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator t = localVarDeclStmt1.getLocalVarDecl().getVarDeclList().get(0);

        Optional<ASTExpression> optInitT = t.getVarInitializer();
        assertTrue(optInitT.isPresent());
        ASTCastExpression castExpr = ensureIsa(optInitT.get(), ASTCastExpression.class);

        ASTPrimary exprPrimary = ensureIsa(castExpr.getExpr(), ASTPrimary.class);
        ASTExpressionName exprName = ensureIsa(exprPrimary.getChild(), ASTExpressionName.class);
        EntitySymbol expr = exprName.getResolvedEntity();
        assertSame(s, expr);
        TypeSymbol exprType = expr.getDataType();
        assertSame(sub, exprType);

        ASTDataType interType = castExpr.getIntersectionType().get(0);
        assertSame(sub, interType.getResolvedDataType());
        assertSame(sub, castExpr.getResolvedDataType());
        assertTrue(castExpr.getResolvedDataType("Sub").isPresent());
        assertSame(sub, castExpr.getResolvedDataType("Sub").get());
        assertTrue(castExpr.getResolvedDataType("Super").isPresent());
        assertSame(sooper, castExpr.getResolvedDataType("Super").get());
        TypeSymbol firstCast = castExpr.getResolvedDataType("Sub").get();
        TypeSymbol secondCast = castExpr.getResolvedDataType("Super").get();

        assertTrue(trio.resolver().getExpressionsResolver().isCastingConvertible(exprType, firstCast));
        assertTrue(trio.resolver().getExpressionsResolver().isCastingConvertible(exprType, secondCast));
    }

    /**
     * Tests cast expression with data type not found.
     */
    @Test
    public void testCastExpressionBadDataTypeResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod() {
                        Integer i = 2;
                        Double d = i as DoesNotExist;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests cast expression with repeated data type.
     */
    @Test
    public void testCastExpressionRepeatedDataTypeResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod() {
                        Double i = 2.0;
                        Double d = i as Double && Double;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests that a cast expression whose expression is not a constant
     * expression is itself not a constant expression.
     */
    @Test
    public void testCastExpressionExpressionNotConstantExpression() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    Integer i = 21;
                    void testMethod(List list) {
                        Integer size = list.size() as Integer;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 1);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclJ = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitJ = varDeclJ.getVarInitializer();
        assertTrue(optInitJ.isPresent());

        ASTCastExpression castExpr = ensureIsa(optInitJ.get(), ASTCastExpression.class);
        assertFalse(castExpr.isConstantExpression(), "Should not be a constant expression!");
    }

    /**
     * Tests that a cast expression whose expression is a constant expression
     * and casts to an allowed type is itself a constant expression.
     */
    @Test
    public void testCastExpressionExpressionCastAllowedTypeConstantExpression() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod() {
                        Integer size = 23 as Integer;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclJ = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitJ = varDeclJ.getVarInitializer();
        assertTrue(optInitJ.isPresent());

        ASTCastExpression castExpr = ensureIsa(optInitJ.get(), ASTCastExpression.class);
        assertTrue(castExpr.isConstantExpression(), "Should be a constant expression!");
    }

    /**
     * Tests that a cast expression whose expression is a constant expression
     * and casts to some other disallowed type is not a constant expression.
     */
    @Test
    public void testCastExpressionCastNotAllowedTypeNotConstantExpression() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(List list) {
                        Any a = list as Any;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclJ = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitJ = varDeclJ.getVarInitializer();
        assertTrue(optInitJ.isPresent());

        ASTCastExpression castExpr = ensureIsa(optInitJ.get(), ASTCastExpression.class);
        assertFalse(castExpr.isConstantExpression(), "Should not be a constant expression!");
    }

    /**
     * Tests that a cast expression whose expression is a constant expression
     * and casts to multiple types, including a disallowed type, is not a
     * constant expression.
     */
    @Test
    public void testCastExpressionCastMultipleNotAllowedTypeNotConstantExpression() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Integer i) {
                        Integer a = i as Integer && Any;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclJ = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitJ = varDeclJ.getVarInitializer();
        assertTrue(optInitJ.isPresent());

        ASTCastExpression castExpr = ensureIsa(optInitJ.get(), ASTCastExpression.class);
        assertFalse(castExpr.isConstantExpression(), "Should not be a constant expression!");
    }

    /**
     * Test IsaExpression resolution.
     */
    @Test
    public void testIsaExpressionResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod() {
                        Integer i = 2;
                        Boolean b = i isa Integer;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);
        TypeSymbol bool = ensureIsa(lang.getTable().get("Boolean"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);

        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclI = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        VariableSymbol i = varDeclI.getDeclSymbol();

        ASTLocalVariableDeclarationStatement localVarDeclStmt1 = ensureIsa(
                getBlockStatement(methodDecl, 1), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclB = localVarDeclStmt1.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitB = varDeclB.getVarInitializer();
        assertTrue(optInitB.isPresent());
        ASTIsaExpression isaExpr = ensureIsa(optInitB.get(), ASTIsaExpression.class);
        ASTPrimary primary = ensureIsa(isaExpr.getExpr(), ASTPrimary.class);
        ASTExpressionName exprName = ensureIsa(primary.getChild(), ASTExpressionName.class);
        EntitySymbol resolved = exprName.getResolvedEntity();
        assertSame(i, resolved);
        assertSame(integer, isaExpr.getIsaTarget().getResolvedDataType());
        assertSame(bool, isaExpr.getResolvedDataType());
    }

    /**
     * Test bad IsaExpression resolution.
     */
    @Test
    public void testBadIsaExpressionResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod() {
                        Integer i = 2;
                        Boolean b = i isa DoesNotExist;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests bad IsaExpression of provably distinct classes.
     */
    @Test
    public void testIsaExpressionProvablyDistinctClasses() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod() {
                        Integer i = 2;
                        Boolean b = i isa String;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests that isa expressions are not constant expressions.
     */
    @Test
    public void testIsaExpressionNotConstantExpression() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(String s) {
                        Boolean b = s isa String;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);

        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclB = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        assertTrue(varDeclB.getVarInitializer().isPresent());
        ASTIsaExpression isaExpr = ensureIsa(varDeclB.getVarInitializer().get(), ASTIsaExpression.class);
        assertFalse(isaExpr.isConstantExpression(), "Should not be a constant expression!");
    }

    /**
     * Tests that primary of class instance creation expression is not a
     * constant expression.
     */
    @Test
    public void testPrimaryOfCiceNotConstantExpression() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod() {
                        Test t = new Test();
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclJ = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitJ = varDeclJ.getVarInitializer();
        assertTrue(optInitJ.isPresent());

        ASTPrimary primary = ensureIsa(optInitJ.get(), ASTPrimary.class);
        assertFalse(primary.isConstantExpression(), "Should not be a constant expression!");
    }

    /**
     * Tests that primary of method invocation is not a constant expression.
     */
    @Test
    public void testPrimaryOfMethodInvocationNotConstantExpression() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(List list) {
                        Integer size = list.size();
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclJ = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitJ = varDeclJ.getVarInitializer();
        assertTrue(optInitJ.isPresent());

        ASTPrimary primary = ensureIsa(optInitJ.get(), ASTPrimary.class);
        assertFalse(primary.isConstantExpression(), "Should not be a constant expression!");
    }

    /**
     * Tests that a primary of a parenthesized expression that does not resolve
     * to a constant expression is not a constant expression.
     */
    @Test
    public void testPrimaryOfParenExpressionNotConstantExpression() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Integer i) {
                        Integer j = (i);
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclJ = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitJ = varDeclJ.getVarInitializer();
        assertTrue(optInitJ.isPresent());

        ASTPrimary primary = ensureIsa(optInitJ.get(), ASTPrimary.class);
        assertFalse(primary.isConstantExpression(), "Should not be a constant expression!");
    }

    /**
     * Tests that a primary of a parenthesized expression that does resolve to
     * a constant expression is itself a constant expression.
     */
    @Test
    public void testPrimaryOfParenExpressionConstantExpression() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod() {
                        Integer j = (22);
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclJ = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitJ = varDeclJ.getVarInitializer();
        assertTrue(optInitJ.isPresent());

        ASTPrimary primary = ensureIsa(optInitJ.get(), ASTPrimary.class);
        assertTrue(primary.isConstantExpression(), "Should be a constant expression!");
    }

    /**
     * Test field access of typename super.
     */
    @Test
    public void testPrimaryOfFieldAccessTypenameSuper() {
        List<String> codes = List.of(
                """
                namespace test;
                class Superclass {
                  Integer one = 1;
                }
                class Nesting {
                  class Nested {
                    class DeepNested extends Superclass {
                      class DeepDeepNested {
                        class DeepDeepDeepNested {
                          void testMethod() {
                            Integer i = DeepNested.super.one;
                          }
                        }
                      }
                    }
                  }
                }
                """,
                CODE_SPRUCE_LANG
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);

        ASTClassDeclaration superclass = ensureIsa(trio.ocus().get(0).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTClassDeclaration nestingDecl = ensureIsa(trio.ocus().get(0).getTypeDeclList().get(1), ASTClassDeclaration.class);
        ASTClassDeclaration nestedDecl = ensureIsa(nestingDecl.getClassParts().get(0), ASTClassDeclaration.class);
        ASTClassDeclaration dnDecl = ensureIsa(nestedDecl.getClassParts().get(0), ASTClassDeclaration.class);
        ASTClassDeclaration ddnDecl = ensureIsa(dnDecl.getClassParts().get(0), ASTClassDeclaration.class);
        ASTClassDeclaration dddnDecl = ensureIsa(ddnDecl.getClassParts().get(0), ASTClassDeclaration.class);

        VariableSymbol one = ensureIsa(superclass.getDeclSymbol().getTable().get("one"), VariableSymbol.class);

        ASTMethodDeclaration methodDecl = ensureIsa(dddnDecl.getClassParts().get(0), ASTMethodDeclaration.class);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclI = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitI = varDeclI.getVarInitializer();
        assertTrue(optInitI.isPresent());

        ASTPrimary primary = ensureIsa(optInitI.get(), ASTPrimary.class);
        ASTFieldAccess fieldAccess = ensureIsa(primary.getChild(), ASTFieldAccess.class);
        EntitySymbol expr = fieldAccess.getResolvedEntity();
        assertSame(one, expr);
        assertSame(integer, expr.getDataType());
        assertSame(integer, primary.getResolvedDataType());
    }

    /**
     * Test bad field access of typename super.
     */
    @Test
    public void testPrimaryOfBadFieldAccessTypenameSuper() {
        List<String> codes = List.of(
                """
                namespace test;
                class Superclass {
                  Integer one = 1;
                }
                class Nesting {
                  class Nested {
                    class DeepNested extends Superclass {
                      class DeepDeepNested {
                        class DeepDeepDeepNested {
                          void testMethod() {
                            Integer i = DeepNested.super.two;
                          }
                        }
                      }
                    }
                  }
                }
                """,
                CODE_SPRUCE_LANG
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Test bad field access of bad typename super.
     */
    @Test
    public void testPrimaryOfBadFieldAccessBadTypenameSuper() {
        List<String> codes = List.of(
                """
                namespace test;
                class Superclass {
                  Integer one = 1;
                }
                class Nesting {
                  class Nested {
                    class DeepNested extends Superclass {
                      class DeepDeepNested {
                        class DeepDeepDeepNested {
                          void testMethod() {
                            Integer i = DoesNotExist.super.one;
                          }
                        }
                      }
                    }
                  }
                }
                """,
                CODE_SPRUCE_LANG
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Test bad field access of no superclass in typename super.
     */
    @Test
    public void testPrimaryOfBadFieldAccessTypenameBadSuper() {
        List<String> codes = List.of(
                """
                namespace test;
                class Superclass {
                  Integer one = 1;
                }
                class Nesting {
                  class Nested {
                    class DeepNested extends Superclass {
                      class DeepDeepNested {
                        class DeepDeepDeepNested {
                          void testMethod() {
                            Integer i = Any.super.one;
                          }
                        }
                      }
                    }
                  }
                }
                """,
                CODE_SPRUCE_LANG
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }



    /**
     * Test bad field access of not an enclosing class in typename super.
     */
    @Test
    public void testPrimaryOfBadFieldAccessTypenameSuperNotEnclosing() {
        List<String> codes = List.of(
                """
                namespace test;
                class Superclass {
                  Integer one = 1;
                }
                class Nesting {
                  class Nested {
                    class DeepNested extends Superclass {
                      class DeepDeepNested {
                        class DeepDeepDeepNested {
                          void testMethod() {
                            Integer i = Superclass.super.one;
                          }
                        }
                      }
                    }
                  }
                }
                """,
                CODE_SPRUCE_LANG
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Test field access of super.
     */
    @Test
    public void testPrimaryOfFieldAccessSuper() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Clock {
                    Integer hour;
                    Integer minute;
                    Integer second;
                }
                """,
                """
                class DigitalClock extends Clock {
                    void testMethod() {
                        Integer m = super.minute;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);
        ParentSymbol unnamed = ensureIsa(trio.global().get(GlobalLookup.UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol clock = ensureIsa(unnamed.getTable().get("Clock"), TypeSymbol.class);
        VariableSymbol intMinute = ensureIsa(clock.getTable().get("minute"), VariableSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 2, 0);
        ASTLocalVariableDeclarationStatement localVarDeclStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclM = localVarDeclStmt.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitM = varDeclM.getVarInitializer();
        assertTrue(optInitM.isPresent());

        ASTPrimary primary = ensureIsa(optInitM.get(), ASTPrimary.class);
        ASTFieldAccess minuteField = ensureIsa(primary.getChild(), ASTFieldAccess.class);
        EntitySymbol minute = minuteField.getResolvedEntity();
        assertSame(intMinute, minute);
        assertSame(integer, minute.getDataType());
        assertSame(integer, primary.getResolvedDataType());
    }

    /**
     * Test bad field access of super.
     */
    @Test
    public void testPrimaryOfBadFieldAccessSuper() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Clock {
                    Integer hour;
                    Integer minute;
                    Integer second;
                }
                """,
                """
                class DigitalClock extends Clock {
                    void testMethod() {
                        Integer m = super.day;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests field access of primary.
     */
    @Test
    public void testPrimaryOfFieldAccessPrimary() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Clock {
                    Integer hour;
                    Integer minute;
                    Integer second;
                }
                """,
                """
                class Test {
                    void testMethod(Clock c) {
                        Integer h = (c).hour;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);
        ParentSymbol unnamed = ensureIsa(trio.global().get(GlobalLookup.UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol clock = ensureIsa(unnamed.getTable().get("Clock"), TypeSymbol.class);
        VariableSymbol intHour = ensureIsa(clock.getTable().get("hour"), VariableSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 2, 0);
        ASTLocalVariableDeclarationStatement localVarDeclStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclH = localVarDeclStmt.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitH = varDeclH.getVarInitializer();
        assertTrue(optInitH.isPresent());

        ASTPrimary primary = ensureIsa(optInitH.get(), ASTPrimary.class);
        ASTFieldAccess hourField = ensureIsa(primary.getChild(), ASTFieldAccess.class);
        EntitySymbol hour = hourField.getResolvedEntity();
        assertSame(intHour, hour);
        assertSame(integer, hour.getDataType());
        assertSame(integer, primary.getResolvedDataType());
    }

    /**
     * Tests bad field access of primary.
     */
    @Test
    public void testPrimaryOfBadFieldAccessPrimary() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Clock {
                    Integer hour;
                    Integer minute;
                    Integer second;
                }
                """,
                """
                class Test {
                    void testMethod(Clock c) {
                        Integer h = (c).day;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests that a primary of a field access that doesn't resolve to a
     * field is not a constant expression.
     */
    @Test
    public void testPrimaryOfFieldAccessFieldNotConstantValueNotConstantExpression() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    Integer i = 21;
                    void testMethod(Test t) {
                        Integer j = (t).i;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 1);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclJ = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitJ = varDeclJ.getVarInitializer();
        assertTrue(optInitJ.isPresent());

        ASTPrimary primary = ensureIsa(optInitJ.get(), ASTPrimary.class);
        assertFalse(primary.isConstantExpression(), "Should not be a constant expression!");
    }

    /**
     * Tests that a primary of a field access that does resolve to a field
     * is a constant expression.
     */
    @Test
    public void testPrimaryOfFieldAccessFieldConstantValueConstantExpression() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    constant Integer i = 21;
                    void testMethod(Test t) {
                        Integer j = (t).i;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 1);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclJ = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitJ = varDeclJ.getVarInitializer();
        assertTrue(optInitJ.isPresent());

        ASTPrimary primary = ensureIsa(optInitJ.get(), ASTPrimary.class);
        assertTrue(primary.isConstantExpression(), "Should be a constant expression!");
    }

    /**
     * Tests simple expression name resolution to local variable.
     */
    @Test
    public void testPrimaryOfExpressionNameSimpleResolutionLocal() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod() {
                        Integer i = 2;
                        Integer j = i;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclI = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        VariableSymbol i = varDeclI.getDeclSymbol();

        ASTLocalVariableDeclarationStatement localVarDeclStmt1 = ensureIsa(
                getBlockStatement(methodDecl, 1), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator j = localVarDeclStmt1.getLocalVarDecl().getVarDeclList().get(0);

        Optional<ASTExpression> optInitJ = j.getVarInitializer();
        assertTrue(optInitJ.isPresent());

        ASTPrimary primary = ensureIsa(optInitJ.get(), ASTPrimary.class);
        ASTExpressionName exprName = ensureIsa(primary.getChild(), ASTExpressionName.class);
        EntitySymbol expr = exprName.getResolvedEntity();
        assertSame(i, expr);
        assertSame(integer, expr.getDataType());
        assertSame(integer, primary.getResolvedDataType());
    }

    /**
     * Tests simple expression name resolution to parameter.
     */
    @Test
    public void testPrimaryOfExpressionNameSimpleResolutionParameter() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Integer i) {
                        Integer j = i;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        VariableSymbol i = getFormalParameterSymbol(methodDecl, 0);

        ASTLocalVariableDeclarationStatement localVarDeclStmt1 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator j = localVarDeclStmt1.getLocalVarDecl().getVarDeclList().get(0);

        Optional<ASTExpression> optInitJ = j.getVarInitializer();
        assertTrue(optInitJ.isPresent());

        ASTPrimary primary = ensureIsa(optInitJ.get(), ASTPrimary.class);
        ASTExpressionName exprName = ensureIsa(primary.getChild(), ASTExpressionName.class);
        EntitySymbol expr = exprName.getResolvedEntity();
        assertSame(i, expr);
        assertSame(integer, expr.getDataType());
        assertSame(integer, primary.getResolvedDataType());
    }

    /**
     * Tests simple expression name resolution to field.
     */
    @Test
    public void testPrimaryOfExpressionNameSimpleResolutionField() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    Integer k = 2;
                    void testMethod() {
                        Integer j = i;
                    }
                    Integer i = 2;
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);

        ASTFieldDeclaration fieldDeclI = getField(trio, 1, 0, 2);
        VariableSymbol i = fieldDeclI.getVarDeclList().get(0).getDeclSymbol();

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 1);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator j = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);

        Optional<ASTExpression> optInitJ = j.getVarInitializer();
        assertTrue(optInitJ.isPresent());

        ASTPrimary primary = ensureIsa(optInitJ.get(), ASTPrimary.class);
        ASTExpressionName exprName = ensureIsa(primary.getChild(), ASTExpressionName.class);
        EntitySymbol expr = exprName.getResolvedEntity();
        assertSame(i, expr);
        assertSame(integer, expr.getDataType());
        assertSame(integer, primary.getResolvedDataType());
    }

    /**
     * Tests bad simple expression name resolution.
     */
    @Test
    public void testPrimaryOfExpressionNameSimpleBadResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod() {
                        Integer j = i;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests simple expression name of inherited field.
     */
    @Test
    public void testPrimaryOfExpressionNameSimpleInherited() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Super {
                    Integer inherited;
                }
                class Sub extends Super {
                    void testMethod() {
                        Integer local = inherited;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);

        ASTClassDeclaration superDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTClassDeclaration subDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(1), ASTClassDeclaration.class);
        VariableSymbol inherited = ensureIsa(superDecl.getDeclSymbol().getTable().get("inherited"), VariableSymbol.class);

        ASTMethodDeclaration methodDecl = ensureIsa(subDecl.getClassParts().get(0), ASTMethodDeclaration.class);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclLocal = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitLocal = varDeclLocal.getVarInitializer();
        assertTrue(optInitLocal.isPresent());

        ASTPrimary primary = ensureIsa(optInitLocal.get(), ASTPrimary.class);
        ASTExpressionName exprName = ensureIsa(primary.getChild(), ASTExpressionName.class);
        EntitySymbol expr = exprName.getResolvedEntity();
        assertSame(inherited, expr);
        assertSame(integer, expr.getDataType());
        assertSame(integer, primary.getResolvedDataType());
    }

    /**
     * Tests simple expression name of inherited field from interface.
     */
    @Test
    public void testPrimaryOfExpressionNameSimpleInheritedInterface() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                interface Super {
                    constant Integer inherited = -2;
                }
                class Sub implements Super {
                    void testMethod() {
                        Integer local = inherited;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);

        ASTInterfaceDeclaration superDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTInterfaceDeclaration.class);
        ASTClassDeclaration subDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(1), ASTClassDeclaration.class);
        VariableSymbol inherited = ensureIsa(superDecl.getDeclSymbol().getTable().get("inherited"), VariableSymbol.class);

        ASTMethodDeclaration methodDecl = ensureIsa(subDecl.getClassParts().get(0), ASTMethodDeclaration.class);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclLocal = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitLocal = varDeclLocal.getVarInitializer();
        assertTrue(optInitLocal.isPresent());

        ASTPrimary primary = ensureIsa(optInitLocal.get(), ASTPrimary.class);
        ASTExpressionName exprName = ensureIsa(primary.getChild(), ASTExpressionName.class);
        EntitySymbol expr = exprName.getResolvedEntity();
        assertSame(inherited, expr);
        assertSame(integer, expr.getDataType());
        assertSame(integer, primary.getResolvedDataType());
    }

    /**
     * Tests simple expression name of field from enclosing type.
     */
    @Test
    public void testPrimaryOfExpressionNameSimpleEnclosingType() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Enclosing {
                    Integer outer = 2;
                    class Inner {
                        void testMethod() {
                            Integer local = outer;
                        }
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);

        ASTClassDeclaration enclosingDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTClassDeclaration nestedDecl = ensureIsa(enclosingDecl.getClassParts().get(1), ASTClassDeclaration.class);
        VariableSymbol outer = ensureIsa(enclosingDecl.getDeclSymbol().getTable().get("outer"), VariableSymbol.class);

        ASTMethodDeclaration methodDecl = ensureIsa(nestedDecl.getClassParts().get(0), ASTMethodDeclaration.class);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclLocal = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitLocal = varDeclLocal.getVarInitializer();
        assertTrue(optInitLocal.isPresent());

        ASTPrimary primary = ensureIsa(optInitLocal.get(), ASTPrimary.class);
        ASTExpressionName exprName = ensureIsa(primary.getChild(), ASTExpressionName.class);
        EntitySymbol expr = exprName.getResolvedEntity();
        assertSame(outer, expr);
        assertSame(integer, expr.getDataType());
        assertSame(integer, primary.getResolvedDataType());
    }

    /**
     * Tests simple expression name of inherited constant of superclass of
     * enclosing class.
     */
    @Test
    public void testPrimaryOfExpressionNameSimpleResolutionNested() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                interface Superinterface {
                    constant Integer CONSTANT = 4;
                }
                class Superclass implements Superinterface {}
                class Enclosing extends Superclass {
                    class Inner {
                        void testMethod() {
                            Integer local = CONSTANT;
                        }
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);

        ASTInterfaceDeclaration superintDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTInterfaceDeclaration.class);
        ASTClassDeclaration enclosingDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(2), ASTClassDeclaration.class);
        ASTClassDeclaration innerDecl = ensureIsa(enclosingDecl.getClassParts().get(0), ASTClassDeclaration.class);
        VariableSymbol constant = ensureIsa(superintDecl.getDeclSymbol().getTable().get("CONSTANT"), VariableSymbol.class);

        ASTMethodDeclaration methodDecl = ensureIsa(innerDecl.getClassParts().get(0), ASTMethodDeclaration.class);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclLocal = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitLocal = varDeclLocal.getVarInitializer();
        assertTrue(optInitLocal.isPresent());

        ASTPrimary primary = ensureIsa(optInitLocal.get(), ASTPrimary.class);
        ASTExpressionName exprName = ensureIsa(primary.getChild(), ASTExpressionName.class);
        EntitySymbol expr = exprName.getResolvedEntity();
        assertSame(constant, expr);
        assertSame(integer, expr.getDataType());
        assertSame(integer, primary.getResolvedDataType());
    }

    /**
     * Tests simple expression name of inherited constant of superclass of
     * enclosing class.
     */
    @Test
    public void testPrimaryOfBadExpressionNameSimpleAmbiguous() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                interface Superinterface {
                    constant Integer CONSTANT = 4;
                }
                class Superclass {
                    constant Integer CONSTANT = 5;
                }
                class Enclosing extends Superclass implements Superinterface {
                    class Inner {
                        void testMethod() {
                            Integer local = CONSTANT;
                        }
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests bad simple expression name of static context.
     */
    @Test
    public void testPrimaryOfBadExpressionNameSimpleSharedContext() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    Integer nonShared = 9;
                    shared void testMethod() {
                        Integer local = nonShared;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests simple expression name with shadowed field.
     */
    @Test
    public void testPrimaryOfExpressionNameSimpleShadowedField() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    Integer foo;
                    void setFoo(Integer foo) {
                        Integer local = foo;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);

        ASTClassDeclaration testDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration methodDecl = ensureIsa(testDecl.getClassParts().get(1), ASTMethodDeclaration.class);
        VariableSymbol foo = getFormalParameterSymbol(methodDecl,0);

        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclLocal = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitLocal = varDeclLocal.getVarInitializer();
        assertTrue(optInitLocal.isPresent());

        ASTPrimary primary = ensureIsa(optInitLocal.get(), ASTPrimary.class);
        ASTExpressionName exprName = ensureIsa(primary.getChild(), ASTExpressionName.class);
        EntitySymbol expr = exprName.getResolvedEntity();
        assertSame(foo, expr);
        assertSame(integer, expr.getDataType());
        assertSame(integer, primary.getResolvedDataType());
    }

    /**
     * Tests simple expression name with shadowed field of enclosing type.
     */
    @Test
    public void testPrimaryOfExpressionNameSimpleShadowedFieldEnclosing() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    Integer foo = 9;
                    class Inner {
                        Integer foo = 8;
                        void testMethod() {
                            Integer local = foo;
                        }
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);

        ASTClassDeclaration testDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTClassDeclaration innerDecl = ensureIsa(testDecl.getClassParts().get(1), ASTClassDeclaration.class);
        ASTFieldDeclaration fooDecl = ensureIsa(innerDecl.getClassParts().get(0), ASTFieldDeclaration.class);
        VariableSymbol foo = fooDecl.getVarDeclList().get(0).getDeclSymbol();
        ASTMethodDeclaration methodDecl = ensureIsa(innerDecl.getClassParts().get(1), ASTMethodDeclaration.class);

        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclLocal = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitLocal = varDeclLocal.getVarInitializer();
        assertTrue(optInitLocal.isPresent());

        ASTPrimary primary = ensureIsa(optInitLocal.get(), ASTPrimary.class);
        ASTExpressionName exprName = ensureIsa(primary.getChild(), ASTExpressionName.class);
        EntitySymbol expr = exprName.getResolvedEntity();
        assertSame(foo, expr);
        assertSame(integer, expr.getDataType());
        assertSame(integer, primary.getResolvedDataType());
    }

    /**
     * Tests simple expression name with hidden fields from supertypes.
     */
    @Test
    public void testPrimaryOfExpressionNameSimpleHiddenSupertypes() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Superclass {
                    Integer foo = 5;
                }
                interface Superinterface {
                    constant Integer foo = 4;
                }
                class Test extends Superclass implements Superinterface {
                    Integer foo = 6;
                    void testMethod() {
                        Integer local = foo;
                    }
                }
                """
            );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);

        ASTClassDeclaration testDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(2), ASTClassDeclaration.class);
        ASTFieldDeclaration fooDecl = getField(trio, 1, 2, 0);
        VariableSymbol foo = fooDecl.getVarDeclList().get(0).getDeclSymbol();

        ASTMethodDeclaration methodDecl = ensureIsa(testDecl.getClassParts().get(1), ASTMethodDeclaration.class);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclLocal = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitLocal = varDeclLocal.getVarInitializer();
        assertTrue(optInitLocal.isPresent());

        ASTPrimary primary = ensureIsa(optInitLocal.get(), ASTPrimary.class);
        ASTExpressionName exprName = ensureIsa(primary.getChild(), ASTExpressionName.class);
        EntitySymbol expr = exprName.getResolvedEntity();
        assertSame(foo, expr);
        assertSame(integer, expr.getDataType());
        assertSame(integer, primary.getResolvedDataType());
    }

    /**
     * Tests expression name resolution to a field.
     */
    @Test
    public void testPrimaryOfQualifiedExpressionNameResolutionField() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Clock {
                    Integer hour;
                    Integer minute;
                    Integer second;
                }
                """,
                """
                class Test {
                    void testMethod(Clock c) {
                        Integer h = c.hour;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);
        ParentSymbol unnamed = ensureIsa(trio.global().get(GlobalLookup.UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol clock = ensureIsa(unnamed.getTable().get("Clock"), TypeSymbol.class);
        VariableSymbol intHour = ensureIsa(clock.getTable().get("hour"), VariableSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 2, 0);
        ASTLocalVariableDeclarationStatement localVarDeclStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclH = localVarDeclStmt.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitH = varDeclH.getVarInitializer();
        assertTrue(optInitH.isPresent());

        ASTPrimary primary = ensureIsa(optInitH.get(), ASTPrimary.class);
        ASTExpressionName hourField = ensureIsa(primary.getChild(), ASTExpressionName.class);
        EntitySymbol hour = hourField.getResolvedEntity();
        assertSame(intHour, hour);
        assertSame(integer, hour.getDataType());
        assertSame(integer, primary.getResolvedDataType());
    }

    /**
     * Tests qualified expression name resolution.
     */
    @Test
    public void testPrimaryOfQualifiedExpressionNameResolution() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {
                    constant Integer ONE = 1;
                }
                """,
                """
                class Test {
                    void testMethod() {
                        Integer i = Integer.ONE;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);
        VariableSymbol one = ensureIsa(integer.getTable().get("ONE"), VariableSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclI = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitI = varDeclI.getVarInitializer();
        assertTrue(optInitI.isPresent());

        ASTPrimary primary = ensureIsa(optInitI.get(), ASTPrimary.class);
        ASTExpressionName exprName = ensureIsa(primary.getChild(), ASTExpressionName.class);
        EntitySymbol expr = exprName.getResolvedEntity();
        assertSame(one, expr);
        assertSame(integer, expr.getDataType());
        assertSame(integer, primary.getResolvedDataType());
    }

    /**
     * Tests fully qualified expression name resolution.
     */
    @Test
    public void testPrimaryOfFullyQualifiedExpressionNameResolution() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {
                    constant Integer ONE = 1;
                }
                """,
                """
                class Test {
                    void testMethod() {
                        Integer i = spruce.lang.Integer.ONE;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);
        VariableSymbol one = ensureIsa(integer.getTable().get("ONE"), VariableSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclI = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitI = varDeclI.getVarInitializer();
        assertTrue(optInitI.isPresent());

        ASTPrimary primary = ensureIsa(optInitI.get(), ASTPrimary.class);
        ASTExpressionName exprName = ensureIsa(primary.getChild(), ASTExpressionName.class);
        EntitySymbol expr = exprName.getResolvedEntity();
        assertSame(one, expr);
        assertSame(integer, expr.getDataType());
        assertSame(integer, primary.getResolvedDataType());
    }

    /**
     * Tests bad qualified expression name resolution.
     */
    @Test
    public void testPrimaryOfBadQualifiedExpressionNameResolution() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {
                    constant Integer ONE = 1;
                }
                """,
                """
                class Test {
                    void testMethod() {
                        Integer i = spruce.lang.Double.PI;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests bad last name of qualified expression name resolution.
     */
    @Test
    public void testPrimaryOfBadLastExpressionNameResolution() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {
                    constant Integer ONE = 1;
                }
                """,
                """
                class Test {
                    void testMethod() {
                        Integer i = spruce.lang.Integer.TWO;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests bad qualified expression name of type name then name not shared.
     */
    @Test
    public void testPrimaryOfBadQualifiedExpressionNameNotShared() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Holder {
                    Integer foo = 10;
                }
                class Test {
                    void testMethod() {
                        Integer i = Holder.foo;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests bad qualified expression name of variable then name shared.
     */
    @Test
    public void testPrimaryOfBadQualifiedExpressionNameShared() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Holder {
                    constant Integer foo = 10;
                }
                class Test {
                    void testMethod(Holder h) {
                        Integer i = h.foo;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests expression name deeply nested.
     */
    @Test
    public void testPrimaryOfDeeplyNestedExpressionNameResolution() {
        List<String> codes = List.of(
                """
                namespace test;
                class Nesting {
                  class Nested {
                    class DeepNested {
                      constant Integer ONE = 1;
                      class DeepDeepNested {
                        class DeepDeepDeepNested {
                          void testMethod() {
                            Integer i = DeepNested.ONE;
                          }
                        }
                      }
                    }
                  }
                }
                """,
                CODE_SPRUCE_LANG
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);

        ASTClassDeclaration nestingDecl = ensureIsa(trio.ocus().get(0).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTClassDeclaration nestedDecl = ensureIsa(nestingDecl.getClassParts().get(0), ASTClassDeclaration.class);
        ASTClassDeclaration dnDecl = ensureIsa(nestedDecl.getClassParts().get(0), ASTClassDeclaration.class);
        ASTClassDeclaration ddnDecl = ensureIsa(dnDecl.getClassParts().get(1), ASTClassDeclaration.class);
        ASTClassDeclaration dddnDecl = ensureIsa(ddnDecl.getClassParts().get(0), ASTClassDeclaration.class);

        VariableSymbol one = ensureIsa(dnDecl.getDeclSymbol().getTable().get("ONE"), VariableSymbol.class);

        ASTMethodDeclaration methodDecl = ensureIsa(dddnDecl.getClassParts().get(0), ASTMethodDeclaration.class);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclI = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitI = varDeclI.getVarInitializer();
        assertTrue(optInitI.isPresent());

        ASTPrimary primary = ensureIsa(optInitI.get(), ASTPrimary.class);
        ASTExpressionName exprName = ensureIsa(primary.getChild(), ASTExpressionName.class);
        EntitySymbol expr = exprName.getResolvedEntity();
        assertSame(one, expr);
        assertSame(integer, expr.getDataType());
        assertSame(integer, primary.getResolvedDataType());
    }

    /**
     * Tests that a primary of an expression name that doesn't resolve to a
     * field is not a constant expression.
     */
    @Test
    public void testPrimaryOfExpressionNameNotFieldNotConstantExpression() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Integer i) {
                        Integer j = i;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclJ = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitJ = varDeclJ.getVarInitializer();
        assertTrue(optInitJ.isPresent());

        ASTPrimary primary = ensureIsa(optInitJ.get(), ASTPrimary.class);
        assertFalse(primary.isConstantExpression(), "Should not be a constant expression!");
    }

    /**
     * Tests that a primary of an expression name that doesn't resolve to a
     * field is not a constant expression.
     */
    @Test
    public void testPrimaryOfExpressionNameFieldNotConstantValueNotConstantExpression() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    Integer i = 21;
                    void testMethod() {
                        Integer j = i;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 1);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclJ = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitJ = varDeclJ.getVarInitializer();
        assertTrue(optInitJ.isPresent());

        ASTPrimary primary = ensureIsa(optInitJ.get(), ASTPrimary.class);
        assertFalse(primary.isConstantExpression(), "Should not be a constant expression!");
    }

    /**
     * Tests that a primary of an expression name that does resolve to a field
     * is a constant expression.
     */
    @Test
    public void testPrimaryOfExpressionNameFieldConstantValueConstantExpression() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    constant Integer i = 21;
                    void testMethod() {
                        Integer j = i;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 1);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclJ = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitJ = varDeclJ.getVarInitializer();
        assertTrue(optInitJ.isPresent());

        ASTPrimary primary = ensureIsa(optInitJ.get(), ASTPrimary.class);
        assertTrue(primary.isConstantExpression(), "Should be a constant expression!");
    }

    /**
     * Tests resolution of self.
     */
    @Test
    public void testPrimaryOfSelfResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod() {
                        Test t = self;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol unnamed = ensureIsa(trio.global().get(GlobalLookup.UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol test = ensureIsa(unnamed.getTable().get("Test"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclT = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitT = varDeclT.getVarInitializer();
        assertTrue(optInitT.isPresent());

        ASTPrimary primary = ensureIsa(optInitT.get(), ASTPrimary.class);
        ASTSelf exprSelf = ensureIsa(primary.getChild(), ASTSelf.class);
        assertSame(test, exprSelf.getResolvedDataType());
        assertSame(test, primary.getResolvedDataType());
    }

    /**
     * Tests that a primary of self is not a constant expression.
     */
    @Test
    public void testPrimaryOfSelfNotConstantExpression() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod() {
                        Test t = self;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclI = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitI = varDeclI.getVarInitializer();
        assertTrue(optInitI.isPresent());

        ASTPrimary primary = ensureIsa(optInitI.get(), ASTPrimary.class);
        assertFalse(primary.isConstantExpression(), "Should not be a constant expression!");
    }

    /**
     * Tests resolution of typename self.
     */
    @Test
    public void testPrimaryOfTypenameSelfResolution() {
        List<String> codes = List.of(
                """
                namespace test;
                class Nesting {
                  class Nested {
                    class DeepNested {
                      Integer one = 1;
                      class DeepDeepNested {
                        class DeepDeepDeepNested {
                          void testMethod() {
                            DeepNested dn = DeepNested.self;
                            Integer i = DeepNested.self.one;
                          }
                        }
                      }
                    }
                  }
                }
                """,
                CODE_SPRUCE_LANG
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ASTClassDeclaration nestingDecl = ensureIsa(trio.ocus().get(0).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTClassDeclaration nestedDecl = ensureIsa(nestingDecl.getClassParts().get(0), ASTClassDeclaration.class);
        ASTClassDeclaration dnDecl = ensureIsa(nestedDecl.getClassParts().get(0), ASTClassDeclaration.class);
        ASTClassDeclaration ddnDecl = ensureIsa(dnDecl.getClassParts().get(1), ASTClassDeclaration.class);
        ASTClassDeclaration dddnDecl = ensureIsa(ddnDecl.getClassParts().get(0), ASTClassDeclaration.class);

        TypeSymbol dn = dnDecl.getDeclSymbol();

        ASTMethodDeclaration methodDecl = ensureIsa(dddnDecl.getClassParts().get(0), ASTMethodDeclaration.class);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDecl0 = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInit0 = varDecl0.getVarInitializer();
        assertTrue(optInit0.isPresent());

        ASTPrimary primary0 = ensureIsa(optInit0.get(), ASTPrimary.class);
        ASTTypenameSelf dnSelf0 = ensureIsa(primary0.getChild(), ASTTypenameSelf.class);
        TypeSymbol resolved0 = dnSelf0.getResolvedDataType();
        assertSame(dn, resolved0);

        ASTLocalVariableDeclarationStatement localVarDeclStmt1 = ensureIsa(
                getBlockStatement(methodDecl, 1), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDecl1 = localVarDeclStmt1.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInit1 = varDecl1.getVarInitializer();
        assertTrue(optInit1.isPresent());

        ASTPrimary primary1 = ensureIsa(optInit1.get(), ASTPrimary.class);
        ASTFieldAccess dnOne = ensureIsa(primary1.getChild(), ASTFieldAccess.class);
        assertTrue(dnOne.getPrimary().isPresent());
        ASTPrimary innerPrimary = dnOne.getPrimary().get();
        ASTTypenameSelf dnSelf1 = ensureIsa(innerPrimary.getChild(), ASTTypenameSelf.class);
        TypeSymbol resolved1 = dnSelf1.getResolvedDataType();
        assertSame(dn, resolved1);
    }

    /**
     * Tests resolution of bad typename self.
     */
    @Test
    public void testPrimaryOfBadTypenameSelfResolution() {
        List<String> codes = List.of(
                """
                namespace test;
                class Nesting {
                  class Nested {
                    class DeepNested {
                      Integer one = 1;
                      class DeepDeepNested {
                        class DeepDeepDeepNested {
                          void testMethod() {
                            Integer i = DoesNotExist.self;
                          }
                        }
                      }
                    }
                  }
                }
                """,
                CODE_SPRUCE_LANG
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests resolution of not an enclosing class in typename self.
     */
    @Test
    public void testPrimaryOfNotEnclosingClassTypenameSelfResolution() {
        List<String> codes = List.of(
                """
                namespace test;
                class Nesting {
                  class Nested {
                    class DeepNested {
                      Integer one = 1;
                      class DeepDeepNested {
                        class DeepDeepDeepNested {
                          void testMethod() {
                            Integer i = Any.self;
                          }
                        }
                      }
                    }
                  }
                }
                """,
                CODE_SPRUCE_LANG
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests that a primary of a typename self is not a constant expression.
     */
    @Test
    public void testPrimaryOfTypeNameSelfNotConstantExpression() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Enclosing {
                    class Test {
                        void testMethod() {
                            Enclosing e = Enclosing.self;
                        }
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ASTClassDeclaration enclosingDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTClassDeclaration testDecl = ensureIsa(enclosingDecl.getClassParts().get(0), ASTClassDeclaration.class);

        ASTMethodDeclaration methodDecl = ensureIsa(testDecl.getClassParts().get(0), ASTMethodDeclaration.class);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclE = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitE = varDeclE.getVarInitializer();
        assertTrue(optInitE.isPresent());

        ASTPrimary primary = ensureIsa(optInitE.get(), ASTPrimary.class);
        assertFalse(primary.isConstantExpression(), "Should not be a constant expression!");
    }

    /**
     * Tests class literal resolution.
     */
    @Test
    public void testPrimaryOfClassLiteralResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod() {
                        Class c = String.class;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol clazz = ensureIsa(lang.getTable().get("Class"), TypeSymbol.class);
        TypeSymbol string = ensureIsa(lang.getTable().get("String"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclC = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitC = varDeclC.getVarInitializer();
        assertTrue(optInitC.isPresent());

        ASTPrimary primary = ensureIsa(optInitC.get(), ASTPrimary.class);
        ASTClassLiteral classLiteral = ensureIsa(primary.getChild(), ASTClassLiteral.class);
        TypeSymbol resolved = classLiteral.getResolvedDataType();
        assertSame(clazz, resolved);
        assertSame(clazz, primary.getResolvedDataType());

        Optional<TypeSymbol> optDt = Optional.ofNullable(classLiteral.getDataType().getResolvedDataType());
        assertTrue(optDt.isPresent());
        assertSame(string, optDt.get());
    }

    /**
     * Tests that a primary of a class literal is not a constant expression.
     */
    @Test
    public void testPrimaryOfClassLiteralNotConstantExpression() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod() {
                        Class c = Test.class;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclI = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitI = varDeclI.getVarInitializer();
        assertTrue(optInitI.isPresent());

        ASTPrimary primary = ensureIsa(optInitI.get(), ASTPrimary.class);
        assertFalse(primary.isConstantExpression(), "Should not be a constant expression!");
    }

    /**
     * Tests bad class literal resolution.
     */
    @Test
    public void testPrimaryOfBadClassLiteralResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod() {
                        Class c = DoesNotExist.class;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
    }

    /**
     * Tests string literal type resolution.
     */
    @Test
    public void testPrimaryOfStringLiteralResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod() {
                        String s = "Spruce!";
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol string = ensureIsa(lang.getTable().get("String"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclS = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitS = varDeclS.getVarInitializer();
        assertTrue(optInitS.isPresent());

        ASTPrimary primary = ensureIsa(optInitS.get(), ASTPrimary.class);
        ASTStringLiteral strLiteral = ensureIsa(primary.getChild(), ASTStringLiteral.class);
        TypeSymbol resolved = strLiteral.getResolvedDataType();
        assertSame(string, resolved);
        assertSame(string, primary.getResolvedDataType());
    }

    /**
     * Tests character literal type resolution.
     */
    @Test
    public void testPrimaryOfCharacterLiteralResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod() {
                        Character c = 'c';
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol character = ensureIsa(lang.getTable().get("Character"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclC = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitC = varDeclC.getVarInitializer();
        assertTrue(optInitC.isPresent());

        ASTPrimary primary = ensureIsa(optInitC.get(), ASTPrimary.class);
        ASTCharacterLiteral strLiteral = ensureIsa(primary.getChild(), ASTCharacterLiteral.class);
        TypeSymbol resolved = strLiteral.getResolvedDataType();
        assertSame(character, resolved);
        assertSame(character, primary.getResolvedDataType());
    }

    /**
     * Tests boolean literal type resolution.
     */
    @Test
    public void testPrimaryOfBooleanLiteralResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod() {
                        Boolean t = true;
                        Boolean f = false;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol bool = ensureIsa(lang.getTable().get("Boolean"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclT = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitT = varDeclT.getVarInitializer();
        assertTrue(optInitT.isPresent());

        ASTPrimary primary0 = ensureIsa(optInitT.get(), ASTPrimary.class);
        ASTBooleanLiteral trueLiteral = ensureIsa(primary0.getChild(), ASTBooleanLiteral.class);
        TypeSymbol resolved0 = trueLiteral.getResolvedDataType();
        assertSame(bool, resolved0);
        assertSame(bool, primary0.getResolvedDataType());

        ASTLocalVariableDeclarationStatement localVarDeclStmt1 = ensureIsa(
                getBlockStatement(methodDecl, 1), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclF = localVarDeclStmt1.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitF = varDeclF.getVarInitializer();
        assertTrue(optInitF.isPresent());

        ASTPrimary primary1 = ensureIsa(optInitT.get(), ASTPrimary.class);
        ASTBooleanLiteral falseLiteral = ensureIsa(primary1.getChild(), ASTBooleanLiteral.class);
        TypeSymbol resolved1 = falseLiteral.getResolvedDataType();
        assertSame(bool, resolved1);
        assertSame(bool, primary1.getResolvedDataType());
    }

    /**
     * Tests floating point literal type resolution.
     */
    @Test
    public void testPrimaryOfFloatingPointLiteralResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod() {
                        Double d = 2.0;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol duble = ensureIsa(lang.getTable().get("Double"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclD = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitD = varDeclD.getVarInitializer();
        assertTrue(optInitD.isPresent());

        ASTPrimary primary = ensureIsa(optInitD.get(), ASTPrimary.class);
        ASTFloatingPointLiteral fpLiteral = ensureIsa(primary.getChild(), ASTFloatingPointLiteral.class);
        TypeSymbol resolved = fpLiteral.getResolvedDataType();
        assertSame(duble, resolved);
        assertSame(duble, primary.getResolvedDataType());
    }

    /**
     * Tests integer literal type resolution.
     */
    @Test
    public void testPrimaryOfIntegerLiteralResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod() {
                        Integer i = 2;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclI = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitI = varDeclI.getVarInitializer();
        assertTrue(optInitI.isPresent());

        ASTPrimary primary = ensureIsa(optInitI.get(), ASTPrimary.class);
        ASTIntegerLiteral intLiteral = ensureIsa(primary.getChild(), ASTIntegerLiteral.class);
        TypeSymbol resolved = intLiteral.getResolvedDataType();
        assertSame(integer, resolved);
        assertSame(integer, primary.getResolvedDataType());
    }

    /**
     * Tests primary of a literal is a constant expression.
     */
    @Test
    public void testPrimaryOfLiteralConstantExpression() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod() {
                        Integer i = 20;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclI = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInitI = varDeclI.getVarInitializer();
        assertTrue(optInitI.isPresent());

        ASTPrimary primary = ensureIsa(optInitI.get(), ASTPrimary.class);
        assertTrue(primary.isConstantExpression(), "Should be a constant expression!");
    }
}
