package org.spruce.compiler.ast.classes;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTTypeName;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTSingleElementAnnotation</code> is "@" followed by a TypeName,
 * then an element value within parentheses.</p>
 *
 * <em>
 * SingleElementAnnotation:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;@ TypeName ( ElementValue )
 * </em>
 */
public final class ASTSingleElementAnnotation extends ASTParentNode implements ASTAnnotation {
    private final ASTTypeName myTypeName;
    private final ASTElementValue myElementValue;

    /**
     * Constructs an <code>ASTSingleElementAnnotation</code> at the given <code>Location</code>
     * with the given <code>ASTTypeName</code> and the given
     * <code>ASTElementValue</code>.
     * @param location The <code>Location</code>.
     * @param typeName An <code>ASTTypeName</code>.
     * @param elementValue An <code>ASTElementValue</code>.
     */
    public ASTSingleElementAnnotation(Location location, ASTTypeName typeName, ASTElementValue elementValue) {
        super(location);
        myTypeName = typeName;
        myElementValue = elementValue;
    }

    /**
     * Returns an <code>ASTTypeName</code>.
     * @return An <code>ASTTypeName</code>.
     */
    public ASTTypeName getTypeName() {
        return myTypeName;
    }

    /**
     * Returns an <code>ASTElementValue</code>.
     * @return An <code>ASTElementValue</code>.
     */
    public ASTElementValue getElementValue() {
        return myElementValue;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myTypeName, myElementValue);
    }
}
