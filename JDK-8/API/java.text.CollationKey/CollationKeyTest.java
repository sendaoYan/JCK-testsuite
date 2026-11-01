/*
 * @test
 * @summary java.text.CollationKey conformance: compareTo/source/toByteArray.
 * @run main CollationKeyTest
 */

import java.text.CollationKey;
import java.text.Collator;
import java.util.Locale;

public class CollationKeyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCollationKey() {
        Collator col = Collator.getInstance(Locale.US);
        CollationKey k1 = col.getCollationKey("apple");
        CollationKey k2 = col.getCollationKey("banana");
        CollationKey k3 = col.getCollationKey("apple");
        check(k1.compareTo(k2) < 0, "compareTo apple < banana");
        check(k2.compareTo(k1) > 0, "compareTo banana > apple");
        check(k1.compareTo(k3) == 0, "compareTo equal");
        check("apple".equals(k1.getSourceString()), "getSourceString");
        check(k1.toByteArray().length > 0, "toByteArray");
        check(k1.equals(k3), "equals");
        check(!k1.equals(k2), "not equals");
        check(k1.hashCode() == k3.hashCode(), "hashCode");
    }

    public static void main(String[] args) {
        testCollationKey();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
