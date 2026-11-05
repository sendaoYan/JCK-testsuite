/*
 * @test
 * @summary javax.management.StringValueExp conformance: white-box structure + black-box behavior.
 * @run main StringValueExpTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class StringValueExpTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.StringValueExp");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(ValueExp.class.isAssignableFrom(c), "implements ValueExp");
        check(hasMethod(c, "getValue"), "has getValue");
        check(hasMethod(c, "apply"), "has apply");
        check(hasMethod(c, "toString"), "has toString");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        StringValueExp s = new StringValueExp("hello");
        check("hello".equals(s.getValue()), "getValue");
        check("'hello'".equals(s.toString()), "toString single-quoted");

        StringValueExp empty = new StringValueExp("");
        check("".equals(empty.getValue()), "empty value");
        check("''".equals(empty.toString()), "empty toString");

        StringValueExp def = new StringValueExp();
        check(def.getValue() == null, "default constructor null value");

        // single quote escaped by doubling in toString
        StringValueExp quote = new StringValueExp("a'b");
        check("'a''b'".equals(quote.toString()), "single quote escaped");

        // apply() returns itself (identity), regardless of ObjectName
        ValueExp applied = s.apply(new ObjectName("d:k=v"));
        check(applied == s, "apply returns this");
        check(((StringValueExp) applied).getValue().equals("hello"), "apply preserves value");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSVEContract();
        testSVESerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened (batch e): apply identity, no-op setMBeanServer, default
    //      equals semantics, serialization, locked serialVersionUID ----

    static void testSVEContract() throws Exception {
        StringValueExp e = new StringValueExp("v");
        e.setMBeanServer(null); // deprecated no-op: must not affect apply
        check(e.apply(new ObjectName("d", "k", "v")) == e, "setMBeanServer is a no-op: apply still returns self");
        check(!new StringValueExp("a").equals(new StringValueExp("a")),
                "no value equality: Object identity semantics");
        check(e.equals(e), "identity equals holds");
        check(e.hashCode() == e.hashCode(), "hashCode stable across calls");
        check(e instanceof ValueExp && e instanceof java.io.Serializable, "ValueExp + Serializable");
        check("s".equals(Query.value("s").getValue()), "Query.value(String) yields StringValueExp with value");
        StringValueExp empty = new StringValueExp();
        check(empty.apply(null) == empty, "no-arg instance also returns self from apply");
    }

    static void testSVESerialization() throws Exception {
        long uid = java.io.ObjectStreamClass.lookup(StringValueExp.class).getSerialVersionUID();
        check(uid == -3256390509806284044L, "serialVersionUID locked: " + uid);
        StringValueExp e = new StringValueExp("round");
        StringValueExp back = (StringValueExp) roundTripS(e);
        check("round".equals(back.getValue()), "serialization round-trip keeps value");
        check("'round'".equals(back.toString()), "round-trip toString");
        StringValueExp nullVal = (StringValueExp) roundTripS(new StringValueExp());
        check(nullVal.getValue() == null, "null value round-trip");
    }

    static Object roundTripS(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        return ois.readObject();
    }
}
