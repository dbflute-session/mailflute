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
package org.dbflute.mail.send.embedded.postie;

import org.dbflute.mail.send.SMailPostalMotorbike;

/**
 * @author jflute
 * @since 0.4.0 (2015/05/05 Tuesday)
 */
public class SMailSimpleGlobalPostie extends SMailSimpleBasePostie {

    private static final String UTF8 = "UTF-8";

    public SMailSimpleGlobalPostie(SMailPostalMotorbike motorbike) {
        super(motorbike);
    }

    @Override
    protected String getEncoding() {
        return UTF8;
    }
}
