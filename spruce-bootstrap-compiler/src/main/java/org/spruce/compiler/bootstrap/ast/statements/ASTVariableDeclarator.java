package org.spruce.compiler.bootstrap.ast.statements;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.ast.expressions.ASTExpression;
import org.spruce.compiler.bootstrap.ast.names.ASTIdentifier;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.symbol.Symbol;

/**
 * <p>An <code>ASTVariableDeclarator</code> is an identifier optionally
 * followed by assignment to an expression.</p>
 *
 * <em>
 * VariableDeclarator:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier = Expression
 * </em>
 */
public class ASTVariableDeclarator extends ASTParentNode {
    private final ASTIdentifier myVarName;
    private final ASTExpression myExpr;
    private Symbol myDeclSymbol;

    /**
     * Constructs an <code>ASTVariableDeclarator</code> at the given <code>Location</code>
     * with the given <code>ASTIdentifier</code> representing the variable name.
     * @param location The <code>Location</code>.
     * @param varName An <code>ASTIdentifier</code> representing the variable name.
     */
    public ASTVariableDeclarator(Location location, ASTIdentifier varName) {
        super(location);
        myVarName = varName;
        myExpr = null;
    }

    /**
     * Constructs an <code>ASTVariableDeclarator</code> at the given <code>Location</code>
     * with the given <code>ASTIdentifier</code> representing the variable name
     * and the given <code>ASTExpression</code>.
     * @param location The <code>Location</code>.
     * @param varName An <code>ASTIdentifier</code> representing the variable name.
     * @param expr An <code>ASTExpression</code>.
     */
    public ASTVariableDeclarator(Location location, ASTIdentifier varName, ASTExpression expr) {
        super(location);
        myVarName = varName;
        myExpr = expr;
    }

    /**
     * Returns an <code>ASTIdentifier</code> representing the variable name.
     * @return An <code>ASTIdentifier</code>.
     */
    public ASTIdentifier getVarName() {
        return myVarName;
    }

    /**
     * Returns an <code>ASTExpression</code>, if it exists.
     * @return An <code>Optional&lt;ASTExpression&gt;</code>.
     */
    public Optional<ASTExpression> getVarInitializer() {
        return Optional.ofNullable(myExpr);
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
        List<Node> children = new ArrayList<>(2);
        children.add(myVarName);
        if (myExpr != null) {
            children.add(myExpr);
        }
        return children;
    }
}
