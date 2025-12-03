# Contributing to XToPDF

Thank you for your interest in contributing to XToPDF! We welcome contributions from the community and appreciate your effort to help improve this project.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [How to Contribute](#how-to-contribute)
- [Development Setup](#development-setup)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Submitting Changes](#submitting-changes)
- [Reporting Bugs](#reporting-bugs)
- [Suggesting Features](#suggesting-features)

## Code of Conduct

This project adheres to a [Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code. Please report unacceptable behavior to the project maintainers.

## Getting Started

1. **Fork the repository** on GitHub
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/YOUR-USERNAME/XToPDF.git
   cd XToPDF
   ```
3. **Add upstream remote**:
   ```bash
   git remote add upstream https://github.com/jmillar0046/XToPDF.git
   ```
4. **Create a feature branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```

## How to Contribute

### Good First Issues

If you're new to the project, look for issues labeled [`good first issue`](https://github.com/jmillar0046/XToPDF/issues?q=is%3Aissue+is%3Aopen+label%3A%22good+first+issue%22). These are suitable for first-time contributors and provide a good introduction to the codebase.

### Areas for Contribution

- **Bug Fixes**: Help identify and fix bugs
- **New Format Support**: Add support for additional file formats
- **Documentation**: Improve documentation, add examples, or fix typos
- **Tests**: Write tests to improve code coverage
- **Performance**: Optimize conversion speed and memory usage
- **Features**: Implement new features or enhance existing ones

## Development Setup

### Prerequisites

- Java 21 or higher
- Gradle (included via wrapper)
- Git

### Building the Project

```bash
./gradlew build
```

### Running Locally

```bash
./gradlew bootRun
```

The server will start at `http://localhost:8080`.

### Running Tests

```bash
./gradlew test
```

## Coding Standards

### Java Style Guidelines

- **Follow existing code style**: Maintain consistency with the existing codebase
- **Use meaningful names**: Variables, methods, and classes should have descriptive names
- **Keep methods small**: Each method should do one thing well
- **Comment complex logic**: Add comments for non-obvious code, but prefer self-documenting code
- **Use Lombok annotations**: Utilize `@Slf4j`, `@Data`, `@RequiredArgsConstructor` where appropriate

### Code Organization

- **Package structure**: Follow the existing package structure (`com.xtopdf.xtopdf.*`)
- **Service layer**: Business logic should be in service classes
- **Controllers**: Keep controllers thin, delegate to services
- **Exception handling**: Use appropriate exception handling and logging

### Best Practices

- **Avoid hardcoded values**: Use constants or configuration properties
- **Handle errors gracefully**: Provide meaningful error messages
- **Log appropriately**: Use proper log levels (DEBUG, INFO, WARN, ERROR)
- **Close resources**: Ensure files and streams are properly closed (use try-with-resources)
- **Security**: Never expose sensitive information or introduce security vulnerabilities

## Testing Guidelines

### Test Coverage

- **Write tests** for all new functionality
- **Maintain coverage**: Aim to maintain or improve code coverage
- **Test edge cases**: Include tests for boundary conditions and error scenarios

### Test Structure

- **Unit tests**: Test individual components in isolation
- **Integration tests**: Test component interactions
- **Test naming**: Use descriptive test method names that explain what is being tested

### Example Test

```java
@Test
void shouldConvertDocxToPdfSuccessfully() throws Exception {
    // Arrange
    MockMultipartFile inputFile = new MockMultipartFile(
        "inputFile",
        "test.docx",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        getClass().getResourceAsStream("/test.docx")
    );
    
    // Act
    byte[] result = conversionService.convert(inputFile, "pdf");
    
    // Assert
    assertNotNull(result);
    assertTrue(result.length > 0);
}
```

## Submitting Changes

### Pull Request Process

1. **Sync with upstream**:
   ```bash
   git fetch upstream
   git rebase upstream/main
   ```

2. **Commit your changes**:
   - Write clear, descriptive commit messages
   - Use present tense ("Add feature" not "Added feature")
   - Reference issues: `Fixes #123` or `Closes #456`

3. **Push to your fork**:
   ```bash
   git push origin feature/your-feature-name
   ```

4. **Create a Pull Request**:
   - Provide a clear title and description
   - Explain what changes were made and why
   - Link related issues
   - Include screenshots for UI changes (if applicable)

5. **Address review feedback**:
   - Respond to reviewer comments
   - Make requested changes
   - Push updates to your branch

### Pull Request Checklist

Before submitting your PR, ensure:

- [ ] Code builds successfully (`./gradlew build`)
- [ ] All tests pass (`./gradlew test`)
- [ ] New tests are added for new functionality
- [ ] Code follows existing style and conventions
- [ ] Documentation is updated (if needed)
- [ ] Commit messages are clear and descriptive
- [ ] No unnecessary files are included (build artifacts, IDE configs)

## Reporting Bugs

### Before Submitting a Bug Report

- Check the [existing issues](https://github.com/jmillar0046/XToPDF/issues) to avoid duplicates
- Try to reproduce the bug with the latest version
- Collect relevant information (error messages, logs, file formats)

### Bug Report Template

When submitting a bug report, include:

- **Description**: Clear description of the issue
- **Steps to reproduce**: Detailed steps to reproduce the bug
- **Expected behavior**: What you expected to happen
- **Actual behavior**: What actually happened
- **Environment**: OS, Java version, XToPDF version
- **Error logs**: Relevant error messages or stack traces
- **Sample files**: If possible, provide sample files that trigger the bug

## Suggesting Features

We welcome feature suggestions! When suggesting a new feature:

1. **Check existing issues** to see if it's already proposed
2. **Open an issue** with the `enhancement` label
3. **Describe the feature**: Explain what you want and why
4. **Provide use cases**: Give examples of how it would be used
5. **Consider alternatives**: Mention alternative solutions you've considered

## Questions?

If you have questions about contributing:

- Check the [Wiki](https://github.com/jmillar0046/XToPDF/wiki) for detailed documentation
- Open a [Discussion](https://github.com/jmillar0046/XToPDF/discussions)
- Ask in an existing issue or pull request

## License

By contributing to XToPDF, you agree that your contributions will be licensed under the [Apache License 2.0](LICENSE).

---

Thank you for contributing to XToPDF! Your efforts help make this project better for everyone. 🎉
