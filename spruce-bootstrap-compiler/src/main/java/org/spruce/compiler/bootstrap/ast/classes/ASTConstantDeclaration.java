package org.spruce.compiler.bootstrap.ast.classes;

import java.util.ArrayList;
import java.util.List;

import org.spruce.compiler.bootstrap.ast.ASTKeywordNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.ast.names.ASTIdentifier;
import org.spruce.compiler.bootstrap.ast.statements.ASTVariableDeclarator;
import org.spruce.compiler.bootstrap.ast.statements.ASTVariableDeclaratorList;
import org.spruce.compiler.bootstrap.ast.types.ASTDataType;
import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.scanner.TokenType;

/**
 * <p>An <code>ASTConstantDeclaration</code> is a ConstantModifier, a DataType, and
 * a VariableDeclaratorList.</p>
 *
 * <em>
 * ConstantDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ConstantModifier DataType VariableDeclaratorList
 * </em>
 */
public final class ASTConstantDeclaration extends ASTParentNode implements ASTInterfacePart {
    private final ASTKeywordNode myConstantMod;
    private final ASTDataType myDataType;
    private final ASTVariableDeclaratorList myVarDeclList;

    /**
     * Constructs an <code>ASTConstantDeclaration</code> at the given <code>Location</code>
     * with the given <code>ASTAnnotationList</code>, the given
     * <code>ASTKeywordNode</code> representing a ConstantModifier, the given
     * <code>ASTDataType</code>, and the given <code>ASTVariableDeclaratorList</code>.
     * @param location    The child nodes.
     * @param constantMod An <code>ASTKeywordNode</code> of keyword <code>constant</code>.
     * @param dataType    An <code>ASTDataType</code>.
     * @param varDeclList An <code>ASTVariableDeclaratorList</code>.
     */
    public ASTConstantDeclaration(Location location, ASTKeywordNode constantMod,
                                  ASTDataType dataType, ASTVariableDeclaratorList varDeclList) {
        super(location);
        myConstantMod = constantMod;
        myDataType = dataType;
        myVarDeclList = varDeclList;
    }

    /**
     * Returns an <code>ASTKeywordNode</code> representing a ConstantModifier.
     * @return An <code>ASTKeywordNode</code> of keyword <code>CONSTANT</code>.
     */
    public ASTKeywordNode getConstantMod() {
        return myConstantMod;
    }

    /**
     * Returns an <code>ASTDataType</code>.
     * @return An <code>ASTDataType</code>.
     */
    public ASTDataType getDataType() {
        return myDataType;
    }

    /**
     * Returns an <code>ASTVariableDeclaratorList</code>.
     * @return An <code>ASTVariableDeclaratorList</code>.
     */
    public ASTVariableDeclaratorList getVarDeclList() {
        return myVarDeclList;
    }

    @Override
    public List<TokenType> getModifiers() {
        return List.of();
    }

    /**
     * Returns a <code>List</code> of all the names of declared constants in
     * this declaration.
     * @return A <code>List</code> of <code>ASTIdentifier</code>s.
     */
    @Override
    public List<ASTIdentifier> getNames() {
        return myVarDeclList.getTypedChildren().stream()
                .map(ASTVariableDeclarator::getVarName)
                .toList();
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(3);
        children.add(myConstantMod);
        children.add(myDataType);
        children.add(myVarDeclList);
        return children;
    }
}

