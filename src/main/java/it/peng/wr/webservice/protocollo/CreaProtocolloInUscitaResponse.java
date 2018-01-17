
package it.peng.wr.webservice.protocollo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for creaProtocolloInUscitaResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="creaProtocolloInUscitaResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" type="{http://protocollo.webservice.wr.peng.it/}risultatoprotocollo" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "creaProtocolloInUscitaResponse", propOrder = {
    "_return"
})
public class CreaProtocolloInUscitaResponse {

    @XmlElement(name = "return")
    protected Risultatoprotocollo _return;

    /**
     * Gets the value of the return property.
     * 
     * @return
     *     possible object is
     *     {@link Risultatoprotocollo }
     *     
     */
    public Risultatoprotocollo getReturn() {
        return _return;
    }

    /**
     * Sets the value of the return property.
     * 
     * @param value
     *     allowed object is
     *     {@link Risultatoprotocollo }
     *     
     */
    public void setReturn(Risultatoprotocollo value) {
        this._return = value;
    }

}
