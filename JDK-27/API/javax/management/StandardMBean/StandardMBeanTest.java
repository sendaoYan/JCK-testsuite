/*
 * @test
 * @summary javax.management.StandardMBean conformance: white-box structure + black-box behavior.
 * @run main StandardMBeanTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.lang.management.*;
import javax.management.*;

public class StandardMBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    public interface ThingMBean {
        int getCount();
        void setCount(int c);
        int reset(int base);
    }
    public static class Thing implements ThingMBean {
        private int count = 7;
        public int getCount() { return count; }
        public void setCount(int c) { count = c; }
        public int reset(int base) { count = base; return count; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.StandardMBean");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(DynamicMBean.class.isAssignableFrom(c), "implements DynamicMBean");
        check(MBeanRegistration.class.isAssignableFrom(c), "implements MBeanRegistration");
        check(hasMethod(c, "getAttribute"), "has getAttribute");
        check(hasMethod(c, "setAttribute"), "has setAttribute");
        check(hasMethod(c, "invoke"), "has invoke");
        check(hasMethod(c, "getMBeanInfo"), "has getMBeanInfo");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        StandardMBean smb = new StandardMBean(new Thing(), ThingMBean.class);
        check(smb.getMBeanInterface() == ThingMBean.class, "getMBeanInterface");
        check(smb.getImplementation() instanceof Thing, "getImplementation");
        check(smb.getImplementationClass() == Thing.class, "getImplementationClass");

        // MBeanInfo introspection
        MBeanInfo info = smb.getMBeanInfo();
        check(Thing.class.getName().equals(info.getClassName()), "getMBeanInfo className");
        check(info.getAttributes().length == 1, "one attribute");
        check("Count".equals(info.getAttributes()[0].getName()), "attribute name");
        check(info.getOperations().length == 1, "one operation");
        check("reset".equals(info.getOperations()[0].getName()), "operation name");

        // attribute get/set via DynamicMBean
        check(((Integer) smb.getAttribute("Count")).intValue() == 7, "getAttribute initial");
        smb.setAttribute(new Attribute("Count", 42));
        check(((Integer) smb.getAttribute("Count")).intValue() == 42, "setAttribute/getAttribute round-trip");

        // getAttributes / setAttributes batch
        AttributeList got = smb.getAttributes(new String[]{"Count"});
        check(got.size() == 1 && got.get(0).equals(new Attribute("Count", 42)), "getAttributes");
        AttributeList set = new AttributeList();
        set.add(new Attribute("Count", 55));
        AttributeList setResult = smb.setAttributes(set);
        check(setResult.size() == 1 && smb.getAttribute("Count").equals(55), "setAttributes");

        // invoke
        Object r = smb.invoke("reset", new Object[]{10}, new String[]{"int"});
        check(((Integer) r).intValue() == 10, "invoke returns value");
        check(smb.getAttribute("Count").equals(10), "invoke mutated state");

        // missing attribute throws AttributeNotFoundException
        boolean threw = false;
        try { smb.getAttribute("Missing"); } catch (AttributeNotFoundException ex) { threw = true; }
        check(threw, "missing attribute throws AttributeNotFoundException");

        // register/unregister against platform MBeanServer
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName on = new ObjectName("jck.test:type=StandardMBeanTest");
        ObjectInstance oi = mbs.registerMBean(smb, on);
        check(oi.getObjectName().equals(on), "registerMBean returns ObjectInstance");
        check(mbs.isRegistered(on), "isRegistered after register");
        check(mbs.getAttribute(on, "Count").equals(10), "MBeanServer getAttribute");
        mbs.setAttribute(on, new Attribute("Count", 99));
        check(mbs.getAttribute(on, "Count").equals(99), "MBeanServer setAttribute round-trip");
        check(((Integer) mbs.invoke(on, "reset", new Object[]{1}, new String[]{"int"})).intValue() == 1,
                "MBeanServer invoke");
        check(mbs.isInstanceOf(on, Thing.class.getName()), "isInstanceOf");
        mbs.unregisterMBean(on);
        check(!mbs.isRegistered(on), "unregistered");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepCtorForms();
        testDeepCtorNegatives();
        testDeepInfoIntrospection();
        testDeepSetImplementation();
        testDeepServerRouting();
        testDeepRegisterNonCompliant();
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

    // ---- deepened (batch e): separated/self constructor forms, introspection
    // matrix, MBeanInfo cache semantics, setImplementation, server routing,
    // non-compliant registration negatives ----

    public static interface StdCounterMBean {
        int getCount(); void setCount(int v); void reset(); int add(int x); boolean isReady();
    }

    public static class StdCounter implements StdCounterMBean {
        private int count;
        public int getCount() { return count; }
        public void setCount(int v) { count = v; }
        public void reset() { count = 0; }
        public int add(int x) { count += x; return count; }
        public boolean isReady() { return true; }
    }

    public static class StdSelfCounter extends StandardMBean implements StdCounterMBean {
        private int count = 77;
        public StdSelfCounter() throws NotCompliantMBeanException { super(StdCounterMBean.class); }
        public int getCount() { return count; }
        public void setCount(int v) { count = v; }
        public void reset() { count = 0; }
        public int add(int x) { count += x; return count; }
        public boolean isReady() { return false; }
    }

    public static class StdPlain { }

    static void testDeepCtorForms() throws Exception {
        StdCounter impl = new StdCounter();
        impl.setCount(3);
        StandardMBean pair = new StandardMBean(impl, StdCounterMBean.class);
        check(pair.getImplementation() == impl, "deep pair getImplementation");
        check(pair.getMBeanInterface() == StdCounterMBean.class, "deep pair getMBeanInterface");
        check(pair.getImplementationClass() == StdCounter.class, "deep pair getImplementationClass");
        StdSelfCounter self = new StdSelfCounter();
        check(self.getImplementation() == self, "deep self implementation is this");
        check(self.getMBeanInterface() == StdCounterMBean.class, "deep self getMBeanInterface");
        check(self.getImplementationClass() == StdSelfCounter.class, "deep self getImplementationClass");
        check(((Integer) self.getAttribute("Count")).intValue() == 77, "deep self getAttribute");
        // (impl, null) is tolerated: the interface is inferred from the implementation class
        StandardMBean inferred = new StandardMBean(new StdCounter(), null);
        check(inferred.getMBeanInterface() == StdCounterMBean.class, "deep null iface inferred from impl");
    }

    static void testDeepCtorNegatives() {
        checkThrows(NotCompliantMBeanException.class, new ThrowingRunnable() {
            public void run() throws Exception { new StandardMBean(new StdCounter(), StdCounter.class); }
        }, "non-interface management interface class");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() throws Exception { new StandardMBean(null, null); }
        }, "null implementation and null interface");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() throws Exception { new StandardMBean(null, StdCounterMBean.class); }
        }, "null implementation with valid interface");
    }

    static void testDeepInfoIntrospection() throws Exception {
        StandardMBean mbean = new StandardMBean(new StdCounter(), StdCounterMBean.class);
        MBeanInfo info = mbean.getMBeanInfo();
        check(mbean.getMBeanInfo() == info, "deep getMBeanInfo cached: same instance twice");
        check(StdCounter.class.getName().equals(info.getClassName()), "deep info className is impl class");
        MBeanAttributeInfo[] attrs = info.getAttributes();
        check(attrs.length == 2, "deep attribute count {Count,Ready}");
        Set<String> attrNames = new HashSet<String>();
        boolean countIsIs = false, countWritable = false, readyIsIs = false, readyWritable = false;
        String countType = null, readyType = null;
        for (MBeanAttributeInfo a : attrs) {
            attrNames.add(a.getName());
            if ("Count".equals(a.getName())) {
                countIsIs = a.isIs(); countWritable = a.isWritable(); countType = a.getType();
            } else if ("Ready".equals(a.getName())) {
                readyIsIs = a.isIs(); readyWritable = a.isWritable(); readyType = a.getType();
            }
        }
        check(attrNames.equals(new HashSet<String>(Arrays.asList("Count", "Ready"))), "deep attribute name set");
        check(!countIsIs && countWritable && "int".equals(countType), "deep Count shape rw non-is int");
        check(readyIsIs && !readyWritable && "boolean".equals(readyType), "deep Ready shape isIs readonly boolean");
        Set<String> opNames = new HashSet<String>();
        for (MBeanOperationInfo o : info.getOperations()) opNames.add(o.getName());
        check(opNames.equals(new HashSet<String>(Arrays.asList("reset", "add"))), "deep operation name set");
        for (MBeanOperationInfo o : info.getOperations()) {
            if ("add".equals(o.getName())) {
                check("int".equals(o.getReturnType()), "deep add return type");
                check(o.getSignature().length == 1 && "int".equals(o.getSignature()[0].getType()), "deep add signature");
                check(o.getImpact() == MBeanOperationInfo.UNKNOWN, "deep add impact UNKNOWN");
            }
        }
        check(info.getConstructors().length == 0, "deep pair form exposes no constructors");
        check(new StdSelfCounter().getMBeanInfo().getConstructors().length == 1,
                "deep self form exposes its own constructor");
    }

    static void testDeepSetImplementation() throws Exception {
        StdCounter first = new StdCounter();
        StandardMBean mbean = new StandardMBean(first, StdCounterMBean.class);
        MBeanInfo cached = mbean.getMBeanInfo();
        StdCounter second = new StdCounter();
        second.setCount(42);
        mbean.setImplementation(second);
        check(mbean.getImplementation() == second, "deep setImplementation swaps implementation");
        check(mbean.getImplementationClass() == StdCounter.class, "deep implementation class after swap");
        check(((Integer) mbean.getAttribute("Count")).intValue() == 42, "deep getAttribute routes to new impl");
        check(mbean.getMBeanInfo() == cached, "deep getMBeanInfo stays cached after setImplementation");
        final StandardMBean target = mbean;
        checkThrows(NotCompliantMBeanException.class, new ThrowingRunnable() {
            public void run() throws Exception { target.setImplementation(new StdPlain()); }
        }, "setImplementation with non-compliant object");
        check(mbean.getImplementation() == second, "deep impl unchanged after failed setImplementation");
    }

    static void testDeepServerRouting() throws Exception {
        final MBeanServer server = MBeanServerFactory.newMBeanServer();
        StdCounter impl = new StdCounter();
        impl.setCount(5);
        final ObjectName on = new ObjectName("jck.std", "type", "Counter");
        ObjectInstance oi = server.registerMBean(new StandardMBean(impl, StdCounterMBean.class), on);
        check(oi.getObjectName().equals(on), "deep register returns ObjectInstance with name");
        check(oi.getClassName().equals(StdCounter.class.getName()), "deep ObjectInstance className");
        check(((Integer) server.getAttribute(on, "Count")).intValue() == 5, "deep server getAttribute Count");
        check(Boolean.TRUE.equals(server.getAttribute(on, "Ready")), "deep server getAttribute isIs Ready");
        server.setAttribute(on, new Attribute("Count", Integer.valueOf(9)));
        check(impl.getCount() == 9, "deep server setAttribute routes to impl");
        check(((Integer) server.invoke(on, "add", new Object[]{Integer.valueOf(3)}, new String[]{"int"})).intValue() == 12,
                "deep server invoke add");
        server.invoke(on, "reset", new Object[0], new String[0]);
        check(impl.getCount() == 0, "deep server invoke reset");
        check(server.isInstanceOf(on, StdCounter.class.getName()), "deep isInstanceOf implementation class");
        check(server.isInstanceOf(on, "javax.management.StandardMBean"),
                "deep pair form is a StandardMBean (DynamicMBean) to the server");
        checkThrows(AttributeNotFoundException.class, new ThrowingRunnable() {
            public void run() throws Exception { server.getAttribute(on, "Nope"); }
        }, "server getAttribute unknown attribute");
        checkThrows(ReflectionException.class, new ThrowingRunnable() {
            public void run() throws Exception { server.invoke(on, "nope", new Object[0], new String[0]); }
        }, "server invoke unknown operation");
        checkThrows(InvalidAttributeValueException.class, new ThrowingRunnable() {
            public void run() throws Exception { server.setAttribute(on, new Attribute("Count", "str")); }
        }, "server setAttribute wrong value type");
    }

    static void testDeepRegisterNonCompliant() throws Exception {
        final MBeanServer server = MBeanServerFactory.newMBeanServer();
        final ObjectName plainName = new ObjectName("jck.std", "type", "Plain");
        checkThrows(NotCompliantMBeanException.class, new ThrowingRunnable() {
            public void run() throws Exception { server.registerMBean(new StdPlain(), plainName); }
        }, "register plain class not following Standard/MXBean conventions");
        check(!server.isRegistered(plainName), "deep plain class not registered");
    }
}
