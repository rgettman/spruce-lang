package org.spruce.compiler.ast.classes;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.ASTAnnotatedNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.types.ASTDataTypeNoArray;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTVariantType</code> is an DataTypeNoArray optionally preceded
 * by an AnnotationList.</p>
 *
 * <em>
 * VariantType:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] DataTypeNoArray
 * </em>
 */
public final class ASTVariantType extends ASTAnnotatedNode implements ASTVariant {
    private final ASTDataTypeNoArray myDtna;

    /**
     * Constructs an <code>ASTAnnotationDeclaration</code> at the given <code>Location</code>
     * with the given AnnotationList and DataTypeNoArray.
     * @param location The <code>Location</code>.
     * @param annList A possibly empty <code>ASTAnnotationList</code>.
     * @param dtna An <code>ASTDataTypeNoArray</code> representing the variant type.
     */
    public ASTVariantType(Location location, ASTAnnotationList annList, ASTDataTypeNoArray dtna) {
        super(location, annList);
        myDtna = dtna;
    }

    /**
     * Returns the <code>ASTDataTypeNoArray</code>.
     * @return The <code>ASTDataTypeNoArray</code>.
     */
    public ASTDataTypeNoArray getDtna() {
        return myDtna;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myAnnList, myDtna);
    }
}
