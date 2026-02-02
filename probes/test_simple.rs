#!/usr/bin/env rust-script
//! Simple test file for rust-analyzer

fn main() {
    // Test basic syntax
    let x = 5;
    let y = 10;

    println!("x = {}, y = {}", x, y);

    // Test standard Rust
    if x < y {
        println!("x is less than y");
    }

    let sum = x + y;
    println!("sum = {}", sum);
}
