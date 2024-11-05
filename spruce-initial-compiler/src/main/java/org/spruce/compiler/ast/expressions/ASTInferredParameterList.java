package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTInferredParameterList</code> is a list of identifiers
 * separated by commas.</p>
 *
 * <em>
 * InferredParameterList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier {, Identifier}<br>
 * </em>
 */
public final class ASTInferredParameterList extends ASTListNode<ASTIdentifier> implements ASTLambdaParameterList {
    /**
     * Constructs an <code>ASTListNode</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTIdentifier</code>s.
     */
    public ASTInferredParameterList(Location location, List<ASTIdentifier> children) {
        super(location, children, Type.INFERRED_PARAMETERS);
    }
}
