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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
