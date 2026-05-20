package org.spruce.compiler.bootstrap.ast.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.bootstrap.ast.ASTKeywordNode;
import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.symbol.DataTypeResolution;
import org.spruce.compiler.bootstrap.ast.types.ASTDataType;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;

/**
 * <p>An <code>ASTResult</code> is "void", or a DataType.</p>
 *
 * <em>
 * Result:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataType<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;void<br>
 * </em>
 */
public class ASTResult extends ASTParentNode implements DataTypeResolution {
    private final ASTDataType myDataType;
    private final ASTKeywordNode myVoid;
    private TypeSymbol myResolvedDataType;

    /**
     * Constructs an <code>ASTResult</code> at the given <code>Location</code>
     * with the given <code>ASTDataType</code>.
     * @param location The <code>Location</code>.
     * @param dataType An <code>ASTDataType</code>.
     */
    public ASTResult(Location location, ASTDataType dataType) {
        super(location);
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
        myDataType = null;
        myVoid = voidKeyword;
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

    /**
     * Returns the normalized string for the data type or "void" if void.
     * @return The normalized string for the data type or "void" if void.
     */
    public String getTypeName() {
        return myDataType != null ? myDataType.getTypeName() : "void";
    }

    @Override
    public void setResolvedDataType(TypeSymbol symbol) {
        myResolvedDataType = symbol;
    }

    @Override
    public TypeSymbol getResolvedDataType() {
        return myResolvedDataType;
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(2);
        if (myDataType != null) {
            children.add(myDataType);
        }
        return children;
    }
}
