package org.spruce.compiler.bootstrap.resolution;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.spruce.compiler.bootstrap.ast.names.ASTIdentifier;
import org.spruce.compiler.bootstrap.ast.types.ASTBaseDataType;
import org.spruce.compiler.bootstrap.ast.types.ASTDataType;
import org.spruce.compiler.bootstrap.ast.types.ASTDataTypeNoArray;
import org.spruce.compiler.bootstrap.ast.types.ASTSimpleType;
import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.symbol.ChildSymbolTable;
import org.spruce.compiler.bootstrap.symbol.ParentSymbol;
import org.spruce.compiler.bootstrap.symbol.Symbol;
import org.spruce.compiler.bootstrap.symbol.SymbolTable;
import org.spruce.compiler.bootstrap.symbol.TypeLookup;

/**
 * A <code>TypesResolver</code> is a <code>BasicResolver</code> that resolves
 * symbols that refer to declared symbols: Data Types of superclasses, fields,
 * parameters, methods, method return types, and variables.  It has many entry
 * points for resolving symbols of various kinds.
 */
public class TypesResolver extends BasicResolver {
    /**
     * Constructs a <code>TypesResolver</code>.
     * @param resolver An <code>Resolver</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     * @param global The global <code>TypeLookup</code>.
     */
    public TypesResolver(Resolver resolver, MessageProducer msgProducer, TypeLookup global) {
        super(resolver, msgProducer, global);
    }

    /**
     * Resolve a <code>DataType</code>.  Sets the symbol found or produces an error.
     * @param dt An <code>ASTDataType</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveDataType(ASTDataType dt, ResolutionContext ctx) {
        ASTBaseDataType bdt = dt.getBaseDataType();
        resolveBaseDataType(bdt, ctx);
        dt.setResolvedSymbol(bdt.getResolvedSymbol());
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
        // Determine what beside the global symbol table these methods need.
        // At least the enclosing namespace symbol.
        // Make it available in a context object for stability of methods that
        // need to pass this information.

        // The parser ensures at least one simple type.
        List<ASTSimpleType> simpleTypes = dtna.getTypedChildren();

        // 1. If just 1 simple type, resolve the identifier as a type.
        if (simpleTypes.size() == 1) {
            resolveSimpleType(simpleTypes.get(0).getName(), ctx)
                    .ifPresent(dtna::setResolvedSymbol);
        }
        // 2. If there are multiple simple types (qualified type name), then
        //    resolve the first identifier as a namespace or a type name.
        //    Then subsequent identifiers are resolved as children of the
        //    previous symbol.
        else {
            Optional<ParentSymbol> optResolved = resolveNamespaceOrType(simpleTypes.get(0).getName(), ctx);
            if (optResolved.isPresent()) {
                optResolved = resolveRestOfQualifiedType(simpleTypes, optResolved.get());
                optResolved.ifPresent(dtna::setResolvedSymbol);
            }
        }
    }

    private Optional<ParentSymbol> resolveSimpleType(ASTIdentifier id, ResolutionContext ctx) {
        String name = id.getValue();
        // This does NOT use the global symbol table.

        // 1. Find an ancestor, starting with the parent, to find the name as a
        //    direct child of the ancestor.  Include the first namespace found,
        //    but not any namespace further up.
        Optional<ParentSymbol> optResolved = resolveChildOfAncestor(name, ctx.enclosingSymbol());
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
        error(id.getLocation(), "Symbol " + name + " not found.");
        return Optional.empty();
    }

    private Optional<ParentSymbol> resolveChildOfAncestor(String name, ParentSymbol parent) {
        boolean namespaceFound = false;
        while (!namespaceFound) {
            SymbolTable table = parent.getTable();
            if (table.containsType(name)) {
                ParentSymbol resolved = (ParentSymbol) table.get(name);
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

    private Optional<ParentSymbol> resolveUsedType(String name, Map<String, ParentSymbol> using) {
        if (using.containsKey(name)) {
            ParentSymbol resolved = using.get(name);
            if (resolved.getKind().isType()) {
                return Optional.of(resolved);
            }
        }
        return Optional.empty();
    }

    private Optional<ParentSymbol> resolveUseAllType(String name, Map<String, ParentSymbol> using) {
        for (ParentSymbol parent : using.values()) {
            if (parent.getKind() == Symbol.Kind.NAMESPACE) {
                SymbolTable table = parent.getTable();
                if (table.containsType(name)) {
                    return Optional.of((ParentSymbol) table.get(name));
                }
            }
        }
        return Optional.empty();
    }

    private Optional<ParentSymbol> resolveNamespaceOrType(ASTIdentifier id, ResolutionContext ctx) {
        String name = id.getValue();

        // 1. Find an ancestor, starting with the parent, to find the name as a
        //    direct child of the ancestor.  Include the first namespace found,
        //    but not any namespace further up.  Must be a type if found this way.
        Optional<ParentSymbol> optResolved = resolveChildOfAncestor(name, ctx.enclosingSymbol());
        if (optResolved.isPresent()) {
            return optResolved;
        }

        // 2. If not found, check the use declaration symbols (types) that
        //    match a type directly.  Must be a type if found this way.
        optResolved = resolveUsedType(name, ctx.using());
        if (optResolved.isPresent()) {
            return optResolved;
        }

        // 3. If not found, check all use-all declaration symbols (namespaces),
        //    looking for a matching child.  Must be a type if found this way.
        optResolved = resolveUseAllType(name, ctx.using());
        if (optResolved.isPresent()) {
            return optResolved;
        }

        // 4. If not found, check the global symbol table for a direct child,
        //    which must be a namespace.
        optResolved = ctx.global().getNamespace(name);
        if (optResolved.isPresent()) {
            return optResolved;
        }

        // 5. If not found, create an unresolved error.
        error(id.getLocation(), "Symbol " + name + " not found.");
        return Optional.empty();
    }

    private Optional<ParentSymbol> resolveRestOfQualifiedType(List<ASTSimpleType> simpleTypes, ParentSymbol first) {
        // The first symbol "first" has already been resolved.
        ParentSymbol resolved = first;

        // Each subsequent identifier must resolve to a child symbol of the
        // previously resolved symbol.
        for (int i = 1; i < simpleTypes.size(); i++) {
            ASTIdentifier id = simpleTypes.get(i).getName();
            String name = id.getValue();
            SymbolTable table = resolved.getTable();
            if (table.containsNamespaceOrType(name)) {
                resolved = (ParentSymbol) table.get(name);
            }
            else {
                error(id.getLocation(), "Symbol " + name + " not found.");
                return Optional.empty();
            }
        }

        // Ensure it's a type here at the end.
        if (resolved.getKind().isType()) {
            return Optional.of(resolved);
        }
        else {
            error(resolved.getLocation(), "Type expected.");
            return Optional.empty();
        }
    }
}
