package org.spruce.compiler.ast.types;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTWildcardBounds</code> is a "&lt;:" or ":&gt;" followed by a
 * DataType.</p>
 *
 * <em>
 * WildcardBounds:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;: DataType<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;:&gt; DataType
 * </em>
 */
public class ASTWildcardBounds extends ASTParentNode {
    private final ASTKeywordNode myBoundKeyword;
    private final ASTDataType myDataType;

    /**
     * Constructs an <code>ASTWildcardBounds</code> at the given <code>Location</code>
     * with the given <code>ASTKeyword</code> representing a lower or upper
     * bound and the given <code>ASTDataType</code>.
     * @param location The <code>Location</code>.
     * @param boundKeyword An <code>ASTKeywordNode</code> of keyword <code>SUBTYPE</code> or <code>SUPERTYPE</code>.
     * @param dataType An <code>ASTDataType</code>.
     */
    public ASTWildcardBounds(Location location, ASTKeywordNode boundKeyword, ASTDataType dataType) {
        super(location);
        myBoundKeyword = boundKeyword;
        myDataType = dataType;
    }

    /**
     * Returns an <code>ASTKeywordNode</code> representing a lower or upper bound.
     * @return An <code>ASTKeywordNode</code> of keyword <code>SUBTYPE</code> or <code>SUPERTYPE</code>.
     */
    public ASTKeywordNode getBoundKeyword() {
        return myBoundKeyword;
    }

    /**
     * Returns an <code>ASTDataType</code>.
     * @return An <code>ASTDataType</code>.
     */
    public ASTDataType getDataType() {
        return myDataType;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myBoundKeyword, myDataType);
    }
}
