# Contributing to samples-test-spring

## Git Workflow

This project follows the Git Flow Worflow without specific relase branches (in principle), where:
- `develop`: Is the default branch in GitHub. All new features and pull requests are merged into this branch.
- `main`: Used for release deploy only.

## CI/CD Workflow

After a push to any branch, the following jobs are executed:
- `test`: Build and test the application (tests that require a browser are separated in src/it and executed in the integration-test maven phase). 
  Artifact `test-reports` contains all surefire, failsafe and junit reports as well as screenshots and recordings.
- Static tests
  - `sonarqube`: SonarQube analysis is sent to [sonarcloud.io](https://sonarcloud.io/project/overview?id=my:samples-test-spring).
  - `dependency-check`: OWASP dependency check. Artifact `dependency-check` contains the generated reports.
- `deploy`: Depoloys the application to Heroku and runs post-deploy smoke tests. Artifact `deploy-test-reports` contains the results.
  Deployment is made to one of the following environments:
  - Production ([samples-test-spring-main](https://samples-test-spring-main.herokuapp.com/)): Deployed after each push to main branch.
  - Pre-Producction/Integration ([samples-test-spring-develop](https://samples-test-spring-develop.herokuapp.com/)): Deployed after each push to any other branch.

## Pull requests

- Before creating a new pull request:
  - Ensure you have an updated version of the `develop` branch. Only fast-forward merges are allowed.
  - Ensure that each PR will submit only one or a few significant commits, and the comment is appropriate. Squash your local branch if needed.
  - Although the documentation is still written in Spanish, all PRs and commits should be written in english.
- Each pull request must pass the the following checks before merge to ensure:
  - All dynamic and static tests are passing.
  - Deployment succeded.
  - See the CI/CD worflow section.
