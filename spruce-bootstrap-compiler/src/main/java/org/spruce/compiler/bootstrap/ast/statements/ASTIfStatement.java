package org.spruce.compiler.bootstrap.ast.statements;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.ast.expressions.ASTValueExpression;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.symbol.Symbol;

/**
 * <p>An <code>ASTIfStatement</code> is "if", optionally followed by an Init
 * within braces, followed by a value expression, and a block, optionally
 * followed by "else" and either another block or another if statement.</p>
 *
 * <p>The parser here is greedy; it will consume an "else" that it finds.  It
 * resolves the parser ambiguity known as the "dangling else" problem by being
 * greedy.</p>
 *
 * <em>
 * IfStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;if [{ Init }] ValueExpression Block else Block<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;if [{ Init }] ValueExpression Block else IfStatement<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;if [{ Init }] ValueExpression Block<br>
 * </em>
 */
public final class ASTIfStatement extends ASTParentNode implements ASTStatement {
    private final ASTInit myInit;
    private final ASTValueExpression myValueExpr;
    private final ASTBlock myIfBlock;
    private final ASTBlock myElseBlock;
    private final ASTIfStatement myElseIf;
    private Symbol myDeclSymbol;

    /**
     * Constructs an <code>ASTIfStatement</code> at the given <code>Location</code>,
     * given <code>ASTInit</code>, the given <code>ASTValueExpression</code>
     * representing the condition, the given <code>ASTBlock</code> representing
     * the "if" Block, the given <code>ASTBlock</code> representing the "else"
     * Block, and the given <code>ASTIfStatement</code> representing the "else if"
     * If Statement.
     * @param location The <code>Location</code>.
     * @param init A possibly null <code>ASTInit</code>.
     * @param valueExpr An <code>ASTValueExpression</code>.
     * @param ifBlock An <code>ASTBlock</code>; the "if" block.
     * @param elseBlock A possibly null <code>ASTBlock</code>, the "else" block.
     * @param elseIf A possibly null <code>ASTIfStatement</code> representing "else if".
     */
    private ASTIfStatement(Location location, ASTInit init, ASTValueExpression valueExpr, ASTBlock ifBlock,
                           ASTBlock elseBlock, ASTIfStatement elseIf) {
        super(location);
        myInit = init;
        myValueExpr = valueExpr;
        myIfBlock = ifBlock;
        myElseBlock = elseBlock;
        myElseIf = elseIf;
    }

    /**
     * Because of the 6 possible cases, use this <code>Builder</code> to build
     * an instance of <code>ASTIfStatement</code>.
     */
    public static class Builder {
        private Location myLocation;
        private ASTInit myInit;
        private ASTValueExpression myValueExpr;
        private ASTBlock myIfBlock;
        private ASTBlock myElseBlock;
        private ASTIfStatement myElseIf;

        /**
         * Sets the <code>Location</code>.
         * @param location A <code>Location</code>.
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
        public Builder setCondExpr(ASTValueExpression valueExpr) {
            this.myValueExpr = valueExpr;
            return this;
        }

        /**
         * Sets the <code>ASTBlock</code> representing the "if" Block.
         * @param ifBlock An <code>ASTBlock</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setIfBlock(ASTBlock ifBlock) {
            this.myIfBlock = ifBlock;
            return this;
        }

        /**
         * Sets the <code>ASTBlock</code> representing the "else" Block.
         * @param elseBlock An <code>ASTBlock</code>.
         * @return This <code>Builder</code>.
         */
        public Builder setElseBlock(ASTBlock elseBlock) {
            this.myElseBlock = elseBlock;
            return this;
        }

        /**
         * Sets the <code>ASTIfStatement</code> representing "else if".
         * @param elseIf The <code>ASTIfStatement</code> representing "else if".
         * @return This <code>Builder</code>.
         */
        public Builder setElseIf(ASTIfStatement elseIf) {
            this.myElseIf = elseIf;
            return this;
        }

        /**
         * Builds and returns a new <code>ASTIfStatement</code>.  Enforces
         * that the productions listed for {@link ASTIfStatement}
         * are created and no others, else an <code>IllegalStateException</code>
         * is thrown.
         * @return An <code>ASTIfStatement</code>.
         */
        public ASTIfStatement build() {
            if (myLocation == null) {
                throw new IllegalStateException("No Location given!");
            }
            if (myValueExpr == null) {
                throw new IllegalStateException("No Value Expression given!");
            }
            if (myIfBlock == null) {
                throw new IllegalStateException("No If Block given!");
            }
            if (myElseBlock != null && myElseIf != null) {
                throw new IllegalStateException("May supply either the else Block, or an else if Statement, but not both!");
            }
            return new ASTIfStatement(myLocation, myInit, myValueExpr, myIfBlock, myElseBlock, myElseIf);
        }
    }

    /**
     * Returns an <code>ASTInit</code>.
     * @return An <code>Optional&lt;ASTInit&gt;</code>.
     */
    public Optional<ASTInit> getInit() {
        return Optional.ofNullable(myInit);
    }

    /**
     * Returns an <code>ASTValueExpression</code>.
     * @return An <code>ASTValueExpression</code>.
     */
    public ASTValueExpression getCondExpr() {
        return myValueExpr;
    }

    /**
     * Returns an <code>ASTBlock</code> representing the "if" BLOCK.
     * @return An <code>ASTBlock</code>.
     */
    public ASTBlock getIfBlock() {
        return myIfBlock;
    }

    /**
     * Returns an <code>ASTBlock</code> representing the "else" Block, if it exists.
     * @return An <code>Optional&lt;ASTBlock&gt;</code>.
     */
    public Optional<ASTBlock> getElseBlock() {
        return Optional.ofNullable(myElseBlock);
    }

    /**
     * Returns an <code>ASTIfStatement</code> representing "else if", if it exists.
     * @return An <code>Optional&lt;ASTIfStatement&gt;</code>.
     */
    public Optional<ASTIfStatement> getElseIf() {
        return Optional.ofNullable(myElseIf);
    }

    /**
     * Sets the declaration <code>Symbol</code>.
     * @param symbol The declaration <code>Symbol</code>.
     */
    public void setDeclSymbol(Symbol symbol) {
        myDeclSymbol = symbol;
    }

    /**
     * Returns the declaration <code>Symbol</code>.
     * @return The declaration <code>Symbol</code>.
     */
    public Symbol getDeclSymbol(){
        return myDeclSymbol;
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(4);
        if (myInit != null) {
            children.add(myInit);
        }
        children.add(myValueExpr);
        children.add(myIfBlock);
        if (myElseBlock != null) {
            children.add(myElseBlock);
        }
        else if (myElseIf != null) {
            children.add(myElseIf);
        }
        return children;
    }
}
