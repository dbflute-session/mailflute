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

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.dbflute.helper.filesystem.FileTextIO;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.mail.Postcard;
import org.dbflute.mail.Postcard.DirectBodyOption;
import org.dbflute.mail.send.SMailReceptionist;
import org.dbflute.mail.send.embedded.proofreader.SMailBodyMetaProofreader;
import org.dbflute.mail.send.exception.SMailBodyMetaParseFailureException;
import org.dbflute.mail.send.exception.SMailIllegalStateException;
import org.dbflute.mail.send.exception.SMailTemplateNotFoundException;
import org.dbflute.util.DfResourceUtil;
import org.dbflute.util.DfTypeUtil;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.4.0 (2015/05/09 Saturday at nakameguro)
 */
public class SMailConventionReceptionist implements SMailReceptionist {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String META_DELIMITER = SMailBodyMetaProofreader.META_DELIMITER;
    protected static final String SUBJECT_LABEL = SMailBodyMetaProofreader.SUBJECT_LABEL;
    protected static final String OPTION_LABEL = SMailBodyMetaProofreader.OPTION_LABEL;
    protected static final String PLUS_HTML_OPTION = SMailBodyMetaProofreader.PLUS_HTML_OPTION;
    protected static final String PROPDEF_PREFIX = SMailBodyMetaProofreader.PROPDEF_PREFIX;
    protected static final Set<String> optionSet = SMailBodyMetaProofreader.optionSet;
    protected static final String LF = "\n";
    protected static final String CR = "\r";
    protected static final String CRLF = "\r\n";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String classpathBasePath; // used when from classpath, e.g. mail
    protected SMailDynamicTextAssist dynamicTextAssist; // e.g. from database, without text cache if specified
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

    // ===================================================================================
    //                                                                       Read BodyFile
    //                                                                       =============
    @Override
    public void accept(Postcard postcard) {
        final String plainText;
        final String bodyFile = postcard.getBodyFile();
        if (bodyFile != null) {
            if (postcard.getHtmlBody() != null) {
                String msg = "Cannot use direct HTML body when body file is specified: " + postcard;
                throw new IllegalStateException(msg);
            }
            final boolean filesystem = postcard.isFromFilesystem();
            plainText = readText(postcard, bodyFile, filesystem);
            analyzeBodyMeta(postcard, bodyFile, plainText);
            final DirectBodyOption option = postcard.useDirectBody(plainText);
            if (postcard.isAlsoHtmlFile()) {
                option.alsoDirectHtml(readText(postcard, deriveHtmlFilePath(bodyFile), filesystem));
            }
        } else { // direct body
            plainText = postcard.getPlainBody();
            if (plainText == null) {
                String msg = "Not found both the body file path and the direct body: " + postcard;
                throw new IllegalStateException(msg);
            }
            analyzeBodyMeta(postcard, bodyFile, plainText);
        }
    }

    // ===================================================================================
    //                                                                       Actually Read
    //                                                                       =============
    protected String readText(Postcard postcard, String path, boolean filesystem) {
        final String assisted = assistDynamicText(postcard, path, filesystem); // e.g. from database
        if (assisted != null) {
            return assisted;
        }
        final String cacheKey = generateCacheKey(path, filesystem);
        final String cached = textCacheMap.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        final String read = doReadText(postcard, path, filesystem);
        if (read == null) { // just in case
            String msg = "Not found the text from the path: " + path + ", filesystem=" + filesystem;
            throw new SMailIllegalStateException(msg);
        }
        textCacheMap.put(cacheKey, read);
        return textCacheMap.get(cacheKey);
    }

    protected String assistDynamicText(Postcard postcard, String path, boolean filesystem) {
        return dynamicTextAssist != null ? dynamicTextAssist.assist(postcard, path, filesystem) : null;
    }

    protected String doReadText(Postcard postcard, String path, boolean filesystem) {
        final String read;
        if (filesystem) {
            read = textIO.read(path);
        } else { // from class-path as default, mainly here
            final String realPath = adjustBasePath(path);
            final InputStream ins = DfResourceUtil.getResourceStream(realPath);
            if (ins == null) {
                throwMailTemplateFromClasspathNotFoundException(postcard, path, realPath);
            }
            read = textIO.read(ins);
        }
        return read;
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

    protected String generateCacheKey(String path, boolean filesystem) {
        return path + ":" + filesystem;
    }

    protected String adjustBasePath(String path) {
        return (classpathBasePath != null ? classpathBasePath + "/" : "") + path;
    }

    // ===================================================================================
    //                                                                     Analyzer Header
    //                                                                     ===============
    protected void analyzeBodyMeta(Postcard postcard, String bodyFile, String plainText) {
        // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
        // subject: Welcome to your sign up, /*pmb.memberName*/
        // >>>
        // Hello, /*pmb.memberName*/
        // ...
        // _/_/_/_/_/_/_/_/_/_/
        checkBodyMetaFormat(bodyFile, plainText);
        final String meta = Srl.replace(Srl.substringFirstFront(plainText, META_DELIMITER), CRLF, LF);
        final String secondOrMoreText = Srl.substringFirstRear(meta, LF);
        if (secondOrMoreText.startsWith(OPTION_LABEL)) {
            final String option = Srl.substringFirstFront(Srl.substringFirstRear(secondOrMoreText, OPTION_LABEL), LF);
            if (option.contains(PLUS_HTML_OPTION)) {
                postcard.officePlusHtml();
            }
        }
    }

    protected String deriveHtmlFilePath(String bodyFile) {
        final String front = Srl.substringLastFront(bodyFile, ".");
        final String rear = Srl.substringLastRear(bodyFile, ".");
        return front + "_html." + rear; // e.g. member_registration_html.dfmail
    }

    // ===================================================================================
    //                                                                        Check Format
    //                                                                        ============
    protected void checkBodyMetaFormat(String bodyFile, String fileText) {
        final String delimiter = SMailBodyMetaProofreader.META_DELIMITER;
        if (fileText.contains(delimiter)) {
            final String meta = Srl.substringFirstFront(fileText, delimiter);
            if (!meta.endsWith(LF)) { // also CRLF checked
                throwMailBodyMetaNoIndependentDelimiterException(bodyFile, fileText);
            }
            final int rearIndex = fileText.indexOf(delimiter) + delimiter.length();
            if (fileText.length() > rearIndex) { // just in case (empty mail possible?)
                final String rearFirstStr = fileText.substring(rearIndex, rearIndex + 1);
                if (!Srl.equalsPlain(rearFirstStr, LF, CR)) { // e.g. >>> Hello, ...
                    throwMailBodyMetaNoIndependentDelimiterException(bodyFile, fileText);
                }
            }
            final List<String> splitList = Srl.splitList(meta, LF);
            if (!splitList.get(0).startsWith(SUBJECT_LABEL)) {
                throwMailBodyMetaSubjectNotFoundException(bodyFile, fileText);
            }
            if (splitList.size() > 1) {
                final List<String> nextList = splitList.subList(1, splitList.size());
                final int nextSize = nextList.size();
                int index = 0;
                int lineNumber = 2;
                for (String line : nextList) {
                    if (index == nextSize - 1) { // last loop
                        if (line.isEmpty()) { // empty line only allowed in last loop
                            break;
                        }
                    }
                    if (!line.startsWith(OPTION_LABEL) && !line.startsWith(PROPDEF_PREFIX)) {
                        throwMailBodyMetaUnknownLineException(bodyFile, fileText, line, lineNumber);
                    }
                    if (line.startsWith(OPTION_LABEL)) {
                        final String options = Srl.substringFirstRear(line, OPTION_LABEL);
                        final List<String> optionList = Srl.splitListTrimmed(options, ".");
                        for (String option : optionList) {
                            if (!optionSet.contains(option)) {
                                throwMailBodyMetaUnknownOptionException(bodyFile, fileText, option);
                            }
                        }
                    }
                    ++lineNumber;
                    ++index;
                }
            } else { // already checked so basically no way but just in case
                throwMailBodyMetaNoIndependentDelimiterException(bodyFile, fileText);
            }
        } else { // no delimiter
            // basically already checked when you generate postcards by DBFlute
            final List<String> splitList = Srl.splitList(fileText, LF);
            final String firstLine = splitList.get(0);
            if (Srl.containsIgnoreCase(firstLine, SUBJECT_LABEL)) { // may be mistake?
                throwMailBodyMetaNotFoundException(bodyFile, fileText);
            }
        }
    }

    protected void throwMailBodyMetaNoIndependentDelimiterException(String bodyFile, String fileText) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("No independent delimter of body meta.");
        br.addItem("Advice");
        br.addElement("The delimter of body meta should be independent in line.");
        br.addElement("For example:");
        br.addElement("  (x)");
        br.addElement("    subject: ...(mail subject)>>>");
        br.addElement("    ...(mail body)");
        br.addElement("  (x)");
        br.addElement("    subject: ...(mail subject)");
        br.addElement("    >>>...(mail body)");
        br.addElement("  (o)");
        br.addElement("    subject: ...(mail subject)");
        br.addElement("    >>>");
        br.addElement("    ...(mail body)");
        br.addItem("Body File");
        br.addElement(bodyFile);
        br.addItem("File Text");
        br.addElement(fileText);
        final String msg = br.buildExceptionMessage();
        throw new SMailBodyMetaParseFailureException(msg);
    }

    protected void throwMailBodyMetaSubjectNotFoundException(String bodyFile, String fileText) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the subject in the MailFlute body meta.");
        br.addItem("Advice");
        br.addElement("The MailFlute body meta should start with subject.");
        br.addElement("For example:");
        br.addElement("  subject: ...(mail subject)");
        br.addElement("  >>>");
        br.addElement("  ...(mail body)");
        br.addItem("Body File");
        br.addElement(bodyFile);
        br.addItem("File Text");
        br.addElement(fileText);
        final String msg = br.buildExceptionMessage();
        throw new SMailBodyMetaParseFailureException(msg);
    }

    protected void throwMailBodyMetaUnknownLineException(String bodyFile, String fileText, String line, int lineNumber) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Unknown line in the MailFlute body meta.");
        br.addItem("Advice");
        br.addElement("The MailFlute body meta should start with subject:");
        br.addElement("For example:");
        br.addElement("  (o):");
        br.addElement("    subject: ...(mail subject)");
        br.addElement("    >>>");
        br.addElement("    ...(mail body)");
        br.addElement("  (o):");
        br.addElement("    subject: ...(mail subject)");
        br.addElement("    option: ...(options)");
        br.addElement("    -- !!String memberName!!");
        br.addElement("    >>>");
        br.addElement("    ...(mail body)");
        br.addElement("  (x):");
        br.addElement("    subject: ...(mail subject)");
        br.addElement("    maihama // *NG: unknown meta definition");
        br.addElement("    >>>");
        br.addElement("    ...(mail body)");
        br.addElement("  (x):");
        br.addElement("    subject: ...(mail subject)");
        br.addElement("        // *NG: empty line not allowed");
        br.addElement("    >>>");
        br.addElement("    ...(mail body)");
        br.addItem("Body File");
        br.addElement(bodyFile);
        br.addItem("File Text");
        br.addElement(fileText);
        br.addItem("Unknown Line");
        br.addElement("Line Number: " + lineNumber);
        br.addElement(line);
        final String msg = br.buildExceptionMessage();
        throw new SMailBodyMetaParseFailureException(msg);
    }

    protected void throwMailBodyMetaNotFoundException(String bodyFile, String fileText) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the delimiter for MailFlute body meta.");
        br.addItem("Advice");
        br.addElement("The delimiter of MailFlute body meta is '>>>'.");
        br.addElement("It should be defined like this:");
        br.addElement("For example:");
        br.addElement("  (o):");
        br.addElement("    subject: ...(mail subject)");
        br.addElement("    >>>");
        br.addElement("    ...(mail body)");
        br.addElement("  (o):");
        br.addElement("    subject: ...(mail subject)");
        br.addElement("    option: ...(options)");
        br.addElement("    -- !!String memberName!!");
        br.addElement("    >>>");
        br.addElement("    ...(mail body)");
        br.addElement("  (x):");
        br.addElement("    subject: ...(mail subject)");
        br.addElement("    ...(mail body)");
        br.addElement("  (x):");
        br.addElement("    Hello, sea...");
        br.addItem("Body File");
        br.addElement(bodyFile);
        br.addItem("File Text");
        br.addElement(fileText);
        final String msg = br.buildExceptionMessage();
        throw new SMailBodyMetaParseFailureException(msg);
    }

    protected void throwMailBodyMetaUnknownOptionException(String bodyFile, String fileText, String option) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Unknown option for MailFlute body meta.");
        br.addItem("Advice");
        br.addElement("You can specify the following option:");
        br.addElement(PLUS_HTML_OPTION);
        br.addElement("For example:");
        br.addElement("  (o):");
        br.addElement("    subject: ...(mail subject)");
        br.addElement("    option: +html");
        br.addElement("    >>>");
        br.addElement("    ...(mail body)");
        br.addElement("  (x):");
        br.addElement("    subject: ...(mail subject)");
        br.addElement("    option: maihama // *NG: unknown option");
        br.addElement("    >>>");
        br.addElement("    ...(mail body)");
        br.addItem("Body File");
        br.addElement(bodyFile);
        br.addItem("File Text");
        br.addElement(fileText);
        br.addItem("Unknown Option");
        br.addElement(option);
        final String msg = br.buildExceptionMessage();
        throw new SMailBodyMetaParseFailureException(msg);
    }

    // ===================================================================================
    //                                                                             Dispose
    //                                                                             =======
    @Override
    public synchronized void workingDispose() { // for hot deploy
        textCacheMap.clear();
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
