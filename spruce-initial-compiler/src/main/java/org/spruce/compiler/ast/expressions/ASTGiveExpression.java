package org.spruce.compiler.ast.expressions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTGiveExpression</code> is an expression optionally preceded by
 * "give".</p>
 *
 * <em>
 * GiveExpression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Expression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;give Expression<br>
 * </em>
 */
public final class ASTGiveExpression extends ASTParentNode {
    private final ASTKeywordNode myGiveKeyword;
    private final ASTExpression myExpr;

    /**
     * Constructs an <code>ASTGiveExpression</code> at the given <code>Location</code>
     * with the given <code>ASTKeywordNode</code> representing <code>give</code>
     * and the given <code>ASTExpression</code> as the expression being given.
     * @param location The <code>Location</code>.
     * @param giveKeyword An <code>ASTKeywordNode</code> representing <code>give</code>.
     * @param expr An <code>ASTExpression</code>.
     */
    public ASTGiveExpression(Location location, ASTKeywordNode giveKeyword, ASTExpression expr) {
        super(location);
        myGiveKeyword = giveKeyword;
        myExpr = expr;
    }

    /**
     * Constructs an <code>ASTGiveExpression</code> at the given <code>Location</code>
     * with the given <code>ASTExpression</code>.
     * @param location The <code>Location</code>.
     * @param expr An <code>ASTExpression</code> as the expression being given.
     */
    public ASTGiveExpression(Location location, ASTExpression expr) {
        super(location);
        myGiveKeyword = null;
        myExpr = expr;
    }

    public Optional<ASTKeywordNode> getGiveKeyword() {
        return Optional.ofNullable(myGiveKeyword);
    }

    /**
     * Returns an <code>ASTExpression</code>.
     * @return An <code>ASTExpression</code>.
     */
    public ASTExpression getExpr() {
        return myExpr;
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(2);
        if (myGiveKeyword != null) {
            children.add(myGiveKeyword);
        }
        children.add(myExpr);
        return children;
    }
}
