/*
 * @test
 * @summary java.text.Normalizer API conformance: final utility class, no public
 *          constructors; NFC/NFD canonical compose/decompose round-trip, NFKC/NFKD
 *          compatibility mappings (ligature, fullwidth, superscript), isNormalized
 *          for each form, idempotence, empty/ascii boundaries, and precise
 *          NullPointerException on null CharSequence/Form.
 * @run main NormalizerTest
 */

import java.text.Normalizer;
import java.text.Normalizer.Form;

public class NormalizerTest {
    static int passed;

    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> expected, Runnable r, String m) {
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
        Class<?> c = Class.forName("java.text.Normalizer");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "final concrete class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getConstructors().length == 0, "no public constructors");
        check(c.getMethod("normalize", CharSequence.class, Form.class) != null,
              "normalize(CharSequence,Form)");
        check(c.getMethod("isNormalized", CharSequence.class, Form.class) != null,
              "isNormalized(CharSequence,Form)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testCanonicalComposition() {
        String composed = "é";            // é (single code point)
        String decomposed = "é";         // e + combining acute
        check(Normalizer.normalize(composed, Form.NFC).equals(composed), "NFC leaves composed e-acute");
        check(Normalizer.normalize(composed, Form.NFD).equals(decomposed), "NFD decomposes e-acute");
        check(Normalizer.normalize(decomposed, Form.NFC).equals(composed), "NFC composes e+0301");
        check(Normalizer.normalize(decomposed, Form.NFD).equals(decomposed), "NFD idempotent on decomposed");
    }

    static void testCompatibilityComposition() {
        String ligature = "ﬃ";            // ffi ligature
        check(Normalizer.normalize(ligature, Form.NFKC).equals("ffi"), "NFKC ligature -> ffi");
        check(Normalizer.normalize(ligature, Form.NFKD).equals("ffi"), "NFKD ligature -> ffi");
        check(Normalizer.normalize("Ａ", Form.NFKC).equals("A"), "NFKC fullwidth A -> A");
        check(Normalizer.normalize("²", Form.NFKC).equals("2"), "NFKC superscript 2 -> 2");
        check(Normalizer.normalize(ligature, Form.NFC).equals(ligature), "NFC does NOT compatibility-compose");
    }

    static void testIsNormalized() {
        String composed = "é";
        String decomposed = "é";
        check(Normalizer.isNormalized(composed, Form.NFC), "e-acute is NFC-normalized");
        check(!Normalizer.isNormalized(composed, Form.NFD), "e-acute is not NFD-normalized");
        check(!Normalizer.isNormalized(decomposed, Form.NFC), "e+0301 is not NFC-normalized");
        check(Normalizer.isNormalized(decomposed, Form.NFD), "e+0301 is NFD-normalized");
        check(Normalizer.isNormalized("ffi", Form.NFKC), "ffi is NFKC-normalized");
    }

    static void testIdempotentAndNull() {
        String s = "é";
        String nfc = Normalizer.normalize(s, Form.NFC);
        check(Normalizer.normalize(nfc, Form.NFC).equals(nfc), "NFC idempotent");

        String decomposed = "é";
        String nfd = Normalizer.normalize(decomposed, Form.NFD);
        check(Normalizer.normalize(nfd, Form.NFD).equals(nfd), "NFD idempotent");

        expectThrows(NullPointerException.class, () -> Normalizer.normalize(null, Form.NFC),
                     "normalize(null) NPE");
        expectThrows(NullPointerException.class, () -> Normalizer.isNormalized(null, Form.NFC),
                     "isNormalized(null) NPE");
        expectThrows(NullPointerException.class, () -> Normalizer.normalize("x", (Form) null),
                     "normalize(x,null) NPE");
        expectThrows(NullPointerException.class, () -> Normalizer.isNormalized("x", (Form) null),
                     "isNormalized(x,null) NPE");
    }

    static void testBoundaryValues() {
        check("".equals(Normalizer.normalize("", Form.NFC)), "normalize empty -> empty");
        check(Normalizer.isNormalized("", Form.NFC), "empty is NFC-normalized");
        check(Normalizer.isNormalized("", Form.NFD), "empty is NFD-normalized");
        check(Normalizer.isNormalized("", Form.NFKC), "empty is NFKC-normalized");
        check(Normalizer.isNormalized("", Form.NFKD), "empty is NFKD-normalized");

        String ascii = "abc123";
        check(Normalizer.isNormalized(ascii, Form.NFC), "ascii NFC-normalized");
        check(Normalizer.isNormalized(ascii, Form.NFD), "ascii NFD-normalized");
        check(Normalizer.isNormalized(ascii, Form.NFKC), "ascii NFKC-normalized");
        check(Normalizer.isNormalized(ascii, Form.NFKD), "ascii NFKD-normalized");

        check(Normalizer.normalize("ﬃ", Form.NFKD).equals("ffi"), "NFKD ligature -> ffi");
        check(Normalizer.normalize("ﬃ", Form.NFKC).equals("ffi"), "NFKC ligature -> ffi");
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
    // 11-sample table x 4 forms, byte-identical between JDK-8 (legacy ICU) and JDK-27 (CLDR/ICD)

    static void testFormTableContract() {
        String[][] table = {
            // {input, NFD, NFC, NFKD, NFKC}
            {"\u00E9", "e\u0301", "\u00E9", "e\u0301", "\u00E9"},
            {"e\u0301", "e\u0301", "\u00E9", "e\u0301", "\u00E9"},
            {"\u00BD", "\u00BD", "\u00BD", "1\u20442", "1\u20442"},
            {"\uFF21", "\uFF21", "\uFF21", "A", "A"},
            {"\uFB01", "\uFB01", "\uFB01", "fi", "fi"},
            {"\u2126", "\u03A9", "\u03A9", "\u03A9", "\u03A9"},
            {"\u304C", "\u304B\u3099", "\u304C", "\u304B\u3099", "\u304C"},
            {"\u304B\u3099", "\u304B\u3099", "\u304C", "\u304B\u3099", "\u304C"},
            {"A\u0300", "A\u0300", "\u00C0", "A\u0300", "\u00C0"},
            {"\u00C5", "A\u030A", "\u00C5", "A\u030A", "\u00C5"},
            {"A\u030A", "A\u030A", "\u00C5", "A\u030A", "\u00C5"},
        };
        Normalizer.Form[] forms = {Normalizer.Form.NFD, Normalizer.Form.NFC,
                Normalizer.Form.NFKD, Normalizer.Form.NFKC};
        for (String[] row : table) {
            String in = row[0];
            for (int f = 0; f < 4; f++) {
                String out = Normalizer.normalize(in, forms[f]);
                check(out.equals(row[f + 1]),
                      "normalize(" + escape(in) + "," + forms[f] + ") == " + escape(row[f + 1]));
                check(out.length() == row[f + 1].length(),
                      "length under " + forms[f] + " for " + escape(in));
                check(Normalizer.isNormalized(out, forms[f]),
                      "normalized output is idempotent under " + forms[f]);
                check(Normalizer.isNormalized(in, forms[f]) == in.equals(row[f + 1]),
                      "isNormalized consistent with normalize().equals() for " + escape(in));
            }
        }
    }

    static String escape(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) sb.append(String.format("\\u%04X", (int) c));
        return sb.toString();
    }

    static void testFormEnumContract() {
        Normalizer.Form[] vals = Normalizer.Form.values();
        check(vals.length == 4, "exactly 4 forms");
        check(vals[0] == Normalizer.Form.NFD && vals[0].ordinal() == 0, "NFD ordinal 0");
        check(vals[1] == Normalizer.Form.NFC && vals[1].ordinal() == 1, "NFC ordinal 1");
        check(vals[2] == Normalizer.Form.NFKD && vals[2].ordinal() == 2, "NFKD ordinal 2");
        check(vals[3] == Normalizer.Form.NFKC && vals[3].ordinal() == 3, "NFKC ordinal 3");
        check(Normalizer.Form.valueOf("NFC") == Normalizer.Form.NFC, "valueOf NFC");
        expectX(IllegalArgumentException.class, () -> Normalizer.Form.valueOf("bogus"),
                "valueOf(bogus) -> IAE");
        check("NFD".equals(Normalizer.Form.NFD.name()), "name NFD");
    }

    static void testCharSequenceOverloadContract() {
        StringBuilder sb = new StringBuilder("e\u0301");
        String nfc = Normalizer.normalize(sb, Normalizer.Form.NFC);
        check(nfc.length() == 1 && nfc.charAt(0) == '\u00E9',
              "normalize accepts any CharSequence (StringBuilder)");
        check(Normalizer.isNormalized(new StringBuilder("abc"), Normalizer.Form.NFC),
              "isNormalized accepts any CharSequence");
        check("".equals(Normalizer.normalize("", Normalizer.Form.NFKC)), "empty stays empty");
        check(Normalizer.isNormalized("", Normalizer.Form.NFD), "empty is normalized");
        check("abc".equals(Normalizer.normalize("abc", Normalizer.Form.NFKC)),
              "pure ASCII unchanged under all forms");
    }
    public static void main(String[] args) throws Exception {
        testFormTableContract();
        testFormEnumContract();
        testCharSequenceOverloadContract();
        testWhiteBox();
        testCanonicalComposition();
        testCompatibilityComposition();
        testIsNormalized();
        testIdempotentAndNull();
        testBoundaryValues();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
