# Document-Driven Workflow

## 1. Workflow Overview

AI Arena usa un Document-Driven Workflow per il lavoro di coding, review e manutenzione. `docs/` e' l'unica fonte di verita' operativa per l'AI coding assistant: requisiti, architettura, sicurezza, task, design e decisioni guidano ogni intervento.

Non esistono agenti di sviluppo persistenti nel repository. L'AI coding assistant assume dinamicamente il ruolo necessario al task corrente, per esempio architect, developer, reviewer, security reviewer, tester, documentation maintainer o frontend implementer.

Regole base:
- `docs/` resta l'unica fonte di verita' per requirements, architecture, security, tasks, design, decisions e workflow.
- Ogni richiesta parte dalla lettura dei documenti rilevanti.
- Lo scope resta limitato alla richiesta o al task corrente.
- Non creare cartelle o file di agenti di sviluppo persistenti.
- Non confondere il workflow di coding con i ruoli AI orchestrati dell'applicazione: esperti AI orchestrati, supervisor, planner, costruttore ruoli runtime, debate orchestrator e componenti simili sono parte del dominio di AI Arena e restano documentati nei documenti di prodotto e architettura.

## 2. Startup Workflow

1. Ricevere la richiesta utente.
2. Identificare i documenti rilevanti in `docs/`.
3. Leggere sempre `docs/workflow.md` per il processo operativo.
4. Leggere, in base al task:
   - `docs/requirements.md` per comportamento atteso e vincoli di prodotto;
   - `docs/architecture.md` per struttura tecnica, package, componenti, logging e convenzioni;
   - `docs/security.md` per rischi, guardrail e pratiche vietate;
   - `docs/tasks.md` per roadmap, priorita', stato e verifiche richieste;
   - `docs/design.md` per UI/UX e frontend, se il task tocca interfaccia o asset visivi;
   - `docs/decisions.md` se presente, per decisioni gia' prese o da aggiornare.
5. Sintetizzare solo il contesto minimo necessario: vincoli, requisiti, task, decisioni e rischi applicabili.
6. Se i documenti sono incoerenti o insufficienti per procedere, proporre o applicare la correzione documentale minima prima dell'implementazione.

## 3. Dynamic Role Assumption

L'AI coding assistant assume ruoli temporanei e proporzionati, senza persisterli come file:

| Ruolo dinamico | Quando usarlo |
| --- | --- |
| Architect | Scelte strutturali, confini di layer, dipendenze, contratti applicativi. |
| Developer | Implementazione nello scope del task. |
| Reviewer | Verifica finale di bug, regressioni, duplicazioni, overengineering e coerenza documentale. |
| Security | Task che toccano input, output, AI, segreti, log, errori, endpoint, dipendenze o superfici pubbliche. |
| Tester | Definizione o aggiornamento di unit, integration, contract, UI o smoke test. |
| Frontend | UI, design system, accessibilita', responsive, stati visuali e rendering sicuro. |
| Documentation Maintainer | Aggiornamenti a workflow, requirements, architecture, security, tasks, design, README o decisions. |

Un singolo task puo' richiedere piu' ruoli dinamici. La responsabilita' resta unitaria: leggere i documenti, applicare modifiche minime, verificare e chiudere con report.

## 4. Development Workflow

1. Classificare task, scope, rischi, dipendenze e documenti impattati.
2. Verificare che la richiesta sia coerente con requirements, architecture, security e tasks.
3. Applicare modifiche nello scope minimo.
4. Preferire semplicita', leggibilita', manutenibilita' e testabilita'.
5. Evitare overengineering, duplicazioni, dipendenze inutili e funzionalita' non richieste.
6. Non cambiare API, modelli, comportamento o documenti di dominio senza requisito, task o decisione coerente.
7. Non modificare codice applicativo durante task puramente documentali.
8. Non duplicare contenuto di `docs/` dentro altri documenti; usare rimandi quando basta.
9. Se emergono decisioni significative su prodotto, architettura, sicurezza o processo, registrarle in `docs/decisions.md`.

## 5. Documentation Update Workflow

- Aggiornare `docs/tasks.md` solo se cambiano roadmap, stato, priorita', dipendenze, task o criteri di verifica.
- Aggiornare `docs/requirements.md` solo per cambiamenti del comportamento atteso di prodotto.
- Aggiornare `docs/architecture.md` solo per scelte tecniche, struttura, componenti, convenzioni o flussi applicativi.
- Aggiornare `docs/security.md` solo se cambia profilo di rischio, policy, guardrail o pratica vietata.
- Aggiornare `docs/design.md` quando cambiano UI/UX, design system, componenti visivi o regole frontend.
- Aggiornare `docs/decisions.md` per decisioni significative, includendo contesto, decisione, motivazione, alternative, impatti e stato.
- Aggiornare `README.md` solo secondo le regole della sezione dedicata.
- Non avanzare lo stato di task applicativi solo per modifiche documentali.

## 6. Code Documentation Policy

Applicare la policy di `docs/architecture.md#12-convenzioni-di-sviluppo`:

- commenti e Javadoc nel codice sono in inglese;
- usare Javadoc breve sulle classi applicative principali quando aiuta a chiarire ruolo, confini o uso;
- quando una classe applicativa principale e' completata o stabilizzata, verificare se serve una breve Javadoc;
- quando un flusso applicativo e' completato, rivedere entry point e componenti attraversati per aggiungere, aggiornare o rimuovere commenti dove utile;
- commentare solo logica non immediata, vincoli di dominio, trade-off, assunzioni o comportamenti sorprendenti;
- evitare commenti banali e contenuti gia' evidenti dai nomi.

## 7. Logging Workflow

Applicare `docs/architecture.md#8-error-handling-e-logging` e `docs/security.md#6-logging-ed-error-handling`:

- quando un flusso applicativo e' completato, rivedere entry point, confini del sistema, integrazioni, decisioni operative ed errori gestibili;
- aggiungere log solo se aiutano diagnosi, audit tecnico, troubleshooting o comprensione dello stato operativo;
- rimuovere log temporanei, rumorosi, duplicati o banali, incluse semplici tracce di ingresso/uscita metodo;
- non loggare segreti, prompt completi, input utente, output AI sensibili, payload completi o dati personali non necessari;
- verificare livelli, correlation/session id e minimizzazione dei dati;
- non creare task di logging artificiali e non rendere il logging obbligatorio per ogni task.

## 8. Testing Workflow

- Eseguire i test rilevanti per il cambiamento o indicare perche' non sono eseguibili.
- Per codice applicativo, seguire `docs/architecture.md#11-testing-strategy` e le verifiche di `docs/tasks.md`.
- I task `MUST` non sono completi senza verifica proporzionata.
- Preferire test unitari per logica domain/application, integration test per web/SSE, contract test per adapter AI e controlli UI/manuali per frontend quando richiesto.
- Per modifiche solo documentali, eseguire controlli statici mirati: coerenza dei riferimenti, assenza di residui obsoleti e nessuna modifica al codice.

## 9. Security Workflow

Assumere il ruolo Security quando il task tocca input, output, AI, prompt, segreti, log, errori, endpoint, dipendenze, filesystem, frontend rendering o superfici pubbliche.

Regole:
- leggere `docs/security.md` prima di modifiche con impatto di sicurezza;
- bloccare o correggere modifiche insicure;
- proporre controlli minimi e proporzionati;
- non disabilitare controlli senza decisione registrata;
- non introdurre segreti;
- validare output AI e contenuti utente prima dell'uso;
- mantenere errori utente non tecnici e log sicuri minimizzati;
- aggiornare `docs/security.md` o `docs/decisions.md` solo se cambia il profilo di rischio o una policy.

## 10. Frontend Workflow

Applicabile quando il task tocca UI, asset, stili, interazioni, accessibilita' o rendering browser.

- Leggere `docs/design.md` prima di implementare UI.
- Mantenere l'esperienza single-screen, leggibile, accessibile e coerente con il design system.
- Non introdurre framework visuali o librerie UI senza decisione motivata.
- Non usare card annidate, palette monocromatica o testi in-app che spiegano il design.
- Garantire stati hover, focus, disabled, loading e stati applicativi visibili quando applicabili.
- Renderizzare contenuti generati come testo sicuro.
- Verificare responsive, testi lunghi, assenza di overlap e accessibilita' quando il task modifica il frontend.

## 11. README Update Rules

Aggiornare `README.md` solo quando cambia l'esperienza di ingresso al progetto:

- setup o prerequisiti;
- comandi di avvio, test o build;
- configurazione richiesta;
- modalita' d'uso;
- funzionalita' principali o stato MVP;
- struttura progetto o entry point principali;
- informazioni necessarie a un nuovo lettore.

Mantenerlo sintetico, operativo e non duplicativo rispetto a `docs/`. Rimandare a `docs/` per requisiti, architettura, sicurezza, task, decisioni e workflow dettagliati.

## 12. Review And Closure Workflow

Prima della chiusura:

1. Verificare coerenza con requirements, architecture, security, tasks, design e decisions rilevanti.
2. Cercare bug, regressioni, duplicazioni, overengineering e modifiche fuori scope.
3. Verificare che documentazione, commenti, logging, README e test siano aggiornati solo quando rilevante.
4. Eseguire test o controlli statici proporzionati.
5. Distinguere chiaramente tra ruoli AI orchestrati dell'applicazione e agenti di sviluppo persistenti, se il task riguarda il workflow.

Il report finale deve includere, quando applicabile:
- file modificati;
- contenuto o decisioni migrate;
- file/cartelle eliminati;
- test o controlli eseguiti;
- residui noti o riferimenti mantenuti intenzionalmente;
- rischi residui.
