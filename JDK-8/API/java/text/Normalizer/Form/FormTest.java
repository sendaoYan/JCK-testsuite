/*
 * @test
 * @summary java.text.Normalizer.Form conformance: enum constants/valueOf/toString.
 * @run main FormTest
 */

import java.text.Normalizer;

public class FormTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testForm() {
        Normalizer.Form[] forms = Normalizer.Form.values();
        check(forms.length == 4, "four forms");
        check(Normalizer.Form.valueOf("NFD") == Normalizer.Form.NFD, "valueOf NFD");
        check(Normalizer.Form.valueOf("NFC") == Normalizer.Form.NFC, "valueOf NFC");
        check(Normalizer.Form.valueOf("NFKD") == Normalizer.Form.NFKD, "valueOf NFKD");
        check(Normalizer.Form.valueOf("NFKC") == Normalizer.Form.NFKC, "valueOf NFKC");
        check("NFD".equals(Normalizer.Form.NFD.toString()), "toString NFD");
        check("NFC".equals(Normalizer.Form.NFC.toString()), "toString NFC");
        check(Normalizer.Form.NFD.ordinal() == 0, "NFD ordinal 0");
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

    static void testContractDeepening() {
        Normalizer.Form[] vals = Normalizer.Form.values();
        check(vals.length == 4, "exactly 4 constants");
        check(vals[0].ordinal() == 0 && "NFD".equals(vals[0].name()), "NFD first");
        check(vals[1].ordinal() == 1 && "NFC".equals(vals[1].name()), "NFC second");
        check(vals[2].ordinal() == 2 && "NFKD".equals(vals[2].name()), "NFKD third");
        check(vals[3].ordinal() == 3 && "NFKC".equals(vals[3].name()), "NFKC fourth");
        try {
            Normalizer.Form.valueOf("nfc");
            check(false, "valueOf is case-sensitive");
        } catch (IllegalArgumentException expected) { passed++; }
        check(Normalizer.isNormalized("abc", Normalizer.Form.valueOf("NFC")),
              "valueOf result usable in API");
        String composed = "\u00E9";
        check(!Normalizer.isNormalized(composed, Normalizer.Form.NFD)
              && Normalizer.isNormalized(Normalizer.normalize(composed, Normalizer.Form.NFD),
                      Normalizer.Form.NFD),
              "enum constants drive normalization semantics");
    }
    public static void main(String[] args) {
        testContractDeepening();
        testForm();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
