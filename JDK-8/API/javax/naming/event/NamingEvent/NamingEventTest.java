/*
 * @test
 * @summary javax.naming.event.NamingEvent conformance: type/bindings/constants.
 * @run main NamingEventTest
 */


public class NamingEventTest {
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
        javax.naming.Binding nb = new javax.naming.Binding("cn=New", "v1");
        javax.naming.Binding ob = new javax.naming.Binding("cn=Old", "v0");
        javax.naming.event.NamingEvent e = new javax.naming.event.NamingEvent(src, javax.naming.event.NamingEvent.OBJECT_ADDED, nb, ob, "change");
        check(e.getType() == javax.naming.event.NamingEvent.OBJECT_ADDED, "getType");
        check(e.getNewBinding() == nb, "getNewBinding");
        check(e.getOldBinding() == ob, "getOldBinding");
        check("change".equals(e.getChangeInfo()), "getChangeInfo");
        check(e.getEventContext() == src, "getEventContext");
        check(javax.naming.event.NamingEvent.OBJECT_CHANGED == 3, "OBJECT_CHANGED");        System.out.println("PASS (" + passed + " assertions)");
    }
}
