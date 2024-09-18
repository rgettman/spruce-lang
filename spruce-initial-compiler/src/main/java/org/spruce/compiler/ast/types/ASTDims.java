package org.spruce.compiler.ast.types;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTDims</code> is a set of dimensions on an array type.</p>
 *
 * <em>
 * Dims:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[]<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Dims []
 * </em>
 */
public class ASTDims extends ASTParentNode {
    /**
     * Constructs an <code>ASTDims</code> at the given <code>Location</code>
     * and with possibly a node as its child.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTDims(Location location, List<ASTNode> children) {
        super(location, children);
    }

    /**
     * This node is NOT collapsible.
     * @return <code>false</code>.
     */
    @Override
    public boolean isCollapsible() {
        return false;
    }
}
