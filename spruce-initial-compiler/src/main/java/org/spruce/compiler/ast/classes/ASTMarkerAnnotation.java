package org.spruce.compiler.ast.classes;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTTypeName;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTMarkerAnnotation</code> is "@" followed by a TypeName.</p>
 *
 * <em>
 * MarkerAnnotation:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;@ TypeName
 * </em>
 */
public final class ASTMarkerAnnotation extends ASTParentNode implements ASTAnnotation {
    private final ASTTypeName myTypeName;

    /**
     * Constructs an <code>ASTMarkerAnnotation</code> at the given <code>Location</code>
     * and an <code>ASTTypeName</code>.
     * @param location The <code>Location</code>.
     * @param typeName An <code>ASTTypeName</code>.
     */
    public ASTMarkerAnnotation(Location location, ASTTypeName typeName) {
        super(location);
        myTypeName = typeName;
    }

    /**
     * Returns an <code>ASTTypeName</code>.
     * @return An <code>ASTTypeName</code>.
     */
    public ASTTypeName getTypeName() {
        return myTypeName;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myTypeName);
    }
}
