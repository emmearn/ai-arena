# Tasks

Fonte di verita' dell'ordine di lavoro per AI Arena. Le attivita' sono organizzate in vertical slice verificabili e collegate a requisiti, architettura, sicurezza e design.

## 1. Strategia MVP

MVP minimo:
- domanda utente valida/non valida;
- validazione e rifiuto sicuro;
- piano team domain-agnostic;
- specialisti runtime distinguibili;
- dibattito progressivo con limiti;
- sintesi finale motivata;
- schermata unica coerente con design system;
- error handling, logging, timeout e segreti sicuri.

Obiettivo: ogni milestone lascia l'app eseguibile e testabile, anche usando provider AI fake/deterministico finche' il provider reale non e' deciso.

## 2. Criteri di ordinamento

1. Valore utente: prima il percorso domanda -> dibattito osservabile -> risposta.
2. Dipendenze tecniche: dominio e use case prima di UI e provider reale.
3. Riduzione rischio: validazione, limiti, output handling e fake AI prima dell'integrazione LLM.
4. Verifica semplice: ogni task ha test o controllo manuale oggettivo.
5. Vertical slices: evitare layer completi non utilizzabili.

## 3. Milestone incrementali

| Milestone | Risultato funzionante | Task |
| --- | --- | --- |
| M1 - Base eseguibile | App parte, struttura package, configurazione limiti, dominio testato. | `TASK-001`-`TASK-004` |
| M2 - Flusso core fake | Domanda valida produce team, dibattito e sintesi deterministici. | `TASK-005`-`TASK-010` |
| M3 - Web e streaming | Endpoint e SSE espongono eventi progressivi e stati errore. | `TASK-011`-`TASK-014` |
| M4 - UI MVP | Schermata unica usabile, accessibile e coerente col design. | `TASK-015`-`TASK-019` |
| M5 - AI reale controllata | Adapter LLM configurabile, output validato, segreti sicuri. | `TASK-020`-`TASK-024` |
| M6 - Hardening | Test, logging, rate limit, edge case e demo readiness. | `TASK-025`-`TASK-030` |

## 3.1 Stato avanzamento

Stati: `TODO`, `IMPLEMENTED`, `VERIFIED_STATIC`, `BLOCKED_RUNTIME`, `DONE`.

| Task | Stato | Evidenza | Note |
| --- | --- | --- | --- |
| `TASK-001` | `DONE` | Package principali creati; wrapper Windows corretto; `mvnw clean test` passa. | Verificato con Java 21.0.11. |
| `TASK-002` | `DONE` | `ArenaProperties`, default applicativi e test sorgente aggiunti; `mvnw clean test` passa. | Verificato con Java 21.0.11. |
| `TASK-003` | `DONE` | Modello dominio creato; test dominio eseguiti da Maven. | Verificato con Java 21.0.11. |
| `TASK-004` | `DONE` | Validazioni di dominio su domanda e limiti presenti; test eseguiti da Maven. | Verificato con Java 21.0.11. |
| `TASK-005` | `DONE` | Porte AI e `FakeAiAdapter` deterministico creati; test fake eseguiti da Maven. | Verificato con Java 21.0.11. |
| `TASK-006` | `DONE` | `ValidationService` con controlli locali su vuoto, lunghezza e prompt ostili; test eseguiti da Maven. | Verificato con Java 21.0.11. |
| `TASK-007` | `DONE` | `PlanningService` e planning fake domain-aware con fallback generale e limiti specialisti; test eseguiti da Maven. | Verificato con Java 21.0.11. |
| `TASK-008` | `TODO` | Non iniziato. | Prossimo task operativo. |

## 4. Task

| ID | Descrizione | Prio | Dipendenze | Requisiti | Riferimenti | Completamento | Verifica/test |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `TASK-001` | Creare struttura package prevista e mantenere app avviabile. | `MUST` | Nessuna | `NFR-006` | `docs/architecture.md#4-struttura-progetto` | Package principali presenti; app compila e parte. | Test smoke Spring esistente passa. |
| `TASK-002` | Definire properties tipizzate per limiti MVP. | `MUST` | `TASK-001` | `REQ-009` | `docs/architecture.md#9-configurazione`, `docs/security.md#5-input-validation-e-output-handling` | Limiti max specialisti, turni, messaggi, timeout e max input configurabili con default. | Unit test binding/validazione limiti. |
| `TASK-003` | Implementare domain model minimo in memoria. | `MUST` | `TASK-001` | `REQ-001`-`REQ-010` | `docs/architecture.md#6-modello-dati` | Tipi domain essenziali rappresentano domanda, validazione, piano, specialisti, messaggi, decisione, risposta, stato. | Unit test costruttori/invarianti base. |
| `TASK-004` | Implementare invarianti dominio su input vuoto e limiti. | `MUST` | `TASK-002`, `TASK-003` | `REQ-001`, `REQ-009` | `docs/requirements.md#8-input-output-e-validazioni-di-dominio` | Input vuoto e limiti non validi non producono sessione valida. | Unit test input vuoto, limiti zero/negativi, superamento max. |
| `TASK-005` | Creare porte AI e adapter fake deterministico. | `MUST` | `TASK-003` | `REQ-002`-`REQ-010` | `docs/architecture.md#3-architettura-applicativa` | Interfacce AI mockabili; fake produce validazione, piano, messaggi e sintesi prevedibili. | Unit test fake con domanda valida e rifiutata. |
| `TASK-006` | Implementare `ValidationService` con controlli locali minimi. | `MUST` | `TASK-004`, `TASK-005` | `REQ-002`, `NFR-003` | `docs/security.md#5-input-validation-e-output-handling` | Domanda vuota/troppo lunga/ostile nota viene rifiutata prima del dibattito. | Unit test valida, vuota, oltre limite, pattern injection/jailbreak di base. |
| `TASK-007` | Implementare classificazione e pianificazione fake coerenti coi limiti. | `MUST` | `TASK-005`, `TASK-006` | `REQ-003`, `REQ-004`, `REQ-012` | `docs/architecture.md#5-componenti-principali` | Piano contiene competenze, numero, ruoli, strategia e rispetta max specialisti. | Unit test domini diversi, dominio incerto, limite specialisti. |
| `TASK-008` | Implementare factory specialisti runtime. | `MUST` | `TASK-007` | `REQ-005`, `REQ-006` | `docs/design.md#3-personalita-visiva-degli-agenti` | Ogni specialista ha id, nome, ruolo, personalita', missione e accento UI stabile. | Unit test completezza e distinzione specialisti. |
| `TASK-009` | Implementare orchestrator sequenziale del dibattito. | `MUST` | `TASK-008` | `REQ-007`, `REQ-008`, `REQ-009` | `docs/architecture.md#7-flussi-applicativi` | Genera messaggi ordinati, applica turni/messaggi/timeout, produce ragione stop. | Unit test ordine turni, limite messaggi, limite turni, timeout simulato. |
| `TASK-010` | Implementare sintesi finale fake e use case end-to-end. | `MUST` | `TASK-009` | `REQ-010`, `NFR-002` | `docs/architecture.md#5-componenti-principali` | Richiesta valida termina con risposta finale; rifiuto non crea team. | Unit test use case valido, rifiutato, stop per limite. |
| `TASK-011` | Definire eventi applicativi di sessione. | `MUST` | `TASK-010` | `REQ-007`, `REQ-011` | `docs/architecture.md#7-flussi-applicativi` | Eventi minimi disponibili: validation, team, specialist, message, decision, final, error. | Unit test mapping stato -> evento. |
| `TASK-012` | Implementare endpoint invio domanda con stream SSE. | `MUST` | `TASK-011` | `REQ-001`, `REQ-007`, `NFR-001` | `docs/architecture.md#2-stack-tecnologico` | Endpoint accetta domanda, emette eventi ordinati e chiude stream a fine sessione. | Integration test SSE con fake AI. |
| `TASK-013` | Implementare error mapping web sicuro. | `MUST` | `TASK-012` | `NFR-004` | `docs/security.md#6-logging-ed-error-handling` | Errori utente e interni producono messaggi standard senza stacktrace. | Integration test input invalido, errore fake provider, nessuno stacktrace in response. |
| `TASK-014` | Applicare limiti HTTP e rate limit minimo. | `MUST` | `TASK-012` | `REQ-009`, `NFR-003` | `docs/security.md#9-api-e-integrazioni` | Payload troppo grande o richieste eccessive vengono rifiutati in modo controllato. | Integration test limite payload; test/manuale rate limit. |
| `TASK-015` | Creare pagina unica base con asset logo. | `MUST` | `TASK-012` | `REQ-011`, `NFR-007` | `docs/design.md`, `assets/logo/` | Pagina caricabile con logo, input, area stato, team, dibattito, sintesi. | Test/manuale browser: pagina carica senza errori console critici. |
| `TASK-016` | Implementare input domanda e stati validazione UI. | `MUST` | `TASK-015` | `REQ-001`, `REQ-002` | `docs/design.md#7-componenti-ui` | Invio da tastiera/click; errore vuoto vicino al campo; stato accepted/rejected visibile. | UI test domanda vuota e domanda valida. |
| `TASK-017` | Implementare rendering team e identita' agenti. | `MUST` | `TASK-016` | `REQ-005`, `REQ-006` | `docs/design.md#3-personalita-visiva-degli-agenti` | Card agenti con avatar/colore/ruolo coerenti e non tabellari. | UI test presenza agenti; controllo visuale responsive. |
| `TASK-018` | Implementare rendering dibattito progressivo e turno attivo. | `MUST` | `TASK-017` | `REQ-007`, `NFR-001` | `docs/design.md#11-animazioni-e-microinterazioni` | Messaggi appaiono progressivamente, associati ad agente, senza HTML eseguibile. | UI/integration test evento message; test XSS renderizzato come testo. |
| `TASK-019` | Implementare sintesi finale, stop reason ed errori UI. | `MUST` | `TASK-018` | `REQ-008`, `REQ-010`, `REQ-011` | `docs/design.md#9-ux-guidelines`, `docs/security.md#10-frontend-e-ux-di-sicurezza` | Giudice/sintesi distinguibile; stop/errori comprensibili e non tecnici. | UI test final answer, validation rejected, timeout/limite fake. |
| `TASK-020` | Registrare decisione provider/modello LLM. | `MUST` | `TASK-010` | `REQ-002`-`REQ-010` | `docs/architecture.md#13-decisioni-architetturali`, `docs/security.md#9-api-e-integrazioni` | Decisione pronta per `docs/decisions.md` con provider, modello, motivazione, alternative, impatti. | Review documentale; nessun codice richiesto. |
| `TASK-021` | Aggiungere dipendenze Spring AI minime per provider scelto. | `MUST` | `TASK-020` | `REQ-002`-`REQ-010` | `docs/architecture.md#2-stack-tecnologico` | Build usa solo starter necessari; fake resta disponibile per test. | Build e test passano. |
| `TASK-022` | Implementare adapter Spring AI per validazione e planning. | `MUST` | `TASK-021` | `REQ-002`, `REQ-003`, `REQ-004` | `docs/security.md#5-input-validation-e-output-handling` | Output AI validato e trasformato in tipi domain; fallback sicuro su output malformato. | Contract test con risposte simulate valide/malformate. |
| `TASK-023` | Implementare adapter Spring AI per dibattito, supervisione e sintesi. | `MUST` | `TASK-022` | `REQ-007`, `REQ-008`, `REQ-010` | `docs/architecture.md#7-flussi-applicativi` | Messaggi/decisioni/sintesi rispettano schema e limiti; errori chiudono in modo controllato. | Contract test output valido, stop, malformed, provider error. |
| `TASK-024` | Configurare segreti e timeout provider senza esposizione frontend. | `MUST` | `TASK-021` | `NFR-003`, `NFR-004` | `docs/security.md#2-segreti`, `docs/security.md#7-comunicazioni` | API key solo env; timeout configurato; nessun segreto in log/UI/repo. | Test config senza key fallisce sicuro; grep manuale segreti placeholder. |
| `TASK-025` | Implementare logging con correlation id. | `MUST` | `TASK-013` | `NFR-004`, `NFR-006` | `docs/security.md#6-logging-ed-error-handling` | Log includono session id, componente, esito, durata; non includono prompt completi/segretI. | Test/log capture su flusso valido e rifiutato. |
| `TASK-026` | Rafforzare test di sicurezza input/output. | `MUST` | `TASK-018`, `TASK-023` | `REQ-002`, `NFR-003` | `docs/security.md#13-minacce-mitigazioni-e-rischi-residui` | Coperti injection base, jailbreak base, XSS in output, output AI malformato. | Suite unit/integration dedicata passa. |
| `TASK-027` | Verificare accessibilita' UI MVP. | `SHOULD` | `TASK-019` | `NFR-005` | `docs/design.md#10-accessibilita` | Focus visibile, tastiera base, contrasto, motion riducibile, testi lunghi senza overflow. | Checklist manuale + eventuale test UI su keyboard path. |
| `TASK-028` | Verificare responsive e demo readiness. | `SHOULD` | `TASK-019` | `REQ-011` | `docs/design.md#12-consistenza-visiva` | UI leggibile su desktop/laptop/mobile; nessun overlap su contenuti lunghi. | Screenshot/controllo manuale viewport principali. |
| `TASK-029` | Eseguire hardening dipendenze e build. | `MUST` | `TASK-024` | `NFR-006` | `docs/security.md#12-dipendenze` | Dipendenze minime, build pulita, nessuna libreria non motivata. | Build/test completi; review `pom.xml`. |
| `TASK-030` | Preparare smoke test end-to-end MVP. | `MUST` | `TASK-029` | Tutti i `REQ-*` MVP | Tutti i documenti | Scenario valido e scenario rifiutato verificabili con fake e, se configurato, provider reale. | E2E light o checklist eseguibile documentata nel test. |

## 5. Vertical slices

| Slice | Include | Esclude |
| --- | --- | --- |
| Validazione visibile | Input, validation service, evento, UI stato, test. | Team, dibattito, AI reale. |
| Dibattito fake | Piano, specialisti, messaggi, supervisione, sintesi deterministica. | Provider reale. |
| Streaming osservabile | SSE, eventi ordinati, UI progressiva, error mapping. | Ottimizzazioni avanzate. |
| AI controllata | Adapter provider, output validation, segreti, timeout, fallback. | Memoria, RAG, tool calling. |
| Demo hardening | Accessibilita', responsive, logging, rate limit, smoke test. | Funzionalita' future. |

## 6. Testing

Priorita' test:
- unit domain/application per invarianti, limiti, validazione, orchestrazione;
- integration web per endpoint, SSE, error mapping;
- contract AI adapter con risposte simulate;
- UI test per input, stati, rendering sicuro, sintesi;
- smoke E2E con provider fake deterministico.

Regola: nessun task `MUST` e' completo senza verifica indicata nel task.

## 7. Sicurezza integrata

Task con sicurezza obbligatoria:
- `TASK-006`: validazione input ostile;
- `TASK-013`: errori senza disclosure;
- `TASK-014`: rate limit e payload limit;
- `TASK-018`: output AI renderizzato come testo;
- `TASK-024`: segreti e timeout provider;
- `TASK-025`: log minimizzati;
- `TASK-026`: test minacce note;
- `TASK-029`: dipendenze minime e mantenute.

## 8. Debito tecnico

| ID | Rinvio accettabile | Motivo | Trigger di rientro |
| --- | --- | --- | --- |
| `DEBT-001` | Provider AI fake come default in test e demo locale. | Riduce rischio finche' provider/modello non sono decisi. | Decisione provider registrata e segreti disponibili. |
| `DEBT-002` | Nessun database e nessuna cronologia. | Esclusi dall'MVP. | Requisito approvato per memoria o storico. |
| `DEBT-003` | Rate limit semplice in-process. | Sufficiente per MVP/demo. | Deploy pubblico con traffico reale o piu' istanze. |
| `DEBT-004` | Scoring qualita' risposta assente. | Escluso dai requisiti MVP. | Requisito approvato di valutazione automatica. |

## 9. Funzionalita' future

| ID | Evoluzione | Dipendenza |
| --- | --- | --- |
| `FUTURE-001` | Cronologia conversazioni. | Requisiti privacy, database, auth/sessioni. |
| `FUTURE-002` | Memoria persistente. | Privacy, retention, modello dati persistente. |
| `FUTURE-003` | Tool calling / MCP. | Security review integrazioni e autorizzazioni tool. |
| `FUTURE-004` | RAG. | Data governance, fonti, privacy, modello dati. |
| `FUTURE-005` | Agenti paralleli. | Nuova architettura concorrenza e gestione costi. |
| `FUTURE-006` | Votazione strutturata. | Nuovi requisiti di decisione/supervisione. |
| `FUTURE-007` | Modelli differenti per agenti. | Decisione costi, provider, fallback. |

## 10. Tracciabilita'

| Requisito/decisione | Task |
| --- | --- |
| `REQ-001` | `TASK-004`, `TASK-012`, `TASK-016` |
| `REQ-002` | `TASK-006`, `TASK-013`, `TASK-022`, `TASK-026` |
| `REQ-003` | `TASK-007`, `TASK-022` |
| `REQ-004` | `TASK-007`, `TASK-022` |
| `REQ-005` | `TASK-008`, `TASK-017` |
| `REQ-006` | `TASK-008`, `TASK-017` |
| `REQ-007` | `TASK-009`, `TASK-011`, `TASK-012`, `TASK-018`, `TASK-023` |
| `REQ-008` | `TASK-009`, `TASK-019`, `TASK-023` |
| `REQ-009` | `TASK-002`, `TASK-004`, `TASK-009`, `TASK-014` |
| `REQ-010` | `TASK-010`, `TASK-019`, `TASK-023` |
| `REQ-011` | `TASK-015`-`TASK-019`, `TASK-027`, `TASK-028` |
| `REQ-012` | `TASK-007`, `TASK-022` |
| Provider/modello LLM | `TASK-020`, `TASK-021`, `TASK-024` |
| Limiti iniziali | `TASK-002`, decisione futura in `docs/decisions.md` |
| Rate limit/CORS/retry | `TASK-014`, `TASK-020`, decisione futura in `docs/decisions.md` |
