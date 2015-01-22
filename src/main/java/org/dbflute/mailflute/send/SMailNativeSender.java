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
package org.dbflute.mailflute.send;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import org.dbflute.mailflute.send.exception.SMailMessageSendFailureException;
import org.dbflute.mailflute.send.exception.SMailSmtpConnectionFailureException;
import org.dbflute.mailflute.send.exception.SMailTransportFailureException;

/**
 * @author jflute
 * @since 0.1.0 (2015/01/20 Tuesday)
 */
public class SMailNativeSender {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final String SMTP_PROTOCOL = "smtp";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String host;
    protected final int port;
    protected final String user;
    protected final String password;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SMailNativeSender(String host, int port, String user, String password) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
    }

    // ===================================================================================
    //                                                                               Send
    //                                                                              ======
    /**
     * Send the message using the session.
     * @param session The mail session to send. (NotNull)
     * @param message The mime session to be sent. (NotNull)
     * @throws SMailTransportFailureException When it fails to get the transport.
     * @throws SMailSmtpConnectionFailureException When it fails to connect the SMTP server.
     * @throws SMailMessageSendFailureException When it fails to send the message.
     */
    public void send(Session session, MimeMessage message) {
        final String protocol = getProtocol();
        final Transport transport;
        try {
            transport = session.getTransport(protocol);
        } catch (MessagingException e) {
            String msg = "Failed to get the transport: protocol=" + protocol + " session=" + protocol;
            throw new SMailTransportFailureException(msg, e);
        }
        try {
            connect(session, transport);
        } catch (MessagingException e) {
            String msg = "Failed to connect the SMTP server: " + host + ":" + port;
            throw new SMailSmtpConnectionFailureException(msg, e);
        }
        try {
            sendMessage(message, transport);
        } catch (MessagingException e) {
            String msg = "Failed to send the message: " + host + ":" + port + " message=" + message;
            throw new SMailMessageSendFailureException(msg, e);
        }
    }

    protected void connect(Session session, Transport transport) throws MessagingException {
        final Properties props = session.getProperties();
        final String host = getHost(props);
        final int port = getPort(props);
        final String user = getUser(props);
        final String password = getPassword(props);
        transport.connect(host, port, user, password);
    }

    protected String getHost(Properties props) {
        return props.getProperty("mail.smtp.host");
    }

    protected int getPort(Properties props) {
        final String portStr = props.getProperty("mail.smtp.port");
        return Integer.parseInt(portStr);
    }

    protected String getUser(Properties props) {
        return props.getProperty("mail.smtp.user");
    }

    protected String getPassword(Properties props) {
        return props.getProperty("mail.smtp.pass");
    }

    protected void sendMessage(MimeMessage message, Transport transport) throws MessagingException {
        transport.sendMessage(message, message.getAllRecipients());
    }

    protected String getProtocol() {
        return SMTP_PROTOCOL;
    }
}
