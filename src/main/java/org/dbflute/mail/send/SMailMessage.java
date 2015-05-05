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
package org.dbflute.mail.send;

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
public class SMailMessage {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final MimeMessage message;

    // save for debug
    protected String plainText;
    protected String htmlText;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SMailMessage(Session session) {
        message = new MimeMessage(session);
    }

    // ===================================================================================
    //                                                                            Settings
    //                                                                            ========
    public void setFrom(Address address) {
        try {
            message.setFrom(address);
        } catch (MessagingException e) {
            String msg = buildAddressSettingFailureMessage("from", address);
            throw new SMailMessageSettingFailureException(msg, e);
        }
    }

    public void addTo(Address address) {
        try {
            message.addRecipient(RecipientType.TO, address);
        } catch (MessagingException e) {
            String msg = buildAddressSettingFailureMessage("to", address);
            throw new SMailMessageSettingFailureException(msg, e);
        }
    }

    public void addCc(Address address) {
        try {
            message.addRecipient(RecipientType.CC, address);
        } catch (MessagingException e) {
            String msg = buildAddressSettingFailureMessage("cc", address);
            throw new SMailMessageSettingFailureException(msg, e);
        }
    }

    public void addBcc(Address address) {
        try {
            message.addRecipient(RecipientType.BCC, address);
        } catch (MessagingException e) {
            String msg = buildAddressSettingFailureMessage("bcc", address);
            throw new SMailMessageSettingFailureException(msg, e);
        }
    }

    protected String buildAddressSettingFailureMessage(String title, Address address) {
        return "Failed to set '" + title + "' address: " + address + " message=" + message;
    }

    public void setSubject(String subject, String encoding) {
        try {
            message.setSubject(subject, encoding);
        } catch (MessagingException e) {
            String msg = "Failed to set subject: " + subject + " message=" + message;
            throw new SMailMessageSettingFailureException(msg, e);
        }
    }

    public void setPlainBody(String plainBody, String encoding) {
        try {
            plainText = plainBody;
            message.setText(plainBody, encoding, "plain");
        } catch (MessagingException e) {
            String msg = "Failed to set plain body: encoding=" + encoding + " message=" + message;
            throw new SMailMessageSettingFailureException(msg, e);
        }
    }

    public void setHtmlBody(String htmlBody, String encoding) {
        try {
            htmlText = htmlBody;
            message.setText(htmlBody, encoding, "html");
        } catch (MessagingException e) {
            String msg = "Failed to set plain body: encoding=" + encoding + " message=" + message;
            throw new SMailMessageSettingFailureException(msg, e);
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

    public String getPlainText() {
        return plainText;
    }

    public String getHtmlText() {
        return htmlText;
    }
}
