package org.spruce.compiler.bootstrap.ast.classes;

import java.util.List;

import org.spruce.compiler.bootstrap.ast.ASTKeywordNode;
import org.spruce.compiler.bootstrap.ast.ASTListNode;
import org.spruce.compiler.bootstrap.common.Location;

/**
 * <p>An <code>ASTInterfaceMethodModifierList</code> is a list of general modifiers,
 * restricted to the following modifiers:</p>
 *
 * <ul>
 *     <li>override</li>
 *     <li>shared</li>
 * </ul>
 *
 * <em>
 * InterfaceMethodModifierList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;GeneralModifier {GeneralModifier}
 * </em>
 */
public class ASTInterfaceMethodModifierList extends ASTListNode<ASTKeywordNode> {
    /**
     * Constructs an <code>ASTInterfaceMethodModifierList</code> at the given <code>Location</code>
     * and with possibly a node as its child.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTInterfaceMethodModifierList(Location location, List<ASTKeywordNode> children) {
        super(location, children, Type.INTERFACE_METHOD_MODIFIERS);
    }
}
