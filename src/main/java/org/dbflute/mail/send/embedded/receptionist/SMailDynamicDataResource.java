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
package org.dbflute.mail.send.embedded.receptionist;

import java.util.Locale;

import org.dbflute.mail.CardView;
import org.dbflute.optional.OptionalThing;

/**
 * @author jflute
 * @since 0.4.2 (2017/03/31 Friday)
 */
public class SMailDynamicDataResource {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final CardView cardView; // not null
    protected final String templatePath; // not null
    protected final boolean filesystem;
    protected final OptionalThing<Locale> receiverLocale; // not null, empty allowed

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SMailDynamicDataResource(CardView cardView, String templatePath, boolean filesystem, OptionalThing<Locale> receiverLocale) {
        this.cardView = cardView;
        this.templatePath = templatePath;
        this.filesystem = filesystem;
        this.receiverLocale = receiverLocale;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() { // card view is not main property
        return "{" + templatePath + ", filesystem=" + filesystem + ", " + receiverLocale + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public CardView getCardView() {
        return cardView;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public boolean isFilesystem() {
        return filesystem;
    }

    public OptionalThing<Locale> getReceiverLocale() {
        return receiverLocale;
    }
}
