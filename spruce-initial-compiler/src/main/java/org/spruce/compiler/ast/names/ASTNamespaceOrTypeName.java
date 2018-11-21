package org.spruce.compiler.ast.names;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTNamespaceOrTypeName</code> is a node representing part of a
 * qualified name that could be a namespace name or a type name.</p>
 *
 * <em>
 * NamespaceOrTypeName:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;NamespaceOrTypeName . Identifier<br>
 * </em>
 */
public class ASTNamespaceOrTypeName extends ASTParentNode
{
    /**
     * Constructs an <code>ASTNamespaceOrTypeName</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTNamespaceOrTypeName(Location location, List<ASTNode> children)
    {
        super(location, children);
    }

    /**
     * This node is collapsible.
     * @return <code>false</code>.
     */
    @Override
    public boolean isCollapsible()
    {
        return false;
    }
}
