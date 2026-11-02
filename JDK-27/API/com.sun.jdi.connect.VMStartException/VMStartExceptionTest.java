/*
 * @test
 * @summary com.sun.jdi.connect.VMStartException conformance: hierarchy, constructors and behavior.
 * @run main VMStartExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;

public class VMStartExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.connect.VMStartException");
        check(c.getSuperclass().getName().equals("java.lang.Exception"), "extends Exception");
        Object e1 = c.getConstructor(Process.class).newInstance(new Object[]{null});
        check(c.getMethod("process").invoke(e1) == null, "process() null");
        Object e2 = c.getConstructor(String.class, Process.class).newInstance("m", null);
        check("m".equals(((Throwable) e2).getMessage()), "message");
        check(c.getMethod("process").invoke(e2) == null, "process() null 2-arg");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
