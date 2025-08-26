# Contributing to JSON2API

Thank you for considering contributing to JSON2API! We welcome all kinds of contributions: bug reports, feature requests, documentation improvements, and code contributions.

## How to Contribute

- Use the **GitHub Issues** tracker to report bugs or request new features.
- Please provide:
  - Clear description of the issue.
  - Steps to reproduce (if applicable).
  - Expected vs actual behavior.
  - Screenshots or logs if helpful.

### 2. Submitting Code Changes
1. **Fork** the repository.
2. **Clone** your fork locally:
   ```bash
   git clone https://github.com/your-username/json2api.git
   cd json2api

### 3. Create a new branch for your change:

- git checkout -b feature/your-feature-name

### 4. Make your changes following the coding standards below.

### 5. Run tests locally.
- ./mvnw test

### 6. Commit your changes using conventional commits:
- feat: add new generator method
- fix: resolve Optional import issue
- docs: update README with new endpoints

### 7. Push your branch to your fork:
- git push origin feature/your-feature-name

## Coding Standards
- Java 17, Spring Boot conventions
- Use CamelCase for class names and camelCase for variables
- All public classes and methods must have JavaDoc
- Methods should be modular and short
- Follow Hexagonal Architecture principles where applicable
- Include unit and integration tests for new features or fixes

## Pull Request Guidelines
- PR title should follow conventional commit style
- Provide a clear description of the changes
- Reference related issues (e.g., Fixes #12)
- Ensure all tests pass
- Maintain backward compatibility when possible

## Code Review
- All PRs will be reviewed by maintainers
- Feedback may include style, structure, or testing improvements
- Please address review comments promptly


 ## Thank You!

We appreciate your contributions! Every fix, feature, or improvement helps make JSON2API better.