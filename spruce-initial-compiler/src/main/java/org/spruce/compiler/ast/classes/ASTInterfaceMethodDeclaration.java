package org.spruce.compiler.ast.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTInterfaceMethodDeclaration</code> is an optional AccessModifier followed by
 * an optional InterfaceMethodModifierList, then a MethodHeader and a MethodBody.</p>
 *
 * <em>
 * InterfaceMethodDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[AccessModifier] [InterfaceMethodModifierList] MethodHeader MethodBody
 * </em>
 */
public final class ASTInterfaceMethodDeclaration extends ASTParentNode implements ASTInterfacePart {
    private final ASTKeywordNode myAccessMod;
    private final ASTInterfaceMethodModifierList myModifierList;
    private final ASTMethodHeader myHeader;
    private final ASTMethodBody myBody;

    /**
     * Constructs an <code>ASTInterfaceMethodDeclaration</code> at the given <code>Location</code>
     * with the given <code>ASTKeywordNode</code> representing an AccessModifier,
     * the given <code>ASTInterfaceMethodModifierList</code>,
     * the given <code>ASTMethodHeader</code>, and the given <code>ASTMethodBody</code>.
     * @param location The <code>Location</code>.
     * @param accessMod An <code>ASTKeywordNode</code> representing an AccessModifier.
     * @param modifierList An <code>ASTInterfaceMethodModifierList</code>.
     * @param header An <code>ASTMethodHeader</code>.
     * @param body An <code>ASTMethodBody</code>.
     */
    public ASTInterfaceMethodDeclaration(Location location, ASTKeywordNode accessMod, ASTInterfaceMethodModifierList modifierList,
                                         ASTMethodHeader header, ASTMethodBody body) {
        super(location);
        myAccessMod = accessMod;
        myModifierList = modifierList;
        myHeader = header;
        myBody = body;
    }

    /**
     * Constructs an <code>ASTInterfaceMethodDeclaration</code> at the given <code>Location</code>
     * with the given <code>ASTInterfaceMethodModifierList</code>,
     * the given <code>ASTMethodHeader</code>, and the given <code>ASTMethodBody</code>.
     * @param location The <code>Location</code>.
     * @param modifierList An <code>ASTInterfaceMethodModifierList</code>.
     * @param header An <code>ASTMethodHeader</code>.
     * @param body An <code>ASTMethodBody</code>.
     */
    public ASTInterfaceMethodDeclaration(Location location, ASTInterfaceMethodModifierList modifierList,
                                         ASTMethodHeader header, ASTMethodBody body) {
        super(location);
        myAccessMod = null;
        myModifierList = modifierList;
        myHeader = header;
        myBody = body;
    }

    /**
     * Returns an <code>ASTKeywordNode</code> representing an AccessModifier, if it exists.
     * @return An <code>Optional&lt;ASTKeywordNode&gt;</code> representing an AccessModifier.
     */
    public Optional<ASTKeywordNode> getAccessMod() {
        return Optional.ofNullable(myAccessMod);
    }

    /**
     * Returns an <code>ASTInterfaceMethodModifierList</code>.
     * @return An <code>ASTInterfaceMethodModifierList</code>.
     */
    public ASTInterfaceMethodModifierList getModifierList() {
        return myModifierList;
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
        children.add(myModifierList);
        children.add(myHeader);
        children.add(myBody);
        return children;
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
        StringBuilder buf = new StringBuilder();
        buf.append(prefix).append(isTail ? "└── " : "├── ").append(getHeaderValue()).append("\n");
        if (myAccessMod != null) {
            buf.append(myAccessMod.toString(prefix + (isTail ? "    " : "|   "), false)).append("\n");
        }
        buf.append(myModifierList.toString(prefix + (isTail ? "    " : "|   "), false)).append("\n");
        buf.append(myHeader.toString(prefix + (isTail ? "    " : "|   "), false)).append("\n");
        buf.append(myBody.toString(prefix + (isTail ? "    " : "|   "), true)).append("\n");
        return buf.toString();
    }
}
