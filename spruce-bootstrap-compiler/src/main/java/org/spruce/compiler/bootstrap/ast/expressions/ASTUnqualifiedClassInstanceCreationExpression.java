package org.spruce.compiler.bootstrap.ast.expressions;

import java.util.ArrayList;
import java.util.List;

import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.common.Location;

/**
 * <p>An <code>ASTUnqualifiedClassInstanceCreationExpression</code> is "new"
 * followed by a type to instantiate, "(", an argument list, and ")".</p>
 * <em>
 * UnqualifiedClassInstanceCreationExpression:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;new [TypeArguments] TypeToInstantiate ( [ArgumentList] )<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;<strong>The following will also be a production:</strong><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;new [TypeArguments] TypeToInstantiate ( [ArgumentList] ) ClassBody
 * </em>
 */
public class ASTUnqualifiedClassInstanceCreationExpression extends ASTParentNode {
    private final ASTTypeToInstantiate myTti;
    private final ASTArgumentList myArgumentList;

    /**
     * Constructs an <code>ASTUnqualifiedClassInstanceCreationExpression</code>
     * at the given <code>Location</code> with the given TypeToInstantiate and
     * the given ArgumentList.
     * @param location The <code>Location</code>.
     * @param tti An <code>ASTTypeToInstantiate</code>.
     * @param argumentList An <code>ASTArgumentList</code>.
     */
    public ASTUnqualifiedClassInstanceCreationExpression(Location location, ASTTypeToInstantiate tti, ASTArgumentList argumentList) {
        super(location);
        myTti = tti;
        myArgumentList = argumentList;
    }

    /**
     * Returns an <code>ASTTypeToInstantiate</code>.
     * @return An <code>ASTTypeToInstantiate</code>.
     */
    public ASTTypeToInstantiate getTti() {
        return myTti;
    }

    /**
     * Returns an <code>ASTArgumentList</code>.
     * @return An <code>ASTArgumentList</code>.
     */
    public ASTArgumentList getArgumentList() {
        return myArgumentList;
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(3);
        children.add(myTti);
        children.add(myArgumentList);
        return children;
    }
}
