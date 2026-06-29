# AI Arena

AI Arena is a Spring Boot web application that turns one user question into a visible debate among orchestrated AI experts: the backend validates the request, plans a temporary team, creates request-specific AI roles, streams debate events, and produces a final answer.

## Stack

- Java 21
- Maven wrapper
- Spring Boot Web MVC
- Spring AI OpenAI starter, disabled by default unless the OpenAI adapter is selected
- Server-Sent Events for progressive session updates
- Deterministic fake AI adapter for local development and tests

## Prerequisites

- JDK 21 available on `PATH`
- No database is required for the MVP
- No API key is required while the fake AI adapter is active
- OpenAI integration uses a server-side API key when the OpenAI adapter is enabled; no ChatGPT password is used

## Commands

```powershell
.\mvnw spring-boot:run
.\mvnw test
.\mvnw clean package
```

The app starts on the default Spring Boot port unless overridden with standard Spring configuration.
Open `http://localhost:8080/` for the single-page arena UI with validation, expert team, and debate feedback.

## Configuration

Operational limits are configured in `src/main/resources/application.properties`:

```properties
arena.limits.max-experts=4
arena.limits.max-turns=6
arena.limits.max-messages=24
arena.limits.timeout=90s
arena.limits.max-input-characters=4000
arena.http.max-payload-bytes=8192
arena.http.rate-limit-max-requests=20
arena.http.rate-limit-window=1m
arena.ai.provider=openai
arena.ai.adapter=fake
arena.ai.model=gpt-5-mini
arena.ai.request-timeout=30s
spring.ai.model.chat=none
spring.ai.model.embedding=none
spring.ai.model.image=none
spring.ai.model.audio.speech=none
spring.ai.model.audio.transcription=none
spring.ai.model.moderation=none
spring.ai.openai.chat.model=gpt-5-mini
spring.ai.retry.max-attempts=2
```

Keep secrets out of files and pass provider credentials through environment variables or a secret manager. The OpenAI starter is on the classpath, but all Spring AI model auto-configurations are set to `none` by default and `arena.ai.adapter=fake` keeps the app runnable without an API key.

To enable the OpenAI adapter locally:

```powershell
$env:OPENAI_API_KEY="..."
$env:ARENA_AI_ADAPTER="openai"
$env:SPRING_AI_MODEL_CHAT="openai"
.\mvnw spring-boot:run
```

Use an OpenAI API key only. Do not use a ChatGPT password, browser session, or user credential.

Environment variables:

| Variable | Required when | Purpose |
| --- | --- | --- |
| `OPENAI_API_KEY` | `ARENA_AI_ADAPTER=openai` | Server-side OpenAI API key used by Spring AI. |
| `ARENA_AI_ADAPTER` | Optional | Runtime adapter selector. Default is `fake`; use `openai` for `SpringAiAdapter`. |
| `SPRING_AI_MODEL_CHAT` | `ARENA_AI_ADAPTER=openai` | Enables Spring AI chat auto-configuration; use `openai`. |

With `arena.ai.adapter=fake`, AI Arena uses the deterministic local adapter. It does not call external services, does not need credentials, and produces predictable validation, team planning, debate messages, supervisor decisions, and final answers for local development, tests, and demos.

## Project Structure

- `src/main/java/com/marnone/ai_arena/web`: HTTP/SSE entry points and web DTOs.
- `src/main/java/com/marnone/ai_arena/application`: validation, planning, orchestrated AI expert creation, debate orchestration, events, and final answer flow.
- `src/main/java/com/marnone/ai_arena/domain`: immutable domain records and execution limits.
- `src/main/java/com/marnone/ai_arena/ai`: AI ports, fake adapter, and Spring AI adapter.
- `src/main/java/com/marnone/ai_arena/config`: Spring configuration and typed properties.
- `src/test/java/com/marnone/ai_arena`: unit and integration tests.

## Application Flow

Clients start a session with `POST /api/arena/sessions` using JSON:

```json
{ "question": "How should AI Arena present a software architecture decision?" }
```

Use `Accept: text/event-stream` to receive validation, team, expert, debate, supervisor, final answer, or error events as they are produced.

## Documentation

Use `docs/` for detailed project guidance:

- `docs/requirements.md`
- `docs/architecture.md`
- `docs/security.md`
- `docs/tasks.md`
- `docs/workflow.md`
- `docs/design.md`
