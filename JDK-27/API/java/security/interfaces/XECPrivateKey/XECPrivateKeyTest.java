/*
 * @test
 * @summary java.security.interfaces.XECPrivateKey conformance: white-box structure + black-box behavior.
 * @run main XECPrivateKeyTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.util.*;
import javax.crypto.KeyAgreement;


public class XECPrivateKeyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.XECPrivateKey");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkInterfaces(c, new String[]{"java.security.interfaces.XECKey", "java.security.PrivateKey"});
        checkMethods(c, new String[]{"getParams", "getScalar"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("XDH");
        kpg.initialize(NamedParameterSpec.X25519);
        KeyPair kp = kpg.generateKeyPair();
        XECPrivateKey priv = (XECPrivateKey) kp.getPrivate();
        check(priv instanceof PrivateKey && priv instanceof XECKey, "supertypes");
        check(priv.getParams() instanceof NamedParameterSpec, "params present");
        check(priv.getFormat().equals("PKCS#8"), "PKCS#8 format");
        // scalar exposed as Optional bytes, 32 for X25519 and 56 for X448
        java.util.Optional<byte[]> scalar = priv.getScalar();
        check(scalar.isPresent(), "scalar present");
        check(scalar.get().length == 32, "32-byte X25519 scalar");
        KeyPairGenerator g448 = KeyPairGenerator.getInstance("XDH");
        g448.initialize(NamedParameterSpec.X448);
        XECPrivateKey k448 = (XECPrivateKey) g448.generateKeyPair().getPrivate();
        check(k448.getScalar().isPresent() && k448.getScalar().get().length == 56, "56-byte X448 scalar");
        // a real key agreement driven through the interface view
        KeyAgreement ka = KeyAgreement.getInstance("XDH");
        ka.init(priv);
        ka.doPhase(kp.getPublic(), true);
        check(ka.generateSecret().length == 32, "32-byte shared secret");
        // KeyFactory round trip from params + scalar
        KeyFactory kf = KeyFactory.getInstance("XDH");
        XECPrivateKey back = (XECPrivateKey) kf.generatePrivate(new XECPrivateKeySpec(priv.getParams(), scalar.get()));
        check(back.getScalar().isPresent() && java.util.Arrays.equals(back.getScalar().get(), scalar.get()),
            "round trip scalar");

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
        XECPrivateKey rec = new RecKey();
        check(rec.getScalar().isPresent() && rec.getScalar().get().length == 1,
                "record getScalar passthrough");
        check(!new RecEmptyKey().getScalar().isPresent(), "record empty Optional passthrough");
        check("XDH-REC".equals(rec.getAlgorithm()), "record getAlgorithm passthrough");
        check("PKCS#8-REC".equals(rec.getFormat()), "record getFormat passthrough");

        // lattice: inherits serialVersionUID from PrivateKey, does not declare its own
        check(PrivateKey.class.isAssignableFrom(XECPrivateKey.class), "is a PrivateKey");
        check(XECKey.class.isAssignableFrom(XECPrivateKey.class), "is an XECKey");
        check(Key.class.isAssignableFrom(XECPrivateKey.class), "is a Key");
        check(Serializable.class.isAssignableFrom(XECPrivateKey.class), "is Serializable");
        check(XECPrivateKey.class.getField("serialVersionUID").getLong(null)
                == 6034044314589513430L, "inherits PrivateKey serialVersionUID value");
        try {
            XECPrivateKey.class.getDeclaredField("serialVersionUID");
            check(false, "declares no own serialVersionUID");
        } catch (NoSuchFieldException e) {
            check(true, "declares no own serialVersionUID");
        }

        // real key: serialization fully equal
        XECPrivateKey key = (XECPrivateKey) KeyPairGenerator.getInstance("XDH")
                .generateKeyPair().getPrivate();
        check(key.getEncoded() != null && key.getEncoded().length > 0, "encoded non-empty");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(key);
        Object copy = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(copy.equals(key), "serialization round-trip equals");
        check(key.equals(key), "reflexive equals");
        check(key.hashCode() == key.hashCode(), "hashCode stable");
    }

    static class RecKey implements XECPrivateKey {
        public Optional<byte[]> getScalar() { return Optional.of(new byte[]{1}); }
        public AlgorithmParameterSpec getParams() { return new NamedParameterSpec("X25519"); }
        public String getAlgorithm() { return "XDH-REC"; }
        public String getFormat() { return "PKCS#8-REC"; }
        public byte[] getEncoded() { return new byte[]{2}; }
    }

    static class RecEmptyKey implements XECPrivateKey {
        public Optional<byte[]> getScalar() { return Optional.empty(); }
        public AlgorithmParameterSpec getParams() { return new NamedParameterSpec("X25519"); }
        public String getAlgorithm() { return "XDH-REC"; }
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
