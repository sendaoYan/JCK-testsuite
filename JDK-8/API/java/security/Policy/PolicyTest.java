/*
 * @test
 * @summary java.security.Policy conformance: abstract class hierarchy and key methods.
 * @run main PolicyTest
 */

public class PolicyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.Policy");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getInstance"), "declares getInstance");
        check(ms.contains("getParameters"), "declares getParameters");
        check(ms.contains("getPermissions"), "declares getPermissions");
        check(ms.contains("getPolicy"), "declares getPolicy");
        check(ms.contains("getProvider"), "declares getProvider");
        check(ms.contains("getType"), "declares getType");
        check(ms.contains("implies"), "declares implies");
        check(ms.contains("refresh"), "declares refresh");
        check(ms.contains("setPolicy"), "declares setPolicy");

        testContractDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: setPolicy round-trip (restored), getInstance, delegation, empty collection ----
    static class AllGrantPolicy extends java.security.Policy {
        public java.security.PermissionCollection getPermissions(java.security.CodeSource cs) {
            java.security.Permissions perms = new java.security.Permissions();
            perms.add(new java.security.AllPermission());
            return perms;
        }
    }

    static void testContractDeep() throws Exception {
        // UNSUPPORTED_EMPTY_COLLECTION: read-only, implies nothing, no elements, add throws
        java.security.PermissionCollection empty = java.security.Policy.UNSUPPORTED_EMPTY_COLLECTION;
        check(!empty.implies(new java.security.SecurityPermission("x")), "empty implies false");
        try {
            empty.implies(null);
            check(false, "empty implies(null) must throw");
        } catch (NullPointerException npe) {
            check(true, "empty implies(null) -> NPE");
        }
        check(!empty.isReadOnly(), "UNSUPPORTED_EMPTY_COLLECTION not readOnly on JDK-8 (add still throws)");
        check(!empty.elements().hasMoreElements(), "empty no elements");
        try {
            empty.add(new java.security.SecurityPermission("x"));
            check(false, "add must throw");
        } catch (SecurityException se) {
            check(true, "add throws SecurityException");
        }

        // installed policy: non-null, refresh callable, grants empty for null codesource
        java.security.Policy orig = java.security.Policy.getPolicy();
        check(orig != null, "getPolicy non-null (JDK-8: PolicyFile)");
        check("sun.security.provider.PolicyFile".equals(orig.getClass().getName()),
                "default policy class PolicyFile (JDK-8)");
        orig.refresh();
        check(true, "refresh callable no-op");
        java.security.PermissionCollection got =
                orig.getPermissions(new java.security.CodeSource(null, (java.security.cert.Certificate[]) null));
        check(got != null, "getPermissions(CodeSource) non-null");
        check(orig.getPermissions(new java.security.ProtectionDomain(null, null)) != null,
                "getPermissions(ProtectionDomain) non-null");
        check(!orig.implies(new java.security.ProtectionDomain(null, null), new java.security.AllPermission()),
                "default policy implies nothing to empty domain");
        check(orig.getType() == null, "PolicyFile.getType() null (no type name)");
        check(orig.getProvider() == null, "system policy has no provider");

        // setPolicy: round-trip with strict restore; getPolicy reflects immediately (JDK-8)
        java.security.Policy custom = new AllGrantPolicy();
        try {
            java.security.Policy.setPolicy(custom);
            check(java.security.Policy.getPolicy() == custom, "setPolicy then getPolicy identity");
            // custom policy implies via getPermissions(CodeSource) delegation (default implies impl)
            check(custom.implies(new java.security.ProtectionDomain(null, null),
                            new java.security.SecurityPermission("any.thing")),
                    "custom implies delegates to getPermissions");
            check(custom.getPermissions((java.security.CodeSource) null).implies(
                    new java.security.SecurityPermission("any.thing")), "custom getPermissions grants AllPermission");
        } finally {
            java.security.Policy.setPolicy(orig);
        }
        check(java.security.Policy.getPolicy() == orig, "original policy restored");

        // getInstance: JavaPolicy installed on JDK-8 (no params needed)
        java.security.Policy p2 = java.security.Policy.getInstance("JavaPolicy", null);
        check("JavaPolicy".equals(p2.getType()), "getInstance type JavaPolicy");
        check(p2.getProvider() != null && "SUN".equals(p2.getProvider().getName()), "getInstance provider SUN");
        check(p2.getParameters() == null, "null parameters passed through");
        p2.refresh();
        check(true, "getInstance policy refresh callable");
        // unknown type -> NSAE naming the type
        try {
            java.security.Policy.getInstance("BogusPolicy", null);
            check(false, "BogusPolicy must fail");
        } catch (java.security.NoSuchAlgorithmException nsae) {
            check(nsae.getMessage() != null && nsae.getMessage().contains("BogusPolicy"),
                    "NSAE message contains requested type");
        }
        // null type -> NPE
        try {
            java.security.Policy.getInstance(null, null);
            check(false, "null type must fail");
        } catch (NullPointerException npe) {
            check(true, "null type -> NPE");
        }

        // Policy.Parameters marker interface
        check(java.security.Policy.Parameters.class.isInterface(), "Parameters is an interface");
        check(new java.security.URIParameter(new java.net.URI("file:///x")) instanceof java.security.Policy.Parameters,
                "URIParameter implements Parameters");
        // DomainLoadStoreParameter is a Configuration parameter, NOT a Policy.Parameters
        check(!java.security.Policy.Parameters.class.isAssignableFrom(java.security.DomainLoadStoreParameter.class),
                "DomainLoadStoreParameter is not Policy.Parameters");
    }
}
