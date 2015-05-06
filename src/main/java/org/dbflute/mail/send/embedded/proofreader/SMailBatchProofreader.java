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
package org.dbflute.mail.send.embedded.proofreader;

import java.util.List;
import java.util.Map;

import org.dbflute.mail.send.SMailTextProofreader;

/**
 * @author jflute
 * @since 0.4.0 (2015/05/05 Tuesday at nakameguro)
 */
public class SMailBatchProofreader implements SMailTextProofreader {

    protected final List<SMailTextProofreader> readerList;

    public SMailBatchProofreader(List<SMailTextProofreader> readerList) {
        this.readerList = readerList;
    }

    @Override
    public String proofreader(final String templateText, Map<String, Object> variableMap) {
        String filteredText = templateText;
        for (SMailTextProofreader proofreader : readerList) {
            filteredText = proofreader.proofreader(filteredText, variableMap);
        }
        return filteredText;
    }

    @Override
    public String toString() {
        return "batch:{" + readerList + "}";
    }
}
