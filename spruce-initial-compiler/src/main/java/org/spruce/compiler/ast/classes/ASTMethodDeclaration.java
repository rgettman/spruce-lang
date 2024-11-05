package org.spruce.compiler.ast.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTMethodDeclaration</code> is an optional AccessModifier followed by
 * an optional MethodModifierList, then a MethodHeader and a MethodBody.</p>
 *
 * <em>
 * MethodDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[AccessModifier] [MethodModifierList] MethodHeader MethodBody
 * </em>
 */
public final class ASTMethodDeclaration extends ASTParentNode implements ASTClassPart {
    private final ASTKeywordNode myAccessMod;
    private final ASTMethodModifierList myMethodModList;
    private final ASTMethodHeader myHeader;
    private final ASTMethodBody myBody;

    /**
     * Constructs an <code>ASTMethodDeclaration</code> at the given <code>Location</code>
     * with the given <code>ASTKeywordNode</code> representing the Access Modifier,
     * the given <code>ASTMethodModifierList</code>,
     * the given <code>ASTMethodHeader</code>, and the given <code>ASTMethodBody</code>.
     * @param location The <code>Location</code>.
     * @param accessMod An <code>ASTKeywordNode</code> representing an AccessModifier.
     * @param methodModList An <code>ASTMethodModifierList</code>.
     * @param header An <code>ASTMethodHeader</code>.
     * @param body An <code>ASTMethodBody</code>.
     */
    public ASTMethodDeclaration(Location location, ASTKeywordNode accessMod, ASTMethodModifierList methodModList,
                                ASTMethodHeader header, ASTMethodBody body) {
        super(location);
        myAccessMod = accessMod;
        myMethodModList = methodModList;
        myHeader = header;
        myBody = body;
    }

    /**
     * Constructs an <code>ASTMethodDeclaration</code> at the given <code>Location</code>
     * with the given <code>ASTKeywordNode</code> representing the Access Modifier,
     * the given <code>ASTMethodModifierList</code>,
     * the given <code>ASTMethodHeader</code>, and the given <code>ASTMethodBody</code>.
     * @param location The <code>Location</code>.
     * @param methodModList An <code>ASTMethodModifierList</code>.
     * @param header An <code>ASTMethodHeader</code>.
     * @param body An <code>ASTMethodBody</code>.
     */
    public ASTMethodDeclaration(Location location, ASTMethodModifierList methodModList,
                                ASTMethodHeader header, ASTMethodBody body) {
        super(location);
        myAccessMod = null;
        myMethodModList = methodModList;
        myHeader = header;
        myBody = body;
    }

    /**
     * Returns an <code>ASTKeywordNode</code> representing the Access Modifier, if it exists.
     * @return An <code>Optional&lt;ASTKeywordNode&gt;</code>.
     */
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
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(4);
        if (myAccessMod != null) {
            children.add(myAccessMod);
        }
        children.add(myMethodModList);
        children.add(myHeader);
        children.add(myBody);
        return children;
    }
}
