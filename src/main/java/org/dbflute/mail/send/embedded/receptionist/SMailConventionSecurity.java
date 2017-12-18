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

import java.util.Collection;
import java.util.Set;

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.mail.Postcard;
import org.dbflute.mail.send.exception.SMailBodyMetaParseFailureException;
import org.dbflute.mail.send.exception.SMailDirectlyEntityVariableNotAllowedException;
import org.dbflute.mail.send.exception.SMailTemplateNotFoundException;

/**
 * @author jflute
 */
public class SMailConventionSecurity {

    // ===================================================================================
    //                                                                         Check First
    //                                                                         ===========
    public void throwDirectlyEntityVariableNotAllowedException(Postcard postcard, String key, Object value) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Registered the entity directly as mail variable.");
        br.addItem("Advice");
        br.addElement("Not allowed to register register entity directly as mail variable.");
        br.addElement("Convert your entity data to mail bean for mail variable.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    Member member = ...");
        br.addElement("    postcard.setMember(member); // *Bad");
        br.addElement("  (o):");
        br.addElement("    Member member = ...");
        br.addElement("    MemberBean bean = mappingToBean(member);");
        br.addElement("    postcard.setMember(bean); // Good");
        br.addItem("Postcard");
        br.addElement(postcard);
        br.addItem("Registered");
        br.addElement("key: " + key);
        if (value instanceof Collection<?>) {
            ((Collection<?>) value).forEach(element -> br.addElement(element));
        } else {
            br.addElement(value);
        }
        final String msg = br.buildExceptionMessage();
        throw new SMailDirectlyEntityVariableNotAllowedException(msg);
    }

    // ===================================================================================
    //                                                                       Actually Read
    //                                                                       =============
    public void throwMailTemplateFromClasspathNotFoundException(Postcard postcard, String path, String realPath) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the mail template file from classpath");
        br.addItem("Advice");
        br.addElement("Confirm the location and file path of mail template.");
        br.addElement("And the file should be in valid classpath.");
        br.addElement("");
        br.addElement("While, The searching mail template process needs");
        br.addElement("context class-loader of current thread.");
        br.addElement("If the class-loader is null, MailFlute cannot search it.");
        br.addItem("Postcard");
        br.addElement(postcard);
        br.addItem("Mail Template");
        br.addElement("plain path : " + path);
        br.addElement("real path  : " + realPath);
        br.addItem("Context ClassLoader");
        br.addElement(Thread.currentThread().getContextClassLoader());
        final String msg = br.buildExceptionMessage();
        throw new SMailTemplateNotFoundException(msg);
    }

    // ===================================================================================
    //                                                                       Verify Format
    //                                                                       =============
    public void throwBodyMetaNoIndependentDelimiterException(String bodyFile, String plainText) {
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

    public void throwBodyMetaNotStartWithHeaderCommentException(String bodyFile, String plainText, String meta) {
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

    public void throwBodyMetaHeaderCommentEndMarkNotFoundException(String bodyFile, String plainText, String meta) {
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

    public void throwBodyMetaTitleCommentNotFoundException(String bodyFile, String plainText) {
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

    public void throwBodyMetaDescriptionCommentNotFoundException(String bodyFile, String plainText) {
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

    public void throwBodyMetaHeaderCommentEndMarkNoIndependentException(String bodyFile, String plainText) {
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

    public void throwBodyMetaSubjectNotFoundException(String bodyFile, String plainText) {
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

    public void throwBodyMetaUnknownLineException(String bodyFile, String plainText, String line) {
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

    public void throwBodyMetaUnknownOptionException(String bodyFile, String fileText, String option, Set<String> optionSet) {
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
    public void throwMailHtmlTemplateTextCannotContainHeaderDelimiterException(String htmlFilePath, String readHtml) {
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
}
