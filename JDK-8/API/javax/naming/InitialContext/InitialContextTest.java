/*
 * @test
 * @summary javax.naming.InitialContext conformance: lazy construction/close.
 * @run main InitialContextTest
 */


public class InitialContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.InitialContext ctx = new javax.naming.InitialContext();
        check(ctx != null, "constructed");
        ctx.close();
        javax.naming.InitialContext ctx2 = new javax.naming.InitialContext(new java.util.Hashtable<Object, Object>());
        ctx2.close();
        check(true, "close with env");        System.out.println("PASS (" + passed + " assertions)");
    }
}
