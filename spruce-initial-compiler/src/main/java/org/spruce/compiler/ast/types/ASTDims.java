package org.spruce.compiler.ast.types;

import java.util.List;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTDims</code> is a set of dimensions on an array type.</p>
 *
 * <em>
 * Dims:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[]<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Dims []
 * </em>
 */
public class ASTDims extends ASTListNode<ASTKeywordNode> {
    /**
     * Constructs an <code>ASTDims</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTKeywordNode</code>s
     *                 representing <code>OPEN_CLOSE_BRACKET</code>.
     */
    public ASTDims(Location location, List<ASTKeywordNode> children) {
        super(location, children, Type.DIMS);
    }
}
