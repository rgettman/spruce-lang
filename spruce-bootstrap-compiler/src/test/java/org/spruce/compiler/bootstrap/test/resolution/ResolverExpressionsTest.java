package org.spruce.compiler.bootstrap.test.resolution;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.bootstrap.ast.classes.ASTClassDeclaration;
import org.spruce.compiler.bootstrap.ast.classes.ASTMethodDeclaration;
import org.spruce.compiler.bootstrap.ast.expressions.*;
import org.spruce.compiler.bootstrap.ast.literals.*;
import org.spruce.compiler.bootstrap.ast.names.ASTExpressionName;
import org.spruce.compiler.bootstrap.ast.statements.ASTLocalVariableDeclarationStatement;
import org.spruce.compiler.bootstrap.ast.statements.ASTVariableDeclarator;
import org.spruce.compiler.bootstrap.ast.types.ASTDataType;
import org.spruce.compiler.bootstrap.symbol.EntitySymbol;
import org.spruce.compiler.bootstrap.symbol.ParentSymbol;
import org.spruce.compiler.bootstrap.symbol.SymbolTable;
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
                        Double d = i as Double;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);
        TypeSymbol duble = ensureIsa(lang.getTable().get("Double"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        VariableSymbol i = getFormalParameterSymbol(methodDecl, 0);

        ASTLocalVariableDeclarationStatement localVarDeclStmt = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator d = localVarDeclStmt.getLocalVarDecl().getVarDeclList().get(0);

        Optional<ASTExpression> optInitD = d.getVarInitializer();
        assertTrue(optInitD.isPresent());
        ASTCastExpression castExpr = ensureIsa(optInitD.get(), ASTCastExpression.class);

        ASTPrimary exprPrimary = ensureIsa(castExpr.getExpr(), ASTPrimary.class);
        ASTExpressionName exprName = ensureIsa(exprPrimary.getChild(), ASTExpressionName.class);
        EntitySymbol expr = exprName.getResolvedEntity();
        assertSame(i, expr);
        assertSame(integer, expr.getDataType());

        ASTDataType target = castExpr.getIntersectionType().get(0);
        assertSame(duble, target.getResolvedDataType());
        assertSame(duble, castExpr.getResolvedDataType());
        assertTrue(castExpr.getResolvedDataType("Double").isPresent());
        assertSame(duble, castExpr.getResolvedDataType("Double").get());
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
                        Integer i = 2;
                        Double d = i as Integer && Double;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        ensureNoErrors(trio.global(), trio.resolver());

        ParentSymbol spruce = ensureIsa(trio.global().get("spruce"), ParentSymbol.class);
        ParentSymbol lang = ensureIsa(spruce.getTable().get("lang"), ParentSymbol.class);
        TypeSymbol integer = ensureIsa(lang.getTable().get("Integer"), TypeSymbol.class);
        TypeSymbol duble = ensureIsa(lang.getTable().get("Double"), TypeSymbol.class);

        ASTMethodDeclaration methodDecl = getMethod(trio, 1, 0);
        ASTLocalVariableDeclarationStatement localVarDeclStmt0 = ensureIsa(
                getBlockStatement(methodDecl, 0), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator varDeclI = localVarDeclStmt0.getLocalVarDecl().getVarDeclList().get(0);
        VariableSymbol i = varDeclI.getDeclSymbol();

        ASTLocalVariableDeclarationStatement localVarDeclStmt1 = ensureIsa(
                getBlockStatement(methodDecl, 1), ASTLocalVariableDeclarationStatement.class);
        ASTVariableDeclarator d = localVarDeclStmt1.getLocalVarDecl().getVarDeclList().get(0);

        Optional<ASTExpression> optInitD = d.getVarInitializer();
        assertTrue(optInitD.isPresent());
        ASTCastExpression castExpr = ensureIsa(optInitD.get(), ASTCastExpression.class);

        ASTPrimary exprPrimary = ensureIsa(castExpr.getExpr(), ASTPrimary.class);
        ASTExpressionName exprName = ensureIsa(exprPrimary.getChild(), ASTExpressionName.class);
        EntitySymbol expr = exprName.getResolvedEntity();
        assertSame(i, expr);
        assertSame(integer, expr.getDataType());

        ASTDataType target = castExpr.getIntersectionType().get(0);
        assertSame(integer, target.getResolvedDataType());
        assertSame(integer, castExpr.getResolvedDataType());
        assertTrue(castExpr.getResolvedDataType("Integer").isPresent());
        assertSame(integer, castExpr.getResolvedDataType("Integer").get());
        assertTrue(castExpr.getResolvedDataType("Double").isPresent());
        assertSame(duble, castExpr.getResolvedDataType("Double").get());
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
                        Integer i = 2;
                        Double d = i as Double && Double;
                    }
                }
                """
        );
        Trio trio = compileSoFar(codes);
        expectError(trio.global(), trio.resolver());
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
        ParentSymbol unnamed = ensureIsa(trio.global().get(SymbolTable.UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
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
        ParentSymbol unnamed = ensureIsa(trio.global().get(SymbolTable.UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
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
     * Tests expression name resolution to a field.
     */
    @Test
    public void testPrimaryOfExpressionNameResolutionField() {
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
        ParentSymbol unnamed = ensureIsa(trio.global().get(SymbolTable.UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
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
     * Tests simple expression name resolution.
     */
    @Test
    public void testPrimaryOfExpressionNameSimpleResolution() {
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

        ParentSymbol unnamed = ensureIsa(trio.global().get(SymbolTable.UNNAMED_NAMESPACE_NAME), ParentSymbol.class);
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
}
