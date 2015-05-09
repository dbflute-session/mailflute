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
package org.dbflute.mail.send.embedded.receptionist;

import java.io.InputStream;

import org.dbflute.helper.filesystem.FileTextIO;
import org.dbflute.mail.Postcard;
import org.dbflute.mail.Postcard.DirectBodyOption;
import org.dbflute.mail.send.SMailReceptionist;
import org.dbflute.mail.send.embedded.proofreader.SMailSubjectHeaderProofreader;
import org.dbflute.util.DfResourceUtil;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.4.0 (2015/05/09 Saturday at nakameguro)
 */
public class SMailConventionFileReceptionist implements SMailReceptionist {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String OPTION_LABEL = "option:";
    public static final String PLUS_HTML = "+html";
    protected static final String LF = "\n";
    protected static final String CRLF = "\r\n";

    // ===================================================================================
    //                                                                       Read BodyFile
    //                                                                       =============
    @Override
    public void readBodyFile(Postcard postcard) {
        final FileTextIO textIO = new FileTextIO().encodeAsUTF8();
        final boolean filesystem = postcard.isFromFilesystem();
        final String bodyFile = postcard.getBodyFile();
        final String plainText = doRead(textIO, bodyFile, filesystem);
        analyzeMetaHeader(postcard, plainText);
        final DirectBodyOption option = postcard.useDirectBody(plainText);
        if (postcard.isAlsoHtmlFile()) {
            option.alsoDirectHtml(doRead(textIO, deriveHtmlFilePath(bodyFile), filesystem));
        }
    }

    // ===================================================================================
    //                                                                     Analyzer Header
    //                                                                     ===============
    protected void analyzeMetaHeader(Postcard postcard, String plainText) {
        // TODO jflute mailflute: [D] check header definition
        final String headerDelimiter = SMailSubjectHeaderProofreader.HEADER_DELIMITER;
        if (!plainText.contains(headerDelimiter)) {
            // TODO jflute mailflute: [D] check mistake, subject: option:
            return;
        }
        final String header = Srl.replace(Srl.substringFirstFront(plainText, headerDelimiter), CRLF, LF);
        final String headerRear = Srl.substringFirstRear(header, LF);
        if (headerRear.startsWith(OPTION_LABEL)) {
            final String option = Srl.substringFirstFront(Srl.substringFirstRear(headerRear, OPTION_LABEL), LF);
            if (option.contains(PLUS_HTML)) {
                postcard.officePlusHtml();
            }
        }
    }

    protected String deriveHtmlFilePath(String bodyFile) {
        final String front = Srl.substringLastFront(bodyFile, ".");
        final String rear = Srl.substringLastRear(bodyFile, ".");
        return front + "_html." + rear; // e.g. member_registration_html.ml
    }

    // ===================================================================================
    //                                                                       Actually Read
    //                                                                       =============
    protected String doRead(FileTextIO textIO, String path, boolean filesystem) {
        if (filesystem) {
            // TODO jflute mailflute: [D] file not found error message
            return textIO.read(path);
        } else { // from class-path as default, mainly here
            final InputStream ins = DfResourceUtil.getResourceStream(path);
            if (ins == null) {
                String msg = "Not found the outside file from classpath: " + path;
                throw new IllegalStateException(msg);
            }
            return textIO.read(ins);
        }
    }
}
