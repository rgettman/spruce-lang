package org.spruce.compiler.bootstrap.resolution;

import java.util.Optional;

import org.spruce.compiler.bootstrap.ast.expressions.*;
import org.spruce.compiler.bootstrap.ast.names.ASTTypeName;
import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.symbol.GlobalLookup;
import org.spruce.compiler.bootstrap.symbol.SymbolTable;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;

/**
 * A <code>CiceResolver</code> is an <code>AbstractConstructorsResolver</code>
 * that resolves class instance creation expressions.
 */
public class CiceResolver extends AbstractConstructorsResolver<ASTClassInstanceCreationExpression> {
    /**
     * Constructs a <code>CiceResolver</code>.
     * @param resolver An <code>Resolver</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     * @param global The <code>GlobalLookup</code>.
     */
    public CiceResolver(Resolver resolver, MessageProducer msgProducer, GlobalLookup global) {
        super(resolver, msgProducer, global);
    }

    /**
     * Interfaces are not allowed on the type to search.
     * @return <code>false</code>
     */
    public boolean isInterfaceAllowedOnTypeToSearch() {
        return false;
    }

    /**
     * 1. Find the "type to search" for the given <code>ClassInstanceCreationExpression</code>,
     *    if it exists.
     * @param cice An <code>ASTClassInstanceCreationExpression</code>.
     * @param ctx A <code>ResolutionContext</code>.
     * @return An <code>Optional&lt;TypeSymbol&gt;</code>.
     */
    public Optional<TypeSymbol> getTypeToSearch(ASTClassInstanceCreationExpression cice, ResolutionContext ctx) {
        Optional<ASTPrimary> optPrimary = cice.getPrimary();
        ASTUnqualifiedClassInstanceCreationExpression ucice = cice.getUcice();
        ASTTypeToInstantiate tti = ucice.getTti();

        if (optPrimary.isPresent()) {
            // 1a. Primary.new TypeToInstantiate(args)
            ASTPrimary primary = optPrimary.get();
            getExpressionsResolver().resolvePrimary(primary, ctx);

            Optional<TypeSymbol> optOuter = Optional.ofNullable(primary.getResolvedDataType());
            if (optOuter.isPresent()) {
                TypeSymbol outer = optOuter.get();
                resolveTypeToInstantiateInner(tti, outer);
                return Optional.ofNullable(tti.getResolvedDataType());
            }
            // Else there was an error resolving the primary.  Don't bother
            // resolving the rest.
        }
        else {
            // 1b. new TypeToInstantiate(args)
            getNamesResolver().resolveTypeName(tti.getTypeName(), ctx);
            tti.setResolvedDataType(tti.getTypeName().getResolvedDataType());
        }
        return Optional.ofNullable(tti.getResolvedDataType());
    }

    // 1a. Primary.new TypeToInstantiate(args)
    private void resolveTypeToInstantiateInner(ASTTypeToInstantiate tti, TypeSymbol outer) {
        SymbolTable table = outer.getTable();
        ASTTypeName tn = tti.getTypeName();
        if (tn.getTypedChildren().size() > 1) {
            error(tn.getLocation(), "Cannot have qualified inner class in type to instantiate.");
            return;
        }

        String simpleType = tn.getTypedChildren().get(0).getValue();
        if (table.containsType(simpleType)) {
            TypeSymbol resolved = (TypeSymbol) table.get(simpleType);
            tn.setResolvedDataType(resolved);
            tti.setResolvedDataType(resolved);
        }
        else {
            errorSymbolNotFound(tn.getLocation(), simpleType);
        }
    }
}
