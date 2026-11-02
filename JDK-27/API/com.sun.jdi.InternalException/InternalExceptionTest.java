/*
 * @test
 * @summary com.sun.jdi.InternalException conformance: hierarchy, constructors and behavior.
 * @run main InternalExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;

public class InternalExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.InternalException");
        check(c.getSuperclass().getName().equals("java.lang.RuntimeException"), "extends RuntimeException");
        Throwable t0 = (Throwable) c.getConstructor().newInstance();
        Throwable t1 = (Throwable) c.getConstructor(String.class).newInstance("msg");
        check("msg".equals(t1.getMessage()), "string ctor message");
        Object e3 = c.getConstructor(int.class).newInstance(42);
        check(((Integer) c.getMethod("errorCode").invoke(e3)).intValue() == 42, "errorCode int ctor");
        Object e4 = c.getConstructor(String.class, int.class).newInstance("m", 43);
        check(((Integer) c.getMethod("errorCode").invoke(e4)).intValue() == 43, "errorCode str,int ctor");
        check("m".equals(((Throwable) e4).getMessage()), "message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
