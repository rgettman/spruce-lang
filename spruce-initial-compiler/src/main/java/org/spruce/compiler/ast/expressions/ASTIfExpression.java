package org.spruce.compiler.ast.expressions;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTIfExpression</code> is a logical or expression or
 * 'if', a logical or expression, 'use', an expression, 'else', and another expression.</p>
 *
 * <em>
 * IfExpression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;LogicalOrExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;if LogicalOrExpression use Expression else Expression<br>
 * </em>
 */
public final class ASTIfExpression extends ASTParentNode implements ASTValueExpression {
    private final ASTValueExpression myCondition;
    private final ASTExpression myExprIfTrue;
    private final ASTExpression myExprIfFalse;

    /**
     * Constructs an <code>ASTIfExpression</code> at the given <code>Location</code>
     * given a condition node, an expression if true, and an expression if false.
     * @param location The <code>Location</code>.
     * @param condition The <code>ASTValueExpression</code> representing the condition.
     * @param exprIfTrue The <code>ASTExpression</code> representing the expression value if true.
     * @param exprIfFalse The <code>ASTExpression</code> representing the expression value if false.
     */
    public ASTIfExpression(Location location, ASTValueExpression condition, ASTExpression exprIfTrue, ASTExpression exprIfFalse) {
        super(location);
        myCondition = condition;
        myExprIfTrue = exprIfTrue;
        myExprIfFalse = exprIfFalse;
    }

    /**
     * Returns the condition to be evaluated.
     * @return An <code>ASTValueExpression</code> representing the condition to be evaluated.
     */
    public ASTValueExpression getCondition() {
        return myCondition;
    }

    /**
     * Returns the expression to be evaluated if the condition is <code>true</code>.
     * @return An <code>ASTExpression</code> representing the expression to be evaluated
     *     if the condition is <code>true</code>.
     */
    public ASTExpression getExprIfTrue() {
        return myExprIfTrue;
    }

    /**
     * Returns the expression to be evaluated if the condition is <code>false</code>.
     * @return An <code>ASTExpression</code> representing the expression to be evaluated
     *      if the condition is <code>false</code>.
     */
    public ASTExpression getExprIfFalse() {
        return myExprIfFalse;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myCondition, myExprIfTrue, myExprIfFalse);
    }
}