package org.spruce.compiler.bootstrap.ast.statements;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.ast.SymbolDeclaration;
import org.spruce.compiler.bootstrap.ast.expressions.ASTValueExpression;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.symbol.ParentSymbol;

/**
 * <p>An <code>ASTWhileStatement</code> is "while", optionally followed by an Init
 * within braces, followed by a value expression, and a block.</p>
 *
 * <em>
 * WhileStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;while { Init } ValueExpression Block<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;while ValueExpression Block
 * </em>
 */
public final class ASTWhileStatement extends ASTParentNode implements ASTStatement, SymbolDeclaration<ParentSymbol> {
    private final ASTInit myInit;
    private final ASTValueExpression myValueExpr;
    private final ASTBlock myBlock;
    private ParentSymbol myDeclSymbol;

    /**
     * Constructs an <code>ASTWhileStatement</code> at the given <code>Location</code>
     * with the given <code>ASTInit</code>, the given <code>ASTValueExpression</code>
     * representing the condition, and the given <code>ASTBlock</code>.
     * @param location The <code>Location</code>.
     * @param init An <code>ASTInit</code>.
     * @param valueExpr An <code>ASTValueExpression</code>.
     * @param block An <code>ASTBlock</code>.
     */
    public ASTWhileStatement(Location location, ASTInit init, ASTValueExpression valueExpr, ASTBlock block) {
        super(location);
        myInit = init;
        myValueExpr = valueExpr;
        myBlock = block;
    }

    /**
     * Constructs an <code>ASTWhileStatement</code> at the given <code>Location</code>
     * with the given <code>ASTValueExpression</code> representing the condition
     * and the given <code>ASTBlock</code>.
     * @param location The <code>Location</code>.
     * @param valueExpr An <code>ASTValueExpression</code>.
     * @param block An <code>ASTBlock</code>.
     */
    public ASTWhileStatement(Location location, ASTValueExpression valueExpr, ASTBlock block) {
        super(location);
        myInit = null;
        myValueExpr = valueExpr;
        myBlock = block;
    }

    /**
     * Returns an <code>ASTInit</code>, if it exists.
     * @return An <code>Optional&lt;ASTInit&gt;</code>.
     */
    public Optional<ASTInit> getInit() {
        return Optional.ofNullable(myInit);
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
    public void setDeclSymbol(ParentSymbol symbol) {
        myDeclSymbol = symbol;
    }

    @Override
    public ParentSymbol getDeclSymbol(){
        return myDeclSymbol;
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(3);
        if (myInit != null) {
            children.add(myInit);
        }
        children.add(myValueExpr);
        children.add(myBlock);
        return children;
    }
}
