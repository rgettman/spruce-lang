package org.spruce.compiler.bootstrap.ast.classes;

import java.util.ArrayList;
import java.util.List;

import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.ast.names.ASTIdentifier;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.symbol.Symbol;

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
    private Symbol myDeclSymbol;

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
     * Sets the declaration <code>Symbol</code>.
     * @param symbol The declaration <code>Symbol</code>.
     */
    public void setDeclSymbol(Symbol symbol) {
        myDeclSymbol = symbol;
    }

    /**
     * Returns the declaration <code>Symbol</code>.
     * @return The declaration <code>Symbol</code>.
     */
    public Symbol getDeclSymbol(){
        return myDeclSymbol;
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(2);
        children.add(myName);
        children.add(myFormalParamList);
        return children;
    }
}
