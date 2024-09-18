package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTPattern</code> is either a <code>TypePattern</code>
 * or a <code>RecordPattern</code>.</p>
 *
 * <em>
 * Pattern:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;TypePattern<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;RecordPattern
 * </em>
 */
public class ASTPattern extends ASTParentNode {
    /**
     * Constructs an <code>ASTPattern</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTPattern(Location location, List<ASTNode> children) {
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
