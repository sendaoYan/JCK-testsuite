/*
 * @test
 * @summary javax.management.JMRuntimeException conformance: white-box structure + black-box behavior.
 * @run main JMRuntimeExceptionTest
 */

import java.lang.reflect.*;
import javax.management.*;

public class JMRuntimeExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.JMRuntimeException");
        check(!c.isInterface(), "concrete class, not interface");
        check((c.getModifiers() & Modifier.PUBLIC) != 0, "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check("java.lang.RuntimeException".equals(c.getSuperclass().getName()), "superclass java.lang.RuntimeException");
        check(Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(java.io.Serializable.class.isAssignableFrom(c), "serializable");
        check(RuntimeException.class.isAssignableFrom(c), "runtime exception");

        java.lang.reflect.Constructor<?> ctor;
        ctor = c.getConstructor();
        check(ctor != null, "no-arg constructor");
        ctor = c.getConstructor(String.class);
        check(ctor != null, "String constructor");
    }
    static void testBlackBox() throws Exception {
        Exception e = new JMRuntimeException("fixed-message");
        check("fixed-message".equals(e.getMessage()), "message preserved");
        check(e.toString().contains("JMRuntimeException"), "toString contains class name");
        check(e instanceof RuntimeException, "instanceof RuntimeException");
        Exception e2 = new JMRuntimeException();
        check(e2.getMessage() == null, "no-arg message null");
        check(e2.toString().contains("JMRuntimeException"), "no-arg toString class name");
        Exception e3 = new JMRuntimeException((String) null);
        check(e3.getMessage() == null, "null String message null");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
