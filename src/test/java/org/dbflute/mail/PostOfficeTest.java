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
import org.dbflute.mail.send.embedded.proofreader.SMailConfigResolver;
import org.dbflute.utflute.core.PlainTestCase;
import org.dbflute.util.DfResourceUtil;

/**
 * @author jflute
 */
public class PostOfficeTest extends PlainTestCase {

    private static final String SIMPLE_GREETING_ML = "mail/office/simple_greeting.ml";

    public void test_deliver_bodyFile() throws Exception {
        // ## Arrange ##
        PostOffice office = prepareOffice();
        Postcard postcard = new Postcard();
        Map<String, Object> map = prepareVariableMap();
        postcard.useBodyFile(SIMPLE_GREETING_ML).useTemplateText(map);

        // ## Act ##
        office.deliver(postcard);

        // ## Assert ##
        doAssertParameter(postcard, map);
    }

    public void test_deliver_directBody() throws Exception {
        // ## Arrange ##
        String plainBody = preparePlainBody();
        PostOffice office = prepareOffice();
        Postcard postcard = new Postcard();
        Map<String, Object> map = prepareVariableMap();
        postcard.useDirectBody(plainBody).useTemplateText(map);

        // ## Act ##
        office.deliver(postcard);

        // ## Assert ##
        doAssertParameter(postcard, map);
    }

    protected void doAssertParameter(Postcard postcard, Map<String, Object> pmb) {
        String plain = postcard.getProofreadingPlain();
        Object birthdate = pmb.get("birthdate");
        assertContainsAll(plain, "jflute", "abc@example.com", "Today is " + birthdate + ".", "Thanks");
        assertNull(postcard.getProofreadingHtml());
    }

    // ===================================================================================
    //                                                                         Test Helper
    //                                                                         ===========
    protected PostOffice prepareOffice() {
        SMailPostalParkingLot parkingLot = new SMailPostalParkingLot();
        SMailPostalMotorbike motorbike = new SMailPostalMotorbike();
        parkingLot.registerMotorbikeAsMain(motorbike);
        Map<String, String> configMap = new LinkedHashMap<String, String>();
        configMap.put("mail.from", "abc@example.com");
        configMap.put("mail.to", "stu@example.com");
        SMailPostalPersonnel personnel = new SMailDogmaticPostalPersonnel(new SMailConfigResolver() {
            public String get(String key) {
                return configMap.get(key);
            }
        }).asTraining();
        SMailDeliveryDepartment deliveryDepartment = new SMailDeliveryDepartment(parkingLot, personnel);
        return new PostOffice(deliveryDepartment);
    }

    protected String preparePlainBody() {
        final InputStream ins = DfResourceUtil.getResourceStream(SIMPLE_GREETING_ML);
        return new FileTextIO().encodeAsUTF8().read(ins);
    }

    protected Map<String, Object> prepareVariableMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("memberName", "jflute");
        map.put("birthdate", currentLocalDate());
        return map;
    }
}
