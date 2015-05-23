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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.dbflute.mail.Postcard;
import org.dbflute.mail.send.SMailTextProofreader;
import org.dbflute.util.Srl;
import org.dbflute.util.Srl.ScopeInfo;

/**
 * @author jflute
 * @since 0.4.0 (2015/05/09 Saturday at nakameguro)
 */
public class SMailBodyMetaProofreader implements SMailTextProofreader {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String META_DELIMITER = ">>>";
    public static final String SUBJECT_LABEL = "subject:";
    public static final String OPTION_LABEL = "option:";
    public static final String PLUS_HTML_OPTION = "+html";
    public static final String PROPDEF_PREFIX = "-- !!";
    public static final Set<String> optionSet;
    static {
        final Set<String> set = new LinkedHashSet<String>();
        set.add(PLUS_HTML_OPTION);
        optionSet = Collections.unmodifiableSet(set);
    }
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
        return doProofreader(removeUTF8BomIfNeeds(templateText)); // filter just in case
    }

    protected String doProofreader(String templateText) {
        // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
        // subject: Welcome to your sign up, /*pmb.memberName*/
        // >>>
        // Hello, /*pmb.memberName*/
        // ...
        // _/_/_/_/_/_/_/_/_/_/
        // no check here, already checked by receptionist
        // and parameters may be resolved here so not to have malfunction
        // TODO jflute mailflute: [D] subject error message
        final String subjectLabel = SUBJECT_LABEL;
        final String delimiter = META_DELIMITER;
        if (templateText.startsWith(subjectLabel) && templateText.contains(delimiter)) {
            if (postcard.getSubject() != null) {
                String msg = "Subject for the mail already exists but also defined at body file: " + postcard;
                throw new IllegalStateException(msg);
            }
            final ScopeInfo scopeFirst = Srl.extractScopeFirst(templateText, subjectLabel, delimiter);
            final String meta = Srl.replace(scopeFirst.getContent().trim(), CRLF, LF);
            final String subject = meta.contains(LF) ? Srl.substringFirstFront(meta, LF) : meta;
            postcard.setSubject(subject);
            final String rear = Srl.substringFirstRear(templateText, delimiter);
            final String realText;
            if (rear.startsWith(LF)) {
                realText = rear.substring(LF.length());
            } else if (rear.startsWith(CRLF)) {
                realText = rear.substring(CRLF.length());
            } else { // e.g. >>> Hello, ...
                realText = rear;
            }
            return realText;
        } else {
            if (postcard.getSubject() == null) {
                String msg = "Not found the subject for the mail: " + postcard;
                throw new IllegalStateException(msg);
            }
            return templateText;
        }
    }

    protected String removeUTF8BomIfNeeds(String plainText) {
        return plainText.charAt(0) == '\uFEFF' ? plainText.substring(1) : plainText;
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
