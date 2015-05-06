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

import java.util.Map;
import java.util.Map.Entry;

import org.dbflute.mail.send.SMailTextProofreader;
import org.dbflute.twowaysql.SqlAnalyzer;
import org.dbflute.twowaysql.context.CommandContext;
import org.dbflute.twowaysql.context.CommandContextCreator;
import org.dbflute.twowaysql.node.Node;
import org.dbflute.twowaysql.pmbean.SimpleMapPmb;

/**
 * @author jflute
 * @since 0.4.0 (2015/05/05 Tuesday at nakameguro)
 */
public class SMailParameterCommentTextProofreader implements SMailTextProofreader {

    protected static final CommandContextCreator contextCreator;
    static {
        final String[] argNames = new String[] { "pmb" };
        final Class<?>[] argTypes = new Class<?>[] { SimpleMapPmb.class };
        contextCreator = new CommandContextCreator(argNames, argTypes);
    }

    @Override
    public String proofreader(String templateText, Map<String, Object> variableMap) {
        // TODO jflute mailflute: [A] option of line separator
        @SuppressWarnings("deprecation")
        final SqlAnalyzer analyzer = new SqlAnalyzer(templateText, true) {
            protected String filterAtFirst(String sql) {
                return sql; // keep body
            }
        }.overlookNativeBinding().switchBindingToReplaceOnlyEmbedded();
        final Node node = analyzer.analyze();
        final SimpleMapPmb<Object> pmb = new SimpleMapPmb<Object>();
        for (Entry<String, Object> entry : variableMap.entrySet()) {
            pmb.addParameter(entry.getKey(), entry.getValue());
        }
        final CommandContext ctx = prepareContextCreator().createCommandContext(new Object[] { pmb });
        node.accept(ctx);
        final String filteredText = ctx.getSql();
        return filteredText;
    }

    protected CommandContextCreator prepareContextCreator() {
        return contextCreator;
    }

    @Override
    public String toString() {
        return "proofreader:{pmcomment}";
    }
}
