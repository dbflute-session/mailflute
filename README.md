# MailFlute
simple mail library with DBFlute and Java8

This library should be collaborated with FreeGen of DBFlute
like e.g. LastaFlute:

## Example Code
### prepare Mail Template
```sql
/*
 [New Member's Registration]
 The member will be formalized after click.
*/
subject: Welcome to your sign up, /*pmb.memberName*/
>>>
Hello, /*pmb.memberName*/

How are you?
/*IF pmb.birthdate != null*/
Happy birthdate! Today is /*pmb.birthdate*/.
/*END*/

Thanks
```

after that, execute DBFlute FreeGen task, and the class will be generated...

### use Generated Class
```java
// e.g. WelcomeMemberPostcard, generated from 'welcome_member.dfmail'

WelcomeMemberPostcard.droppedInto(postbox, postcard -> {
    postcard.setFrom("from@example.com", LABELS_OFFICE_MAIL);
    postcard.addTo("to@example.com");
    postcard.setMemberName("sea");
    postcard.setBirthdate(birthdate);
    postcard.addReplyTo("replyto@example.com");
});
```

# Quick Trial
Can boot it by example of LastaFlute:

1. prepare Java8 compile environment
2. clone https://github.com/dbflute-session/lastaflute-example-harbor
3. execute the main method of (org.docksidestage.boot) HarborBoot
4. access to http://localhost:8090/harbor

```java
public class HarborBoot {

    public static void main(String[] args) {
        new JettyBoot(8090, "/harbor").asDevelopment().bootAwait();
    }
}
```

Can login by user 'Pixy' and password 'sea', and can see debug log at console.

# Information
## Maven Dependency
```xml
<dependency>
    <groupId>org.dbflute.mail</groupId>
    <artifactId>mailflute</artifactId>
    <version>0.4.0-RCD</version>
</dependency>
```

## License
Apache License 2.0

## Official site
comming soon...

# Thanks, Friends
MailFlute is used by:  
comming soon...
