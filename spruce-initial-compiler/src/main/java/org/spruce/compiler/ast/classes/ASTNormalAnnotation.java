package org.spruce.compiler.ast.classes;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTTypeName;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTNormalAnnotation</code> is "@" followed by a TypeName,
 * then an optional element value pair list within parentheses.</p>
 *
 * <em>
 * NormalAnnotation:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;@ TypeName ( [ElementValuePairList] )
 * </em>
 */
public final class ASTNormalAnnotation extends ASTParentNode implements ASTAnnotation {
    private final ASTTypeName myTypeName;
    private final ASTElementValuePairList myElementValuePairList;

    /**
     * Constructs an <code>ASTNormalAnnotation</code> at the given <code>Location</code>
     * with the given <code>ASTTypeName</code> and the given
     * <code>ASTElementValuePairList</code>.
     * @param location The <code>Location</code>.
     * @param typeName An <code>ASTTypeName</code>.
     * @param elementValuePairList An <code>ASTElementValuePairList</code>.
     */
    public ASTNormalAnnotation(Location location, ASTTypeName typeName, ASTElementValuePairList elementValuePairList) {
        super(location);
        myTypeName = typeName;
        myElementValuePairList = elementValuePairList;
    }

    /**
     * Returns an <code>ASTTypeName</code>.
     * @return An <code>ASTTypeName</code>.
     */
    public ASTTypeName getTypeName() {
        return myTypeName;
    }

    /**
     * Returns an <code>ASTElementValuePairList</code>.
     * @return An <code>ASTElementValuePairList</code>.
     */
    public ASTElementValuePairList getElementValuePairList() {
        return myElementValuePairList;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myTypeName, myElementValuePairList);
    }
}
