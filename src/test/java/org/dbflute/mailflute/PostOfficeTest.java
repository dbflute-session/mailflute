package org.dbflute.mailflute;

import jdk.nashorn.internal.ir.annotations.Ignore;

import org.dbflute.utflute.core.PlainTestCase;

/**
 * @author CreativeGear
 */
@Ignore
public class PostOfficeTest extends PlainTestCase {

    // TODO miya なんとかして by jflute 
    //	public void test_kick() throws AddressException {
    //	    SMailSessionHolder holder = new SMailSessionHolder();
    //        SMailSession session = new SMailSession();
    //        session.registerConnectionInfo("localhost", 25);
    //        // session.registerUserInfo(user, password);
    //        // session.registerProxy(proxyHost, proxyPort);
    //        holder.registerSession("main", session);
    //
    //		MailClerk mailKicker = new MailClerk();
    //		TestMailBean mailBean = new TestMailBean();
    //		mailKicker.kick(mailBean);
    //	}
    //
    //	/**
    //	 * Dummy Mail Bean for running test.
    //	 */
    //	private class TestMailBean implements MailFlutist {
    //
    //		@Override
    //		public Address getFromAddress() {
    //			try {
    //				return new InternetAddress("write email address for 'From header'.");
    //			} catch (AddressException e) {
    //				throw new RuntimeException(e);
    //			}
    //		}
    //
    //		@Override
    //		public List<Address> getToAddressList() {
    //			List<Address> toAddrList = new ArrayList<Address>();
    //			try {
    //				toAddrList.add(new InternetAddress("write email address for 'To header'."));
    //				return toAddrList;
    //			} catch (AddressException e) {
    //				throw new RuntimeException(e);
    //			}
    //		}
    //
    //		@Override
    //		public List<Address> getCcAddressList() {
    //			List<Address> ccAddrList = new ArrayList<Address>();
    //			try {
    //				ccAddrList.add(new InternetAddress("write email address for 'Cc header'."));
    //				return ccAddrList;
    //			} catch (AddressException e) {
    //				throw new RuntimeException(e);
    //			}
    //		}
    //
    //		@Override
    //		public List<Address> getBccAddressList() {
    //			List<Address> bccAddrList = new ArrayList<Address>();
    //			try {
    //				bccAddrList.add(new InternetAddress("write email address for 'Bcc header'."));
    //				return bccAddrList;
    //			} catch (AddressException e) {
    //				throw new RuntimeException(e);
    //			}
    //		}
    //
    //		@Override
    //		public String getSubject() {
    //			return "Subject";
    //		}
    //
    //		@Override
    //		public String getPlainBody() {
    //			return "Hello Mail Flute! Plane text.";
    //		}
    //
    //		@Override
    //		public String getHtmlBody() {
    //			return "<html><head></head><body>Hello Mail Flute! Html.</body></html>";
    //		}
    //	}
}
