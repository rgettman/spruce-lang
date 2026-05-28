package org.spruce.compiler.bootstrap.ast.classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.bootstrap.ast.ASTKeywordNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.ast.names.ASTIdentifier;
import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.scanner.TokenType;
import org.spruce.compiler.bootstrap.symbol.ParameterizedSymbol;
import org.spruce.compiler.bootstrap.symbol.SymbolDeclaration;

/**
 * <p>An <code>ASTInterfaceMethodDeclaration</code> is an optional InterfaceMethodModifierList,
 * then a MethodHeader and a MethodBody.</p>
 *
 * <em>
 * InterfaceMethodDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[InterfaceMethodModifierList] MethodHeader MethodBody
 * </em>
 */
public final class ASTInterfaceMethodDeclaration extends ASTParentNode implements ASTInterfacePart,
        SymbolDeclaration<ParameterizedSymbol> {
    private final ASTInterfaceMethodModifierList myModifierList;
    private final ASTMethodHeader myHeader;
    private final ASTMethodBody myBody;
    private ParameterizedSymbol myDeclSymbol;

    /**
     * Constructs an <code>ASTInterfaceMethodDeclaration</code> at the given <code>Location</code>
     * with the given <code>ASTInterfaceMethodModifierList</code>,
     * the given <code>ASTMethodHeader</code>, and the given <code>ASTMethodBody</code>.
     * @param location The <code>Location</code>.
     * @param modifierList An <code>ASTInterfaceMethodModifierList</code>.
     * @param header An <code>ASTMethodHeader</code>.
     * @param body An <code>ASTMethodBody</code>.
     */
    public ASTInterfaceMethodDeclaration(Location location,
                                         ASTInterfaceMethodModifierList modifierList,
                                         ASTMethodHeader header, ASTMethodBody body) {
        super(location);
        myModifierList = modifierList;
        myHeader = header;
        myBody = body;
    }

    /**
     * Returns an <code>ASTInterfaceMethodModifierList</code>.
     * @return An <code>ASTInterfaceMethodModifierList</code>.
     */
    public ASTInterfaceMethodModifierList getModifierList() {
        return myModifierList;
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
        for (ASTKeywordNode modifier : myModifierList.getTypedChildren()) {
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
        List<Node> children = new ArrayList<>(3);
        children.add(myModifierList);
        children.add(myHeader);
        children.add(myBody);
        return children;
    }
}
