/*
 * @test
 * @summary javax.crypto.KEM.Decapsulator conformance: white-box structure + black-box behavior.
 * @run main DecapsulatorTest
 */

import javax.crypto.*;
import java.lang.reflect.*;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.NamedParameterSpec;
import java.util.Arrays;

public class DecapsulatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.KEM$Decapsulator");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getDeclaringClass().getName().equals("javax.crypto.KEM"), "nested in javax.crypto.KEM");
        boolean publicCtor = false;
        for (Constructor<?> k : c.getDeclaredConstructors())
            if (Modifier.isPublic(k.getModifiers())) publicCtor = true;
        check(!publicCtor, "no public constructor (created via KEM.newDecapsulator)");
        checkMethod(c, "providerName", "java.lang.String", 0);
        checkMethod(c, "decapsulate", "javax.crypto.SecretKey", 1);
        checkMethod(c, "decapsulate", "javax.crypto.SecretKey", 4);
        checkMethod(c, "secretSize", "int", 0);
        checkMethod(c, "encapsulationSize", "int", 0);
        // 4-arg overload throws DecapsulateException
        Method m = c.getDeclaredMethod("decapsulate", byte[].class, int.class, int.class,
                String.class);
        boolean throwsDecap = false;
        for (Class<?> t : m.getExceptionTypes())
            if (t.getName().equals("javax.crypto.DecapsulateException")) throwsDecap = true;
        check(throwsDecap, "decapsulate throws DecapsulateException");
    }

    static void checkMethod(Class<?> c, String name, String ret, int params) throws Exception {
        boolean found = false;
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == params
                    && m.getReturnType().getName().equals(ret)) found = true;
        }
        check(found, name + "/" + params + " returns " + ret);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // --- preserved original scenario: ML-KEM ---
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("ML-KEM");
        KeyPair kp = kpg.generateKeyPair();
        KEM kem = KEM.getInstance("ML-KEM");
        KEM.Encapsulator enc = kem.newEncapsulator(kp.getPublic());
        KEM.Encapsulated e = enc.encapsulate();
        KEM.Decapsulator dec = kem.newDecapsulator(kp.getPrivate());
        check(dec.providerName() != null, "providerName non-null");
        check(dec.secretSize() == 32, "secretSize 32");
        check(dec.encapsulationSize() > 0, "encapsulationSize positive");
        check(dec.decapsulate(e.encapsulation()) != null, "decapsulate non-null");

        // --- DHKEM X25519 ---
        KeyPairGenerator xdg = KeyPairGenerator.getInstance("XDH");
        xdg.initialize(NamedParameterSpec.X25519);
        KeyPair xkp = xdg.generateKeyPair();
        KEM dh = KEM.getInstance("DHKEM");
        KEM.Encapsulator xenc = dh.newEncapsulator(xkp.getPublic());
        KEM.Decapsulator xdec = dh.newDecapsulator(xkp.getPrivate());
        check("SunJCE".equals(xdec.providerName()), "providerName SunJCE");
        check(xdec.secretSize() == 32 && xdec.encapsulationSize() == 32,
                "X25519 decapsulator sizes 32/32");
        check(xdec.secretSize() == xenc.secretSize()
                && xdec.encapsulationSize() == xenc.encapsulationSize(),
                "decapsulator sizes match encapsulator");

        KEM.Encapsulated ct = xenc.encapsulate();
        byte[] secret = xdec.decapsulate(ct.encapsulation()).getEncoded();
        check(Arrays.equals(secret, ct.key().getEncoded()), "paired decapsulation");
        check(Arrays.equals(xdec.decapsulate(ct.encapsulation()).getEncoded(), secret),
                "decapsulate is deterministic for same ct");
        // documented equivalence: decapsulate(ct) == decapsulate(ct, 0, secretSize(), "Generic")
        check(Arrays.equals(
                xdec.decapsulate(ct.encapsulation(), 0, xdec.secretSize(), "Generic").getEncoded(),
                secret), "1-arg == 4-arg(0,secretSize,Generic)");
        // sub-range overloads carve the matching bytes out of the same secret
        check(Arrays.equals(
                xdec.decapsulate(ct.encapsulation(), 0, 16, "AES").getEncoded(),
                Arrays.copyOf(secret, 16)), "first 16 bytes as AES key");
        SecretKey tail = xdec.decapsulate(ct.encapsulation(), 16, 32, "AES");
        check(Arrays.equals(tail.getEncoded(), Arrays.copyOfRange(secret, 16, 32))
                && "AES".equals(tail.getAlgorithm()), "last 16 bytes as AES key");

        // malformed ciphertexts
        try {
            xdec.decapsulate(new byte[32]);
            check(false, "all-zero ct must fail");
        } catch (DecapsulateException ex) { check(true, "all-zero ct -> DecapsulateException"); }
        try {
            xdec.decapsulate(new byte[31]);
            check(false, "short ct must fail");
        } catch (DecapsulateException ex) { check(true, "short ct -> DecapsulateException"); }
        try {
            xdec.decapsulate(new byte[33]);
            check(false, "long ct must fail");
        } catch (DecapsulateException ex) { check(true, "long ct -> DecapsulateException"); }

        // EC P-256: corrupting a valid ciphertext is detected
        KeyPairGenerator ecg = KeyPairGenerator.getInstance("EC");
        ecg.initialize(new ECGenParameterSpec("secp256r1"));
        KeyPair eckp = ecg.generateKeyPair();
        KEM.Encapsulator eenc = dh.newEncapsulator(eckp.getPublic());
        KEM.Decapsulator edec = dh.newDecapsulator(eckp.getPrivate());
        KEM.Encapsulated ect = eenc.encapsulate();
        check(Arrays.equals(edec.decapsulate(ect.encapsulation()).getEncoded(),
                ect.key().getEncoded()), "P-256 paired decapsulation");
        byte[] corrupted = ect.encapsulation();
        corrupted[10] ^= 1;
        try {
            edec.decapsulate(corrupted);
            check(false, "corrupted P-256 ct must fail");
        } catch (DecapsulateException ex) { check(true, "corrupted ct -> DecapsulateException"); }

        // argument validation
        try {
            xdec.decapsulate(null);
            check(false, "null ct must fail");
        } catch (NullPointerException ex) { check(true, "null ct -> NullPointerException"); }
        try {
            xdec.decapsulate(ct.encapsulation(), -1, 16, "AES");
            check(false, "negative from must fail");
        } catch (IndexOutOfBoundsException ex) { check(true, "negative from -> IndexOutOfBoundsException"); }
        try {
            xdec.decapsulate(ct.encapsulation(), 2, 1, "AES");
            check(false, "from>to must fail");
        } catch (IndexOutOfBoundsException ex) { check(true, "from>to -> IndexOutOfBoundsException"); }
        try {
            xdec.decapsulate(ct.encapsulation(), 0, xdec.secretSize() + 1, "AES");
            check(false, "to>secretSize must fail");
        } catch (IndexOutOfBoundsException ex) { check(true, "to>secretSize -> IndexOutOfBoundsException"); }
        try {
            xdec.decapsulate(ct.encapsulation(), 0, 16, null);
            check(false, "null algorithm must fail");
        } catch (NullPointerException ex) { check(true, "null algorithm -> NullPointerException"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
