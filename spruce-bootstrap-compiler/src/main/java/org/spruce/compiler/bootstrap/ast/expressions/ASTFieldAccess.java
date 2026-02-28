package org.spruce.compiler.bootstrap.ast.expressions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.bootstrap.ast.ASTKeywordNode;
import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.ast.names.ASTIdentifier;
import org.spruce.compiler.bootstrap.ast.names.ASTTypeName;
//import org.spruce.compiler.bootstrap.ast.statements.ASTResource;
import org.spruce.compiler.bootstrap.common.Location;

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
public final class ASTFieldAccess extends ASTParentNode implements /*ASTResource,*/ ASTLeftHandSide {
    private final ASTTypeName myTypeName;
    private final ASTKeywordNode mySooper;
    private final ASTPrimary myPrimary;
    private final ASTIdentifier myIdentifier;

    /**
     * Constructs an <code>ASTFieldAccess</code> given a Primary and an Identifier.
     * @param location A <code>Location</code>.
     * @param primary An <code>ASTPrimary</code>.
     * @param identifier An <code>ASTIdentifier</code>.
     */
    public ASTFieldAccess(Location location, ASTPrimary primary, ASTIdentifier identifier) {
        super(location);
        myTypeName = null;
        mySooper = null;
        myPrimary = primary;
        myIdentifier = identifier;
    }

    /**
     * Constructs an <code>ASTFieldAccess</code> given a Primary and an Identifier.
     * @param location A <code>Location</code>.
     * @param sooper An <code>ASTKeywordNode</code> of type <code>super</code>.
     * @param identifier An <code>ASTIdentifier</code>.
     */
    public ASTFieldAccess(Location location, ASTKeywordNode sooper, ASTIdentifier identifier) {
        super(location);
        myTypeName = null;
        mySooper = sooper;
        myPrimary = null;
        myIdentifier = identifier;
    }

    /**
     * Constructs an <code>ASTFieldAccess</code> given a Primary and an Identifier.
     * @param location A <code>Location</code>.
     * @param typeName An <code>ASTTypeName</code>.
     * @param sooper An <code>ASTKeywordNode</code> of type <code>super</code>.
     * @param identifier An <code>ASTIdentifier</code>.
     */
    public ASTFieldAccess(Location location, ASTTypeName typeName, ASTKeywordNode sooper, ASTIdentifier identifier) {
        super(location);
        myTypeName = typeName;
        mySooper = sooper;
        myPrimary = null;
        myIdentifier = identifier;
    }

    /**
     * Returns an <code>ASTTypeName</code>, if it exists.
     * @return An <code>Optional&lt;ASTTypeName&gt;</code>.
     */
    public Optional<ASTTypeName> getTypeName() {
        return Optional.ofNullable(myTypeName);
    }

    /**
     * Returns an <code>ASTKeywordNode</code> of type <code>super</code>, if it exists.
     * @return An <code>Optional&lt;ASTKeywordNode&gt;</code> of type <code>super</code>.
     */
    public Optional<ASTKeywordNode> getSooper() {
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

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(3);
        if (myPrimary != null) {
            children.add(myPrimary);
        }
        if (myTypeName != null) {
            children.add(myTypeName);
        }
        if (mySooper != null) {
            children.add(mySooper);
        }
        children.add(myIdentifier);
        return children;
    }
}
