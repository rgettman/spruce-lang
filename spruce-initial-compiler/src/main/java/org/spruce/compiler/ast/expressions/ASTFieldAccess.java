package org.spruce.compiler.ast.expressions;

import java.util.Arrays;
import java.util.Optional;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.scanner.Location;

import static org.spruce.compiler.scanner.TokenType.DOT;

/**
 * <p>An <code>ASTFieldAccess</code> is primary, "super", or TypeName "." "super"
 * followed by "." Identifier.</p>
 *
 * <em>
 * FieldAccess:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Primary . Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;super . Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;TypeName . super . Identifier
 * </em>
 */
public class ASTFieldAccess extends ASTParentNode {
    private final ASTListNode myTypeName;
    private final ASTSuper mySooper;
    private final ASTPrimary myPrimary;
    private final ASTIdentifier myIdentifier;

    /**
     * Constructs an <code>ASTFieldAccess</code> given a Primary and an Identifier.
     * @param location A <code>Location</code>.
     * @param primary An <code>ASTPrimary</code>.
     * @param identifier An <code>ASTIdentifier</code>.
     */
    public ASTFieldAccess(Location location, ASTPrimary primary, ASTIdentifier identifier) {
        super(location, Arrays.asList(primary, identifier), DOT);
        myTypeName = null;
        mySooper = null;
        myPrimary = primary;
        myIdentifier = identifier;
    }

    /**
     * Constructs an <code>ASTFieldAccess</code> given a Primary and an Identifier.
     * @param location A <code>Location</code>.
     * @param sooper An <code>ASTSuper</code>.
     * @param identifier An <code>ASTIdentifier</code>.
     */
    public ASTFieldAccess(Location location, ASTSuper sooper, ASTIdentifier identifier) {
        super(location, Arrays.asList(sooper, identifier), DOT);
        myTypeName = null;
        mySooper = sooper;
        myPrimary = null;
        myIdentifier = identifier;
    }

    /**
     * Constructs an <code>ASTFieldAccess</code> given a Primary and an Identifier.
     * @param location A <code>Location</code>.
     * @param typeName An <code>ASTListNode</code> representing a Type Name.
     * @param sooper An <code>ASTSuper</code>.
     * @param identifier An <code>ASTIdentifier</code>.
     */
    public ASTFieldAccess(Location location, ASTListNode typeName, ASTSuper sooper, ASTIdentifier identifier) {
        super(location, Arrays.asList(typeName, sooper, identifier), DOT);
        myTypeName = typeName;
        mySooper = sooper;
        myPrimary = null;
        myIdentifier = identifier;
    }

    /**
     * TODO: For removal when removing collapsing.
     */
    @Override
    public boolean isCollapsible() {
        return false;
    }

    /**
     * Returns an <code>ASTListNode</code> representing a Type Name, if it exists.
     * @return An <code>Optional&lt;ASTListNode&gt;</code>.
     */
    public Optional<ASTListNode> getTypeName() {
        return Optional.ofNullable(myTypeName);
    }

    /**
     * Returns an <code>ASTSuper</code>, if it exists.
     * @return An <code>Optional&lt;ASTSuper&gt;</code>.
     */
    public Optional<ASTSuper> getSooper() {
        return Optional.ofNullable(mySooper);
    }

    /**
     * Returns an <code>ASTPrimary</code>, if it exists.
     * @return An <code>Optional&lt;ASTPrimary&gt;</code>.
     */
    public Optional<ASTPrimary> getPrimary() {
        return Optional.ofNullable(myPrimary);
    }

    /**
     * Returns an <code>ASTIdentifier</code>.
     * @return An <code>ASTIdentifier</code>.
     */
    public ASTIdentifier getIdentifier() {
        return myIdentifier;
    }
}
