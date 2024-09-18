package org.spruce.compiler.ast.names;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTIdentifierList</code> is a comma-separated list of identifiers.</p>
 *
 * <em>
 * IdentifierList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier {, Identifier}
 * </em>
 */
public class ASTIdentifierList extends ASTParentNode {
    /**
     * Constructs an <code>ASTIdentifierList</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTIdentifierList(Location location, List<ASTNode> children) {
        super(location, children);
    }

    /**
     * This node is collapsible.
     * @return <code>false</code>.
     */
    @Override
    public boolean isCollapsible() {
        return true;
    }
}
