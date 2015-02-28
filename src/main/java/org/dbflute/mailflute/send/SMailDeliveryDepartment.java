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

/**
 * @author jflute
 * @since 0.1.0 (2015/02/28 Saturday)
 */
public class SMailDeliveryDepartment {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final SMailPostalMotorbikeParkingLot parkingLot;
    protected final SMailPostalPersonnel personnel;
    protected final SMailTemplateScanner templateScanner;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SMailDeliveryDepartment(SMailPostalMotorbikeParkingLot parkingLot, SMailPostalPersonnel personnel, SMailTemplateScanner templateScanner) {
        this.parkingLot = parkingLot;
        this.personnel = personnel;
        this.templateScanner = templateScanner;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public SMailPostalMotorbikeParkingLot getParkingLot() {
        return parkingLot;
    }

    public SMailPostalPersonnel getPersonnel() {
        return personnel;
    }

    public SMailTemplateScanner getTemplateScanner() {
        return templateScanner;
    }
}
