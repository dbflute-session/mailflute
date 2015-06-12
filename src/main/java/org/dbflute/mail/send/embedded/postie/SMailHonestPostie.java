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
package org.dbflute.mail.send.embedded.postie;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.mail.Postcard;
import org.dbflute.mail.send.SMailPostalMotorbike;
import org.dbflute.mail.send.SMailPostie;
import org.dbflute.mail.send.exception.SMailMessageSettingFailureException;
import org.dbflute.mail.send.exception.SMailTransportFailureException;
import org.dbflute.mail.send.supplement.SMailAttachment;
import org.dbflute.mail.send.supplement.async.SMailAsyncStrategy;
import org.dbflute.mail.send.supplement.async.SMailAsyncStrategyNone;
import org.dbflute.mail.send.supplement.filter.SMailAddressFilter;
import org.dbflute.mail.send.supplement.filter.SMailAddressFilterNone;
import org.dbflute.mail.send.supplement.filter.SMailCancelFilter;
import org.dbflute.mail.send.supplement.filter.SMailCancelFilterNone;
import org.dbflute.mail.send.supplement.filter.SMailSubjectFilter;
import org.dbflute.mail.send.supplement.filter.SMailSubjectFilterNone;
import org.dbflute.mail.send.supplement.logging.SMailLoggingStrategy;
import org.dbflute.mail.send.supplement.logging.SMailTypicalLoggingStrategy;
import org.dbflute.optional.OptionalThing;

/**
 * @author jflute
 * @since 0.4.0 (2015/05/05 Tuesday)
 */
public class SMailHonestPostie implements SMailPostie {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final SMailCancelFilter noneCancelFilter = new SMailCancelFilterNone();
    private static final SMailAddressFilter noneAddressFilter = new SMailAddressFilterNone();
    private static final SMailSubjectFilterNone noneSubjectFilter = new SMailSubjectFilterNone();
    private static final SMailAsyncStrategy noneAsyncStrategy = new SMailAsyncStrategyNone();
    private static final SMailLoggingStrategy typicalLoggingStrategy = new SMailTypicalLoggingStrategy();

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final SMailPostalMotorbike motorbike; // not null
    protected SMailCancelFilter cancelFilter = noneCancelFilter; // not null
    protected SMailAddressFilter addressFilter = noneAddressFilter; // not null
    protected SMailSubjectFilter subjectFilter = noneSubjectFilter; // not null
    protected SMailAsyncStrategy asyncStrategy = noneAsyncStrategy; // not null
    protected SMailLoggingStrategy loggingStrategy = typicalLoggingStrategy; // not null
    protected boolean training;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SMailHonestPostie(SMailPostalMotorbike motorbike) {
        assertArgumentNotNull("motorbike", motorbike);
        this.motorbike = motorbike;
    }

    public SMailHonestPostie withCancelFilter(SMailCancelFilter cancelFilter) {
        assertArgumentNotNull("cancelFilter", cancelFilter);
        this.cancelFilter = cancelFilter;
        return this;
    }

    public SMailHonestPostie withAddressFilter(SMailAddressFilter addressFilter) {
        assertArgumentNotNull("addressFilter", addressFilter);
        this.addressFilter = addressFilter;
        return this;
    }

    public SMailHonestPostie withSubjectFilter(SMailSubjectFilter subjectFilter) {
        assertArgumentNotNull("subjectFilter", subjectFilter);
        this.subjectFilter = subjectFilter;
        return this;
    }

    public SMailHonestPostie withAsyncStrategy(SMailAsyncStrategy asyncStrategy) {
        assertArgumentNotNull("asyncStrategy", asyncStrategy);
        this.asyncStrategy = asyncStrategy;
        return this;
    }

    public SMailHonestPostie withLoggingStrategy(SMailLoggingStrategy loggingStrategy) {
        assertArgumentNotNull("loggingStrategy", loggingStrategy);
        this.loggingStrategy = loggingStrategy;
        return this;
    }

    public SMailHonestPostie asTraining() {
        training = true;
        return this;
    }

    // ===================================================================================
    //                                                                             Deliver
    //                                                                             =======
    @Override
    public void deliver(Postcard postcard) {
        final SMailPostingMessage message = createMailMessage(motorbike);
        if (isCancel(postcard)) {
            return; // no logging here, only filter knows the reason
        }
        prepareAddress(postcard, message);
        prepareSubject(postcard, message);
        prepareBody(postcard, message);
        send(postcard, message);
    }

    protected SMailPostingMessage createMailMessage(SMailPostalMotorbike motorbike) {
        return new SMailPostingMessage(extractNativeSession(motorbike));
    }

    protected Session extractNativeSession(SMailPostalMotorbike motorbike) {
        return motorbike.getNativeSession();
    }

    protected boolean isCancel(Postcard postcard) {
        return cancelFilter.isCancel(postcard);
    }

    // ===================================================================================
    //                                                                     Prepare Address
    //                                                                     ===============
    protected void prepareAddress(Postcard postcard, SMailPostingMessage message) {
        final Address fromAddress = postcard.getFrom();
        if (fromAddress == null) {
            throw new IllegalStateException("Not found the from address in the postcard: " + postcard);
        }
        final Address filteredFrom = addressFilter.filterFrom(postcard, fromAddress);
        message.setFrom(verifyFilteredFromAddress(postcard, filteredFrom));
        boolean existsToAddress = false;
        for (Address to : postcard.getToList()) {
            final OptionalThing<Address> opt = addressFilter.filterTo(postcard, to);
            verifyFilteredOptionalAddress(postcard, opt).ifPresent(address -> message.addTo(address));
            if (opt.isPresent()) {
                existsToAddress = true;
            }
        }
        verifyFilteredToAddressExists(postcard, existsToAddress);
        for (Address cc : postcard.getCcList()) {
            final OptionalThing<Address> opt = addressFilter.filterCc(postcard, cc);
            verifyFilteredOptionalAddress(postcard, opt).ifPresent(address -> message.addCc(address));
        }
        for (Address bcc : postcard.getBccList()) {
            final OptionalThing<Address> opt = addressFilter.filterBcc(postcard, bcc);
            verifyFilteredOptionalAddress(postcard, opt).ifPresent(address -> message.addBcc(address));
        }
    }

    protected Address verifyFilteredFromAddress(Postcard postcard, Address filteredFrom) {
        if (filteredFrom == null) {
            String msg = "The filtered from-address should not be null: " + postcard;
            throw new IllegalStateException(msg);
        }
        return filteredFrom;
    }

    protected OptionalThing<Address> verifyFilteredOptionalAddress(Postcard postcard, OptionalThing<Address> opt) {
        if (opt == null) {
            String msg = "The filtered optional should not be null: postcard=" + postcard;
            throw new IllegalStateException(msg);
        }
        return opt;
    }

    protected void verifyFilteredToAddressExists(Postcard postcard, boolean existsToAddress) {
        if (!existsToAddress) {
            String msg = "Empty to-address by filtering: specifiedToAddress=" + postcard.getToList();
            throw new IllegalStateException(msg);
        }
    }

    // ===================================================================================
    //                                                                     Prepare Subject
    //                                                                     ===============
    protected void prepareSubject(Postcard postcard, SMailPostingMessage message) {
        message.setSubject(getSubject(postcard), getSubjectEncoding());
    }

    protected String getSubject(Postcard postcard) {
        return subjectFilter.filterSubject(postcard, postcard.getSubject());
    }

    protected String getSubjectEncoding() {
        return getBasicEncoding();
    }

    // ===================================================================================
    //                                                                        Prepare Body
    //                                                                        ============
    protected void prepareBody(Postcard postcard, SMailPostingMessage message) {
        final String plainText = postcard.toCompletePlainText();
        final String htmlText = postcard.toCompleteHtmlText();
        message.savePlainTextForDisplay(plainText);
        message.saveHtmlTextForDisplay(htmlText);
        final Map<String, SMailAttachment> attachmentMap = postcard.getAttachmentMap();
        final MimeMessage nativeMessage = message.getMimeMessage();
        if (attachmentMap.isEmpty()) { // normally here
            setupTextPart(nativeMessage, plainText, TextType.PLAIN); // plain is required
            if (htmlText != null) { // HTML is optional
                setupTextPart(nativeMessage, htmlText, TextType.HTML);
            }
        } else { // with attachment
            if (htmlText != null) {
                throw new IllegalStateException("Unsupported HTML mail with attachment for now: " + postcard);
            }
            try {
                final MimeMultipart multipart = createTextWithAttachmentMultipart(postcard, message, plainText, attachmentMap);
                nativeMessage.setContent(multipart);
            } catch (MessagingException e) {
                String msg = "Failed to set attachment multipart content: " + postcard;
                throw new IllegalStateException(msg, e);
            }
        }
    }

    protected MimeMultipart createTextWithAttachmentMultipart(Postcard postcard, SMailPostingMessage message, String plain,
            Map<String, SMailAttachment> attachmentMap) throws MessagingException {
        final MimeMultipart multipart = newMimeMultipart();
        multipart.setSubType("mixed");
        multipart.addBodyPart((BodyPart) setupTextPart(newMimeBodyPart(), plain, TextType.PLAIN));
        for (Entry<String, SMailAttachment> entry : attachmentMap.entrySet()) {
            final SMailAttachment attachment = entry.getValue();
            multipart.addBodyPart((BodyPart) setupAttachmentPart(attachment));
            message.saveAttachmentForDisplay(attachment.getFilenameOnHeader());
        }
        return multipart;
    }

    protected MimeMultipart newMimeMultipart() {
        return new MimeMultipart();
    }

    protected MimeBodyPart newMimeBodyPart() {
        return new MimeBodyPart();
    }

    // ===================================================================================
    //                                                                           Text Part
    //                                                                           =========
    protected MimePart setupTextPart(MimePart part, String text, TextType textType) {
        final String encoding = getTextEncoding();
        final ByteBuffer buffer = prepareTextByteBuffer(text, encoding);
        final DataSource source = prepareTextDataSource(buffer);
        try {
            part.setDataHandler(createDataHandler(source));
            part.setHeader("Content-Transfer-Encoding", "7bit");
            part.setHeader("Content-Type", "text/" + textType.code() + "; charset=\"" + encoding + "\"");
        } catch (MessagingException e) {
            throw new SMailMessageSettingFailureException("Failed to set headers: " + encoding, e);
        }
        return part;
    }

    protected String getTextEncoding() {
        return getBasicEncoding();
    }

    protected ByteBuffer prepareTextByteBuffer(String text, final String encoding) {
        final ByteBuffer buffer;
        try {
            buffer = ByteBuffer.wrap(text.getBytes(encoding));
        } catch (UnsupportedEncodingException e) {
            throw new SMailMessageSettingFailureException("Unknown encoding: " + encoding, e);
        }
        return buffer;
    }

    protected ByteArrayDataSource prepareTextDataSource(final ByteBuffer buffer) {
        return new ByteArrayDataSource(buffer.array(), "application/octet-stream");
    }

    protected static enum TextType {

        PLAIN("plain"), HTML("html");
        private final String code;

        private TextType(String code) {
            this.code = code;
        }

        public String code() {
            return code;
        }
    }

    // ===================================================================================
    //                                                                     Attachment Part
    //                                                                     ===============
    protected MimePart setupAttachmentPart(SMailAttachment attachment) {
        assertArgumentNotNull("attachment", attachment);
        final MimePart part = newMimeBodyPart();
        final String contentType = buildAttachmentContentType(attachment);
        final DataSource source = prepareAttachmentDataSource(attachment);
        try {
            part.setDataHandler(createDataHandler(source));
            part.setHeader("Content-Transfer-Encoding", "base64");
            part.addHeader("Content-Type", contentType);
            part.addHeader("Content-Disposition", buildAttachmentContentDisposition(attachment));
        } catch (MessagingException e) {
            throw new SMailMessageSettingFailureException("Failed to set headers: " + attachment, e);
        }
        return part;
    }

    protected DataSource prepareAttachmentDataSource(SMailAttachment attachment) {
        final DataSource source;
        try {
            source = new ByteArrayDataSource(attachment.getReourceStream(), "application/octet-stream");
        } catch (IOException e) {
            throw new SMailMessageSettingFailureException("Failed to create data source: " + attachment, e);
        }
        return source;
    }

    protected String buildAttachmentContentType(SMailAttachment attachment) {
        final String filenameEncoding = getAttachmentFilenameEncoding();
        final String encodedFilename;
        try {
            final String filename = attachment.getFilenameOnHeader();
            encodedFilename = MimeUtility.encodeText(filename, filenameEncoding, "B"); // uses 'B' for various characters
        } catch (UnsupportedEncodingException e) {
            throw new SMailMessageSettingFailureException("Unknown encoding: " + filenameEncoding, e);
        }
        final StringBuilder sb = new StringBuilder();
        final String contentType = attachment.getContentType();
        sb.append(contentType);
        if (contentType.equals("text/plain")) {
            sb.append("; charset=").append(getAttachmentFilenameEncoding());
        }
        sb.append("; name=\"").append(encodedFilename).append("\"");
        return sb.toString();
    }

    protected String buildAttachmentContentDisposition(SMailAttachment attachObject) {
        return "attachment; filename=\"" + attachObject.getFilenameOnHeader() + "\"";
    }

    protected String getAttachmentFilenameEncoding() {
        return getBasicEncoding();
    }

    protected String getAttachmentTextFileEncoding() {
        return getBasicEncoding();
    }

    // ===================================================================================
    //                                                                        Send Message
    //                                                                        ============
    protected void send(Postcard postcard, SMailPostingMessage message) {
        try {
            if (postcard.isAsync()) {
                asyncStrategy.async(postcard, () -> doSend(postcard, message));
            } else {
                doSend(postcard, message);
            }
        } catch (RuntimeException e) {
            if (postcard.isSuppressSendFailure()) {
                loggingStrategy.logSuppressedCause(postcard, message, training, e);
            } else {
                throw e;
            }
        }
    }

    protected void doSend(Postcard postcard, SMailPostingMessage message) {
        logMailMessage(postcard, message);
        if (!training) {
            retryableSend(postcard, message);
        }
    }

    // -----------------------------------------------------
    //                                               Logging
    //                                               -------
    protected void logMailMessage(Postcard postcard, SMailPostingMessage message) {
        // TODO jflute mailflute: [B] eml file (2015/05/11)
        loggingStrategy.logMailMessage(postcard, message, training);
    }

    // -----------------------------------------------------
    //                                             Retryable
    //                                             ---------
    protected void retryableSend(Postcard postcard, SMailPostingMessage message) {
        final int retryCount = getRetryCount(postcard); // not negative, zero means no retry
        final long intervalMillis = getIntervalMillis(postcard); // not negative
        int challengeCount = 0;
        Exception firstCause = null;
        while (true) {
            if (challengeCount > retryCount) { // over retry limit, cannot send
                if (firstCause != null) { // just in case
                    handleSendFailure(postcard, message, firstCause);
                }
                break;
            }
            try {
                if (challengeCount > 0) { // means retry sending
                    waitBeforeRetrySending(intervalMillis);
                }
                actuallySend(message);
                if (challengeCount > 0) { // means retry success
                    noticeRetrySuccess(postcard, message, challengeCount, firstCause);
                }
                break;
            } catch (RuntimeException | MessagingException e) {
                if (firstCause == null) { // first cause may be most important
                    firstCause = e;
                }
            }
            ++challengeCount;
        }
    }

    protected int getRetryCount(Postcard postcard) { // you can override if all mails needs retry
        return postcard.getRetryCount();
    }

    protected long getIntervalMillis(Postcard postcard) { // you can as well
        return postcard.getIntervalMillis();
    }

    protected void waitBeforeRetrySending(long intervalMillis) {
        if (intervalMillis > 0) {
            try {
                Thread.sleep(intervalMillis);
            } catch (InterruptedException ignored) {}
        }
    }

    protected void actuallySend(SMailPostingMessage message) throws MessagingException {
        Transport.send(message.getMimeMessage());
    }

    protected void noticeRetrySuccess(Postcard postcard, SMailPostingMessage message, int challengeCount, Exception firstCause) {
        loggingStrategy.logRetrySuccess(postcard, message, training, challengeCount, firstCause);
    }

    protected void handleSendFailure(Postcard postcard, SMailPostingMessage message, Exception e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to send the mail message.");
        br.addItem("Postcard");
        br.addElement(postcard);
        br.addItem("Posting Message");
        br.addElement(Integer.hashCode(message.hashCode()));
        br.addElement(message);
        final String msg = br.buildExceptionMessage();
        throw new SMailTransportFailureException(msg, e);
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    protected String getBasicEncoding() {
        return "UTF-8";
    }

    protected DataHandler createDataHandler(DataSource source) {
        return new DataHandler(source);
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected void assertArgumentNotNull(String variableName, Object value) {
        if (variableName == null) {
            throw new IllegalArgumentException("The variableName should not be null.");
        }
        if (value == null) {
            throw new IllegalArgumentException("The argument '" + variableName + "' should not be null.");
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public boolean isTraining() {
        return training;
    }
}
