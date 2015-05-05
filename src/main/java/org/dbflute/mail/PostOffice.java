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

import org.dbflute.helper.filesystem.FileTextIO;
import org.dbflute.mail.send.SMailDeliveryDepartment;
import org.dbflute.mail.send.SMailPostalMotorbike;
import org.dbflute.mail.send.SMailPostalPersonnel;
import org.dbflute.mail.send.SMailPostie;
import org.dbflute.mail.send.SMailTextProofreader;
import org.dbflute.mail.send.exception.SMailDelivertyCategoryNotFoundException;
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
        readOutsideBodyIfNeeds(postcard);
        proofreadIfNeeds(postcard);
        final SMailPostalMotorbike motorbike = fetchMotorbike(postcard);
        final SMailPostie postie = callPostie(postcard, motorbike);
        postie.deliver(postcard);
    }

    protected void readOutsideBodyIfNeeds(Postcard postcard) {
        if (!postcard.isDirectBodyUsed()) {
            final FileTextIO textIO = new FileTextIO().encodeAsUTF8();
            postcard.setPlainBody(textIO.read(postcard.getPlainBody()));
            postcard.setHtmlBody(textIO.read(postcard.getHtmlBody()));
        }
    }

    protected void proofreadIfNeeds(Postcard postcard) {
        if (!postcard.isFixedTextUsed()) {
            final SMailPostalPersonnel personnel = deliveryDepartment.getPersonnel();
            final SMailTextProofreader proofreader = personnel.selectProofreader(postcard);
            postcard.setPlainBody(proofreader.proofreader(postcard.getPlainBody(), postcard.getVariableMap()));
            postcard.setHtmlBody(proofreader.proofreader(postcard.getHtmlBody(), postcard.getVariableMap()));
        }
    }

    protected SMailPostalMotorbike fetchMotorbike(Postcard postcard) {
        final DeliveryCategory category = postcard.getDeliveryCategory();
        final SMailPostalMotorbike motorbike = deliveryDepartment.getParkingLot().findMotorbike(category);
        if (motorbike == null) {
            String msg = "Not found the motorbike for the category: " + category;
            throw new SMailDelivertyCategoryNotFoundException(msg);
        }
        return motorbike;
    }

    protected SMailPostie callPostie(Postcard postcard, SMailPostalMotorbike motorbike) {
        final SMailPostie postie = deliveryDepartment.getPersonnel().selectPostie(postcard, motorbike);
        if (postie == null) {
            String msg = "Not found the postie for the postcard: " + postcard + ", " + motorbike;
            throw new SMailDelivertyCategoryNotFoundException(msg);
        }
        return postie;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return DfTypeUtil.toClassTitle(this) + ":{" + deliveryDepartment + "}@" + Integer.toHexString(hashCode());
    }
}
