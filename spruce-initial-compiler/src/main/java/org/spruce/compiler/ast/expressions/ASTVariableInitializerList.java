package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTVariableInitializerList</code> is a list of comma-separated
 * variable initializer instances.</p>
 *
 * <em>
 * VariableInitializerList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;VariableInitializer {, VariableInitializer}
 */
public class ASTVariableInitializerList extends ASTParentNode {
    /**
     * Constructs an <code>ASTVariableInitializerList</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTVariableInitializerList(Location location, List<ASTNode> children) {
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
