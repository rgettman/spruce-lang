package org.spruce.compiler.bootstrap.ast.names;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.bootstrap.ast.ASTListNode;
import org.spruce.compiler.bootstrap.ast.expressions.ASTLeftHandSide;
//import org.spruce.compiler.bootstrap.ast.statements.ASTResource;
import org.spruce.compiler.bootstrap.common.Location;

/**
 * <p>An <code>ASTExpressionName</code> is a node representing a simple name or
 * a qualified name.</p>
 *
 * <em>
 * ExpressionName:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;AmbiguousName . Identifier<br>
 * </em>
 */
public final class ASTExpressionName extends ASTListNode<ASTIdentifier> implements /*ASTResource,*/ ASTLeftHandSide {
    /**
     * Constructs an <code>ASTExpressionName</code> at the given <code>Location</code>
     * and with at least one node as its children.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTIdentifier</code>s.
     */
    public ASTExpressionName(Location location, List<ASTIdentifier> children) {
        super(location, children, Type.EXPR_NAME_IDS);
    }

    /**
     * Create a bad ExpressionName as a placeholder in case of an error.
     * @param loc The <code>Location</code>.
     * @return An <code>ASTExpressionName</code> at the given <code>Location</code>
     *     with one identifier "Bad Expression Name".
     */
    public static ASTExpressionName badExpressionName(Location loc) {
        return new ASTExpressionName(loc, Arrays.asList(new ASTIdentifier(loc, "Bad Expression Name")));
    }

    /**
     * Converts this to an <code>ASTTypeName</code>.
     * @return An <code>ASTTypeName</code> with the same structure as this
     *     <code>ASTExpressionName</code>.
     */
    public ASTTypeName convertToTypeName() {
        return new ASTTypeName(getLocation(), getTypedChildren());
    }
}
