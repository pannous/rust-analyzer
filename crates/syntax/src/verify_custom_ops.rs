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

    #[test]
    fn test_not_operator() {
        let code = "fn f() { let x = not true; }";
        let parse = SourceFile::parse(code, parser::Edition::CURRENT);
        assert!(parse.errors().is_empty(), "Unexpected errors: {:?}", parse.errors());

        let debug = format!("{:#?}", parse.tree().syntax());
        assert!(debug.contains("PREFIX_EXPR"), "Expected PREFIX_EXPR in AST, got:\n{}", debug);
    }

    #[test]
    fn test_not_not_operator() {
        // Double negation
        let code = "fn f() { let x = not not false; }";
        let parse = SourceFile::parse(code, parser::Edition::CURRENT);
        assert!(parse.errors().is_empty(), "Unexpected errors: {:?}", parse.errors());
    }

    #[test]
    fn test_not_with_and() {
        // not should work with and/or
        let code = "fn f() { let x = not a and b; }";
        let parse = SourceFile::parse(code, parser::Edition::CURRENT);
        assert!(parse.errors().is_empty(), "Unexpected errors: {:?}", parse.errors());
    }

    #[test]
    fn test_not_as_identifier() {
        // 'not' followed by '.' should be treated as identifier, not as operator
        // This allows variables named 'not' to have methods called on them
        let code = "fn f() { let x = not; }";  // 'not' as simple identifier
        let parse = SourceFile::parse(code, parser::Edition::CURRENT);
        let debug = format!("{:#?}", parse.tree().syntax());
        // Should contain PATH_EXPR with IDENT "not" (not PREFIX_EXPR)
        assert!(debug.contains("PATH_EXPR"), "Expected PATH_EXPR for 'not' as identifier");
        assert!(!debug.contains("PREFIX_EXPR"), "Should NOT be PREFIX_EXPR when 'not' is alone");
    }

    #[test]
    fn test_not_with_parens() {
        let code = "fn f() { let x = not (a and b); }";
        let parse = SourceFile::parse(code, parser::Edition::CURRENT);
        assert!(parse.errors().is_empty(), "Unexpected errors: {:?}", parse.errors());
    }

    // Unicode operator tests

    #[test]
    fn test_unicode_le() {
        // ≤ should be parsed as <=
        let code = "fn f() { let x = a ≤ b; }";
        let parse = SourceFile::parse(code, parser::Edition::CURRENT);
        assert!(parse.errors().is_empty(), "Unexpected errors for ≤: {:?}", parse.errors());

        let debug = format!("{:#?}", parse.tree().syntax());
        assert!(debug.contains("BIN_EXPR"), "Expected BIN_EXPR for ≤ comparison");
    }

    #[test]
    fn test_unicode_ge() {
        // ≥ should be parsed as >=
        let code = "fn f() { let x = a ≥ b; }";
        let parse = SourceFile::parse(code, parser::Edition::CURRENT);
        assert!(parse.errors().is_empty(), "Unexpected errors for ≥: {:?}", parse.errors());

        let debug = format!("{:#?}", parse.tree().syntax());
        assert!(debug.contains("BIN_EXPR"), "Expected BIN_EXPR for ≥ comparison");
    }

    #[test]
    fn test_unicode_ne() {
        // ≠ should be parsed as !=
        let code = "fn f() { let x = a ≠ b; }";
        let parse = SourceFile::parse(code, parser::Edition::CURRENT);
        assert!(parse.errors().is_empty(), "Unexpected errors for ≠: {:?}", parse.errors());

        let debug = format!("{:#?}", parse.tree().syntax());
        assert!(debug.contains("BIN_EXPR"), "Expected BIN_EXPR for ≠ comparison");
    }

    #[test]
    fn test_unicode_ellipsis() {
        // … should be parsed as .. (range)
        let code = "fn f() { for i in 0…10 { } }";
        let parse = SourceFile::parse(code, parser::Edition::CURRENT);
        assert!(parse.errors().is_empty(), "Unexpected errors for …: {:?}", parse.errors());

        let debug = format!("{:#?}", parse.tree().syntax());
        assert!(debug.contains("RANGE_EXPR"), "Expected RANGE_EXPR for … range");
    }

    #[test]
    fn test_unicode_not() {
        // ¬ should be parsed as ! (negation)
        let code = "fn f() { let x = ¬true; }";
        let parse = SourceFile::parse(code, parser::Edition::CURRENT);
        assert!(parse.errors().is_empty(), "Unexpected errors for ¬: {:?}", parse.errors());

        let debug = format!("{:#?}", parse.tree().syntax());
        assert!(debug.contains("PREFIX_EXPR"), "Expected PREFIX_EXPR for ¬ negation");
    }

    #[test]
    fn test_unicode_mixed() {
        // Test mixing Unicode and ASCII operators
        let code = "fn f() { let x = a ≤ b and c ≥ d; }";
        let parse = SourceFile::parse(code, parser::Edition::CURRENT);
        assert!(parse.errors().is_empty(), "Unexpected errors: {:?}", parse.errors());
    }
}
