package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTWhileStatement</code> is "while", optionally followed by an Init
 * within braces, followed by a conditional expression, and a block.</p>
 *
 * <em>
 * WhileStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;while { Init } ConditionalExpression Block<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;while ConditionalExpression Block
 * </em>
 */
public class ASTWhileStatement extends ASTParentNode {
    /**
     * Constructs an <code>ASTWhileStatement</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTWhileStatement(Location location, List<ASTNode> children) {
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
}
