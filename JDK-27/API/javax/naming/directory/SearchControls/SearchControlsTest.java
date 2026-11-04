/*
 * @test
 * @summary javax.naming.directory.SearchControls conformance: white-box structure + black-box behavior.
 * @run main SearchControlsTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.directory.*;

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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
