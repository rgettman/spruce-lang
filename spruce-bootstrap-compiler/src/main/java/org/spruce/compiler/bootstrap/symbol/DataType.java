package org.spruce.compiler.bootstrap.symbol;

/**
 * A <code>DataType</code> represents the datatype of a declared
 * <code>Symbol</code>.
 * @param namespace The namespace in which the datatype is declared.
 * @param typeName The name of the datatype.
 */
public record DataType(String namespace, String typeName) {
    /**
     * Used for symbols for non-variables.
     */
    public static final DataType NONE = new DataType("(none)", "(none)");
}
