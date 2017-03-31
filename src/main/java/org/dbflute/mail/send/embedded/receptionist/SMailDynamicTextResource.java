/*
 * Copyright 2015-2016 the original author or authors.
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
public class SMailDynamicTextResource {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final CardView cardView; // not null
    protected final String templatePath; // not null
    protected final boolean html;
    protected final boolean filesystem;
    protected final OptionalThing<Locale> receiverLocale; // not null, empty allowed
    protected final Object dynamicData; // not null

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SMailDynamicTextResource(CardView cardView, String templatePath, boolean html, boolean filesystem,
            OptionalThing<Locale> receiverLocale, Object dynamicData) {
        this.cardView = cardView;
        this.templatePath = templatePath;
        this.html = html;
        this.filesystem = filesystem;
        this.receiverLocale = receiverLocale;
        this.dynamicData = dynamicData;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() { // card view is not main property
        return "{" + templatePath + ", html=" + html + ", filesystem=" + filesystem + ", " + receiverLocale + "}";
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

    public boolean isHtml() {
        return html;
    }

    public boolean isFilesystem() {
        return filesystem;
    }

    public OptionalThing<Locale> getReceiverLocale() {
        return receiverLocale;
    }

    public Object getDynamicData() {
        return dynamicData;
    }
}
