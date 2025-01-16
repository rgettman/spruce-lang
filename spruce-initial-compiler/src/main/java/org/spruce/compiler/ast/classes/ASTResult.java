package org.spruce.compiler.ast.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.types.ASTDataType;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTResult</code> is "void", or an optional MutModifier
 * followed by a DataType.</p>
 *
 * <em>
 * Result:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;MutModifier DataType<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataType<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;void<br>
 * </em>
 */
public class ASTResult extends ASTParentNode {
    private final ASTKeywordNode myMutMod;
    private final ASTDataType myDataType;
    private final ASTKeywordNode myVoid;

    /**
     * Constructs an <code>ASTResult</code> at the given <code>Location</code>
     * with the given MutModifier and the given <code>ASTDataType</code>.
     * @param location The <code>Location</code>.
     * @param mutMod An <code>ASTKeyword</code> representing a MutModifier.
     * @param dataType An <code>ASTDataType</code>.
     */
    public ASTResult(Location location, ASTKeywordNode mutMod, ASTDataType dataType) {
        super(location);
        myMutMod = mutMod;
        myDataType = dataType;
        myVoid = null;
    }

    /**
     * Constructs an <code>ASTResult</code> at the given <code>Location</code>
     * with the given <code>ASTDataType</code>.
     * @param location The <code>Location</code>.
     * @param dataType An <code>ASTDataType</code>.
     */
    public ASTResult(Location location, ASTDataType dataType) {
        super(location);
        myMutMod = null;
        myDataType = dataType;
        myVoid = null;
    }

    /**
     * Constructs an <code>ASTResult</code> at the given <code>Location</code>
     * representing <code>void</code>.
     * @param location The <code>Location</code>.
     */
    public ASTResult(Location location, ASTKeywordNode voidKeyword) {
        super(location);
        myMutMod = null;
        myDataType = null;
        myVoid = voidKeyword;
    }

    /**
     * Returns an <code>ASTKeywordNode</code> representing the MutModifier, if it exists.
     * @return An <code>Optional&lt;ASTKeywordNode&gt;</code>.
     */
    public Optional<ASTKeywordNode> getMutMod() {
        return Optional.ofNullable(myMutMod);
    }

    /**
     * Returns an <code>ASTDataType</code>, if it exists.
     * @return An <code>Optional&lt;ASTDataType&gt;</code>.
     */
    public Optional<ASTDataType> getDataType() {
        return Optional.ofNullable(myDataType);
    }

    /**
     * Returns an <code>ASTKeywordNode</code> representing <code>void</code>, if it exists.
     * @return An <code>Optional&lt;ASTKeywordNode&gt;</code>.
     */
    public Optional<ASTKeywordNode> getVoidKeyword() {
        return Optional.ofNullable(myVoid);
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(2);
        if (myMutMod != null) {
            children.add(myMutMod);
        }
        if (myDataType != null) {
            children.add(myDataType);
        }
        return children;
    }
}
