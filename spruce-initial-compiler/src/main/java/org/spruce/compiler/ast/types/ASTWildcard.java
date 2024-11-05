package org.spruce.compiler.ast.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTWildcard</code> is a "?" optionally followed by a
 * WildcardBounds.</p>
 *
 * <em>
 * WildCard:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;?<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;? WildcardBounds
 * </em>
 */
public final class ASTWildcard extends ASTParentNode implements ASTTypeArgument {
    private final ASTKeywordNode myWildcard;
    private final ASTWildcardBounds myBounds;

    /**
     * Constructs an <code>ASTWildcard</code> at the given <code>Location</code>
     * given the <code>ASTKeywordNode</code> representing a Wildcard.
     * @param location The <code>Location</code>.
     * @param wildcard An <code>ASTKeywordNode</code> of keyword <code>QUESTION_MARK</code>.
     * @param bounds An <code>ASTWildcardBounds</code>.
     */
    public ASTWildcard(Location location, ASTKeywordNode wildcard, ASTWildcardBounds bounds) {
        super(location);
        myWildcard = wildcard;
        myBounds = bounds;
    }

    /**
     * Constructs an <code>ASTWildcard</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param wildcard An <code>ASTKeywordNode</code> of keyword <code>QUESTION_MARK</code>.
     */
    public ASTWildcard(Location location, ASTKeywordNode wildcard) {
        super(location);
        myWildcard = wildcard;
        myBounds = null;
    }

    /**
     * Returns an <code>ASTKeywordNode</code> representing a Wildcard.
     * @return An <code>ASTKeywordNode</code> of keyword <code>QUESTION_MARK</code>.
     */
    public ASTKeywordNode getWildcard() {
        return myWildcard;
    }

    /**
     * Returns an <code>ASTWildcardBounds</code>, if it exists.
     * @return An <code>Optional&lt;ASTWildcardBounds&gt;</code>.
     */
    public Optional<ASTWildcardBounds> getBounds() {
        return Optional.ofNullable(myBounds);
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(2);
        children.add(myWildcard);
        if (myBounds != null) {
            children.add(myBounds);
        }
        return children;
    }
}
