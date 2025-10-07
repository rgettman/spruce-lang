package org.spruce.compiler.ast.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTAnnotatedNode;
import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.statements.ASTVariableDeclarator;
import org.spruce.compiler.ast.statements.ASTVariableDeclaratorList;
import org.spruce.compiler.ast.statements.ASTVariableModifierList;
import org.spruce.compiler.ast.types.ASTDataType;
import org.spruce.compiler.ast.types.ASTTypeParameterList;
import org.spruce.compiler.common.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * <p>An <code>ASTFieldDeclaration</code> is an optional AnnotationList followed
 * by AccessModifier followed by an optional FieldModifierList, an optional
 * VariableModifierList, a DataType, and a VariableDeclaratorList.</p>
 *
 * <em>
 * FieldDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] [AccessModifier] [FieldModifierList] [VariableModifierList] DataType VariableDeclaratorList
 * </em>
 */
public final class ASTFieldDeclaration extends ASTAnnotatedNode implements ASTClassPart {
    private final ASTKeywordNode myAccessMod;
    private final ASTFieldModifierList myFieldModList;
    private final ASTVariableModifierList myVarModList;
    private final ASTDataType myDataType;
    private final ASTVariableDeclaratorList myVarDeclList;

    /**
     * Constructs an <code>ASTFieldDeclaration</code> at the given <code>Location</code>
     * with the given <code>ASTAnnotationList</code>, the given
     * <code>ASTKeywordNode</code> representing an AccessModifier, the given
     * <code>ASTFieldModifierList</code>, the given <code>ASTVariableModifierList</code>,
     * the given <code>ASTDataType</code>, and the given <code>ASTVariableDeclaratorList</code>.
     * @param location The <code>Location</code>.
     * @param annList An <code>ASTAnnotationList</code>, possibly empty.
     * @param accessMod An <code>ASTKeywordNode</code> representing the AccessModifier.
     * @param fieldModList An <code>ASTFieldModifierList</code>.
     * @param varModList An <code>ASTVariableModifierList</code>.
     * @param dataType An <code>ASTDataType</code>.
     * @param varDeclList An <code>ASTVariableDeclaratorList</code>.
     */
    public ASTFieldDeclaration(Location location, ASTAnnotationList annList, ASTKeywordNode accessMod,
                               ASTFieldModifierList fieldModList, ASTVariableModifierList varModList,
                               ASTDataType dataType, ASTVariableDeclaratorList varDeclList) {
        super(location, annList);
        myAccessMod = accessMod;
        myFieldModList = fieldModList;
        myVarModList = varModList;
        myDataType = dataType;
        myVarDeclList = varDeclList;
    }

    /**
     * Constructs an <code>ASTFieldDeclaration</code> at the given <code>Location</code>
     * with the given <code>ASTAnnotationList</code>, the given
     * <code>ASTFieldModifierList</code>, the given <code>ASTVariableModifierList</code>,
     * the given <code>ASTDataType</code>, and the given <code>ASTVariableDeclaratorList</code>.
     * @param location The <code>Location</code>.
     * @param annList An <code>ASTAnnotationList</code>, possibly empty.
     * @param fieldModList An <code>ASTFieldModifierList</code>.
     * @param varModList An <code>ASTVariableModifierList</code>.
     * @param dataType An <code>ASTDataType</code>.
     * @param varDeclList An <code>ASTVariableDeclaratorList</code>.
     */
    public ASTFieldDeclaration(Location location, ASTAnnotationList annList, ASTFieldModifierList fieldModList,
                               ASTVariableModifierList varModList, ASTDataType dataType, ASTVariableDeclaratorList varDeclList) {
        super(location, annList);
        myAccessMod = null;
        myFieldModList = fieldModList;
        myVarModList = varModList;
        myDataType = dataType;
        myVarDeclList = varDeclList;
    }

    @Override
    public List<TokenType> getModifiers() {
        List<TokenType> modifiers = new ArrayList<>();
        if (myAccessMod != null) {
            modifiers.add(myAccessMod.getKeyword());
        }
        for (ASTKeywordNode modifier : getFieldModList().getTypedChildren()) {
            modifiers.add(modifier.getKeyword());
        }
        for (ASTKeywordNode modifier : getVarModList().getTypedChildren()) {
            modifiers.add(modifier.getKeyword());
        }
        return modifiers;
    }

    /**
     * Returns an <code>ASTKeywordNode</code> representing the AccessModifier, if it exists.
     * @return An <code>Optional&lt;ASTKeywordNode&gt;</code>.
     */
    public Optional<ASTKeywordNode> getAccessMod() {
        return Optional.ofNullable(myAccessMod);
    }

    /**
     * Returns an <code>ASTFieldModifierList</code>.
     * @return An <code>ASTFieldModifierList</code>.
     */
    public ASTFieldModifierList getFieldModList() {
        return myFieldModList;
    }

    /**
     * Returns an <code>ASTVariableModifierList</code>.
     * @return An <code>ASTVariableModifierList</code>.
     */
    public ASTVariableModifierList getVarModList() {
        return myVarModList;
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

    /**
     * Returns a <code>List</code> of all the names of declared field variables
     * in this declaration.
     * @return A <code>List</code> of <code>ASTIdentifier</code>s.
     */
    @Override
    public List<ASTIdentifier> getNames() {
        return myVarDeclList.getTypedChildren().stream()
                .map(ASTVariableDeclarator::getVarName)
                .toList();
    }

    /**
     * Returns no <code>ASTTypeParameterList</code>.
     * @return An empty <code>Optional</code>.
     */
    @Override
    public Optional<ASTTypeParameterList> getTypeParams() {
        return Optional.empty();
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(5);
        children.add(myAnnList);
        if (myAccessMod != null) {
            children.add(myAccessMod);
        }
        children.add(myFieldModList);
        children.add(myVarModList);
        children.add(myDataType);
        children.add(myVarDeclList);
        return children;
    }
}
