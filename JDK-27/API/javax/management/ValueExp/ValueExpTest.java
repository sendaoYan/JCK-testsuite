/*
 * @test
 * @summary javax.management.ValueExp conformance: white-box structure + light behavior.
 * @run main ValueExpTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

@SuppressWarnings("deprecation")
public class ValueExpTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.ValueExp");
        check(c.isInterface(), "is interface");
        check(java.io.Serializable.class.isAssignableFrom(c), "extends Serializable");
        check(hasMethod(c, "apply"), "has apply");
        check(hasMethod(c, "setMBeanServer"), "has setMBeanServer");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        final ValueExp[] applied = { null };
        ValueExp exp = new ValueExp() {
            public ValueExp apply(ObjectName name) { applied[0] = this; return this; }
            public void setMBeanServer(MBeanServer s) { }
        };
        ObjectName on = new ObjectName("d:k=v");
        ValueExp result = exp.apply(on);
        check(result == exp, "apply returns instance");
        check(applied[0] == exp, "apply invoked");

        exp.setMBeanServer(null);
        check(true, "setMBeanServer callable");

        // concrete ValueExp implementations are real ValueExps
        check(new StringValueExp("x") instanceof ValueExp, "StringValueExp is a ValueExp");
        check(new AttributeValueExp("a") instanceof ValueExp, "AttributeValueExp is a ValueExp");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
