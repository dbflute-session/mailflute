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

import javax.mail.Address;

import org.dbflute.mailflute.send.SMailPost;
import org.dbflute.mailflute.send.SMailSender;
import org.dbflute.mailflute.send.SMailSession;
import org.dbflute.mailflute.send.SMailSessionHolder;
import org.dbflute.mailflute.send.simple.SMailSimpleJapaneseSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @author Takeshi Kato
 * @since 1.0.0 (2015/01/12 Monday at higashi-ginza)
 */
public class MailKicker {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger LOG = LoggerFactory.getLogger(MailKicker.class);

    // ===================================================================================
    //                                                                           Send mail
    //                                                                    ================
	public void kick(MailFlutist flutist) {
		SMailSender sender = getSMailSender();

		SMailPost post = new SMailPost();
		
		post.setSubject(flutist.getSubject());
		post.setPlainBody(flutist.getPlainBody());
		post.setHtmlBody(flutist.getHtmlBody());

		post.setFrom(flutist.getFromAddress());
		
		for (Address toAddr : flutist.getToAddressList()) {
			post.addTo(toAddr);
		}
		
		for (Address ccAddr : flutist.getCcAddressList()) {
			post.addTo(ccAddr);
		}
		
		for (Address bccAddr : flutist.getBccAddressList()) {
			post.addTo(bccAddr);
		}
		
		sender.send(post);
	}

	protected SMailSender getSMailSender() { //TODO Takeshi Kato: SmailSenderのインスタンス取得。仮実装
		SMailSessionHolder holder = getMailSessionHandler();

		SMailSession session = new SMailSession();
		session.registerConnectionInfo("localhost", 25);
		// session.registerUserInfo(user, password);
		// session.registerProxy(proxyHost, proxyPort);

		holder.registerSession("main", session);

		SMailSimpleJapaneseSender sender = new SMailSimpleJapaneseSender(holder);
		return sender;
	}

	protected SMailSessionHolder getMailSessionHandler() { //TODO Takeshi Kato: SmailSenderのインスタンス生成。仮実装
		SMailSessionHolder holder = new SMailSessionHolder();
		return holder;
	}
}
