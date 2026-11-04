/*
 * @test
 * @summary javax.naming.directory.SearchResult conformance: name/object/attributes.
 * @run main SearchResultTest
 */


public class SearchResultTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.directory.BasicAttributes attrs = new javax.naming.directory.BasicAttributes("cn", "John");
        javax.naming.directory.SearchResult sr = new javax.naming.directory.SearchResult("cn=John", "value", attrs);
        check("cn=John".equals(sr.getName()), "getName");
        check("value".equals(sr.getObject()), "getObject");
        check(sr.getAttributes() == attrs, "getAttributes");
        javax.naming.directory.BasicAttributes attrs2 = new javax.naming.directory.BasicAttributes("sn", "Doe");
        sr.setAttributes(attrs2);
        check(sr.getAttributes() == attrs2, "setAttributes");
        check(sr instanceof javax.naming.Binding, "is Binding");        System.out.println("PASS (" + passed + " assertions)");
    }
}
