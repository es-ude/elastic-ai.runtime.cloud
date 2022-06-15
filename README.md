[![Unit Tests](https://github.com/es-ude/elastic-ai.runtime/actions/workflows/unitTests.yml/badge.svg)](https://github.com/es-ude/elastic-ai.runtime/actions/workflows/unitTests.yml)
[![Integration Tests](https://github.com/es-ude/elastic-ai.runtime/actions/workflows/integrationTests.yml/badge.svg)](https://github.com/es-ude/elastic-ai.runtime/actions/workflows/integrationTests.yml)

# elastic-AI.runtime

The elastic-AI.runtime provides a backend for operating digital twins. It uses MQTT as a messaging protocol and is
primarily focused on the use with the Elastic Node v5.
This repository uses gradles multi-project feature and currently contains the following projects:

- runtime: elastic-AI.runtime

## Prerequisites

- Java Version 17

## Experimental

This is meant as an RFC from my side for the team.
It proposes a new / different communication protocol and API for digital twins.

## Tests

Unit tests and integration tests can be executed independently. Both use _jacoco_ to finalize the tests with a report
that shows the test results, as well as the code coverage. The reports can be found in the location `build/reports/`
relative to the corresponding build file.

### Test execution

- All unit tests: `gradle test`
- Unit test from specific subproject `gradle :subproject:test`
    - i.e. `gradle :runtime:test`
- All Integration test `gradle integrationTest`
- Integration test from specific subproject `gradle :subproject:integrationTest`
    - i.e. `gradle :runtime:integrationTest`

## Monitor

To start the monitor: `gradle :monitor:run`.

The monitor can then be accessed locally at [localhost.com:8081](localhost.com:8081).
