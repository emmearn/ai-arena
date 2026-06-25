# Requirements Template

Standard compatto per scrivere `docs/requirements.md`. Questo documento definisce il formato dei requisiti; non contiene requisiti del progetto.

## 1. Principi di scrittura

Ogni requisito deve essere:

- chiaro: comprensibile senza interpretazioni implicite;
- atomico: descrive un solo bisogno verificabile;
- non ambiguo: evita termini vaghi come "veloce", "semplice", "adeguato" senza misura o criterio;
- verificabile: esiste un modo pratico per dimostrare se e' soddisfatto;
- testabile: puo' essere collegato a test manuali, automatici o controlli osservabili;
- tracciabile: mantiene collegamenti verso origine, decisioni, task, test, rischi e modifiche;
- indipendente dallo stack: non impone librerie, framework, database o pattern salvo vincoli espliciti gia' approvati altrove.

## 2. Separazione concettuale

| Elemento | Significato | Uso |
| --- | --- | --- |
| Requisito | Bisogno o comportamento che il sistema deve soddisfare | Cosa deve essere vero per utenti, sistema o stakeholder |
| Vincolo | Limite imposto alla soluzione | Obblighi normativi, organizzativi, temporali, tecnici gia' decisi |
| Assunzione | Ipotesi non ancora verificata | Punto da validare; non deve diventare requisito implicito |
| Regola di dominio | Norma del dominio applicativo | Logica di business stabile o condizione valida nel contesto |
| Criterio di accettazione | Evidenza osservabile di soddisfacimento | Base per test, review e validazione |

## 3. Formato requisito

Usare una scheda per ogni requisito.

| Campo | Contenuto |
| --- | --- |
| ID | Identificativo stabile, es. `REQ-001` |
| Titolo | Sintesi breve e specifica |
| Descrizione | Comportamento o bisogno richiesto, in forma atomica |
| Motivazione | Valore, problema risolto o rischio mitigato |
| Attori | Utenti, sistemi o ruoli coinvolti |
| Precondizioni | Condizioni che devono valere prima del requisito |
| Flusso principale | Sequenza nominale attesa, se applicabile |
| Alternative/errori | Varianti, errori, rifiuti o casi limite |
| Regole di dominio | Regole applicabili al requisito |
| Criteri di accettazione | Given/When/Then o checklist verificabile |
| Impatti | Sicurezza, dati, privacy, integrazioni, performance, accessibilita', operativita', solo se rilevanti |
| Priorita' | `MUST`, `SHOULD`, `COULD` |
| Stato | `Draft`, `Approved`, `Changed`, `Deprecated` |
| Tracciabilita' | Origine e link futuri a task, decisioni, test, rischi |

## 4. ID e classificazioni

- ID requisiti: `REQ-001`, `REQ-002`, `REQ-003`.
- ID stabili: non riutilizzare un ID eliminato o deprecato.
- Priorita':
  - `MUST`: necessario per l'obiettivo minimo approvato.
  - `SHOULD`: importante ma differibile con motivazione.
  - `COULD`: utile, opzionale, a basso impatto sul nucleo.
- Stati:
  - `Draft`: proposto, non ancora approvato.
  - `Approved`: valido come base di implementazione.
  - `Changed`: modificato rispetto a una versione approvata.
  - `Deprecated`: non piu' valido, mantenuto per storico.

## 5. Criteri di accettazione

Preferire Given/When/Then quando il comportamento e' scenario-based:

```text
Given <contesto iniziale>
When <azione o evento>
Then <risultato osservabile>
```

Usare checklist quando la verifica e' statica o ispettiva:

```text
- [ ] <condizione osservabile>
- [ ] <errore o caso limite gestito>
- [ ] <evidenza disponibile in test/log/UI/API/documentazione>
```

Ogni criterio deve essere misurabile o osservabile. Evitare criteri che richiedono interpretazione soggettiva.

## 6. Tracciabilita'

Ogni requisito deve predisporre collegamenti futuri, anche se inizialmente vuoti:

- Origine: `docs/vision.md`, stakeholder, decisione o issue sorgente.
- Task: riferimenti futuri a `docs/tasks.md`.
- Decisioni: riferimenti futuri a `docs/decisions.md`.
- Test: casi manuali, automatici o suite collegate.
- Rischi: rischi aperti, mitigazioni o verifiche pendenti.

Formato consigliato:

```text
Origine: docs/vision.md#...
Task: TBD
Decisioni: TBD
Test: TBD
Rischi: TBD
```

## 7. Impatti trasversali

Compilare solo gli impatti rilevanti per il requisito. Non aggiungere sezioni trasversali vuote o speculative.

| Area | Quando indicarla |
| --- | --- |
| Sicurezza | Autorizzazioni, abuso, input ostili, limiti, audit, error handling |
| Dati | Modello dati, qualita', retention, migrazione, consistenza |
| Privacy | Dati personali, consenso, minimizzazione, cancellazione, esposizione |
| Integrazioni | Sistemi esterni, API, contratti, indisponibilita', retry |
| Performance | Tempi, volumi, concorrenza, throughput, timeout, risorse |
| Accessibilita' | Uso da tastiera, screen reader, contrasto, alternative testuali |
| Operativita' | Logging, monitoraggio, configurazione, recovery, supporto |

## 8. Esempio minimo

| Campo | Esempio |
| --- | --- |
| ID | `REQ-001` |
| Titolo | Validazione di un input utente |
| Descrizione | Il sistema deve verificare che un input obbligatorio sia presente prima di procedere con l'operazione richiesta. |
| Motivazione | Evitare elaborazioni incomplete e fornire un feedback comprensibile. |
| Attori | Utente, sistema |
| Precondizioni | L'utente ha accesso alla funzione che richiede l'input. |
| Flusso principale | L'utente invia un input valido; il sistema lo accetta e continua l'operazione. |
| Alternative/errori | Se l'input manca, il sistema interrompe l'operazione e comunica il problema. |
| Regole di dominio | Un input obbligatorio non puo' essere vuoto. |
| Criteri di accettazione | Given una richiesta con input valido, When l'utente invia la richiesta, Then il sistema procede. Given una richiesta senza input obbligatorio, When l'utente invia la richiesta, Then il sistema non procede e mostra un errore osservabile. |
| Impatti | Dati: impedisce la creazione di informazioni incomplete. |
| Priorita' | `MUST` |
| Stato | `Draft` |
| Tracciabilita' | Origine: TBD; Task: TBD; Decisioni: TBD; Test: TBD; Rischi: TBD |
