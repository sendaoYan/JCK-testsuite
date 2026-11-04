/*
 * @test
 * @summary java.lang.Character.UnicodeScript conformance: white-box structure + black-box behavior.
 * @run main UnicodeScriptTest
 */

import java.lang.Character.UnicodeScript;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class UnicodeScriptTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Character$UnicodeScript");
        check(Modifier.isFinal(c.getModifiers()), "UnicodeScript is final");
        check(c.getSuperclass() == Enum.class, "extends Enum");
        check(c.isEnum(), "is an enum");
        for (Method m : new Method[]{c.getMethod("of", int.class),
                                     c.getMethod("values"),
                                     c.getMethod("valueOf", String.class)}) {
            check(Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers()),
                  m.getName() + " is public static");
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testOf() {
        check(UnicodeScript.of('A') == UnicodeScript.LATIN, "of maps 'A' to LATIN");
        check(UnicodeScript.of(0x4E00) == UnicodeScript.HAN, "of maps CJK ideograph to HAN");
        check(UnicodeScript.of('(') == UnicodeScript.COMMON, "of maps punctuation to COMMON");
        check(UnicodeScript.of(0x0410) == UnicodeScript.CYRILLIC, "of maps Cyrillic");
        check(UnicodeScript.of(0x05D0) == UnicodeScript.HEBREW, "of maps Hebrew");
        check(UnicodeScript.of(0x1F600) == UnicodeScript.COMMON,
              "of maps emoji to COMMON (emoji are Common script)");
    }

    static void testEnumMechanics() {
        check(UnicodeScript.values().length >= 160, "at least 160 scripts defined");
        check(UnicodeScript.valueOf("HAN") == UnicodeScript.HAN, "valueOf round-trip");
        check(UnicodeScript.LATIN.name().equals("LATIN"), "name is LATIN");
        check(UnicodeScript.LATIN.compareTo(UnicodeScript.HAN) != 0, "compareTo non-zero across constants");
        try {
            UnicodeScript.valueOf("NO_SUCH_SCRIPT");
            throw new AssertionError("valueOf(invalid): expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testOf();
        testEnumMechanics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
