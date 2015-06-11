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

import org.dbflute.mail.Postcard;
import org.dbflute.mail.send.SMailPostalMotorbike;
import org.dbflute.mail.send.SMailPostie;
import org.dbflute.mail.send.exception.SMailTransportFailureException;
import org.dbflute.mail.send.supplement.SMailAddressFilter;
import org.dbflute.mail.send.supplement.SMailAddressFilterNone;
import org.dbflute.mail.send.supplement.SMailAttachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 0.4.0 (2015/05/05 Tuesday)
 */
public class SMailSimplePostie implements SMailPostie {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger logger = LoggerFactory.getLogger(SMailSimplePostie.class);
    private static final SMailAddressFilterNone noneAddressFilter = new SMailAddressFilterNone();

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final SMailPostalMotorbike motorbike; // not null
    protected SMailAddressFilter addressFilter = noneAddressFilter; // not null
    protected boolean training;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SMailSimplePostie(SMailPostalMotorbike motorbike) {
        assertArgumentNotNull("motorbike", motorbike);
        this.motorbike = motorbike;
    }

    public SMailSimplePostie withAddressFilter(SMailAddressFilter addressFilter) {
        assertArgumentNotNull("addressFilter", addressFilter);
        this.addressFilter = addressFilter;
        return this;
    }

    public SMailSimplePostie asTraining() {
        training = true;
        return this;
    }

    // ===================================================================================
    //                                                                             Deliver
    //                                                                             =======
    @Override
    public void deliver(Postcard postcard) {
        // TODO jflute mailflute: [C] postie's retry
        // TODO jflute mailflute: [C] postie's async
        final SMailPostingMessage message = createMailMessage(motorbike);
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

    protected void send(Postcard postcard, SMailPostingMessage message) {
        // TODO jflute mailflute: [B] mail logging, eml file (2015/05/11)
        if (logger.isDebugEnabled()) {
            final String state = training ? "as training" : "actually";
            final String disp = message.toDisplay();
            logger.debug("...Sending mail message {}:\n{}", state, disp);
        }
        if (!training) {
            try {
                Transport.send(message.getMimeMessage());
            } catch (MessagingException e) {
                throw new SMailTransportFailureException("Failed to send mail: " + postcard, e);
            }
        }
    }

    // ===================================================================================
    //                                                                     Prepare Address
    //                                                                     ===============
    protected void prepareAddress(Postcard postcard, SMailPostingMessage message) {
        final Address fromAddress = postcard.getFrom();
        if (fromAddress == null) {
            throw new IllegalStateException("Not found the from address in the postcard: " + postcard);
        }
        message.setFrom(addressFilter.filterFrom(postcard, fromAddress));
        for (Address to : postcard.getToList()) {
            message.addTo(addressFilter.filterTo(postcard, to));
        }
        for (Address cc : postcard.getCcList()) {
            message.addCc(addressFilter.filterCc(postcard, cc));
        }
        for (Address bcc : postcard.getBccList()) {
            message.addBcc(addressFilter.filterBcc(postcard, bcc));
        }
    }

    // ===================================================================================
    //                                                                     Prepare Subject
    //                                                                     ===============
    protected void prepareSubject(Postcard postcard, SMailPostingMessage message) {
        message.setSubject(postcard.getSubject(), getSubjectEncoding());
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
                final MimeMultipart multipart = createTextWithAttachmentMultipart(plainText, attachmentMap);
                nativeMessage.setContent(multipart);
            } catch (MessagingException e) {
                String msg = "Failed to set attachment multipart content: " + postcard;
                throw new IllegalStateException(msg, e);
            }
        }
    }

    protected MimeMultipart createTextWithAttachmentMultipart(String plain, Map<String, SMailAttachment> attachmentMap)
            throws MessagingException {
        final MimeMultipart multipart = newMimeMultipart();
        multipart.setSubType("mixed");
        multipart.addBodyPart((BodyPart) setupTextPart(newMimeBodyPart(), plain, TextType.PLAIN));
        for (Entry<String, SMailAttachment> entry : attachmentMap.entrySet()) {
            final SMailAttachment attachment = entry.getValue();
            multipart.addBodyPart((BodyPart) setupAttachmentPart(attachment));
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
            throw new IllegalStateException("Failed to set headers: " + encoding, e);
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
            throw new IllegalStateException("Unknown encoding: " + encoding, e);
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
            throw new IllegalStateException("Failed to set headers: " + attachment, e);
        }
        return part;
    }

    protected DataSource prepareAttachmentDataSource(SMailAttachment attachment) {
        final DataSource source;
        try {
            source = new ByteArrayDataSource(attachment.getReourceStream(), "application/octet-stream");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create data source: " + attachment, e);
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
            throw new IllegalStateException("Unknown encoding: " + filenameEncoding, e);
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
