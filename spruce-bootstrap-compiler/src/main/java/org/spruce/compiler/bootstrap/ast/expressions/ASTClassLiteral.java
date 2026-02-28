package org.spruce.compiler.bootstrap.ast.expressions;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.ast.types.ASTDataType;
import org.spruce.compiler.bootstrap.common.Location;

/**
 * <p>An <code>ASTClassLiteral</code> is a data type followed by "." and "class".</p>
 *
 * <p>
 *     TODO: When annotations are introduced, make sure they aren't allowed
 *     when parsing "Dims" here.
 * </p>
 *
 * <em>
 * ClassLiteral:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataType . class
 * </em>
 */
public class ASTClassLiteral extends ASTParentNode {
    private final ASTDataType myDataType;

    /**
     * Constructs an <code>ASTClassLiteral</code> at the given <code>Location</code>
     * with the given <code>ASTDataType</code>.
     * @param location The <code>Location</code>.
     * @param dataType An <code>ASTDataType</code>.
     */
    public ASTClassLiteral(Location location, ASTDataType dataType) {
        super(location);
        myDataType = dataType;
    }

    /**
     * Returns an <code>ASTDataType</code>.
     * @return An <code>ASTDataType</code>.
     */
    public ASTDataType getDataType() {
        return myDataType;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myDataType);
    }
}
