/*
 * @test
 * @summary javax.naming.spi.NamingManager conformance: getObjectInstance/getInitialContext.
 * @run main NamingManagerTest
 */


public class NamingManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.Reference ref = new javax.naming.Reference("java.lang.String");
        Object o = javax.naming.spi.NamingManager.getObjectInstance(ref, null, null, null);
        check(o != null, "getObjectInstance returns ref info");
        try {
            javax.naming.spi.NamingManager.getInitialContext(new java.util.Hashtable<Object, Object>());
            throw new AssertionError("expected NoInitialContextException");
        } catch (javax.naming.NoInitialContextException expected) {
            check(true, "no factory -> NoInitialContextException");
        }        System.out.println("PASS (" + passed + " assertions)");
    }
}
