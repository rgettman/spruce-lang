package org.spruce.compiler.ast.statements;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.expressions.ASTLeftHandSide;
import org.spruce.compiler.common.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * <p>An <code>ASTPostfix</code> is a left hand side with a unary
 * increment or decrement operator.</p>
 *
 * <em>
 * Postfix:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;LeftHandSide ++<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;LeftHandSide --
 * </em>
 */
public final class ASTPostfix extends ASTParentNode implements ASTStatementExpression {
    private final ASTLeftHandSide myLeftHandSide;
    private final TokenType myOperator;

    /**
     * Constructs an <code>ASTPostfix</code> at the given <code>Location</code>
     * and with an increment or decrement operator, represented by the given
     * <code>TokenType</code>, and an <code>ASTLeftHandSide</code> as its child.
     * @param location The <code>Location</code>.
     * @param operand The operand, a <code>ASTLeftHandSide</code>.
     * @param operator The token type of the operator for this unary expression.
     */
    public ASTPostfix(Location location, ASTLeftHandSide operand, TokenType operator) {
        super(location);
        myLeftHandSide = operand;
        myOperator = operator;
    }

    /**
     * Returns the <code>ASTLeftHandSide</code>.
     * @return The <code>ASTLeftHandSide</code>.
     */
    public ASTLeftHandSide getLeftHandSide() {
        return myLeftHandSide;
    }

    /**
     * Returns the <code>TokenType</code> representing the operation on the child.
     * @return The <code>TokenType</code> representing the operation name.
     */
    public TokenType getOperator() {
        return myOperator;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myLeftHandSide);
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
