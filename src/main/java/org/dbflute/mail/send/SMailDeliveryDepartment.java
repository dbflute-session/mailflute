/*
 * Copyright 2015-2018 the original author or authors.
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

import org.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 * @since 0.1.0 (2015/02/28 Saturday)
 */
public class SMailDeliveryDepartment {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final SMailPostalParkingLot parkingLot; // not null
    protected final SMailPostalPersonnel personnel; // not null

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SMailDeliveryDepartment(SMailPostalParkingLot parkingLot, SMailPostalPersonnel personnel) {
        assertArgumentNotNull("parkingLot", parkingLot);
        assertArgumentNotNull("personnel", personnel);
        this.parkingLot = parkingLot;
        this.personnel = personnel;
    }

    protected void assertArgumentNotNull(String title, Object value) {
        if (value == null) {
            throw new IllegalStateException("The argument '" + title + "' should not be null.");
        }
    }

    // ===================================================================================
    //                                                                             Dispose
    //                                                                             =======
    public void workingDispose() {
        personnel.workingDispose();
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return DfTypeUtil.toClassTitle(this) + ":{" + parkingLot + ", " + personnel + "}";
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
}
