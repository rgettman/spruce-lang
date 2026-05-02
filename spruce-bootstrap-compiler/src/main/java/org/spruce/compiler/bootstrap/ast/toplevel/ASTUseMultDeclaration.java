package org.spruce.compiler.bootstrap.ast.toplevel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.MultSymbolReference;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.ast.names.ASTIdentifierList;
import org.spruce.compiler.bootstrap.ast.names.ASTNamespaceOrTypeName;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.symbol.ParentSymbol;

/**
 * <p>An <code>ASTUseMultDeclaration</code> is "use" followed
 * by a Namespace Name, a dot, then an identifier list within braces, and a semicolon.</p>
 *
 * <em>
 * UseMultDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;use NamespaceOrTypeName . { IdentifierList } ;
 * </em>
 */
public final class ASTUseMultDeclaration extends ASTParentNode
        implements ASTUseDeclaration, MultSymbolReference<ParentSymbol> {
    private final ASTNamespaceOrTypeName myNamespaceOrTypeName;
    private final ASTIdentifierList myIdentifierList;
    private final Map<String, ParentSymbol> myResolvedSymbols;

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
        myResolvedSymbols = new HashMap<>();
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
    public void addResolvedSymbol(ParentSymbol symbol) {
        myResolvedSymbols.put(symbol.getName(), symbol);
    }

    @Override
    public Optional<ParentSymbol> getResolvedSymbol(String name) {
        return Optional.ofNullable(myResolvedSymbols.get(name));
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myNamespaceOrTypeName, myIdentifierList);
    }
}

