package org.spruce.compiler.ast.expressions;

import java.util.Arrays;

import org.spruce.compiler.ast.ASTBinaryNode;
import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.ASTUnaryNode;
import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.TokenType;

import static org.spruce.compiler.scanner.TokenType.SWITCH;

/**
 * <p>An <code>ASTUnaryExpression</code> is an expression with a possible unary
 * operator and a value, or it could be a switch expression.</p>
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
public class ASTUnaryExpression extends ASTUnaryNode {
    /**
     * Constructs an <code>ASTUnaryExpression</code> at the given <code>Location</code>
     * and with a Primary as its child.
     * @param location The <code>Location</code>.
     * @param primary An <code>ASTPrimary</code>.
     */
    public ASTUnaryExpression(Location location, ASTPrimary primary) {
        super(location, primary);
    }

    /**
     * Constructs an <code>ASTUnaryExpression</code> at the given <code>Location</code>
     * and with a "!" operator, represented by the given <code>TokenType</code>,
     * and a <code>ASTUnaryExpression</code> as its child.
     * @param location The <code>Location</code>.
     * @param operand The operand, another <code>ASTUnaryNode</code>.
     * @param operator The token type of the operator for this unary expression.
     */
    public ASTUnaryExpression(Location location, ASTUnaryNode operand, TokenType operator) {
        super(location, operator, operand);
    }

    /**
     * Constructs an <code>ASTUnaryExpression</code> at the given <code>Location</code>
     * and with a switch expression as its child.
     * @param location The <code>Location</code>.
     * @param switchExpr The operand, a <code>ASTSwitchExpression</code>.
     */
    public ASTUnaryExpression(Location location, ASTBinaryNode switchExpr) {
        super(location, SWITCH, switchExpr);
    }
}
