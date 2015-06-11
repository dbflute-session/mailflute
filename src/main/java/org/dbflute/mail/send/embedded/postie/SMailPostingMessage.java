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

import java.util.ArrayList;
import java.util.List;

import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.dbflute.mail.send.exception.SMailMessageSettingFailureException;

/**
 * @author jflute
 * @author Takeshi Kato
 * @since 0.1.0 (2015/01/20 Tuesday)
 */
public class SMailPostingMessage {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String LF = "\n";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final MimeMessage message;

    // save for debug
    protected Address fromAddress;
    protected List<Address> toAddressList;
    protected List<Address> ccAddressList;
    protected List<Address> bccAddressList;
    protected String subject;
    protected String plainText;
    protected String htmlText;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SMailPostingMessage(Session session) {
        message = newMimeMessage(session);
    }

    protected MimeMessage newMimeMessage(Session session) {
        return new MimeMessage(session);
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

    // -----------------------------------------------------
    //                                          Assist Logic
    //                                          ------------
    protected String buildAddressSettingFailureMessage(String title, Address address) {
        return "Failed to set '" + title + "' address: " + address + " message=" + message;
    }

    // ===================================================================================
    //                                                                  Display Expression
    //                                                                  ==================
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
        sb.append(LF).append(">>>");
        sb.append(LF).append(plainText);
        if (htmlText != null) {
            sb.append(LF).append(" - - - - - - - - - - (HTML)");
            sb.append(LF).append(htmlText);
        }
        sb.append(LF).append("= = = = = = = = = =/");
        return sb.toString();
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

    public String getSubject() {
        return subject;
    }

    public String getPlainText() {
        return plainText;
    }

    public String getHtmlText() {
        return htmlText;
    }
}
