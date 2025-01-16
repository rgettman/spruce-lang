package org.spruce.compiler.ast.statements;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.expressions.ASTValueExpression;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTCriticalStatement</code> is a "critical statement".
 * It is "critical" followed by a value expression and a block.</p>
 *
 * <em>
 * CriticalStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;critical ValueExpression Block
 * </em>
 */
public final class ASTCriticalStatement extends ASTParentNode implements ASTStatement {
    private final ASTValueExpression myValueExpr;
    private final ASTBlock myBlock;

    /**
     * Constructs an <code>ASTCriticalStatement</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param valueExpr An <code>ASTValueExpression</code>.
     * @param block An <code>ASTBlock</code>.
     */
    public ASTCriticalStatement(Location location, ASTValueExpression valueExpr, ASTBlock block) {
        super(location);
        myValueExpr = valueExpr;
        myBlock = block;
    }

    /**
     * Returns an <code>ASTValueExpression</code>.
     * @return An <code>ASTValueExpression</code>.
     */
    public ASTValueExpression getValueExpr() {
        return myValueExpr;
    }

    /**
     * Returns an <code>ASTBlock</code>.
     * @return An <code>ASTBlock</code>.
     */
    public ASTBlock getBlock() {
        return myBlock;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myValueExpr, myBlock);
    }
}
