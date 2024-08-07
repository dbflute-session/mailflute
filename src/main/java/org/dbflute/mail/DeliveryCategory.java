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
package org.dbflute.mail;

/**
 * @author jflute
 * @since 0.4.0 (2015/05/05 Tuesday at nakameguro)
 */
public class DeliveryCategory {

    protected final String category; // not null

    public DeliveryCategory(String category) {
        if (category == null) {
            throw new IllegalArgumentException("The argument 'category' should not be null.");
        }
        this.category = category;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DeliveryCategory)) {
            return false;
        }
        return category.equals(((DeliveryCategory) obj).category);
    }

    @Override
    public int hashCode() { // needs to find motorbike
        return category.hashCode();
    }

    @Override
    public String toString() {
        return "category:{" + category + "}";
    }

    public String getCategory() {
        return category;
    }
}
