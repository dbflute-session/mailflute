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
package org.dbflute.mail.send.supplement.label;

import java.util.Locale;

import org.dbflute.mail.CardView;

/**
 * @author jflute
 * @since 0.4.0 (2015/06/24 Wednesday)
 */
public interface SMailLabelStrategy {

    /**
     * @param view The view of postcard, you can access the attributes. (NotNull)
     * @param locale The locale for filtering. (NotNull)
     * @param label The label to be resolved, may be 'labels.xxx'. (NotNull)
     * @return The resolved label for mail resource, e.g. personal. (NotNull)
     */
    String resolveLabel(CardView view, Locale locale, String label);
}
