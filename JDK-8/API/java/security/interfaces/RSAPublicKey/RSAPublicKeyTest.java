/*
 * @test
 * @summary java.security.interfaces.RSAPublicKey conformance: interface hierarchy and key methods,
 *          serialVersionUID -8727434096241101194L, record-style implementation passthrough,
 *          supertype lattice (RSAKey + PublicKey + Key + Serializable), real RSA-2048 public
 *          key contracts: standard exponent 65537, X.509 format, RSAPublicKeySpec and
 *          X509EncodedKeySpec round-trips equal, serialization round-trip, signature verify
 *          closed loop.
 * @run main RSAPublicKeyTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSAPublicKeyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.RSAPublicKey");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getPublicExponent"), "declares getPublicExponent");

        testContract();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 2: contract — constant, record impl, real-key closed loops ----
    static void testContract() throws Exception {
        // serialVersionUID is a fixed public contract value
        check(RSAPublicKey.class.getField("serialVersionUID").getLong(null)
                == -8727434096241101194L, "serialVersionUID == -8727434096241101194L");

        // record-style minimal implementation
        RSAPublicKey rec = new RecKey();
        check(rec.getPublicExponent().equals(BigInteger.valueOf(65537)), "record e passthrough");
        check(rec.getModulus().equals(BigInteger.valueOf(3233)), "record modulus passthrough");
        check("RSA-REC".equals(rec.getAlgorithm()), "record getAlgorithm passthrough");
        check("X.509-REC".equals(rec.getFormat()), "record getFormat passthrough");
        check(rec.getEncoded() != null && rec.getEncoded().length == 1, "record getEncoded passthrough");

        // supertype lattice
        check(RSAKey.class.isAssignableFrom(RSAPublicKey.class), "is an RSAKey");
        check(PublicKey.class.isAssignableFrom(RSAPublicKey.class), "is a PublicKey");
        check(Key.class.isAssignableFrom(RSAPublicKey.class), "is a Key");
        check(Serializable.class.isAssignableFrom(RSAPublicKey.class), "is Serializable");
        check(RSAPublicKey.class.getInterfaces().length == 2, "exactly two direct superinterfaces");

        // real generated key
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        RSAPublicKey key = (RSAPublicKey) kp.getPublic();
        check(key instanceof RSAKey && key instanceof PublicKey, "real key supertypes");
        check("RSA".equals(key.getAlgorithm()), "getAlgorithm is RSA");
        check("X.509".equals(key.getFormat()), "getFormat is X.509");
        check(key.getEncoded() != null && key.getEncoded().length > 0, "encoded non-empty");
        check(key.getModulus().bitLength() == 2048, "modulus is 2048 bits");
        check(key.getPublicExponent().intValue() == 65537, "SunRsaSign standard exponent 65537");
        check(key.getPublicExponent().signum() > 0
                && key.getPublicExponent().compareTo(key.getModulus()) < 0, "e in (0, n)");
        check(key.getPublicExponent().testBit(0), "e odd (coprime with phi)");

        // KeyFactory round-trips restore an equal key
        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPublicKeySpec spec = kf.getKeySpec(key, RSAPublicKeySpec.class);
        check(spec.getModulus().equals(key.getModulus()), "spec preserves modulus");
        check(spec.getPublicExponent().equals(key.getPublicExponent()), "spec preserves e");
        RSAPublicKey back = (RSAPublicKey) kf.generatePublic(
                new RSAPublicKeySpec(spec.getModulus(), spec.getPublicExponent()));
        check(back.equals(key), "RSAPublicKeySpec round-trip equals");
        RSAPublicKey x509 = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(key.getEncoded()));
        check(x509.equals(key), "X509EncodedKeySpec round-trip equals");

        // serialization round-trip
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(key);
        Object copy = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(copy instanceof RSAPublicKey, "deserialized still an RSAPublicKey");
        check(copy.equals(key), "serialization round-trip equals");

        // signature closed loop through the interface reference
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign((RSAPrivateKey) kp.getPrivate());
        byte[] msg = "interfaces-RSA".getBytes("UTF-8");
        sig.update(msg);
        byte[] s = sig.sign();
        sig.initVerify(key);
        sig.update(msg);
        check(sig.verify(s), "verify with RSAPublicKey succeeds");
        sig.update("other".getBytes("UTF-8"));
        check(!sig.verify(s), "verify fails for different message");

        // equals semantics
        check(key.equals(key), "reflexive equals");
        check(!key.equals(kp.getPrivate()), "public not equal to private half");
        check(key.hashCode() == key.hashCode(), "hashCode stable");
    }

    static class RecKey implements RSAPublicKey {
        public BigInteger getPublicExponent() { return BigInteger.valueOf(65537); }
        public BigInteger getModulus() { return BigInteger.valueOf(3233); }
        public String getAlgorithm() { return "RSA-REC"; }
        public String getFormat() { return "X.509-REC"; }
        public byte[] getEncoded() { return new byte[]{5}; }
    }
}
