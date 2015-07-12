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
package org.dbflute.mail.send.embedded.receptionist;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.dbflute.helper.filesystem.FileTextIO;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.mail.PostOffice;
import org.dbflute.mail.Postcard;
import org.dbflute.mail.Postcard.DirectBodyOption;
import org.dbflute.mail.send.SMailReceptionist;
import org.dbflute.mail.send.exception.SMailBodyMetaParseFailureException;
import org.dbflute.mail.send.exception.SMailIllegalStateException;
import org.dbflute.mail.send.exception.SMailTemplateNotFoundException;
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

    protected FileTextIO createFileTextIO() {
        return new FileTextIO().encodeAsUTF8().removeUTF8Bom().replaceCrLfToLf();
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
            if (!postcard.getPlainBody().isPresent()) {
                String msg = "Not found both the body file path and the direct body: " + postcard;
                throw new SMailIllegalStateException(msg);
            }
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
        final String read = doReadText(postcard, path, filesystem, receiverLocale);
        if (read == null) { // just in case
            String msg = "Not found the text from the path: " + path + ", filesystem=" + filesystem;
            throw new SMailIllegalStateException(msg);
        }
        textCacheMap.put(cacheKey, read);
        return textCacheMap.get(cacheKey);
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
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the mail template file from classpath");
        br.addItem("Postcard");
        br.addElement(postcard);
        br.addItem("Mail Template");
        br.addElement("plain path : " + path);
        br.addElement("real path  : " + realPath);
        final String msg = br.buildExceptionMessage();
        throw new SMailTemplateNotFoundException(msg);
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
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("No independent delimter of mail body meta.");
        br.addItem("Advice");
        br.addElement("The delimter of mail body meta should be independent in line.");
        br.addElement("For example:");
        br.addElement("  (x)");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    */ subject: ... >>>   // *NG");
        br.addElement("    ...your mail body");
        br.addElement("  (x)");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    */");
        br.addElement("    subject: ...");
        br.addElement("    >>> ...your mail body // *NG");
        br.addElement("  (o)");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    */");
        br.addElement("    subject: ...");
        br.addElement("    >>>                   // OK");
        br.addElement("    ...your mail body");
        setupBodyFileInfo(br, bodyFile, plainText);
        final String msg = br.buildExceptionMessage();
        throw new SMailBodyMetaParseFailureException(msg);
    }

    protected void throwBodyMetaNotStartWithHeaderCommentException(String bodyFile, String plainText, String meta) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not start with the header comment in the mail body meta.");
        br.addItem("Advice");
        br.addElement("The mail body meta should start with '/*' and should contain '*/'.");
        br.addElement("It means header comment of template file is required.");
        br.addElement("For example:");
        br.addElement("  (x)");
        br.addElement("    subject: ...              // *NG");
        br.addElement("    >>>");
        br.addElement("    ...your mail body");
        br.addElement("");
        br.addElement("  (o)");
        br.addElement("    /*                        // OK");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    */");
        br.addElement("    subject: ...");
        br.addElement("    >>>");
        br.addElement("    ...your mail body");
        br.addElement("");
        br.addElement("And example:");
        br.addElement("  /*");
        br.addElement("   [New Member's Registration]");
        br.addElement("   The memebr will be formalized after click.");
        br.addElement("   And the ...");
        br.addElement("  */");
        br.addElement("  subject: Welcome to your sign up, /*pmb.memberName*/");
        br.addElement("  >>>");
        br.addElement("  Hello, sea");
        br.addElement("  ...");
        setupBodyFileInfo(br, bodyFile, plainText);
        br.addItem("Body Meta");
        br.addElement(meta);
        final String msg = br.buildExceptionMessage();
        throw new SMailBodyMetaParseFailureException(msg);
    }

    protected void throwBodyMetaHeaderCommentEndMarkNotFoundException(String bodyFile, String plainText, String meta) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the header comment end mark in the mail body meta.");
        br.addItem("Advice");
        br.addElement("The mail body meta should start with '/*' and should contain '*/'.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    subject: ...");
        br.addElement("    >>>");
        br.addElement("    ...             // *NG: not found");
        br.addElement("  (x):");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    >>>");
        br.addElement("    */              // *NG: after delimiter");
        br.addElement("    subject: ...");
        br.addElement("    ...");
        br.addElement("  (o):");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    */              // OK");
        br.addElement("    subject: ...");
        br.addElement("    >>>");
        br.addElement("    ...");
        setupBodyFileInfo(br, bodyFile, plainText);
        br.addItem("Body Meta");
        br.addElement(meta);
        final String msg = br.buildExceptionMessage();
        throw new SMailBodyMetaParseFailureException(msg);
    }

    protected void throwBodyMetaTitleCommentNotFoundException(String bodyFile, String plainText) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the title in the header comment of mail body meta.");
        br.addItem("Advice");
        br.addElement("The mail body meta should contain TITLE in the header comment.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    /*");
        br.addElement("     ...your mail's description     // *NG");
        br.addElement("    */");
        br.addElement("    subject: ...");
        br.addElement("    >>>");
        br.addElement("    ...");
        br.addElement("  (o):");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]         // OK");
        br.addElement("     ...your mail's description");
        br.addElement("    */");
        br.addElement("    subject: ...");
        br.addElement("    >>>");
        br.addElement("    ...");
        setupBodyFileInfo(br, bodyFile, plainText);
        final String msg = br.buildExceptionMessage();
        throw new SMailBodyMetaParseFailureException(msg);
    }

    protected void throwBodyMetaDescriptionCommentNotFoundException(String bodyFile, String plainText) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the description in the header comment of mail body meta.");
        br.addItem("Advice");
        br.addElement("The mail body meta should contain DESCRIPTION");
        br.addElement("in the header comment like this:");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("    */                              // *NG");
        br.addElement("    subject: ...");
        br.addElement("    >>>");
        br.addElement("    ...");
        br.addElement("  (o):");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description     // OK");
        br.addElement("    */");
        br.addElement("    subject: ...");
        br.addElement("    >>>");
        br.addElement("    ...");
        setupBodyFileInfo(br, bodyFile, plainText);
        final String msg = br.buildExceptionMessage();
        throw new SMailBodyMetaParseFailureException(msg);
    }

    protected void throwBodyMetaHeaderCommentEndMarkNoIndependentException(String bodyFile, String plainText) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("No independent the header comment end mark in the mail body meta.");
        br.addItem("Advice");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    */ subject: ...        // *NG");
        br.addElement("    >>>");
        br.addElement("    ...your mail body");
        br.addElement("  (o):");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    */");
        br.addElement("    subject: ...           // OK");
        br.addElement("    >>>");
        br.addElement("    ...your mail body");
        setupBodyFileInfo(br, bodyFile, plainText);
        final String msg = br.buildExceptionMessage();
        throw new SMailBodyMetaParseFailureException(msg);
    }

    protected void throwBodyMetaSubjectNotFoundException(String bodyFile, String plainText) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the subject in the mail body meta.");
        br.addItem("Advice");
        br.addElement("The mail body meta should have subject.");
        br.addElement("And should be defined immediately after header comment.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    */");
        br.addElement("    >>>                    // *NG");
        br.addElement("    ...your mail body");
        br.addElement("  (x):");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    */");
        br.addElement("    option: ...");
        br.addElement("    subject: ...           // *NG");
        br.addElement("    >>>");
        br.addElement("    ...your mail body");
        br.addElement("  (o):");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    */");
        br.addElement("    subject: ...           // OK");
        br.addElement("    >>>");
        br.addElement("    ...your mail body");
        setupBodyFileInfo(br, bodyFile, plainText);
        final String msg = br.buildExceptionMessage();
        throw new SMailBodyMetaParseFailureException(msg);
    }

    protected void throwBodyMetaUnknownLineException(String bodyFile, String plainText, String line) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Unknown line in the template meta.");
        br.addItem("Advice");
        br.addElement("The mail body meta should start with option:");
        br.addElement("or fixed style, e.g. '-- !!...!!'");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    */");
        br.addElement("    subject: ...");
        br.addElement("    maihama     // *NG: unknown meta definition");
        br.addElement("    >>>");
        br.addElement("    ...");
        br.addElement("  (x):");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    */");
        br.addElement("    subject: ...");
        br.addElement("                // *NG: empty line not allowed");
        br.addElement("    >>>");
        br.addElement("    ...");
        br.addElement("  (o):");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    */");
        br.addElement("    subject: ...");
        br.addElement("    >>>");
        br.addElement("    ...");
        br.addElement("  (o):");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    */");
        br.addElement("    subject: ...");
        br.addElement("    -- !!String memberName!!");
        br.addElement("    >>>");
        br.addElement("    ...");
        setupBodyFileInfo(br, bodyFile, plainText);
        br.addItem("Unknown Line");
        br.addElement(line);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }

    protected void throwBodyMetaUnknownOptionException(String bodyFile, String fileText, String option) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Unknown option for MailFlute body meta.");
        br.addItem("Advice");
        br.addElement("You can specify the following option:");
        br.addElement(optionSet);
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    */");
        br.addElement("    subject: ...");
        br.addElement("    option: maihama      // *NG: unknown option");
        br.addElement("    >>>");
        br.addElement("    ...");
        br.addElement("  (o):");
        br.addElement("    /*");
        br.addElement("     [...your mail's title]");
        br.addElement("     ...your mail's description");
        br.addElement("    */");
        br.addElement("    subject: ...");
        br.addElement("    option: genAsIs      // OK");
        br.addElement("    >>>");
        br.addElement("    ...");
        br.addItem("Body File");
        br.addElement(bodyFile);
        br.addItem("File Text");
        br.addElement(fileText);
        br.addItem("Unknown Option");
        br.addElement(option);
        final String msg = br.buildExceptionMessage();
        throw new SMailBodyMetaParseFailureException(msg);
    }

    // cannot verify because of not required in runtime process, freegen verify instead
    //protected void throwBodyMetaNotFoundException(String bodyFile, String plainText) {
    //    final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
    //    br.addNotice("Not found the delimiter for mail body meta.");
    //    br.addItem("Advice");
    //    br.addElement("The delimiter of mail body meta is '>>>'.");
    //    br.addElement("It should be defined.");
    //    br.addElement("For example:");
    //    br.addElement("  (x):");
    //    br.addElement("    /*");
    //    br.addElement("     [...your mail's title]");
    //    br.addElement("     ...your mail's description");
    //    br.addElement("    */");
    //    br.addElement("    subject: ...");
    //    br.addElement("    ...your mail body        // *NG");
    //    br.addElement("  (o):");
    //    br.addElement("    /*");
    //    br.addElement("     [...your mail's title]");
    //    br.addElement("     ...your mail's description");
    //    br.addElement("    */");
    //    br.addElement("    subject: ...");
    //    br.addElement("    >>>                      // OK");
    //    br.addElement("    ...your mail body");
    //    br.addElement("  (o):");
    //    br.addElement("    /*");
    //    br.addElement("     [...your mail's title]");
    //    br.addElement("     ...your mail's description");
    //    br.addElement("    */");
    //    br.addElement("    subject: ...");
    //    br.addElement("    option: ...options");
    //    br.addElement("    -- !!String memberName!!");
    //    br.addElement("    >>>                      // OK");
    //    br.addElement("    ...your mail body");
    //    setupBodyFileInfo(br, bodyFile, plainText);
    //    final String msg = br.buildExceptionMessage();
    //    throw new SMailBodyMetaParseFailureException(msg);
    //}

    protected void setupBodyFileInfo(ExceptionMessageBuilder br, String bodyFile, String plainText) {
        br.addItem("Body File");
        br.addElement(bodyFile);
        br.addItem("Plain Text");
        br.addElement(plainText);
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
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("HTML template cannot contain meta delimiter '>>>'.");
        br.addItem("Advice");
        br.addElement("Body meta delimiter '>>>' can be used by plain text template.");
        br.addElement("HTML template has only its body.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    /*");
        br.addElement("     ...");
        br.addElement("    */");
        br.addElement("    >>>        // *NG");
        br.addElement("    <html>");
        br.addElement("    ...");
        br.addElement("  (o):");
        br.addElement("    <html>     // OK");
        br.addElement("    ...");
        br.addItem("HTML Template");
        br.addElement(htmlFilePath);
        br.addItem("Read HTML");
        br.addElement(readHtml);
        final String msg = br.buildExceptionMessage();
        throw new SMailBodyMetaParseFailureException(msg);
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
