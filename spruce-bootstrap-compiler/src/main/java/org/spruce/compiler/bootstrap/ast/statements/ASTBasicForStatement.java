package org.spruce.compiler.bootstrap.ast.statements;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.ast.expressions.ASTValueExpression;
import org.spruce.compiler.bootstrap.common.Location;

/**
 * <p>An <code>ASTBasicForStatement</code> is "for (", an optional Init, a
 * semicolon, an optional Value Expression, another semicolon, an optional statement
 * expression list, ")", and a block.</p>
 *
 * <em>
 * BasicForStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;for ( [Init] ; [ValueExpression] ; [StatementExpressionList] ) Block<br>
 * </em>
 */
public final class ASTBasicForStatement extends ASTParentNode implements ASTForStatement {
    private final ASTInit myInit;
    private final ASTValueExpression myValueExpr;
    private final ASTStatementExpressionList myStmtExprList;
    private final ASTBlock myBlock;

    /**
     * Constructs an <code>ASTBasicForStatement</code> at the given <code>Location</code>
     * with the given <code>ASTInit</code>, the given <code>ASTValueExpression</code>,
     * the given <code>ASTStatementExpressionList</code>, and the given <code>ASTBlock</code>.
     * @param location The <code>Location</code>.
     * @param init A possibly null <code>ASTInit</code>.
     * @param condExpr A possibly null <code>ASTValueExpression</code>.
     * @param stmtExprList A possibly empty <code>ASTStatementExpressionList</code>.
     * @param block An <code>ASTBlock</code>.
     */
    private ASTBasicForStatement(Location location, ASTInit init, ASTValueExpression condExpr, ASTStatementExpressionList stmtExprList, ASTBlock block) {
        super(location);
        myInit = init;
        myValueExpr = condExpr;
        myStmtExprList = stmtExprList;
        myBlock = block;
    }

    /**
     * Because of the 4 possible cases, use this <code>Builder</code> to build
     * an instance of <code>ASTBasicForStatement</code>.
     */
    public static class Builder {
        private Location myLocation;
        private ASTInit myInit;
        private ASTValueExpression myValueExpr;
        private ASTStatementExpressionList myStmtExprList;
        private ASTBlock myBlock;

        /**
         * Sets the <code>Location</code>.
         * @param location An <code>Location</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setLocation(Location location) {
            this.myLocation = location;
            return this;
        }

        /**
         * Sets the <code>ASTInit</code>.
         * @param init An <code>ASTInit</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setInit(ASTInit init) {
            this.myInit = init;
            return this;
        }

        /**
         * Sets the <code>ASTValueExpression</code>.
         * @param valueExpr An <code>ASTValueExpression</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setValueExpr(ASTValueExpression valueExpr) {
            this.myValueExpr = valueExpr;
            return this;
        }

        /**
         * Sets the <code>ASTStatementExpressionList</code>.
         * @param stmtExprList An <code>ASTStatementExpressionList</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setStmtExprList(ASTStatementExpressionList stmtExprList) {
            this.myStmtExprList = stmtExprList;
            return this;
        }

        /**
         * Sets the <code>ASTBlock</code>.
         * @param block An <code>ASTBlock</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setBlock(ASTBlock block) {
            this.myBlock = block;
            return this;
        }

        /**
         * Builds and returns a new <code>ASTBasicForStatement</code>.  Enforces
         * that the productions listed for {@link ASTBasicForStatement}
         * are created and no others, else an <code>IllegalStateException</code>
         * is thrown.
         * @return An <code>ASTBasicForStatement</code>.
         */
        public ASTBasicForStatement build() {
            if (myLocation == null) {
                throw new IllegalStateException("No Location given!");
            }
            if (myBlock == null) {
                throw new IllegalStateException("No Block given!");
            }
            if (myStmtExprList == null) {
                throw new IllegalStateException("No Statement Expression List given!");
            }
            return new ASTBasicForStatement(myLocation, myInit, myValueExpr, myStmtExprList, myBlock);
        }
    }

    /**
     * Returns an <code>ASTInit</code>, if it exists.
     * @return An <code>Optional&lt;ASTInit&gt;</code>.
     */
    public Optional<ASTInit> getInit() {
        return Optional.ofNullable(myInit);
    }

    /**
     * Returns an <code>ASTValueExpression</code>, if it exists.
     * @return An <code>Optional&lt;ASTValueExpression&gt;</code>.
     */
    public Optional<ASTValueExpression> getValueExpr() {
        return Optional.ofNullable(myValueExpr);
    }

    /**
     * Returns an <code>ASTStatementExpressionList</code>.
     * @return An <code>ASTStatementExpressionList</code>.
     */
    public ASTStatementExpressionList getStmtExprList() {
        return myStmtExprList;
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
        List<Node> children = new ArrayList<>(4);
        if (myInit != null) {
            children.add(myInit);
        }
        if (myValueExpr != null) {
            children.add(myValueExpr);
        }
        if (myStmtExprList != null) {
            children.add(myStmtExprList);
        }
        children.add(myBlock);
        return children;
    }
}
