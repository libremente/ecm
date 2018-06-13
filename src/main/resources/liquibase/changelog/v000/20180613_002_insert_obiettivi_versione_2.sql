SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = ecmdb, pg_catalog;

update obiettivo set versione = 1;

INSERT INTO obiettivo (id, categoria, codice_cogeaps, nazionale, nome, versione) VALUES (nextval('hibernate_sequence'), 'TECNICO_PROFESSIONALI', 10, true, 'Epidemiologia - prevenzione e promozione della salute con acquisizione di nozioni tecnico-professionali', 2);
INSERT INTO obiettivo (id, categoria, codice_cogeaps, nazionale, nome, versione) VALUES (nextval('hibernate_sequence'), 'TECNICO_PROFESSIONALI', 18, true, 'Contenuti tecnico-professionali (conoscenze e competenze) specifici di ciascuna professione, di ciascuna specializzazione e di ciascuna attività ultraspecialistica ivi  incluse le malattie rare e la medicina di genere', 2);
INSERT INTO obiettivo (id, categoria, codice_cogeaps, nazionale, nome, versione) VALUES (nextval('hibernate_sequence'), 'TECNICO_PROFESSIONALI', 19, true, 'Medicine non convenzionali: valutazione dell''efficacia in ragione degli esiti e degli ambiti di complementarietà', 2);
INSERT INTO obiettivo (id, categoria, codice_cogeaps, nazionale, nome, versione) VALUES (nextval('hibernate_sequence'), 'TECNICO_PROFESSIONALI', 20, true, 'Tematiche speciali del SSN e/o SSR a carattere urgente e/o straordinario individuate dalla commissionale nazionale per la formazione continua e dalle regioni/province autonome per far fronte a specifiche emergenze sanitarie con acquisizione di nozioni di tecnico-professionali', 2);
INSERT INTO obiettivo (id, categoria, codice_cogeaps, nazionale, nome, versione) VALUES (nextval('hibernate_sequence'), 'TECNICO_PROFESSIONALI', 21, true, 'Trattamento del dolore acuto e cronico. Palliazione', 2);
INSERT INTO obiettivo (id, categoria, codice_cogeaps, nazionale, nome, versione) VALUES (nextval('hibernate_sequence'), 'TECNICO_PROFESSIONALI', 22, true, 'Fragilità  e cronicità (minori, anziani, dipendenze da stupefacenti, alcool e ludopatia, salute mentale),  nuove povertà, tutela degli aspetti assistenziali, sociosanitari, e socio-assistenziali', 2);
INSERT INTO obiettivo (id, categoria, codice_cogeaps, nazionale, nome, versione) VALUES (nextval('hibernate_sequence'), 'TECNICO_PROFESSIONALI', 23, true, 'Sicurezza alimentare e/o patologie correlate', 2);
INSERT INTO obiettivo (id, categoria, codice_cogeaps, nazionale, nome, versione) VALUES (nextval('hibernate_sequence'), 'TECNICO_PROFESSIONALI', 24, true, 'Sanità veterinaria. Attività presso gli stabulari', 2);
INSERT INTO obiettivo (id, categoria, codice_cogeaps, nazionale, nome, versione) VALUES (nextval('hibernate_sequence'), 'TECNICO_PROFESSIONALI', 25, true, 'Farmaco epidemiologia, farmacoeconomia, farmacovigilanza', 2);
INSERT INTO obiettivo (id, categoria, codice_cogeaps, nazionale, nome, versione) VALUES (nextval('hibernate_sequence'), 'TECNICO_PROFESSIONALI', 26, true, 'Sicurezza ambientale e/o patologie correlate', 2);
INSERT INTO obiettivo (id, categoria, codice_cogeaps, nazionale, nome, versione) VALUES (nextval('hibernate_sequence'), 'TECNICO_PROFESSIONALI', 27, true, 'Sicurezza negli ambienti e nei luoghi di lavoro e patologie correlate. Radioprotezione', 2);
INSERT INTO obiettivo (id, categoria, codice_cogeaps, nazionale, nome, versione) VALUES (nextval('hibernate_sequence'), 'TECNICO_PROFESSIONALI', 28, true, 'Implementazione della cultura e della sicurezza in materia di donazione trapianto', 2);
INSERT INTO obiettivo (id, categoria, codice_cogeaps, nazionale, nome, versione) VALUES (nextval('hibernate_sequence'), 'TECNICO_PROFESSIONALI', 29, true, 'Innovazione tecnologica: valutazione, miglioramento dei processi di gestione delle tecnologie biomediche e dei dispositivi medici. Health Technology Assessment', 2);
INSERT INTO obiettivo (id, categoria, codice_cogeaps, nazionale, nome, versione) VALUES (nextval('hibernate_sequence'), 'TECNICO_PROFESSIONALI', 34, true, 'Accreditamento strutture sanitarie e dei professionisti;  cultura della qualità con acquisizione di nozioni tecnico-professionali', 2);
INSERT INTO obiettivo (id, categoria, codice_cogeaps, nazionale, nome, versione) VALUES (nextval('hibernate_sequence'), 'TECNICO_PROFESSIONALI', 35, true, 'Argomenti di carattere generale: sanità digitale, informatica di livello avanzato e lingua inglese scientifica; normativa in materia sanitaria: i principi etici e civili del SSN  con acquisizione di nozioni tecnico-professionali', 2);
INSERT INTO obiettivo (id, categoria, codice_cogeaps, nazionale, nome, versione) VALUES (nextval('hibernate_sequence'), 'DI_PROCESSO', 3, true, 'Documentazione clinica. Percorsi clinico-assistenziali diagnostici e riabilitativi, profili di assistenza - profili di cura', 2);
INSERT INTO obiettivo (id, categoria, codice_cogeaps, nazionale, nome, versione) VALUES (nextval('hibernate_sequence'), 'DI_PROCESSO', 4, true, 'Appropriatezza delle prestazioni sanitarie nei LEA, sistemi di valutazione, verifica e miglioramento dell''efficienza ed efficacia', 2);
INSERT INTO obiettivo (id, categoria, codice_cogeaps, nazionale, nome, versione) VALUES (nextval('hibernate_sequence'), 'DI_PROCESSO', 7, true, 'La comunicazione efficace interna, esterna, con paziente. La privacy ed il consenso informato', 2);
INSERT INTO obiettivo (id, categoria, codice_cogeaps, nazionale, nome, versione) VALUES (nextval('hibernate_sequence'), 'DI_PROCESSO', 8, true, 'Integrazione interprofessionale e multiprofessionale, interistituzionale', 2);
INSERT INTO obiettivo (id, categoria, codice_cogeaps, nazionale, nome, versione) VALUES (nextval('hibernate_sequence'), 'DI_PROCESSO', 9, true, 'Integrazione tra assistenza territoriale ed ospedaliera', 2);
INSERT INTO obiettivo (id, categoria, codice_cogeaps, nazionale, nome, versione) VALUES (nextval('hibernate_sequence'), 'DI_PROCESSO', 11, true, 'Management sanitario. Innovazione gestionale e sperimentazione di modelli organizzativi e gestionali', 2);
INSERT INTO obiettivo (id, categoria, codice_cogeaps, nazionale, nome, versione) VALUES (nextval('hibernate_sequence'), 'DI_PROCESSO', 12, true, 'Aspetti relazionali e umanizzazione delle cure', 2);
INSERT INTO obiettivo (id, categoria, codice_cogeaps, nazionale, nome, versione) VALUES (nextval('hibernate_sequence'), 'DI_PROCESSO', 13, true, 'Metodologia e tecniche di comunicazione sociale per lo sviluppo dei programmi nazionali e regionali di prevenzione primaria', 2);
INSERT INTO obiettivo (id, categoria, codice_cogeaps, nazionale, nome, versione) VALUES (nextval('hibernate_sequence'), 'DI_PROCESSO', 14, true, 'Accreditamento strutture sanitarie e dei professionisti. La cultura della qualità con acquisizione di nozioni di processo', 2);
INSERT INTO obiettivo (id, categoria, codice_cogeaps, nazionale, nome, versione) VALUES (nextval('hibernate_sequence'), 'DI_PROCESSO', 15, true, 'Multiculturalità e cultura dell''accoglienza nell''attività sanitaria, medicina relativa alle popolazioni migranti', 2);
INSERT INTO obiettivo (id, categoria, codice_cogeaps, nazionale, nome, versione) VALUES (nextval('hibernate_sequence'), 'DI_PROCESSO', 30, true, 'Epidemiologia - prevenzione e promozione della salute con acquisizione di nozioni di processo', 2);
INSERT INTO obiettivo (id, categoria, codice_cogeaps, nazionale, nome, versione) VALUES (nextval('hibernate_sequence'), 'DI_PROCESSO', 32, true, 'Tematiche speciali del SSN e/o SSR a carattere urgente e/o straordinario individuate dalla commissionale nazionale per la formazione continua e dalle regioni/province autonome per far fronte a specifiche emergenze sanitarie con acquisizione di nozioni di processo', 2);
INSERT INTO obiettivo (id, categoria, codice_cogeaps, nazionale, nome, versione) VALUES (nextval('hibernate_sequence'), 'DI_SISTEMA', 1, true, 'Applicazione nella pratica quotidiana dei principi e delle procedure dell''evidence based practice (EBM - EBN - EBP)', 2);
INSERT INTO obiettivo (id, categoria, codice_cogeaps, nazionale, nome, versione) VALUES (nextval('hibernate_sequence'), 'DI_SISTEMA', 2, true, 'Linee guida - protocolli – procedure', 2);
INSERT INTO obiettivo (id, categoria, codice_cogeaps, nazionale, nome, versione) VALUES (nextval('hibernate_sequence'), 'DI_SISTEMA', 5, true, 'Principi, procedure e strumenti per il governo clinico delle attività sanitarie', 2);
INSERT INTO obiettivo (id, categoria, codice_cogeaps, nazionale, nome, versione) VALUES (nextval('hibernate_sequence'), 'DI_SISTEMA', 6, true, 'Sicurezza del paziente, risk management  e responsabilità professionale', 2);
INSERT INTO obiettivo (id, categoria, codice_cogeaps, nazionale, nome, versione) VALUES (nextval('hibernate_sequence'), 'DI_SISTEMA', 16, true, 'Etica, bioetica e deontologia', 2);
INSERT INTO obiettivo (id, categoria, codice_cogeaps, nazionale, nome, versione) VALUES (nextval('hibernate_sequence'), 'DI_SISTEMA', 17, true, 'Argomenti di carattere generale: sanità digitale, informatica di livello avanzato e lingua inglese scientifica. Normativa in materia sanitaria: i principi etici e civili del SSN  con acquisizione di nozioni di sistema', 2);
INSERT INTO obiettivo (id, categoria, codice_cogeaps, nazionale, nome, versione) VALUES (nextval('hibernate_sequence'), 'DI_SISTEMA', 31, true, 'Epidemiologia - prevenzione e promozione della salute con acquisizione di nozioni di sistema', 2);
INSERT INTO obiettivo (id, categoria, codice_cogeaps, nazionale, nome, versione) VALUES (nextval('hibernate_sequence'), 'DI_SISTEMA', 33, true, 'Tematiche speciali del SSN e/o SSR  a carattere urgente e/o straordinario individuate dalla commissionale nazionale per la formazione continua e dalle regioni/province autonome per far fronte a specifiche emergenze sanitarie con acquisizione di nozioni di sistema', 2);

update ecmdb.obiettivo set nome = '(' || codice_cogeaps || ') '  || nome where versione = 2;