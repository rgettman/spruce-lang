package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTAssertStatement</code> is "assert" followed by an expression,
 * then optionally a colon and another expression, then a semicolon.</p>
 *
 * <em>
 * AssertStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;assert Expression ;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;assert Expression : Expression;
 * </em>
 */
public class ASTAssertStatement extends ASTParentNode {
    /**
     * Constructs an <code>ASTAssertStatement</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTAssertStatement(Location location, List<ASTNode> children) {
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
