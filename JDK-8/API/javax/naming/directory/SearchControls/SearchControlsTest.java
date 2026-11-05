/*
 * @test
 * @summary javax.naming.directory.SearchControls conformance: scope/limits.
 * @run main SearchControlsTest
 */



import javax.naming.directory.SearchControls;
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
              && javax.naming.directory.SearchControls.SUBTREE_SCOPE == 2, "scope constants");        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dScConstants();
        dScDefaults();
        dScFullCtor();
        dScSetters();
        dScSerialization();
        System.out.println("deep: SearchControls contracts OK");
    }

    static void dScConstants() {
        check(SearchControls.OBJECT_SCOPE == 0, "OBJECT_SCOPE == 0");
        check(SearchControls.ONELEVEL_SCOPE == 1, "ONELEVEL_SCOPE == 1");
        check(SearchControls.SUBTREE_SCOPE == 2, "SUBTREE_SCOPE == 2");
    }

    static void dScDefaults() {
        SearchControls sc = new SearchControls();
        check(sc.getSearchScope() == SearchControls.ONELEVEL_SCOPE, "default scope ONELEVEL");
        check(sc.getTimeLimit() == 0, "default time limit 0 (unlimited)");
        check(sc.getCountLimit() == 0L, "default count limit 0 (unlimited)");
        check(sc.getDerefLinkFlag() == false, "default deref false");
        check(sc.getReturningObjFlag() == false, "default returning-obj false");
        check(sc.getReturningAttributes() == null, "default returning attrs null (all)");
    }

    static void dScFullCtor() {
        String[] attrs = {"cn", "sn"};
        SearchControls sc = new SearchControls(SearchControls.SUBTREE_SCOPE,
                5L, 1000, attrs, true, true);
        check(sc.getSearchScope() == SearchControls.SUBTREE_SCOPE, "ctor scope");
        check(sc.getCountLimit() == 5L, "ctor count limit");
        check(sc.getTimeLimit() == 1000, "ctor time limit");
        check(sc.getReturningObjFlag(), "ctor returning obj");
        check(sc.getDerefLinkFlag(), "ctor deref");
        check(java.util.Arrays.equals(sc.getReturningAttributes(), attrs), "ctor attrs");
        // extreme values are accepted (validation is the provider's job)
        SearchControls big = new SearchControls(Integer.MAX_VALUE, Long.MAX_VALUE,
                Integer.MAX_VALUE, null, false, false);
        check(big.getSearchScope() == Integer.MAX_VALUE
                && big.getCountLimit() == Long.MAX_VALUE, "extreme values accepted");
    }

    static void dScSetters() {
        SearchControls sc = new SearchControls();
        sc.setSearchScope(SearchControls.OBJECT_SCOPE);
        check(sc.getSearchScope() == SearchControls.OBJECT_SCOPE, "set scope");
        sc.setTimeLimit(42);
        check(sc.getTimeLimit() == 42, "set time");
        sc.setCountLimit(9L);
        check(sc.getCountLimit() == 9L, "set count");
        sc.setDerefLinkFlag(true);
        check(sc.getDerefLinkFlag(), "set deref");
        sc.setReturningObjFlag(true);
        check(sc.getReturningObjFlag(), "set returning obj");
        sc.setReturningAttributes(new String[] {"a"});
        check(sc.getReturningAttributes().length == 1
                && "a".equals(sc.getReturningAttributes()[0]), "set attrs");
        sc.setReturningAttributes(null);
        check(sc.getReturningAttributes() == null, "null attrs means all");
        sc.setReturningAttributes(new String[0]);
        check(sc.getReturningAttributes() != null && sc.getReturningAttributes().length == 0,
                "empty attrs means none");
    }

    static void dScSerialization() throws Exception {
        SearchControls sc = new SearchControls(SearchControls.SUBTREE_SCOPE,
                3L, 77, new String[] {"x"}, true, false);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) { oos.writeObject(sc); }
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            SearchControls back = (SearchControls) ois.readObject();
            check(back.getSearchScope() == SearchControls.SUBTREE_SCOPE, "round-trip scope");
            check(back.getCountLimit() == 3L, "round-trip count");
            check(back.getTimeLimit() == 77, "round-trip time");
            check(back.getReturningObjFlag(), "round-trip obj flag");
            check(back.getDerefLinkFlag() == false, "round-trip deref");
            check(java.util.Arrays.equals(back.getReturningAttributes(), new String[] {"x"}),
                    "round-trip attrs");
        }
    }

}
