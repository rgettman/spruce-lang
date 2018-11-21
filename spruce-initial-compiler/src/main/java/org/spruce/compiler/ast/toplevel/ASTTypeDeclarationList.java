package org.spruce.compiler.ast.toplevel;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTTypeDeclarationList</code> is multiple type declarations.</p>
 *
 * <em>
 * TypeDeclarationList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;TypeDeclaration {TypeDeclaration}
 * </em>
 */
public class ASTTypeDeclarationList extends ASTParentNode
{
    /**
     * Constructs an <code>ASTTypeDeclarationList</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTTypeDeclarationList(Location location, List<ASTNode> children)
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

