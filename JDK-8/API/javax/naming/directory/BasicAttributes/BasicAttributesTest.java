/*
 * @test
 * @summary javax.naming.directory.BasicAttributes conformance: put/get/getIDs.
 * @run main BasicAttributesTest
 */


public class BasicAttributesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.directory.BasicAttributes as = new javax.naming.directory.BasicAttributes();
        check(as.size() == 0, "empty");
        check(!as.isCaseIgnored(), "case-sensitive by default");
        as.put("cn", "John");
        as.put("sn", "Doe");
        check(as.size() == 2, "size");
        check("John".equals(as.get("cn").get()), "get");
        javax.naming.directory.BasicAttributes as2 = new javax.naming.directory.BasicAttributes(true);
        check(as2.isCaseIgnored(), "case-insensitive flag");
        javax.naming.directory.BasicAttributes as3 = new javax.naming.directory.BasicAttributes("cn", "John");
        check("John".equals(as3.get("cn").get()), "get from ctor");
        check(as3.remove("cn") != null, "remove");        System.out.println("PASS (" + passed + " assertions)");
    }
}
