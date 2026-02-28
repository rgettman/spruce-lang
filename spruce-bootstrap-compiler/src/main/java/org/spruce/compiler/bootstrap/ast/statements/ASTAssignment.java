package org.spruce.compiler.bootstrap.ast.statements;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.ast.expressions.ASTExpression;
import org.spruce.compiler.bootstrap.ast.expressions.ASTLeftHandSide;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.scanner.TokenType;

/**
 * <p>An <code>ASTAssignment</code> is a left hand side, an assignment operator,
 * and an expression.</p>
 *
 * <p>The operators associated with assignments are right-associative.</p>
 *
 * <em>
 * Assignment:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;LeftHandSide AssignmentOperator Expression
 * </em>
 * <em>
 * AssignmentOperator:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;=
 * </em>
 */
public final class ASTAssignment extends ASTParentNode implements ASTStatementExpression {
    private final ASTLeftHandSide myLeftHandSide;
    private final ASTExpression myExpr;
    private final TokenType myOperator;

    /**
     * Constructs an <code>ASTAssignment</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param leftHandSide An <code>ASTLeftHandSide</code>.
     * @param expr An <code>ASTExpression</code>.
     * @param operator The token type of the operator for this unary expression.
     */
    public ASTAssignment(Location location, ASTLeftHandSide leftHandSide, ASTExpression expr, TokenType operator) {
        super(location);
        myLeftHandSide = leftHandSide;
        myExpr = expr;
        myOperator = operator;
    }

    /**
     * Returns an <code>ASTLeftHandSide</code>.
     * @return An <code>ASTLeftHandSide</code>.
     */
    public ASTLeftHandSide getLeftHandSide() {
        return myLeftHandSide;
    }

    /**
     * Returns an <code>ASTExpression</code>.
     * @return An <code>ASTExpression</code>.
     */
    public ASTExpression getExpr() {
        return myExpr;
    }

    /**
     * Returns the <code>TokenType</code> representing the operator.
     * @return The <code>TokenType</code> representing the operator.
     */
    public TokenType getOperator() {
        return myOperator;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myLeftHandSide, myExpr);
    }

    /**
     * Include the operator as the header value in this assignment operation.
     * @return The operator's string representation.
     */
    @Override
    public String getHeaderValue() {
        return myOperator.getRepresentation();
    }
}
