package org.spruce.compiler.bootstrap.resolution;

import java.util.List;
import java.util.Optional;

import org.spruce.compiler.bootstrap.ast.names.*;
import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.symbol.ChildSymbolTable;
import org.spruce.compiler.bootstrap.symbol.EntitySymbol;
import org.spruce.compiler.bootstrap.symbol.ParentSymbol;
import org.spruce.compiler.bootstrap.symbol.Symbol;
import org.spruce.compiler.bootstrap.symbol.SymbolTable;
import org.spruce.compiler.bootstrap.symbol.GlobalLookup;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;
import org.spruce.compiler.bootstrap.symbol.VariableSymbol;

/**
 * A <code>NamesResolver</code> is a <code>BasicResolver</code> that resolves
 * symbols that refer to declared symbols: Variable names of local variables,
 * parameters, and fields.  It has many entry points for resolving name-like
 * symbols of various kinds.
 */
public class NamesResolver extends BasicResolver {
    /**
     * Constructs a <code>NamesResolver</code>.
     * @param resolver An <code>Resolver</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     * @param global The <code>GlobalLookup</code>.
     */
    public NamesResolver(Resolver resolver, MessageProducer msgProducer, GlobalLookup global) {
        super(resolver, msgProducer, global);
    }

    /**
     * Resolve all symbols in a <code>TypeName</code>.
     * @param tn An <code>ASTTypeName</code>.
     * @return An <code>Optional&lt;TypeSymbol&gt;</code>.
     */
    public Optional<TypeSymbol> resolveUseTypeName(ASTTypeName tn) {
        List<ASTIdentifier> typeIds = tn.getTypedChildren();
        Optional<ParentSymbol> optParent = resolveNamespaceOrTypeNameIds(typeIds, getGlobalLookup());
        if (optParent.isPresent()) {
            ParentSymbol parent = optParent.get();
            if (parent.getKind().isType()) {
                return Optional.of((TypeSymbol) parent);
            }
        }
        return Optional.empty();
    }

    /**
     * Resolve all symbols in a <code>TypeName</code>.
     * @param tn An <code>ASTTypeName</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveTypeName(ASTTypeName tn, ResolutionContext ctx) {
        // The parser ensures at least one simple type.
        List<ASTIdentifier> ids = tn.getTypedChildren();
        getTypesResolver().resolveIdentifiers(ids, ctx).ifPresent(tn::setResolvedDataType);
    }

    /**
     * Resolve all symbols in a <code>NamespaceOrTypeName</code>.
     * @param notn An <code>ASTNamespaceOrTypeName</code>.
     * @return An <code>Optional&lt;ParentSymbol&gt;</code>.
     */
    public Optional<ParentSymbol> resolveNamespaceOrTypeName(ASTNamespaceOrTypeName notn) {
        List<ASTIdentifier> namespaceOrTypeIds = notn.getTypedChildren();
        SymbolTable currTable = getGlobalLookup();
        return resolveNamespaceOrTypeNameIds(namespaceOrTypeIds, currTable);
    }

    private Optional<ParentSymbol> resolveNamespaceOrTypeNameIds(List<ASTIdentifier> ids, SymbolTable currTable) {
        // Parser guarantees at least one identifier, which must be a namespace.
        String first = ids.get(0).getValue();
        ParentSymbol resolved;
        if (currTable.containsNamespace(first)) {
            resolved = (ParentSymbol) currTable.get(first);
            currTable = resolved.getTable();
        }
        else {
            errorSymbolNotFound(ids.get(0).getLocation(), first);
            return Optional.empty();
        }

        // The remaining identifiers can be namespaces or types, but once a
        // type is found, then all subsequent identifiers must be types until
        // fully resolved.
        boolean typeFound = false;
        for (int i = 1; i < ids.size(); i++) {
            ASTIdentifier id = ids.get(i);
            String name = id.getValue();

            boolean nameFound;
            if (typeFound) {
                nameFound = currTable.containsType(name);
            }
            else {
                nameFound = currTable.containsNamespaceOrType(name);
            }
            if (nameFound) {
                resolved = (ParentSymbol) currTable.get(name);
                currTable = resolved.getTable();
                typeFound = resolved.getKind() != Symbol.Kind.NAMESPACE;
            }
            else {
                errorSymbolNotFound(id.getLocation(), name);
                return Optional.empty();
            }
        }

        return Optional.of(resolved);
    }

    /**
     * Resolves the <code>ExpressionName</code> to a variable and a datatype.
     * It can be resolved to a variable, a parameter, or a field.
     * @param exprName An <code>ASTExpressionName</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveExpressionName(ASTExpressionName exprName, ResolutionContext ctx) {
        // The parser ensures at least one identifier.
        List<ASTIdentifier> ids = exprName.getTypedChildren();

        // Resolve the first identifier.
        Optional<Symbol> optResolved = resolveIdentifier(ids.get(0), ctx);
        if (optResolved.isPresent()) {
            Symbol resolved = optResolved.get();
            // If only 1 identifier, then it must be a variable.
            if (ids.size() == 1) {
                if (resolved.getKind().isVariable()) {
                    exprName.setResolvedEntity((EntitySymbol) resolved);
                }
                else {
                    errorSymbolNotFound(resolved.getLocation(), resolved.getName());
                }
            }
            // Else it's qualified.
            else {
                Optional<EntitySymbol> optEntity = resolveRestOfQualifiedType(ids, resolved);
                optEntity.ifPresent(exprName::setResolvedEntity);
            }
        }
    }

    private Optional<Symbol> resolveIdentifier(ASTIdentifier id, ResolutionContext ctx) {
        String name = id.getValue();

        // 1. Find an ancestor, starting with the parent, to find the name as a
        //    direct child of the ancestor.  Include the first namespace found,
        //    but not any namespace further up.
        Optional<Symbol> optResolved = resolveChildOfAncestor(name, ctx.enclosingSymbol());
        if (optResolved.isPresent()) {
            return optResolved;
        }

        // 2. If not found, check the use declaration symbols (types) that
        //    match a type directly.  Must be a type if found this way.
        TypesResolver typesResolver = getTypesResolver();
        Optional<TypeSymbol> optTypeResolved = typesResolver.resolveUsedType(name, ctx.using());
        if (optTypeResolved.isPresent()) {
            return optTypeResolved.map(ts -> ts);
        }

        // 3. If not found, check all use-all declaration symbols (namespaces),
        //    looking for a matching child.  Must be a type if found this way.
        optTypeResolved = typesResolver.resolveUseAllType(name, ctx.using());
        if (optTypeResolved.isPresent()) {
            return optTypeResolved.map(ts -> ts);
        }

        // 4. If not found, check the global symbol table for a direct child,
        //    which must be a namespace.
        Optional<ParentSymbol> optNamespace = getGlobalLookup().getNamespace(name);
        if (optNamespace.isPresent()) {
            return optNamespace.map(s -> s);
        }

        // 5. If not found, create an unresolved error.
        errorSymbolNotFound(id.getLocation(), name);
        return Optional.empty();
    }

    private Optional<Symbol> resolveChildOfAncestor(String name, ParentSymbol parent) {
        boolean namespaceFound = false;
        while (!namespaceFound) {
            SymbolTable table = parent.getTable();
            if (table.containsNamespaceTypeOrVariable(name)) {
                return Optional.of(table.get(name));
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

    private Optional<EntitySymbol> resolveRestOfQualifiedType(List<ASTIdentifier> ids, Symbol first) {
        // The first symbol "first" has already been resolved.
        ParentSymbol parent;
        if (first.getKind().isVariable()) {
            parent = ((EntitySymbol) first).getDataType();
        }
        else {
            parent = (ParentSymbol) first;
        }
        VariableSymbol resolved = null;

        // Each subsequent identifier must resolve to a child symbol of the
        // previously resolved symbol.
        for (int i = 1; i < ids.size(); i++) {
            // For forms of var.field, check the variable's type next.
            if (resolved != null && resolved.getKind().isVariable()) {
                parent = resolved.getDataType();
            }
            ASTIdentifier id = ids.get(i);
            String name = id.getValue();
            SymbolTable table = parent.getTable();
            if (table.containsVariable(name)) {
                resolved = (VariableSymbol) table.get(name);
            }
            else if (table.containsNamespaceOrType(name)) {
                parent = (ParentSymbol) table.get(name);
            }
            else {
                errorSymbolNotFound(id.getLocation(), name);
                return Optional.empty();
            }
        }

        // Ensure it's a variable here at the end.
        if (resolved != null && resolved.getKind().isVariable()) {
            return Optional.of(resolved);
        }
        else {
            error(parent.getLocation(), "Variable expected.");
            return Optional.empty();
        }
    }
}
