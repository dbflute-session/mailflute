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
package org.dbflute.mail.send.embedded.proofreader;

import java.util.Map;

import org.dbflute.mail.Postcard;
import org.dbflute.mail.send.SMailTextProofreader;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.4.0 (2015/05/09 Saturday at nakameguro)
 */
public class SMailSubjectHeaderProofreader implements SMailTextProofreader {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String SUBJECT_LABEL = "subject:";
    public static final String HEADER_DELIMITER = ">>>";
    protected static final String LF = "\n";
    protected static final String CRLF = "\r\n";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Postcard postcard;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SMailSubjectHeaderProofreader(Postcard postcard) { // used by post office
        this.postcard = postcard;
    }

    // ===================================================================================
    //                                                                           Proofread
    //                                                                           =========
    @Override
    public String proofreader(String templateText, Map<String, Object> variableMap) {
        // TODO jflute mailflute: [D] subject error message
        // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
        // subject: Welcome to your sign up, /*pmb.memberName*/
        // >>>
        // Hello, /*pmb.memberName*/
        // ...
        // _/_/_/_/_/_/_/_/_/_/
        final String filtered = removeUTF8BomIfNeeds(templateText);
        final String subjectLabel = SUBJECT_LABEL;
        final String delimiter = HEADER_DELIMITER;
        if (filtered.startsWith(subjectLabel) && filtered.contains(delimiter)) {
            if (postcard.getSubject() != null) {
                String msg = "Subject for the mail already exists but also defined at body file: " + postcard;
                throw new IllegalStateException(msg);
            }
            final String header = Srl.replace(Srl.extractScopeFirst(filtered, subjectLabel, delimiter).getContent().trim(), CRLF, LF);
            final String subject;
            if (header.contains(LF)) {
                subject = Srl.substringFirstFront(header, LF);
                final String headerRear = Srl.substringFirstRear(header, LF);
                checkHeaderRear(headerRear);
            } else {
                subject = header;
            }
            postcard.setSubject(subject);
            final String rear = Srl.substringFirstRear(filtered, delimiter);
            final String realText;
            if (rear.startsWith(LF)) {
                realText = rear.substring(LF.length());
            } else if (rear.startsWith(CRLF)) {
                realText = rear.substring(CRLF.length());
            } else {
                realText = rear;
            }
            return realText;
        } else {
            if (postcard.getSubject() == null) {
                String msg = "Not found the subject for the mail: " + postcard;
                throw new IllegalStateException(msg);
            }
            return templateText; // without filter, keep plain for writer
        }
    }

    protected void checkHeaderRear(String headerRear) {
        // TODO jflute mailflute: [D] header check
    }

    protected String removeUTF8BomIfNeeds(String plainText) {
        return plainText.charAt(0) == '\uFEFF' ? plainText.substring(1) : plainText;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "proofreader:{subject_header}";
    }
}
