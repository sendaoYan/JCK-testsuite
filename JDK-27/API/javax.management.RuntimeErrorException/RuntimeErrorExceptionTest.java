/*
 * @test
 * @summary javax.management.RuntimeErrorException conformance: white-box structure + black-box behavior.
 * @run main RuntimeErrorExceptionTest
 */

import java.lang.reflect.*;
import javax.management.*;

public class RuntimeErrorExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.RuntimeErrorException");
        check(!c.isInterface(), "concrete class, not interface");
        check((c.getModifiers() & Modifier.PUBLIC) != 0, "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check("javax.management.JMRuntimeException".equals(c.getSuperclass().getName()), "superclass javax.management.JMRuntimeException");
        check(Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(java.io.Serializable.class.isAssignableFrom(c), "serializable");
        java.lang.reflect.Constructor<?> ctor;
        ctor = c.getConstructor(Error.class);
        check(ctor != null, "Error constructor");
        ctor = c.getConstructor(Error.class, String.class);
        check(ctor != null, "Error + String constructor");
        c.getMethod("getTargetError");
        c.getMethod("getCause");
    }
    static void testBlackBox() throws Exception {
        Error cause = new Error("cause-message");
        RuntimeErrorException e = new RuntimeErrorException(cause);
        check(e.getMessage() == null, "one-arg message null");
        check(e.getTargetError() == cause, "getTargetError round-trip");
        check(e.getCause() == cause, "getCause mirrors target");
        check(e.toString().contains("RuntimeErrorException"), "toString contains class name");

        RuntimeErrorException e2 = new RuntimeErrorException(cause, "wrapped-message");
        check("wrapped-message".equals(e2.getMessage()), "two-arg message preserved");
        check(e2.getTargetError() == cause, "two-arg getTargetError round-trip");
        check(e2.getCause() == cause, "two-arg getCause mirrors target");
        check(e2.toString().contains("RuntimeErrorException"), "two-arg toString class name");

        Error cause2 = new Error("second");
        RuntimeErrorException e3 = new RuntimeErrorException(cause2);
        check(e3.getCause() == cause2, "independent instance target");
        check(e3.getMessage() == null, "independent instance message null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
