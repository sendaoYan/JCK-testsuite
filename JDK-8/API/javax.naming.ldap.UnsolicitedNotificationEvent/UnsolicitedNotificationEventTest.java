/*
 * @test
 * @summary javax.naming.ldap.UnsolicitedNotificationEvent conformance: notification.
 * @run main UnsolicitedNotificationEventTest
 */


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
        check("src".equals(e.getSource()), "getSource");        System.out.println("PASS (" + passed + " assertions)");
    }
}
