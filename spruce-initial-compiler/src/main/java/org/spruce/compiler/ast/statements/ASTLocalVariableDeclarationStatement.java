package org.spruce.compiler.ast.statements;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTLocalVariableDeclarationStatement</code> is a local variable
 * declaration followed by a semicolon.</p>
 *
 * <em>
 * LocalVariableDeclarationStatement:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;LocalVariableDeclaration ;
 * </em>
 */
public final class ASTLocalVariableDeclarationStatement extends ASTParentNode implements ASTBlockStatement {
    private final ASTLocalVariableDeclaration myLocalVarDecl;

    /**
     * Constructs an <code>ASTLocalVariableDeclarationStatement</code> at the given <code>Location</code>
     * with the given <code>ASTLocalVariableDeclaration</code>.
     * @param location The <code>Location</code>.
     * @param localVarDecl An <code>ASTLocalVariableDeclaration</code>.
     */
    public ASTLocalVariableDeclarationStatement(Location location, ASTLocalVariableDeclaration localVarDecl) {
        super(location);
        myLocalVarDecl = localVarDecl;
    }

    /**
     * Returns an <code>ASTLocalVariableDeclaration</code>.
     * @return An <code>ASTLocalVariableDeclaration</code>.
     */
    public ASTLocalVariableDeclaration getLocalVarDecl() {
        return myLocalVarDecl;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myLocalVarDecl);
    }
}
