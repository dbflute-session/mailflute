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
import org.dbflute.mail.send.SMailReceptionist;
import org.dbflute.mail.send.SMailTextProofreader;
import org.dbflute.mail.send.embedded.postie.SMailSimplePostie;
import org.dbflute.mail.send.embedded.proofreader.SMailBatchProofreader;
import org.dbflute.mail.send.embedded.proofreader.SMailPmCommentProofreader;
import org.dbflute.mail.send.embedded.receptionist.SMailConventionReceptionist;
import org.dbflute.mail.send.supplement.SMailAddressFilter;
import org.dbflute.optional.OptionalThing;
import org.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 */
public class SMailDogmaticPostalPersonnel implements SMailPostalPersonnel {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String CLASSPATH_BASEDIR = "mail";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final SMailReceptionist receptionist;
    protected final SMailTextProofreader proofreader;
    protected final OptionalThing<SMailAddressFilter> addressFilter;
    protected boolean training;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SMailDogmaticPostalPersonnel() {
        receptionist = createOutsideBodyReceptionist();
        proofreader = createProofreader();
        addressFilter = createAddressFilter();
    }

    public void workingDispose() {
        receptionist.workingDispose();
        proofreader.workingDispose();
    }

    // -----------------------------------------------------
    //                                          Receptionist
    //                                          ------------
    protected SMailReceptionist createOutsideBodyReceptionist() { // you can change it e.g. from database
        return newMailConventionReceptionist().asClasspathBase(CLASSPATH_BASEDIR);
    }

    protected SMailConventionReceptionist newMailConventionReceptionist() {
        return new SMailConventionReceptionist();
    }

    // -----------------------------------------------------
    //                                           Proofreader
    //                                           -----------
    protected SMailTextProofreader createProofreader() {
        final List<SMailTextProofreader> readerList = new ArrayList<SMailTextProofreader>(4);
        setupProofreader(readerList);
        return new SMailBatchProofreader(readerList);
    }

    protected void setupProofreader(List<SMailTextProofreader> readerList) { // you can add yours
        readerList.add(createTemplateProofreader());
    }

    protected SMailTextProofreader createTemplateProofreader() { // you can change it e.g. Velocity
        return newMailPmCommentProofreader();
    }

    protected SMailPmCommentProofreader newMailPmCommentProofreader() {
        return new SMailPmCommentProofreader();
    }

    // -----------------------------------------------------
    //                                        Address Filter
    //                                        --------------
    protected OptionalThing<SMailAddressFilter> createAddressFilter() {
        return OptionalThing.empty();
    }

    // -----------------------------------------------------
    //                                              Training
    //                                              --------
    public SMailDogmaticPostalPersonnel asTraining() {
        training = true;
        return this;
    }

    // ===================================================================================
    //                                                                              Select
    //                                                                              ======
    // -----------------------------------------------------
    //                                          Receptionist
    //                                          ------------
    @Override
    public SMailReceptionist selectReceptionist(Postcard postcard) {
        return receptionist;
    }

    // -----------------------------------------------------
    //                                           Proofreader
    //                                           -----------
    @Override
    public SMailTextProofreader selectProofreader(Postcard postcard) {
        return proofreader;
    }

    // -----------------------------------------------------
    //                                                Postie
    //                                                ------
    @Override
    public SMailPostie selectPostie(Postcard postcard, SMailPostalMotorbike motorbike) {
        final SMailSimplePostie postie = newSMailSimplePostie(motorbike);
        addressFilter.ifPresent(filter -> {
            postie.withAddressFilter(filter);
        });
        return training ? postie.asTraining() : postie;
    }

    protected SMailSimplePostie newSMailSimplePostie(SMailPostalMotorbike motorbike) {
        return new SMailSimplePostie(motorbike);
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(DfTypeUtil.toClassTitle(this));
        sb.append(":{").append(receptionist);
        sb.append(", ").append(proofreader).append((training ? ", *training" : ""));
        sb.append("}@").append(Integer.toHexString(hashCode()));
        return sb.toString();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public boolean isTraining() {
        return training;
    }
}
