/*
 * @test
 * @summary javax.naming.ldap.UnsolicitedNotificationListener conformance: white-box structure + black-box behavior.
 * @run main UnsolicitedNotificationListenerTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.*;
import javax.naming.event.*;
import javax.naming.ldap.*;

public class UnsolicitedNotificationListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.UnsolicitedNotificationListener");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(NamingListener.class.isAssignableFrom(c), "extends NamingListener");
        check(java.util.EventListener.class.isAssignableFrom(c), "extends EventListener");
        check(hasMethod(c, "notificationReceived"), "notificationReceived");
        check(hasMethod(c, "namingExceptionThrown"), "namingExceptionThrown");
    }

    static void testBlackBox() throws Exception {
        final UnsolicitedNotificationEvent[] received = new UnsolicitedNotificationEvent[1];
        UnsolicitedNotificationListener listener = new UnsolicitedNotificationListener() {
            public void notificationReceived(UnsolicitedNotificationEvent evt) { received[0] = evt; }
            public void namingExceptionThrown(NamingExceptionEvent evt) { }
        };
        UnsolicitedNotification n = new UnsolicitedNotification() {
            public String[] getReferrals() { return null; }
            public NamingException getException() { return null; }
            public String getID() { return "1.2.3"; }
            public byte[] getEncodedValue() { return null; }
            public Control[] getControls() { return null; }
        };
        UnsolicitedNotificationEvent evt = new UnsolicitedNotificationEvent("src", n);
        evt.dispatch(listener);
        check(received[0] == evt, "dispatch delivered to listener");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
