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
package org.dbflute.mailflute;

import java.util.List;

import javax.mail.Address;

/**
 * @author jflute
 * @author Takeshi Kato
 * @since 1.0.0 (2015/01/12 Monday at higashi-ginza)
 */
public interface MailFlutist {
	
    Address getFromAddress();
    
    List<Address> getToAddressList();
    
    List<Address> getCcAddressList();
    
    List<Address> getBccAddressList();
    
    String getSubject();
    
    String getPlainBody();
    
    String getHtmlBody();
}
