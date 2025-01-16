package org.spruce.compiler.ast.expressions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTLambdaParameters</code> is either an identifier or a pair of
 * pipe characters with an optional lambda parameter list in between.</p>
 *
 * <em>
 * LambdaParameters:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;||<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;| [LambdaParameterList] |<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier<br>
 * </em>
 */
public class ASTLambdaParameters extends ASTParentNode {
    private final ASTLambdaParameterList myLambdaParamList;
    private final ASTIdentifier myIdentifier;

    /**
     * Constructs an <code>ASTLambdaParameters</code> with no params.
     * @param location A <code>Location</code>.
     */
    public ASTLambdaParameters(Location location) {
        super(location);
        myLambdaParamList = null;
        myIdentifier = null;
    }

    /**
     * Constructs an <code>ASTLambdaParameters</code> with a list of lambda
     * parameters.
     * @param location The <code>Location</code>.
     * @param lambdaParams An <code>ASTLambdaParameterList</code>.
     */
    public ASTLambdaParameters(Location location, ASTLambdaParameterList lambdaParams) {
        super(location);
        myLambdaParamList = lambdaParams;
        myIdentifier = null;
    }

    /**
     * Constructs an <code>ASTLambdaParameters</code> with a bare identifier as
     * its only parameter.
     * @param location The <code>Location</code>.
     * @param identifier An <code>ASTIdentifier</code>.
     */
    public ASTLambdaParameters(Location location, ASTIdentifier identifier) {
        super(location);
        myLambdaParamList = null;
        myIdentifier = identifier;
    }

    /**
     * Returns an <code>ASTLambdaParameterList</code>, if it exists.
     * @return An <code>Optional&lt;ASTLambdaParameterList&gt;</code>.
     */
    public Optional<ASTLambdaParameterList> getLambdaParams() {
        return Optional.ofNullable(myLambdaParamList);
    }

    /**
     * Returns an <code>ASTIdentifier</code>, if it exists.
     * @return An <code>Optional&lt;ASTIdentifier&gt;</code>.
     */
    public Optional<ASTIdentifier> getIdentifier() {
        return Optional.ofNullable(myIdentifier);
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(2);
        if (myLambdaParamList != null) {
            children.add(myLambdaParamList);
        }
        else if (myIdentifier != null) {
            children.add(myIdentifier);
        }
        return children;
    }
}
