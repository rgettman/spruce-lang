package org.spruce.compiler.bootstrap.ast.statements;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.ast.names.ASTIdentifier;
import org.spruce.compiler.bootstrap.ast.types.ASTDataType;
import org.spruce.compiler.bootstrap.ast.types.ASTDataTypeNoArray;
import org.spruce.compiler.bootstrap.ast.types.ASTSimpleType;
import org.spruce.compiler.bootstrap.common.Location;

/**
 * <p>An <code>ASTLocalVariableDeclaration</code> is an optional variable
 * modifier list, a local variable type, and a variable declarator list.</p>
 *
 * <em>
 * LocalVariableDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;LocalVariableType VariableDeclaratorList
 * </em>
 */
public final class ASTLocalVariableDeclaration extends ASTParentNode implements ASTInit {
    private final ASTLocalVariableType myLocalVarType;
    private final ASTVariableDeclaratorList myVarDeclList;

    /**
     * Constructs an <code>ASTLocalVariableDeclaration</code> at the given <code>Location</code>,
     * with the given Variable Modifier List, Local Variable Type, and Variable Declarator List.
     * @param location The <code>Location</code>.
     * @param localVarType An <code>ASTLocalVariableType</code>.
     * @param varDeclList An <code>ASTVariableDeclaratorList</code>.
     */
    public ASTLocalVariableDeclaration(Location location, ASTLocalVariableType localVarType,
                                       ASTVariableDeclaratorList varDeclList) {
        super(location);
        myLocalVarType = localVarType;
        myVarDeclList = varDeclList;
    }

    /**
     * Create a bad LocalVariableDeclaration as a placeholder in case of an error.
     * @param loc The <code>Location</code>.
     * @return An <code>ASTLocalVariableDeclaration</code> at the given <code>Location</code>.
     */
    public static ASTLocalVariableDeclaration badLocalVariableDeclaration(Location loc) {
        return new ASTLocalVariableDeclaration(loc,
                new ASTLocalVariableType(loc,
                        new ASTDataType(loc,
                                new ASTDataTypeNoArray(loc, Arrays.asList(
                                        new ASTSimpleType(loc,
                                               new ASTIdentifier(loc, "Bad Local Variable Declaration")))))),
                new ASTVariableDeclaratorList(loc, Arrays.asList(
                        new ASTVariableDeclarator(loc,
                                new ASTIdentifier(loc,"Bad local variable")))));
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
        return Arrays.asList(myLocalVarType, myVarDeclList);
    }
}
