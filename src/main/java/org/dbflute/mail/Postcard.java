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
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

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

    protected Address from; // required
    protected List<Address> toList; // required at least one, lazy loaded
    protected List<Address> ccList; // optional, lazy loaded
    protected List<Address> bccList; // optional, lazy loaded
    protected String subject; // required or optional (e.g. from template file)

    // either required: bodyFile or plain/html body
    protected String bodyFile; // either required, also deriving HTML
    protected boolean alsoHtmlFile; // derive HTML file path from bodyFile
    protected boolean fromFilesystem;
    protected String plainBody; // null when body file used, direct text
    protected String htmlBody; // null when body file used, path or direct text

    // either required
    protected Map<String, Object> templateVariableMap; // optional, lazy loaded
    protected boolean wholeFixedTextUsed; // optional , may be sometimes used

    // post office work
    protected String proofreadingPlain;
    protected String proofreadingHtml;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public Postcard() {
    }

    public Postcard asDeliveryCategory(DeliveryCategory category) {
        this.deliveryCategory = category;
        return this;
    }

    // ===================================================================================
    //                                                                    Postcard Request
    //                                                                    ================
    // -----------------------------------------------------
    //                                               Address
    //                                               -------
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

    // -----------------------------------------------------
    //                                               Subject
    //                                               -------
    public void setSubject(String subject) {
        this.subject = subject;
    }

    // -----------------------------------------------------
    //                                             Body File
    //                                             ---------
    public BodyFileOption useBodyFile(String bodyFile) {
        this.bodyFile = bodyFile;
        return new BodyFileOption();
    }

    public class BodyFileOption {

        public BodyFileOption alsoHtmlFile() {
            alsoHtmlFile = true;
            return this;
        }

        public BodyFileOption fromFilesystem() {
            fromFilesystem = true;
            return this;
        }

        public void useTemplateText(Map<String, Object> variableMap) {
            Postcard.this.templateVariableMap = variableMap;
        }

        public void useWholeFixedText() {
            wholeFixedTextUsed = true;
        }
    }

    // -----------------------------------------------------
    //                                           Direct Body
    //                                           -----------
    public DirectBodyOption useDirectBody(String plainBody) {
        this.plainBody = plainBody;
        return new DirectBodyOption();
    }

    public class DirectBodyOption {

        public void alsoDirectHtml(String directHtml) {
            htmlBody = directHtml;
        }

        public void useTemplateText(Map<String, Object> variableMap) {
            Postcard.this.templateVariableMap = variableMap;
        }

        public void useWholeFixedText() {
            wholeFixedTextUsed = true;
        }
    }

    // ===================================================================================
    //                                                                     PostOffice Work
    //                                                                     ===============
    public void officeCheck() {
        // TODO jflute mailflute: [D] postcard exception
        if ((bodyFile == null && plainBody == null) || (bodyFile != null && plainBody != null)) {
            String msg = "Set either body file or plain body: bodyFile=" + bodyFile + " plainBody=" + plainBody;
            throw new IllegalStateException(msg);
        }
        if (plainBody == null && htmlBody != null) {
            String msg = "Cannot set html body only (without plain body): htmlBody=" + htmlBody;
            throw new IllegalStateException(msg);
        }
        if ((!wholeFixedTextUsed && templateVariableMap == null) || (wholeFixedTextUsed && templateVariableMap != null)) {
            String msg = "You can set either fixed body or template body:";
            msg = msg + " fixedBodyUsed=" + wholeFixedTextUsed + " variableMap=" + templateVariableMap;
            throw new IllegalStateException(msg);
        }
        if (templateVariableMap != null && templateVariableMap.isEmpty()) {
            String msg = "Empty variable map for template text: variableMap=" + templateVariableMap;
            throw new IllegalStateException(msg);
        }
    }

    public void officePlusHtml() {
        alsoHtmlFile = true;
    }

    public void proofreadPlain(BiFunction<String, Map<String, Object>, String> proofreader) {
        this.proofreadingPlain = proofreader.apply(getProofreadingOrOriginalPlain(), getTemplaetVariableMap());
    }

    public void proofreadHtml(BiFunction<String, Map<String, Object>, String> proofreader) {
        this.proofreadingHtml = proofreader.apply(getProofreadingOrOriginalHtml(), getTemplaetVariableMap());
    }

    public String toCompletePlainText() {
        return getProofreadingOrOriginalPlain();
    }

    public String toCompleteHtmlText() {
        return getProofreadingOrOriginalHtml();
    }

    protected String getProofreadingOrOriginalPlain() {
        return proofreadingPlain != null ? proofreadingPlain : plainBody;
    }

    protected String getProofreadingOrOriginalHtml() {
        return proofreadingHtml != null ? proofreadingHtml : htmlBody;
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

    public String getBodyFile() {
        return bodyFile;
    }

    public boolean hasBodyFile() {
        return bodyFile != null;
    }

    public boolean isAlsoHtmlFile() {
        return alsoHtmlFile;
    }

    public boolean isFromFilesystem() {
        return fromFilesystem;
    }

    public String getPlainBody() {
        return plainBody;
    }

    public boolean hasHtmlBody() {
        return htmlBody != null;
    }

    public String getHtmlBody() {
        return htmlBody;
    }

    public boolean hasTemplateVariable() {
        return templateVariableMap != null;
    }

    public Map<String, Object> getTemplaetVariableMap() {
        return templateVariableMap != null ? templateVariableMap : DfCollectionUtil.emptyMap();
    }

    public boolean isWholeFixedTextUsed() {
        return wholeFixedTextUsed;
    }
}
