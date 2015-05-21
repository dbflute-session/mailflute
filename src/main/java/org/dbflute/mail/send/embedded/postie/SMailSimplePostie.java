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

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;

import org.dbflute.mail.Postcard;
import org.dbflute.mail.send.SMailMessage;
import org.dbflute.mail.send.SMailPostalMotorbike;
import org.dbflute.mail.send.SMailPostie;
import org.dbflute.mail.send.exception.SMailTransportFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 0.4.0 (2015/05/05 Tuesday)
 */
public class SMailSimplePostie implements SMailPostie {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger logger = LoggerFactory.getLogger(SMailSimplePostie.class);
    private static final String DEFAULT_ENCODING = "UTF-8";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final SMailPostalMotorbike motorbike;
    protected boolean training;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SMailSimplePostie(SMailPostalMotorbike motorbike) {
        this.motorbike = motorbike;
    }

    public SMailSimplePostie asTraining() {
        training = true;
        return this;
    }

    // ===================================================================================
    //                                                                             Deliver
    //                                                                             =======
    @Override
    public void deliver(Postcard postcard) {
        // TODO jflute mailflute: [C] postie's retry
        // TODO jflute mailflute: [C] postie's async
        final SMailMessage message = createMailMessage(motorbike);
        message.setFrom(postcard.getFrom());
        for (Address to : postcard.getToList()) {
            message.addTo(to);
        }
        for (Address cc : postcard.getCcList()) {
            message.addCc(cc);
        }
        for (Address bcc : postcard.getBccList()) {
            message.addBcc(bcc);
        }
        final String encoding = getEncoding();
        message.setSubject(postcard.getSubject(), encoding);
        final String plain = postcard.toCompletePlainText();
        if (plain != null) {
            message.setPlainBody(plain, encoding);
        }
        final String html = postcard.toCompleteHtmlText();
        if (html != null) {
            message.setHtmlBody(html, encoding);
        }
        try {
            send(message);
        } catch (MessagingException e) {
            throw new SMailTransportFailureException("Failed to send mail: " + postcard, e);
        }
    }

    // TODO jflute mailflute: [B] attachment
    //protected void attach(Postcard postcard, SMailAttachment attachment) {
    //    final MimeMultipart multipart = new MimeMultipart();
    //    try {
    //        multipart.setSubType("mixed");
    //        final MimeBodyPart bodyPart = new MimeBodyPart();
    //        //ByteBuffer buf = ByteBuffer.wrap(postcard.getPlainBody().getBytes("UTF-8"));
    //        DataSource source = new ByteArrayDataSource(buf.array(), "application/octet-stream");
    //        bodyPart.setDataHandler(new DataHandler(source));
    //        multipart.addBodyPart(bodyPart);
    //    } catch (MessagingException e) {
    //        throw new IllegalStateException("Failed to attach the file:" + attachment, e);
    //    }
    //}

    protected void send(SMailMessage message) throws MessagingException {
        if (training) {
            // TODO jflute mailflute: [B] mail logging (2015/05/11)
            logger.debug("your message:\n" + message.getPlainText());
        } else {
            Transport.send(message.getMimeMessage());
        }
    }

    protected SMailMessage createMailMessage(SMailPostalMotorbike motorbike) {
        return new SMailMessage(extractNativeSession(motorbike));
    }

    protected Session extractNativeSession(SMailPostalMotorbike motorbike) {
        return motorbike.getNativeSession();
    }

    protected String getEncoding() {
        return DEFAULT_ENCODING;
    };

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public boolean isTraining() {
        return training;
    }
}
