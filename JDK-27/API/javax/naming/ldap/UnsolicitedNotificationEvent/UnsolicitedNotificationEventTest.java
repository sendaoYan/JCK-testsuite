/*
 * @test
 * @summary javax.naming.ldap.UnsolicitedNotificationEvent conformance: white-box structure + black-box behavior.
 * @run main UnsolicitedNotificationEventTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.*;
import javax.naming.event.*;
import javax.naming.ldap.*;

public class UnsolicitedNotificationEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.UnsolicitedNotificationEvent");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(java.util.EventObject.class == c.getSuperclass(), "extends EventObject");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 2 && x.getParameterTypes()[0] == Object.class && x.getParameterTypes()[1] == UnsolicitedNotification.class), "ctor(Object,UnsolicitedNotification)");
        for (String m : new String[]{"getNotification", "dispatch"}) {
            boolean found = false;
            for (Method mm : c.getMethods()) if (mm.getName().equals(m)) found = true;
            check(found, m + " present");
        }
    }

    static void testBlackBox() throws Exception {
        UnsolicitedNotification n = new UnsolicitedNotification() {
            public String[] getReferrals() { return new String[]{"ldap://a"}; }
            public NamingException getException() { return null; }
            public String getID() { return "1.2.3"; }
            public byte[] getEncodedValue() { return new byte[]{1}; }
            public Control[] getControls() { return new Control[0]; }
        };
        UnsolicitedNotificationEvent evt = new UnsolicitedNotificationEvent("src", n);
        check("src".equals(evt.getSource()), "source");
        check(evt.getNotification() == n, "notification identity");

        UnsolicitedNotificationEvent evtNull = new UnsolicitedNotificationEvent("src2", null);
        check(evtNull.getNotification() == null, "null notification");

        final boolean[] dispatched = new boolean[1];
        UnsolicitedNotificationListener listener = new UnsolicitedNotificationListener() {
            public void notificationReceived(UnsolicitedNotificationEvent e) { dispatched[0] = true; }
            public void namingExceptionThrown(NamingExceptionEvent e) { }
        };
        evt.dispatch(listener);
        check(dispatched[0], "dispatch invokes listener");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
