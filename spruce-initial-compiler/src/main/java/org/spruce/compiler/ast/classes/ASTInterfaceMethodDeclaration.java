package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTInterfaceMethodDeclaration</code> is an optional AccessModifier followed by
 * an optional InterfaceMethodModifierList, then a MethodHeader and a MethodBody.</p>
 *
 * <em>
 * InterfaceMethodDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[AccessModifier] [InterfaceMethodModifierList] MethodHeader MethodBody
 * </em>
 */
public class ASTInterfaceMethodDeclaration extends ASTParentNode {
    /**
     * Constructs an <code>ASTInterfaceMethodDeclaration</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTInterfaceMethodDeclaration(Location location, List<ASTNode> children) {
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
