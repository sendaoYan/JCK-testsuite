/*
 * @test
 * @summary java.security.Identity conformance: white-box structure + black-box behavior;
 *          deepened: constructor matrix (null name, scope notification, dup-name KME),
 *          final getName/getScope/equals, default info string, certificate matrix
 *          (null -> NPE, key mismatch -> KME, duplicates kept, silent removes),
 *          name-based equals/hashCode across scopes, null-name equals -> NPE,
 *          identityEquals, toString() == name / toString(true) detailed,
 *          scope notified only at construction, serialization round-trip.
 * @run main IdentityTest
 */

import java.io.*;
import java.lang.reflect.*;
import java.security.*;
import java.util.*;

public class IdentityTest {
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

    /** Recording java.security.Certificate (deprecated interface), serializable. */
    static class RecCert implements Certificate, Serializable {
        private final PublicKey k;
        RecCert(PublicKey k) { this.k = k; }
        public Principal getGuarantor() { return null; }
        public Principal getPrincipal() { return null; }
        public PublicKey getPublicKey() { return k; }
        public void encode(OutputStream out) {}
        public void decode(InputStream in) {}
        public String getFormat() { return "REC"; }
        public String toString(boolean detailed) { return "RecCert"; }
    }

    /** Counting scope: observes construction-time notification only. */
    static class MapScope extends IdentityScope {
        final Map<String, Identity> m = new LinkedHashMap<>();
        int addCalls;
        MapScope(String name) { super(name); }
        public int size() { return m.size(); }
        public Identity getIdentity(String name) { return m.get(name); }
        public Identity getIdentity(PublicKey key) { return null; }
        public void addIdentity(Identity i) throws KeyManagementException { addCalls++; m.put(i.getName(), i); }
        public void removeIdentity(Identity i) { m.remove(i.getName()); }
        public Enumeration<Identity> identities() { return Collections.enumeration(m.values()); }
    }

    /** Strict scope rejecting duplicate names: exercises the ctor KME path. */
    static class StrictScope extends MapScope {
        StrictScope(String n) { super(n); }
        public void addIdentity(Identity i) throws KeyManagementException {
            if (m.containsKey(i.getName())) throw new KeyManagementException("dup");
            super.addIdentity(i);
        }
    }

    /** Named subclass exposing protected identityEquals. */
    static class EqIdentity extends Identity {
        EqIdentity(String n) { super(n); }
        boolean eq(Identity o) { return identityEquals(o); }
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

    // ---- Part 3: deepened contract matrix ----
    static void testContractMatrix() throws Exception {
        Class<?> c = Identity.class;
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(512);
        KeyPair kp = kpg.generateKeyPair();
        KeyPair otherPair = kpg.generateKeyPair();

        // constructor matrix
        check(Modifier.isProtected(c.getDeclaredConstructor().getModifiers()), "no-arg ctor protected");
        Identity nullName = new Identity((String) null) {};
        check(nullName.getName() == null, "null name accepted");
        check(nullName.toString() == null, "toString of null-name identity returns null");
        MapScope scope = new MapScope("scope1");
        Identity scoped = new Identity("scoped", scope) {};
        check(scoped.getScope() == scope, "scope ctor wires getScope");
        check(scope.addCalls == 1, "scope notified exactly once at construction");
        check(scope.getIdentity("scoped") == scoped, "scope sees identity by name");
        StrictScope strict = new StrictScope("strict");
        new Identity("dup", strict) {};
        checkThrows(KeyManagementException.class, () -> new Identity("dup", strict) {},
                "dup-name ctor propagates scope's KeyManagementException");

        // final methods lock name/scope/equals
        check(Modifier.isFinal(c.getMethod("getName").getModifiers()), "getName final");
        check(Modifier.isFinal(c.getMethod("getScope").getModifiers()), "getScope final");
        check(Modifier.isFinal(c.getMethod("equals", Object.class).getModifiers()), "equals final");

        // setInfo null + default
        check(new Identity("d") {}.getInfo().equals("No further information available."),
                "default info string exact");
        Identity info = new Identity("info") {};
        info.setInfo(null);
        check(info.getInfo() == null, "setInfo(null) stores null");

        // certificates matrix
        Identity kv = new Identity("kv") {};
        check(kv.certificates() != null && kv.certificates().length == 0,
                "certificates() initially empty array (not null)");
        kv.setPublicKey(kp.getPublic());
        RecCert c1 = new RecCert(kp.getPublic());
        RecCert c2 = new RecCert(kp.getPublic());
        kv.addCertificate(c1);
        check(kv.certificates().length == 1 && kv.certificates()[0] == c1, "certificate added");
        kv.addCertificate(c1);
        check(kv.certificates().length == 2, "duplicate certificate add allowed");
        kv.addCertificate(c2);
        check(kv.certificates().length == 3, "distinct certificate appended");
        checkThrows(NullPointerException.class, () -> kv.addCertificate(null),
                "addCertificate(null) -> NullPointerException");
        checkThrows(KeyManagementException.class,
                () -> kv.addCertificate(new RecCert(otherPair.getPublic())),
                "certificate key mismatch -> KeyManagementException");
        check(kv.certificates().length == 3, "failed adds leave list unchanged");
        kv.removeCertificate(c2);
        check(kv.certificates().length == 2, "removeCertificate present shrinks");
        kv.removeCertificate(c2);
        check(kv.certificates().length == 2, "removeCertificate absent silent");
        kv.removeCertificate(null);
        check(kv.certificates().length == 2, "removeCertificate(null) silent");
        check(scope.addCalls == 1, "key/certificate ops never re-notify the scope");

        // equals/hashCode: name-based only, scope and key ignored
        EqIdentity e1 = new EqIdentity("same"); e1.setPublicKey(kp.getPublic());
        EqIdentity e2 = new EqIdentity("same"); e2.setPublicKey(kp.getPublic());
        EqIdentity e3 = new EqIdentity("same");
        EqIdentity e4 = new EqIdentity("other"); e4.setPublicKey(kp.getPublic());
        check(e1.equals(e2) && e1.hashCode() == e2.hashCode(), "same name+key equal with equal hashCode");
        check(e1.equals(e3), "same name null-vs-set key still equal (name-based)");
        check(!e1.equals(e4), "different name unequal");
        check(!e1.equals("same"), "equals foreign type false");
        Identity inScope = new Identity("scoped", scope) {};
        Identity outScope = new Identity("scoped") {};
        Identity inOther = new Identity("scoped", new MapScope("other")) {};
        check(inScope.equals(outScope) && inScope.equals(inOther),
                "same name equal across scope boundaries");
        checkThrows(NullPointerException.class, () -> nullName.equals(new Identity((String) null) {}),
                "equals on null-name identity -> NullPointerException");

        // identityEquals via subclass
        check(e1.eq(e2), "identityEquals same name+key true");
        check(!e1.eq(e3) && !e3.eq(e1), "identityEquals null-key asymmetric false");
        check(!e1.eq(e4), "identityEquals different name false");

        // toString
        check(e1.toString().equals("same"), "toString() returns just the name");
        Identity noKey = new Identity("nokey") {};
        check(noKey.toString(true).contains("no public key")
                && noKey.toString(true).contains("no certificates"),
                "toString(true) keyless state");
        kv.setInfo("info string");
        check(kv.toString(true).contains("public key initialized")
                && kv.toString(true).contains("info string"), "toString(true) key+custom info");

        // serialization round-trip
        Identity ser = new Identity("sername") {};
        ser.setPublicKey(kp.getPublic());
        ser.setInfo("serial info");
        ser.addCertificate(new RecCert(kp.getPublic()));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(ser); }
        Identity back = (Identity) new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back.getName().equals("sername"), "round-trip name");
        check(back.getInfo().equals("serial info"), "round-trip info");
        check(back.getPublicKey() != null && back.getPublicKey().equals(kp.getPublic()), "round-trip key");
        check(back.certificates().length == 1, "round-trip certificates");
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
