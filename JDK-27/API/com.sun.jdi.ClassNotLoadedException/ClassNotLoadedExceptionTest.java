/*
 * @test
 * @summary com.sun.jdi.ClassNotLoadedException conformance: hierarchy, constructors and behavior.
 * @run main ClassNotLoadedExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;

public class ClassNotLoadedExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.ClassNotLoadedException");
        check(c.getSuperclass().getName().equals("java.lang.Exception"), "extends Exception");
        check(Throwable.class.isAssignableFrom(c), "is Throwable");
        Object e1 = c.getConstructor(String.class).newInstance("Foo");
        check("Foo".equals(c.getMethod("className").invoke(e1)), "className()");
        Object e2 = c.getConstructor(String.class, String.class).newInstance("Foo", "msg");
        check("Foo".equals(c.getMethod("className").invoke(e2)), "className() 2-arg");
        check("msg".equals(((Throwable) e2).getMessage()), "message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
