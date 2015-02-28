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
package org.dbflute.mailflute;

import java.util.ArrayList;
import java.util.List;

import javax.mail.Address;

import org.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 * @since 0.1.0 (2015/01/20 Tuesday at higashi-ginza)
 */
public class Postcard {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected Address fromAdr;
    protected List<Address> toAdrList;
    protected List<Address> ccAdrList;
    protected List<Address> bccAdrList;
    protected String subject;
    protected String plainBody;
    protected String plainTemplatePath;
    protected String htmlBody;
    protected String htmlTemplatePath;
    protected String category;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public Postcard() {
    }

    public Postcard asDeliveryCategory(String category) {
        this.category = category;
        return this;
    }

    // ===================================================================================
    //                                                                            Settings
    //                                                                            ========
    public void setFrom(Address address) {
        fromAdr = address;
    }

    public void addTo(Address address) {
        if (toAdrList == null) {
            toAdrList = new ArrayList<Address>();
        }
        toAdrList.add(address);
    }

    public void addCc(Address address) {
        if (ccAdrList == null) {
            ccAdrList = new ArrayList<Address>();
        }
        ccAdrList.add(address);
    }

    public void addBcc(Address address) {
        if (bccAdrList == null) {
            bccAdrList = new ArrayList<Address>();
        }
        bccAdrList.add(address);
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

    // ===================================================================================
    //                                                                              Getter
    //                                                                              ======
    public Address getFromAdr() {
        return fromAdr;
    }

    public List<Address> getToAdrList() {
        return toAdrList != null ? toAdrList : DfCollectionUtil.emptyList();
    }

    public List<Address> getCcAdrList() {
        return ccAdrList != null ? ccAdrList : DfCollectionUtil.emptyList();
    }

    public List<Address> getBccAdrList() {
        return bccAdrList != null ? bccAdrList : DfCollectionUtil.emptyList();
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

    public String getCategory() {
        return category;
    }
}
