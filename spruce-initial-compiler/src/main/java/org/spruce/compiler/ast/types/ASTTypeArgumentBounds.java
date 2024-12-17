package org.spruce.compiler.ast.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTTypeArgumentBounds</code> is a data type optionally prefixed with
 * <code>in</code> or <code>out</code>.</p>
 *
 * <em>
 * TypeArgumentBounds:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;in DataType<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;out DataType<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataType
 * </em>
 */
public final class ASTTypeArgumentBounds extends ASTParentNode implements ASTTypeArgument {
    private final ASTKeywordNode myGenericModifier;
    private final ASTDataType myDataType;

    /**
     * Constructs an <code>ASTTypeArgumentBounds</code> at the given <code>Location</code>
     * with the given <code>ASTKeywordNode</code> representing a generic modifier
     * and the given <code>ASTDataType</code>.
     * @param location The <code>Location</code>.
     * @param genericModifier An <code>ASTKeywordNode</code> representing a
     *                        generic modifier, either <code>in</code> or <code>out</code>.
     * @param dataType An <code>ASTDataType</code>.
     */
    public ASTTypeArgumentBounds(Location location, ASTKeywordNode genericModifier, ASTDataType dataType) {
        super(location);
        myGenericModifier = genericModifier;
        myDataType = dataType;
    }

    /**
     * Constructs an <code>ASTTypeArgumentBounds</code> at the given <code>Location</code>
     * with the given <code>ASTDataType</code>.
     * @param location The <code>Location</code>.
     * @param dataType An <code>ASTDataType</code>.
     */
    public ASTTypeArgumentBounds(Location location, ASTDataType dataType) {
        super(location);
        myGenericModifier = null;
        myDataType = dataType;
    }

    /**
     * Returns the <code>ASTKeywordNode</code> representing a generic modifier,
     * if it exists.
     * @return An <code>Optional&lt;ASTKeywordNode&gt;</code> that may represent
     *     <code>in</code> or <code>out</code>.
     */
    public Optional<ASTKeywordNode> getGenericModifier() {
        return Optional.ofNullable(myGenericModifier);
    }

    /**
     * Returns the <code>ASTDataType</code>.
     * @return The <code>ASTDataType</code>.
     */
    public ASTDataType getDataType() {
        return myDataType;
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(2);
        if (myGenericModifier != null) {
            children.add(myGenericModifier);
        }
        children.add(myDataType);
        return children;
    }
}
