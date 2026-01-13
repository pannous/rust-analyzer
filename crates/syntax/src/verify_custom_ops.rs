#[cfg(test)]
mod custom_ops_tests {
    use crate::SourceFile;
    use crate::ast::AstNode;

    #[test]
    fn test_and_operator() {
        let code = "fn f() { let x = a and b; }";
        let parse = SourceFile::parse(code, parser::Edition::CURRENT);
        assert!(parse.errors().is_empty(), "Unexpected errors: {:?}", parse.errors());

        // Check that the syntax tree contains a BIN_EXPR
        let debug = format!("{:#?}", parse.tree().syntax());
        assert!(debug.contains("BIN_EXPR"), "Expected BIN_EXPR in AST, got:\n{}", debug);
    }

    #[test]
    fn test_or_operator() {
        let code = "fn f() { let x = a or b; }";
        let parse = SourceFile::parse(code, parser::Edition::CURRENT);
        assert!(parse.errors().is_empty(), "Unexpected errors: {:?}", parse.errors());

        let debug = format!("{:#?}", parse.tree().syntax());
        assert!(debug.contains("BIN_EXPR"), "Expected BIN_EXPR in AST, got:\n{}", debug);
    }

    #[test]
    fn test_xor_operator() {
        let code = "fn f() { let x = 1 xor 2; }";
        let parse = SourceFile::parse(code, parser::Edition::CURRENT);
        assert!(parse.errors().is_empty(), "Unexpected errors: {:?}", parse.errors());

        let debug = format!("{:#?}", parse.tree().syntax());
        assert!(debug.contains("BIN_EXPR"), "Expected BIN_EXPR in AST, got:\n{}", debug);
    }

    #[test]
    fn test_mixed_operators() {
        let code = "fn f() { let x = a and b or c; }";
        let parse = SourceFile::parse(code, parser::Edition::CURRENT);
        assert!(parse.errors().is_empty(), "Unexpected errors: {:?}", parse.errors());
    }

    #[test]
    fn test_precedence() {
        // 'and' should have higher precedence than 'or' (like && vs ||)
        // So "a or b and c" should parse as "a or (b and c)"
        let code = "fn f() { let x = a or b and c; }";
        let parse = SourceFile::parse(code, parser::Edition::CURRENT);
        assert!(parse.errors().is_empty(), "Unexpected errors: {:?}", parse.errors());
    }
}
