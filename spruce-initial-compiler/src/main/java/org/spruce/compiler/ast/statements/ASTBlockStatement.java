package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTBlockStatement</code> is a local variable declaration
 * statement, a class declaration, or a statement.</p>
 *
 * <em>
 * BlockStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;LocalVariableDeclarationStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Statement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;<strong>The following will also be a production:</strong><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ClassDeclaration<br>
 * </em>
 */
public class ASTBlockStatement extends ASTParentNode
{
    /**
     * Constructs an <code>ASTBlockStatement</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTBlockStatement(Location location, List<ASTNode> children)
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
