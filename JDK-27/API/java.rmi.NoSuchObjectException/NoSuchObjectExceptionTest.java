/*
 * @test
 * @summary java.rmi.NoSuchObjectException conformance: white-box structure + black-box behavior.
 * @run main NoSuchObjectExceptionTest
 */

import java.rmi.NoSuchObjectException;

public class NoSuchObjectExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.rmi.NoSuchObjectException");
        check(c.getSuperclass() == java.rmi.RemoteException.class, "extends RemoteException");
        check(!c.isInterface() && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !java.lang.reflect.Modifier.isFinal(c.getModifiers()), "concrete non-final");
        check(Throwable.class.isAssignableFrom(c), "is Throwable");
        java.util.TreeSet<String> params = new java.util.TreeSet<>();
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            StringBuilder b = new StringBuilder();
            for (Class<?> p : k.getParameterTypes()) b.append(p.getSimpleName()).append(',');
            params.add(b.toString());
        }
        check(params.equals(java.util.Set.of("String,")), "ctor parameter types");

    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        NoSuchObjectException e = new NoSuchObjectException("detail-msg");
        check("detail-msg".equals(e.getMessage()), "message");
        check(e.getCause() == null, "cause null");
        check(e.detail == null, "detail null");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
