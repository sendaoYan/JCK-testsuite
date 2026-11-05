/*
 * @test
 * @summary javax.naming.ldap.SortKey conformance: white-box structure + black-box behavior.
 * @run main SortKeyTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.ldap.*;
import javax.naming.ldap.SortControl;
import javax.naming.ldap.SortKey;
import java.util.Arrays;

public class SortKeyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.SortKey");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 1 && x.getParameterTypes()[0] == String.class), "ctor(String)");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 3 && x.getParameterTypes()[0] == String.class && x.getParameterTypes()[1] == boolean.class && x.getParameterTypes()[2] == String.class), "ctor(String,boolean,String)");
        for (String m : new String[]{"getAttributeID", "isAscending", "getMatchingRuleID"}) {
            boolean found = false;
            for (Method mm : c.getMethods()) if (mm.getName().equals(m)) found = true;
            check(found, m + " present");
        }
    }

    static void testBlackBox() {
        SortKey k = new SortKey("cn", true, "caseIgnoreMatch");
        check("cn".equals(k.getAttributeID()), "attributeID");
        check(k.isAscending(), "ascending");
        check("caseIgnoreMatch".equals(k.getMatchingRuleID()), "matchingRuleID");

        SortKey d = new SortKey("sn");
        check("sn".equals(d.getAttributeID()), "default attributeID");
        check(d.isAscending(), "default ascending");
        check(d.getMatchingRuleID() == null, "default matching rule null");

        SortKey desc = new SortKey("mail", false, null);
        check(!desc.isAscending(), "descending");
        check(desc.getMatchingRuleID() == null, "null matching rule");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContracts();
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
