package org.spruce.compiler.ast.names;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTTypeName</code> is a node representing a simple type name or
 * a name that could be a namespace name or a type name.</p>
 *
 * <em>
 * TypeName:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;NamespaceOrTypeName . Identifier
 * </em>
 */
public class ASTTypeName extends ASTListNode<ASTIdentifier> {
    /**
     * Constructs an <code>ASTTypeName</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTIdentifier</code>s.
     */
    public ASTTypeName(Location location, List<ASTIdentifier> children) {
        super(location, children, Type.TYPENAME_IDS);
    }

    /**
     * Converts this type name to a package or type name.
     * @return A package or type name.
     */
    public ASTNamespaceOrTypeName convertToNamespaceOrTypeName() {
        return new ASTNamespaceOrTypeName(getLocation(), getTypedChildren());
    }
}
