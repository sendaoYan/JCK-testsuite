/*
 * @test
 * @summary javax.naming.ldap.SortKey conformance: attribute/ascending/rule.
 * @run main SortKeyTest
 */


public class SortKeyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.ldap.SortKey k = new javax.naming.ldap.SortKey("cn");
        check("cn".equals(k.getAttributeID()), "getAttributeID");
        check(k.isAscending(), "isAscending default");
        check(k.getMatchingRuleID() == null, "rule null default");
        javax.naming.ldap.SortKey k2 = new javax.naming.ldap.SortKey("sn", false, "caseIgnoreMatch");
        check(!k2.isAscending(), "isAscending false");
        check("caseIgnoreMatch".equals(k2.getMatchingRuleID()), "getMatchingRuleID");        System.out.println("PASS (" + passed + " assertions)");
    }
}
