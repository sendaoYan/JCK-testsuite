/*
 * @test
 * @summary java.security.Policy.Parameters conformance: interface hierarchy and key methods.
 * @run main ParametersTest
 */

public class ParametersTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.Policy$Parameters");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.isEmpty(), "marker interface: no declared methods");

        testContractDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: marker typing, implementations, non-assignability ----
    static void testContractDeep() throws Exception {
        Class<?> p = java.security.Policy.Parameters.class;

        // pure marker: no methods of its own, nothing super-interface beyond nothing
        check(p.getDeclaredMethods().length == 0, "zero declared methods");
        check(p.getInterfaces().length == 0, "extends no other interface");
        check(!java.lang.annotation.Annotation.class.isAssignableFrom(p), "not an annotation");
        check(java.lang.reflect.Modifier.isPublic(p.getModifiers()), "declared public (interface)");
        // Policy itself declares Parameters as a nested member interface
        check(java.security.Policy.class.getDeclaredClasses().length > 0, "Policy has nested classes");
        boolean found = false;
        for (Class<?> n : java.security.Policy.class.getDeclaredClasses())
            if (n == p) found = true;
        check(found, "Parameters is nested inside Policy");

        // PolicySpi.JavaPolicy? no - the known public implementation is URIParameter
        java.security.URIParameter up = new java.security.URIParameter(
                new java.net.URI("file:///tmp/jck.policy"));
        check(up instanceof java.security.Policy.Parameters, "URIParameter implements Parameters");
        check(java.security.Policy.Parameters.class.isAssignableFrom(java.security.URIParameter.class),
                "isAssignableFrom(URIParameter)");
        check(up.getURI() != null && up.getURI().getPath().equals("/tmp/jck.policy"),
                "URIParameter keeps the URI");
        // round-trip through equals: identity (not overridden by value)
        java.security.URIParameter up2 = new java.security.URIParameter(
                new java.net.URI("file:///tmp/jck.policy"));
        check(!up.equals(up2), "URIParameter equals is identity-based");

        // a hand-rolled implementation is accepted wherever Parameters is taken
        java.security.Policy.Parameters mine = new java.security.Policy.Parameters() {};
        check(mine != null, "anonymous implementation instantiable");
        check(mine.getClass().getName().contains("ParametersTest"), "anonymous impl class name");

        // Configuration.Parameters look-alikes are NOT Policy.Parameters
        check(!java.security.Policy.Parameters.class.isAssignableFrom(
                        java.security.DomainLoadStoreParameter.class),
                "DomainLoadStoreParameter is not Policy.Parameters");
        // PolicySpi is not related either
        check(!java.security.Policy.Parameters.class.isAssignableFrom(java.security.PolicySpi.class),
                "PolicySpi is not Policy.Parameters");
        // and Policy itself is not its own parameter type
        check(!java.security.Policy.Parameters.class.isAssignableFrom(java.security.Policy.class),
                "Policy is not Policy.Parameters");
        // serialization: the interface imposes nothing, and URIParameter is NOT serializable
        check(!java.io.Serializable.class.isAssignableFrom(java.security.URIParameter.class),
                "URIParameter is not Serializable");
    }
}
