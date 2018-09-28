package org.spruce.compiler.ast;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTPackageOrTypeName</code> is a node representing part of a
 * qualified name that could be a package name or a type name.</p>
 *
 * <em>
 * PackageOrTypeName:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;PackageOrTypeName . Identifier<br>
 * </em>
 */
public class ASTPackageOrTypeName extends ASTParentNode
{
    /**
     * Constructs an <code>ASTPackageOrTypeName</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTPackageOrTypeName(Location location, List<ASTNode> children)
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
