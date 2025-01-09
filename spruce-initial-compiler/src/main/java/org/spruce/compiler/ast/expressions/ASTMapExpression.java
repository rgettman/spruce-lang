package org.spruce.compiler.ast.expressions;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTMapExpression</code> is an argument list surrounded by braces.</p>
 *
 * <em>
 * MapExpression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;{ ArgumentList }
 * </em>
 */
public final class ASTMapExpression extends ASTParentNode {
    private final ASTArgumentList myArgList;

    /**
     * Constructs an <code>ASTMapExpression</code> at the given <code>Location</code>
     * with an <code>ASTArgumentList</code> as its child.
     * @param location The <code>Location</code>.
     * @param argList An <code>ASTArgumentList</code>.
     */
    public ASTMapExpression(Location location, ASTArgumentList argList) {
        super(location);
        myArgList = argList;
    }

    /**
     * Returns an <code>ASTArgumentList</code>.
     * @return An <code>ASTArgumentList</code>.
     */
    public ASTArgumentList getArgList() {
        return myArgList;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myArgList);
    }
}
