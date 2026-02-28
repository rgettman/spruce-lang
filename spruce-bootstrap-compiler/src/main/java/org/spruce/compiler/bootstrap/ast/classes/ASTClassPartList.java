package org.spruce.compiler.bootstrap.ast.classes;

import java.util.List;

import org.spruce.compiler.bootstrap.ast.ASTListNode;
import org.spruce.compiler.bootstrap.common.Location;

/**
 * <p>An <code>ASTClassPartList</code> is a list of class parts.</p>
 *
 * <em>
 * ClassPartList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ClassPart {ClassPart}
 * </em>
 */
public class ASTClassPartList extends ASTListNode<ASTClassPart> {
    /**
     * Constructs an <code>ASTClassPartList</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTClassPart</code>s.
     */
    public ASTClassPartList(Location location, List<ASTClassPart> children) {
        super(location, children, Type.CLASS_PARTS);
    }
}
