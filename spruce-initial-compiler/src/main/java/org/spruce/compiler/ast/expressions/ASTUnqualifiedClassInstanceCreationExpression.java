package org.spruce.compiler.ast.expressions;

import java.util.Arrays;
import java.util.Optional;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.types.ASTTypeArguments;
import org.spruce.compiler.scanner.Location;

import static org.spruce.compiler.scanner.TokenType.NEW;

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
    private final ASTTypeArguments myTypeArgs;
    private final ASTTypeToInstantiate myTti;
    private final ASTListNode myArgumentList;

    /**
     * Constructs an <code>ASTUnqualifiedClassInstanceCreationExpression</code>
     * at the given <code>Location</code> with the given TypeToInstantiate and
     * the given ArgumentList.
     * @param location The <code>Location</code>.
     * @param typeArgs An <code>ASTTypeArguments</code>.
     * @param tti An <code>ASTTypeToInstantiate</code>.
     * @param argumentList An <code>ASTListNode</code> representing the argument list.
     */
    public ASTUnqualifiedClassInstanceCreationExpression(Location location, ASTTypeArguments typeArgs,
                                                         ASTTypeToInstantiate tti, ASTListNode argumentList) {
        super(location, Arrays.asList(typeArgs, tti, argumentList), NEW);
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
     * @param argumentList An <code>ASTListNode</code> representing the argument list.
     */
    public ASTUnqualifiedClassInstanceCreationExpression(Location location, ASTTypeToInstantiate tti, ASTListNode argumentList) {
        super(location, Arrays.asList(tti, argumentList), NEW);
        myTypeArgs = null;
        myTti = tti;
        myArgumentList = argumentList;
    }

    /**
     * TODO: For removal when removing collapsing.
     */
    @Override
    public boolean isCollapsible() {
        return false;
    }

    /**
     * Returns an <code>ASTTypeArguments</code>, if it exists.
     * @return An <code>Optional&lt;ASTTypeArguments&gt;</code>.
     */
    public Optional<ASTTypeArguments> getTypeArgs() {
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
     * Returns an <code>ASTListNode</code> representing the argument list.
     * @return An <code>ASTListNode</code> representing the argument list.
     */
    public ASTListNode getArgumentList() {
        return myArgumentList;
    }
}
