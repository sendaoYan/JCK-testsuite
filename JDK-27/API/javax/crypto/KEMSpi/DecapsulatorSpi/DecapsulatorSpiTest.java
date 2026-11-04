/*
 * @test
 * @summary javax.crypto.KEMSpi.DecapsulatorSpi conformance: white-box structure + black-box behavior.
 * @run main DecapsulatorSpiTest
 */

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.*;
import java.util.Arrays;

public class DecapsulatorSpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.KEMSpi$DecapsulatorSpi");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getDeclaringClass().getName().equals("javax.crypto.KEMSpi"),
                "nested in javax.crypto.KEMSpi");
        // preserved: engine method names exist
        checkMethods(c, new String[]{"engineDecapsulate", "engineSecretSize",
                                     "engineEncapsulationSize"});
        Method dec = c.getDeclaredMethod("engineDecapsulate", byte[].class, int.class,
                int.class, String.class);
        check(Modifier.isAbstract(dec.getModifiers()), "engineDecapsulate abstract");
        check(dec.getReturnType().getName().equals("javax.crypto.SecretKey"),
                "engineDecapsulate returns SecretKey");
        boolean throwsDecap = false;
        for (Class<?> t : dec.getExceptionTypes())
            if (t.getName().equals("javax.crypto.DecapsulateException")) throwsDecap = true;
        check(throwsDecap, "engineDecapsulate throws DecapsulateException");
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
    static final byte[] SECRET32 = new byte[32];
    static final byte[] SECRET16 = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
    static { for (int i = 0; i < 32; i++) SECRET32[i] = (byte) (50 + i); }

    static class FixedDec32 implements KEMSpi.DecapsulatorSpi {
        int calls; byte[] seenCt; int from, to; String alg;
        public SecretKey engineDecapsulate(byte[] encapsulation, int f, int t, String a)
                throws DecapsulateException {
            calls++; seenCt = encapsulation.clone(); from = f; to = t; alg = a;
            if (encapsulation.length != 32)
                throw new DecapsulateException("wrong size: " + encapsulation.length);
            return new SecretKeySpec(SECRET32, a);
        }
        public int engineSecretSize() { return 32; }
        public int engineEncapsulationSize() { return 32; }
    }

    static class FixedDec16 implements KEMSpi.DecapsulatorSpi {
        int calls;
        public SecretKey engineDecapsulate(byte[] encapsulation, int f, int t, String a) {
            calls++;
            return new SecretKeySpec(SECRET16, a);
        }
        public int engineSecretSize() { return 16; }
        public int engineEncapsulationSize() { return 16; }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // polymorphic dispatch through the interface type
        KEMSpi.DecapsulatorSpi spi = new FixedDec32();
        check(spi.engineSecretSize() == 32 && spi.engineEncapsulationSize() == 32,
                "impl A sizes 32/32");
        byte[] ct = new byte[32];
        ct[0] = 7;
        SecretKey k = spi.engineDecapsulate(ct, 0, 32, "Generic");
        check("Generic".equals(k.getAlgorithm()) && Arrays.equals(k.getEncoded(), SECRET32),
                "impl A fixed secret + algorithm passthrough");
        FixedDec32 a = (FixedDec32) spi;
        check(a.calls == 1 && Arrays.equals(a.seenCt, ct) && a.from == 0 && a.to == 32
                && "Generic".equals(a.alg), "impl A received ct/from/to/algorithm");
        // mutating the caller's array afterwards does not change what was received
        ct[0] = 99;
        check(a.seenCt[0] == 7, "impl A snapshot of ct unaffected by caller mutation");

        // failure path: implementation-defined DecapsulateException
        try {
            spi.engineDecapsulate(new byte[31], 0, 32, "Generic");
            check(false, "impl A must reject wrong-size ct");
        } catch (DecapsulateException ex) {
            check("wrong size: 31".equals(ex.getMessage()), "impl A DecapsulateException message");
        }
        check(a.calls == 2, "impl A dispatched twice");

        // a different implementation through the same interface type
        spi = new FixedDec16();
        check(spi.engineSecretSize() == 16 && spi.engineEncapsulationSize() == 16,
                "impl B sizes independent of impl A");
        SecretKey b = spi.engineDecapsulate(new byte[16], 0, 16, "AES");
        check("AES".equals(b.getAlgorithm()) && b.getEncoded().length == 16,
                "impl B fixed secret");
        check(!Arrays.equals(b.getEncoded(), k.getEncoded()),
                "no shared state between implementations");
        check(((FixedDec16) spi).calls == 1, "impl B dispatched independently");

        // the built-in DHKEM decapsulator is driven by this SPI (same engine contract)
        javax.crypto.KEM kem = javax.crypto.KEM.getInstance("DHKEM");
        java.security.KeyPairGenerator xdg =
                java.security.KeyPairGenerator.getInstance("XDH");
        xdg.initialize(java.security.spec.NamedParameterSpec.X25519);
        java.security.KeyPair kp = xdg.generateKeyPair();
        javax.crypto.KEM.Encapsulated real = kem.newEncapsulator(kp.getPublic()).encapsulate();
        javax.crypto.KEM.Decapsulator rdec = kem.newDecapsulator(kp.getPrivate());
        check(rdec.secretSize() == 32 && rdec.encapsulationSize() == 32,
                "built-in decapsulator honors engineSecretSize/engineEncapsulationSize");
        check(Arrays.equals(rdec.decapsulate(real.encapsulation()).getEncoded(),
                real.key().getEncoded()), "built-in engineDecapsulate recovers the secret");
        try {
            rdec.decapsulate(new byte[32]);
            check(false, "built-in SPI must reject all-zero ct");
        } catch (DecapsulateException ex) {
            check(true, "built-in SPI reports DecapsulateException");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
