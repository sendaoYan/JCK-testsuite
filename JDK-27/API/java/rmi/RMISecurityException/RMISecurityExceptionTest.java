/*
 * @test
 * @summary java.rmi.RMISecurityException conformance: white-box structure + black-box behavior.
 * @run main RMISecurityExceptionTest
 */

import java.rmi.RMISecurityException;

public class RMISecurityExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.rmi.RMISecurityException");
        check(c.getSuperclass() == java.lang.SecurityException.class, "extends SecurityException");
        check(!c.isInterface() && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !java.lang.reflect.Modifier.isFinal(c.getModifiers()), "concrete non-final");
        check(RuntimeException.class.isAssignableFrom(c), "is RuntimeException");
        check(c.getAnnotation(Deprecated.class) != null, "deprecated");
        java.util.TreeSet<String> params = new java.util.TreeSet<>();
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            StringBuilder b = new StringBuilder();
            for (Class<?> p : k.getParameterTypes()) b.append(p.getSimpleName()).append(',');
            params.add(b.toString());
        }
        check(params.equals(java.util.Set.of("String,", "String,String,")), "ctor parameter types");

    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        RMISecurityException e1 = new RMISecurityException("detail-msg");
        check("detail-msg".equals(e1.getMessage()), "message");
        check(e1.getCause() == null, "cause null");

        RMISecurityException e2 = new RMISecurityException("detail-msg", "ignored-arg");
        check("detail-msg".equals(e2.getMessage()), "two-arg ignores second arg");
        check(e2.getCause() == null, "two-arg cause null");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
