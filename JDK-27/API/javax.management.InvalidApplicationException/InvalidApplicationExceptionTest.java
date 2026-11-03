/*
 * @test
 * @summary javax.management.InvalidApplicationException conformance: white-box structure + black-box behavior.
 * @run main InvalidApplicationExceptionTest
 */

import java.lang.reflect.*;
import javax.management.*;

public class InvalidApplicationExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }


    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.InvalidApplicationException");
        check(!c.isInterface(), "concrete class, not interface");
        check((c.getModifiers() & Modifier.PUBLIC) != 0, "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check("java.lang.Exception".equals(c.getSuperclass().getName()), "extends Exception");
        check(Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(java.io.Serializable.class.isAssignableFrom(c), "serializable");
        check(c.getConstructor(Object.class) != null, "Object constructor");
    }

    static void testBlackBox() throws Exception {
        Object val = new Object();
        InvalidApplicationException e = new InvalidApplicationException(val);
        check(e.getMessage() == null, "no detail message");
        check("javax.management.InvalidApplicationException".equals(e.toString()), "toString is bare class name");
        check(e instanceof Exception, "instanceof Exception");

        InvalidApplicationException e2 = new InvalidApplicationException(null);
        check(e2.getMessage() == null, "null value still no message");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
