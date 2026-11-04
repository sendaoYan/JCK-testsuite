/*
 * @test
 * @summary javax.crypto.interfaces.DHPublicKey conformance: white-box structure + black-box behavior.
 * @run main DHPublicKeyTest
 */

import java.io.*;
import java.lang.reflect.*;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.*;
import java.util.Arrays;
import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.*;

public class DHPublicKeyTest {
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
        Class<?> c = Class.forName("javax.crypto.interfaces.DHPublicKey");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkInterfaces(c, new String[]{"javax.crypto.interfaces.DHKey","java.security.PublicKey"});
        checkMethods(c, new String[]{"getY"});
        // JDK-8: getParams is inherited from the DHKey superinterface (not re-declared here
        // as it is on JDK-9+); the method is still part of every DH key's public surface.
        boolean paramsVisible = false;
        for (Method m : c.getMethods()) if (m.getName().equals("getParams")) paramsVisible = true;
        check(paramsVisible, "getParams visible on the interface surface (inherited from DHKey)");
        // exact signatures
        check(c.getDeclaredMethod("getY").getReturnType() == BigInteger.class, "getY returns BigInteger");
        check(c.getMethod("getParams").getReturnType() == DHParameterSpec.class,
                "getParams returns DHParameterSpec");
        // JDK-9+ re-declares getParams here as a default method; on JDK-8 it exists only on
        // DHKey, so there is no local default-method declaration to check.
        // serialVersionUID: public static final long, value fixed since J2SE 1.4
        Field svuid = c.getDeclaredField("serialVersionUID");
        check(Modifier.isPublic(svuid.getModifiers()) && Modifier.isStatic(svuid.getModifiers())
                && Modifier.isFinal(svuid.getModifiers()) && svuid.getType() == long.class,
                "serialVersionUID public static final long");
        check(svuid.getLong(null) == -6628103563352519193L, "serialVersionUID value");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DiffieHellman");
        kpg.initialize(512);
        KeyPair kp = kpg.generateKeyPair();
        PublicKey rawPub = kp.getPublic();
        check(rawPub instanceof DHPublicKey, "generated public key instanceof DHPublicKey");
        check(rawPub instanceof javax.crypto.interfaces.DHKey, "public key instanceof DHKey");
        check(rawPub instanceof Key, "public key instanceof Key");
        DHPublicKey pub = (DHPublicKey) rawPub;
        DHParameterSpec params = pub.getParams();
        BigInteger p = params.getP(), g = params.getG(), y = pub.getY();
        javax.crypto.interfaces.DHPrivateKey priv =
                (javax.crypto.interfaces.DHPrivateKey) kp.getPrivate();

        // getY(): public value y = g^x mod p, in range 1 <= y <= p-1
        check(y != null && y.signum() == 1, "getY positive");
        check(y.compareTo(p) < 0, "getY < p");
        check(y.equals(g.modPow(priv.getX(), p)), "y equals g^x mod p");

        // key metadata inherited from Key
        check("DH".equals(pub.getAlgorithm()), "getAlgorithm == DH");
        check("X.509".equals(pub.getFormat()), "getFormat == X.509");
        byte[] enc1 = pub.getEncoded();
        byte[] enc2 = pub.getEncoded();
        check(enc1 != null && enc1.length > 0, "getEncoded non-empty");
        check(Arrays.equals(enc1, enc2), "getEncoded stable across calls");
        check(enc1 != enc2, "getEncoded returns a fresh copy each call");

        // spec roundtrip through KeyFactory
        KeyFactory kf = KeyFactory.getInstance("DH");
        DHPublicKeySpec spec = kf.getKeySpec(pub, DHPublicKeySpec.class);
        check(spec.getY().equals(y) && spec.getP().equals(p) && spec.getG().equals(g),
                "DHPublicKeySpec carries y/p/g of the key");
        DHPublicKey fromSpec = (DHPublicKey) kf.generatePublic(spec);
        check(fromSpec.getY().equals(y), "regenerated key has same y");
        check(fromSpec.equals(pub), "regenerated key equals original");
        check(fromSpec.hashCode() == pub.hashCode(), "equal keys share hashCode");
        // X.509 encoding roundtrip
        DHPublicKey fromX509 = (DHPublicKey) kf.generatePublic(new X509EncodedKeySpec(enc1));
        check(fromX509.getY().equals(y) && Arrays.equals(fromX509.getEncoded(), enc1),
                "X.509 encoding roundtrip");
        // translateKey wraps foreign key instances back into provider form
        Key translated = kf.translateKey(pub);
        check(translated instanceof DHPublicKey && ((DHPublicKey) translated).getY().equals(y),
                "translateKey returns DHPublicKey with same y");

        // a second party consumes this public key in a KeyAgreement
        KeyPairGenerator kpg2 = KeyPairGenerator.getInstance("DiffieHellman");
        kpg2.initialize(new DHParameterSpec(p, g));
        KeyPair kpB = kpg2.generateKeyPair();
        KeyAgreement kaB = KeyAgreement.getInstance("DiffieHellman");
        kaB.init(kpB.getPrivate());
        kaB.doPhase(pub, true);
        byte[] sB = kaB.generateSecret();
        KeyAgreement kaA = KeyAgreement.getInstance("DiffieHellman");
        kaA.init(priv);
        kaA.doPhase(kpB.getPublic(), true);
        check(Arrays.equals(kaA.generateSecret(), sB), "peer agreement with this public key matches");
        // and equivalently with the regenerated public key
        KeyAgreement kaC = KeyAgreement.getInstance("DiffieHellman");
        kaC.init(kpB.getPrivate());
        kaC.doPhase(fromSpec, true);
        check(Arrays.equals(kaC.generateSecret(), sB), "agreement with regenerated key matches");

        // serialization roundtrip (KeyRep based) keeps y and encoding
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(pub); }
        DHPublicKey pubD;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            pubD = (DHPublicKey) ois.readObject();
        }
        check(pubD != null && pubD.getY().equals(y) && Arrays.equals(pubD.getEncoded(), enc1),
                "deserialized public key keeps y and encoding");

        // equals contract across distinct key pairs
        KeyPair other = kpg2.generateKeyPair();
        check(!pub.equals(other.getPublic()), "distinct key pairs are not equal");
        check(pub.equals(pubD), "deserialized key equals original");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
