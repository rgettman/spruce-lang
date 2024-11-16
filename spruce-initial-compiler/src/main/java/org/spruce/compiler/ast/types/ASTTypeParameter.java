package org.spruce.compiler.ast.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTAnnotatedNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.classes.ASTAnnotationList;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTTypeParameter</code> is an optional AnnotationList, followed
 * by an identifier, optionally followed by a TypeBound.</p>
 *
 * <em>
 * TypeParameter:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] Identifier [TypeBound]<br>
 * </em>
 */
public class ASTTypeParameter extends ASTAnnotatedNode {
    private final ASTIdentifier myName;
    private final ASTIntersectionType myTypeBound;

    /**
     * Constructs an <code>ASTTypeParameter</code> at the given <code>Location</code>
     * with the given <code>ASTAnnotationList</code>, the given <code>ASTIdentifier</code>
     * representing the type parameter name, and the given <code>ASTIntersectionType</code>.
     * @param location The <code>Location</code>.
     * @param annList An <code>ASTAnnotationList</code>, possibly empty.
     * @param name An <code>ASTIdentifier</code> representing the type parameter name.
     * @param typeBound An <code>ASTIntersectionType</code>.
     */
    public ASTTypeParameter(Location location, ASTAnnotationList annList, ASTIdentifier name, ASTIntersectionType typeBound) {
        super(location, annList);
        myName = name;
        myTypeBound = typeBound;
    }

    /**
     * Constructs an <code>ASTTypeParameter</code> at the given <code>Location</code>
     * with the given <code>ASTAnnotationList</code> and the given <code>ASTIdentifier</code>
     * representing the type parameter name.
     * @param location The <code>Location</code>.
     * @param annList An <code>ASTAnnotationList</code>, possibly empty.
     * @param name An <code>ASTIdentifier</code> representing the type parameter name.
     */
    public ASTTypeParameter(Location location, ASTAnnotationList annList, ASTIdentifier name) {
        super(location, annList);
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
        List<Node> children = new ArrayList<>(3);
        children.add(myAnnList);
        children.add(myName);
        if (myTypeBound != null) {
            children.add(myTypeBound);
        }
        return children;
    }
}
