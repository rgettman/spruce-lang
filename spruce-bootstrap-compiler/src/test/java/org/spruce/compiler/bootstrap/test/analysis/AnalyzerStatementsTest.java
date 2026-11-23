package org.spruce.compiler.bootstrap.test.analysis;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.spruce.compiler.bootstrap.test.analysis.AnalyzerTestUtility.*;
import static org.spruce.compiler.bootstrap.test.resolution.ResolverTestUtility.CODE_SPRUCE_LANG;

/**
 * All tests for the statements analyzer.
 */
public class AnalyzerStatementsTest {

    /**
     * Test missing return statement, non-void method.
     */
    @Test
    public void testBlockMissingReturnStatementNonVoid() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                """,
                """
                use spruce.lang.Integer;
                class Test {
                    Integer testMethod() {
                        Integer testInt = 3;
                    }
                }
                """
        );
        Pair pair = compileSoFar(codes);
        expectError(pair.analyzer());
    }

    /**
     * Test return statement, non-void method.
     */
    @Test
    public void testBlockReturnStatementNonVoid() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                """,
                """
                use spruce.lang.Integer;
                class Test {
                    Integer testMethod() {
                        Integer testInt = 3;
                        return testInt;
                    }
                }
                """
        );
        Pair pair = compileSoFar(codes);
        ensureNoErrors(pair.analyzer());
    }

    /**
     * Test missing return statement is ok, void method.
     */
    @Test
    public void testBlockMissingReturnStatementVoid() {
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
        Pair pair = compileSoFar(codes);
        ensureNoErrors(pair.analyzer());
    }

    /**
     * Test return statement is ok, void method.
     */
    @Test
    public void testBlockReturnStatementVoid() {
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
                        return;
                    }
                }
                """
        );
        Pair pair = compileSoFar(codes);
        ensureNoErrors(pair.analyzer());
    }

    /**
     * Test bad return statement in constructor prologue.
     */
    @Test
    public void testReturnStatementConstructorPrologue() {
        List<String> codes = List.of(
                """
                namespace spruce.lang;
                class Any {}
                class Integer {}
                """,
                """
                use spruce.lang.Integer;
                class Test {
                    constructor() {
                        Integer testInt = 3;
                        return;
                        super();
                    }
                }
                """
        );
        Pair pair = compileSoFar(codes);
        expectError(pair.analyzer());
    }

    /**
     * Test unreachable statement, only one error.
     */
    @Test
    public void testUnreachableStatement() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                use spruce.lang.Integer;
                class Test {
                    void testMethod() {
                        Integer testInt = 3;
                        return;
                        Integer foo = testInt + 1;
                        String bar = "bar";
                    }
                }
                """
        );
        Pair pair = compileSoFar(codes);
        expectError(pair.analyzer());
    }

    /**
     * Test bad second constructor invocation.
     */
    @Test
    public void testSecondConstructorInvocation() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                use spruce.lang.Integer;
                class Test {
                    constructor() {
                        super();
                        self("foo");
                    }
                    constructor(String foo) {}
                }
                """
        );
        Pair pair = compileSoFar(codes);
        expectError(pair.analyzer());
    }

    /**
     * Test one constructor invocation.
     */
    @Test
    public void testOneConstructorInvocation() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                use spruce.lang.Integer;
                class Test {
                    constructor() {
                        super();
                    }
                }
                """
        );
        Pair pair = compileSoFar(codes);
        ensureNoErrors(pair.analyzer());
    }

    /**
     * Test bad constructor invocation in method.
     */
    @Test
    public void testMethodConstructorInvocation() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                use spruce.lang.Integer;
                class Test {
                    void testMethod() {
                        super();
                    }
                }
                """
        );
        Pair pair = compileSoFar(codes);
        expectError(pair.analyzer());
    }

    /**
     * Test bad break outside a target.
     */
    @Test
    public void testBreakOutsideTarget() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                use spruce.lang.Integer;
                class Test {
                    void testMethod() {
                        break;
                    }
                }
                """
        );
        Pair pair = compileSoFar(codes);
        expectError(pair.analyzer());
    }

    /**
     * Test bad break inside a sub-block.
     */
    @Test
    public void testBreakInsideSubBlock() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                use spruce.lang.Integer;
                class Test {
                    void testMethod() {
                        {
                            break;
                        }
                    }
                }
                """
        );
        Pair pair = compileSoFar(codes);
        expectError(pair.analyzer());
    }

    /**
     * Test bad break inside an if statement.
     */
    @Test
    public void testBreakInsideIfStatement() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                use spruce.lang.Integer;
                class Test {
                    void testMethod(Integer i) {
                        if (i == 0) {
                            break;
                        }
                        else if (i == 1) {
                            continue;
                        }
                        else {
                            break;
                        }
                    }
                }
                """
        );
        Pair pair = compileSoFar(codes);
        expectError(pair.analyzer(), 3);
    }

    /**
     * Test break inside a basic for statement.
     */
    @Test
    public void testBreakInsideBasicForStatement() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                use spruce.lang.Integer;
                class Test {
                    void testMethod() {
                        for (Integer i = 0; i < 5; i = i + 1) {
                            if (i == 2) {
                                break;
                            }
                        }
                    }
                }
                """
        );
        Pair pair = compileSoFar(codes);
        ensureNoErrors(pair.analyzer());
    }

    /**
     * Test break inside an enhanced for statement.
     */
    @Test
    public void testBreakInsideEnhancedForStatement() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                use spruce.lang.Integer;
                class Test {
                    void testMethod() {
                        for (Integer i in someList) {
                            if (i == 2) {
                                break;
                            }
                        }
                    }
                }
                """
        );
        Pair pair = compileSoFar(codes);
        ensureNoErrors(pair.analyzer());
    }

    /**
     * Test break inside a while statement.
     */
    @Test
    public void testBreakInsideWhileStatement() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                use spruce.lang.Integer;
                class Test {
                    void testMethod() {
                        while {Integer i = 0} (i < 5) {
                            if (i == 2) {
                                break;
                            }
                            i = i + 1;
                        }
                    }
                }
                """
        );
        Pair pair = compileSoFar(codes);
        ensureNoErrors(pair.analyzer());
    }
}
