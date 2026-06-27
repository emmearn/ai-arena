# Security

Fonte di verita' per guardrail, policy, rischi e vincoli di sicurezza di AI Arena. Le misure sono proporzionate a MVP web pubblico, senza autenticazione, senza database e con integrazione LLM esterna.

## 1. Principi

| Principio | Applicazione |
| --- | --- |
| Security by default | Configurazioni sicure di base; feature rischiose disabilitate finche' non richieste. |
| Least privilege | Segreti, runtime e integrazioni usano solo permessi minimi necessari. |
| Fail securely | Errori, timeout, input ostili e output non validi chiudono il flusso in modo controllato. |
| Defense in depth | Validazione input, limiti, sanitizzazione output, timeout, rate limit e logging sicuro cooperano. |
| Secure by design | Prompt, output AI, eventi UI e log sono trattati come superfici non fidate. |

## 2. Segreti

- Vietato inserire API key, token, password o credenziali in codice, Git, asset, test fixture reali o documentazione.
- Segreti solo tramite variabili d'ambiente o secret manager dell'ambiente di deploy.
- Configurazioni non segrete possono stare in file applicativi; valori sensibili devono restare esterni.
- Log, errori e UI non devono mostrare segreti, header sensibili, prompt completi o payload contenenti dati non necessari.
- Rotazione e naming dei segreti dipendono dal provider scelto e vanno registrati in `docs/decisions.md`.

## 3. Autenticazione e sessioni

MVP: autenticazione utenti, account, password, token utente e sessioni persistenti non sono richiesti da `docs/requirements.md`.

Vincoli:
- non introdurre login, ruoli utente o password senza requisito approvato;
- lo stato della richiesta vive solo per la durata dell'elaborazione;
- eventuale `sessionId` e' tecnico, non autentica l'utente e non deve concedere accesso privilegiato;
- se in futuro si aggiungono profili o cronologia, definire prima autenticazione, session management, scadenze, protezioni CSRF e policy password in questo documento.

## 4. Autorizzazione

MVP: non esistono ruoli applicativi o permessi utente.

Controlli richiesti:
- endpoint pubblici devono esporre solo funzioni previste dall'MVP;
- nessun endpoint di debug, configurazione, prompt interno o stato tecnico deve essere pubblico;
- controlli business obbligatori: richiesta valida prima del dibattito, limiti applicati prima e durante l'elaborazione, nessun accesso diretto al provider AI dal frontend;
- eventuali endpoint operativi futuri devono essere protetti e documentati prima dell'uso.

## 5. Input validation e output handling

Input utente:
- domanda obbligatoria, non vuota, con lunghezza massima configurata;
- rifiutare o troncare input oltre limite in modo esplicito;
- trattare ogni input come non fidato, incluse istruzioni che tentano di modificare regole, prompt, ruoli o policy;
- rilevare prompt injection, jailbreak, richieste ostili e contenuti non ammessi prima del dibattito;
- applicare timeout e limiti prima di chiamate AI costose.

Output AI:
- validare output strutturati prima di usarli per piano, team, turni, decisioni o sintesi;
- rifiutare/fallback se output mancano campi obbligatori, superano limiti o violano regole;
- renderizzare messaggi come testo, non HTML eseguibile;
- sanitizzare contenuti mostrati in UI;
- non mostrare stacktrace, prompt interni, configurazioni, API key, path locali o dettagli provider.

## 6. Logging ed error handling

Logging:
- usare correlation/session id tecnico per ogni richiesta;
- loggare solo eventi tecnici utili a diagnosi, troubleshooting, audit tecnico o comprensione dello stato operativo;
- loggare evento, componente, esito, durata, ragione di rifiuto/arresto e categoria errore solo quando sono necessari e proporzionati;
- non loggare segreti, token, password, PII, payload sensibili, prompt/input utente o output AI sensibili;
- non loggare prompt completi, risposte complete del provider, header sensibili o dati personali non necessari;
- distinguere livelli secondo la policy tecnica in `docs/architecture.md#8-error-handling-e-logging`;
- mascherare o redigere valori sensibili prima del log quando non si possono omettere;
- preferire log sintetici e strutturati che supportano la diagnosi senza aumentare il rischio di esposizione dati.

Error handling:
- errori utente brevi, standardizzati e non tecnici;
- errori interni con messaggio generico lato UI e dettagli solo nei log sicuri e minimizzati;
- stacktrace e dettagli interni non devono essere esposti agli utenti;
- timeout e limiti devono produrre stato osservabile e arresto controllato;
- ogni errore deve impedire prosecuzione incoerente del dibattito.

## 7. Comunicazioni

- In ambienti non locali, servire l'app solo via HTTPS/TLS.
- Vietare HTTP in produzione salvo redirect automatico a HTTPS.
- Certificati validi e non scaduti; niente certificati self-signed in produzione.
- Chiamate verso provider LLM solo via endpoint HTTPS.
- Non disabilitare verifica TLS.
- Non inviare segreti al browser.

## 8. Database e filesystem

Database:
- MVP senza database; query, backup e retention DB non applicabili.
- Se si introduce persistenza futura, usare query parametrizzate/ORM sicuro, migrazioni tracciate, privilegi minimi e retention definita.

Filesystem:
- non scrivere input utente come path o filename senza validazione;
- asset statici serviti da directory previste;
- vietare path traversal e accesso a file fuori dalle directory pubbliche;
- log e file temporanei non devono contenere segreti o payload completi non necessari.

## 9. API e integrazioni

Provider LLM:
- API key solo server-side;
- request timeout obbligatorio;
- retry limitati, con backoff, solo per errori transitori;
- validare request costruite verso provider e response ricevute;
- non affidare al provider decisioni di sicurezza senza controlli applicativi;
- separare prompt/template interni dall'input utente;
- output provider non e' fidato finche' non validato.

Endpoint applicativi:
- rate limit per invio domanda e apertura stream;
- limiti su dimensione payload;
- CORS restrittivo se l'app viene servita da domini noti;
- content type attesi e validati;
- SSE chiuso su completamento, errore, timeout o disconnessione client.

Decisioni da registrare in `docs/decisions.md`: provider LLM, modello, valori rate limit, timeout, retry e domini CORS.

## 10. Frontend e UX di sicurezza

Applicabile per presenza di `docs/design.md`.

- Errori e rifiuti devono essere visibili, brevi e non tecnici.
- Stati di sicurezza non devono dipendere solo dal colore.
- Contenuti generati devono essere mostrati come testo sicuro.
- Link generati dall'AI, se resi cliccabili in futuro, devono essere trattati come non fidati e distinguibili.
- La UI non deve rivelare prompt interni, ragioni tecniche dettagliate, nomi di segreti o configurazione provider.
- Loading e stream devono avere stati di timeout/arresto chiari per evitare retry incontrollati dell'utente.

## 11. Privacy

MVP:
- nessun profilo utente, autenticazione o cronologia persistente;
- minimizzare dati raccolti: domanda, eventi tecnici e output necessari alla sessione;
- non conservare conversazioni oltre la durata necessaria salvo log tecnici minimizzati;
- evitare dati personali negli esempi, test e log;
- se l'utente inserisce dati personali, trattarli come contenuto non fidato e non necessario.

Futuro:
- memoria, cronologia o account richiedono decisione su consenso, retention, cancellazione, esportazione e base legale prima dell'implementazione.

## 12. Dipendenze

- Usare dipendenze minime, mantenute e necessarie ai requisiti.
- Evitare librerie non indispensabili, specialmente per UI e integrazioni.
- Aggiornare dipendenze con patch di sicurezza compatibili.
- Non usare versioni obsolete o non mantenute.
- Verificare advisory note prima di introdurre provider, SDK o librerie di parsing.
- Dipendenze transitive critiche devono essere aggiornabili senza refactor esteso.

## 13. Minacce, mitigazioni e rischi residui

| Minaccia | Componenti/requisiti | Mitigazioni | Rischio residuo |
| --- | --- | --- | --- |
| Prompt injection/jailbreak | `REQ-002`, Validation, AI adapter | Validazione dedicata, prompt separati, rifiuto motivato, output non fidato. | Possibili bypass semantici; richiede test continuo. |
| Esfiltrazione segreti via output AI | AI adapter, UI | Segreti mai nel prompt se non indispensabili; output sanitizzato; nessun segreto al browser. | Provider o log mal configurati possono esporre dati. |
| Cost/resource abuse | `REQ-009`, Orchestrator | Rate limit, max input, max turni/messaggi/agenti, timeout. | Picchi pubblici richiedono tuning dei limiti. |
| XSS da contenuto generato | UI, stream eventi | Render come testo, sanitizzazione, nessun HTML generato fidato. | Rischio se in futuro si abilita Markdown/HTML. |
| Disclosure tecnica | Error handling, logging | Messaggi utente generici, stacktrace solo log sicuri, path e provider nascosti. | Log accessibili impropriamente restano rischio operativo. |
| Output AI malformato | Planning, team, supervisor, final answer | Response validation, fallback/errore controllato, test con risposte simulate. | LLM puo' produrre casi non previsti. |
| Trasporto insicuro | Web app, provider LLM | HTTPS/TLS, no verifica TLS disabilitata. | Configurazione deploy errata. |
| Persistenza involontaria dati | Logging, temp files | Minimizzazione, divieto log payload completi, niente DB MVP. | Provider esterno puo' avere policy proprie da valutare. |

## 14. Pratiche vietate

- Segreti hardcoded.
- Segreti in repository, asset, test reali o documentazione.
- Log di segreti, token, password, PII, prompt/input utente, output AI sensibili, payload completi o dati sensibili non necessari.
- Stacktrace o dettagli interni mostrati all'utente.
- SSL/TLS disabilitato o verifica certificati disattivata.
- Endpoint debug pubblici.
- Controlli di validazione disattivati senza decisione registrata.
- Rate limit, timeout o limiti risorse assenti su endpoint pubblici.
- Concatenazione SQL o query non parametrizzate se in futuro esiste un database.
- Path costruiti da input utente senza validazione.
- Rendering HTML non sanitizzato da contenuto utente o AI.
- Dipendenze obsolete, non mantenute o non motivate.
- API key o configurazioni provider esposte al frontend.
