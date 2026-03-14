package org.spruce.compiler.bootstrap.ast.types;

import java.util.ArrayList;
import java.util.List;

import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.ast.names.ASTIdentifier;
import org.spruce.compiler.bootstrap.common.Location;

/**
 * <p>An <code>ASTSimpleType</code> is an identifier.</p>
 *
 * <em>
 * SimpleType:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier
 * </em>
 */
public class ASTSimpleType extends ASTParentNode {
    private final ASTIdentifier myName;

    /**
     * Constructs an <code>ASTSimpleType</code> at the given <code>Location</code>
     * with the given <code>ASTIdentifier</code> representing the name.
     * @param location The <code>Location</code>.
     * @param name An <code>ASTIdentifier</code> representing the name.
     */
    public ASTSimpleType(Location location, ASTIdentifier name) {
        super(location);
        myName = name;
    }

    /**
     * Returns an <code>ASTIdentifier</code> representing the name.
     * @return An <code>ASTIdentifier</code> representing the name.
     */
    public ASTIdentifier getName() {
        return myName;
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(2);
        children.add(myName);
        return children;
    }
}
