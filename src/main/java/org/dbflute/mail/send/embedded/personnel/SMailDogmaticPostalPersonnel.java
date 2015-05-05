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
package org.dbflute.mail.send.embedded.personnel;

import org.dbflute.mail.Postcard;
import org.dbflute.mail.send.SMailPostalMotorbike;
import org.dbflute.mail.send.SMailPostalPersonnel;
import org.dbflute.mail.send.SMailPostie;
import org.dbflute.mail.send.SMailTextProofreader;
import org.dbflute.mail.send.embedded.postie.SMailSimpleGlobalPostie;
import org.dbflute.mail.send.embedded.proofreader.SMailParameterCommentTextProofreader;
import org.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 */
public class SMailDogmaticPostalPersonnel implements SMailPostalPersonnel {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final SMailParameterCommentTextProofreader PROOFREADER = new SMailParameterCommentTextProofreader();

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected boolean training;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SMailDogmaticPostalPersonnel asTraining() {
        training = true;
        return this;
    }

    // ===================================================================================
    //                                                                              Select
    //                                                                              ======
    @Override
    public SMailTextProofreader selectProofreader(Postcard postcard) {
        return PROOFREADER;
    }

    @Override
    public SMailPostie selectPostie(Postcard postcard, SMailPostalMotorbike motorbike) {
        final SMailSimpleGlobalPostie postie = newSMailSimpleGlobalPostie(motorbike);
        return training ? postie.asTraining() : postie;
    }

    protected SMailSimpleGlobalPostie newSMailSimpleGlobalPostie(SMailPostalMotorbike motorbike) {
        return new SMailSimpleGlobalPostie(motorbike);
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final String hash = Integer.toHexString(hashCode());
        return DfTypeUtil.toClassTitle(this) + ":{" + PROOFREADER + (training ? ", *training" : "") + "}@" + hash;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public boolean isTraining() {
        return training;
    }
}
