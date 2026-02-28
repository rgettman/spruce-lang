package org.spruce.compiler.bootstrap.ast.classes;

import java.util.ArrayList;
import java.util.List;

import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.common.Location;

/**
 * <p>An <code>ASTMethodHeader</code> is an optional TypeParameters followed by
 * a Result and a MethodDeclarator.</p>
 *
 * <em>
 * MethodHeader:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Result MethodDeclarator
 * </em>
 */
public class ASTMethodHeader extends ASTParentNode {
    private final ASTResult myResult;
    private final ASTMethodDeclarator myMethodDecl;

    /**
     * Constructs an <code>ASTMethodHeader</code> at the given <code>Location</code>
     * with the given <code>ASTResult</code> and the given <code>ASTMethodDeclarator</code>.
     * @param location The <code>Location</code>.
     * @param result An <code>ASTResult</code>.
     * @param methodDecl An <code>ASTMethodDeclarator</code>.
     */
    public ASTMethodHeader(Location location, ASTResult result, ASTMethodDeclarator methodDecl) {
        super(location);
        myResult = result;
        myMethodDecl = methodDecl;
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
        children.add(myResult);
        children.add(myMethodDecl);
        return children;
    }
}
