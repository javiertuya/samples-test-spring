# Contributing to samples-test-spring

## Git Workflow

This project follows a worflow with different specific relase branches that publish the application in Azure, where:
- `develop`: Is the default branch in GitHub. Used to publish the application to a preproduction/integration site.
- `main`: Only used to publish final versions to a production site.

## CI/CD Workflow

After a push to any branch, the following jobs are executed:
- `test`: Build and test the application (tests that require a browser are separated in src/it and executed in the integration-test maven phase). 
  Artifact `test-reports` contains all surefire, failsafe and junit reports as well as screenshots and recordings.
- Static tests
  - `sonarqube`: SonarQube analysis is sent to [sonarcloud.io](https://sonarcloud.io/project/overview?id=my:samples-test-spring).
  - `dependency-check`: (not active) OWASP dependency check. Artifact `dependency-check` contains the generated reports.
- `deploy-azure`: Deploys the application to Azure and runs post-deploy smoke tests. Artifact `deploy-test-reports` contains the results.
  Deployment is made to one of the following environments (depends on the pushed branch):
  - Production ([samples-test-spring-main](https://samples-test-spring-main.azurewebsites.net/)): Deployed after each push to the `main` branch.
  - Pre-Producction/Integration ([samples-test-spring-develop](https://samples-test-spring-develop.azurewebsites.net/)): Deployed after each push to any branch starting with `develop`.

## Pull requests

- Before creating a new pull request:
  - Ensure you have an updated version of the `develop` branch. Only fast-forward merges are allowed.
  - Ensure that each PR will submit only one or a few significant commits, and the comment is appropriate. Squash your local branch if needed.
  - Although the documentation is still written in Spanish, all PRs and commits should be written in english.
- Each pull request must pass the following checks before merge to ensure:
  - All dynamic (java) and static (sonarqube, dependency check) tests are passing.
  - Deployment and post deploy tests succeded (for `main` and `develop*` branches)
  - See the CI/CD worflow section.
