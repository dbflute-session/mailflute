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
import org.dbflute.util.Srl;

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
    protected static final String MAIL_SMTP_AUTH = "mail.smtp.auth"; // for e.g. starttls, ssl
    protected static final String MAIL_TRANSPORT_PROTOCOL = "mail.transport.protocol";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Session session; // not null
    protected final boolean hasAuth;
    protected MotorbikeSecurityType securityType = MotorbikeSecurityType.NONE;

    protected enum MotorbikeSecurityType {
        NONE, SSL, STARTTLS
    }

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SMailPostalMotorbike() { // for normal
        session = createSession();
        hasAuth = false;
    }

    /**
     * <pre>
     * e.g. STARTTLS
     *  SMailPostalMotorbike motorbike = new SMailPostalMotorbike("sea", "land");
     *  motorbike.registerConnectionInfo(...);
     *  motorbike.registerReturnPath(...);
     *  motorbike.<span style="color: #CC4747">registerStarttls()</span>;
     * 
     * e.g. SSL
     *  SMailPostalMotorbike motorbike = new SMailPostalMotorbike("sea", "land").<span style="color: #CC4747">useSsl()</span>;
     *  motorbike.registerConnectionInfo(...);
     *  motorbike.registerReturnPath(...);
     * </pre>
     * @param userName The user name for authentication to mail server. (NotNul)
     * @param password The password for the user. (NotNul)
     */
    public SMailPostalMotorbike(String userName, String password) { // for e.g. starttls
        assertArgumentNotNull("userName", userName);
        assertArgumentNotNull("password", password);
        session = createSession(createAuthenticator(userName, password));
        hasAuth = true;
    }

    /**
     * @param userName The user name for authentication to mail server. (NotNul)
     * @param password The password for the user. (NotNul)
     * @return The new-created authenticator. (NotNull)
     */
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

    /**
     * Motorbike uses SMTP over SSL. <br>
     * It needs authenticator, that you can specify by constructor.
     * <pre>
     * e.g.
     *  SMailPostalMotorbike motorbike = new SMailPostalMotorbike("sea", "land").<span style="color: #CC4747">useSsl()</span>;
     *  motorbike.registerConnectionInfo(...);
     *  motorbike.registerReturnPath(...);
     * </pre>
     * @return this. (NotNull)
     */
    public SMailPostalMotorbike useSsl() {
        if (!hasAuth) {
            throw new IllegalStateException("Not found the authenticator for SSL: session=" + session);
        }
        if (MotorbikeSecurityType.STARTTLS.equals(securityType)) {
            throw new IllegalStateException("Cannot use SSL with STARTTLS: session=" + session);
        }
        securityType = MotorbikeSecurityType.SSL;
        registerTransportProtocol("smtps");
        registerSmtpAuth();
        return this;
    }

    /**
     * Motorbike uses STARTTLS. <br>
     * It needs authenticator, that you can specify by constructor.
     * <pre>
     * e.g.
     *  SMailPostalMotorbike motorbike = new SMailPostalMotorbike("sea", "land").<span style="color: #CC4747">useStarttls()</span>;
     *  motorbike.registerConnectionInfo(...);
     *  motorbike.registerReturnPath(...);
     * </pre>
     * @return this. (NotNull)
     */
    public SMailPostalMotorbike useStarttls() {
        if (!hasAuth) {
            throw new IllegalStateException("Not found the authenticator for STARTTLS: session=" + session);
        }
        if (MotorbikeSecurityType.SSL.equals(securityType)) {
            throw new IllegalStateException("Cannot use STARTTLS with SSL: session=" + session);
        }
        securityType = MotorbikeSecurityType.STARTTLS;
        registerSmtpAuth();
        final Properties props = session.getProperties();
        props.setProperty("mail.smtp.starttls.enable", "true");
        props.setProperty("mail.smtp.starttls.required", "true");
        return this;
    }

    protected void registerSmtpAuth() {
        session.getProperties().setProperty(resolveProtocolKey(MAIL_SMTP_AUTH), "true");
    }

    // ===================================================================================
    //                                                                            Register
    //                                                                            ========
    public void registerConnectionInfo(String host, int port) {
        assertArgumentNotNull("passhostword", host);
        final Properties props = session.getProperties();
        props.setProperty(resolveProtocolKey(MAIL_SMTP_HOST), host);
        props.setProperty(resolveProtocolKey(MAIL_SMTP_PORT), String.valueOf(port));
    }

    public void registerProxy(String proxyHost, String proxyPort) { // #thinking: needs SSL key handling?
        assertArgumentNotNull("proxyHost", proxyHost);
        assertArgumentNotNull("proxyPort", proxyPort);
        final Properties props = session.getProperties();
        props.setProperty("proxySet", "true");
        props.setProperty("socksProxyHost", proxyHost);
        props.setProperty("socksProxyPort", proxyPort);
        props.setProperty("mail.smtp.socks.host", proxyHost);
        props.setProperty("mail.smtp.socks.port", proxyPort);
    }

    /**
     * @deprecated use useStarttls()
     */
    public void registerStarttls() {
        useStarttls();
    }

    public void registerReturnPath(String address) {
        assertArgumentNotNull("address", address);
        session.getProperties().setProperty(resolveProtocolKey(MAIL_SMTP_FROM), address);
    }

    public void registerTransportProtocol(String protocol) { // public just in case
        assertArgumentNotNull("protocol", protocol);
        session.getProperties().setProperty(MAIL_TRANSPORT_PROTOCOL, protocol);
    }

    // -----------------------------------------------------
    //                                      SSL Key Handling
    //                                      ----------------
    protected String resolveProtocolKey(String key) {
        return MotorbikeSecurityType.SSL.equals(securityType) ? Srl.replace(key, ".smtp.", ".smtps.") : key;
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
        return "motorbike:{session=" + session + (hasAuth ? ", auth(" + securityType + ")" : "") + "}";
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
