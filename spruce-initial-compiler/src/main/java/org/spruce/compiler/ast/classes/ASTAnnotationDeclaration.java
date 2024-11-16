package org.spruce.compiler.ast.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTAnnotatedNode;
import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.scanner.Location;

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

    /**
     * Returns an <code>ASTKeywordNode</code> representing the Access Modifier, if it exists.
     * @return An <code>Optional&lt;ASTListNode&gt;</code>.
     */
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
     * Returns an <code>ASTIdentifier</code> representing the annotation name.
     * @return An <code>ASTIdentifier</code> representing the annotation name.
     */
    public ASTIdentifier getName() {
        return myName;
    }

    /**
     * Returns an <code>ASTAnnotationPartList</code>.
     * @return An <code>ASTAnnotationPartList</code>.
     */
    public ASTAnnotationPartList getBody() {
        return myBody;
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
