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

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.mail.Postcard;
import org.dbflute.mail.send.SMailTextProofreader;
import org.dbflute.mail.send.exception.SMailSubjectDuplicateException;
import org.dbflute.mail.send.exception.SMailSubjectNotFoundException;
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
        return doProofreader(removeUTF8BomIfNeeds(templateText)); // receptionist already remove it but just in case
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
        final String subjectLabel = SUBJECT_LABEL;
        final String delimiter = META_DELIMITER;
        if (templateText.startsWith(subjectLabel) && templateText.contains(delimiter)) {
            if (postcard.getSubject().isPresent()) {
                throwMailSubjectDuplicateException(templateText);
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
            } else { // e.g. >>> Hello, ... but receptionist checks it so basically no way
                realText = rear;
            }
            return realText;
        } else {
            if (postcard.getSubject() == null) {
                throwMailSubjectNotFoundException(templateText);
            }
            return templateText;
        }
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
        br.addElement("  subject: ...(subject)");
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
