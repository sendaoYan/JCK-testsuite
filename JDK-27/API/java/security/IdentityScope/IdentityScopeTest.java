/*
 * @test
 * @summary java.security.IdentityScope conformance: white-box structure + black-box behavior.
 * @run main IdentityScopeTest
 */

import java.lang.reflect.*;
import java.security.*;
import java.util.*;

public class IdentityScopeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
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
