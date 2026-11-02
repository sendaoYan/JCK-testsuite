/*
 * @test
 * @summary com.sun.jdi.connect.IllegalConnectorArgumentsException conformance: hierarchy, constructors and behavior.
 * @run main IllegalConnectorArgumentsExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;

public class IllegalConnectorArgumentsExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.connect.IllegalConnectorArgumentsException");
        check(c.getSuperclass().getName().equals("java.lang.Exception"), "extends Exception");
        Object e1 = c.getConstructor(String.class, String.class).newInstance("msg", "aname");
        List<?> n1 = (List<?>) c.getMethod("argumentNames").invoke(e1);
        check(n1 != null && n1.size() == 1 && "aname".equals(n1.get(0)), "argumentNames single");
        Object e2 = c.getConstructor(String.class, List.class).newInstance("msg", List.of("a", "b"));
        List<?> n2 = (List<?>) c.getMethod("argumentNames").invoke(e2);
        check(n2 != null && n2.size() == 2, "argumentNames list size");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
