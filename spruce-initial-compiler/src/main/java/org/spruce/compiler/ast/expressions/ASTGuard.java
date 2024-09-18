package org.spruce.compiler.ast.expressions;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * <p>An <code>ASTGuard</code> is "when" followed by a conditional expression.</p>
 *
 * <em>
 * Guard:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;when ConditionalExpression<br>
 * </em>
 */
public class ASTGuard extends ASTParentNode {
    /**
     * Constructs an <code>ASTGuard</code> at the given <code>Location</code>
     * and with a Primary as its child.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     * @param operation The <code>TokenType</code> representing the operation.
     */
    public ASTGuard(Location location, List<ASTNode> children, TokenType operation) {
        super(location, children, operation);
    }

    /**
     * This node is collapsible.
     * @return <code>true</code>.
     */
    @Override
    public boolean isCollapsible() {
        return true;
    }
}
