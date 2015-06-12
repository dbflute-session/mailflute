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

import org.dbflute.mail.CardView;
import org.dbflute.mail.send.supplement.SMailPostingDiscloser;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 0.4.0 (2015/06/11 Thursday)
 */
public class SMailTypicalLoggingStrategy implements SMailLoggingStrategy {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String MESSAGE_LOGGER_NAME = "mailflute.sending";
    private static final Logger messageLogger = LoggerFactory.getLogger(MESSAGE_LOGGER_NAME);
    private static final Logger normalLogger = LoggerFactory.getLogger(SMailTypicalLoggingStrategy.class);

    // ===================================================================================
    //                                                                        Mail Message
    //                                                                        ============
    @Override
    public void logMailMessage(CardView view, SMailPostingDiscloser discloser) {
        if (messageLogger.isInfoEnabled()) {
            messageLogger.info(buildMailMessageDisp(view, discloser));
        }
    }

    protected String buildMailMessageDisp(CardView view, SMailPostingDiscloser discloser) {
        final String state = discloser.isTraining() ? "as training" : "actually";
        final String hash = toHash(discloser);
        final String disp = discloser.toDisplay();
        return "...Sending mail " + state + ": #" + hash + "\n" + disp;
    }

    // ===================================================================================
    //                                                                       Retry Success
    //                                                                       =============
    @Override
    public void logRetrySuccess(CardView view, SMailPostingDiscloser discloser, int challengeCount, Exception firstCause) {
        if (normalLogger.isInfoEnabled()) {
            normalLogger.info(buildRetrySuccessDisp(view, discloser, challengeCount, firstCause));
        }
    }

    protected String buildRetrySuccessDisp(CardView view, SMailPostingDiscloser discloser, int challengeCount, Exception firstCause) {
        final String hash = toHash(discloser);
        final String causeExp = buildCauseExp(firstCause);
        return "Successful mail by retry: #" + hash + " challengeCount=" + challengeCount + " postcard=" + view + " cause=" + causeExp;
    }

    protected String buildCauseExp(Exception firstCause) {
        final String tmp = firstCause.getMessage();
        return tmp != null ? Srl.substringFirstFront(tmp.trim(), "\n").trim() : null;
    }

    // ===================================================================================
    //                                                                    Suppressed Cause
    //                                                                    ================
    @Override
    public void logSuppressedCause(CardView view, SMailPostingDiscloser discloser, Exception suppressedCause) {
        if (normalLogger.isWarnEnabled()) {
            normalLogger.warn(buildSuppressedCauseDisp(view, discloser, suppressedCause), suppressedCause);
        }
    }

    protected String buildSuppressedCauseDisp(CardView view, SMailPostingDiscloser discloser, Exception suppressedCause) {
        return "Failed to send the mail but continued: #" + toHash(discloser) + " " + view;
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    protected String toHash(SMailPostingDiscloser discloser) {
        return discloser.toHash();
    }
}
