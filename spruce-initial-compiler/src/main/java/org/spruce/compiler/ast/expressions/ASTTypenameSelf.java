package org.spruce.compiler.ast.expressions;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTTypeName;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTTypeNameSelf</code> is a type name followed by a "." followed
 * by "self".  This is only used inside an <code>ASTPrimary</code>.</p>
 * @see ASTPrimary
 */
public class ASTTypenameSelf extends ASTParentNode {
    private final ASTTypeName myTypename;
    private final ASTKeywordNode mySelfKeyword;

    /**
     * Constructs an <code>ASTTypenameSelf</code> at the given <code>Location</code>
     * with the given <code>ASTTypeName</code> and the given <code>ASTKeywordNode</code>
     * with keyword <code>SELF</code>.
     * @param location The <code>Location</code>.
     * @param typename An <code>ASTTypeName</code>.
     * @param selfKeyword An <code>ASTKeywordNode</code> with keyword <code>SELF</code>.
     */
    public ASTTypenameSelf(Location location, ASTTypeName typename, ASTKeywordNode selfKeyword) {
        super(location);
        myTypename = typename;
        mySelfKeyword = selfKeyword;
    }

    /**
     * Returns an <code>ASTTypeName</code>.
     * @return An <code>ASTTypeName</code>.
     */
    public ASTTypeName getTypename() {
        return myTypename;
    }

    /**
     * Returns an <code>ASTKeywordNode</code> representing the "self" keyword.
     * @return An <code>ASTKeywordNode</code> with keyword <code>SELF</code>.
     */
    public ASTKeywordNode getSelfKeyword() {
        return mySelfKeyword;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myTypename, mySelfKeyword);
    }
}
