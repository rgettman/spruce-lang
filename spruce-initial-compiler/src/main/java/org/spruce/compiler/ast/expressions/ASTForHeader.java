package org.spruce.compiler.ast.expressions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.statements.ASTLocalVariableDeclaration;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTForHeader</code> is 'for (', a local variable declaration, the keyword
 * 'in', a Value Expression, followed by an optional 'if' and a Value Expression, then a ')'.</p>
 * <p>The local variable declaration must declare exactly one variable.</p>
 *
 * <em>
 * ForHeader:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;for ( LocalVariableDeclaration in ValueExpression )<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;for ( LocalVariableDeclaration in ValueExpression if ValueExpression)<br>
 * </em>
 */
public final class ASTForHeader extends ASTParentNode {
    private final ASTLocalVariableDeclaration myLocalVarDecl;
    private final ASTValueExpression myValueExpr;
    private final ASTValueExpression myCondition;

    /**
     * Constructs an <code>ASTForHeader</code> at the given <code>Location</code>
     * with the given <code>ASTLocalVariableDeclaration</code>, the given
     * <code>ASTValueExpression</code> representing the Iterable, and the given
     * <code>ASTValueExpression</code> representing the condition.
     * @param location The <code>Location</code>.
     * @param localVarDecl An <code>ASTLocalVariableDeclaration</code>.
     * @param valueExpr An <code>ASTValueExpression</code> representing the Iterable.
     * @param condition An <code>ASTValueExpression</code> representing the condition.
     */
    public ASTForHeader(Location location, ASTLocalVariableDeclaration localVarDecl, ASTValueExpression valueExpr,
                        ASTValueExpression condition) {
        super(location);
        myLocalVarDecl = localVarDecl;
        myValueExpr = valueExpr;
        myCondition = condition;
    }

    /**
     * Constructs an <code>ASTForHeader</code> at the given <code>Location</code>
     * with the given <code>ASTLocalVariableDeclaration</code> the given
     * <code>ASTValueExpression</code> representing the Iterable.
     * @param location The <code>Location</code>.
     * @param localVarDecl An <code>ASTLocalVariableDeclaration</code>.
     * @param valueExpr An <code>ASTValueExpression</code> representing the Iterable.
     */
    public ASTForHeader(Location location, ASTLocalVariableDeclaration localVarDecl, ASTValueExpression valueExpr) {
        super(location);
        myLocalVarDecl = localVarDecl;
        myValueExpr = valueExpr;
        myCondition = null;
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
     * Returns an <code>ASTValueExpression</code> representing the condition, if it exists.
     * @return An <code>Optional&lt;ASTValueExpression&gt;</code>.
     */
    public Optional<ASTValueExpression> getCondition() {
        return Optional.ofNullable(myCondition);
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(3);
        children.add(myLocalVarDecl);
        children.add(myValueExpr);
        if (myCondition != null) {
            children.add(myCondition);
        }
        return children;
    }
}
