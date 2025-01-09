package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTInterfacePartList</code> is a list of interface parts.</p>
 *
 * <em>
 * InterfacePartList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;InterfacePart {InterfacePart}
 * </em>
 */
public class ASTInterfacePartList extends ASTListNode<ASTInterfacePart> {
    /**
     * Constructs an <code>ASTInterfacePartList</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTInterfacePart</code>s.
     */
    public ASTInterfacePartList(Location location, List<ASTInterfacePart> children) {
        super(location, children, Type.INTERFACE_PARTS);
    }
}
