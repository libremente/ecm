//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.08.18 at 06:09:02 PM CEST 
//


package it.veneto.regione.firma;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}SOURCE"/>
 *         &lt;element ref="{}INFORMAZIONI"/>
 *         &lt;element ref="{}METADATA"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="bytes" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "source",
    "informazioni",
    "metadata"
})
@XmlRootElement(name = "FILE")
public class FILE {

    @XmlElement(name = "SOURCE", required = true)
    protected SOURCE source;
    @XmlElement(name = "INFORMAZIONI", required = true)
    protected String informazioni;
    @XmlElement(name = "METADATA", required = true)
    protected String metadata;
    @XmlAttribute(required = true)
    protected String name;
    @XmlAttribute(required = true)
    protected String bytes;

    /**
     * Gets the value of the source property.
     * 
     * @return
     *     possible object is
     *     {@link SOURCE }
     *     
     */
    public SOURCE getSOURCE() {
        return source;
    }

    /**
     * Sets the value of the source property.
     * 
     * @param value
     *     allowed object is
     *     {@link SOURCE }
     *     
     */
    public void setSOURCE(SOURCE value) {
        this.source = value;
    }

    /**
     * Gets the value of the informazioni property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getINFORMAZIONI() {
        return informazioni;
    }

    /**
     * Sets the value of the informazioni property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setINFORMAZIONI(String value) {
        this.informazioni = value;
    }

    /**
     * Gets the value of the metadata property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMETADATA() {
        return metadata;
    }

    /**
     * Sets the value of the metadata property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMETADATA(String value) {
        this.metadata = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the bytes property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBytes() {
        return bytes;
    }

    /**
     * Sets the value of the bytes property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBytes(String value) {
        this.bytes = value;
    }

}