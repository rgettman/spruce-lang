package org.spruce.compiler.bootstrap.symbol;

import java.util.stream.Collectors;

import org.spruce.compiler.bootstrap.ast.types.*;
import org.spruce.compiler.bootstrap.common.MessageProducer;

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
            case ASTDataTypeNoArray dtna -> getNameForDataTypeNoArray(dtna);
        };
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
        return simpleType.getName().getValue();
    }
}
