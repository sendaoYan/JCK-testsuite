/*
 * @test
 * @summary javax.crypto.KEM.Encapsulator conformance: white-box structure + black-box behavior.
 * @run main EncapsulatorTest
 */

import javax.crypto.*;
import java.lang.reflect.*;
import java.security.*;
import java.security.spec.NamedParameterSpec;
import java.util.Arrays;

public class EncapsulatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.KEM$Encapsulator");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getDeclaringClass().getName().equals("javax.crypto.KEM"), "nested in javax.crypto.KEM");
        boolean publicCtor = false;
        for (Constructor<?> k : c.getDeclaredConstructors())
            if (Modifier.isPublic(k.getModifiers())) publicCtor = true;
        check(!publicCtor, "no public constructor (created via KEM.newEncapsulator)");
        checkMethod(c, "providerName", "java.lang.String", 0);
        checkMethod(c, "encapsulate", "javax.crypto.KEM$Encapsulated", 0);
        checkMethod(c, "encapsulate", "javax.crypto.KEM$Encapsulated", 3);
        checkMethod(c, "secretSize", "int", 0);
        checkMethod(c, "encapsulationSize", "int", 0);
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
        check(enc.providerName() != null, "providerName non-null");
        check(enc.secretSize() == 32, "ML-KEM secretSize 32");
        check(enc.encapsulationSize() > 0, "encapsulationSize positive");
        KEM.Encapsulated e = enc.encapsulate();
        check(e.key() != null && e.encapsulation() != null, "encapsulate non-null");

        // --- DHKEM X25519 ---
        KeyPairGenerator xdg = KeyPairGenerator.getInstance("XDH");
        xdg.initialize(NamedParameterSpec.X25519);
        KeyPair xkp = xdg.generateKeyPair();
        KEM dh = KEM.getInstance("DHKEM");
        KEM.Encapsulator xenc = dh.newEncapsulator(xkp.getPublic());
        check("SunJCE".equals(xenc.providerName()), "providerName SunJCE");
        check(xenc.secretSize() == 32, "X25519 secretSize 32");
        check(xenc.encapsulationSize() == 32, "X25519 encapsulationSize 32");

        // sizes are available before any encapsulate() call and stay constant
        int s0 = xenc.secretSize(), c0 = xenc.encapsulationSize();
        KEM.Encapsulated g = xenc.encapsulate();
        check(s0 == xenc.secretSize() && c0 == xenc.encapsulationSize(), "sizes immutable");
        check("Generic".equals(g.key().getAlgorithm()) && g.key().getEncoded().length == s0
                && g.encapsulation().length == c0, "encapsulate() == (0, secretSize, Generic)");

        // explicit (0, secretSize(), "Generic") overload matches documented default
        KEM.Encapsulated g2 = xenc.encapsulate(0, xenc.secretSize(), "Generic");
        check("Generic".equals(g2.key().getAlgorithm()) && g2.key().getEncoded().length == s0
                && g2.encapsulation().length == c0, "explicit Generic overload");

        // partial-secret AES overload
        KEM.Encapsulated a = xenc.encapsulate(0, 16, "AES");
        check("AES".equals(a.key().getAlgorithm()) && a.key().getEncoded().length == 16,
                "encapsulate(0,16,AES) key");
        check(a.encapsulation().length == c0, "AES overload ciphertext full size");

        // each fresh encapsulation pairs with the decapsulator
        KEM.Decapsulator xdec = dh.newDecapsulator(xkp.getPrivate());
        KEM.Encapsulated p1 = xenc.encapsulate();
        KEM.Encapsulated p2 = xenc.encapsulate();
        check(!Arrays.equals(p1.encapsulation(), p2.encapsulation()), "fresh ciphertext each call");
        check(!Arrays.equals(p1.key().getEncoded(), p2.key().getEncoded()), "fresh secret each call");
        check(Arrays.equals(xdec.decapsulate(p1.encapsulation()).getEncoded(),
                p1.key().getEncoded()), "pair 1 roundtrip");
        check(Arrays.equals(xdec.decapsulate(p2.encapsulation()).getEncoded(),
                p2.key().getEncoded()), "pair 2 roundtrip");

        // other newEncapsulator overloads produce equally usable encapsulators
        KEM.Encapsulator withRnd = dh.newEncapsulator(xkp.getPublic(), new SecureRandom());
        KEM.Encapsulator withNulls = dh.newEncapsulator(xkp.getPublic(), null, null);
        check(withRnd.secretSize() == 32 && withNulls.secretSize() == 32,
                "SecureRandom/null-spec overloads");
        KEM.Encapsulated r1 = withRnd.encapsulate();
        KEM.Encapsulated r2 = withRnd.encapsulate(0, 32, "Generic");
        check(Arrays.equals(xdec.decapsulate(r1.encapsulation()).getEncoded(),
                r1.key().getEncoded()), "SecureRandom overload roundtrip");
        check(!Arrays.equals(r1.key().getEncoded(), r2.key().getEncoded()),
                "distinct encapsulations have distinct secrets");

        // argument validation
        try {
            xenc.encapsulate(2, 1, "Generic");
            check(false, "from>to must fail");
        } catch (IndexOutOfBoundsException ex) { check(true, "from>to -> IndexOutOfBoundsException"); }
        try {
            xenc.encapsulate(0, xenc.secretSize() + 1, "Generic");
            check(false, "to>secretSize must fail");
        } catch (IndexOutOfBoundsException ex) { check(true, "to>secretSize -> IndexOutOfBoundsException"); }
        try {
            xenc.encapsulate(-1, 16, "AES");
            check(false, "negative from must fail");
        } catch (IndexOutOfBoundsException ex) { check(true, "negative from -> IndexOutOfBoundsException"); }
        try {
            xenc.encapsulate(0, 16, null);
            check(false, "null algorithm must fail");
        } catch (NullPointerException ex) { check(true, "null algorithm -> NullPointerException"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
