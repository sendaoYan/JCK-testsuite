/*
 * @test
 * @summary org.w3c.dom.DOMImplementationList conformance: reflection shape checks.
 * @run main DOMImplementationListTest
 */

import java.lang.reflect.*;

public class DOMImplementationListTest {
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
        Class<?> c = Class.forName("org.w3c.dom.DOMImplementationList");
        check(c != null, "org.w3c.dom.DOMImplementationList loads");
        check(c.getName().equals("org.w3c.dom.DOMImplementationList"), "binary name is org.w3c.dom.DOMImplementationList");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.DOMImplementationList is public");
        check(c.isInterface(), "org.w3c.dom.DOMImplementationList is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "item"), "declares item");
        check(declares(c, "getLength"), "declares getLength");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral DOMImplementationList contract (via registry) ----
    static void deepContract() throws Exception {
        org.w3c.dom.bootstrap.DOMImplementationRegistry reg = org.w3c.dom.bootstrap.DOMImplementationRegistry.newInstance();
        org.w3c.dom.DOMImplementationList l = reg.getDOMImplementationList("Core 3.0");
        check(l != null, "getDOMImplementationList(Core 3.0) non-null");
        check(l.getLength() >= 1, "Core 3.0 list non-empty");
        check(l.item(0) != null, "item(0) is the first implementation");
        check(l.item(99) == null, "item(past end) null");
        check(l.item(-1) == null, "item(negative) null");
        boolean consistent = true;
        for (int i = 0; i < l.getLength(); i++) consistent &= (l.item(i) != null);
        check(consistent, "all in-range items resolve");
        org.w3c.dom.DOMImplementationList empty = reg.getDOMImplementationList("bogus 1.0");
        check(empty.getLength() == 0 && empty.item(0) == null, "unknown feature list is empty");
        // every listed implementation really supports the requested feature
        boolean allSupport = true;
        for (int i = 0; i < l.getLength(); i++) allSupport &= l.item(i).hasFeature("Core", "3.0");
        check(allSupport, "listed implementations satisfy the queried feature");
        // lists are snapshots: independent queries give equal-length results
        check(reg.getDOMImplementationList("Core 3.0").getLength() == l.getLength(), "repeated query is deterministic");
        // XML and LS feature lists also resolve
        check(reg.getDOMImplementationList("XML 3.0").getLength() >= 1, "XML 3.0 list non-empty");
    }
}
