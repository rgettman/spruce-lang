package org.spruce.compiler.symbol;

import java.util.stream.Collectors;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.types.*;
import org.spruce.compiler.common.MessageProducer;

/**
 * A <code>TypesSymbolCreator</code> is a <code>BasicSymbolCreator</code>
 * that creates symbol names belonging to types level AST elements.
 */
public class TypesSymbolCreator extends BasicSymbolCreator {
    /**
     * Constructs a <code>TypesSymbolCreator</code>.
     * @param symbolCreator A <code>SymbolCreator</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     */
    public TypesSymbolCreator(SymbolCreator symbolCreator, MessageProducer msgProducer) {
        super(symbolCreator, msgProducer);
    }

    /**
     * Returns the symbol name for a <code>DataType</code>.
     * @param dataType An <code>ASTDataType</code>.
     * @return The name for the internal <code>BaseDataType</code> with a
     *     suffix for the suffix operator if it exists.
     */
    public String getNameForDataType(ASTDataType dataType) {
        StringBuilder buf = new StringBuilder(getNameForBaseDataType(dataType.getBaseDataType()));
        if (dataType.getSuffixOperator().isPresent()) {
            buf.append(dataType.getSuffixOperator().get().getKeyword().getRepresentation());
        }
        return buf.toString();
    }

    /**
     * Returns the symbol name for a <code>BaseDataType</code>.
     * @param bdt An <code>ASTBaseDataType</code>.
     * @return The name for whether it's an <code>ArrayType</code> or a
     *      <code>DataTypeNoArray</code>.
     */
    public String getNameForBaseDataType(ASTBaseDataType bdt) {
        return switch(bdt) {
            case ASTArrayType arrayType -> getNameForArrayType(arrayType);
            case ASTDataTypeNoArray dtna -> getNameForDataTypeNoArray(dtna);
        };
    }

    /**
     * Returns the symbol name for an <code>ArrayType</code>.
     * @param arrayType An <code>ASTArrayType</code>.
     * @return The symbol name for the internal <code>DataTypeNoArray</code>
     *     with "[]" appended for <code>Dims</code> found.
     */
    public String getNameForArrayType(ASTArrayType arrayType) {
        StringBuilder buf = new StringBuilder(getNameForDataTypeNoArray(arrayType.getDataTypeNoArray()));
        for (ASTKeywordNode ignored : arrayType.getDims().getTypedChildren()) {
            buf.append("[]");
        }
        return buf.toString();
    }

    /**
     * Returns a symbol name for a <code>DataTypeNoArray</code>.
     * @param dtna An <code>ASTDataTypeNoArray</code>.
     * @return A dot-separated string of simple type symbol names.
     */
    public String getNameForDataTypeNoArray(ASTDataTypeNoArray dtna) {
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
        String name = simpleType.getName().getValue();
        if (simpleType.getTypeArgs().isPresent()) {
            name += getNameForTypeArgumentList(simpleType.getTypeArgs().get());
        }
        return name;
    }

    /**
     * Returns a symbol name for a <code>TypeArgumentList</code>.
     * @param typeArgList An <code>ASTTypeArgumentList</code>.
     * @return A comma-separated list of <code>TypeArgument</code> symbol names.
     */
    public String getNameForTypeArgumentList(ASTTypeArgumentList typeArgList) {
        return typeArgList.getTypedChildren().stream()
                .map(this::getNameForTypeArgument)
                .collect(Collectors.joining(", ", "<", ">"));
    }

    /**
     * Returns a symbol name for a <code>TypeArgument</code>.
     * @param typeArg An <code>ASTTypeArgument</code>.
     * @return The name for whether it's a <code>TypeArgumentBounds</code> or a
     *     <code>Wildcard</code>.
     */
    public String getNameForTypeArgument(ASTTypeArgument typeArg) {
        return switch (typeArg) {
            case ASTTypeArgumentBounds tab -> getNameForTypeArgumentBounds(tab);
            case ASTWildcard ignored -> "_";
        };
    }

    /**
     * Creates a symbol name for a <code>TypeArgumentBounds</code>.
     * @param tab An <code>ASTTypeArgumentBounds</code>.
     * @return The internal data type name possibly prefixed with a generic
     *     modifier, <code>in</code> or <code>out</code>.
     */
    public String getNameForTypeArgumentBounds(ASTTypeArgumentBounds tab) {
        StringBuilder buf = new StringBuilder();
        if (tab.getGenericModifier().isPresent()) {
            buf.append(tab.getGenericModifier().get().getKeyword().getRepresentation()).append(" ");
        }
        buf.append(getNameForDataType(tab.getDataType()));
        return buf.toString();
    }
}
