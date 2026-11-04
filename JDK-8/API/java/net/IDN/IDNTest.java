/*
 * @test
 * @summary java.net.IDN conformance: full contract coverage. toASCII/toUnicode
 *          round-trip of internationalized labels, ACE (xn--) form, ASCII
 *          pass-through, USE_STD3_ASCII_RULES (leading/trailing hyphen and
 *          non-LDH ASCII rejected), empty-label validation, trailing-dot/root
 *          labels, ALLOW_UNASSIGNED flag, and exact IAE/NPE paths.
 * @run main IDNTest
 */

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

    // ------------------------------------------------------------------
    // Deepening: exact ACE table, flag constants, label length boundary,
    // toUnicode never throws (probed identical on JDK-8 and JDK-27)
    // ------------------------------------------------------------------

    static void testFlagConstantValues() {
        check(IDN.ALLOW_UNASSIGNED == 1, "ALLOW_UNASSIGNED == 1");
        check(IDN.USE_STD3_ASCII_RULES == 2, "USE_STD3_ASCII_RULES == 2");
    }

    static void testExactAceTable() {
        // deterministic punycode encodings (probed on both JDKs)
        String[][] table = {
            {"例え.jp", "xn--r8jz45g.jp"},
            {"例え.テスト", "xn--r8jz45g.xn--zckzah"},
            {"bücher.de", "xn--bcher-kva.de"},
            {"日本語.jp", "xn--wgv71a119e.jp"},
            {"münchen.de", "xn--mnchen-3ya.de"},
        };
        for (String[] row : table) {
            check(row[1].equals(IDN.toASCII(row[0])), "toASCII(" + row[0] + ") exact");
            check(row[0].equals(IDN.toUnicode(row[1])), "toUnicode(" + row[1] + ") exact round trip");
        }
        // multi-label mixed forms
        check("www.xn--bcher-kva.de".equals(IDN.toASCII("www.bücher.de")), "prefixed label encoded");
        check("bücher.de.".equals(IDN.toUnicode("xn--bcher-kva.de.")), "trailing dot preserved toUnicode");
        check("xn--bcher-kva.de.".equals(IDN.toASCII("bücher.de.")), "trailing dot preserved toASCII");
    }

    static void testCaseHandling() {
        // case of ASCII characters is preserved by both directions (probed)
        check("example.COM".equals(IDN.toASCII("example.COM")), "toASCII upper TLD preserved");
        check("EXAMPLE.com".equals(IDN.toASCII("EXAMPLE.com")), "toASCII upper SLD preserved");
        check("example.COM".equals(IDN.toUnicode("example.COM")), "toUnicode case preserved");
        // already-ACE input passes through unchanged, even with mixed case
        check("xn--r8jz45g.jp".equals(IDN.toASCII("xn--r8jz45g.jp")), "ACE passthrough toASCII");
        check("xn--BCHER-kva.de".equals(IDN.toASCII("xn--BCHER-kva.de")), "mixed-case ACE passthrough");
        // mixed-case ACE decodes with case quirks (probed deterministic both JDKs)
        check("BüCHER.DE".equals(IDN.toUnicode("XN--BCHER-KVA.DE")), "uppercase ACE prefix decoded");
    }

    static void testNonLdhWithoutStd3() {
        // without USE_STD3_ASCII_RULES, spaces/underscores/punctuation pass through (probed)
        check("a b".equals(IDN.toASCII("a b", 0)), "space passthrough without STD3");
        check("a_b".equals(IDN.toASCII("a_b", 0)), "underscore passthrough without STD3");
        check("ex!ample.com".equals(IDN.toASCII("ex!ample.com", 0)), "exclamation passthrough");
        // with the flag they are rejected with exact reason (probed)
        try {
            IDN.toASCII("a b", IDN.USE_STD3_ASCII_RULES);
            check(false, "space with STD3 must throw");
        } catch (IllegalArgumentException e) {
            check("Contains non-LDH ASCII characters".equals(e.getMessage()), "space STD3 message exact");
        }
        checkThrows(IllegalArgumentException.class,
                () -> IDN.toASCII("a_b", IDN.USE_STD3_ASCII_RULES), "underscore STD3 rejected");
        checkThrows(IllegalArgumentException.class,
                () -> IDN.toASCII("ex!ample.com", IDN.USE_STD3_ASCII_RULES), "exclamation STD3 rejected");
        // toUnicode never validates: same inputs are returned unchanged (probed)
        check("a b".equals(IDN.toUnicode("a b", 0)), "toUnicode space passthrough");
        check("a b".equals(IDN.toUnicode("a b", IDN.USE_STD3_ASCII_RULES)),
                "toUnicode space passthrough even with STD3");
        check("a_b".equals(IDN.toUnicode("a_b", IDN.USE_STD3_ASCII_RULES)), "toUnicode underscore passthrough");
    }

    static void testLabelLengthBoundary() {
        StringBuilder b63 = new StringBuilder();
        for (int i = 0; i < 63; i++) b63.append('a');
        check((b63 + ".com").equals(IDN.toASCII(b63 + ".com")), "63-char label accepted");
        StringBuilder b64 = new StringBuilder();
        for (int i = 0; i < 64; i++) b64.append('a');
        try {
            IDN.toASCII(b64 + ".com");
            check(false, "64-char label must throw");
        } catch (IllegalArgumentException e) {
            check("The label in the input is too long".equals(e.getMessage()),
                    "label-too-long message exact");
        }
        // an encoded non-ASCII label must also stay within 63 chars
        String ace = IDN.toASCII("日本語.jp");
        check(ace.split("\\.")[0].length() <= 63, "encoded label length <= 63");
    }

    static void testToUnicodeNeverThrows() {
        // invalid ACE labels are returned unchanged, never throwing (probed)
        String[] passthrough = {
            "xn--a-ecp.ru", "xn--.jp", "xn---.jp", "xn--a", "xn--0.jp",
            "!!invalid!!.jp", "plain.example.com", "", ".",
            "bücher.de"
        };
        for (String s : passthrough) {
            String out = IDN.toUnicode(s);
            check(s.equals(out), "toUnicode(" + (s.isEmpty() ? "<empty>" : s) + ") unchanged");
        }
        // valid ACE still decodes under flags
        check("bücher.de".equals(IDN.toUnicode("xn--bcher-kva.de", IDN.USE_STD3_ASCII_RULES)),
                "flagged toUnicode decodes");
        // a single valid ACE label decodes too (no dot required)
        check("bücher".equals(IDN.toUnicode("xn--bcher-kva")), "single-label ACE decodes");
    }

    static void testProhibitedCodePoints() {
        // U+E000 private-use character is prohibited with and without ALLOW_UNASSIGNED (probed)
        String bad = "example.com";
        checkThrows(IllegalArgumentException.class, () -> IDN.toASCII(bad), "prohibited code point rejected");
        checkThrows(IllegalArgumentException.class,
                () -> IDN.toASCII(bad, IDN.ALLOW_UNASSIGNED), "ALLOW_UNASSIGNED does not permit U+E000");
        try {
            IDN.toASCII(bad);
            check(false, "must throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("A prohibited code point"),
                    "prohibited message contains reason");
        }
    }

    static void testExpandedAceTable() {
        // additional deterministic encodings across scripts (probed on both JDKs)
        String[][] table = {
            {"bücher.example", "xn--bcher-kva.example"},
            {"mitäsuomi.fi", "xn--mitsuomi-2za.fi"},
            {"레스토랑.kr", "xn--9i2bvi24zosp.kr"},
            {"испытание.рф", "xn--80akhbyknj4f.xn--p1ai"},
        };
        for (String[] row : table) {
            check(row[1].equals(IDN.toASCII(row[0])), "toASCII(" + row[0] + ") exact");
            check(row[0].equals(IDN.toUnicode(row[1])), "toUnicode(" + row[1] + ") exact");
            // with flags the same encodings are produced
            check(row[1].equals(IDN.toASCII(row[0], 0)), "toASCII(" + row[0] + ",0) identical");
        }
        // digits and hyphens inside labels survive
        check("xn--bcher1-3ya.de".equals(IDN.toASCII("bücher1.de")), "digit in unicode label");
        check("a-b.example".equals(IDN.toASCII("a-b.example", IDN.USE_STD3_ASCII_RULES)),
                "interior hyphen legal under STD3");
    }

    static void testStd3IllegalMatrix() {
        // every non-LDH ASCII character is rejected under STD3 (probed class of failure)
        char[] bad = {'!', '"', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '/', ':',
                      ';', '<', '=', '>', '?', '@', '[', '\\', ']', '^', '`', '{', '|', '}', '~'};
        for (char c : bad) {
            String s = "ex" + c + "ample.com";
            checkThrows(IllegalArgumentException.class,
                    () -> IDN.toASCII(s, IDN.USE_STD3_ASCII_RULES),
                    "STD3 rejects '" + c + "'");
        }
        // LDH characters are accepted under STD3
        String[] ok = {"a-z.example", "0-9.example", "A-Z.example", "a9-zA.example"};
        for (String s : ok) {
            String out = IDN.toASCII(s, IDN.USE_STD3_ASCII_RULES);
            check(s.equals(out), "STD3 accepts LDH " + s);
        }
    }

    static void testEmptyLabelMatrixExtended() {
        String[] illegal = {"a..b", "..", "a..", "..b", ".a.b.", "a.b.."};
        for (String s : illegal) {
            checkThrows(IllegalArgumentException.class, () -> IDN.toASCII(s), "empty label " + s);
            // toUnicode is lenient: empty labels pass through unchanged (probed)
            check(s.equals(IDN.toUnicode(s)), "toUnicode(" + s + ") passthrough");
        }
    }

    public static void main(String[] args) throws Exception {
        testFlags();
        testToAsciiUnicode();
        testTrailingDotAndRoot();
        testEmptyLabelValidation();
        testStd3Rules();
        testNullSemantics();
        testFlagConstantValues();
        testExactAceTable();
        testCaseHandling();
        testNonLdhWithoutStd3();
        testLabelLengthBoundary();
        testToUnicodeNeverThrows();
        testProhibitedCodePoints();
        testExpandedAceTable();
        testStd3IllegalMatrix();
        testEmptyLabelMatrixExtended();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
