package org.spruce.compiler.ast.toplevel;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTAnnotatedNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.classes.ASTAnnotationList;
import org.spruce.compiler.ast.names.ASTNamespaceName;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTNamespaceDeclaration</code> is an optional AnnotationList
 * followed by "namespace" followed by a Namespace Name.</p>
 *
 * <em>
 * NamespaceDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] namespace NamespaceName
 * </em>
 */
public class ASTNamespaceDeclaration extends ASTAnnotatedNode {
    private final ASTNamespaceName myNamespace;

    /**
     * Constructs an <code>ASTNamespaceDeclaration</code> at the given <code>Location</code>
     * with the given <code>ASTNamespaceName</code>.
     * @param location The <code>Location</code>.
     * @param annList A possibly empty <code>ASTAnnotationList</code>.
     * @param namespace An <code>ASTNamespaceName</code>.
     */
    public ASTNamespaceDeclaration(Location location, ASTAnnotationList annList, ASTNamespaceName namespace) {
        super(location, annList);
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
        return Arrays.asList(myAnnList, myNamespace);
    }
}

