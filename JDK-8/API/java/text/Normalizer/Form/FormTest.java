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

    public static void main(String[] args) {
        testForm();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
