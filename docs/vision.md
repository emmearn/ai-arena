# AI Arena — Product Vision (Vision v2)

> Questo documento descrive la visione del prodotto. Non è una specifica tecnica e non contiene decisioni implementative.
> Lo scopo è fornire una sorgente unica da cui derivare requisiti, architettura, design, sicurezza e roadmap.

# 1. Vision

AI Arena è una web application dimostrativa sviluppata con Spring Boot e Spring AI.

L'obiettivo è mostrare in modo intuitivo come un sistema di ruoli AI orchestrati possa produrre risposte più robuste attraverso confronto, revisione e sintesi, anziché tramite una singola chiamata ad un LLM.

L'utente non conversa con un chatbot.
L'utente assiste ad una discussione tra esperti AI orchestrati.

# 2. Obiettivi

- Dimostrare Spring AI.
- Mostrare orchestrazione di ruoli AI.
- Evidenziare streaming della conversazione.
- Dimostrare creazione dinamica di ruoli AI orchestrati.
- Offrire un'interfaccia piacevole e coinvolgente.

# 3. Esperienza utente

L'utente inserisce una domanda.
Il sistema:
1. valida la richiesta;
2. pianifica il team;
3. crea gli esperti AI orchestrati;
4. mostra il dibattito in tempo reale;
5. interrompe la discussione quando raggiunge un criterio di arresto;
6. produce una risposta finale motivata.

# 4. Filosofia

Il sistema è domain-agnostic.

Non esistono esperti AI permanenti di dominio.
Le competenze vengono selezionate e trasformate in ruoli AI orchestrati per ogni richiesta.

# 5. Workflow

Utente
→ Ruolo di validazione
→ Ruolo planner
→ Costruttore ruoli runtime
→ Debate Orchestrator
→ Supervisor
→ Risposta finale

# 6. Ruolo di validazione

Responsabilità:
- validazione input;
- rilevazione prompt injection;
- rilevazione jailbreak;
- classificazione della richiesta;
- eventuale rifiuto motivato.

Il dibattito non parte se la richiesta non supera questa fase.

# 7. Ruolo planner

Non risponde alla domanda.

Produce un piano contenente:
- competenze richieste;
- numero di esperti AI orchestrati;
- ruoli;
- strategia iniziale.

# 8. Costruttore ruoli runtime

Genera dinamicamente gli esperti AI orchestrati.

Ogni esperto AI orchestrato possiede:
- nome;
- ruolo;
- personalità;
- missione;
- system prompt;
- configurazione del modello.

# 9. Personalità

Gli esperti AI orchestrati non devono sembrare copie dello stesso modello.

Ogni esperto AI orchestrato ha un carattere professionale coerente.

Esempi:
- Software Architect: pragmatico, orientato al design.
- Security Engineer: prudente, scettico.
- Performance Engineer: focalizzato sull'efficienza.
- Fitness Coach: pratico e motivante.
- Nutrition Expert: rigoroso e basato sulle evidenze.

# 10. Dibattito

Gli esperti AI orchestrati:
- propongono;
- criticano;
- correggono;
- convergono.

Il valore del sistema nasce dal confronto.

# 11. Supervisor

Responsabilità:
- gestire i turni;
- impedire loop;
- interrompere il dibattito;
- sintetizzare il risultato.

# 12. Regole

Configurabili:
- massimo esperti AI orchestrati;
- massimo turni;
- massimo messaggi;
- timeout.

# 13. UI

Schermata unica composta da:
- input domanda;
- stato validazione;
- team creato;
- chat live;
- decisione del supervisor;
- risposta finale.

# 14. Scenari

Domande:
- programmazione;
- fitness;
- alimentazione;
- viaggi;
- finanza;
- studio;
- produttività.

# 15. Sicurezza

- prompt injection;
- jailbreak;
- rate limiting;
- timeout;
- limiti risorse;
- logging;
- gestione errori.

# 16. Evoluzioni

- Tool Calling
- MCP
- RAG
- memoria persistente
- ruoli AI paralleli
- votazione
- modelli differenti
- cronologia conversazioni

# 17. Criteri di successo

La PoC è riuscita se:
- il team viene creato dinamicamente;
- il dibattito è osservabile;
- il supervisor controlla il flusso;
- la UI rende evidente il processo decisionale;
- l'architettura è estendibile.

# 18. Domande aperte

- Come scegliere automaticamente il numero ottimale di esperti AI orchestrati?
- Il supervisor puo' richiedere un nuovo esperto AI orchestrato durante il dibattito?
- Quando interrompere una discussione per consenso o saturazione?
- Come valutare automaticamente la qualità della risposta finale?

# 19. Visione finale

AI Arena è una sala riunioni virtuale.

L'utente porta un problema.

Il sistema costruisce automaticamente un team di esperti AI orchestrati, coordina il confronto e restituisce una risposta nata dal dibattito.

L'esperienza deve essere tanto educativa quanto funzionale, mostrando in modo trasparente il valore dell'orchestrazione di ruoli AI.
