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
import org.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 * @author Takeshi Kato
 * @since 0.1.0 (2015/01/12 Monday at higashi-ginza)
 */
public class PostOffice {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final SMailDeliveryDepartment deliveryDepartment;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public PostOffice(SMailDeliveryDepartment deliveryDepartment) {
        this.deliveryDepartment = deliveryDepartment;
    }

    // ===================================================================================
    //                                                                        Deliver Mail
    //                                                                        ============
    public void deliver(Postcard postcard) {
        callPostie(postcard).deliver(postcard);
    }

    protected SMailPostie callPostie(Postcard postcard) {
        final DeliveryCategory category = postcard.getDeliveryCategory();
        final SMailPostalParkingLot parkingLot = deliveryDepartment.getParkingLot();
        final SMailPostalMotorbike motorbike = parkingLot.findMotorbike(category);
        assertCategorySessionValid(category, motorbike);
        final SMailPostalPersonnel personnel = deliveryDepartment.getPersonnel();
        return personnel.selectPostie(motorbike, postcard);
    }

    protected void assertCategorySessionValid(DeliveryCategory category, SMailPostalMotorbike session) {
        if (session == null) {
            String msg = "Not found the session for the category: " + category;
            throw new SMailPostCategoryNotFoundException(msg);
        }
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return DfTypeUtil.toClassTitle(this) + ":{" + deliveryDepartment + "}@" + Integer.toHexString(hashCode());
    }
}
