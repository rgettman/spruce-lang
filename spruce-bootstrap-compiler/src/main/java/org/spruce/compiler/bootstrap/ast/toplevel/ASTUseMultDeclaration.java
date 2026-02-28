package org.spruce.compiler.bootstrap.ast.toplevel;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.ast.names.ASTIdentifierList;
import org.spruce.compiler.bootstrap.ast.names.ASTNamespaceOrTypeName;
import org.spruce.compiler.bootstrap.common.Location;

/**
 * <p>An <code>ASTUseMultDeclaration</code> is "use" followed
 * by a Namespace Name, a dot, then an identifier list within braces, and a semicolon.</p>
 *
 * <em>
 * UseMultDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;use NamespaceOrTypeName . { IdentifierList } ;
 * </em>
 */
public final class ASTUseMultDeclaration extends ASTParentNode implements ASTUseDeclaration {
    private final ASTNamespaceOrTypeName myNamespaceOrTypeName;
    private final ASTIdentifierList myIdentifierList;

    /**
     * Constructs an <code>ASTUseMultDeclaration</code> at the given <code>Location</code>
     * with the given <code>ASTNamespaceOrTypeName</code> and the given
     * <code>ASTIdentifierList</code>.
     * @param location The <code>Location</code>.
     * @param namespaceOrTypeName An <code>ASTNamespaceOrTypeName</code>.
     * @param identifierList An <code>ASTIdentifierList</code>.
     */
    public ASTUseMultDeclaration(Location location, ASTNamespaceOrTypeName namespaceOrTypeName, ASTIdentifierList identifierList) {
        super(location);
        myNamespaceOrTypeName = namespaceOrTypeName;
        myIdentifierList = identifierList;
    }

    /**
     * Returns an <code>ASTNamespaceOrTypeName</code>.
     * @return An <code>ASTNamespaceOrTypeName</code>.
     */
    public ASTNamespaceOrTypeName getNamespaceOrTypeName() {
        return myNamespaceOrTypeName;
    }

    /**
     * Returns an <code>ASTIdentifierList</code>.
     * @return An <code>ASTIdentifierList</code>.
     */
    public ASTIdentifierList getIdentifierList() {
        return myIdentifierList;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myNamespaceOrTypeName, myIdentifierList);
    }
}

