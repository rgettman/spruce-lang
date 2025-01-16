package org.spruce.compiler.ast;

import org.spruce.compiler.common.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * An <code>ASTKeywordNode</code> is a leaf <code>ASTNode</code> with a keyword
 * but no value.
 */
public class ASTKeywordNode extends ASTNode {
    private final TokenType myKeyword;

    /**
     * Constructs an <code>ASTKeywordNode</code> with the given <code>Location</code>
     * and the keyword.
     * @param location The <code>Location</code>.
     * @param keyword The keyword as a <code>TokenType</code>.
     */
    public ASTKeywordNode(Location location, TokenType keyword) {
        super(location);
        myKeyword = keyword;
    }

    /**
     * Returns the <code>TokenType</code> representing the keyword.
     * @return The <code>TokenType</code> representing the keyword.
     */
    public TokenType getKeyword() {
        return myKeyword;
    }

    /**
     * Returns the keyword's string representation.
     * @return A header value for this node.
     */
    @Override
    public String getHeaderValue() {
        return getKeyword().getRepresentation();
    }
}
