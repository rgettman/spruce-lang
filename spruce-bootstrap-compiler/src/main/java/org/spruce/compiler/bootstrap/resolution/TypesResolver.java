package org.spruce.compiler.bootstrap.resolution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.spruce.compiler.bootstrap.ast.names.ASTIdentifier;
import org.spruce.compiler.bootstrap.ast.types.*;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.symbol.ChildSymbolTable;
import org.spruce.compiler.bootstrap.symbol.ParentSymbol;
import org.spruce.compiler.bootstrap.symbol.Symbol;
import org.spruce.compiler.bootstrap.symbol.SymbolTable;
import org.spruce.compiler.bootstrap.symbol.GlobalLookup;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;

/**
 * A <code>TypesResolver</code> is a <code>BasicResolver</code> that resolves
 * symbols that refer to declared symbols: Data Types of superclasses, fields,
 * parameters, methods, method return types, and variables.  It has many entry
 * points for resolving data type-like symbols.
 */
public class TypesResolver extends BasicResolver {
    /**
     * The name of the root type in the Spruce object hierarchy!
     */
    public static final String ROOT_TYPE = "Any";

    /**
     * The name of the boolean type.
     */
    public static final String BOOLEAN_TYPE = "Boolean";
    /**
     * The name of the character type.
     */
    public static final String CHARACTER_TYPE = "Character";
    /**
     * The name of the double floating-point type.
     */
    public static final String DOUBLE_TYPE = "Double";
    /**
     * The name of the integer type.
     */
    public static final String INTEGER_TYPE = "Integer";
    /**
     * The name of the string type.
     */
    public static final String STRING_TYPE = "String";

    private final Map<String, TypeSymbol> builtInTypes;

    /**
     * Constructs a <code>TypesResolver</code>.
     * @param resolver An <code>Resolver</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     * @param global The <code>GlobalLookup</code>.
     */
    public TypesResolver(Resolver resolver, MessageProducer msgProducer, GlobalLookup global) {
        super(resolver, msgProducer, global);
        builtInTypes = new HashMap<>();
    }

    /**
     * Resolves symbols in an <code>IntersectionType</code>.
     * @param intersectionType An <code>ASTIntersectionType</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveIntersectionType(ASTIntersectionType intersectionType, ResolutionContext ctx) {
        for (ASTDataType dt : intersectionType.getTypedChildren()) {
            resolveDataType(dt, ctx);
        }
    }

    /**
     * Resolve a <code>DataType</code>.  Sets the symbol found or produces an error.
     * @param dt An <code>ASTDataType</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveDataType(ASTDataType dt, ResolutionContext ctx) {
        ASTBaseDataType bdt = dt.getBaseDataType();
        resolveBaseDataType(bdt, ctx);
        dt.setResolvedDataType(bdt.getResolvedDataType());
    }

    /**
     * Resolve a <code>BaseDataType</code>.  Sets the symbol found or produces
     * an error.
     * @param bdt An <code>ASTBaseDataType</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveBaseDataType(ASTBaseDataType bdt, ResolutionContext ctx) {
        // The only subtype of BaseDataType is DataTypeNoArray.
        switch(bdt) {
        case ASTDataTypeNoArray dtna -> resolveDataTypeNoArray(dtna, ctx);
        }
    }

    /**
     * Resolve a <code>DataTypeNoArray</code>.  Sets the symbol found or
     * produces an error.
     * @param dtna An <code>ASTDataTypeNoArray</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveDataTypeNoArray(ASTDataTypeNoArray dtna, ResolutionContext ctx) {
        // The parser ensures at least one simple type.
        List<ASTIdentifier> ids = dtna.getTypedChildren().stream()
                .map(ASTSimpleType::getName)
                .toList();
        resolveIdentifiers(ids, ctx).ifPresent(dtna::setResolvedDataType);
    }

    /**
     * Resolves a list of identifiers.
     * @param ids A <code>List</code> of <code>ASTIdentifier</code>s.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public Optional<TypeSymbol> resolveIdentifiers(List<ASTIdentifier> ids, ResolutionContext ctx) {
        // 1. If just 1 simple type, resolve the identifier as a type.
        if (ids.size() == 1) {
            return resolveIdentifier(ids.get(0), ctx);
        }
        // 2. If there are multiple simple types (qualified type name), then
        //    resolve the first identifier as a namespace or a type name.
        //    Then subsequent identifiers are resolved as children of the
        //    previous symbol.
        else {
            Optional<ParentSymbol> optNorT = resolveNamespaceOrType(ids.get(0), ctx);
            if (optNorT.isPresent()) {
                return resolveRestOfQualifiedType(ids, optNorT.get());
            }
        }
        return Optional.empty();
    }

    private Optional<TypeSymbol> resolveIdentifier(ASTIdentifier id, ResolutionContext ctx) {
        String name = id.getValue();
        // This does NOT use the global symbol table.

        // 1. Find an ancestor, starting with the parent, to find the name as a
        //    direct child of the ancestor.  Include the first namespace found,
        //    but not any namespace further up.
        Optional<TypeSymbol> optResolved = resolveChildOfAncestor(name, ctx.enclosingSymbol());
        if (optResolved.isPresent()) {
            return optResolved;
        }

        // 2. If not found, check the use declaration symbols (types) that
        //    match a type directly.
        optResolved = resolveUsedType(name, ctx.using());
        if (optResolved.isPresent()) {
            return optResolved;
        }

        // 3. If not found, check all use-all declaration symbols (namespaces),
        //    looking for a matching child.
        optResolved = resolveUseAllType(name, ctx.using());
        if (optResolved.isPresent()) {
            return optResolved;
        }

        // 4. If not found, create an unresolved error.
        errorSymbolNotFound(id.getLocation(), name);
        return Optional.empty();
    }

    private Optional<TypeSymbol> resolveChildOfAncestor(String name, ParentSymbol parent) {
        boolean namespaceFound = false;
        while (!namespaceFound) {
            SymbolTable table = parent.getTable();
            if (table.containsType(name)) {
                TypeSymbol resolved = (TypeSymbol) table.get(name);
                return Optional.of(resolved);
            }

            if (parent.getKind() == Symbol.Kind.NAMESPACE) {
                namespaceFound = true;
            }
            else {
                parent = ((ChildSymbolTable) parent.getParent()).getParent();
            }
        }
        return Optional.empty();
    }

    /**
     * Resolves a name against any use-type declarations.
     * @param name A string name to resolve.
     * @param using A <code>Map</code> of names to <code>ParentSymbol</code>s
     *              representing use declarations.
     * @return An <code>Optional&lt;TypeSymbol&gt;</code>.
     */
    public Optional<TypeSymbol> resolveUsedType(String name, Map<String, ParentSymbol> using) {
        if (using.containsKey(name)) {
            ParentSymbol resolved = using.get(name);
            if (resolved.getKind().isType()) {
                return Optional.of((TypeSymbol) resolved);
            }
        }
        return Optional.empty();
    }

    /**
     * Resolves a name against any use-all declarations.
     * @param name A string name to resolve.
     * @param using A <code>Map</code> of names to <code>ParentSymbol</code>s
     *              representing use declarations.
     * @return An <code>Optional&lt;TypeSymbol&gt;</code>.
     */
    public Optional<TypeSymbol> resolveUseAllType(String name, Map<String, ParentSymbol> using) {
        for (ParentSymbol parent : using.values()) {
            if (parent.getKind() == Symbol.Kind.NAMESPACE) {
                SymbolTable table = parent.getTable();
                if (table.containsType(name)) {
                    return Optional.of((TypeSymbol) table.get(name));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Resolves a namespace or a type name based on the given first identifier,
     * if it exists. This is used as the first step in resolving a qualified
     * name, either a datatype or an expression name.
     * @param id An <code>ASTIdentifier</code>.
     * @param ctx A <code>ResolutionContext</code>.
     * @return An <code>Optional&lt;ParentSymbol&gt;</code>.
     */
    public Optional<ParentSymbol> resolveNamespaceOrType(ASTIdentifier id, ResolutionContext ctx) {
        String name = id.getValue();

        // 1. Find an ancestor, starting with the parent, to find the name as a
        //    direct child of the ancestor.  Include the first namespace found,
        //    but not any namespace further up.  Must be a type if found this way.
        Optional<TypeSymbol> optResolved = resolveChildOfAncestor(name, ctx.enclosingSymbol());
        if (optResolved.isPresent()) {
            return optResolved.map(ts -> ts);
        }

        // 2. If not found, check the use declaration symbols (types) that
        //    match a type directly.  Must be a type if found this way.
        optResolved = resolveUsedType(name, ctx.using());
        if (optResolved.isPresent()) {
            return optResolved.map(ts -> ts);
        }

        // 3. If not found, check all use-all declaration symbols (namespaces),
        //    looking for a matching child.  Must be a type if found this way.
        optResolved = resolveUseAllType(name, ctx.using());
        if (optResolved.isPresent()) {
            return optResolved.map(ts -> ts);
        }

        // 4. If not found, check the global symbol table for a direct child,
        //    which must be a namespace.
        Optional<ParentSymbol> optNamespace = getGlobalLookup().getNamespace(name);
        if (optNamespace.isPresent()) {
            return optNamespace;
        }

        // 5. If not found, create an unresolved error.
        errorSymbolNotFound(id.getLocation(), name);
        return Optional.empty();
    }

    private Optional<TypeSymbol> resolveRestOfQualifiedType(List<ASTIdentifier> ids, ParentSymbol first) {
        // The first symbol "first" has already been resolved.
        ParentSymbol resolved = first;

        // Each subsequent identifier must resolve to a child symbol of the
        // previously resolved symbol.
        for (int i = 1; i < ids.size(); i++) {
            ASTIdentifier id = ids.get(i);
            String name = id.getValue();
            SymbolTable table = resolved.getTable();
            if (table.containsNamespaceOrType(name)) {
                resolved = (ParentSymbol) table.get(name);
            }
            else {
                errorSymbolNotFound(id.getLocation(), name);
                return Optional.empty();
            }
        }

        // Ensure it's a type here at the end.
        if (resolved.getKind().isType()) {
            return Optional.of((TypeSymbol) resolved);
        }
        else {
            error(resolved.getLocation(), "Type expected.");
            return Optional.empty();
        }
    }

    /**
     * Resolves a built-in data type for the root of the object hierarchy.
     * @param ctx A <code>ResolutionContext</code>.
     * @return A <code>TypeSymbol</code> for the resolved built-in data type
     *         for the root of the object hierarchy.
     */
    public TypeSymbol resolveRootType(ResolutionContext ctx) {
        return resolveBuiltInDataTypeByName(ROOT_TYPE, ctx);
    }

    /**
     * Resolve a built-in data type by simple name.
     * @param simpleName The simple name of the built-in data type.
     * @param ctx A <code>ResolutionContext</code>.
     * @return A <code>TypeSymbol</code> for the resolved built-in data type
     *         for the given simple name.
     */
    public TypeSymbol resolveBuiltInDataTypeByName(String simpleName, ResolutionContext ctx) {
        // Lazy cache of built-in type names, including literals.
        if (builtInTypes.containsKey(simpleName)) {
            return builtInTypes.get(simpleName);
        }
        else {
            Location loc = new Location("<builtin>", 0, 0, simpleName);
            List<ASTIdentifier> ids = List.of(
                    new ASTIdentifier(loc, "spruce"),
                    new ASTIdentifier(loc, "lang"),
                    new ASTIdentifier(loc, simpleName)
            );
            List<ASTSimpleType> simpleTypes = ids.stream()
                    .map(id -> new ASTSimpleType(loc, id))
                    .toList();
            ASTDataTypeNoArray dtna = new ASTDataTypeNoArray(loc, simpleTypes);
            ASTDataType dt = new ASTDataType(loc, dtna);
            resolveDataType(dt, ctx);
            Optional<TypeSymbol> optResolved = Optional.ofNullable(dt.getResolvedDataType());
            if (optResolved.isPresent()) {
                TypeSymbol resolved = optResolved.get();
                builtInTypes.put(simpleName, resolved);
                return resolved;
            }
            else {
                throw internalError("Can't find built-in type " + simpleName + "!");
            }
        }
    }

    /**
     * Returns whether the given <code>TypeSymbol</code> is the symbol for the
     * built-in <code>Boolean</code> type.
     * @param symbol A <code>TypeSymbol</code>.
     * @param ctx A <code>ResolutionContext</code>.
     * @return Whether it is the <code>Boolean</code> type.
     */
    public boolean isBoolean(TypeSymbol symbol, ResolutionContext ctx) {
        TypeSymbol bool = resolveBuiltInDataTypeByName(BOOLEAN_TYPE, ctx);
        return symbol == bool;
    }

    /**
     * Returns whether the given <code>TypeSymbol</code> is any of the symbols
     * for a built-in numeric type: <code>Integer</code>, <code>Double</code>,
     * or <code>Character</code>.
     * @param symbol A <code>TypeSymbol</code>.
     * @param ctx A <code>ResolutionContext</code>.
     * @return Whether it is a numeric type.
     */
    public boolean isNumeric(TypeSymbol symbol, ResolutionContext ctx) {
        TypeSymbol integer = resolveBuiltInDataTypeByName(INTEGER_TYPE, ctx);
        TypeSymbol duble = resolveBuiltInDataTypeByName(DOUBLE_TYPE, ctx);
        TypeSymbol character = resolveBuiltInDataTypeByName(CHARACTER_TYPE, ctx);
        return symbol == integer || symbol == duble || symbol == character;
    }

    /**
     * Returns whether the given <code>TypeSymbol</code> is the symbol for the
     * built-in <code>String</code> type.
     * @param symbol A <code>TypeSymbol</code>.
     * @param ctx A <code>ResolutionContext</code>.
     * @return Whether it is the <code>String</code> type.
     */
    public boolean isString(TypeSymbol symbol, ResolutionContext ctx) {
        TypeSymbol string = resolveBuiltInDataTypeByName(STRING_TYPE, ctx);
        return symbol == string;
    }

    /**
     * Determines the enclosing <code>TypeSymbol</code> for the given
     * <code>Symbol</code>.
     * @param symbol Find the enclosing type for this <code>Symbol</code>.
     * @return A <code>TypeSymbol</code> representing the enclosing type for
     *         the given <code>Symbol</code>.
     */
    public TypeSymbol findEnclosingType(Symbol symbol) {
        SymbolTable table = symbol.getParent();
        if (!(table instanceof ChildSymbolTable)) {
            throw internalError("Enclosing SymbolTable was not a ChildSymbolTable! " + symbol);
        }
        ParentSymbol ancestor = ((ChildSymbolTable) symbol.getParent()).getParent();
        while (!ancestor.getKind().isType()) {
            table = ancestor.getParent();
            if (!(table instanceof ChildSymbolTable)) {
                throw internalError("Enclosing SymbolTable was not a ChildSymbolTable! " + symbol);
            }
            ancestor = ((ChildSymbolTable) symbol.getParent()).getParent();
        }
        return (TypeSymbol) ancestor;
    }

    /**
     * Returns whether the given <code>ParentSymbol</code> is an enclosing
     * type, direct or indirect, for the given <code>Symbol</code>.
     * @param type A <code>ParentSymbol</code> representing a resolved type name.
     * @param symbol Determine whether the <codeParentSymbol</code> is an
     *               enclosing type for this <code>Symbol</code>.
     * @return Whether the given <code>ParentSymbol</code> is an enclosing
     *         type, direct or indirect, for the given <code>Symbol</code>.
     */
    public boolean isEnclosingType(ParentSymbol type, Symbol symbol) {
        ParentSymbol ancestor = findEnclosingType(symbol);

        boolean namespaceFound = false;
        while (!namespaceFound) {
            if (ancestor == type) {
                return true;
            }

            if (ancestor.getKind() == Symbol.Kind.NAMESPACE) {
                namespaceFound = true;
            }
            else {
                ancestor = ((ChildSymbolTable) ancestor.getParent()).getParent();
            }
        }
        return false;
    }
}
