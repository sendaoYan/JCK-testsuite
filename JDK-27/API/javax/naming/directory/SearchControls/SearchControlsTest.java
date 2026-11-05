/*
 * @test
 * @summary javax.naming.directory.SearchControls conformance: white-box structure + black-box behavior.
 * @run main SearchControlsTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.directory.*;
import javax.naming.directory.SearchControls;
import java.util.Arrays;

public class SearchControlsTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.directory.SearchControls");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(SearchControls.OBJECT_SCOPE == 0, "OBJECT_SCOPE==0");
        check(SearchControls.ONELEVEL_SCOPE == 1, "ONELEVEL_SCOPE==1");
        check(SearchControls.SUBTREE_SCOPE == 2, "SUBTREE_SCOPE==2");
        check(Arrays.asList(c.getConstructors()).stream().anyMatch(x -> x.getParameterCount() == 0), "no-arg ctor");
        check(Arrays.asList(c.getConstructors()).stream().anyMatch(x -> x.getParameterCount() == 6), "6-arg ctor");
    }

    static void testBlackBox() throws Exception {
        SearchControls sc = new SearchControls();
        check(sc.getSearchScope() == SearchControls.ONELEVEL_SCOPE, "default scope ONELEVEL");
        check(sc.getTimeLimit() == 0, "default time limit 0");
        check(!sc.getDerefLinkFlag(), "default deref false");
        check(!sc.getReturningObjFlag(), "default returningObj false");
        check(sc.getCountLimit() == 0, "default count limit 0");
        check(sc.getReturningAttributes() == null, "default returningAttributes null");

        sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
        check(sc.getSearchScope() == SearchControls.SUBTREE_SCOPE, "setSearchScope");
        sc.setTimeLimit(5000);
        check(sc.getTimeLimit() == 5000, "setTimeLimit");
        sc.setDerefLinkFlag(true);
        check(sc.getDerefLinkFlag(), "setDerefLinkFlag");
        sc.setReturningObjFlag(true);
        check(sc.getReturningObjFlag(), "setReturningObjFlag");
        sc.setCountLimit(100L);
        check(sc.getCountLimit() == 100L, "setCountLimit");
        String[] attrs = {"cn", "sn", "mail"};
        sc.setReturningAttributes(attrs);
        check(Arrays.equals(sc.getReturningAttributes(), attrs), "setReturningAttributes");

        // full constructor
        String[] fullAttrs = {"cn"};
        SearchControls sc2 = new SearchControls(SearchControls.OBJECT_SCOPE, 25L, 3000, fullAttrs, true, true);
        check(sc2.getSearchScope() == SearchControls.OBJECT_SCOPE, "ctor scope");
        check(sc2.getCountLimit() == 25L, "ctor count");
        check(sc2.getTimeLimit() == 3000, "ctor time");
        check(sc2.getReturningObjFlag(), "ctor retobj");
        check(sc2.getDerefLinkFlag(), "ctor deref");
        check(Arrays.equals(sc2.getReturningAttributes(), fullAttrs), "ctor attrs");

        // scope round-trip across all three scopes
        for (int scope : new int[]{SearchControls.OBJECT_SCOPE, SearchControls.ONELEVEL_SCOPE, SearchControls.SUBTREE_SCOPE}) {
            sc.setSearchScope(scope);
            check(sc.getSearchScope() == scope, "scope round-trip " + scope);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContracts();
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
