/*
 * @test
 * @summary javax.naming.directory.BasicAttribute conformance: id/add/get/ordered.
 * @run main BasicAttributeTest
 */


public class BasicAttributeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.directory.BasicAttribute a = new javax.naming.directory.BasicAttribute("cn");
        check("cn".equals(a.getID()), "getID");
        check(a.size() == 0, "empty");
        a.add("John"); a.add("Doe");
        check(a.size() == 2, "size after add");
        check("John".equals(a.get()), "get first");
        check(a.contains("Doe"), "contains");
        check(!a.isOrdered(), "unordered by default");
        a.remove("John");
        check(a.size() == 1 && !a.contains("John"), "remove");
        javax.naming.directory.BasicAttribute b = new javax.naming.directory.BasicAttribute("cn", "John", true);
        check(b.isOrdered(), "ordered flag");
        check("John".equals(b.get()), "get value");
        javax.naming.directory.BasicAttribute c2 = (javax.naming.directory.BasicAttribute) b.clone();
        check(b.equals(c2), "clone equals");        System.out.println("PASS (" + passed + " assertions)");
    }
}
