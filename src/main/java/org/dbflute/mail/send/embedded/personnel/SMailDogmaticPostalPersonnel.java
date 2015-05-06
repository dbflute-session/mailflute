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

import java.util.ArrayList;
import java.util.List;

import org.dbflute.mail.Postcard;
import org.dbflute.mail.send.SMailPostalMotorbike;
import org.dbflute.mail.send.SMailPostalPersonnel;
import org.dbflute.mail.send.SMailPostie;
import org.dbflute.mail.send.SMailTextProofreader;
import org.dbflute.mail.send.embedded.postie.SMailSimpleGlobalPostie;
import org.dbflute.mail.send.embedded.proofreader.SMailBatchProofreader;
import org.dbflute.mail.send.embedded.proofreader.SMailConfigProofreader;
import org.dbflute.mail.send.embedded.proofreader.SMailConfigResolver;
import org.dbflute.mail.send.embedded.proofreader.SMailPmcommentProofreader;
import org.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 */
public class SMailDogmaticPostalPersonnel implements SMailPostalPersonnel {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final SMailTextProofreader proofreader;
    protected boolean training;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SMailDogmaticPostalPersonnel(SMailConfigResolver configResolver) {
        this.proofreader = createProofreader(configResolver);
    }

    protected SMailTextProofreader createProofreader(SMailConfigResolver configResolver) {
        return new SMailBatchProofreader(prepareProofreaderList(configResolver));
    }

    protected List<SMailTextProofreader> prepareProofreaderList(SMailConfigResolver configResolver) {
        final List<SMailTextProofreader> readerList = new ArrayList<SMailTextProofreader>(4);
        readerList.add(newMailConfigProofreader(configResolver));
        readerList.add(newMailPmcommentProofreader());
        return readerList;
    }

    protected SMailConfigProofreader newMailConfigProofreader(SMailConfigResolver configResolver) {
        return new SMailConfigProofreader(configResolver);
    }

    protected SMailPmcommentProofreader newMailPmcommentProofreader() {
        return new SMailPmcommentProofreader();
    }

    public SMailDogmaticPostalPersonnel asTraining() {
        training = true;
        return this;
    }

    // ===================================================================================
    //                                                                              Select
    //                                                                              ======
    @Override
    public SMailTextProofreader selectProofreader(Postcard postcard) {
        return proofreader;
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
        return DfTypeUtil.toClassTitle(this) + ":{" + proofreader + (training ? ", *training" : "") + "}@" + hash;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public boolean isTraining() {
        return training;
    }
}
