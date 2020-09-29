/*
 * Copyright 2015-2018 the original author or authors.
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
import javax.mail.NoSuchProviderException;
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
import org.dbflute.mail.CardView;
import org.dbflute.mail.Postcard;
import org.dbflute.mail.send.SMailAddress;
import org.dbflute.mail.send.SMailPostalMotorbike;
import org.dbflute.mail.send.SMailPostie;
import org.dbflute.mail.send.exception.SMailIllegalStateException;
import org.dbflute.mail.send.exception.SMailMessageSettingFailureException;
import org.dbflute.mail.send.exception.SMailTransportFailureException;
import org.dbflute.mail.send.hook.SMailCallbackContext;
import org.dbflute.mail.send.hook.SMailPreparedMessageHook;
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
import org.dbflute.mail.send.supplement.header.SMailMailHeaderStrategy;
import org.dbflute.mail.send.supplement.header.SMailMailHeaderStrategyNone;
import org.dbflute.mail.send.supplement.inetaddr.SMailInternetAddressCreator;
import org.dbflute.mail.send.supplement.inetaddr.SMailNormalInternetAddressCreator;
import org.dbflute.mail.send.supplement.label.SMailLabelStrategy;
import org.dbflute.mail.send.supplement.label.SMailLabelStrategyNone;
import org.dbflute.mail.send.supplement.logging.SMailLoggingStrategy;
import org.dbflute.mail.send.supplement.logging.SMailTypicalLoggingStrategy;
import org.dbflute.mail.send.supplement.retry.SMailRetryStrategy;
import org.dbflute.mail.send.supplement.retry.SMailRetryStrategyNone;
import org.dbflute.optional.OptionalThing;
import org.dbflute.system.DBFluteSystem;
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
    private static final SMailMailHeaderStrategy noneMailHeaderStrategy = new SMailMailHeaderStrategyNone();
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
    protected SMailMailHeaderStrategy mailHeaderStrategy = noneMailHeaderStrategy; // not null
    protected SMailInternetAddressCreator internetAddressCreator = normalInternetAddressCreator; // not null
    protected boolean training;
    protected OptionalThing<String> textTransferEncoding = OptionalThing.empty();

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

    public SMailHonestPostie withMailHeaderStrategy(SMailMailHeaderStrategy mailHeaderStrategy) {
        assertArgumentNotNull("mailHeaderStrategy", mailHeaderStrategy);
        this.mailHeaderStrategy = mailHeaderStrategy;
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
        hookPreparedMessage(postcard, message);
        if (postcard.isDryrun()) {
            logger.debug("*dryrun: postcard={}", postcard); // normal logging here
            return;
        }
        send(postcard, message);
    }

    protected SMailPostingMessage createMailMessage(CardView view) {
        final MimeMessage mimeMessage = createMimeMessage(view, extractNativeSession(view, motorbike));
        final Map<String, Object> pushedLoggingMap = view.getPushedLoggingMap();
        final Map<String, Map<String, Object>> officeManagedLoggingMap = view.getOfficeManagedLoggingMap();
        return new SMailPostingMessage(mimeMessage, motorbike, training, pushedLoggingMap, officeManagedLoggingMap);
    }

    protected Session extractNativeSession(CardView view, SMailPostalMotorbike motorbike) {
        return motorbike.getNativeSession();
    }

    protected MimeMessage createMimeMessage(CardView view, Session session) {
        return new MimeMessage(session);
    }

    protected boolean isCancel(CardView view) {
        return cancelFilter.isCancel(view);
    }

    // ===================================================================================
    //                                                                     Prepare Address
    //                                                                     ===============
    protected void prepareAddress(CardView view, SMailPostingMessage message) {
        final SMailAddress from = view.getFrom().orElseThrow(() -> { /* already checked, but just in case */
            return new SMailIllegalStateException("Not found the from address in the postcard: " + view);
        });
        final Address filteredFrom = addressFilter.filterFrom(view, toInternetAddress(view, from));
        message.setFrom(verifyFilteredFromAddress(view, filteredFrom));
        boolean existsToAddress = false;
        for (SMailAddress to : view.getToList()) {
            final OptionalThing<Address> opt = addressFilter.filterTo(view, toInternetAddress(view, to));
            verifyFilteredOptionalAddress(view, opt).ifPresent(address -> message.addTo(address));
            if (opt.isPresent()) {
                existsToAddress = true;
            }
        }
        verifyFilteredToAddressExists(view, existsToAddress);
        for (SMailAddress cc : view.getCcList()) {
            final OptionalThing<Address> opt = addressFilter.filterCc(view, toInternetAddress(view, cc));
            verifyFilteredOptionalAddress(view, opt).ifPresent(address -> message.addCc(address));
        }
        for (SMailAddress bcc : view.getBccList()) {
            final OptionalThing<Address> opt = addressFilter.filterBcc(view, toInternetAddress(view, bcc));
            verifyFilteredOptionalAddress(view, opt).ifPresent(address -> message.addBcc(address));
        }
        final List<SMailAddress> replyToList = view.getReplyToList();
        if (!replyToList.isEmpty()) {
            final List<Address> filteredList = new ArrayList<Address>(replyToList.size());
            for (SMailAddress replyTo : replyToList) {
                final OptionalThing<Address> opt = addressFilter.filterReplyTo(view, toInternetAddress(view, replyTo));
                verifyFilteredOptionalAddress(view, opt).ifPresent(address -> filteredList.add(address));
            }
            message.setReplyTo(filteredList);
        }
    }

    // -----------------------------------------------------
    //                                        Label Handling
    //                                        --------------
    protected Address toInternetAddress(CardView view, SMailAddress address) {
        return createAddress(view, address);
    }

    protected Address createAddress(CardView view, SMailAddress address) {
        final InternetAddress internetAddress;
        try {
            internetAddress = createInternetAddress(view, address.getAddress(), isStrictAddress());
        } catch (AddressException e) {
            throw new IllegalStateException("Failed to create internet address: " + address, e);
        }
        address.getPersonal().ifPresent(personal -> {
            final String encoding = getPersonalEncoding();
            try {
                final Locale locale = view.getReceiverLocale().orElseGet(() -> getDefaultReceiverLocale());
                final String resolved = labelStrategy.resolveLabel(view, locale, personal);
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

    protected InternetAddress createInternetAddress(CardView view, String address, boolean strict) throws AddressException {
        return internetAddressCreator.create(view, address, strict);
    }

    protected String getPersonalEncoding() {
        return getBasicEncoding();
    }

    protected Locale getDefaultReceiverLocale() {
        return DBFluteSystem.getFinalLocale();
    }

    // -----------------------------------------------------
    //                                        Verify Address
    //                                        --------------
    protected Address verifyFilteredFromAddress(CardView view, Address filteredFrom) {
        if (filteredFrom == null) {
            String msg = "The filtered from-address should not be null: postcard=" + view;
            throw new SMailIllegalStateException(msg);
        }
        return filteredFrom;
    }

    protected OptionalThing<Address> verifyFilteredOptionalAddress(CardView view, OptionalThing<Address> opt) {
        if (opt == null) {
            String msg = "The filtered optional should not be null: postcard=" + view;
            throw new SMailIllegalStateException(msg);
        }
        return opt;
    }

    protected void verifyFilteredToAddressExists(CardView view, boolean existsToAddress) {
        if (!existsToAddress) {
            String msg = "Empty to-address by filtering: specifiedToAddress=" + view.getToList();
            throw new SMailIllegalStateException(msg);
        }
    }

    // ===================================================================================
    //                                                                     Prepare Subject
    //                                                                     ===============
    protected void prepareSubject(CardView view, SMailPostingMessage message) {
        message.setSubject(getSubject(view), getSubjectEncoding());
    }

    protected String getSubject(CardView view) {
        return subjectFilter.filterSubject(view, view.getSubject().get());
    }

    protected String getSubjectEncoding() {
        return getBasicEncoding();
    }

    // ===================================================================================
    //                                                                        Prepare Body
    //                                                                        ============
    protected void prepareBody(CardView view, SMailPostingMessage message) {
        final String plainText = toCompletePlainText(view);
        final OptionalThing<String> optHtmlText = toCompleteHtmlText(view);
        message.savePlainTextForDisplay(plainText);
        message.saveHtmlTextForDisplay(optHtmlText);
        final Map<String, SMailAttachment> attachmentMap = view.getAttachmentMap();
        final MimeMessage nativeMessage = message.getMimeMessage();
        if (attachmentMap.isEmpty()) { // normally here
            setupTextPart(view, nativeMessage, plainText, TextType.PLAIN); // plain is required
            optHtmlText.ifPresent(htmlText -> {
                setupTextPart(view, nativeMessage, htmlText, TextType.HTML);
            });
        } else { // with attachment
            if (optHtmlText.isPresent()) {
                throw new SMailIllegalStateException("Unsupported HTML mail with attachment for now: postcard=" + view);
            }
            try {
                final MimeMultipart multipart = createTextWithAttachmentMultipart(view, message, plainText, attachmentMap);
                nativeMessage.setContent(multipart);
            } catch (MessagingException e) {
                String msg = "Failed to set attachment multipart content: postcard=" + view;
                throw new SMailIllegalStateException(msg, e);
            }
        }
    }

    protected String toCompletePlainText(CardView view) {
        return view.toCompletePlainText().map(plainText -> {
            return bodyTextFilter.filterBody(view, plainText, /*html*/false);
        }).get();
    }

    protected OptionalThing<String> toCompleteHtmlText(CardView view) {
        return view.toCompleteHtmlText().map(htmlText -> {
            return bodyTextFilter.filterBody(view, htmlText, /*html*/true);
        });
    }

    protected MimeMultipart createTextWithAttachmentMultipart(CardView view, SMailPostingMessage message, String plain,
            Map<String, SMailAttachment> attachmentMap) throws MessagingException {
        final MimeMultipart multipart = newMimeMultipart();
        multipart.setSubType("mixed");
        multipart.addBodyPart((BodyPart) setupTextPart(view, newMimeBodyPart(), plain, TextType.PLAIN));
        for (Entry<String, SMailAttachment> entry : attachmentMap.entrySet()) {
            final SMailAttachment attachment = entry.getValue();
            multipart.addBodyPart((BodyPart) setupAttachmentPart(view, message, attachment));
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
    protected MimePart setupTextPart(CardView view, MimePart part, String text, TextType textType) {
        assertArgumentNotNull("view", view);
        assertArgumentNotNull("part", part);
        assertArgumentNotNull("text", text);
        assertArgumentNotNull("textType", textType);
        final String textEncoding = getTextEncoding(view);
        final ByteBuffer buffer = prepareTextByteBuffer(view, text, textEncoding);
        final DataSource source = prepareTextDataSource(view, buffer);
        try {
            part.setDataHandler(createDataHandler(source));
            if (!isSuppressTextTransferEncoding(view)) {
                part.setHeader("Content-Transfer-Encoding", getTextTransferEncoding(view));
            }
            part.setHeader("Content-Type", buildTextContentType(view, textType, textEncoding));
        } catch (MessagingException e) {
            throw new SMailMessageSettingFailureException("Failed to set headers: postcard=" + view, e);
        }
        return part;
    }

    protected String getTextEncoding(CardView view) {
        return mailHeaderStrategy.getTextEncoding(view).orElseGet(() -> getBasicEncoding());
    }

    protected ByteBuffer prepareTextByteBuffer(CardView view, String text, String encoding) {
        final ByteBuffer buffer;
        try {
            buffer = ByteBuffer.wrap(text.getBytes(encoding));
        } catch (UnsupportedEncodingException e) {
            throw new SMailMessageSettingFailureException("Unknown encoding: " + encoding, e);
        }
        return buffer;
    }

    protected ByteArrayDataSource prepareTextDataSource(CardView view, ByteBuffer buffer) {
        return new ByteArrayDataSource(buffer.array(), getTextMimeType(view));
    }

    protected String getTextMimeType(CardView view) {
        return mailHeaderStrategy.getTextMimeType(view).orElseGet(() -> {
            return "application/octet-stream"; // as default of MailFlute
        });
    }

    protected boolean isSuppressTextTransferEncoding(CardView view) {
        return mailHeaderStrategy.isSuppressTextTransferEncoding();
    }

    protected String getTextTransferEncoding(CardView view) {
        return mailHeaderStrategy.getTextTransferEncoding(view).orElseGet(() -> {
            return "base64"; // as default of MailFlute (for UTF-8/base64)
        });
    }

    protected String buildTextContentType(CardView view, TextType textType, String encoding) {
        return "text/" + textType.code() + "; charset=\"" + encoding + "\"";
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
    protected MimePart setupAttachmentPart(CardView view, SMailPostingMessage message, SMailAttachment attachment) {
        assertArgumentNotNull("view", view);
        assertArgumentNotNull("message", message);
        assertArgumentNotNull("attachment", attachment);
        final MimePart part = newMimeBodyPart();
        final OptionalThing<String> textEncoding = getAttachmentTextEncoding(view, attachment);
        final DataSource source = prepareAttachmentDataSource(view, message, attachment, textEncoding);
        final String contentType = buildAttachmentContentType(view, attachment, textEncoding);
        final String contentDisposition = buildAttachmentContentDisposition(view, attachment, textEncoding);
        try {
            part.setDataHandler(createDataHandler(source));
            if (!isSuppressAttachmentTransferEncoding(view)) {
                part.setHeader("Content-Transfer-Encoding", getAttachmentTransferEncoding(view));
            }
            part.setHeader("Content-Type", contentType);
            part.setHeader("Content-Disposition", contentDisposition);
        } catch (MessagingException e) {
            String msg = "Failed to set headers: " + attachment;
            throw new SMailMessageSettingFailureException(msg, e);
        }
        return part;
    }

    protected OptionalThing<String> getAttachmentTextEncoding(CardView view, SMailAttachment attachment) {
        return attachment.getTextEncoding(); // always exists if text/plain
    }

    protected DataSource prepareAttachmentDataSource(CardView view, SMailPostingMessage message, SMailAttachment attachment,
            OptionalThing<String> textEncoding) {
        final byte[] attachedBytes = readAttachedBytes(view, attachment);
        message.saveAttachmentForDisplay(attachment, attachedBytes, textEncoding);
        return new ByteArrayDataSource(attachedBytes, getAttachmentMimeType(view));
    }

    protected byte[] readAttachedBytes(CardView view, SMailAttachment attachment) {
        final InputStream ins = attachment.getReourceStream();
        ByteArrayOutputStream ous = null;
        try {
            ous = new ByteArrayOutputStream();
            final byte[] buffer = new byte[8192];
            int length;
            while ((length = ins.read(buffer)) > 0) {
                ous.write(buffer, 0, length);
            }
            return ous.toByteArray();
        } catch (IOException e) {
            String msg = "Failed to read the attached stream as bytes: " + attachment;
            throw new SMailIllegalStateException(msg, e);
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

    protected String getAttachmentMimeType(CardView view) {
        return mailHeaderStrategy.getAttachmentMimeType(view).orElseGet(() -> {
            return "application/octet-stream"; // as default of MailFlute
        });
    }

    protected String buildAttachmentContentType(CardView view, SMailAttachment attachment, OptionalThing<String> textEncoding) {
        final String encodedFilename = getEncodedFilename(view, attachment.getFilenameOnHeader());
        final StringBuilder sb = new StringBuilder();
        final String contentType = attachment.getContentType();
        sb.append(contentType);
        if (contentType.equals("text/plain")) {
            sb.append("; charset=").append(textEncoding.get());
        }
        sb.append("; name=\"").append(encodedFilename).append("\"");
        return sb.toString();
    }

    protected String buildAttachmentContentDisposition(CardView view, SMailAttachment attachment, OptionalThing<String> textEncoding) {
        final String encodedFilename = getEncodedFilename(view, attachment.getFilenameOnHeader());
        final StringBuilder sb = new StringBuilder();
        sb.append("attachment; filename=\"").append(encodedFilename).append("\"");
        return sb.toString();
    }

    protected String getEncodedFilename(CardView view, String filename) {
        final String filenameEncoding = getAttachmentFilenameEncoding(view);
        final String encodedFilename;
        try {
            encodedFilename = MimeUtility.encodeText(filename, filenameEncoding, "B"); // uses 'B' for various characters
        } catch (UnsupportedEncodingException e) {
            throw new SMailMessageSettingFailureException("Unknown encoding: " + filenameEncoding, e);
        }
        return encodedFilename;
    }

    protected String getAttachmentFilenameEncoding(CardView view) {
        return getBasicEncoding();
    }

    protected boolean isSuppressAttachmentTransferEncoding(CardView view) {
        return mailHeaderStrategy.isSuppressAttachmentTransferEncoding();
    }

    protected String getAttachmentTransferEncoding(CardView view) {
        return mailHeaderStrategy.getAttachmentTransferEncoding(view).orElseGet(() -> {
            return "base64"; // as default of MailFlute (no change from the beginning)
        });
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
    //                                                                    Callback Context
    //                                                                    ================
    protected void hookPreparedMessage(Postcard postcard, final SMailPostingMessage message) {
        if (SMailCallbackContext.isExistPreparedMessageHookOnThread()) {
            final SMailCallbackContext context = SMailCallbackContext.getCallbackContextOnThread();
            final SMailPreparedMessageHook hook = context.getPreparedMessageHook();
            hook.hookPreparedMessage(postcard, message);
        }
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
        return postcard.isAsync() && !postcard.isDefinitelySync();
    }

    // -----------------------------------------------------
    //                                          with Logging
    //                                          ------------
    protected void doSend(Postcard postcard, SMailPostingMessage message) {
        logMailBefore(postcard, message);
        RuntimeException cause = null;
        try {
            retryableSend(postcard, message);
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
                stagingSend(postcard, message);
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

    // -----------------------------------------------------
    //                                               Staging
    //                                               -------
    // you can override this to switch sender to e.g. remote api
    protected void stagingSend(Postcard postcard, SMailPostingMessage message) throws MessagingException {
        if (!training) {
            actuallySend(message);
        }
    }

    // -----------------------------------------------------
    //                                              Actually
    //                                              --------
    protected void actuallySend(SMailPostingMessage message) throws MessagingException {
        final Transport transport = prepareTransport();
        try {
            final MimeMessage mimeMessage = message.getMimeMessage();
            transport.connect(); // authenticated by session's authenticator
            transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
            message.acceptSentTransport(transport); // keep e.g. last return code
        } finally {
            closeTransport(transport);
        }
    }

    protected Transport prepareTransport() throws NoSuchProviderException {
        return motorbike.getNativeSession().getTransport();
    }

    protected void closeTransport(Transport transport) {
        try {
            transport.close();
        } catch (MessagingException continued) {
            logger.warn("Failed to close the transport: " + transport, continued);
        }
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    protected String getBasicEncoding() {
        return "UTF-8"; // as default of MailFlute
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
