/*
 * @test
 * @summary javax.naming.CompositeName conformance: add/get/size/toString.
 * @run main CompositeNameTest
 */


public class CompositeNameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.CompositeName n = new javax.naming.CompositeName("a/b/c");
        check(n.size() == 3, "size");
        check("a".equals(n.get(0)), "get(0)");
        check("c".equals(n.get(2)), "get(2)");
        check("a/b/c".equals(n.toString()), "toString");
        check(n.isEmpty() == false, "not empty");
        n.add("d");
        check(n.size() == 4 && "d".equals(n.get(3)), "add");
        javax.naming.CompositeName n2 = new javax.naming.CompositeName();
        check(n2.isEmpty(), "empty");
        javax.naming.CompositeName clone = (javax.naming.CompositeName) n.clone();
        check(n.equals(clone), "clone equals");        System.out.println("PASS (" + passed + " assertions)");
    }
}
