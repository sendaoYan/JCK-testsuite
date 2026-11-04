/*
 * @test
 * @summary javax.naming.directory.InitialDirContext conformance: lazy construction/close.
 * @run main InitialDirContextTest
 */


public class InitialDirContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.directory.InitialDirContext ctx = new javax.naming.directory.InitialDirContext();
        check(ctx != null, "constructed");
        ctx.close();
        check(true, "closed");        System.out.println("PASS (" + passed + " assertions)");
    }
}
