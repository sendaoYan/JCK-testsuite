/*
 * @test
 * @summary java.security.Signer conformance: abstract class hierarchy and key methods;
 *          plus black-box contract: constructors (String)/(String,scope-with-notification),
 *          getPrivateKey initially null, setKeyPair installs both halves (same private
 *          instance across calls), setKeyPair(null) -> NPE (message null on JDK-8),
 *          half-null pairs -> InvalidParameterException, full replacement silent,
 *          no scope re-notification, name-based equals/hashCode inherited from Identity,
 *          toString "[[Signer]name]" and toString(true) detailed, serialization
 *          round-trip preserving name + both key halves.
 * @run main SignerTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Identity;
import java.security.IdentityScope;
import java.security.InvalidParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Principal;
import java.security.Signer;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

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

    static class MapScope extends IdentityScope {
        final Map<String, Identity> m = new LinkedHashMap<String, Identity>();
        int addCalls;
        MapScope(String name) { super(name); }
        public int size() { return m.size(); }
        public Identity getIdentity(String name) { return m.get(name); }
        public Identity getIdentity(java.security.PublicKey key) { return null; }
        public void addIdentity(Identity i) { addCalls++; m.put(i.getName(), i); }
        public void removeIdentity(Identity i) { m.remove(i.getName()); }
        public Enumeration<Identity> identities() { return Collections.enumeration(m.values()); }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.Signer");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == Class.forName("java.security.Identity"), "extends java.security.Identity");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getPrivateKey"), "declares getPrivateKey");
        check(ms.contains("setKeyPair"), "declares setKeyPair");
        check(ms.contains("toString"), "declares toString");

        // ---- constructor matrix ----
        check(java.lang.reflect.Modifier.isProtected(c.getDeclaredConstructor().getModifiers()),
                "no-arg constructor is protected");
        Signer s = new Signer("sig") {};
        check(s.getName().equals("sig"), "(String) ctor name");
        check(s.getScope() == null, "(String) ctor unscoped");
        check(s.getPrivateKey() == null, "private key initially null");
        check(s.getPublicKey() == null, "public key initially null");
        MapScope scope = new MapScope("sc");
        Signer scoped = new Signer("scoped", scope) {};
        check(scoped.getScope() == scope, "(String,scope) ctor wires scope");
        check(scope.addCalls == 1, "scope ctor notifies scope once");
        check(scope.getIdentity("scoped") == scoped, "scope sees signer by name");

        // ---- setKeyPair matrix ----
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(512);
        KeyPair kp = kpg.generateKeyPair();
        NullPointerException npe = null;
        try { s.setKeyPair(null); } catch (NullPointerException e) { npe = e; }
        check(npe != null, "setKeyPair(null) -> NullPointerException");
        check(npe.getMessage() == null, "JDK-8 NPE carries no detail message");
        check(s.getPrivateKey() == null, "failed setKeyPair leaves key unset");
        checkThrows(InvalidParameterException.class,
                () -> s.setKeyPair(new KeyPair(kp.getPublic(), null)),
                "setKeyPair(pub, null priv) -> InvalidParameterException");
        checkThrows(InvalidParameterException.class,
                () -> s.setKeyPair(new KeyPair(null, kp.getPrivate())),
                "setKeyPair(null pub, priv) -> InvalidParameterException");
        s.setKeyPair(kp);
        check(s.getPrivateKey() == kp.getPrivate(), "private key stored (same instance)");
        check(s.getPublicKey().equals(kp.getPublic()), "public key installed");
        check(s.getPrivateKey() == s.getPrivateKey(), "getPrivateKey stable across calls");
        KeyPair second = kpg.generateKeyPair();
        s.setKeyPair(second);
        check(s.getPrivateKey() == second.getPrivate()
                && s.getPublicKey().equals(second.getPublic()), "full pair replacement silent");
        check(scope.addCalls == 1, "setKeyPair does not re-notify scope");
        checkThrows(InvalidParameterException.class,
                () -> scoped.setKeyPair(new KeyPair(kp.getPublic(), null)),
                "half-null pair rejected for scoped signer too");

        // ---- inherited Identity contract on a Signer ----
        check(s instanceof Identity && s instanceof Principal && s instanceof java.io.Serializable,
                "Signer is Identity/Principal/Serializable");
        Signer sameName = new Signer("sig") {};
        sameName.setKeyPair(second);
        check(s.equals(sameName), "equals inherited: name-based");
        check(s.hashCode() == sameName.hashCode(), "hashCode consistent");
        Signer otherName = new Signer("other") {};
        check(!s.equals(otherName), "different name unequal");
        check(s.getInfo().equals("No further information available."),
                "inherited default info string");
        s.setInfo("signer info");
        check(s.getInfo().equals("signer info"), "setInfo inherited");

        // ---- toString ----
        check(s.toString().equals("[Signer]sig"), "toString format [Signer]name");
        check(s.toString(false).equals(s.toString()), "toString(false) == toString()");
        String detailed = s.toString(true);
        check(detailed.contains("sig") && detailed.contains("public and private keys initialized"),
                "toString(true) details both key halves");
        Signer keyless = new Signer("keyless") {};
        String d2 = keyless.toString(true);
        check(d2.contains("keyless") && d2.contains("no keys"), "toString(true) without keys prints \"no keys\"");

        // ---- serialization round-trip ----
        Signer ser = new Signer("serSigner") {};
        KeyPair serKp = kpg.generateKeyPair();
        ser.setKeyPair(serKp);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(ser); }
        Signer back = (Signer) new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back.getName().equals("serSigner"), "round-trip name preserved");
        check(back.getPrivateKey() != null && back.getPrivateKey().equals(serKp.getPrivate()),
                "round-trip private key preserved");
        check(back.getPublicKey() != null && back.getPublicKey().equals(serKp.getPublic()),
                "round-trip public key preserved");
        check(back.equals(ser), "round-trip equals original");
        check(back.getPrivateKey() instanceof java.security.PrivateKey,
                "round-trip private key still a PrivateKey");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
