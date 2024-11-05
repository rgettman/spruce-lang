package org.spruce.compiler.ast.names;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.ast.statements.ASTResource;
import org.spruce.compiler.scanner.Location;

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
public final class ASTExpressionName extends ASTListNode<ASTIdentifier> implements ASTResource {
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
     * Converts this to an <code>ASTTypeName</code>.  Converts any child
     * <code>ASTAmbiguousName</code> to an <code>ASTPackageOrTypeName</code>.
     * @return An <code>ASTTypeName</code> with the same structure as this
     *     <code>ASTExpressionName</code>.
     * @see ASTAmbiguousName#convertToNamespaceOrTypeName
     */
    public ASTTypeName convertToTypeName() {
        return new ASTTypeName(getLocation(), getTypedChildren());
    }
}
