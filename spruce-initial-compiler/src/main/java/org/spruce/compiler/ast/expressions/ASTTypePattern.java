package org.spruce.compiler.ast.expressions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.statements.ASTVariableModifierList;
import org.spruce.compiler.ast.types.ASTDataType;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTTypePattern</code> is an optional variable modifier list,
 * a data type, and an identifier.</p>
 *
 * <em>
 * TypePattern:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;VariableModifierList DataType Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataType Identifier<br>
 * </em>
 */
public final class ASTTypePattern extends ASTParentNode implements ASTPattern {
    private final ASTVariableModifierList myVarModList;
    private final ASTDataType myDataType;
    private final ASTIdentifier myIdentifier;

    /**
     * Constructs an <code>ASTTypePattern</code> with the given Location,
     * VariableModifierList, DataType, and Identifier.
     * @param location A <code>Location</code>.
     * @param varModList An <code>ASTVariableModifierList</code>.
     * @param dataType An <code>ASTDataType</code>.
     * @param identifier An <code>ASTIdentifier</code>.
     */
    public ASTTypePattern(Location location, ASTVariableModifierList varModList, ASTDataType dataType, ASTIdentifier identifier) {
        super(location);
        myVarModList = varModList;
        myDataType = dataType;
        myIdentifier = identifier;
    }

    /**
     * Constructs an <code>ASTTypePattern</code> with the given Location,
     * DataType, and Identifier, but no VariableModifierList.
     * @param location A <code>Location</code>.
     * @param dataType An <code>ASTDataType</code>.
     * @param identifier An <code>ASTIdentifier</code>.
     */
    public ASTTypePattern(Location location, ASTDataType dataType, ASTIdentifier identifier) {
        super(location);
        myVarModList = null;
        myDataType = dataType;
        myIdentifier = identifier;
    }

    /**
     * Returns the <code>ASTVariableModifierList</code>, if it exists.
     * @return An <code>Optional&lt;ASTVariableModifierList&gt;</code>.
     */
    public Optional<ASTVariableModifierList> getVarModList() {
        return Optional.ofNullable(myVarModList);
    }

    /**
     * Returns the <code>DataType</code>.
     * @return An <code>ASTDataType</code>.
     */
    public ASTDataType getDataType() {
        return myDataType;
    }

    /**
     * Returns the identifier.
     * @return An <code>ASTIdentifier</code>.
     */
    public ASTIdentifier getIdentifier() {
        return myIdentifier;
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(3);
        if (myVarModList != null) {
            children.add(myVarModList);
        }
        children.add(myDataType);
        children.add(myIdentifier);
        return children;
    }
}
