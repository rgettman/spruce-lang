package org.spruce.compiler.ast.statements;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTCatchFormalParameter</code> is an optional variable
 * modifier list, a catch type, and an identifier.</p>
 *
 * <em>
 * CatchFormalParameter:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;VariableModifierList CatchType Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;CatchType Identifier
 * </em>
 */
public class ASTCatchFormalParameter extends ASTParentNode {
    private final ASTVariableModifierList myVarModifierList;
    private final ASTCatchType myCatchType;
    private final ASTIdentifier myVarName;

    /**
     * Constructs an <code>ASTCatchFormalParameter</code> at the given <code>Location</code>
     * with the given <code>ASTVariableModifierList</code>, an <code>ASTCatchType</code>,
     * and an <code>ASTIdentifier</code> representing the variable name.
     * @param location The <code>Location</code>.
     * @param varModifierList An <code>ASTVariableModifierList</code>.
     * @param catchType An <code>ASTCatchType</code>.
     * @param varName An <code>ASTIdentifier</code> representing the variable name.
     */
    public ASTCatchFormalParameter(Location location, ASTVariableModifierList varModifierList, ASTCatchType catchType, ASTIdentifier varName) {
        super(location);
        myVarModifierList = varModifierList;
        myCatchType = catchType;
        myVarName = varName;
    }

    /**
     * Returns an <code>ASTVariableModifierList</code>.
     * @return An <code>ASTVariableModifierList</code>.
     */
    public ASTVariableModifierList getVarModifierList() {
        return myVarModifierList;
    }

    /**
     * Returns an <code>ASTCatchType</code>.
     * @return An <code>ASTCatchType</code>.
     */
    public ASTCatchType getCatchType() {
        return myCatchType;
    }

    /**
     * Returns an <code>ASTIdentifier</code> representing the variable name.
     * @return An <code>ASTIdentifier</code>.
     */
    public ASTIdentifier getVarName() {
        return myVarName;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myVarModifierList, myCatchType, myVarName);
    }
}
