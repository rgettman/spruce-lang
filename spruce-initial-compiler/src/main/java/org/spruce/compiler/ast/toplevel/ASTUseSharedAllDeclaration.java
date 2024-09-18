package org.spruce.compiler.ast.toplevel;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTUseSharedAllDeclaration</code> is "use" followed
 * by "shared" followed by a Type Name, dot, star, then a semicolon.</p>
 *
 * <em>
 * UseSharedAllDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;use shared TypeName . * ;
 * </em>
 */
public class ASTUseSharedAllDeclaration extends ASTParentNode {
    /**
     * Constructs an <code>ASTUseSharedAllDeclaration</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTUseSharedAllDeclaration(Location location, List<ASTNode> children) {
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

