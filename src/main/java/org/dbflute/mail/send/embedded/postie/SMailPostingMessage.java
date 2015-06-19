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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.dbflute.helper.filesystem.FileTextIO;
import org.dbflute.mail.send.exception.SMailIllegalStateException;
import org.dbflute.mail.send.exception.SMailMessageSettingFailureException;
import org.dbflute.mail.send.supplement.SMailPostingDiscloser;
import org.dbflute.mail.send.supplement.attachment.SMailAttachment;
import org.dbflute.mail.send.supplement.attachment.SMailReadAttachedData;
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
    protected final boolean training;
    protected final Map<String, Object> pushedLoggingMap;

    // -----------------------------------------------------
    //                                     Saved for Display
    //                                     -----------------
    protected Address fromAddress;
    protected List<Address> toAddressList;
    protected List<Address> ccAddressList;
    protected List<Address> bccAddressList;
    protected String subject;
    protected String plainText;
    protected String htmlText;
    protected Map<String, SMailReadAttachedData> attachmentMap; // keyed by filenameOnHeader

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SMailPostingMessage(MimeMessage message, boolean training, Map<String, Object> pushedLoggingMap) {
        this.message = message;
        this.training = training;
        this.pushedLoggingMap = pushedLoggingMap;
    }

    // ===================================================================================
    //                                                                            Settings
    //                                                                            ========
    // -----------------------------------------------------
    //                                          From Address
    //                                          ------------
    public void setFrom(Address address) {
        fromAddress = address;
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
        saveToAddress(address);
        try {
            message.addRecipient(RecipientType.TO, address);
        } catch (MessagingException e) {
            String msg = buildAddressSettingFailureMessage("to", address);
            throw new SMailMessageSettingFailureException(msg, e);
        }
    }

    protected void saveToAddress(Address address) {
        if (toAddressList == null) {
            toAddressList = new ArrayList<Address>(2);
        }
        toAddressList.add(address);
    }

    // -----------------------------------------------------
    //                                            Cc Address
    //                                            ----------
    public void addCc(Address address) {
        saveCcAddress(address);
        try {
            message.addRecipient(RecipientType.CC, address);
        } catch (MessagingException e) {
            String msg = buildAddressSettingFailureMessage("cc", address);
            throw new SMailMessageSettingFailureException(msg, e);
        }
    }

    protected void saveCcAddress(Address address) {
        if (ccAddressList == null) {
            ccAddressList = new ArrayList<Address>(2);
        }
        ccAddressList.add(address);
    }

    // -----------------------------------------------------
    //                                           Bcc Address
    //                                           -----------
    public void addBcc(Address address) {
        saveBccAddress(address);
        try {
            message.addRecipient(RecipientType.BCC, address);
        } catch (MessagingException e) {
            String msg = buildAddressSettingFailureMessage("bcc", address);
            throw new SMailMessageSettingFailureException(msg, e);
        }
    }

    protected void saveBccAddress(Address address) {
        if (bccAddressList == null) {
            bccAddressList = new ArrayList<Address>(2);
        }
        bccAddressList.add(address);
    }

    // -----------------------------------------------------
    //                                                 Body
    //                                                ------
    public void setSubject(String subject, String encoding) {
        saveSubject(subject);
        try {
            message.setSubject(subject, encoding);
        } catch (MessagingException e) {
            String msg = "Failed to set subject: " + subject + " message=" + message;
            throw new SMailMessageSettingFailureException(msg, e);
        }
    }

    protected void saveSubject(String subject) {
        this.subject = subject;
    }

    // *text setting is very complex so not here
    // so public is saving methods of text

    public void savePlainTextForDisplay(String plainText) {
        this.plainText = plainText;
    }

    public void saveHtmlTextForDisplay(String htmlText) {
        this.htmlText = htmlText;
    }

    public void saveAttachmentForDisplay(SMailAttachment attachment, byte[] attachedBytes, String textEncoding) {
        if (attachmentMap == null) {
            attachmentMap = new LinkedHashMap<String, SMailReadAttachedData>(2);
        }
        final String filenameOnHeader = attachment.getFilenameOnHeader();
        final String contentType = attachment.getContentType();
        final SMailReadAttachedData attachedData = newMailReadAttachedData(filenameOnHeader, contentType, textEncoding, attachedBytes);
        attachmentMap.put(filenameOnHeader, attachedData);
    }

    protected SMailReadAttachedData newMailReadAttachedData(String filenameOnHeader, String contentType, String textEncoding,
            byte[] attachedBytes) {
        return new SMailReadAttachedData(filenameOnHeader, contentType, attachedBytes, textEncoding);
    }

    // -----------------------------------------------------
    //                                          Assist Logic
    //                                          ------------
    protected String buildAddressSettingFailureMessage(String title, Address address) {
        return "Failed to set '" + title + "' address: " + address + " message=" + message;
    }

    // ===================================================================================
    //                                                                  Display Expression
    //                                                                  ==================
    @Override
    public String toDisplay() {
        final StringBuilder sb = new StringBuilder();
        sb.append("/= = = = = = = = = = = = = = = = = = = = = = = = = = Mail Message");
        sb.append(LF).append("subject: " + subject);
        sb.append(LF).append("   from: " + fromAddress);
        if (toAddressList != null && !toAddressList.isEmpty()) {
            sb.append(LF).append("     to: " + (toAddressList.size() == 1 ? toAddressList.get(0) : toAddressList));
        }
        if (ccAddressList != null && !ccAddressList.isEmpty()) {
            sb.append(LF).append("     cc: " + (ccAddressList.size() == 1 ? ccAddressList.get(0) : ccAddressList));
        }
        if (bccAddressList != null && !bccAddressList.isEmpty()) {
            sb.append(LF).append("    bcc: " + (bccAddressList.size() == 1 ? bccAddressList.get(0) : bccAddressList));
        }
        if (pushedLoggingMap != null && !pushedLoggingMap.isEmpty()) {
            sb.append(LF).append("logging: " + pushedLoggingMap);
        }
        sb.append(LF).append(">>>");
        sb.append(LF).append(plainText);
        if (htmlText != null) {
            sb.append(LF).append(" - - - - - - - - - - (HTML)");
            sb.append(LF).append(htmlText);
        }
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
            final String textEncoding = attachedData.getTextEncoding();
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
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "message:{" + message + ", " + plainText + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public MimeMessage getMimeMessage() {
        return message;
    }

    @Override
    public boolean isTraining() {
        return training;
    }

    // -----------------------------------------------------
    //                                     Saved for Display
    //                                     -----------------
    public Address getSavedFromAddress() { // basically not null but consider null
        return fromAddress;
    }

    public List<Address> getSavedToAddressList() {
        return toAddressList != null ? Collections.unmodifiableList(toAddressList) : Collections.emptyList();
    }

    public List<Address> getSavedCcAddressList() {
        return ccAddressList != null ? Collections.unmodifiableList(ccAddressList) : Collections.emptyList();
    }

    public List<Address> getSavedBccAddressList() {
        return bccAddressList != null ? Collections.unmodifiableList(bccAddressList) : Collections.emptyList();
    }

    public String getSavedSubject() { // basically not null but consider null
        return subject;
    }

    public String getSavedPlainText() { // basically not null but consider null
        return plainText;
    }

    public String getSavedHtmlText() { // formally null allowed
        return htmlText;
    }

    public Map<String, SMailReadAttachedData> getSavedAttachmentMap() {
        return attachmentMap != null ? Collections.unmodifiableMap(attachmentMap) : Collections.emptyMap();
    }

    public Map<String, Object> getPushedLoggingMap() {
        return pushedLoggingMap != null ? Collections.unmodifiableMap(pushedLoggingMap) : Collections.emptyMap();
    }
}
