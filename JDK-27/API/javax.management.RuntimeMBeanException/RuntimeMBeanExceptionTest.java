/*
 * @test
 * @summary javax.management.RuntimeMBeanException conformance: white-box structure + black-box behavior.
 * @run main RuntimeMBeanExceptionTest
 */

import java.lang.reflect.*;
import javax.management.*;

public class RuntimeMBeanExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.RuntimeMBeanException");
        check(!c.isInterface(), "concrete class, not interface");
        check((c.getModifiers() & Modifier.PUBLIC) != 0, "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check("javax.management.JMRuntimeException".equals(c.getSuperclass().getName()), "superclass javax.management.JMRuntimeException");
        check(Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(java.io.Serializable.class.isAssignableFrom(c), "serializable");
        java.lang.reflect.Constructor<?> ctor;
        ctor = c.getConstructor(RuntimeException.class);
        check(ctor != null, "RuntimeException constructor");
        ctor = c.getConstructor(RuntimeException.class, String.class);
        check(ctor != null, "RuntimeException + String constructor");
        c.getMethod("getTargetException");
        c.getMethod("getCause");
    }
    static void testBlackBox() throws Exception {
        RuntimeException cause = new RuntimeException("cause-message");
        RuntimeMBeanException e = new RuntimeMBeanException(cause);
        check(e.getMessage() == null, "one-arg message null");
        check(e.getTargetException() == cause, "getTargetException round-trip");
        check(e.getCause() == cause, "getCause mirrors target");
        check(e.toString().contains("RuntimeMBeanException"), "toString contains class name");

        RuntimeMBeanException e2 = new RuntimeMBeanException(cause, "wrapped-message");
        check("wrapped-message".equals(e2.getMessage()), "two-arg message preserved");
        check(e2.getTargetException() == cause, "two-arg getTargetException round-trip");
        check(e2.getCause() == cause, "two-arg getCause mirrors target");
        check(e2.toString().contains("RuntimeMBeanException"), "two-arg toString class name");

        RuntimeException cause2 = new RuntimeException("second");
        RuntimeMBeanException e3 = new RuntimeMBeanException(cause2);
        check(e3.getCause() == cause2, "independent instance target");
        check(e3.getMessage() == null, "independent instance message null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
