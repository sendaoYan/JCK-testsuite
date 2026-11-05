/*
 * @test
 * @summary javax.naming.ldap.UnsolicitedNotificationListener conformance: interface hierarchy and members.
 * @run main UnsolicitedNotificationListenerTest
 */



import javax.naming.NamingException;
import javax.naming.event.EventContext;
import javax.naming.event.NamingExceptionEvent;
import javax.naming.event.NamingListener;
import javax.naming.ldap.Control;
import javax.naming.ldap.UnsolicitedNotification;
import javax.naming.ldap.UnsolicitedNotificationEvent;
import javax.naming.ldap.UnsolicitedNotificationListener;
public class UnsolicitedNotificationListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.UnsolicitedNotificationListener");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("javax.naming.event.NamingListener");
        java.util.Set<String> actIfs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("namingExceptionThrown"), "has method namingExceptionThrown");
        check(names.contains("notificationReceived"), "has method notificationReceived");        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dUnlSurface();
        dUnlInvocation();
        System.out.println("deep: UnsolicitedNotificationListener contracts OK");
    }

    static void dUnlSurface() throws Exception {
        java.util.Set<String> own = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : UnsolicitedNotificationListener.class.getDeclaredMethods())
            own.add(m.getName());
        check(own.contains("notificationReceived"), "declares notificationReceived");
        check(NamingListener.class.isAssignableFrom(UnsolicitedNotificationListener.class),
                "extends NamingListener");
        // the inherited method is namingExceptionThrown
        java.util.Set<String> all = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : UnsolicitedNotificationListener.class.getMethods())
            all.add(m.getName());
        check(all.contains("namingExceptionThrown"), "inherits namingExceptionThrown");
    }

    static class DeepNotice implements UnsolicitedNotification {
        public String getID() { return "n"; }
        public byte[] getEncodedValue() { return null; }
        public String[] getReferrals() { return null; }
        public NamingException getException() { return null; }
        public Control[] getControls() { return null; }
    }

    static void dUnlInvocation() {
        final java.util.List<String> log = new java.util.ArrayList<>();
        UnsolicitedNotificationListener l = new UnsolicitedNotificationListener() {
            public void notificationReceived(UnsolicitedNotificationEvent e) {
                log.add("notice:" + e.getNotification().getID());
            }
            public void namingExceptionThrown(NamingExceptionEvent e) { log.add("exc"); }
        };
        UnsolicitedNotificationEvent e =
                new UnsolicitedNotificationEvent("src", new DeepNotice());
        e.dispatch(l);
        check(log.equals(java.util.Arrays.asList("notice:n")), "notification delivered");
        new NamingExceptionEvent(nullStub(), new javax.naming.NamingException()).dispatch(l);
        check(log.equals(java.util.Arrays.asList("notice:n", "exc")),
                "inherited exception callback also delivered");
    }

    static EventContext nullStub() {
        try {
            return (EventContext) java.lang.reflect.Proxy.newProxyInstance(
                    EventContext.class.getClassLoader(), new Class<?>[]{EventContext.class},
                    (proxy, method, args) -> null);
        } catch (RuntimeException e) { throw e; }
    }

}
