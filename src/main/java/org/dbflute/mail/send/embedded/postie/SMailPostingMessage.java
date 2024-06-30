/*
 * Copyright 2015-2024 the original author or authors.
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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import org.dbflute.helper.filesystem.FileTextIO;
import org.dbflute.mail.PostOffice;
import org.dbflute.mail.send.SMailPostalMotorbike;
import org.dbflute.mail.send.exception.SMailIllegalStateException;
import org.dbflute.mail.send.exception.SMailMessageSettingFailureException;
import org.dbflute.mail.send.supplement.SMailPostingDiscloser;
import org.dbflute.mail.send.supplement.attachment.SMailAttachment;
import org.dbflute.mail.send.supplement.attachment.SMailReadAttachedData;
import org.dbflute.optional.OptionalThing;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @author Takeshi Kato
 * @since 0.1.0 (2015/01/20 Tuesday)
 */
public class SMailPostingMessage implements SMailPostingDiscloser {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger logger = LoggerFactory.getLogger(SMailPostingMessage.class);
    protected static final String LF = "\n";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final MimeMessage message;
    protected final SMailPostalMotorbike motorbike;
    protected final boolean training;
    protected final Map<String, Object> pushedLoggingMap;
    protected final Map<String, Map<String, Object>> officeManagedLoggingMap;

    // -----------------------------------------------------
    //                                     Saved for Display
    //                                     -----------------
    protected String subject;
    protected Address from;
    protected List<Address> toList;
    protected List<Address> ccList;
    protected List<Address> bccList;
    protected List<Address> replyToList;
    protected String plainText;
    protected OptionalThing<String> optHtmlText = OptionalThing.empty();
    protected Map<String, SMailReadAttachedData> attachmentMap; // keyed by filenameOnHeader

    // -----------------------------------------------------
    //                                    Finished Transport
    //                                    ------------------
    protected Integer lastReturnCode;
    protected String lastServerResponse;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SMailPostingMessage(MimeMessage message, SMailPostalMotorbike motorbike, boolean training, Map<String, Object> pushedLoggingMap,
            Map<String, Map<String, Object>> officeManagedLoggingMap) {
        assertArgumentNotNull("message", message);
        assertArgumentNotNull("motorbike", motorbike);
        assertArgumentNotNull("pushedLoggingMap", pushedLoggingMap);
        assertArgumentNotNull("officeManagedLoggingMap", officeManagedLoggingMap);
        this.message = message;
        this.motorbike = motorbike;
        this.training = training;
        this.pushedLoggingMap = pushedLoggingMap;
        this.officeManagedLoggingMap = officeManagedLoggingMap;
    }

    // ===================================================================================
    //                                                                            Settings
    //                                                                            ========
    // -----------------------------------------------------
    //                                          From Address
    //                                          ------------
    public void setFrom(Address address) {
        assertArgumentNotNull("address", address);
        from = address;
        try {
            message.setFrom(address);
        } catch (MessagingException e) {
            String msg = buildAddressSettingFailureMessage("from", address);
            throw new SMailMessageSettingFailureException(msg, e);
        }
    }

    // -----------------------------------------------------
    //                                            To Address
    //                                            ----------
    public void addTo(Address address) {
        assertArgumentNotNull("address", address);
        saveTo(address);
        try {
            message.addRecipient(RecipientType.TO, address);
        } catch (MessagingException e) {
            String msg = buildAddressSettingFailureMessage("to", address);
            throw new SMailMessageSettingFailureException(msg, e);
        }
    }

    protected void saveTo(Address address) {
        if (toList == null) {
            toList = new ArrayList<Address>(2);
        }
        toList.add(address);
    }

    // -----------------------------------------------------
    //                                            Cc Address
    //                                            ----------
    public void addCc(Address address) {
        assertArgumentNotNull("address", address);
        saveCc(address);
        try {
            message.addRecipient(RecipientType.CC, address);
        } catch (MessagingException e) {
            String msg = buildAddressSettingFailureMessage("cc", address);
            throw new SMailMessageSettingFailureException(msg, e);
        }
    }

    protected void saveCc(Address address) {
        if (ccList == null) {
            ccList = new ArrayList<Address>(2);
        }
        ccList.add(address);
    }

    // -----------------------------------------------------
    //                                           Bcc Address
    //                                           -----------
    public void addBcc(Address address) {
        assertArgumentNotNull("address", address);
        saveBcc(address);
        try {
            message.addRecipient(RecipientType.BCC, address);
        } catch (MessagingException e) {
            String msg = buildAddressSettingFailureMessage("bcc", address);
            throw new SMailMessageSettingFailureException(msg, e);
        }
    }

    protected void saveBcc(Address address) {
        if (bccList == null) {
            bccList = new ArrayList<Address>(2);
        }
        bccList.add(address);
    }

    // -----------------------------------------------------
    //                                       ReplyTo Address
    //                                       ---------------
    public void setReplyTo(List<Address> addressList) {
        assertArgumentNotNull("addressList", addressList);
        saveReplyTo(addressList);
        try {
            message.setReplyTo(addressList.toArray(new Address[addressList.size()]));
        } catch (MessagingException e) {
            String msg = buildAddressSettingFailureMessage("reply-to", addressList);
            throw new SMailMessageSettingFailureException(msg, e);
        }
    }

    protected void saveReplyTo(List<Address> addressList) {
        replyToList = addressList;
    }

    // -----------------------------------------------------
    //                                                 Body
    //                                                ------
    public void setSubject(String subject, String encoding) {
        assertArgumentNotNull("encoding", encoding);
        saveSubject(subject);
        try {
            message.setSubject(subject, encoding);
        } catch (MessagingException e) {
            String msg = "Failed to set subject: " + subject + " message=" + message;
            throw new SMailMessageSettingFailureException(msg, e);
        }
    }

    protected void saveSubject(String subject) {
        assertArgumentNotNull("subject", subject);
        this.subject = subject;
    }

    // *text setting is very complex so not here
    // so public is saving methods of text

    public void savePlainTextForDisplay(String plainText) {
        assertArgumentNotNull("plainText", plainText);
        this.plainText = plainText;
    }

    public void saveHtmlTextForDisplay(OptionalThing<String> optHtmlText) {
        assertArgumentNotNull("optHtmlText", optHtmlText);
        this.optHtmlText = optHtmlText;
    }

    public void saveAttachmentForDisplay(SMailAttachment attachment, byte[] attachedBytes, OptionalThing<String> textEncoding) {
        assertArgumentNotNull("attachment", attachment);
        assertArgumentNotNull("attachedBytes", attachedBytes);
        assertArgumentNotNull("textEncoding", textEncoding);
        if (attachmentMap == null) {
            attachmentMap = new LinkedHashMap<String, SMailReadAttachedData>(2);
        }
        final String filenameOnHeader = attachment.getFilenameOnHeader();
        final String contentType = attachment.getContentType();
        final SMailReadAttachedData attachedData = newMailReadAttachedData(filenameOnHeader, contentType, attachedBytes, textEncoding);
        attachmentMap.put(filenameOnHeader, attachedData);
    }

    protected SMailReadAttachedData newMailReadAttachedData(String filenameOnHeader, String contentType, byte[] attachedBytes,
            OptionalThing<String> textEncoding) {
        return new SMailReadAttachedData(filenameOnHeader, contentType, attachedBytes, textEncoding);
    }

    // -----------------------------------------------------
    //                                          Assist Logic
    //                                          ------------
    protected String buildAddressSettingFailureMessage(String title, Address address) {
        return "Failed to set '" + title + "' address: " + address + " message=" + message;
    }

    protected String buildAddressSettingFailureMessage(String title, List<Address> addressList) {
        return "Failed to set '" + title + "' addresses: " + addressList + " message=" + message;
    }

    // ===================================================================================
    //                                                                  Finished Transport
    //                                                                  ==================
    public void acceptSentTransport(Transport transport) {
        if (transport instanceof com.sun.mail.smtp.SMTPTransport) {
            final com.sun.mail.smtp.SMTPTransport smtp = (com.sun.mail.smtp.SMTPTransport) transport;
            lastReturnCode = smtp.getLastReturnCode();
            lastServerResponse = smtp.getLastServerResponse();
        }
    }

    // ===================================================================================
    //                                                                  Display Expression
    //                                                                  ==================
    @Override
    public String toDisplay() {
        final StringBuilder sb = new StringBuilder();
        sb.append("/= = = = = = = = = = = = = = = = = = = = = = = = = = Mail Message");
        sb.append(LF).append("subject: " + subject);
        sb.append(LF).append("   from: " + from);
        if (toList != null && !toList.isEmpty()) {
            sb.append(LF).append("     to: " + (toList.size() == 1 ? toList.get(0) : toList));
        }
        if (ccList != null && !ccList.isEmpty()) {
            sb.append(LF).append("     cc: " + (ccList.size() == 1 ? ccList.get(0) : ccList));
        }
        if (bccList != null && !bccList.isEmpty()) {
            sb.append(LF).append("    bcc: " + (bccList.size() == 1 ? bccList.get(0) : bccList));
        }
        if (replyToList != null && !replyToList.isEmpty()) {
            sb.append(LF).append("  reply: " + (replyToList.size() == 1 ? replyToList.get(0) : replyToList));
        }
        motorbike.getReturnPath().ifPresent(returnPath -> {
            sb.append(LF).append(" return: " + returnPath);
        });
        if (officeManagedLoggingMap != null && !officeManagedLoggingMap.isEmpty()) {
            officeManagedLoggingMap.forEach((title, valueMap) -> {
                sb.append(LF).append(Srl.lfill(title, 7, ' ')).append(": ").append(valueMap);
            });
        }
        if (pushedLoggingMap != null && !pushedLoggingMap.isEmpty()) {
            sb.append(LF).append(Srl.lfill(PostOffice.LOGGING_TITLE_APPINFO, 7, ' ')).append(": ").append(pushedLoggingMap);
        }
        sb.append(LF).append(">>>");
        sb.append(LF).append(plainText);
        optHtmlText.ifPresent(htmlText -> {
            sb.append(LF).append(" - - - - - - - - - - (HTML)");
            sb.append(LF).append(htmlText);
        });
        if (attachmentMap != null && !attachmentMap.isEmpty()) {
            sb.append(LF).append(" - - - - - - - - - - (Attachment)");
            attachmentMap.forEach((filenameOnHeader, attachedData) -> {
                buildAttachmentDisplay(sb, filenameOnHeader, attachedData);
            });
        }
        sb.append(LF).append("= = = = = = = = = =/");
        return sb.toString();
    }

    protected void buildAttachmentDisplay(StringBuilder sb, String filenameOnHeader, SMailReadAttachedData attachedData) {
        final String contentType = attachedData.getContentType();
        sb.append(LF).append("*").append(filenameOnHeader).append(" (").append(contentType).append(")");
        if ("text/plain".equals(contentType)) {
            final String textEncoding = attachedData.getTextEncoding().get();
            final String attachedText;
            try {
                attachedText = new String(attachedData.getAttachedBytes(), textEncoding);
            } catch (UnsupportedEncodingException e) {
                throw new SMailIllegalStateException("Unknown encoding: " + textEncoding);
            }
            sb.append(":").append(LF).append(attachedText);
        }
    }

    // ===================================================================================
    //                                                                     Hash Expression
    //                                                                     ===============
    @Override
    public String toHash() {
        return Integer.toHexString(hashCode());
    }

    // ===================================================================================
    //                                                                       Make EML File
    //                                                                       =============
    @Override
    public void makeEmlFile(String path) {
        assertArgumentNotNull("path", path);
        ByteArrayOutputStream ous = null;
        try {
            ous = new ByteArrayOutputStream();
            message.writeTo(ous);
            final String eml = ous.toString();
            new FileTextIO().encodeAsUTF8().write(path, eml);
        } catch (IOException | MessagingException e) {
            logger.info("Failed to make EML file to the path: " + path + " subject=" + subject, e);
        } finally {
            if (ous != null) {
                try {
                    ous.close();
                } catch (IOException ignored) {}
            }
        }
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
        return "message:{" + message + ", " + subject + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    // -----------------------------------------------------
    //                                       Basic Attribute
    //                                       ---------------
    public MimeMessage getMimeMessage() {
        return message;
    }

    @Override
    public boolean isTraining() {
        return training;
    }

    public Map<String, Object> getPushedLoggingMap() {
        return pushedLoggingMap != null ? Collections.unmodifiableMap(pushedLoggingMap) : Collections.emptyMap();
    }

    public Map<String, Map<String, Object>> getOfficeManagedLoggingMap() {
        return officeManagedLoggingMap != null ? Collections.unmodifiableMap(officeManagedLoggingMap) : Collections.emptyMap();
    }

    // -----------------------------------------------------
    //                                     Saved for Display
    //                                     -----------------
    public OptionalThing<String> getSavedSubject() { // basically present after saving
        return OptionalThing.ofNullable(subject, () -> {
            throw new SMailIllegalStateException("Not found the subject: " + toString());
        });
    }

    public OptionalThing<Address> getSavedFrom() {
        return OptionalThing.ofNullable(from, () -> {
            throw new SMailIllegalStateException("Not found the from address: " + toString());
        });
    }

    public List<Address> getSavedToList() {
        return toList != null ? Collections.unmodifiableList(toList) : Collections.emptyList();
    }

    public List<Address> getSavedCcList() {
        return ccList != null ? Collections.unmodifiableList(ccList) : Collections.emptyList();
    }

    public List<Address> getSavedBccList() {
        return bccList != null ? Collections.unmodifiableList(bccList) : Collections.emptyList();
    }

    public List<Address> getSavedReplyToList() {
        return replyToList != null ? Collections.unmodifiableList(replyToList) : Collections.emptyList();
    }

    public OptionalThing<String> getSavedReturnPath() {
        return motorbike.getReturnPath();
    }

    public OptionalThing<String> getSavedPlainText() { // basically present after saving
        return OptionalThing.ofNullable(plainText, () -> {
            throw new SMailIllegalStateException("Not found the plain text: " + toString());
        });
    }

    public OptionalThing<String> getSavedHtmlText() { // formally empty-able
        return optHtmlText;
    }

    public Map<String, SMailReadAttachedData> getSavedAttachmentMap() {
        return attachmentMap != null ? Collections.unmodifiableMap(attachmentMap) : Collections.emptyMap();
    }

    // -----------------------------------------------------
    //                                    Finished Transport
    //                                    ------------------
    public OptionalThing<Integer> getLastReturnCode() {
        return OptionalThing.ofNullable(lastReturnCode, () -> {
            throw new SMailIllegalStateException("Not found the last return code: " + toString());
        });
    }

    public OptionalThing<String> getLastServerResponse() {
        return OptionalThing.ofNullable(lastServerResponse, () -> {
            throw new SMailIllegalStateException("Not found the last server response: " + toString());
        });
    }
}
