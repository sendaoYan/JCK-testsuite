/*
 * @test
 * @summary javax.management.InstanceAlreadyExistsException conformance: white-box structure + black-box behavior.
 * @run main InstanceAlreadyExistsExceptionTest
 */

import java.lang.reflect.*;
import javax.management.*;

public class InstanceAlreadyExistsExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.InstanceAlreadyExistsException");
        check(!c.isInterface(), "concrete class, not interface");
        check((c.getModifiers() & Modifier.PUBLIC) != 0, "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check("javax.management.OperationsException".equals(c.getSuperclass().getName()), "superclass javax.management.OperationsException");
        check(Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(java.io.Serializable.class.isAssignableFrom(c), "serializable");
        check(!RuntimeException.class.isAssignableFrom(c), "checked exception");

        java.lang.reflect.Constructor<?> ctor;
        ctor = c.getConstructor();
        check(ctor != null, "no-arg constructor");
        ctor = c.getConstructor(String.class);
        check(ctor != null, "String constructor");
    }
    static void testBlackBox() throws Exception {
        Exception e = new InstanceAlreadyExistsException("fixed-message");
        check("fixed-message".equals(e.getMessage()), "message preserved");
        check(e.toString().contains("InstanceAlreadyExistsException"), "toString contains class name");
        check(e instanceof javax.management.OperationsException, "instanceof OperationsException");
        check(e instanceof javax.management.JMException, "instanceof JMException");
        Exception e2 = new InstanceAlreadyExistsException();
        check(e2.getMessage() == null, "no-arg message null");
        check(e2.toString().contains("InstanceAlreadyExistsException"), "no-arg toString class name");
        Exception e3 = new InstanceAlreadyExistsException((String) null);
        check(e3.getMessage() == null, "null String message null");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
