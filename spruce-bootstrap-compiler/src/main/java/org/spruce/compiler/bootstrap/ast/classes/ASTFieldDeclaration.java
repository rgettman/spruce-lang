package org.spruce.compiler.bootstrap.ast.classes;

import java.util.ArrayList;
import java.util.List;

import org.spruce.compiler.bootstrap.ast.ASTKeywordNode;
import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.ast.names.ASTIdentifier;
import org.spruce.compiler.bootstrap.ast.statements.ASTVariableDeclarator;
import org.spruce.compiler.bootstrap.ast.statements.ASTVariableDeclaratorList;
import org.spruce.compiler.bootstrap.ast.types.ASTDataType;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.scanner.TokenType;

/**
 * <p>An <code>ASTFieldDeclaration</code> is an optional FieldModifierList,
 * a DataType, and a VariableDeclaratorList.</p>
 *
 * <em>
 * FieldDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[FieldModifierList] DataType VariableDeclaratorList
 * </em>
 */
public final class ASTFieldDeclaration extends ASTParentNode implements ASTClassPart {
    private final ASTFieldModifierList myFieldModList;
    private final ASTDataType myDataType;
    private final ASTVariableDeclaratorList myVarDeclList;

    /**
     * Constructs an <code>ASTFieldDeclaration</code> at the given <code>Location</code>
     * with the given <code>ASTFieldModifierList</code>, the given <code>ASTDataType</code>,
     * and the given <code>ASTVariableDeclaratorList</code>.
     * @param location The <code>Location</code>.
     * @param fieldModList An <code>ASTFieldModifierList</code>.
     * @param dataType An <code>ASTDataType</code>.
     * @param varDeclList An <code>ASTVariableDeclaratorList</code>.
     */
    public ASTFieldDeclaration(Location location, ASTFieldModifierList fieldModList,
                               ASTDataType dataType, ASTVariableDeclaratorList varDeclList) {
        super(location);
        myFieldModList = fieldModList;
        myDataType = dataType;
        myVarDeclList = varDeclList;
    }

    @Override
    public List<TokenType> getModifiers() {
        List<TokenType> modifiers = new ArrayList<>();
        for (ASTKeywordNode modifier : getFieldModList().getTypedChildren()) {
            modifiers.add(modifier.getKeyword());
        }
        return modifiers;
    }

    /**
     * Returns an <code>ASTFieldModifierList</code>.
     * @return An <code>ASTFieldModifierList</code>.
     */
    public ASTFieldModifierList getFieldModList() {
        return myFieldModList;
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

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(3);
        children.add(myFieldModList);
        children.add(myDataType);
        children.add(myVarDeclList);
        return children;
    }
}
