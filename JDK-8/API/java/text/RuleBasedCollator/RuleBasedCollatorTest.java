/*
 * @test
 * @summary java.text.RuleBasedCollator conformance: white-box structure + black-box rule-based compare/key behavior.
 * @run main RuleBasedCollatorTest
 */

import java.text.CollationElementIterator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.text.StringCharacterIterator;

public class RuleBasedCollatorTest {
    static int passed;

    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Exception; }

    static void expectThrows(ThrowingRunnable r, Class<? extends Throwable> t, String m) {
        try {
            r.run();
        } catch (Throwable x) {
            if (t.isInstance(x)) { passed++; return; }
            throw new AssertionError(m + ": expected " + t.getSimpleName() + " but got " + x, x);
        }
        throw new AssertionError(m + ": expected " + t.getSimpleName());
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.text.RuleBasedCollator");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(c.getSuperclass().getName().equals("java.text.Collator"), "extends Collator");
        check(c.getConstructor(String.class) != null, "ctor(String)");
        check(c.getMethod("getRules") != null, "getRules");
        check(c.getMethod("getCollationElementIterator", String.class) != null, "getCollationElementIterator(String)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testRuleCompare() throws Exception {
        RuleBasedCollator rbc = new RuleBasedCollator("< a < b < c");
        check("< a < b < c".equals(rbc.getRules()), "getRules round-trip");
        check(rbc.compare("a", "b") < 0, "a < b");
        check(rbc.compare("b", "c") < 0, "b < c");
        check(rbc.compare("c", "a") > 0, "c > a");
        check(rbc.compare("a", "a") == 0, "a == a");
        check(rbc.compare("ab", "ac") < 0, "substring secondary char compared");
        check(rbc.equals("a", "a") && !rbc.equals("a", "b"), "equals(String,String)");
    }

    static void testInvalidRules() {
        expectThrows(() -> new RuleBasedCollator("no-relation-here"), ParseException.class, "invalid rules throw ParseException");
        expectThrows(() -> new RuleBasedCollator(""), ParseException.class, "empty rules throw ParseException");
    }

    static void testCollationElementIterator() throws Exception {
        RuleBasedCollator rbc = new RuleBasedCollator("< a < b < c");
        CollationElementIterator it = rbc.getCollationElementIterator("abc");
        int count = 0;
        while (it.next() != CollationElementIterator.NULLORDER) count++;
        check(count == 3, "3 collation elements for abc");

        CollationElementIterator it2 = rbc.getCollationElementIterator(new StringCharacterIterator("ab"));
        int count2 = 0;
        while (it2.next() != CollationElementIterator.NULLORDER) count2++;
        check(count2 == 2, "2 collation elements via CharacterIterator");
    }

    static void testContract() throws Exception {
        RuleBasedCollator a = new RuleBasedCollator("< a < b < c");
        RuleBasedCollator b = new RuleBasedCollator("< a < b < c");
        check(a.equals(b) && b.equals(a), "same rules equal");
        check(a.hashCode() == b.hashCode(), "same rules hashCode");
        RuleBasedCollator c = (RuleBasedCollator) a.clone();
        check(a.equals(c) && c.equals(a), "clone equals");
        check(a.hashCode() == c.hashCode(), "clone hashCode");
        RuleBasedCollator d = new RuleBasedCollator("< a < b < c < d");
        check(!a.equals(d), "different rules not equal");
    }

    
    static void testBlackBox() throws Exception {
        testRuleCompare();
        testInvalidRules();
        testCollationElementIterator();
        testContract();
    }
public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
