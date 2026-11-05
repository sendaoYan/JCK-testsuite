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
import javax.naming.NamingException;
import javax.naming.event.NamingExceptionEvent;
import javax.naming.event.NamingListener;
import javax.naming.ldap.Control;
import javax.naming.ldap.UnsolicitedNotification;
import javax.naming.ldap.UnsolicitedNotificationEvent;
import javax.naming.ldap.UnsolicitedNotificationListener;
import java.util.Arrays;

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
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dUneConstruction();
        dUneDispatch();
        dUneNullSource();
        System.out.println("deep: UnsolicitedNotificationEvent contracts OK");
    }

    static class DeepNotice implements UnsolicitedNotification {
        public String getID() { return "notice.id"; }
        public byte[] getEncodedValue() { return null; }
        public String[] getReferrals() { return new String[0]; }
        public NamingException getException() { return new javax.naming.ServiceUnavailableException(); }
        public Control[] getControls() { return null; }
    }

    static void dUneConstruction() {
        DeepNotice notice = new DeepNotice();
        UnsolicitedNotificationEvent e = new UnsolicitedNotificationEvent("source-obj", notice);
        check(e.getNotification() == notice, "notification identity");
        check(e.getSource().equals("source-obj"), "source is any Object");
        check(e.toString().contains("source-obj"), "toString mentions source");
    }

    static void dUneDispatch() {
        DeepNotice notice = new DeepNotice();
        UnsolicitedNotificationEvent e = new UnsolicitedNotificationEvent("source-obj", notice);
        final java.util.List<UnsolicitedNotificationEvent> seen = new java.util.ArrayList<>();
        UnsolicitedNotificationListener l = new UnsolicitedNotificationListener() {
            public void notificationReceived(UnsolicitedNotificationEvent ev) { seen.add(ev); }
            public void namingExceptionThrown(NamingExceptionEvent ev) { }
        };
        e.dispatch(l);
        check(seen.size() == 1 && seen.get(0) == e, "dispatch routes to unsolicitedNotification");
        check(l instanceof NamingListener, "listener is a NamingListener");
    }

    static void dUneNullSource() {
        boolean iae = false;
        try { new UnsolicitedNotificationEvent(null, new DeepNotice()); }
        catch (IllegalArgumentException e) { iae = true; }
        check(iae, "null source -> IllegalArgumentException");
    }

}
