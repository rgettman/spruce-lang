package org.spruce.compiler.ast.classes;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTAnnotatedNode;
import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.expressions.ASTArgumentList;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.common.Location;
import org.spruce.compiler.scanner.TokenType;

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
public final class ASTEnumConstant extends ASTAnnotatedNode implements ASTMember {
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

    /**
     * Enum constants don't declare any modifiers.
     * @return An empty <code>List</code>.
     */
    @Override
    public List<TokenType> getModifiers() {
        return List.of();
    }

    /**
     * Enum constants don't declare an access modifier.
     * @return An empty <code>Optional</code>.
     */
    @Override
    public Optional<ASTKeywordNode> getAccessMod() {
        return Optional.empty();
    }

    /**
     * Returns a <code>List</code> of exactly one <code>ASTIdentifier</code>
     * representing the enum constant name.
     * @return A <code>List</code> of <code>ASTIdentifier</code> of size 1.
     */
    @Override
    public List<ASTIdentifier> getNames() {
        return Arrays.asList(myName);
    }
}
