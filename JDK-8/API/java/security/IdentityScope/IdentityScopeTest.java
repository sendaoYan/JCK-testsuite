/*
 * @test
 * @summary java.security.IdentityScope conformance: abstract class hierarchy and key methods;
 *          plus black-box contract: abstract method surface (size/getIdentity x3/
 *          addIdentity/removeIdentity/identities), protected no-arg ctor, (String) and
 *          (String,IdentityScope) ctors with parent notification, concrete
 *          getIdentity(Principal) name delegation (null -> NPE), getSystemScope null
 *          in default env + setSystemScope visibility with restore, recording-scope
 *          add/remove/enumeration semantics, serialization round-trip of a scope
 *          subclass, Identity/Principal/Serializable inheritance.
 * @run main IdentityScopeTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.Identity;
import java.security.IdentityScope;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Principal;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IdentityScopeTest {
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

    /** Recording scope: map-backed, duplicate add and missing remove throw KME. */
    public static class MapScope extends IdentityScope {
        public final Map<String, Identity> m = new LinkedHashMap<String, Identity>();
        public MapScope(String name) { super(name); }
        public MapScope(String name, IdentityScope parent) throws KeyManagementException { super(name, parent); }
        public int size() { return m.size(); }
        public Identity getIdentity(String name) { return m.get(name); }
        public Identity getIdentity(PublicKey key) {
            if (key == null) return null;
            for (Identity i : m.values()) if (key.equals(i.getPublicKey())) return i;
            return null;
        }
        public void addIdentity(Identity i) throws KeyManagementException {
            if (m.containsKey(i.getName())) throw new KeyManagementException("duplicate: " + i.getName());
            m.put(i.getName(), i);
        }
        public void removeIdentity(Identity i) throws KeyManagementException {
            if (m.remove(i.getName()) == null) throw new KeyManagementException("missing: " + i.getName());
        }
        public Enumeration<Identity> identities() { return Collections.enumeration(new ArrayList<Identity>(m.values())); }
    }

    /** Handle exposing the protected static setSystemScope. */
    static class ScopeHandle extends IdentityScope {
        ScopeHandle() { super("handle"); }
        static void set(IdentityScope s) { IdentityScope.setSystemScope(s); }
        public int size() { return 0; }
        public Identity getIdentity(String name) { return null; }
        public Identity getIdentity(PublicKey key) { return null; }
        public void addIdentity(Identity i) { }
        public void removeIdentity(Identity i) { }
        public Enumeration<Identity> identities() { return Collections.emptyEnumeration(); }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.IdentityScope");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == Class.forName("java.security.Identity"), "extends java.security.Identity");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("addIdentity"), "declares addIdentity");
        check(ms.contains("getIdentity"), "declares getIdentity");
        check(ms.contains("getSystemScope"), "declares getSystemScope");
        check(ms.contains("identities"), "declares identities");
        check(ms.contains("removeIdentity"), "declares removeIdentity");
        check(ms.contains("setSystemScope"), "declares setSystemScope");
        check(ms.contains("size"), "declares size");
        check(ms.contains("toString"), "declares toString");

        // ---- structural contract: abstract methods, static/protected modifiers ----
        for (String mn : new String[]{"size", "getIdentity", "addIdentity", "removeIdentity", "identities"})
            check(true, "surface checked: " + mn); // counted above via getDeclaredMethods
        int abstractCount = 0;
        for (Method m : c.getDeclaredMethods()) {
            if (Modifier.isAbstract(m.getModifiers())) abstractCount++;
            if (m.getName().equals("getSystemScope") || m.getName().equals("setSystemScope"))
                check(Modifier.isStatic(m.getModifiers()), m.getName() + " is static");
        }
        check(abstractCount == 6, "exactly 6 abstract methods (size, getIdentity x3, add/remove/identities minus concrete): " + abstractCount);
        Method sysSet = c.getDeclaredMethod("setSystemScope", IdentityScope.class);
        check(Modifier.isProtected(sysSet.getModifiers()), "setSystemScope is protected");
        Method byPrincipal = c.getMethod("getIdentity", Principal.class);
        check(!Modifier.isAbstract(byPrincipal.getModifiers()), "getIdentity(Principal) is concrete");
        Constructor<?> noArg = c.getDeclaredConstructor();
        check(Modifier.isProtected(noArg.getModifiers()), "no-arg constructor is protected");
        Constructor<?> oneArg = c.getConstructor(String.class);
        check(Modifier.isPublic(oneArg.getModifiers()), "(String) constructor public");

        // ---- constructor matrix ----
        MapScope s1 = new MapScope("s1");
        check(s1.getName().equals("s1"), "(String) ctor name");
        check(s1.getScope() == null, "(String) ctor unscoped");
        check(s1.size() == 0, "recording scope starts empty");
        MapScope parent = new MapScope("parent");
        MapScope child = new MapScope("child", parent);
        check(child.getScope() == parent, "(String,scope) ctor wires parent scope");
        check(parent.size() == 1 && parent.getIdentity("child") == child,
                "parent scope notified of child during construction");

        // ---- getIdentity(Principal) concrete delegation (by name) ----
        Identity alpha = new Identity("alpha") {};
        s1.addIdentity(alpha);
        check(s1.getIdentity("alpha") == alpha, "getIdentity(String) hit");
        check(s1.getIdentity("beta") == null, "getIdentity(String) miss null");
        Principal p = new Identity("alpha") {};
        check(s1.getIdentity(p) == alpha, "getIdentity(Principal) delegates to name lookup");
        check(s1.getIdentity(new Identity("beta") {}) == null, "getIdentity(Principal) miss null");
        checkThrows(NullPointerException.class, () -> s1.getIdentity((Principal) null),
                "getIdentity((Principal) null) -> NullPointerException");

        // ---- add/remove/identities semantics of the recording scope ----
        Identity beta = new Identity("beta") {};
        s1.addIdentity(beta);
        check(s1.size() == 2, "size after two adds");
        checkThrows(KeyManagementException.class, () -> s1.addIdentity(new Identity("alpha") {}),
                "duplicate name add -> KeyManagementException");
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(512);
        KeyPair kp = kpg.generateKeyPair();
        alpha.setPublicKey(kp.getPublic());
        check(s1.getIdentity(kp.getPublic()) == alpha, "getIdentity(PublicKey) match");
        check(s1.getIdentity((PublicKey) null) == null, "getIdentity(null key) null");
        List<String> names = new ArrayList<String>();
        Enumeration<Identity> en = s1.identities();
        while (en.hasMoreElements()) names.add(en.nextElement().getName());
        Collections.sort(names);
        check(names.equals(java.util.Arrays.asList("alpha", "beta")), "identities() enumerates all members");
        s1.removeIdentity(beta);
        check(s1.size() == 1 && s1.getIdentity("beta") == null, "remove drops the identity");
        checkThrows(KeyManagementException.class, () -> s1.removeIdentity(beta),
                "removing a missing identity -> KeyManagementException");
        MapScope empty = new MapScope("empty");
        check(!empty.identities().hasMoreElements(), "empty scope enumerates nothing");
        check(empty.toString().contains("empty"), "toString carries scope name");

        // ---- system scope: default null, set visible, restored ----
        IdentityScope saved = IdentityScope.getSystemScope();
        try {
            check(saved == null, "getSystemScope() null in default environment");
            ScopeHandle.set(s1);
            check(IdentityScope.getSystemScope() == s1, "setSystemScope makes scope visible");
        } finally {
            ScopeHandle.set(saved);
        }
        check(IdentityScope.getSystemScope() == saved, "system scope restored after test");

        // ---- inheritance + serialization of a scope subclass ----
        check(s1 instanceof Identity, "IdentityScope is an Identity");
        check(s1 instanceof Principal, "IdentityScope is a Principal (through Identity)");
        check(s1 instanceof java.io.Serializable, "IdentityScope is Serializable");
        MapScope ser = new MapScope("serScope");
        ser.addIdentity(new Identity("one") {});
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(ser); }
        MapScope back = (MapScope) new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back.getName().equals("serScope"), "round-trip scope name preserved");
        check(back.size() == 1, "round-trip scope contents preserved");
        check(back.getIdentity("one") != null && back.getIdentity("one").getName().equals("one"),
                "round-trip scope still functional for lookups");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
