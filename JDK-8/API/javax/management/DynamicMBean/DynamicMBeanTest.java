/*
 * @test
 * @summary javax.management.DynamicMBean conformance: interface reflection.
 * @run main DynamicMBeanTest
 */

import javax.management.*;
import java.util.*;

public class DynamicMBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.DynamicMBean");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "getAttribute"), "declares getAttribute");
        check(hasMethod(c, "getAttributes"), "declares getAttributes");
        check(hasMethod(c, "getMBeanInfo"), "declares getMBeanInfo");
        testDynPassthrough();
        testDynCustomInfo();
        testDynNegatives();
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

    // ---- deepened (batch e): recording DynamicMBean, no introspection,
    //      full method passthrough via a real server, custom MBeanInfo ----

    public static class RecDyn implements DynamicMBean {
        public static final List<String> calls = new ArrayList<String>();
        static void rec(String s) { calls.add(s); }
        private int count = 5;
        public Object getAttribute(String attribute) throws AttributeNotFoundException {
            rec("getAttribute:" + attribute);
            if ("Count".equals(attribute)) return Integer.valueOf(count);
            throw new AttributeNotFoundException(attribute);
        }
        public void setAttribute(Attribute attribute) throws AttributeNotFoundException {
            rec("setAttribute:" + attribute.getName() + "=" + attribute.getValue());
            if (!"Count".equals(attribute.getName())) throw new AttributeNotFoundException(attribute.getName());
            count = ((Integer) attribute.getValue()).intValue();
        }
        public AttributeList getAttributes(String[] attributes) {
            rec("getAttributes:" + attributes.length);
            AttributeList l = new AttributeList();
            for (String n : attributes) if ("Count".equals(n)) l.add(new Attribute("Count", Integer.valueOf(count)));
            return l;
        }
        public AttributeList setAttributes(AttributeList attributes) {
            rec("setAttributes:" + attributes.size());
            for (Object o : attributes) {
                Attribute a = (Attribute) o;
                if ("Count".equals(a.getName())) count = ((Integer) a.getValue()).intValue();
            }
            return attributes;
        }
        public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException {
            rec("invoke:" + actionName);
            if ("add".equals(actionName)) {
                count += ((Integer) params[0]).intValue();
                return Integer.valueOf(count);
            }
            throw new MBeanException(new NoSuchMethodException(actionName));
        }
        public MBeanInfo getMBeanInfo() {
            rec("getMBeanInfo");
            return new MBeanInfo(RecDyn.class.getName(), "recording dynamic mbean",
                    new MBeanAttributeInfo[]{ new MBeanAttributeInfo("Count", "int", "counter", true, true, false) },
                    new MBeanConstructorInfo[0],
                    new MBeanOperationInfo[]{ new MBeanOperationInfo("add", "add x",
                            new MBeanParameterInfo[]{ new MBeanParameterInfo("x", "int", "amount") },
                            "int", MBeanOperationInfo.ACTION) },
                    new MBeanNotificationInfo[0]);
        }
    }

    static void testDynPassthrough() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName on = new ObjectName("jck.dyn", "k", "d");
        RecDyn dyn = new RecDyn();
        server.registerMBean(dyn, on);
        RecDyn.calls.clear();
        check(Integer.valueOf(5).equals(server.getAttribute(on, "Count")), "dyn getAttribute passthrough");
        server.setAttribute(on, new Attribute("Count", Integer.valueOf(9)));
        check(Integer.valueOf(9).equals(server.getAttribute(on, "Count")), "dyn setAttribute then read back");
        check(Integer.valueOf(12).equals(server.invoke(on, "add", new Object[]{Integer.valueOf(3)}, new String[]{"int"})),
                "dyn invoke passthrough");
        AttributeList many = server.getAttributes(on, new String[]{"Count", "Nope"});
        check(many.size() == 1 && "Count".equals(((Attribute) many.get(0)).getName()),
                "dyn getAttributes returns only known attributes");
        AttributeList toSet = new AttributeList();
        toSet.add(new Attribute("Count", Integer.valueOf(21)));
        AttributeList setBack = server.setAttributes(on, toSet);
        check(setBack.size() == 1, "dyn setAttributes list passthrough");
        check(Integer.valueOf(21).equals(server.getAttribute(on, "Count")), "dyn setAttributes took effect");
        List<String> c = new ArrayList<String>(RecDyn.calls);
        check(c.size() >= 4
                && "getAttribute:Count".equals(c.get(0))
                && "setAttribute:Count=9".equals(c.get(1))
                && "invoke:add".equals(c.get(3)), "dyn call sequence passthrough: " + c);
        check(c.contains("getAttributes:2"), "dyn getAttributes recorded");
        check(c.contains("setAttributes:1"), "dyn setAttributes recorded");
    }

    static void testDynCustomInfo() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName on = new ObjectName("jck.dyn", "k", "info");
        server.registerMBean(new RecDyn(), on);
        MBeanInfo info = server.getMBeanInfo(on);
        check(RecDyn.class.getName().equals(info.getClassName()), "dyn MBeanInfo className is the custom class");
        check("recording dynamic mbean".equals(info.getDescription()), "dyn MBeanInfo description is custom");
        check(info.getAttributes().length == 1 && "Count".equals(info.getAttributes()[0].getName()),
                "dyn custom attribute list");
        check(info.getOperations().length == 1 && "add".equals(info.getOperations()[0].getName()),
                "dyn custom operation list");
        check(info.getConstructors().length == 0, "dyn custom constructor list empty (no introspection)");
        check(server.isInstanceOf(on, RecDyn.class.getName()), "dyn isInstanceOf own class");
        check(!server.isInstanceOf(on, "javax.management.StandardMBean"), "dyn isInstanceOf not StandardMBean");
    }

    static void testDynNegatives() throws Exception {
        final MBeanServer server = MBeanServerFactory.newMBeanServer();
        final ObjectName on = new ObjectName("jck.dyn", "k", "neg");
        server.registerMBean(new RecDyn(), on);
        checkThrows(AttributeNotFoundException.class, new ThrowingRunnable() {
            public void run() throws Exception { server.getAttribute(on, "Missing"); }
        }, "dyn getAttribute unknown -> AttributeNotFoundException from bean");
        checkThrows(AttributeNotFoundException.class, new ThrowingRunnable() {
            public void run() throws Exception { server.setAttribute(on, new Attribute("Missing", Integer.valueOf(1))); }
        }, "dyn setAttribute unknown -> AttributeNotFoundException from bean");
        try {
            server.invoke(on, "nope", new Object[0], new String[0]);
            check(false, "dyn invoke unknown should throw MBeanException");
        } catch (MBeanException me) {
            check(me.getCause() instanceof NoSuchMethodException, "dyn invoke unknown wraps our NoSuchMethodException");
            passed++;
        }
    }
}
