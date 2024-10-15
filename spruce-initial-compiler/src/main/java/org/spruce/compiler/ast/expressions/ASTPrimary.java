package org.spruce.compiler.ast.expressions;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTBinaryNode;
import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.ASTUnaryNode;
import org.spruce.compiler.ast.names.ASTExpressionName;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.TokenType;

import static org.spruce.compiler.ast.ASTListNode.Type.EXPR_NAME_IDS;
import static org.spruce.compiler.scanner.TokenType.OPEN_BRACKET;

/**
 * <p>An <code>ASTPrimary</code> is a simple expression.</p>
 *
 * <em>
 * Primary:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Literal<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ClassLiteral<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ExpressionName<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;self<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;TypeName . self<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;( Expression )<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ElementAccess<br> // Array, List, Map access with [i]
 * &nbsp;&nbsp;&nbsp;&nbsp;MethodInvocation<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ArrayCreationExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ClassInstanceCreationExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;FieldAccess<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;MethodReference
 * </em>
 */
public class ASTPrimary extends ASTUnaryNode {

    /**
     * Constructs an <code>ASTPrimary</code> at the given <code>Location</code>
     * and the given child.
     * @param location The <code>Location</code>.
     * @param first The only child node.
     */
    public ASTPrimary(Location location, ASTNode first) {
        super(location, first);
    }

    /**
     * Constructs an <code>ASTPrimary</code> at the given <code>Location</code>,
     * the given child, and the given operation.
     * @param location The <code>Location</code>.
     * @param first The only child node.
     * @param operation The operation, as a <code>TokenType</code>.
     */
    public ASTPrimary(Location location, ASTNode first, TokenType operation) {
        super(location, operation, first);
    }

    /**
     * TODO: Pull implementation from ASTParentNode to here; this is the only place
     * TODO: convertDescendant is called.  Must add test cases.
     * Looks for something that can be the child of an <code>ASTLeftHandSide</code>.
     * If found, creates and returns the <code>ASTLeftHandSide</code>.
     * @return The <code>ASTLeftHandSide</code>.
     * @throws CompileException If no descendant node can be a child of an
     *     <code>ASTLeftHandSide</code>.
     */
    public ASTLeftHandSide getLeftHandSide() {
        Location loc = getFirst().getLocation();
        return switch (getFirst()) {
            case ASTListNode exprName when exprName.getType() == EXPR_NAME_IDS ->
                new ASTLeftHandSide(loc, Arrays.asList(exprName));
            case ASTBinaryNode elementAccess when elementAccess.getOperation() == OPEN_BRACKET ->
                new ASTLeftHandSide(loc, Arrays.asList(elementAccess));
            case ASTFieldAccess fieldAccess ->
                new ASTLeftHandSide(loc, Arrays.asList(fieldAccess));
            default -> throw new CompileException(getLocation(), "Expected variable or element access.");
        };
    }
}
