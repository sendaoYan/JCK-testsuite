/*
 * @test
 * @summary java.security.interfaces.EdECKey conformance: white-box structure + black-box behavior.
 * @run main EdECKeyTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.util.*;
import javax.crypto.KeyAgreement;


public class EdECKeyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.EdECKey");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkMethods(c, new String[]{"getParams"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // Ed25519 and Ed448 keys both report their named parameters
        KeyPairGenerator e25519 = KeyPairGenerator.getInstance("Ed25519");
        KeyPair p1 = e25519.generateKeyPair();
        EdECKey pub1 = (EdECKey) p1.getPublic();
        EdECKey priv1 = (EdECKey) p1.getPrivate();
        check(pub1.getParams() instanceof NamedParameterSpec, "Ed25519 named params");
        check(((NamedParameterSpec) pub1.getParams()).getName().equals("Ed25519"), "ED25519 parameter set");
        check(priv1.getParams().getName().equals("Ed25519"), "private side name");
        KeyPairGenerator e448 = KeyPairGenerator.getInstance("Ed448");
        KeyPair p2 = e448.generateKeyPair();
        EdECKey pub2 = (EdECKey) p2.getPublic();
        check(((NamedParameterSpec) pub2.getParams()).getName().equals("Ed448"), "ED448 parameter set");
        check(!((NamedParameterSpec) pub1.getParams()).getName()
            .equals(((NamedParameterSpec) pub2.getParams()).getName()), "distinct parameter sets");
        // explicit initialization with the constant selects the same curve
        KeyPairGenerator explicit = KeyPairGenerator.getInstance("EdDSA");
        explicit.initialize(NamedParameterSpec.ED448);
        check(((EdECKey) explicit.generateKeyPair().getPublic()).getParams().getName().equals("Ed448"),
            "explicit constant honored");
        // key sizes differ accordingly
        check(p1.getPublic().getEncoded().length < p2.getPublic().getEncoded().length, "Ed448 encodes larger");

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
        NamedParameterSpec np = new NamedParameterSpec("Ed25519");
        EdECKey rec = new RecEdECKey(np);
        check(rec.getParams() == np, "record getParams identity passthrough");
        check(new RecEdECKey(null).getParams() == null, "record null params passthrough");

        // lattice: bare interface, not a Key, not Serializable, no constant
        check(EdECKey.class.getInterfaces().length == 0, "no superinterfaces");
        check(!Key.class.isAssignableFrom(EdECKey.class), "not a Key");
        check(!Serializable.class.isAssignableFrom(EdECKey.class), "not Serializable");
        check(EdECKey.class.isAssignableFrom(EdECPublicKey.class), "EdECPublicKey is an EdECKey");
        check(EdECKey.class.isAssignableFrom(EdECPrivateKey.class), "EdECPrivateKey is an EdECKey");
        try {
            EdECKey.class.getDeclaredField("serialVersionUID");
            check(false, "no serialVersionUID declared");
        } catch (NoSuchFieldException e) {
            check(true, "no serialVersionUID declared");
        }

        // NamedParameterSpec does not override equals: a key's params instance is a
        // distinct object carrying the same name as the standard constant
        KeyPair kp = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
        NamedParameterSpec fromKey = (NamedParameterSpec) ((EdECKey) kp.getPublic()).getParams();
        check(fromKey != NamedParameterSpec.ED25519,
                "key params is a distinct instance from the constant");
        check(fromKey.getName().equals(NamedParameterSpec.ED25519.getName()),
                "same parameter name as the constant");

        // equals semantics on real halves
        check(kp.getPublic().equals(kp.getPublic()), "reflexive equals");
        check(!kp.getPublic().equals(kp.getPrivate()), "halves not equal");
        check(kp.getPublic().hashCode() == kp.getPublic().hashCode(), "hashCode stable");
    }

    static class RecEdECKey implements EdECKey {
        private final NamedParameterSpec params;
        RecEdECKey(NamedParameterSpec params) { this.params = params; }
        public NamedParameterSpec getParams() { return params; }
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
