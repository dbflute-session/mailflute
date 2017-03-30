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
package org.dbflute.mail.send.embedded.receptionist;

import org.dbflute.mail.CardView;
import org.dbflute.optional.OptionalThing;

/**
 * @author jflute
 * @since 0.4.0 (2015/05/16 Saturday at nakameguro)
 */
public interface SMailDynamicTextAssist {

    /**
     * Prepare dynamic data, and set dynamic properties.
     * <pre>
     * e.g.
     *  o DB access by templatePath
     *  o set dynamic properties by acceptor
     *  o return DB result for dynamic text this.assist()
     * </pre>
     * @param cardView The view of postcard. (NotNull)
     * @param templatePath The path of template file. (NotNull)
     * @param dynamicPropAcceptor The acceptor for dynamic property of postcard. (NotNull)
     * @return The optional dynamic data from e.g. database for assist(). (NullAllowed: means no dynamic data in assist())
     */
    default OptionalThing<Object> prepareDynamicData(CardView cardView, String templatePath, SMailDynamicPropAcceptor dynamicPropAcceptor) {
        return OptionalThing.empty(); // as default
    }

    /**
     * Assist dynamic text from e.g. database by resource. <br>
     * This method may be called twice in one mail sending, for plain text and html text.
     * @param resource The resource of dynamic text. (NotNull)
     * @return The optional dynamic text from e.g. database. (NotNull, EmptyAllowed: means no dynamic text)
     */
    OptionalThing<String> assist(SMailDynamicTextResource resource);
}
