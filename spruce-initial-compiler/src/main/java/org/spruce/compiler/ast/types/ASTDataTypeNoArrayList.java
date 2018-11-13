package org.spruce.compiler.ast.types;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTDataTypeNoArrayList</code> is a comma-separated list of
 * data types (no array).</p>
 *
 * <em>
 * DataTypeNoArrayList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataTypeNoArray {, DataTypeNoArray}
 * </em>
 */
public class ASTDataTypeNoArrayList extends ASTParentNode
{
    /**
     * Constructs an <code>ASTDataTypeNoArrayList</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTDataTypeNoArrayList(Location location, List<ASTNode> children)
    {
        super(location, children);
    }

    /**
     * This node is collapsible.
     * @return <code>true</code>.
     */
    @Override
    public boolean isCollapsible()
    {
        return true;
    }
}
