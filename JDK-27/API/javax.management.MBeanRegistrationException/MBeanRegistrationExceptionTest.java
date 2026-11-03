/*
 * @test
 * @summary javax.management.MBeanRegistrationException conformance: white-box structure + black-box behavior.
 * @run main MBeanRegistrationExceptionTest
 */

import java.lang.reflect.*;
import javax.management.*;

public class MBeanRegistrationExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.MBeanRegistrationException");
        check(!c.isInterface(), "concrete class, not interface");
        check((c.getModifiers() & Modifier.PUBLIC) != 0, "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check("javax.management.MBeanException".equals(c.getSuperclass().getName()), "superclass javax.management.MBeanException");
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
        MBeanRegistrationException e = new MBeanRegistrationException(cause);
        check(e.getMessage() == null, "one-arg message null");
        check(e.getTargetException() == cause, "getTargetException round-trip");
        check(e.getCause() == cause, "getCause mirrors target");
        check(e.toString().contains("MBeanRegistrationException"), "toString contains class name");

        MBeanRegistrationException e2 = new MBeanRegistrationException(cause, "wrapped-message");
        check("wrapped-message".equals(e2.getMessage()), "two-arg message preserved");
        check(e2.getTargetException() == cause, "two-arg getTargetException round-trip");
        check(e2.getCause() == cause, "two-arg getCause mirrors target");
        check(e2.toString().contains("MBeanRegistrationException"), "two-arg toString class name");

        Exception cause2 = new Exception("second");
        MBeanRegistrationException e3 = new MBeanRegistrationException(cause2);
        check(e3.getCause() == cause2, "independent instance target");
        check(e3.getMessage() == null, "independent instance message null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
