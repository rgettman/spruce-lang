package org.spruce.compiler.ast.expressions;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTSwitchExpression</code> is "switch", followed by a
 * conditional expression, followed by a Switch Expression Block.</p>
 *
 * <em>
 * SwitchStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;switch VariableExpression SwitchExpressionBlock
 * </em>
 */
public final class ASTSwitchExpression extends ASTParentNode implements ASTValueExpression {
    private final ASTValueExpression myCondExpr;
    private final ASTSwitchExpressionRules mySwitchExprRules;

    /**
     * Constructs an <code>ASTSwitchStatement</code> at the given <code>Location</code>
     * with the given <code>ASTNode</code> representing a Condition Expression
     * and the given <code>ASTSwitchExpressionRules</code>.
     * @param location The <code>Location</code>.
     * @param condExpr An <code>ASTValueExpression</code>.
     * @param switchExprRules An <code>ASTSwitchExpressionRules</code>.
     */
    public ASTSwitchExpression(Location location, ASTValueExpression condExpr, ASTSwitchExpressionRules switchExprRules) {
        super(location);
        myCondExpr = condExpr;
        mySwitchExprRules = switchExprRules;
    }

    /**
     * Returns an <code>ASTValueExpression</code>.
     * @return An <code>ASTValueExpression</code>.
     */
    public ASTValueExpression getCondExpr() {
        return myCondExpr;
    }

    /**
     * Returns an <code>ASTSwitchExpressionRules</code>.
     * @return An <code>ASTSwitchExpressionRules</code>.
     */
    public ASTSwitchExpressionRules getSwitchExprRules() {
        return mySwitchExprRules;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myCondExpr, mySwitchExprRules);
    }
}
