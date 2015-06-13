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

import org.dbflute.mail.send.exception.SMailBodyMetaParseFailureException;
import org.dbflute.utflute.core.PlainTestCase;

/**
 * @author jflute
 */
public class SMailConventionReceptionistTest extends PlainTestCase {

    public void test_checkBodyMetaFormat_noIndependentDelimiter_inSubject() throws Exception {
        // ## Arrange ##
        SMailConventionReceptionist receptionist = new SMailConventionReceptionist();
        StringBuilder sb = new StringBuilder();
        sb.append("subject: sea>>>");
        sb.append(ln()).append("body now");

        // ## Act ##
        // ## Assert ##
        assertException(SMailBodyMetaParseFailureException.class, () -> {
            receptionist.checkBodyMetaFormat("sea.dfmail", sb.toString());
        });
    }

    public void test_checkBodyMetaFormat_noIndependentDelimiter_inOption() throws Exception {
        // ## Arrange ##
        SMailConventionReceptionist receptionist = new SMailConventionReceptionist();
        StringBuilder sb = new StringBuilder();
        sb.append("subject: sea");
        sb.append(ln()).append("option: +html>>>");
        sb.append(ln()).append("body now");

        // ## Act ##
        // ## Assert ##
        assertException(SMailBodyMetaParseFailureException.class, () -> {
            receptionist.checkBodyMetaFormat("sea.dfmail", sb.toString());
        });
    }

    public void test_checkBodyMetaFormat_noIndependentDelimiter_noLineBody() throws Exception {
        // ## Arrange ##
        SMailConventionReceptionist receptionist = new SMailConventionReceptionist();
        StringBuilder sb = new StringBuilder();
        sb.append("subject: sea");
        sb.append(ln()).append(">>> body now");

        // ## Act ##
        // ## Assert ##
        assertException(SMailBodyMetaParseFailureException.class, () -> {
            receptionist.checkBodyMetaFormat("sea.dfmail", sb.toString());
        });
    }
}
