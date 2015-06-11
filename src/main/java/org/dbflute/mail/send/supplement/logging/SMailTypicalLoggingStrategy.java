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
package org.dbflute.mail.send.supplement.logging;

import org.dbflute.mail.Postcard;
import org.dbflute.mail.send.embedded.postie.SMailPostingMessage;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 0.4.0 (2015/06/11 Thursday)
 */
public class SMailTypicalLoggingStrategy implements SMailLoggingStrategy {

    private static final Logger messageLogger = LoggerFactory.getLogger("mailflute.sending");
    private static final Logger normalLogger = LoggerFactory.getLogger(SMailTypicalLoggingStrategy.class);

    @Override
    public void logMailMessage(Postcard postcard, SMailPostingMessage message, boolean training) {
        if (messageLogger.isInfoEnabled()) {
            final String state = training ? "as training" : "actually";
            final String disp = message.toDisplay();
            final String hash = toHash(message);
            messageLogger.info("...Sending mail {}: #{}\n{}", state, hash, disp);
        }
    }

    protected String toHash(SMailPostingMessage message) {
        return Integer.toHexString(message.hashCode());
    }

    @Override
    public void logRetrySuccess(Postcard postcard, SMailPostingMessage message, boolean training, int challengeCount, Exception firstCause) {
        if (normalLogger.isInfoEnabled()) {
            final String causeMsg = firstCause.getMessage();
            final String causeExp = causeMsg != null ? Srl.substringFirstFront(causeMsg.trim(), "\n").trim() : null;
            normalLogger.info("Successful mail by retry: #{} challengeCount={} postcard={} cause={}", toHash(message), challengeCount,
                    postcard, causeExp);
        }
    }

    @Override
    public void logSuppressedCause(Postcard postcard, SMailPostingMessage message, boolean training, Exception suppressedCause) {
        if (normalLogger.isWarnEnabled()) {
            normalLogger.warn("Failed to send the mail but continued: #{} {}", toHash(message), postcard, suppressedCause);
        }
    }
}
