package org.spruce.compiler.ast.classes;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTAdtBody</code> is a "{", followed by a variant list,
 * optionally followed by adt body declarations, followed by a "}".</p>
 *
 * <em>
 * AdtBody:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;{ VariantList [AdtBodyDeclarations] }
 * </em>
 */
public class ASTAdtBody extends ASTParentNode {
    private final ASTVariantList myVariantList;
    private final ASTInterfacePartList myBodyDecls;

    /**
     * Constructs an <code>ASTAdtBody</code> at the given <code>Location</code>
     * with the given <code>ASTVariantList</code> and the given
     * <code>ASTInterfacePartList</code>.
     * @param location The <code>Location</code>.
     * @param variantList An <code>ASTVariantList</code>.
     * @param bodyDecls An <code>ASTInterfacePartList</code>.
     */
    public ASTAdtBody(Location location, ASTVariantList variantList, ASTInterfacePartList bodyDecls) {
        super(location);
        myVariantList = variantList;
        myBodyDecls = bodyDecls;
    }

    /**
     * Returns an <code>ASTVariantList</code>.
     * @return An <code>ASTVariantList</code>.
     */
    public ASTVariantList getVariantList() {
        return myVariantList;
    }

    /**
     * Returns an <code>ASTInterfacePartList</code>.
     * @return An <code>ASTInterfacePartList</code>.
     */
    public ASTInterfacePartList getBodyDecls() {
        return myBodyDecls;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myVariantList, myBodyDecls);
    }
}
