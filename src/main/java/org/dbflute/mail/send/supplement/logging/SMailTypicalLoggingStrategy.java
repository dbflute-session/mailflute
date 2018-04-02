/*
 * Copyright 2015-2018 the original author or authors.
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
import org.dbflute.optional.OptionalThing;
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
    public void logMailBefore(CardView view, SMailPostingDiscloser discloser) {
        showMessage(view, discloser);
    }

    protected void showMessage(CardView view, SMailPostingDiscloser discloser) {
        final boolean messageInfoEnabled = messageLogger.isInfoEnabled();
        final boolean normalDebugEnabled = normalLogger.isDebugEnabled();
        if (!messageInfoEnabled && !normalDebugEnabled) {
            return;
        }
        final String msg = buildMessageDisp(view, discloser);
        if (messageInfoEnabled) {
            messageLogger.info(msg);
        }
        if (normalDebugEnabled) {
            normalLogger.debug(msg);
        }
    }

    protected String buildMessageDisp(CardView view, SMailPostingDiscloser discloser) {
        final String state = discloser.isTraining() ? "as training" : "actually";
        final String hash = toHash(discloser);
        final String disp = discloser.toDisplay();
        return "...Sending mail " + state + ": #" + hash + "\n" + disp;
    }

    @Override
    public void logMailFinally(CardView view, SMailPostingDiscloser discloser, OptionalThing<Exception> cause) {
        showResult(discloser, cause);
    }

    protected void showResult(SMailPostingDiscloser discloser, OptionalThing<Exception> cause) {
        final boolean messageInfoEnabled = messageLogger.isInfoEnabled();
        final boolean normalDebugEnabled = normalLogger.isDebugEnabled();
        if (!messageInfoEnabled && !normalDebugEnabled) {
            return;
        }
        final String msg = buildResultDisp(discloser, cause);
        if (messageInfoEnabled) {
            messageLogger.info(msg);
        }
        if (normalDebugEnabled) {
            normalLogger.debug(msg);
        }
    }

    protected String buildResultDisp(SMailPostingDiscloser discloser, OptionalThing<Exception> cause) {
        // no exception message and stack trace here because the info is catched as error logging
        final String hash = toHash(discloser);
        final String returnExp = doBuildResultDispLastReturnCode(discloser);
        final String responseExp = doBuildResultDispLastServerResponse(discloser);
        final String causeExp = doBuildResultDispCause(cause);
        return "Finished mail: #" + hash + returnExp + responseExp + causeExp;
    }

    protected String doBuildResultDispLastReturnCode(SMailPostingDiscloser discloser) {
        return discloser.getLastReturnCode().map(code -> " return=" + code).orElse("");
    }

    protected String doBuildResultDispLastServerResponse(SMailPostingDiscloser discloser) {
        return discloser.getLastServerResponse().map(res -> " response=" + res.trim()).orElse("");
    }

    protected String doBuildResultDispCause(OptionalThing<Exception> cause) {
        return cause.map(exp -> " *" + exp.getClass().getSimpleName()).orElse("");
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
