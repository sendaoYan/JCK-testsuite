/*
 * @test
 * @summary java.net.IDN conformance: white-box structure + black-box behavior.
 * @run main IDNTest
 */

import java.lang.reflect.*;
import java.net.*;

public class IDNTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowR { void run() throws Exception; }
    static void fails(Class<? extends Throwable> ex, String m, ThrowR r) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(m + " wrong exception " + t);
        }
        throw new AssertionError(m + " no exception");
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

    static void testBlackBox() throws Exception {
        // toASCII: punycode conversion
        check(IDN.toASCII("bücher.de").equals("xn--bcher-kva.de"), "toASCII umlaut");
        check(IDN.toASCII("faß.de").equals("fass.de"), "toASCII sharp-s");
        check(IDN.toASCII("example.com").equals("example.com"), "toASCII ascii passthrough");
        // toUnicode: inverse
        check(IDN.toUnicode("xn--bcher-kva.de").equals("bücher.de"), "toUnicode umlaut");
        check(IDN.toUnicode("example.com").equals("example.com"), "toUnicode ascii passthrough");
        // roundtrip
        check(IDN.toUnicode(IDN.toASCII("bücher.de")).equals("bücher.de"), "roundtrip");
        // flags: STD3 rejects spaces
        fails(IllegalArgumentException.class, "std3 space", () -> IDN.toASCII("a b", IDN.USE_STD3_ASCII_RULES));
        // labels: dotted multi-label
        check(IDN.toASCII("www.bücher.de").equals("www.xn--bcher-kva.de"), "multi-label toASCII");
        // unicode uppercase normalization
        check(IDN.toASCII("BÜCHER.de").equals("xn--bcher-kva.de"), "uppercase normalized");
        fails(NullPointerException.class, "toASCII null", () -> IDN.toASCII(null));
        fails(NullPointerException.class, "toUnicode null", () -> IDN.toUnicode(null));
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
