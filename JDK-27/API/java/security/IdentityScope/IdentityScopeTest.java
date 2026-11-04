/*
 * @test
 * @summary java.security.IdentityScope conformance: white-box structure + black-box behavior;
 *          deepened: structural metadata (6 abstract methods, protected no-arg ctor,
 *          static system-scope accessors, protected setSystemScope), (String,scope) ctor
 *          parent notification, getIdentity(Principal) name delegation + null -> NPE,
 *          getSystemScope null default with setSystemScope round-trip restore,
 *          serialization round-trip of a scope subclass, empty-scope enumeration.
 * @run main IdentityScopeTest
 */

import java.io.*;
import java.lang.reflect.*;
import java.security.*;
import java.util.*;

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

    /** Scope subclass exposing the protected static setSystemScope. */
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

    /** Serializable recording scope for round-trip tests. */
    public static class SerScope extends IdentityScope {
        public final Map<String, Identity> m = new LinkedHashMap<>();
        public SerScope(String name) { super(name); }
        public int size() { return m.size(); }
        public Identity getIdentity(String name) { return m.get(name); }
        public Identity getIdentity(PublicKey key) { return null; }
        public void addIdentity(Identity i) throws KeyManagementException { m.put(i.getName(), i); }
        public void removeIdentity(Identity i) throws KeyManagementException { m.remove(i.getName()); }
        public Enumeration<Identity> identities() { return Collections.enumeration(new ArrayList<>(m.values())); }
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.IdentityScope");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.security.Identity"), "superclass");
        checkMethods(c, new String[]{"addIdentity", "getIdentity", "getSystemScope", "identities", "removeIdentity", "size", "toString"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // IdentityScope is abstract with a system-scope concept
        check(IdentityScope.getSystemScope() == null, "system scope null");
        // concrete in-memory scope implementation drives the contract
        class MapScope extends IdentityScope {
            final Map<String, Identity> byName = new LinkedHashMap<>();
            MapScope(String name) { super(name); }
            public int size() { return byName.size(); }
            public Identity getIdentity(String name) { return byName.get(name); }
            public Identity getIdentity(PublicKey key) {
                if (key == null) return null;
                for (Identity i : byName.values()) if (key.equals(i.getPublicKey())) return i;
                return null;
            }
            public void addIdentity(Identity i) throws KeyManagementException {
                if (byName.putIfAbsent(i.getName(), i) != null) throw new KeyManagementException("duplicate");
            }
            public void removeIdentity(Identity i) throws KeyManagementException {
                if (byName.remove(i.getName()) == null) throw new KeyManagementException("missing");
            }
            public Enumeration<Identity> identities() { return Collections.enumeration(byName.values()); }
        }
        MapScope scope = new MapScope("jck-scope");
        check(scope.size() == 0, "size 0");
        check(scope.getIdentity("nobody") == null, "lookup miss");
        Identity a = new Identity("alpha") {};
        Identity b = new Identity("beta") {};
        scope.addIdentity(a);
        scope.addIdentity(b);
        check(scope.size() == 2, "size 2");
        check(scope.getIdentity("alpha") == a, "by name");
        Principal pa = a; check(scope.getIdentity(pa) == a, "by principal");
        // duplicate add rejected by our scope
        try {
            scope.addIdentity(new Identity("alpha") {});
            check(false, "duplicate throws");
        } catch (KeyManagementException kme) {
            check(true, "duplicate KeyManagementException");
        }
        // by-key lookup with a real key
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        a.setPublicKey(kp.getPublic());
        check(scope.getIdentity(kp.getPublic()) == a, "by key");
        check(scope.getIdentity((PublicKey) null) == null, "null key miss");
        // enumerate identities (sorted for determinism)
        List<String> names = new ArrayList<>();
        for (Enumeration<Identity> e = scope.identities(); e.hasMoreElements(); ) names.add(e.nextElement().getName());
        Collections.sort(names);
        check(names.equals(java.util.Arrays.asList("alpha", "beta")), "identities enumeration");
        // remove
        scope.removeIdentity(b);
        check(scope.size() == 1 && scope.getIdentity("beta") == null, "after remove");
        try {
            scope.removeIdentity(b);
            check(false, "remove missing throws");
        } catch (KeyManagementException kme) {
            check(true, "remove missing");
        }
        // toString contains name
        check(scope.toString().contains("jck-scope"), "toString");
        // IdentityScope is an Identity
        check(scope instanceof Identity, "scope is Identity");

    }

    // ---- Part 3: deepened contract matrix ----
    static void testContractMatrix() throws Exception {
        Class<?> c = IdentityScope.class;

        // structural metadata
        int abstractCount = 0;
        for (Method m : c.getDeclaredMethods()) {
            if (Modifier.isAbstract(m.getModifiers())) abstractCount++;
            if (m.getName().equals("getSystemScope") || m.getName().equals("setSystemScope"))
                check(Modifier.isStatic(m.getModifiers()), m.getName() + " is static");
        }
        check(abstractCount == 6, "exactly 6 abstract methods (size, getIdentity x3 abstract, add, remove, identities): " + abstractCount);
        check(Modifier.isProtected(c.getDeclaredMethod("setSystemScope", IdentityScope.class).getModifiers()),
                "setSystemScope is protected");
        check(!Modifier.isAbstract(c.getMethod("getIdentity", Principal.class).getModifiers()),
                "getIdentity(Principal) is concrete (delegates by name)");
        check(Modifier.isProtected(c.getDeclaredConstructor().getModifiers()),
                "no-arg constructor is protected");

        // (String, scope) ctor notifies parent
        class NScope extends IdentityScope {
            final Map<String, Identity> m = new LinkedHashMap<>();
            int addCalls;
            NScope(String n) { super(n); }
            NScope(String n, IdentityScope p) throws KeyManagementException { super(n, p); }
            public int size() { return m.size(); }
            public Identity getIdentity(String name) { return m.get(name); }
            public Identity getIdentity(PublicKey key) { return null; }
            public void addIdentity(Identity i) { addCalls++; m.put(i.getName(), i); }
            public void removeIdentity(Identity i) { m.remove(i.getName()); }
            public Enumeration<Identity> identities() { return Collections.enumeration(m.values()); }
        }
        NScope parent = new NScope("parent");
        NScope child = new NScope("child", parent) { };
        check(child.getScope() == parent, "nested ctor wires parent scope");
        check(parent.addCalls == 1 && parent.getIdentity("child") == child,
                "parent scope notified of the child at construction");

        // getIdentity(Principal) delegation
        NScope sc = new NScope("sc");
        Identity alpha = new Identity("alpha") {};
        sc.addIdentity(alpha);
        check(sc.getIdentity((Principal) new Identity("alpha") {}) == alpha,
                "getIdentity(Principal) delegates to the name lookup");
        check(sc.getIdentity((Principal) new Identity("beta") {}) == null,
                "getIdentity(Principal) miss null");
        checkThrows(NullPointerException.class, () -> sc.getIdentity((Principal) null),
                "getIdentity((Principal) null) -> NullPointerException");

        // system scope: default null, set visible, restored (finally!)
        IdentityScope saved = IdentityScope.getSystemScope();
        try {
            check(saved == null, "getSystemScope() null in default environment");
            ScopeHandle.set(sc);
            check(IdentityScope.getSystemScope() == sc, "setSystemScope makes scope visible");
        } finally {
            ScopeHandle.set(saved);
        }
        check(IdentityScope.getSystemScope() == saved, "system scope restored after test");

        // empty scope enumeration
        NScope empty = new NScope("empty");
        check(empty.size() == 0 && !empty.identities().hasMoreElements(),
                "empty scope: size 0, empty enumeration");
        check(empty.toString().contains("empty"), "toString carries scope name");

        // inheritance + serialization round-trip of the scope itself
        check(empty instanceof Identity && empty instanceof Principal && empty instanceof Serializable,
                "IdentityScope is Identity/Principal/Serializable");
        SerScope ser = new SerScope("serScope");
        ser.addIdentity(new Identity("one") {});
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(ser); }
        SerScope back = (SerScope) new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back.getName().equals("serScope"), "round-trip scope name");
        check(back.size() == 1 && back.getIdentity("one") != null,
                "round-trip scope contents and lookup still work");
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
