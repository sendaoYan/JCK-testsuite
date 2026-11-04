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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testCanonicalComposition();
        testCompatibilityComposition();
        testIsNormalized();
        testIdempotentAndNull();
        testBoundaryValues();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
