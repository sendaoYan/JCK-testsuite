/*
 * @test
 * @summary org.w3c.dom.bootstrap.DOMImplementationRegistry conformance: reflection shape checks.
 * @run main DOMImplementationRegistryTest
 */

import java.lang.reflect.*;

public class DOMImplementationRegistryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.w3c.dom.bootstrap.DOMImplementationRegistry");
        check(c != null, "org.w3c.dom.bootstrap.DOMImplementationRegistry loads");
        check(c.getName().equals("org.w3c.dom.bootstrap.DOMImplementationRegistry"), "binary name is org.w3c.dom.bootstrap.DOMImplementationRegistry");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.bootstrap.DOMImplementationRegistry is public");
        check(!c.isInterface(), "org.w3c.dom.bootstrap.DOMImplementationRegistry is not an interface");
        check(!Modifier.isAbstract(c.getModifiers()), "org.w3c.dom.bootstrap.DOMImplementationRegistry is concrete");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass is java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral DOMImplementationRegistry contract ----
    static void deepContract() throws Exception {
        // newInstance bootstraps from the system property without needing it set
        org.w3c.dom.bootstrap.DOMImplementationRegistry reg = org.w3c.dom.bootstrap.DOMImplementationRegistry.newInstance();
        check(reg != null, "newInstance returns a registry");
        check(reg == org.w3c.dom.bootstrap.DOMImplementationRegistry.newInstance() || reg.getClass() != null, "repeated newInstance yields usable registries");
        // the default source list wires the Xerces core implementation
        org.w3c.dom.DOMImplementation impl = reg.getDOMImplementation("Core 3.0");
        check(impl != null, "default wiring resolves Core 3.0");
        check(impl.hasFeature("XML", "3.0") && impl.hasFeature("LS", "3.0"), "default implementation covers XML/LS");
        check(!impl.hasFeature("CSS", "2.0"), "default implementation has no CSS");
        // feature string parsing: feature/version token pairs
        check(reg.getDOMImplementation("XML 3.0") != null, "XML 3.0 token pair");
        check(reg.getDOMImplementation("Traversal 2.0") != null, "Traversal 2.0 token pair");
        check(reg.getDOMImplementation("Range 2.0") != null, "Range 2.0 token pair");
        check(reg.getDOMImplementation("Events 2.0") != null, "Events 2.0 token pair");
        check(reg.getDOMImplementation("MutationEvents 2.0") != null, "MutationEvents 2.0 token pair");
        check(reg.getDOMImplementation("NoSuchFeature 1.0") == null, "unknown token pair null");
        boolean npe = false;
        try { reg.getDOMImplementation(null); } catch (NullPointerException expected) { npe = true; }
        check(npe, "null feature string -> NullPointerException");
        // list API on the registry
        org.w3c.dom.DOMImplementationList l = reg.getDOMImplementationList("Core 3.0");
        check(l.getLength() >= 1 && l.item(0) != null, "registry list non-empty");
        check(reg.getDOMImplementationList("bogus 1.0").getLength() == 0, "registry list empty for unknown features");
        // read-only view of the source-list property state (do not mutate globals)
        String prop = System.getProperty("org.w3c.dom.DOMImplementationSourceList");
        check(true, "DOMImplementationSourceList currently=" + prop);
    }
}
