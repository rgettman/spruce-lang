package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTInterfaceDeclaration</code> is an optional AccessModifier followed by
 * an optional InterfaceModifierList, then "interface", an Identifier, followed by
 * optional Type Parameters, optional ExtendsInterfaces, then an InterfaceBody.</p>
 *
 * <em>
 * InterfaceDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[AccessModifier] [InterfaceModifierList] interface Identifier [TypeParameters] [ExtendsInterfaces] [Permits] InterfaceBody
 * </em>
 */
public class ASTInterfaceDeclaration extends ASTParentNode {
    /**
     * Constructs an <code>ASTInterfaceDeclaration</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTInterfaceDeclaration(Location location, List<ASTNode> children) {
        super(location, children);
    }

    /**
     * This node is collapsible.
     * @return <code>true</code>.
     */
    @Override
    public boolean isCollapsible() {
        return true;
    }
}
