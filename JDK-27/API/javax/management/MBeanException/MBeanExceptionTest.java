/*
 * @test
 * @summary javax.management.MBeanException conformance: white-box structure + black-box behavior.
 * @run main MBeanExceptionTest
 */

import java.lang.reflect.*;
import javax.management.*;

public class MBeanExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.MBeanException");
        check(!c.isInterface(), "concrete class, not interface");
        check((c.getModifiers() & Modifier.PUBLIC) != 0, "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check("javax.management.JMException".equals(c.getSuperclass().getName()), "superclass javax.management.JMException");
        check(Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(java.io.Serializable.class.isAssignableFrom(c), "serializable");
        java.lang.reflect.Constructor<?> ctor;
        ctor = c.getConstructor(Exception.class);
        check(ctor != null, "Exception constructor");
        ctor = c.getConstructor(Exception.class, String.class);
        check(ctor != null, "Exception + String constructor");
        c.getMethod("getTargetException");
        c.getMethod("getCause");
    }
    static void testBlackBox() throws Exception {
        Exception cause = new Exception("cause-message");
        MBeanException e = new MBeanException(cause);
        check(e.getMessage() == null, "one-arg message null");
        check(e.getTargetException() == cause, "getTargetException round-trip");
        check(e.getCause() == cause, "getCause mirrors target");
        check(e.toString().contains("MBeanException"), "toString contains class name");

        MBeanException e2 = new MBeanException(cause, "wrapped-message");
        check("wrapped-message".equals(e2.getMessage()), "two-arg message preserved");
        check(e2.getTargetException() == cause, "two-arg getTargetException round-trip");
        check(e2.getCause() == cause, "two-arg getCause mirrors target");
        check(e2.toString().contains("MBeanException"), "two-arg toString class name");

        Exception cause2 = new Exception("second");
        MBeanException e3 = new MBeanException(cause2);
        check(e3.getCause() == cause2, "independent instance target");
        check(e3.getMessage() == null, "independent instance message null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
