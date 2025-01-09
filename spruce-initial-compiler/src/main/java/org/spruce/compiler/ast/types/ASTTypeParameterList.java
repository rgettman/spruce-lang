package org.spruce.compiler.ast.types;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTTypeParameterList</code> is a comma-separated list of type
 * parameter instances.</p>
 *
 * <em>
 * TypeParameterList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;TypeParameter {, TypeParameter}
 * </em>
 */
public class ASTTypeParameterList extends ASTListNode<ASTTypeParameter> {
    /**
     * Constructs an <code>ASTTypeParameterList</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTTypeParameter</code>s.
     */
    public ASTTypeParameterList(Location location, List<ASTTypeParameter> children) {
        super(location, children, Type.TYPE_PARAMETERS);
    }
}
