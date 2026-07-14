# Security Policy

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 0.x.x   | :white_check_mark: |

## Reporting a Vulnerability

If you discover a security vulnerability in Simple Builders, we appreciate your help in disclosing it to us in a responsible manner.

### How to Report

**Please do not report security vulnerabilities through public GitHub issues.**
Doing so discloses an unpatched issue to attackers before a fix is available.

Instead, use GitHub's private vulnerability reporting:

1. Open the repository's [**Security** tab](https://github.com/java-helpers/simple-builders/security).
2. Click **Report a vulnerability** to file a private
   [GitHub Security Advisory](https://docs.github.com/en/code-security/security-advisories/guidance-on-reporting-and-writing-information-about-vulnerabilities/privately-reporting-a-security-vulnerability)
   visible only to you and the maintainers.

> Maintainers: enable *Private vulnerability reporting* under
> **Settings → Code security** so the button above is available to reporters.

### What to Include

When reporting a vulnerability, please include:
- A description of the vulnerability
- Steps to reproduce the issue
- Any mitigation or workaround if known
- Your name/handle for recognition (unless you wish to remain anonymous)

### Our Commitment

- We will confirm the vulnerability and determine its impact
- We will keep you informed of the progress towards resolving the issue
- We will credit you for your discovery (unless you prefer to remain anonymous)

## Security Updates

Security updates will be released as patch versions (e.g., 0.1.1) for the latest minor version. We recommend always using the latest version of the library.

## Dependencies

We regularly update our dependencies to include security fixes. You can check for known vulnerabilities in our dependencies using:

```bash
mvn dependency-check:check
```

## Best Practices

When using Simple Builders, we recommend:
1. Always use the latest version
2. Review the code before using in production
3. Follow the principle of least privilege
4. Validate all inputs, especially when using builder patterns with untrusted data

## Security Considerations

While Simple Builders provides utility for creating builders, please be aware that:
- Builders may be used to construct objects with sensitive data
- Always validate inputs before using them in your application
- Be cautious when serializing/deserializing builder-created objects

## License

By contributing to this project, you agree that your contributions will be licensed under its MIT License.
