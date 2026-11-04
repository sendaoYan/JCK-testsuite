/*
 * @test
 * @summary javax.print.URIException conformance: interface.
 * @run main URIExceptionTest
 */

import java.lang.reflect.*;


public class URIExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasIf(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    public static void main(String[] a) throws Exception {
        check(javax.print.URIException.URIInaccessible == 1, "URIInaccessible constant");
        check(javax.print.URIException.URISchemeNotSupported == 2, "URISchemeNotSupported constant");
        check(javax.print.URIException.URIOtherProblem == -1, "URIOtherProblem constant");
        Class<?> c = Class.forName("javax.print.URIException");
        check(c.isInterface(), "isInterface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "getUnsupportedURI"), "declares getUnsupportedURI");
        check(hasMethod(c, "getReason"), "declares getReason");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
