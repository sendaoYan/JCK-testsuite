/*
 * @test
 * @summary javax.crypto.interfaces.DHPrivateKey conformance: white-box structure + black-box behavior.
 * @run main DHPrivateKeyTest
 */

import java.io.*;
import java.lang.reflect.*;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.*;
import java.util.Arrays;
import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.spec.*;

public class DHPrivateKeyTest {
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
        Class<?> c = Class.forName("javax.crypto.interfaces.DHPrivateKey");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkInterfaces(c, new String[]{"javax.crypto.interfaces.DHKey","java.security.PrivateKey"});
        checkMethods(c, new String[]{"getX"});
        // JDK-8: getParams is inherited from the DHKey superinterface (not re-declared here
        // as it is on JDK-9+); the method is still part of every DH key's public surface.
        boolean paramsVisible = false;
        for (Method m : c.getMethods()) if (m.getName().equals("getParams")) paramsVisible = true;
        check(paramsVisible, "getParams visible on the interface surface (inherited from DHKey)");
        // exact signatures
        check(c.getDeclaredMethod("getX").getReturnType() == BigInteger.class, "getX returns BigInteger");
        check(c.getMethod("getParams").getReturnType() == DHParameterSpec.class,
                "getParams returns DHParameterSpec");
        // JDK-9+ re-declares getParams here as a default method; on JDK-8 it exists only on
        // DHKey, so there is no local default-method declaration to check.
        // serialVersionUID: public static final long, value fixed since J2SE 1.4
        Field svuid = c.getDeclaredField("serialVersionUID");
        check(Modifier.isPublic(svuid.getModifiers()) && Modifier.isStatic(svuid.getModifiers())
                && Modifier.isFinal(svuid.getModifiers()) && svuid.getType() == long.class,
                "serialVersionUID public static final long");
        check(svuid.getLong(null) == 2211791113380396553L, "serialVersionUID value");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DiffieHellman");
        kpg.initialize(512);
        KeyPair kp = kpg.generateKeyPair();
        PrivateKey rawPriv = kp.getPrivate();
        check(rawPriv instanceof DHPrivateKey, "generated private key instanceof DHPrivateKey");
        check(rawPriv instanceof javax.crypto.interfaces.DHKey, "private key instanceof DHKey");
        check(rawPriv instanceof Key, "private key instanceof Key");
        DHPrivateKey priv = (DHPrivateKey) rawPriv;
        DHParameterSpec params = priv.getParams();
        BigInteger p = params.getP(), g = params.getG(), x = priv.getX();
        javax.crypto.interfaces.DHPublicKey pub = (javax.crypto.interfaces.DHPublicKey) kp.getPublic();

        // getX(): PKCS#3 private value in range 1 <= x <= p-2
        check(x != null && x.signum() == 1, "getX positive");
        check(x.compareTo(p) < 0, "getX < p");
        // algebraic family relation: y == g^x mod p
        check(pub.getY().equals(g.modPow(x, p)), "y equals g^x mod p");

        // key metadata inherited from Key
        check("DH".equals(priv.getAlgorithm()), "getAlgorithm == DH");
        check("PKCS#8".equals(priv.getFormat()), "getFormat == PKCS#8");
        byte[] enc1 = priv.getEncoded();
        byte[] enc2 = priv.getEncoded();
        check(enc1 != null && enc1.length > 0, "getEncoded non-empty");
        check(Arrays.equals(enc1, enc2), "getEncoded stable across calls");
        check(enc1 != enc2, "getEncoded returns a fresh copy each call");

        // SunJCE 512-bit keys use precomputed safe-prime params; the default private
        // exponent size (L) reported by getParams() is 160 for these legacy sizes.
        check(params.getL() == 160, "getParams().getL() == 160 for 512-bit safe prime");

        // spec roundtrip through KeyFactory
        KeyFactory kf = KeyFactory.getInstance("DH");
        DHPrivateKeySpec spec = kf.getKeySpec(priv, DHPrivateKeySpec.class);
        check(spec.getX().equals(x) && spec.getP().equals(p) && spec.getG().equals(g),
                "DHPrivateKeySpec carries x/p/g of the key");
        DHPrivateKey fromSpec = (DHPrivateKey) kf.generatePrivate(spec);
        check(fromSpec.getX().equals(x), "regenerated key has same x");
        check(fromSpec.getParams().getP().equals(p) && fromSpec.getParams().getG().equals(g),
                "regenerated key preserves p and g values");
        check(fromSpec.equals(priv), "spec roundtrip key equals original");
        // DHPrivateKeySpec carries no exponent-size L, so the spec-regenerated
        // PKCS#8 body differs from the original; the raw-encoding roundtrip below
        // is the byte-exact path
        DHPrivateKey fromPkcs8 = (DHPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(enc1));
        check(fromPkcs8.getX().equals(x) && "PKCS#8".equals(fromPkcs8.getFormat()),
                "PKCS#8 encoding roundtrip");
        check(Arrays.equals(fromPkcs8.getEncoded(), enc1)
                && fromPkcs8.getParams().getL() == params.getL(),
                "PKCS#8 roundtrip is byte-exact and preserves L");

        // KeyAgreement consumes the private key end-to-end
        KeyAgreement ka1 = KeyAgreement.getInstance("DiffieHellman");
        ka1.init(priv);
        ka1.doPhase(pub, true);
        byte[] s1 = ka1.generateSecret();
        check(s1.length == p.bitLength() / 8, "agreement secret length == p.bitLength()/8");

        // serialization roundtrip (KeyRep based) keeps x, params and usability
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(priv); }
        DHPrivateKey privD;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            privD = (DHPrivateKey) ois.readObject();
        }
        check(privD != null && privD.getX().equals(x) && privD.getParams().getP().equals(p),
                "deserialized private key keeps x and params");
        check("PKCS#8".equals(privD.getFormat()), "deserialized private key format");
        KeyAgreement ka2 = KeyAgreement.getInstance("DiffieHellman");
        ka2.init(privD);
        ka2.doPhase(pub, true);
        check(Arrays.equals(ka2.generateSecret(), s1), "deserialized key derives same secret");

        // equals/hashCode contract on regenerated instances
        check(priv.hashCode() == priv.hashCode(), "hashCode stable");
        check(fromSpec.hashCode() == priv.hashCode(), "equal keys share hashCode");
        KeyPair other = kpg.generateKeyPair();
        check(!priv.equals(other.getPrivate()), "distinct key pairs are not equal");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
