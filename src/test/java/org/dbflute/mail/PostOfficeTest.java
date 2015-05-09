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
package org.dbflute.mail;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.dbflute.helper.filesystem.FileTextIO;
import org.dbflute.mail.send.SMailDeliveryDepartment;
import org.dbflute.mail.send.SMailPostalMotorbike;
import org.dbflute.mail.send.SMailPostalParkingLot;
import org.dbflute.mail.send.SMailPostalPersonnel;
import org.dbflute.mail.send.embedded.personnel.SMailDogmaticPostalPersonnel;
import org.dbflute.utflute.core.PlainTestCase;
import org.dbflute.util.DfResourceUtil;

/**
 * @author jflute
 */
public class PostOfficeTest extends PlainTestCase {

    private static final String BODY_ONLY_ML = "mail/office/body_only.ml";
    private static final String HEADER_SUBJECT_ML = "mail/office/header_subject.ml";
    private static final String OPTION_HTMLEXISTS_ML = "mail/office/option_htmlexists.ml";
    private static final String OPTION_HTMLNOFILE_ML = "mail/office/option_htmlnofile.ml";

    public void test_deliver_bodyFile_bodyOnly() throws Exception {
        // ## Arrange ##
        Postcard postcard = new Postcard();
        String subject = "Welcome to your source code reading";
        postcard.setSubject(subject);
        Map<String, Object> map = prepareVariableMap();
        postcard.useBodyFile(BODY_ONLY_ML).useTemplateText(map);

        // ## Act ##
        prepareOffice().deliver(postcard);

        // ## Assert ##
        doAssertParameter(postcard, map, subject, false);
    }

    public void test_deliver_bodyFile_headerSubject() throws Exception {
        // ## Arrange ##
        Postcard postcard = new Postcard();
        Map<String, Object> map = prepareVariableMap();
        postcard.useBodyFile(HEADER_SUBJECT_ML).useTemplateText(map);

        // ## Act ##
        prepareOffice().deliver(postcard);

        // ## Assert ##
        doAssertParameter(postcard, map, "Welcome to your test code reading, jflute", false);
    }

    public void test_deliver_bodyFile_optionPlusHtml_exists() throws Exception {
        // ## Arrange ##
        Postcard postcard = new Postcard();
        Map<String, Object> map = prepareVariableMap();
        postcard.useBodyFile(OPTION_HTMLEXISTS_ML).useTemplateText(map);

        // ## Act ##
        prepareOffice().deliver(postcard);

        // ## Assert ##
        doAssertParameter(postcard, map, "Welcome to your test code reading, jflute", true);
    }

    public void test_deliver_bodyFile_optionPlusHtml_noFile() throws Exception {
        // ## Arrange ##
        PostOffice office = prepareOffice();
        Postcard postcard = new Postcard();
        postcard.useBodyFile(OPTION_HTMLNOFILE_ML).useTemplateText(prepareVariableMap());

        // ## Act ##
        try {
            office.deliver(postcard);
            // ## Assert ##
            fail();
        } catch (IllegalStateException e) {
            log(e.getMessage());
        }
    }

    public void test_deliver_directBody() throws Exception {
        // ## Arrange ##
        String plainBody = preparePlainBody();
        Postcard postcard = new Postcard();
        String subject = "Welcome to your source code reading";
        postcard.setSubject(subject);
        Map<String, Object> map = prepareVariableMap();
        postcard.useDirectBody(plainBody).useTemplateText(map);

        // ## Act ##
        prepareOffice().deliver(postcard);

        // ## Assert ##
        doAssertParameter(postcard, map, subject, false);
    }

    protected void doAssertParameter(Postcard postcard, Map<String, Object> pmb, String subject, boolean hasHtml) {
        String plain = postcard.toCompletePlainText();
        Object birthdate = pmb.get("birthdate");
        assertContainsAll(plain, "jflute", "Today is " + birthdate + ".", "Thanks");
        if (hasHtml) {
            assertContainsAll(postcard.toCompleteHtmlText(), "jflute", "Today is " + birthdate + ".", "Thanks");
        } else {
            assertNull(postcard.toCompleteHtmlText());
        }
        assertEquals(postcard.getSubject(), subject);
    }

    // ===================================================================================
    //                                                                         Test Helper
    //                                                                         ===========
    protected PostOffice prepareOffice() {
        SMailPostalParkingLot parkingLot = new SMailPostalParkingLot();
        SMailPostalMotorbike motorbike = new SMailPostalMotorbike();
        parkingLot.registerMotorbikeAsMain(motorbike);
        SMailPostalPersonnel personnel = new SMailDogmaticPostalPersonnel().asTraining();
        SMailDeliveryDepartment deliveryDepartment = new SMailDeliveryDepartment(parkingLot, personnel);
        return new PostOffice(deliveryDepartment);
    }

    protected String preparePlainBody() {
        final InputStream ins = DfResourceUtil.getResourceStream(BODY_ONLY_ML);
        return new FileTextIO().encodeAsUTF8().read(ins);
    }

    protected Map<String, Object> prepareVariableMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("memberName", "jflute");
        map.put("birthdate", currentLocalDate());
        return map;
    }
}
