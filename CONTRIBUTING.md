# Contributing to samples-test-spring

## Git Workflow

This project follows the Git Flow Worflow, where:
- `develop`: Is the default branch in GitHub. All new features and pull requests are merged into this branch
- `main`: Used for releases only

## Deployment

TODO

## Pull requests

- Before creating a new pull request:
  - Ensure you have an updated version of the `develop` branch. Only fast-forward merges are allowed.
  - Ensure that each PR will submit only one or a few commits, and the comment is appropriate. Squash your local branch if needed.
  - Although the documentation is still written in Spanish, all PRs and commits should be written in english.
- Each pull request must pass the the following checks before merge to ensure:
  - All tests are passing. See the reports in the *test-reports* artifact
  - SonarQube passes the quality gate. See the analysis at [sonarcloud.io](https://sonarcloud.io/project/overview?id=my%3Asamples-test-spring)
  - OWASP dependency check does not detects vulnerable dependencies. See the report in the *dependency-check-reports* artifact
- After the checks ... (TODO)
