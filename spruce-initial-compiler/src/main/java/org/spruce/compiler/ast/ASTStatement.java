package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTStatement</code> is an expression statement, if-then, if-then-else,
 * while, for statement, block, assert, switch, do-while, break, continue,
 * fallthrough, return, synchronized, throw, or try statement.</p>
 *
 * <p>At this time, the design poses no empty statement or labeled statement.</p>
 *
 * <em>
 * Statement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Block<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;StatementExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;BreakStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ContinueStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;FallthroughStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ReturnStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ThrowStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;IfStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;WhileStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DoWhileStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;SynchronizedStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ForStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;TryStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;SwitchStatement
 * </em>
 */
public class ASTStatement extends ASTParentNode
{
    /**
     * Constructs an <code>ASTStatement</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTStatement(Location location, List<ASTNode> children)
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
