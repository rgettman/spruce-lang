package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTStatementExpression</code> is an assignment expression,
 * postfix expression, prefix expression, method invocation, or class instance
 * creation expression.</p>
 *
 * <em>
 * StatementExpression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Assignment<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;PrefixExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;PostfixExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;MethodInvocationExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;<strong>The following will also be a production:</strong><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ClassInstanceCreationExpression
 * </em>
 */
public class ASTStatementExpression extends ASTParentNode {
    /**
     * Constructs an <code>ASTStatementExpression</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTStatementExpression(Location location, List<ASTNode> children) {
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
