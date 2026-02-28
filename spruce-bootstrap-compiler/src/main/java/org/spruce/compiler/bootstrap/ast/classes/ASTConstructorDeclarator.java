package org.spruce.compiler.bootstrap.ast.classes;

import java.util.ArrayList;
import java.util.List;

import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.common.Location;

/**
 * <p>An <code>ASTConstructorDeclarator</code> is optionally Type Arguments,
 * then "constructor", followed by a pair of parentheses that may contain an
 * Formal Parameter List.</p>
 *
 * <em>
 * ConstructorDeclarator:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;constructor ( [FormalParameterList] )
 * </em>
 */
public class ASTConstructorDeclarator extends ASTParentNode {
    private final ASTFormalParameterList myFormalParamList;

    /**
     * Constructs an <code>ASTConstructorDeclarator</code> at the given <code>Location</code>
     * with the given <code>ASTFormalParameterList</code>.
     * @param location The <code>Location</code>.
     * @param formalParamList An <code>ASTFormalParameterList</code>.
     */
    public ASTConstructorDeclarator(Location location, ASTFormalParameterList formalParamList) {
        super(location);
        myFormalParamList = formalParamList;
    }

    /**
     * Returns an <code>ASTFormalParameterList</code>.
     * @return An <code>ASTFormalParameterList</code>.
     */
    public ASTFormalParameterList getFormalParamList() {
        return myFormalParamList;
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(2);
        children.add(myFormalParamList);
        return children;
    }
}
