/*
 * @test
 * @summary javax.crypto.KEM.Encapsulated conformance: white-box structure + black-box behavior.
 * @run main EncapsulatedTest
 */

import javax.crypto.*;
import javax.crypto.spec.*;
import java.lang.reflect.*;
import java.security.*;
import java.security.spec.NamedParameterSpec;
import java.util.Arrays;

public class EncapsulatedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.KEM$Encapsulated");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getDeclaringClass().getName().equals("javax.crypto.KEM"), "nested in javax.crypto.KEM");
        // public constructor (SecretKey, byte[], byte[])
        boolean ctor = false;
        for (Constructor<?> k : c.getDeclaredConstructors()) {
            if (Modifier.isPublic(k.getModifiers()) && k.getParameterCount() == 3) {
                Class<?>[] ps = k.getParameterTypes();
                if (ps[0].getName().equals("javax.crypto.SecretKey")
                        && ps[1] == byte[].class && ps[2] == byte[].class) ctor = true;
            }
        }
        check(ctor, "public ctor (SecretKey, byte[], byte[])");
        checkMethod(c, "key", "javax.crypto.SecretKey");
        checkMethod(c, "encapsulation", "[B");
        checkMethod(c, "params", "[B");
        check(c.getDeclaredFields().length >= 3, "holds key/encapsulation/params state");
    }

    static void checkMethod(Class<?> c, String name, String ret) throws Exception {
        Method m = c.getDeclaredMethod(name);
        check(m.getReturnType().getName().equals(ret), name + " returns " + ret);
        check(Modifier.isPublic(m.getModifiers()), name + " public");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // --- preserved original scenario: direct construction + accessors ---
        SecretKeySpec key = new SecretKeySpec(new byte[16], "AES");
        byte[] enc = new byte[]{1, 2, 3};
        byte[] params = new byte[]{4, 5};
        KEM.Encapsulated e = new KEM.Encapsulated(key, enc, params);
        check("AES".equals(e.key().getAlgorithm()), "key accessor");
        check(Arrays.equals(enc, e.encapsulation()), "encapsulation accessor");
        check(Arrays.equals(params, e.params()), "params accessor");
        check(e.key() == key, "key() returns same SecretKey instance");

        // defensive copies of constructor inputs
        enc[0] = 99;
        params[0] = 99;
        check(e.encapsulation()[0] == 1 && e.params()[0] == 4, "ctor inputs copied");

        // accessors return fresh copies each call
        byte[] got1 = e.encapsulation();
        byte[] got2 = e.encapsulation();
        check(got1 != got2, "encapsulation() fresh copy each call");
        got1[0] = 77;
        check(e.encapsulation()[0] == 1, "mutating returned encapsulation has no effect");
        byte[] p1 = e.params();
        check(p1 != e.params(), "params() fresh copy each call");
        p1[0] = 88;
        check(e.params()[0] == 4, "mutating returned params has no effect");

        // null params allowed, content of repeated access stable
        KEM.Encapsulated enp = new KEM.Encapsulated(key, new byte[]{7}, null);
        check(enp.params() == null, "null params accepted");
        check(Arrays.equals(enp.encapsulation(), enp.encapsulation()), "content stable across calls");

        // null key / null encapsulation rejected
        try {
            new KEM.Encapsulated(null, new byte[]{1}, null);
            check(false, "null key must fail");
        } catch (NullPointerException ex) { check(true, "null key -> NullPointerException"); }
        try {
            new KEM.Encapsulated(key, null, null);
            check(false, "null encapsulation must fail");
        } catch (NullPointerException ex) { check(true, "null encapsulation -> NullPointerException"); }

        // --- real DHKEM-produced Encapsulated wired into a decapsulator ---
        KeyPairGenerator xdg = KeyPairGenerator.getInstance("XDH");
        xdg.initialize(NamedParameterSpec.X25519);
        KeyPair xkp = xdg.generateKeyPair();
        KEM kem = KEM.getInstance("DHKEM");
        KEM.Encapsulator xenc = kem.newEncapsulator(xkp.getPublic());
        KEM.Encapsulated real = xenc.encapsulate();
        check(real.key() != null && "Generic".equals(real.key().getAlgorithm()),
                "real key() Generic non-null");
        check(real.key().getEncoded().length == xenc.secretSize(),
                "real key length == encapsulator.secretSize()");
        check(real.encapsulation().length == xenc.encapsulationSize(),
                "real encapsulation length == encapsulator.encapsulationSize()");
        KEM.Decapsulator xdec = kem.newDecapsulator(xkp.getPrivate());
        check(Arrays.equals(xdec.decapsulate(real.encapsulation()).getEncoded(),
                real.key().getEncoded()), "decapsulate(real.encapsulation()) == real.key()");
        // mutating the returned encapsulation does not corrupt decapsulation
        byte[] sabotage = real.encapsulation();
        sabotage[0] ^= (byte) 0xAA;
        check(!Arrays.equals(sabotage, real.encapsulation()),
                "real.encapsulation() immune to external mutation");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
