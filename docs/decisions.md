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
