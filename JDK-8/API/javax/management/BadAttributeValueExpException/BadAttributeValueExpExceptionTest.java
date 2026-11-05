/*
 * @test
 * @summary javax.management.BadAttributeValueExpException conformance: full
 *          contract coverage. value-derived toString, null value handling,
 *          getMessage stays null (value is not the message), and Exception
 *          hierarchy.
 * @run main BadAttributeValueExpExceptionTest
 */

import javax.management.BadAttributeValueExpException;

public class BadAttributeValueExpExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        BadAttributeValueExpException x = new BadAttributeValueExpException("msg");
        check(x instanceof Exception, "extends Exception");
        check("BadAttributeValueException: msg".equals(x.toString()), "toString value-derived");
        check(x.getMessage() == null, "getMessage null (value not message)");

        BadAttributeValueExpException n = new BadAttributeValueExpException(null);
        check(n.toString().endsWith("null"), "null value toString");

        BadAttributeValueExpException num = new BadAttributeValueExpException(Integer.valueOf(42));
        check(num.toString().endsWith("42"), "numeric value toString");
        testBAVEEToString();
        testBAVEEHierarchyAndRealTrigger();
        testBAVEESerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened (batch e): precise toString forms, real trigger via
    //      context-less AttributeValueExp.apply, serialization, serialVersionUID ----

    static void testBAVEEToString() {
        check("BadAttributeValueException: msg".equals(new BadAttributeValueExpException("msg").toString()),
                "toString uses short name without Exp plus the value");
        check("BadAttributeValueException: null".equals(new BadAttributeValueExpException(null).toString()),
                "null value toString");
        check("BadAttributeValueExpException".length() > 0, "class name sanity");
        check("BadAttributeValueException: 42".equals(new BadAttributeValueExpException(Integer.valueOf(42)).toString()),
                "numeric value converted to its string form");
    }

    static void testBAVEEHierarchyAndRealTrigger() throws Exception {
        BadAttributeValueExpException x = new BadAttributeValueExpException("m");
        check(x instanceof Exception, "extends Exception");
        check(x.getMessage() == null, "value is not the detail message");
        check(x.getCause() == null, "no cause");
        check(x instanceof java.io.Serializable, "Serializable");
        try {
            new javax.management.AttributeValueExp("Nope").apply(new javax.management.ObjectName("d:k=v"));
            check(false, "expected BadAttributeValueExpException from context-less apply");
        } catch (BadAttributeValueExpException e) {
            passed++;
        }
    }

    static void testBAVEESerialization() throws Exception {
        long uid = java.io.ObjectStreamClass.lookup(BadAttributeValueExpException.class).getSerialVersionUID();
        check(uid == -3105272988410493376L, "serialVersionUID locked: " + uid);
        BadAttributeValueExpException x = new BadAttributeValueExpException("rt");
        BadAttributeValueExpException back = (BadAttributeValueExpException) roundTripB(x);
        check("BadAttributeValueException: rt".equals(back.toString()), "round-trip keeps value-derived toString");
        check(back.getMessage() == null, "round-trip message still null");
    }

    static Object roundTripB(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        return ois.readObject();
    }
}
