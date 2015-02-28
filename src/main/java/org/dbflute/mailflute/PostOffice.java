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
package org.dbflute.mailflute;

import org.dbflute.mailflute.send.SMailDeliveryDepartment;
import org.dbflute.mailflute.send.SMailPostalMotorbike;
import org.dbflute.mailflute.send.SMailPostalParkingLot;
import org.dbflute.mailflute.send.SMailPostalPersonnel;
import org.dbflute.mailflute.send.SMailPostie;
import org.dbflute.mailflute.send.exception.SMailPostCategoryNotFoundException;

/**
 * @author jflute
 * @author Takeshi Kato
 * @since 0.1.0 (2015/01/12 Monday at higashi-ginza)
 */
public class PostOffice {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final SMailDeliveryDepartment sendMailResource;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public PostOffice(SMailDeliveryDepartment sendMailResource) {
        this.sendMailResource = sendMailResource;
    }

    // ===================================================================================
    //                                                                        Deliver Mail
    //                                                                        ============
    public void deliver(Postcard post) {
        callPostie(post).deliver(post);
    }

    protected SMailPostie callPostie(Postcard post) {
        final String category = post.getCategory();
        final SMailPostalParkingLot sessionHolder = sendMailResource.getParkingLot();
        final SMailPostalMotorbike session = sessionHolder.findSession(category);
        assertCategorySessionValid(category, session);
        final SMailPostalPersonnel personnel = sendMailResource.getPersonnel();
        return personnel.selectDeliverer(session, post);

        //SMailSession session = new SMailSession();
        //session.registerConnectionInfo("localhost", 25);
        //// session.registerUserInfo(user, password);
        //// session.registerProxy(proxyHost, proxyPort);
        //holder.registerSession("main", session);
    }

    protected void assertCategorySessionValid(String category, SMailPostalMotorbike session) {
        if (session == null) {
            String msg = "Not found the session for the category: " + category;
            throw new SMailPostCategoryNotFoundException(msg);
        }
    }
}
