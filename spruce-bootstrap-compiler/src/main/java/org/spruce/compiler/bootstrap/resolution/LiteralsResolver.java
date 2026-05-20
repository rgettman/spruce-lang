package org.spruce.compiler.bootstrap.resolution;

import org.spruce.compiler.bootstrap.ast.literals.*;
import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.symbol.GlobalLookup;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;

/**
 * A <code>LiteralsResolver</code> is a <code>BasicResolver</code> that resolves
 * all literals: integer, floating-point, character, string, boolean, and class,
 * but not class literals.
 */
public class LiteralsResolver extends BasicResolver {
    /**
     * Constructs a <code>LiteralsResolver</code>.
     * @param resolver An <code>Resolver</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     * @param global The <code>GlobalLookup</code>.
     */
    public LiteralsResolver(Resolver resolver, MessageProducer msgProducer, GlobalLookup global) {
        super(resolver, msgProducer, global);
    }

    /**
     * Resolves the symbols in a <code>Literal</code>.
     * @param literal An <code>ASTLiteral</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveLiteral(ASTLiteral literal, ResolutionContext ctx) {
        switch (literal) {
        case ASTBooleanLiteral boolLiteral -> resolveBooleanLiteral(boolLiteral, ctx);
        case ASTCharacterLiteral charLiteral -> resolveCharacterLiteral(charLiteral, ctx);
        case ASTFloatingPointLiteral fltPtLiteral -> resolveFloatingPointLiteral(fltPtLiteral, ctx);
        case ASTIntegerLiteral intLiteral -> resolveIntegerLiteral(intLiteral, ctx);
        case ASTStringLiteral strLiteral -> resolveStringLiteral(strLiteral, ctx);
        }
    }

    /**
     * Resolves a boolean literal.
     * @param literal An <code>ASTBooleanLiteral</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveBooleanLiteral(ASTBooleanLiteral literal, ResolutionContext ctx) {
        TypeSymbol symbol = getTypesResolver().resolveBuiltInDataTypeByName("Boolean", ctx);
        literal.setResolvedDataType(symbol);
    }

    /**
     * Resolves a character literal.
     * @param literal An <code>ASTCharacterLiteral</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveCharacterLiteral(ASTCharacterLiteral literal, ResolutionContext ctx) {
        TypeSymbol symbol = getTypesResolver().resolveBuiltInDataTypeByName("Character", ctx);
        literal.setResolvedDataType(symbol);
    }

    /**
     * Resolves a floating-point literal.
     * @param literal An <code>ASTFloatingPointLiteral</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveFloatingPointLiteral(ASTFloatingPointLiteral literal, ResolutionContext ctx) {
        TypeSymbol symbol = getTypesResolver().resolveBuiltInDataTypeByName("Double", ctx);
        literal.setResolvedDataType(symbol);
    }

    /**
     * Resolves an integer literal.
     * @param literal An <code>ASTIntegerLiteral</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveIntegerLiteral(ASTIntegerLiteral literal, ResolutionContext ctx) {
        TypeSymbol symbol = getTypesResolver().resolveBuiltInDataTypeByName("Integer", ctx);
        literal.setResolvedDataType(symbol);
    }

    /**
     * Resolves a string literal.
     * @param literal An <code>ASTStringLiteral</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveStringLiteral(ASTStringLiteral literal, ResolutionContext ctx) {
        TypeSymbol symbol = getTypesResolver().resolveBuiltInDataTypeByName("String", ctx);
        literal.setResolvedDataType(symbol);
    }
}
