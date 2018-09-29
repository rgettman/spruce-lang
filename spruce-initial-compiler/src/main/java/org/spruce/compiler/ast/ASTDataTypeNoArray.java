package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTDataTypeNoArray</code> is a simple or fully qualified
 * type.</p>
 *
 * <em>
 * DataTypeNoArray:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;SimpleType<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataTypeNoArray . SimpleType
 * </em>
 */
public class ASTDataTypeNoArray extends ASTParentNode
{
    /**
     * Constructs an <code>ASTDataTypeNoArray</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTDataTypeNoArray(Location location, List<ASTNode> children)
    {
        super(location, children);
    }

    /**
     * This node is collapsible.
     * @return <code>false</code>.
     */
    @Override
    public boolean isCollapsible()
    {
        return false;
    }
}
