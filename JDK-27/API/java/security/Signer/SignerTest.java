/*
 * @test
 * @summary java.security.Signer conformance: white-box structure + black-box behavior;
 *          deepened: protected no-arg ctor, (String,scope) ctor with scope notification,
 *          setKeyPair matrix (null -> NPE with helpful JDK-9+ message mentioning "pair",
 *          half-null pairs -> InvalidParameterException, stable same-instance private key,
 *          no scope re-notification), inherited name-based equals/hashCode and default
 *          info, toString "[Signer]name" / toString(true) "no keys" vs
 *          "public and private keys initialized", serialization round-trip of both halves.
 * @run main SignerTest
 */

import java.io.*;
import java.lang.reflect.*;
import java.security.*;
import java.util.*;

public class SignerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface ThrowingRunnable { void run() throws Throwable; }
    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but nothing was thrown");
    }

    /** Counting scope: observes construction-time notification only. */
    static class MapScope extends IdentityScope {
        final Map<String, Identity> m = new LinkedHashMap<>();
        int addCalls;
        MapScope(String name) { super(name); }
        public int size() { return m.size(); }
        public Identity getIdentity(String name) { return m.get(name); }
        public Identity getIdentity(PublicKey key) { return null; }
        public void addIdentity(Identity i) { addCalls++; m.put(i.getName(), i); }
        public void removeIdentity(Identity i) { m.remove(i.getName()); }
        public Enumeration<Identity> identities() { return Collections.enumeration(m.values()); }
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.Signer");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.security.Identity"), "superclass");
        checkMethods(c, new String[]{"getPrivateKey", "setKeyPair", "toString"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // Signer is an abstract subclass of Identity carrying a private key
        Signer s = new Signer("jck-signer") {};
        check(s.getName().equals("jck-signer"), "name");
        check(s.getPrivateKey() == null, "null key");
        check(s instanceof Identity && s instanceof Principal, "hierarchy");
        // setKeyPair installs both halves
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        s.setKeyPair(kp);
        check(s.getPrivateKey() == kp.getPrivate(), "private key");
        check(s.getPublicKey().equals(kp.getPublic()), "public key");
        // resetting with the same pair succeeds; JDK-27 replaces silently even with a different pair
        s.setKeyPair(new KeyPair(kp.getPublic(), kp.getPrivate()));
        check(s.getPrivateKey() != null, "same pair ok");
        KeyPair other = kpg.generateKeyPair();
        s.setKeyPair(other);
        check(s.getPrivateKey() == other.getPrivate() && s.getPublicKey().equals(other.getPublic()), "different pair replaces");
        // toString mentions the signer identity
        check(s.toString().contains("jck-signer"), "toString");
        // null key pair rejected
        try {
            s.setKeyPair(null);
            check(false, "null pair");
        } catch (NullPointerException | InvalidParameterException e) {
            check(true, "null pair rejected");
        }

    }

    // ---- Part 3: deepened contract matrix ----
    static void testContractMatrix() throws Exception {
        Class<?> c = Signer.class;
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(512);

        // constructor matrix
        check(Modifier.isProtected(c.getDeclaredConstructor().getModifiers()),
                "no-arg constructor is protected");
        Signer fresh = new Signer("fresh") {};
        check(fresh.getPrivateKey() == null && fresh.getPublicKey() == null,
                "both key halves initially null");
        MapScope scope = new MapScope("sc");
        Signer scoped = new Signer("scoped", scope) {};
        check(scoped.getScope() == scope, "(String,scope) ctor wires scope");
        check(scope.addCalls == 1, "scope notified exactly once at construction");
        check(scope.getIdentity("scoped") == scoped, "scope sees signer by name");

        // setKeyPair matrix
        Signer s = new Signer("sig") {};
        KeyPair kp = kpg.generateKeyPair();
        NullPointerException npe = null;
        try { s.setKeyPair(null); } catch (NullPointerException e) { npe = e; }
        check(npe != null, "setKeyPair(null) -> NullPointerException");
        check(npe.getMessage() != null && npe.getMessage().contains("pair"),
                "JDK-9+ helpful NPE message mentions the pair parameter");
        check(s.getPrivateKey() == null, "failed setKeyPair leaves keys unset");
        checkThrows(InvalidParameterException.class,
                () -> s.setKeyPair(new KeyPair(kp.getPublic(), null)),
                "setKeyPair(pub, null priv) -> InvalidParameterException");
        checkThrows(InvalidParameterException.class,
                () -> s.setKeyPair(new KeyPair(null, kp.getPrivate())),
                "setKeyPair(null pub, priv) -> InvalidParameterException");
        s.setKeyPair(kp);
        check(s.getPrivateKey() == kp.getPrivate(), "private key stored (same instance)");
        check(s.getPrivateKey() == s.getPrivateKey(), "getPrivateKey stable across calls");
        KeyPair second = kpg.generateKeyPair();
        s.setKeyPair(second);
        check(s.getPrivateKey() == second.getPrivate()
                && s.getPublicKey().equals(second.getPublic()), "full pair replacement silent");
        check(scope.addCalls == 1, "setKeyPair never re-notifies the scope");

        // inherited Identity contract
        check(s instanceof Identity && s instanceof Principal && s instanceof Serializable,
                "Signer is Identity/Principal/Serializable");
        Signer sameName = new Signer("sig") {};
        check(s.equals(sameName) && s.hashCode() == sameName.hashCode(),
                "inherited equals/hashCode are name-based");
        check(!s.equals(new Signer("other") {}), "different name unequal");
        check(s.getInfo().equals("No further information available."),
                "inherited default info string");
        s.setInfo("info");
        check(s.getInfo().equals("info"), "inherited setInfo");

        // toString formats
        check(s.toString().equals("[Signer]sig"), "toString format [Signer]name");
        check(s.toString(false).equals(s.toString()), "toString(false) == toString()");
        check(s.toString(true).contains("public and private keys initialized"),
                "toString(true) details both key halves");
        Signer keyless = new Signer("keyless") {};
        check(keyless.toString(true).contains("no keys"), "toString(true) keyless prints \"no keys\"");

        // serialization round-trip
        Signer ser = new Signer("serSigner") {};
        KeyPair serKp = kpg.generateKeyPair();
        ser.setKeyPair(serKp);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(ser); }
        Signer back = (Signer) new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back.getName().equals("serSigner"), "round-trip name");
        check(back.getPrivateKey() != null && back.getPrivateKey().equals(serKp.getPrivate()),
                "round-trip private key preserved");
        check(back.getPublicKey() != null && back.getPublicKey().equals(serKp.getPublic()),
                "round-trip public key preserved");
        check(back.equals(ser), "round-trip equals original");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractMatrix();
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
