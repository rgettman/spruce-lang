package org.spruce.compiler.bootstrap.ast.expressions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.spruce.compiler.bootstrap.ast.ASTParentNode;
import org.spruce.compiler.bootstrap.ast.Node;
import org.spruce.compiler.bootstrap.ast.types.ASTIntersectionType;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.symbol.MultDataTypeResolution;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;

/**
 * An <code>ASTCastExpression</code> is an Expression followed by "as",
 * followed by an IntersectionType.
 * <em>
 * CastExpression:
 * &nbsp;&nbsp;&nbsp;&nbsp;Expression as IntersectionType
 * </em>
 */
public final class ASTCastExpression extends ASTParentNode implements ASTValueExpression, MultDataTypeResolution {
    private final ASTExpression myExpr;
    private final ASTIntersectionType myIntersectionType;
    private TypeSymbol myFirstDataType;
    private final Map<String, TypeSymbol> myResolvedDataTypes;

    /**
     * Constructs an <code>ASTCastExpression</code> at the given <code>Location</code>
     * with the given <code>ASTExpression</code> and the given
     * <code>ASTIntersectionType</code>.
     * @param location The <code>Location</code>.
     * @param expr An <code>ASTExpression</code>.
     * @param intersectionType An <code>ASTIntersectionType</code>.
     */
    public ASTCastExpression(Location location, ASTExpression expr, ASTIntersectionType intersectionType) {
        super(location);
        myExpr = expr;
        myIntersectionType = intersectionType;
        myResolvedDataTypes = new HashMap<>();
    }

    /**
     * Returns an <code>ASTExpression</code>.
     * @return An <code>ASTExpression</code>.
     */
    public ASTExpression getExpr() {
        return myExpr;
    }

    /**
     * Returns an <code>ASTIntersectionType</code>.
     * @return An <code>ASTIntersectionType</code>.
     */
    public ASTIntersectionType getIntersectionType() {
        return myIntersectionType;
    }

    @Override
    public void setResolvedDataType(TypeSymbol symbol) {
        addResolvedDataType(symbol);
    }

    @Override
    public TypeSymbol getResolvedDataType() {
        return myFirstDataType;
    }

    @Override
    public void addResolvedDataType(TypeSymbol symbol) {
        if (myResolvedDataTypes.isEmpty()) {
            myFirstDataType = symbol;
        }
        myResolvedDataTypes.put(symbol.getName(), symbol);
    }

    @Override
    public Optional<TypeSymbol> getResolvedDataType(String name) {
        return Optional.ofNullable(myResolvedDataTypes.get(name));
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(myExpr, myIntersectionType);
    }
}
