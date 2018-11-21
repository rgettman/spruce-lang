package org.spruce.compiler.ast.toplevel;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTNamespaceDeclaration</code> is a "namespace" followed by a
 * Namespace Name.</p>
 *
 * <em>
 * NamespaceDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;namespace NamespaceName
 * </em>
 */
public class ASTNamespaceDeclaration extends ASTParentNode
{
    /**
     * Constructs an <code>ASTNamespaceDeclaration</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTNamespaceDeclaration(Location location, List<ASTNode> children)
    {
        super(location, children);
    }

    /**
     * This node is collapsible.
     * @return <code>true</code>.
     */
    @Override
    public boolean isCollapsible()
    {
        return true;
    }
}

