package org.spruce.compiler.bootstrap.ast.classes;

import java.util.List;

import org.spruce.compiler.bootstrap.ast.ASTListNode;
import org.spruce.compiler.bootstrap.common.Location;

/**
 * <p>An <code>ASTFormalParameterList</code> is a comma-separated list of
 * formal parameter instances.  Only the last formal parameter may have an
 * ellipsis.</p>
 *
 * <em>
 * FormalParameterList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;FormalParameter {, FormalParameter}
 * </em>
 */
public final class ASTFormalParameterList extends ASTListNode<ASTFormalParameter> {
    /**
     * Constructs an <code>ASTFormalParameterList</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTFormalParameter</code>s.
     */
    public ASTFormalParameterList(Location location, List<ASTFormalParameter> children) {
        super(location, children, Type.FORMAL_PARAMETERS);
    }
}
