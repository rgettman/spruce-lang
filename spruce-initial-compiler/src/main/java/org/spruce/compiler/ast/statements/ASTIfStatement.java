package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTIfStatement</code> is "if", optionally followed by an Init
 * within braces, followed by a conditional expression, and a block,
 * optionally followed by "else" and another block or another if statement.</p>
 *
 * <p>The parser here is greedy; it will consume an "else" that it finds.  It
 * resolves the parser ambiguity known as the "dangling else" problem by being
 * greedy.</p>
 *
 * <em>
 * IfStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;if [{ Init }] ConditionalExpression Block else Block<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;if [{ Init }] ConditionalExpression Block else IfStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;if [{ Init }] ConditionalExpression Block<br>
 * </em>
 */
public class ASTIfStatement extends ASTParentNode {
    /**
     * Constructs an <code>ASTIfStatement</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTIfStatement(Location location, List<ASTNode> children) {
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
