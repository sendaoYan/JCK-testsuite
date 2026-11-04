/*
 * @test
 * @summary java.rmi.RemoteException conformance: white-box structure + black-box behavior.
 * @run main RemoteExceptionTest
 */

import java.rmi.RemoteException;

public class RemoteExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.rmi.RemoteException");
        check(c.getSuperclass() == java.io.IOException.class, "extends IOException");
        check(!c.isInterface() && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !java.lang.reflect.Modifier.isFinal(c.getModifiers()), "concrete non-final");
        check(Throwable.class.isAssignableFrom(c), "is Throwable");
        java.util.TreeSet<String> params = new java.util.TreeSet<>();
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            StringBuilder b = new StringBuilder();
            for (Class<?> p : k.getParameterTypes()) b.append(p.getSimpleName()).append(',');
            params.add(b.toString());
        }
        check(params.equals(java.util.Set.of("", "String,", "String,Throwable,")), "ctor parameter types");
        java.lang.reflect.Field detail = c.getField("detail");
        check(detail.getType() == Throwable.class, "public detail field is Throwable");

    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        RemoteException e0 = new RemoteException();
        check(e0.getMessage() == null, "default message null");
        check(e0.getCause() == null, "default cause null");
        check(e0.detail == null, "default detail null");

        RemoteException e1 = new RemoteException("msg");
        check("msg".equals(e1.getMessage()), "string message");
        check(e1.getCause() == null, "string cause null");
        check(e1.detail == null, "string detail null");

        Exception inner = new Exception("inner");
        RemoteException e2 = new RemoteException("outer", inner);
        check(e2.getCause() == inner, "cause identity");
        check(e2.detail == inner, "detail identity");
        check(e2.getMessage().startsWith("outer"), "outer message prefix");
        check(e2.getMessage().contains("; nested exception is:"), "nested marker");
        check(e2.getMessage().contains(inner.toString()), "message includes cause toString");

        RemoteException e3 = new RemoteException("only", null);
        check("only".equals(e3.getMessage()), "message with null cause");
        check(e3.getCause() == null, "null cause");
        check(e3.detail == null, "null detail");

        try {
            e0.initCause(new Exception("x"));
            check(false, "initCause must throw");
        } catch (IllegalStateException expected) {
            check(true, "initCause throws IllegalStateException");
        }

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
