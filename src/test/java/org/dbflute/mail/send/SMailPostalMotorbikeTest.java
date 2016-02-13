package org.dbflute.mail.send;

import java.util.Properties;

import javax.mail.PasswordAuthentication;

import org.dbflute.utflute.core.PlainTestCase;

/**
 * @author jflute
 */
public class SMailPostalMotorbikeTest extends PlainTestCase {

    public void test_register_basic() throws Exception {
        // ## Arrange ##
        SMailPostalMotorbike motorbike = new SMailPostalMotorbike();

        // ## Act ##
        motorbike.registerConnectionInfo("host", 1234);
        motorbike.registerReturnPath("returnPath");

        // ## Assert ##
        log(motorbike);
        assertNull(motorbike.getNativeSession().requestPasswordAuthentication(null, 1234, "smtp", null, null));
        Properties props = motorbike.getNativeSession().getProperties();
        assertEquals("host", props.get(SMailPostalMotorbike.MAIL_SMTP_HOST));
        assertEquals("1234", props.get(SMailPostalMotorbike.MAIL_SMTP_PORT));
        assertEquals("returnPath", props.get(SMailPostalMotorbike.MAIL_SMTP_FROM));
        assertNull(props.get(SMailPostalMotorbike.MAIL_TRANSPORT_PROTOCOL));
    }

    public void test_register_useSsl() throws Exception {
        // ## Arrange ##
        SMailPostalMotorbike motorbike = new SMailPostalMotorbike("sea", "land").useSsl();

        // ## Act ##
        motorbike.registerConnectionInfo("host", 1234);
        motorbike.registerReturnPath("returnPath");

        // ## Assert ##
        log(motorbike);
        Properties props = motorbike.getNativeSession().getProperties();
        assertNull(props.get("mail.smtp.auth"));
        assertEquals("true", props.get("mail.smtps.auth"));
        assertNull(props.get("mail.smtp.starttls.enable"));
        assertNull(props.get("mail.smtp.starttls.required"));
        assertNull(props.get("mail.smtps.starttls.enable"));
        assertNull(props.get("mail.smtps.starttls.required"));
        PasswordAuthentication pa = motorbike.getNativeSession().requestPasswordAuthentication(null, 1234, "smtp", null, null);
        assertEquals("sea", pa.getUserName());
        assertEquals("land", pa.getPassword());
        assertEquals("host", props.get("mail.smtps.host"));
        assertEquals("1234", props.get("mail.smtps.port"));
        assertEquals("returnPath", props.get("mail.smtps.from"));
        assertEquals("smtps", props.get(SMailPostalMotorbike.MAIL_TRANSPORT_PROTOCOL));
    }

    public void test_register_useStarttls() throws Exception {
        // ## Arrange ##
        SMailPostalMotorbike motorbike = new SMailPostalMotorbike("sea", "land").useStarttls();

        // ## Act ##
        motorbike.registerConnectionInfo("host", 1234);
        motorbike.registerReturnPath("returnPath");

        // ## Assert ##
        log(motorbike);
        Properties props = motorbike.getNativeSession().getProperties();
        assertNull(props.get(SMailPostalMotorbike.MAIL_TRANSPORT_PROTOCOL));
        assertEquals("true", props.get("mail.smtp.auth"));
        assertEquals("true", props.get("mail.smtp.starttls.enable"));
        assertEquals("true", props.get("mail.smtp.starttls.required"));
        PasswordAuthentication pa = motorbike.getNativeSession().requestPasswordAuthentication(null, 1234, "smtp", null, null);
        assertEquals("sea", pa.getUserName());
        assertEquals("land", pa.getPassword());
        assertEquals("host", props.get(SMailPostalMotorbike.MAIL_SMTP_HOST));
        assertEquals("1234", props.get(SMailPostalMotorbike.MAIL_SMTP_PORT));
        assertEquals("returnPath", props.get(SMailPostalMotorbike.MAIL_SMTP_FROM));
    }

    @SuppressWarnings("deprecation")
    public void test_register_deprecated() throws Exception {
        // ## Arrange ##
        SMailPostalMotorbike motorbike = new SMailPostalMotorbike("sea", "land");

        // ## Act ##
        motorbike.registerConnectionInfo("host", 1234);
        motorbike.registerReturnPath("returnPath");

        // ## Assert ##
        log(motorbike);
        Properties props = motorbike.getNativeSession().getProperties();
        assertNull(props.get("mail.smtp.auth"));
        assertNull(props.get("mail.smtp.starttls.enable"));
        assertNull(props.get("mail.smtp.starttls.required"));
        PasswordAuthentication pa = motorbike.getNativeSession().requestPasswordAuthentication(null, 1234, "smtp", null, null);
        assertEquals("sea", pa.getUserName());
        assertEquals("land", pa.getPassword());
        assertEquals("host", props.get(SMailPostalMotorbike.MAIL_SMTP_HOST));
        assertEquals("1234", props.get(SMailPostalMotorbike.MAIL_SMTP_PORT));
        assertEquals("returnPath", props.get(SMailPostalMotorbike.MAIL_SMTP_FROM));
        assertNull(props.get(SMailPostalMotorbike.MAIL_TRANSPORT_PROTOCOL));

        // ## Act ##
        motorbike.registerStarttls();

        // ## Assert ##
        log(motorbike);
        assertEquals("true", props.get("mail.smtp.auth"));
        assertEquals("true", props.get("mail.smtp.starttls.enable"));
        assertEquals("true", props.get("mail.smtp.starttls.required"));
    }
}
