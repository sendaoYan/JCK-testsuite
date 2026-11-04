/*
 * @test
 * @summary java.security.interfaces.EdECPublicKey conformance: white-box structure + black-box behavior.
 * @run main EdECPublicKeyTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.util.*;
import javax.crypto.KeyAgreement;


public class EdECPublicKeyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.EdECPublicKey");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkInterfaces(c, new String[]{"java.security.interfaces.EdECKey", "java.security.PublicKey"});
        checkMethods(c, new String[]{"getParams", "getPoint"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519");
        KeyPair kp = kpg.generateKeyPair();
        EdECPublicKey pub = (EdECPublicKey) kp.getPublic();
        check(pub instanceof PublicKey && pub instanceof EdECKey, "supertypes");
        // the point: y coordinate in range plus the x-parity bit
        EdECPoint pt = pub.getPoint();
        check(pt.getY().signum() >= 0, "y non-negative");
        check(pt.getY().bitLength() <= 255, "y fits in 255 bits");
        check(!pt.isXOdd() || pt.isXOdd(), "parity boolean readable");
        check(pub.getParams().getName().equals("Ed25519"), "params name");
        check(pub.getFormat().equals("X.509"), "X.509 format");
        // Ed448 coordinate space is larger
        EdECPublicKey p448 = (EdECPublicKey) KeyPairGenerator.getInstance("Ed448").generateKeyPair().getPublic();
        check(p448.getPoint().getY().bitLength() <= 455, "Ed448 y fits in 455 bits");
        // KeyFactory round trip from point + params preserves both components
        KeyFactory kf = KeyFactory.getInstance("EdDSA");
        EdECPublicKey back = (EdECPublicKey) kf.generatePublic(new EdECPublicKeySpec(pub.getParams(), pt));
        check(back.getPoint().getY().equals(pt.getY()), "round trip y");
        check(back.getPoint().isXOdd() == pt.isXOdd(), "round trip x parity");
        // a signature made by the private half verifies under this view
        Signature sig = Signature.getInstance("Ed25519");
        sig.initSign((EdECPrivateKey) kp.getPrivate());
        byte[] msg = "jck".getBytes("UTF-8");
        sig.update(msg);
        byte[] s = sig.sign();
        sig.initVerify(back);
        sig.update(msg);
        check(sig.verify(s), "round-trip key verifies");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 3: contract — record impl, lattice, serialization, equals ----
    static void testContract() throws Exception {
        // record-style minimal implementation: EdECPoint passthrough and Key surface
        EdECPoint pt = new EdECPoint(true, BigInteger.valueOf(12345));
        EdECPublicKey rec = new RecKey(pt);
        check(rec.getPoint() == pt, "record getPoint identity passthrough");
        check(rec.getPoint().isXOdd(), "record point xOdd passthrough");
        check(rec.getPoint().getY().equals(BigInteger.valueOf(12345)), "record point y passthrough");
        check("EdDSA-REC".equals(rec.getAlgorithm()), "record getAlgorithm passthrough");
        check("X.509-REC".equals(rec.getFormat()), "record getFormat passthrough");

        // lattice: inherits serialVersionUID from PublicKey, does not declare its own
        check(PublicKey.class.isAssignableFrom(EdECPublicKey.class), "is a PublicKey");
        check(EdECKey.class.isAssignableFrom(EdECPublicKey.class), "is an EdECKey");
        check(Key.class.isAssignableFrom(EdECPublicKey.class), "is a Key");
        check(Serializable.class.isAssignableFrom(EdECPublicKey.class), "is Serializable");
        check(EdECPublicKey.class.getField("serialVersionUID").getLong(null)
                == 7187392471159151072L, "inherits PublicKey serialVersionUID value");
        try {
            EdECPublicKey.class.getDeclaredField("serialVersionUID");
            check(false, "declares no own serialVersionUID");
        } catch (NoSuchFieldException e) {
            check(true, "declares no own serialVersionUID");
        }

        // real key: serialization fully equal
        KeyPair kp = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
        EdECPublicKey key = (EdECPublicKey) kp.getPublic();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(key);
        Object copy = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(copy.equals(key), "serialization round-trip equals");
        check(key.equals(key), "reflexive equals");
        check(!key.equals(kp.getPrivate()), "public not equal to private half");
        check(key.hashCode() == key.hashCode(), "hashCode stable");
    }

    static class RecKey implements EdECPublicKey {
        private final EdECPoint point;
        RecKey(EdECPoint point) { this.point = point; }
        public EdECPoint getPoint() { return point; }
        public NamedParameterSpec getParams() { return new NamedParameterSpec("Ed25519"); }
        public String getAlgorithm() { return "EdDSA-REC"; }
        public String getFormat() { return "X.509-REC"; }
        public byte[] getEncoded() { return new byte[]{3}; }
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
