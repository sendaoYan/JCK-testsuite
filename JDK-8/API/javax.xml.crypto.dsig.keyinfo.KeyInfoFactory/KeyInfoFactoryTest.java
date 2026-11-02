/*
 * @test
 * @summary javax.xml.crypto.dsig.keyinfo.KeyInfoFactory conformance: key info factory.
 * @run main KeyInfoFactoryTest
 */


public class KeyInfoFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        javax.xml.crypto.dsig.keyinfo.KeyInfoFactory kif = javax.xml.crypto.dsig.keyinfo.KeyInfoFactory.getInstance();
        check(kif != null, "factory non-null");
        check(kif.getMechanismType() != null, "mechanism type");
        javax.xml.crypto.dsig.keyinfo.KeyName kn = kif.newKeyName("test-key");
        check("test-key".equals(kn.getName()), "key name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
