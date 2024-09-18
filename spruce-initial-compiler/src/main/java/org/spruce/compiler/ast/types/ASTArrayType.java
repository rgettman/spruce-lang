package org.spruce.compiler.ast.types;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTArrayType</code> is a data type (no array) with dimensions.</p>
 *
 * <em>
 * ArrayType:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataTypeNoArray Dims
 * </em>
 */
public class ASTArrayType extends ASTParentNode {
    /**
     * Constructs an <code>ASTArrayType</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTArrayType(Location location, List<ASTNode> children) {
        super(location, children);
    }

    /**
     * This node is collapsible.
     * @return <code>false</code>.
     */
    @Override
    public boolean isCollapsible() {
        return false;
    }
}
