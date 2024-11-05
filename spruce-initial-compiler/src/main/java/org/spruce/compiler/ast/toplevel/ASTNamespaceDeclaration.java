package org.spruce.compiler.ast.toplevel;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTNamespaceName;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTNamespaceDeclaration</code> is a "namespace" followed by a
 * Namespace Name.</p>
 *
 * <em>
 * NamespaceDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;namespace NamespaceName
 * </em>
 */
public class ASTNamespaceDeclaration extends ASTParentNode {
    private final ASTNamespaceName myNamespace;

    /**
     * Constructs an <code>ASTNamespaceDeclaration</code> at the given <code>Location</code>
     * with the given <code>ASTNamespaceName</code>.
     * @param location The <code>Location</code>.
     * @param namespace An <code>ASTNamespaceName</code>.
     */
    public ASTNamespaceDeclaration(Location location, ASTNamespaceName namespace) {
        super(location);
        myNamespace = namespace;
    }

    /**
     * Returns an <code>ASTNamespaceName</code>.
     * @return An <code>ASTNamespaceName</code>.
     */
    public ASTNamespaceName getNamespace() {
        return myNamespace;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myNamespace);
    }
}

