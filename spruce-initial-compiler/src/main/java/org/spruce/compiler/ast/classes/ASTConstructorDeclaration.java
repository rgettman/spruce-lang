package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTConstructorDeclaration</code> is an optional AccessModifier followed by
 * a ConstructorDeclarator, optionally followed by a Constructor Invocation, followed by
 * a Block.</p>
 *
 * <em>
 * ConstructorDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[AccessModifier] ConstructorDeclarator [ConstructorInvocation] Block
 * </em>
 */
public class ASTConstructorDeclaration extends ASTParentNode {
    /**
     * Constructs an <code>ASTConstructorDeclaration</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTConstructorDeclaration(Location location, List<ASTNode> children) {
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
