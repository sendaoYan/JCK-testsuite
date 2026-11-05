/*
 * @test
 * @summary javax.management.AttributeValueExp conformance: white-box structure + black-box behavior.
 * @run main AttributeValueExpTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.lang.management.*;
import javax.management.*;

public class AttributeValueExpTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.AttributeValueExp");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(ValueExp.class.isAssignableFrom(c), "implements ValueExp");
        check(hasMethod(c, "getAttributeName"), "has getAttributeName");
        check(hasMethod(c, "apply"), "has apply");
        check(hasMethod(c, "setMBeanServer"), "has setMBeanServer");
        check(hasMethod(c, "toString"), "has toString");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        AttributeValueExp exp = new AttributeValueExp("VmName");
        check("VmName".equals(exp.getAttributeName()), "getAttributeName");
        check("VmName".equals(exp.toString()), "toString returns attribute name");

        // apply() resolves the attribute through the QueryEval thread-local MBeanServer.
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        QueryEval qe = new QueryEval() { };
        qe.setMBeanServer(mbs);
        ValueExp result = exp.apply(new ObjectName("java.lang:type=Runtime"));
        check(result instanceof StringValueExp, "apply returns StringValueExp for String attribute");
        String v = ((StringValueExp) result).getValue();
        check(v != null && v.length() > 0, "VmName attribute non-empty: " + v);

        // A non-existent attribute makes getAttribute return null, so apply() throws
        // BadAttributeValueExpException (the null result is not Number/String/Boolean).
        boolean threwMissing = false;
        try {
            new AttributeValueExp("NoSuchAttribute999").apply(new ObjectName("java.lang:type=Runtime"));
        } catch (BadAttributeValueExpException ex) { threwMissing = true; }
        check(threwMissing, "missing attribute throws BadAttributeValueExpException");

        // numeric attribute wraps into a NumericValueExp
        ValueExp num = new AttributeValueExp("ThreadCount").apply(new ObjectName("java.lang:type=Threading"));
        check(num != null, "numeric attribute applies without error");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testAVEBasicContract();
        testAVEApply();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened (batch e): apply against a real server via QueryEval
    //      context, value-type mapping, failure modes, serialization ----

    public static interface ACounterMBean { int getCount(); boolean isReady(); }

    public static class ACounter implements ACounterMBean {
        public int getCount() { return 5; }
        public boolean isReady() { return true; }
    }

    public static interface ANamedMBean { String getName(); }

    public static class ANamed implements ANamedMBean {
        public String getName() { return "alpha"; }
    }

    public static interface AObjMBean { Object getBlob(); }

    public static class AObj implements AObjMBean {
        public Object getBlob() { return new Object(); }
    }

    static void expectBAVEE(String attr, ObjectName name, String msg) {
        try {
            new AttributeValueExp(attr).apply(name);
            check(false, msg + ": expected BadAttributeValueExpException");
        } catch (BadAttributeValueExpException e) {
            passed++;
        } catch (Exception e) {
            throw new AssertionError(msg + ": expected BadAttributeValueExpException but got "
                    + e.getClass().getName(), e);
        }
    }

    static void testAVEBasicContract() throws Exception {
        AttributeValueExp e = new AttributeValueExp("Count");
        check("Count".equals(e.getAttributeName()), "attribute name");
        check("Count".equals(e.toString()), "toString is the bare attribute name");
        check(new AttributeValueExp().getAttributeName() == null, "no-arg constructor yields null name");
        check(Query.attr("Rate") instanceof AttributeValueExp
                && "Rate".equals(Query.attr("Rate").getAttributeName()), "Query.attr(String) form");
        check("Count".equals(Query.attr(ACounter.class.getName(), "Count").getAttributeName()),
                "Query.attr(class,name) keeps plain attribute name");
        long uid = java.io.ObjectStreamClass.lookup(AttributeValueExp.class).getSerialVersionUID();
        check(uid == -7768025046539163385L, "serialVersionUID locked: " + uid);
        AttributeValueExp back = (AttributeValueExp) roundTripA(e);
        check("Count".equals(back.getAttributeName()), "serialization round-trip keeps attribute name");
    }

    static void testAVEApply() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName cn = new ObjectName("jck.ave", "k", "c");
        ObjectName sn = new ObjectName("jck.ave", "k", "s");
        ObjectName on = new ObjectName("jck.ave", "k", "o");
        server.registerMBean(new ACounter(), cn);
        server.registerMBean(new ANamed(), sn);
        server.registerMBean(new AObj(), on);
        // AttributeValueExp.setMBeanServer is a deprecated no-op: without a
        // QueryEval-provided context the attribute cannot be resolved
        AttributeValueExp numeric = new AttributeValueExp("Count");
        numeric.setMBeanServer(server);
        expectBAVEE("Count", cn, "deprecated setMBeanServer has no effect");
        new QueryEval() { }.setMBeanServer(server);
        try {
            ValueExp num = new AttributeValueExp("Count").apply(cn);
            check("5".equals(num.toString()), "numeric attribute maps to numeric ValueExp: " + num);
            ValueExp bool = new AttributeValueExp("Ready").apply(cn);
            check("true".equals(bool.toString()), "boolean attribute maps to boolean ValueExp: " + bool);
            ValueExp str = new AttributeValueExp("Name").apply(sn);
            check(str instanceof StringValueExp && "alpha".equals(((StringValueExp) str).getValue()),
                    "string attribute maps to StringValueExp");
            expectBAVEE("Blob", on, "non-Number/String/Boolean attribute value");
            expectBAVEE("Missing", cn, "unknown attribute");
            expectBAVEE("Count", new ObjectName("jck.ave", "k", "unregistered"), "unregistered ObjectName");
        } finally {
            new QueryEval() { }.setMBeanServer(null);
        }
        check(QueryEval.getMBeanServer() == null, "query context cleaned up");
        expectBAVEE("Count", cn, "apply fails again after context cleanup");
    }

    static Object roundTripA(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        return ois.readObject();
    }
}
