/*
 * @test
 * @summary javax.naming.event.NamingExceptionEvent conformance: exception/context.
 * @run main NamingExceptionEventTest
 */


public class NamingExceptionEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static Object proxyFor(Class<?> itf) {
        return java.lang.reflect.Proxy.newProxyInstance(
            itf.getClassLoader(), new Class<?>[]{itf},
            new java.lang.reflect.InvocationHandler() {
                public Object invoke(Object p, java.lang.reflect.Method m, Object[] a) { return null; }
            });
    }
    public static void main(String[] args) throws Exception {
        javax.naming.event.EventContext src = (javax.naming.event.EventContext) proxyFor(javax.naming.event.EventContext.class);
        javax.naming.NamingException ex = new javax.naming.NamingException("boom");
        javax.naming.event.NamingExceptionEvent e = new javax.naming.event.NamingExceptionEvent(src, ex);
        check(e.getException() == ex, "getException");
        check(e.getEventContext() == src, "getEventContext");        System.out.println("PASS (" + passed + " assertions)");
    }
}
