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
package org.dbflute.mail.send;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import org.dbflute.mail.send.exception.SMailIllegalStateException;
import org.dbflute.optional.OptionalThing;

/**
 * @author jflute
 * @since 0.1.0 (2015/01/20 Tuesday)
 */
public class SMailPostalMotorbike {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String MAIL_SMTP_HOST = "mail.smtp.host";
    protected static final String MAIL_SMTP_PORT = "mail.smtp.port";
    protected static final String MAIL_SMTP_FROM = "mail.smtp.from"; // return-path
    protected static final String MAIL_TRANSPORT_PROTOCOL = "mail.transport.protocol";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Session session; // not null

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SMailPostalMotorbike() { // for normal
        session = createSession();
    }

    public SMailPostalMotorbike(String userName, String password) { // for e.g. starttls
        session = createSession(createAuthenticator(userName, password));
    }

    protected Authenticator createAuthenticator(String userName, String password) {
        return new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, password);
            }
        };
    }

    protected Session createSession() {
        return Session.getInstance(createSessionProperties());
    }

    protected Session createSession(Authenticator auth) {
        return Session.getInstance(createSessionProperties(), auth);
    }

    protected Properties createSessionProperties() {
        return new Properties();
    }

    // ===================================================================================
    //                                                                            Register
    //                                                                            ========
    public void registerConnectionInfo(String host, int port) {
        assertArgumentNotNull("passhostword", host);
        final Properties props = session.getProperties();
        props.setProperty(MAIL_SMTP_HOST, host);
        props.setProperty(MAIL_SMTP_PORT, String.valueOf(port));
    }

    public void registerProxy(String proxyHost, String proxyPort) {
        assertArgumentNotNull("proxyHost", proxyHost);
        assertArgumentNotNull("proxyPort", proxyPort);
        final Properties props = session.getProperties();
        props.setProperty("proxySet", "true");
        props.setProperty("socksProxyHost", proxyHost);
        props.setProperty("socksProxyPort", proxyPort);
        props.setProperty("mail.smtp.socks.host", proxyHost);
        props.setProperty("mail.smtp.socks.port", proxyPort);
    }

    public void registerStarttls() {
        final Properties props = session.getProperties();
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.smtp.starttls.enable", "true");
        props.setProperty("mail.smtp.starttls.required", "true");
    }

    public void registerTransportProtocol(String protocol) {
        assertArgumentNotNull("protocol", protocol);
        session.getProperties().setProperty(MAIL_TRANSPORT_PROTOCOL, protocol);
    }

    public void registerReturnPath(String address) {
        assertArgumentNotNull("address", address);
        session.getProperties().setProperty(MAIL_SMTP_FROM, address);
    }

    // -----------------------------------------------------
    //                                     Free Registration
    //                                     -----------------
    public void setProperty(String key, String value) {
        assertArgumentNotNull("key", key);
        assertArgumentNotNull("value", value);
        session.getProperties().setProperty(key, value);
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
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
        final String host = session.getProperty(MAIL_SMTP_HOST);
        final String port = session.getProperty(MAIL_SMTP_PORT);
        return "motorbike:{host=" + host + ", port=" + port + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public Session getNativeSession() {
        return session;
    }

    public OptionalThing<String> getReturnPath() {
        return OptionalThing.ofNullable(session.getProperty(MAIL_SMTP_FROM), () -> {
            throw new SMailIllegalStateException("Not found the return path (" + MAIL_SMTP_FROM + "): " + session.getProperties());
        });
    }
}
