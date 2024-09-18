package org.spruce.compiler.ast.expressions;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.names.ASTExpressionName;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.scanner.Location;

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
public class ASTPrimary extends ASTParentNode {
    /**
     * Constructs an <code>ASTPrimary</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTPrimary(Location location, List<ASTNode> children) {
        super(location, children);
    }

    /**
     * This node is collapsible.
     * @return <code>true</code>.
     */
    @Override
    public boolean isCollapsible() {
        return true;
    }

    /**
     * Looks for something that can be the child of an <code>ASTLeftHandSide</code>.
     * If found, creates and returns the <code>ASTLeftHandSide</code>.
     * @return The <code>ASTLeftHandSide</code>.
     * @throws CompileException If no descendant node can be a child of an
     *     <code>ASTLeftHandSide</code>.
     */
    public ASTLeftHandSide getLeftHandSide() {
        return convertDescendant(Arrays.asList(ASTExpressionName.class, ASTElementAccess.class, ASTFieldAccess.class),
                ASTLeftHandSide::new,
                "Expected variable or element access.");
    }
}
