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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dbflute.mail.send.SMailTextProofreader;
import org.dbflute.util.DfTypeUtil;
import org.dbflute.util.Srl;
import org.dbflute.util.Srl.ScopeInfo;

/**
 * @author jflute
 * @since 0.4.0 (2015/05/06 Wednesday nakameguro)
 */
public class SMailConfigProofreader implements SMailTextProofreader {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String CONFIG_PREFIX = "/*pmb.config(";
    protected static final String CONFIG_SUFFIX = ")*/";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final SMailConfigResolver resolver;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SMailConfigProofreader(SMailConfigResolver resolver) {
        this.resolver = resolver;
    }

    // ===================================================================================
    //                                                                           Proofread
    //                                                                           =========
    @Override
    public String proofreader(String templateText, Map<String, Object> variableMap) {
        final List<ScopeInfo> scopeList = Srl.extractScopeList(templateText, getConfigPrefix(), getConfigSuffix());
        final Map<String, String> replaceMap = new LinkedHashMap<String, String>(scopeList.size());
        for (ScopeInfo scopeInfo : scopeList) {
            final String configKey = scopeInfo.getContent().trim();
            final String replaceKey = buildConfigReplaceKey(configKey);
            if (replaceMap.containsKey(replaceKey)) {
                continue;
            }
            final String configValue = resolver.get(configKey);
            if (configValue == null) {
                // TODO jflute mailflute: [E] error message when config not found
                String msg = "Not found the config value by the key: " + configKey;
                throw new IllegalStateException(msg);
            }
            replaceMap.put(replaceKey, configValue);
        }
        return Srl.replaceBy(templateText, replaceMap);
    }

    protected String buildConfigReplaceKey(String configKey) {
        return getConfigPrefix() + configKey + getConfigSuffix();
    }

    protected String getConfigPrefix() {
        return CONFIG_PREFIX;
    }

    protected String getConfigSuffix() {
        return CONFIG_SUFFIX;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "proofreader:{" + DfTypeUtil.toClassTitle(resolver) + "}";
    }
}
