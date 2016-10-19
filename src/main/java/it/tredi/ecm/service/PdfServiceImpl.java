package it.tredi.ecm.service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.List;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import it.tredi.ecm.dao.entity.File;
import it.tredi.ecm.dao.enumlist.FileEnum;
import it.tredi.ecm.pdf.PdfAccreditamentoProvvisorioAccreditatoInfo;
import it.tredi.ecm.pdf.PdfAccreditamentoProvvisorioIntegrazionePreavvisoRigettoInfo;
import it.tredi.ecm.pdf.PdfAccreditamentoProvvisorioRigettoInfo;

@Service
public class PdfServiceImpl implements PdfService {
	private static Logger LOGGER = LoggerFactory.getLogger(WorkflowServiceImpl.class);

	@Autowired
	private FileService fileService;

	private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	//private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

    //tipi font
	private int sizeIntesta = 11;
	private int sizeCorpo = 11;
	private Font.FontFamily fontFamily = Font.FontFamily.TIMES_ROMAN;
	private Font fontDenominazioneProvider = new Font(fontFamily, sizeIntesta, Font.BOLD);
	private Font fontIndirizzoProvider = new Font(fontFamily, sizeIntesta, Font.NORMAL);
	private Font fontCorpo = new Font(fontFamily, sizeCorpo, Font.NORMAL);
	private Font fontListItem = new Font(fontFamily, sizeCorpo, Font.NORMAL);
	private Font fontListItemBold = new Font(fontFamily, sizeCorpo, Font.BOLD);
	private float indentationLeftList = 15F;
	private float spacingBefore = 10F;
	private float spacingAfter = 10F;	
	private float cellPadding = 5F;

	@Override
	public File creaPdfAccreditamentoProvvisiorioIntegrazione(PdfAccreditamentoProvvisorioIntegrazionePreavvisoRigettoInfo integrazioneInfo) throws Exception {
        ByteArrayOutputStream byteArrayOutputStreamAccreditata = new ByteArrayOutputStream();
        writePdfAccreditamentoProvvisiorioIntegrazione(byteArrayOutputStreamAccreditata, integrazioneInfo);
        
		File file = new File();
		file.setData(byteArrayOutputStreamAccreditata.toByteArray());
		file.setDataCreazione(LocalDate.now());
		file.setNomeFile(FileEnum.FILE_ACCREDITAMENTO_PROVVISORIO_INTEGRAZIONE.getNome() + ".pdf");
		file.setTipo(FileEnum.FILE_ACCREDITAMENTO_PROVVISORIO_INTEGRAZIONE);
		fileService.save(file);
		return file;
	}

	@Override
	public File creaPdfAccreditamentoProvvisiorioPreavvisoRigetto(PdfAccreditamentoProvvisorioIntegrazionePreavvisoRigettoInfo preavvisoRigettoInfo) throws Exception {
        ByteArrayOutputStream byteArrayOutputStreamAccreditata = new ByteArrayOutputStream();
        writePdfAccreditamentoProvvisiorioPreavvisoRigetto(byteArrayOutputStreamAccreditata, preavvisoRigettoInfo);
        
		File file = new File();
		file.setData(byteArrayOutputStreamAccreditata.toByteArray());
		file.setDataCreazione(LocalDate.now());
		file.setNomeFile(FileEnum.FILE_ACCREDITAMENTO_PROVVISORIO_PREAVVISO_RIGETTO.getNome() + ".pdf");
		file.setTipo(FileEnum.FILE_ACCREDITAMENTO_PROVVISORIO_PREAVVISO_RIGETTO);
		fileService.save(file);
		return file;
	}

	@Override
	public File creaPdfAccreditamentoProvvisiorioDiniego(PdfAccreditamentoProvvisorioRigettoInfo diniegoInfo) throws Exception {
        ByteArrayOutputStream byteArrayOutputStreamAccreditata = new ByteArrayOutputStream();
        writePdfAccreditamentoProvvisiorioDiniego(byteArrayOutputStreamAccreditata, diniegoInfo);
        
		File file = new File();
		file.setData(byteArrayOutputStreamAccreditata.toByteArray());
		file.setDataCreazione(LocalDate.now());
		file.setNomeFile(FileEnum.FILE_ACCREDITAMENTO_PROVVISORIO_DECRETO_DINIEGO.getNome() + ".pdf");
		file.setTipo(FileEnum.FILE_ACCREDITAMENTO_PROVVISORIO_DECRETO_DINIEGO);
		fileService.save(file);
		return file;
	}

	@Override
	public File creaPdfAccreditamentoProvvisiorioAccreditato(PdfAccreditamentoProvvisorioAccreditatoInfo accreditatoInfo) throws Exception {
        ByteArrayOutputStream byteArrayOutputStreamAccreditata = new ByteArrayOutputStream();
        writePdfAccreditamentoProvvisiorioAccreditato(byteArrayOutputStreamAccreditata, accreditatoInfo);
        
		File file = new File();
		file.setData(byteArrayOutputStreamAccreditata.toByteArray());
		file.setDataCreazione(LocalDate.now());
		file.setNomeFile(FileEnum.FILE_ACCREDITAMENTO_PROVVISORIO_DECRETO_ACCREDITAMENTO.getNome() + ".pdf");
		file.setTipo(FileEnum.FILE_ACCREDITAMENTO_PROVVISORIO_DECRETO_ACCREDITAMENTO);
		fileService.save(file);
		return file;
	}
    	
	private void writePdfAccreditamentoProvvisiorioPreavvisoRigetto(OutputStream outputStream, PdfAccreditamentoProvvisorioIntegrazionePreavvisoRigettoInfo preavvisoRigettoInfo) throws Exception {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);

            document.open();
            //Info documento
            document.addAuthor("Ecm");
            document.addCreationDate();
            document.addCreator("Ecm");
            document.addTitle("Preavviso rigetto");
            
            
            //DENOMINAZIONE PROVIDER
            Paragraph parDenominazioneProvider = new Paragraph(); 
            parDenominazioneProvider.setAlignment(Element.ALIGN_RIGHT);
            parDenominazioneProvider.setFont(fontDenominazioneProvider);
            parDenominazioneProvider.add(preavvisoRigettoInfo.getProviderInfo().getProviderDenominazione());
            document.add(parDenominazioneProvider);
            
            //INDIRIZZO PROVIDER
            Paragraph parIndirizzoProvider = new Paragraph(); 
            parIndirizzoProvider.setAlignment(Element.ALIGN_RIGHT);
            parIndirizzoProvider.setFont(fontIndirizzoProvider);
            parIndirizzoProvider.add(preavvisoRigettoInfo.getProviderInfo().getProviderIndirizzo());
            document.add(parIndirizzoProvider);

            //CAP – COMUNE – PROVINCIA PROVIDER
            Paragraph parCapComuneProvincia = new Paragraph(); 
            parCapComuneProvincia.setAlignment(Element.ALIGN_RIGHT);
            parCapComuneProvincia.setFont(fontIndirizzoProvider);
            MessageFormat msgFormat = new MessageFormat("{0} - {1} - {2}");
            Object[] values = {preavvisoRigettoInfo.getProviderInfo().getProviderCap(), preavvisoRigettoInfo.getProviderInfo().getProviderComune(), preavvisoRigettoInfo.getProviderInfo().getProviderProvincia()};
            parCapComuneProvincia.add(msgFormat.format(values));
            //parCapComuneProvincia.add(preavvisoRigettoInfo.getProviderCap() + " – " + preavvisoRigettoInfo.getProviderComune() + " – " + preavvisoRigettoInfo.getProviderProvincia());
            document.add(parCapComuneProvincia);

            //Linea vuota
            Paragraph parLineaVuota = new Paragraph(); 
            parLineaVuota.setAlignment(Element.ALIGN_RIGHT);
            parLineaVuota.setFont(fontIndirizzoProvider);
            parLineaVuota.add(" ");
            document.add(parLineaVuota);

            //Alla c.a.:     NOME e COGNOME LR PROVIDER
            Paragraph parNomeCognome = new Paragraph(); 
            parNomeCognome.setAlignment(Element.ALIGN_RIGHT);
            parNomeCognome.setFont(fontDenominazioneProvider);
            msgFormat = new MessageFormat("Alla c.a.: {0} {1}");
            Object[] valuesNomCognProv = {preavvisoRigettoInfo.getProviderInfo().getProviderNomeLegaleRappresentante(), preavvisoRigettoInfo.getProviderInfo().getProviderCognomeLegaleRappresentante()};
            parNomeCognome.add(msgFormat.format(valuesNomCognProv));
            
            //parNomeCognome.add("Alla c.a.: " + preavvisoRigettoInfo.getProviderNomeLegaleRappresentante() + " " + preavvisoRigettoInfo.getProviderCognomeLegaleRappresentante());
            parNomeCognome.setSpacingAfter(spacingAfter);
            document.add(parNomeCognome);
            
            //.SetLeading(fixed, multiplied)
            /*
			The first parameter is the fixed leading: if you want a leading of 15 no matter which font size is used, you can choose fixed = 15 and multiplied = 0.
			The second parameter is a factor: for instance if you want the leading to be twice the font size, you can choose fixed = 0 and multiplied = 2. In this case, the leading for a paragraph with font size 12 will be 24, for a font size 10, it will be 20, and son on.
			You can also combine fixed and multiplied leading.
			*/
            //PEC: INDIRIZZO PEC PROVIDER
            Paragraph parPec = new Paragraph(); 
            parPec.setAlignment(Element.ALIGN_LEFT);
            parPec.setFont(fontDenominazioneProvider);
            msgFormat = new MessageFormat("PEC: {0}");
            Object[] valuesPec = {preavvisoRigettoInfo.getProviderInfo().getProviderPec()};
            parPec.add(msgFormat.format(valuesPec));
            //parPec.add("PEC: " + preavvisoRigettoInfo.getProviderPec());
            parPec.setSpacingAfter(spacingAfter);
            document.add(parPec);
            
            //Oggetto:  Comunicazione motivi ostativi all’accoglimento della domanda ai sensi dell’art. 10 bis della l.241/90 e successive integrazioni e modificazioni.
            Paragraph parOggetto = new Paragraph(); 
            parOggetto.setAlignment(Element.ALIGN_LEFT);
            parOggetto.setFont(fontDenominazioneProvider);
            parOggetto.add("Oggetto:  Comunicazione motivi ostativi all’accoglimento della domanda ai sensi dell’art. 10 bis della l.241/90 e successive integrazioni e modificazioni.");
            parOggetto.setSpacingAfter(spacingAfter);
            //Interlinea OK
            //parOggetto.setMultipliedLeading(5);
            //Interlinea da provare
            //parOggetto.setLeading(0F, 2F);
            document.add(parOggetto);
            
            //Corpo
            //In ordine alla Vs. domanda di accreditamento in qualità di Provider Provvisorio, validata il DATA VALIDAZIONE ACCREDITAMENTO PROVVISORIO, sulla base della normativa in calce, nonché dell’ulteriore regolamentazione relativa alla materia (consultabile sul sito internet della Regione), si rappresenta che la Commissione Regionale per la Formazione Continua ECM nella seduta del DATA SEDUTA, non ritenendo rispettati i requisiti di cui alla normativa sotto richiamata, ha manifestato l’intento di esprimere parere negativo in riferimento alle seguenti criticità di seguito indicate:
            msgFormat = new MessageFormat("In ordine alla Vs. domanda di accreditamento in qualità di Provider Provvisorio, validata il {0} "
            		+ ", sulla base della normativa in calce, nonché dell’ulteriore regolamentazione relativa alla materia (consultabile sul sito internet della Regione), " 
            		+ " si rappresenta che la Commissione Regionale per la Formazione Continua ECM nella seduta del {1}" 
            		+ ", non ritenendo rispettati i requisiti di cui alla normativa sotto richiamata, " 
            		+ "ha manifestato l’intento di esprimere parere negativo in riferimento alle seguenti criticità di seguito indicate:");
            Object[] valuesCorpo = {preavvisoRigettoInfo.getAccreditamentoDataValidazione().format(dateTimeFormatter), preavvisoRigettoInfo.getAccreditamentoDataSeduta().format(dateTimeFormatter)};
            addCorpoParagraph(document, false, true, msgFormat.format(valuesCorpo));

            /*
            testo = "In ordine alla Vs. domanda di accreditamento in qualità di Provider Provvisorio, validata il "
            		+ preavvisoRigettoInfo.getAccreditamentoDataValidazione().format(dateTimeFormatter) 
            		+ ", sulla base della normativa in calce, nonché dell’ulteriore regolamentazione relativa alla materia (consultabile sul sito internet della Regione), si rappresenta che la Commissione Regionale per la Formazione Continua ECM nella seduta del " 
            		+ preavvisoRigettoInfo.getAccreditamentoDataSeduta().format(dateTimeFormatter)
            		+ ", non ritenendo rispettati i requisiti di cui alla normativa sotto richiamata, ha manifestato l’intento di esprimere parere negativo in riferimento alle seguenti criticità di seguito indicate:"; 
            addCorpoParagraph(document, false, true, testo);
            */
            
            
            List list;
            //Elenco numerato
            if(preavvisoRigettoInfo.getListaCriticita() != null && preavvisoRigettoInfo.getListaCriticita().size() > 0) {
	            list = new List(List.ORDERED);
	            list.setIndentationLeft(indentationLeftList);
	            for(String criticita : preavvisoRigettoInfo.getListaCriticita()) 
	            	list.add(getListItem(criticita, fontListItemBold));
	            document.add(list);
            }
            //Note seduta
            Paragraph par = new Paragraph(preavvisoRigettoInfo.getNoteSedutaDomanda(), fontListItemBold);
	        par.setAlignment(Element.ALIGN_JUSTIFIED);
	        par.setSpacingBefore(0);
	        par.setSpacingAfter(spacingAfter);
	        par.setIndentationLeft(indentationLeftList);
	        document.add(par);

            
            addCorpoParagraph(document, true, true, "Ai sensi dell’art. 10 bis della legge 241/1990, si invita ad inserire osservazioni, eventualmente corredate da integrazioni documentali. " 
            		+ "La documentazione, debitamente sottoscritta in maniera autografa e trasmessa con firma “digitale qualificata” del legale rappresentante in file formato PDF inferiori a 2Mb" 
            		+ ", deve essere inserita entro 10 giorni dal ricevimento della presente nota.");
            addCorpoParagraph(document, false, true, "Si sottolinea che le modifiche, così pervenute, saranno sottoposte alla valutazione di competenza della Commissione Regionale per la Formazione Continua " 
            		+ "in Medicina ECM, previo riscontro da parte della scrivente Segreteria.");
            addCorpoParagraph(document, false, true, "Con l’occasione, la scrivente Segreteria comunica che l’aspirante provider ha la facoltà di chiedere una consulenza alla Segreteria della CRFC " 
            		+ "per delucidazioni in merito al contenuto della presente nota. " 
            		+ "La consulenza può essere richiesta tramite la funzione “Comunicazioni” scegliendo tra le “Tipologie”: “Consulenza 10 Bis” all’interno dell’Area Riservata ECM " 
            		+ "nel sito http://providerveneto.agenas.it, allegando l’immagine digitalizzata (scannerizzata) della richiesta firmata, in forma autografa, dal legale rappresentante. "
            		+ "La richiesta deve contenere le motivazioni e gli argomenti per i quali si chiede la consulenza.");

            //interruzione di pagina
            document.newPage();
            
            addCorpoParagraph(document, false, true, "Normativa di riferimento:");
            
            list = new List(List.ORDERED);
            //Elimina l'indentazione del testo rispetto al simbolo, occorre vedere come indentare il testo
            //list.setAutoindent(false);
            list.setIndentationLeft(indentationLeftList);
            list.add(getListItem("Legge n. 241/1990 e successive modificazioni ed integrazioni;", fontListItem));
            list.add(getListItem("Raccomandazione della Comunità Europea n. 11/03/2002/236/CE;", fontListItem));
            list.add(getListItem("Accordo tra il Governo, le Regioni e le Province autonome di Trento e di Bolzano del 01/08/2007, concernente il “Riordino del sistema di Formazione continua in Medicina\" (L. 244/2007);", fontListItem));
            list.add(getListItem("Accordo del 05/11/2009, concernente il nuovo sistema di formazione continua in medicina – Accreditamento dei Provider ECM, formazione a distanza, obiettivi formativi, valutazione della qualità del sistema formativo sanitario, attività formative realizzate all’estero, liberi professionisti (D.P.C.M. 26 luglio 2010);", fontListItem));
            list.add(getListItem("Deliberazione della Giunta Regionale n. 2215 del 20 dicembre 2011. Programma regionale d’Educazione Continua in Medicina (ECM) anno 2011. Approvazione dei requisiti e delle procedure di accreditamento dei Provider regionali.", fontListItem));
            list.add(getListItem("Accordo del 19/04/2012 recante “Il nuovo sistema di formazione continua in medicina – Linee guida per i Manuali di accreditamento dei provider, albo nazionale dei provider, crediti formativi triennio 2011/2013, federazioni, ordini, collegi e associazioni professionali, sistema di verifiche, controlli e monitoraggio della qualità, liberi professionisti”.", fontListItem));
            list.add(getListItem("Deliberazione della Giunta Regionale n. 1969 del 02 ottobre 2012. Programma regionale d’Educazione Continua in Medicina (ECM) anno 2012. Recepimento dell’Accordo Stato/Regioni del 19 aprile 2012, rep. 101/CSR “Il nuovo sistema di formazione continua in medicina – Linee guida per i Manuali di accreditamento dei provider, albo nazionale dei provider, crediti formativi triennio 2011/2013, federazioni, ordini, collegi e associazioni professionali, sistema di verifiche, controlli e monitoraggio della qualità, liberi professionisti”", fontListItem));
            //list.add(getListItem("Deliberazione della Giunta Regionale n. 1236 del 16 ottobre 2013 “Approvazione dello schema di Convenzione tra l’Agenzia Nazionale per i Servizi Sanitari Regionali – Agenas e la Regione Veneto finalizzato alla gestione del sistema di formazione Continua”.", fontListItem));
            list.add(getListItem("Deliberazione della Giunta Regionale n. 1753 del 29 settembre 2014 “Programma regionale per l’educazione continua in medicina. Sviluppo e ruolo dei Provider ECM pubblici (Aziende sanitarie e ospedaliere, Istituto Oncologico Veneto) nella realizzazione del Piano Regionale della formazione continua in medicina ECM. Approvazione delle procedure e delle modalità per la conduzione delle visite di verifica nell’ambito del processo di accreditamento standard dei Provider ECM ai sensi della DGR n. 2215 del 20.12.2011. Attivazione del corso di formazione dei valutatori e funzionalità dell’osservatorio regionale per la formazione continua (nomina del Coordinatore e sostituzione componenti).", fontListItem));
            list.add(getListItem("Deliberazione della Giunta Regionale n. 1247 del 28/09/2015 “Programma regionale per la formazione continua. Definizione delle evidenze documentali per la verifica dei requisiti dei Provider regionali di formazione pubblici e privati, previste nell’ambito del processo di accreditamento standard ai sensi della DGR n. 1753/2014. Proroga delle attività degli organismi di governance dell’ECM. Disciplina delle attività di monitoraggio presso le sedi dei Provider ECM”.", fontListItem));

            document.add(list);
            	
            document.close();
            outputStream.close();
        } catch (Exception e) {
        	LOGGER.error("writePdfAccreditamentoProvvisiorioPreavvisoRigetto impossibile creare il pdf", e);
            throw e;
        }
    }
    
	private void writePdfAccreditamentoProvvisiorioIntegrazione(OutputStream outputStream, PdfAccreditamentoProvvisorioIntegrazionePreavvisoRigettoInfo integrazioneInfo) throws Exception {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);

            document.open();
            //Info documento
            document.addAuthor("Ecm");
            document.addCreationDate();
            document.addCreator("Ecm");
            document.addTitle("Richiesta integrazione");
            
            //DENOMINAZIONE PROVIDER
            Paragraph parDenominazioneProvider = new Paragraph(); 
            parDenominazioneProvider.setAlignment(Element.ALIGN_RIGHT);
            parDenominazioneProvider.setFont(fontDenominazioneProvider);
            parDenominazioneProvider.add(integrazioneInfo.getProviderInfo().getProviderDenominazione());
            document.add(parDenominazioneProvider);
            
            //INDIRIZZO PROVIDER
            Paragraph parIndirizzoProvider = new Paragraph(); 
            parIndirizzoProvider.setAlignment(Element.ALIGN_RIGHT);
            parIndirizzoProvider.setFont(fontIndirizzoProvider);
            parIndirizzoProvider.add(integrazioneInfo.getProviderInfo().getProviderIndirizzo());
            document.add(parIndirizzoProvider);

            //CAP – COMUNE – PROVINCIA PROVIDER
            Paragraph parCapComuneProvincia = new Paragraph(); 
            parCapComuneProvincia.setAlignment(Element.ALIGN_RIGHT);
            parCapComuneProvincia.setFont(fontIndirizzoProvider);
            MessageFormat msgFormat = new MessageFormat("{0} - {1} - {2}");
            Object[] values = {integrazioneInfo.getProviderInfo().getProviderCap(), integrazioneInfo.getProviderInfo().getProviderComune(), integrazioneInfo.getProviderInfo().getProviderProvincia()};
            parCapComuneProvincia.add(msgFormat.format(values));
            document.add(parCapComuneProvincia);

            //Linea vuota
            Paragraph parLineaVuota = new Paragraph(); 
            parLineaVuota.setAlignment(Element.ALIGN_RIGHT);
            parLineaVuota.setFont(fontIndirizzoProvider);
            parLineaVuota.add(" ");
            document.add(parLineaVuota);

            //Alla c.a.:     NOME e COGNOME LR PROVIDER
            Paragraph parNomeCognome = new Paragraph(); 
            parNomeCognome.setAlignment(Element.ALIGN_RIGHT);
            parNomeCognome.setFont(fontDenominazioneProvider);
            msgFormat = new MessageFormat("Alla c.a.: {0} {1}");
            Object[] valuesNomCognProv = {integrazioneInfo.getProviderInfo().getProviderNomeLegaleRappresentante(), integrazioneInfo.getProviderInfo().getProviderCognomeLegaleRappresentante()};
            parNomeCognome.add(msgFormat.format(valuesNomCognProv));
            parNomeCognome.setSpacingAfter(spacingAfter);
            document.add(parNomeCognome);
            
            //PEC: INDIRIZZO PEC PROVIDER
            Paragraph parPec = new Paragraph(); 
            parPec.setAlignment(Element.ALIGN_LEFT);
            parPec.setFont(fontDenominazioneProvider);
            msgFormat = new MessageFormat("PEC: {0}");
            Object[] valuesPec = {integrazioneInfo.getProviderInfo().getProviderPec()};
            parPec.add(msgFormat.format(valuesPec));
            parPec.setSpacingAfter(spacingAfter);
            document.add(parPec);
            
            //Oggetto:  Richiesta integrazione documentazione ai sensi della l. 241/90 e successive modificazioni e integrazioni.
            Paragraph parOggetto = new Paragraph(); 
            parOggetto.setAlignment(Element.ALIGN_LEFT);
            parOggetto.setFont(fontDenominazioneProvider);
            parOggetto.add("Oggetto:  Richiesta integrazione documentazione ai sensi della l. 241/90 e successive modificazioni e integrazioni.");
            parOggetto.setSpacingAfter(spacingAfter);
            //Interlinea OK
            //parOggetto.setMultipliedLeading(5);
            //Interlinea da provare
            //parOggetto.setLeading(0F, 2F);
            document.add(parOggetto);
            
            //Corpo
            //In ordine alla Vs. domanda di accreditamento in qualità di Provider Provvisorio, validata il DATA VALIDAZIONE ACCREDITAMENTO PROVVISORIO, sulla base della normativa in calce, nonché dell’ulteriore regolamentazione relativa alla materia (consultabile sul sito internet della Regione), si rappresenta che la Commissione Regionale per la Formazione Continua ECM nella seduta del DATA SEDUTA, non ritenendo rispettati i requisiti di cui alla normativa sotto richiamata, ha manifestato l’intento di esprimere parere negativo in riferimento alle seguenti criticità di seguito indicate:
            msgFormat = new MessageFormat("In ordine alla Vs. domanda di accreditamento in qualità di Provider Provvisorio, validata il {0}, "
            		+ "sulla base della normativa in calce, nonché dell’ulteriore regolamentazione relativa alla materia "
            		+ "(consultabile sul sito internet della Regione http://providerveneto.agenas.it/ProviderModuliDoc.aspx, unitamente agli esempi di “atti”), " 
            		+ "la Commissione Regionale per la Formazione Continua ECM in data {1}, ha rilevato le seguenti criticità:");
            Object[] valuesCorpo = {integrazioneInfo.getAccreditamentoDataValidazione().format(dateTimeFormatter), integrazioneInfo.getAccreditamentoDataSeduta().format(dateTimeFormatter)};
            addCorpoParagraph(document, false, true, msgFormat.format(valuesCorpo));

            List list;
            //Elenco numerato
            if(integrazioneInfo.getListaCriticita() != null && integrazioneInfo.getListaCriticita().size() > 0) {
	            list = new List(List.ORDERED);
	            list.setIndentationLeft(indentationLeftList);
	            for(String criticita : integrazioneInfo.getListaCriticita()) 
	            	list.add(getListItem(criticita, fontListItemBold));
	            document.add(list);
            }
            //Note seduta
            Paragraph par = new Paragraph(integrazioneInfo.getNoteSedutaDomanda(), fontListItemBold);
	        par.setAlignment(Element.ALIGN_JUSTIFIED);
	        par.setSpacingBefore(0);
	        par.setSpacingAfter(spacingAfter);
	        par.setIndentationLeft(indentationLeftList);
	        document.add(par);
            

            addCorpoParagraph(document, true, true, "Al fine di sanare le stesse, si richiede di produrre adeguata documentazione, suddivisa in file, "
            		+ "debitamente sottoscritta in maniera autografa e trasmessa con firma “digitale qualificata” del legale rappresentante in file formato PDF inferiori a 2Mb, "
            		+ "tale documentazione deve essere inserita entro 30 giorni dal ricevimento della presente nota.");
            
            
            addCorpoParagraph(document, false, true, "La documentazione oggetto di integrazione sarà verificata dalla scrivente Sezione e, nel caso di esito positivo del riscontro, sarà proposta in valutazione per l’eventuale provvedimento di accreditamento.");
            addCorpoParagraph(document, false, true, "Con l’occasione, la scrivente Sezione comunica che l’aspirante provider ha la facoltà di chiedere una consulenza alla Sezione della CRFC per delucidazioni in merito al contenuto della presente nota. La consulenza può essere richiesta tramite la funzione “Comunicazioni”, allegando l’immagine digitalizzata (scannerizzata) della richiesta firmata, in forma autografa, dal legale rappresentante. La richiesta deve contenere le motivazioni e gli argomenti per i quali si chiede la consulenza e deve avere per oggetto  “Richiesta consulenza in merito alla proposta di integrazione documentale del (inserire la data di ricevimento della presente nota).");
            addCorpoParagraph(document, false, true, "La presente comunicazione è inviata ai sensi della legge 7 agosto 1990, n. 241 e successive integrazioni e modifiche.");

            //interruzione di pagina
            document.newPage();
            
            addCorpoParagraph(document, false, true, "Normativa di riferimento:");
            
            list = new List(List.ORDERED);
            //Elimina l'indentazione del testo rispetto al simbolo, occorre vedere come indentare il testo
            //list.setAutoindent(false);
            list.setIndentationLeft(indentationLeftList);
            list.add(getListItem("Legge n. 241/1990 e successive modificazioni ed integrazioni;", fontListItem));
            list.add(getListItem("Raccomandazione della Comunità Europea n. 11/03/2002/236/CE;", fontListItem));
            list.add(getListItem("Accordo tra il Governo, le Regioni e le Province autonome di Trento e di Bolzano del 01/08/2007, concernente il “Riordino del sistema di Formazione continua in Medicina” (L. 244/2007);", fontListItem));
            list.add(getListItem("Accordo del 05/11/2009, concernente il nuovo sistema di formazione continua in medicina – Accreditamento dei Provider ECM, formazione a distanza, obiettivi formativi, valutazione della qualità del sistema formativo sanitario, attività formative realizzate all’estero, liberi professionisti (D.P.C.M. 26 luglio 2010);", fontListItem));
            list.add(getListItem("Deliberazione della Giunta Regionale n. 2215 del 20 dicembre 2011. Programma regionale d’Educazione Continua in Medicina (ECM) anno 2011. Approvazione dei requisiti e delle procedure di accreditamento dei Provider regionali.", fontListItem));
            list.add(getListItem("Accordo del 19/04/2012 recante “Il nuovo sistema di formazione continua in medicina – Linee guida per i Manuali di accreditamento dei provider, albo nazionale dei provider, crediti formativi triennio 2011/2013, federazioni, ordini, collegi e associazioni professionali, sistema di verifiche, controlli e monitoraggio della qualità, liberi professionisti”.", fontListItem));
            list.add(getListItem("Deliberazione della Giunta Regionale n. 1969 del 02 ottobre 2012. Programma regionale d’Educazione Continua in Medicina (ECM) anno 2012. Recepimento dell’Accordo Stato/Regioni del 19 aprile 2012, rep. 101/CSR “Il nuovo sistema di formazione continua in medicina – Linee guida per i Manuali di accreditamento dei provider, albo nazionale dei provider, crediti formativi triennio 2011/2013, federazioni, ordini, collegi e associazioni professionali, sistema di verifiche, controlli e monitoraggio della qualità, liberi professionisti”", fontListItem));
            //list.add(getListItem("Deliberazione della Giunta Regionale n. 1236 del 16 ottobre 2013 “Approvazione dello schema di Convenzione tra l’Agenzia Nazionale per i Servizi Sanitari Regionali – Agenas e la Regione Veneto finalizzato alla gestione del sistema di formazione Continua”.", fontListItem));
            list.add(getListItem("Deliberazione della Giunta Regionale n. 1753 del 29 settembre 2014 “Programma regionale per l’educazione continua in medicina. Sviluppo e ruolo dei Provider ECM pubblici (Aziende sanitarie e ospedaliere, Istituto Oncologico Veneto) nella realizzazione del Piano Regionale della formazione continua in medicina ECM. Approvazione delle procedure e delle modalità per la conduzione delle visite di verifica nell’ambito del processo di accreditamento standard dei Provider ECM ai sensi della DGR n. 2215 del 20.12.2011. Attivazione del corso di formazione dei valutatori e funzionalità dell’osservatorio regionale per la formazione continua (nomina del Coordinatore e sostituzione componenti).", fontListItem));
            list.add(getListItem("Deliberazione della Giunta Regionale n. 1247 del 28/09/2015 “Programma regionale per la formazione continua. Definizione delle evidenze documentali per la verifica dei requisiti dei Provider regionali di formazione pubblici e privati, previste nell’ambito del processo di accreditamento standard ai sensi della DGR n. 1753/2014. Proroga delle attività degli organismi di governance dell’ECM. Disciplina delle attività di monitoraggio presso le sedi dei Provider ECM”.", fontListItem));

            document.add(list);
            	
            document.close();
            outputStream.close();
            
        } catch (Exception e) {
        	LOGGER.error("writePdfAccreditamentoProvvisiorioIntegrazione impossibile creare il pdf", e);
            throw e;
        }
    	
    }

	private void writePdfAccreditamentoProvvisiorioDiniego(OutputStream outputStream, PdfAccreditamentoProvvisorioRigettoInfo diniegoInfo) throws Exception {
        try {
            Object[] valuesProvDenom = {diniegoInfo.getProviderInfo().getProviderDenominazione()};
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            
            //Inserimento immagine
            //TODO correggere pe l'applicazione Ecm
            Image img = null;
    		URL url = Thread.currentThread().getContextClassLoader().getResource("LogoRegioneVeneto.png");
    		//String pathImgFile = "C:\\__Progetti\\ECM\\Doc da produrre in pdf\\LogoRegioneVeneto.png";
    		try {
    			img = Image.getInstance(url);
    			//img = Image.getInstance(pathImgFile);
    			Float scala = 1.2F;
    			Float width = 400F/scala;
    			Float height = 85F/scala;
    			img.scaleToFit(width, height);
                img.setAlignment(Element.ALIGN_CENTER);
    		} catch(Exception e) {
    			//Non mostro l'immagine
    		}

            document.open();
            //Info documento
            document.addAuthor("Ecm");
            document.addCreationDate();
            document.addCreator("Ecm");
            document.addTitle("Richiesta integrazione");
            
            if(img != null)
            	document.add(img);

            //DECRETO N. ………………  DEL ……….…………
            addCorpoParagraph(document, false, true, "DECRETO N. ………………  DEL ……….…………");
            
            //OGGETTO: Programma regionale per l’Educazione Continua in Medicina (ECM): rigetto dell’istanza di accreditamento provvisorio come provider regionale ECM di NOME PROVIDER ai sensi delle DD.G.R. n. 1969 del 2 ottobre 2012 e n. 1236 del 16 luglio 2013.
            MessageFormat msgFormat = new MessageFormat("OGGETTO: Programma regionale per l’Educazione Continua in Medicina (ECM): rigetto dell’istanza di accreditamento provvisorio come provider regionale ECM di {0} ai sensi delle DD.G.R. n. 1969 del 2 ottobre 2012 e n. 1236 del 16 luglio 2013.");
            addCorpoParagraph(document, false, true, msgFormat.format(valuesProvDenom));

            //Linea vuota
            /*
            Paragraph parLineaVuota = new Paragraph(); 
            parLineaVuota.setAlignment(Element.ALIGN_LEFT);
            parLineaVuota.setFont(fontCorpo);
            parLineaVuota.add(" ");
			document.add(parLineaVuota);
            */
            
            //Tabella
            //NOTE PER LA TRASPARENZA:
			PdfPTable tableNoteTrasp = new PdfPTable(1);
			tableNoteTrasp.setWidthPercentage(100);
			tableNoteTrasp.setWidths(new int[]{1});
			
			//tableNoteTrasp.setTotalWidth(new float[]{520});
			//tableNoteTrasp.setLockedWidth(true);
			
			tableNoteTrasp.setHorizontalAlignment(Element.ALIGN_CENTER);
			tableNoteTrasp.setSpacingBefore(spacingBefore);
			tableNoteTrasp.setSpacingAfter(spacingAfter);

			PdfPCell cell = new PdfPCell(new Phrase("NOTE PER LA TRASPARENZA:", fontCorpo));
			cell.setPadding(cellPadding);
			//cell.setBorder(PdfPCell.NO_BORDER);
			tableNoteTrasp.addCell(cell);
            
			cell = new PdfPCell();
			cell.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
			//cell.setSpaceCharRatio(0);
			cell.setPadding(cellPadding);
			//cell.setPaddingBottom(20);
			msgFormat = new MessageFormat("La fase attuativa degli Accordi Stato-Regioni vigenti in materia di ECM prevede il passaggio da un sistema di accreditamento degli eventi ad un sistema di accreditamento dei Provider, intesi come soggetti attivi e qualificati nel campo della formazione continua in sanità abilitati a realizzare attività formative riconosciute idonee per il sistema di formazione continua (ECM). Con il presente provvedimento a seguito dell’istruttoria effettuata si procede al rigetto dell’istanza di accreditamento provvisorio come Provider regionale di {0}.");
			
			Paragraph par = new Paragraph(); 
	        par.setAlignment(Element.ALIGN_JUSTIFIED);
	        par.setFont(fontCorpo);
	        par.add(msgFormat.format(valuesProvDenom));
	        par.setSpacingBefore(0);
	        par.setSpacingAfter(spacingAfter);
			cell.addElement(par);
			
            //Linea vuota
            Paragraph parLineaVuota = new Paragraph(); 
            parLineaVuota.setAlignment(Element.ALIGN_LEFT);
            parLineaVuota.setFont(fontCorpo);
            parLineaVuota.add(" ");
			//cell.addElement(parLineaVuota);
			
			cell.addElement(new Phrase("Estremi dei principali documenti dell’istruttoria:", fontCorpo));

			msgFormat = new MessageFormat("istanza di accreditamento provvisorio validata il {0}"); 
            Object[] valuesDataValid = {diniegoInfo.getAccreditamentoDataValidazione().format(dateTimeFormatter)};
			cell.addElement(new Phrase(msgFormat.format(valuesDataValid), fontCorpo));
			msgFormat = new MessageFormat("parere della Commissione Regionale ECM in data {0} (verbale n.{1}/{2})."); 
            Object[] valuesDataSed = {diniegoInfo.getRigettoInfo().getDataSedutaCommissione().format(dateTimeFormatter), diniegoInfo.getRigettoInfo().getVerbaleNumero(), diniegoInfo.getRigettoInfo().getNumeroProtocollo()};
			cell.addElement(new Phrase(msgFormat.format(valuesDataSed), fontCorpo));

			tableNoteTrasp.addCell(cell);
			
			document.add(tableNoteTrasp);
            
			//IL DIRETTORE 
			//DELLA SEZIONE CONTROLLI GOVERNO E PERSONALE SSR
			par = new Paragraph("IL DIRETTORE");
	        par.setAlignment(Element.ALIGN_CENTER);
	        par.setFont(fontCorpo);
	        par.setSpacingBefore(0);
	        par.setSpacingAfter(0);
	        document.add(par);
			par = new Paragraph("DELLA SEZIONE CONTROLLI GOVERNO E PERSONALE SSR");
	        par.setAlignment(Element.ALIGN_CENTER);
	        par.setFont(fontCorpo);
	        par.setSpacingBefore(0);
	        par.setSpacingAfter(15);
	        document.add(par);
			
	        addCorpoParagraph(document, false, true, "VISTO il decreto legislativo 30 Dicembre 1992, n. 502, recante il “Riordino della disciplina in materia sanitaria, a norma dell’art.1 della legge 23 ottobre 1992,  n. 421”, e ss.mm.ii. e in particolare l’art. 16-ter che istituisce la Commissione Nazionale per la Formazione Continua in Medicina successivamente modificata nella sua composizione dall’art. 2, comma 357, della legge del 24 Dicembre 2007, n. 244;");
	        addCorpoParagraph(document, false, true, "VISTO l’art. 92, comma 5, della legge 23 Dicembre 2000, n. 388 recante disposizioni in materia di accreditamento per lo svolgimento di attività formative dei soggetti pubblici e privati e delle società scientifiche;");
	        addCorpoParagraph(document, false, true, "VISTA la deliberazione della Giunta regionale n. 2220 del 21 settembre 2010 recante il “Recepimento degli Accordi del 1° agosto 2007 e del 5 novembre 2009, adottati in sede di Conferenza Permanente per i Rapporti tra lo Stato, le Regioni e le Province Autonome di Trento e di Bolzano, in materia di Educazione Continua in Medicina (ECM). Piano regionale della formazione - anno 2010”;");			
	        addCorpoParagraph(document, false, true, "VISTA la deliberazione della Giunta regionale n. 2215 del 20 dicembre 2011 recante il “Programma regionale d’Educazione Continua in Medicina (ECM) anno 2011. Approvazione dei requisiti e delle procedure di accreditamento dei Provider regionali. Piano regionale della formazione. Impegno di spesa” con la quale è stato avviato il sistema di accreditamento dei Provider regionali e approvato il documento (Allegato “A”) che stabilisce le regole di funzionamento del sistema veneto denominato “Disciplinare e requisiti per l’accreditamento dei Provider ECM nella Regione del Veneto”;");			
	        addCorpoParagraph(document, false, true, "VISTA la deliberazione della Giunta regionale n. 1969 del 2 ottobre 2012 con la quale si è proceduto a recepire l’Accordo Stato-Regioni del 19 aprile 2012 (Rep. Atti n. 101/CSR) sul documento recante “Il nuovo sistema di formazione continua in medicina – Linee guida per i Manuali di accreditamento dei provider, albo nazionale dei provider, crediti formativi triennio 2011/2013, federazioni, ordini, collegi e associazioni professionali, sistema di verifiche, controlli e monitoraggio della qualità, liberi professionisti”;");			
	        addCorpoParagraph(document, false, true, "VISTA la deliberazione della Giunta regionale n. 1049 del 28 giugno 2013 recante “Aggiornamento della ricognizione dei procedimenti amministrativi regionali, con individuazione del relativo termine di conclusione”, in particolare l’allegato A all’interno del quale viene indicato il termine ultimo per la conclusione del procedimento di accreditamento dei provider regionali;");			
	        addCorpoParagraph(document, false, true, "VISTA la deliberazione della Giunta regionale n. 1236 del 16 luglio 2013 recante “Approvazione dello schema di Convenzione tra l'Agenzia Nazionale per i Servizi Sanitari Regionali (Age.Na.S) e la Regione del Veneto finalizzato alla gestione del sistema di formazione continua” ed in particolare al punto 3) del deliberato dove si approva l’ammontare del contributo alle spese a carico dei soggetti che si accreditano presso il sistema di formazione continua della Regione del Veneto;");			
	        addCorpoParagraph(document, false, true, "VISTA la Convenzione stipulata il 29 luglio 2013 tra Regione del Veneto e Age.Na.S. sulla base di quanto approvato con la suddetta deliberazione n. 1236 del 16 luglio 2013;");			
	        addCorpoParagraph(document, false, true, "VISTA la deliberazione della Giunta regionale n. 1756 del 3 ottobre 2013 in merito al rinnovo dei componenti  della Commissione Regionale ECM;");			
	        
			msgFormat = new MessageFormat("VISTA l’istanza del Provider {0} validata in data {1} nella piattaforma Age.Na.S.- Regione del Veneto;"); 
            Object[] valuesNomeProvDataValid = {diniegoInfo.getProviderInfo().getProviderDenominazione(), diniegoInfo.getAccreditamentoDataValidazione().format(dateTimeFormatter)};	        
            addCorpoParagraph(document, false, true, msgFormat.format(valuesNomeProvDataValid));
            
            addCorpoParagraph(document, false, true, "ATTESO CHE la valutazione della suddetta istanza si è svolta secondo le modalità definite con nota prot. n. 382498 del 13 settembre 2013, con la quale questa Amministrazione ha delegato il Direttore pro-tempore della Sezione “Piani di rientro e Educazione continua in medicina - ECM” di Age.Na.S., all’adempimento delle procedure formali della fase istruttoria dell’accreditamento dei provider e della sottoscrizione della richiesta di eventuali documenti integrativi, ivi compresa la sottoscrizione della comunicazione di preavviso di rigetto ai sensi dell’art.10bis della L.241/1990 e ss.mm.ii.;");			
	        
            //Integrazione
			msgFormat = new MessageFormat("VISTA la nota prot. n. {0}. del {1}, notificata all’aspirante Provider {2} con la richiesta di integrazione documentale ai sensi della L.241/1990 e ss.mm.ii, a seguito delle decisioni assunte dalla Commissione Regionale ECM di cui al verbale n. {3} del {4};"); 
            Object[] valuesIntegrazione = {diniegoInfo.getIntegrazioneInfo().getNumeroProtocollo(), (diniegoInfo.getIntegrazioneInfo().getDataProtocollo() == null) ? "" : diniegoInfo.getIntegrazioneInfo().getDataProtocollo().format(dateTimeFormatter), diniegoInfo.getProviderInfo().getProviderDenominazione(), diniegoInfo.getIntegrazioneInfo().getVerbaleNumero(), diniegoInfo.getIntegrazioneInfo().getDataSedutaCommissione().format(dateTimeFormatter)};	        
	        addCorpoParagraph(document, false, true, msgFormat.format(valuesIntegrazione));

            //Rigetto
			msgFormat = new MessageFormat("VISTA la nota prot. n. {0}. del {1}, notificata all’aspirante Provider {2} sui rilevati motivi ostativi all’accoglimento della richiesta di accreditamento che anticipa il rigetto dell’istanza ai sensi dell’art.10 bis della L.241/90 e ss.mm.ii, a seguito a seguito delle decisioni assunte dalla Commissione Regionale ECM di cui al verbale n. {3} del {4};"); 
            Object[] valuesRigetto = {diniegoInfo.getRigettoInfo().getNumeroProtocollo(), diniegoInfo.getRigettoInfo().getDataProtocollo() == null ? "" :  diniegoInfo.getRigettoInfo().getDataProtocollo().format(dateTimeFormatter), diniegoInfo.getProviderInfo().getProviderDenominazione(), diniegoInfo.getRigettoInfo().getVerbaleNumero(), diniegoInfo.getRigettoInfo().getDataSedutaCommissione().format(dateTimeFormatter)};	        
	        addCorpoParagraph(document, false, true, msgFormat.format(valuesRigetto));

	        addCorpoParagraph(document, false, true, "RITENUTO di approvare e fare proprie le risultanze dell’istruttoria condotta dalla struttura amministrativa competente ed il contenuto di cui alle citate comunicazioni trasmesse dal Direttore pro-tempore della Sezione “Piani di rientro e Educazione continua in medicina - ECM” di Age.Na.S.;");
	        
	        if(diniegoInfo.getRigettoInfo().isEseguitaDaProvider()) {
				msgFormat = new MessageFormat("VISTA la documentazione inviata da parte dell’aspirante Provider {0} per il tramite del legale rappresentante pro-tempore contenente osservazioni e documentazioni in ossequio al richiamato art.10 bis della L.241/1990 e ss.mm.ii.;"); 
		        addCorpoParagraph(document, false, true, msgFormat.format(valuesProvDenom));	        	
	        } else {
				msgFormat = new MessageFormat("PRESO ATTO che alla data della seduta della Commissione Regionale ECM del {0} sono decorsi inutilmente i termini indicati nella nota prot. n. {1}. del {2} (10bis) senza che nessuna osservazione o documentazione sia pervenuta per essere sottoposta alla valutazione della Commissione Regionale ECM;"); 
	            Object[] valuesAttoRigetto = {diniegoInfo.getRigettoInfo().getDataSedutaCommissione().format(dateTimeFormatter), diniegoInfo.getRigettoInfo().getNumeroProtocollo(), diniegoInfo.getRigettoInfo().getDataProtocollo() == null ? "" :  diniegoInfo.getRigettoInfo().getDataProtocollo().format(dateTimeFormatter)};	        
		        addCorpoParagraph(document, false, true, msgFormat.format(valuesAttoRigetto));	        	
	        }
	        
	        addCorpoParagraph(document, false, true, "VERIFICATA la non sussistenza dei requisiti minimi e standard  previsti dall’allegato “1” dell'Accordo Stato-Regioni del 19 aprile 2012 recante le “Linee guida per i Manuali di accreditamento dei provider nazionali e regionali/province autonome: requisiti minimi e standard” recepito dalla deliberazione della Giunta regionale n. 1969/2012;");
	        
			msgFormat = new MessageFormat("RITENUTO di approvare la decisione assunta dalla Commissione Regionale ECM di cui al verbale n. {0} del {1} con la quale esprime parere motivato al rigetto della richiesta di accreditamento provvisorio come Provider regionale ECM del {2} in nome del legale rappresentante pro-tempore;"); 
            Object[] valuesRigetto2 = {diniegoInfo.getRigettoInfo().getVerbaleNumero(), diniegoInfo.getRigettoInfo().getDataSedutaCommissione().format(dateTimeFormatter), diniegoInfo.getProviderInfo().getProviderDenominazione()};	        	        
            addCorpoParagraph(document, false, true, msgFormat.format(valuesRigetto2));
            
            addCorpoParagraph(document, false, true, "Tutto ciò premesso,");
	        
			par = new Paragraph("DECRETA");
	        par.setAlignment(Element.ALIGN_CENTER);
	        par.setFont(fontCorpo);
	        par.setSpacingBefore(0);
	        par.setSpacingAfter(20);
	        document.add(par);

	        List list;
            list = new List(List.ORDERED);
            //Elimina l'indentazione del testo rispetto al simbolo, occorre vedere come indentare il testo
            //list.setAutoindent(false);
            list.setIndentationLeft(indentationLeftList);
            list.add(getListItem("di ritenere le premesse parte integrale ed essenziale del presente atto;", fontListItem));
            
			msgFormat = new MessageFormat("di approvare la decisione assunta dalla Commissione Regionale ECM di cui al verbale n. {0} del {1};"); 
            Object[] valuesRigetto3 = {diniegoInfo.getRigettoInfo().getVerbaleNumero(), diniegoInfo.getRigettoInfo().getDataSedutaCommissione().format(dateTimeFormatter)};	        	        
            list.add(getListItem(msgFormat.format(valuesRigetto3), fontListItem));

			msgFormat = new MessageFormat("di rigettare l’istanza di accreditamento provvisorio come Provider regionale ECM del NOME PROVIDER in nome del legale rappresentante pro-tempore validata in data DATA VALIDAZIONE per le seguenti motivazioni:"); 
            Object[] valuesRigetto4 = {diniegoInfo.getProviderInfo().getProviderDenominazione(), diniegoInfo.getAccreditamentoDataValidazione().format(dateTimeFormatter)};
            ListItem listItem = getListItem(msgFormat.format(valuesRigetto4), fontListItem);
            
            par = new Paragraph(diniegoInfo.getNoteSedutaDomanda(), fontListItem);
	        par.setAlignment(Element.ALIGN_JUSTIFIED);
	        par.setSpacingBefore(0);
	        par.setSpacingAfter(0);
	        par.setIndentationLeft(30);
	        listItem.add(par);
            /*
            if(diniegoInfo.getListaMotivazioni() != null && diniegoInfo.getListaMotivazioni().size() > 0) {
	            List subList = new List(List.ORDERED, List.ALPHABETICAL);
	            subList.setLowercase(true);
	            subList.setIndentationLeft(indentationLeftList);
	            for(String motivazione : diniegoInfo.getListaMotivazioni()) 
	            	subList.add(getListItem(motivazione, fontListItem));
	            listItem.add(subList);
            }
            */
            list.add(listItem);

            list.add(getListItem("di dare atto che avverso il presente provvedimento è ammesso il ricorso giurisdizionale al Tribunale Amministrativo Regionale o alternativamente ricorso straordinario al Capo dello Stato rispettivamente entro i termini previsti dalla legge;", fontListItem));
            list.add(getListItem("di dare atto che il presente provvedimento è soggetto a pubblicazione ai sensi dell’art. 23 del decreto legislativo 14 marzo 2013, n. 33;", fontListItem));
            list.add(getListItem("di pubblicare il presente decreto nel Bollettino Ufficiale della Regione del Veneto.", fontListItem));
            //list.add(getListItem("", fontListItem));

            document.add(list);
	        
			par = new Paragraph("dott. Claudio Costa");
	        par.setAlignment(Element.ALIGN_LEFT);
	        par.setFont(fontCorpo);
	        par.setIndentationLeft(320F);
	        par.setSpacingBefore(100);
	        par.setSpacingAfter(0);
	        document.add(par);
            
            document.close();
            outputStream.close();
            
        } catch (Exception e) {
        	LOGGER.error("writePdfAccreditamentoProvvisiorioDiniego impossibile creare il pdf", e);
            throw e;
        }
    	
    }

	private void writePdfAccreditamentoProvvisiorioAccreditato(OutputStream outputStream, PdfAccreditamentoProvvisorioAccreditatoInfo accreditatoInfo) throws Exception {
        try {
            Object[] valuesProvDenom = {accreditatoInfo.getProviderInfo().getProviderDenominazione()};
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            
            //Inserimento immagine
            //TODO correggere pe l'applicazione Ecm
            Image img = null;
    		URL url = Thread.currentThread().getContextClassLoader().getResource("LogoRegioneVeneto.png");
    		//String pathImgFile = "C:\\__Progetti\\ECM\\Doc da produrre in pdf\\LogoRegioneVeneto.png";
    		try {
    			img = Image.getInstance(url);
    			//img = Image.getInstance(pathImgFile);
    			Float scala = 1.2F;
    			Float width = 400F/scala;
    			Float height = 85F/scala;
    			img.scaleToFit(width, height);
                img.setAlignment(Element.ALIGN_CENTER);
    		} catch(Exception e) {
    			//Non mostro l'immagine
    		}

            document.open();
            //Info documento
            document.addAuthor("Ecm");
            document.addCreationDate();
            document.addCreator("Ecm");
            document.addTitle("Richiesta integrazione");
            
            if(img != null)
            	document.add(img);

            //DECRETO N. ………………  DEL ……….…………
            addCorpoParagraph(document, false, true, "DECRETO N. ………………  DEL ……….…………");
            
            //OGGETTO: Programma regionale per l’Educazione Continua in Medicina (ECM): rigetto dell’istanza di accreditamento provvisorio come provider regionale ECM di NOME PROVIDER ai sensi delle DD.G.R. n. 1969 del 2 ottobre 2012 e n. 1236 del 16 luglio 2013.
            MessageFormat msgFormat = new MessageFormat("OGGETTO: Programma regionale per l’Educazione Continua in Medicina: riconoscimento dell’accreditamento provvisorio come provider regionale ECM di {0} ai sensi delle DD.G.R n. 1969 del 2 ottobre 2012 e n. 1236 del 16 luglio 2013.");
            addCorpoParagraph(document, false, true, msgFormat.format(valuesProvDenom));

            //Linea vuota
            /*
            Paragraph parLineaVuota = new Paragraph(); 
            parLineaVuota.setAlignment(Element.ALIGN_LEFT);
            parLineaVuota.setFont(fontCorpo);
            parLineaVuota.add(" ");
			document.add(parLineaVuota);
            */
            
            //Tabella
            //NOTE PER LA TRASPARENZA:
			PdfPTable tableNoteTrasp = new PdfPTable(1);
			tableNoteTrasp.setWidthPercentage(100);
			tableNoteTrasp.setWidths(new int[]{1});
			
			//tableNoteTrasp.setTotalWidth(new float[]{520});
			//tableNoteTrasp.setLockedWidth(true);
			
			tableNoteTrasp.setHorizontalAlignment(Element.ALIGN_CENTER);
			tableNoteTrasp.setSpacingBefore(spacingBefore);
			tableNoteTrasp.setSpacingAfter(spacingAfter);

			PdfPCell cell = new PdfPCell(new Phrase("NOTE PER LA TRASPARENZA:", fontCorpo));
			cell.setPadding(cellPadding);
			//cell.setBorder(PdfPCell.NO_BORDER);
			tableNoteTrasp.addCell(cell);
            
			cell = new PdfPCell();
			cell.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
			//cell.setSpaceCharRatio(0);
			cell.setPadding(cellPadding);
			//cell.setPaddingBottom(20);
			msgFormat = new MessageFormat("La fase attuativa degli Accordi Stato-Regioni vigenti in materia di ECM prevede il passaggio da un sistema di accreditamento degli eventi ad un sistema di accreditamento dei Provider, intesi come soggetti attivi e qualificati nel campo della formazione continua in sanità abilitati a realizzare attività formative riconosciute idonee per il sistema di formazione continua (ECM), individuando ed attribuendo direttamente i crediti agli eventi formativi e quindi ai partecipanti. Con il presente provvedimento si procede all’accreditamento provvisorio come Provider regionale di {0}.");
			
			Paragraph par = new Paragraph(); 
	        par.setAlignment(Element.ALIGN_JUSTIFIED);
	        par.setFont(fontCorpo);
	        par.add(msgFormat.format(valuesProvDenom));
	        par.setSpacingBefore(0);
	        par.setSpacingAfter(spacingAfter);
			cell.addElement(par);
			
			cell.addElement(new Phrase("Estremi dei principali documenti dell’istruttoria:", fontCorpo));

			msgFormat = new MessageFormat("istanza di accreditamento provvisorio validata il {0}"); 
            Object[] valuesDataValid = {accreditatoInfo.getAccreditamentoDataValidazione().format(dateTimeFormatter)};
			cell.addElement(new Phrase(msgFormat.format(valuesDataValid), fontCorpo));
			msgFormat = new MessageFormat("parere della Commissione Regionale ECM in data {0} (verbale n.{1}/{2})."); 
            Object[] valuesDataSed = {accreditatoInfo.getAccreditamentoInfo().getDataSedutaCommissione().format(dateTimeFormatter), accreditatoInfo.getAccreditamentoInfo().getVerbaleNumero(), accreditatoInfo.getAccreditamentoInfo().getNumeroProtocollo()};
			cell.addElement(new Phrase(msgFormat.format(valuesDataSed), fontCorpo));

			tableNoteTrasp.addCell(cell);
			
			document.add(tableNoteTrasp);
            
			//IL DIRETTORE 
			//DELLA SEZIONE CONTROLLI GOVERNO E PERSONALE SSR
			par = new Paragraph("IL DIRETTORE");
	        par.setAlignment(Element.ALIGN_CENTER);
	        par.setFont(fontCorpo);
	        par.setSpacingBefore(0);
	        par.setSpacingAfter(0);
	        document.add(par);
			par = new Paragraph("DELLA SEZIONE CONTROLLI GOVERNO E PERSONALE SSR");
	        par.setAlignment(Element.ALIGN_CENTER);
	        par.setFont(fontCorpo);
	        par.setSpacingBefore(0);
	        par.setSpacingAfter(15);
	        document.add(par);
			
	        addCorpoParagraph(document, false, true, "VISTO il decreto legislativo n. 502 del 30 dicembre 1992 recante “Riordino della disciplina in materia sanitaria, a norma dell’art.1 della legge 23 ottobre 1992,  n. 421” e ss.mm.ii., e in particolare l’art. 16-quarter dove si rileva la  necessità per gli operatori sanitari di partecipare alle attività di formazione continua, considerato requisito indispensabile per svolgere la propria attività professionale;");
	        addCorpoParagraph(document, false, true, "VISTO l’art. 92, comma 5, della legge n. 388 del 23 dicembre 2000 recante disposizioni in materia di accreditamento per lo svolgimento di attività formative dei soggetti pubblici e privati e delle società scientifiche;");
	        addCorpoParagraph(document, false, true, "VISTA la deliberazione della Giunta regionale n. 2220 del 21 settembre 2010 recante “Recepimento degli Accordi del 1° agosto 2007 e del 5 novembre 2009, adottati in sede di Conferenza Permanente per i Rapporti tra lo Stato, le Regioni e le Province Autonome di Trento e di Bolzano, in materia di Educazione Continua in Medicina (ECM). Piano regionale della formazione - anno 2010”;");
	        addCorpoParagraph(document, false, true, "VISTA la deliberazione della Giunta regionale n. 2215 del 20 dicembre 2011 recante “Programma regionale d’Educazione Continua in Medicina (ECM) anno 2011. Approvazione dei requisiti e delle procedure di accreditamento dei Provider regionali. Piano regionale della formazione. Impegno di spesa” con la quale è stato avviato il sistema di accreditamento dei Provider regionali e approvato il documento (Allegato “A”) che stabilisce le regole di funzionamento del sistema veneto denominato “Disciplinare e requisiti per l’accreditamento dei Provider ECM nella Regione del Veneto”;");
	        addCorpoParagraph(document, false, true, "VISTA la deliberazione della Giunta regionale n. 1969 del 2 ottobre 2012 recante “Il nuovo sistema di formazione continua in medicina – Linee guida per i Manuali di accreditamento dei provider, albo nazionale dei provider, crediti formativi triennio 2011/2013, federazioni, ordini, collegi e associazioni professionali, sistema di verifiche, controlli e monitoraggio della qualità, liberi professionisti”, con la quale si è proceduto a recepire l’Accordo Stato-Regioni del 19 aprile 2012 (Rep. Atti n. 101/CSR) e in particolare l’Allegato “1” “Linee Guida per i Manuali di accreditamento dei provider nazionali e regionali/province autonome: requisiti minimi e standard” e l’Allegato “2” “Determina della CNFC in materia di violazioni” dell’08 ottobre 2010;");
	        addCorpoParagraph(document, false, true, "VISTO il Decreto Ministeriale del 26 marzo 2013 sul “contributo alle spese dovuto ai soggetti pubblici e privati e dalle società scientifiche che chiedono il loro accreditamento per lo svolgimento di attività di formazione continua ovvero l’accreditamento di specifiche attività formative promosse o organizzate dagli stessi ai fini dell’attribuzione dei crediti formativi”;");
	        addCorpoParagraph(document, false, true, "VISTA la deliberazione della Giunta regionale n. 1236 del 16 luglio 2013 recante “Approvazione dello schema di Convenzione tra l'Agenzia Nazionale per i Servizi Sanitari Regionali  (Age.Na.S) e la Regione Veneto finalizzato alla gestione del sistema di formazione continua” ed in particolare al punto 3) del deliberato dove si approva l’ammontare del contributo alle spese a carico dei soggetti che si accreditano presso il sistema di formazione continua della Regione del Veneto;");
	        addCorpoParagraph(document, false, true, "VISTA la Convenzione stipulata il 29 luglio 2013 (rep. n. 29005) tra la Regione del Veneto e Age.Na.S. ai sensi della deliberazione n. 1236 del 16 luglio 2013;");
	        addCorpoParagraph(document, false, true, "VISTA la deliberazione della Giunta regionale n. 1756 del 3 ottobre 2013 in merito al rinnovo dei componenti  della Commissione Regionale ECM;");
	        addCorpoParagraph(document, false, true, "VISTA la deliberazione della Giunta regionale n. 2620 del 29 dicembre 2014 recante “Aggiornamento della ricognizione dei procedimenti amministrativi di competenza della Giunta regionale, con individuazione del relativo termine di conclusione”, in particolare l’allegato A all’interno del quale viene indicato il termine ultimo per la conclusione del procedimento di accreditamento dei Provider regionali, quantificato in 180 giorni;");
	        
			msgFormat = new MessageFormat("VISTA l’istanza del Provider {0} validata in data {1} nella piattaforma Age.Na.S. -Regione del Veneto ai fini del procedimento di accreditamento provvisorio;"); 
            Object[] valuesNomeProvDataValid = {accreditatoInfo.getProviderInfo().getProviderDenominazione(), accreditatoInfo.getAccreditamentoDataValidazione().format(dateTimeFormatter)};	        
            addCorpoParagraph(document, false, true, msgFormat.format(valuesNomeProvDataValid));
            
            addCorpoParagraph(document, false, true, "ATTESO che la valutazione della suddetta istanza si è svolta secondo le modalità definite con nota prot. n. 382498 del 13 settembre 2013, con la quale codesta Amministrazione ha delegato il Direttore pro-tempore della Sezione “Piani di rientro e Educazione continua in medicina - ECM” di Age.Na.S. all’adempimento delle procedure formali della fase istruttoria dell’accreditamento provvisorio dei Provider e alla sottoscrizione della richiesta di documentazione integrativa ai sensi della L.241/1990 e ss.mm.ii;");			
	        
            //Integrazione
            if(accreditatoInfo.getIntegrazioneInfo() != null) {
				msgFormat = new MessageFormat("VISTA la nota prot. n. {0}. del {1} notificata al Provider {2} con la richiesta di integrazione documentale ai sensi della L.241/1990 e ss.mm.ii. a seguito delle decisioni assunte dalla Commissione Regionale ECM di cui al verbale n. {3} del {4};"); 
				Object[] valuesIntegrazione = {accreditatoInfo.getIntegrazioneInfo().getNumeroProtocollo(), accreditatoInfo.getIntegrazioneInfo().getDataProtocollo() == null ? "" :  accreditatoInfo.getIntegrazioneInfo().getDataProtocollo().format(dateTimeFormatter), accreditatoInfo.getProviderInfo().getProviderDenominazione(), accreditatoInfo.getIntegrazioneInfo().getVerbaleNumero(), accreditatoInfo.getIntegrazioneInfo().getDataSedutaCommissione().format(dateTimeFormatter)};	        
		        addCorpoParagraph(document, false, true, msgFormat.format(valuesIntegrazione));
            }

            //Rigetto
            if(accreditatoInfo.getRigettoInfo() != null) {
				msgFormat = new MessageFormat("VISTA la nota prot. n. {0}. del {1}. notificata al {2} sui rilevati motivi ostativi all’accoglimento della richiesta di accreditamento standard che anticipa il rigetto dell’istanza ai sensi dell’art.10 bis della L.241/90 e ss.mm.ii. a seguito delle decisioni assunte dalla Commissione Regionale ECM di cui al verbale n. {3} del {4};"); 
	            Object[] valuesRigetto = {accreditatoInfo.getRigettoInfo().getNumeroProtocollo(), accreditatoInfo.getRigettoInfo().getDataProtocollo() == null ? "" : accreditatoInfo.getRigettoInfo().getDataProtocollo().format(dateTimeFormatter), accreditatoInfo.getProviderInfo().getProviderDenominazione(), accreditatoInfo.getRigettoInfo().getVerbaleNumero(), accreditatoInfo.getRigettoInfo().getDataSedutaCommissione().format(dateTimeFormatter)};	        
		        addCorpoParagraph(document, false, true, msgFormat.format(valuesRigetto));
            }

			msgFormat = new MessageFormat("VISTA la documentazione prodotta da parte del Provider {0} per il tramite del legale rappresentante pro-tempore in ossequio al richiamato art.10 bis della L.241/1990 e ss.mm.ii.;"); 
	        addCorpoParagraph(document, false, true, msgFormat.format(valuesProvDenom));	        	
            
	        addCorpoParagraph(document, false, true, "RITENUTO di approvare e fare proprie le risultanze dell’istruttoria condotta dalla struttura amministrativa competente ed il contenuto di cui alla/e citata/e comunicazione/i trasmessa/e dal Direttore pro-tempore della Sezione “Piani di rientro e Educazione continua in medicina - ECM” di Age.Na.S.;");
	        addCorpoParagraph(document, false, true, "VERIFICATA la sussistenza dei requisiti minimi e standard  previsti dall’allegato “1” dell'Accordo Stato-Regioni del 19 aprile 2012 recante le “Linee guida per i Manuali di accreditamento dei provider nazionali e regionali/province autonome: requisiti minimi e standard” recepito dalla deliberazione della Giunta regionale n. 1969/2012;");

			msgFormat = new MessageFormat("TENUTO CONTO della decisione assunta dalla Commissione Regionale ECM nella seduta del {0}, il cui verbale è agli atti della scrivente Sezione, con la quale esprime il proprio parere positivo all’accoglimento della richiesta di accreditamento come Provider regionale ECM del {1} in nome del legale rappresentante pro-tempore;"); 
			Object[] valuesDataCommissioneAccrDenom = {accreditatoInfo.getDataCommissioneAccreditamento().format(dateTimeFormatter), accreditatoInfo.getProviderInfo().getProviderDenominazione()};	        
			addCorpoParagraph(document, false, true, msgFormat.format(valuesDataCommissioneAccrDenom));	        	
	        
            addCorpoParagraph(document, false, true, "Tutto ciò premesso,");
	        
			par = new Paragraph("DECRETA");
	        par.setAlignment(Element.ALIGN_CENTER);
	        par.setFont(fontCorpo);
	        par.setSpacingBefore(0);
	        par.setSpacingAfter(20);
	        document.add(par);

	        List list;
            list = new List(List.ORDERED);
            //Elimina l'indentazione del testo rispetto al simbolo, occorre vedere come indentare il testo
            //list.setAutoindent(false);
            list.setIndentationLeft(indentationLeftList);
            //1
            list.add(getListItem("di ritenere le premesse parte integrale ed essenziale del presente atto;", fontListItem));
            
            //2
			msgFormat = new MessageFormat("di dare seguito a quanto deciso dalla Commissione Regionale ECM nella seduta del {0} in merito all’accoglimento della richiesta di accreditamento come Provider Provvisorio della {1} in nome del legale rappresentante pro-tempore;"); 
            list.add(getListItem(msgFormat.format(valuesDataCommissioneAccrDenom), fontListItem));

            //3
			msgFormat = new MessageFormat("di riconoscere l’accreditamento provvisorio in qualità di Provider regionale ECM al {0} con numero identificativo {1}, per un periodo di 24 mesi con decorrenza dal {2}, sulla base della rispondenza ai requisiti minimi e standard previsti dall’allegato “1” dell'Accordo Stato-Regioni del 19 aprile 2012 recepito dalla deliberazione della Giunta regionale n. 1969/2012;"); 
            Object[] values3 = {accreditatoInfo.getProviderInfo().getProviderDenominazione(), accreditatoInfo.getProviderInfo().getProviderId(), accreditatoInfo.getDataCommissioneAccreditamento().format(dateTimeFormatter)};
            list.add(getListItem(msgFormat.format(values3), fontListItem));
            
            //4
			msgFormat = new MessageFormat("di stabilire che entro 90 giorni dalla data di comunicazione all’interessato del presente provvedimento, il {0} è tenuto al versamento del contributo annuo alle spese in qualità di Provider provvisoriamente accreditato ai sensi della deliberazione della Giunta regionale n. 1236/2013 e che per l’anno solare successivo il versamento dovrà essere corrisposto entro il 31 marzo;"); 
            list.add(getListItem(msgFormat.format(valuesProvDenom), fontListItem));

            //5
            list.add(getListItem("di stabilire che per lo svolgimento di specifiche attività formative il Provider è tenuto a versare un contributo alle spese così come stabilito dalla deliberazione della Giunta regionale n. 1236/2013 entro 90 giorni dalla data di conclusione dell’evento formativo;", fontListItem));
            //6
            list.add(getListItem("di stabilire altresì che il venir meno dei requisiti minimi e standard previsti nell’allegato “1” di cui al predetto punto 3) e il mancato pagamento del contributo annuo e del contributo alle spese per lo svolgimento di specifiche attività formative, fissato dalla deliberazione della Giunta regionale n. 1236/2013, nonché il mancato adempimento di quanto previsto dall’allegato “1” e dall’allegato “2” “Determina della CNFC del 08 ottobre 2010” dell’Accordo Stato-Regioni del 19 aprile 2012 danno luogo, previo contraddittorio, alle conseguenze stabilite dal citato allegato “2” assunte con provvedimento regionale;", fontListItem));
            //7
            list.add(getListItem("che i requisiti minimi, gli adempimenti richiesti e le eventuali sanzioni potranno essere modificati, con preavviso di giorni 30, in conseguenza di deliberazioni regionali che dovessero sopravvenire al presente provvedimento;", fontListItem));
            //8
            list.add(getListItem("di dare atto che il presente provvedimento è soggetto a pubblicazione ai sensi dell’art. 23 del decreto legislativo 14 marzo 2013, n. 33;", fontListItem));
            //9
            list.add(getListItem("di pubblicare il presente decreto nel Bollettino Ufficiale della Regione del Veneto.", fontListItem));

            document.add(list);
	        
			par = new Paragraph("dott. Claudio Costa");
	        par.setAlignment(Element.ALIGN_LEFT);
	        par.setFont(fontCorpo);
	        par.setIndentationLeft(320F);
	        par.setSpacingBefore(50);
	        par.setSpacingAfter(0);
	        document.add(par);
            
            document.close();
            outputStream.close();
            
        } catch (Exception e) {
        	LOGGER.error("writePdfAccreditamentoProvvisiorioAccreditato impossibile creare il pdf", e);
            throw e;
        }
    	
    }
	
	private void addCorpoParagraph(Document document, boolean addSpacingBefore, boolean addSpacingAfter, String testo) throws DocumentException  {
		Paragraph par = new Paragraph(); 
        par.setAlignment(Element.ALIGN_JUSTIFIED);
        par.setFont(fontCorpo);
        par.add(testo);
        if(addSpacingBefore)
        	par.setSpacingBefore(spacingBefore);
        if(addSpacingAfter)
        	par.setSpacingAfter(spacingAfter);
        
        //Indentazione
        //par.setIndentationLeft (18);
        //par.setFirstLineIndent(-18);
        
        //Interlinea OK
        //parOggetto.setMultipliedLeading(5);
        //Interlinea da provare
        //parOggetto.setLeading(0F, 2F);
        document.add(par);
	}
	
    private static ListItem getListItem(String testo, Font fontListItem) {
        Paragraph parList = new Paragraph();
        parList.setFont(fontListItem);
        parList.add(testo);
        
        //Da testare se richiesta indentazione diversa dal default
        //parList.setIndentationLeft(40);
        //parList.setFirstLineIndent(-18);
        
        parList.setAlignment(Element.ALIGN_JUSTIFIED);
        //parList.setSpacingAfter(spacingAfter);
        
        ListItem listItem = new ListItem(parList);
        //non serve a nulla
        //listItem.setIndentationLeft(40);
        return listItem;
        //return new ListItem(parList);
        
    }

}