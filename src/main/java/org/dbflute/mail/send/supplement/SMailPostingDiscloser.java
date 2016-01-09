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
package org.dbflute.mail.send.supplement;

import java.util.List;
import java.util.Map;

import javax.mail.Address;
import javax.mail.internet.MimeMessage;

import org.dbflute.mail.send.supplement.attachment.SMailReadAttachedData;
import org.dbflute.optional.OptionalThing;

/**
 * @author jflute
 * @author Takeshi Kato
 * @since 0.4.0 (2015/06/12 Friday)
 */
public interface SMailPostingDiscloser {

    // ===================================================================================
    //                                                                     Basic Attribute
    //                                                                     ===============
    MimeMessage getMimeMessage();

    boolean isTraining();

    Map<String, Object> getPushedLoggingMap();

    Map<String, Map<String, Object>> getOfficeManagedLoggingMap();

    // ===================================================================================
    //                                                                    Display Handling
    //                                                                    ================
    String toDisplay();

    String toHash();

    void makeEmlFile(String path); // called by e.g. logging strategy of application customization

    // ===================================================================================
    //                                                                   Saved for Display
    //                                                                   =================
    OptionalThing<String> getSavedSubject(); // basically present after saving

    OptionalThing<Address> getSavedFrom(); // basically present after saving

    List<Address> getSavedToList();

    List<Address> getSavedCcList();

    List<Address> getSavedBccList();

    List<Address> getSavedReplyToList();

    OptionalThing<String> getSavedReturnPath();

    OptionalThing<String> getSavedPlainText(); // basically present after saving

    OptionalThing<String> getSavedHtmlText(); // formally empty-able

    Map<String, SMailReadAttachedData> getSavedAttachmentMap(); // keyed by filenameOnHeader

    // ===================================================================================
    //                                                                  Finished Transport
    //                                                                  ==================
    OptionalThing<Integer> getLastReturnCode();

    OptionalThing<String> getLastServerResponse();
}
