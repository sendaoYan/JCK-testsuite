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

    // ---- deepening helper (contract-level additions) ----
    interface TestBlock { void go() throws Exception; }

    static void expectX(Class<? extends Throwable> expected, TestBlock r, String m) {
        try {
            r.go();
        } catch (Throwable x) {
            if (expected.isInstance(x)) { passed++; return; }
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " but got " + x.getClass().getName(), x);
        }
        throw new AssertionError(m + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }


    // ================= contract deepening =================

    static void testCustomRuleOrderingContract() throws java.text.ParseException {
        java.text.RuleBasedCollator rbc = new java.text.RuleBasedCollator("< b < a");
        check(rbc.compare("a", "b") == 1, "custom rule < b < a: a sorts after b");
        check(rbc.compare("b", "a") == -1, "custom rule: b before a");
        check("< b < a".equals(rbc.getRules()), "getRules round-trips rule text");
        java.text.RuleBasedCollator r2 = new java.text.RuleBasedCollator("< c < a < b");
        check(r2.compare("a", "c") == 1, "rule < c < a < b: a after c");
        check(r2.compare("c", "a") == -1, "rule: c before a");
        java.text.RuleBasedCollator r3 = new java.text.RuleBasedCollator("< a,A < b,B");
        check(r3.compare("a", "A") == -1, "comma lists one primary relation: case still tertiary-different");
        java.text.RuleBasedCollator r4 = new java.text.RuleBasedCollator("< a = A < b");
        check(r4.compare("a", "A") == 0, "'=' relation makes exact equality");
        java.text.RuleBasedCollator amp = new java.text.RuleBasedCollator("< a < b & b < c");
        check("< a < b & b < c".equals(amp.getRules()), "reset '&' round-trips");
        check(amp.compare("a", "c") == -1, "reset relation participates in ordering");
    }

    static void testIllegalRulesContract() {
        expectX(java.text.ParseException.class, () -> new java.text.RuleBasedCollator("< b << "),
                "trailing relation -> ParseException");
        try {
            new java.text.RuleBasedCollator("< b << ");
            throw new AssertionError("unreachable");
        } catch (java.text.ParseException pe) {
            check(pe.getMessage() != null && pe.getMessage().startsWith("missing chars"),
                  "trailing relation message starts with 'missing chars'");
        }
        expectX(java.text.ParseException.class, () -> new java.text.RuleBasedCollator("bogus"),
                "no relations -> ParseException");
        expectX(java.text.ParseException.class, () -> new java.text.RuleBasedCollator("& a < b"),
                "leading reset -> ParseException");
        expectX(java.text.ParseException.class, () -> new java.text.RuleBasedCollator("< a << b << b"),
                "duplicate relation -> ParseException");
        expectX(NullPointerException.class, () -> new java.text.RuleBasedCollator((String) null),
                "null rules -> NPE");
    }

    static void testEqualityAndCloneContract() throws java.text.ParseException {
        java.text.RuleBasedCollator a = new java.text.RuleBasedCollator("< a < b < c");
        java.text.RuleBasedCollator b = new java.text.RuleBasedCollator("< a < b < c");
        check(a.equals(b) && a.hashCode() == b.hashCode(), "equal rules equal");
        check(a.clone().equals(a), "clone equal");
        check(!a.equals(new java.text.RuleBasedCollator("< b < a")), "different rules not equal");
        check(a.compare("a", "a") == 0, "same string compares 0");
        java.text.RuleBasedCollator mix = new java.text.RuleBasedCollator("< a,A < b,B < c,C");
        mix.setStrength(java.text.Collator.TERTIARY);
        check(mix.compare("A", "a") == 1,
              "comma list: earlier-listed char first at TERTIARY (A after a)");
        check(mix.compare("a", "A") == -1, "comma list antisymmetric at TERTIARY");
        mix.setStrength(java.text.Collator.PRIMARY);
        check(mix.compare("A", "a") == 0, "custom-rule collator honors setStrength(PRIMARY)");
        mix.setStrength(java.text.Collator.SECONDARY);
        check(mix.compare("B", "b") == 0, "custom-rule collator honors setStrength(SECONDARY)");
    }

    static void testElementIteratorAccessContract() {
        java.text.RuleBasedCollator en = (java.text.RuleBasedCollator) java.text.Collator.getInstance(java.util.Locale.ENGLISH);
        java.text.CollationElementIterator i1 = en.getCollationElementIterator("ab");
        check(i1 != null, "getCollationElementIterator(String)");
        java.text.CollationElementIterator i2 =
                en.getCollationElementIterator(new java.text.StringCharacterIterator("ab"));
        check(i2 != null, "getCollationElementIterator(CharacterIterator)");
        expectX(NullPointerException.class, () -> en.getCollationElementIterator((String) null),
                "getCollationElementIterator(null) -> NPE");
    }
public static void main(String[] args) throws Exception {
        testCustomRuleOrderingContract();
        testIllegalRulesContract();
        testEqualityAndCloneContract();
        testElementIteratorAccessContract();
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
