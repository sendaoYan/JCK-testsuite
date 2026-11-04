/*
 * @test
 * @summary java.rmi.ServerError conformance: white-box structure + black-box behavior.
 * @run main ServerErrorTest
 */

import java.rmi.ServerError;

public class ServerErrorTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.rmi.ServerError");
        check(c.getSuperclass() == java.rmi.RemoteException.class, "extends RemoteException");
        check(!c.isInterface() && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !java.lang.reflect.Modifier.isFinal(c.getModifiers()), "concrete non-final");
        check(Throwable.class.isAssignableFrom(c), "is Throwable");
        java.util.TreeSet<String> params = new java.util.TreeSet<>();
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            StringBuilder b = new StringBuilder();
            for (Class<?> p : k.getParameterTypes()) b.append(p.getSimpleName()).append(',');
            params.add(b.toString());
        }
        check(params.equals(java.util.Set.of("String,Error,")), "ctor parameter types");

    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Error inner = new Error("inner-error");
        ServerError e = new ServerError("outer", inner);
        check(e.getCause() == inner, "cause identity");
        check(e.detail == inner, "detail identity");
        check(e.getCause() instanceof Error, "cause is Error");
        check("inner-error".equals(e.getCause().getMessage()), "cause message");
        check(e.getMessage().startsWith("outer"), "outer message prefix");
        check(e.getMessage().contains("; nested exception is:"), "nested marker");

        ServerError e2 = new ServerError("only", null);
        check("only".equals(e2.getMessage()), "message with null cause");
        check(e2.getCause() == null, "cause null");
        check(e2.detail == null, "detail null");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
