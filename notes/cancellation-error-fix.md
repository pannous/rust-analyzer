# CancellationException During Checkin

## Problem
IntelliJ's code analysis before checkin triggers LSP semantic token refresh, which gets cancelled and logged as an error (even though CancellationException is a control-flow exception that should be rethrown, not logged).

## Root Cause
- IntelliJ platform bug: CodeAnalysisBeforeCheckinHandler.findCodeSmells doesn't properly handle CancellationException  
- Triggered by LSP4IJ refreshing semantic tokens during pre-commit analysis
- The custom rust-analyzer LSP server operation gets cancelled, possibly due to timeout

## Solutions

### 1. Disable code analysis before checkin (Recommended)
In RustRover:
- Settings → Version Control → Commit → Uncheck "Analyze code"
- Or in commit dialog: Gear icon → Uncheck "Analyze code"

### 2. Skip analysis for specific commits
Use: `git commit --no-verify` to bypass pre-commit hooks and analysis

### 3. Commit from command line
Regular `git commit` from terminal bypasses RustRover's UI and pre-commit analysis

## The Error Is NOT In Your Code
This is an IntelliJ platform bug where CancellationException should be rethrown, not logged.
Your lexer changes are innocent - they just trigger the code analysis which exposes this bug.
