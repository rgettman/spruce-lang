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
 * <p>An <code>ASTAnnotationDeclaration</code> is an optional AnnotationList,
 * followed by an optional AccessModifier, followed by an optional
 * InterfaceModifierList, then "annotation", an Identifier, then an AnnotationBody.</p>
 *
 * <em>
 * AnnotationDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] [AccessModifier] [InterfaceModifierList] annotation Identifier AnnotationBody
 * </em>
 */
public final class ASTAnnotationDeclaration extends ASTAnnotatedNode implements ASTTypeDeclaration {
    private final ASTKeywordNode myAccessMod;
    private final ASTInterfaceModifierList myInterfaceModList;
    private final ASTIdentifier myName;
    private final ASTAnnotationPartList myBody;

    /**
     * Constructs an <code>ASTAnnotationDeclaration</code> at the given <code>Location</code>
     * with the given AnnotationList, AccessModifier, InterfaceModifierList, Annotation Name, and
     * Annotation Parts List.
     * @param location The <code>Location</code>.
     * @param annList A possibly empty <code>ASTAnnotationList</code>.
     * @param accessMod A possibly null <code>ASTKeywordNode</code> representing an AccessModifier.
     * @param interfaceModList An <code>ASTInterfaceModifierList</code>.
     * @param name An <code>ASTIdentifier</code> representing the annotation name.
     * @param body An <code>ASTAnnotationPartList</code>.
     */
    public ASTAnnotationDeclaration(Location location, ASTAnnotationList annList, ASTKeywordNode accessMod,
                                    ASTInterfaceModifierList interfaceModList, ASTIdentifier name, ASTAnnotationPartList body) {
        super(location, annList);
        myAccessMod = accessMod;
        myInterfaceModList = interfaceModList;
        myName = name;
        myBody = body;
    }

    /**
     * Constructs an <code>ASTAnnotationDeclaration</code> at the given <code>Location</code>
     * with the given AnnotationList, AccessModifier, InterfaceModifierList, Annotation Name, and
     * Annotation Parts List.
     * @param location The <code>Location</code>.
     * @param annList A possibly empty <code>ASTAnnotationList</code>.
     * @param interfaceModList An <code>ASTInterfaceModifierList</code>.
     * @param name An <code>ASTIdentifier</code> representing the annotation name.
     * @param body An <code>ASTAnnotationPartList</code>.
     */
    public ASTAnnotationDeclaration(Location location, ASTAnnotationList annList, ASTInterfaceModifierList interfaceModList,
                                    ASTIdentifier name, ASTAnnotationPartList body) {
        super(location, annList);
        myAccessMod = null;
        myInterfaceModList = interfaceModList;
        myName = name;
        myBody = body;
    }

    @Override
    public List<TokenType> getModifiers() {
        List<TokenType> modifiers = new ArrayList<>();
        if (myAccessMod != null) {
            modifiers.add(myAccessMod.getKeyword());
        }
        for (ASTKeywordNode modifier : myInterfaceModList.getTypedChildren()) {
            modifiers.add(modifier.getKeyword());
        }
        return modifiers;
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
     * Returns an <code>ASTInterfaceModifierList</code>.
     * @return An <code>ASTInterfaceModifierList</code>.
     */
    public ASTInterfaceModifierList getInterfaceModList() {
        return myInterfaceModList;
    }

    /**
     * Returns an <code>ASTIdentifier</code> representing the record name.
     * @return An <code>ASTIdentifier</code>.
     */
    @Override
    public ASTIdentifier getName() {
        return myName;
    }

    /**
     * Returns a <code>List</code> of <code>ASTIdentifier</code> containing
     * only one identifier - the name.
     * @return A <code>List</code> of <code>ASTIdentifier</code> of size 1.
     */
    @Override
    public List<ASTIdentifier> getNames() {
        return Arrays.asList(myName);
    }

    /**
     * Returns no <code>ASTTypeParameterList</code>.
     * @return An empty <code>Optional</code>.
     */
    @Override
    public Optional<ASTTypeParameterList> getTypeParams() {
        return Optional.empty();
    }

    /**
     * Returns an <code>ASTAnnotationPartList</code>.
     * @return An <code>ASTAnnotationPartList</code>.
     */
    public ASTAnnotationPartList getBody() {
        return myBody;
    }

    /**
     * Returns a <code>List</code> of <code>ASTMembers</code> consisting of all
     * interface parts.
     * @return A <code>List</code> of <code>ASTMembers</code>.
     */
    @Override
    public List<ASTMember> getMembers() {
        return myBody.getTypedChildren().stream()
                .map(part -> (ASTMember) part)
                .toList();
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(5);
        children.add(myAnnList);
        if (myAccessMod != null) {
            children.add(myAccessMod);
        }
        children.add(myInterfaceModList);
        children.add(myName);
        children.add(myBody);
        return children;
    }
}
