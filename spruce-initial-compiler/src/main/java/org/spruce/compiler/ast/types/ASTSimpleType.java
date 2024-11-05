package org.spruce.compiler.ast.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTSimpleType</code> is a simple type with optional type arguments.</p>
 *
 * <em>
 * SimpleType:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier TypeArguments
 * </em>
 */
public class ASTSimpleType extends ASTParentNode {
    private final ASTIdentifier myName;
    private final ASTTypeArgumentList myTypeArgs;

    /**
     * Constructs an <code>ASTSimpleType</code> at the given <code>Location</code>
     * with the given <code>ASTIdentifier</code> representing the name, and the
     * given <code>ASTListNode</code> representing the TypeArguments.
     * @param location The <code>Location</code>.
     * @param name An <code>ASTIdentifier</code> representing the name.
     * @param typeArgs An <code>ASTTypeArgumentList</code>.
     */
    public ASTSimpleType(Location location, ASTIdentifier name, ASTTypeArgumentList typeArgs) {
        super(location);
        myName = name;
        myTypeArgs = typeArgs;
    }

    /**
     * Constructs an <code>ASTSimpleType</code> at the given <code>Location</code>
     * with the given <code>ASTIdentifier</code> representing the name.
     * @param location The <code>Location</code>.
     * @param name An <code>ASTIdentifier</code> representing the name.
     */
    public ASTSimpleType(Location location, ASTIdentifier name) {
        super(location);
        myName = name;
        myTypeArgs = null;
    }

    /**
     * Returns an <code>ASTIdentifier</code> representing the name.
     * @return An <code>ASTIdentifier</code> representing the name.
     */
    public ASTIdentifier getName() {
        return myName;
    }

    /**
     * Returns an <code>ASTTypeArgumentList</code>, if it exists.
     * @return An <code>Optional&lt;ASTTypeArgumentList&gt;</code>.
     */
    public Optional<ASTTypeArgumentList> getTypeArgs() {
        return Optional.ofNullable(myTypeArgs);
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(2);
        children.add(myName);
        if (myTypeArgs != null) {
            children.add(myTypeArgs);
        }
        return children;
    }
}
