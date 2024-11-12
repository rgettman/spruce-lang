package org.spruce.compiler.ast.statements;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.expressions.ASTSwitchLabel;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTSwitchStatementRule</code> is a switch label, then an arrow
 * (->), then either an expression statement, a block, or a throw statement.</p>
 *
 * <em>
 * SwitchStatementRule:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;SwitchLabel -> ExpressionStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;SwitchLabel -> Block<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;SwitchLabel -> ThrowStatement<br>
 * </em>
 */
public class ASTSwitchStatementRule extends ASTParentNode {
    private final ASTSwitchLabel mySwitchLabel;
    private final ASTExpressionStatement myExprStmt;
    private final ASTBlock myBlock;
    private final ASTThrowStatement myThrowStmt;

    /**
     * Constructs an <code>ASTSwitchStatementRule</code> at the given <code>Location</code>
     * with the given <code>ASTSwitchLabel</code> and the given <code>ASTExpressionStatement</code>.
     * @param location The <code>Location</code>.
     * @param switchLabel An <code>ASTSwitchLabel</code>.
     * @param exprStmt An <code>ASTExpressionStatement</code>.
     */
    public ASTSwitchStatementRule(Location location, ASTSwitchLabel switchLabel, ASTExpressionStatement exprStmt) {
        super(location);
        mySwitchLabel = switchLabel;
        myExprStmt = exprStmt;
        myBlock = null;
        myThrowStmt = null;
    }

    /**
     * Constructs an <code>ASTSwitchStatementRule</code> at the given <code>Location</code>
     * with the given <code>ASTSwitchLabel</code> and the given <code>ASTBlock</code>.
     * @param location The <code>Location</code>.
     * @param switchLabel An <code>ASTSwitchLabel</code>.
     * @param block An <code>ASTBlock</code>.
     */
    public ASTSwitchStatementRule(Location location, ASTSwitchLabel switchLabel, ASTBlock block) {
        super(location);
        mySwitchLabel = switchLabel;
        myExprStmt = null;
        myBlock = block;
        myThrowStmt = null;
    }

    /**
     * Constructs an <code>ASTSwitchStatementRule</code> at the given <code>Location</code>
     * with the given <code>ASTSwitchLabel</code> and the given
     * <code>ASTThrowStatement</code>.
     * @param location The <code>Location</code>.
     * @param switchLabel An <code>ASTSwitchLabel</code>.
     * @param throwStmt An <code>ASTThrowStatement</code>.
     */
    public ASTSwitchStatementRule(Location location, ASTSwitchLabel switchLabel, ASTThrowStatement throwStmt) {
        super(location);
        mySwitchLabel = switchLabel;
        myExprStmt = null;
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
     * Returns an <code>ASTExpressionStatement</code>, if it exists.
     * @return An <code>Optional&lt;ASTExpressionStatement&gt;</code>.
     */
    public Optional<ASTExpressionStatement> getExprStmt() {
        return Optional.ofNullable(myExprStmt);
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
        children.add(mySwitchLabel);
        if (myExprStmt != null) {
            children.add(myExprStmt);
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