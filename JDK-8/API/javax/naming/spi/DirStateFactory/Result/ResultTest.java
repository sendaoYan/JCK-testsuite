/*
 * @test
 * @summary javax.naming.spi.DirStateFactory.Result conformance: object/attributes.
 * @run main ResultTest
 */


public class ResultTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.directory.BasicAttributes attrs = new javax.naming.directory.BasicAttributes("cn", "John");
        javax.naming.spi.DirStateFactory.Result r = new javax.naming.spi.DirStateFactory.Result("obj", attrs);
        check("obj".equals(r.getObject()), "getObject");
        check(r.getAttributes() == attrs, "getAttributes");        System.out.println("PASS (" + passed + " assertions)");
    }
}
