# Design System

Fonte di verita' per UI/UX e design system di AI Arena. Questo documento definisce stile, componenti e regole di esperienza; non definisce architettura, requisiti, sicurezza, task o implementazione.

## 1. Visione estetica generale

AI Arena deve sembrare una competizione di idee osservata in una sala digitale premium: energia, confronto, personalita' e controllo. Il tono e' moderno, memorabile e presentabile in contesto aziendale.

Direzione:
- arena professionale, non videogioco;
- spettacolarita' controllata, non effetti gratuiti;
- IA come presenza sofisticata, non mascotte;
- dibattito leggibile prima dell'estetica;
- enfasi visiva su turno di parola, tensione dialettica e sintesi finale.

Da evitare:
- dashboard enterprise tradizionale;
- CRUD, portale admin, gestionale;
- cyberpunk estremo, neon aggressivo, look gaming;
- minimalismo piatto e troppo serio;
- effetti decorativi che riducono leggibilita'.

## 2. Principi di design

| Principio | Regola |
| --- | --- |
| Chiarezza teatrale | Ogni stato deve essere immediatamente leggibile, ma presentato con presenza scenica. |
| Gerarchia del dibattito | Il contenuto parlato ha priorita' su decorazioni, metriche e ornamenti. |
| Personalita' controllata | Ogni agente ha identita' visiva distinta senza rompere il sistema. |
| Tensione ordinata | Contrasto, movimento e colore indicano confronto; layout e spacing mantengono calma. |
| Giudice centrale | La figura di giudizio/sintesi deve apparire piu' autorevole degli agenti. |
| Progressione visibile | L'utente deve percepire avanzamento, turno attivo, arresto e conclusione. |
| Business-ready | Ogni scelta deve reggere in demo aziendale: pulita, credibile, non caricaturale. |

## 3. Personalita' visiva degli agenti

Gli agenti sono identificati visivamente per ruolo e tono, non tramite elementi infantili.

Regole:
- ogni agente ha accento colore, icona/avatar astratto, iniziali o simbolo professionale;
- avatar geometrici, non cartoon;
- stessa struttura visiva per tutti, variazione solo su accento, pattern sottile e icona;
- il turno attivo aumenta contrasto, bordo, glow leggero o motion, senza cambiare layout;
- messaggi dello stesso agente mantengono colore e identificatore costanti;
- il giudice/supervisore usa trattamento distinto: piu' neutro, centrale, autorevole, con accento metallico/luce calda.

Archetipi visuali consigliati:

| Tipo | Accento | Icona/metafora | Tono |
| --- | --- | --- | --- |
| Analitico | Cyan profondo | Prisma, lente, griglia | preciso, razionale |
| Critico | Rosso corallo scuro | Scudo, alert, taglio diagonale | prudente, sfidante |
| Strategico | Oro soft | Bussola, corona minima, nodo | sintetico, orientato alla decisione |
| Creativo | Viola controllato | Spark sobrio, forma fluida | generativo, divergente |
| Tecnico | Verde teal | Circuito lineare, parentesi | concreto, verificabile |
| Giudice | Avorio caldo + grafite | Bilancia astratta, sigillo, anello | autorevole, conclusivo |

## 4. Layout generale

Non progettare schermate fisse, ma rispettare queste regole:
- esperienza single-screen con aree chiaramente distinguibili;
- input utente sempre riconoscibile come punto di partenza;
- dibattito come area dominante;
- team/agenti come cast laterale o fascia di presenza, mai tabella gestionale;
- giudice/sintesi con peso visivo superiore e posizione stabile;
- stati di validazione, avanzamento, arresto ed errore come segnali compatti;
- su mobile, priorita': input, stato corrente, messaggi, sintesi; elementi secondari comprimibili;
- nessuna card dentro card;
- card solo per elementi individuali: agente, messaggio, sintesi, stato;
- radius massimo 8px salvo avatar circolari;
- griglie dense ma ariose, con spacing costante.

## 5. Palette colori

Palette base scura premium con accenti controllati. Evitare dominanza monocromatica viola/blu.

| Token | Valore | Uso |
| --- | --- | --- |
| `color-bg` | `#0D0F14` | Sfondo principale. |
| `color-surface` | `#171A21` | Pannelli e aree contenuto. |
| `color-surface-glass` | `rgba(255,255,255,0.07)` | Glassmorphism leggero. |
| `color-border` | `rgba(255,255,255,0.14)` | Separazioni. |
| `color-text` | `#F3F5F7` | Testo primario. |
| `color-muted` | `#A7AFBC` | Testo secondario. |
| `color-cyan` | `#4CC9D8` | Agente analitico, focus freddo. |
| `color-coral` | `#E46363` | Critica, rischio, contrasto dialettico. |
| `color-gold` | `#D8B45A` | Giudizio, sintesi, momento decisivo. |
| `color-teal` | `#51B89F` | Validazione positiva, tecnico. |
| `color-violet` | `#8F7AF5` | Creativita' controllata. |
| `color-error` | `#FF6B6B` | Errori osservabili. |
| `color-success` | `#65D6A4` | Esiti positivi. |
| `color-warning` | `#F0C66A` | Limiti, timeout, arresti controllati. |

Gradienti:
- usare gradienti sottili su superfici o bordi, non come unico linguaggio;
- preferire combinazioni grafite/cyan/gold o grafite/teal/coral;
- evitare sfondi saturi a piena pagina;
- glassmorphism: blur leggero, bordo visibile, contrasto testo sempre verificato.

## 6. Tipografia

Obiettivo: tecnologica, leggibile, autorevole.

| Ruolo | Indicazione |
| --- | --- |
| Display/title | Sans moderna con carattere, peso 650-750, uso parsimonioso. |
| Body/UI | Sans altamente leggibile, peso 400-600. |
| Mono opzionale | Solo per stati tecnici, ID o log sintetici visibili. |

Regole:
- nessun font size scalato con viewport width;
- letter spacing 0, salvo micro-label uppercase con massimo `0.04em`;
- headline energiche ma non hero ovunque;
- messaggi lunghi ottimizzati per lettura: line-height 1.45-1.6;
- testo in card compatte con heading piccoli e chiari;
- evitare tutto maiuscolo per contenuti principali.

## 7. Componenti UI

Componenti ammessi nel design system:

| Componente | Regole UX/UI |
| --- | --- |
| Input domanda | Ampio, chiaro, orientato all'azione; stato focus evidente; errore vicino al campo. |
| Pulsante primario | Forte ma sobrio; icona + label quando utile; stato loading visibile. |
| Card agente | Avatar, nome, ruolo, accento colore, stato; contenuto compatto. |
| Avatar agente | Geometrico/astratto; colore coerente; iniziali o icona. |
| Messaggio dibattito | Autore, ruolo, contenuto, turno; accento laterale o top border. |
| Indicatore turno | Evidenzia chi parla con bordo, luce o pulse discreto. |
| Stato processo | Validazione, planning, dibattito, sintesi, errore; breve e non invasivo. |
| Giudice/sintesi | Trattamento piu' autorevole: oro soft, bordo marcato, gerarchia alta. |
| Badge | Stato, ruolo, esito; pochi colori semantici. |
| Timeline leggera | Utile per progressione, senza diventare workflow tecnico. |
| Toast/alert | Solo per errori o arresti; testo breve e azionabile. |

Stati obbligatori:
- idle, focus, hover, active, loading, disabled;
- validation accepted/rejected;
- speaking, waiting, completed;
- stopped by limit, timeout, error;
- final answer ready.

## 8. Iconografia e illustrazioni

Stile icone:
- lineare, coerente, stroke uniforme;
- metafore astratte: confronto, onda, anello, sigillo, prisma, scudo, bussola;
- evitare robot cartoon, mascotte, armi, fuoco, trofei eccessivi;
- icone come supporto, mai come contenuto primario.

Illustrazioni:
- non necessarie per l'MVP se la UI ha identita' forte;
- se introdotte, devono essere astratte, premium, geometriche, coerenti con arena/dibattito;
- nessuna immagine stock generica;
- nessun elemento decorativo che sembri gaming competitivo.

## 9. UX Guidelines

- L'utente deve capire sempre: cosa sta succedendo, chi sta parlando, perche' il sistema si e' fermato, qual e' la risposta finale.
- La validazione deve dare feedback rapido e compatto.
- Il dibattito deve essere leggibile come scambio tra personalita' distinte.
- Il turno corrente deve essere piu' evidente dei turni passati.
- I messaggi non devono spostare layout critici in modo brusco.
- La risposta finale deve essere chiaramente separata dal dibattito.
- Gli errori devono spiegare il problema senza linguaggio tecnico non necessario.
- Non usare tabelle per contenuti di dibattito o agenti nella UI finale.
- Non sovraccaricare l'utente con configurazioni avanzate nell'MVP.
- La UI deve funzionare bene anche quando il contenuto testuale e' lungo.

## 10. Accessibilita'

Requisiti UI:
- contrasto testo/sfondo almeno WCAG AA;
- focus visibile su input, bottoni e controlli interattivi;
- navigazione base da tastiera;
- stati non comunicati solo dal colore;
- animazioni riducibili o disattivabili con `prefers-reduced-motion`;
- messaggi progressivi annunciabili senza interrompere continuamente la lettura;
- dimensioni touch target minime 44x44px;
- testi lunghi wrappano senza overflow;
- errori posizionati vicino alla causa e collegati semanticamente.

## 11. Animazioni e microinterazioni

Animazioni discrete, funzionali e brevi.

Usare per:
- ingresso progressivo dei messaggi;
- indicatore di turno attivo;
- transizione da dibattito a sintesi;
- feedback di invio domanda;
- stato di elaborazione;
- enfasi leggera sul giudice quando decide.

Regole:
- durata tipica 120-260ms;
- easing morbido, non elastico;
- pulse lento e molto sottile per chi parla;
- nessun tremolio, shake continuo, lampeggio aggressivo;
- niente animazioni decorative permanenti che competono col testo;
- ridurre motion su dispositivi piccoli o quando richiesto dall'utente.

## 12. Consistenza visiva

Token e regole:
- spacing base 8px;
- radius: 6-8px per card/pannelli, 999px solo per pill/avatar;
- border 1px con opacita' controllata;
- shadow morbide e rare, mai pesanti;
- glass effect solo su pannelli principali o overlay, non su ogni elemento;
- massimo 1 accento dominante per agente in una card;
- componenti con stesso ruolo devono mantenere struttura identica;
- stati semantici sempre con stesso colore;
- UI leggibile in demo su schermo grande e laptop.

## 13. Regole di implementazione UI

- Nuovi componenti, colori, tipografie, stati o stili devono essere aggiunti prima a questo documento.
- Non introdurre framework visuali o librerie UI che impongano look gestionale.
- Non usare componenti standard da dashboard se non ristilizzati secondo questo design system.
- Non usare card annidate.
- Non usare palette monocromatica o neon saturo.
- Non usare testi in-app per spiegare il design o le feature.
- Ogni componente interattivo deve avere stati hover/focus/disabled/loading quando applicabili.
- Ogni stato applicativo visibile deve avere label breve, icona o segnale coerente.
- Ogni agente visualizzato deve avere identita' cromatica e avatar coerenti per tutta la sessione.
- Il giudice/sintesi deve essere sempre distinguibile dagli agenti.
- L'implementazione deve preservare leggibilita', accessibilita' e gerarchia anche con contenuti generati lunghi.
