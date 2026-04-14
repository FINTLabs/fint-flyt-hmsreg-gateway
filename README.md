# FINT Flyt HMSREG Gateway

Spring Boot service for receiving HMSREG case instances over HTTP, persisting attached files through the shared Flyt file service, and publishing mapped instance events into the Flyt flow.

## Stack

- Kotlin 2.3.10
- Java 25
- Spring Boot 3.5.x
- Spring MVC (`spring-boot-starter-web`)
- `flyt-web-instance-gateway`
- `flyt-web-resource-server`
- `ktlint`

## Profiles

The application includes these Spring profiles by default:

- `flyt-kafka`
- `flyt-logging`
- `flyt-web-resource-server`
- `flyt-file-client`

Local development can use `local-staging`, which points Kafka to `localhost:9092`, file service to `http://localhost:8091`, and disables integration existence checks in the shared web instance gateway.

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
