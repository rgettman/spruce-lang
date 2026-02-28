package org.spruce.compiler.bootstrap.ast.statements;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.common.Location;

/**
 * <p>An <code>ASTExpressionStatement</code> is a statement expression followed
 * by a semicolon.</p>
 *
 * <em>
 * ExpressionStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;StatementExpression ;
 * </em>
 */
public final class ASTExpressionStatement extends ASTParentNode implements ASTStatement {
    private final ASTStatementExpression myStmtExpr;

    /**
     * Constructs an <code>ASTExpressionStatement</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param stmtExpr An <code>ASTStatementExpression</code>.
     */
    public ASTExpressionStatement(Location location, ASTStatementExpression stmtExpr) {
        super(location);
        myStmtExpr = stmtExpr;
    }

    /**
     * Returns an <code>ASTStatementExpression</code>.
     * @return An <code>ASTStatementExpression</code>.
     */
    public ASTStatementExpression getStmtExpr() {
        return myStmtExpr;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myStmtExpr);
    }
}
