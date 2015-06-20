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
import java.util.Locale;
import java.util.Map;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.dbflute.helper.filesystem.FileTextIO;
import org.dbflute.mail.send.SMailDeliveryDepartment;
import org.dbflute.mail.send.SMailPostalMotorbike;
import org.dbflute.mail.send.SMailPostalParkingLot;
import org.dbflute.mail.send.SMailPostalPersonnel;
import org.dbflute.mail.send.embedded.personnel.SMailDogmaticPostalPersonnel;
import org.dbflute.mail.send.exception.SMailTemplateNotFoundException;
import org.dbflute.utflute.core.PlainTestCase;
import org.dbflute.util.DfResourceUtil;
import org.dbflute.util.Srl;

/**
 * @author jflute
 */
public class PostOfficeTest extends PlainTestCase {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final String BODY_ONLY_ML = "office/body_only.dfmail";
    private static final String HEADER_SUBJECT_ML = "office/header_subject.dfmail";
    private static final String OPTION_HTMLEXISTS_ML = "office/option_htmlexists.dfmail";
    private static final String OPTION_HTMLNOFILE_ML = "office/option_htmlnofile.dfmail";
    private static final String RECEIVER_LOCALE_ML = "office/receiver_locale.dfmail";
    private static final String RECEIVER_LOCALENOFILE_ML = "office/receiver_localenofile.dfmail";
    private static final String VARIOUS_LINES_ML = "office/various_lines.dfmail";

    // ===================================================================================
    //                                                                           Body File
    //                                                                           =========
    public void test_deliver_bodyFile_bodyOnly() throws Exception {
        // ## Arrange ##
        Postcard postcard = new Postcard();
        prepareMockAddress(postcard);
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
        prepareMockAddress(postcard);
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
        prepareMockAddress(postcard);
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
        prepareMockAddress(postcard);
        postcard.useBodyFile(OPTION_HTMLNOFILE_ML).useTemplateText(prepareVariableMap());

        // ## Act ##
        try {
            office.deliver(postcard);
            // ## Assert ##
            fail();
        } catch (SMailTemplateNotFoundException e) {
            log(e.getMessage());
        }
    }

    public void test_deliver_directBody() throws Exception {
        // ## Arrange ##
        String plainBody = preparePlainBody();
        Postcard postcard = new Postcard();
        prepareMockAddress(postcard);
        String subject = "Welcome to your source code reading";
        postcard.setSubject(subject);
        Map<String, Object> map = prepareVariableMap();
        postcard.useDirectBody(plainBody).useTemplateText(map);

        // ## Act ##
        prepareOffice().deliver(postcard);

        // ## Assert ##
        doAssertParameter(postcard, map, subject, false);
    }

    // -----------------------------------------------------
    //                                          Small Assist
    //                                          ------------
    protected String preparePlainBody() {
        final String baseDir = SMailDogmaticPostalPersonnel.CLASSPATH_BASEDIR;
        final InputStream ins = DfResourceUtil.getResourceStream(baseDir + "/" + BODY_ONLY_ML);
        assertNotNull(ins);
        return new FileTextIO().encodeAsUTF8().read(ins);
    }

    protected void doAssertParameter(Postcard postcard, Map<String, Object> pmb, String subject, boolean hasHtml) {
        String plain = postcard.toCompletePlainText().get();
        Object birthdate = pmb.get("birthdate");
        assertContainsAll(plain, "jflute", "Today is " + birthdate + ".", "Thanks");
        if (hasHtml) {
            assertContainsAll(postcard.toCompleteHtmlText().get(), "jflute", "Today is " + birthdate + ".", "Thanks");
        } else {
            assertFalse(postcard.toCompleteHtmlText().isPresent());
        }
        assertEquals(postcard.getSubject().get(), subject);
    }

    protected Map<String, Object> prepareVariableMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("memberName", "jflute");
        map.put("birthdate", currentLocalDate());
        return map;
    }

    // ===================================================================================
    //                                                                     Receiver Locale
    //                                                                     ===============
    public void test_deliver_receiver_locale_found() throws Exception {
        // ## Arrange ##
        Postcard postcard = new Postcard();
        prepareMockAddress(postcard);
        String subject = "Welcome to your source code reading";
        postcard.setSubject(subject);
        postcard.useBodyFile(RECEIVER_LOCALE_ML).receiverLocale(Locale.JAPANESE).useTemplateText(prepareVariableMap());

        // ## Act ##
        prepareOffice().deliver(postcard);

        // ## Assert ##
        String plain = postcard.toCompletePlainText().get();
        assertContains(plain, "Konnichiha");
    }

    public void test_deliver_receiver_locale_html() throws Exception {
        // ## Arrange ##
        Postcard postcard = new Postcard();
        prepareMockAddress(postcard);
        String subject = "Welcome to your source code reading";
        postcard.setSubject(subject);
        postcard.useBodyFile(RECEIVER_LOCALE_ML).alsoHtmlFile().receiverLocale(Locale.JAPANESE).useTemplateText(prepareVariableMap());

        // ## Act ##
        prepareOffice().deliver(postcard);

        // ## Assert ##
        String plain = postcard.toCompletePlainText().get();
        assertContains(plain, "Konnichiha");
        String html = postcard.toCompleteHtmlText().get();
        assertContainsAll(html, "<html>", "Konnichiha");
    }

    public void test_deliver_receiver_locale_notFound() throws Exception {
        // ## Arrange ##
        Postcard postcard = new Postcard();
        prepareMockAddress(postcard);
        String subject = "Welcome to your source code reading";
        postcard.setSubject(subject);
        postcard.useBodyFile(RECEIVER_LOCALENOFILE_ML).receiverLocale(Locale.JAPANESE).useTemplateText(prepareVariableMap());

        // ## Act ##
        prepareOffice().deliver(postcard);

        // ## Assert ##
        String plain = postcard.toCompletePlainText().get();
        assertContains(plain, "Hello");
        assertNotContains(plain, "Konnichiha");
    }

    // ===================================================================================
    //                                                                       Line Handling
    //                                                                       =============
    public void test_deliver_various_lines() throws Exception {
        // ## Arrange ##
        Postcard postcard = new Postcard();
        prepareMockAddress(postcard);
        String subject = "Welcome to your source code reading";
        postcard.setSubject(subject);
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("sea", "exists");
        map.put("land", null);
        map.put("iks", newArrayList("a", "b", "c"));
        map.put("amba", null);
        postcard.useBodyFile(VARIOUS_LINES_ML).useTemplateText(map);

        // ## Act ##
        prepareOffice().deliver(postcard);

        // ## Assert ##
        String plain = postcard.toCompletePlainText().get();
        assertEquals(Srl.removeEmptyLine(plain).length(), plain.length()); // expects no empty line
    }

    // ===================================================================================
    //                                                                         Test Helper
    //                                                                         ===========
    protected void prepareMockAddress(Postcard postcard) throws AddressException {
        postcard.setFrom(new InternetAddress("sea@example.com"));
        postcard.addTo(new InternetAddress("land@example.com"));
    }

    protected PostOffice prepareOffice() {
        SMailPostalParkingLot parkingLot = new SMailPostalParkingLot();
        SMailPostalMotorbike motorbike = new SMailPostalMotorbike();
        parkingLot.registerMotorbikeAsMain(motorbike);
        SMailPostalPersonnel personnel = new SMailDogmaticPostalPersonnel().asTraining();
        SMailDeliveryDepartment deliveryDepartment = new SMailDeliveryDepartment(parkingLot, personnel);
        return new PostOffice(deliveryDepartment);
    }
}
