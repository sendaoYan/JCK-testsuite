/*
 * @test
 * @summary javax.management.StringValueExp conformance: value access, toString
 *          quoting, apply() returning self, and setMBeanServer no-op.
 * @run main StringValueExpTest
 */

import javax.management.ObjectName;
import javax.management.StringValueExp;
import javax.management.*;

public class StringValueExpTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        StringValueExp e = new StringValueExp("hello");
        check("hello".equals(e.getValue()), "getValue");
        check("'hello'".equals(e.toString()), "toString quoted");
        check(e.toString() != null, "toString");
        check(new StringValueExp().getValue() == null, "no-arg null value");
        e.setMBeanServer(null);

        check(e.apply(new ObjectName("d:type=x")) == e, "apply returns self");
        check(new StringValueExp("it's").toString().equals("'it''s'"), "toString escapes quote");
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
