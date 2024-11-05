package org.spruce.compiler.ast.expressions;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * <p>An <code>ASTBinaryExpression</code> is an expression with a binary
 * operator and two operands.</p>
 */
public final class ASTBinaryExpression extends ASTParentNode implements ASTValueExpression {
    private final ASTValueExpression myFirst;
    private final ASTValueExpression mySecond;
    private final TokenType myOperation;

    /**
     * Constructs an <code>ASTUnaryExpression</code> at the given <code>Location</code>
     * and with an operator represented by the given <code>TokenType</code>,
     * and a <code>ASTNode</code> as its child.
     * @param location The <code>Location</code>.
     * @param first The first operand, an <code>ASTValueExpression</code>.
     * @param second The second operand, an <code>ASTValueExpression</code>.
     * @param operator The token type of the operator for this unary expression.
     */
    public ASTBinaryExpression(Location location, ASTValueExpression first, ASTValueExpression second, TokenType operator) {
        super(location);
        Objects.requireNonNull(operator);
        myOperation = operator;
        myFirst = first;
        mySecond = second;
    }

    /**
     * Returns the name representing the operation on the child.
     * @return The <code>TokenType</code> representing the operation name.
     */
    public TokenType getOperation() {
        return myOperation;
    }

    /**
     * Returns the first child.
     * @return The first child.
     */
    public ASTValueExpression getFirst() {
        return myFirst;
    }

    /**
     * Returns the second child.
     * @return The second child.
     */
    public ASTValueExpression getSecond() {
        return mySecond;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myFirst, mySecond);
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
