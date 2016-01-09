/*
 * Copyright 2015-2016 the original author or authors.
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
package org.dbflute.mail.send;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.dbflute.mail.DeliveryCategory;
import org.dbflute.mail.PostOffice;
import org.dbflute.mail.Postcard;
import org.dbflute.mail.send.exception.SMailIllegalStateException;
import org.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 * @since 0.1.0 (2015/01/20 Tuesday)
 */
public class SMailPostalParkingLot {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final DeliveryCategory MAIN_CATEGORY = new DeliveryCategory("main");

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Map<DeliveryCategory, SMailPostalMotorbike> motorbikeMap = new ConcurrentHashMap<>();

    // ===================================================================================
    //                                                                    Session Handling
    //                                                                    ================
    public SMailPostalMotorbike findMotorbike(Postcard postcard) {
        final DeliveryCategory category = postcard.getDeliveryCategory().orElse(MAIN_CATEGORY);
        postcard.officeManagedLogging(PostOffice.LOGGING_TITLE_SYSINFO, "category", category.getCategory());
        final SMailPostalMotorbike motorbike = motorbikeMap.get(category);
        if (motorbike == null) {
            String msg = "Not found the motorbike (session) by the category: " + category + ", " + motorbikeMap;
            throw new SMailIllegalStateException(msg);
        }
        return motorbike;
    }

    public void registerMotorbike(DeliveryCategory category, SMailPostalMotorbike motorbike) {
        motorbikeMap.put(category, motorbike);
    }

    public void registerMotorbikeAsMain(SMailPostalMotorbike motorbike) {
        registerMotorbike(MAIN_CATEGORY, motorbike);
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return DfTypeUtil.toClassTitle(this) + ":{" + motorbikeMap + "}@" + Integer.toHexString(hashCode());
    }
}
