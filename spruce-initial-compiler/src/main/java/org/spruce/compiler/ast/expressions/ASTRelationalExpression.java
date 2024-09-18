package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTRelationalExpression</code> is a compare expression or
 * a relational expression, a relational operator, and a compare
 * expression.  The second operand of <code>instanceof</code> is a data type.</p>
 *
 * <p>The operators associated with relational expressions are left-associative.</p>
 *
 * <em>
 * RelationalExpression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;CompareExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;RelationalExpression &lt; CompareExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;RelationalExpression &lt;= CompareExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;RelationalExpression &gt; CompareExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;RelationalExpression &gt;= CompareExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;RelationalExpression == CompareExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;RelationalExpression != CompareExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;RelationalExpression is CompareExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;RelationalExpression isa DataType
 * </em>
 */
public class ASTRelationalExpression extends ASTParentNode {
    /**
     * Constructs an <code>ASTRelationalExpression</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTRelationalExpression(Location location, List<ASTNode> children) {
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