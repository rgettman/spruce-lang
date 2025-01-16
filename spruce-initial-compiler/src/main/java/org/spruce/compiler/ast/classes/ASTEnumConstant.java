package org.spruce.compiler.ast.classes;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTAnnotatedNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.expressions.ASTArgumentList;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTEnumConstant</code> is an optional AnnotationList followed by
 * an identifier with an optional argument list within parentheses, optionally
 * followed by a ClassBody.</p>
 *
 * <em>
 * EnumConstant:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] Identifier [( ArgumentList )] [ClassBody]
 * </em>
 */
public class ASTEnumConstant extends ASTAnnotatedNode {
    private final ASTIdentifier myName;
    private final ASTArgumentList myArgsList;
    private final ASTClassPartList myClassParts;

    /**
     * Constructs an <code>ASTEnumConstant</code> at the given <code>Location</code>
     * with the given <code>ASTAnnotationList</code>, the given
     * <code>ASTArgumentList</code>, and the given <code>ASTClassPartList</code>.
     * @param location The <code>Location</code>.
     * @param annList An <code>ASTAnnotationList</code>, possibly empty.
     * @param argsList An <code>ASTArgumentList</code>.
     * @param classParts An <code>ASTClassPartList</code>.
     */
    public ASTEnumConstant(Location location, ASTAnnotationList annList, ASTIdentifier name, ASTArgumentList argsList,
                           ASTClassPartList classParts) {
        super(location, annList);
        myName = name;
        myArgsList = argsList;
        myClassParts = classParts;
    }

    /**
     * Returns an <code>ASTIdentifier</code> representing the enum constant name.
     * @return An <code>ASTIdentifier</code>.
     */
    public ASTIdentifier getName() {
        return myName;
    }

    /**
     * Returns an <code>ASTArgumentList</code>.
     * @return An <code>ASTArgumentList</code>.
     */
    public ASTArgumentList getArgsList() {
        return myArgsList;
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
        return Arrays.asList(myAnnList, myName, myArgsList, myClassParts);
    }
}
