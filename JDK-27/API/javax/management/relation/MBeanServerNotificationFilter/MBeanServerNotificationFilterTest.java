/*
 * @test
 * @summary javax.management.relation.MBeanServerNotificationFilter conformance: white-box structure + black-box behavior.
 * @run main MBeanServerNotificationFilterTest
 */
import java.util.Vector;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.relation.MBeanServerNotificationFilter;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
public class MBeanServerNotificationFilterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static ObjectName on(String s) { try { return new ObjectName(s); } catch (Exception e) { throw new RuntimeException(e); } }
    static MBeanServerNotification reg(ObjectName o) {
        return new MBeanServerNotification(MBeanServerNotification.REGISTRATION_NOTIFICATION, o, 1L, o);
    }
    static MBeanServerNotification unreg(ObjectName o) {
        return new MBeanServerNotification(MBeanServerNotification.UNREGISTRATION_NOTIFICATION, o, 2L, o);
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.relation.MBeanServerNotificationFilter");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "filter must be public");
        check(!c.isInterface(), "filter must be a class");
        check(c.getSuperclass() == NotificationFilterSupport.class, "filter must extend NotificationFilterSupport");
        c.getMethod("enableObjectName", ObjectName.class);
        c.getMethod("disableObjectName", ObjectName.class);
        c.getMethod("enableAllObjectNames");
        c.getMethod("disableAllObjectNames");
        c.getMethod("getEnabledObjectNames");
        c.getMethod("getDisabledObjectNames");
        c.getMethod("isNotificationEnabled", Notification.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ObjectName n1 = on("d:type=one");
        ObjectName n2 = on("d:type=two");

        // fresh filter: constructor enables both registration and unregistration types
        MBeanServerNotificationFilter fresh = new MBeanServerNotificationFilter();
        Vector<String> types = fresh.getEnabledTypes();
        check(types.contains(MBeanServerNotification.REGISTRATION_NOTIFICATION), "registration type enabled");
        check(types.contains(MBeanServerNotification.UNREGISTRATION_NOTIFICATION), "unregistration type enabled");
        check(fresh.getEnabledObjectNames() != null && fresh.getEnabledObjectNames().isEmpty(), "fresh enabled ObjectNames empty");
        check(fresh.getDisabledObjectNames() == null, "fresh disabled ObjectNames null");

        // enableAllObjectNames: accepts all names
        MBeanServerNotificationFilter f = new MBeanServerNotificationFilter();
        f.enableAllObjectNames();
        check(f.getEnabledObjectNames() == null, "enableAll -> enabled names null");
        check(f.getDisabledObjectNames() != null && f.getDisabledObjectNames().isEmpty(), "enableAll -> disabled names empty");
        check(f.isNotificationEnabled(reg(n1)), "enableAll accepts registration for n1");
        check(f.isNotificationEnabled(unreg(n2)), "enableAll accepts unregistration for n2");

        // disableAllObjectNames: rejects everything
        f.disableAllObjectNames();
        check(f.getEnabledObjectNames() != null && f.getEnabledObjectNames().isEmpty(), "disableAll -> enabled names empty");
        check(f.getDisabledObjectNames() == null, "disableAll -> disabled names null");
        check(!f.isNotificationEnabled(reg(n1)), "disableAll rejects registration");

        // enableAll then disableObjectName(n1): rejects n1, accepts n2
        f.enableAllObjectNames();
        f.disableObjectName(n1);
        check(!f.isNotificationEnabled(reg(n1)), "explicitly disabled name rejected");
        check(f.isNotificationEnabled(reg(n2)), "non-disabled name accepted");

        // disableAll then enableObjectName(n1): accepts n1, rejects n2
        f.disableAllObjectNames();
        f.enableObjectName(n1);
        check(f.isNotificationEnabled(reg(n1)), "explicitly enabled name accepted");
        check(!f.isNotificationEnabled(reg(n2)), "non-enabled name rejected");

        // a non-MBeanServerNotification type is always rejected
        f.enableAllObjectNames();
        check(!f.isNotificationEnabled(new Notification("other.type", n1, 3L)), "non MBeanServer type rejected");

        // null arguments
        try { f.enableObjectName(null); check(false, "enableObjectName(null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "enableObjectName null message"); }
        try { f.disableObjectName(null); check(false, "disableObjectName(null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "disableObjectName null message"); }
        try { f.isNotificationEnabled(null); check(false, "isNotificationEnabled(null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "isNotificationEnabled null message"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFreshFilterSelectMode();
        testSelectionSemantics();
        testReturnedVectorsAreCopies();
        testInheritedTypeGate();
        testFilterSerialization();
        testLiveDelegateLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }

    @SuppressWarnings("unchecked")
    static <T> T roundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        return (T) ois.readObject();
    }

    // ---- deepened (batch k): fresh-filter select mode, copy semantics, inherited type gate,
    //      serialization, and a live MBeanServerDelegate notification loop ----

    public static interface ObservedMBean { int getCount(); void setCount(int count); }

    public static class Observed implements ObservedMBean {
        private int count;
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }

    static void testFreshFilterSelectMode() {
        MBeanServerNotificationFilter fresh = new MBeanServerNotificationFilter();
        // a fresh filter is in SELECT mode with an empty selected set: it rejects everything
        check(fresh.getEnabledObjectNames() != null && fresh.getEnabledObjectNames().isEmpty(),
              "fresh filter: the selected set exists and is empty");
        check(fresh.getDisabledObjectNames() == null, "fresh filter: the deselected set is null (select mode)");
        check(!fresh.isNotificationEnabled(reg(on("jck.ns:fresh=a"))),
              "fresh filter rejects registration notifications");
        check(!fresh.isNotificationEnabled(unreg(on("jck.ns:fresh=b"))),
              "fresh filter rejects unregistration notifications");
    }

    static void testSelectionSemantics() {
        ObjectName a = on("jck.ns:sel=a");
        ObjectName b = on("jck.ns:sel=b");
        MBeanServerNotificationFilter f = new MBeanServerNotificationFilter();
        f.enableObjectName(a);
        Vector<ObjectName> enabled = f.getEnabledObjectNames();
        check(enabled.size() == 1 && enabled.contains(a), "enableObjectName adds the name to the selected set");
        check(f.isNotificationEnabled(reg(a)), "an enabled name passes registration");
        check(f.isNotificationEnabled(unreg(a)), "an enabled name passes unregistration too");
        check(!f.isNotificationEnabled(reg(b)), "a non-enabled name is filtered out");
        // in select mode disableObjectName removes the name from the selected set
        f.disableObjectName(a);
        check(f.getEnabledObjectNames().isEmpty(), "disableObjectName removes the name from the selected set");
        check(!f.isNotificationEnabled(reg(a)), "the removed name no longer passes");
        // deselect mode: enableAll, then explicit disables subtract from the accepted set
        f.enableAllObjectNames();
        check(f.getEnabledObjectNames() == null, "enableAllObjectNames switches to deselect mode (selected null)");
        check(f.isNotificationEnabled(reg(a)) && f.isNotificationEnabled(reg(b)),
              "enableAllObjectNames accepts any name");
        f.disableObjectName(a);
        check(!f.isNotificationEnabled(reg(a)), "an explicitly disabled name is rejected in deselect mode");
        check(f.isNotificationEnabled(reg(b)), "other names stay accepted in deselect mode");
        check(f.getDisabledObjectNames().size() == 1 && f.getDisabledObjectNames().contains(a),
              "getDisabledObjectNames reports the deselected name");
        // re-enabling the name clears the deselect
        f.enableObjectName(a);
        check(f.isNotificationEnabled(reg(a)), "enableObjectName clears a previous disable");
        check(f.getDisabledObjectNames().isEmpty(), "the deselected set is empty again");
        // disableAll wins over everything
        f.disableAllObjectNames();
        check(f.getEnabledObjectNames() != null && f.getEnabledObjectNames().isEmpty(),
              "disableAllObjectNames: the selected set is empty (not null)");
        check(f.getDisabledObjectNames() == null, "disableAllObjectNames: the deselected set is null");
        check(!f.isNotificationEnabled(reg(a)) && !f.isNotificationEnabled(reg(b)),
              "disableAllObjectNames rejects every name");
        // enabling the same name twice keeps a single entry
        MBeanServerNotificationFilter g = new MBeanServerNotificationFilter();
        g.enableObjectName(a);
        g.enableObjectName(a);
        check(g.getEnabledObjectNames().size() == 1, "enabling the same name twice keeps one entry");
        g.disableObjectName(a);
        g.disableObjectName(a);
        check(g.getEnabledObjectNames().isEmpty(), "disabling twice keeps the set empty");
    }

    static void testReturnedVectorsAreCopies() {
        ObjectName a = on("jck.ns:copy=a");
        MBeanServerNotificationFilter f = new MBeanServerNotificationFilter();
        f.enableObjectName(a);
        Vector<ObjectName> v = f.getEnabledObjectNames();
        v.clear();
        check(f.getEnabledObjectNames().size() == 1,
              "clearing a returned enabled-vector leaves the filter intact");
        f.enableAllObjectNames();
        f.disableObjectName(a);
        Vector<ObjectName> d = f.getDisabledObjectNames();
        d.clear();
        check(f.getDisabledObjectNames().size() == 1,
              "clearing a returned disabled-vector leaves the filter intact");
    }

    static void testInheritedTypeGate() {
        ObjectName a = on("jck.ns:gate=a");
        MBeanServerNotificationFilter f = new MBeanServerNotificationFilter();
        f.enableAllObjectNames();
        f.disableType(MBeanServerNotification.REGISTRATION_NOTIFICATION);
        check(!f.isNotificationEnabled(reg(a)), "disableType(REGISTRATION) rejects registration");
        check(f.isNotificationEnabled(unreg(a)), "unregistration stays enabled");
        check(f.getEnabledTypes().size() == 1
              && f.getEnabledTypes().contains(MBeanServerNotification.UNREGISTRATION_NOTIFICATION),
              "only the unregistration type remains enabled");
        // disabling both types blocks everything even in enableAll mode
        f.disableType(MBeanServerNotification.UNREGISTRATION_NOTIFICATION);
        check(!f.isNotificationEnabled(reg(a)) && !f.isNotificationEnabled(unreg(a)),
              "with both types disabled every notification is rejected");
    }

    static void testFilterSerialization() throws Exception {
        MBeanServerNotificationFilter f = new MBeanServerNotificationFilter();
        ObjectName a = on("jck.ns:ser=a");
        ObjectName b = on("jck.ns:ser=b");
        f.enableObjectName(a);
        f.enableObjectName(b);
        MBeanServerNotificationFilter back = roundTrip(f);
        check(back.isNotificationEnabled(reg(a)) && back.isNotificationEnabled(unreg(b)),
              "round-trip keeps the selected names working for both types");
        check(!back.isNotificationEnabled(reg(on("jck.ns:ser=c"))),
              "round-trip keeps unselected names out");
        // deselect mode survives a round-trip
        MBeanServerNotificationFilter f2 = new MBeanServerNotificationFilter();
        f2.enableAllObjectNames();
        f2.disableObjectName(a);
        MBeanServerNotificationFilter back2 = roundTrip(f2);
        check(back2.getEnabledObjectNames() == null, "round-trip keeps deselect mode");
        check(!back2.isNotificationEnabled(reg(a)), "round-trip keeps the deselected name out");
        check(back2.isNotificationEnabled(reg(b)), "round-trip keeps other names passing");
        long suid = java.io.ObjectStreamClass.lookup(MBeanServerNotificationFilter.class).getSerialVersionUID();
        check(suid == 2605900539589789736L, "serialVersionUID locked across releases: " + suid);
    }

    static void testLiveDelegateLoop() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName accepted = new ObjectName("jck.ns:live=accepted");
        ObjectName rejected = new ObjectName("jck.ns:live=rejected");
        final java.util.List<Notification> received =
                java.util.Collections.synchronizedList(new java.util.ArrayList<Notification>());
        MBeanServerNotificationFilter filter = new MBeanServerNotificationFilter();
        filter.enableObjectName(accepted);
        server.addNotificationListener(new ObjectName("JMImplementation:type=MBeanServerDelegate"),
                new NotificationListener() {
                    public void handleNotification(Notification n, Object handback) { received.add(n); }
                }, filter, null);
        server.registerMBean(new Observed(), accepted);
        server.registerMBean(new Observed(), rejected);
        // bounded wait (<=5s, 50ms steps) for the filtered-in registration
        long deadline = System.currentTimeMillis() + 5000;
        while (received.size() < 1 && System.currentTimeMillis() < deadline) Thread.sleep(50);
        check(received.size() == 1, "exactly one notification was delivered, got " + received.size());
        Notification n = received.get(0);
        check(MBeanServerNotification.REGISTRATION_NOTIFICATION.equals(n.getType()),
              "the live loop delivered the registration type");
        check(accepted.equals(((MBeanServerNotification) n).getMBeanName()),
              "the live loop carried the enabled ObjectName");
        // bounded observation window: the rejected registration must never arrive
        long quiet = System.currentTimeMillis() + 500;
        while (System.currentTimeMillis() < quiet && received.size() == 1) Thread.sleep(50);
        check(received.size() == 1,
              "the rejected name's registration never passed the filter, got " + received.size());
    }
}
