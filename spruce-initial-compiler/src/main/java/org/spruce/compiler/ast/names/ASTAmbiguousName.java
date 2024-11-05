package org.spruce.compiler.ast.names;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTAmbiguousName</code> is a node representing part of a
 * qualified name that could be an expression name, a namespace name, or a type
 * name.</p>
 *
 * <em>
 * AmbiguousName:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;AmbiguousName . Identifier<br>
 * </em>
 */
public class ASTAmbiguousName extends ASTListNode<ASTIdentifier> {
    /**
     * Constructs an <code>ASTAmbiguousName</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTIdentifier</code>s.
     */
    public ASTAmbiguousName(Location location, List<ASTIdentifier> children) {
        super(location, children, Type.AMBIGUOUS_NAME_IDS);
    }

    /**
     * Converts this to an <code>ASTNamespaceOrTypeName</code>.  Converts any child
     * <code>ASTAmbiguousName</code> to an <code>ASTNamespaceOrTypeName</code>.
     * @return An <code>ASTNamespaceOrTypeName</code> with the same structure as this
     *     <code>ASTAmbiguousName</code>.
     * @see ASTExpressionName#convertToTypeName
     */
    public ASTNamespaceOrTypeName convertToNamespaceOrTypeName() {
        return new ASTNamespaceOrTypeName(getLocation(), getTypedChildren());
    }
}
