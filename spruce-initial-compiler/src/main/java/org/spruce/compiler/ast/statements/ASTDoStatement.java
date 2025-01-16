package org.spruce.compiler.ast.statements;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.expressions.ASTValueExpression;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTDoStatement</code> is "do", followed by a Block, "while ",
 * a value expression, and a semicolon.</p>
 *
 * <em>
 * DoStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;do Block while ValueExpression ;
 * </em>
 */
public final class ASTDoStatement extends ASTParentNode implements ASTStatement {
    private final ASTBlock myBlock;
    private final ASTValueExpression myValueExpr;

    /**
     * Constructs an <code>ASTDoStatement</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param block An <code>ASTBlock</code>.
     * @param valueExpr An <code>ASTValueExpression</code>.
     */
    public ASTDoStatement(Location location, ASTBlock block, ASTValueExpression valueExpr) {
        super(location);
        myBlock = block;
        myValueExpr = valueExpr;
    }

    /**
     * Returns an <code>ASTBlock</code>.
     * @return An <code>ASTBlock</code>.
     */
    public ASTBlock getBlock() {
        return myBlock;
    }

    /**
     * Returns an <code>ASTValueExpression</code>.
     * @return An <code>ASTValueExpression</code>.
     */
    public ASTValueExpression getValueExpr() {
        return myValueExpr;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myBlock, myValueExpr);
    }
}
