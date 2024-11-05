package org.spruce.compiler.ast.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.types.ASTTypeParameterList;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTConstructorDeclarator</code> is optionally Type Arguments,
 * then "constructor", followed by a pair of parentheses that may contain an
 * Formal Parameter List.</p>
 *
 * <em>
 * ConstructorDeclarator:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[TypeParameters] constructor ( [FormalParameterList] )
 * </em>
 */
public class ASTConstructorDeclarator extends ASTParentNode {
    private final ASTTypeParameterList myTypeParams;
    private final ASTFormalParameterList myFormalParamList;

    /**
     * Constructs an <code>ASTConstructorDeclarator</code> at the given <code>Location</code>
     * with the given <code>ASTTypeParameterList</code> and the given
     * <code>ASTFormalParameterList</code>.
     * @param location The <code>Location</code>.
     * @param typeParams An <code>ASTTypeParameterList</code>.
     * @param formalParamList An <code>ASTFormalParameterList</code>.
     */
    public ASTConstructorDeclarator(Location location, ASTTypeParameterList typeParams, ASTFormalParameterList formalParamList) {
        super(location);
        myTypeParams = typeParams;
        myFormalParamList = formalParamList;
    }

    /**
     * Constructs an <code>ASTConstructorDeclarator</code> at the given <code>Location</code>
     * with the given <code>ASTListNode</code> representing the Formal Parameter list.
     * @param location The <code>Location</code>.
     * @param formalParamList An <code>ASTFormalParameterList</code>.
     */
    public ASTConstructorDeclarator(Location location, ASTFormalParameterList formalParamList) {
        super(location);
        myTypeParams = null;
        myFormalParamList = formalParamList;
    }

    /**
     * Returns an <code>ASTTypeParameterList</code>, if it exists.
     * @return An <code>Optional&lt;ASTTypeParameterList&gt;</code>.
     */
    public Optional<ASTTypeParameterList> getTypeParams() {
        return Optional.ofNullable(myTypeParams);
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
        if (myTypeParams != null) {
            children.add(myTypeParams);
        }
        children.add(myFormalParamList);
        return children;
    }

    /**
     * Helper method to create a string representation of this node.  It takes
     * into account where in the tree this node is.
     * @param prefix A string to indent the printing of this node.
     * @param isTail Whether this node is last in its siblings (or the only child).
     * @return The String representation of this node.
     */
    @Override
    public String toString(String prefix, boolean isTail) {
        StringBuilder buf = new StringBuilder();
        buf.append(prefix).append(isTail ? "└── " : "├── ").append(getHeaderValue()).append("\n");
        if (myTypeParams != null) {
            buf.append(myTypeParams.toString(prefix + (isTail ? "    " : "|   "), false)).append("\n");
        }
        buf.append(myFormalParamList.toString(prefix + (isTail ? "    " : "|   "), true)).append("\n");
        return buf.toString();
    }
}
