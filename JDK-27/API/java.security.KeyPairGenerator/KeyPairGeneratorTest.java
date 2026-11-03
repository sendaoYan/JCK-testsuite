/*
 * @test
 * @summary java.security.KeyPairGenerator conformance: white-box structure + black-box behavior.
 * @run main KeyPairGeneratorTest
 */

import java.lang.reflect.Modifier;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;

public class KeyPairGeneratorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.KeyPairGenerator");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.security.KeyPairGeneratorSpi"), "extends KeyPairGeneratorSpi");
        checkMethods(c, new String[]{"getAlgorithm", "getInstance", "getProvider",
            "initialize", "genKeyPair", "generateKeyPair"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // RSA
        KeyPairGenerator rsa = KeyPairGenerator.getInstance("RSA");
        check("RSA".equals(rsa.getAlgorithm()), "RSA getAlgorithm");
        check(rsa.getProvider() != null, "RSA getProvider non-null");
        rsa.initialize(2048);
        KeyPair rsaPair = rsa.generateKeyPair();
        PublicKey rsaPub = rsaPair.getPublic();
        PrivateKey rsaPriv = rsaPair.getPrivate();
        check(rsaPub != null && rsaPriv != null, "RSA keys non-null");
        check("RSA".equals(rsaPub.getAlgorithm()), "RSA public algorithm");
        check("RSA".equals(rsaPriv.getAlgorithm()), "RSA private algorithm");
        check(rsaPub.getFormat() != null, "RSA public format non-null");
        check(rsaPriv.getFormat() != null, "RSA private format non-null");
        check(rsaPub.getEncoded() != null && rsaPub.getEncoded().length > 0, "RSA public encoded non-empty");
        check(rsaPriv.getEncoded() != null && rsaPriv.getEncoded().length > 0, "RSA private encoded non-empty");

        // genKeyPair() is the final delegate of generateKeyPair()
        KeyPair rsaPair2 = rsa.genKeyPair();
        check(rsaPair2.getPublic() != null && rsaPair2.getPrivate() != null, "genKeyPair() keys non-null");

        // EC
        KeyPairGenerator ec = KeyPairGenerator.getInstance("EC");
        check("EC".equals(ec.getAlgorithm()), "EC getAlgorithm");
        ec.initialize(new ECGenParameterSpec("secp256r1"));
        KeyPair ecPair = ec.generateKeyPair();
        check(ecPair.getPublic() != null && ecPair.getPrivate() != null, "EC keys non-null");
        check("EC".equals(ecPair.getPublic().getAlgorithm()), "EC public algorithm");
        check(ecPair.getPublic().getEncoded() != null && ecPair.getPublic().getEncoded().length > 0, "EC public encoded non-empty");

        // getInstance(alg, provider)
        KeyPairGenerator rsaViaProvider = KeyPairGenerator.getInstance("RSA", "SunRsaSign");
        check("RSA".equals(rsaViaProvider.getAlgorithm()), "getInstance(alg,provider)");
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static void checkMethods(Class<?> c, String[] names) {
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
