/*
 * @test
 * @summary com.sun.jdi.InvocationException conformance: hierarchy, constructors and behavior.
 * @run main InvocationExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;

public class InvocationExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.InvocationException");
        check(c.getSuperclass().getName().equals("java.lang.Exception"), "extends Exception");
        Object e = c.getConstructor(Class.forName("com.sun.jdi.ObjectReference")).newInstance(new Object[]{null});
        check(c.getMethod("exception").invoke(e) == null, "exception() null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
