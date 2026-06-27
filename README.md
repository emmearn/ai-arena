# AI Arena

AI Arena is a Spring Boot web application that turns one user question into a visible multi-specialist debate: the backend validates the request, plans a temporary team, creates runtime specialists, streams debate events, and produces a final answer.

## Stack

- Java 21
- Maven wrapper
- Spring Boot Web MVC
- Server-Sent Events for progressive session updates
- Deterministic fake AI adapter for local development and tests

## Prerequisites

- JDK 21 available on `PATH`
- No database is required for the MVP
- No API key is required while the fake AI adapter is active

## Commands

```powershell
.\mvnw spring-boot:run
.\mvnw test
.\mvnw clean package
```

The app starts on the default Spring Boot port unless overridden with standard Spring configuration.

## Configuration

Operational limits are configured in `src/main/resources/application.properties`:

```properties
arena.limits.max-specialists=4
arena.limits.max-turns=6
arena.limits.max-messages=24
arena.limits.timeout=90s
arena.limits.max-input-characters=4000
```

Keep secrets out of files and pass future provider credentials through environment variables or a secret manager.

## Project Structure

- `src/main/java/com/marnone/ai_arena/web`: HTTP/SSE entry points and web DTOs.
- `src/main/java/com/marnone/ai_arena/application`: validation, planning, specialist creation, debate orchestration, events, and final answer flow.
- `src/main/java/com/marnone/ai_arena/domain`: immutable domain records and execution limits.
- `src/main/java/com/marnone/ai_arena/ai`: AI ports and the fake adapter.
- `src/main/java/com/marnone/ai_arena/config`: Spring configuration and typed properties.
- `src/test/java/com/marnone/ai_arena`: unit and integration tests.

## Application Flow

Clients start a session with `POST /api/arena/sessions` using JSON:

```json
{ "question": "How should AI Arena present a software architecture decision?" }
```

Use `Accept: text/event-stream` to receive validation, team, specialist, debate, supervisor, final answer, or error events as they are produced.

## Documentation

Use `docs/` for detailed project guidance:

- `docs/requirements.md`
- `docs/architecture.md`
- `docs/security.md`
- `docs/tasks.md`
- `docs/workflow.md`
- `docs/design.md`
