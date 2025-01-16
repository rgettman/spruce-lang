package org.spruce.compiler.ast.types;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTIntersectionType</code> is an "&" separated list of DataTypes.</p>
 *
 * <em>
 * IntersectionType:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataType {& DataType}
 * </em>
 */
public class ASTIntersectionType extends ASTListNode<ASTDataType> {
    /**
     * Constructs an <code>ASTIntersectionType</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTDataType</code>s.
     */
    public ASTIntersectionType(Location location, List<ASTDataType> children) {
        super(location, children, Type.DATA_TYPES);
    }
}
