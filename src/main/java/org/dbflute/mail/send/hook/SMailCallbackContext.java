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
package org.dbflute.mail.send.hook;

import org.dbflute.mail.CardView;
import org.dbflute.mail.send.supplement.SMailPostingDiscloser;
import org.dbflute.util.DfTypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 0.5.2 (2016/12/04 Sunday)
 */
public class SMailCallbackContext { // from DBFlute CallbackContext

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(SMailCallbackContext.class);

    // ===================================================================================
    //                                                                        Thread Local
    //                                                                        ============
    // -----------------------------------------------------
    //                                         Thread Object
    //                                         -------------
    /** The default thread-local for this. */
    protected static final ThreadLocal<SMailCallbackContext> _defaultThreadLocal = new ThreadLocal<SMailCallbackContext>();

    /** The default holder for callback context, using thread local. (NotNull) */
    protected static final SMailCallbackContextHolder _defaultHolder = new SMailCallbackContextHolder() {

        public SMailCallbackContext provide() {
            return _defaultThreadLocal.get();
        }

        public void save(SMailCallbackContext context) {
            _defaultThreadLocal.set(context);
        }
    };

    /** The holder for callback context, might be changed. (NotNull: null setting is not allowed) */
    protected static SMailCallbackContextHolder _holder = _defaultHolder; // as default

    /** Is this static world locked? e.g. you should unlock it to set your own provider. */
    protected static boolean _locked = true; // at first locked

    /**
     * The holder of for callback context. <br>
     * Basically for asynchronous of web framework e.g. Play2.
     */
    public static interface SMailCallbackContextHolder {

        /**
         * Provide callback context. <br>
         * You should return same instance in same request.
         * @return The instance of callback context. (NullAllowed: when no context, but should exist in real handling)
         */
        SMailCallbackContext provide();

        /**
         * Hold callback context and save it in holder.
         * @param callbackContext The callback context set by static setter. (NullAllowed: if null, context is removed)
         */
        void save(SMailCallbackContext callbackContext);
    }

    // -----------------------------------------------------
    //                                        Basic Handling
    //                                        --------------
    /**
     * Get callback context on thread.
     * @return The context of callback. (NullAllowed)
     */
    public static SMailCallbackContext getCallbackContextOnThread() {
        return getActiveHolder().provide();
    }

    /**
     * Set callback context on thread. <br>
     * You can use setting methods per interface instead of this method.
     * @param callbackContext The context of callback. (NotNull)
     */
    public static void setCallbackContextOnThread(SMailCallbackContext callbackContext) {
        if (callbackContext == null) {
            String msg = "The argument 'callbackContext' must not be null.";
            throw new IllegalArgumentException(msg);
        }
        getActiveHolder().save(callbackContext);
    }

    /**
     * Is existing callback context on thread? <br>
     * You can use determination methods per interface instead of this method.
     * @return The determination, true or false.
     */
    public static boolean isExistCallbackContextOnThread() {
        return getActiveHolder().provide() != null;
    }

    /**
     * Clear callback context on thread. <br>
     * Basically you should call other clear methods per interfaces,
     * because this clear method clears all interfaces. 
     */
    public static void clearCallbackContextOnThread() {
        getActiveHolder().save(null);
    }

    /**
     * Get the active holder for callback context.
     * @return The holder instance to handle callback context. (NotNull)
     */
    protected static SMailCallbackContextHolder getActiveHolder() {
        return _holder;
    }

    // -----------------------------------------------------
    //                                            Management
    //                                            ----------
    /**
     * Use the surrogate holder for callback context. (automatically locked after setting) <br>
     * You should call this in application initialization if it needs.
     * @param holder The holder instance. (NullAllowed: if null, use default holder)
     */
    public static void useSurrogateHolder(SMailCallbackContextHolder holder) {
        assertNotLocked();
        if (_log.isInfoEnabled()) {
            _log.info("...Setting surrogate holder for callback context: " + holder);
        }
        if (holder != null) {
            _holder = holder;
        } else {
            _holder = _defaultHolder;
        }
        _locked = true;
    }

    /**
     * Is this static world locked?
     * @return The determination, true or false.
     */
    public static boolean isLocked() {
        return _locked;
    }

    /**
     * Lock this static world, e.g. not to set the holder of thread-local.
     */
    public static void lock() {
        if (_log.isInfoEnabled()) {
            _log.info("...Locking the static world of the callback context!");
        }
        _locked = true;
    }

    /**
     * Unlock this static world, e.g. to set the holder of thread-local.
     */
    public static void unlock() {
        if (_log.isInfoEnabled()) {
            _log.info("...Unlocking the static world of the callback context!");
        }
        _locked = false;
    }

    /**
     * Assert this is not locked.
     */
    protected static void assertNotLocked() {
        if (!isLocked()) {
            return;
        }
        String msg = "The callback context is locked! Don't access at this timing!";
        throw new IllegalStateException(msg);
    }

    // -----------------------------------------------------
    //                                   PreparedMessageHook
    //                                   -------------------
    public static void setPreparedMessageHookOnThread(SMailPreparedMessageHook preparedMessageHook) {
        final SMailCallbackContext context = getOrCreateContext();
        context.setPreparedMessageHook(preparedMessageHook);
    }

    public static boolean isExistPreparedMessageHookOnThread() {
        return isExistCallbackContextOnThread() && getCallbackContextOnThread().getPreparedMessageHook() != null;
    }

    public static void clearPreparedMessageHookOnThread() {
        if (isExistCallbackContextOnThread()) {
            final SMailCallbackContext context = getCallbackContextOnThread();
            context.setPreparedMessageHook(null);
            clearIfNoInterface(context);
        }
    }

    // -----------------------------------------------------
    //                                         Assist Helper
    //                                         -------------
    protected static SMailCallbackContext getOrCreateContext() {
        if (isExistCallbackContextOnThread()) {
            return getCallbackContextOnThread();
        } else {
            final SMailCallbackContext context = new SMailCallbackContext();
            setCallbackContextOnThread(context);
            return context;
        }
    }

    protected static void clearIfNoInterface(final SMailCallbackContext context) {
        if (!context.hasAnyInterface()) {
            clearCallbackContextOnThread();
        }
    }

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected SMailPreparedMessageHook _preparedMessageHook;

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    public boolean hasAnyInterface() {
        return _preparedMessageHook != null;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final String title = DfTypeUtil.toClassTitle(this);
        final StringBuilder sb = new StringBuilder();
        sb.append(title);
        sb.append(":{preparedMessageHook=").append(_preparedMessageHook);
        sb.append("}");
        return sb.toString();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    // -----------------------------------------------------
    //                                   PreparedMessageHook
    //                                   -------------------
    public SMailPreparedMessageHook getPreparedMessageHook() {
        return _preparedMessageHook;
    }

    public void setPreparedMessageHook(SMailPreparedMessageHook preparedMessageHook) {
        if (_preparedMessageHook != null && preparedMessageHook != null && preparedMessageHook.inheritsExistingHook()) {
            _preparedMessageHook = createInheritablePreparedMessageHook(preparedMessageHook);
        } else {
            _preparedMessageHook = preparedMessageHook;
        }
    }

    protected InheritablePreparedMessageHook createInheritablePreparedMessageHook(SMailPreparedMessageHook preparedMessageHook) {
        return new InheritablePreparedMessageHook(_preparedMessageHook, preparedMessageHook);
    }

    protected static class InheritablePreparedMessageHook implements SMailPreparedMessageHook {

        protected final SMailPreparedMessageHook _originally; // might be null e.g. when first one
        protected final SMailPreparedMessageHook _yourHook;

        public InheritablePreparedMessageHook(SMailPreparedMessageHook originally, SMailPreparedMessageHook yourHook) {
            _originally = originally;
            _yourHook = yourHook;
        }

        public void hookPreparedMessage(CardView cardView, SMailPostingDiscloser discloser) {
            if (_originally != null) {
                _originally.hookPreparedMessage(cardView, discloser);
            }
            _yourHook.hookPreparedMessage(cardView, discloser);
        }
    }
}
