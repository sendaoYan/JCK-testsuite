/*
 * @test
 * @summary java.net.IDN conformance: white-box structure + full contract
 *          coverage. toASCII/toUnicode round-trip, ACE (xn--) form, ASCII
 *          pass-through, USE_STD3_ASCII_RULES (leading/trailing hyphen and
 *          non-LDH ASCII rejected), empty-label validation, trailing-dot/root
 *          labels, ALLOW_UNASSIGNED flag, and exact IAE/NPE paths.
 * @run main IDNTest
 */

import java.lang.reflect.Modifier;
import java.net.IDN;

public class IDNTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.IDN");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getField("ALLOW_UNASSIGNED").getInt(null) > 0, "ALLOW_UNASSIGNED");
        check(c.getField("USE_STD3_ASCII_RULES").getInt(null) > 0, "USE_STD3_ASCII_RULES");
        check(c.getMethod("toASCII", String.class) != null, "toASCII(String)");
        check(c.getMethod("toUnicode", String.class) != null, "toUnicode(String)");
        check(Modifier.isPrivate(c.getDeclaredConstructor().getModifiers()), "private ctor");
    }

    static void testFlags() {
        check(IDN.ALLOW_UNASSIGNED != 0, "ALLOW_UNASSIGNED non-zero");
        check(IDN.USE_STD3_ASCII_RULES != 0, "USE_STD3_ASCII_RULES non-zero");
        check(IDN.ALLOW_UNASSIGNED != IDN.USE_STD3_ASCII_RULES, "flags distinct");
    }

    static void testToAsciiUnicode() throws Exception {
        check("xn--bcher-kva.de".equals(IDN.toASCII("bücher.de")), "toASCII umlaut");
        check("bücher.de".equals(IDN.toUnicode("xn--bcher-kva.de")), "toUnicode ACE");
        check("bücher.de".equals(IDN.toUnicode(IDN.toASCII("bücher.de"))), "toASCII/toUnicode round trip");
        check("example.com".equals(IDN.toASCII("example.com")), "ASCII passthrough toASCII");
        check("example.com".equals(IDN.toUnicode("example.com")), "ASCII passthrough toUnicode");
        check(IDN.toASCII("example.com").equals(IDN.toASCII("example.com", 0)), "flag 0 default");
        check("www.xn--exmple-j43e.com".equals(IDN.toASCII("www.exあmple.com")), "label with kana");
    }

    static void testTrailingDotAndRoot() throws Exception {
        check("com.".equals(IDN.toASCII("com.", IDN.USE_STD3_ASCII_RULES)), "trailing dot legal");
        check(".".equals(IDN.toASCII(".", IDN.USE_STD3_ASCII_RULES)), "root label legal");
        check("com.".equals(IDN.toASCII("com。")), "ideographic full stop maps to dot");
    }

    static void testEmptyLabelValidation() {
        String[] illegal = {"com..net", "com..", ".com", ".com."};
        for (String name : illegal) {
            checkThrows(IllegalArgumentException.class, () -> IDN.toASCII(name), "empty label toASCII: " + name);
            checkThrows(IllegalArgumentException.class, () -> IDN.toASCII(name, IDN.USE_STD3_ASCII_RULES), "empty label toASCII STD3: " + name);
        }
    }

    static void testStd3Rules() {
        String[] illegal = {
            "www.example.com-", "-www.example.com", "-www.example.com-",
            "www.ex,mple.com", "www.ex{mple.com", "www.ex\u007Fmple.com"
        };
        for (String name : illegal) {
            checkThrows(IllegalArgumentException.class, () -> IDN.toASCII(name, IDN.USE_STD3_ASCII_RULES), "STD3 illegal: " + name);
        }
        String[] legal = {
            "www.ex-ample.com", "www.ex-mple.com", "www.exzmple.com",
            "www.exあmple.com", "www.あいう.com", "www.示例.com"
        };
        for (String name : legal) {
            String out = IDN.toASCII(name, IDN.USE_STD3_ASCII_RULES);
            check(out != null && out.length() > 0, "STD3 legal non-empty: " + name);
        }
    }

    static void testNullSemantics() {
        checkThrows(NullPointerException.class, () -> IDN.toASCII((String) null), "toASCII null");
        checkThrows(NullPointerException.class, () -> IDN.toASCII((String) null, 0), "toASCII(null, flags)");
        checkThrows(NullPointerException.class, () -> IDN.toUnicode((String) null), "toUnicode null");
        checkThrows(NullPointerException.class, () -> IDN.toUnicode((String) null, 0), "toUnicode(null, flags)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testFlags();
        testToAsciiUnicode();
        testTrailingDotAndRoot();
        testEmptyLabelValidation();
        testStd3Rules();
        testNullSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
