/*
 * Copyright 2015-2024 the original author or authors.
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.dbflute.mail.send.SMailAddress;
import org.dbflute.mail.send.SMailDeliveryDepartment;
import org.dbflute.mail.send.SMailPostalMotorbike;
import org.dbflute.mail.send.SMailPostalParkingLot;
import org.dbflute.mail.send.SMailPostalPersonnel;
import org.dbflute.mail.send.embedded.personnel.SMailDogmaticPostalPersonnel;
import org.dbflute.twowaysql.exception.EmbeddedVariableCommentParameterNullValueException;
import org.dbflute.utflute.core.PlainTestCase;

import jakarta.mail.internet.AddressException;

/**
 * @author jflute
 */
public class BindOptionTest extends PlainTestCase {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final String FORMATAS_ML = "office/bindop/bindoption_formatas.dfmail";
    private static final String ORELSE_ML = "office/bindop/bindoption_orelse.dfmail";

    // ===================================================================================
    //                                                                          formatAs()
    //                                                                          ==========
    public void test_bindOption_formatAs_basic() throws Exception {
        // ## Arrange ##
        Postcard postcard = new Postcard();
        prepareMockAddress(postcard);
        LocalDateTime current = currentLocalDateTime();
        Map<String, Object> map = newHashMap("birthdate", current.toLocalDate(), "formalizedDatetime", current);
        postcard.useBodyFile(FORMATAS_ML).useTemplateText(map);

        // ## Act ##
        prepareOffice().deliver(postcard);

        // ## Assert ##
        String plain = postcard.toCompletePlainText().get();
        String subject = postcard.getOfficePostingDiscloser().get().getSavedSubject().get();
        log(ln() + subject + ln() + plain);
        assertContains(subject, current.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
        assertContains(plain, current.format(DateTimeFormatter.ofPattern("yyyy+MM+dd")));
        assertContains(plain, current.format(DateTimeFormatter.ofPattern("yyyy@MM@dd HH:mm:ss.SSS")));
    }

    public void test_bindOption_formatAs_null() throws Exception {
        // ## Arrange ##
        Postcard postcard = new Postcard();
        prepareMockAddress(postcard);
        Map<String, Object> map = newHashMap("birthdate", null, "formalizedDatetime", null);
        postcard.useBodyFile(FORMATAS_ML).useTemplateText(map);

        // ## Act ##
        // ## Assert ##
        assertException(EmbeddedVariableCommentParameterNullValueException.class, () -> prepareOffice().deliver(postcard));
    }

    // ===================================================================================
    //                                                                            orElse()
    //                                                                            ========
    public void test_bindOption_orElse_exists() throws Exception {
        // ## Arrange ##
        Postcard postcard = new Postcard();
        prepareMockAddress(postcard);
        Map<String, Object> map = newHashMap("memberName", "mystic", "memberAccount", "oneman");
        postcard.useBodyFile(ORELSE_ML).useTemplateText(map);

        // ## Act ##
        prepareOffice().deliver(postcard);

        // ## Assert ##
        String plain = postcard.toCompletePlainText().get();
        String subject = postcard.getOfficePostingDiscloser().get().getSavedSubject().get();
        log(ln() + subject + ln() + plain);
        assertContains(subject, "mystic");
        assertContains(plain, "mystic");
        assertContains(plain, "oneman");
    }

    public void test_bindOption_orElse_null() throws Exception {
        // ## Arrange ##
        Postcard postcard = new Postcard();
        prepareMockAddress(postcard);
        Map<String, Object> map = newHashMap("memberName", null, "memberAccount", null);
        postcard.useBodyFile(ORELSE_ML).useTemplateText(map);

        // ## Act ##
        prepareOffice().deliver(postcard);

        // ## Assert ##
        String plain = postcard.toCompletePlainText().get();
        String subject = postcard.getOfficePostingDiscloser().get().getSavedSubject().get();
        log(ln() + subject + ln() + plain);
        assertContains(subject, "sea");
        assertContains(plain, "land");
        assertContains(plain, "piari");
    }

    // ===================================================================================
    //                                                                         Test Helper
    //                                                                         ===========
    protected void prepareMockAddress(Postcard postcard) throws AddressException {
        postcard.setFrom(new SMailAddress("sea@example.com", "Sea"));
        postcard.addTo(new SMailAddress("land@example.com", "Land"));
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
