/*
 * @test
 * @summary java.security.interfaces.XECKey conformance: white-box structure + black-box behavior.
 * @run main XECKeyTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.util.*;
import javax.crypto.KeyAgreement;


public class XECKeyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.XECKey");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkMethods(c, new String[]{"getParams"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // X25519 and X448 report their named parameters
        KeyPairGenerator x25519 = KeyPairGenerator.getInstance("XDH");
        x25519.initialize(NamedParameterSpec.X25519);
        KeyPair p1 = x25519.generateKeyPair();
        XECKey pub1 = (XECKey) p1.getPublic();
        XECKey priv1 = (XECKey) p1.getPrivate();
        check(pub1.getParams() instanceof NamedParameterSpec, "named params");
        check(((NamedParameterSpec) pub1.getParams()).getName().equals("X25519"), "X25519 name");
        check(((NamedParameterSpec) priv1.getParams()).getName().equals("X25519"), "private side name");
        KeyPairGenerator x448 = KeyPairGenerator.getInstance("XDH");
        x448.initialize(NamedParameterSpec.X448);
        KeyPair p2 = x448.generateKeyPair();
        check(((NamedParameterSpec) ((XECKey) p2.getPublic()).getParams()).getName().equals("X448"), "X448 name");
        check(!((NamedParameterSpec) pub1.getParams()).getName()
            .equals(((NamedParameterSpec) ((XECKey) p2.getPublic()).getParams()).getName()),
            "distinct parameter sets");
        // encoding sizes reflect the curve
        check(p1.getPublic().getEncoded().length < p2.getPublic().getEncoded().length, "X448 encodes larger");
        // both halves share the same parameter object semantics
        check(((NamedParameterSpec) pub1.getParams()).getName()
            .equals(((NamedParameterSpec) priv1.getParams()).getName()), "same params both halves");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 3: contract — record impl, lattice, constant identity semantics ----
    static void testContract() throws Exception {
        // record-style minimal implementation: getParams passes the holder value through
        AlgorithmParameterSpec np = new NamedParameterSpec("X25519");
        XECKey rec = new RecXECKey(np);
        check(rec.getParams() == np, "record getParams identity passthrough");
        check(new RecXECKey(null).getParams() == null, "record null params passthrough");

        // lattice: bare interface, not a Key, not Serializable, no constant
        check(XECKey.class.getInterfaces().length == 0, "no superinterfaces");
        check(!Key.class.isAssignableFrom(XECKey.class), "not a Key");
        check(!Serializable.class.isAssignableFrom(XECKey.class), "not Serializable");
        check(XECKey.class.isAssignableFrom(XECPublicKey.class), "XECPublicKey is an XECKey");
        check(XECKey.class.isAssignableFrom(XECPrivateKey.class), "XECPrivateKey is an XECKey");
        try {
            XECKey.class.getDeclaredField("serialVersionUID");
            check(false, "no serialVersionUID declared");
        } catch (NoSuchFieldException e) {
            check(true, "no serialVersionUID declared");
        }

        // a real key's params instance is distinct from the constant but carries its name
        KeyPair kp = KeyPairGenerator.getInstance("XDH").generateKeyPair();
        NamedParameterSpec fromKey = (NamedParameterSpec) ((XECKey) kp.getPublic()).getParams();
        check(fromKey != NamedParameterSpec.X25519,
                "key params is a distinct instance from the constant");
        check(fromKey.getName().equals(NamedParameterSpec.X25519.getName()),
                "same parameter name as the constant");

        // equals semantics on real halves
        check(kp.getPublic().equals(kp.getPublic()), "reflexive equals");
        check(!kp.getPublic().equals(kp.getPrivate()), "halves not equal");
        check(kp.getPublic().hashCode() == kp.getPublic().hashCode(), "hashCode stable");
    }

    static class RecXECKey implements XECKey {
        private final AlgorithmParameterSpec params;
        RecXECKey(AlgorithmParameterSpec params) { this.params = params; }
        public AlgorithmParameterSpec getParams() { return params; }
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
