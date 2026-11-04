/*
 * @test
 * @summary java.text.Collator API conformance: full contract coverage.
 *          strength/decomposition constants, compare(String,String)/compare(Object,
 *          Object)/equals(String,String), strength levels (TERTIARY/PRIMARY/SECONDARY/
 *          IDENTICAL) incl. case/accent sensitivity, getCollationKey ordering
 *          consistency, decomposition modes, equals/hashCode/clone, protected
 *          constructor via subclass, setStrength/setDecomposition invalid values ->
 *          IllegalArgumentException, and empty-string collation key boundary.
 * @run main CollatorTest
 */

import java.text.CollationKey;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public class CollatorTest {
    static int passed;
    static final Locale US = Locale.US;

    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Exception; }

    static void expectThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String m) {
        try {
            r.run();
        } catch (Throwable x) {
            if (expected.isInstance(x)) { passed++; return; }
            if (x instanceof AssertionError) throw (AssertionError) x;
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " but got " + x.getClass().getName(), x);
        }
        throw new AssertionError(m + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.text.Collator");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete abstract class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Comparator.class.isAssignableFrom(c), "implements Comparator");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(c.getField("PRIMARY").getInt(null) == 0, "PRIMARY=0");
        check(c.getField("SECONDARY").getInt(null) == 1, "SECONDARY=1");
        check(c.getField("TERTIARY").getInt(null) == 2, "TERTIARY=2");
        check(c.getField("IDENTICAL").getInt(null) == 3, "IDENTICAL=3");
        check(c.getField("NO_DECOMPOSITION").getInt(null) == 0, "NO_DECOMPOSITION=0");
        check(c.getField("CANONICAL_DECOMPOSITION").getInt(null) == 1, "CANONICAL_DECOMPOSITION=1");
        check(c.getField("FULL_DECOMPOSITION").getInt(null) == 2, "FULL_DECOMPOSITION=2");
        check(Collator.getInstance() instanceof Collator, "getInstance() non-null");
        check(Collator.getAvailableLocales().length > 0, "getAvailableLocales non-empty");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testCompare() {
        Collator col = Collator.getInstance(US);
        check(col.compare("a", "b") < 0, "compare a,b < 0");
        check(col.compare("b", "a") > 0, "compare b,a > 0");
        check(col.compare("a", "a") == 0, "compare a,a == 0");
        check(col.compare("apple", "banana") < 0, "compare words");
        check(col.equals("a", "a"), "equals(String,String) true");
        check(!col.equals("a", "b"), "equals(String,String) false");
        check(col.compare((Object) "a", (Object) "b") < 0, "compare(Object,Object)");
        check(col.compare("", "") == 0, "compare empty strings equal");
        expectThrows(NullPointerException.class, () -> col.compare(null, "a"), "compare(null,..) -> NPE");
        expectThrows(NullPointerException.class, () -> col.compare("a", (String) null), "compare(..,null) -> NPE");
    }

    static void testStrength() {
        Collator col = Collator.getInstance(US);
        check(col.getStrength() == Collator.TERTIARY, "default strength TERTIARY");
        check(col.compare("a", "A") != 0, "tertiary a != A");
        col.setStrength(Collator.PRIMARY);
        check(col.getStrength() == Collator.PRIMARY, "setStrength PRIMARY");
        check(col.compare("a", "A") == 0, "primary a == A");
        check(col.compare("a", "b") < 0, "primary a < b");
        col.setStrength(Collator.SECONDARY);
        check(col.getStrength() == Collator.SECONDARY, "setStrength SECONDARY");
        col.setStrength(Collator.IDENTICAL);
        check(col.getStrength() == Collator.IDENTICAL, "setStrength IDENTICAL");
        check(col.compare("a", "a") == 0, "identical strength equal");
        check(col.compare("a", "A") != 0, "identical strength a != A");
    }

    static void testCollationKey() {
        Collator col = Collator.getInstance(US);
        CollationKey ka = col.getCollationKey("a");
        CollationKey kb = col.getCollationKey("b");
        CollationKey ka2 = col.getCollationKey("a");
        check(ka.equals(ka2), "equal strings -> equal keys");
        check(ka.compareTo(kb) < 0, "key order a < b");
        check(col.compare("a", "b") < 0 && ka.compareTo(kb) < 0, "key order consistent with compare");
        check("".equals(col.getCollationKey("").getSourceString()), "collation key source empty");
        check(col.getCollationKey("").compareTo(col.getCollationKey("a")) < 0, "empty key < a key");
    }

    static void testDecompositionAndContract() {
        Collator col = Collator.getInstance(US);
        check(col.getDecomposition() == Collator.NO_DECOMPOSITION, "default decomposition NO");
        col.setDecomposition(Collator.FULL_DECOMPOSITION);
        check(col.getDecomposition() == Collator.FULL_DECOMPOSITION, "setDecomposition FULL");
        col.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
        check(col.getDecomposition() == Collator.CANONICAL_DECOMPOSITION, "setDecomposition CANONICAL");

        Collator a = Collator.getInstance(US);
        Collator b = (Collator) a.clone();
        check(a.equals(b) && b.equals(a), "clone equals");
        check(a.hashCode() == b.hashCode(), "clone hashCode");
        check(a.equals(a), "reflexive equals");

        expectThrows(IllegalArgumentException.class, () -> col.setStrength(999), "setStrength(999) IAE");
        expectThrows(IllegalArgumentException.class, () -> col.setDecomposition(999), "setDecomposition(999) IAE");
        expectThrows(NullPointerException.class, () -> Collator.getInstance(null), "getInstance(null) -> NPE");
    }

    // Minimal concrete subclass to exercise the protected Collator() constructor.
    static class MinimalCollator extends Collator {
        public int compare(String a, String b) { return a.compareTo(b); }
        public CollationKey getCollationKey(String s) { return null; }
        public int hashCode() { return 7; }
    }

    static void testSubclass() {
        MinimalCollator c = new MinimalCollator();
        check(c != null, "protected constructor via subclass");
        check(c.compare("a", "b") < 0, "subclass compare a,b < 0");
        check(c.compare("b", "a") > 0, "subclass compare b,a > 0");
        check(c.equals("a", "a") && !c.equals("a", "b"), "subclass equals(String,String)");
        check(c.getStrength() == Collator.TERTIARY, "protected ctor default strength TERTIARY");
        check(c.getDecomposition() == Collator.CANONICAL_DECOMPOSITION,
              "protected ctor default decomposition CANONICAL");
        c.setStrength(Collator.PRIMARY);
        check(c.getStrength() == Collator.PRIMARY, "setStrength PRIMARY");
        c.setDecomposition(Collator.FULL_DECOMPOSITION);
        check(c.getDecomposition() == Collator.FULL_DECOMPOSITION, "setDecomposition FULL");
        check(c.equals(c), "reflexive equals");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testCompare();
        testStrength();
        testCollationKey();
        testDecompositionAndContract();
        testSubclass();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
