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

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.mail.Address;

import org.dbflute.mail.send.supplement.attachment.SMailAttachment;
import org.dbflute.optional.OptionalThing;

/**
 * @author jflute
 * @since 0.4.0 (2015/06/12 Friday)
 */
public interface CardView {

    // ===================================================================================
    //                                                                    Postcard Request
    //                                                                    ================
    OptionalThing<DeliveryCategory> getDeliveryCategory();

    OptionalThing<String> getSubject();

    OptionalThing<Address> getFrom();

    List<Address> getToList();

    List<Address> getCcList();

    List<Address> getBccList();

    List<Address> getReplyToList();

    Map<String, SMailAttachment> getAttachmentMap();

    // ===================================================================================
    //                                                                           Body File
    //                                                                           =========
    OptionalThing<String> getBodyFile();

    boolean hasBodyFile();

    boolean isAlsoHtmlFile();

    boolean isFromFilesystem();

    OptionalThing<Locale> getReceiverLocale();

    boolean hasTemplateVariable();

    Map<String, Object> getTemplaetVariableMap();

    boolean isWholeFixedTextUsed();

    // ===================================================================================
    //                                                                           Body Text
    //                                                                           =========
    OptionalThing<String> getPlainBody();

    boolean hasHtmlBody();

    OptionalThing<String> getHtmlBody();

    // ===================================================================================
    //                                                                             Logging
    //                                                                             =======
    boolean hasPushedLogging();

    Map<String, Object> getPushedLoggingMap();

    boolean hasOfficeManagedLogging();

    Map<String, Map<String, Object>> getOfficeManagedLoggingMap();

    // ===================================================================================
    //                                                                       Postie Option
    //                                                                       =============
    boolean isAsync();

    int getRetryCount();

    long getIntervalMillis();

    boolean isSuppressSendFailure();
}
