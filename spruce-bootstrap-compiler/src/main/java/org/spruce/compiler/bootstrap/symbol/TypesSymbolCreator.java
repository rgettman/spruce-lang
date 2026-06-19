package org.spruce.compiler.bootstrap.symbol;

import java.util.Optional;
import java.util.stream.Collectors;

import org.spruce.compiler.bootstrap.ast.types.*;
import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.resolution.ResolutionContext;
import org.spruce.compiler.bootstrap.resolution.Resolver;
import org.spruce.compiler.bootstrap.resolution.TypesResolver;

/**
 * A <code>TypesSymbolCreator</code> is a <code>BasicSymbolCreator</code>
 * that creates symbol names belonging to types level AST elements.
 */
public class TypesSymbolCreator extends BasicSymbolCreator {
    /**
     * Constructs a <code>TypesSymbolCreator</code>.
     * @param symbolCreator A <code>SymbolCreator</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     * @param globalLookup A <code>GlobalLookup</code>.
     */
    public TypesSymbolCreator(SymbolCreator symbolCreator, MessageProducer msgProducer, GlobalLookup globalLookup) {
        super(symbolCreator, msgProducer, globalLookup);
    }

    /**
     * Performs early resolution of the given <code>DataType</code>.
     * Returns the symbol name for a <code>DataType</code>.
     * @param dataType An <code>ASTDataType</code>.
     * @param ctx A <code>ResolutionContext</code> for early resolution.
     * @return The name for the internal <code>BaseDataType</code> with a
     *     suffix for the suffix operator if it exists.
     */
    public String getNameForDataType(ASTDataType dataType, ResolutionContext ctx) {
        StringBuilder buf = new StringBuilder(getNameForBaseDataType(dataType.getBaseDataType(), ctx));
        if (dataType.getSuffixOperator().isPresent()) {
            buf.append(dataType.getSuffixOperator().get().getKeyword().getRepresentation());
        }
        // Suffix operators will eventually change this, but for now, no change.
        dataType.setResolvedDataType(dataType.getBaseDataType().getResolvedDataType());
        return buf.toString();
    }

    /**
     * Performs early resolution of the given <code>BaseDataType</code>.
     * Returns the symbol name for a <code>BaseDataType</code>.
     * @param bdt An <code>ASTBaseDataType</code>.
     * @param ctx A <code>ResolutionContext</code> for early resolution.
     * @return The name for whether it's an <code>ArrayType</code> or a
     *      <code>DataTypeNoArray</code>.
     */
    public String getNameForBaseDataType(ASTBaseDataType bdt, ResolutionContext ctx) {
        return switch(bdt) {
            case ASTDataTypeNoArray dtna -> getNameForDataTypeNoArray(dtna, ctx);
        };
    }

    /**
     * Performs early resolution of the given <code>DataTypeNoArray</code>.
     * Returns a symbol name for a <code>DataTypeNoArray</code>.
     * @param dtna An <code>ASTDataTypeNoArray</code>.
     * @param ctx A <code>ResolutionContext</code> for early resolution.
     * @return A dot-separated string of simple type symbol names.
     */
    public String getNameForDataTypeNoArray(ASTDataTypeNoArray dtna, ResolutionContext ctx) {
        Resolver resolver = getEarlyResolver();
        TypesResolver typesResolver = resolver.getTypesResolver();
        typesResolver.resolveDataTypeNoArray(dtna, ctx);
        Optional<TypeSymbol> optResolved = Optional.ofNullable(dtna.getResolvedDataType());
        if (optResolved.isPresent()) {
            TypeSymbol resolved = optResolved.get();
            return resolved.getFullyQualifiedName();
        }
        // Not resolved, bummer.  Fallback to names of simple types in an
        // attempt to continue compilation.
        return dtna.getTypedChildren().stream()
                .map(this::getNameForSimpleType)
                .collect(Collectors.joining("."));
    }

    /**
     * Returns a symbol name for a <code>SimpleType</code>.
     * @param simpleType An <code>ASTSimpleType</code>.
     * @return The identifier name that may have type arguments appended.
     */
    public String getNameForSimpleType(ASTSimpleType simpleType) {
        return simpleType.getName().getValue();
    }
}
