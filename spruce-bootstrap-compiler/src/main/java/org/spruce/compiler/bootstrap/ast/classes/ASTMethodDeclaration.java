package org.spruce.compiler.bootstrap.ast.classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.bootstrap.ast.ASTKeywordNode;
import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.symbol.SymbolDeclaration;
import org.spruce.compiler.bootstrap.ast.names.ASTIdentifier;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.scanner.TokenType;
import org.spruce.compiler.bootstrap.symbol.ParameterizedSymbol;

/**
 * <p>An <code>ASTMethodDeclaration</code> is an optional MethodModifierList,
 * then a MethodHeader and a MethodBody.</p>
 *
 * <em>
 * MethodDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[MethodModifierList] MethodHeader MethodBody
 * </em>
 */
public final class ASTMethodDeclaration extends ASTParentNode implements ASTClassPart,
        SymbolDeclaration<ParameterizedSymbol> {
    private final ASTMethodModifierList myMethodModList;
    private final ASTMethodHeader myHeader;
    private final ASTMethodBody myBody;
    private ParameterizedSymbol myDeclSymbol;

    /**
     * Constructs an <code>ASTMethodDeclaration</code> at the given <code>Location</code>
     * with the given <code>ASTMethodModifierList</code>,
     * the given <code>ASTMethodHeader</code>, and the given <code>ASTMethodBody</code>.
     * @param location The <code>Location</code>.
     * @param methodModList An <code>ASTMethodModifierList</code>.
     * @param header An <code>ASTMethodHeader</code>.
     * @param body An <code>ASTMethodBody</code>.
     */
    public ASTMethodDeclaration(Location location, ASTMethodModifierList methodModList,
                                ASTMethodHeader header, ASTMethodBody body) {
        super(location);
        myMethodModList = methodModList;
        myHeader = header;
        myBody = body;
    }

    /**
     * Returns an <code>ASTMethodModifierList</code>.
     * @return An <code>ASTMethodModifierList</code>.
     */
    public ASTMethodModifierList getMethodModList() {
        return myMethodModList;
    }

    /**
     * Returns an <code>ASTMethodHeader</code>.
     * @return An <code>ASTMethodHeader</code>.
     */
    public ASTMethodHeader getHeader() {
        return myHeader;
    }

    /**
     * Returns an <code>ASTMethodBody</code>.
     * @return An <code>ASTMethodBody</code>.
     */
    public ASTMethodBody getBody() {
        return myBody;
    }

    @Override
    public List<TokenType> getModifiers() {
        List<TokenType> modifiers = new ArrayList<>();
        for (ASTKeywordNode modifier : myMethodModList.getTypedChildren()) {
            modifiers.add(modifier.getKeyword());
        }
        return modifiers;
    }

    /**
     * Returns a <code>List</code> of exactly one <code>ASTIdentifier</code>
     * representing the method name.
     * @return A <code>List</code> of <code>ASTIdentifier</code> of size 1.
     */
    @Override
    public List<ASTIdentifier> getNames() {
        return Arrays.asList(myHeader.getMethodDecl().getName());
    }

    @Override
    public void setDeclSymbol(ParameterizedSymbol symbol) {
        myDeclSymbol = symbol;
    }

    @Override
    public ParameterizedSymbol getDeclSymbol(){
        return myDeclSymbol;
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(5);
        children.add(myMethodModList);
        children.add(myHeader);
        children.add(myBody);
        return children;
    }
}
