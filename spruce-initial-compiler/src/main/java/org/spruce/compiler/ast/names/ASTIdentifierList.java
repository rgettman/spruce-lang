package org.spruce.compiler.ast.names;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.scanner.Location;

public class ASTIdentifierList extends ASTListNode<ASTIdentifier> {
    /**
     * Constructs an <code>ASTListNode</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTIdentifier</code>s.
     */
    public ASTIdentifierList(Location location, List<ASTIdentifier> children) {
        super(location, children, Type.IDENTIFIERS);
    }
}
