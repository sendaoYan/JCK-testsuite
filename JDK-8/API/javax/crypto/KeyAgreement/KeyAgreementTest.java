/*
 * @test
 * @summary javax.crypto.KeyAgreement conformance: white-box structure + black-box behavior.
 * @run main KeyAgreementTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;

public class KeyAgreementTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    interface Thunk { void run() throws Exception; }
    static void expect(Class<? extends Throwable> t, Thunk r) {
        try {
            r.run();
        } catch (Throwable e) {
            if (t.isInstance(e)) { passed++; return; }
            throw new AssertionError("expected " + t.getName() + " but got " + e, e);
        }
        throw new AssertionError("expected " + t.getName() + " but nothing was thrown");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.KeyAgreement");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "direct superclass java.lang.Object");
        boolean protCtor = false;
        for (Constructor<?> k : c.getDeclaredConstructors()) {
            Class<?>[] ps = k.getParameterTypes();
            if (Modifier.isProtected(k.getModifiers()) && ps.length == 3
                    && ps[0].getName().equals("javax.crypto.KeyAgreementSpi")
                    && ps[1].getName().equals("java.security.Provider")
                    && ps[2] == String.class) {
                protCtor = true;
            }
        }
        check(protCtor, "protected (KeyAgreementSpi, Provider, String) constructor");
        String[] names = {"getAlgorithm", "getInstance", "getProvider", "init", "doPhase", "generateSecret"};
        for (String n : names) {
            boolean found = false;
            for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) found = true;
            check(found, "declares method " + n);
        }
        boolean finalGetAlg = false;
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals("getAlgorithm") && Modifier.isFinal(m.getModifiers())) finalGetAlg = true;
        check(finalGetAlg, "getAlgorithm is final");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        diffieHellmanTwoParty();
        ecdhTwoParty();
        factoryAndErrors();
    }

    // DH: two independent parties derive the same shared secret; state machine fully exercised.
    static void diffieHellmanTwoParty() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DH");
        kpg.initialize(1024);
        KeyPair kpA = kpg.generateKeyPair();
        KeyPair kpB = kpg.generateKeyPair();

        KeyAgreement kaA = KeyAgreement.getInstance("DiffieHellman");
        check("DiffieHellman".equals(kaA.getAlgorithm()), "getAlgorithm DiffieHellman");
        check(kaA.getProvider() != null && !kaA.getProvider().getName().isEmpty(), "provider attached");

        // state errors before initialization (JDK-8: generateSecret() dereferences the
        // not-yet-set engine and reports NullPointerException; the byte[] overload checks
        // state first and reports IllegalStateException)
        expect(IllegalStateException.class, () -> kaA.doPhase(kpB.getPublic(), true));
        expect(NullPointerException.class, () -> kaA.generateSecret());
        expect(IllegalStateException.class, () -> kaA.generateSecret(new byte[128], 0));

        // initialized but no phase executed yet
        kaA.init(kpA.getPrivate());
        expect(IllegalStateException.class, () -> kaA.generateSecret());

        kaA.doPhase(kpB.getPublic(), true);
        byte[] secretA = kaA.generateSecret();
        check(secretA.length > 0, "DH secret non-empty");

        // secret already consumed: another generateSecret without a new doPhase fails
        expect(IllegalStateException.class, () -> kaA.generateSecret());

        KeyAgreement kaB = KeyAgreement.getInstance("DiffieHellman");
        kaB.init(kpB.getPrivate());
        kaB.doPhase(kpA.getPublic(), true);
        byte[] secretB = kaB.generateSecret();
        check(Arrays.equals(secretA, secretB), "both DH parties derive the identical secret");

        // generateSecret(byte[], offset) writes at the offset and returns the length
        KeyAgreement kaC = KeyAgreement.getInstance("DiffieHellman");
        kaC.init(kpA.getPrivate(), new SecureRandom());
        kaC.doPhase(kpB.getPublic(), true);
        byte[] buf = new byte[secretA.length + 16];
        Arrays.fill(buf, (byte) 0);
        int n = kaC.generateSecret(buf, 8);
        check(n == secretA.length, "generateSecret(byte[],int) returns secret length");
        check(Arrays.equals(Arrays.copyOfRange(buf, 8, 8 + n), secretA), "secret written at offset 8");
        check(buf[0] == 0 && buf[7] == 0, "bytes before offset untouched");
        KeyAgreement kaShort = KeyAgreement.getInstance("DiffieHellman");
        kaShort.init(kpA.getPrivate());
        kaShort.doPhase(kpB.getPublic(), true);
        expect(ShortBufferException.class, () -> kaShort.generateSecret(new byte[2], 0));

        // generateSecret(String): SunJCE DH rejects unknown algorithms deterministically
        KeyAgreement kaD = KeyAgreement.getInstance("DiffieHellman");
        kaD.init(kpA.getPrivate());
        kaD.doPhase(kpB.getPublic(), true);
        final KeyAgreement kaDf = kaD;
        expect(NoSuchAlgorithmException.class, () -> kaDf.generateSecret("NoSuchSecretAlg"));
        expect(NoSuchAlgorithmException.class, () -> kaDf.generateSecret("AES"));
    }

    // ECDH: two parties on P-256 derive the same secret; no generateSecret(String) besides
    // the deterministic rejection of unsupported names (JDK-8 SunEC only supports TlsPremasterSecret).
    static void ecdhTwoParty() throws Exception {
        KeyPairGenerator ecA = KeyPairGenerator.getInstance("EC");
        ecA.initialize(256);
        KeyPair kpA = ecA.generateKeyPair();
        KeyPairGenerator ecB = KeyPairGenerator.getInstance("EC");
        ecB.initialize(256);
        KeyPair kpB = ecB.generateKeyPair();

        KeyAgreement kaA = KeyAgreement.getInstance("ECDH");
        check("ECDH".equals(kaA.getAlgorithm()), "getAlgorithm is ECDH");
        kaA.init(kpA.getPrivate());
        kaA.doPhase(kpB.getPublic(), true);
        byte[] secretA = kaA.generateSecret();
        check(secretA.length >= 1 && secretA.length <= 32,
                "ECDH secret length in [1,32] (leading zeros may shorten): " + secretA.length);

        KeyAgreement kaB = KeyAgreement.getInstance("ECDH");
        kaB.init(kpB.getPrivate());
        kaB.doPhase(kpA.getPublic(), true);
        byte[] secretB = kaB.generateSecret();
        check(Arrays.equals(secretA, secretB), "both ECDH parties derive the identical secret");

        // generateSecret(byte[],int) writes at the offset
        KeyAgreement kaC = KeyAgreement.getInstance("ECDH");
        kaC.init(kpA.getPrivate(), new SecureRandom());
        kaC.doPhase(kpB.getPublic(), true);
        byte[] buf = new byte[secretA.length + 8];
        Arrays.fill(buf, (byte) 0);
        int n = kaC.generateSecret(buf, 4);
        check(n == secretA.length && Arrays.equals(Arrays.copyOfRange(buf, 4, 4 + n), secretA),
                "generateSecret(byte[],int) writes ECDH secret at offset");
        KeyAgreement kaShort = KeyAgreement.getInstance("ECDH");
        kaShort.init(kpA.getPrivate());
        kaShort.doPhase(kpB.getPublic(), true);
        expect(ShortBufferException.class, () -> kaShort.generateSecret(new byte[2], 0));

        // JDK-8 SunEC rejects all generateSecret(String) names except TlsPremasterSecret
        KeyAgreement kaD = KeyAgreement.getInstance("ECDH");
        kaD.init(kpA.getPrivate());
        kaD.doPhase(kpB.getPublic(), true);
        final KeyAgreement kaDf = kaD;
        expect(NoSuchAlgorithmException.class, () -> kaDf.generateSecret("Generic"));
        expect(NoSuchAlgorithmException.class, () -> kaDf.generateSecret("AES"));
    }

    // factory overloads and their error paths
    static void factoryAndErrors() throws Exception {
        expect(NoSuchAlgorithmException.class, () -> KeyAgreement.getInstance("NoSuchKAAlgorithm"));
        expect(NoSuchProviderException.class, () -> KeyAgreement.getInstance("DiffieHellman", "NoSuchProviderXYZ"));
        expect(NoSuchAlgorithmException.class,
                () -> KeyAgreement.getInstance("NoSuchKAAlgorithm", "SunJCE"));
        expect(NoSuchAlgorithmException.class,
                () -> KeyAgreement.getInstance("ECDH", java.security.Security.getProvider("SunJCE")));
        KeyAgreement kaP = KeyAgreement.getInstance("DiffieHellman", "SunJCE");
        check("SunJCE".equals(kaP.getProvider().getName()), "getInstance(String,String) selects named provider");
        check("DiffieHellman".equals(kaP.getAlgorithm()), "getInstance(String,String) keeps algorithm name");
        KeyAgreement kaQ = KeyAgreement.getInstance("ECDH", java.security.Security.getProvider("SunEC"));
        check("SunEC".equals(kaQ.getProvider().getName()), "getInstance(String,Provider) uses given provider");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
