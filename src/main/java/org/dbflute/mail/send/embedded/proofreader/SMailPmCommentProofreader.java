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
package org.dbflute.mail.send.embedded.proofreader;

import java.util.List;
import java.util.Map;

import org.dbflute.mail.send.SMailTextProofreader;
import org.dbflute.twowaysql.SqlAnalyzer;
import org.dbflute.twowaysql.context.CommandContext;
import org.dbflute.twowaysql.context.CommandContextCreator;
import org.dbflute.twowaysql.node.Node;
import org.dbflute.twowaysql.pmbean.SimpleMapPmb;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.4.0 (2015/05/05 Tuesday at nakameguro)
 */
public class SMailPmCommentProofreader implements SMailTextProofreader {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String IF_PREFIX = "/*IF ";
    protected static final String FOR_PREFIX = "/*FOR ";
    protected static final String END_COMMENT = "/*END*/";
    protected static final String CLOSE_MARK = "*/";
    protected static final String LF = "\n";
    protected static final String CRLF = "\r\n";

    // ===================================================================================
    //                                                                           Proofread
    //                                                                           =========
    @Override
    public String proofread(String templateText, Map<String, Object> variableMap) {
        final SimpleMapPmb<Object> pmb = new SimpleMapPmb<Object>();
        variableMap.forEach((key, value) -> pmb.addParameter(key, value));
        return evaluate(templateText, pmb);
    }

    // ===================================================================================
    //                                                                            Evaluate
    //                                                                            ========
    // very similar to simple template manager of LastaFlute but no recycle to be independent
    // -----------------------------------------------------
    //                                              Evaluate
    //                                              --------
    protected String evaluate(String templateText, Object pmb) {
        final Node node = analyze(filterTemplateText(templateText, pmb));
        final CommandContext ctx = prepareContext(pmb);
        node.accept(ctx);
        return ctx.getSql();
    }

    // -----------------------------------------------------
    //                                       Line Adjustment
    //                                       ---------------
    protected String filterTemplateText(String templateText, Object pmb) {
        final String replaced = Srl.replace(templateText, CRLF, LF);
        final List<String> lineList = Srl.splitList(replaced, LF);
        final StringBuilder sb = new StringBuilder(templateText.length());
        boolean nextNoLine = false;
        int lineNumber = 0;
        for (String line : lineList) {
            ++lineNumber;
            if (nextNoLine) {
                sb.append(line);
                nextNoLine = false;
                continue;
            }
            if (isIfEndCommentLine(line) || isForEndCommentLine(line)) {
                appendLfLine(sb, lineNumber, Srl.substringLastFront(line, END_COMMENT));
                sb.append(LF).append(END_COMMENT);
                nextNoLine = true;
                continue;
            }
            final String realLine;
            if (isOnlyIfCommentLine(line) || isOnlyForCommentLine(line) || isOnlyEndCommentLine(line)) {
                nextNoLine = true;
                realLine = Srl.ltrim(line);
            } else {
                realLine = line;
            }
            appendLfLine(sb, lineNumber, realLine);
        }
        return sb.toString();
    }

    protected boolean isOnlyIfCommentLine(String line) {
        final String trimmed = line.trim();
        return trimmed.startsWith(IF_PREFIX) && trimmed.endsWith(CLOSE_MARK) && Srl.count(line, CLOSE_MARK) == 1;
    }

    protected boolean isOnlyForCommentLine(String line) {
        final String trimmed = line.trim();
        return trimmed.startsWith(FOR_PREFIX) && trimmed.endsWith(CLOSE_MARK) && Srl.count(line, CLOSE_MARK) == 1;
    }

    protected boolean isOnlyEndCommentLine(String line) {
        return line.trim().equals(END_COMMENT);
    }

    protected boolean isIfEndCommentLine(String line) {
        return line.startsWith(IF_PREFIX) && line.endsWith(END_COMMENT) && Srl.count(line, CLOSE_MARK) > 1;
    }

    protected boolean isForEndCommentLine(String line) {
        return line.startsWith(FOR_PREFIX) && line.endsWith(END_COMMENT) && Srl.count(line, CLOSE_MARK) > 1;
    }

    protected void appendLfLine(final StringBuilder sb, int lineNumber, String line) {
        sb.append(lineNumber > 1 ? LF : "").append(line);
    }

    // -----------------------------------------------------
    //                                      Analyze Template
    //                                      ----------------
    protected Node analyze(String templateText) {
        return createSqlAnalyzer(templateText, true).analyze();
    }

    protected SqlAnalyzer createSqlAnalyzer(String templateText, boolean blockNullParameter) {
        final SqlAnalyzer analyzer = new SqlAnalyzer(templateText, blockNullParameter) {
            protected String filterAtFirst(String sql) {
                return sql; // keep body
            }
        }.overlookNativeBinding().switchBindingToReplaceOnlyEmbedded(); // adjust for plain template
        return analyzer;
    }

    protected CommandContext prepareContext(Object pmb) {
        final Object filteredPmb = filterPmb(pmb);
        final String[] argNames = new String[] { "pmb" };
        final Class<?>[] argTypes = new Class<?>[] { filteredPmb.getClass() };
        final CommandContextCreator creator = newCommandContextCreator(argNames, argTypes);
        return creator.createCommandContext(new Object[] { filteredPmb });
    }

    protected static CommandContextCreator newCommandContextCreator(String[] argNames, Class<?>[] argTypes) {
        return new CommandContextCreator(argNames, argTypes);
    }

    protected Object filterPmb(Object pmb) {
        if (pmb instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            final Map<String, Object> variableMap = ((Map<String, Object>) pmb);
            final SimpleMapPmb<Object> mapPmb = new SimpleMapPmb<Object>();
            variableMap.forEach((key, value) -> mapPmb.addParameter(key, value));
            return mapPmb;
        } else {
            return pmb;
        }
    }

    // ===================================================================================
    //                                                                             Dispose
    //                                                                             =======
    @Override
    public void workingDispose() {
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "proofreader:{pmcomment}";
    }
}
