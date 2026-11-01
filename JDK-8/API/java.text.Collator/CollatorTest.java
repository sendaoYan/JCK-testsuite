/*
 * @test
 * @summary java.text.Collator conformance: compare/strength/decomposition/constants.
 * @run main CollatorTest
 */

import java.text.Collator;
import java.util.Locale;

public class CollatorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCollator() {
        Collator col = Collator.getInstance(Locale.US);
        check(col.compare("a", "b") < 0, "compare a < b");
        check(col.compare("b", "a") > 0, "compare b > a");
        check(col.compare("a", "a") == 0, "compare equal");
        check(col.equals(Collator.getInstance(Locale.US)), "equals");
        check(Collator.getAvailableLocales().length > 0, "getAvailableLocales");
        check(col.getCollationKey("x") != null, "getCollationKey");
        check(col.equals("a", "a"), "equals(String,String) true");
        check(!col.equals("a", "b"), "equals(String,String) false");

        col.setStrength(Collator.SECONDARY);
        check(col.getStrength() == Collator.SECONDARY, "set/getStrength");
        col.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
        check(col.getDecomposition() == Collator.CANONICAL_DECOMPOSITION, "set/getDecomposition");

        check(Collator.PRIMARY == 0, "PRIMARY == 0");
        check(Collator.SECONDARY == 1, "SECONDARY == 1");
        check(Collator.TERTIARY == 2, "TERTIARY == 2");
        check(Collator.IDENTICAL == 3, "IDENTICAL == 3");
        check(Collator.NO_DECOMPOSITION == 0, "NO_DECOMPOSITION == 0");
        check(Collator.CANONICAL_DECOMPOSITION == 1, "CANONICAL_DECOMPOSITION == 1");
        check(Collator.FULL_DECOMPOSITION == 2, "FULL_DECOMPOSITION == 2");
    }

    public static void main(String[] args) {
        testCollator();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
