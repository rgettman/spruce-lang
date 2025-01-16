package org.spruce.compiler.ast.statements;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTContinueStatement</code> is "continue" followed by a semicolon.</p>
 *
 * <em>
 * ContinueStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;continue ;<br>
 * </em>
 */
public final class ASTContinueStatement extends ASTParentNode implements ASTStatement {
    private final ASTKeywordNode myContinueKeyword;

    /**
     * Constructs an <code>ASTContinueStatement</code> at the given <code>Location</code>
     * with the given <code>ASTKeywordNode</code> with keyword <code>continue</code>.
     * @param location The <code>Location</code>.
     * @param continueKeyword An <code>ASTKeywordNode</code> of keyword <code>continue</code>.
     */
    public ASTContinueStatement(Location location, ASTKeywordNode continueKeyword) {
        super(location);
        myContinueKeyword = continueKeyword;
    }

    /**
     * Returns an <code>ASTKeywordNode</code> of keyword <code>continue</code>.
     * @return An <code>ASTKeywordNode</code> of keyword <code>continue</code>.
     */
    public ASTKeywordNode getContinueKeyword() {
        return myContinueKeyword;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myContinueKeyword);
    }
}
