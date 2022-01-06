//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2022.01.04 at 06:53:26 PM CET 
//


package com.silvionetto.budget.xjb;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Budget complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Budget"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{}BaseEntity"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="category" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="january" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
 *         &lt;element name="february" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
 *         &lt;element name="march" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
 *         &lt;element name="april" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
 *         &lt;element name="may" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
 *         &lt;element name="june" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
 *         &lt;element name="july" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
 *         &lt;element name="august" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
 *         &lt;element name="september" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
 *         &lt;element name="october" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
 *         &lt;element name="november" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
 *         &lt;element name="december" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Budget", propOrder = {
    "category",
    "january",
    "february",
    "march",
    "april",
    "may",
    "june",
    "july",
    "august",
    "september",
    "october",
    "november",
    "december"
})
public class Budget
    extends BaseEntity
{

    @XmlElement(required = true)
    protected String category;
    @XmlElement(required = true)
    protected BigDecimal january;
    @XmlElement(required = true)
    protected BigDecimal february;
    @XmlElement(required = true)
    protected BigDecimal march;
    @XmlElement(required = true)
    protected BigDecimal april;
    @XmlElement(required = true)
    protected BigDecimal may;
    @XmlElement(required = true)
    protected BigDecimal june;
    @XmlElement(required = true)
    protected BigDecimal july;
    @XmlElement(required = true)
    protected BigDecimal august;
    @XmlElement(required = true)
    protected BigDecimal september;
    @XmlElement(required = true)
    protected BigDecimal october;
    @XmlElement(required = true)
    protected BigDecimal november;
    @XmlElement(required = true)
    protected BigDecimal december;

    /**
     * Gets the value of the category property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the value of the category property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCategory(String value) {
        this.category = value;
    }

    /**
     * Gets the value of the january property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getJanuary() {
        return january;
    }

    /**
     * Sets the value of the january property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setJanuary(BigDecimal value) {
        this.january = value;
    }

    /**
     * Gets the value of the february property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getFebruary() {
        return february;
    }

    /**
     * Sets the value of the february property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setFebruary(BigDecimal value) {
        this.february = value;
    }

    /**
     * Gets the value of the march property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMarch() {
        return march;
    }

    /**
     * Sets the value of the march property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMarch(BigDecimal value) {
        this.march = value;
    }

    /**
     * Gets the value of the april property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getApril() {
        return april;
    }

    /**
     * Sets the value of the april property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setApril(BigDecimal value) {
        this.april = value;
    }

    /**
     * Gets the value of the may property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMay() {
        return may;
    }

    /**
     * Sets the value of the may property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMay(BigDecimal value) {
        this.may = value;
    }

    /**
     * Gets the value of the june property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getJune() {
        return june;
    }

    /**
     * Sets the value of the june property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setJune(BigDecimal value) {
        this.june = value;
    }

    /**
     * Gets the value of the july property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getJuly() {
        return july;
    }

    /**
     * Sets the value of the july property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setJuly(BigDecimal value) {
        this.july = value;
    }

    /**
     * Gets the value of the august property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getAugust() {
        return august;
    }

    /**
     * Sets the value of the august property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setAugust(BigDecimal value) {
        this.august = value;
    }

    /**
     * Gets the value of the september property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getSeptember() {
        return september;
    }

    /**
     * Sets the value of the september property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setSeptember(BigDecimal value) {
        this.september = value;
    }

    /**
     * Gets the value of the october property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getOctober() {
        return october;
    }

    /**
     * Sets the value of the october property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setOctober(BigDecimal value) {
        this.october = value;
    }

    /**
     * Gets the value of the november property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getNovember() {
        return november;
    }

    /**
     * Sets the value of the november property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setNovember(BigDecimal value) {
        this.november = value;
    }

    /**
     * Gets the value of the december property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getDecember() {
        return december;
    }

    /**
     * Sets the value of the december property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setDecember(BigDecimal value) {
        this.december = value;
    }

}