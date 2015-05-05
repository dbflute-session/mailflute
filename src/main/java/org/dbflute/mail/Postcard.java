/*
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.dbflute.mail;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Address;

import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 * @since 0.1.0 (2015/01/20 Tuesday at higashi-ginza)
 */
public class Postcard {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected DeliveryCategory deliveryCategory; // optional (has default)
    protected boolean fixedTextUsed; // may be sometimes used
    protected boolean directBodyUsed; // normally false, for easy sending
    protected Address from; // required
    protected List<Address> toList; // required at least one, lazy loaded
    protected List<Address> ccList; // optional, lazy loaded
    protected List<Address> bccList; // optional, lazy loaded
    protected String subject; // required or optional (e.g. from template file)
    protected String plainBody; // required, path or direct text
    protected String htmlBody; // optional, path or direct text
    protected Map<String, Object> variableMap; // optional, lazy loaded

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public Postcard() {
    }

    public Postcard asDeliveryCategory(DeliveryCategory category) {
        this.deliveryCategory = category;
        return this;
    }

    public Postcard useDirectBody() {
        this.directBodyUsed = true;
        return this;
    }

    public Postcard useFixedText() {
        this.fixedTextUsed = true;
        return this;
    }

    // ===================================================================================
    //                                                                            Settings
    //                                                                            ========
    public void setFrom(Address address) {
        from = address;
    }

    public void addTo(Address address) {
        if (toList == null) {
            toList = new ArrayList<Address>();
        }
        toList.add(address);
    }

    public void addCc(Address address) {
        if (ccList == null) {
            ccList = new ArrayList<Address>();
        }
        ccList.add(address);
    }

    public void addBcc(Address address) {
        if (bccList == null) {
            bccList = new ArrayList<Address>();
        }
        bccList.add(address);
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setPlainBody(String plainBody) {
        this.plainBody = plainBody;
    }

    public void setHtmlBody(String htmlBody) {
        this.htmlBody = htmlBody;
    }

    public void addVariable(String name, Object value) {
        if (variableMap == null) {
            variableMap = new LinkedHashMap<String, Object>();
        }
        variableMap.put(name, value);
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return DfTypeUtil.toClassTitle(this) + ":{from=" + from + ", to=" + toList + ", " + subject + "}";
    }

    // ===================================================================================
    //                                                                              Getter
    //                                                                              ======
    public DeliveryCategory getDeliveryCategory() {
        return deliveryCategory;
    }

    public boolean isDirectBodyUsed() {
        return directBodyUsed;
    }

    public boolean isFixedTextUsed() {
        return fixedTextUsed;
    }

    public Address getFrom() {
        return from;
    }

    public List<Address> getToList() {
        return toList != null ? toList : DfCollectionUtil.emptyList();
    }

    public List<Address> getCcList() {
        return ccList != null ? ccList : DfCollectionUtil.emptyList();
    }

    public List<Address> getBccList() {
        return bccList != null ? bccList : DfCollectionUtil.emptyList();
    }

    public String getSubject() {
        return subject;
    }

    public String getPlainBody() {
        return plainBody;
    }

    public String getHtmlBody() {
        return htmlBody;
    }

    public Map<String, Object> getVariableMap() {
        return variableMap != null ? variableMap : DfCollectionUtil.emptyMap();
    }
}
