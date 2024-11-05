package org.spruce.compiler.ast.toplevel;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTNamespaceOrTypeName;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTUseAllDeclaration</code> is "use" followed
 * by a Namespace Or Type Name, dot, star, then a semicolon.</p>
 *
 * <em>
 * UseAllDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;use NamespaceOrTypeName . * ;
 * </em>
 */
public final class ASTUseAllDeclaration extends ASTParentNode implements ASTUseDeclaration {
    private final ASTNamespaceOrTypeName myNamespaceOrTypeName;

    /**
     * Constructs an <code>ASTUseAllDeclaration</code> at the given <code>Location</code>
     * with the given <code>ASTNamespaceOrTypeName</code>.
     * @param location The <code>Location</code>.
     * @param namespaceOrTypeName An <code>ASTNamespaceOrTypeName</code>.
     */
    public ASTUseAllDeclaration(Location location, ASTNamespaceOrTypeName namespaceOrTypeName) {
        super(location);
        myNamespaceOrTypeName = namespaceOrTypeName;
    }

    /**
     * Returns an <code>ASTNamespaceOrTypeName</code>.
     * @return An <code>ASTNamespaceOrTypeName</code>.
     */
    public ASTNamespaceOrTypeName getNamespaceOrTypeName() {
        return myNamespaceOrTypeName;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myNamespaceOrTypeName);
    }
}

