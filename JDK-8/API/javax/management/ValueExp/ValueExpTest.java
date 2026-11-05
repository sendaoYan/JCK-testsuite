/*
 * @test
 * @summary javax.management.ValueExp conformance: interface reflection.
 * @run main ValueExpTest
 */

import javax.management.*;

public class ValueExpTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.ValueExp");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("java.io.Serializable").isAssignableFrom(c), "implements/extends java.io.Serializable");
        check(hasMethod(c, "apply"), "declares apply");
        check(hasMethod(c, "setMBeanServer"), "declares setMBeanServer");
        testVExpContract();
        testVExpComposition();
        testVExpSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened (batch e): interface contract, custom ValueExp composed
    //      into standard operators, serialization round-trip ----

    static class ConstExp implements ValueExp {
        int applyCalls;
        MBeanServer seenServer;
        private final ValueExp constant;
        ConstExp(ValueExp constant) { this.constant = constant; }
        public ValueExp apply(ObjectName name) {
            applyCalls++;
            return constant;
        }
        public void setMBeanServer(MBeanServer s) { seenServer = s; }
    }

    static void testVExpContract() throws Exception {
        Class<?> c = Class.forName("javax.management.ValueExp");
        check(c.isInterface(), "vexp is an interface");
        check(java.io.Serializable.class.isAssignableFrom(c), "vexp extends Serializable");
        check(c.getMethod("apply", ObjectName.class).getReturnType() == ValueExp.class,
                "vexp apply(ObjectName) returns ValueExp");
        check(c.getMethod("setMBeanServer", MBeanServer.class).getReturnType() == void.class,
                "vexp setMBeanServer(MBeanServer) returns void");
    }

    static void testVExpComposition() throws Exception {
        ConstExp three = new ConstExp(Query.value(3));
        check(Query.eq(three, Query.value(3)).apply(null), "custom ValueExp works in eq");
        check(Query.gt(three, Query.value(2)).apply(null), "custom ValueExp works in gt");
        check(!Query.lt(three, Query.value(2)).apply(null), "custom ValueExp lt false");
        ValueExp sum = Query.plus(three, Query.value(2)).apply(null);
        check("5".equals(sum.toString()), "custom ValueExp in arithmetic plus");
        check(three.applyCalls == 4, "apply invoked for each evaluation: " + three.applyCalls);
        three.setMBeanServer(null);
        check(three.seenServer == null, "setMBeanServer callback observed on custom ValueExp");
    }

    static void testVExpSerialization() throws Exception {
        ValueExp v = roundTripV(Query.value(5));
        check("5".equals(v.toString()), "numeric ValueExp round-trip keeps value");
        ValueExp s = roundTripV(Query.value("q"));
        check(s instanceof StringValueExp && "q".equals(((StringValueExp) s).getValue()),
                "string ValueExp round-trip keeps value");
        ValueExp sum = roundTripV(Query.plus(Query.value(2), Query.value(3)));
        check("5".equals(sum.apply(null).toString()), "arithmetic ValueExp round-trip applies");
    }

    static ValueExp roundTripV(ValueExp o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        return (ValueExp) ois.readObject();
    }
}
