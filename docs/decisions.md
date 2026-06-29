# Decisions

Fonte di verita' per decisioni significative di prodotto, architettura, sicurezza e processo.

## DEC-001 - Adopt Document-Driven Workflow

| Campo | Contenuto |
| --- | --- |
| Stato | Accepted |
| Data | 2026-06-27 |
| Contesto | Il progetto era stato impostato con Dynamic Agent-Driven Workflow e una cartella `agents/` contenente ruoli operativi di sviluppo persistenti. |
| Decisione | AI Arena usa Document-Driven Workflow: `docs/` e' l'unica fonte di verita' operativa per l'AI coding assistant, che assume ruoli dinamici per il task corrente senza file di agenti di sviluppo persistenti. |
| Motivazione | Ridurre duplicazione e manutenzione del workflow, evitare proliferazione di ruoli statici e mantenere il processo governato dalla documentazione canonica. |
| Alternative | Mantenere Dynamic Agent-Driven Workflow; creare altri agenti specializzati persistenti; tornare a un Agent-Driven Workflow statico. |
| Impatti | La cartella `agents/` viene rimossa dopo migrazione delle regole utili in `docs/workflow.md`. I riferimenti ai ruoli AI orchestrati dell'applicazione restano validi per il dominio di AI Arena. |
| Riferimenti | `docs/workflow.md` |

## DEC-002 - Separare Supervisor e LLM-as-a-Judge

| Campo | Contenuto |
| --- | --- |
| Stato | Accepted |
| Data | 2026-06-27 |
| Contesto | Nell'implementazione corrente `SupervisorAiPort` copre sia decisione di flusso del dibattito sia sintesi finale. Questo rende naturale chiamarlo "giudice" nella UI e nella narrazione prodotto, ma rischia di mescolare orchestrazione, sintesi e valutazione qualitativa. |
| Decisione | Il Supervisor resta responsabile del controllo del flusso: continua/ferma, prossimo esperto AI orchestrato, limiti, loop e timeout. LLM-as-a-Judge diventa una responsabilita' separata per valutazione qualitativa strutturata di contributi, dibattito o risposta finale tramite rubrica esplicita e output validato. |
| Motivazione | Separare le responsabilita' evita che un solo componente mescoli orchestrazione, sintesi e valutazione; migliora qualita', testabilita', affidabilita' e facilita l'introduzione incrementale di quality gate. |
| Alternative | Lasciare valutazione, sintesi e orchestrazione dentro `SupervisorAiPort`; introdurre subito refactor completo; usare solo euristiche deterministiche senza Judge LLM. |
| Impatti | Futuri task architetturali e test per `JudgeAiPort`, `JudgeService`, `JudgeRequest`, `Judgement`, `JudgeVerdict` e `JudgeRubric`; nessuna rimozione degli agenti runtime dell'applicazione; nessuna implementazione immediata richiesta. |
| Riferimenti | `docs/architecture.md`, `docs/tasks.md`, `docs/security.md`, `docs/design.md` |

## DEC-003 - Limiti HTTP MVP in-process

| Campo | Contenuto |
| --- | --- |
| Stato | Accepted |
| Data | 2026-06-27 |
| Contesto | L'MVP espone un endpoint pubblico per aprire sessioni SSE senza autenticazione o database. `TASK-014` richiede rifiuto controllato di payload troppo grandi e richieste eccessive. |
| Decisione | Applicare un filtro HTTP in-process su `POST /api/arena/sessions` con limite payload configurabile e rate limit per indirizzo remoto. I default sono `arena.http.max-payload-bytes=8192`, `arena.http.rate-limit-max-requests=20`, `arena.http.rate-limit-window=1m`. |
| Motivazione | I limiti proteggono risorse prima della validazione e prima di eventuali chiamate AI costose, restando semplici e senza nuove dipendenze per il monolite MVP. |
| Alternative | Nessun rate limit; rate limit via proxy esterno soltanto; introdurre una libreria dedicata o uno store distribuito. |
| Impatti | Sufficiente per demo e uso locale; non e' adatto da solo a deploy multi-istanza o traffico pubblico elevato. I valori vanno rivalutati quando saranno definiti provider LLM, costi e target di concorrenza. |
| Riferimenti | `docs/security.md#9-api-e-integrazioni`, `docs/tasks.md#task-014` |

## DEC-004 - Provider LLM OpenAI via Spring AI

| Campo | Contenuto |
| --- | --- |
| Stato | Accepted |
| Data | 2026-06-29 |
| Contesto | L'MVP usa un adapter AI fake deterministico; per proseguire verso AI reale serve scegliere un provider, un modello iniziale, dipendenze Spring AI minime e regole di configurazione dei segreti. |
| Decisione | Il primo provider LLM supportato e' OpenAI tramite Spring AI `spring-ai-starter-model-openai`. Il modello applicativo iniziale e' `gpt-5-mini`, configurato come default in `arena.ai.model` e `spring.ai.openai.chat.model`. Tutte le auto-configurazioni Spring AI model restano disabilitate di default con `spring.ai.model.*=none`, cosi' l'app continua a partire senza API key e il fake rimane il comportamento locale/test finche' gli adapter reali non sono implementati. |
| Motivazione | OpenAI e' supportato direttamente da Spring AI, e' compatibile con l'ecosistema ChatGPT/API, riduce il numero di dipendenze iniziali e permette di usare una sola integrazione per validazione, planning, dibattito, supervisione e sintesi. |
| Alternative | Anthropic, Google Gemini, Azure OpenAI, provider locali/Ollama, mantenere solo fake AI. Le alternative restano possibili tramite porte AI e configurazione futura, ma aumenterebbero subito variabilita' di SDK, costi, opzioni modello e test contract. |
| Impatti | Richiede API key OpenAI server-side tramite variabile d'ambiente/secret manager quando gli adapter reali verranno attivati; nessuna password ChatGPT o credenziale utente e' richiesta. I timeout applicativi partono da `arena.ai.request-timeout=30s`; retry Spring AI iniziale limitato a 2 tentativi. Nessuna API key viene salvata in repo o inviata al frontend. |
| Riferimenti | `docs/architecture.md#2-stack-tecnologico`, `docs/security.md#2-segreti`, `docs/tasks.md#task-020` |
