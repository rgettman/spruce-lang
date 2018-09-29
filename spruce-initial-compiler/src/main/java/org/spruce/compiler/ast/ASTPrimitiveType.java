package org.spruce.compiler.ast;

import java.util.Arrays;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTPrimitiveType</code> is a node representing a primitive type.</p>
 *
 * <em>
 * PrimitiveType:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;boolean<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;byte<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;char<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;short<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;int<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;long<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;float<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;double<br>
 * </em>
 */
public class ASTPrimitiveType extends ASTParentNode
{
    /**
     * Constructs an <code>ASTPrimitiveType</code> at the given <code>Location</code>
     * and with one child as its node.
     * @param location The <code>Location</code>.
     * @param child The child node.
     */
    public ASTPrimitiveType(Location location, ASTNode child)
    {
        super(location, Arrays.asList(child));
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
