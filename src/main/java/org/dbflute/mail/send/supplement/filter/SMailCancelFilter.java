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
package org.dbflute.mail.send.supplement.filter;

import org.dbflute.mail.CardView;

/**
 * @author jflute
 * @since 0.4.0 (2015/06/12 Friday)
 */
@FunctionalInterface
public interface SMailCancelFilter {

    /**
     * @param view The view of postcard, read-only operation is provided. (NotNull)
     * @return true if the mail should be canceled, then you should log the reason.
     */
    boolean isCancel(CardView view);
}
