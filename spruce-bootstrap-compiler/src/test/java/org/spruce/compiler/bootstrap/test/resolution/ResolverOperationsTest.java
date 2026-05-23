package org.spruce.compiler.bootstrap.test.resolution;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.bootstrap.ast.classes.ASTMethodDeclaration;
import org.spruce.compiler.bootstrap.ast.expressions.*;
import org.spruce.compiler.bootstrap.ast.statements.ASTLocalVariableDeclarationStatement;
import org.spruce.compiler.bootstrap.ast.statements.ASTVariableDeclarator;
import org.spruce.compiler.bootstrap.symbol.ParentSymbol;
import org.spruce.compiler.bootstrap.symbol.SymbolTable;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;
import org.spruce.compiler.bootstrap.symbol.VariableSymbol;

import static org.junit.jupiter.api.Assertions.*;
import static org.spruce.compiler.bootstrap.test.resolution.ResolverTestUtility.*;
import static org.spruce.compiler.bootstrap.test.util.TestUtility.ensureIsa;

/**
 * All tests for the operations resolver.
 */
public class ResolverOperationsTest {

    /**
     * Test binary expression of the plus operator.
     */
    @Test
    public void testBinaryExpressionPlusResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Character c, Integer i, Double d) {
                        Character c1 = c + 'c';
                        Integer i1 = i + '0';
                        Integer i2 = 10 + i;
                        Double d1 = d + 3.14159;
                        Double d2 = 2.71828 + i;
                        Double d3 = d + 'a';
                    }
                }
                """
        );

        ResolverTestUtility.Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol character = ensureIsa(lang.getTable().get("Character"), TypeSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);
        TypeSymbol duble = ensureIsa(lang.getTable().get("Double"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        VariableSymbol c = getFormalParameterSymbol(methodDecl, 0);
        assertSame(character, c.getDataType());
        VariableSymbol i = getFormalParameterSymbol(methodDecl, 1);
        assertSame(integer, i.getDataType());
        VariableSymbol d = getFormalParameterSymbol(methodDecl, 2);
        assertSame(duble, d.getDataType());

        ASTExpression expr0 = getVarInitializerFromVarDecl(methodDecl, 0);
        ASTBinaryExpression binaryExpr0 = ensureIsa(expr0, ASTBinaryExpression.class);
        ASTPrimary primary00 = ensureIsa(binaryExpr0.getFirst(), ASTPrimary.class);
        ASTPrimary primary01 = ensureIsa(binaryExpr0.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary00, primary01, binaryExpr0, character, character, character);

        ASTExpression expr1 = getVarInitializerFromVarDecl(methodDecl, 1);
        ASTBinaryExpression binaryExpr1 = ensureIsa(expr1, ASTBinaryExpression.class);
        ASTPrimary primary10 = ensureIsa(binaryExpr1.getFirst(), ASTPrimary.class);
        ASTPrimary primary11 = ensureIsa(binaryExpr1.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary10, primary11, binaryExpr1, integer, character, integer);

        ASTExpression expr2 = getVarInitializerFromVarDecl(methodDecl, 2);
        ASTBinaryExpression binaryExpr2 = ensureIsa(expr2, ASTBinaryExpression.class);
        ASTPrimary primary20 = ensureIsa(binaryExpr2.getFirst(), ASTPrimary.class);
        ASTPrimary primary21 = ensureIsa(binaryExpr2.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary20, primary21, binaryExpr2, integer, integer, integer);

        ASTExpression expr3 = getVarInitializerFromVarDecl(methodDecl, 3);
        ASTBinaryExpression binaryExpr3 = ensureIsa(expr3, ASTBinaryExpression.class);
        ASTPrimary primary30 = ensureIsa(binaryExpr3.getFirst(), ASTPrimary.class);
        ASTPrimary primary31 = ensureIsa(binaryExpr3.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary30, primary31, binaryExpr3, duble, duble, duble);

        ASTExpression expr4 = getVarInitializerFromVarDecl(methodDecl, 4);
        ASTBinaryExpression binaryExpr4 = ensureIsa(expr4, ASTBinaryExpression.class);
        ASTPrimary primary40 = ensureIsa(binaryExpr4.getFirst(), ASTPrimary.class);
        ASTPrimary primary41 = ensureIsa(binaryExpr4.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary40, primary41, binaryExpr4, duble, integer, duble);

        ASTExpression expr5 = getVarInitializerFromVarDecl(methodDecl, 5);
        ASTBinaryExpression binaryExpr5 = ensureIsa(expr5, ASTBinaryExpression.class);
        ASTPrimary primary50 = ensureIsa(binaryExpr5.getFirst(), ASTPrimary.class);
        ASTPrimary primary51 = ensureIsa(binaryExpr5.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary50, primary51, binaryExpr5, duble, character, duble);
    }

    /**
     * Test binary expression of the string plus operator.
     */
    @Test
    public void testBinaryExpressionStringPlusResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class MacGuffin {}
                """,
                """
                class Test {
                    void testMethod(Integer i, MacGuffin m) {
                        String s1 = "test" + i;
                        String s2 = "test" + 'i';
                        String s3 = "test" + "ing";
                        String s4 = m + "test";
                        String s5 = i + ". test";
                        String s6 = '1' + ". test";
                    }
                }
                """
        );

        ResolverTestUtility.Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        ParentSymbol unnamed = ensureIsa(trio.global().get(SymbolTable.UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
        TypeSymbol character = ensureIsa(lang.getTable().get("Character"), TypeSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);
        TypeSymbol string = ensureIsa(lang.getTable().get("String"), TypeSymbol.class);
        TypeSymbol macGuffin = ensureIsa(unnamed.getTable().get("MacGuffin"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 2, 0);

        ASTExpression expr0 = getVarInitializerFromVarDecl(methodDecl, 0);
        ASTBinaryExpression binaryExpr0 = ensureIsa(expr0, ASTBinaryExpression.class);
        ASTPrimary primary00 = ensureIsa(binaryExpr0.getFirst(), ASTPrimary.class);
        ASTPrimary primary01 = ensureIsa(binaryExpr0.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary00, primary01, binaryExpr0, string, integer, string);

        ASTExpression expr1 = getVarInitializerFromVarDecl(methodDecl, 1);
        ASTBinaryExpression binaryExpr1 = ensureIsa(expr1, ASTBinaryExpression.class);
        ASTPrimary primary10 = ensureIsa(binaryExpr1.getFirst(), ASTPrimary.class);
        ASTPrimary primary11 = ensureIsa(binaryExpr1.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary10, primary11, binaryExpr1, string, character, string);

        ASTExpression expr2 = getVarInitializerFromVarDecl(methodDecl, 2);
        ASTBinaryExpression binaryExpr2 = ensureIsa(expr2, ASTBinaryExpression.class);
        ASTPrimary primary20 = ensureIsa(binaryExpr2.getFirst(), ASTPrimary.class);
        ASTPrimary primary21 = ensureIsa(binaryExpr2.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary20, primary21, binaryExpr2, string, string, string);

        ASTExpression expr3 = getVarInitializerFromVarDecl(methodDecl, 3);
        ASTBinaryExpression binaryExpr3 = ensureIsa(expr3, ASTBinaryExpression.class);
        ASTPrimary primary30 = ensureIsa(binaryExpr3.getFirst(), ASTPrimary.class);
        ASTPrimary primary31 = ensureIsa(binaryExpr3.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary30, primary31, binaryExpr3, macGuffin, string, string);

        ASTExpression expr4 = getVarInitializerFromVarDecl(methodDecl, 4);
        ASTBinaryExpression binaryExpr4 = ensureIsa(expr4, ASTBinaryExpression.class);
        ASTPrimary primary40 = ensureIsa(binaryExpr4.getFirst(), ASTPrimary.class);
        ASTPrimary primary41 = ensureIsa(binaryExpr4.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary40, primary41, binaryExpr4, integer, string, string);

        ASTExpression expr5 = getVarInitializerFromVarDecl(methodDecl, 5);
        ASTBinaryExpression binaryExpr5 = ensureIsa(expr5, ASTBinaryExpression.class);
        ASTPrimary primary50 = ensureIsa(binaryExpr5.getFirst(), ASTPrimary.class);
        ASTPrimary primary51 = ensureIsa(binaryExpr5.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary50, primary51, binaryExpr5, character, string, string);
    }

    /**
     * Test bad binary expression of the plus operator.
     */
    @Test
    public void testBadBinaryExpressionPlusResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class MacGuffin {}
                """,
                """
                class Test {
                    void testMethod(MacGuffin m, Any a) {
                        String bad1 = m + a;
                        String bad2 = m + m;
                        String bad3 = a + a;
                    }
                }
                """
        );

        ResolverTestUtility.Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 3);
    }

    /**
     * Test binary expression of the minus operator.
     */
    @Test
    public void testBinaryExpressionMinusResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Character c, Integer i, Double d) {
                        Character c1 = c - 'c';
                        Integer i1 = i - '0';
                        Integer i2 = 10 - i;
                        Double d1 = d - 3.14159;
                        Double d2 = 2.71828 - i;
                        Double d3 = d - 'a';
                    }
                }
                """
        );

        ResolverTestUtility.Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol character = ensureIsa(lang.getTable().get("Character"), TypeSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);
        TypeSymbol duble = ensureIsa(lang.getTable().get("Double"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        VariableSymbol c = getFormalParameterSymbol(methodDecl, 0);
        assertSame(character, c.getDataType());
        VariableSymbol i = getFormalParameterSymbol(methodDecl, 1);
        assertSame(integer, i.getDataType());
        VariableSymbol d = getFormalParameterSymbol(methodDecl, 2);
        assertSame(duble, d.getDataType());

        ASTExpression expr0 = getVarInitializerFromVarDecl(methodDecl, 0);
        ASTBinaryExpression binaryExpr0 = ensureIsa(expr0, ASTBinaryExpression.class);
        ASTPrimary primary00 = ensureIsa(binaryExpr0.getFirst(), ASTPrimary.class);
        ASTPrimary primary01 = ensureIsa(binaryExpr0.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary00, primary01, binaryExpr0, character, character, character);

        ASTExpression expr1 = getVarInitializerFromVarDecl(methodDecl, 1);
        ASTBinaryExpression binaryExpr1 = ensureIsa(expr1, ASTBinaryExpression.class);
        ASTPrimary primary10 = ensureIsa(binaryExpr1.getFirst(), ASTPrimary.class);
        ASTPrimary primary11 = ensureIsa(binaryExpr1.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary10, primary11, binaryExpr1, integer, character, integer);

        ASTExpression expr2 = getVarInitializerFromVarDecl(methodDecl, 2);
        ASTBinaryExpression binaryExpr2 = ensureIsa(expr2, ASTBinaryExpression.class);
        ASTPrimary primary20 = ensureIsa(binaryExpr2.getFirst(), ASTPrimary.class);
        ASTPrimary primary21 = ensureIsa(binaryExpr2.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary20, primary21, binaryExpr2, integer, integer, integer);

        ASTExpression expr3 = getVarInitializerFromVarDecl(methodDecl, 3);
        ASTBinaryExpression binaryExpr3 = ensureIsa(expr3, ASTBinaryExpression.class);
        ASTPrimary primary30 = ensureIsa(binaryExpr3.getFirst(), ASTPrimary.class);
        ASTPrimary primary31 = ensureIsa(binaryExpr3.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary30, primary31, binaryExpr3, duble, duble, duble);

        ASTExpression expr4 = getVarInitializerFromVarDecl(methodDecl, 4);
        ASTBinaryExpression binaryExpr4 = ensureIsa(expr4, ASTBinaryExpression.class);
        ASTPrimary primary40 = ensureIsa(binaryExpr4.getFirst(), ASTPrimary.class);
        ASTPrimary primary41 = ensureIsa(binaryExpr4.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary40, primary41, binaryExpr4, duble, integer, duble);

        ASTExpression expr5 = getVarInitializerFromVarDecl(methodDecl, 5);
        ASTBinaryExpression binaryExpr5 = ensureIsa(expr5, ASTBinaryExpression.class);
        ASTPrimary primary50 = ensureIsa(binaryExpr5.getFirst(), ASTPrimary.class);
        ASTPrimary primary51 = ensureIsa(binaryExpr5.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary50, primary51, binaryExpr5, duble, character, duble);
    }

    /**
     * Tests bad binary expression resolution of the minus operator.
     */
    @Test
    public void testBadBinaryExpressionMinusResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Character c, String s, Integer i) {
                        Double bad0 = s - c;
                        Double bad1 = i - s;
                        Double bad2 = s - "bad";
                    }
                }
                """
        );
        ResolverTestUtility.Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 3);
    }

    /**
     * Tests binary expression resolution of relational operators.
     */
    @Test
    public void testBinaryExpressionRelationalResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Character c, Integer i, Double d) {
                        Boolean lt = c < c;
                        Boolean le = c <= i;
                        Boolean gt = c > d;
                        Boolean ge = i >= i;
                        Boolean eq = i == d;
                        Boolean ne = d != d;
                    }
                }
                """
        );
        ResolverTestUtility.Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol bool = ensureIsa(lang.getTable().get("Boolean"), TypeSymbol.class);
        TypeSymbol character = ensureIsa(lang.getTable().get("Character"), TypeSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);
        TypeSymbol duble = ensureIsa(lang.getTable().get("Double"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        VariableSymbol c = getFormalParameterSymbol(methodDecl, 0);
        assertSame(character, c.getDataType());
        VariableSymbol i = getFormalParameterSymbol(methodDecl, 1);
        assertSame(integer, i.getDataType());
        VariableSymbol d = getFormalParameterSymbol(methodDecl, 2);
        assertSame(duble, d.getDataType());

        ASTExpression expr0 = getVarInitializerFromVarDecl(methodDecl, 0);
        ASTBinaryExpression binaryExpr0 = ensureIsa(expr0, ASTBinaryExpression.class);
        ASTPrimary primary00 = ensureIsa(binaryExpr0.getFirst(), ASTPrimary.class);
        ASTPrimary primary01 = ensureIsa(binaryExpr0.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary00, primary01, binaryExpr0, character, character, bool);

        ASTExpression expr1 = getVarInitializerFromVarDecl(methodDecl, 1);
        ASTBinaryExpression binaryExpr1 = ensureIsa(expr1, ASTBinaryExpression.class);
        ASTPrimary primary10 = ensureIsa(binaryExpr1.getFirst(), ASTPrimary.class);
        ASTPrimary primary11 = ensureIsa(binaryExpr1.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary10, primary11, binaryExpr1, character, integer, bool);

        ASTExpression expr2 = getVarInitializerFromVarDecl(methodDecl, 2);
        ASTBinaryExpression binaryExpr2 = ensureIsa(expr2, ASTBinaryExpression.class);
        ASTPrimary primary20 = ensureIsa(binaryExpr2.getFirst(), ASTPrimary.class);
        ASTPrimary primary21 = ensureIsa(binaryExpr2.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary20, primary21, binaryExpr2, character, duble, bool);

        ASTExpression expr3 = getVarInitializerFromVarDecl(methodDecl, 3);
        ASTBinaryExpression binaryExpr3 = ensureIsa(expr3, ASTBinaryExpression.class);
        ASTPrimary primary30 = ensureIsa(binaryExpr3.getFirst(), ASTPrimary.class);
        ASTPrimary primary31 = ensureIsa(binaryExpr3.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary30, primary31, binaryExpr3, integer, integer, bool);

        ASTExpression expr4 = getVarInitializerFromVarDecl(methodDecl, 4);
        ASTBinaryExpression binaryExpr4 = ensureIsa(expr4, ASTBinaryExpression.class);
        ASTPrimary primary40 = ensureIsa(binaryExpr4.getFirst(), ASTPrimary.class);
        ASTPrimary primary41 = ensureIsa(binaryExpr4.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary40, primary41, binaryExpr4, integer, duble, bool);

        ASTExpression expr5 = getVarInitializerFromVarDecl(methodDecl, 5);
        ASTBinaryExpression binaryExpr5 = ensureIsa(expr5, ASTBinaryExpression.class);
        ASTPrimary primary50 = ensureIsa(binaryExpr5.getFirst(), ASTPrimary.class);
        ASTPrimary primary51 = ensureIsa(binaryExpr5.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary50, primary51, binaryExpr5, duble, duble, bool);
    }

    /**
     * Tests bad binary expression resolution of relational operators.
     */
    @Test
    public void testBadBinaryExpressionRelationalResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Boolean b, String s, Integer i) {
                        Boolean bad0 = b < i;
                        Boolean bad1 = s < i;
                        Boolean bad2 = b < s;
                    }
                }
                """
        );
        ResolverTestUtility.Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 3);
    }

    /**
     * Tests binary expression resolution of logical operators.
     */
    @Test
    public void testBinaryExpressionLogicalResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Boolean b0, Boolean b1) {
                        Boolean and = b0 && b1;
                        Boolean or = b0 || b1;
                    }
                }
                """
        );
        ResolverTestUtility.Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol bool = ensureIsa(lang.getTable().get("Boolean"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        VariableSymbol b0 = getFormalParameterSymbol(methodDecl, 0);
        assertSame(bool, b0.getDataType());
        VariableSymbol b1 = getFormalParameterSymbol(methodDecl, 0);
        assertSame(bool, b1.getDataType());

        ASTExpression expr0 = getVarInitializerFromVarDecl(methodDecl, 0);
        ASTBinaryExpression binaryExpr0 = ensureIsa(expr0, ASTBinaryExpression.class);
        ASTPrimary primary00 = ensureIsa(binaryExpr0.getFirst(), ASTPrimary.class);
        ASTPrimary primary01 = ensureIsa(binaryExpr0.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary00, primary01, binaryExpr0, bool, bool, bool);

        ASTExpression expr1 = getVarInitializerFromVarDecl(methodDecl, 1);
        ASTBinaryExpression binaryExpr1 = ensureIsa(expr1, ASTBinaryExpression.class);
        ASTPrimary primary10 = ensureIsa(binaryExpr1.getFirst(), ASTPrimary.class);
        ASTPrimary primary11 = ensureIsa(binaryExpr1.getSecond(), ASTPrimary.class);
        checkBinaryExpression(primary10, primary11, binaryExpr1, bool, bool, bool);
    }

    /**
     * Tests bad binary expression resolution of logical operators.
     */
    @Test
    public void testBinaryExpressionBadLogicalResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Boolean b, String s, Integer i) {
                        Boolean bad0 = b && s;
                        Boolean bad1 = i || b;
                        Boolean bad2 = s || i;
                    }
                }
                """
        );
        ResolverTestUtility.Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 3);
    }

    /**
     * Tests unary expression of minus resolution.
     */
    @Test
    public void testUnaryExpressionMinusResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Character c, Integer i, Double d) {
                        Character minusC = -c;
                        Integer minusI = -i;
                        Double minusD = -d;
                    }
                }
                """
        );
        ResolverTestUtility.Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol character = ensureIsa(lang.getTable().get("Character"), TypeSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);
        TypeSymbol duble = ensureIsa(lang.getTable().get("Double"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        VariableSymbol c = getFormalParameterSymbol(methodDecl, 0);
        VariableSymbol i = getFormalParameterSymbol(methodDecl, 1);
        VariableSymbol d = getFormalParameterSymbol(methodDecl, 2);
        assertSame(character, c.getDataType());
        assertSame(integer, i.getDataType());
        assertSame(duble, d.getDataType());

        ASTExpression expr0 = getVarInitializerFromVarDecl(methodDecl, 0);
        ASTUnaryExpression unaryExpr0 = ensureIsa(expr0, ASTUnaryExpression.class);
        ASTPrimary primary0 = ensureIsa(unaryExpr0.getFirst(), ASTPrimary.class);
        checkUnaryExpression(primary0, unaryExpr0, character, character);

        ASTExpression expr1 = getVarInitializerFromVarDecl(methodDecl, 1);
        ASTUnaryExpression unaryExpr1 = ensureIsa(expr1, ASTUnaryExpression.class);
        ASTPrimary primary1 = ensureIsa(unaryExpr1.getFirst(), ASTPrimary.class);
        checkUnaryExpression(primary1, unaryExpr1, integer, integer);

        ASTExpression expr2 = getVarInitializerFromVarDecl(methodDecl, 2);
        ASTUnaryExpression unaryExpr2 = ensureIsa(expr2, ASTUnaryExpression.class);
        ASTPrimary primary2 = ensureIsa(unaryExpr2.getFirst(), ASTPrimary.class);
        checkUnaryExpression(primary2, unaryExpr2, duble, duble);
    }

    /**
     * Tests bad unary expression of minus not numeric.
     */
    @Test
    public void testBadUnaryExpressionMinusResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Boolean b, String s) {
                        Boolean minusB = -b;
                        String minusS = -s;
                    }
                }
                """
        );
        ResolverTestUtility.Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 2);
    }

    /**
     * Tests unary expression of exclamation resolution.
     */
    @Test
    public void testUnaryExpressionBooleanResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Boolean b) {
                        Boolean not = !b;
                    }
                }
                """
        );
        ResolverTestUtility.Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol bool = ensureIsa(lang.getTable().get("Boolean"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        VariableSymbol b = getFormalParameterSymbol(methodDecl, 0);
        assertSame(bool, b.getDataType());

        ASTExpression expr0 = getVarInitializerFromVarDecl(methodDecl, 0);
        ASTUnaryExpression unaryExpr = ensureIsa(expr0, ASTUnaryExpression.class);
        ASTPrimary primary = ensureIsa(unaryExpr.getFirst(), ASTPrimary.class);
        assertSame(bool, primary.getResolvedDataType());
        assertSame(bool, unaryExpr.getResolvedDataType());
        checkUnaryExpression(primary, unaryExpr, bool, bool);
    }

    /**
     * Tests bad unary expression of exclamation not boolean.
     */
    @Test
    public void testBadUnaryExpressionBooleanResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Integer i, String s) {
                        Boolean notI = !i;
                        Boolean notS = !s;
                    }
                }
                """
        );
        ResolverTestUtility.Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver(), 2);
    }

    private void checkBinaryExpression(ASTValueExpression first, ASTValueExpression second,
                                       ASTBinaryExpression binaryExpr, TypeSymbol tsFirst,
                                       TypeSymbol tsSecond, TypeSymbol tsBinary) {
        assertSame(tsFirst, first.getResolvedDataType());
        assertSame(tsSecond, second.getResolvedDataType());
        assertSame(tsBinary, binaryExpr.getResolvedDataType());
    }

    private void checkUnaryExpression(ASTValueExpression first, ASTUnaryExpression unaryExpr,
                                      TypeSymbol tsFirst, TypeSymbol tsUnary) {
        assertSame(tsFirst, first.getResolvedDataType());
        assertSame(tsUnary, unaryExpr.getResolvedDataType());
    }

    private ASTExpression getVarInitializerFromVarDecl(ASTMethodDeclaration methodDecl, int idx) {
        ASTLocalVariableDeclarationStatement localVarDeclStmt = ensureIsa(
                getBlockStatement(methodDecl, idx), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDecl = localVarDeclStmt.getLocalVarDecl().getVarDeclList().get(0);
        Optional<ASTExpression> optInit = varDecl.getVarInitializer();
        assertTrue(optInit.isPresent());
        return optInit.get();
    }
}
