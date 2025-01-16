package org.spruce.compiler.ast.statements;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTFallthroughStatement</code> is "fallthrough" followed by a semicolon.</p>
 *
 * <em>
 * FallthroughStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;fallthrough ;<br>
 * </em>
 */
public final class ASTFallthroughStatement extends ASTParentNode implements ASTStatement {
    private final ASTKeywordNode myFallthroughKeyword;

    /**
     * Constructs an <code>ASTFallthroughStatement</code> at the given <code>Location</code>
     * with the given <code>ASTKeywordNode</code> with keyword <code>fallthrough</code>.
     * @param location The <code>Location</code>.
     * @param fallthroughKeyword An <code>ASTKeywordNode</code> of keyword <code>fallthrough</code>.
     */
    public ASTFallthroughStatement(Location location, ASTKeywordNode fallthroughKeyword) {
        super(location);
        myFallthroughKeyword = fallthroughKeyword;
    }

    /**
     * Returns an <code>ASTKeywordNode</code> of keyword <code>fallthrough</code>.
     * @return An <code>ASTKeywordNode</code> of keyword <code>fallthrough</code>.
     */
    public ASTKeywordNode getFallthroughKeyword() {
        return myFallthroughKeyword;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myFallthroughKeyword);
    }
}
