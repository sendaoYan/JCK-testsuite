/*
 * @test
 * @summary java.text.RuleBasedCollator conformance: custom rules, compare, keys, elements.
 * @run main RuleBasedCollatorTest
 */

import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;

public class RuleBasedCollatorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testRuleBasedCollator() throws ParseException {
        String rules = "< a < b < c < d";
        RuleBasedCollator col = new RuleBasedCollator(rules);
        check(col.compare("a", "d") < 0, "compare a < d");
        check(col.compare("c", "b") > 0, "compare c > b");
        check(col.compare("a", "a") == 0, "compare equal");
        check(rules.equals(col.getRules()), "getRules");
        check(col.getCollationKey("a") != null, "getCollationKey");
        check(col.getCollationElementIterator("a") != null, "getCollationElementIterator");
        check(col.getCollationKey("a").compareTo(col.getCollationKey("d")) < 0,
              "key ordering");

        RuleBasedCollator col2 = new RuleBasedCollator(rules);
        check(col.equals(col2), "equals");
        check(col.hashCode() == col2.hashCode(), "hashCode");

        col.setStrength(Collator.PRIMARY);
        check(col.getStrength() == Collator.PRIMARY, "setStrength");
    }

    public static void main(String[] args) throws ParseException {
        testRuleBasedCollator();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
