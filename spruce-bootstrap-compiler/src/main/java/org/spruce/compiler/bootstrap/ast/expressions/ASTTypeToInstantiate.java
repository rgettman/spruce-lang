package org.spruce.compiler.bootstrap.ast.expressions;

import java.util.ArrayList;
import java.util.List;

import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.symbol.DataTypeResolution;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.ast.names.ASTTypeName;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;

/**
 * <p>An <code>ASTTypeToInstantiate</code> is a TypeName.
 * <em>
 * TypeToInstantiate:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;TypeName
 * </em>
 */
public class ASTTypeToInstantiate extends ASTParentNode implements DataTypeResolution {
    private final ASTTypeName myTypeName;
    private TypeSymbol myResolvedSymbol;

    /**
     * Constructs an <code>ASTTypeToInstantiate</code> given a type name.
     * @param location A <code>Location</code>.
     * @param typeName An <code>ASTTypeName</code>.
     */
    public ASTTypeToInstantiate(Location location, ASTTypeName typeName) {
        super(location);
        myTypeName = typeName;
    }

    /**
     * Returns an <code>ASTTypeName</code>.
     * @return An <code>ASTTypeName</code>.
     */
    public ASTTypeName getTypeName() {
        return myTypeName;
    }

    @Override
    public void setResolvedDataType(TypeSymbol symbol) {
        myResolvedSymbol = symbol;
    }

    @Override
    public TypeSymbol getResolvedDataType() {
        return myResolvedSymbol;
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(1);
        children.add(myTypeName);
        return children;
    }
}
