/*
 * @test
 * @summary java.text.Normalizer conformance: white-box structure + black-box NFC/NFD/NFKC/NFKD behavior.
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

    static void expectThrows(Runnable r, Class<? extends Throwable> t, String m) {
        try {
            r.run();
        } catch (Throwable x) {
            if (t.isInstance(x)) { passed++; return; }
            throw new AssertionError(m + ": expected " + t.getSimpleName() + " but got " + x, x);
        }
        throw new AssertionError(m + ": expected " + t.getSimpleName());
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.text.Normalizer");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "final concrete class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getConstructors().length == 0, "no public constructors");
        check(c.getMethod("normalize", CharSequence.class, Form.class) != null, "normalize(CharSequence,Form)");
        check(c.getMethod("isNormalized", CharSequence.class, Form.class) != null, "isNormalized(CharSequence,Form)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testCanonicalComposition() {
        String composed = "é";   // é
        String decomposed = "é"; // e + combining acute
        check(Normalizer.normalize(composed, Form.NFC).equals(composed), "NFC leaves composed é");
        check(Normalizer.normalize(composed, Form.NFD).equals(decomposed), "NFD decomposes é -> e+0301");
        check(Normalizer.normalize(decomposed, Form.NFC).equals(composed), "NFC composes e+0301 -> é");
        check(Normalizer.normalize(decomposed, Form.NFD).equals(decomposed), "NFD idempotent on decomposed");
    }

    static void testCompatibilityComposition() {
        String ligature = "ﬃ";   // ﬃ ligature ffi
        check(Normalizer.normalize(ligature, Form.NFKC).equals("ffi"), "NFKC ligature -> ffi");
        check(Normalizer.normalize(ligature, Form.NFKD).equals("ffi"), "NFKD ligature -> ffi");
        check(Normalizer.normalize("Ａ", Form.NFKC).equals("A"), "NFKC fullwidth A -> A");
        check(Normalizer.normalize("²", Form.NFKC).equals("2"), "NFKC superscript 2 -> 2");
        check(Normalizer.normalize(ligature, Form.NFC).equals(ligature), "NFC does NOT compatibility-compose");
    }

    static void testIsNormalized() {
        String composed = "é";
        String decomposed = "é";
        check(Normalizer.isNormalized(composed, Form.NFC), "é is NFC-normalized");
        check(!Normalizer.isNormalized(composed, Form.NFD), "é is not NFD-normalized");
        check(!Normalizer.isNormalized(decomposed, Form.NFC), "e+0301 is not NFC-normalized");
        check(Normalizer.isNormalized(decomposed, Form.NFD), "e+0301 is NFD-normalized");
        check(Normalizer.isNormalized("ffi", Form.NFKC), "ffi is NFKC-normalized");
    }

    static void testIdempotentAndNull() {
        String s = "é";
        String nfc = Normalizer.normalize(s, Form.NFC);
        check(Normalizer.normalize(nfc, Form.NFC).equals(nfc), "NFC idempotent");
        expectThrows(() -> Normalizer.normalize(null, Form.NFC), NullPointerException.class, "normalize(null) NPE");
        expectThrows(() -> Normalizer.isNormalized(null, Form.NFC), NullPointerException.class, "isNormalized(null) NPE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testCanonicalComposition();
        testCompatibilityComposition();
        testIsNormalized();
        testIdempotentAndNull();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
