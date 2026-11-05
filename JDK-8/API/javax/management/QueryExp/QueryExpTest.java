/*
 * @test
 * @summary javax.management.QueryExp conformance: interface reflection.
 * @run main QueryExpTest
 */

import javax.management.*;
import java.util.*;

public class QueryExpTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.QueryExp");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("java.io.Serializable").isAssignableFrom(c), "implements/extends java.io.Serializable");
        check(hasMethod(c, "apply"), "declares apply");
        check(hasMethod(c, "setMBeanServer"), "declares setMBeanServer");
        testExpContract();
        testExpThroughServer();
        testExpSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened (batch e): interface contract, custom QueryExp through a
    //      real server query, boolean composition, serialization round-trip ----

    public static interface ExpCounterMBean { int getCount(); }

    public static class ExpCounter implements ExpCounterMBean {
        public int getCount() { return 1; }
    }

    static class RecordingExp implements QueryExp {
        int applyCalls;
        MBeanServer seenServer;
        private final String matchKey;
        RecordingExp(String matchKey) { this.matchKey = matchKey; }
        public boolean apply(ObjectName name) {
            applyCalls++;
            return matchKey.equals(name.getKeyProperty("name"));
        }
        public void setMBeanServer(MBeanServer s) { seenServer = s; }
    }

    static class ThrowingExp implements QueryExp {
        public boolean apply(ObjectName name) { throw new ArithmeticException("boom"); }
        public void setMBeanServer(MBeanServer s) { }
    }

    static void testExpContract() throws Exception {
        Class<?> c = Class.forName("javax.management.QueryExp");
        check(c.isInterface(), "exp is an interface");
        check(java.io.Serializable.class.isAssignableFrom(c), "exp extends Serializable");
        check(c.getMethod("apply", ObjectName.class).getReturnType() == boolean.class,
                "exp apply(ObjectName) returns boolean");
        check(c.getMethod("setMBeanServer", MBeanServer.class).getReturnType() == void.class,
                "exp setMBeanServer(MBeanServer) returns void");
    }

    static void testExpThroughServer() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName hit = new ObjectName("jck.exp", "name", "hit");
        ObjectName miss = new ObjectName("jck.exp", "name", "miss");
        server.registerMBean(new ExpCounter(), hit);
        server.registerMBean(new ExpCounter(), miss);
        RecordingExp exp = new RecordingExp("hit");
        Set<ObjectName> res = server.queryNames(new ObjectName("jck.exp:*"), exp);
        check(res.equals(new HashSet<ObjectName>(Arrays.asList(hit))), "custom QueryExp filters server-side");
        check(exp.applyCalls == 2, "apply called once per candidate MBean: " + exp.applyCalls);
        check(exp.seenServer == null, "server does not push itself via the deprecated setMBeanServer");
        check(server.queryNames(new ObjectName("jck.exp:*"), Query.and(exp, exp))
                .equals(new HashSet<ObjectName>(Arrays.asList(hit))), "and with custom exp");
        check(server.queryNames(new ObjectName("jck.exp:*"), Query.not(exp))
                .equals(new HashSet<ObjectName>(Arrays.asList(miss))), "not with custom exp");
        check(server.queryNames(new ObjectName("jck.exp:*"), Query.or(exp, new RecordingExp("miss"))).size() == 2,
                "or with custom exp");
        check(server.queryNames(new ObjectName("jck.exp:*"), new ThrowingExp()).isEmpty(),
                "throwing exp yields empty result without propagation");
    }

    static void testExpSerialization() throws Exception {
        QueryExp eq = Query.eq(Query.value(1), Query.value(1));
        QueryExp roundTripped = (QueryExp) roundTripObj(eq);
        check(roundTripped.apply(null), "standard QueryExp survives serialization round-trip");
        QueryExp gt = Query.gt(Query.value(2), Query.value(1));
        check(((QueryExp) roundTripObj(gt)).apply(null), "gt exp round-trip still applies");
    }

    static Object roundTripObj(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        return ois.readObject();
    }
}
