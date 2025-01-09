package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTVariantList</code> is a comma-separated list of variants.</p>
 *
 * <em>
 * VariantList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Variant {, Variant}
 * </em>
 */
public class ASTVariantList extends ASTListNode<ASTVariant> {
    /**
     * Constructs an <code>ASTVariantList</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTVariant</code>s.
     */
    public ASTVariantList(Location location, List<ASTVariant> children) {
        super(location, children, Type.VARIANTS);
    }
}
