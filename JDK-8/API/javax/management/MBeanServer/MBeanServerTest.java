/*
 * @test
 * @summary javax.management.MBeanServer conformance: platform MBeanServer only
 *          (no custom MBean registration). getAttribute/getMBeanInfo/queryNames/
 *          queryMBeans/isRegistered against the deterministic
 *          java.lang:type=Memory MXBean, setAttribute idempotent round-trip,
 *          AttributeNotFoundException for unknown attribute,
 *          InstanceNotFoundException for unknown ObjectName, empty queryNames,
 *          and MBeanServerConnection interface conformance.
 * @run main MBeanServerTest
 */

import java.lang.management.ManagementFactory;
import java.util.Set;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

public class MBeanServerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

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

    static final ObjectName MEMORY = newObjectName("java.lang:type=Memory");

    static ObjectName newObjectName(String s) {
        try { return new ObjectName(s); } catch (Exception e) { throw new Error(e); }
    }

    public static void main(String[] args) throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        check(mbs != null, "platform MBeanServer");
        check(mbs instanceof MBeanServerConnection, "MBeanServer is a MBeanServerConnection");

        // ---- getAttribute against the deterministic Memory MXBean ----
        check(mbs.isRegistered(MEMORY), "Memory MXBean registered");
        Object heap = mbs.getAttribute(MEMORY, "HeapMemoryUsage");
        check(heap instanceof CompositeData, "HeapMemoryUsage is CompositeData");
        CompositeData cd = (CompositeData) heap;
        check(cd.containsKey("used") && cd.containsKey("max"), "CompositeData keys");
        check(((Number) cd.get("used")).longValue() >= 0, "used >= 0");

        Object pending = mbs.getAttribute(MEMORY, "ObjectPendingFinalizationCount");
        check(pending instanceof Number && ((Number) pending).longValue() >= 0, "pending finalization count");

        // ---- setAttribute idempotent round-trip (no state change) ----
        Object verbOrig = mbs.getAttribute(MEMORY, "Verbose");
        check(verbOrig instanceof Boolean, "Verbose is Boolean");
        mbs.setAttribute(MEMORY, new Attribute("Verbose", verbOrig));
        check(verbOrig.equals(mbs.getAttribute(MEMORY, "Verbose")), "setAttribute idempotent");

        // ---- getMBeanInfo ----
        MBeanInfo info = mbs.getMBeanInfo(MEMORY);
        check(info.getClassName() != null, "MBeanInfo class name non-null");
        check(info.getAttributes().length > 0, "MBeanInfo has attributes");
        boolean hasHeap = false;
        for (javax.management.MBeanAttributeInfo ai : info.getAttributes()) {
            if ("HeapMemoryUsage".equals(ai.getName())) { hasHeap = true; break; }
        }
        check(hasHeap, "MBeanInfo lists HeapMemoryUsage");

        // ---- queryNames / queryMBeans ----
        Set<ObjectName> names = mbs.queryNames(newObjectName("java.lang:type=*"), null);
        check(names.contains(MEMORY), "queryNames contains Memory");
        Set<ObjectName> memOnly = mbs.queryNames(MEMORY, null);
        check(memOnly.size() == 1 && memOnly.contains(MEMORY), "queryNames exact");
        Set<ObjectInstance> objs = mbs.queryMBeans(newObjectName("java.lang:type=Memory"), null);
        check(objs.size() == 1, "queryMBeans exact");
        Set<ObjectName> none = mbs.queryNames(newObjectName("no.such.domain:type=*"), null);
        check(none.isEmpty(), "queryNames empty for unknown domain");

        // ---- AttributeNotFoundException ----
        checkThrows(AttributeNotFoundException.class,
                new ThrowingRunnable() { public void run() throws Exception { mbs.getAttribute(MEMORY, "NoSuchAttribute"); } },
                "getAttribute unknown attribute");

        // ---- InstanceNotFoundException ----
        final ObjectName missing = newObjectName("java.lang:type=DoesNotExist");
        check(!mbs.isRegistered(missing), "missing not registered");
        checkThrows(InstanceNotFoundException.class,
                new ThrowingRunnable() { public void run() throws Exception { mbs.getAttribute(missing, "Verbose"); } },
                "getAttribute missing instance");
        checkThrows(InstanceNotFoundException.class,
                new ThrowingRunnable() { public void run() throws Exception { mbs.getMBeanInfo(missing); } },
                "getMBeanInfo missing instance");
        checkThrows(InstanceNotFoundException.class,
                new ThrowingRunnable() { public void run() throws Exception { mbs.invoke(missing, "gc", new Object[0], new String[0]); } },
                "invoke missing instance");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
