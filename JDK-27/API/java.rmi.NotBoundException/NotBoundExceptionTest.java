/*
 * @test
 * @summary java.rmi.NotBoundException conformance: white-box structure + black-box behavior.
 * @run main NotBoundExceptionTest
 */

import java.rmi.NotBoundException;

public class NotBoundExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.rmi.NotBoundException");
        check(c.getSuperclass() == java.lang.Exception.class, "extends Exception");
        check(!java.rmi.RemoteException.class.isAssignableFrom(c), "not a RemoteException");
        check(!c.isInterface() && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !java.lang.reflect.Modifier.isFinal(c.getModifiers()), "concrete non-final");
        check(Throwable.class.isAssignableFrom(c), "is Throwable");
        java.util.TreeSet<String> params = new java.util.TreeSet<>();
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            StringBuilder b = new StringBuilder();
            for (Class<?> p : k.getParameterTypes()) b.append(p.getSimpleName()).append(',');
            params.add(b.toString());
        }
        check(params.equals(java.util.Set.of("", "String,")), "ctor parameter types");

    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        NotBoundException e0 = new NotBoundException();
        check(e0.getMessage() == null, "default ctor message null");
        check(e0.getCause() == null, "default cause null");

        NotBoundException e1 = new NotBoundException("detail-msg");
        check("detail-msg".equals(e1.getMessage()), "message");
        check(e1.getCause() == null, "cause null");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
