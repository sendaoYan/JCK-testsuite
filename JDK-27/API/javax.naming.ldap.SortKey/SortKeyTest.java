/*
 * @test
 * @summary javax.naming.ldap.SortKey conformance: API type verification.
 * @run main SortKeyTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SortKeyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.ldap.SortKey k = new javax.naming.ldap.SortKey("cn", true, "caseIgnoreMatch");
check(k.getAttributeID().equals("cn"), "attr");
check(k.isAscending(), "ascending");
check(k.getMatchingRuleID().equals("caseIgnoreMatch"), "rule");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
