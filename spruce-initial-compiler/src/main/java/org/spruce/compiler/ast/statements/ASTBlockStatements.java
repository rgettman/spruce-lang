package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTBlockStatements</code> is a list of block statement instances.</p>
 *
 * <em>
 * BlockStatements:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;BlockStatement {BlockStatement}
 * </em>
 */
public class ASTBlockStatements extends ASTParentNode
{
    /**
     * Constructs an <code>ASTBlockStatements</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTBlockStatements(Location location, List<ASTNode> children)
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
