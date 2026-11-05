/*
 * @test
 * @summary javax.naming.ldap.UnsolicitedNotificationEvent conformance: notification.
 * @run main UnsolicitedNotificationEventTest
 */



import javax.naming.NamingException;
import javax.naming.event.NamingExceptionEvent;
import javax.naming.event.NamingListener;
import javax.naming.ldap.Control;
import javax.naming.ldap.UnsolicitedNotification;
import javax.naming.ldap.UnsolicitedNotificationEvent;
import javax.naming.ldap.UnsolicitedNotificationListener;
public class UnsolicitedNotificationEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static javax.naming.ldap.UnsolicitedNotification mkNotif() {
        return new javax.naming.ldap.UnsolicitedNotification() {
            public String getID() { return "1.2.3"; }
            public byte[] getEncodedValue() { return null; }
            public javax.naming.ldap.Control[] getControls() { return null; }
            public String[] getReferrals() { return null; }
            public javax.naming.NamingException getException() { return null; }
        };
    }
    public static void main(String[] args) throws Exception {
        javax.naming.ldap.UnsolicitedNotification n = mkNotif();
        javax.naming.ldap.UnsolicitedNotificationEvent e = new javax.naming.ldap.UnsolicitedNotificationEvent("src", n);
        check(e.getNotification() == n, "getNotification");
        check("src".equals(e.getSource()), "getSource");        testDeepContracts();
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
