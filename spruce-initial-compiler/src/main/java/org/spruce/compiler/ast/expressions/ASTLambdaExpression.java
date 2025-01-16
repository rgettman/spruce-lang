package org.spruce.compiler.ast.expressions;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTLambdaExpression</code> is lambda parameters, an arrow,
 * then a lambda body.</p>
 *
 * <em>
 * LambdaExpression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;LambdaParameters -> LambdaBody<br>
 * </em>
 */
public final class ASTLambdaExpression extends ASTParentNode implements ASTExpression {
    private final ASTLambdaParameters myLambdaParameters;
    private final ASTLambdaBody myLambdaBody;

    /**
     * Constructs an <code>ASTLambdaExpression</code> at the given <code>Location</code>
     * with the given <code>ASTLambdaParameters</code> and the given
     * <code>ASTLambdaBody</code>.
     * @param location The <code>Location</code>.
     * @param lambdaParameters A <code>ASTLambdaParameters</code>.
     * @param lambdaBody An <code>ASTLambdaBody</code>.
     */
    public ASTLambdaExpression(Location location, ASTLambdaParameters lambdaParameters, ASTLambdaBody lambdaBody) {
        super(location);
        myLambdaParameters = lambdaParameters;
        myLambdaBody = lambdaBody;
    }

    /**
     * Returns an <code>ASTLambdaParameters</code>.
     * @return An <code>ASTLambdaParameters</code>.
     */
    public ASTLambdaParameters getLambdaParameters() {
        return myLambdaParameters;
    }

    /**
     * Returns an <code>ASTLambdaBody</code>.
     * @return An <code>ASTLambdaBody</code>.
     */
    public ASTLambdaBody getLambdaBody() {
        return myLambdaBody;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myLambdaParameters, myLambdaBody);
    }
}
