package org.spruce.compiler.ast.types;

import java.util.ArrayList;
import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.names.ASTAmbiguousName;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTDataTypeNoArray</code> is a simple or fully qualified
 * type.</p>
 *
 * <em>
 * DataTypeNoArray:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;SimpleType<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataTypeNoArray . SimpleType
 * </em>
 */
public class ASTDataTypeNoArray extends ASTParentNode {
    /**
     * Constructs an <code>ASTDataTypeNoArray</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTDataTypeNoArray(Location location, List<ASTNode> children) {
        super(location, children);
    }

    /**
     * This node is collapsible.
     * @return <code>false</code>.
     */
    @Override
    public boolean isCollapsible() {
        return false;
    }

    /**
     * Converts the children from (DTNA, SimpleType) to (AmbiguousName, Identifier)
     * or (SimpleType) to (Identifier).
     * @return A <code>List</code> of child nodes suitable for an
     *     <code>ASTAmbiguousName</code> or an <code>ASTExpressionName</code>.
     */
    public List<ASTNode> convertChildren() {
        List<ASTNode> children = getChildren();
        List<ASTNode> convertedChildren = new ArrayList<>(children.size());
        for (ASTNode child : children) {
            if (child instanceof ASTDataTypeNoArray dtna) {
                ASTAmbiguousName ambName = new ASTAmbiguousName(dtna.getLocation(), dtna.convertChildren());
                ambName.setOperation(dtna.getOperation());
                convertedChildren.add(ambName);
            }
            else if (child instanceof ASTSimpleType st) {
                List<ASTNode> stChildren = st.getChildren();
                if (stChildren.size() > 1) {
                    throw new CompileException(child.getLocation(), "Variable declarator expected after type.");
                }
                convertedChildren.add(stChildren.get(0)); // ASTIdentifier
            }
        }
        return convertedChildren;
    }
}
