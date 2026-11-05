/*
 * @test
 * @summary javax.management.BadStringOperationException conformance: white-box structure + black-box behavior.
 * @run main BadStringOperationExceptionTest
 */

import java.lang.reflect.*;
import javax.management.*;

public class BadStringOperationExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }


    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.BadStringOperationException");
        check(!c.isInterface(), "concrete class, not interface");
        check((c.getModifiers() & Modifier.PUBLIC) != 0, "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check("java.lang.Exception".equals(c.getSuperclass().getName()), "extends Exception");
        check(Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(java.io.Serializable.class.isAssignableFrom(c), "serializable");
        check(c.getConstructor(String.class) != null, "String constructor");
        c.getMethod("toString");
    }

    static void testBlackBox() throws Exception {
        BadStringOperationException e = new BadStringOperationException("plus");
        check("BadStringOperationException: plus".equals(e.toString()), "toString embeds op");
        check(e.getMessage() == null, "no detail message");
        check(e instanceof Exception, "instanceof Exception");

        BadStringOperationException e2 = new BadStringOperationException((String) null);
        check("BadStringOperationException: null".equals(e2.toString()), "null op toString");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testBSOContract();
        testBSORealTriggerAndSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened (batch e): op-derived toString, real trigger from string
    //      arithmetic, serialization, serialVersionUID ----

    static void testBSOContract() {
        BadStringOperationException x = new BadStringOperationException("minus");
        check("BadStringOperationException: minus".equals(x.toString()), "toString op-derived");
        check(x.getMessage() == null, "op is not the detail message");
        check(x instanceof Exception && x instanceof java.io.Serializable, "Exception + Serializable");
        check(x.getCause() == null, "no cause");
        check("BadStringOperationException: null".equals(new BadStringOperationException(null).toString()),
                "null op toString");
    }

    static void testBSORealTriggerAndSerialization() throws Exception {
        try {
            javax.management.Query.minus(javax.management.Query.value("a"), javax.management.Query.value("b")).apply(null);
            check(false, "expected BadStringOperationException from minus on strings");
        } catch (BadStringOperationException e) {
            passed++;
        }
        try {
            javax.management.Query.div(javax.management.Query.value("a"), javax.management.Query.value("b")).apply(null);
            check(false, "expected BadStringOperationException from div on strings");
        } catch (BadStringOperationException e) {
            passed++;
        }
        long uid = java.io.ObjectStreamClass.lookup(BadStringOperationException.class).getSerialVersionUID();
        check(uid == 7802201238441662100L, "serialVersionUID locked: " + uid);
        BadStringOperationException back =
                (BadStringOperationException) roundTripBS(new BadStringOperationException("minus"));
        check("BadStringOperationException: minus".equals(back.toString()), "round-trip keeps op");
    }

    static Object roundTripBS(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        return ois.readObject();
    }
}
