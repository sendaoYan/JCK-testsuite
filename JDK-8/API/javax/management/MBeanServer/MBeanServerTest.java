/*
 * @test
 * @summary [deepened 2026-11: local server closed loop, exception matrices] javax.management.MBeanServer conformance: platform MBeanServer only
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

        deepRegisterInstance();
        deepAttributesInvoke();
        deepExceptionMatrix();
        deepQueryLifecycle();
        deepMetadataCreateMBean();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ==== appended deepening (2026-11): local MBeanServer closed-loop types ====

    public static class CustomBoom extends Exception {
        private static final long serialVersionUID = 1L;
        CustomBoom(String m) { super(m); }
    }

    public static interface GaugeMBean {
        int getCount();
        void setCount(int v);
        boolean isReady();
        void reset();
        int add(int x);
        void boom() throws Exception;
    }

    public static class Gauge implements GaugeMBean {
        static final Exception BOOM = new CustomBoom("boom-marker");
        private int count = 3;
        public int getCount() { return count; }
        public void setCount(int v) { count = v; }
        public boolean isReady() { return true; }
        public void reset() { count = 0; }
        public int add(int x) { return count + x; }
        public void boom() throws Exception { throw BOOM; }
    }

    static final boolean IS_JAVA_8 = System.getProperty("java.version").startsWith("1.8");

    // ==== appended deepening (2026-11): registerMBean / ObjectInstance ====

    static void deepRegisterInstance() throws Exception {
        final javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        final javax.management.ObjectName on = new javax.management.ObjectName("jck.srv:type=Gauge");
        javax.management.ObjectInstance oi = server.registerMBean(new Gauge(), on);
        check(oi != null, "registerMBean returns an ObjectInstance");
        check(on.equals(oi.getObjectName()), "ObjectInstance.getObjectName exact");
        check(Gauge.class.getName().equals(oi.getClassName()), "ObjectInstance.getClassName exact");
        check(server.isRegistered(on), "isRegistered after register");
        check(on.equals(server.getObjectInstance(on).getObjectName()), "getObjectInstance name exact");
        check(Gauge.class.getName().equals(server.getObjectInstance(on).getClassName()), "getObjectInstance className exact");
        check(server.isInstanceOf(on, Gauge.class.getName()), "isInstanceOf exact class");
        check(server.isInstanceOf(on, Object.class.getName()), "isInstanceOf accepts superclass name");
        checkThrows(javax.management.InstanceAlreadyExistsException.class, new ThrowingRunnable() {
            public void run() throws Exception { server.registerMBean(new Gauge(), on); }
        }, "duplicate registerMBean");
        // null-argument matrix
        checkThrows(javax.management.RuntimeOperationsException.class, new ThrowingRunnable() {
            public void run() throws Exception { server.registerMBean(new Gauge(), null); }
        }, "registerMBean with null ObjectName");
        if (IS_JAVA_8) {
            checkThrows(NullPointerException.class, new ThrowingRunnable() {
                public void run() throws Exception { server.registerMBean(null, on); }
            }, "registerMBean(null impl) on JDK 8 -> plain NPE");
        } else {
            checkThrows(javax.management.RuntimeOperationsException.class, new ThrowingRunnable() {
                public void run() throws Exception { server.registerMBean(null, on); }
            }, "registerMBean(null impl) on JDK 9+ -> RuntimeOperationsException");
        }
    }

    // ==== appended deepening (2026-11): attribute/invoke round-trip ====

    static void deepAttributesInvoke() throws Exception {
        final javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        final javax.management.ObjectName on = new javax.management.ObjectName("jck.srv:type=Gauge");
        server.registerMBean(new Gauge(), on);
        check(server.getAttribute(on, "Count") instanceof Integer, "getAttribute boxes to Integer");
        check(Integer.valueOf(3).equals(server.getAttribute(on, "Count")), "getAttribute Count initial");
        check(Boolean.TRUE.equals(server.getAttribute(on, "Ready")), "isIs attribute surfaces as Ready");
        server.setAttribute(on, new javax.management.Attribute("Count", Integer.valueOf(9)));
        check(Integer.valueOf(9).equals(server.getAttribute(on, "Count")), "setAttribute round-trip");
        // batch getAttributes: unknown names are silently omitted
        javax.management.AttributeList got = server.getAttributes(on, new String[]{"Count", "Nope", "Ready"});
        check(got.size() == 2, "getAttributes omits unknown names");
        java.util.Map<String, Object> byName = new java.util.HashMap<String, Object>();
        for (Object o : got) {
            javax.management.Attribute a = (javax.management.Attribute) o;
            byName.put(a.getName(), a.getValue());
        }
        check(Integer.valueOf(9).equals(byName.get("Count")), "getAttributes Count value");
        check(Boolean.TRUE.equals(byName.get("Ready")), "getAttributes Ready value");
        // batch setAttributes: only successfully applied entries come back
        javax.management.AttributeList in = new javax.management.AttributeList();
        in.add(new javax.management.Attribute("Count", Integer.valueOf(42)));
        in.add(new javax.management.Attribute("Nope", Integer.valueOf(1)));
        check(server.setAttributes(on, in).size() == 1, "setAttributes omits unknown names");
        check(Integer.valueOf(42).equals(server.getAttribute(on, "Count")), "setAttributes applied");
        // typed invoke and void invoke
        Object r = server.invoke(on, "add", new Object[]{Integer.valueOf(5)}, new String[]{"int"});
        check(r instanceof Integer && ((Integer) r).intValue() == 47, "invoke add(5) exact result");
        check(Integer.valueOf(42).equals(server.getAttribute(on, "Count")), "pure operation leaves state");
        check(server.invoke(on, "reset", new Object[0], new String[0]) == null, "void invoke returns null");
        check(Integer.valueOf(0).equals(server.getAttribute(on, "Count")), "reset effect visible");
    }

    // ==== appended deepening (2026-11): exception matrix ====

    static void deepExceptionMatrix() throws Exception {
        final javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        final javax.management.ObjectName on = new javax.management.ObjectName("jck.srv:type=Gauge");
        server.registerMBean(new Gauge(), on);
        // AttributeNotFoundException carries the attribute name
        try {
            server.getAttribute(on, "NopeAttr");
            check(false, "getAttribute unknown must throw");
        } catch (javax.management.AttributeNotFoundException e) {
            check(e.getMessage() != null && e.getMessage().contains("NopeAttr"), "ANFE message contains attribute name");
        }
        // ReflectionException wraps NoSuchMethodException; target == cause
        try {
            server.invoke(on, "noOp", new Object[0], new String[0]);
            check(false, "invoke unknown operation must throw");
        } catch (javax.management.ReflectionException e) {
            check(e.getCause() instanceof NoSuchMethodException, "ReflectionException cause is NoSuchMethodException");
            check(e.getCause() == e.getTargetException(), "getTargetException identical to getCause");
            check(e.getMessage() != null && e.getMessage().contains("noOp"), "ReflectionException message names the operation");
        }
        // MBeanException preserves the exact thrown instance
        try {
            server.invoke(on, "boom", new Object[0], new String[0]);
            check(false, "business exception must surface");
        } catch (javax.management.MBeanException e) {
            check(e.getCause() == Gauge.BOOM, "MBeanException cause is the exact thrown instance");
            check(e.getTargetException() == Gauge.BOOM, "getTargetException identical");
        }
        // InvalidAttributeValueException for a wrong value type; state unchanged
        try {
            server.setAttribute(on, new javax.management.Attribute("Count", "notAnInt"));
            check(false, "wrong value type must throw");
        } catch (javax.management.InvalidAttributeValueException e) {
            check(e.getMessage() != null && e.getMessage().contains("Count"), "IAVE message contains attribute name");
        }
        check(Integer.valueOf(3).equals(server.getAttribute(on, "Count")), "failed setAttribute leaves state unchanged");
        // setAttribute unknown attribute
        try {
            server.setAttribute(on, new javax.management.Attribute("NopeAttr", Integer.valueOf(1)));
            check(false, "setAttribute unknown must throw");
        } catch (javax.management.AttributeNotFoundException e) {
            check(e.getMessage() != null && e.getMessage().contains("NopeAttr"), "setAttribute ANFE message contains name");
        }
        // InstanceNotFoundException on an unregistered name, per operation
        final javax.management.ObjectName miss = new javax.management.ObjectName("jck.srv:type=Missing");
        check(!server.isRegistered(miss), "missing name is not registered");
        checkThrows(javax.management.InstanceNotFoundException.class, new ThrowingRunnable() {
            public void run() throws Exception { server.getAttribute(miss, "Count"); }
        }, "getAttribute on missing MBean");
        checkThrows(javax.management.InstanceNotFoundException.class, new ThrowingRunnable() {
            public void run() throws Exception { server.setAttribute(miss, new javax.management.Attribute("Count", Integer.valueOf(1))); }
        }, "setAttribute on missing MBean");
        checkThrows(javax.management.InstanceNotFoundException.class, new ThrowingRunnable() {
            public void run() throws Exception { server.getAttributes(miss, new String[]{"Count"}); }
        }, "getAttributes on missing MBean");
        checkThrows(javax.management.InstanceNotFoundException.class, new ThrowingRunnable() {
            public void run() throws Exception { server.setAttributes(miss, new javax.management.AttributeList()); }
        }, "setAttributes on missing MBean");
        checkThrows(javax.management.InstanceNotFoundException.class, new ThrowingRunnable() {
            public void run() throws Exception { server.invoke(miss, "reset", new Object[0], new String[0]); }
        }, "invoke on missing MBean");
        checkThrows(javax.management.InstanceNotFoundException.class, new ThrowingRunnable() {
            public void run() throws Exception { server.unregisterMBean(miss); }
        }, "unregisterMBean on missing MBean");
        checkThrows(javax.management.InstanceNotFoundException.class, new ThrowingRunnable() {
            public void run() throws Exception { server.getMBeanInfo(miss); }
        }, "getMBeanInfo on missing MBean");
        checkThrows(javax.management.InstanceNotFoundException.class, new ThrowingRunnable() {
            public void run() throws Exception { server.getObjectInstance(miss); }
        }, "getObjectInstance on missing MBean");
        checkThrows(javax.management.InstanceNotFoundException.class, new ThrowingRunnable() {
            public void run() throws Exception { server.isInstanceOf(miss, "java.lang.Object"); }
        }, "isInstanceOf on missing MBean");
        // null-argument guards
        checkThrows(javax.management.RuntimeOperationsException.class, new ThrowingRunnable() {
            public void run() throws Exception { server.setAttribute(on, null); }
        }, "setAttribute(null Attribute)");
        checkThrows(javax.management.RuntimeOperationsException.class, new ThrowingRunnable() {
            public void run() throws Exception { server.setAttribute(on, new javax.management.Attribute(null, Integer.valueOf(1))); }
        }, "setAttribute(null attribute name)");
        checkThrows(javax.management.RuntimeOperationsException.class, new ThrowingRunnable() {
            public void run() throws Exception { server.getAttribute(on, null); }
        }, "getAttribute(null attribute name)");
        checkThrows(javax.management.ReflectionException.class, new ThrowingRunnable() {
            public void run() throws Exception { server.invoke(on, null, new Object[0], new String[0]); }
        }, "invoke(null operation)");
        checkThrows(javax.management.RuntimeOperationsException.class, new ThrowingRunnable() {
            public void run() throws Exception { server.getAttribute(null, "Count"); }
        }, "getAttribute(null ObjectName)");
        checkThrows(javax.management.RuntimeOperationsException.class, new ThrowingRunnable() {
            public void run() throws Exception { server.isRegistered(null); }
        }, "isRegistered(null)");
        checkThrows(javax.management.RuntimeOperationsException.class, new ThrowingRunnable() {
            public void run() throws Exception { server.unregisterMBean(null); }
        }, "unregisterMBean(null)");
        checkThrows(javax.management.RuntimeOperationsException.class, new ThrowingRunnable() {
            public void run() throws Exception { server.getMBeanInfo(null); }
        }, "getMBeanInfo(null)");
        checkThrows(javax.management.RuntimeOperationsException.class, new ThrowingRunnable() {
            public void run() throws Exception { server.getObjectInstance(null); }
        }, "getObjectInstance(null)");
    }

    // ==== appended deepening (2026-11): query / domains / count / lifecycle ====

    static void deepQueryLifecycle() throws Exception {
        final javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        final javax.management.ObjectName a = new javax.management.ObjectName("jck.q:type=A");
        final javax.management.ObjectName b = new javax.management.ObjectName("jck.q:type=B");
        server.registerMBean(new Gauge(), a);
        server.registerMBean(new Gauge(), b);
        java.util.Set<javax.management.ObjectName> names = server.queryNames(new javax.management.ObjectName("jck.q:*"), null);
        check(names.size() == 2 && names.contains(a) && names.contains(b), "queryNames domain pattern content");
        check(server.queryNames(a, null).size() == 1 && server.queryNames(a, null).contains(a), "queryNames exact name");
        check(server.queryNames(new javax.management.ObjectName("jck.none:*"), null).isEmpty(), "queryNames unknown domain empty");
        java.util.Set<javax.management.ObjectInstance> insts = server.queryMBeans(a, null);
        check(insts.size() == 1, "queryMBeans exact size");
        javax.management.ObjectInstance qi = insts.iterator().next();
        check(a.equals(qi.getObjectName()) && Gauge.class.getName().equals(qi.getClassName()), "queryMBeans ObjectInstance content");
        check(server.queryMBeans(new javax.management.ObjectName("jck.q:type=*"), null).size() == 2, "queryMBeans property pattern");
        boolean dom = false;
        boolean jmi = false;
        for (String d : server.getDomains()) {
            if ("jck.q".equals(d)) dom = true;
            if ("JMImplementation".equals(d)) jmi = true;
        }
        check(dom, "getDomains contains registered domain");
        check(jmi, "getDomains contains JMImplementation for the delegate");
        check(server.getMBeanCount().intValue() == 3, "getMBeanCount = delegate + 2");
        check("DefaultDomain".equals(server.getDefaultDomain()), "getDefaultDomain is DefaultDomain");
        check(server.getClassLoaderFor(a) == MBeanServerTest.class.getClassLoader(), "getClassLoaderFor returns the defining loader");
        server.unregisterMBean(b);
        check(!server.isRegistered(b), "unregisterMBean removes the MBean");
        checkThrows(javax.management.InstanceNotFoundException.class, new ThrowingRunnable() {
            public void run() throws Exception { server.getAttribute(b, "Count"); }
        }, "operations after unregister fail");
        check(server.getMBeanCount().intValue() == 2, "count drops back to delegate + 1");
    }

    // ==== appended deepening (2026-11): introspected metadata + createMBean ====

    static void deepMetadataCreateMBean() throws Exception {
        final javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        final javax.management.ObjectName on = new javax.management.ObjectName("jck.mi:type=Gauge");
        server.registerMBean(new Gauge(), on);
        javax.management.MBeanInfo info = server.getMBeanInfo(on);
        check(Gauge.class.getName().equals(info.getClassName()), "MBeanInfo className is the implementation class");
        javax.management.MBeanAttributeInfo[] attrs = info.getAttributes();
        check(attrs.length == 2, "two attributes introspected");
        java.util.Map<String, javax.management.MBeanAttributeInfo> byName =
                new java.util.HashMap<String, javax.management.MBeanAttributeInfo>();
        for (javax.management.MBeanAttributeInfo at : attrs) byName.put(at.getName(), at);
        javax.management.MBeanAttributeInfo count = byName.get("Count");
        check(count != null, "Count attribute present");
        check(count.isReadable() && count.isWritable() && !count.isIs(), "Count read-write, not is");
        check("int".equals(count.getType()), "Count type string is int");
        javax.management.MBeanAttributeInfo ready = byName.get("Ready");
        check(ready != null, "Ready attribute present");
        check(ready.isReadable() && !ready.isWritable() && ready.isIs(), "Ready read-only is");
        check("boolean".equals(ready.getType()), "Ready type string is boolean");
        java.util.Set<String> ops = new java.util.HashSet<String>();
        for (javax.management.MBeanOperationInfo o : info.getOperations()) ops.add(o.getName());
        check(ops.size() == 3 && ops.contains("reset") && ops.contains("add") && ops.contains("boom"),
                "operation name set is exactly reset/add/boom");
        check("true".equals(String.valueOf(info.getDescriptor().getFieldValue("immutableInfo"))), "standard MBean descriptor immutableInfo=true");
        check("false".equals(String.valueOf(info.getDescriptor().getFieldValue("mxbean"))), "standard MBean descriptor mxbean=false");
        // createMBean: reflective instantiation + registration in one step
        final javax.management.ObjectName made = new javax.management.ObjectName("jck.mi:type=Created");
        javax.management.ObjectInstance ci = server.createMBean(Gauge.class.getName(), made);
        check(made.equals(ci.getObjectName()), "createMBean ObjectInstance name");
        check(Gauge.class.getName().equals(ci.getClassName()), "createMBean ObjectInstance className");
        check(Integer.valueOf(3).equals(server.getAttribute(made, "Count")), "created MBean is live");
        check(server.isInstanceOf(made, GaugeMBean.class.getName()), "created MBean isInstanceOf its interface");
        server.unregisterMBean(made);
    }
}

