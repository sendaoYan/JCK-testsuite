/*
 * @test
 * @summary javax.crypto.KEMSpi conformance: white-box structure + black-box behavior.
 * @run main KEMSpiTest
 */

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.*;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

public class KEMSpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.KEMSpi");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        // preserved: engine methods exist
        checkMethods(c, new String[]{"engineNewEncapsulator", "engineNewDecapsulator"});
        // signatures: abstract engine methods with provider-style throws clauses
        Method en = c.getDeclaredMethod("engineNewEncapsulator", PublicKey.class,
                AlgorithmParameterSpec.class, SecureRandom.class);
        check(Modifier.isAbstract(en.getModifiers()), "engineNewEncapsulator abstract");
        check(en.getReturnType().getName().equals("javax.crypto.KEMSpi$EncapsulatorSpi"),
                "engineNewEncapsulator returns EncapsulatorSpi");
        Method ed = c.getDeclaredMethod("engineNewDecapsulator", PrivateKey.class,
                AlgorithmParameterSpec.class);
        check(Modifier.isAbstract(ed.getModifiers()), "engineNewDecapsulator abstract");
        check(ed.getReturnType().getName().equals("javax.crypto.KEMSpi$DecapsulatorSpi"),
                "engineNewDecapsulator returns DecapsulatorSpi");
        boolean iape = false, ike = false;
        for (Class<?> t : en.getExceptionTypes()) {
            if (t.getName().equals("java.security.InvalidAlgorithmParameterException")) iape = true;
            if (t.getName().equals("java.security.InvalidKeyException")) ike = true;
        }
        check(iape && ike, "engineNewEncapsulator declares IAPE/IKE");
        // nested SPI types
        Class<?> esi = Class.forName("javax.crypto.KEMSpi$EncapsulatorSpi");
        Class<?> dsi = Class.forName("javax.crypto.KEMSpi$DecapsulatorSpi");
        check(esi.isInterface() && esi.getDeclaringClass() == c, "nested EncapsulatorSpi interface");
        check(dsi.isInterface() && dsi.getDeclaringClass() == c, "nested DecapsulatorSpi interface");
    }

    static void checkMethods(Class<?> c, String[] ns) {
        for (String n : ns) {
            boolean found = false;
            for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) found = true;
            check(found, "method " + n);
        }
    }

    // ---- hand-written provider used for the black-box part ----
    static final byte[] FIXED_CT = {10, 11, 12, 13, 14, 15, 16, 17};
    static final byte[] FIXED_SECRET = {21, 22, 23, 24, 25, 26, 27, 28};

    static class FixedEncapsulatorSpi implements KEMSpi.EncapsulatorSpi {
        int calls;
        int lastFrom, lastTo; String lastAlg;
        public KEM.Encapsulated engineEncapsulate(int from, int to, String algorithm) {
            calls++; lastFrom = from; lastTo = to; lastAlg = algorithm;
            return new KEM.Encapsulated(new SecretKeySpec(FIXED_SECRET, "Generic"),
                    FIXED_CT.clone(), null);
        }
        public int engineSecretSize() { return FIXED_SECRET.length; }
        public int engineEncapsulationSize() { return FIXED_CT.length; }
    }

    static class FixedDecapsulatorSpi implements KEMSpi.DecapsulatorSpi {
        int calls;
        public javax.crypto.SecretKey engineDecapsulate(byte[] encapsulation,
                int from, int to, String algorithm) throws DecapsulateException {
            calls++;
            if (encapsulation.length == 0)
                throw new DecapsulateException("synthetic decapsulation failure");
            return new SecretKeySpec(FIXED_SECRET, algorithm);
        }
        public int engineSecretSize() { return FIXED_SECRET.length; }
        public int engineEncapsulationSize() { return FIXED_CT.length; }
    }

    static class FixedKEMSpi implements KEMSpi {
        int encCalls, decCalls;
        PublicKey seenPub; PrivateKey seenPriv; SecureRandom seenRnd;
        public KEMSpi.EncapsulatorSpi engineNewEncapsulator(PublicKey publicKey,
                AlgorithmParameterSpec spec, SecureRandom secureRandom)
                throws InvalidAlgorithmParameterException, InvalidKeyException {
            encCalls++; seenPub = publicKey; seenRnd = secureRandom;
            return new FixedEncapsulatorSpi();
        }
        public KEMSpi.DecapsulatorSpi engineNewDecapsulator(PrivateKey privateKey,
                AlgorithmParameterSpec spec)
                throws InvalidAlgorithmParameterException, InvalidKeyException {
            decCalls++; seenPriv = privateKey;
            return new FixedDecapsulatorSpi();
        }
    }

    static class ThrowingKEMSpi implements KEMSpi {
        public KEMSpi.EncapsulatorSpi engineNewEncapsulator(PublicKey publicKey,
                AlgorithmParameterSpec spec, SecureRandom secureRandom)
                throws InvalidKeyException {
            throw new InvalidKeyException("unsupported public key");
        }
        public KEMSpi.DecapsulatorSpi engineNewDecapsulator(PrivateKey privateKey,
                AlgorithmParameterSpec spec)
                throws InvalidAlgorithmParameterException {
            throw new InvalidAlgorithmParameterException("unsupported parameters");
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        KeyPairGenerator xdg = KeyPairGenerator.getInstance("XDH");
        xdg.initialize(java.security.spec.NamedParameterSpec.X25519);
        KeyPair kp = xdg.generateKeyPair();

        // dispatch through the interface type (as a Provider would)
        KEMSpi spi = new FixedKEMSpi();
        SecureRandom rnd = new SecureRandom();
        KEMSpi.EncapsulatorSpi esp = spi.engineNewEncapsulator(kp.getPublic(), null, rnd);
        KEMSpi.DecapsulatorSpi dsp = spi.engineNewDecapsulator(kp.getPrivate(), null);
        check(esp instanceof KEMSpi.EncapsulatorSpi && dsp instanceof KEMSpi.DecapsulatorSpi,
                "factory methods return SPI implementations");
        check(((FixedKEMSpi) spi).encCalls == 1 && ((FixedKEMSpi) spi).decCalls == 1,
                "each engine factory dispatched once");
        check(((FixedKEMSpi) spi).seenPub == kp.getPublic()
                && ((FixedKEMSpi) spi).seenPriv == kp.getPrivate()
                && ((FixedKEMSpi) spi).seenRnd == rnd, "arguments passed through");

        // the produced EncapsulatorSpi honors the KEM.Encapsulated contract
        KEM.Encapsulated out = esp.engineEncapsulate(0, 8, "Generic");
        check(Arrays.equals(out.key().getEncoded(), FIXED_SECRET)
                && Arrays.equals(out.encapsulation(), FIXED_CT), "engineEncapsulate fixed values");
        check(esp.engineSecretSize() == 8 && esp.engineEncapsulationSize() == 8,
                "Spi size queries");
        FixedEncapsulatorSpi f = (FixedEncapsulatorSpi) esp;
        check(f.calls == 1 && f.lastFrom == 0 && f.lastTo == 8 && "Generic".equals(f.lastAlg),
                "engineEncapsulate receives from/to/algorithm");

        // the produced DecapsulatorSpi returns keys and reports failures
        javax.crypto.SecretKey recovered = dsp.engineDecapsulate(FIXED_CT.clone(), 0, 8, "Generic");
        check(Arrays.equals(recovered.getEncoded(), FIXED_SECRET)
                && "Generic".equals(recovered.getAlgorithm()), "engineDecapsulate fixed value");
        try {
            dsp.engineDecapsulate(new byte[0], 0, 8, "Generic");
            check(false, "failure path must throw");
        } catch (DecapsulateException ex) {
            check("synthetic decapsulation failure".equals(ex.getMessage()),
                    "DecapsulateException propagates from Spi");
        }
        check(((FixedDecapsulatorSpi) dsp).calls == 2, "decapsulator dispatched twice");

        // exceptions declared by the interface propagate unchanged
        KEMSpi bad = new ThrowingKEMSpi();
        try {
            bad.engineNewEncapsulator(kp.getPublic(), null, null);
            check(false, "InvalidKeyException must propagate");
        } catch (InvalidKeyException ex) {
            check("unsupported public key".equals(ex.getMessage()), "IKE propagates");
        }
        try {
            bad.engineNewDecapsulator(kp.getPrivate(), null);
            check(false, "InvalidAlgorithmParameterException must propagate");
        } catch (InvalidAlgorithmParameterException ex) {
            check("unsupported parameters".equals(ex.getMessage()), "IAPE propagates");
        }

        // the real SunJCE DHKEM is also driven through KEMSpi internally:
        // its encapsulator/decapsulator honor the same paired-secret contract
        KEM kem = KEM.getInstance("DHKEM");
        KEM.Encapsulated real = kem.newEncapsulator(kp.getPublic()).encapsulate();
        check(Arrays.equals(kem.newDecapsulator(kp.getPrivate())
                .decapsulate(real.encapsulation()).getEncoded(), real.key().getEncoded()),
                "built-in DHKEM honors KEMSpi contract");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
