package org.spruce.compiler.ast.classes;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTElementValuePair</code> is an identifier, the assignment
 * operator, and an element value.</p>
 *
 * <em>
 * ElementValuePair:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier = ElementValue
 * </em>
 */
public class ASTElementValuePair extends ASTParentNode {
    private final ASTIdentifier myElementName;
    private final ASTElementValue myElementValue;

    /**
     * Constructs an <code>ASTElementValuePair</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param elementName An <code>ASTIdentifier</code> representing the element name.
     * @param elementValue An <code>ASTElementValue</code>.
     */
    public ASTElementValuePair(Location location, ASTIdentifier elementName, ASTElementValue elementValue) {
        super(location);
        myElementName = elementName;
        myElementValue = elementValue;
    }

    /**
     * Returns an <code>ASTIdentifier</code> representing the element name.
     * @return An <code>ASTIdentifier</code> representing the element name.
     */
    public ASTIdentifier getElementName() {
        return myElementName;
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
        return Arrays.asList(myElementName, myElementValue);
    }
}
