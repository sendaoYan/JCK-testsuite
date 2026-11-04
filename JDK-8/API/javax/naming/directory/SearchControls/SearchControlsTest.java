/*
 * @test
 * @summary javax.naming.directory.SearchControls conformance: scope/limits.
 * @run main SearchControlsTest
 */


public class SearchControlsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.directory.SearchControls sc = new javax.naming.directory.SearchControls();
        check(sc.getSearchScope() == javax.naming.directory.SearchControls.ONELEVEL_SCOPE, "default scope ONELEVEL");
        sc.setSearchScope(javax.naming.directory.SearchControls.SUBTREE_SCOPE);
        check(sc.getSearchScope() == javax.naming.directory.SearchControls.SUBTREE_SCOPE, "setSearchScope");
        sc.setCountLimit(10); sc.setTimeLimit(5000);
        check(sc.getCountLimit() == 10, "getCountLimit");
        check(sc.getTimeLimit() == 5000, "getTimeLimit");
        sc.setReturningObjFlag(true);
        check(sc.getReturningObjFlag(), "getReturningObjFlag");
        sc.setReturningAttributes(new String[]{"cn", "sn"});
        check(sc.getReturningAttributes().length == 2, "getReturningAttributes");
        check(javax.naming.directory.SearchControls.OBJECT_SCOPE == 0
              && javax.naming.directory.SearchControls.SUBTREE_SCOPE == 2, "scope constants");        System.out.println("PASS (" + passed + " assertions)");
    }
}
