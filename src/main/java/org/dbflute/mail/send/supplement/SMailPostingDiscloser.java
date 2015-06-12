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
package org.dbflute.mail.send.supplement;

import java.util.List;

import javax.mail.Address;
import javax.mail.internet.MimeMessage;

/**
 * @author jflute
 * @author Takeshi Kato
 * @since 0.4.0 (2015/06/12 Friday)
 */
public interface SMailPostingDiscloser {

    String toDisplay();

    String toHash();

    void makeEmlFile(String path); // called by e.g. logging strategy of application customization

    MimeMessage getMimeMessage();

    Address getSavedFromAddress();

    List<Address> getSavedToAddressList();

    List<Address> getSavedCcAddressList();

    List<Address> getSavedBccAddressList();

    String getSavedSubject();

    String getSavedPlainText();

    String getSavedHtmlText();

    List<String> getSavedAttachmentList(); // list of filenameOnHeader
}
