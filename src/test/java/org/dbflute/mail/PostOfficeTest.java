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

import org.dbflute.mail.send.SMailDeliveryDepartment;
import org.dbflute.mail.send.SMailPostalMotorbike;
import org.dbflute.mail.send.SMailPostalParkingLot;
import org.dbflute.mail.send.SMailPostalPersonnel;
import org.dbflute.mail.send.embedded.personnel.SMailDogmaticPostalPersonnel;
import org.dbflute.twowaysql.pmbean.SimpleMapPmb;
import org.dbflute.utflute.core.PlainTestCase;

/**
 * @author jflute
 */
public class PostOfficeTest extends PlainTestCase {

    public void test_deliver_bodyFile() throws Exception {
        // ## Arrange ##
        PostOffice office = prepareOffice();
        Postcard postcard = new Postcard();
        postcard.useBodyFile("mail/office/simple_greeting.ml").fromClasspath();
        postcard.useTemplateBody(preparePmb().getParameterMap());

        // ## Act ##
        office.deliver(postcard);

        // ## Assert ##
        String plain = postcard.getProofreadingPlain();
        assertContainsAll(plain, "jflute", "Thanks");
        assertNull(postcard.getProofreadingHtml());
    }

    public void test_deliver_directBody() throws Exception {
        // ## Arrange ##
        String plainBody = preparePlainBody();
        PostOffice office = prepareOffice();
        Postcard postcard = new Postcard();
        postcard.useDirectBody(plainBody);
        postcard.useTemplateBody(preparePmb().getParameterMap());

        // ## Act ##
        office.deliver(postcard);

        // ## Assert ##
        String plain = postcard.getProofreadingPlain();
        assertContainsAll(plain, "jflute", "Thanks");
        assertNull(postcard.getProofreadingHtml());
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
        StringBuilder sb = new StringBuilder();
        sb.append("Hello, /*$pmb.memberName*/\n");
        sb.append("\n");
        sb.append("How are you?\n");
        sb.append("/*IF pmb.birthdate != null*/\n");
        sb.append("Happy birthdate! Today is /*$pmb.birthdate*/.\n");
        sb.append("/*END*/\n");
        sb.append("\n");
        sb.append("Thanks");
        return sb.toString();
    }

    protected SimpleMapPmb<Object> preparePmb() {
        SimpleMapPmb<Object> pmb = new SimpleMapPmb<Object>();
        pmb.addParameter("memberName", "jflute");
        pmb.addParameter("birthdate", currentLocalDate());
        return pmb;
    }
}
