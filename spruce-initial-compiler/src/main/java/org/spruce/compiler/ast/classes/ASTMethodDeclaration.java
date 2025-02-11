package org.spruce.compiler.ast.classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTAnnotatedNode;
import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.common.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * <p>An <code>ASTMethodDeclaration</code> is an optional AnnotationList,
 * followed by an optional AccessModifier, followed by an optional MethodModifierList,
 * then a MethodHeader and a MethodBody.</p>
 *
 * <em>
 * MethodDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] [AccessModifier] [MethodModifierList] MethodHeader MethodBody
 * </em>
 */
public final class ASTMethodDeclaration extends ASTAnnotatedNode implements ASTClassPart {
    private final ASTKeywordNode myAccessMod;
    private final ASTMethodModifierList myMethodModList;
    private final ASTMethodHeader myHeader;
    private final ASTMethodBody myBody;

    /**
     * Constructs an <code>ASTMethodDeclaration</code> at the given <code>Location</code>
     * with the given <code>ASTAnnotationList</code>, the given <code>ASTKeywordNode</code>
     * representing the Access Modifier, the given <code>ASTMethodModifierList</code>,
     * the given <code>ASTMethodHeader</code>, and the given <code>ASTMethodBody</code>.
     * @param location The <code>Location</code>.
     * @param annList A possibly empty <code>ASTAnnotationList</code>.
     * @param accessMod An <code>ASTKeywordNode</code> representing an AccessModifier.
     * @param methodModList An <code>ASTMethodModifierList</code>.
     * @param header An <code>ASTMethodHeader</code>.
     * @param body An <code>ASTMethodBody</code>.
     */
    public ASTMethodDeclaration(Location location, ASTAnnotationList annList, ASTKeywordNode accessMod,
                                ASTMethodModifierList methodModList, ASTMethodHeader header, ASTMethodBody body) {
        super(location, annList);
        myAccessMod = accessMod;
        myMethodModList = methodModList;
        myHeader = header;
        myBody = body;
    }

    /**
     * Constructs an <code>ASTMethodDeclaration</code> at the given <code>Location</code>
     * with the given <code>ASTAnnotationList</code>, the given <code>ASTKeywordNode</code>
     * representing the Access Modifier, the given <code>ASTMethodModifierList</code>,
     * the given <code>ASTMethodHeader</code>, and the given <code>ASTMethodBody</code>.
     * @param location The <code>Location</code>.
     * @param annList A possibly empty <code>ASTAnnotationList</code>.
     * @param methodModList An <code>ASTMethodModifierList</code>.
     * @param header An <code>ASTMethodHeader</code>.
     * @param body An <code>ASTMethodBody</code>.
     */
    public ASTMethodDeclaration(Location location, ASTAnnotationList annList, ASTMethodModifierList methodModList,
                                ASTMethodHeader header, ASTMethodBody body) {
        super(location, annList);
        myAccessMod = null;
        myMethodModList = methodModList;
        myHeader = header;
        myBody = body;
    }

    /**
     * Returns an <code>ASTKeywordNode</code> representing the Access Modifier, if it exists.
     * @return An <code>Optional&lt;ASTKeywordNode&gt;</code>.
     */
    @Override
    public Optional<ASTKeywordNode> getAccessMod() {
        return Optional.ofNullable(myAccessMod);
    }

    /**
     * Returns an <code>ASTMethodModifierList</code>.
     * @return An <code>ASTMethodModifierList</code>.
     */
    public ASTMethodModifierList getMethodModList() {
        return myMethodModList;
    }

    /**
     * Returns an <code>ASTMethodHeader</code>.
     * @return An <code>ASTMethodHeader</code>.
     */
    public ASTMethodHeader getHeader() {
        return myHeader;
    }

    /**
     * Returns an <code>ASTMethodBody</code>.
     * @return An <code>ASTMethodBody</code>.
     */
    public ASTMethodBody getBody() {
        return myBody;
    }

    @Override
    public List<TokenType> getModifiers() {
        List<TokenType> modifiers = new ArrayList<>();
        if (myAccessMod != null) {
            modifiers.add(myAccessMod.getKeyword());
        }
        for (ASTKeywordNode modifier : myMethodModList.getTypedChildren()) {
            modifiers.add(modifier.getKeyword());
        }
        return modifiers;
    }

    /**
     * Returns a <code>List</code> of exactly one <code>ASTIdentifier</code>
     * representing the method name.
     * @return A <code>List</code> of <code>ASTIdentifier</code> of size 1.
     */
    @Override
    public List<ASTIdentifier> getNames() {
        return Arrays.asList(myHeader.getMethodDecl().getName());
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(5);
        children.add(myAnnList);
        if (myAccessMod != null) {
            children.add(myAccessMod);
        }
        children.add(myMethodModList);
        children.add(myHeader);
        children.add(myBody);
        return children;
    }
}
