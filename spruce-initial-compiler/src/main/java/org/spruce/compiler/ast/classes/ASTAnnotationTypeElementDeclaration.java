package org.spruce.compiler.ast.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.types.ASTDataType;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTAnnotationTypeElementDeclaration</code> is a data type, then
 * an identifier, then an empty parentheses pair, optionally followed by a default
 * value, ending with a semicolon.</p>
 *
 * <em>
 * AnnotationTypeElementDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataType Identifier ( ) ;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataType Identifier ( ) DefaultValue ;
 * </em>
 */
public final class ASTAnnotationTypeElementDeclaration extends ASTParentNode implements ASTAnnotationPart {
    private final ASTDataType myDataType;
    private final ASTIdentifier myName;
    private final ASTElementValue myDefaultValue;

    /**
     * Constructs an <code>ASTAnnotationTypeElementDeclaration</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param location The <code>Location</code>.
     * @param dataType An <code>ASTDataType</code>.
     * @param name An <code>ASTIdentifier</code> representing the type element name.
     * @param defaultValue An <code>ASTElementValue</code>.
     */
    public ASTAnnotationTypeElementDeclaration(Location location, ASTDataType dataType,
                                               ASTIdentifier name, ASTElementValue defaultValue) {
        super(location);
        myDataType = dataType;
        myName = name;
        myDefaultValue = defaultValue;
    }

    /**
     * Constructs an <code>ASTAnnotationTypeElementDeclaration</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param location The <code>Location</code>.
     * @param dataType An <code>ASTDataType</code>.
     * @param name An <code>ASTIdentifier</code> representing the type element name.
     */
    public ASTAnnotationTypeElementDeclaration(Location location, ASTDataType dataType, ASTIdentifier name) {
        super(location);
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
        List<Node> children = new ArrayList<>(3);
        children.add(myDataType);
        children.add(myName);
        if (myDefaultValue != null) {
            children.add(myDefaultValue);
        }
        return children;
    }
}
