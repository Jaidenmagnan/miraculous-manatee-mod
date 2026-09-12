# NEATcoder Review Guide

## Priorities

- correctness
- security
- tests
- maintainability

## Rules

- id: no-secrets
  severity: critical
  instruction: Never add credentials, tokens, API keys, or private keys to source control.
- id: no-debug-output
  severity: warning
  instruction: Do not leave debug print statements or console logging in production code.
- id: tests-for-production-changes
  severity: warning
  instruction: Add or update tests when changing production behaviour.

## Exclusions

- generated/\*\*
- vendor/\*\*
