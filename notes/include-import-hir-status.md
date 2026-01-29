# Include/Import HIR Lowering Implementation Status

## Goal
Enable full IDE features (go-to-definition, autocomplete, refactoring) for `include` and `import` keywords by adding HIR lowering support in rust-analyzer.

## What Was Completed ✅

### 1. AST Support
- Include and Import are already fully parsed into AST nodes
- AST nodes defined in `crates/syntax/src/ast/generated/nodes.rs`
- Grammar defined in `crates/syntax/rust.ungram`

### 2. ModItemId Integration
- Added Include and Import to `mod_items!` macro in `item_tree.rs:433-434`
- Creates `ModItemId::Include` and `ModItemId::Import` variants
- Registered in big_data storage

### 3. Item Tree Data Structures
- Created `Include` struct in `item_tree.rs:474-477` (mirrors Use structure)
- Created `Import` struct in `item_tree.rs:480-483` (mirrors Use structure)
- Both have `visibility` and `use_tree` fields
- Added to `BigModItem` enum

### 4. Lowering Functions
- Implemented `lower_include()` in `item_tree/lower.rs:293-303`
- Implemented `lower_import()` in `item_tree/lower.rs:306-316`
- Both parse use_tree and create appropriate item tree nodes
- Added to `lower_mod_item()` match statement

### 5. AST ID Support
- Implemented `AstIdNode` for Include and Import in `span/src/ast_id.rs:352-377`
- Added `include_ast_id()` and `import_ast_id()` functions
- Added `ErasedFileAstIdKind::Include` and `::Import` variants
- Registered in should_alloc() and alloc_id() functions

### 6. Name Resolution Preparation
- Added Import::from_include() and Import::from_import() in `nameres/collector.rs`
- Added match arms in collector to handle Include/Import items
- Created expand() methods for Include and Import structs

### 7. ID System Setup
- Created `IncludeLoc = ItemLoc<ast::Include>` in `lib.rs:355-356`
- Created `ImportItemLoc = ItemLoc<ast::Import>` in `lib.rs:358-359`
- Set up intern/lookup methods with impl_intern! macro
- Created `IncludeId` and `ImportItemId` types

### 8. Pretty Printing
- Added Include and Import cases to pretty printer in `item_tree/pretty.rs`
- Displays as "include" and "import" with use_tree

## Remaining Work  ❌

### Critical Issue: ImportId Type Conflict

The main blocker is a namespace collision:
- **item_scope::ImportId** - existing type for tracking imports during name resolution
  - Structure: `{ use_: UseId, idx: Idx<ast::UseTree> }`
  - Used throughout name resolution to track import statements

- **Our new ImportItemId** - for the `import` keyword AST item
  - Created to intern Import AST nodes
  - Needs to be usable in ImportSource alongside UseId and IncludeId

### The Problem

`ImportSource` (in collector.rs) needs to store which use/include/import statement an import came from. Created `UseOrImportId` enum:

```rust
enum UseOrImportId {
    Use(UseId),
    Include(IncludeId),
    Import(ImportItemId),  // Our new ID
}
```

But `item_scope::ImportId` expects:
```rust
struct ImportId {
    use_: UseId,  // ← Expects UseId, not UseOrImportId!
    idx: Idx<ast::UseTree>,
}
```

### Solutions to Consider

**Option 1:** Change item_scope::ImportId to accept UseOrImportId
- Pros: Clean, maintains separate IDs
- Cons: Affects many files, large refactor

**Option 2:** Use only UseId for all three
- Pros: Simple, no type conflicts
- Cons: Loses type safety, can't distinguish item types

**Option 3:** Add lookup() method to UseOrImportId (partially done)
- Implemented lookup() that returns ItemLoc<ast::Item>
- Still need to fix ImportId construction sites

**Option 4:** Create ItemImportId (different name to avoid collision)
- Use: ImportItemId (done)
- Include: IncludeId (done)
- Import: ImportItemId (done, but naming unclear)

## Files Modified

1. `crates/hir-def/src/item_tree.rs` - Added Include/Import structs, mod_items!, BigModItem
2. `crates/hir-def/src/item_tree/lower.rs` - Added lowering functions
3. `crates/hir-def/src/item_tree/pretty.rs` - Added pretty printing
4. `crates/hir-def/src/lib.rs` - Added IncludeLoc, ImportItemLoc, impl_intern!
5. `crates/hir-def/src/nameres/collector.rs` - Added from_* methods, UseOrImportId, match arms
6. `crates/span/src/ast_id.rs` - Added AstIdNode impls, ErasedFileAstIdKind variants

## Current Build Status

```
error: could not compile `hir-def` (lib) due to 10 previous errors
```

Main errors:
1. `ImportOrExternCrate::Import` expects item_scope::ImportId with UseId
2. `GlobId` expects UseId
3. Missing intern methods (need to add to DefDatabase trait)
4. BigModItem size assertion needs updating

## Next Steps

1. **Decide on approach** for ImportId conflict (recommend Option 1 or 3)
2. **Update item_scope.rs** if going with Option 1
3. **Add UseOrImportId.lookup()** usage at all construction sites
4. **Update DefDatabase trait** to include intern/lookup methods
5. **Fix BigModItem size assertion** in item_tree.rs:328
6. **Test with real code** - create include/import statements and verify IDE features

## Testing Plan

Once compilation succeeds:

1. Create test file with:
   ```rust
   include std::collections::HashMap;
   import std::fs::File;
   use std::io::Write;
   ```

2. Test IDE features:
   - Go-to-definition on HashMap, File
   - Autocomplete after typing `HashMap::`
   - Hover shows documentation
   - Rename works
   - Find references works

3. Verify in RustRover after `./install-plugin.sh`

## Architecture Summary

```
Source Code (include/import)
    ↓
Parser (already works) ✅
    ↓
AST (Include/Import nodes) ✅
    ↓
Item Tree (Include/Import in ModItemId) ✅
    ↓
Lowering (lower_include/lower_import) ✅
    ↓
HIR (stored as Include/Import in BigModItem) ✅
    ↓
Name Resolution (from_include/from_import) ⚠️ BLOCKED
    ↓
Semantic Analysis
    ↓
IDE Features (go-to-def, autocomplete, etc.)
```

## Notes

- Include and Import are semantically identical to Use at the HIR level
- They have the same structure: visibility + use_tree
- The only difference is the keyword used in source code
- This is purely syntax sugar - compiler already desugars them

## Time Investment

- Initial exploration: ~30 min
- AST ID setup: ~20 min
- Item tree integration: ~30 min
- Name resolution setup: ~40 min
- Debugging type issues: ~60 min
- **Total: ~3 hours**

The implementation is ~80% complete. The remaining 20% is resolving the ImportId type conflict, which requires a design decision and careful refactoring of the name resolution code.
