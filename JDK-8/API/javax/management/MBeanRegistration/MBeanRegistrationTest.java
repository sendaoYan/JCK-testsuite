/*
 * @test
 * @summary javax.management.MBeanRegistration conformance: interface reflection.
 * @run main MBeanRegistrationTest
 */

import javax.management.*;
import java.util.*;

public class MBeanRegistrationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.MBeanRegistration");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "postDeregister"), "declares postDeregister");
        check(hasMethod(c, "postRegister"), "declares postRegister");
        check(hasMethod(c, "preDeregister"), "declares preDeregister");
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
