/*
 * @test
 * @summary javax.naming.ldap.UnsolicitedNotificationEvent conformance: API type verification.
 * @run main UnsolicitedNotificationEventTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class UnsolicitedNotificationEventTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.ldap.UnsolicitedNotificationEvent e = new javax.naming.ldap.UnsolicitedNotificationEvent("src", null);
check("src".equals(e.getSource()), "source");
check(e.getNotification() == null, "notification null");
check(e instanceof java.util.EventObject, "EventObject");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
