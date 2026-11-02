/*
 * @test
 * @summary javax.naming.directory.SearchControls conformance: API type verification.
 * @run main SearchControlsTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SearchControlsTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.directory.SearchControls sc = new javax.naming.directory.SearchControls();
check(sc.getSearchScope() == javax.naming.directory.SearchControls.ONELEVEL_SCOPE, "default scope");
sc.setSearchScope(javax.naming.directory.SearchControls.SUBTREE_SCOPE);
check(sc.getSearchScope() == javax.naming.directory.SearchControls.SUBTREE_SCOPE, "scope");
sc.setCountLimit(10);
check(sc.getCountLimit() == 10, "count limit");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
