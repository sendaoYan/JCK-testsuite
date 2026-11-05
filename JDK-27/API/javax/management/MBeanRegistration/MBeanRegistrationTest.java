/*
 * @test
 * @summary javax.management.MBeanRegistration conformance: white-box structure + light behavior.
 * @run main MBeanRegistrationTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.lang.management.*;
import javax.management.*;

public class MBeanRegistrationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    public interface ThingMBean {
        int getCount();
    }
    public static class Thing implements ThingMBean, MBeanRegistration {
        private int count = 7;
        private final List<String> events;
        Thing(List<String> events) { this.events = events; }
        public int getCount() { return count; }
        public ObjectName preRegister(MBeanServer server, ObjectName name) { events.add("preRegister"); return name; }
        public void postRegister(Boolean registrationDone) { events.add("postRegister"); }
        public void preDeregister() { events.add("preDeregister"); }
        public void postDeregister() { events.add("postDeregister"); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.MBeanRegistration");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "preRegister"), "has preRegister");
        check(hasMethod(c, "postRegister"), "has postRegister");
        check(hasMethod(c, "preDeregister"), "has preDeregister");
        check(hasMethod(c, "postDeregister"), "has postDeregister");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        List<String> events = new ArrayList<String>();
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName on = new ObjectName("jck.test:type=MBeanRegistrationTest");
        mbs.registerMBean(new Thing(events), on);
        check(events.contains("preRegister"), "preRegister invoked");
        check(events.contains("postRegister"), "postRegister invoked");

        mbs.unregisterMBean(on);
        check(events.contains("preDeregister"), "preDeregister invoked");
        check(events.contains("postDeregister"), "postDeregister invoked");

        // lifecycle order: preRegister before postRegister, preDeregister before postDeregister
        int preR = events.indexOf("preRegister"), postR = events.indexOf("postRegister");
        int preD = events.indexOf("preDeregister"), postD = events.indexOf("postDeregister");
        check(preR >= 0 && postR > preR, "register order");
        check(preD >= 0 && postD > preD, "deregister order");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testRegFullCycle();
        testRegRename();
        testRegNullName();
        testRegPreRegisterFailures();
        testRegPreDeregisterFailure();
        testRegDuplicateName();
        System.out.println("PASS (" + passed + " assertions)");
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    // ---- deepened (batch e): full lifecycle ordering, rename, null name,
    //      exceptions thrown from hooks, duplicate-name postRegister(false) ----

    public static interface RBaseMBean {
        int getCount(); void setCount(int v); void reset(); int add(int x); boolean isReady();
    }

    public static class RBase implements RBaseMBean, MBeanRegistration {
        public static final List<String> log = new ArrayList<String>();
        public static MBeanServer seenServer;
        protected int count = 5;
        public static void resetLog() { log.clear(); seenServer = null; }
        public int getCount() { return count; }
        public void setCount(int v) { count = v; }
        public void reset() { count = 0; }
        public int add(int x) { return count + x; }
        public boolean isReady() { return true; }
        public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
            log.add("preRegister:" + name.getCanonicalName());
            seenServer = server;
            return name;
        }
        public void postRegister(Boolean registrationDone) { log.add("postRegister:" + registrationDone); }
        public void preDeregister() throws Exception { log.add("preDeregister"); }
        public void postDeregister() { log.add("postDeregister"); }
    }

    public static class RRename extends RBase {
        public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
            log.add("preRegister:" + name.getCanonicalName());
            seenServer = server;
            return new ObjectName("jck.reg", "renamed", "yes");
        }
    }

    public static class RNullName extends RBase {
        public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
            log.add("preRegister:" + name.getCanonicalName());
            seenServer = server;
            return null;
        }
    }

    public static class RPreChk extends RBase {
        public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
            log.add("preRegister:" + name.getCanonicalName());
            seenServer = server;
            throw new Exception("checked-boom");
        }
    }

    public static class RPreRte extends RBase {
        public ObjectName preRegister(MBeanServer server, ObjectName name) {
            log.add("preRegister:" + name.getCanonicalName());
            seenServer = server;
            throw new IllegalStateException("rte-boom");
        }
    }

    public static class RPreDeregChk extends RBase {
        public void preDeregister() throws Exception {
            log.add("preDeregister");
            throw new Exception("dereg-boom");
        }
    }

    static void testRegFullCycle() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName on = new ObjectName("jck.reg", "k", "cycle");
        RBase.resetLog();
        server.registerMBean(new RBase(), on);
        server.unregisterMBean(on);
        check(RBase.log.equals(Arrays.asList(
                "preRegister:jck.reg:k=cycle", "postRegister:true", "preDeregister", "postDeregister")),
                "registration lifecycle order: " + RBase.log);
        check(RBase.seenServer == server, "preRegister receives the target server");
    }

    static void testRegRename() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName requested = new ObjectName("jck.reg", "k", "orig");
        ObjectName renamed = new ObjectName("jck.reg", "renamed", "yes");
        RBase.resetLog();
        ObjectInstance oi = server.registerMBean(new RRename(), requested);
        check(oi.getObjectName().equals(renamed), "registered under the name returned by preRegister");
        check(!server.isRegistered(requested), "requested name not registered");
        check(server.isRegistered(renamed), "renamed name is registered");
        check(server.getMBeanInfo(renamed).getClassName().equals(RRename.class.getName()),
                "renamed name reachable via getMBeanInfo");
    }

    static void testRegNullName() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName requested = new ObjectName("jck.reg", "k", "nullname");
        RBase.resetLog();
        server.registerMBean(new RNullName(), requested);
        check(server.isRegistered(requested), "preRegister returning null keeps the requested name");
        check(RBase.log.equals(Arrays.asList("preRegister:jck.reg:k=nullname", "postRegister:true")),
                "null-name registration log: " + RBase.log);
    }

    static void testRegPreRegisterFailures() throws Exception {
        final MBeanServer server = MBeanServerFactory.newMBeanServer();
        final ObjectName chk = new ObjectName("jck.reg", "k", "chk");
        RBase.resetLog();
        checkThrows(MBeanRegistrationException.class, new ThrowingRunnable() {
            public void run() throws Exception { server.registerMBean(new RPreChk(), chk); }
        }, "checked exception from preRegister wrapped in MBeanRegistrationException");
        check(!server.isRegistered(chk), "not registered after preRegister failure");
        check(RBase.log.equals(Arrays.asList("preRegister:jck.reg:k=chk")),
                "postRegister not invoked when preRegister throws: " + RBase.log);
        final ObjectName rte = new ObjectName("jck.reg", "k", "rte");
        RBase.resetLog();
        checkThrows(RuntimeMBeanException.class, new ThrowingRunnable() {
            public void run() throws Exception { server.registerMBean(new RPreRte(), rte); }
        }, "runtime exception from preRegister surfaces as RuntimeMBeanException");
        check(!server.isRegistered(rte), "not registered after preRegister RTE");
    }

    static void testRegPreDeregisterFailure() throws Exception {
        final MBeanServer server = MBeanServerFactory.newMBeanServer();
        final ObjectName on = new ObjectName("jck.reg", "k", "dereg");
        RBase.resetLog();
        server.registerMBean(new RPreDeregChk(), on);
        RBase.resetLog();
        checkThrows(MBeanRegistrationException.class, new ThrowingRunnable() {
            public void run() throws Exception { server.unregisterMBean(on); }
        }, "checked exception from preDeregister wrapped in MBeanRegistrationException");
        check(server.isRegistered(on), "MBean stays registered when preDeregister throws");
        check(RBase.log.equals(Arrays.asList("preDeregister")), "postDeregister not invoked: " + RBase.log);
    }

    static void testRegDuplicateName() throws Exception {
        final MBeanServer server = MBeanServerFactory.newMBeanServer();
        final ObjectName on = new ObjectName("jck.reg", "k", "dup");
        server.registerMBean(new RBase(), on);
        RBase.resetLog();
        checkThrows(InstanceAlreadyExistsException.class, new ThrowingRunnable() {
            public void run() throws Exception { server.registerMBean(new RBase(), on); }
        }, "duplicate registration name");
        check(RBase.log.equals(Arrays.asList("preRegister:jck.reg:k=dup", "postRegister:false")),
                "postRegister(false) invoked after name conflict: " + RBase.log);
    }
}
