/*
 * @test
 * @summary javax.management.BadBinaryOpValueExpException conformance: white-box structure + black-box behavior.
 * @run main BadBinaryOpValueExpExceptionTest
 */

import java.lang.reflect.*;
import javax.management.*;

public class BadBinaryOpValueExpExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }


    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.BadBinaryOpValueExpException");
        check(!c.isInterface(), "concrete class, not interface");
        check((c.getModifiers() & Modifier.PUBLIC) != 0, "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check("java.lang.Exception".equals(c.getSuperclass().getName()), "extends Exception");
        check(Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(java.io.Serializable.class.isAssignableFrom(c), "serializable");
        check(c.getConstructor(javax.management.ValueExp.class) != null, "ValueExp constructor");
        c.getMethod("getExp");
        c.getMethod("toString");
    }

    static void testBlackBox() throws Exception {
        javax.management.StringValueExp exp = new javax.management.StringValueExp("op-expr");
        BadBinaryOpValueExpException e = new BadBinaryOpValueExpException(exp);
        check(e.getExp() == exp, "getExp round-trip");
        check("BadBinaryOpValueExpException: 'op-expr'".equals(e.toString()), "toString embeds exp");
        check(e.getMessage() == null, "no detail message");
        check(e instanceof Exception, "instanceof Exception");

        javax.management.StringValueExp exp2 = new javax.management.StringValueExp("other");
        BadBinaryOpValueExpException e2 = new BadBinaryOpValueExpException(exp2);
        check(e2.getExp() == exp2, "independent getExp round-trip");
        check("BadBinaryOpValueExpException: 'other'".equals(e2.toString()), "independent toString");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testBBOExactGetter();
        testBBOHierarchyAndSerialization();
        testBBOMismatchIsCCE();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened (batch e): exact getter identity, null handling,
    //      serialization, serialVersionUID, mixed-type sibling behavior ----

    static void testBBOExactGetter() {
        ValueExp exp = Query.value(7);
        BadBinaryOpValueExpException x = new BadBinaryOpValueExpException(exp);
        check(x.getExp() == exp, "getExp returns the very same ValueExp instance");
        check("BadBinaryOpValueExpException: 7".equals(x.toString()), "toString includes exp");
        BadBinaryOpValueExpException n = new BadBinaryOpValueExpException(null);
        check(n.getExp() == null, "null exp allowed");
        check("BadBinaryOpValueExpException: null".equals(n.toString()), "null exp toString");
    }

    static void testBBOHierarchyAndSerialization() throws Exception {
        BadBinaryOpValueExpException x = new BadBinaryOpValueExpException(Query.value(1));
        check(x instanceof Exception && x instanceof java.io.Serializable, "Exception + Serializable");
        check(x.getCause() == null, "no cause");
        check(x.getMessage() == null, "no detail message");
        long uid = java.io.ObjectStreamClass.lookup(BadBinaryOpValueExpException.class).getSerialVersionUID();
        check(uid == 5068475589449021227L, "serialVersionUID locked: " + uid);
        BadBinaryOpValueExpException back = (BadBinaryOpValueExpException) roundTripBB(x);
        check(back.getExp() != null && "1".equals(back.getExp().toString()), "round-trip keeps exp value");
    }

    static void testBBOMismatchIsCCE() throws Exception {
        // sibling behavior recorded: mixed-type operands in arithmetic apply
        // surface as unchecked ClassCastException, not as this exception
        try {
            Query.plus(Query.value(1), Query.value("a")).apply(null);
            check(false, "expected ClassCastException for mixed-type plus");
        } catch (java.lang.ClassCastException e) {
            passed++;
        }
    }

    static Object roundTripBB(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        return ois.readObject();
    }
}
