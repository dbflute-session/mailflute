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
package org.dbflute.mailflute.send;

import org.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 * @since 0.1.0 (2015/02/28 Saturday)
 */
public class SMailDeliveryDepartment {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final SMailPostalParkingLot parkingLot;
    protected final SMailPostalPersonnel personnel;
    protected final SMailTextProofreader proofreader;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SMailDeliveryDepartment(SMailPostalParkingLot parkingLot, SMailPostalPersonnel personnel, SMailTextProofreader proofreader) {
        this.parkingLot = parkingLot;
        this.personnel = personnel;
        this.proofreader = proofreader;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return DfTypeUtil.toClassTitle(this) + ":{" + parkingLot + ", " + personnel + ", " + proofreader + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public SMailPostalParkingLot getParkingLot() {
        return parkingLot;
    }

    public SMailPostalPersonnel getPersonnel() {
        return personnel;
    }

    public SMailTextProofreader getProofreader() {
        return proofreader;
    }
}
