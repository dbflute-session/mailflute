package org.dbflute.mailflute.send;

import org.dbflute.mailflute.Postcard;

/**
 * 
 * @author Takeshi Kato
 *
 */
public interface SMailDeliverer {

    void send(Postcard post);

}
