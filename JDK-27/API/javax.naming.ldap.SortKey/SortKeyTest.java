/*
 * @test
 * @summary javax.naming.ldap.SortKey conformance: white-box structure + black-box behavior.
 * @run main SortKeyTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.ldap.*;

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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
