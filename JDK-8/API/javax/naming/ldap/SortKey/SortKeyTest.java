/*
 * @test
 * @summary javax.naming.ldap.SortKey conformance: attribute/ascending/rule.
 * @run main SortKeyTest
 */



import javax.naming.ldap.SortControl;
import javax.naming.ldap.SortKey;
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
        check("caseIgnoreMatch".equals(k2.getMatchingRuleID()), "getMatchingRuleID");        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dSkAccessors();
        dSkDefaults();
        dSkNegative();
        System.out.println("deep: SortKey contracts OK");
    }

    static void dSkAccessors() {
        SortKey full = new SortKey("cn", false, "caseIgnoreOrderingMatch");
        check("cn".equals(full.getAttributeID()), "getAttributeID");
        check(!full.isAscending(), "descending order");
        check("caseIgnoreOrderingMatch".equals(full.getMatchingRuleID()), "matching rule");
        SortKey asc = new SortKey("cn", true, null);
        check(asc.isAscending(), "ascending order");
        check(asc.getMatchingRuleID() == null, "no matching rule");
    }

    static void dSkDefaults() {
        SortKey simple = new SortKey("sn");
        check("sn".equals(simple.getAttributeID()), "single-arg ctor id");
        check(simple.isAscending(), "single-arg ctor defaults ascending");
        check(simple.getMatchingRuleID() == null, "single-arg ctor no rule");
        // SortKey does not override equals/hashCode/toString: identity semantics
        SortKey a = new SortKey("cn");
        SortKey b = new SortKey("cn", true, null);
        check(!a.equals(b), "same-valued keys are distinct (no equals override)");
        check(a.equals(a), "identity equality");
        check(a.hashCode() == a.hashCode(), "identity hashCode stable");
        check(a.toString().startsWith("javax.naming.ldap.SortKey@"), "Object.toString form");
    }

    static void dSkNegative() {
        // documented as non-null but stored as-is by the implementation
        check(new SortKey(null).getAttributeID() == null, "null id tolerated (single-arg)");
        check(new SortKey(null, false, null).getAttributeID() == null, "null id tolerated (three-arg)");
        check(new SortKey(null, false, null).getMatchingRuleID() == null, "null rule stored");
        // SortKey[] drives SortControl (array form is the canonical usage)
        try {
            new SortControl(new SortKey[]{new SortKey("cn")}, true);
            check(true, "array form accepted by SortControl");
        } catch (java.io.IOException e) {
            throw new AssertionError("array form rejected: " + e);
        }
    }

}
