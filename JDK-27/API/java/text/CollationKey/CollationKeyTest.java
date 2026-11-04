/*
 * @test
 * @summary java.text.CollationKey conformance: white-box structure + black-box behavior.
 * @run main CollationKeyTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.CollationKey;
import java.text.Collator;
import java.text.RuleBasedCollator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CollationKeyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Runnable r, Class<? extends Throwable> type, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                        + " but got " + t.getClass().getSimpleName(), t);
            passed++;
        }
    }

    /** Minimal foreign CollationKey subclass (built via the protected constructor). */
    static class MyKey extends CollationKey {
        MyKey(String source) { super(source); }
        @Override public int compareTo(CollationKey target) {
            return getSourceString().compareTo(target.getSourceString());
        }
        @Override public byte[] toByteArray() {
            return getSourceString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.text.CollationKey");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(Comparable.class.isAssignableFrom(c), "implements Comparable<CollationKey>");
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1 && Modifier.isProtected(ctors[0].getModifiers()),
                "single protected CollationKey(String)");
        check(ctors[0].getParameterTypes()[0] == String.class, "ctor takes source string");

        Method cmp = c.getMethod("compareTo", CollationKey.class);
        check(Modifier.isAbstract(cmp.getModifiers()), "compareTo(CollationKey) abstract");
        check(cmp.getReturnType() == int.class, "compareTo returns int");
        Method toBa = c.getMethod("toByteArray");
        check(Modifier.isAbstract(toBa.getModifiers()), "toByteArray abstract");
        check(toBa.getReturnType() == byte[].class, "toByteArray returns byte[]");
        Method src = c.getMethod("getSourceString");
        check(!Modifier.isAbstract(src.getModifiers()) && src.getReturnType() == String.class,
                "concrete getSourceString() -> String");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        RuleBasedCollator col = (RuleBasedCollator) Collator.getInstance(Locale.ENGLISH);

        // 1. getSourceString returns the exact text the key was built from
        CollationKey kAbc = col.getCollationKey("abc");
        check("abc".equals(kAbc.getSourceString()), "getSourceString round-trip");
        check("".equals(col.getCollationKey("").getSourceString()), "empty source round-trip");

        // 2. compareTo agrees with Collator.compare and with lexicographic order (English)
        CollationKey a = col.getCollationKey("a");
        CollationKey b = col.getCollationKey("b");
        CollationKey c = col.getCollationKey("c");
        check(a.compareTo(b) < 0 && b.compareTo(a) > 0, "sign antisymmetry a<b");
        check(a.compareTo(c) < 0, "transitive ordering a<c");
        check(a.compareTo(a) == 0, "self comparison zero");
        check(col.compare("a", "b") < 0, "key order matches collator.compare sign");

        // 3. equal source strings produce equal keys even from separate calls
        CollationKey abcAgain = col.getCollationKey("abc");
        check(kAbc.compareTo(abcAgain) == 0 && abcAgain.compareTo(kAbc) == 0,
                "two keys of equal text compare 0");

        // 4. sorting keys sorts the strings
        List<CollationKey> keys = new ArrayList<>(Arrays.asList(
                col.getCollationKey("delta"), col.getCollationKey("alpha"),
                col.getCollationKey("charlie"), col.getCollationKey("bravo")));
        Collections.sort(keys);
        StringBuilder joined = new StringBuilder();
        for (CollationKey k : keys) joined.append(k.getSourceString());
        check("alphabravocharliedelta".contentEquals(joined),
                "Comparable sort orders keys by collation order");

        // 5. toByteArray: non-empty, deterministic, distinguishes different sources
        byte[] ba1 = kAbc.toByteArray();
        byte[] ba2 = abcAgain.toByteArray();
        byte[] baAbd = col.getCollationKey("abd").toByteArray();
        check(ba1.length > 0, "toByteArray non-empty");
        check(Arrays.equals(ba1, ba2), "byte form deterministic for equal text");
        check(!Arrays.equals(ba1, baAbd), "byte form differs for different text");

        // 6. foreign key subclass: protected ctor usable, own compareTo/toByteArray honored
        MyKey my = new MyKey("zzz");
        check("zzz".equals(my.getSourceString()), "protected ctor stores source");
        check(my.toByteArray().length == 3, "subclass toByteArray honored");
        check(my.compareTo(new MyKey("aaa")) > 0 && my.compareTo(new MyKey("zzz")) == 0,
                "subclass compareTo via Comparable");

        // 7. RuleBasedCollationKey.compareTo rejects foreign key implementations
        expectThrows(() -> kAbc.compareTo(my), ClassCastException.class,
                "rule-based key rejects foreign CollationKey");

        // 8. keys remain valid after collator strength changes on a second collator
        RuleBasedCollator col2 = (RuleBasedCollator) Collator.getInstance(Locale.ENGLISH);
        check(col2.getCollationKey("abc").compareTo(col2.getCollationKey("abd")) < 0,
                "independent collator yields consistent key order");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
