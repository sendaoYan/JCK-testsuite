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
        System.out.println("PASS (" + passed + " assertions)");
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
