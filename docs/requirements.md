# Requirements

Fonte di verita' del comportamento atteso di AI Arena. Il documento descrive cosa deve fare il sistema, non come implementarlo.

## 1. Vision

| Area | Contenuto |
| --- | --- |
| Scopo | Mostrare in modo intuitivo come una risposta possa migliorare tramite confronto, revisione e sintesi tra esperti AI orchestrati creati per la richiesta. |
| Problema | Una singola risposta opaca non rende visibile il processo di ragionamento, critica e convergenza. |
| Utenti target | Visitatori, stakeholder tecnici, team prodotto e sviluppatori che vogliono osservare una demo domain-agnostic con piu' esperti AI orchestrati. |
| Valore atteso | Esperienza educativa e funzionale: l'utente vede validazione, team, dibattito, controllo del flusso e risposta finale motivata. |

## 2. Ambito

Incluse:
- inserimento di una domanda utente;
- validazione della richiesta prima del dibattito;
- classificazione della richiesta;
- pianificazione dinamica del team di ruoli AI orchestrati;
- creazione runtime di esperti AI orchestrati con ruolo, personalita' e missione;
- dibattito osservabile in tempo reale;
- controllo di turni, limiti e arresto;
- sintesi finale motivata;
- gestione di rifiuti, errori, timeout e limiti configurabili;
- schermata unica con domanda, stato, team, dibattito, decisione di arresto e risposta finale.

Escluse dall'MVP:
- cronologia persistente delle conversazioni;
- memoria utente persistente;
- esperti AI permanenti di dominio;
- tool esterni, RAG, MCP, modelli multipli, votazione strutturata;
- valutazione automatica oggettiva della qualita' della risposta;
- profili utente, autenticazione e autorizzazioni.

MVP:
- una richiesta alla volta;
- team generato per singola domanda;
- streaming o aggiornamento progressivo del dibattito;
- arresto entro limiti definiti;
- risposta finale sempre presente per richieste valide.

Evoluzioni future:
- tool calling, MCP, RAG, memoria persistente, ruoli AI paralleli, votazione, modelli differenti, cronologia conversazioni;
- LLM-as-a-Judge separato dal supervisore, con valutazione qualitativa strutturata della risposta finale e segnale consultivo sullo STOP del Supervisor.

## 3. Attori e casi d'uso

| Attore | Ruolo |
| --- | --- |
| Utente | Inserisce una domanda e osserva dibattito e risposta finale. |
| Sistema | Valida, pianifica, crea esperti AI orchestrati, coordina dibattito, arresta e sintetizza. |
| Esperto AI orchestrato | Contribuisce al dibattito secondo ruolo, missione e personalita'. |
| Supervisore | Gestisce turni, limiti, loop, criterio di arresto e sintesi finale nell'MVP. |
| Judge | Valuta la qualita' della risposta finale con rubrica esplicita e output strutturato; puo' chiedere piu' dibattito come segnale consultivo quando il Supervisor propone STOP. |

| Caso d'uso | Flusso principale | Alternative/errori |
| --- | --- | --- |
| Porre una domanda valida | Utente invia domanda; sistema valida; crea team; mostra dibattito; produce risposta finale. | Se un limite e' raggiunto, il sistema arresta e sintetizza quanto disponibile. |
| Richiesta non valida o ostile | Utente invia domanda; sistema rileva problema; rifiuta motivando. | Il dibattito non parte. |
| Dibattito non convergente | Sistema rileva loop, saturazione o limite; arresta. | Risposta finale indica sintesi e ragione dell'arresto. |
| Errore durante elaborazione | Sistema comunica errore osservabile. | Non deve esporre dettagli interni non necessari. |

## 4. Funzionalita' principali

| Funzione | Input | Output | Vincoli di dominio | Attori |
| --- | --- | --- | --- | --- |
| Acquisizione domanda | Testo domanda | Richiesta pronta alla validazione | Domanda obbligatoria e non vuota | Utente, sistema |
| Validazione richiesta | Domanda | Esito valido/rifiutato con motivazione | Nessun dibattito se non valida | Sistema |
| Pianificazione team | Richiesta valida | Competenze, numero di ruoli AI orchestrati, ruoli, strategia | Team creato per richiesta, non permanente | Sistema |
| Creazione esperti AI orchestrati | Piano | Esperti con nome, ruolo, personalita', missione | Personalita' distinguibili e coerenti | Sistema |
| Dibattito | Team e domanda | Messaggi progressivi | Gli esperti AI orchestrati propongono, criticano, correggono, convergono | Esperti AI orchestrati, supervisore |
| Supervisione | Messaggi, limiti, stato | Prossimo turno, arresto o sintesi | Prevenire loop e rispettare limiti | Supervisore |
| Risposta finale | Dibattito concluso | Risposta motivata | Presente per ogni richiesta valida conclusa | Sistema |
| Valutazione qualitativa | Risposta finale e STOP del Supervisor | Giudizio strutturato | Migliorare qualita' e affidabilita' senza sostituire i controlli applicativi | Judge, sistema |
| UI singola | Stati e risultati | Vista completa del processo | Processo decisionale osservabile | Utente |

## 5. User stories

- Come utente, voglio inserire una domanda in linguaggio naturale, in modo da ottenere una risposta nata da un confronto.
- Come utente, voglio vedere se la richiesta e' stata accettata o rifiutata, in modo da capire perche' il dibattito parte o non parte.
- Come utente, voglio vedere il team creato per la mia domanda, in modo da comprendere quali competenze partecipano.
- Come utente, voglio osservare il dibattito in modo progressivo, in modo da seguire proposte, critiche e convergenza.
- Come utente, voglio ricevere una risposta finale motivata, in modo da usare il risultato senza leggere tutto il dibattito.
- Come stakeholder, voglio che il sistema sia domain-agnostic, in modo da dimostrare scenari diversi senza configurare esperti permanenti.
- Come responsabile prodotto, voglio limiti e arresti chiari, in modo da evitare loop e attese indefinite.

## 6. Requisiti funzionali

### REQ-001 - Inserimento domanda

| Campo | Contenuto |
| --- | --- |
| ID | `REQ-001` |
| Titolo | Inserimento domanda utente |
| Descrizione | Il sistema deve permettere all'utente di inviare una domanda testuale. |
| Motivazione | La domanda e' l'input primario dell'esperienza. |
| Attori | Utente, sistema |
| Precondizioni | La schermata principale e' disponibile. |
| Flusso principale | L'utente inserisce una domanda e la invia; il sistema acquisisce il testo. |
| Alternative/errori | Se la domanda e' vuota, il sistema non procede e segnala l'errore. |
| Regole di dominio | Una richiesta senza domanda non e' valida. |
| Criteri di accettazione | Given una domanda non vuota, When l'utente invia, Then il sistema avvia la validazione. Given una domanda vuota, When l'utente invia, Then il sistema mostra un errore e non avvia il dibattito. |
| Impatti | Accessibilita': input utilizzabile da tastiera. |
| Priorita' | `MUST` |
| Stato | `Draft` |
| Tracciabilita' | Origine: `docs/vision.md#3-esperienza-utente`; Task: Da definire; Decisioni: Da definire; Test: Da definire; Rischi: Da definire |

### REQ-002 - Validazione richiesta

| Campo | Contenuto |
| --- | --- |
| ID | `REQ-002` |
| Titolo | Validazione prima del dibattito |
| Descrizione | Il sistema deve validare la richiesta prima di creare il team e avviare il dibattito. |
| Motivazione | Evitare elaborazioni non ammesse, incomplete o ostili. |
| Attori | Sistema, utente |
| Precondizioni | Una domanda e' stata inviata. |
| Flusso principale | Il sistema analizza la richiesta, assegna un esito e prosegue solo se valida. |
| Alternative/errori | Se la richiesta non e' valida, il sistema rifiuta con motivazione osservabile. |
| Regole di dominio | Il dibattito non parte se la validazione fallisce. |
| Criteri di accettazione | Given una richiesta valida, When la validazione termina, Then il sistema procede alla pianificazione. Given una richiesta non valida, When la validazione termina, Then il sistema non crea il team e mostra il motivo del rifiuto. |
| Impatti | Sicurezza: rilevazione input ostili; Operativita': errori gestiti in modo osservabile. |
| Priorita' | `MUST` |
| Stato | `Draft` |
| Tracciabilita' | Origine: `docs/vision.md#6-ruolo-di-validazione`; Task: Da definire; Decisioni: Da definire; Test: Da definire; Rischi: Da definire |

### REQ-003 - Classificazione richiesta

| Campo | Contenuto |
| --- | --- |
| ID | `REQ-003` |
| Titolo | Classificazione della domanda |
| Descrizione | Il sistema deve classificare la richiesta valida per supportare la scelta delle competenze necessarie. |
| Motivazione | Rendere il team coerente con il dominio della domanda. |
| Attori | Sistema |
| Precondizioni | La richiesta e' valida. |
| Flusso principale | Il sistema determina il tipo di richiesta e le aree di competenza rilevanti. |
| Alternative/errori | Se il dominio e' incerto, il sistema usa una classificazione generale e continua. |
| Regole di dominio | Il sistema e' domain-agnostic e non dipende da domini preconfigurati permanenti. |
| Criteri di accettazione | Given una richiesta valida, When viene classificata, Then il piano contiene competenze coerenti con il contenuto. |
| Impatti | Nessuno rilevante. |
| Priorita' | `MUST` |
| Stato | `Draft` |
| Tracciabilita' | Origine: `docs/vision.md#6-ruolo-di-validazione`; Task: Da definire; Decisioni: Da definire; Test: Da definire; Rischi: Da definire |

### REQ-004 - Pianificazione team

| Campo | Contenuto |
| --- | --- |
| ID | `REQ-004` |
| Titolo | Piano dei ruoli AI orchestrati |
| Descrizione | Il sistema deve produrre un piano con competenze richieste, numero di ruoli AI orchestrati, ruoli e strategia iniziale. |
| Motivazione | Rendere il dibattito intenzionale e proporzionato alla domanda. |
| Attori | Sistema |
| Precondizioni | La richiesta e' valida e classificata. |
| Flusso principale | Il sistema definisce il team necessario e la strategia iniziale. |
| Alternative/errori | Se la domanda e' semplice, il sistema puo' scegliere un team minimo entro i limiti configurati. |
| Regole di dominio | Il piano non deve rispondere direttamente alla domanda. |
| Criteri di accettazione | Given una richiesta valida, When il piano e' prodotto, Then contiene competenze, numero di ruoli AI orchestrati, ruoli e strategia iniziale. |
| Impatti | Performance: il numero di ruoli AI orchestrati deve rispettare i limiti. |
| Priorita' | `MUST` |
| Stato | `Draft` |
| Tracciabilita' | Origine: `docs/vision.md#7-ruolo-planner`; Task: Da definire; Decisioni: Da definire; Test: Da definire; Rischi: Da definire |

### REQ-005 - Creazione dinamica degli esperti AI orchestrati

| Campo | Contenuto |
| --- | --- |
| ID | `REQ-005` |
| Titolo | Esperti AI orchestrati |
| Descrizione | Il sistema deve creare esperti AI orchestrati per la singola richiesta, ciascuno con nome, ruolo, personalita' e missione. Gli esperti AI orchestrati sono ruoli AI generati per la singola richiesta e guidati dall'orchestratore; non sono agenti indipendenti, processi autonomi o modelli distinti. |
| Motivazione | Dimostrare adattamento dinamico al problema dell'utente. |
| Attori | Sistema, esperto AI orchestrato |
| Precondizioni | Esiste un piano approvato dal sistema. |
| Flusso principale | Il sistema crea esperti AI orchestrati coerenti con il piano e li rende visibili all'utente. |
| Alternative/errori | Se non e' possibile creare un team completo, il sistema segnala errore o procede solo se il team minimo e' sufficiente. |
| Regole di dominio | Non esistono esperti AI permanenti di dominio nell'MVP. |
| Criteri di accettazione | Given un piano valido, When il team e' creato, Then ogni esperto AI orchestrato ha nome, ruolo, personalita' e missione osservabili. |
| Impatti | Dati: i metadati del team devono essere consistenti durante la richiesta. |
| Priorita' | `MUST` |
| Stato | `Draft` |
| Tracciabilita' | Origine: `docs/vision.md#8-costruttore-ruoli-runtime`; Task: Da definire; Decisioni: Da definire; Test: Da definire; Rischi: Da definire |

### REQ-006 - Differenziazione personalita'

| Campo | Contenuto |
| --- | --- |
| ID | `REQ-006` |
| Titolo | Personalita' distinguibili |
| Descrizione | Il sistema deve assegnare agli esperti AI orchestrati personalita' professionali coerenti e distinguibili. |
| Motivazione | Evitare contributi percepiti come copie equivalenti e rendere leggibile il confronto. |
| Attori | Sistema, esperto AI orchestrato, utente |
| Precondizioni | Il team e' stato creato. |
| Flusso principale | Ogni esperto AI orchestrato partecipa secondo ruolo, missione e tono coerenti. |
| Alternative/errori | Se i contributi risultano ridondanti, il supervisore puo' favorire convergenza o arresto. |
| Regole di dominio | Le personalita' devono restare professionali e coerenti con il ruolo. |
| Criteri di accettazione | Given un team creato, When l'utente visualizza i membri, Then puo' distinguere ruoli e missioni. Given il dibattito, When piu' esperti AI orchestrati rispondono, Then i contributi riflettono prospettive differenti. |
| Impatti | Nessuno rilevante. |
| Priorita' | `SHOULD` |
| Stato | `Draft` |
| Tracciabilita' | Origine: `docs/vision.md#9-personalita`; Task: Da definire; Decisioni: Da definire; Test: Da definire; Rischi: Da definire |

### REQ-007 - Dibattito osservabile

| Campo | Contenuto |
| --- | --- |
| ID | `REQ-007` |
| Titolo | Dibattito progressivo |
| Descrizione | Il sistema deve mostrare progressivamente i messaggi del dibattito tra esperti AI orchestrati. |
| Motivazione | Il valore della demo nasce dall'osservabilita' del confronto. |
| Attori | Utente, esperto AI orchestrato, supervisore |
| Precondizioni | Team creato e richiesta valida. |
| Flusso principale | Gli esperti AI orchestrati producono contributi visibili in sequenza durante la discussione. |
| Alternative/errori | Se il dibattito non puo' continuare, il sistema comunica arresto o errore. |
| Regole di dominio | I contributi devono includere proposta, critica, correzione o convergenza quando rilevante. |
| Criteri di accettazione | Given un dibattito avviato, When vengono generati contributi, Then l'utente li vede progressivamente associati agli esperti AI orchestrati. |
| Impatti | Performance: aggiornamento progressivo senza attesa della risposta finale; Accessibilita': messaggi leggibili e ordinati. |
| Priorita' | `MUST` |
| Stato | `Draft` |
| Tracciabilita' | Origine: `docs/vision.md#10-dibattito`; Task: Da definire; Decisioni: Da definire; Test: Da definire; Rischi: Da definire |

### REQ-008 - Supervisione del flusso

| Campo | Contenuto |
| --- | --- |
| ID | `REQ-008` |
| Titolo | Controllo turni e loop |
| Descrizione | Il sistema deve supervisionare il dibattito gestendo turni, loop e criteri di arresto. |
| Motivazione | Evitare discussioni infinite e mantenere l'esperienza controllata. |
| Attori | Supervisore, sistema |
| Precondizioni | Dibattito avviato. |
| Flusso principale | Il supervisore decide il prossimo turno o arresta il dibattito secondo stato e limiti. |
| Alternative/errori | In caso di loop, timeout o limite raggiunto, il supervisore arresta il dibattito. |
| Regole di dominio | Il dibattito deve terminare sempre con arresto esplicito o errore gestito. |
| Criteri di accettazione | Given un dibattito attivo, When viene raggiunto un criterio di arresto, Then il sistema interrompe nuovi turni e passa alla sintesi. |
| Impatti | Affidabilita': prevenzione loop; Operativita': ragione di arresto osservabile. |
| Priorita' | `MUST` |
| Stato | `Draft` |
| Tracciabilita' | Origine: `docs/vision.md#11-supervisor`; Task: Da definire; Decisioni: Da definire; Test: Da definire; Rischi: Da definire |

### REQ-009 - Limiti configurabili

| Campo | Contenuto |
| --- | --- |
| ID | `REQ-009` |
| Titolo | Rispetto dei limiti di esecuzione |
| Descrizione | Il sistema deve rispettare limiti definiti per massimo esperti AI orchestrati, turni, messaggi e timeout. |
| Motivazione | Controllare durata, costo, risorse e prevedibilita' dell'esperienza. |
| Attori | Sistema, supervisore |
| Precondizioni | Limiti disponibili al sistema. |
| Flusso principale | Il sistema applica i limiti durante pianificazione e dibattito. |
| Alternative/errori | Se un limite viene raggiunto, il sistema arresta o rifiuta l'azione eccedente. |
| Regole di dominio | Nessun flusso deve superare i limiti attivi. |
| Criteri di accettazione | Given limiti definiti, When il sistema pianifica o dibatte, Then numero di esperti AI orchestrati, turni, messaggi e durata restano entro i limiti. |
| Impatti | Performance: controllo risorse; Operativita': comportamento prevedibile. |
| Priorita' | `MUST` |
| Stato | `Draft` |
| Tracciabilita' | Origine: `docs/vision.md#12-regole`; Task: Da definire; Decisioni: Da definire; Test: Da definire; Rischi: Da definire |

### REQ-010 - Sintesi finale motivata

| Campo | Contenuto |
| --- | --- |
| ID | `REQ-010` |
| Titolo | Risposta finale |
| Descrizione | Il sistema deve produrre una risposta finale motivata al termine del dibattito valido. |
| Motivazione | Fornire all'utente un risultato utilizzabile oltre al processo osservato. |
| Attori | Supervisore, sistema, utente |
| Precondizioni | Dibattito concluso o arrestato in modo controllato. |
| Flusso principale | Il supervisore sintetizza il risultato del confronto e lo presenta all'utente. |
| Alternative/errori | Se il dibattito termina per limite, la risposta finale deve basarsi sui contributi disponibili e indicare il contesto dell'arresto. |
| Regole di dominio | La risposta finale deriva dal dibattito, non da un singolo contributo isolato. Come evoluzione, la risposta finale deve essere valutabile prima della consegna tramite giudizio strutturato, senza rendere obbligatorio lo scoring automatico nell'MVP. |
| Criteri di accettazione | Given un dibattito concluso, When la sintesi e' prodotta, Then l'utente vede una risposta finale motivata. |
| Impatti | Nessuno rilevante. |
| Priorita' | `MUST` |
| Stato | `Draft` |
| Tracciabilita' | Origine: `docs/vision.md#11-supervisor`; Task: Da definire; Decisioni: Da definire; Test: Da definire; Rischi: Da definire |

### REQ-011 - Schermata unica

| Campo | Contenuto |
| --- | --- |
| ID | `REQ-011` |
| Titolo | Vista completa del processo |
| Descrizione | Il sistema deve presentare in una singola schermata domanda, stato validazione, team, dibattito, decisione del supervisore e risposta finale. |
| Motivazione | Rendere evidente il processo decisionale senza navigazione complessa. |
| Attori | Utente, sistema |
| Precondizioni | L'applicazione e' accessibile. |
| Flusso principale | L'utente interagisce con una schermata che si aggiorna durante il ciclo della richiesta. |
| Alternative/errori | In caso di errore, la schermata mostra uno stato comprensibile. |
| Regole di dominio | La UI deve rendere osservabile il percorso dalla domanda alla risposta. |
| Criteri di accettazione | Given una richiesta in corso, When cambia lo stato del processo, Then la schermata riflette lo stato corrente. |
| Impatti | Accessibilita': stati e contenuti leggibili; Operativita': errori osservabili. |
| Priorita' | `MUST` |
| Stato | `Draft` |
| Tracciabilita' | Origine: `docs/vision.md#13-ui`; Task: Da definire; Decisioni: Da definire; Test: Da definire; Rischi: Da definire |

### REQ-012 - Scenari domain-agnostic

| Campo | Contenuto |
| --- | --- |
| ID | `REQ-012` |
| Titolo | Supporto a domande di domini diversi |
| Descrizione | Il sistema deve accettare domande valide appartenenti a domini diversi senza richiedere esperti AI permanenti predefiniti. |
| Motivazione | Dimostrare la natura domain-agnostic del prodotto. |
| Attori | Utente, sistema |
| Precondizioni | La domanda supera la validazione. |
| Flusso principale | Il sistema adatta classificazione, piano e team al dominio della domanda. |
| Alternative/errori | Se il dominio non e' riconosciuto, il sistema usa ruoli AI generali o rifiuta solo se la richiesta viola le policy. |
| Regole di dominio | Esempi supportati: programmazione, fitness, alimentazione, viaggi, finanza, studio, produttivita'. |
| Criteri di accettazione | Given domande valide di domini diversi, When vengono elaborate, Then il sistema crea team coerenti senza dipendere da esperti AI permanenti. |
| Impatti | Scalabilita': adattamento funzionale a domini diversi. |
| Priorita' | `SHOULD` |
| Stato | `Draft` |
| Tracciabilita' | Origine: `docs/vision.md#14-scenari`; Task: Da definire; Decisioni: Da definire; Test: Da definire; Rischi: Da definire |

## 7. Requisiti non funzionali

| ID | Area | Requisito | Priorita' | Stato | Tracciabilita' |
| --- | --- | --- | --- | --- | --- |
| `NFR-001` | Prestazioni | Il sistema deve fornire feedback di stato durante l'elaborazione, evitando che l'utente resti senza evidenza di avanzamento. | `MUST` | `Draft` | Origine: `docs/vision.md#3-esperienza-utente`; Task/Decisioni/Test/Rischi: Da definire |
| `NFR-002` | Affidabilita' | Ogni richiesta valida deve terminare con risposta finale, arresto motivato o errore gestito. | `MUST` | `Draft` | Origine: `docs/vision.md#11-supervisor`; Task/Decisioni/Test/Rischi: Da definire |
| `NFR-003` | Sicurezza | Il sistema deve rilevare e rifiutare richieste ostili note, inclusi tentativi di prompt injection o jailbreak. | `MUST` | `Draft` | Origine: `docs/vision.md#15-sicurezza`; Task/Decisioni/Test/Rischi: Da definire |
| `NFR-004` | Operativita' | Errori, timeout e limiti raggiunti devono essere rappresentati con stati comprensibili per l'utente. | `MUST` | `Draft` | Origine: `docs/vision.md#15-sicurezza`; Task/Decisioni/Test/Rischi: Da definire |
| `NFR-005` | Accessibilita' | La schermata principale deve consentire uso base tramite tastiera e contenuti testuali leggibili. | `SHOULD` | `Draft` | Origine: `docs/vision.md#13-ui`; Task/Decisioni/Test/Rischi: Da definire |
| `NFR-006` | Manutenibilita' | I requisiti funzionali devono restare tracciabili a task, decisioni e test futuri. | `MUST` | `Draft` | Origine: `docs/requirements_template.md`; Task/Decisioni/Test/Rischi: Da definire |
| `NFR-007` | Compatibilita' | L'esperienza deve essere fruibile come applicazione web da browser moderno. | `SHOULD` | `Draft` | Origine: `docs/vision.md#1-vision`; Task/Decisioni/Test/Rischi: Da definire |

## 8. Input, output e validazioni di dominio

| Elemento | Descrizione | Validazioni |
| --- | --- | --- |
| Domanda utente | Testo libero inviato dall'utente | Obbligatoria, non vuota, ammessa dalle policy, elaborabile entro limiti. |
| Esito validazione | Stato della richiesta | Deve distinguere richiesta valida, rifiutata, errore. |
| Piano team | Competenze, numero, ruoli, strategia | Deve rispettare limiti e non rispondere direttamente alla domanda. |
| Esperto AI orchestrato | Nome, ruolo, personalita', missione | Deve essere coerente con piano e distinguibile dagli altri. |
| Messaggio dibattito | Contributo attribuito a un esperto AI orchestrato | Deve essere ordinato, leggibile e associato al mittente. |
| Decisione supervisore | Prossimo turno, arresto, motivazione | Deve rispettare limiti e rendere osservabile la ragione dell'arresto. |
| Risposta finale | Sintesi motivata | Deve derivare dal dibattito concluso o arrestato. |
| Giudizio qualitativo | Verdict, rubrica, motivazione sintetica | Deve essere strutturato, validato e non sostituire i controlli di sicurezza. |

## 9. Regole di dominio, policy, assunzioni e vincoli

Regole di dominio:
- il sistema e' domain-agnostic;
- il team viene creato per ogni richiesta valida;
- non esistono esperti AI permanenti di dominio nell'MVP;
- il dibattito non parte senza validazione positiva;
- il piano definisce il team ma non risponde alla domanda;
- ogni dibattito deve terminare entro limiti;
- la risposta finale deve essere distinta dai singoli messaggi del dibattito.

Policy:
- rifiutare richieste non valide o ostili con motivazione osservabile;
- non esporre dettagli interni non necessari negli errori;
- applicare limiti di esperti AI orchestrati, turni, messaggi e timeout;
- mostrare stato e progressione del processo all'utente.

Assunzioni:
- l'MVP gestisce una richiesta utente per volta;
- gli utenti non sono autenticati;
- la persistenza della cronologia non e' richiesta;
- i limiti operativi esistono come valori disponibili al sistema;
- la qualita' della risposta e' valutata anche tramite Judge strutturato post-sintesi e segnale consultivo sullo STOP del Supervisor; metriche di successo di prodotto restano evoluzione documentata.

Vincoli:
- sorgente vision: `docs/vision.md`;
- standard requisiti: `docs/requirements_template.md`;
- nessuna scelta di librerie, database, API, classi o pattern e' definita da questo documento.

## 10. Tracciabilita'

| Origine | Requisiti collegati | Task | Decisioni | Test | Rischi |
| --- | --- | --- | --- | --- | --- |
| `docs/vision.md#1-vision` | `REQ-012`, `NFR-007` | Da definire | Da definire | Da definire | Da definire |
| `docs/vision.md#3-esperienza-utente` | `REQ-001`, `REQ-007`, `REQ-010`, `NFR-001` | Da definire | Da definire | Da definire | Da definire |
| `docs/vision.md#6-ruolo-di-validazione` | `REQ-002`, `REQ-003`, `NFR-003` | Da definire | Da definire | Da definire | Da definire |
| `docs/vision.md#7-ruolo-planner` | `REQ-004` | Da definire | Da definire | Da definire | Da definire |
| `docs/vision.md#8-costruttore-ruoli-runtime` | `REQ-005` | Da definire | Da definire | Da definire | Da definire |
| `docs/vision.md#9-personalita` | `REQ-006` | Da definire | Da definire | Da definire | Da definire |
| `docs/vision.md#10-dibattito` | `REQ-007` | Da definire | Da definire | Da definire | Da definire |
| `docs/vision.md#11-supervisor` | `REQ-008`, `REQ-010`, `NFR-002` | Da definire | Da definire | Da definire | Da definire |
| `docs/vision.md#12-regole` | `REQ-009` | Da definire | Da definire | Da definire | Da definire |
| `docs/vision.md#13-ui` | `REQ-011`, `NFR-005` | Da definire | Da definire | Da definire | Da definire |
| `docs/vision.md#14-scenari` | `REQ-012` | Da definire | Da definire | Da definire | Da definire |
| `docs/vision.md#15-sicurezza` | `REQ-002`, `NFR-003`, `NFR-004` | Da definire | Da definire | Da definire | Da definire |
| `docs/requirements_template.md` | Tutti | Da definire | Da definire | Da definire | Da definire |
