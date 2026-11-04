/*
 * @test
 * @summary java.security.Identity conformance: abstract class hierarchy and key methods;
 *          plus black-box contract: constructor matrix (null name accepted, scope ctor
 *          notifies scope, dup-name ctor propagates KeyManagementException), final
 *          getName/getScope/equals, info default "No further information available.",
 *          setPublicKey replace/clear, addCertificate null->NPE / key-mismatch->
 *          KeyManagementException("public key different from cert public key") /
 *          duplicates allowed, removeCertificate silent on absent/null, certificates()
 *          empty array initially, name-based equals/hashCode (scope and key ignored,
 *          null-name equals -> NPE), identityEquals(name+key) via subclass, toString()
 *          == name / toString(true) detailed, scope notified only at construction,
 *          serialization round-trip preserving name/info/key/certs.
 * @run main IdentityTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Modifier;
import java.security.Certificate;
import java.security.Identity;
import java.security.IdentityScope;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Principal;
import java.security.PublicKey;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

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

    /** Recording java.security.Certificate (deprecated interface). */
    static class RecCert implements Certificate, java.io.Serializable {
        private final PublicKey k;
        RecCert(PublicKey k) { this.k = k; }
        public Principal getGuarantor() { return null; }
        public Principal getPrincipal() { return null; }
        public PublicKey getPublicKey() { return k; }
        public void encode(java.io.OutputStream out) {}
        public void decode(java.io.InputStream in) {}
        public String getFormat() { return "REC"; }
        public String toString(boolean detailed) { return "RecCert"; }
    }

    /** Counting scope to observe construction-time notification only. */
    static class MapScope extends IdentityScope {
        final Map<String, Identity> m = new LinkedHashMap<String, Identity>();
        int addCalls;
        MapScope(String name) { super(name); }
        public int size() { return m.size(); }
        public Identity getIdentity(String name) { return m.get(name); }
        public Identity getIdentity(PublicKey key) { return null; }
        public void addIdentity(Identity i) throws KeyManagementException { addCalls++; m.put(i.getName(), i); }
        public void removeIdentity(Identity i) { m.remove(i.getName()); }
        public Enumeration<Identity> identities() { return Collections.enumeration(m.values()); }
    }

    /** Strict scope rejecting duplicate names, to exercise the ctor KME path. */
    static class StrictScope extends MapScope {
        StrictScope(String n) { super(n); }
        public void addIdentity(Identity i) throws KeyManagementException {
            if (m.containsKey(i.getName())) throw new KeyManagementException("dup");
            super.addIdentity(i);
        }
    }

    /** Named subclass exposing the protected identityEquals. */
    static class EqIdentity extends Identity {
        EqIdentity(String n) { super(n); }
        boolean eq(Identity o) { return identityEquals(o); }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.Identity");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("java.security.Principal"), "implements java.security.Principal");
        check(in.contains("java.io.Serializable"), "implements java.io.Serializable");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("addCertificate"), "declares addCertificate");
        check(ms.contains("certificates"), "declares certificates");
        check(ms.contains("equals"), "declares equals");
        check(ms.contains("getInfo"), "declares getInfo");
        check(ms.contains("getName"), "declares getName");
        check(ms.contains("getPublicKey"), "declares getPublicKey");
        check(ms.contains("getScope"), "declares getScope");
        check(ms.contains("hashCode"), "declares hashCode");
        check(ms.contains("identityEquals"), "declares identityEquals");
        check(ms.contains("removeCertificate"), "declares removeCertificate");
        check(ms.contains("setInfo"), "declares setInfo");
        check(ms.contains("setPublicKey"), "declares setPublicKey");
        check(ms.contains("toString"), "declares toString");

        // ---- black-box: constructor matrix ----
        check(Modifier.isProtected(c.getDeclaredConstructor().getModifiers()),
                "no-arg constructor is protected (not public API)");
        Identity plain = new Identity("plain") {};
        check(plain.getName().equals("plain"), "(String) ctor name");
        check(plain.getScope() == null, "(String) ctor leaves scope null");
        check(plain.getInfo().equals("No further information available."),
                "default info string exact");
        Identity nullName = new Identity((String) null) {};
        check(nullName.getName() == null, "null name accepted, getName null");
        check(nullName.toString() == null, "toString of null-name identity returns null (printed as \"null\")");
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(512);
        KeyPair kp = kpg.generateKeyPair();
        MapScope scope = new MapScope("scope1");
        Identity scoped = new Identity("scoped", scope) {};
        check(scoped.getScope() == scope, "(String,scope) ctor wires getScope");
        check(scope.addCalls == 1 && scope.size() == 1,
                "scope ctor notifies scope.addIdentity exactly once");
        check(scope.getIdentity("scoped") == scoped, "scope sees the new identity by name");
        StrictScope strict = new StrictScope("strict");
        new Identity("dup", strict) {};
        checkThrows(KeyManagementException.class, () -> new Identity("dup", strict) {},
                "dup-name ctor propagates scope's KeyManagementException");

        // ---- final methods: name/scope/equals locked by the API ----
        check(Modifier.isFinal(c.getMethod("getName").getModifiers()), "getName is final");
        check(Modifier.isFinal(c.getMethod("getScope").getModifiers()), "getScope is final");
        check(Modifier.isFinal(c.getMethod("equals", Object.class).getModifiers()), "equals is final");

        // ---- setPublicKey / setInfo ----
        Identity kv = new Identity("kv") {};
        check(kv.getPublicKey() == null, "public key initially null");
        kv.setPublicKey(kp.getPublic());
        check(kv.getPublicKey().equals(kp.getPublic()), "setPublicKey round-trip");
        KeyPair otherPair = kpg.generateKeyPair();
        kv.setPublicKey(otherPair.getPublic());
        check(kv.getPublicKey().equals(otherPair.getPublic()), "second key silently replaces");
        kv.setPublicKey(null);
        check(kv.getPublicKey() == null, "setPublicKey(null) clears");
        check(scope.addCalls == 1, "setPublicKey does not re-notify the scope");
        kv.setInfo("custom info");
        check(kv.getInfo().equals("custom info"), "setInfo/getInfo round-trip");
        kv.setInfo(null);
        check(kv.getInfo() == null, "setInfo(null) stores null");

        // ---- certificates ----
        check(kv.certificates() != null && kv.certificates().length == 0,
                "certificates() initially empty array (not null)");
        kv.setPublicKey(kp.getPublic()); // key must match cert key
        RecCert c1 = new RecCert(kp.getPublic());
        RecCert c2 = new RecCert(kp.getPublic());
        kv.addCertificate(c1);
        check(kv.certificates().length == 1 && kv.certificates()[0] == c1, "certificate added");
        kv.addCertificate(c1);
        check(kv.certificates().length == 2, "duplicate certificate add allowed (duplicates kept)");
        kv.addCertificate(c2);
        check(kv.certificates().length == 3, "distinct certificate appended");
        checkThrows(NullPointerException.class, () -> kv.addCertificate(null),
                "addCertificate(null) -> NullPointerException");
        checkThrows(KeyManagementException.class,
                () -> kv.addCertificate(new RecCert(otherPair.getPublic())),
                "certificate with different key -> KeyManagementException");
        check(kv.certificates().length == 3, "failed adds leave certificate list unchanged");
        kv.removeCertificate(c2);
        check(kv.certificates().length == 2, "removeCertificate present shrinks list");
        kv.removeCertificate(c2);
        check(kv.certificates().length == 2, "removeCertificate absent is silent");
        kv.removeCertificate(null);
        check(kv.certificates().length == 2, "removeCertificate(null) is silent");
        check(scope.addCalls == 1, "certificate ops do not re-notify the scope");

        // ---- equals/hashCode: name-based only (scope and key ignored) ----
        EqIdentity e1 = new EqIdentity("same");
        e1.setPublicKey(kp.getPublic());
        EqIdentity e2 = new EqIdentity("same");
        e2.setPublicKey(kp.getPublic());
        EqIdentity e3 = new EqIdentity("same");
        EqIdentity e4 = new EqIdentity("other");
        e4.setPublicKey(kp.getPublic());
        check(e1.equals(e2), "same name same key equal");
        check(e1.hashCode() == e2.hashCode(), "hashCode consistent for equal identities");
        check(e1.equals(e3), "same name, null-vs-set key still equal (name-based)");
        check(e1.equals(e4) == false, "different name unequal");
        check(e1.equals(null) == false, "equals(null) false");
        check(e1.equals("same") == false, "equals(foreign type) false");
        check(e1.equals(e1), "equals reflexive");
        Identity inScope = new Identity("scoped", scope) {};
        Identity outScope = new Identity("scoped") {};
        check(inScope.equals(outScope), "same name equal across scope boundary");
        Identity inOther = new Identity("scoped", new MapScope("other")) {};
        check(inScope.equals(inOther), "same name equal in two different scopes");
        checkThrows(NullPointerException.class, () -> nullName.equals(new Identity((String) null) {}),
                "equals on null-name identity -> NullPointerException");

        // ---- identityEquals (protected, exposed by named subclass) ----
        check(e1.eq(e2), "identityEquals: same name + same key true");
        check(!e1.eq(e3), "identityEquals: null key vs set key false");
        check(!e3.eq(e1), "identityEquals: set key vs null key false (asymmetric input)");
        check(!e1.eq(e4), "identityEquals: different name false");

        // ---- toString ----
        check(e1.toString().equals("same"), "toString() returns just the name");
        Identity noKey = new Identity("nokey") {};
        String detailed = noKey.toString(true);
        check(detailed.contains("nokey") && detailed.contains("no public key")
                && detailed.contains("no certificates"), "toString(true) details key/cert state");
        kv.setInfo("roundtrip info");
        String detailed2 = kv.toString(true);
        check(detailed2.contains("public key initialized") && detailed2.contains("roundtrip info"),
                "toString(true) reflects key and custom info");

        // ---- serialization round-trip ----
        Identity ser = new Identity("sername") {};
        ser.setPublicKey(kp.getPublic());
        ser.setInfo("serial info");
        ser.addCertificate(new RecCert(kp.getPublic()));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(ser); }
        Identity back = (Identity) new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back.getName().equals("sername"), "round-trip name preserved");
        check(back.getInfo().equals("serial info"), "round-trip info preserved");
        check(back.getPublicKey() != null && back.getPublicKey().equals(kp.getPublic()),
                "round-trip public key preserved");
        check(back.certificates().length == 1, "round-trip certificates preserved");
        check(back.equals(ser), "round-trip equals original");
        check(back.getScope() == null, "round-trip unscoped identity stays unscoped");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
