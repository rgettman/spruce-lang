package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTVariableInitializerList</code> is a list of comma-separated
 * variable initializer instances.</p>
 *
 * <em>
 * VariableInitializerList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;VariableInitializer {, VariableInitializer}
 * </em>
 */
public class ASTVariableInitializerList extends ASTListNode<ASTVariableInitializer> {
    /**
     * Constructs an <code>ASTListNode</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTVariableInitializer</code>s.
     */
    public ASTVariableInitializerList(Location location, List<ASTVariableInitializer> children) {
        super(location, children, Type.VARIABLE_INITIALIZERS);
    }
}
