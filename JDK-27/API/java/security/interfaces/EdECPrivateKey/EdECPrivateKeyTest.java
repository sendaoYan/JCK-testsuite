/*
 * @test
 * @summary java.security.interfaces.EdECPrivateKey conformance: white-box structure + black-box behavior.
 * @run main EdECPrivateKeyTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.util.*;
import javax.crypto.KeyAgreement;


public class EdECPrivateKeyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.EdECPrivateKey");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkInterfaces(c, new String[]{"java.security.interfaces.EdECKey", "java.security.PrivateKey"});
        checkMethods(c, new String[]{"getBytes", "getParams"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519");
        KeyPair kp = kpg.generateKeyPair();
        EdECPrivateKey priv = (EdECPrivateKey) kp.getPrivate();
        check(priv instanceof PrivateKey && priv instanceof EdECKey, "supertypes");
        check(priv.getParams().getName().equals("Ed25519"), "params name");
        check(priv.getFormat().equals("PKCS#8"), "PKCS#8 format");
        // raw secret bytes are exposed via the Optional, 32 bytes for Ed25519
        java.util.Optional<byte[]> bytes = priv.getBytes();
        check(bytes.isPresent(), "bytes present");
        check(bytes.get().length == 32, "32 raw bytes");
        // Ed448 uses 57 bytes
        EdECPrivateKey p448 = (EdECPrivateKey) KeyPairGenerator.getInstance("Ed448").generateKeyPair().getPrivate();
        check(p448.getBytes().isPresent() && p448.getBytes().get().length == 57, "57 bytes for Ed448");
        // sign/verify round trip driven purely by the interface view
        Signature sig = Signature.getInstance("Ed25519");
        sig.initSign(priv);
        byte[] msg = "jck-ed".getBytes("UTF-8");
        sig.update(msg);
        byte[] s = sig.sign();
        sig.initVerify(kp.getPublic());
        sig.update(msg);
        check(sig.verify(s), "signature verifies");
        check(s.length == 64, "64-byte Ed25519 signature");
        // KeyFactory round trip through PKCS#8 keeps the byte view
        KeyFactory kf = KeyFactory.getInstance("EdDSA");
        EdECPrivateKey back = (EdECPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(priv.getEncoded()));
        check(back.getBytes().isPresent() && java.util.Arrays.equals(back.getBytes().get(), bytes.get()),
            "PKCS#8 round trip bytes");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 3: contract — record impl, lattice, serialization, equals ----
    static void testContract() throws Exception {
        // record-style minimal implementation: Optional passthrough and Key surface
        EdECPrivateKey rec = new RecKey();
        check(rec.getBytes().isPresent() && rec.getBytes().get().length == 1,
                "record getBytes passthrough");
        check(!new RecEmptyKey().getBytes().isPresent(), "record empty Optional passthrough");
        check("EdDSA-REC".equals(rec.getAlgorithm()), "record getAlgorithm passthrough");
        check("PKCS#8-REC".equals(rec.getFormat()), "record getFormat passthrough");

        // lattice: inherits serialVersionUID from PrivateKey, does not declare its own
        check(PrivateKey.class.isAssignableFrom(EdECPrivateKey.class), "is a PrivateKey");
        check(EdECKey.class.isAssignableFrom(EdECPrivateKey.class), "is an EdECKey");
        check(Key.class.isAssignableFrom(EdECPrivateKey.class), "is a Key");
        check(Serializable.class.isAssignableFrom(EdECPrivateKey.class), "is Serializable");
        check(EdECPrivateKey.class.getField("serialVersionUID").getLong(null)
                == 6034044314589513430L, "inherits PrivateKey serialVersionUID value");
        try {
            EdECPrivateKey.class.getDeclaredField("serialVersionUID");
            check(false, "declares no own serialVersionUID");
        } catch (NoSuchFieldException e) {
            check(true, "declares no own serialVersionUID");
        }

        // real key: serialization fully equal, encoded present
        EdECPrivateKey key = (EdECPrivateKey) KeyPairGenerator.getInstance("Ed25519")
                .generateKeyPair().getPrivate();
        check(key.getEncoded() != null && key.getEncoded().length > 0, "encoded non-empty");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(key);
        Object copy = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(copy.equals(key), "serialization round-trip equals");
        check(key.equals(key), "reflexive equals");
        check(key.hashCode() == key.hashCode(), "hashCode stable");
    }

    static class RecKey implements EdECPrivateKey {
        public Optional<byte[]> getBytes() { return Optional.of(new byte[]{1}); }
        public NamedParameterSpec getParams() { return new NamedParameterSpec("Ed25519"); }
        public String getAlgorithm() { return "EdDSA-REC"; }
        public String getFormat() { return "PKCS#8-REC"; }
        public byte[] getEncoded() { return new byte[]{2}; }
    }

    static class RecEmptyKey implements EdECPrivateKey {
        public Optional<byte[]> getBytes() { return Optional.empty(); }
        public NamedParameterSpec getParams() { return new NamedParameterSpec("Ed25519"); }
        public String getAlgorithm() { return "EdDSA-REC"; }
        public String getFormat() { return "PKCS#8-REC"; }
        public byte[] getEncoded() { return new byte[]{2}; }
    }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    static void checkInterfaces(Class<?> c, String[] names) {
        for (String n : names) check(hasInterface(c, n), "implements " + n);
    }
    static void checkMethods(Class<?> c, String[] names) {
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }
    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static void checkEnumConstants(Class<?> c, String[] names) {
        Object[] cs = c.getEnumConstants();
        check(cs != null, "enum constants");
        check(cs.length == names.length, "enum constant count");
        for (String n : names) check(hasConst(cs, n), "constant " + n);
    }
    static boolean hasConst(Object[] cs, String name) {
        for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true;
        return false;
    }
}
