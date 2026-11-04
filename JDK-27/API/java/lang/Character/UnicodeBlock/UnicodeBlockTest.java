/*
 * @test
 * @summary java.lang.Character.UnicodeBlock conformance: white-box structure + black-box behavior.
 * @run main UnicodeBlockTest
 */

import java.lang.Character.UnicodeBlock;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class UnicodeBlockTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Character$UnicodeBlock");
        check(Modifier.isFinal(c.getModifiers()), "UnicodeBlock is final");
        check(c.getSuperclass() == Character.Subset.class, "extends Character.Subset");
        for (Method m : new Method[]{c.getMethod("of", char.class),
                                     c.getMethod("of", int.class),
                                     c.getMethod("forName", String.class)}) {
            check(Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers()),
                  m.getName() + " is public static");
        }
        check(c.getField("BASIC_LATIN").get(null) == UnicodeBlock.BASIC_LATIN,
              "BASIC_LATIN is a public static field");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testOf() {
        check(UnicodeBlock.of('A') == UnicodeBlock.BASIC_LATIN, "of maps 'A' to BASIC_LATIN");
        check(UnicodeBlock.of((char) 0x4E00) == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS,
              "of maps CJK ideograph");
        check(UnicodeBlock.of(0x1F600) == UnicodeBlock.EMOTICONS, "of(int) maps supplementary plane");
        check(UnicodeBlock.of(0x0000) == UnicodeBlock.BASIC_LATIN, "of(0) is BASIC_LATIN");
    }

    static void testForName() {
        check(UnicodeBlock.forName("Basic Latin") == UnicodeBlock.BASIC_LATIN,
              "forName accepts the original block name");
        check(UnicodeBlock.forName("BASIC_LATIN") == UnicodeBlock.BASIC_LATIN,
              "forName accepts the constant name");
        check(UnicodeBlock.forName("CJK Unified Ideographs") == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS,
              "forName long name");
        try {
            UnicodeBlock.forName("No Such Block Jck");
            throw new AssertionError("forName(invalid): expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
    }

    static void testConstants() {
        check("BASIC_LATIN".equals(UnicodeBlock.BASIC_LATIN.toString()), "BASIC_LATIN toString");
        check(UnicodeBlock.GREEK != UnicodeBlock.CYRILLIC, "distinct blocks are distinct instances");
        check(UnicodeBlock.BASIC_LATIN.equals(UnicodeBlock.BASIC_LATIN), "constant equals self");
        check(UnicodeBlock.BASIC_LATIN.hashCode() == UnicodeBlock.BASIC_LATIN.hashCode(),
              "hashCode stable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testOf();
        testForName();
        testConstants();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
