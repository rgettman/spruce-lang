package org.spruce.compiler.ast.expressions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.statements.ASTBlock;
import org.spruce.compiler.ast.statements.ASTThrowStatement;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTSwitchExpressionRule</code> is a switch label, then an arrow
 * (->), then either an expression, a block, or a throw statement.</p>
 *
 * <em>
 * SwitchExpressionRule:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;SwitchLabel -> Expression ;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;SwitchLabel -> Block<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;SwitchLabel -> ThrowStatement<br>
 * </em>
 */
public class ASTSwitchExpressionRule extends ASTParentNode {
    private final ASTSwitchLabel mySwitchLabel;
    private final ASTExpression myExpr;
    private final ASTBlock myBlock;
    private final ASTThrowStatement myThrowStmt;

    /**
     * Constructs an <code>ASTSwitchExpressionRule</code> at the given <code>Location</code>
     * with the given <code>ASTSwitchLabel</code> and the given
     * <code>ASTExpression</code>.
     * @param location The <code>Location</code>.
     * @param switchLabel An <code>ASTSwitchLabel</code>.
     * @param expr An <code>ASTExpression</code>.
     */
    public ASTSwitchExpressionRule(Location location, ASTSwitchLabel switchLabel, ASTExpression expr) {
        super(location);
        mySwitchLabel = switchLabel;
        myExpr = expr;
        myBlock = null;
        myThrowStmt = null;
    }

    /**
     * Constructs an <code>ASTSwitchExpressionRule</code> at the given <code>Location</code>
     * with the given <code>ASTSwitchLabel</code> and the given <code>ASTBlock</code>.
     * @param location The <code>Location</code>.
     * @param switchLabel An <code>ASTSwitchLabel</code>.
     * @param block An <code>ASTBlock</code>.
     */
    public ASTSwitchExpressionRule(Location location, ASTSwitchLabel switchLabel, ASTBlock block) {
        super(location);
        mySwitchLabel = switchLabel;
        myExpr = null;
        myBlock = block;
        myThrowStmt = null;
    }

    /**
     * Constructs an <code>ASTSwitchExpressionRule</code> at the given <code>Location</code>
     * with the given <code>ASTSwitchLabel</code> and the given
     * <code>ASTThrowStatement</code>.
     * @param location The <code>Location</code>.
     * @param switchLabel An <code>ASTSwitchLabel</code>.
     * @param throwStmt An <code>ASTThrowStatement</code>.
     */
    public ASTSwitchExpressionRule(Location location, ASTSwitchLabel switchLabel, ASTThrowStatement throwStmt) {
        super(location);
        mySwitchLabel = switchLabel;
        myExpr = null;
        myBlock = null;
        myThrowStmt = throwStmt;
    }

    /**
     * Returns an <code>ASTSwitchLabel</code>.
     * @return An <code>ASTSwitchLabel</code>.
     */
    public ASTSwitchLabel getSwitchLabel() {
        return mySwitchLabel;
    }

    /**
     * Returns an <code>ASTExpression</code> , if it exists.
     * @return An <code>Optional&lt;ASTExpression&gt;</code>.
     */
    public Optional<ASTExpression> getExpr() {
        return Optional.ofNullable(myExpr);
    }

    /**
     * Returns an <code>ASTBlock</code>, if it exists.
     * @return An <code>Optional&lt;ASTBlock&gt;</code>.
     */
    public Optional<ASTBlock> getBlock() {
        return Optional.ofNullable(myBlock);
    }

    /**
     * Returns an <code>ASTThrowStatement</code>, if it exists.
     * @return An <code>Optional&lt;ASTThrowStatement&gt;</code>.
     */
    public Optional<ASTThrowStatement> getThrowStmt() {
        return Optional.ofNullable(myThrowStmt);
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(2);
        if (myExpr != null) {
            children.add(myExpr);
        }
        else if (myBlock != null) {
            children.add(myBlock);
        }
        else {
            children.add(myThrowStmt);
        }
        return children;
    }
}