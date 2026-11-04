/*
 * @test
 * @summary javax.naming.spi.ResolveResult conformance: resolved obj/remaining name.
 * @run main ResolveResultTest
 */


public class ResolveResultTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.spi.ResolveResult r = new javax.naming.spi.ResolveResult("obj", "a/b");
        check("obj".equals(r.getResolvedObj()), "getResolvedObj");
        check(r.getRemainingName() != null && r.getRemainingName().size() == 2, "getRemainingName");
        r.setResolvedObj("other");
        check("other".equals(r.getResolvedObj()), "setResolvedObj");
        r.appendRemainingComponent("c");
        check(r.getRemainingName().size() == 3, "appendRemainingComponent");        System.out.println("PASS (" + passed + " assertions)");
    }
}
