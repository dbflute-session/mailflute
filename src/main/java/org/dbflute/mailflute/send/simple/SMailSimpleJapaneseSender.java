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
package org.dbflute.mailflute.send.simple;

import javax.mail.Address;

import org.dbflute.mailflute.send.SMailMessage;
import org.dbflute.mailflute.send.SMailPost;
import org.dbflute.mailflute.send.SMailSession;
import org.dbflute.mailflute.send.SMailSessionHolder;

/**
 * @author jflute
 * @since 0.1.0 (2015/01/20 Tuesday)
 */
public class SMailSimpleJapaneseSender {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final String ISO_2022_JP = "iso-2022-jp";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final SMailSessionHolder sessionHolder;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SMailSimpleJapaneseSender(SMailSessionHolder sessionHolder) {
        this.sessionHolder = sessionHolder;
    }

    public void send(SMailPost post) {
        final SMailSession session = sessionHolder.getSession("main");
        final SMailMessage message = newMailMessage(session);
        message.setFrom(post.getFromAdr());
        for (Address adr : post.getToAdrList()) {
            message.addTo(adr);
        }
        for (Address adr : post.getCcAdrList()) {
            message.addCc(adr);
        }
        for (Address adr : post.getBccAdrList()) {
            message.addBcc(adr);
        }
        final String encoding = getJapaneseEncoding();
        message.setSubject(post.getSubject(), encoding);
        message.setPlainBody(post.getPlainBody(), encoding);
    }

    protected SMailMessage newMailMessage(SMailSession session) {
        return new SMailMessage(session);
    }

    protected String getJapaneseEncoding() {
        return ISO_2022_JP;
    }
}
