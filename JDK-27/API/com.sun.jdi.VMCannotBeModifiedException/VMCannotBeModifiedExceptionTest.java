/*
 * @test
 * @summary com.sun.jdi.VMCannotBeModifiedException conformance: exception hierarchy and constructors.
 * @run main VMCannotBeModifiedExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;

public class VMCannotBeModifiedExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.VMCannotBeModifiedException");
        check(c.getSuperclass().getName().equals("java.lang.UnsupportedOperationException"), "extends java.lang.UnsupportedOperationException");
        check(Throwable.class.isAssignableFrom(c), "is Throwable");
        Throwable t0 = (Throwable) c.getConstructor().newInstance();
        check(t0.getMessage() == null, "no-arg message null");
        Throwable t1 = (Throwable) c.getConstructor(String.class).newInstance("msg");
        check("msg".equals(t1.getMessage()), "string ctor message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
