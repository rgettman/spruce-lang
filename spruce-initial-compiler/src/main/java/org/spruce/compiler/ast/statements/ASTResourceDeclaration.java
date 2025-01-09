package org.spruce.compiler.ast.statements;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.expressions.ASTExpression;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTResourceDeclaration</code> is an optional variable modifier
 * list, a local variable type, an identifier, an assignment, and an
 * expression.</p>
 *
 * <em>
 * ResourceDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;VariableModifierList LocalVariableType Identifier = Expression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;LocalVariableType Identifier = Expression
 * </em>
 */
public final class ASTResourceDeclaration extends ASTParentNode implements ASTResource {
    private final ASTVariableModifierList myVarModifierList;
    private final ASTLocalVariableType myLocalVarType;
    private final ASTIdentifier myResourceName;
    private final ASTExpression myExpression;

    /**
     * Constructs an <code>ASTResourceDeclaration</code> at the given <code>Location</code>
     * with the given <code>ASTVariableModifierList</code>, the given
     * <code>ASTIdentifier</code> representing the resource name, and the given
     * <code>ASTExpression</code>.
     * @param location The <code>Location</code>.
     * @param varModifierList An <code>ASTVariableModifierList</code>.
     * @param localVarType An <code>ASTLocalVariableType</code>.
     * @param resourceName An <code>ASTIdentifier</code> representing the resource name.
     * @param expression An <code>ASTExpression</code>.
     */
    public ASTResourceDeclaration(Location location, ASTVariableModifierList varModifierList, ASTLocalVariableType localVarType,
                                  ASTIdentifier resourceName, ASTExpression expression) {
        super(location);
        myVarModifierList = varModifierList;
        myLocalVarType = localVarType;
        myResourceName = resourceName;
        myExpression = expression;
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
     * Returns an <code>ASTIdentifier</code> representing the resource name.
     * @return An <code>ASTIdentifier</code>.
     */
    public ASTIdentifier getResourceName() {
        return myResourceName;
    }

    /**
     * Returns an <code>ASTExpression</code>.
     * @return An <code>ASTExpression</code>.
     */
    public ASTExpression getExpression() {
        return myExpression;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myVarModifierList, myLocalVarType, myResourceName, myExpression);
    }
}
