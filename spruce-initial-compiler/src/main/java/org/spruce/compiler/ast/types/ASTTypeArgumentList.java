package org.spruce.compiler.ast.types;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTTypeArgumentList</code> is a comma-separated list of type
 * argument instances.</p>
 *
 * <em>
 * TypeArgumentList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;TypeArgument {, TypeArgument}
 * </em>
 */
public class ASTTypeArgumentList extends ASTListNode<ASTTypeArgument> {
    /**
     * Constructs an <code>ASTTypeArgumentList</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTTypeArgument</code>s.
     */
    public ASTTypeArgumentList(Location location, List<ASTTypeArgument> children) {
        super(location, children, Type.TYPE_ARGUMENTS);
    }
}
