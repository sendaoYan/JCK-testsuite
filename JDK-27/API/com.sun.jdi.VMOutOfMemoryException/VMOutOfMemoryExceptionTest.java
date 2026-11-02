/*
 * @test
 * @summary com.sun.jdi.VMOutOfMemoryException conformance: exception hierarchy and constructors.
 * @run main VMOutOfMemoryExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;

public class VMOutOfMemoryExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.VMOutOfMemoryException");
        check(c.getSuperclass().getName().equals("java.lang.RuntimeException"), "extends java.lang.RuntimeException");
        check(Throwable.class.isAssignableFrom(c), "is Throwable");
        Throwable t0 = (Throwable) c.getConstructor().newInstance();
        check(t0.getMessage() == null, "no-arg message null");
        Throwable t1 = (Throwable) c.getConstructor(String.class).newInstance("msg");
        check("msg".equals(t1.getMessage()), "string ctor message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
