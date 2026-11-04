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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
