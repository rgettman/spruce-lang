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
     * with the given <code>ASTEnumConstantList</code> and the given
     * <code>ASTClassPartList</code>.
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
}
