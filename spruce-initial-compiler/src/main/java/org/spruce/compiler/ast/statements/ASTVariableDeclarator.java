package org.spruce.compiler.ast.statements;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.expressions.ASTVariableInitializer;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTVariableDeclarator</code> is an identifier optionally
 * followed by assignment to a variable initializer.</p>
 *
 * <em>
 * VariableDeclarator:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier = VariableInitializer
 * </em>
 */
public class ASTVariableDeclarator extends ASTParentNode {
    private final ASTIdentifier myVarName;
    private final ASTVariableInitializer myVarInitializer;

    /**
     * Constructs an <code>ASTVariableDeclarator</code> at the given <code>Location</code>
     * with the given <code>ASTIdentifier</code> representing the variable name.
     * @param location The <code>Location</code>.
     * @param varName An <code>ASTIdentifier</code> representing the variable name.
     */
    public ASTVariableDeclarator(Location location, ASTIdentifier varName) {
        super(location);
        myVarName = varName;
        myVarInitializer = null;
    }

    /**
     * Constructs an <code>ASTVariableDeclarator</code> at the given <code>Location</code>
     * with the given <code>ASTIdentifier</code> representing the variable name
     * and the given <code>ASTNode</code> representing the variable initializer.
     * @param location The <code>Location</code>.
     * @param varName An <code>ASTIdentifier</code> representing the variable name.
     * @param varInitializer An <code>ASTVariableInitializer</code>.
     */
    public ASTVariableDeclarator(Location location, ASTIdentifier varName, ASTVariableInitializer varInitializer) {
        super(location);
        myVarName = varName;
        myVarInitializer = varInitializer;
    }

    /**
     * Returns an <code>ASTIdentifier</code> representing the variable name.
     * @return An <code>ASTIdentifier</code>.
     */
    public ASTIdentifier getVarName() {
        return myVarName;
    }

    /**
     * Returns an <code>ASTVariableInitializer</code>, if it exists.
     * @return An <code>Optional&lt;ASTVariableInitializer&gt;</code>.
     */
    public Optional<ASTVariableInitializer> getVarInitializer() {
        return Optional.ofNullable(myVarInitializer);
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(2);
        children.add(myVarName);
        if (myVarInitializer != null) {
            children.add(myVarInitializer);
        }
        return children;
    }
}
