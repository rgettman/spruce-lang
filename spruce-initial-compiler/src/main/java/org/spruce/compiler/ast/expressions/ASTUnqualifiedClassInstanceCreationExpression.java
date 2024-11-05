package org.spruce.compiler.ast.expressions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.types.ASTTypeArgumentList;
import org.spruce.compiler.scanner.Location;

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
    private final ASTTypeArgumentList myTypeArgs;
    private final ASTTypeToInstantiate myTti;
    private final ASTArgumentList myArgumentList;

    /**
     * Constructs an <code>ASTUnqualifiedClassInstanceCreationExpression</code>
     * at the given <code>Location</code> with the given TypeToInstantiate and
     * the given ArgumentList.
     * @param location The <code>Location</code>.
     * @param typeArgs An <code>ASTTypeArgumentList</code>.
     * @param tti An <code>ASTTypeToInstantiate</code>.
     * @param argumentList An <code>ASTArgumentList</code>.
     */
    public ASTUnqualifiedClassInstanceCreationExpression(Location location, ASTTypeArgumentList typeArgs,
                                                         ASTTypeToInstantiate tti, ASTArgumentList argumentList) {
        super(location);
        myTypeArgs = typeArgs;
        myTti = tti;
        myArgumentList = argumentList;
    }

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
        myTypeArgs = null;
        myTti = tti;
        myArgumentList = argumentList;
    }

    /**
     * Returns an <code>ASTTypeArgumentList</code>, if it exists.
     * @return An <code>Optional&lt;ASTTypeArgumentList&gt;</code>.
     */
    public Optional<ASTTypeArgumentList> getTypeArgs() {
        return Optional.ofNullable(myTypeArgs);
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
        if (myTypeArgs != null) {
            children.add(myTypeArgs);
        }
        children.add(myTti);
        children.add(myArgumentList);
        return children;
    }
}
