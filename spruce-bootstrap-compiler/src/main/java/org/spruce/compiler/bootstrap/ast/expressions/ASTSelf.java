package org.spruce.compiler.bootstrap.ast.expressions;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.bootstrap.ast.ASTKeywordNode;
import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;

/**
 * An <code>ASTSelf</code> represents "self" by itself, not as part of a larger
 * primary such as a <code>TypenameSelf</code>.
 * <em>
 * Self:
 * &nbsp;&nbsp;&nbsp;&nbsp;self
 * </em>
 */
public final class ASTSelf extends ASTParentNode implements ASTPrimaryChild {
    private final ASTKeywordNode mySelfKeyword;
    private TypeSymbol myResolvedDataType;

    /**
     * Constructs an <code>ASTSelf</code> at the given <code>Location</code>
     * with the given <code>ASTKeywordNode</code> with keyword <code>SELF</code>.
     * @param location The <code>Location</code>.
     * @param selfKeyword An <code>ASTKeywordNode</code> with keyword <code>SELF</code>.
     */
    public ASTSelf(Location location, ASTKeywordNode selfKeyword) {
        super(location);
        mySelfKeyword = selfKeyword;
    }

    /**
     * Returns an <code>ASTKeywordNode</code> representing the "self" keyword.
     * @return An <code>ASTKeywordNode</code> with keyword <code>SELF</code>.
     */
    public ASTKeywordNode getSelfKeyword() {
        return mySelfKeyword;
    }

    @Override
    public void setResolvedDataType(TypeSymbol symbol) {
        myResolvedDataType = symbol;
    }

    @Override
    public TypeSymbol getResolvedDataType() {
        return myResolvedDataType;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(mySelfKeyword);
    }
}
