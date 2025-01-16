package org.spruce.compiler.ast.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.types.ASTTypeParameterList;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTMethodHeader</code> is an optional TypeParameters followed by
 * a Result and a MethodDeclarator.</p>
 *
 * <em>
 * MethodHeader:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Result MethodDeclarator<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;TypeParameters Result MethodDeclarator
 * </em>
 */
public class ASTMethodHeader extends ASTParentNode {
    private final ASTTypeParameterList myTypeParams;
    private final ASTResult myResult;
    private final ASTMethodDeclarator myMethodDecl;

    /**
     * Constructs an <code>ASTMethodHeader</code> at the given <code>Location</code>
     * with the given <code>ASTTypeParameterList</code>,
     * the given <code>ASTResult</code>, and the given <code>ASTMethodDeclarator</code>.
     * @param location The <code>Location</code>.
     * @param typeParams An <code>ASTTypeParameterList</code>.
     * @param result An <code>ASTResult</code>.
     * @param methodDecl An <code>ASTMethodDeclarator</code>.
     */
    public ASTMethodHeader(Location location, ASTTypeParameterList typeParams, ASTResult result, ASTMethodDeclarator methodDecl) {
        super(location);
        myTypeParams = typeParams;
        myResult = result;
        myMethodDecl = methodDecl;
    }

    /**
     * Constructs an <code>ASTMethodHeader</code> at the given <code>Location</code>
     * with the given <code>ASTResult</code> and the given <code>ASTMethodDeclarator</code>.
     * @param location The <code>Location</code>.
     * @param result An <code>ASTResult</code>.
     * @param methodDecl An <code>ASTMethodDeclarator</code>.
     */
    public ASTMethodHeader(Location location, ASTResult result, ASTMethodDeclarator methodDecl) {
        super(location);
        myTypeParams = null;
        myResult = result;
        myMethodDecl = methodDecl;
    }

    /**
     * Returns an <code>ASTTypeParameterList</code>, if it exists.
     * @return An <code>Optional&lt;ASTTypeParameterList&gt;</code>.
     */
    public Optional<ASTTypeParameterList> getTypeParams() {
        return Optional.ofNullable(myTypeParams);
    }

    /**
     * Returns an <code>ASTResult</code>.
     * @return An <code>ASTResult</code>.
     */
    public ASTResult getResult() {
        return myResult;
    }

    /**
     * Returns an <code>ASTMethodDeclarator</code>.
     * @return An <code>ASTMethodDeclarator</code>.
     */
    public ASTMethodDeclarator getMethodDecl() {
        return myMethodDecl;
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(3);
        if (myTypeParams != null) {
            children.add(myTypeParams);
        }
        children.add(myResult);
        children.add(myMethodDecl);
        return children;
    }
}
