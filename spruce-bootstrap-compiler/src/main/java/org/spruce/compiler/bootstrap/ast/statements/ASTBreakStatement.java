package org.spruce.compiler.bootstrap.ast.statements;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.bootstrap.ast.ASTKeywordNode;
import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.common.Location;

/**
 * <p>An <code>ASTBreakStatement</code> is "break" followed by a semicolon.</p>
 *
 * <em>
 * BreakStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;break ;<br>
 * </em>
 */
public final class ASTBreakStatement extends ASTParentNode implements ASTStatement, ASTHasTarget {
    private final ASTKeywordNode myBreakKeyword;
    private ASTBlockStatement myTarget;

    /**
     * Constructs an <code>ASTBreakStatement</code> at the given <code>Location</code>
     * with the given <code>ASTKeywordNode</code> with keyword <code>break</code>.
     * @param location The <code>Location</code>.
     * @param breakKeyword An <code>ASTKeywordNode</code> of keyword <code>break</code>.
     */
    public ASTBreakStatement(Location location, ASTKeywordNode breakKeyword) {
        super(location);
        myBreakKeyword = breakKeyword;
    }

    /**
     * Returns an <code>ASTKeywordNode</code> of keyword <code>break</code>.
     * @return An <code>ASTKeywordNode</code> of keyword <code>break</code>.
     */
    public ASTKeywordNode getBreakKeyword() {
        return myBreakKeyword;
    }

    @Override
    public void setTarget(ASTBlockStatement target) {
        myTarget = target;
    }

    @Override
    public ASTBlockStatement getTarget() {
        return myTarget;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myBreakKeyword);
    }
}
