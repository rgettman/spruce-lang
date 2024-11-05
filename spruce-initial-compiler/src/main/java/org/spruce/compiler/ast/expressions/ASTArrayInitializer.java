package org.spruce.compiler.ast.expressions;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.scanner.Location;

public final class ASTArrayInitializer extends ASTParentNode implements ASTVariableInitializer {
    private final ASTVariableInitializerList myVarInitializers;

    /**
     * Constructs an <code>ASTArrayInitializer</code> at the given <code>Location</code>
     * with the given <code>ASTListNode</code> representing a VariableInitializerList.
     * @param location The <code>Location</code>.
     * @param varInitializers An <code>ASTVariableInitializerList</code>.
     */
    public ASTArrayInitializer(Location location, ASTVariableInitializerList varInitializers) {
        super(location);
        myVarInitializers = varInitializers;
    }

    /**
     * Returns an <code>ASTVariableInitializerList</code>.
     * @return An <code>ASTVariableInitializerList</code>.
     */
    public ASTVariableInitializerList getVarInitializers() {
        return myVarInitializers;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myVarInitializers);
    }
}
