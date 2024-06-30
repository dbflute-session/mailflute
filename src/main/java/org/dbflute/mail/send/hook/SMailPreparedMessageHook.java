/*
 * Copyright 2015-2024 the original author or authors.
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
package org.dbflute.mail.send.hook;

import org.dbflute.mail.CardView;
import org.dbflute.mail.send.supplement.SMailPostingDiscloser;

/**
 * @author jflute
 * @since 0.5.2 (2016/12/04 Sunday)
 */
public interface SMailPreparedMessageHook {

    void hookPreparedMessage(CardView cardView, SMailPostingDiscloser discloser);

    /**
     * Does it inherit the existing hook? <br>
     * Completely overriding as default but you can inherit it by this determination.
     * @return The determination, true or false.
     */
    default boolean inheritsExistingHook() {
        return true;
    }
}
