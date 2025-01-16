package org.spruce.compiler.ast.expressions;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.common.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * <p>An <code>ASTUnaryExpression</code> is an expression with a unary
 * operator and an operand.</p>
 *
 * <em>
 * UnaryExpression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Primary<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;- UnaryExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;~ UnaryExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;! UnaryExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;SwitchExpression<br>
 * </em>
 */
public final class ASTUnaryExpression extends ASTParentNode implements ASTValueExpression {
    private final ASTValueExpression myFirst;
    private final TokenType myOperation;

    /**
     * Constructs an <code>ASTUnaryExpression</code> at the given <code>Location</code>
     * and with an operator represented by the given <code>TokenType</code>,
     * and a <code>ASTValueExpression</code> as its child.
     * @param location The <code>Location</code>.
     * @param operand The operand, an <code>ASTValueExpression</code>.
     * @param operator The token type of the operator for this unary expression.
     */
    public ASTUnaryExpression(Location location, ASTValueExpression operand, TokenType operator) {
        super(location);
        Objects.requireNonNull(operator);
        myFirst = operand;
        myOperation = operator;
    }

    /**
     * Returns the first child.
     * @return The first child.
     */
    public ASTValueExpression getFirst() {
        return myFirst;
    }

    /**
     * Returns the <code>TokenType</code>> representing the operation on the child.
     * @return The <code>TokenType</code> representing the operation name.
     */
    public TokenType getOperation() {
        return myOperation;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myFirst);
    }

    /**
     * Returns the String representation of the operator.
     * @return The String representation of the operator.
     */
    @Override
    public String getHeaderValue() {
        return myOperation.getRepresentation();
    }
}
