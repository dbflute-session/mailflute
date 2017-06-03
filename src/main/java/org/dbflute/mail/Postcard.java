/*
 * Copyright 2015-2016 the original author or authors.
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.mail.send.SMailAddress;
import org.dbflute.mail.send.exception.SMailFromAddressNotFoundException;
import org.dbflute.mail.send.exception.SMailIllegalStateException;
import org.dbflute.mail.send.exception.SMailPostcardIllegalStateException;
import org.dbflute.mail.send.supplement.SMailPostingDiscloser;
import org.dbflute.mail.send.supplement.attachment.SMailAttachment;
import org.dbflute.optional.OptionalThing;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 * @since 0.1.0 (2015/01/20 Tuesday at higashi-ginza)
 */
public class Postcard implements CardView {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                      Postcard Request
    //                                      ----------------
    protected DeliveryCategory deliveryCategory; // optional (has default)
    protected Locale receiverLocale; // optional, and the locale file is not found, default file
    protected String subject; // required or optional (e.g. from template file)
    protected SMailAddress from; // required
    protected List<SMailAddress> toList; // required at least one, lazy loaded
    protected List<SMailAddress> ccList; // optional, lazy loaded
    protected List<SMailAddress> bccList; // optional, lazy loaded
    protected List<SMailAddress> replyToList; // optional, lazy loaded
    protected Map<String, SMailAttachment> attachmentMap; // optional, lozy loaded

    // -----------------------------------------------------
    //                                             Body File
    //                                             ---------
    // either required: bodyFile or plain/html body
    protected String bodyFile; // either required, also deriving HTML
    protected boolean alsoHtmlFile; // derive HTML file path from bodyFile
    protected boolean fromFilesystem;
    protected String plainBody; // null when body file used, direct text
    protected String htmlBody; // null when body file used, path or direct text
    protected Map<String, Object> templateVariableMap; // optional, lazy loaded
    protected boolean wholeFixedTextUsed; // may be sometimes used
    protected boolean forcedlyDirect; // e.g. real sending after dryrun 

    // -----------------------------------------------------
    //                                         Postie Option
    //                                         -------------
    // postie option
    protected boolean async;
    protected int retryCount;
    protected long intervalMillis;
    protected boolean suppressSendFailure;
    protected boolean dryrun;
    protected Map<String, Object> pushedLoggingMap; // optional, lazy loaded

    // -----------------------------------------------------
    //                                       PostOffice Work
    //                                       ---------------
    protected String proofreadingPlain;
    protected String proofreadingHtml;
    protected Map<String, Map<String, Object>> officeManagedLoggingMap; // optional, lazy loaded
    protected SMailPostingDiscloser officePostingDiscloser;

    // -----------------------------------------------------
    //                                          Message Memo
    //                                          ------------
    protected Object messageAuthor; // optional
    protected Class<?> messageTheme; // optional

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public Postcard() {
    }

    public Postcard asDeliveryCategory(DeliveryCategory category) {
        assertArgumentNotNull("category", category);
        this.deliveryCategory = category;
        return this;
    }

    public void asReceiverLocale(Locale receiverLocale) {
        assertArgumentNotNull("receiverLocale", receiverLocale);
        this.receiverLocale = receiverLocale;
    }

    // ===================================================================================
    //                                                                    Postcard Request
    //                                                                    ================
    // -----------------------------------------------------
    //                                               Subject
    //                                               -------
    public void setSubject(String subject) {
        this.subject = subject;
    }

    // -----------------------------------------------------
    //                                               Address
    //                                               -------
    public void setFrom(SMailAddress address) {
        assertArgumentNotNull("address", address);
        from = address;
    }

    public void addTo(SMailAddress address) {
        assertArgumentNotNull("address", address);
        if (toList == null) {
            toList = new ArrayList<SMailAddress>(2);
        }
        toList.add(address);
    }

    public void addCc(SMailAddress address) {
        assertArgumentNotNull("address", address);
        if (ccList == null) {
            ccList = new ArrayList<SMailAddress>(2);
        }
        ccList.add(address);
    }

    public void addBcc(SMailAddress address) {
        assertArgumentNotNull("address", address);
        if (bccList == null) {
            bccList = new ArrayList<SMailAddress>(2);
        }
        bccList.add(address);
    }

    public void addReplyTo(SMailAddress address) {
        assertArgumentNotNull("address", address);
        if (replyToList == null) {
            replyToList = new ArrayList<SMailAddress>(2);
        }
        replyToList.add(address);
    }

    // -----------------------------------------------------
    //                                            Attachment
    //                                            ----------
    public void attachPlainText(String filenameOnHeader, InputStream resourceStream, String textEncoding) {
        assertArgumentNotNull("filenameOnHeader", filenameOnHeader);
        assertArgumentNotNull("resourceStream", resourceStream);
        assertArgumentNotNull("textEncoding", textEncoding);
        doAttach(filenameOnHeader, "text/plain", resourceStream, textEncoding);
    }

    public void attachVarious(String filenameOnHeader, String contentType, InputStream resourceStream) {
        assertArgumentNotNull("filenameOnHeader", filenameOnHeader);
        assertArgumentNotNull("contentType", contentType);
        assertArgumentNotNull("resourceStream", resourceStream);
        doAttach(filenameOnHeader, contentType, resourceStream, null);
    }

    protected void doAttach(String filenameOnHeader, String contentType, InputStream resourceStream, String textEncoding) {
        if (attachmentMap == null) {
            attachmentMap = new LinkedHashMap<String, SMailAttachment>(4);
        }
        if (attachmentMap.containsKey(filenameOnHeader)) {
            String msg = "Already exists the attachment file: " + filenameOnHeader + ", " + contentType + ", existing=" + attachmentMap;
            throw new SMailIllegalStateException(msg);
        }
        final SMailAttachment attachment = createAttachment(filenameOnHeader, contentType, resourceStream, textEncoding);
        attachmentMap.put(filenameOnHeader, attachment);
    }

    protected SMailAttachment createAttachment(String filenameOnHeader, String contentType, InputStream resourceStream,
            String textEncoding) {
        return new SMailAttachment(filenameOnHeader, contentType, resourceStream, textEncoding);
    }

    // -----------------------------------------------------
    //                                             Body File
    //                                             ---------
    public BodyFileOption useBodyFile(String bodyFile) {
        assertArgumentNotNull("bodyFile", bodyFile);
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
            assertArgumentNotNull("variableMap", variableMap);
            templateVariableMap = variableMap;
        }

        public void useWholeFixedText() {
            wholeFixedTextUsed = true;
        }
    }

    protected void clearBodyFile() {
        bodyFile = null;
        alsoHtmlFile = false;
        fromFilesystem = false;
    }

    // -----------------------------------------------------
    //                                           Direct Body
    //                                           -----------
    public DirectBodyOption useDirectBody(String plainBody) {
        assertArgumentNotNull("plainBody", plainBody);
        this.plainBody = plainBody;
        return new DirectBodyOption();
    }

    public class DirectBodyOption {

        public DirectBodyOption alsoDirectHtml(String directHtml) {
            assertArgumentNotNull("directHtml", directHtml);
            htmlBody = directHtml;
            return this;
        }

        public void useTemplateText(Map<String, Object> variableMap) {
            assertArgumentNotNull("variableMap", variableMap);
            templateVariableMap = variableMap;
        }

        public WholeFixedTextOption useWholeFixedText() {
            wholeFixedTextUsed = true;
            return new WholeFixedTextOption();
        }
    }

    public class WholeFixedTextOption {

        public WholeFixedTextOption forcedlyDirect(String subject) {
            assertArgumentNotNull("subject", subject);
            Postcard.this.subject = subject;
            forcedlyDirect = true;
            officeManagedLogging(PostOffice.LOGGING_TITLE_SYSINFO, "forcedlyDirect", true);
            return this;
        }
    }

    // -----------------------------------------------------
    //                                     Override BodyFile
    //                                     -----------------
    public OverridingBodyFileOption overrideBodyFile(String subject, String plainBody) {
        assertArgumentNotNull("subject", subject);
        assertArgumentNotNull("plainBody", plainBody);
        if (bodyFile == null) {
            throw new IllegalStateException("No body file so mistake?: specified-subject=" + subject);
        }
        clearBodyFile();
        setSubject(subject);
        final DirectBodyOption option = useDirectBody(plainBody);
        return new OverridingBodyFileOption(option);
    }

    public class OverridingBodyFileOption {

        protected final DirectBodyOption directBodyOption;

        public OverridingBodyFileOption(DirectBodyOption directBodyOption) {
            this.directBodyOption = directBodyOption;
        }

        public OverridingBodyFileOption alsoHtmlBody(String htmlBody) {
            assertArgumentNotNull("htmlBody", htmlBody);
            directBodyOption.alsoDirectHtml(htmlBody);
            return this;
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

    public void dryrun() { // no chain to bring it into clear view
        dryrun = true;
    }

    // -----------------------------------------------------
    //                                               Logging
    //                                               -------
    public void pushLogging(String key, Object value) {
        assertArgumentNotNull("key", key);
        assertArgumentNotNull("value", value);
        if (pushedLoggingMap == null) {
            pushedLoggingMap = new LinkedHashMap<String, Object>(4);
        }
        pushedLoggingMap.put(key, value);
    }

    // ===================================================================================
    //                                                                     PostOffice Work
    //                                                                     ===============
    // -----------------------------------------------------
    //                                          Office Check
    //                                          ------------
    public void officeCheck() {
        // move to receptionist for dynamic text
        //if (from == null) {
        //    throwMailFromAddressNotFoundException();
        //}
        if (toList == null || toList.isEmpty()) {
            throwMailToAddressNotFoundException();
        }
        if (bodyFile == null && plainBody == null) {
            String msg = "Not found body file or plain body: bodyFile=" + bodyFile + " plainBody=" + plainBody;
            throw new SMailPostcardIllegalStateException(msg);
        }
        if (!forcedlyDirect && (bodyFile != null && plainBody != null)) {
            String msg = "Set either body file or plain body: bodyFile=" + bodyFile + " plainBody=" + plainBody;
            throw new SMailPostcardIllegalStateException(msg);
        }
        if (plainBody == null && htmlBody != null) {
            String msg = "Cannot set html body only (without plain body): htmlBody=" + htmlBody;
            throw new SMailPostcardIllegalStateException(msg);
        }
        if (!forcedlyDirect && (!wholeFixedTextUsed && templateVariableMap == null)) {
            String msg = "Not found template variable map:";
            msg = msg + " wholeFixedTextUsed=" + wholeFixedTextUsed + " variableMap=" + templateVariableMap;
            throw new SMailPostcardIllegalStateException(msg);
        }
        if (!forcedlyDirect && (wholeFixedTextUsed && templateVariableMap != null)) {
            String msg = "Unneeded template variable map:";
            msg = msg + " wholeFixedTextUsed=" + wholeFixedTextUsed + " variableMap=" + templateVariableMap;
            throw new SMailPostcardIllegalStateException(msg);
        }
        // no check for no variable mail (fixed message)
        //if (templateVariableMap != null && templateVariableMap.isEmpty()) {
        //    String msg = "Empty variable map for template text: variableMap=" + templateVariableMap;
        //    throw new SMailPostcardIllegalStateException(msg);
        //}
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
    public boolean needsMainProofreading() {
        return !isWholeFixedTextUsed() && hasTemplateVariable();
    }

    public void proofreadPlain(BiFunction<String, Map<String, Object>, String> proofreader) {
        this.proofreadingPlain = proofreader.apply(getProofreadingOrOriginalPlain().get(), getTemplaetVariableMap());
    }

    public void proofreadHtml(BiFunction<String, Map<String, Object>, String> proofreader) {
        this.proofreadingHtml = proofreader.apply(getProofreadingOrOriginalHtml().get(), getTemplaetVariableMap());
    }

    public OptionalThing<String> toCompletePlainText() {
        return getProofreadingOrOriginalPlain();
    }

    public OptionalThing<String> toCompleteHtmlText() {
        return getProofreadingOrOriginalHtml();
    }

    protected OptionalThing<String> getProofreadingOrOriginalPlain() {
        return OptionalThing.ofNullable(proofreadingPlain != null ? proofreadingPlain : plainBody, () -> {
            throw new SMailIllegalStateException("Not found the plain text: " + toString());
        });
    }

    protected OptionalThing<String> getProofreadingOrOriginalHtml() {
        return OptionalThing.ofNullable(proofreadingHtml != null ? proofreadingHtml : htmlBody, () -> {
            throw new SMailIllegalStateException("Not found the HTML text: " + toString());
        });
    }

    // -----------------------------------------------------
    //                                Office Managed Logging
    //                                ----------------------
    public void officeManagedLogging(String title, String key, Object value) {
        assertArgumentNotNull("title", title);
        assertArgumentNotNull("key", key);
        assertArgumentNotNull("value", value);
        if (officeManagedLoggingMap == null) {
            officeManagedLoggingMap = new LinkedHashMap<String, Map<String, Object>>(4);
        }
        Map<String, Object> valueMap = officeManagedLoggingMap.get(title);
        if (valueMap == null) {
            valueMap = new LinkedHashMap<String, Object>(4);
            officeManagedLoggingMap.put(title, valueMap);
        }
        valueMap.put(key, value);
    }

    // -----------------------------------------------------
    //                                      Office Discloser
    //                                      ----------------
    public void officeDisclosePostingState(SMailPostingDiscloser postingDiscloser) {
        assertArgumentNotNull("postingDiscloser", postingDiscloser);
        this.officePostingDiscloser = postingDiscloser;
    }

    // ===================================================================================
    //                                                                        Message Memo
    //                                                                        ============
    public void writeMessageAuthor(Object messageAuthor) {
        assertArgumentNotNull("messageAuthor", messageAuthor);
        this.messageAuthor = messageAuthor;
        final String exp = buildMessageAuthorLoggingExp(messageAuthor);
        officeManagedLogging(PostOffice.LOGGING_TITLE_SYSINFO, "author", exp);
    }

    protected String buildMessageAuthorLoggingExp(Object messageAuthor) {
        final String exp;
        if (messageAuthor instanceof Class<?>) {
            exp = ((Class<?>) messageAuthor).getSimpleName();
        } else {
            if (messageAuthor.getClass().getName().startsWith("java.")) { // e.g. String, Integer
                exp = messageAuthor.toString();
            } else {
                exp = messageAuthor.getClass().getSimpleName();
            }
        }
        return exp;
    }

    public void writeMessageTheme(Class<?> messageTheme) {
        assertArgumentNotNull("messageTheme", messageTheme);
        this.messageTheme = messageTheme;
        officeManagedLogging(PostOffice.LOGGING_TITLE_SYSINFO, "theme", messageTheme.getSimpleName());
    }

    // ===================================================================================
    //                                                                        Small Helper
    //                                                                        ============
    protected void assertArgumentNotNull(String variableName, Object value) {
        if (variableName == null) {
            throw new IllegalArgumentException("The variableName should not be null.");
        }
        if (value == null) {
            throw new IllegalArgumentException("The argument '" + variableName + "' should not be null.");
        }
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
        sb.append(", to=").append(toList);
        if (subject != null) {
            sb.append(", subject=").append(subject);
        }
        if (bodyFile != null) {
            sb.append(", bodyFile=").append(bodyFile);
        }
        sb.append("}");
        return sb.toString();
    }

    // ===================================================================================
    //                                                                              Getter
    //                                                                              ======
    // -----------------------------------------------------
    //                                      Postcard Request
    //                                      ----------------
    public OptionalThing<DeliveryCategory> getDeliveryCategory() {
        return OptionalThing.ofNullable(deliveryCategory, () -> {
            throw new SMailIllegalStateException("Not found the delivery category: " + toString());
        });
    }

    public OptionalThing<Locale> getReceiverLocale() {
        return OptionalThing.ofNullable(receiverLocale, () -> {
            throw new SMailIllegalStateException("Not found the receiver locale: " + toString());
        });
    }

    public OptionalThing<String> getSubject() {
        return OptionalThing.ofNullable(subject, () -> {
            throw new SMailIllegalStateException("Not found the subject: " + toString());
        });
    }

    public OptionalThing<SMailAddress> getFrom() {
        return OptionalThing.ofNullable(from, () -> {
            throw new SMailIllegalStateException("Not found the from address: " + toString());
        });
    }

    public List<SMailAddress> getToList() {
        return toList != null ? Collections.unmodifiableList(toList) : DfCollectionUtil.emptyList();
    }

    public List<SMailAddress> getCcList() {
        return ccList != null ? Collections.unmodifiableList(ccList) : DfCollectionUtil.emptyList();
    }

    public List<SMailAddress> getBccList() {
        return bccList != null ? Collections.unmodifiableList(bccList) : DfCollectionUtil.emptyList();
    }

    public List<SMailAddress> getReplyToList() {
        return replyToList != null ? Collections.unmodifiableList(replyToList) : DfCollectionUtil.emptyList();
    }

    public Map<String, SMailAttachment> getAttachmentMap() {
        return attachmentMap != null ? Collections.unmodifiableMap(attachmentMap) : DfCollectionUtil.emptyMap();
    }

    // -----------------------------------------------------
    //                                             Body File
    //                                             ---------
    public boolean hasBodyFile() {
        return bodyFile != null;
    }

    public OptionalThing<String> getBodyFile() {
        return OptionalThing.ofNullable(bodyFile, () -> {
            throw new SMailIllegalStateException("Not found the body file: " + toString());
        });
    }

    public boolean isAlsoHtmlFile() {
        return alsoHtmlFile;
    }

    public boolean isFromFilesystem() {
        return fromFilesystem;
    }

    public boolean hasTemplateVariable() {
        return templateVariableMap != null;
    }

    public Map<String, Object> getTemplaetVariableMap() {
        return templateVariableMap != null ? Collections.unmodifiableMap(templateVariableMap) : Collections.emptyMap();
    }

    public boolean isWholeFixedTextUsed() {
        return wholeFixedTextUsed;
    }

    public boolean isForcedlyDirect() {
        return forcedlyDirect;
    }

    // -----------------------------------------------------
    //                                             Body Text
    //                                             ---------
    public OptionalThing<String> getPlainBody() {
        return OptionalThing.ofNullable(plainBody, () -> {
            throw new SMailIllegalStateException("Not found the plain body: " + toString());
        });
    }

    public boolean hasHtmlBody() {
        return htmlBody != null;
    }

    public OptionalThing<String> getHtmlBody() {
        return OptionalThing.ofNullable(htmlBody, () -> {
            throw new SMailIllegalStateException("Not found the html body: " + toString());
        });
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

    public boolean isDryrun() {
        return dryrun;
    }

    public boolean hasPushedLogging() {
        return pushedLoggingMap != null;
    }

    public Map<String, Object> getPushedLoggingMap() {
        return pushedLoggingMap != null ? Collections.unmodifiableMap(pushedLoggingMap) : Collections.emptyMap();
    }

    // -----------------------------------------------------
    //                                       PostOffice Work
    //                                       ---------------
    public boolean hasOfficeManagedLogging() {
        return officeManagedLoggingMap != null;
    }

    public Map<String, Map<String, Object>> getOfficeManagedLoggingMap() {
        return officeManagedLoggingMap != null ? Collections.unmodifiableMap(officeManagedLoggingMap) : Collections.emptyMap();
    }

    public boolean hasOfficePostingDiscloser() {
        return officePostingDiscloser != null;
    }

    public OptionalThing<SMailPostingDiscloser> getOfficePostingDiscloser() {
        return OptionalThing.ofNullable(officePostingDiscloser, () -> {
            throw new IllegalStateException("Not found the office posting discloser: " + toString());
        });
    }

    // -----------------------------------------------------
    //                                          Message Memo
    //                                          ------------
    public OptionalThing<Object> getMessageAuthor() {
        return OptionalThing.ofNullable(messageAuthor, () -> {
            throw new IllegalStateException("Not found the message author: " + toString());
        });
    }

    public OptionalThing<Class<?>> getMessageTheme() {
        return OptionalThing.ofNullable(messageTheme, () -> {
            throw new IllegalStateException("Not found the message theme: " + toString());
        });
    }
}
