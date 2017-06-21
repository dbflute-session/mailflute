/*
 * Copyright 2015-2016 the original author or authors.
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
package org.dbflute.mail.send.supplement.header;

import org.dbflute.mail.CardView;
import org.dbflute.optional.OptionalThing;

/**
 * @author jflute
 * @since 0.5.5 (2017/06/21 Wednesday)
 */
public interface SMailMailHeaderStrategy {

    // ===================================================================================
    //                                                                           Text Part
    //                                                                           =========
    default OptionalThing<String> getTextEncoding(CardView view) {
        return OptionalThing.empty();
    }

    default OptionalThing<String> getTextMimeType(CardView view) {
        return OptionalThing.empty();
    }

    default OptionalThing<String> getTextTransferEncoding(CardView view) {
        return OptionalThing.empty();
    }

    // ===================================================================================
    //                                                                     Attachment Part
    //                                                                     ===============
    default OptionalThing<String> getAttachmentMimeType(CardView view) {
        return OptionalThing.empty();
    }

    default OptionalThing<String> getAttachmentTransferEncoding(CardView view) {
        return OptionalThing.empty();
    }
}
