/*
 * @test
 * @summary java.security.Principal conformance: interface hierarchy and key methods.
 * @run main PrincipalTest
 */

public class PrincipalTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.Principal");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("equals"), "declares equals");
        check(ms.contains("getName"), "declares getName");
        check(ms.contains("hashCode"), "declares hashCode");
        check(ms.contains("implies"), "declares implies");
        check(ms.contains("toString"), "declares toString");

        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- appended contract dimensions ----
    static void testContract() throws Exception {
        // exact method surface: 5 methods, implies is a JDK-8 default method
        java.lang.reflect.Method[] ms = java.security.Principal.class.getDeclaredMethods();
        check(ms.length == 5, "exactly 5 declared methods");
        java.lang.reflect.Method impl = java.security.Principal.class
                .getMethod("implies", javax.security.auth.Subject.class);
        check(impl.isDefault(), "implies is a default method");
        check(impl.getReturnType() == boolean.class, "implies returns boolean");
        check(java.security.Principal.class.getMethod("getName").getReturnType() == String.class,
                "getName returns String");

        // minimal implementations: lambda and anonymous class
        java.security.Principal lambda = () -> "jck-lambda";
        check("jck-lambda".equals(lambda.getName()), "lambda getName");
        java.security.Principal anon = new java.security.Principal() {
            public String getName() { return "jck-anon"; }
        };
        check("jck-anon".equals(anon.getName()), "anon getName");
        check(anon.toString().contains("PrincipalTest") || anon.toString().contains("$"),
                "default toString mentions implementation class");

        // implies default semantics: true iff this principal is a member of the subject
        javax.security.auth.x500.X500Principal p = new javax.security.auth.x500.X500Principal("CN=jck-root");
        javax.security.auth.Subject with =
                new javax.security.auth.Subject(false,
                        java.util.Collections.singleton((java.security.Principal) p),
                        java.util.Collections.emptySet(), java.util.Collections.emptySet());
        javax.security.auth.Subject without = new javax.security.auth.Subject();
        check(p.implies(with), "implies true when member of subject");
        check(!p.implies(without), "implies false when not a member");
        check(!p.implies(null), "implies false for null subject");

        // flagship implementation: X500Principal value semantics
        javax.security.auth.x500.X500Principal same = new javax.security.auth.x500.X500Principal("CN=jck-root");
        check(p.equals(same) && same.equals(p), "X500 equals symmetric");
        check(p.hashCode() == same.hashCode(), "X500 hashCode equal");
        check(!p.equals(new javax.security.auth.x500.X500Principal("CN=other")), "X500 different not equal");
        check(!p.equals(null), "equals null false");
        check(!p.equals("CN=jck-root"), "equals wrong type false");
        check(p.getName().equals("CN=jck-root"), "X500 RFC2253 name");
        check(p.getName(javax.security.auth.x500.X500Principal.CANONICAL).equals("cn=jck-root"),
                "X500 canonical name");
        check(p.toString().contains("CN=jck-root"), "X500 toString");
        check(new javax.security.auth.x500.X500Principal(p.getEncoded()).equals(p),
                "X500 from encoded der bytes");

        // principals participate in ProtectionDomain
        java.security.ProtectionDomain pd = new java.security.ProtectionDomain(null, null, null,
                new java.security.Principal[]{p, anon});
        java.security.Principal[] got = pd.getPrincipals();
        check(got.length == 2 && got[0] == p && got[1] == anon, "ProtectionDomain principals identity");

        // Identity (deprecated) implements Principal
        java.security.Identity id = new java.security.Identity("jckid") {};
        check(id instanceof java.security.Principal && "jckid".equals(id.getName()),
                "Identity is a Principal");
    }
}
