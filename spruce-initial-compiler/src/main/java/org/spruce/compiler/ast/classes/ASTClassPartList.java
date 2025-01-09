package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTClassPartList</code> is a list of interface parts.</p>
 *
 * <em>
 * InterfacePartList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;InterfacePart {InterfacePart}
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
