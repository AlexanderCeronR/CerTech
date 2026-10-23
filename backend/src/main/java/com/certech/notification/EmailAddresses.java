package com.certech.notification;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

final class EmailAddresses {
    private EmailAddresses() {}
    static boolean validSingle(String value) {
        if (value == null || value.isBlank() || value.length() > 180 || value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0) return false;
        try {
            InternetAddress[] parsed = InternetAddress.parse(value, true);
            if (parsed.length != 1 || parsed[0].isGroup() || parsed[0].getPersonal() != null) return false;
            parsed[0].validate();
            return parsed[0].getAddress().equals(value) && value.indexOf('@') > 0;
        } catch (AddressException ex) { return false; }
    }
    static void requireSingle(String value) {
        if (!validSingle(value)) throw new IllegalArgumentException("A single valid notification mailbox is required");
    }
}
