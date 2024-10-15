package org.spruce.compiler.ast.expressions;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.types.ASTDataType;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTRecordPattern</code> is a data type
 * followed by an optional PatternList enclosed in parentheses.</p>
 *
 * <em>
 * RecordPattern:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataType ( [PatternList] )<br>
 * </em>
 */
public class ASTRecordPattern extends ASTParentNode {
    private final ASTDataType myDataType;
    private final ASTListNode myPatternList;

    /**
     * Constructs an <code>ASTRecordPattern</code> with the given Location,
     * DataType, but no PatternList.
     * @param location A <code>Location</code>.
     * @param dataType An <code>ASTDataType</code>.
     */
    public ASTRecordPattern(Location location, ASTDataType dataType) {
        super(location, Arrays.asList(dataType));
        myDataType = dataType;
        myPatternList = null;
    }

    /**
     * Constructs an <code>ASTRecordPattern</code> with the given Location,
     * DataType, and PatternList.
     * @param location A <code>Location</code>.
     * @param dataType An <code>ASTDataType</code>.
     * @param patternList An <code>ASTListNode</code>.
     */
    public ASTRecordPattern(Location location, ASTDataType dataType, ASTListNode patternList) {
        super(location, Arrays.asList(dataType, patternList));
        myDataType = dataType;
        myPatternList = patternList;
    }

    /**
     * TODO: For removal when removing collapsing.
     */
    @Override
    public boolean isCollapsible() {
        return false;
    }

    /**
     * Returns the <code>ASTDataType</code>.
     * @return The <code>ASTDataType</code>.
     */
    public ASTDataType getDataType() {
        return myDataType;
    }

    /**
     * Returns the pattern list as an <code>ASTListNode</code> with type
     * <code>PATTERNS</code>, if it exists.
     * @return An <code>Optional&lt;ASTListNode&gt;</code>.
     */
    public Optional<ASTListNode> getPatternList() {
        return Optional.ofNullable(myPatternList);
    }

    /**
     * Prints this node and its children to the output stream.
     * @param prefix A string to indent the printing of this node.
     * @param isTail Whether this node is last in its siblings (or the only child).
     */
    @Override
    public void print(String prefix, boolean isTail) {
        System.out.println(prefix + (isTail ? "└── " : "├── ") + toString());
        myDataType.print(prefix + (isTail ? "    " : "|   "), myPatternList == null);
        if (myPatternList != null) {
            myPatternList.print(prefix + (isTail ? "    " : "│   "), true);
        }
    }
}
