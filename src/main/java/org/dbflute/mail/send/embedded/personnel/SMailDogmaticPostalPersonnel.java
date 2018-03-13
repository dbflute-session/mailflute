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
package org.dbflute.mail.send.embedded.personnel;

import java.util.ArrayList;
import java.util.List;

import org.dbflute.mail.Postcard;
import org.dbflute.mail.send.SMailPostalMotorbike;
import org.dbflute.mail.send.SMailPostalPersonnel;
import org.dbflute.mail.send.SMailPostie;
import org.dbflute.mail.send.SMailReceptionist;
import org.dbflute.mail.send.SMailTextProofreader;
import org.dbflute.mail.send.embedded.postie.SMailHonestPostie;
import org.dbflute.mail.send.embedded.proofreader.SMailBatchProofreader;
import org.dbflute.mail.send.embedded.proofreader.SMailPmCommentProofreader;
import org.dbflute.mail.send.embedded.receptionist.SMailConventionReceptionist;
import org.dbflute.mail.send.supplement.async.SMailAsyncStrategy;
import org.dbflute.mail.send.supplement.filter.SMailAddressFilter;
import org.dbflute.mail.send.supplement.filter.SMailBodyTextFilter;
import org.dbflute.mail.send.supplement.filter.SMailCancelFilter;
import org.dbflute.mail.send.supplement.filter.SMailSubjectFilter;
import org.dbflute.mail.send.supplement.header.SMailMailHeaderStrategy;
import org.dbflute.mail.send.supplement.inetaddr.SMailInternetAddressCreator;
import org.dbflute.mail.send.supplement.label.SMailLabelStrategy;
import org.dbflute.mail.send.supplement.logging.SMailLoggingStrategy;
import org.dbflute.mail.send.supplement.retry.SMailRetryStrategy;
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

    // -----------------------------------------------------
    //                                           Deep Option
    //                                           -----------
    protected final OptionalThing<SMailCancelFilter> cancelFilter;
    protected final OptionalThing<SMailAddressFilter> addressFilter;
    protected final OptionalThing<SMailSubjectFilter> subjectFilter;
    protected final OptionalThing<SMailBodyTextFilter> bodyTextFilter;
    protected final OptionalThing<SMailAsyncStrategy> asyncStrategy;
    protected final OptionalThing<SMailRetryStrategy> retryStrategy;
    protected final OptionalThing<SMailLabelStrategy> labelStrategy;
    protected final OptionalThing<SMailLoggingStrategy> loggingStrategy;
    protected final OptionalThing<SMailMailHeaderStrategy> mailHeaderStrategy;
    protected final OptionalThing<SMailInternetAddressCreator> internetAddressCreator;

    // -----------------------------------------------------
    //                                       for Development
    //                                       ---------------
    protected boolean training;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SMailDogmaticPostalPersonnel() {
        receptionist = createReceptionist();
        proofreader = createProofreader();
        cancelFilter = createCancelFilter();
        addressFilter = createAddressFilter();
        subjectFilter = createSubjectFilter();
        bodyTextFilter = createBodyTextFilter();
        asyncStrategy = createAsyncStrategy();
        retryStrategy = createRetryStrategy();
        labelStrategy = createLabelStrategy();
        loggingStrategy = createLoggingStrategy();
        mailHeaderStrategy = createMailHeaderStrategy();
        internetAddressCreator = createInternetAddressCreator();
    }

    public void workingDispose() {
        receptionist.workingDispose();
        proofreader.workingDispose();
    }

    // -----------------------------------------------------
    //                                          Receptionist
    //                                          ------------
    protected SMailReceptionist createReceptionist() {
        return createConventionReceptionist();
    }

    protected SMailConventionReceptionist createConventionReceptionist() { // you can customize it e.g. locale, database
        // you can switch mail template by user locale like this:
        // /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        //  @Override
        //  protected SMailConventionReceptionist createConventionReceptionist() {
        //      return super.createConventionReceptionist().asReceiverLocale(postcard -> {
        //          return OptionalThing.of(requestManager.getUserLocale());
        //      });
        //  }
        // - - - - - - - - - -/
        // 
        // and templates from database:
        // /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        //  @Override
        //  protected SMailConventionReceptionist createConventionReceptionist() {
        //      return super.createConventionReceptionist().asDynamicText((postcard, path, filesystem, receiverLocale) -> {
        //          return ...;
        //      });
        //  }
        // - - - - - - - - - -/
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
    //                                           Deep Option
    //                                           -----------
    protected OptionalThing<SMailCancelFilter> createCancelFilter() {
        return OptionalThing.empty();
    }

    protected OptionalThing<SMailAddressFilter> createAddressFilter() {
        return OptionalThing.empty();
    }

    protected OptionalThing<SMailSubjectFilter> createSubjectFilter() {
        return OptionalThing.empty();
    }

    protected OptionalThing<SMailBodyTextFilter> createBodyTextFilter() {
        return OptionalThing.empty();
    }

    protected OptionalThing<SMailLabelStrategy> createLabelStrategy() {
        return OptionalThing.empty();
    }

    protected OptionalThing<SMailAsyncStrategy> createAsyncStrategy() {
        return OptionalThing.empty();
    }

    protected OptionalThing<SMailRetryStrategy> createRetryStrategy() {
        return OptionalThing.empty();
    }

    protected OptionalThing<SMailLoggingStrategy> createLoggingStrategy() {
        return OptionalThing.empty();
    }

    protected OptionalThing<SMailMailHeaderStrategy> createMailHeaderStrategy() {
        return OptionalThing.empty();
    }

    protected OptionalThing<SMailInternetAddressCreator> createInternetAddressCreator() {
        return OptionalThing.empty();
    }

    // -----------------------------------------------------
    //                                       for Development
    //                                       ---------------
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
        final SMailHonestPostie postie = newMailHonestPostie(motorbike);
        cancelFilter.ifPresent(filter -> postie.withCancelFilter(filter));
        addressFilter.ifPresent(filter -> postie.withAddressFilter(filter));
        subjectFilter.ifPresent(filter -> postie.withSubjectFilter(filter));
        bodyTextFilter.ifPresent(filter -> postie.withBodyTextFilter(filter));
        labelStrategy.ifPresent(filter -> postie.withLabelStrategy(filter));
        asyncStrategy.ifPresent(strategy -> postie.withAsyncStrategy(strategy));
        retryStrategy.ifPresent(strategy -> postie.withRetryStrategy(strategy));
        loggingStrategy.ifPresent(strategy -> postie.withLoggingStrategy(strategy));
        mailHeaderStrategy.ifPresent(strategy -> postie.withMailHeaderStrategy(strategy));
        internetAddressCreator.ifPresent(creator -> postie.withInternetAddressCreator(creator));
        return training ? postie.asTraining() : postie;
    }

    protected SMailHonestPostie newMailHonestPostie(SMailPostalMotorbike motorbike) {
        return new SMailHonestPostie(motorbike);
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(DfTypeUtil.toClassTitle(this));
        sb.append(":{").append(receptionist);
        sb.append(", ").append(proofreader).append(training ? ", *training" : "");
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
