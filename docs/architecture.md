# Architecture

Fonte di verita' delle scelte tecniche e strutturali per soddisfare `docs/requirements.md`. Questo documento descrive come costruire il sistema, senza dettagliare security, task o workflow operativi.

## 1. Overview

AI Arena e' una web application monolitica, stateless rispetto alla persistenza applicativa dell'MVP, con orchestration server-side e UI a schermata unica. Il backend riceve la domanda, valida, classifica, pianifica il team, crea esperti AI orchestrati, coordina il dibattito, pubblica eventi progressivi verso il browser e produce la risposta finale.

| Requisiti | Scelta architetturale |
| --- | --- |
| `REQ-001`, `REQ-011`, `NFR-007` | Web app browser con pagina singola e controller HTTP. |
| `REQ-002`, `NFR-003` | Componente applicativo dedicato alla validazione input e policy di rifiuto. |
| `REQ-003`, `REQ-004`, `REQ-012` | Planner domain-agnostic basato su richiesta valida e limiti configurati. |
| `REQ-005`, `REQ-006` | Factory runtime di ruoli AI orchestrati senza registry persistente di dominio. |
| `REQ-007`, `NFR-001` | Streaming/eventi progressivi dal backend alla UI. |
| `REQ-008`, `REQ-009`, `REQ-010`, `NFR-002` | Orchestrator con stato di sessione richiesta, limiti, arresto e sintesi finale. |
| Evoluzione Judge | Separazione tra orchestrazione del dibattito e valutazione qualitativa strutturata; il Judge e' integrato come quality gate post-sintesi e resta consultivo rispetto a sicurezza e limiti. |
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
| Provider LLM esterno via Spring AI | OpenAI selezionato come primo provider tramite `DEC-004`; il fake resta default operativo, mentre `SpringAiAdapter` e' opt-in. |

Dipendenze applicative previste: starter web, Spring AI OpenAI starter, starter test. Non introdurre database, code, broker, RAG, tool calling o autenticazione finche' non richiesti.

## 3. Architettura applicativa

Layer:

| Layer | Responsabilita' | Dipendenze consentite |
| --- | --- | --- |
| `web` | Controller, pagina singola, DTO di input/output, stream eventi, mapping errori utente. | Dipende da `application`; non contiene logica di dominio. |
| `application` | Use case `RunArenaSession`, coordinamento validazione -> planning -> team -> dibattito -> sintesi. | Dipende da `domain` e porte in `ai`; non dipende da dettagli web. |
| `domain` | Modello e regole: domanda, esito validazione, piano, esperto AI orchestrato, messaggio, sessione, limiti, decisione supervisore e giudizio Judge. | Nessuna dipendenza da Spring, web o provider esterni. |
| `ai` | Porte e adapter Spring AI per chiamate LLM strutturate. | Implementa interfacce usate da `application`; non conosce controller. |
| `config` | Properties tipizzate, limiti, bean applicativi. | Puo' cablare layer; non contiene logica di business. |

Regole:
- il flusso applicativo e' orchestrato da un solo use case principale;
- i componenti AI restituiscono risultati strutturati, non testo libero da interpretare a valle quando serve controllo;
- i limiti vengono applicati sia in pianificazione sia durante il dibattito;
- il dominio non deve importare framework;
- lo streaming espone stati/eventi, non dettagli interni o prompt completi.
- il Supervisor controlla il flusso del dibattito; il Judge valuta qualita' con rubrica esplicita dopo la sintesi finale e non sostituisce limiti, validazione o policy applicative.

Stato attuale:
- `SupervisorAiPort` espone oggi sia `decide(...)` per prossimo turno/arresto sia `synthesize(...)` per produrre la risposta finale;
- `DebateOrchestrator` usa il Supervisor per il controllo sequenziale del dibattito;
- `FinalAnswerService` delega la sintesi finale al `SupervisorAiPort`;
- `JudgeRequest`, `Judgement`, `JudgeVerdict`, `JudgeRubric`, `JudgeAiPort` e `JudgeService` esistono come quality gate post-sintesi separato;
- `SpringAiAdapter` implementa le porte AI via Spring AI `ChatModel` con output JSON strutturati e validati;
- `arena.ai.adapter=fake` mantiene il comportamento locale/test, mentre `arena.ai.adapter=openai` abilita l'adapter reale insieme a `spring.ai.model.chat=openai` e API key server-side;
- lo stream SSE espone l'evento `JUDGEMENT` prima di `FINAL_ANSWER`.

Direzione evolutiva:
- mantenere il Supervisor responsabile di orchestrazione, limiti, loop, timeout e scelta del prossimo esperto AI orchestrato;
- introdurre un Judge separato per valutazione qualitativa di contributi, dibattito o risposta finale;
- lasciare al Supervisor la facolta' di usare un giudizio strutturato per continuare, revisionare, accettare o fermare, solo dopo test dedicati.

## 4. Struttura progetto

Package base: `com.marnone.ai_arena`.

```text
src/main/java/com/marnone/ai_arena/
  AiArenaApplication.java
  web/
    ArenaController
    ArenaQuestionRequest
    ArenaErrorResponse
  application/
    RunArenaSessionUseCase
    ArenaSessionResult
    SessionEvent / SessionEventMapper
    ValidationService
    PlanningService
    OrchestratedAiExpertFactory
    DebateOrchestrator
    SupervisorService
    FinalAnswerService
    JudgeService
  domain/
    Question
    ValidationResult
    RequestClassification
    TeamPlan
    OrchestratedAiExpert
    DebateMessage
    SupervisorDecision
    FinalAnswer
    JudgeRequest / Judgement / JudgeVerdict / JudgeRubric
    ArenaLimits
    ArenaSessionState
  ai/
    AiClientPort
    ValidationAiPort
    PlanningAiPort
    OrchestratedAiExpertAiPort
    DebateAiPort
    SupervisorAiPort
    JudgeAiPort
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
| `OrchestratedAiExpertFactory` | Creare esperti AI orchestrati coerenti e distinguibili. | `REQ-005`, `REQ-006` |
| `DebateOrchestrator` | Gestire turni, messaggi progressivi e stato dibattito. | `REQ-007`, `REQ-008`, `REQ-009` |
| `SupervisorService` | Decidere prossimo turno, arresto e ragione. | `REQ-008`, `NFR-002` |
| `FinalAnswerService` | Produrre sintesi motivata dal dibattito. | `REQ-010` |
| `JudgeAiPort` | Esporre una valutazione qualitativa strutturata separata dal Supervisor. | Evoluzione Judge |
| `JudgeService` | Valutare la risposta finale tramite rubrica esplicita e output strutturato validato, applicando accept/revise/reject e fallback controllato. | Evoluzione Judge |
| `ArenaProperties` | Esporre limiti configurabili. | `REQ-009` |
| `SpringAiAdapter` | Incapsulare Spring AI e provider LLM. | `REQ-002`-`REQ-010` |

## 6. Modello dati

Nessuna persistenza richiesta nell'MVP. Il modello e' in memoria per durata della richiesta.

| Entita' | Campi essenziali | Relazioni |
| --- | --- | --- |
| `Question` | `text`, `submittedAt` | Input della sessione. |
| `ValidationResult` | `status`, `reason`, `classificationHint` | Precede piano; se rifiutato chiude il flusso. |
| `RequestClassification` | `domain`, `intent`, `confidence`, `notes` | Guida il piano. |
| `TeamPlan` | `skills`, `expertCount`, `roles`, `initialStrategy` | Origina esperti AI orchestrati. |
| `OrchestratedAiExpert` | `id`, `name`, `role`, `personality`, `mission`, `uiAccent` | Autore dei messaggi e identita' UI stabile. |
| `DebateMessage` | `id`, `expertId`, `turn`, `type`, `content`, `createdAt` | Appartiene alla sessione. |
| `SupervisorDecision` | `action`, `reason`, `nextExpertId` | Controlla turni/arresto. |
| `FinalAnswer` | `content`, `rationale`, `stopReason` | Output finale. |
| `JudgeRequest` | `question`, `messages`, `finalAnswer`, `evaluationTarget` | Input strutturato per una valutazione qualitativa. |
| `Judgement` | `verdict`, `rubric`, `reason`, `revisionHints` | Output validato del Judge. |
| `JudgeVerdict` | `ACCEPT`, `REVISE`, `REJECT` | Esito sintetico del giudizio. |
| `JudgeRubric` | `relevance`, `correctness`, `completeness`, `clarity`, `safety`, `overall` | Rubrica esplicita della valutazione con score 1-5. |
| `ArenaLimits` | `maxExperts`, `maxTurns`, `maxMessages`, `timeout` | Vincola piano e dibattito. |
| `ArenaSessionState` | `sessionId`, `question`, `team`, `messages`, `status`, `limits` | Aggregato runtime non persistente. |

## 7. Flussi applicativi

Flusso richiesta valida:

```text
Utente -> ArenaController -> RunArenaSessionUseCase
  -> ValidationService
  -> PlanningService
  -> OrchestratedAiExpertFactory
  -> DebateOrchestrator
      -> OrchestratedAiExpertAiPort*
      -> SupervisorService after each turn
  -> FinalAnswerService
  -> JudgeService
  -> ArenaController streams events to UI
```

Flusso Judge post-sintesi:

```text
FinalAnswerService -> JudgeService
  -> JudgeAiPort
  -> Judgement ACCEPT | REVISE | REJECT
  -> accept final answer, add controlled revision note, or replace with controlled rejection message
```

Il Judge e' inserito prima della consegna della risposta finale. In una fase successiva potra' diventare segnale consultivo per il Supervisor durante il dibattito. In entrambi i casi il giudizio e' input al controllo applicativo, non autorita' unica: limiti, sicurezza e fallback restano nel sistema.

Eventi stream minimi:

```text
VALIDATION_STARTED
VALIDATION_ACCEPTED | VALIDATION_REJECTED
TEAM_PLANNED
EXPERT_CREATED
DEBATE_MESSAGE
SUPERVISOR_DECISION
JUDGEMENT
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
- loggare solo quando il dato aiuta diagnosi, troubleshooting, audit tecnico o comprensione dello stato operativo;
- loggare eventi applicativi significativi, confini del sistema, integrazioni esterne, decisioni operative rilevanti ed errori gestibili;
- includere stato, componente, durata, esito e ragione di arresto quando sono utili al contesto;
- usare livelli coerenti con lo stack: `DEBUG` solo per diagnostica locale non sensibile, `INFO` per stati principali, `WARN` per condizioni gestite anomale, `ERROR` per fallimenti inattesi o non recuperabili;
- usare correlation/request id per flussi multi-step, API o operazioni asincrone quando aiutano a ricostruire la richiesta;
- evitare log rumorosi, duplicati, temporanei o banali, incluse semplici tracce di ingresso/uscita metodo;
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
| Judge non disponibile | Consegnare la sintesi con giudizio fallback `ACCEPT`, rubrica neutra e `fallbackApplied=true`; non perdere la ragione di arresto del Supervisor. |
| Output Judge invalido | Scartare il giudizio, registrare evento tecnico minimizzato e usare fallback controllato. |
| Errore inatteso | Evento errore generico, log tecnico correlato. |

## 9. Configurazione

Properties applicative:

| Proprieta' | Scopo |
| --- | --- |
| `arena.limits.max-experts` | Limite massimo esperti AI orchestrati. |
| `arena.limits.max-turns` | Limite massimo turni. |
| `arena.limits.max-messages` | Limite massimo messaggi. |
| `arena.limits.timeout` | Durata massima sessione. |
| `arena.limits.max-input-characters` | Lunghezza massima della domanda utente dopo parsing JSON. |
| `arena.http.max-payload-bytes` | Dimensione massima del payload HTTP per apertura sessione. |
| `arena.http.rate-limit-max-requests` | Numero massimo di richieste per finestra e indirizzo remoto. |
| `arena.http.rate-limit-window` | Durata della finestra rate limit in-process. |
| `arena.ai.provider` | Provider LLM selezionato. |
| `arena.ai.adapter` | Adapter AI runtime; default `fake`, `openai` abilita `SpringAiAdapter`. |
| `arena.ai.model` | Modello LLM selezionato. |
| `arena.ai.request-timeout` | Timeout applicativo massimo previsto per chiamata provider. |
| `arena.ai.temperature.*` | Parametri per validazione, planning, dibattito, sintesi se necessari. |
| `spring.ai.model.*` | Modalita' model Spring AI; default `none` per chat, embedding, image, audio e moderation finche' l'adapter OpenAI non viene abilitato. |
| `spring.ai.openai.chat.model` | Modello OpenAI usato quando la chat Spring AI viene abilitata. |
| `spring.ai.retry.*` | Retry limitati per errori transitori del provider. |

Segreti:
- API key e credenziali solo via variabili d'ambiente o secret manager dell'ambiente di deploy;
- non salvare segreti in repository;
- per OpenAI usare API key server-side; non sono richieste password ChatGPT o credenziali browser;
- non abilitare `arena.ai.adapter=openai` senza `spring.ai.model.chat=openai` e secret server-side configurato;
- dettagli di rotazione, storage e policy in `docs/security.md`.

## 10. Performance e scalabilita'

Misure giustificate:
- applicare limiti prima di avviare chiamate AI costose (`REQ-009`);
- usare SSE per inviare feedback progressivo (`NFR-001`);
- mantenere stato solo in memoria per MVP, liberandolo a fine sessione;
- eseguire il dibattito in sequenza per leggibilita' e controllo; ruoli AI paralleli sono evoluzione futura;
- impostare timeout per chiamate AI e sessione completa;
- evitare database finche' non servono cronologia o memoria.

Punto aperto: target numerici di latenza e concorrenza non definiti nei requisiti; registrarli in `docs/decisions.md` quando disponibili.

## 11. Testing strategy

| Tipo | Oggetto | Criteri |
| --- | --- | --- |
| Unit domain | `ArenaLimits`, `ArenaSessionState`, decisioni e invarianti | Nessuna dipendenza Spring o AI. |
| Unit application | Validazione, planning, factory, orchestrator, supervisor, sintesi | AI ports mockate; coprire rifiuto, successo, limite, timeout, errore. |
| Unit/application | JudgeService e integrazione post-sintesi | AI ports mockate; coprire verdict, revisione, reject e fallback. |
| Integration web | Controller e stream eventi | Verificare ordine eventi, error mapping e risposta finale. |
| Contract AI adapter | Parsing output strutturato e fallback | Test con risposte simulate del provider. |
| End-to-end light | Richiesta valida e richiesta rifiutata | Provider AI fake/deterministico. |

Test minimi tracciati:
- `REQ-001`: domanda vuota/non vuota;
- `REQ-002`: valida/rifiutata/ostile;
- `REQ-004`: piano con ruoli e limiti;
- `REQ-005`: esperti AI orchestrati completi;
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

Documentazione del codice:
- commenti e Javadoc nel codice sono in inglese;
- usare Javadoc breve sulle classi applicative principali quando aiuta a chiarire ruolo, confini o uso;
- commentare solo metodi o passaggi con logica non immediata, vincoli di dominio, trade-off, assunzioni non ovvie o comportamenti sorprendenti;
- non commentare getter/setter, assegnazioni o codice gia' evidente dai nomi;
- preferire nomi chiari a commenti esplicativi quando possibile.

README.md:
- e' la guida d'ingresso per umani;
- contiene solo informazioni operative utili a un nuovo lettore: scopo, stack, prerequisiti, comandi di avvio/test/build, configurazione essenziale senza segreti, struttura progetto, entry point principali e rimandi a `docs/`;
- non duplica requirements, architecture, security, tasks, decisions o workflow.

## 13. Decisioni architetturali

| Decisione | Motivazione | Alternative | Impatto | Registrare in `docs/decisions.md` |
| --- | --- | --- | --- | --- |
| Monolite Spring Boot | MVP piccolo, singola UI, orchestrazione centralizzata. | Microservizi. | Minore complessita', deploy semplice. | Si, come ADR iniziale. |
| SSE per progress updates | Requisito unidirezionale di osservabilita'. | Polling, WebSocket. | Meno complessita' di WebSocket, migliore UX del polling. | Si. |
| Stato in memoria, nessun DB | Cronologia e memoria escluse dall'MVP. | Database relazionale/documentale. | Semplice, ma sessioni non recuperabili dopo restart. | Si. |
| Porte AI mockabili | Testabilita' e isolamento provider. | Chiamate dirette Spring AI nei servizi. | Maggiore controllo nei test e sostituibilita'. | Si. |
| Separazione Supervisor/Judge | Evitare mescolare orchestrazione, sintesi e valutazione qualitativa. | Lasciare tutto in `SupervisorAiPort`. | Maggiore testabilita' e roadmap per quality gate strutturato. | Si. |
| Dibattito sequenziale | Chiarezza, controllo limiti, ruoli AI paralleli esclusi dall'MVP. | Esecuzione parallela. | Meno throughput, piu' prevedibilita'. | Si. |
| Frontend vanilla servito dal backend | Schermata unica e nessun requisito di SPA complessa. | React/Vue/Angular. | Meno dipendenze, UI sufficiente per demo. | Si. |
| Provider LLM OpenAI | Supportato direttamente da Spring AI e sufficiente per MVP reale controllato. | Anthropic, Gemini, Azure OpenAI, Ollama, solo fake. | Adapter reale opt-in; segreti server-side e fake default preservato. | `DEC-004` |

Punti aperti:
- valori iniziali dei limiti `max-experts`, `max-turns`, `max-messages`, `timeout`;
- target numerici di performance/concorrenza;
- eventuale uso consultivo del Judge nel Supervisor durante il dibattito.
