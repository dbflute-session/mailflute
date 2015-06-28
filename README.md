MailFlute
======================
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

1. git clone https://github.com/dbflute-session/lastaflute-example-harbor.git
2. prepare MySQL on 3306 port as empty root password, execute *ReplaceSchema  
3. compile it as Java8, e.g. maven (mvn compile) or Eclipse or IntelliJ or ...
4. execute the *main method of (org.docksidestage.boot) HarborBoot
5. access to http://localhost:8090/harbor  
and login by user 'Pixy' and password 'sea', and can see debug log at console.

*ReplaceSchema
```java
// in lastaflute-example-harbor/dbflute_maihamadb
...:dbflute_maihamadb ...$ sh manage.sh
```

*Main method
```java
public class HarborBoot {

    public static void main(String[] args) {
        new JettyBoot(8090, "/harbor").asDevelopment().bootAwait();
    }
}
```

# Information
## Maven
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
