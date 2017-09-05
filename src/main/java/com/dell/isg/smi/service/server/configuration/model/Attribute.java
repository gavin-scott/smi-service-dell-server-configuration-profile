/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.service.server.configuration.model;

import javax.xml.bind.annotation.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Attribute", description = "This has the attribute name and value of a component.")
@JsonInclude(Include.NON_DEFAULT)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "value" })
@XmlRootElement(name = "Attribute")
public class Attribute {

    @ApiModelProperty(value = "Attribute Name", required = true)
    @XmlAttribute(name = "Name", required = true)
    protected String name;

    @ApiModelProperty(value = "Attribute Value", required = true)
    @XmlValue
    protected String value;


    /**
     * Gets the value of the value property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getValue() {
        return value;
    }


    /**
     * Sets the value of the value property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setValue(String value) {
        this.value = value;
    }


    /**
     * Gets the value of the name property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getName() {
        return name;
    }


    /**
     * Sets the value of the name property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setName(String value) {
        this.name = value;
    }

}
