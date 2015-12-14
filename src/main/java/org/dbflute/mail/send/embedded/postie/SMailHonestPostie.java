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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.mail.Postcard;
import org.dbflute.mail.send.SMailAddress;
import org.dbflute.mail.send.SMailPostalMotorbike;
import org.dbflute.mail.send.SMailPostie;
import org.dbflute.mail.send.exception.SMailIllegalStateException;
import org.dbflute.mail.send.exception.SMailMessageSettingFailureException;
import org.dbflute.mail.send.exception.SMailTransportFailureException;
import org.dbflute.mail.send.supplement.async.SMailAsyncStrategy;
import org.dbflute.mail.send.supplement.async.SMailAsyncStrategyNone;
import org.dbflute.mail.send.supplement.attachment.SMailAttachment;
import org.dbflute.mail.send.supplement.filter.SMailAddressFilter;
import org.dbflute.mail.send.supplement.filter.SMailAddressFilterNone;
import org.dbflute.mail.send.supplement.filter.SMailBodyTextFilter;
import org.dbflute.mail.send.supplement.filter.SMailBodyTextFilterNone;
import org.dbflute.mail.send.supplement.filter.SMailCancelFilter;
import org.dbflute.mail.send.supplement.filter.SMailCancelFilterNone;
import org.dbflute.mail.send.supplement.filter.SMailSubjectFilter;
import org.dbflute.mail.send.supplement.filter.SMailSubjectFilterNone;
import org.dbflute.mail.send.supplement.inetaddr.SMailInternetAddressCreator;
import org.dbflute.mail.send.supplement.inetaddr.SMailNormalInternetAddressCreator;
import org.dbflute.mail.send.supplement.label.SMailLabelStrategy;
import org.dbflute.mail.send.supplement.label.SMailLabelStrategyNone;
import org.dbflute.mail.send.supplement.logging.SMailLoggingStrategy;
import org.dbflute.mail.send.supplement.logging.SMailTypicalLoggingStrategy;
import org.dbflute.mail.send.supplement.retry.SMailRetryStrategy;
import org.dbflute.mail.send.supplement.retry.SMailRetryStrategyNone;
import org.dbflute.optional.OptionalThing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 0.4.0 (2015/05/05 Tuesday)
 */
public class SMailHonestPostie implements SMailPostie {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger logger = LoggerFactory.getLogger(SMailHonestPostie.class); // for normal logging
    private static final SMailCancelFilter noneCancelFilter = new SMailCancelFilterNone();
    private static final SMailAddressFilter noneAddressFilter = new SMailAddressFilterNone();
    private static final SMailSubjectFilter noneSubjectFilter = new SMailSubjectFilterNone();
    private static final SMailBodyTextFilter noneBodyTextFilter = new SMailBodyTextFilterNone();
    private static final SMailAsyncStrategy noneAsyncStrategy = new SMailAsyncStrategyNone();
    private static final SMailRetryStrategy noneRetryStrategy = new SMailRetryStrategyNone();
    private static final SMailLabelStrategy noneLabelStrategy = new SMailLabelStrategyNone();
    private static final SMailLoggingStrategy typicalLoggingStrategy = new SMailTypicalLoggingStrategy();
    private static final SMailInternetAddressCreator normalInternetAddressCreator = new SMailNormalInternetAddressCreator();

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final SMailPostalMotorbike motorbike; // not null
    protected SMailCancelFilter cancelFilter = noneCancelFilter; // not null
    protected SMailAddressFilter addressFilter = noneAddressFilter; // not null
    protected SMailSubjectFilter subjectFilter = noneSubjectFilter; // not null
    protected SMailBodyTextFilter bodyTextFilter = noneBodyTextFilter; // not null
    protected SMailAsyncStrategy asyncStrategy = noneAsyncStrategy; // not null
    protected SMailRetryStrategy retryStrategy = noneRetryStrategy; // not null
    protected SMailLabelStrategy labelStrategy = noneLabelStrategy; // not null
    protected SMailLoggingStrategy loggingStrategy = typicalLoggingStrategy; // not null
    protected SMailInternetAddressCreator internetAddressCreator = normalInternetAddressCreator; // not null
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

    public SMailHonestPostie withBodyTextFilter(SMailBodyTextFilter bodyTextFilter) {
        assertArgumentNotNull("bodyTextFilter", bodyTextFilter);
        this.bodyTextFilter = bodyTextFilter;
        return this;
    }

    public SMailHonestPostie withAsyncStrategy(SMailAsyncStrategy asyncStrategy) {
        assertArgumentNotNull("asyncStrategy", asyncStrategy);
        this.asyncStrategy = asyncStrategy;
        return this;
    }

    public SMailHonestPostie withRetryStrategy(SMailRetryStrategy retryStrategy) {
        assertArgumentNotNull("retryStrategy", retryStrategy);
        this.retryStrategy = retryStrategy;
        return this;
    }

    public SMailHonestPostie withLabelStrategy(SMailLabelStrategy labelStrategy) {
        assertArgumentNotNull("labelStrategy", labelStrategy);
        this.labelStrategy = labelStrategy;
        return this;
    }

    public SMailHonestPostie withLoggingStrategy(SMailLoggingStrategy loggingStrategy) {
        assertArgumentNotNull("loggingStrategy", loggingStrategy);
        this.loggingStrategy = loggingStrategy;
        return this;
    }

    public SMailHonestPostie withInternetAddressCreator(SMailInternetAddressCreator internetAddressCreator) {
        assertArgumentNotNull("internetAddressCreator", internetAddressCreator);
        this.internetAddressCreator = internetAddressCreator;
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
        final SMailPostingMessage message = createMailMessage(postcard);
        if (isCancel(postcard)) {
            return; // no logging here, only filter knows the reason
        }
        prepareAddress(postcard, message);
        prepareSubject(postcard, message);
        prepareBody(postcard, message);
        prepareAsync(postcard);
        prepareRetry(postcard);
        disclosePostingState(postcard, message);
        if (postcard.isDryrun()) {
            logger.debug("*dryrun: postcard={}", postcard); // normal logging here
            return;
        }
        send(postcard, message);
    }

    protected SMailPostingMessage createMailMessage(Postcard postcard) {
        final MimeMessage mimeMessage = createMimeMessage(extractNativeSession(motorbike));
        final Map<String, Object> pushedLoggingMap = postcard.getPushedLoggingMap();
        final Map<String, Map<String, Object>> officeManagedLoggingMap = postcard.getOfficeManagedLoggingMap();
        return new SMailPostingMessage(mimeMessage, motorbike, training, pushedLoggingMap, officeManagedLoggingMap);
    }

    protected Session extractNativeSession(SMailPostalMotorbike motorbike) {
        return motorbike.getNativeSession();
    }

    protected MimeMessage createMimeMessage(Session session) {
        return new MimeMessage(session);
    }

    protected boolean isCancel(Postcard postcard) {
        return cancelFilter.isCancel(postcard);
    }

    // ===================================================================================
    //                                                                     Prepare Address
    //                                                                     ===============
    protected void prepareAddress(Postcard postcard, SMailPostingMessage message) {
        final SMailAddress from = postcard.getFrom().orElseThrow(() -> { /* already checked, but just in case */
            return new SMailIllegalStateException("Not found the from address in the postcard: " + postcard);
        });
        final Address filteredFrom = addressFilter.filterFrom(postcard, toInternetAddress(postcard, from));
        message.setFrom(verifyFilteredFromAddress(postcard, filteredFrom));
        boolean existsToAddress = false;
        for (SMailAddress to : postcard.getToList()) {
            final OptionalThing<Address> opt = addressFilter.filterTo(postcard, toInternetAddress(postcard, to));
            verifyFilteredOptionalAddress(postcard, opt).ifPresent(address -> message.addTo(address));
            if (opt.isPresent()) {
                existsToAddress = true;
            }
        }
        verifyFilteredToAddressExists(postcard, existsToAddress);
        for (SMailAddress cc : postcard.getCcList()) {
            final OptionalThing<Address> opt = addressFilter.filterCc(postcard, toInternetAddress(postcard, cc));
            verifyFilteredOptionalAddress(postcard, opt).ifPresent(address -> message.addCc(address));
        }
        for (SMailAddress bcc : postcard.getBccList()) {
            final OptionalThing<Address> opt = addressFilter.filterBcc(postcard, toInternetAddress(postcard, bcc));
            verifyFilteredOptionalAddress(postcard, opt).ifPresent(address -> message.addBcc(address));
        }
        final List<SMailAddress> replyToList = postcard.getReplyToList();
        if (!replyToList.isEmpty()) {
            final List<Address> filteredList = new ArrayList<Address>(replyToList.size());
            for (SMailAddress replyTo : replyToList) {
                final OptionalThing<Address> opt = addressFilter.filterReplyTo(postcard, toInternetAddress(postcard, replyTo));
                verifyFilteredOptionalAddress(postcard, opt).ifPresent(address -> filteredList.add(address));
            }
            message.setReplyTo(filteredList);
        }
    }

    // -----------------------------------------------------
    //                                        Label Handling
    //                                        --------------
    protected Address toInternetAddress(Postcard postcard, SMailAddress address) {
        return createAddress(postcard, address);
    }

    protected Address createAddress(Postcard postcard, SMailAddress address) {
        final InternetAddress internetAddress;
        try {
            internetAddress = createInternetAddress(postcard, address.getAddress(), isStrictAddress());
        } catch (AddressException e) {
            throw new IllegalStateException("Failed to create internet address: " + address, e);
        }
        address.getPersonal().ifPresent(personal -> {
            final String encoding = getPersonalEncoding();
            try {
                final Locale locale = postcard.getReceiverLocale().orElse(Locale.getDefault());
                final String resolved = labelStrategy.resolveLabel(postcard, locale, personal);
                internetAddress.setPersonal(resolved, encoding);
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException("Unknown encoding for personal: encoding=" + encoding + " personal=" + personal, e);
            }
        });
        return internetAddress;
    }

    protected boolean isStrictAddress() {
        return true;
    }

    protected InternetAddress createInternetAddress(Postcard postcard, String address, boolean strict) throws AddressException {
        return internetAddressCreator.create(postcard, address, strict);
    }

    protected String getPersonalEncoding() {
        return getBasicEncoding();
    }

    // -----------------------------------------------------
    //                                        Verify Address
    //                                        --------------
    protected Address verifyFilteredFromAddress(Postcard postcard, Address filteredFrom) {
        if (filteredFrom == null) {
            String msg = "The filtered from-address should not be null: " + postcard;
            throw new SMailIllegalStateException(msg);
        }
        return filteredFrom;
    }

    protected OptionalThing<Address> verifyFilteredOptionalAddress(Postcard postcard, OptionalThing<Address> opt) {
        if (opt == null) {
            String msg = "The filtered optional should not be null: postcard=" + postcard;
            throw new SMailIllegalStateException(msg);
        }
        return opt;
    }

    protected void verifyFilteredToAddressExists(Postcard postcard, boolean existsToAddress) {
        if (!existsToAddress) {
            String msg = "Empty to-address by filtering: specifiedToAddress=" + postcard.getToList();
            throw new SMailIllegalStateException(msg);
        }
    }

    // ===================================================================================
    //                                                                     Prepare Subject
    //                                                                     ===============
    protected void prepareSubject(Postcard postcard, SMailPostingMessage message) {
        message.setSubject(getSubject(postcard), getSubjectEncoding());
    }

    protected String getSubject(Postcard postcard) {
        return subjectFilter.filterSubject(postcard, postcard.getSubject().get());
    }

    protected String getSubjectEncoding() {
        return getPersonalEncoding();
    }

    // ===================================================================================
    //                                                                        Prepare Body
    //                                                                        ============
    protected void prepareBody(Postcard postcard, SMailPostingMessage message) {
        final String plainText = toCompletePlainText(postcard);
        final OptionalThing<String> optHtmlText = toCompleteHtmlText(postcard);
        message.savePlainTextForDisplay(plainText);
        message.saveHtmlTextForDisplay(optHtmlText);
        final Map<String, SMailAttachment> attachmentMap = postcard.getAttachmentMap();
        final MimeMessage nativeMessage = message.getMimeMessage();
        if (attachmentMap.isEmpty()) { // normally here
            setupTextPart(nativeMessage, plainText, TextType.PLAIN); // plain is required
            optHtmlText.ifPresent(htmlText -> {
                setupTextPart(nativeMessage, htmlText, TextType.HTML);
            });
        } else { // with attachment
            if (optHtmlText.isPresent()) {
                throw new SMailIllegalStateException("Unsupported HTML mail with attachment for now: " + postcard);
            }
            try {
                final MimeMultipart multipart = createTextWithAttachmentMultipart(postcard, message, plainText, attachmentMap);
                nativeMessage.setContent(multipart);
            } catch (MessagingException e) {
                String msg = "Failed to set attachment multipart content: " + postcard;
                throw new SMailIllegalStateException(msg, e);
            }
        }
    }

    protected String toCompletePlainText(Postcard postcard) {
        return postcard.toCompletePlainText().map(plainText -> {
            return bodyTextFilter.filterBody(postcard, plainText, /*html*/false);
        }).get();
    }

    protected OptionalThing<String> toCompleteHtmlText(Postcard postcard) {
        return postcard.toCompleteHtmlText().map(htmlText -> {
            return bodyTextFilter.filterBody(postcard, htmlText, /*html*/true);
        });
    }

    protected MimeMultipart createTextWithAttachmentMultipart(Postcard postcard, SMailPostingMessage message, String plain,
            Map<String, SMailAttachment> attachmentMap) throws MessagingException {
        final MimeMultipart multipart = newMimeMultipart();
        multipart.setSubType("mixed");
        multipart.addBodyPart((BodyPart) setupTextPart(newMimeBodyPart(), plain, TextType.PLAIN));
        for (Entry<String, SMailAttachment> entry : attachmentMap.entrySet()) {
            final SMailAttachment attachment = entry.getValue();
            multipart.addBodyPart((BodyPart) setupAttachmentPart(message, attachment));
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
        assertArgumentNotNull("part", part);
        assertArgumentNotNull("text", text);
        assertArgumentNotNull("textType", textType);
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
        return getPersonalEncoding();
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
    protected MimePart setupAttachmentPart(SMailPostingMessage message, SMailAttachment attachment) {
        assertArgumentNotNull("message", message);
        assertArgumentNotNull("attachment", attachment);
        final MimePart part = newMimeBodyPart();
        final OptionalThing<String> textEncoding = getAttachmentTextEncoding(attachment);
        final DataSource source = prepareAttachmentDataSource(message, attachment, textEncoding);
        final String contentType = buildAttachmentContentType(message, attachment, textEncoding);
        final String contentDisposition = buildAttachmentContentDisposition(message, attachment, textEncoding);
        try {
            part.setDataHandler(createDataHandler(source));
            part.setHeader("Content-Transfer-Encoding", "base64");
            part.addHeader("Content-Type", contentType);
            part.addHeader("Content-Disposition", contentDisposition);
        } catch (MessagingException e) {
            throw new SMailMessageSettingFailureException("Failed to set headers: " + attachment, e);
        }
        return part;
    }

    protected String buildAttachmentContentType(SMailPostingMessage message, SMailAttachment attachment,
            OptionalThing<String> textEncoding) {
        final String encodedFilename = getEncodedFilename(attachment.getFilenameOnHeader());
        final StringBuilder sb = new StringBuilder();
        final String contentType = attachment.getContentType();
        sb.append(contentType);
        if (contentType.equals("text/plain")) {
            sb.append("; charset=").append(textEncoding.get());
        }
        sb.append("; name=\"").append(encodedFilename).append("\"");
        return sb.toString();
    }

    protected String buildAttachmentContentDisposition(SMailPostingMessage message, SMailAttachment attachment,
            OptionalThing<String> textEncoding) {
        final String encodedFilename = getEncodedFilename(attachment.getFilenameOnHeader());
        final StringBuilder sb = new StringBuilder();
        sb.append("attachment; filename=\"").append(encodedFilename).append("\"");
        return sb.toString();
    }

    protected String getEncodedFilename(String filename) {
        final String filenameEncoding = getAttachmentFilenameEncoding();
        final String encodedFilename;
        try {
            encodedFilename = MimeUtility.encodeText(filename, filenameEncoding, "B"); // uses 'B' for various characters
        } catch (UnsupportedEncodingException e) {
            throw new SMailMessageSettingFailureException("Unknown encoding: " + filenameEncoding, e);
        }
        return encodedFilename;
    }

    protected DataSource prepareAttachmentDataSource(SMailPostingMessage message, SMailAttachment attachment,
            OptionalThing<String> textEncoding) {
        final byte[] attachedBytes = readAttachedBytes(message, attachment);
        message.saveAttachmentForDisplay(attachment, attachedBytes, textEncoding);
        return new ByteArrayDataSource(attachedBytes, "application/octet-stream");
    }

    protected byte[] readAttachedBytes(SMailPostingMessage message, SMailAttachment attachment) {
        final InputStream ins = attachment.getReourceStream();
        AccessibleByteArrayOutputStream ous = null;
        try {
            ous = new AccessibleByteArrayOutputStream();
            final byte[] buffer = new byte[8192];
            int length;
            while ((length = ins.read(buffer)) > 0) {
                ous.write(buffer, 0, length);
            }
            return ous.getBytes();
        } catch (IOException e) {
            throw new SMailIllegalStateException("Failed to read the attached stream as bytes: " + attachment);
        } finally {
            if (ous != null) {
                try {
                    ous.close();
                } catch (IOException ignored) {}
            }
            try {
                ins.close();
            } catch (IOException ignored) {}
        }
    }

    protected static class AccessibleByteArrayOutputStream extends ByteArrayOutputStream {
        public byte[] getBytes() {
            return buf;
        }
    }

    protected OptionalThing<String> getAttachmentTextEncoding(SMailAttachment attachment) {
        return attachment.getTextEncoding(); // always exists if text/plain
    }

    protected String getAttachmentFilenameEncoding() {
        return getPersonalEncoding();
    }

    // ===================================================================================
    //                                                                 Prepare Async/Retry
    //                                                                 ===================
    protected void prepareAsync(Postcard postcard) {
        if (asyncStrategy.alwaysAsync(postcard) && !postcard.isAsync()) {
            logger.debug("...Calling async() automatically by strategy: {}", asyncStrategy);
            postcard.async();
        }
    }

    protected void prepareRetry(Postcard postcard) {
        retryStrategy.retry(postcard, (retryCount, intervalMillis) -> {
            if (postcard.getRetryCount() == 0) {
                logger.debug("...Calling retry({}, {}) automatically by strategy: {}", retryCount, intervalMillis, asyncStrategy);
                postcard.retry(retryCount, intervalMillis);
            }
        });
    }

    // ===================================================================================
    //                                                                            Disclose
    //                                                                            ========
    protected void disclosePostingState(Postcard postcard, SMailPostingMessage message) {
        postcard.officeDisclosePostingState(message);
    }

    // ===================================================================================
    //                                                                        Send Message
    //                                                                        ============
    protected void send(Postcard postcard, SMailPostingMessage message) {
        if (needsAsync(postcard)) {
            asyncStrategy.async(postcard, () -> doSend(postcard, message));
        } else {
            doSend(postcard, message);
        }
    }

    protected boolean needsAsync(Postcard postcard) {
        return postcard.isAsync();
    }

    // -----------------------------------------------------
    //                                          with Logging
    //                                          ------------
    protected void doSend(Postcard postcard, SMailPostingMessage message) {
        logMailBefore(postcard, message);
        RuntimeException cause = null;
        try {
            if (!training) {
                retryableSend(postcard, message);
            }
        } catch (RuntimeException e) {
            cause = e;
            if (postcard.isSuppressSendFailure()) {
                logSuppressedCause(postcard, message, e);
            } else {
                throw e;
            }
        } finally {
            logMailFinally(postcard, message, cause);
        }
    }

    protected void logMailBefore(Postcard postcard, SMailPostingMessage message) {
        loggingStrategy.logMailBefore(postcard, message); // you can also make EML file here by overriding
    }

    protected void logSuppressedCause(Postcard postcard, SMailPostingMessage message, RuntimeException e) {
        loggingStrategy.logSuppressedCause(postcard, message, e);
    }

    protected void logMailFinally(Postcard postcard, SMailPostingMessage message, RuntimeException cause) {
        loggingStrategy.logMailFinally(postcard, message, OptionalThing.ofNullable(cause, () -> {
            throw new IllegalStateException("Not found the exception for the mail finally: " + postcard);
        }));
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
                    logRetrySuccess(postcard, message, challengeCount, firstCause);
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
        final Transport transport = motorbike.getNativeSession().getTransport();
        final MimeMessage mimeMessage = message.getMimeMessage();
        transport.connect(); // authenticated by session's authenticator
        transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
        message.acceptSentTransport(transport); // keep e.g. last return code
    }

    protected void logRetrySuccess(Postcard postcard, SMailPostingMessage message, int challengeCount, Exception firstCause) {
        loggingStrategy.logRetrySuccess(postcard, message, challengeCount, firstCause);
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
