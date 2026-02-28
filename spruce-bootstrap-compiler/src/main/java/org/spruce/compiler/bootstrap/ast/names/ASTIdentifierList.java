package org.spruce.compiler.bootstrap.ast.names;

import java.util.List;

import org.spruce.compiler.bootstrap.ast.ASTListNode;
import org.spruce.compiler.bootstrap.common.Location;

public class ASTIdentifierList extends ASTListNode<ASTIdentifier> {
    /**
     * Constructs an <code>ASTIdentifierList</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTIdentifier</code>s.
     */
    public ASTIdentifierList(Location location, List<ASTIdentifier> children) {
        super(location, children, Type.IDENTIFIERS);
    }
}
