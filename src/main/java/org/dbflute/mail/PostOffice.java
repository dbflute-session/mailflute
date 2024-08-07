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

import org.dbflute.mail.send.SMailDeliveryDepartment;
import org.dbflute.mail.send.SMailPostalMotorbike;
import org.dbflute.mail.send.SMailPostie;
import org.dbflute.mail.send.SMailReceptionist;
import org.dbflute.mail.send.SMailTextProofreader;
import org.dbflute.mail.send.embedded.proofreader.SMailBodyMetaProofreader;
import org.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 * @author Takeshi Kato
 * @since 0.1.0 (2015/01/12 Monday at higashi-ginza)
 */
public class PostOffice {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String LOGGING_TITLE_SYSINFO = "sysInfo";
    public static final String LOGGING_TITLE_APPINFO = "appInfo";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final SMailDeliveryDepartment deliveryDepartment;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public PostOffice(SMailDeliveryDepartment deliveryDepartment) {
        if (deliveryDepartment == null) {
            throw new IllegalArgumentException("The argument 'deliveryDepartment' should not be null.");
        }
        this.deliveryDepartment = deliveryDepartment;
    }

    // ===================================================================================
    //                                                                        Deliver Mail
    //                                                                        ============
    public void deliver(Postcard postcard) {
        postcard.officeCheck();

        final SMailReceptionist receptionist = fetchReceptionist(postcard);
        receptionist.accept(postcard); // make body text (may be from body file)

        proofreadIfNeeds(postcard); // make complete text

        final SMailPostalMotorbike motorbike = fetchMotorbike(postcard);
        final SMailPostie postie = fetchPostie(postcard, motorbike);
        postie.deliver(postcard);
    }

    // ===================================================================================
    //                                                                           Proofread
    //                                                                           =========
    protected void proofreadIfNeeds(Postcard postcard) {
        if (needsProofreading(postcard)) {
            final SMailTextProofreader proofreader = fetchProofreader(postcard);
            postcard.proofreadPlain((reading, varMap) -> proofreader.proofread(reading, varMap));
            if (postcard.hasHtmlBody()) {
                postcard.proofreadHtml((reading, varMap) -> proofreader.proofread(reading, varMap));
            }
        }
        if (needsSubjectHeader(postcard)) { // because forcedly-direct requires direct subject
            proofreadSubjectHeader(postcard); // fixed proofreading
        }
    }

    protected boolean needsProofreading(Postcard postcard) {
        return !postcard.isWholeFixedTextUsed() && postcard.hasTemplateVariable();
    }

    protected boolean needsSubjectHeader(Postcard postcard) {
        return !postcard.isForcedlyDirect();
    }

    protected void proofreadSubjectHeader(Postcard postcard) {
        final SMailBodyMetaProofreader proofreader = newMailSubjectHeaderProofreader(postcard);
        postcard.proofreadPlain((plainText, variableMap) -> proofreader.proofread(plainText, variableMap));
    }

    protected SMailBodyMetaProofreader newMailSubjectHeaderProofreader(Postcard postcard) {
        return new SMailBodyMetaProofreader(postcard);
    }

    // ===================================================================================
    //                                                                        Fetch Member
    //                                                                        ============
    protected SMailReceptionist fetchReceptionist(Postcard postcard) {
        return deliveryDepartment.getPersonnel().selectReceptionist(postcard);
    }

    protected SMailTextProofreader fetchProofreader(Postcard postcard) {
        return deliveryDepartment.getPersonnel().selectProofreader(postcard);
    }

    protected SMailPostalMotorbike fetchMotorbike(Postcard postcard) {
        return deliveryDepartment.getParkingLot().findMotorbike(postcard);
    }

    protected SMailPostie fetchPostie(Postcard postcard, SMailPostalMotorbike motorbike) {
        return deliveryDepartment.getPersonnel().selectPostie(postcard, motorbike);
    }

    // ===================================================================================
    //                                                                             Dispose
    //                                                                             =======
    public void workingDispose() {
        deliveryDepartment.workingDispose();
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return DfTypeUtil.toClassTitle(this) + ":{" + deliveryDepartment + "}@" + Integer.toHexString(hashCode());
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public SMailDeliveryDepartment getDeliveryDepartment() {
        return deliveryDepartment;
    }
}
