# Architecture

Fonte di verita' delle scelte tecniche e strutturali per soddisfare `docs/requirements.md`. Questo documento descrive come costruire il sistema, senza dettagliare security, task o workflow operativi.

## 1. Overview

AI Arena e' una web application monolitica, stateless rispetto alla persistenza applicativa dell'MVP, con orchestration server-side e UI a schermata unica. Il backend riceve la domanda, valida, classifica, pianifica il team, crea specialisti runtime, coordina il dibattito, pubblica eventi progressivi verso il browser e produce la risposta finale.

| Requisiti | Scelta architetturale |
| --- | --- |
| `REQ-001`, `REQ-011`, `NFR-007` | Web app browser con pagina singola e controller HTTP. |
| `REQ-002`, `NFR-003` | Componente applicativo dedicato alla validazione input e policy di rifiuto. |
| `REQ-003`, `REQ-004`, `REQ-012` | Planner domain-agnostic basato su richiesta valida e limiti configurati. |
| `REQ-005`, `REQ-006` | Factory runtime di specialisti senza registry persistente di dominio. |
| `REQ-007`, `NFR-001` | Streaming/eventi progressivi dal backend alla UI. |
| `REQ-008`, `REQ-009`, `REQ-010`, `NFR-002` | Orchestrator con stato di sessione richiesta, limiti, arresto e sintesi finale. |
| `NFR-004`, `NFR-006` | Error model uniforme, logging tecnico e test tracciabili. |

## 2. Stack tecnologico

| Scelta | Motivazione |
| --- | --- |
| Java 21 | Gia' configurato nel progetto; LTS moderna, adatta a codice server tipizzato e testabile. |
| Maven | Gia' presente; gestione build e dipendenze semplice per MVP. |
| Spring Boot | Vincolo di vision e progetto esistente; riduce boilerplate per web app, configurazione, test e packaging. |
| Spring Web MVC | Necessario per pagina web, endpoint applicativi e streaming SSE; sufficiente per una richiesta alla volta e UI singola. |
| Spring AI | Vincolo di vision; astrae l'interazione con LLM per validazione, pianificazione, contributi e sintesi. |
| Server-Sent Events | Scelta minima per aggiornamenti progressivi unidirezionali backend -> browser richiesti da `REQ-007`; evita complessita' bidirezionale non richiesta. |
| HTML/CSS/JavaScript vanilla serviti dall'app | Sufficiente per schermata unica; evita framework frontend non richiesti. |
| Nessun database nell'MVP | Cronologia e memoria persistente sono escluse; lo stato vive solo durante la richiesta. |
| JUnit + Spring Boot Test | Gia' presenti; coprono unit e integration test della logica principale. |
| Provider LLM esterno via Spring AI | Necessario per generazione AI; provider/modello specifico e' punto aperto da registrare in `docs/decisions.md`. |

Dipendenze applicative previste: starter web, Spring AI starter del provider scelto, starter test. Non introdurre database, code, broker, RAG, tool calling o autenticazione finche' non richiesti.

## 3. Architettura applicativa

Layer:

| Layer | Responsabilita' | Dipendenze consentite |
| --- | --- | --- |
| `web` | Controller, pagina singola, DTO di input/output, stream eventi, mapping errori utente. | Dipende da `application`; non contiene logica di dominio. |
| `application` | Use case `RunArenaSession`, coordinamento validazione -> planning -> team -> dibattito -> sintesi. | Dipende da `domain` e porte in `ai`; non dipende da dettagli web. |
| `domain` | Modello e regole: domanda, esito validazione, piano, specialista, messaggio, sessione, limiti, decisione supervisore. | Nessuna dipendenza da Spring, web o provider esterni. |
| `ai` | Porte e adapter Spring AI per chiamate LLM strutturate. | Implementa interfacce usate da `application`; non conosce controller. |
| `config` | Properties tipizzate, limiti, bean applicativi. | Puo' cablare layer; non contiene logica di business. |

Regole:
- il flusso applicativo e' orchestrato da un solo use case principale;
- i componenti AI restituiscono risultati strutturati, non testo libero da interpretare a valle quando serve controllo;
- i limiti vengono applicati sia in pianificazione sia durante il dibattito;
- il dominio non deve importare framework;
- lo streaming espone stati/eventi, non dettagli interni o prompt completi.

## 4. Struttura progetto

Package base: `com.marnone.ai_arena`.

```text
src/main/java/com/marnone/ai_arena/
  AiArenaApplication.java
  web/
    ArenaController
    ArenaEvent
    ArenaRequest
    ArenaErrorResponse
  application/
    RunArenaSessionUseCase
    ArenaSessionService
    ValidationService
    PlanningService
    SpecialistFactory
    DebateOrchestrator
    SupervisorService
    FinalAnswerService
  domain/
    Question
    ValidationResult
    RequestClassification
    TeamPlan
    Specialist
    DebateMessage
    SupervisorDecision
    FinalAnswer
    ArenaLimits
    ArenaSessionState
  ai/
    AiClientPort
    ValidationAiPort
    PlanningAiPort
    SpecialistAiPort
    DebateAiPort
    SupervisorAiPort
    SpringAiAdapter
  config/
    ArenaProperties
    AiConfiguration
    WebConfiguration
  support/
    ClockProvider
    IdGenerator
```

Test mirror:

```text
src/test/java/com/marnone/ai_arena/
  application/
  domain/
  web/
  ai/
```

Convenzioni:
- nomi domain al singolare;
- DTO web separati dai tipi domain;
- porte AI esplicite per rendere mockabile la logica principale;
- `support` solo per utilita' tecniche trasversali piccole e stabili.

## 5. Componenti principali

| Componente | Responsabilita' | Requisiti |
| --- | --- | --- |
| `ArenaController` | Servire schermata, ricevere domanda, avviare stream eventi. | `REQ-001`, `REQ-007`, `REQ-011` |
| `RunArenaSessionUseCase` | Coordinare l'intero ciclo di richiesta. | Tutti i `REQ-*` principali |
| `ValidationService` | Validare input, distinguere valido/rifiutato/errore. | `REQ-002`, `NFR-003` |
| `PlanningService` | Classificare richiesta e produrre piano team. | `REQ-003`, `REQ-004`, `REQ-012` |
| `SpecialistFactory` | Creare specialisti runtime coerenti e distinguibili. | `REQ-005`, `REQ-006` |
| `DebateOrchestrator` | Gestire turni, messaggi progressivi e stato dibattito. | `REQ-007`, `REQ-008`, `REQ-009` |
| `SupervisorService` | Decidere prossimo turno, arresto e ragione. | `REQ-008`, `REQ-010`, `NFR-002` |
| `FinalAnswerService` | Produrre sintesi motivata dal dibattito. | `REQ-010` |
| `ArenaProperties` | Esporre limiti configurabili. | `REQ-009` |
| `SpringAiAdapter` | Incapsulare Spring AI e provider LLM. | `REQ-002`-`REQ-010` |

## 6. Modello dati

Nessuna persistenza richiesta nell'MVP. Il modello e' in memoria per durata della richiesta.

| Entita' | Campi essenziali | Relazioni |
| --- | --- | --- |
| `Question` | `text`, `submittedAt` | Input della sessione. |
| `ValidationResult` | `status`, `reason`, `classificationHint` | Precede piano; se rifiutato chiude il flusso. |
| `RequestClassification` | `domain`, `intent`, `confidence`, `notes` | Guida il piano. |
| `TeamPlan` | `skills`, `specialistCount`, `roles`, `initialStrategy` | Origina specialisti. |
| `Specialist` | `id`, `name`, `role`, `personality`, `mission` | Autore dei messaggi. |
| `DebateMessage` | `id`, `specialistId`, `turn`, `type`, `content`, `createdAt` | Appartiene alla sessione. |
| `SupervisorDecision` | `action`, `reason`, `nextSpecialistId` | Controlla turni/arresto. |
| `FinalAnswer` | `content`, `rationale`, `stopReason` | Output finale. |
| `ArenaLimits` | `maxSpecialists`, `maxTurns`, `maxMessages`, `timeout` | Vincola piano e dibattito. |
| `ArenaSessionState` | `sessionId`, `question`, `team`, `messages`, `status`, `limits` | Aggregato runtime non persistente. |

## 7. Flussi applicativi

Flusso richiesta valida:

```text
Utente -> ArenaController -> RunArenaSessionUseCase
  -> ValidationService
  -> PlanningService
  -> SpecialistFactory
  -> DebateOrchestrator
      -> SpecialistAiPort*
      -> SupervisorService after each turn
  -> FinalAnswerService
  -> ArenaController streams events to UI
```

Eventi stream minimi:

```text
VALIDATION_STARTED
VALIDATION_ACCEPTED | VALIDATION_REJECTED
TEAM_PLANNED
SPECIALIST_CREATED
DEBATE_MESSAGE
SUPERVISOR_DECISION
FINAL_ANSWER
ERROR
```

Flusso rifiuto:

```text
Domanda -> validazione negativa -> evento VALIDATION_REJECTED -> fine sessione
```

Flusso limite/timeout:

```text
Dibattito attivo -> limite raggiunto -> SupervisorDecision STOP -> sintesi finale con stopReason
```

## 8. Error handling e logging

Regole tecniche:
- errori di input diventano rifiuti o validation error osservabili;
- errori AI, timeout e risultati non parsabili diventano errori applicativi gestiti;
- ogni sessione ha `sessionId` tecnico per correlare log ed eventi;
- loggare stato, componente, durata, esito e ragione di arresto;
- non loggare prompt completi, segreti o dati sensibili non necessari;
- messaggi utente devono essere comprensibili e non esporre stack trace;
- policy di sicurezza dettagliate rimandate a `docs/security.md`.

Categorie errori:

| Categoria | Comportamento |
| --- | --- |
| Input non valido | Nessun team, evento di rifiuto, motivo utente. |
| Richiesta ostile | Nessun team, rifiuto motivato, log tecnico sintetico. |
| Provider AI non disponibile | Evento errore, fine controllata. |
| Timeout/limite | Arresto controllato, sintesi se ci sono contributi utili. |
| Errore inatteso | Evento errore generico, log tecnico correlato. |

## 9. Configurazione

Properties applicative:

| Proprieta' | Scopo |
| --- | --- |
| `arena.limits.max-specialists` | Limite massimo specialisti. |
| `arena.limits.max-turns` | Limite massimo turni. |
| `arena.limits.max-messages` | Limite massimo messaggi. |
| `arena.limits.timeout` | Durata massima sessione. |
| `arena.ai.provider` | Provider LLM selezionato. |
| `arena.ai.model` | Modello LLM selezionato. |
| `arena.ai.temperature.*` | Parametri per validazione, planning, dibattito, sintesi se necessari. |

Segreti:
- API key e credenziali solo via variabili d'ambiente o secret manager dell'ambiente di deploy;
- non salvare segreti in repository;
- dettagli di rotazione, storage e policy in `docs/security.md`.

## 10. Performance e scalabilita'

Misure giustificate:
- applicare limiti prima di avviare chiamate AI costose (`REQ-009`);
- usare SSE per inviare feedback progressivo (`NFR-001`);
- mantenere stato solo in memoria per MVP, liberandolo a fine sessione;
- eseguire il dibattito in sequenza per leggibilita' e controllo; agenti paralleli sono evoluzione futura;
- impostare timeout per chiamate AI e sessione completa;
- evitare database finche' non servono cronologia o memoria.

Punto aperto: target numerici di latenza e concorrenza non definiti nei requisiti; registrarli in `docs/decisions.md` quando disponibili.

## 11. Testing strategy

| Tipo | Oggetto | Criteri |
| --- | --- | --- |
| Unit domain | `ArenaLimits`, `ArenaSessionState`, decisioni e invarianti | Nessuna dipendenza Spring o AI. |
| Unit application | Validazione, planning, factory, orchestrator, supervisor, sintesi | AI ports mockate; coprire rifiuto, successo, limite, timeout, errore. |
| Integration web | Controller e stream eventi | Verificare ordine eventi, error mapping e risposta finale. |
| Contract AI adapter | Parsing output strutturato e fallback | Test con risposte simulate del provider. |
| End-to-end light | Richiesta valida e richiesta rifiutata | Provider AI fake/deterministico. |

Test minimi tracciati:
- `REQ-001`: domanda vuota/non vuota;
- `REQ-002`: valida/rifiutata/ostile;
- `REQ-004`: piano con ruoli e limiti;
- `REQ-005`: specialisti completi;
- `REQ-007`: eventi progressivi ordinati;
- `REQ-008`/`REQ-009`: arresto per limite;
- `REQ-010`: risposta finale dopo dibattito;
- `REQ-011`: stati principali esposti alla UI.

## 12. Convenzioni di sviluppo

- package per layer, non per feature tecnica casuale;
- classi applicative con nomi orientati al caso d'uso;
- domain model immutabile dove pratico;
- dipendenze verso interfacce per componenti AI;
- prompt e template AI versionati come risorse applicative, non inline nei controller;
- risultati AI trasformati subito in tipi domain validati;
- nessuna logica di orchestration nel controller;
- nessun accesso diretto al provider AI fuori da `ai`;
- niente persistenza finche' non esiste requisito approvato;
- evitare nuove librerie senza decisione motivata.

## 13. Decisioni architetturali

| Decisione | Motivazione | Alternative | Impatto | Registrare in `docs/decisions.md` |
| --- | --- | --- | --- | --- |
| Monolite Spring Boot | MVP piccolo, singola UI, orchestrazione centralizzata. | Microservizi. | Minore complessita', deploy semplice. | Si, come ADR iniziale. |
| SSE per progress updates | Requisito unidirezionale di osservabilita'. | Polling, WebSocket. | Meno complessita' di WebSocket, migliore UX del polling. | Si. |
| Stato in memoria, nessun DB | Cronologia e memoria escluse dall'MVP. | Database relazionale/documentale. | Semplice, ma sessioni non recuperabili dopo restart. | Si. |
| Porte AI mockabili | Testabilita' e isolamento provider. | Chiamate dirette Spring AI nei servizi. | Maggiore controllo nei test e sostituibilita'. | Si. |
| Dibattito sequenziale | Chiarezza, controllo limiti, agenti paralleli esclusi dall'MVP. | Esecuzione parallela. | Meno throughput, piu' prevedibilita'. | Si. |
| Frontend vanilla servito dal backend | Schermata unica e nessun requisito di SPA complessa. | React/Vue/Angular. | Meno dipendenze, UI sufficiente per demo. | Si. |
| Provider LLM non fissato | Requisiti non indicano vendor/modello. | Fissare provider subito. | Serve decisione prima dell'implementazione AI reale. | Si, punto aperto prioritario. |

Punti aperti:
- provider e modello LLM;
- valori iniziali dei limiti `max-specialists`, `max-turns`, `max-messages`, `timeout`;
- target numerici di performance/concorrenza;
- formato esatto degli output strutturati AI.
