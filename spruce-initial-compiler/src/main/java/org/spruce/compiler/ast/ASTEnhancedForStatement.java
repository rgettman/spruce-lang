package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTEnhancedForStatement</code> is "for (", a local variable
 * declaration, a colon, an Expression (no incr/decr), ")", and a statement.  The local
 * variable declaration must declare exactly one variable.</p>
 *
 * <em>
 * EnhancedForStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;for ( LocalVariableDeclaration : ExpressionNoIncrDecr ) Statement<br>
 * </em>
 */
public class ASTEnhancedForStatement extends ASTParentNode
{
    /**
     * Constructs an <code>ASTEnhancedForStatement</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTEnhancedForStatement(Location location, List<ASTNode> children)
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
