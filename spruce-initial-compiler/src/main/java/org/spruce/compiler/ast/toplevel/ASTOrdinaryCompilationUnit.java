package org.spruce.compiler.ast.toplevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTOrdinaryCompilationUnit</code> is an optional namespace declaration
 * followed by a (possibly empty) use declaration list and a (possibly
 * empty) type declaration list.</p>
 *
 * <em>
 * OrdinaryCompilationUnit:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;NamespaceDeclaration UseDeclarationList TypeDeclarationList<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;UseDeclarationList TypeDeclarationList
 * </em>
 */
public class ASTOrdinaryCompilationUnit extends ASTParentNode {
    private final ASTNamespaceDeclaration myNamespaceDecl;
    private final ASTUseDeclarationList myUseDeclList;
    private final ASTTypeDeclarationList myTypeDeclList;

    /**
     * Constructs an <code>ASTOrdinaryCompilationUnit</code> at the given <code>Location</code>
     * with the given <code>ASTNamespaceDeclaration</code>, the given
     * <code>ASTUseDeclarationList</code>, and the given <code>ASTTypeDeclarationList</code>.
     * @param location The <code>Location</code>.
     * @param namespaceDecl An <code>ASTNamespaceDeclaration</code>.
     * @param useDeclList An <code>ASTUseDeclarationList</code>.
     * @param typeDeclList An <code>ASTTypeDeclarationList</code>.
     */
    public ASTOrdinaryCompilationUnit(Location location, ASTNamespaceDeclaration namespaceDecl,
                                      ASTUseDeclarationList useDeclList, ASTTypeDeclarationList typeDeclList) {
        super(location);
        myNamespaceDecl = namespaceDecl;
        myUseDeclList = useDeclList;
        myTypeDeclList = typeDeclList;
    }

    /**
     * Constructs an <code>ASTOrdinaryCompilationUnit</code> at the given <code>Location</code>
     * with the given <code>ASTNamespaceDeclaration</code>, the given
     * <code>ASTUseDeclarationList</code>, and the given <code>ASTTypeDeclarationList</code>.
     * @param location The <code>Location</code>.
     * @param useDeclList An <code>ASTListNode</code> of type <code>USE_DECLARATIONS</code>.
     * @param typeDeclList An <code>ASTTypeDeclarationList</code>.
     */
    public ASTOrdinaryCompilationUnit(Location location, ASTUseDeclarationList useDeclList, ASTTypeDeclarationList typeDeclList) {
        super(location);
        myNamespaceDecl = null;
        myUseDeclList = useDeclList;
        myTypeDeclList = typeDeclList;
    }

    /**
     * Returns an <code>ASTNamespaceDeclaration</code>, if it exists.
     * @return An <code>Optional&lt;ASTNamespaceDeclaration&gt;</code>.
     */
    public Optional<ASTNamespaceDeclaration> getNamespaceDecl() {
        return Optional.ofNullable(myNamespaceDecl);
    }

    /**
     * Returns an <code>ASTUseDeclarationList</code>.
     * @return An <code>ASTUseDeclarationList</code>.
     */
    public ASTUseDeclarationList getUseDeclList() {
        return myUseDeclList;
    }

    /**
     * Returns an <code>ASTTypeDeclarationList</code>.
     * @return An <code>ASTTypeDeclarationList</code>.
     */
    public ASTTypeDeclarationList getTypeDeclList() {
        return myTypeDeclList;
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(3);
        if (myNamespaceDecl != null) {
            children.add(myNamespaceDecl);
        }
        children.add(myUseDeclList);
        children.add(myTypeDeclList);
        return children;
    }
}

