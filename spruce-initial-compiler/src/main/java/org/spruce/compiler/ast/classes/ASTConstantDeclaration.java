package org.spruce.compiler.ast.classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTAnnotatedNode;
import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.statements.ASTVariableDeclarator;
import org.spruce.compiler.ast.statements.ASTVariableDeclaratorList;
import org.spruce.compiler.ast.types.ASTDataType;
import org.spruce.compiler.common.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * <p>An <code>ASTConstantDeclaration</code> is an optional AnnotationList
 * followed by an optional AccessModifier, a ConstantModifier, a DataType, and
 * a VariableDeclaratorList.</p>
 *
 * <em>
 * ConstantDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] [AccessModifier] ConstantModifier DataType VariableDeclaratorList
 * </em>
 */
public final class ASTConstantDeclaration extends ASTAnnotatedNode implements ASTAnnotationPart, ASTInterfacePart {
    private final ASTKeywordNode myAccessMod;
    private final ASTKeywordNode myConstantMod;
    private final ASTDataType myDataType;
    private final ASTVariableDeclaratorList myVarDeclList;

    /**
     * Constructs an <code>ASTConstantDeclaration</code> at the given <code>Location</code>
     * with the given <code>ASTAnnotationList</code>, the given
     * <code>ASTKeywordNode</code> representing a ConstantModifier, the given
     * <code>ASTDataType</code>, and the given <code>ASTVariableDeclaratorList</code>.
     * @param location    The child nodes.
     * @param annList     An <code>ASTAnnotationList</code>, possibly empty.
     * @param constantMod An <code>ASTKeywordNode</code> of keyword <code>constant</code>.
     * @param dataType    An <code>ASTDataType</code>.
     * @param varDeclList An <code>ASTVariableDeclaratorList</code>.
     */
    public ASTConstantDeclaration(Location location, ASTAnnotationList annList, ASTKeywordNode constantMod,
                                  ASTDataType dataType, ASTVariableDeclaratorList varDeclList) {
        super(location, annList);
        myAccessMod = null;
        myConstantMod = constantMod;
        myDataType = dataType;
        myVarDeclList = varDeclList;
    }

    /**
     * Constructs an <code>ASTConstantDeclaration</code> at the given <code>Location</code>
     * with the given <code>ASTAnnotationList</code>, the given
     * <code>ASTKeywordNode</code> representing an AccessModifier, the given
     * <code>ASTKeywordNode</code> representing a ConstantModifier, the given
     * <code>ASTDataType</code>, and the given <code>ASTVariableDeclaratorList</code>.
     * @param location    The child nodes.
     * @param annList     An <code>ASTAnnotationList</code>, possibly empty.
     * @param accessMod   An <code>ASTKeywordNode</code> representing an AccessModifier.
     * @param constantMod An <code>ASTKeywordNode</code> of keyword <code>constant</code>.
     * @param dataType    An <code>ASTDataType</code>.
     * @param varDeclList An <code>ASTVariableDeclaratorList</code>.
     */
    public ASTConstantDeclaration(Location location, ASTAnnotationList annList, ASTKeywordNode accessMod,
                                  ASTKeywordNode constantMod, ASTDataType dataType, ASTVariableDeclaratorList varDeclList) {
        super(location, annList);
        myAccessMod = accessMod;
        myConstantMod = constantMod;
        myDataType = dataType;
        myVarDeclList = varDeclList;
    }

    /**
     * Returns an <code>ASTKeywordNode</code> representing an AccessModifier, if it exists.
     * @return An <code>Optional&lt;ASTKeywordNode&gt;</code> representing an AccessModifier.
     */
    @Override
    public Optional<ASTKeywordNode> getAccessMod() {
        return Optional.ofNullable(myAccessMod);
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
        List<Node> children = new ArrayList<>(5);
        children.add(myAnnList);
        if (myAccessMod != null) {
            children.add(myAccessMod);
        }
        children.add(myConstantMod);
        children.add(myDataType);
        children.add(myVarDeclList);
        return children;
    }
}

