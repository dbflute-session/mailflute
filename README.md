MailFlute
=======================
simple mail library with DBFlute and Java8

This library should be collaborated with FreeGen of DBFlute
like e.g. LastaFlute:

## Example Code
### prepare Mail Template (.dfmail)
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

# PMEditor
EMecha (Eclipse plugin) supports MailFlute mail template .dfmail:
- highlight
- complemention

https://dbflute-emecha.github.io

*It is very useful if you use MailFlute.

# Quick Trial
Can boot it by example of LastaFlute:

1. git clone https://github.com/lastaflute/lastaflute-example-harbor.git
2. prepare database by *ReplaceSchema at DBFlute client directory 'dbflute_maihamadb'  
3. compile it by Java8, on e.g. Eclipse or IntelliJ or ... as Maven project
4. execute the *main() method of (org.docksidestage.boot) HarborBoot
5. access to http://localhost:8090/harbor  
and login by user 'Pixy' and password 'sea', and can see debug log at console.

*ReplaceSchema
```java
// call manage.sh at lastaflute-example-harbor/dbflute_maihamadb
// and select replace-schema in displayed menu
...$ sh manage.sh
```

*main() method
```java
public class HarborBoot {

    public static void main(String[] args) {
        new JettyBoot(8090, "/harbor").asDevelopment().bootAwait();
    }
}
```

# Information
## Maven Dependency in pom.xml
```xml
<dependency>
    <groupId>org.dbflute.mail</groupId>
    <artifactId>mailflute</artifactId>
    <version>0.5.2</version>
</dependency>
```

## License
Apache License 2.0

## Official site
comming soon...

# Thanks, Friends
MailFlute is used by:  
comming soon...
