
テンプレート部分まで含めて、
リーンスタートアップ＆インクリメンタル開発のサービス開発で
気軽に使えるメールライブラリを目指す。


[memo]

(2015/01/12)
(1)
MailBean mb = new MailBean();
mb.setFrom("foo@gmail.com");
mb.setTo("foo@gmail.com");
mailKicker.kick(mb);

(x)new MailBean(
    from = "foo@gmail.com"
    , to = "foo@gmail.com")

(2)
MailBean mb = new MailBean();
mb.setFrom("foo@gmail.com");
mb.setTo("foo@gmail.com");
mb.kick();

(2-1)
MailBean mb = mailFactory.create();
mb.setFrom("foo@gmail.com");
mb.setTo("foo@gmail.com");
mb.kick();

(3)
mailKicker.kick(mb -> {   
    mb.setFrom("foo@gmail.com");
    mb.setTo("foo@gmail.com");
});

(3-1)
mailKicker.kick(mb -> {
    mb.subject("");
    mb.plainText(MailPath.MEMBER_REGISTER);
    mb.htmlText(MailPath.MEMBER_REGISTER_HTML);
    mb.from("foo@gmail.com");
    mb.to("foo@gmail.com");
});

(3-2)
mailKicker.kick(mb -> {   
    mb.from("foo@gmail.com"); //
      .to("foo@gmail.com"); //
      .subject("aaaa"); //
});

(4)
mailKicker.get();
memberRegisteredKicker.send(mb -> {
    mb.
});

MemberRegisteredMB mb = new MemberRegisteredMB();
mb.setFrom();
mb.addTo("aaa");
mb.addCc("aaa");
mb.addBcc("aaa");
mb.setMemberAccount("aaa");
mailKicker.kick(mb);

mail.set

mailKicker.send((MemberRegisteredMail)mail -> );

mail.send("foo@gmail.com", map);

