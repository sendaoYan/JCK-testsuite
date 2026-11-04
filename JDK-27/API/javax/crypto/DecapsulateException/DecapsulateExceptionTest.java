/*
 * @test
 * @summary javax.crypto.DecapsulateException conformance: white-box structure + black-box behavior.
 * @run main DecapsulateExceptionTest
 */

import javax.crypto.DecapsulateException;
import javax.crypto.KEM;
import javax.crypto.KEMSpi;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.spec.NamedParameterSpec;
import java.util.Arrays;

public class DecapsulateExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.DecapsulateException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isAbstract(c.getModifiers()) && !Modifier.isFinal(c.getModifiers()),
                "non-abstract non-final class");
        check(c.getSuperclass().getName().equals("java.security.GeneralSecurityException"),
                "extends GeneralSecurityException");
        // exactly two public constructors: (String) and (String, Throwable)
        boolean msgCtor = false, msgCauseCtor = false;
        int publicCtors = 0;
        for (Constructor<?> k : c.getConstructors()) {
            publicCtors++;
            if (k.getParameterCount() == 1 && k.getParameterTypes()[0] == String.class) msgCtor = true;
            if (k.getParameterCount() == 2 && k.getParameterTypes()[0] == String.class
                    && k.getParameterTypes()[1] == Throwable.class) msgCauseCtor = true;
        }
        check(publicCtors == 2 && msgCtor && msgCauseCtor, "ctors (String),(String,Throwable)");
    }

    // ---- a decapsulator whose failure we control ----
    static class FailingSpi implements KEMSpi.DecapsulatorSpi {
        public SecretKey engineDecapsulate(byte[] encapsulation, int from, int to, String alg)
                throws DecapsulateException {
            throw new DecapsulateException("spi failure", new IllegalStateException("inner"));
        }
        public int engineSecretSize() { return 32; }
        public int engineEncapsulationSize() { return 32; }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // --- preserved original assertions ---
        DecapsulateException e = new DecapsulateException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof java.security.GeneralSecurityException, "is GeneralSecurityException");
        Throwable c = new Throwable("cause");
        check(new DecapsulateException("m", c).getCause() == c, "cause ctor");

        // message-only form: no cause, no suppressed
        check(e.getCause() == null, "message ctor leaves cause null");
        check(e.getSuppressed().length == 0, "no suppressed exceptions");
        check(e.toString().equals("javax.crypto.DecapsulateException: msg"),
                "toString carries FQN + message");

        // message + cause form: both retrievable, message of cause intact
        Throwable inner = new IllegalStateException("inner-state");
        DecapsulateException e2 = new DecapsulateException("m2", inner);
        check("m2".equals(e2.getMessage()) && e2.getCause() == inner
                && "inner-state".equals(e2.getCause().getMessage()), "message and cause exact");
        check(e2.toString().contains("DecapsulateException: m2"), "toString with cause");
        // null cause may be supplied; the constructor consumes the cause slot
        DecapsulateException e3 = new DecapsulateException("m3", null);
        check(e3.getCause() == null, "explicit null cause stays null");
        try {
            e3.initCause(inner);
            check(false, "initCause after ctor must fail");
        } catch (IllegalStateException ex) {
            check(e3.getCause() == null, "cause slot already consumed by ctor");
        }

        // caught polymorphically as its superclass
        try {
            throw new DecapsulateException("poly");
        } catch (GeneralSecurityException gse) {
            check(gse instanceof DecapsulateException && "poly".equals(gse.getMessage()),
                    "catchable as GeneralSecurityException");
        }

        // thrown out of a hand-written KEMSpi.DecapsulatorSpi with full context
        try {
            new FailingSpi().engineDecapsulate(new byte[32], 0, 32, "Generic");
            check(false, "spi failure must propagate");
        } catch (DecapsulateException ex) {
            check("spi failure".equals(ex.getMessage()), "spi message preserved");
            check(ex.getCause() instanceof IllegalStateException
                    && "inner".equals(ex.getCause().getMessage()), "spi cause preserved");
        }

        // produced by the real provider when decapsulation genuinely fails
        KeyPairGenerator xdg = KeyPairGenerator.getInstance("XDH");
        xdg.initialize(NamedParameterSpec.X25519);
        java.security.KeyPair kp = xdg.generateKeyPair();
        KEM kem = KEM.getInstance("DHKEM");
        KEM.Encapsulated ok = kem.newEncapsulator(kp.getPublic()).encapsulate();
        KEM.Decapsulator dec = kem.newDecapsulator(kp.getPrivate());
        check(Arrays.equals(dec.decapsulate(ok.encapsulation()).getEncoded(),
                ok.key().getEncoded()), "valid ct still decapsulates");
        try {
            dec.decapsulate(new byte[32]);
            check(false, "zero ciphertext must fail");
        } catch (DecapsulateException ex) {
            check(ex instanceof GeneralSecurityException && ex.getMessage() != null,
                    "provider failure type and message non-null");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
