# FINT Flyt HMSREG Gateway

Spring Boot service for receiving HMSREG case instances over HTTP, persisting attached files through the shared Flyt file service, and publishing mapped instance events into the Flyt flow.

## Stack

- Kotlin 2.3.10
- Java 25
- Spring Boot 3.5.x
- Spring MVC (`spring-boot-starter-web`)
- `flyt-gateway-starter`
- `flyt-web-resource-server`
- `ktlint`

## Profiles

The application includes these Spring profiles by default:

- `flyt-kafka`
- `flyt-logging`
- `flyt-web-resource-server`
- `flyt-file-client`

Local development can use `local-staging`, which points Kafka to `localhost:9092` (start it with `docker compose up -d`; add `--profile tools` for Kafdrop on http://localhost:19000), file service to `http://localhost:8091`, and disables integration existence checks in the shared gateway starter.

## Build And Run

Requirements:

- Java 25
- Docker if you need local dependencies such as Kafka or the Flyt file service

Build and test:

```shell
./gradlew build
```

Run locally:

```shell
SPRING_PROFILES_ACTIVE=local-staging ./gradlew bootRun
```

## API

Base endpoint:

```text
/api/hmsreg/instances/sak
```

Endpoints:

- `POST /api/hmsreg/instances/sak`
- `GET /api/hmsreg/instances/sak/{sourceApplicationInstanceId}/status`

The controller is secured by `flyt-web-resource-server`, and overlays set `server.servlet.context-path` so the runtime paths match ingress, probes, and metrics in each environment.

## Deployment

- `kustomize/base/flais.yaml` keeps `spec.url.basePath` empty.
- Each overlay sets the public base path and the servlet context path for its environment.
- Beta overlays use `/beta/<org>` and API overlays use `/<org>`.

## Quality Gates

`./gradlew build` runs compilation, tests, and `ktlintCheck`.

## Dependency Locking

The build uses [Gradle dependency locking](https://docs.gradle.org/current/userguide/dependency_locking.html), so every build resolves exactly the same direct and transitive versions. The locked versions live in the committed `*.lockfile` files: `gradle.lockfile` for project dependencies and the others for Gradle plugins. A pull request therefore shows every transitive version change it causes.

**Automatic (Dependabot):** Dependabot updates the lockfiles in its own pull requests, so no manual step is needed. If two Dependabot pull requests conflict in a lockfile, Dependabot rebases the remaining one after the first is merged. If it does not, comment `@dependabot recreate` on the pull request.

**Manual:** Any change you make by hand that affects dependency resolution requires regenerating the lockfiles. That covers versions, `extra[...]` properties, constraints, adding or removing a dependency, and plugin versions:

```bash
./gradlew dependencies buildEnvironment --write-locks
```

Commit the updated lockfiles together with the change. If you forget, the build fails both locally and in CI with an error saying the resolved version "is not part of the dependency lock state".
