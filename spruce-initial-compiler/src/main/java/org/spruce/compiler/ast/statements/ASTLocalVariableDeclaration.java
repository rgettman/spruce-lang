package org.spruce.compiler.ast.statements;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTLocalVariableDeclaration</code> is an optional variable
 * modifier list, a local variable type, and a variable declarator list.</p>
 *
 * <em>
 * LocalVariableDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;VariableModifierList LocalVariableType VariableDeclaratorList<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;LocalVariableType VariableDeclaratorList
 * </em>
 */
public final class ASTLocalVariableDeclaration extends ASTParentNode implements ASTInit {
    private final ASTVariableModifierList myVarModifierList;
    private final ASTLocalVariableType myLocalVarType;
    private final ASTVariableDeclaratorList myVarDeclList;

    /**
     * Constructs an <code>ASTLocalVariableDeclaration</code> at the given <code>Location</code>,
     * with the given Variable Modifier List, Local Variable Type, and Variable Declarator List.
     * @param location The <code>Location</code>.
     * @param varModifierList An <code>ASTVariableModifierList</code>.
     * @param localVarType An <code>ASTLocalVariableType</code>.
     * @param varDeclList An <code>ASTVariableDeclaratorList</code>.
     */
    public ASTLocalVariableDeclaration(Location location, ASTVariableModifierList varModifierList,
                                       ASTLocalVariableType localVarType, ASTVariableDeclaratorList varDeclList) {
        super(location);
        myVarModifierList = varModifierList;
        myLocalVarType = localVarType;
        myVarDeclList = varDeclList;
    }

    /**
     * Returns an <code>ASTVariableModifierList</code>.
     * @return An <code>ASTVariableModifierList</code>.
     */
    public ASTVariableModifierList getVarModifierList() {
        return myVarModifierList;
    }

    /**
     * Returns an <code>ASTLocalVariableType</code>.
     * @return An <code>ASTLocalVariableType</code>.
     */
    public ASTLocalVariableType getLocalVarType() {
        return myLocalVarType;
    }

    /**
     * Returns an <code>ASTVariableDeclaratorList</code>.
     * @return An <code>ASTVariableDeclaratorList</code>.
     */
    public ASTVariableDeclaratorList getVarDeclList() {
        return myVarDeclList;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myVarModifierList, myLocalVarType, myVarDeclList);
    }
}
