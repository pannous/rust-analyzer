#!/usr/bin/env rust-script
//! Test file for custom rust-analyzer syntax features

fn main() {
    // Test semicolon inference from newlines
    let x = 5
    let y = 10

    // Test power operator
    let power = 2 ** 3
    println!("2^3 = {}", power)

    // Test and/or operators
    let a = true
    let b = false
    if a and not b {
        println!("a is true and b is false")
    }

    // Test or operator
    if a or b {
        println!("at least one is true")
    }

    // Test xor operator
    if a xor b {
        println!("exactly one is true")
    }

    // Unicode operators (if supported)
    let sum = 1 + 2
    let product = 3 × 4  // Unicode multiply

    println!("Tests completed!")
}
