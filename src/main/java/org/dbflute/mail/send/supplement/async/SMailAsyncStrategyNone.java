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
package org.dbflute.mail.send.supplement.async;

import org.dbflute.mail.CardView;

/**
 * @author jflute
 * @since 0.4.0 (2015/06/12 Friday)
 */
public class SMailAsyncStrategyNone implements SMailAsyncStrategy {

    @Override
    public void async(CardView view, Runnable runnable) {
        runnable.run();
    }
}
