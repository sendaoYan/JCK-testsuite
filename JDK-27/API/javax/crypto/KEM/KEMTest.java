/*
 * @test
 * @summary javax.crypto.KEM conformance: white-box structure + black-box behavior.
 * @run main KEMTest
 */

import javax.crypto.*;
import java.lang.reflect.*;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.NamedParameterSpec;
import java.util.Arrays;

public class KEMTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.KEM");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass() == Object.class, "extends Object");
        // engine class: instantiation only through getInstance factory
        boolean publicCtor = false;
        for (Constructor<?> k : c.getDeclaredConstructors())
            if (Modifier.isPublic(k.getModifiers())) publicCtor = true;
        check(!publicCtor, "no public constructor");
        String[] expect = {"getInstance", "newEncapsulator", "newDecapsulator", "getAlgorithm"};
        for (String n : expect) {
            boolean found = false;
            for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) found = true;
            check(found, "method " + n);
        }
        int getInstanceOverloads = 0, newEncapsulatorOverloads = 0, newDecapsulatorOverloads = 0;
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("getInstance")) getInstanceOverloads++;
            if (m.getName().equals("newEncapsulator")) newEncapsulatorOverloads++;
            if (m.getName().equals("newDecapsulator")) newDecapsulatorOverloads++;
        }
        check(getInstanceOverloads == 3, "3 getInstance overloads");
        check(newEncapsulatorOverloads == 3, "3 newEncapsulator overloads");
        check(newDecapsulatorOverloads == 2, "2 newDecapsulator overloads");
        // nested value/engine types
        for (String nested : new String[]{"javax.crypto.KEM$Encapsulated",
                                          "javax.crypto.KEM$Encapsulator",
                                          "javax.crypto.KEM$Decapsulator"}) {
            Class<?> n = Class.forName(nested);
            check(Modifier.isPublic(n.getModifiers()) && Modifier.isFinal(n.getModifiers()),
                    nested + " public final");
            check(n.getDeclaringClass() == c, nested + " nested in KEM");
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // --- preserved original scenario: ML-KEM round trip ---
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("ML-KEM");
        KeyPair kp = kpg.generateKeyPair();
        KEM kem = KEM.getInstance("ML-KEM");
        check("ML-KEM".equals(kem.getAlgorithm()), "getAlgorithm");
        KEM.Encapsulator enc = kem.newEncapsulator(kp.getPublic());
        check(enc.secretSize() > 0 && enc.encapsulationSize() > 0, "sizes positive");
        KEM.Encapsulated e = enc.encapsulate();
        check(e.key() != null && e.encapsulation() != null, "encapsulated non-null");
        KEM.Decapsulator dec = kem.newDecapsulator(kp.getPrivate());
        SecretKey sk = dec.decapsulate(e.encapsulation());
        check(Arrays.equals(sk.getEncoded(), e.key().getEncoded()), "shared secret matches");

        // --- DHKEM over X25519 ---
        KeyPairGenerator xdg = KeyPairGenerator.getInstance("XDH");
        xdg.initialize(NamedParameterSpec.X25519);
        KeyPair xkp = xdg.generateKeyPair();
        KEM dh = KEM.getInstance("DHKEM");
        check("DHKEM".equals(dh.getAlgorithm()), "DHKEM getAlgorithm");
        KEM.Encapsulator xenc = dh.newEncapsulator(xkp.getPublic());
        KEM.Encapsulated x1 = xenc.encapsulate();
        check("Generic".equals(x1.key().getAlgorithm()) && "RAW".equals(x1.key().getFormat()),
                "X25519 secret key Generic/RAW");
        check(x1.key().getEncoded().length == 32 && xenc.secretSize() == 32, "X25519 secretSize 32");
        check(x1.encapsulation().length == 32 && xenc.encapsulationSize() == 32,
                "X25519 encapsulationSize 32");
        check(x1.params() == null, "X25519 params null");
        KEM.Decapsulator xdec = dh.newDecapsulator(xkp.getPrivate());
        SecretKey xs = xdec.decapsulate(x1.encapsulation());
        check(Arrays.equals(xs.getEncoded(), x1.key().getEncoded()), "X25519 roundtrip");
        // repeat decapsulation of same ciphertext is deterministic
        check(Arrays.equals(xdec.decapsulate(x1.encapsulation()).getEncoded(), xs.getEncoded()),
                "repeat decapsulate same ct");
        // a second encapsulation yields fresh ciphertext, each pair decapsulates to its own secret
        KEM.Encapsulated x2 = xenc.encapsulate();
        check(!Arrays.equals(x1.encapsulation(), x2.encapsulation()), "fresh ciphertext differs");
        check(!Arrays.equals(x1.key().getEncoded(), x2.key().getEncoded()), "fresh secret differs");
        check(Arrays.equals(xdec.decapsulate(x2.encapsulation()).getEncoded(), x2.key().getEncoded()),
                "second pair roundtrip");
        check(!Arrays.equals(xdec.decapsulate(x2.encapsulation()).getEncoded(),
                             xdec.decapsulate(x1.encapsulation()).getEncoded()),
                "secrets of different pairs differ");
        // malformed ciphertexts
        try {
            xdec.decapsulate(new byte[32]);
            check(false, "all-zero ct must fail");
        } catch (DecapsulateException ex) {
            check(true, "all-zero ct -> DecapsulateException");
        }
        try {
            xdec.decapsulate(new byte[31]);
            check(false, "short ct must fail");
        } catch (DecapsulateException ex) { check(true, "short ct -> DecapsulateException"); }
        try {
            xdec.decapsulate(new byte[33]);
            check(false, "long ct must fail");
        } catch (DecapsulateException ex) { check(true, "long ct -> DecapsulateException"); }

        // --- DHKEM over EC P-256 ---
        KeyPairGenerator ecg = KeyPairGenerator.getInstance("EC");
        ecg.initialize(new ECGenParameterSpec("secp256r1"));
        KeyPair eckp = ecg.generateKeyPair();
        KEM.Encapsulator eenc = dh.newEncapsulator(eckp.getPublic());
        KEM.Encapsulated ee = eenc.encapsulate();
        check(eenc.secretSize() == 32 && ee.key().getEncoded().length == 32, "P-256 secretSize 32");
        check(eenc.encapsulationSize() == 65 && ee.encapsulation().length == 65,
                "P-256 encapsulationSize 65");
        KEM.Decapsulator edec = dh.newDecapsulator(eckp.getPrivate());
        check(Arrays.equals(edec.decapsulate(ee.encapsulation()).getEncoded(),
                            ee.key().getEncoded()), "P-256 roundtrip");

        // --- provider selection overloads ---
        KEM byName = KEM.getInstance("DHKEM", "SunJCE");
        KEM byProv = KEM.getInstance("DHKEM", Security.getProvider("SunJCE"));
        check("DHKEM".equals(byName.getAlgorithm()) && "DHKEM".equals(byProv.getAlgorithm()),
                "provider variants algorithm");
        check("SunJCE".equals(byName.newEncapsulator(xkp.getPublic()).providerName()),
                "encapsulator providerName SunJCE");
        check("SunJCE".equals(byName.newDecapsulator(xkp.getPrivate()).providerName()),
                "decapsulator providerName SunJCE");
        // KEM object is immutable/reusable: same instance drives a second full round trip
        KEM.Encapsulated again = byProv.newEncapsulator(xkp.getPublic()).encapsulate();
        check(Arrays.equals(byProv.newDecapsulator(xkp.getPrivate())
                .decapsulate(again.encapsulation()).getEncoded(), again.key().getEncoded()),
                "same KEM reused for second roundtrip");

        // --- error paths ---
        try {
            KEM.getInstance("NoSuchKEM-xyz");
            check(false, "unknown algorithm must fail");
        } catch (NoSuchAlgorithmException ex) { check(true, "unknown -> NoSuchAlgorithmException"); }
        try {
            KEM.getInstance("DHKEM", "NoSuchProvider-xyz");
            check(false, "unknown provider must fail");
        } catch (NoSuchProviderException ex) { check(true, "unknown -> NoSuchProviderException"); }
        try {
            dh.newEncapsulator(null);
            check(false, "null public key must fail");
        } catch (InvalidKeyException ex) { check(true, "null public -> InvalidKeyException"); }
        try {
            dh.newDecapsulator(null);
            check(false, "null private key must fail");
        } catch (InvalidKeyException ex) { check(true, "null private -> InvalidKeyException"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
