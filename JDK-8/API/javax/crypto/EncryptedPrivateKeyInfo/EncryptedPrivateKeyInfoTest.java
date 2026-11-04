/*
 * @test
 * @summary javax.crypto.EncryptedPrivateKeyInfo conformance: constructors and getters.
 * @run main EncryptedPrivateKeyInfoTest
 */

public class EncryptedPrivateKeyInfoTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        byte[] enc = new byte[]{1,2,3,4};
        javax.crypto.EncryptedPrivateKeyInfo e = new javax.crypto.EncryptedPrivateKeyInfo("PBEWithMD5AndDES", enc);
        check("PBEWithMD5AndDES".equals(e.getAlgName()), "getAlgName");
        check(java.util.Arrays.equals(enc, e.getEncryptedData()), "getEncryptedData");
        byte[] der = e.getEncoded();
        javax.crypto.EncryptedPrivateKeyInfo e2 = new javax.crypto.EncryptedPrivateKeyInfo(der);
        check("PBEWithMD5AndDES".equals(e2.getAlgName()), "byte[] ctor getAlgName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
