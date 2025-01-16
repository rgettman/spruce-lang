package org.spruce.compiler.ast.expressions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.types.ASTDataType;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTRecordPattern</code> is a data type
 * followed by an optional PatternList enclosed in parentheses.</p>
 *
 * <em>
 * RecordPattern:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataType ( [PatternList] )<br>
 * </em>
 */
public final class ASTRecordPattern extends ASTParentNode implements ASTPattern {
    private final ASTDataType myDataType;
    private final ASTPatternList myPatternList;

    /**
     * Constructs an <code>ASTRecordPattern</code> with the given Location,
     * DataType, and PatternList.
     * @param location A <code>Location</code>.
     * @param dataType An <code>ASTDataType</code>.
     * @param patternList An <code>ASTPatternList</code>.
     */
    public ASTRecordPattern(Location location, ASTDataType dataType, ASTPatternList patternList) {
        super(location);
        myDataType = dataType;
        myPatternList = patternList;
    }

    /**
     * Returns the <code>ASTDataType</code>.
     * @return The <code>ASTDataType</code>.
     */
    public ASTDataType getDataType() {
        return myDataType;
    }

    /**
     * Returns an <code>ASTPatternList</code>, if it exists.
     * @return An <code>Optional&lt;ASTPatternList&gt;</code>.
     */
    public Optional<ASTPatternList> getPatternList() {
        return Optional.ofNullable(myPatternList);
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(2);
        children.add(myDataType);
        if (myPatternList != null) {
            children.add(myPatternList);
        }
        return children;
    }
}
