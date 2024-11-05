package org.spruce.compiler.ast.classes;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTEnumBody</code> is a "{", optionally a list of enum constants,
 * followed by an optional enum body declaration, then a "}".</p>
 *
 * <em>
 * EnumBody:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;{ [EnumConstantList] [EnumBodyDeclarations] }
 * </em>
 */
public class ASTEnumBody extends ASTParentNode {
    private final ASTEnumConstantList myEnumConstants;
    private final ASTClassPartList myClassParts;

    /**
     * Constructs an <code>ASTEnumBody</code> at the given <code>Location</code>
     * with the given <code>ASTListNode</code> representing an EnumConstantList
     * and the given <code>ASTListNode</code> representing a ClassPartsList.
     * @param location The <code>Location</code>.
     * @param enumConstants An <code>ASTEnumConstantList</code>.
     * @param classParts An <code>ASTClassPartList</code>.
     */
    public ASTEnumBody(Location location, ASTEnumConstantList enumConstants, ASTClassPartList classParts) {
        super(location);
        myEnumConstants = enumConstants;
        myClassParts = classParts;
    }

    /**
     * Returns an <code>ASTEnumConstantList</code>.
     * @return An <code>ASTEnumConstantList</code>.
     */
    public ASTEnumConstantList getEnumConstants() {
        return myEnumConstants;
    }

    /**
     * Returns an <code>ASTClassPartList</code>.
     * @return An <code>ASTClassPartList</code>.
     */
    public ASTClassPartList getClassParts() {
        return myClassParts;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myEnumConstants, myClassParts);
    }

    /**
     * Helper method to create a string representation of this node.  It takes
     * into account where in the tree this node is.
     * @param prefix A string to indent the printing of this node.
     * @param isTail Whether this node is last in its siblings (or the only child).
     * @return The String representation of this node.
     */
    @Override
    public String toString(String prefix, boolean isTail) {
        return prefix + (isTail ? "└── " : "├── ") + getHeaderValue() + "\n" +
                myEnumConstants.toString(prefix + (isTail ? "    " : "|   "), false) + "\n" +
                myClassParts.toString(prefix + (isTail ? "    " : "|   "), true) + "\n";
    }
}
