package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTGeneralModifier</code> is "abstract", "final", "override",
 * "shared", or "strictfp".</p>
 *
 * <p>Access modifiers "public", "protected", "internal", and "private" are in
 * a separate production, "AccessModifier".</p>
 *
 * <p>The keyword "synchronized" will NOT apply to methods, only synchronized
 * statements, which will take only Lockable instances.</p>
 *
 * <p>The keyword "native" may be applied later if it makes sense to include
 * that feature.</p>
 *
 * <p>The keyword "const" may be applied to the return type if seen before the
 * method name; after the parameter list it would be the "ConstModifier".</p>
 *
 * <p>Conversion to more specific modifier lists will involve ensuring that
 * some modifiers aren't present in the list.</p>
 *
 * <em>
 * MethodModifier:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;abstract<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;const<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;final<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;override<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;shared<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;strictfp<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;transient<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;volatile
 * </em>
 */
public class ASTGeneralModifier extends ASTParentNode
{
    /**
     * Constructs an <code>ASTGeneralModifier</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTGeneralModifier(Location location, List<ASTNode> children)
    {
        super(location, children);
    }

    /**
     * This node is collapsible.
     * @return <code>true</code>.
     */
    @Override
    public boolean isCollapsible()
    {
        return true;
    }
}
