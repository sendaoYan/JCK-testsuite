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
import java.security.spec.ECGenParameterSpec;
import java.security.spec.NamedParameterSpec;
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
        ecdhTwoParty();
        xdhLegacyAndAlias();
        factoryAndErrors();
    }

    // ECDH: two independent parties (P-256, both initialization styles) derive the same secret.
    static void ecdhTwoParty() throws Exception {
        KeyPairGenerator ecA = KeyPairGenerator.getInstance("EC");
        ecA.initialize(256);
        KeyPair kpA = ecA.generateKeyPair();
        KeyPairGenerator ecB = KeyPairGenerator.getInstance("EC");
        ecB.initialize(new ECGenParameterSpec("secp256r1"));
        KeyPair kpB = ecB.generateKeyPair();

        KeyAgreement kaA = KeyAgreement.getInstance("ECDH");
        check("ECDH".equals(kaA.getAlgorithm()), "getAlgorithm is ECDH");
        check(kaA.getProvider() != null && !kaA.getProvider().getName().isEmpty(), "provider attached");

        // state errors before initialization
        expect(IllegalStateException.class, () -> kaA.doPhase(kpB.getPublic(), true));
        expect(IllegalStateException.class, () -> kaA.generateSecret());
        expect(IllegalStateException.class, () -> kaA.generateSecret(new byte[64], 0));

        // initialized but no phase executed yet
        kaA.init(kpA.getPrivate());
        expect(IllegalStateException.class, () -> kaA.generateSecret());

        kaA.doPhase(kpB.getPublic(), true);
        byte[] secretA = kaA.generateSecret();
        check(secretA.length >= 1 && secretA.length <= 32,
                "ECDH secret length in [1,32] (leading zeros may shorten): " + secretA.length);

        // secret already consumed: another generateSecret without a new doPhase
        expect(IllegalStateException.class, () -> kaA.generateSecret());

        KeyAgreement kaB = KeyAgreement.getInstance("ECDH");
        kaB.init(kpB.getPrivate());
        kaB.doPhase(kpA.getPublic(), true);
        byte[] secretB = kaB.generateSecret();
        check(Arrays.equals(secretA, secretB), "both parties derive the identical secret");

        // generateSecret(byte[], offset) writes at the offset and returns the length
        KeyAgreement kaC = KeyAgreement.getInstance("ECDH");
        kaC.init(kpA.getPrivate(), new SecureRandom());
        kaC.doPhase(kpB.getPublic(), true);
        byte[] buf = new byte[48];
        java.util.Arrays.fill(buf, (byte) 0);
        int n = kaC.generateSecret(buf, 8);
        check(n == secretA.length, "generateSecret(byte[],int) returns secret length");
        check(Arrays.equals(Arrays.copyOfRange(buf, 8, 8 + n), secretA), "secret written at offset 8");
        check(buf[0] == 0 && buf[7] == 0 && buf[8 + n] == 0, "bytes outside [8,8+n) untouched");
        expect(ShortBufferException.class, () -> kaC.generateSecret(new byte[2], 0));

        // generateSecret(String): SunEC only accepts "Generic"/"TlsPremasterSecret"
        KeyAgreement kaD = KeyAgreement.getInstance("ECDH");
        kaD.init(kpA.getPrivate());
        kaD.doPhase(kpB.getPublic(), true);
        SecretKey sk = kaD.generateSecret("Generic");
        check("Generic".equals(sk.getAlgorithm()), "generateSecret(\"Generic\") key algorithm");
        check(Arrays.equals(sk.getEncoded(), secretA), "generateSecret(\"Generic\") key material equals raw secret");

        // unsupported secret algorithms (observed: even "AES" is rejected by SunEC)
        KeyAgreement kaE = KeyAgreement.getInstance("ECDH");
        kaE.init(kpA.getPrivate());
        kaE.doPhase(kpB.getPublic(), true);
        final KeyAgreement kaF = kaE;
        expect(NoSuchAlgorithmException.class, () -> kaF.generateSecret("NoSuchSecretAlg"));
        KeyAgreement kaG = KeyAgreement.getInstance("ECDH");
        kaG.init(kpA.getPrivate());
        kaG.doPhase(kpB.getPublic(), true);
        final KeyAgreement kaH = kaG;
        expect(NoSuchAlgorithmException.class, () -> kaH.generateSecret("AES"));

        // initializing an ECDH agreement with an XDH key is rejected
        KeyPair xk = KeyPairGenerator.getInstance("X25519").generateKeyPair();
        KeyAgreement kaI = KeyAgreement.getInstance("ECDH");
        expect(InvalidKeyException.class, () -> kaI.init(xk.getPrivate()));
    }

    // XDH: keep legacy single-party flow plus a full two-party X25519 agreement.
    static void xdhLegacyAndAlias() throws Exception {
        // legacy assertions (from previous shallow test)
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("X25519");
        KeyPair kp = kpg.generateKeyPair();
        KeyAgreement ka = KeyAgreement.getInstance("X25519");
        check("X25519".equals(ka.getAlgorithm()), "getAlgorithm");
        check(ka.getProvider() != null, "getProvider non-null");
        ka.init(kp.getPrivate());
        ka.doPhase(kp.getPublic(), true);
        check(ka.generateSecret().length == 32, "generateSecret length 32");

        // two-party agreement through the generic "XDH" name and NamedParameterSpec
        KeyPairGenerator xg = KeyPairGenerator.getInstance("XDH");
        xg.initialize(new NamedParameterSpec("X25519"));
        KeyPair xa = xg.generateKeyPair();
        xg.initialize(new NamedParameterSpec("X25519"));
        KeyPair xb = xg.generateKeyPair();
        KeyAgreement xka = KeyAgreement.getInstance("XDH");
        check("XDH".equals(xka.getAlgorithm()), "getAlgorithm is XDH");
        xka.init(xa.getPrivate());
        check(xka.doPhase(xb.getPublic(), true) == null, "doPhase returns null for X25519 last phase");
        byte[] xs1 = xka.generateSecret();
        KeyAgreement xkb = KeyAgreement.getInstance("XDH");
        xkb.init(xb.getPrivate());
        xkb.doPhase(xa.getPublic(), true);
        byte[] xs2 = xkb.generateSecret();
        check(xs1.length == 32 && xs2.length == 32, "X25519 secrets are exactly 32 bytes");
        check(Arrays.equals(xs1, xs2), "X25519 parties derive the identical secret");
    }

    // factory overloads and their error paths
    static void factoryAndErrors() throws Exception {
        expect(NoSuchAlgorithmException.class, () -> KeyAgreement.getInstance("NoSuchKAAlgorithm"));
        expect(NoSuchProviderException.class, () -> KeyAgreement.getInstance("ECDH", "NoSuchProviderXYZ"));
        expect(NoSuchAlgorithmException.class,
                () -> KeyAgreement.getInstance("NoSuchKAAlgorithm", "SunJCE"));
        expect(NoSuchAlgorithmException.class,
                () -> KeyAgreement.getInstance("ECDH", java.security.Security.getProvider("SunJCE")));
        KeyAgreement kaP = KeyAgreement.getInstance("ECDH", "SunEC");
        check("SunEC".equals(kaP.getProvider().getName()), "getInstance(String,String) selects named provider");
        check("ECDH".equals(kaP.getAlgorithm()), "getInstance(String,String) keeps algorithm name");
        KeyAgreement kaQ = KeyAgreement.getInstance("ECDH", java.security.Security.getProvider("SunEC"));
        check("SunEC".equals(kaQ.getProvider().getName()), "getInstance(String,Provider) uses given provider");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
