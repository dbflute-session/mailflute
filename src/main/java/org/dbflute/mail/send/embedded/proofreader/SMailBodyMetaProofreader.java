/*
 * Copyright 2015-2018 the original author or authors.
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
package org.dbflute.mail.send.embedded.proofreader;

import java.util.Map;

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.mail.Postcard;
import org.dbflute.mail.send.SMailTextProofreader;
import org.dbflute.mail.send.embedded.receptionist.SMailConventionReceptionist;
import org.dbflute.mail.send.exception.SMailIllegalStateException;
import org.dbflute.mail.send.exception.SMailSubjectDuplicateException;
import org.dbflute.mail.send.exception.SMailSubjectNotFoundException;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.4.0 (2015/05/09 Saturday at nakameguro)
 */
public class SMailBodyMetaProofreader implements SMailTextProofreader {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String META_DELIMITER = SMailConventionReceptionist.META_DELIMITER;
    protected static final String COMMENT_BEGIN = SMailConventionReceptionist.COMMENT_BEGIN;
    protected static final String COMMENT_END = SMailConventionReceptionist.COMMENT_END;
    protected static final String SUBJECT_LABEL = SMailConventionReceptionist.SUBJECT_LABEL;
    protected static final String LF = "\n";
    protected static final String CRLF = "\r\n";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Postcard postcard;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SMailBodyMetaProofreader(Postcard postcard) { // used by post office
        this.postcard = postcard;
    }

    // ===================================================================================
    //                                                                           Proofread
    //                                                                           =========
    @Override
    public String proofread(String templateText, Map<String, Object> variableMap) {
        return doProofreader(removeUTF8BomIfNeeds(templateText)); // receptionist already remove it but just in case
    }

    protected String doProofreader(String templateText) {
        // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
        // /*
        //  [New Member's Registration]
        //  The member will be formalized after clicking the URL.
        // */
        // subject: Welcome to your sign up, /*pmb.memberName*/
        // >>>
        // Hello, /*pmb.memberName*/
        // ...
        // _/_/_/_/_/_/_/_/_/_/
        // no check here, already checked by receptionist
        // and parameters may be resolved here so not to have malfunction
        final String delimiter = META_DELIMITER;
        if (templateText.startsWith(COMMENT_BEGIN) && templateText.contains(delimiter)) {
            if (postcard.getSubject().isPresent()) {
                throwMailSubjectDuplicateException(templateText);
            }
            final String subject = extractSubject(templateText, delimiter);
            postcard.setSubject(subject);
            return extractRealText(templateText, delimiter);
        } else {
            if (postcard.getSubject() == null) {
                throwMailSubjectNotFoundException(templateText);
            }
            return templateText;
        }
    }

    protected String extractSubject(String templateText, String delimiter) {
        final String meta = Srl.substringFirstFront(templateText, delimiter);
        final String commentRear = Srl.substringFirstRear(meta, COMMENT_END);
        if (commentRear == null) { // basically no way because of receptionist verification
            throw new SMailIllegalStateException("Body meta should have header comment: " + meta);
        }
        return Srl.substringFirstFront(Srl.substringFirstRear(commentRear, SUBJECT_LABEL), LF).trim();
    }

    protected String extractRealText(String templateText, String delimiter) {
        final String delimRear = Srl.substringFirstRear(templateText, delimiter);
        final String realText;
        if (delimRear.startsWith(LF)) {
            realText = delimRear.substring(LF.length());
        } else if (delimRear.startsWith(CRLF)) {
            realText = delimRear.substring(CRLF.length());
        } else { // e.g. >>> Hello, ... but receptionist checks it so basically no way
            realText = delimRear;
        }
        return realText;
    }

    protected String removeUTF8BomIfNeeds(String plainText) {
        return plainText.charAt(0) == '\uFEFF' ? plainText.substring(1) : plainText;
    }

    protected void throwMailSubjectDuplicateException(String templateText) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Duplicate subject specified by postcard and defined in body file.");
        br.addItem("Advice");
        br.addElement("If subject in body file exists,");
        br.addElement("you don't need to specify it by postcard.");
        br.addItem("Postcard");
        br.addElement(postcard);
        br.addItem("Template Text");
        br.addElement(templateText);
        final String msg = br.buildExceptionMessage();
        throw new SMailSubjectDuplicateException(msg);
    }

    protected void throwMailSubjectNotFoundException(String templateText) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the subject for the postcard.");
        br.addItem("Advice");
        br.addElement("Specify subject by postcard or define it in body file.");
        br.addElement("For example, subject on body meta like this:");
        br.addElement("  comment: ...(one liner)");
        br.addElement("  subject: ...(mail subject)");
        br.addElement("  >>>");
        br.addElement("  ...(mail body)");
        br.addItem("Postcard");
        br.addElement(postcard);
        br.addItem("Template Text");
        br.addElement(templateText);
        final String msg = br.buildExceptionMessage();
        throw new SMailSubjectNotFoundException(msg);
    }

    // ===================================================================================
    //                                                                             Dispose
    //                                                                             =======
    @Override
    public void workingDispose() {
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "proofreader:{body_meta}";
    }
}
