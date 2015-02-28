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
package org.dbflute.mailflute.send;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 0.1.0 (2015/01/20 Tuesday)
 */
public class SMailPostalParkingLot {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger LOG = LoggerFactory.getLogger(SMailPostalParkingLot.class);
    private static final String DEFAULT_CATEGORY = "main";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Map<String, SMailPostalMotorbike> sessionMap = new ConcurrentHashMap<String, SMailPostalMotorbike>();

    // ===================================================================================
    //                                                                    Session Handling
    //                                                                    ================
    public SMailPostalMotorbike findSession(String category) {
        return sessionMap.get(category != null ? category : DEFAULT_CATEGORY);
    }

    public void registerSession(String category, SMailPostalMotorbike session) {
        LOG.info("...Registering mail session: {}, {}", category, session);
        sessionMap.put(category, session);
    }
}
