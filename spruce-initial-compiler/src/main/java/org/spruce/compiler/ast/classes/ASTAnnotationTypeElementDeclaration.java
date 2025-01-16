package org.spruce.compiler.ast.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTAnnotatedNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.types.ASTDataType;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTAnnotationTypeElementDeclaration</code> is an optional
 * AnnotationList, followed by a data type, then an identifier, then an empty
 * parentheses pair, optionally followed by a default value, ending with a
 * semicolon.</p>
 *
 * <em>
 * AnnotationTypeElementDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] DataType Identifier ( ) ;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] DataType Identifier ( ) DefaultValue ;
 * </em>
 */
public final class ASTAnnotationTypeElementDeclaration extends ASTAnnotatedNode implements ASTAnnotationPart {
    private final ASTDataType myDataType;
    private final ASTIdentifier myName;
    private final ASTElementValue myDefaultValue;

    /**
     * Constructs an <code>ASTAnnotationTypeElementDeclaration</code> at the given <code>Location</code>
     * with the given <code>ASTAnnotationList</code>, the given <code>ASTDataType</code>,
     * the given <code>ASTIdentifier</code> representing the annotation type
     * element name, and the given <code>ASTElementValue</code> representing
     * the default value.
     * @param location The <code>Location</code>.
     * @param annList A possibly empty <code>ASTAnnotationList</code>.
     * @param dataType An <code>ASTDataType</code>.
     * @param name An <code>ASTIdentifier</code> representing the type element name.
     * @param defaultValue An <code>ASTElementValue</code>.
     */
    public ASTAnnotationTypeElementDeclaration(Location location, ASTAnnotationList annList, ASTDataType dataType,
                                               ASTIdentifier name, ASTElementValue defaultValue) {
        super(location, annList);
        myDataType = dataType;
        myName = name;
        myDefaultValue = defaultValue;
    }

    /**
     * Constructs an <code>ASTAnnotationTypeElementDeclaration</code> at the given <code>Location</code>
     * with the given <code>ASTAnnotationList</code>, the given <code>ASTDataType</code>,
     * and the given <code>ASTIdentifier</code> representing the annotation type
     * element name.
     * @param location The <code>Location</code>.
     * @param annList A possibly empty <code>ASTAnnotationList</code>.
     * @param dataType An <code>ASTDataType</code>.
     * @param name An <code>ASTIdentifier</code> representing the type element name.
     */
    public ASTAnnotationTypeElementDeclaration(Location location, ASTAnnotationList annList, ASTDataType dataType,
                                               ASTIdentifier name) {
        super(location, annList);
        myDataType = dataType;
        myName = name;
        myDefaultValue = null;
    }

    /**
     * Returns an <code>ASTDataType</code>.
     * @return An <code>ASTDataType</code>.
     */
    public ASTDataType getDataType() {
        return myDataType;
    }

    /**
     * Returns an <code>ASTIdentifier</code> representing the type element name.
     * @return An <code>ASTIdentifier</code>.
     */
    public ASTIdentifier getName() {
        return myName;
    }

    /**
     * Returns an <code>ASTElementValue</code> representing the default element value,
     * if it exists.
     * @return An <code>Optional&lt;ASTElementValue&gt;</code>.
     */
    public Optional<ASTElementValue> getDefaultValue() {
        return Optional.ofNullable(myDefaultValue);
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(4);
        children.add(myAnnList);
        children.add(myDataType);
        children.add(myName);
        if (myDefaultValue != null) {
            children.add(myDefaultValue);
        }
        return children;
    }
}
