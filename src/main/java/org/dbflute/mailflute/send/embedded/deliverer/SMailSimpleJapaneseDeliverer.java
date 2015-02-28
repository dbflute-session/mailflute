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
package org.dbflute.mailflute.send.embedded.deliverer;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Transport;

import org.dbflute.mailflute.Postcard;
import org.dbflute.mailflute.send.SMailMessage;
import org.dbflute.mailflute.send.SMailDeliverer;
import org.dbflute.mailflute.send.SMailSession;
import org.dbflute.mailflute.send.exception.SMailTransportFailureException;

/**
 * @author jflute
 * @author Takeshi Kato
 * @since 0.1.0 (2015/01/20 Tuesday)
 */
public class SMailSimpleJapaneseDeliverer implements SMailDeliverer {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final String ISO_2022_JP = "iso-2022-jp";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final SMailSession session;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SMailSimpleJapaneseDeliverer(SMailSession session) {
        this.session = session;
    }

    public void send(Postcard postcard) {
        final SMailMessage message = newMailMessage(session);
        message.setFrom(postcard.getFromAdr());
        for (Address adr : postcard.getToAdrList()) {
            message.addTo(adr);
        }
        for (Address adr : postcard.getCcAdrList()) {
            message.addCc(adr);
        }
        for (Address adr : postcard.getBccAdrList()) {
            message.addBcc(adr);
        }
        final String encoding = getJapaneseEncoding();
        message.setSubject(postcard.getSubject(), encoding);
        message.setPlainBody(postcard.getPlainBody(), encoding);
        message.setHtmlBody(postcard.getHtmlBody(), encoding);

        try {
            Transport.send(message.getMimeMessage());
        } catch (MessagingException e) {
            throw new SMailTransportFailureException("Failed to send mail.", e);
        }
    }

    protected SMailMessage newMailMessage(SMailSession session) {
        return new SMailMessage(session);
    }

    protected String getJapaneseEncoding() {
        return ISO_2022_JP;
    }
}
