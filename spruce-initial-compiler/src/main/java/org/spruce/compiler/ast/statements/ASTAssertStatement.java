package org.spruce.compiler.ast.statements;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.expressions.ASTValueExpression;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTAssertStatement</code> is "assert" followed by a value expression,
 * then optionally "else" and another value expression, then a semicolon.</p>
 *
 * <em>
 * AssertStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;assert ValueExpression ;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;assert ValueExpression else ValueExpression;
 * </em>
 */
public final class ASTAssertStatement extends ASTParentNode implements ASTStatement {
    private final ASTValueExpression myCondition;
    private final ASTValueExpression myMessage;

    /**
     * Constructs an <code>ASTAssertStatement</code> at the given <code>Location</code>
     * with the given <code>ASTValueExpression</code> representing the assert condition.
     * @param location The <code>Location</code>.
     * @param condition An <code>ASTValueExpression</code> representing the assert condition.
     */
    public ASTAssertStatement(Location location, ASTValueExpression condition) {
        super(location);
        myCondition = condition;
        myMessage = null;
    }

    /**
     * Constructs an <code>ASTAssertStatement</code> at the given <code>Location</code>
     * with the given <code>ASTValueExpression</code> representing the assert condition
     * and the given <code>ASTValueExpression</code> representing the assert message.
     * @param location The <code>Location</code>.
     * @param condition An <code>ASTValueExpression</code> representing the assert condition.
     * @param message An <code>ASTValueExpression</code> representing the message.
     */
    public ASTAssertStatement(Location location, ASTValueExpression condition, ASTValueExpression message) {
        super(location);
        myCondition = condition;
        myMessage = message;
    }

    /**
     * Returns an <code>ASTValueExpression</code> representing the assert condition.
     * @return An <code>ASTValueExpression</code> representing the assert condition.
     */
    public ASTValueExpression getCondExprCondition() {
        return myCondition;
    }

    /**
     * Returns an <code>ASTValueExpression</code> representing the message, if it exists.
     * @return An <code>Optional&lt;ASTValueExpression&gt;</code> representing the message.
     */
    public Optional<ASTValueExpression> getCondExprMessage() {
        return Optional.ofNullable(myMessage);
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(2);
        children.add(myCondition);
        if (myMessage != null) {
            children.add(myMessage);
        }
        return children;
    }
}
