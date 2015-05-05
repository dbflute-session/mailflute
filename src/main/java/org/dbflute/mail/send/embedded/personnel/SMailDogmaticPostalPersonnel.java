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
package org.dbflute.mail.send.embedded.personnel;

import org.dbflute.mail.Postcard;
import org.dbflute.mail.send.SMailPostalMotorbike;
import org.dbflute.mail.send.SMailPostalPersonnel;
import org.dbflute.mail.send.SMailPostie;
import org.dbflute.mail.send.SMailTextProofreader;
import org.dbflute.mail.send.embedded.postie.SMailSimpleGlobalPostie;
import org.dbflute.mail.send.embedded.proofreader.SMailParameterCommentTextProofreader;

/**
 * @author jflute
 */
public class SMailDogmaticPostalPersonnel implements SMailPostalPersonnel {

    protected static final SMailParameterCommentTextProofreader PROOFREADER = new SMailParameterCommentTextProofreader();

    protected boolean training;

    public SMailDogmaticPostalPersonnel asTraining() {
        training = true;
        return this;
    }

    @Override
    public SMailTextProofreader selectProofreader(Postcard postcard) {
        return PROOFREADER;
    }

    @Override
    public SMailPostie selectPostie(Postcard postcard, SMailPostalMotorbike motorbike) {
        final SMailSimpleGlobalPostie postie = new SMailSimpleGlobalPostie(motorbike);
        return training ? postie.asTraining() : postie;
    }

    public boolean isTraining() {
        return training;
    }
}
