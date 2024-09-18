package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTForStatement</code> is either a basic for statement or an
 * enhanced for statement.</p>
 *
 * <em>
 * ForStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;BasicForStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;EnhancedForStatement
 * </em>
 */
public class ASTForStatement extends ASTParentNode {
    /**
     * Constructs an <code>ASTStatement</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTForStatement(Location location, List<ASTNode> children) {
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
