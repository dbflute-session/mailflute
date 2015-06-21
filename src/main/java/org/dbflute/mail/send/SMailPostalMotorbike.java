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

import java.util.Properties;

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
    public static final String MAIL_SMTP_HOST = "mail.smtp.host";
    public static final String MAIL_SMTP_PORT = "mail.smtp.port";
    public static final String MAIL_SMTP_FROM = "mail.smtp.from"; // return-path

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Session session;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SMailPostalMotorbike() {
        session = Session.getInstance(newSessionProperties());
    }

    protected Properties newSessionProperties() {
        return new Properties();
    }

    // ===================================================================================
    //                                                                            Register
    //                                                                            ========
    public void registerConnectionInfo(String host, int port) {
        final Properties props = session.getProperties();
        props.setProperty(MAIL_SMTP_HOST, host);
        props.setProperty(MAIL_SMTP_PORT, String.valueOf(port));
    }

    public void registerUserInfo(String user, String password) {
        final Properties props = session.getProperties();
        props.setProperty("mail.smtp.user", user);
        props.setProperty("mail.smtp.pass", password);
    }

    public void registerProxy(String proxyHost, String proxyPort) {
        final Properties props = session.getProperties();
        props.setProperty("proxySet", "true");
        props.setProperty("socksProxyHost", proxyHost);
        props.setProperty("socksProxyPort", proxyPort);
        props.setProperty("mail.smtp.socks.host", proxyHost);
        props.setProperty("mail.smtp.socks.port", proxyPort);
    }

    public void registerReturnPath(String address) {
        session.getProperties().setProperty(MAIL_SMTP_FROM, address);
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
