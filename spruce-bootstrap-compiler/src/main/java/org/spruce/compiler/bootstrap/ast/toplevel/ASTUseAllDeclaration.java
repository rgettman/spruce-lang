package org.spruce.compiler.bootstrap.ast.toplevel;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.ast.SymbolReference;
import org.spruce.compiler.bootstrap.ast.names.ASTNamespaceOrTypeName;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.symbol.ParentSymbol;

/**
 * <p>An <code>ASTUseAllDeclaration</code> is "use" followed
 * by a Namespace Or Type Name, dot, star, then a semicolon.</p>
 *
 * <em>
 * UseAllDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;use NamespaceOrTypeName . * ;
 * </em>
 */
public final class ASTUseAllDeclaration extends ASTParentNode
        implements ASTUseDeclaration, SymbolReference<ParentSymbol> {
    private final ASTNamespaceOrTypeName myNamespaceOrTypeName;
    private ParentSymbol myResolvedSymbol;

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
    public void setResolvedSymbol(ParentSymbol symbol) {
        myResolvedSymbol = symbol;
    }

    @Override
    public ParentSymbol getResolvedSymbol() {
        return myResolvedSymbol;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myNamespaceOrTypeName);
    }
}

