package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTFieldAccess</code> is primary, "super", or TypeName "." "super"
 * followed by "." Identifier.</p>
 *
 * <em>
 * FieldAccess:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Primary . Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;super . Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;TypeName . super . Identifier
 * </em>
 */
public class ASTFieldAccess extends ASTParentNode {
    /**
     * Constructs an <code>ASTFieldAccess</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTFieldAccess(Location location, List<ASTNode> children) {
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
