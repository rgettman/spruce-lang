package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTLocalVariableDeclarationStatement</code> is a local variable
 * declaration followed by a semicolon.</p>
 *
 * <em>
 * LocalVariableDeclarationStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;LocalVariableDeclaration ;
 * </em>
 */
public class ASTLocalVariableDeclarationStatement extends ASTParentNode
{
    /**
     * Constructs an <code>ASTLocalVariableDeclarationStatement</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTLocalVariableDeclarationStatement(Location location, List<ASTNode> children)
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
