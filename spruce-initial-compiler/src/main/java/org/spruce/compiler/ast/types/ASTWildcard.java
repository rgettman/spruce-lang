package org.spruce.compiler.ast.types;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTWildcard</code> is a "_".</p>
 *
 * <em>
 * WildCard:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;_
 * </em>
 */
public final class ASTWildcard extends ASTParentNode implements ASTTypeArgument {
    private final ASTKeywordNode myWildcard;

    /**
     * Constructs an <code>ASTWildcard</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param wildcard An <code>ASTKeywordNode</code> of keyword <code>UNDERSCORE</code>.
     */
    public ASTWildcard(Location location, ASTKeywordNode wildcard) {
        super(location);
        myWildcard = wildcard;
    }

    /**
     * Returns an <code>ASTKeywordNode</code> representing a Wildcard.
     * @return An <code>ASTKeywordNode</code> of keyword <code>UNDERSCORE</code>.
     */
    public ASTKeywordNode getWildcard() {
        return myWildcard;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myWildcard);
    }
}
