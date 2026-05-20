package org.spruce.compiler.bootstrap.ast.expressions;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.bootstrap.ast.ASTKeywordNode;
import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.ast.names.ASTExpressionName;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;

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
 * &nbsp;&nbsp;&nbsp;&nbsp;MethodInvocation<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ClassInstanceCreationExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;FieldAccess
 * </em>
 */
public final class ASTPrimary extends ASTParentNode implements ASTValueExpression {
    public enum Type {
        CLASS_INSTANCE_CREATION_EXPR, CLASS_LITERAL,
        EXPR_NAME,
        FIELD_ACCESS,
        LITERAL,
        METHOD_INVOCATION,
        PAREN_EXPR,
        SELF,
        TYPENAME_SELF,
        BAD
    }
    private final ASTPrimaryChild myChild;
    private final Type myType;
    private TypeSymbol myResolvedDataType;

    /**
     * Constructs an <code>ASTPrimary</code> at the given <code>Location</code>
     * with the given <code>ASTPrimaryChild</code>, and with the given
     * <code>Type</code>.
     * @param location The <code>Location</code>.
     * @param child The <code>ASTPrimaryChild</code>.
     * @param type The <code>Type</code> of primary.
     */
    public ASTPrimary(Location location, ASTPrimaryChild child, Type type) {
        super(location);
        myChild = child;
        myType = type;
    }

    public static final class ASTBadPrimary extends ASTParentNode implements ASTPrimaryChild {
        private final ASTKeywordNode myKeywordNode;

        public ASTBadPrimary(Location location, ASTKeywordNode badKeyword) {
            super(location);
            myKeywordNode = badKeyword;
        }

        @Override
        public void setResolvedDataType(TypeSymbol symbol) {
            throw new IllegalStateException("Attempt to set a resolved datatype on a bad primary!");
        }

        @Override
        public TypeSymbol getResolvedDataType() {
            throw new IllegalStateException("Attempt to get a resolved datatype on a bad primary!");
        }

        @Override
        public List<Node> getChildren() {
            return Arrays.asList(myKeywordNode);
        }
    }

    /**
     * Returns an <code>ASTPrimaryChild</code>.
     * @return An <code>ASTPrimaryChild</code>.
     */
    public ASTPrimaryChild getChild() {
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
        return Arrays.asList(Type.EXPR_NAME/*, Type.ELEMENT_ACCESS*/, Type.FIELD_ACCESS).contains(myType);
    }

    /**
     * Looks for something that can be the child of an <code>ASTLeftHandSide</code>.
     * If found, creates and returns the <code>ASTLeftHandSide</code>.
     * @return The <code>ASTLeftHandSide</code>.
     */
    public ASTLeftHandSide getLeftHandSide() {
        return switch (myChild) {
            case ASTExpressionName exprName -> exprName;
            //case ASTElementAccess elementAccess -> elementAccess;
            case ASTFieldAccess fieldAccess -> fieldAccess;
            default -> throw new IllegalStateException("Internal error: Expected a LeftHandSide!");
        };
    }

    @Override
    public void setResolvedDataType(TypeSymbol symbol) {
        myResolvedDataType = symbol;
    }

    @Override
    public TypeSymbol getResolvedDataType() {
        return myResolvedDataType;
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
