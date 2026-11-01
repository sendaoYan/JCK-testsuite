/*
 * @test
 * @summary java.text.Normalizer conformance: NFC/NFD/NFKC/NFKD and isNormalized.
 * @run main NormalizerTest
 */

import java.text.Normalizer;

public class NormalizerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testNormalizer() {
        String cafe = "café"; // c a f e-acute (4 code points)
        String nfc = Normalizer.normalize(cafe, Normalizer.Form.NFC);
        check(cafe.equals(nfc), "NFC leaves composed unchanged");
        String nfd = Normalizer.normalize(cafe, Normalizer.Form.NFD);
        check(nfd.length() == 5, "NFD decomposes e-acute");
        check("café".equals(nfd), "NFD result is c a f e + combining acute");

        check(Normalizer.isNormalized(nfc, Normalizer.Form.NFC), "isNormalized NFC");
        check(Normalizer.isNormalized(nfd, Normalizer.Form.NFD), "isNormalized NFD");
        check(!Normalizer.isNormalized(nfd, Normalizer.Form.NFC), "NFD not NFC-normalized");
        check(cafe.equals(Normalizer.normalize(nfd, Normalizer.Form.NFC)), "NFD -> NFC round-trip");

        String half = "½"; // 1/2 fraction
        String nfkc = Normalizer.normalize(half, Normalizer.Form.NFKC);
        check("1⁄2".equals(nfkc), "NFKC maps 1/2 fraction to 1/2");
        String nfkd = Normalizer.normalize(half, Normalizer.Form.NFKD);
        check("1⁄2".equals(nfkd), "NFKD maps 1/2 fraction to 1/2");
    }

    public static void main(String[] args) {
        testNormalizer();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
