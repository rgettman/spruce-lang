package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTArrayCreationExpression</code> is a data type (no array),
 * possibly multiple dimension expressions, possibly multiple "[]", and
 * possibly an array initializer.</p>
 *
 * <em>
 * ArrayCreationExpression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataTypeNoArray DimExprs<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataTypeNoArray DimExprs Dims<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataTypeNoArray Dims ArrayInitializer
 * </em>
 */
public class ASTArrayCreationExpression extends ASTParentNode
{
    /**
     * Constructs an <code>ASTArrayCreationExpression</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTArrayCreationExpression(Location location, List<ASTNode> children)
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
