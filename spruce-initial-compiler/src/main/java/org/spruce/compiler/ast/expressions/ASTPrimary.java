package org.spruce.compiler.ast.expressions;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTExpressionName;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTPrimary</code> is a simple expression.</p>
 *
 * <em>
 * Primary:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Literal<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ClassLiteral<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ExpressionName<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;self<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;TypeName . self<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;( Expression )<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;CollectionComprehension<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;CollectionExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;MapComprehension<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;MapExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ElementAccess<br> // Array, List, Map access with [i]
 * &nbsp;&nbsp;&nbsp;&nbsp;MethodInvocation<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ArrayCreationExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ClassInstanceCreationExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;FieldAccess<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;MethodReference
 * </em>
 */
public final class ASTPrimary extends ASTParentNode implements ASTValueExpression {
    public enum Type {
        LITERAL, CLASS_LITERAL, EXPR_NAME, SELF, TYPENAME_SELF, PAREN_EXPR, ELEMENT_ACCESS,
        METHOD_INVOCATION, ARRAY_CREATION_EXPR, CLASS_INSTANCE_CREATION_EXPR, FIELD_ACCESS, METHOD_REFERENCE,
        COLLECTION_EXPR, COLLECTION_COMPREHENSION, MAP_EXPR, MAP_COMPREHENSION,
        BAD
    }
    private final Node myChild;
    private final Type myType;

    /**
     * Constructs an <code>ASTPrimary</code> at the given <code>Location</code>
     * with the given <code>Node</code> as its child, and with the given
     * <code>Type</code>.
     * @param location The <code>Location</code>.
     * @param child The child <code>Node</code>.
     * @param type The <code>Type</code> of primary.
     */
    public ASTPrimary(Location location, Node child, Type type) {
        super(location);
        myChild = child;
        myType = type;
    }

    /**
     * Returns an <code>Node</code> as the child.
     * @return An <code>Node</code>.
     */
    public Node getChild() {
        return myChild;
    }

    /**
     * Returns the <code>Type</code> of this primary that can be used
     * to identify the type of child.
     * @return The <code>Type</code>.
     */
    public Type getType() {
        return myType;
    }

    /**
     * Returns whether this <code>Primary</code> represents a <code>LeftHandSide</code>:
     * an ExpressionName, ElementAccess, or FieldAccess.
     * @return Whether this <code>Primary</code> represents a <code>LeftHandSide</code>.
     */
    public boolean isLeftHandSide() {
        return Arrays.asList(Type.EXPR_NAME, Type.ELEMENT_ACCESS, Type.FIELD_ACCESS).contains(myType);
    }

    /**
     * Looks for something that can be the child of an <code>ASTLeftHandSide</code>.
     * If found, creates and returns the <code>ASTLeftHandSide</code>.
     * @return The <code>ASTLeftHandSide</code>.
     */
    public ASTLeftHandSide getLeftHandSide() {
        return switch (myChild) {
            case ASTExpressionName exprName -> exprName;
            case ASTElementAccess elementAccess -> elementAccess;
            case ASTFieldAccess fieldAccess -> fieldAccess;
            default -> throw new IllegalStateException("Internal error: Expected a LeftHandSide!");
        };
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myChild);
    }

    /**
     * Returns the first line of the string representation of this node in the
     * format "ClassSimpleName(type) at Location".
     * @return A header line for this node.
     */
    @Override
    public String getHeaderValue() {
        return getClass().getSimpleName() + ("(" + myType + ")")  + " at " + getLocation();
    }
}
