# Dynamic Agent-Driven Workflow

## 1. Workflow Overview

Dynamic Agent-Driven Workflow coordina lavoro di coding e documentazione usando `docs/` come unica fonte di verita' e `agents/` come set minimo di agenti operativi. Gli agenti iniziali sono pochi, riutilizzabili e non contengono conoscenza di dominio: leggono i documenti necessari, applicano il task corrente e creano agenti specializzati solo quando serve.

Regole base:
- `docs/` resta l'unica fonte di verita' per requirements, architecture, security, tasks, design e decisions.
- `agents/` contiene solo agenti minimi iniziali e agenti dinamici eventualmente creati.
- Il comportamento operativo vive solo in `docs/workflow.md` e `agents/`.
- Questo workflow e' alternativo a Document-Driven Workflow e Agent-Driven Workflow statico; non li modifica e non li rigenera.
- La PoC Spring AI deve supportare agenti specializzati creati a runtime, senza trasformare il repository in un set statico di ruoli.

## 2. Core Agents

| Agente | Scopo |
| --- | --- |
| `agents/orchestrator-agent.md` | Punto di ingresso, coordinamento, scope, esecuzione, test e closure. |
| `agents/agent-factory-agent.md` | Creazione/proposta di agenti specializzati solo se necessari e riutilizzabili. |
| `agents/context-agent.md` | Lettura e sintesi minima del contesto documentale. |
| `agents/security-agent.md` | Verifica rischi, guardrail e vincoli di sicurezza. |
| `agents/review-agent.md` | Review finale di risultato, coerenza, regressioni, documentazione essenziale e agenti creati. |

## 3. Orchestrator Workflow

1. Ricevere la richiesta utente.
2. Usare Context Agent per leggere solo documentazione rilevante.
3. Classificare task, scope, rischi, dipendenze e documenti impattati.
4. Decidere se bastano agenti esistenti.
5. Coinvolgere Agent Factory solo se serve un agente specializzato riutilizzabile.
6. Coordinare esecuzione nello scope minimo.
7. Eseguire test rilevanti o indicare perche' non sono eseguibili.
8. Coinvolgere Security Agent per rischi o superfici toccate.
9. Coinvolgere Review Agent per coerenza finale, regressioni, duplicazioni e agenti creati.
10. Aggiornare `docs/tasks.md` solo se cambiano roadmap, stato, priorita' o dipendenze.
11. Aggiornare `docs/decisions.md` solo per decisioni significative.
12. Produrre risultato finale con modifiche, test, documenti aggiornati, agenti creati e rischi residui.

## 4. Agent Factory Workflow

1. Analizzare task, documenti rilevanti e agenti esistenti.
2. Verificare se un agente esistente copre adeguatamente il bisogno.
3. Creare o proporre un nuovo agente solo se necessario, riutilizzabile e con ambito chiaro.
4. Definire solo: Mission, Responsibilities, Read, Write, Forbidden Actions.
5. Evitare agenti troppo specifici, ridondanti, temporanei o basati su preferenze stilistiche.
6. Mantenere ogni agente compatto, prescrittivo e non duplicativo dei documenti.
7. Registrare in `docs/decisions.md` la creazione se significativa per il processo.

## 5. Dynamic Agent Creation Rules

Creare un agente solo se:
- il task richiede competenze ricorrenti e specializzate;
- nessun agente esistente copre adeguatamente il bisogno;
- l'agente puo' essere riutilizzato in task futuri;
- l'ambito e' chiaro, limitato e documentabile.

Non creare agenti per:
- task singoli e banali;
- modifiche locali;
- responsabilita' gia' coperte;
- preferenze stilistiche;
- duplicare documentazione di dominio;
- generare set statici come backend/frontend/database/devops senza motivazione documentale esplicita.

## 6. Documentation Workflow

- Gli agenti leggono sempre `docs/` prima di decidere.
- Context Agent sintetizza solo il contesto minimo necessario.
- Aggiornare `docs/tasks.md` solo se cambiano task, stato, priorita', dipendenze o roadmap.
- Aggiornare `docs/decisions.md` per decisioni significative su prodotto, architettura, sicurezza, processo o nuovi agenti.
- Aggiornare altri documenti solo secondo la loro responsabilita' specifica.
- Se i documenti sono incoerenti, proporre correzione documentale minima prima dell'implementazione.
- Non duplicare contenuto di `docs/` dentro `docs/workflow.md` o `agents/`.

Documentazione del codice:
- applicare la policy di `docs/architecture.md#12-convenzioni-di-sviluppo`;
- quando una classe applicativa principale e' completata o stabilizzata, verificare se serve una breve Javadoc in inglese;
- quando un flusso applicativo e' completato, rivedere entry point e componenti attraversati per aggiungere, aggiornare o rimuovere commenti dove utile;
- commentare solo logica non immediata, vincoli di dominio, trade-off, assunzioni o comportamenti sorprendenti;
- evitare commenti banali e contenuti gia' evidenti dai nomi.

README.md:
- aggiornare `README.md` solo quando cambia l'esperienza di ingresso al progetto: setup, prerequisiti, comandi di avvio/test/build, configurazione richiesta, modalita' d'uso, funzionalita' principali, stato MVP o informazioni necessarie a un nuovo lettore;
- mantenerlo sintetico, operativo e non duplicativo rispetto a `docs/`;
- rimandare a `docs/` per requisiti, architettura, sicurezza, task, decisioni e workflow dettagliati.

## 7. Execution Workflow

| Fase | Regola |
| --- | --- |
| Startup | Leggere documentazione rilevante. |
| Context | Produrre sintesi minima e vincoli applicabili. |
| Agent Selection | Usare agenti esistenti o coinvolgere Agent Factory. |
| Execution | Applicare modifiche nello scope minimo. |
| Security | Verificare rischi e controlli proporzionati. |
| Testing | Eseguire test rilevanti o indicare perche' non sono eseguibili. |
| Review | Verificare coerenza, regressioni, duplicazioni, overengineering, commenti/Javadoc e README quando rilevanti, e necessita' degli agenti creati. |
| Closure | Riportare risultato, test, documenti aggiornati, agenti creati e rischi residui. |

## 8. Anti-Proliferation Rules

- Preferire agenti esistenti.
- Preferire istruzioni nel workflow rispetto a nuovi agenti se il bisogno non e' ricorrente.
- Eliminare o non creare agenti ridondanti.
- Ogni agente dinamico deve avere responsabilita' distinta e riutilizzabile.
- Motivare la creazione di un agente e registrarla in `docs/decisions.md` quando significativa.
- Non creare agenti per accelerare un singolo task se il contesto basta.

## 9. General Execution Rules

- Limitare lo scope alla richiesta o al task corrente.
- Preferire semplicita', leggibilita', manutenibilita' e testabilita'.
- Evitare overengineering, duplicazioni, dipendenze inutili e funzionalita' non richieste.
- Non cambiare API, modelli o comportamento senza requisito, task o decisione coerente.
- Non duplicare contenuto di `docs/` dentro workflow o agenti.
- Non generare codice applicativo, documenti o agenti fuori dallo scope richiesto.
