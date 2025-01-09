package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTResourceList</code> is a semicolon-separated list of resources.</p>
 *
 * <em>
 * ResourceList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Resource {; Resource}
 * </em>
 */
public class ASTResourceList extends ASTListNode<ASTResource> {
    /**
     * Constructs an <code>ASTResourceList</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTResource</code>s.
     */
    public ASTResourceList(Location location, List<ASTResource> children) {
        super(location, children, Type.RESOURCES);
    }
}
