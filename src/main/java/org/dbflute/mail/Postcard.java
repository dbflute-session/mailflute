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

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;

import javax.mail.Address;

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.mail.send.exception.SMailFromAddressNotFoundException;
import org.dbflute.mail.send.exception.SMailPostcardIllegalStateException;
import org.dbflute.mail.send.supplement.SMailAttachment;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 * @since 0.1.0 (2015/01/20 Tuesday at higashi-ginza)
 */
public class Postcard implements CardView, Serializable {

    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected DeliveryCategory deliveryCategory; // optional (has default)

    protected Address from; // required
    protected List<Address> toList; // required at least one, lazy loaded
    protected List<Address> ccList; // optional, lazy loaded
    protected List<Address> bccList; // optional, lazy loaded
    protected String subject; // required or optional (e.g. from template file)
    protected Map<String, SMailAttachment> attachmentMap; // optional, lozy loaded

    // either required: bodyFile or plain/html body
    protected String bodyFile; // either required, also deriving HTML
    protected boolean alsoHtmlFile; // derive HTML file path from bodyFile
    protected boolean fromFilesystem;
    protected String plainBody; // null when body file used, direct text
    protected String htmlBody; // null when body file used, path or direct text
    protected Locale receiverLocale; // optional, and the locale file is not found, default file

    // either required
    protected Map<String, Object> templateVariableMap; // optional, lazy loaded
    protected boolean wholeFixedTextUsed; // optional , may be sometimes used

    // post office work
    protected String proofreadingPlain;
    protected String proofreadingHtml;

    // postie option
    protected boolean async;
    protected int retryCount;
    protected long intervalMillis;
    protected boolean suppressSendFailure;

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
            toList = new ArrayList<Address>(2);
        }
        toList.add(address);
    }

    public void addCc(Address address) {
        if (ccList == null) {
            ccList = new ArrayList<Address>(2);
        }
        ccList.add(address);
    }

    public void addBcc(Address address) {
        if (bccList == null) {
            bccList = new ArrayList<Address>(2);
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
    //                                            Attachment
    //                                            ----------
    public void attach(String filenameOnHeader, String contentType, InputStream stream) {
        if (attachmentMap == null) {
            attachmentMap = new LinkedHashMap<String, SMailAttachment>(4);
        }
        if (attachmentMap.containsKey(filenameOnHeader)) {
            String msg = "Already exists the attachment file: " + filenameOnHeader + ", " + contentType + ", existing=" + attachmentMap;
            throw new IllegalStateException(msg);
        }
        attachmentMap.put(filenameOnHeader, createAttachment(filenameOnHeader, contentType, stream));
    }

    protected SMailAttachment createAttachment(String filenameOnHeader, String contentType, InputStream stream) {
        return new SMailAttachment(filenameOnHeader, contentType, stream);
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

        public BodyFileOption receiverLocale(Locale receiverLocale) {
            Postcard.this.receiverLocale = receiverLocale;
            return this;
        }

        public void useTemplateText(Map<String, Object> variableMap) {
            templateVariableMap = variableMap;
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
            templateVariableMap = variableMap;
        }

        public void useWholeFixedText() {
            wholeFixedTextUsed = true;
        }
    }

    // ===================================================================================
    //                                                                       Postie Option
    //                                                                       =============
    public Postcard async() {
        async = true;
        return this;
    }

    public Postcard retry(int retryCount, long intervalMillis) {
        if (retryCount <= 0) {
            throw new IllegalArgumentException("The arguyment 'retryCount' should be positive integer: " + retryCount);
        }
        if (intervalMillis <= 0) {
            throw new IllegalArgumentException("The arguyment 'intervalMillis' should be positive long: " + intervalMillis);
        }
        this.retryCount = retryCount;
        this.intervalMillis = intervalMillis;
        return this;
    }

    public Postcard suppressSendFailure() {
        suppressSendFailure = true;
        return this;
    }

    // ===================================================================================
    //                                                                     PostOffice Work
    //                                                                     ===============
    // -----------------------------------------------------
    //                                          Office Check
    //                                          ------------
    public void officeCheck() {
        if (from == null) {
            throwMailFromAddressNotFoundException();
        }
        if (toList == null || toList.isEmpty()) {
            throwMailToAddressNotFoundException();
        }
        if ((bodyFile == null && plainBody == null) || (bodyFile != null && plainBody != null)) {
            String msg = "Set either body file or plain body: bodyFile=" + bodyFile + " plainBody=" + plainBody;
            throw new SMailPostcardIllegalStateException(msg);
        }
        if (plainBody == null && htmlBody != null) {
            String msg = "Cannot set html body only (without plain body): htmlBody=" + htmlBody;
            throw new SMailPostcardIllegalStateException(msg);
        }
        if ((!wholeFixedTextUsed && templateVariableMap == null) || (wholeFixedTextUsed && templateVariableMap != null)) {
            String msg = "You can set either fixed body or template body:";
            msg = msg + " fixedBodyUsed=" + wholeFixedTextUsed + " variableMap=" + templateVariableMap;
            throw new SMailPostcardIllegalStateException(msg);
        }
        if (templateVariableMap != null && templateVariableMap.isEmpty()) {
            String msg = "Empty variable map for template text: variableMap=" + templateVariableMap;
            throw new SMailPostcardIllegalStateException(msg);
        }
    }

    protected void throwMailFromAddressNotFoundException() {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the 'from' address in the postcard.");
        br.addItem("Advice");
        br.addElement("Specify your 'from' address.");
        br.addItem("Postcard");
        br.addElement(toString());
        final String msg = br.buildExceptionMessage();
        throw new SMailFromAddressNotFoundException(msg);
    }

    protected void throwMailToAddressNotFoundException() {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the 'to' address in the postcard.");
        br.addItem("Advice");
        br.addElement("Specify your 'to' address.");
        br.addItem("Postcard");
        br.addElement(toString());
        final String msg = br.buildExceptionMessage();
        throw new SMailFromAddressNotFoundException(msg);
    }

    // -----------------------------------------------------
    //                                         Office Option
    //                                         -------------
    public void officePlusHtml() {
        alsoHtmlFile = true;
    }

    // -----------------------------------------------------
    //                                      Office Proofread
    //                                      ----------------
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
        final StringBuilder sb = new StringBuilder();
        sb.append(DfTypeUtil.toClassTitle(this));
        sb.append(":{");
        sb.append("from=").append(from);
        sb.append("to=").append(toList);
        if (subject != null) {
            sb.append("subject=").append(subject);
        }
        if (bodyFile != null) {
            sb.append("bodyFile=").append(bodyFile);
        }
        sb.append("}");
        return sb.toString();
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

    public Map<String, SMailAttachment> getAttachmentMap() {
        return attachmentMap != null ? attachmentMap : DfCollectionUtil.emptyMap();
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

    public Locale getReceiverLocale() {
        return receiverLocale;
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

    // -----------------------------------------------------
    //                                         Postie Option
    //                                         -------------
    public boolean isAsync() {
        return async;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public long getIntervalMillis() {
        return intervalMillis;
    }

    public boolean isSuppressSendFailure() {
        return suppressSendFailure;
    }
}
