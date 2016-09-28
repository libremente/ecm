
package it.veneto.regione.pagamenti.ente.pagamentitelematicidovutipagati;

import java.util.Calendar;
import java.util.List;
import javax.activation.DataHandler;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.Holder;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import it.veneto.regione.pagamenti.ente.FaultBean;
import it.veneto.regione.pagamenti.ente.PaaSILAutorizzaImportFlusso;
import it.veneto.regione.pagamenti.ente.PaaSILAutorizzaImportFlussoRisposta;
import it.veneto.regione.pagamenti.ente.PaaSILAvvisoPendente;
import it.veneto.regione.pagamenti.ente.PaaSILChiediStatoExportFlusso;
import it.veneto.regione.pagamenti.ente.PaaSILChiediStatoExportFlussoRisposta;
import it.veneto.regione.pagamenti.ente.PaaSILChiediStatoImportFlusso;
import it.veneto.regione.pagamenti.ente.PaaSILChiediStatoImportFlussoRisposta;
import it.veneto.regione.pagamenti.ente.PaaSILImportaDovuto;
import it.veneto.regione.pagamenti.ente.PaaSILImportaDovutoRisposta;
import it.veneto.regione.pagamenti.ente.PaaSILInviaDovuti;
import it.veneto.regione.pagamenti.ente.PaaSILInviaDovutiRisposta;
import it.veneto.regione.pagamenti.ente.PaaSILPrenotaExportFlusso;
import it.veneto.regione.pagamenti.ente.PaaSILPrenotaExportFlussoIncrementaleConRicevuta;
import it.veneto.regione.pagamenti.ente.PaaSILPrenotaExportFlussoIncrementaleConRicevutaRisposta;
import it.veneto.regione.pagamenti.ente.PaaSILPrenotaExportFlussoRisposta;
import it.veneto.regione.pagamenti.ente.PaaSILVerificaAvviso;
import it.veneto.regione.pagamenti.ente.PaaSILVerificaAvvisoRisposta;
import it.veneto.regione.pagamenti.ente.ppthead.IntestazionePPT;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.6 in JDK 6
 * Generated source version: 2.1
 * 
 */
@WebService(name = "PagamentiTelematiciDovutiPagati", targetNamespace = "http://www.regione.veneto.it/pagamenti/ente/PagamentiTelematiciDovutiPagati")
@XmlSeeAlso({
    it.veneto.regione.pagamenti.ente.ppthead.ObjectFactory.class,
    it.veneto.regione.pagamenti.ente.ObjectFactory.class,
    it.veneto.regione.schemas._2012.pagamenti.ente.ObjectFactory.class
})
public interface PagamentiTelematiciDovutiPagati {


    /**
     * 
     * @param bodyrichiesta
     * @param header
     * @return
     *     returns it.veneto.regione.pagamenti.ente.PaaSILInviaDovutiRisposta
     */
    @WebMethod(action = "paaSILInviaDovuti")
    @WebResult(name = "paaSILInviaDovutiRisposta", targetNamespace = "http://www.regione.veneto.it/pagamenti/ente/", partName = "bodyrisposta")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public PaaSILInviaDovutiRisposta paaSILInviaDovuti(
        @WebParam(name = "paaSILInviaDovuti", targetNamespace = "http://www.regione.veneto.it/pagamenti/ente/", partName = "bodyrichiesta")
        PaaSILInviaDovuti bodyrichiesta,
        @WebParam(name = "intestazionePPT", targetNamespace = "http://www.regione.veneto.it/pagamenti/ente/ppthead", header = true, partName = "header")
        IntestazionePPT header);

    /**
     * 
     * @param bodyrichiesta
     * @param header
     * @return
     *     returns it.veneto.regione.pagamenti.ente.PaaSILVerificaAvvisoRisposta
     */
    @WebMethod(action = "paaSILVerificaAvviso")
    @WebResult(name = "paaSILVerificaAvvisoRisposta", targetNamespace = "http://www.regione.veneto.it/pagamenti/ente/", partName = "bodyrisposta")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public PaaSILVerificaAvvisoRisposta paaSILVerificaAvviso(
        @WebParam(name = "paaSILVerificaAvviso", targetNamespace = "http://www.regione.veneto.it/pagamenti/ente/", partName = "bodyrichiesta")
        PaaSILVerificaAvviso bodyrichiesta,
        @WebParam(name = "intestazionePPT", targetNamespace = "http://www.regione.veneto.it/pagamenti/ente/ppthead", header = true, partName = "header")
        IntestazionePPT header);

    /**
     * 
     * @param codIpaEnte
     * @param idSession
     * @param pagati
     * @param fault
     * @param password
     */
    @WebMethod(action = "paaSILChiediPagati")
    @RequestWrapper(localName = "paaSILChiediPagati", targetNamespace = "http://www.regione.veneto.it/pagamenti/ente/", className = "it.veneto.regione.pagamenti.ente.PaaSILChiediPagati")
    @ResponseWrapper(localName = "paaSILChiediPagatiRisposta", targetNamespace = "http://www.regione.veneto.it/pagamenti/ente/", className = "it.veneto.regione.pagamenti.ente.PaaSILChiediPagatiRisposta")
    public void paaSILChiediPagati(
        @WebParam(name = "codIpaEnte", targetNamespace = "")
        String codIpaEnte,
        @WebParam(name = "password", targetNamespace = "")
        String password,
        @WebParam(name = "idSession", targetNamespace = "")
        String idSession,
        @WebParam(name = "fault", targetNamespace = "", mode = WebParam.Mode.OUT)
        Holder<FaultBean> fault,
        @WebParam(name = "pagati", targetNamespace = "", mode = WebParam.Mode.OUT)
        Holder<DataHandler> pagati);

    /**
     * 
     * @param codIpaEnte
     * @param tipoFirma
     * @param idSession
     * @param pagati
     * @param rt
     * @param fault
     * @param password
     */
    @WebMethod(action = "paaSILChiediPagatiConRicevuta")
    @RequestWrapper(localName = "paaSILChiediPagatiConRicevuta", targetNamespace = "http://www.regione.veneto.it/pagamenti/ente/", className = "it.veneto.regione.pagamenti.ente.PaaSILChiediPagatiConRicevuta")
    @ResponseWrapper(localName = "paaSILChiediPagatiConRicevutaRisposta", targetNamespace = "http://www.regione.veneto.it/pagamenti/ente/", className = "it.veneto.regione.pagamenti.ente.PaaSILChiediPagatiConRicevutaRisposta")
    public void paaSILChiediPagatiConRicevuta(
        @WebParam(name = "codIpaEnte", targetNamespace = "")
        String codIpaEnte,
        @WebParam(name = "password", targetNamespace = "")
        String password,
        @WebParam(name = "idSession", targetNamespace = "")
        String idSession,
        @WebParam(name = "fault", targetNamespace = "", mode = WebParam.Mode.OUT)
        Holder<FaultBean> fault,
        @WebParam(name = "pagati", targetNamespace = "", mode = WebParam.Mode.OUT)
        Holder<DataHandler> pagati,
        @WebParam(name = "tipoFirma", targetNamespace = "", mode = WebParam.Mode.OUT)
        Holder<String> tipoFirma,
        @WebParam(name = "rt", targetNamespace = "", mode = WebParam.Mode.OUT)
        Holder<DataHandler> rt);

    /**
     * 
     * @param bodyrichiesta
     * @param header
     * @return
     *     returns it.veneto.regione.pagamenti.ente.PaaSILImportaDovutoRisposta
     */
    @WebMethod(action = "paaSILImportaDovuto")
    @WebResult(name = "paaSILImportaDovutoRisposta", targetNamespace = "http://www.regione.veneto.it/pagamenti/ente/", partName = "bodyrisposta")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public PaaSILImportaDovutoRisposta paaSILImportaDovuto(
        @WebParam(name = "paaSILImportaDovuto", targetNamespace = "http://www.regione.veneto.it/pagamenti/ente/", partName = "bodyrichiesta")
        PaaSILImportaDovuto bodyrichiesta,
        @WebParam(name = "intestazionePPT", targetNamespace = "http://www.regione.veneto.it/pagamenti/ente/ppthead", header = true, partName = "header")
        IntestazionePPT header);

    /**
     * 
     * @param bodyrichiesta
     * @param header
     * @return
     *     returns it.veneto.regione.pagamenti.ente.PaaSILAutorizzaImportFlussoRisposta
     */
    @WebMethod(action = "paaSILAutorizzaImportFlusso")
    @WebResult(name = "paaSILAutorizzaImportFlussoRisposta", targetNamespace = "http://www.regione.veneto.it/pagamenti/ente/", partName = "bodyrisposta")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public PaaSILAutorizzaImportFlussoRisposta paaSILAutorizzaImportFlusso(
        @WebParam(name = "paaSILAutorizzaImportFlusso", targetNamespace = "http://www.regione.veneto.it/pagamenti/ente/", partName = "bodyrichiesta")
        PaaSILAutorizzaImportFlusso bodyrichiesta,
        @WebParam(name = "intestazionePPT", targetNamespace = "http://www.regione.veneto.it/pagamenti/ente/ppthead", header = true, partName = "header")
        IntestazionePPT header);

    /**
     * 
     * @param bodyrichiesta
     * @param header
     * @return
     *     returns it.veneto.regione.pagamenti.ente.PaaSILChiediStatoImportFlussoRisposta
     */
    @WebMethod(action = "paaSILChiediStatoImportFlusso")
    @WebResult(name = "paaSILChiediStatoImportFlussoRisposta", targetNamespace = "http://www.regione.veneto.it/pagamenti/ente/", partName = "bodyrisposta")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public PaaSILChiediStatoImportFlussoRisposta paaSILChiediStatoImportFlusso(
        @WebParam(name = "paaSILChiediStatoImportFlusso", targetNamespace = "http://www.regione.veneto.it/pagamenti/ente/", partName = "bodyrichiesta")
        PaaSILChiediStatoImportFlusso bodyrichiesta,
        @WebParam(name = "intestazionePPT", targetNamespace = "http://www.regione.veneto.it/pagamenti/ente/ppthead", header = true, partName = "header")
        IntestazionePPT header);

    /**
     * 
     * @param bodyrichiesta
     * @param header
     * @return
     *     returns it.veneto.regione.pagamenti.ente.PaaSILPrenotaExportFlussoRisposta
     */
    @WebMethod(action = "paaSILPrenotaExportFlusso")
    @WebResult(name = "paaSILPrenotaExportFlussoRisposta", targetNamespace = "http://www.regione.veneto.it/pagamenti/ente/", partName = "bodyrisposta")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public PaaSILPrenotaExportFlussoRisposta paaSILPrenotaExportFlusso(
        @WebParam(name = "paaSILPrenotaExportFlusso", targetNamespace = "http://www.regione.veneto.it/pagamenti/ente/", partName = "bodyrichiesta")
        PaaSILPrenotaExportFlusso bodyrichiesta,
        @WebParam(name = "intestazionePPT", targetNamespace = "http://www.regione.veneto.it/pagamenti/ente/ppthead", header = true, partName = "header")
        IntestazionePPT header);

    /**
     * 
     * @param bodyrichiesta
     * @param header
     * @return
     *     returns it.veneto.regione.pagamenti.ente.PaaSILPrenotaExportFlussoIncrementaleConRicevutaRisposta
     */
    @WebMethod(action = "paaSILPrenotaExportFlussoIncrementaleConRicevuta")
    @WebResult(name = "paaSILPrenotaExportFlussoIncrementaleConRicevutaRisposta", targetNamespace = "http://www.regione.veneto.it/pagamenti/ente/", partName = "bodyrisposta")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public PaaSILPrenotaExportFlussoIncrementaleConRicevutaRisposta paaSILPrenotaExportFlussoIncrementaleConRicevuta(
        @WebParam(name = "paaSILPrenotaExportFlussoIncrementaleConRicevuta", targetNamespace = "http://www.regione.veneto.it/pagamenti/ente/", partName = "bodyrichiesta")
        PaaSILPrenotaExportFlussoIncrementaleConRicevuta bodyrichiesta,
        @WebParam(name = "intestazionePPT", targetNamespace = "http://www.regione.veneto.it/pagamenti/ente/ppthead", header = true, partName = "header")
        IntestazionePPT header);

    /**
     * 
     * @param bodyrichiesta
     * @param header
     * @return
     *     returns it.veneto.regione.pagamenti.ente.PaaSILChiediStatoExportFlussoRisposta
     */
    @WebMethod(action = "paaSILChiediStatoExportFlusso")
    @WebResult(name = "paaSILChiediStatoExportFlussoRisposta", targetNamespace = "http://www.regione.veneto.it/pagamenti/ente/", partName = "bodyrisposta")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public PaaSILChiediStatoExportFlussoRisposta paaSILChiediStatoExportFlusso(
        @WebParam(name = "paaSILChiediStatoExportFlusso", targetNamespace = "http://www.regione.veneto.it/pagamenti/ente/", partName = "bodyrichiesta")
        PaaSILChiediStatoExportFlusso bodyrichiesta,
        @WebParam(name = "intestazionePPT", targetNamespace = "http://www.regione.veneto.it/pagamenti/ente/ppthead", header = true, partName = "header")
        IntestazionePPT header);

    /**
     * 
     * @param codIpaEnte
     * @param dateTo
     * @param paaSILAvvisoPendente
     * @param fault
     * @param dateFrom
     * @param password
     */
    @WebMethod(action = "paaSILChiediAvvisiPendenti")
    @RequestWrapper(localName = "paaSILChiediAvvisiPendenti", targetNamespace = "http://www.regione.veneto.it/pagamenti/ente/", className = "it.veneto.regione.pagamenti.ente.PaaSILChiediAvvisiPendenti")
    @ResponseWrapper(localName = "paaSILChiediAvvisiPendentiRisposta", targetNamespace = "http://www.regione.veneto.it/pagamenti/ente/", className = "it.veneto.regione.pagamenti.ente.PaaSILChiediAvvisiPendentiRisposta")
    public void paaSILChiediAvvisiPendenti(
        @WebParam(name = "password", targetNamespace = "")
        String password,
        @WebParam(name = "codIpaEnte", targetNamespace = "")
        String codIpaEnte,
        @WebParam(name = "dateFrom", targetNamespace = "")
        Calendar dateFrom,
        @WebParam(name = "dateTo", targetNamespace = "")
        Calendar dateTo,
        @WebParam(name = "fault", targetNamespace = "", mode = WebParam.Mode.OUT)
        Holder<FaultBean> fault,
        @WebParam(name = "paaSILAvvisoPendente", targetNamespace = "", mode = WebParam.Mode.OUT)
        Holder<List<PaaSILAvvisoPendente>> paaSILAvvisoPendente);

}
