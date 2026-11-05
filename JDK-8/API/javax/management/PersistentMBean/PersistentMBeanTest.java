/*
 * @test
 * @summary javax.management.PersistentMBean conformance: interface reflection.
 * @run main PersistentMBeanTest
 */

import javax.management.*;
import java.util.*;

public class PersistentMBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.PersistentMBean");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "load"), "declares load");
        check(hasMethod(c, "store"), "declares store");
        testPersistInterfaceShape();
        testPersistRecording();
        testPersistAsMBean();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened (batch e): interface surface, recording implementation,
    //      store() exception propagation, coexistence with standard MBean ----

    public static interface PPersistMBean {
        int getCount(); void setCount(int v); void reset(); int add(int x); boolean isReady();
    }

    public static class PPersist implements PPersistMBean, PersistentMBean {
        public static final List<String> log = new ArrayList<String>();
        private int count = 11;
        public static void resetLog() { log.clear(); }
        public int getCount() { return count; }
        public void setCount(int v) { count = v; }
        public void reset() { count = 0; }
        public int add(int x) { return count + x; }
        public boolean isReady() { return true; }
        public void load() { log.add("load"); }
        public void store() throws MBeanException {
            log.add("store:" + count);
            throw new MBeanException(new Exception("boom-store"));
        }
    }

    static void testPersistInterfaceShape() throws Exception {
        Class<?> c = Class.forName("javax.management.PersistentMBean");
        check(c.isInterface(), "persist is an interface");
        java.lang.reflect.Method load = c.getMethod("load");
        java.lang.reflect.Method store = c.getMethod("store");
        check(load.getExceptionTypes().length == 3, "load declares 3 checked exceptions");
        check(store.getExceptionTypes().length == 3, "store declares 3 checked exceptions");
        boolean loadThrowsMbe = false, storeThrowsInf = false, roe = false;
        for (Class<?> e : load.getExceptionTypes()) {
            if (e.getName().equals("javax.management.MBeanException")) loadThrowsMbe = true;
            if (e.getName().equals("javax.management.RuntimeOperationsException")) roe = true;
        }
        for (Class<?> e : store.getExceptionTypes()) {
            if (e.getName().equals("javax.management.InstanceNotFoundException")) storeThrowsInf = true;
        }
        check(loadThrowsMbe && roe && storeThrowsInf, "load/store throw MBeanException/RuntimeOperationsException/InstanceNotFoundException");
    }

    static void testPersistRecording() throws Exception {
        PPersist.resetLog();
        PPersist bean = new PPersist();
        bean.load();
        check(PPersist.log.equals(Arrays.asList("load")), "load recorded");
        try {
            bean.store();
            check(false, "store exception must propagate");
        } catch (MBeanException me) {
            check("boom-store".equals(me.getCause().getMessage()), "store exception propagated with same cause");
            passed++;
        }
        check(PPersist.log.equals(Arrays.asList("load", "store:11")), "store recorded with count");
    }

    static void testPersistAsMBean() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName on = new ObjectName("jck.persist", "k", "p");
        PPersist bean = new PPersist();
        ObjectInstance oi = server.registerMBean(bean, on);
        check(oi.getObjectName().equals(on), "PersistentMBean impl registers as a standard MBean");
        check(Integer.valueOf(11).equals(server.getAttribute(on, "Count")),
                "attribute routing alongside PersistentMBean");
        check(Boolean.TRUE.equals(server.getAttribute(on, "Ready")), "isIs routing alongside PersistentMBean");
        check(Integer.valueOf(14).equals(server.invoke(on, "add", new Object[]{Integer.valueOf(3)}, new String[]{"int"})),
                "invoke routing alongside PersistentMBean");
        MBeanInfo info = server.getMBeanInfo(on);
        boolean hasLoad = false, hasStore = false;
        for (MBeanOperationInfo o : info.getOperations()) {
            if ("load".equals(o.getName())) hasLoad = true;
            if ("store".equals(o.getName())) hasStore = true;
        }
        check(!hasLoad && !hasStore, "load/store are not exposed as management operations");
    }
}
