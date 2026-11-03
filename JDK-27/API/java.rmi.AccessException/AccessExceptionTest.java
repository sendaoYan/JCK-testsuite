/*
 * @test
 * @summary java.rmi.AccessException conformance: white-box structure + black-box behavior.
 * @run main AccessExceptionTest
 */

import java.rmi.AccessException;

public class AccessExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.rmi.AccessException");
        check(c.getSuperclass() == java.rmi.RemoteException.class, "extends RemoteException");
        check(!c.isInterface() && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !java.lang.reflect.Modifier.isFinal(c.getModifiers()), "concrete non-final");
        check(Throwable.class.isAssignableFrom(c), "is Throwable");
        java.util.TreeSet<String> params = new java.util.TreeSet<>();
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            StringBuilder b = new StringBuilder();
            for (Class<?> p : k.getParameterTypes()) b.append(p.getSimpleName()).append(',');
            params.add(b.toString());
        }
        check(params.equals(java.util.Set.of("String,", "String,Exception,")), "ctor parameter types");

    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        AccessException e1 = new AccessException("detail-msg");
        check("detail-msg".equals(e1.getMessage()), "message, no cause");
        check(e1.getCause() == null, "cause null");
        check(e1.detail == null, "detail null");

        Exception inner = new Exception("inner");
        AccessException e2 = new AccessException("outer", inner);
        check(e2.getCause() == inner, "cause identity");
        check(e2.detail == inner, "detail field identity");
        check("inner".equals(e2.getCause().getMessage()), "cause message");
        check(e2.getMessage().startsWith("outer"), "outer message prefix");
        check(e2.getMessage().contains("; nested exception is:"), "nested marker in message");

        AccessException e3 = new AccessException("only", null);
        check("only".equals(e3.getMessage()), "message with null cause");
        check(e3.getCause() == null, "cause null (null cause)");
        check(e3.detail == null, "detail null (null cause)");

        AccessException e4 = new AccessException("msg");
        AccessException e5 = new AccessException("msg");
        check(!e4.equals(e5), "exceptions are not value-equal");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
