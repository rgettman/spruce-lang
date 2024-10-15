package org.spruce.compiler.ast.expressions;

import java.util.Arrays;
import java.util.Optional;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.scanner.Location;

import static org.spruce.compiler.scanner.TokenType.PIPE;

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
    private final ASTListNode myLambdaParams;
    private final ASTIdentifier myIdentifier;

    /**
     * Constructs an <code>ASTLambdaParameters</code> with no params.
     * @param location A <code>Location</code>.
     */
    public ASTLambdaParameters(Location location) {
        super(location, Arrays.asList(), PIPE);
        myLambdaParams = null;
        myIdentifier = null;
    }

    /**
     * Constructs an <code>ASTLambdaParameters</code> with a list of lambda
     * parameters.
     * @param location The <code>Location</code>.
     * @param lambdaParams An <code>ASTListNode</code> representing the lambda parameters.
     */
    public ASTLambdaParameters(Location location, ASTListNode lambdaParams) {
        super(location, Arrays.asList(lambdaParams), PIPE);
        myLambdaParams = lambdaParams;
        myIdentifier = null;
    }

    /**
     * Constructs an <code>ASTLambdaParameters</code> with a bare identifier as
     * its only parameter.
     * @param location The <code>Location</code>.
     * @param identifier An <code>ASTIdentifier</code>.
     */
    public ASTLambdaParameters(Location location, ASTIdentifier identifier) {
        super(location, Arrays.asList(identifier));
        myLambdaParams = null;
        myIdentifier = identifier;
    }

    /**
     * TODO: For removal when removing collapsing.
     */
    @Override
    public boolean isCollapsible() {
        return false;
    }

    /**
     * Returns an <code>ASTListNode</code> of lambda parameters, if it exists.
     * @return An <code>Optional&lt;ASTListNode&gt;</code>.
     */
    public Optional<ASTListNode> getLambdaParams() {
        return Optional.ofNullable(myLambdaParams);
    }

    /**
     * Returns an <code>ASTIdentifier</code>, if it exists.
     * @return An <code>Optional&lt;ASTIdentifier&gt;</code>.
     */
    public Optional<ASTIdentifier> getIdentifier() {
        return Optional.ofNullable(myIdentifier);
    }
}
