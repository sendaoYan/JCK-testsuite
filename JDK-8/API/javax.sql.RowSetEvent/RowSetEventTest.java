/*
 * @test
 * @summary javax.sql.RowSetEvent conformance: source.
 * @run main RowSetEventTest
 */


public class RowSetEventTest {
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
        javax.sql.RowSet rs = (javax.sql.RowSet) proxyFor(javax.sql.RowSet.class);
        javax.sql.RowSetEvent e = new javax.sql.RowSetEvent(rs);
        check(e.getSource() == rs, "getSource");
        check(e instanceof java.util.EventObject, "is EventObject");        System.out.println("PASS (" + passed + " assertions)");
    }
}
