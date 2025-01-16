package org.spruce.compiler.ast.expressions;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTMapEntry</code> is a pair of logical-or expressions (the "key"
 * and the "value") separated by a colon.</p>
 * <em>
 * MapEntry:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;LogicalOrExpression : LogicalOrExpression
 * </em>
 */
public final class ASTMapEntry extends ASTParentNode implements ASTValueExpression {
    private final ASTValueExpression myKey;
    private final ASTValueExpression myValue;

    /**
     * Constructs an <code>ASTUnaryExpression</code> at the given <code>Location</code>
     * with a pair of logical-or expressions separated by a colon.
     * @param location The <code>Location</code>.
     * @param key The key expression, an <code>ASTValueExpression</code>.
     * @param value The value expression, an <code>ASTValueExpression</code>.
     */
    public ASTMapEntry(Location location, ASTValueExpression key, ASTValueExpression value) {
        super(location);
        myKey = key;
        myValue = value;
    }

    /**
     * Returns the <code>ASTValueExpression</code> representing the key.
     * @return The <code>ASTValueExpression</code> representing the key.
     */
    public ASTValueExpression getKey() {
        return myKey;
    }

    /**
     * Returns the <code>ASTValueExpression</code> representing the value.
     * @return The <code>ASTValueExpression</code> representing the value.
     */
    public ASTValueExpression getValue() {
        return myValue;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myKey, myValue);
    }
}
