# XToPDF Build & Quality Targets

.PHONY: build test format format-check clean

## Build the project (skip tests for speed)
build:
	./gradlew bootJar -x test

## Run all tests
test:
	./gradlew test

## Apply code formatting (auto-fix)
format:
	./gradlew spotlessApply

## Check code formatting (CI-friendly, fails on violations)
format-check:
	./gradlew spotlessCheck

## Run full quality pipeline: format check + tests + coverage
check: format-check test
	./gradlew jacocoTestCoverageVerification

## Clean build artifacts
clean:
	./gradlew clean
