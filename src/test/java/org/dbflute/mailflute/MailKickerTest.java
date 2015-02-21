package org.dbflute.mailflute;

import java.util.ArrayList;
import java.util.List;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import jdk.nashorn.internal.ir.annotations.Ignore;

import org.junit.Test;

/**
 * 
 * @author CreativeGear
 *
 */
@Ignore
public class MailKickerTest {

	@Test
	public void test_kick() throws AddressException {
		MailKicker mailKicker = new MailKicker();
		TestMailBean mailBean = new TestMailBean();
		mailKicker.kick(mailBean);
	}

	/**
	 * Dummy Mail Bean for running test.
	 */
	private class TestMailBean implements MailFlutist {

		@Override
		public Address getFromAddress() {
			try {
				return new InternetAddress("write email address for 'From header'.");
			} catch (AddressException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public List<Address> getToAddressList() {
			List<Address> toAddrList = new ArrayList<Address>();
			try {
				toAddrList.add(new InternetAddress("write email address for 'To header'."));
				return toAddrList;
			} catch (AddressException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public List<Address> getCcAddressList() {
			List<Address> ccAddrList = new ArrayList<Address>();
			try {
				ccAddrList.add(new InternetAddress("write email address for 'Cc header'."));
				return ccAddrList;
			} catch (AddressException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public List<Address> getBccAddressList() {
			List<Address> bccAddrList = new ArrayList<Address>();
			try {
				bccAddrList.add(new InternetAddress("write email address for 'Bcc header'."));
				return bccAddrList;
			} catch (AddressException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public String getSubject() {
			return "Subject";
		}

		@Override
		public String getPlainBody() {
			return "Hello Mail Flute! Plane text.";
		}

		@Override
		public String getHtmlBody() {
			return "<html><head></head><body>Hello Mail Flute! Html.</body></html>";
		}
	}
}
