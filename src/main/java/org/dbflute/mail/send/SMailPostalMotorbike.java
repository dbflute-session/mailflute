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

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final String host = session.getProperty(MAIL_SMTP_HOST);
        final String port = session.getProperty(MAIL_SMTP_PORT);
        return "motobike:{host=" + host + ", port=" + port + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public Session getNativeSession() {
        return session;
    }
}
