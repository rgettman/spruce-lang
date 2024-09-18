package org.spruce.compiler.ast.toplevel;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTUseAllDeclaration</code> is "use" followed
 * by a Namespace Or Type Name, dot, star, then a semicolon.</p>
 *
 * <em>
 * UseAllDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;use NamespaceOrTypeName . * ;
 * </em>
 */
public class ASTUseAllDeclaration extends ASTParentNode
{
    /**
     * Constructs an <code>ASTUseAllDeclaration</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTUseAllDeclaration(Location location, List<ASTNode> children) {
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

