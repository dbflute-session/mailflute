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
package org.dbflute.mail.send.embedded.receptionist;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.dbflute.Entity;
import org.dbflute.helper.filesystem.FileTextIO;
import org.dbflute.mail.PostOffice;
import org.dbflute.mail.Postcard;
import org.dbflute.mail.Postcard.DirectBodyOption;
import org.dbflute.mail.send.SMailReceptionist;
import org.dbflute.mail.send.exception.SMailIllegalStateException;
import org.dbflute.mail.send.exception.SMailUserLocaleNotFoundException;
import org.dbflute.optional.OptionalThing;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfResourceUtil;
import org.dbflute.util.DfTypeUtil;
import org.dbflute.util.Srl;
import org.dbflute.util.Srl.ScopeInfo;

/**
 * @author jflute
 * @since 0.4.0 (2015/05/09 Saturday at nakameguro)
 */
public class SMailConventionReceptionist implements SMailReceptionist {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String META_DELIMITER = ">>>";
    public static final String COMMENT_BEGIN = "/*";
    public static final String COMMENT_END = "*/";
    public static final String TITLE_BEGIN = "[";
    public static final String TITLE_END = "]";
    public static final String SUBJECT_LABEL = "subject:";
    public static final String OPTION_LABEL = "option:";
    public static final String PLUS_HTML_OPTION = "+html";
    public static final String PROPDEF_PREFIX = "-- !!";
    public static final Set<String> optionSet;

    static {
        optionSet = Collections.unmodifiableSet(DfCollectionUtil.newLinkedHashSet(PLUS_HTML_OPTION));
    }

    public static final List<String> allowedPrefixList; // except first line (comment)

    static {
        allowedPrefixList = Arrays.asList(OPTION_LABEL, PROPDEF_PREFIX);
    }

    protected static final String LF = "\n";
    protected static final String CR = "\r";
    protected static final String CRLF = "\r\n";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String classpathBasePath; // used when from classpath, e.g. mail
    protected SMailDynamicTextAssist dynamicTextAssist; // e.g. from database, without text cache if specified
    protected SMailReceiverLocaleAssist receiverLocaleAssist; // e.g. null means no locale switch
    protected final Map<String, String> textCacheMap = new ConcurrentHashMap<String, String>();
    protected final FileTextIO textIO = createFileTextIO();
    protected final SMailConventionSecurity security = createConventionSecurity();

    protected FileTextIO createFileTextIO() {
        return new FileTextIO().encodeAsUTF8().removeUTF8Bom().replaceCrLfToLf();
    }

    protected SMailConventionSecurity createConventionSecurity() {
        return new SMailConventionSecurity();
    }

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SMailConventionReceptionist asClasspathBase(String classpathBasePath) {
        this.classpathBasePath = classpathBasePath;
        return this;
    }

    public SMailConventionReceptionist asDynamicText(SMailDynamicTextAssist dynamicTextAssist) {
        this.dynamicTextAssist = dynamicTextAssist;
        return this;
    }

    public SMailConventionReceptionist asReceiverLocale(SMailReceiverLocaleAssist receiverLocaleAssist) {
        this.receiverLocaleAssist = receiverLocaleAssist;
        return this;
    }

    // ===================================================================================
    //                                                                       Read BodyFile
    //                                                                       =============
    @Override
    public void accept(Postcard postcard) {
        readyPostcardFirst(postcard);
        checkPostcardFirst(postcard);
        if (postcard.isForcedlyDirect()) { // should ignore body file
            assertPlainBodyExistsForDirectBody(postcard);
            postcard.getBodyFile().ifPresent(bodyFile -> { // but wants logging
                officeManagedLogging(postcard, bodyFile, prepareReceiverLocale(postcard));
            });
            return;
        }
        postcard.getBodyFile().ifPresent(bodyFile -> {
            if (postcard.getHtmlBody().isPresent()) {
                String msg = "Cannot use direct HTML body when body file is specified: " + postcard;
                throw new SMailIllegalStateException(msg);
            }
            final boolean filesystem = postcard.isFromFilesystem();
            final OptionalThing<Locale> receiverLocale = prepareReceiverLocale(postcard);
            officeManagedLogging(postcard, bodyFile, receiverLocale);
            final String plainText = readText(postcard, bodyFile, filesystem, receiverLocale);
            analyzeBodyMeta(postcard, bodyFile, plainText);
            final DirectBodyOption option = postcard.useDirectBody(plainText);
            if (postcard.isAlsoHtmlFile()) {
                final String htmlFilePath = deriveHtmlFilePath(bodyFile);
                final String readHtml = readText(postcard, htmlFilePath, filesystem, receiverLocale);
                verifyMailHtmlTemplateTextFormat(htmlFilePath, readHtml);
                option.alsoDirectHtml(readHtml);
            }
            // no check about unneeded HTML template file because of runtime performance
            // DBFlute generator checks it instead
        }).orElse(() -> { /* direct body, check only here */
            assertPlainBodyExistsForDirectBody(postcard);
        });
    }

    protected OptionalThing<Locale> prepareReceiverLocale(Postcard postcard) {
        final OptionalThing<Locale> receiverLocale = postcard.getReceiverLocale();
        if (receiverLocale.isPresent()) {
            return receiverLocale;
        } else {
            if (receiverLocaleAssist != null) {
                final OptionalThing<Locale> assistedLocale = receiverLocaleAssist.assist(postcard);
                if (assistedLocale == null) {
                    String msg = "Not found the user locale from the assist: " + receiverLocaleAssist + ", " + postcard;
                    throw new SMailUserLocaleNotFoundException(msg);
                }
                assistedLocale.ifPresent(locale -> {
                    postcard.asReceiverLocale(locale); /* save for next steps */
                });
                return assistedLocale;
            }
            return OptionalThing.ofNullable(null, () -> {
                throw new SMailIllegalStateException("Not found the locale: " + postcard);
            });
        }
    }

    protected void officeManagedLogging(Postcard postcard, String bodyFile, OptionalThing<Locale> receiverLocale) {
        final String systemTitle = PostOffice.LOGGING_TITLE_SYSINFO;
        postcard.officeManagedLogging(systemTitle, "dfmail", bodyFile);
        postcard.officeManagedLogging(systemTitle, "locale", receiverLocale.map(lo -> lo.toString()).orElse("none"));
    }

    protected void assertPlainBodyExistsForDirectBody(Postcard postcard) {
        if (!postcard.getPlainBody().isPresent()) {
            String msg = "Not found both the body file path and the direct body: " + postcard;
            throw new SMailIllegalStateException(msg);
        }
    }

    // ===================================================================================
    //                                                                         Ready First
    //                                                                         ===========
    protected void readyPostcardFirst(Postcard postcard) { // may be overridden
    }

    // ===================================================================================
    //                                                                         Check First
    //                                                                         ===========
    protected void checkPostcardFirst(Postcard postcard) { // similar to LastaFlute's check for display data
        final Map<String, Object> variableMap = postcard.getTemplaetVariableMap();
        variableMap.forEach((key, value) -> {
            stopDirectlyEntityVariable(postcard, key, value);
        });
    }

    protected void stopDirectlyEntityVariable(Postcard postcard, String key, Object value) {
        // though rare case about mail, because of hard to generate, but check just in case
        if (value instanceof Entity) {
            throwDirectlyEntityVariableNotAllowedException(postcard, key, value);
        } else if (value instanceof Collection<?>) {
            final Collection<?> coll = ((Collection<?>) value);
            if (!coll.isEmpty()) {
                // care performance for List that the most frequent pattern
                final Object first = coll instanceof List<?> ? ((List<?>) coll).get(0) : coll.iterator().next();
                if (first instanceof Entity) {
                    throwDirectlyEntityVariableNotAllowedException(postcard, key, value);
                }
            }
        }
        // cannot check perfectly e.g. map's value, but only primary patterns are enough
    }

    protected void throwDirectlyEntityVariableNotAllowedException(Postcard postcard, String key, Object value) {
        security.throwDirectlyEntityVariableNotAllowedException(postcard, key, value);
    }

    // ===================================================================================
    //                                                                       Actually Read
    //                                                                       =============
    protected String readText(Postcard postcard, String path, boolean filesystem, OptionalThing<Locale> receiverLocale) {
        final String assisted = assistDynamicText(postcard, path, filesystem, receiverLocale); // e.g. from database
        if (assisted != null) {
            return assisted;
        }
        final String cacheKey = generateCacheKey(path, filesystem, receiverLocale);
        final String cached = textCacheMap.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        synchronized (this) {
            final String retried = textCacheMap.get(cacheKey);
            if (retried != null) {
                return retried;
            }
            final String read = doReadText(postcard, path, filesystem, receiverLocale);
            if (read == null) { // just in case
                String msg = "Not found the text from the path: " + path + ", filesystem=" + filesystem;
                throw new SMailIllegalStateException(msg);
            }
            textCacheMap.put(cacheKey, read);
            return textCacheMap.get(cacheKey);
        }
    }

    protected String assistDynamicText(Postcard postcard, String path, boolean filesystem, OptionalThing<Locale> receiverLocale) {
        return dynamicTextAssist != null ? dynamicTextAssist.assist(postcard, path, filesystem, receiverLocale) : null;
    }

    protected String generateCacheKey(String path, boolean filesystem, OptionalThing<Locale> receiverLocale) {
        return path + ":" + filesystem + ":" + receiverLocale;
    }

    protected String doReadText(Postcard postcard, String path, boolean filesystem, OptionalThing<Locale> receiverLocale) {
        final String read;
        if (filesystem) {
            final String realPath = receiverLocale.map(locale -> {
                return deriveLocaleFilePath(path, locale).filter(localeFilePath -> {
                    return new File(localeFilePath).exists();
                }).orElse(path);
            }).orElse(path);
            read = textIO.read(realPath);
        } else { // from class-path as default, mainly here
            final InputStream ins = receiverLocale.map(locale -> {
                return findLocaleFileResourceStream(path, locale).orElseGet(() -> {
                    return findMainFileResourceStream(postcard, path);
                });
            }).orElseGet(() -> {
                return findMainFileResourceStream(postcard, path);
            });
            read = textIO.read(ins);
        }
        return read;
    }

    protected OptionalThing<InputStream> findLocaleFileResourceStream(String path, Locale locale) {
        return deriveLocaleFilePath(path, locale).map(localeFilePath -> {
            final String localeRealPath = adjustClasspathBasePath(localeFilePath);
            return OptionalThing.ofNullable(DfResourceUtil.getResourceStream(localeRealPath), () -> {
                throw new SMailIllegalStateException("Not found the resource stream for the locale file: " + path + ", " + locale);
            });
        }).orElseGet(() -> {
            return OptionalThing.ofNullable(null, () -> {
                throw new SMailIllegalStateException("Not found the language from the locale: " + locale);
            });
        });
    }

    protected InputStream findMainFileResourceStream(Postcard postcard, String path) {
        final String realPath = adjustClasspathBasePath(path);
        final InputStream ins = DfResourceUtil.getResourceStream(realPath);
        if (ins == null) {
            throwMailTemplateFromClasspathNotFoundException(postcard, path, realPath);
        }
        return ins;
    }

    protected String adjustClasspathBasePath(String path) {
        return (classpathBasePath != null ? classpathBasePath + "/" : "") + path;
    }

    protected OptionalThing<String> deriveLocaleFilePath(String path, Locale locale) {
        final String front = Srl.substringLastFront(path, ".");
        final String rear = Srl.substringLastRear(path, ".");
        final String lang = locale.getLanguage();
        if (lang != null && !lang.isEmpty()) {
            return OptionalThing.of(front + "." + lang.toLowerCase() + "." + rear);
        } else {
            return OptionalThing.ofNullable(null, () -> {
                throw new SMailIllegalStateException("Not found the language from the locale: " + locale);
            });
        }
    }

    protected void throwMailTemplateFromClasspathNotFoundException(Postcard postcard, String path, String realPath) {
        security.throwMailTemplateFromClasspathNotFoundException(postcard, path, realPath);
    }

    // ===================================================================================
    //                                                                     Analyzer Header
    //                                                                     ===============
    protected void analyzeBodyMeta(Postcard postcard, String bodyFile, String plainText) {
        // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
        // /*
        //  [New Member's Registration]
        //  The member will be formalized after click.
        // */
        // subject: Welcome to your sign up, /*pmb.memberName*/
        // >>>
        // Hello, /*pmb.memberName*/
        // ...
        // _/_/_/_/_/_/_/_/_/_/
        final String delimiter = META_DELIMITER;
        if (plainText.contains(delimiter)) {
            verifyFormat(bodyFile, plainText, delimiter);
        }
        final String meta = Srl.replace(Srl.substringFirstFront(plainText, delimiter), CRLF, LF);
        final ScopeInfo optionScope = Srl.extractScopeFirst(meta, OPTION_LABEL, LF);
        if (optionScope != null && optionScope.getContent().contains(PLUS_HTML_OPTION)) {
            postcard.officePlusHtml();
        }
    }

    // ===================================================================================
    //                                                                             Dispose
    //                                                                             =======
    @Override
    public synchronized void workingDispose() { // for hot deploy
        textCacheMap.clear();
    }

    // ===================================================================================
    //                                                                       Verify Format
    //                                                                       =============
    protected void verifyFormat(String bodyFile, String plainText, String delimiter) {
        final String meta = Srl.substringFirstFront(plainText, delimiter);
        if (!meta.endsWith(LF)) { // also CRLF checked
            throwBodyMetaNoIndependentDelimiterException(bodyFile, plainText);
        }
        final int rearIndex = plainText.indexOf(delimiter) + delimiter.length();
        if (plainText.length() > rearIndex) { // just in case (empty mail possible?)
            final String rearFirstStr = plainText.substring(rearIndex, rearIndex + 1);
            if (!Srl.equalsPlain(rearFirstStr, LF, CR)) { // e.g. >>> Hello, ...
                throwBodyMetaNoIndependentDelimiterException(bodyFile, plainText);
            }
        }
        if (!meta.startsWith(COMMENT_BEGIN)) { // also leading spaces not allowed
            throwBodyMetaNotStartWithHeaderCommentException(bodyFile, plainText, meta);
        }
        if (!meta.contains(COMMENT_END)) {
            throwBodyMetaHeaderCommentEndMarkNotFoundException(bodyFile, plainText, meta);
        }
        final String headerComment = Srl.extractScopeFirst(plainText, COMMENT_BEGIN, COMMENT_END).getContent();
        final ScopeInfo titleScope = Srl.extractScopeFirst(headerComment, TITLE_BEGIN, TITLE_END);
        if (titleScope == null) {
            throwBodyMetaTitleCommentNotFoundException(bodyFile, plainText);
        }
        final String desc = Srl.substringFirstRear(headerComment, TITLE_END);
        if (desc.isEmpty()) {
            throwBodyMetaDescriptionCommentNotFoundException(bodyFile, plainText);
        }
        final String rearMeta = Srl.substringFirstRear(meta, COMMENT_END);
        // no way because of already checked
        //if (!rearMeta.contains(LF)) {
        //}
        final List<String> splitList = Srl.splitList(rearMeta, LF);
        if (!splitList.get(0).trim().isEmpty()) { // after '*/'
            throwBodyMetaHeaderCommentEndMarkNoIndependentException(bodyFile, plainText);
        }
        if (!splitList.get(1).startsWith(SUBJECT_LABEL)) { // also leading spaces not allowed
            throwBodyMetaSubjectNotFoundException(bodyFile, plainText);
        }
        final int nextIndex = 2;
        if (splitList.size() > nextIndex) { // after subject
            final List<String> nextList = splitList.subList(nextIndex, splitList.size());
            final int nextSize = nextList.size();
            int index = 0;
            for (String line : nextList) {
                if (index == nextSize - 1) { // last loop
                    if (line.isEmpty()) { // empty line only allowed in last loop
                        break;
                    }
                }
                if (!allowedPrefixList.stream().anyMatch(prefix -> line.startsWith(prefix))) {
                    throwBodyMetaUnknownLineException(bodyFile, plainText, line);
                }
                if (line.startsWith(OPTION_LABEL)) {
                    final String options = Srl.substringFirstRear(line, OPTION_LABEL);
                    final List<String> optionList = Srl.splitListTrimmed(options, ".");
                    for (String option : optionList) {
                        if (!optionSet.contains(option)) {
                            throwBodyMetaUnknownOptionException(bodyFile, plainText, option);
                        }
                    }
                }
                ++index;
            }
        }
    }

    protected void throwBodyMetaNoIndependentDelimiterException(String bodyFile, String plainText) {
        security.throwBodyMetaNoIndependentDelimiterException(bodyFile, plainText);
    }

    protected void throwBodyMetaNotStartWithHeaderCommentException(String bodyFile, String plainText, String meta) {
        security.throwBodyMetaNotStartWithHeaderCommentException(bodyFile, plainText, meta);
    }

    protected void throwBodyMetaHeaderCommentEndMarkNotFoundException(String bodyFile, String plainText, String meta) {
        security.throwBodyMetaHeaderCommentEndMarkNotFoundException(bodyFile, plainText, meta);
    }

    protected void throwBodyMetaTitleCommentNotFoundException(String bodyFile, String plainText) {
        security.throwBodyMetaTitleCommentNotFoundException(bodyFile, plainText);
    }

    protected void throwBodyMetaDescriptionCommentNotFoundException(String bodyFile, String plainText) {
        security.throwBodyMetaDescriptionCommentNotFoundException(bodyFile, plainText);
    }

    protected void throwBodyMetaHeaderCommentEndMarkNoIndependentException(String bodyFile, String plainText) {
        security.throwBodyMetaHeaderCommentEndMarkNoIndependentException(bodyFile, plainText);
    }

    protected void throwBodyMetaSubjectNotFoundException(String bodyFile, String plainText) {
        security.throwBodyMetaSubjectNotFoundException(bodyFile, plainText);
    }

    protected void throwBodyMetaUnknownLineException(String bodyFile, String plainText, String line) {
        security.throwBodyMetaUnknownLineException(bodyFile, plainText, line);
    }

    protected void throwBodyMetaUnknownOptionException(String bodyFile, String fileText, String option) {
        security.throwBodyMetaUnknownOptionException(bodyFile, fileText, option, optionSet);
    }

    // ===================================================================================
    //                                                                       HTML Template
    //                                                                       =============
    protected String deriveHtmlFilePath(String bodyFile) {
        final String dirBase = bodyFile.contains("/") ? Srl.substringLastFront(bodyFile, "/") + "/" : "";
        final String pureFileName = Srl.substringLastRear(bodyFile, "/"); // same if no delimiter
        final String front = Srl.substringFirstFront(pureFileName, "."); // e.g. member_registration
        final String rear = Srl.substringFirstRear(pureFileName, "."); // e.g. dfmail or ja.dfmail
        return dirBase + front + "_html." + rear; // e.g. member_registration_html.dfmail
    }

    protected void verifyMailHtmlTemplateTextFormat(String htmlFilePath, String readHtml) {
        if (readHtml.contains(META_DELIMITER)) {
            throwMailHtmlTemplateTextCannotContainHeaderDelimiterException(htmlFilePath, readHtml);
        }
    }

    protected void throwMailHtmlTemplateTextCannotContainHeaderDelimiterException(String htmlFilePath, String readHtml) {
        security.throwMailHtmlTemplateTextCannotContainHeaderDelimiterException(htmlFilePath, readHtml);
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final String title = DfTypeUtil.toClassTitle(this);
        return title + ":{" + textCacheMap.keySet() + "}@" + Integer.toHexString(hashCode());
    }
}
