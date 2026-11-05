/*
 * @test
 * @summary javax.xml.crypto.KeySelector.Purpose conformance: key selector purpose constants.
 * @run main PurposeTest
 */

public class PurposeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.crypto.KeySelector$Purpose");
        // typesafe-enum pattern: plain class with singleton constants, NOT a java.lang.Enum
        check(!java.lang.Enum.class.isAssignableFrom(c), "not java.lang.Enum");
        check(c.getConstructors().length == 0, "no public constructors (singleton instances)");
        javax.xml.crypto.KeySelector.Purpose[] vals = new javax.xml.crypto.KeySelector.Purpose[] {
            javax.xml.crypto.KeySelector.Purpose.SIGN, javax.xml.crypto.KeySelector.Purpose.VERIFY,
            javax.xml.crypto.KeySelector.Purpose.ENCRYPT, javax.xml.crypto.KeySelector.Purpose.DECRYPT };
        check(vals.length == 4, "exactly four constants");
        check(vals[0] != null && vals[1] != null && vals[2] != null && vals[3] != null,
            "SIGN/VERIFY/ENCRYPT/DECRYPT present");
        // lowercase display names
        check(javax.xml.crypto.KeySelector.Purpose.SIGN.toString().equals("sign"), "SIGN toString sign");
        check(javax.xml.crypto.KeySelector.Purpose.VERIFY.toString().equals("verify"), "VERIFY toString verify");
        check(javax.xml.crypto.KeySelector.Purpose.ENCRYPT.toString().equals("encrypt"), "ENCRYPT toString encrypt");
        check(javax.xml.crypto.KeySelector.Purpose.DECRYPT.toString().equals("decrypt"), "DECRYPT toString decrypt");
        // distinctness: all pairwise-unequal singleton instances
        javax.xml.crypto.KeySelector.Purpose[] all = vals;
        for (int i = 0; i < all.length; i++)
            for (int j = i + 1; j < all.length; j++)
                check(all[i] != all[j], "distinct " + all[i] + " vs " + all[j]);
        check(javax.xml.crypto.KeySelector.Purpose.SIGN.equals(javax.xml.crypto.KeySelector.Purpose.SIGN), "self equals");
        check(!javax.xml.crypto.KeySelector.Purpose.SIGN.equals(javax.xml.crypto.KeySelector.Purpose.VERIFY), "cross unequal");
        check(javax.xml.crypto.KeySelector.Purpose.SIGN.hashCode() == javax.xml.crypto.KeySelector.Purpose.SIGN.hashCode(),
            "hashCode stable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
