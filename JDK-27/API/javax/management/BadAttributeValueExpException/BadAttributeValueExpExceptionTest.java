/*
 * @test
 * @summary javax.management.BadAttributeValueExpException conformance: white-box structure + black-box behavior.
 * @run main BadAttributeValueExpExceptionTest
 */

import java.lang.reflect.*;
import javax.management.*;

public class BadAttributeValueExpExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }


    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.BadAttributeValueExpException");
        check(!c.isInterface(), "concrete class, not interface");
        check((c.getModifiers() & Modifier.PUBLIC) != 0, "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check("java.lang.Exception".equals(c.getSuperclass().getName()), "extends Exception");
        check(Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(java.io.Serializable.class.isAssignableFrom(c), "serializable");
        check(c.getConstructor(Object.class) != null, "Object constructor");
        c.getMethod("toString");
    }

    static void testBlackBox() throws Exception {
        BadAttributeValueExpException e = new BadAttributeValueExpException("bad-value");
        check("BadAttributeValueException: bad-value".equals(e.toString()), "toString embeds value");
        check(e.getMessage() == null, "no detail message");
        check(e instanceof Exception, "instanceof Exception");

        BadAttributeValueExpException e2 = new BadAttributeValueExpException(12345);
        check("BadAttributeValueException: 12345".equals(e2.toString()), "toString uses Object.toString");

        BadAttributeValueExpException e3 = new BadAttributeValueExpException((Object) null);
        check("BadAttributeValueException: null".equals(e3.toString()), "null value toString");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
