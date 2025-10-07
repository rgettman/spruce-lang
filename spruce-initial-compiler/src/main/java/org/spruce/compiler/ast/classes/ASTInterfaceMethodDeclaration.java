package org.spruce.compiler.ast.classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTAnnotatedNode;
import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.types.ASTTypeParameterList;
import org.spruce.compiler.common.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * <p>An <code>ASTInterfaceMethodDeclaration</code> is an optional AnnotationList,
 * followed by an optional AccessModifier, followed by an optional InterfaceMethodModifierList,
 * then a MethodHeader and a MethodBody.</p>
 *
 * <em>
 * InterfaceMethodDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] [AccessModifier] [InterfaceMethodModifierList] MethodHeader MethodBody
 * </em>
 */
public final class ASTInterfaceMethodDeclaration extends ASTAnnotatedNode implements ASTInterfacePart {
    private final ASTKeywordNode myAccessMod;
    private final ASTInterfaceMethodModifierList myModifierList;
    private final ASTMethodHeader myHeader;
    private final ASTMethodBody myBody;

    /**
     * Constructs an <code>ASTInterfaceMethodDeclaration</code> at the given <code>Location</code>
     * with the given <code>ASTAnnotationList</code>, the given <code>ASTKeywordNode</code>
     * representing an AccessModifier, the given <code>ASTInterfaceMethodModifierList</code>,
     * the given <code>ASTMethodHeader</code>, and the given <code>ASTMethodBody</code>.
     * @param location The <code>Location</code>.
     * @param annList A possibly empty <code>ASTAnnotationList</code>.
     * @param accessMod An <code>ASTKeywordNode</code> representing an AccessModifier.
     * @param modifierList An <code>ASTInterfaceMethodModifierList</code>.
     * @param header An <code>ASTMethodHeader</code>.
     * @param body An <code>ASTMethodBody</code>.
     */
    public ASTInterfaceMethodDeclaration(Location location, ASTAnnotationList annList, ASTKeywordNode accessMod,
                                         ASTInterfaceMethodModifierList modifierList,
                                         ASTMethodHeader header, ASTMethodBody body) {
        super(location, annList);
        myAccessMod = accessMod;
        myModifierList = modifierList;
        myHeader = header;
        myBody = body;
    }

    /**
     * Constructs an <code>ASTInterfaceMethodDeclaration</code> at the given <code>Location</code>
     * with the given <code>ASTAnnotationList</code>, the given <code>ASTInterfaceMethodModifierList</code>,
     * the given <code>ASTMethodHeader</code>, and the given <code>ASTMethodBody</code>.
     * @param location The <code>Location</code>.
     * @param annList A possibly empty <code>ASTAnnotationList</code>.
     * @param modifierList An <code>ASTInterfaceMethodModifierList</code>.
     * @param header An <code>ASTMethodHeader</code>.
     * @param body An <code>ASTMethodBody</code>.
     */
    public ASTInterfaceMethodDeclaration(Location location, ASTAnnotationList annList,
                                         ASTInterfaceMethodModifierList modifierList,
                                         ASTMethodHeader header, ASTMethodBody body) {
        super(location, annList);
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
    public List<TokenType> getModifiers() {
        List<TokenType> modifiers = new ArrayList<>();
        if (myAccessMod != null) {
            modifiers.add(myAccessMod.getKeyword());
        }
        for (ASTKeywordNode modifier : myModifierList.getTypedChildren()) {
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

    /**
     * Returns an <code>ASTTypeParameterList</code>, if it exists.
     * @return An <code>Optional&lt;ASTTypeParameterList&gt;</code>.
     */
    @Override
    public Optional<ASTTypeParameterList> getTypeParams() {
        return myHeader.getTypeParams();
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(5);
        children.add(myAnnList);
        if (myAccessMod != null) {
            children.add(myAccessMod);
        }
        children.add(myModifierList);
        children.add(myHeader);
        children.add(myBody);
        return children;
    }
}
