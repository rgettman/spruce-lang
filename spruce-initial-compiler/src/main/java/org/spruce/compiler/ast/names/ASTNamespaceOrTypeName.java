package org.spruce.compiler.ast.names;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTNamespaceOrTypeName</code> is a node representing part of a
 * qualified name that could be a namespace name or a type name.</p>
 *
 * <em>
 * NamespaceOrTypeName:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;NamespaceOrTypeName . Identifier<br>
 * </em>
 */
public class ASTNamespaceOrTypeName extends ASTListNode<ASTIdentifier> {
    /**
     * Constructs an <code>ASTNamespaceOrTypeName</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTIdentifier</code>s.
     */
    public ASTNamespaceOrTypeName(Location location, List<ASTIdentifier> children) {
        super(location, children, Type.NAMESPACE_OR_TYPENAME_IDS);
    }
}
