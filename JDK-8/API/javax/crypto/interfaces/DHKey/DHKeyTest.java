/*
 * @test
 * @summary javax.crypto.interfaces.DHKey conformance: white-box structure + black-box behavior.
 * @run main DHKeyTest
 */

import java.lang.reflect.*;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHKey;
import javax.crypto.spec.DHParameterSpec;

public class DHKeyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static void checkInterfaces(Class<?> c, String[] ns) { for (String n : ns) check(hasInterface(c, n), "implements " + n); }
    static void checkMethods(Class<?> c, String[] ns) { for (String n : ns) check(hasMethod(c, n), "method " + n); }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static void checkField(Class<?> c, String n) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(n)) { check(true, "field " + n); return; } check(false, "field " + n); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.interfaces.DHKey");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkMethods(c, new String[]{"getParams"});
        // exact return type of the single declared method
        Method gp = c.getDeclaredMethod("getParams");
        check(gp.getReturnType() == Class.forName("javax.crypto.spec.DHParameterSpec"),
                "getParams returns javax.crypto.spec.DHParameterSpec");
        check(Modifier.isAbstract(gp.getModifiers()), "getParams is abstract");
        // DHKey declares no fields (no serialVersionUID in this interface)
        check(c.getDeclaredFields().length == 0, "no declared fields");
        // the companion key interfaces are subtypes of DHKey
        check(c.isAssignableFrom(Class.forName("javax.crypto.interfaces.DHPrivateKey")),
                "DHPrivateKey is a DHKey subtype");
        check(c.isAssignableFrom(Class.forName("javax.crypto.interfaces.DHPublicKey")),
                "DHPublicKey is a DHKey subtype");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Real DHKey instances come from a Diffie-Hellman KeyPairGenerator (SunJCE).
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DiffieHellman");
        kpg.initialize(512);
        KeyPair kp = kpg.generateKeyPair();
        check(kp.getPrivate() instanceof DHKey, "DH private key instanceof DHKey");
        check(kp.getPublic() instanceof DHKey, "DH public key instanceof DHKey");
        DHKey privKey = (DHKey) kp.getPrivate();
        DHKey pubKey = (DHKey) kp.getPublic();

        // getParams(): structural properties of the shared DH parameters
        DHParameterSpec params = privKey.getParams();
        check(params != null, "private getParams() non-null");
        BigInteger p = params.getP(), g = params.getG();
        check(p != null && p.signum() == 1 && p.bitLength() == 512, "p positive 512-bit");
        check(g != null && g.signum() == 1 && g.compareTo(p) < 0, "g positive and < p");
        check(p.isProbablePrime(64), "p is (probable) prime");

        // both keys of a pair carry the same parameter values
        DHParameterSpec pubParams = pubKey.getParams();
        check(pubParams.getP().equals(p) && pubParams.getG().equals(g),
                "public/private getParams() agree on p and g");
        // repeated getParams() returns equal values
        DHParameterSpec again = privKey.getParams();
        check(again.getP().equals(p) && again.getG().equals(g), "repeated getParams() equal values");

        // conversion chain: DHKey params -> AlgorithmParameters -> DHParameterSpec roundtrip
        AlgorithmParameters ap = AlgorithmParameters.getInstance("DH");
        ap.init(params);
        DHParameterSpec back = ap.getParameterSpec(DHParameterSpec.class);
        check(back.getP().equals(p) && back.getG().equals(g) && back.getL() == params.getL(),
                "AlgorithmParameters roundtrip preserves p/g/L");

        // family glue: re-initialize two generators with the params taken from a DHKey
        KeyPairGenerator kpgA = KeyPairGenerator.getInstance("DiffieHellman");
        kpgA.initialize(pubParams);
        KeyPair pairA = kpgA.generateKeyPair();
        KeyPairGenerator kpgB = KeyPairGenerator.getInstance("DiffieHellman");
        kpgB.initialize(new DHParameterSpec(pubParams.getP(), pubParams.getG()));
        KeyPair pairB = kpgB.generateKeyPair();
        check(pairA.getPrivate() instanceof DHKey && pairA.getPublic() instanceof DHKey
                && pairB.getPrivate() instanceof DHKey && pairB.getPublic() instanceof DHKey,
                "keys derived from DHKey params are DHKeys");
        KeyAgreement kaA = KeyAgreement.getInstance("DiffieHellman");
        kaA.init(pairA.getPrivate());
        kaA.doPhase(pairB.getPublic(), true);
        KeyAgreement kaB = KeyAgreement.getInstance("DiffieHellman");
        kaB.init(pairB.getPrivate());
        kaB.doPhase(pairA.getPublic(), true);
        byte[] sA = kaA.generateSecret(), sB = kaB.generateSecret();
        check(Arrays.equals(sA, sB), "two parties reach the same DH secret");
        check(sA.length == p.bitLength() / 8, "secret length is p.bitLength()/8");

        // freshly generated parameters via AlgorithmParameterGenerator also yield DHKeys
        AlgorithmParameterGenerator apg = AlgorithmParameterGenerator.getInstance("DH");
        apg.init(512);
        AlgorithmParameters apGen = apg.generateParameters();
        DHParameterSpec genSpec = apGen.getParameterSpec(DHParameterSpec.class);
        check(genSpec.getP().bitLength() == 512, "generated params have 512-bit p");
        KeyPairGenerator kpgC = KeyPairGenerator.getInstance("DiffieHellman");
        kpgC.initialize(genSpec);
        KeyPair pairC = kpgC.generateKeyPair();
        check(pairC.getPrivate() instanceof DHKey && pairC.getPublic() instanceof DHKey,
                "keys from generated params are DHKeys");
        check(((DHKey) pairC.getPrivate()).getParams().getP().equals(genSpec.getP()),
                "key params equal the generated p");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
