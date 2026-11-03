/*
 * @test
 * @summary java.security.Identity conformance: white-box structure + black-box behavior.
 * @run main IdentityTest
 */

import java.lang.reflect.*;
import java.security.*;
import java.util.*;

public class IdentityTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.Identity");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        checkInterfaces(c, new String[]{"java.security.Principal", "java.io.Serializable"});
        checkMethods(c, new String[]{"addCertificate", "certificates", "equals", "getInfo", "getName", "getPublicKey", "getScope", "hashCode", "removeCertificate", "setInfo", "setPublicKey", "toString"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // Identity is abstract; concrete anonymous subclass
        Identity id = new Identity("jck-identity") {};
        check(id.getName().equals("jck-identity"), "name");
        check(id.getScope() == null, "no scope");
        check(id.getPublicKey() == null, "no key");
        check(id.getInfo() != null, "default info non-null");
        check(id.toString().contains("jck-identity"), "toString");
        // info string round trip
        id.setInfo("jck test identity info");
        check(id.getInfo().equals("jck test identity info"), "info");
        // real key round trip
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        id.setPublicKey(kp.getPublic());
        check(id.getPublicKey().equals(kp.getPublic()), "public key");
        // JDK-27: setPublicKey is an unchecked setter - a different key simply replaces
        KeyPairGenerator kpg2 = KeyPairGenerator.getInstance("RSA");
        kpg2.initialize(2048);
        PublicKey replacementKey = kpg2.generateKeyPair().getPublic();
        id.setPublicKey(replacementKey);
        check(id.getPublicKey().equals(replacementKey), "different key replaces silently");
        id.setPublicKey(null);
        check(id.getPublicKey() == null, "null key clears silently");
        check(id.getName().equals("jck-identity"), "name unchanged");
        // certificates(): deprecated java.security.Certificate list; none initially
        check(id.certificates() == null || id.certificates().length == 0, "no certs");
        // equality by name and public key
        Identity other = new Identity("jck-identity") {};
        other.setPublicKey(kp.getPublic());
        check(id.equals(other), "identityEquals by name+key");
        // same-name unscoped identities are equal via fullName(), regardless of keys
        Identity otherKey = new Identity("jck-identity") {};
        check(id.equals(otherKey), "same name equal regardless of key");
        Identity otherName = new Identity("different") {};
        otherName.setPublicKey(kp.getPublic());
        check(!id.equals(otherName), "different name unequal");
        // Identity implements Principal
        check(id instanceof Principal, "Identity is Principal");
        // identityEquals via subclass with different scopes constructor
        Identity scoped = new Identity("scoped") {};
        check(scoped.getName().equals("scoped"), "scoped name");

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
