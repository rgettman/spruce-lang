package org.spruce.compiler.ast.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTTypeParameter</code> is an identifier optionally followed by
 * a TypeBound.</p>
 *
 * <em>
 * TypeParameter:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier TypeBound
 * </em>
 */
public class ASTTypeParameter extends ASTParentNode {
    private final ASTIdentifier myName;
    private final ASTIntersectionType myTypeBound;

    /**
     * Constructs an <code>ASTTypeParameter</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param name An <code>ASTIdentifier</code> representing the type parameter name.
     * @param typeBound An <code>ASTIntersectionType</code>.
     */
    public ASTTypeParameter(Location location, ASTIdentifier name, ASTIntersectionType typeBound) {
        super(location);
        myName = name;
        myTypeBound = typeBound;
    }

    /**
     * Constructs an <code>ASTTypeParameter</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param name An <code>ASTIdentifier</code> representing the type parameter name.
     */
    public ASTTypeParameter(Location location, ASTIdentifier name) {
        super(location);
        myName = name;
        myTypeBound = null;
    }

    /**
     * Returns an <code>ASTIdentifier</code> representing the type parameter name.
     * @return An <code>ASTIdentifier</code> representing the type parameter name.
     */
    public ASTIdentifier getName() {
        return myName;
    }

    /**
     * Returns an <code>ASTIntersectionType</code>, if it exists.
     * @return An <code>Optional&lt;ASTIntersectionType&gt;</code>.
     */
    public Optional<ASTIntersectionType> getTypeBound() {
        return Optional.ofNullable(myTypeBound);
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(2);
        children.add(myName);
        if (myTypeBound != null) {
            children.add(myTypeBound);
        }
        return children;
    }
}
