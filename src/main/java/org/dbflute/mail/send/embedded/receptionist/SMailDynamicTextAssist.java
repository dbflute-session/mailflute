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
package org.dbflute.mail.send.embedded.receptionist;

import org.dbflute.optional.OptionalThing;

/**
 * @author jflute
 * @since 0.4.0 (2015/05/16 Saturday at nakameguro)
 */
public interface SMailDynamicTextAssist {

    /**
     * Prepare dynamic data for dynamic property and dynamic text.
     * <pre>
     * e.g.
     *  o select data from database by templatePath and other resoures.
     *  o return DB result for this.accept and this.assist()
     * </pre>
     * @param resource The resource of dynamic data. (NotNull)
     * @return The optional dynamic data from e.g. database for accept(), assist(). (NotNull, EmptyAllowed: then no accept(), assist())
     */
    OptionalThing<? extends Object> prepareDynamicData(SMailDynamicDataResource resource);

    /**
     * Accept dynamic property from prepared dynamic data, called if dynamic data exists.
     * @param resource The resource of dynamic property. (NotNull)
     * @param dynamicPropAcceptor The acceptor for dynamic property of postcard. (NotNull)
     */
    default void accept(SMailDynamicPropResource resource, SMailDynamicPropAcceptor dynamicPropAcceptor) {
    }

    /**
     * Assist dynamic text from prepared dynamic data, called if dynamic data exists. <br>
     * This method may be called twice in one mail sending, for plain text and html text.
     * @param resource The resource of dynamic text. (NotNull)
     * @return The optional dynamic text from e.g. database. (NotNull, EmptyAllowed: means no dynamic text)
     */
    OptionalThing<String> assist(SMailDynamicTextResource resource);
}
