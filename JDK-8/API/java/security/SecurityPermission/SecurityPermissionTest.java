/*
 * @test
 * @summary java.security.SecurityPermission conformance: full contract coverage.
 *          name/name+actions constructors, getName/getActions, null name -> NPE,
 *          empty name -> IAE, implies exact and wildcard, equals/hashCode, and
 *          subclass relationship to BasicPermission.
 * @run main SecurityPermissionTest
 */

import java.security.BasicPermission;
import java.security.SecurityPermission;

public class SecurityPermissionTest {
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
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    public static void main(String[] args) throws Exception {
        // ---- subclass hierarchy ----
        check(BasicPermission.class.isAssignableFrom(SecurityPermission.class),
                "SecurityPermission extends BasicPermission");

        // ---- constructors + getters ----
        SecurityPermission sp = new SecurityPermission("getPolicy");
        check("getPolicy".equals(sp.getName()), "getName");
        check("".equals(sp.getActions()), "getActions empty");
        SecurityPermission spa = new SecurityPermission("setPolicy", "ignored");
        check("setPolicy".equals(spa.getName()), "(name,actions) getName");

        // ---- null/empty name ----
        checkThrows(NullPointerException.class, () -> new SecurityPermission(null), "SecurityPermission(null)");
        checkThrows(IllegalArgumentException.class, () -> new SecurityPermission(""), "SecurityPermission(empty)");

        // ---- implies ----
        check(sp.implies(new SecurityPermission("getPolicy")), "implies exact match");
        check(!sp.implies(new SecurityPermission("setPolicy")), "implies different name false");
        SecurityPermission wildcard = new SecurityPermission("authProvider.*");
        check(wildcard.implies(new SecurityPermission("authProvider.x509")), "wildcard implies sub");

        // ---- equals/hashCode ----
        check(sp.equals(new SecurityPermission("getPolicy")), "equals same name");
        check(sp.hashCode() == new SecurityPermission("getPolicy").hashCode(), "hashCode equal");
        check(!sp.equals(new SecurityPermission("setPolicy")), "equals different name false");

        testContractDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: actions-ignored matrix, wildcard depth, toString, serialization ----
    static void testContractDeep() throws Exception {
        // BasicPermission(name, actions) IGNORES the actions argument entirely (never throws
        // even for non-null non-empty actions; javadoc's IAE note is not enforced)
        SecurityPermission withActions = new SecurityPermission("insertProvider.x", "read,write");
        check("insertProvider.x".equals(withActions.getName()), "actions ctor keeps name");
        check("".equals(withActions.getActions()), "non-empty actions ignored, getActions == \"\"");
        check(new SecurityPermission("y", "").getActions().isEmpty(), "empty actions accepted");

        // null name has a precise message at the BasicPermission level
        try {
            new SecurityPermission(null);
        } catch (NullPointerException npe) {
            check("name can't be null".equals(npe.getMessage()), "null name exact message");
        }
        try {
            new SecurityPermission("");
        } catch (IllegalArgumentException iae) {
            check("name can't be empty".equals(iae.getMessage()), "empty name exact message");
        }

        // wildcard hierarchy: deeper and shallower names
        SecurityPermission wc = new SecurityPermission("a.b.*");
        check(wc.implies(new SecurityPermission("a.b.c")), "x.y.* implies x.y.z");
        check(wc.implies(new SecurityPermission("a.b.c.d")), "x.y.* implies x.y.z.w");
        check(!wc.implies(new SecurityPermission("a.b")), "x.y.* does not imply x.y");
        check(wc.implies(new SecurityPermission("a.b.c.*")), "x.y.* implies x.y.z.* (suffix is literal name prefix match)");
        check(!wc.implies(new BasicPermission("a.b.c") {}), "implies requires same class");
        check(new SecurityPermission("*").implies(new SecurityPermission("anything.here")),
                "lone * implies everything (same class)");

        // equals is class-sensitive: same name in another BasicPermission subclass is not equal
        check(!sp0().equals(new BasicPermission("getPolicy") {}), "cross-subclass equals false");
        check(!sp0().equals(null), "equals null false");
        check(!sp0().equals("getPolicy"), "equals non-Permission false");

        // toString format: ("class" "name")
        String ts = sp0().toString();
        check(ts.contains("java.security.SecurityPermission") && ts.contains("getPolicy"),
                "toString contains class and name");
        check(ts.startsWith("(\""), "toString paren-quote form");

        // newPermissionCollection: homogeneous BasicPermission collections
        java.security.PermissionCollection pc = sp0().newPermissionCollection();
        check(pc != null, "newPermissionCollection non-null");
        pc.add(new SecurityPermission("getPolicy"));
        pc.add(new SecurityPermission("setPolicy.*"));
        check(pc.implies(new SecurityPermission("getPolicy")), "collection implies exact");
        check(pc.implies(new SecurityPermission("setPolicy.now")), "collection implies wildcard");
        check(!pc.implies(new SecurityPermission("getProperties")), "collection non-member false");

        // well-known targets all instantiate and serialize round-trip
        String[] targets = {"getPolicy", "setPolicy", "getProperty.key", "putProviderProperty.name",
                "removeProviderProperty.name", "setSystemScope", "getSignerPrivateKey"};
        for (String t : targets) {
            SecurityPermission probe = new SecurityPermission(t);
            check(t.equals(probe.getName()), "target instantiates: " + t);
            check(probe.implies(new SecurityPermission(t)), "target implies itself: " + t);
        }

        // serialization round-trip
        SecurityPermission rt = (SecurityPermission) roundTrip(sp0());
        check("getPolicy".equals(rt.getName()), "round-trip name");
        check("".equals(rt.getActions()), "round-trip actions");
        check(rt.equals(sp0()), "round-trip equals");
        check(rt.hashCode() == sp0().hashCode(), "round-trip hashCode");
    }

    static SecurityPermission sp0() { return new SecurityPermission("getPolicy"); }

    static Object roundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        java.io.ObjectInputStream oin = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        return oin.readObject();
    }
}
