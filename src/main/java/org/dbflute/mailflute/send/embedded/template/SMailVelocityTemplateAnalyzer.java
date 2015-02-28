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
package org.dbflute.mailflute.send.embedded.template;

import java.util.Map;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Transport;

import org.dbflute.mailflute.Postcard;
import org.dbflute.mailflute.send.SMailMessage;
import org.dbflute.mailflute.send.SMailDeliverer;
import org.dbflute.mailflute.send.SMailSession;
import org.dbflute.mailflute.send.SMailTemplateAnalyzer;
import org.dbflute.mailflute.send.exception.SMailTransportFailureException;

/**
 * @author jflute
 * @author Takeshi Kato
 * @since 0.1.0 (2015/02/28 Saturday at nakameguro)
 */
public class SMailVelocityTemplateAnalyzer implements SMailTemplateAnalyzer {

    @Override
    public String analyzeTemplate(String templatePath, Map<String, Object> contextMap) {
        // TODO jflute mailflute: velocity
        return null;
    }
}
