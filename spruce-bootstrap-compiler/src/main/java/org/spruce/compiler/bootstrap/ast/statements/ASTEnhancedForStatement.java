package org.spruce.compiler.bootstrap.ast.statements;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.ast.expressions.ASTValueExpression;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.symbol.ParentSymbol;

/**
 * <p>An <code>ASTEnhancedForStatement</code> is "for (", a local variable
 * declaration, the keyword 'in', a Value Expression, ")", and a block.  The local
 * variable declaration must declare exactly one variable.</p>
 *
 * <em>
 * EnhancedForStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;for ( LocalVariableDeclaration in ValueExpression ) Block<br>
 * </em>
 */
public final class ASTEnhancedForStatement extends ASTParentNode implements ASTForStatement {
    private final ASTLocalVariableDeclaration myLocalVarDecl;
    private final ASTValueExpression myValueExpr;
    private final ASTBlock myBlock;
    private ParentSymbol myDeclSymbol;

    /**
     * Constructs an <code>ASTEnhancedForStatement</code> at the given <code>Location</code>
     * with the given <code>ASTLocalVariableDeclaration</code>, the given
     * <code>ValueExpression</code> representing the Iterable, and the given
     * <code>ASTBlock</code>.
     * @param location The <code>Location</code>.
     * @param localVarDecl An <code>ASTLocalVariableDeclaration</code>.
     * @param valueExpr An <code>ASTValueExpression</code>.
     * @param block An <code>ASTBlock</code>.
     */
    public ASTEnhancedForStatement(Location location, ASTLocalVariableDeclaration localVarDecl,
                                   ASTValueExpression valueExpr, ASTBlock block) {
        super(location);
        myLocalVarDecl = localVarDecl;
        myValueExpr = valueExpr;
        myBlock = block;
    }

    /**
     * Returns an <code>ASTLocalVariableDeclaration</code>.
     * @return An <code>ASTLocalVariableDeclaration</code>.
     */
    public ASTLocalVariableDeclaration getLocalVarDecl() {
        return myLocalVarDecl;
    }

    /**
     * Returns an <code>ASTValueExpression</code> representing the Iterable.
     * @return An <code>ASTValueExpression</code> representing the Iterable.
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
        return Arrays.asList(myLocalVarDecl, myValueExpr, myBlock);
    }
}
