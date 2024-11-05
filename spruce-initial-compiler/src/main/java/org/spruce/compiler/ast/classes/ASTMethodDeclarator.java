package org.spruce.compiler.ast.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTMethodDeclarator</code> is an identifier followed by an
 * optional formal parameter list within parentheses, optionally followed by a
 * MutModifier.</p>
 *
 * <em>
 * MethodDeclarator:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier ( [FormalParameterList] ) [MutModifier]
 * </em>
 */
public class ASTMethodDeclarator extends ASTParentNode {
    private final ASTIdentifier myName;
    private final ASTFormalParameterList myFormalParamList;
    private final ASTKeywordNode myMutModifier;

    /**
     * Constructs an <code>ASTMethodDeclarator</code> at the given <code>Location</code>
     * with the given <code>ASTIdentifier</code> representing the method name,
     * the given <code>ASTFormalParameterList</code>,
     * and the given <code>ASTKeywordNode</code> representing the MutModifier.
     * @param location The <code>Location</code>.
     * @param name An <code>ASTIdentifier</code> representing the method name.
     * @param formalParamList An <code>ASTFormalParameterList</code>.
     * @param mutMod An <code>ASTKeywordNode</code> of keyword <code>mut</code>.
     */
    public ASTMethodDeclarator(Location location, ASTIdentifier name, ASTFormalParameterList formalParamList, ASTKeywordNode mutMod) {
        super(location);
        myName = name;
        myFormalParamList = formalParamList;
        myMutModifier = mutMod;
    }

    /**
     * Constructs an <code>ASTMethodDeclarator</code> at the given <code>Location</code>
     * with the given <code>ASTIdentifier</code> representing the method name, and
     * the given <code>ASTFormalParameterList</code>.
     * @param location The <code>Location</code>.
     * @param name An <code>ASTIdentifier</code> representing the method name.
     * @param formalParamList An <code>ASTFormalParameterList</code>.
     */
    public ASTMethodDeclarator(Location location, ASTIdentifier name, ASTFormalParameterList formalParamList) {
        super(location);
        myName = name;
        myFormalParamList = formalParamList;
        myMutModifier = null;
    }

    /**
     * Returns an <code>ASTIdentifier</code> representing the method name.
     * @return An <code>ASTIdentifier</code> representing the method name.
     */
    public ASTIdentifier getName() {
        return myName;
    }

    /**
     * Returns an <code>ASTFormalParameterList</code>.
     * @return An <code>ASTFormalParameterList</code>.
     */
    public ASTFormalParameterList getFormalParamList() {
        return myFormalParamList;
    }

    /**
     * Returns an <code>ASTKeywordNode</code> representing the MutModifier, if it exists.
     * @return An <code>Optional&lt;ASTKeywordNode&gt;</code> of keyword <code>mut</code>.
     */
    public Optional<ASTKeywordNode> getMutModifier() {
        return Optional.ofNullable(myMutModifier);
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(3);
        children.add(myName);
        children.add(myFormalParamList);
        if (myMutModifier != null) {
            children.add(myMutModifier);
        }
        return children;
    }
}
