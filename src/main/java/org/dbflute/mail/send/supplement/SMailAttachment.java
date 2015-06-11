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
package org.dbflute.mail.send.supplement;

import java.io.InputStream;

/**
 * @author jflute
 * @since 0.4.0 (2015/05/18 Monday)
 */
public class SMailAttachment {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String filenameOnHeader;
    protected final String contentType;
    protected final InputStream reourceStream;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SMailAttachment(String filenameOnHeader, String contentType, InputStream resourceStream) {
        assertArgumentNotNull("filenameOnHeader", filenameOnHeader);
        assertArgumentNotNull("contentType", contentType);
        assertArgumentNotNull("reourceStream", resourceStream);
        this.filenameOnHeader = filenameOnHeader;
        this.contentType = contentType;
        this.reourceStream = resourceStream;
    }

    // ===================================================================================
    //                                                                        Small Helper
    //                                                                        ============
    protected void assertArgumentNotNull(String variableName, Object value) {
        if (variableName == null) {
            throw new IllegalArgumentException("The variableName should not be null.");
        }
        if (value == null) {
            throw new IllegalArgumentException("The argument '" + variableName + "' should not be null.");
        }
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "{" + filenameOnHeader + ", " + contentType + ", " + reourceStream + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getFilenameOnHeader() {
        return filenameOnHeader;
    }

    public String getContentType() {
        return contentType;
    }

    public InputStream getReourceStream() {
        return reourceStream;
    }
}
