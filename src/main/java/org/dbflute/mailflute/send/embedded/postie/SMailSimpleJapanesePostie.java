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
package org.dbflute.mailflute.send.embedded.postie;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;

import org.dbflute.mailflute.Postcard;
import org.dbflute.mailflute.send.SMailMessage;
import org.dbflute.mailflute.send.SMailPostalMotorbike;
import org.dbflute.mailflute.send.SMailPostie;
import org.dbflute.mailflute.send.exception.SMailTransportFailureException;

/**
 * @author jflute
 * @author Takeshi Kato
 * @since 0.1.0 (2015/01/20 Tuesday)
 */
public class SMailSimpleJapanesePostie implements SMailPostie {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final String ISO_2022_JP = "iso-2022-jp";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final SMailPostalMotorbike motorbike;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SMailSimpleJapanesePostie(SMailPostalMotorbike motorbike) {
        this.motorbike = motorbike;
    }

    public void deliver(Postcard postcard) {
        // TODO jflute mailflute: postie's retry
        final SMailMessage message = createMailMessage(motorbike);
        message.setFrom(postcard.getFrom());
        for (Address adr : postcard.getToList()) {
            message.addTo(adr);
        }
        for (Address adr : postcard.getCcList()) {
            message.addCc(adr);
        }
        for (Address adr : postcard.getBccList()) {
            message.addBcc(adr);
        }
        final String encoding = getJapaneseEncoding();
        message.setSubject(postcard.getSubject(), encoding);
        message.setPlainBody(postcard.getPlainBody(), encoding);
        message.setHtmlBody(postcard.getHtmlBody(), encoding);

        try {
            Transport.send(message.getMimeMessage());
        } catch (MessagingException e) {
            throw new SMailTransportFailureException("Failed to send mail: " + postcard, e);
        }
    }

    protected SMailMessage createMailMessage(SMailPostalMotorbike motorbike) {
        return new SMailMessage(extractNativeSession(motorbike));
    }

    protected Session extractNativeSession(SMailPostalMotorbike motorbike) {
        return motorbike.getNativeSession();
    }

    protected String getJapaneseEncoding() {
        return ISO_2022_JP;
    }
}
