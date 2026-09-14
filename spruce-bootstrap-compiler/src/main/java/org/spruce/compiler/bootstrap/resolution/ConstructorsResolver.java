package org.spruce.compiler.bootstrap.resolution;

import java.util.Optional;

import org.spruce.compiler.bootstrap.ast.statements.ASTConstructorInvocation;
import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.scanner.TokenType;
import org.spruce.compiler.bootstrap.symbol.GlobalLookup;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;

import static org.spruce.compiler.bootstrap.scanner.TokenType.SELF;
import static org.spruce.compiler.bootstrap.scanner.TokenType.SUPER;

/**
 * A <code>ConstructorsResolver</code> is an <code>AbstractConstructorsResolver</code>
 * that resolves constructor invocations.
 */
public class ConstructorsResolver extends AbstractConstructorsResolver<ASTConstructorInvocation> {
    /**
     * Constructs a <code>ConstructorsResolver</code>.
     * @param resolver An <code>Resolver</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     * @param global The <code>GlobalLookup</code>.
     */
    public ConstructorsResolver(Resolver resolver, MessageProducer msgProducer, GlobalLookup global) {
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
     * 1. Find the "type to search" for the given <code>ConstructorInvocation</code>,
     *    if it exists.
     * @param constrInvocation An <code>ASTConstructorInvocation</code>.
     * @param ctx A <code>ResolutionContext</code>.
     * @return An <code>Optional&lt;TypeSymbol&gt;</code>.
     */
    public Optional<TypeSymbol> getTypeToSearch(ASTConstructorInvocation constrInvocation, ResolutionContext ctx) {
        TokenType tokenType = constrInvocation.getConstructorKeyword().getKeyword();
        TypesResolver typesResolver = getTypesResolver();
        TypeSymbol type = typesResolver.findEnclosingType(ctx.enclosingSymbol());
        if (tokenType == SELF) {
            // 1a. Self
            return Optional.of(type);
        }
        else if (tokenType == SUPER) {
            // 1b. Super
            Optional<TypeSymbol> optSuperclass = type.getSuperclass();
            if (optSuperclass.isEmpty()) {
                error(constrInvocation.getLocation(), "Type " + type.getName() + " has no superclass");
            }
            return optSuperclass;
        }
        else {
            throw internalError("expected self or super on constructor invocation!");
        }
    }
}
