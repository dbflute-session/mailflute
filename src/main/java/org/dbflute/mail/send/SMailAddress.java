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
package org.dbflute.mail.send;

import org.dbflute.optional.OptionalThing;

/**
 * @author jflute
 * @since 0.4.0 (2015/06/28 Sunday)
 */
public class SMailAddress {

    protected final String address;
    protected final String personal; // null allowed

    public SMailAddress(String address, String personal) {
        if (address == null) {
            throw new IllegalArgumentException("The argument 'address' should not be null.");
        }
        this.address = address;
        this.personal = personal;
    }

    public String getAddress() {
        return address;
    }

    public OptionalThing<String> getPersonal() {
        return OptionalThing.ofNullable(personal, () -> {
            throw new IllegalStateException("Not found the personal for the address: " + address);
        });
    }
}
