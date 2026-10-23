package com.certech.notification;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.mail.MailProperties;

class SmtpTransportGuardTest {
    MailProperties mail() {
        var p=new MailProperties();p.setHost("smtp.example.invalid");p.setPort(587);p.setUsername("synthetic");p.setPassword("test-only");
        for(String k:new String[]{"auth","starttls.enable","starttls.required","ssl.checkserveridentity"})p.getProperties().put("mail.smtp."+k,"true");
        for(String k:new String[]{"connectiontimeout","timeout","writetimeout"})p.getProperties().put("mail.smtp."+k,"5000");
        return p;
    }
    @Test void acceptsBoundedVerifiedStarttls() { assertDoesNotThrow(()->new SmtpTransportGuard(mail(),ContactEmailTest.settings(true),60000)); }
    @Test void rejectsMissingCredentialsPlaintextAndInfiniteTimeouts() {
        var p=mail();p.setPassword("");assertThrows(IllegalArgumentException.class,()->new SmtpTransportGuard(p,ContactEmailTest.settings(true),60000));
        p.setPassword("test-only");p.getProperties().put("mail.smtp.starttls.required","false");assertThrows(IllegalArgumentException.class,()->new SmtpTransportGuard(p,ContactEmailTest.settings(true),60000));
        p.getProperties().put("mail.smtp.starttls.required","true");p.getProperties().put("mail.smtp.timeout","0");assertThrows(IllegalArgumentException.class,()->new SmtpTransportGuard(p,ContactEmailTest.settings(true),60000));
    }
    @Test void rejectsUnsafeLeaseAndExcessivePolling() {
        var s=new NotificationSettings(true,"owner@example.invalid","sender@example.invalid","America/Bogota",3,1,60,60,1);
        assertThrows(IllegalArgumentException.class,()->new SmtpTransportGuard(mail(),s,60000));
        assertThrows(IllegalArgumentException.class,()->new SmtpTransportGuard(mail(),ContactEmailTest.settings(true),1));
    }
}
