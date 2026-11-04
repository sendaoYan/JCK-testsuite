/*
 * @test
 * @summary javax.crypto.KEMSpi.EncapsulatorSpi conformance: white-box structure + black-box behavior.
 * @run main EncapsulatorSpiTest
 */

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.*;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

public class EncapsulatorSpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.KEMSpi$EncapsulatorSpi");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getDeclaringClass().getName().equals("javax.crypto.KEMSpi"),
                "nested in javax.crypto.KEMSpi");
        // preserved: engine method names exist
        checkMethods(c, new String[]{"engineEncapsulate", "engineSecretSize",
                                     "engineEncapsulationSize"});
        // precise signatures
        Method enc = c.getDeclaredMethod("engineEncapsulate", int.class, int.class, String.class);
        check(Modifier.isAbstract(enc.getModifiers()), "engineEncapsulate abstract");
        check(enc.getReturnType().getName().equals("javax.crypto.KEM$Encapsulated"),
                "engineEncapsulate returns KEM$Encapsulated");
        check(enc.getExceptionTypes().length == 0, "engineEncapsulate declares no checked exceptions");
        Method ss = c.getDeclaredMethod("engineSecretSize");
        check(Modifier.isAbstract(ss.getModifiers()) && ss.getReturnType() == int.class,
                "engineSecretSize abstract int");
        Method es = c.getDeclaredMethod("engineEncapsulationSize");
        check(Modifier.isAbstract(es.getModifiers()) && es.getReturnType() == int.class,
                "engineEncapsulationSize abstract int");
        check(c.getDeclaredMethods().length == 3, "exactly 3 abstract methods");
    }

    static void checkMethods(Class<?> c, String[] ns) {
        for (String n : ns) {
            boolean found = false;
            for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) found = true;
            check(found, "method " + n);
        }
    }

    // ---- two independent hand-written implementations ----
    static class FixedEnc32 implements KEMSpi.EncapsulatorSpi {
        int calls; int from, to; String alg;
        final byte[] secret = new byte[32];
        final byte[] ct = new byte[32];
        { for (int i = 0; i < 32; i++) { secret[i] = (byte) i; ct[i] = (byte) (100 + i); } }
        public KEM.Encapsulated engineEncapsulate(int f, int t, String a) {
            calls++; from = f; to = t; alg = a;
            return new KEM.Encapsulated(new SecretKeySpec(secret, a), ct.clone(), null);
        }
        public int engineSecretSize() { return 32; }
        public int engineEncapsulationSize() { return 32; }
    }

    static class FixedEnc16 implements KEMSpi.EncapsulatorSpi {
        int calls;
        final byte[] secret = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
        public KEM.Encapsulated engineEncapsulate(int f, int t, String a) {
            calls++;
            return new KEM.Encapsulated(new SecretKeySpec(secret, a),
                    new byte[]{9, 8, 7}, new byte[]{1});
        }
        public int engineSecretSize() { return 16; }
        public int engineEncapsulationSize() { return 3; }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // polymorphic dispatch through the interface type
        KEMSpi.EncapsulatorSpi spi = new FixedEnc32();
        check(spi.engineSecretSize() == 32, "impl A engineSecretSize 32");
        check(spi.engineEncapsulationSize() == 32, "impl A engineEncapsulationSize 32");
        KEM.Encapsulated out = spi.engineEncapsulate(0, 32, "Generic");
        check("Generic".equals(out.key().getAlgorithm()), "impl A algorithm passthrough");
        check(out.key().getEncoded().length == 32
                && out.key().getEncoded()[0] == 0
                && out.key().getEncoded()[31] == 31, "impl A fixed secret bytes");
        check(Arrays.equals(out.encapsulation(), new FixedEnc32().ct),
                "impl A fixed ciphertext bytes");
        check(out.params() == null, "impl A null params");
        FixedEnc32 a = (FixedEnc32) spi;
        check(a.calls == 1 && a.from == 0 && a.to == 32 && "Generic".equals(a.alg),
                "impl A received from/to/algorithm");

        // a different implementation dispatched through the same interface type
        spi = new FixedEnc16();
        check(spi.engineSecretSize() == 16 && spi.engineEncapsulationSize() == 3,
                "impl B sizes independent of impl A");
        KEM.Encapsulated b = spi.engineEncapsulate(0, 16, "AES");
        check("AES".equals(b.key().getAlgorithm()) && b.key().getEncoded().length == 16,
                "impl B AES key");
        check(b.encapsulation().length == 3 && Arrays.equals(b.params(), new byte[]{1}),
                "impl B fixed ct and non-null params");
        check(((FixedEnc16) spi).calls == 1, "impl B dispatched independently");
        check(!Arrays.equals(out.key().getEncoded(), b.key().getEncoded()),
                "no shared state between implementations");

        // repeated calls return equal but independently usable values
        KEM.Encapsulated again = new FixedEnc32().engineEncapsulate(0, 32, "Generic");
        check(Arrays.equals(again.key().getEncoded(), out.key().getEncoded())
                && again.encapsulation() != out.encapsulation(), "repeat call fresh Encapsulated");

        // the built-in DHKEM encapsulator is an instance of this SPI (same engine contract)
        javax.crypto.KEM kem = javax.crypto.KEM.getInstance("DHKEM");
        java.security.KeyPairGenerator xdg =
                java.security.KeyPairGenerator.getInstance("XDH");
        xdg.initialize(java.security.spec.NamedParameterSpec.X25519);
        java.security.KeyPair kp = xdg.generateKeyPair();
        javax.crypto.KEM.Encapsulator real = kem.newEncapsulator(kp.getPublic());
        check(real.secretSize() == 32 && real.encapsulationSize() == 32,
                "built-in encapsulator honors engineSecretSize/engineEncapsulationSize");
        javax.crypto.KEM.Encapsulated rc = real.encapsulate(0, 16, "AES");
        check(rc.key().getEncoded().length == 16 && "AES".equals(rc.key().getAlgorithm()),
                "built-in encapsulate(from,to,alg) delegates to engineEncapsulate");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
