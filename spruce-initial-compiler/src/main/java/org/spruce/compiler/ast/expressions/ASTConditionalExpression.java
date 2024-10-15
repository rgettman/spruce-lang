package org.spruce.compiler.ast.expressions;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.names.ASTExpressionName;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.scanner.Location;

import static org.spruce.compiler.scanner.TokenType.QUESTION_MARK;

/**
 * <p>An <code>ASTConditionalExpression</code> is a logical or expression or
 * a logical or expression, '?', an expression, ':', and another expression.</p>
 *
 * <p>The operators associated with conditional expressions are right-associative.</p>
 *
 * <em>
 * ConditionalExpression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;LogicalOrExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;LogicalOrExpression ? Expression : Expression<br>
 * </em>
 */
public class ASTConditionalExpression extends ASTParentNode {
    private final ASTNode myCondition;
    private final ASTNode myExprIfTrue;
    private final ASTNode myExprIfFalse;

    /**
     * Constructs an <code>ASTConditionalExpression</code> at the given <code>Location</code>
     * given a condition node, an expression if true, and an expression if false.
     * @param location The <code>Location</code>.
     * @param condition The <code>ASTNode</code> representing the condition.
     * @param exprIfTrue The <code>ASTNode</code> representing the expression value if true.
     * @param exprIfFalse The <code>ASTNode</code> representing the expression value if false.
     */
    public ASTConditionalExpression(Location location, ASTNode condition, ASTNode exprIfTrue, ASTNode exprIfFalse) {
        super(location, Arrays.asList(condition, exprIfTrue, exprIfFalse), QUESTION_MARK);
        myCondition = condition;
        myExprIfTrue = exprIfTrue;
        myExprIfFalse = exprIfFalse;
    }

    /**
     * TODO: For removal when removing collapsing.
     */
    @Override
    public boolean isCollapsible() {
        return false;
    }

    /**
     * Returns the condition to be evaluated.
     * @return An <code>ASTNode</code> representing the condition to be evaluated.
     */
    public ASTNode getCondition() {
        return myCondition;
    }

    /**
     * Returns the expression to be evaluated if the condition is <code>true</code>.
     * @return An <code>ASTNode</code> representing the expression to be evaluated
     *     if the condition is <code>true</code>.
     */
    public ASTNode getExprIfTrue() {
        return myExprIfTrue;
    }

    /**
     * Returns the expression to be evaluated if the condition is <code>false</code>.
     * @return An <code>ASTNode</code> representing the expression to be evaluated
     *      if the condition is <code>false</code>.
     */
    public ASTNode getExprIfFalse() {
        return myExprIfFalse;
    }
}