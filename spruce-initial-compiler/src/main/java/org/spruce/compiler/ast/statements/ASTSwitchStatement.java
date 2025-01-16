package org.spruce.compiler.ast.statements;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.expressions.ASTValueExpression;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTSwitchStatement</code> is "switch", followed by a value
 * expression, followed by a Switch Block.</p>
 *
 * <em>
 * SwitchStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;switch ValueExpression SwitchBlock
 * </em>
 */
public final class ASTSwitchStatement extends ASTParentNode implements ASTStatement {
    private final ASTValueExpression myValueExpr;
    private final ASTSwitchStatementRules mySwitchStmtRules;

    /**
     * Constructs an <code>ASTSwitchStatement</code> at the given <code>Location</code>
     * with the given <code>ASTValueExpression</code> and the given
     * <code>ASTSwitchStatementRules</code>.
     * @param location The <code>Location</code>.
     * @param valueExpr An <code>ASTValueExpression</code>.
     * @param switchStmtRules An <code>ASTSwitchStatementRules</code>.
     */
    public ASTSwitchStatement(Location location, ASTValueExpression valueExpr, ASTSwitchStatementRules switchStmtRules) {
        super(location);
        myValueExpr = valueExpr;
        mySwitchStmtRules = switchStmtRules;
    }

    /**
     * Returns an <code>ASTValueExpression</code>.
     * @return An <code>ASTValueExpression</code>.
     */
    public ASTValueExpression getValueExpr() {
        return myValueExpr;
    }

    /**
     * Returns an <code>ASTSwitchStatementRules</code>.
     * @return An <code>ASTSwitchStatementRules</code>.
     */
    public ASTSwitchStatementRules getSwitchStmtRules() {
        return mySwitchStmtRules;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myValueExpr, mySwitchStmtRules);
    }
}
