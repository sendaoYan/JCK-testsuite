/*
 * @test
 * @summary org.w3c.dom.ranges.DocumentRange conformance: reflection shape checks.
 * @run main DocumentRangeTest
 */

import java.lang.reflect.*;

public class DocumentRangeTest {
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
        Class<?> c = Class.forName("org.w3c.dom.ranges.DocumentRange");
        check(c != null, "org.w3c.dom.ranges.DocumentRange loads");
        check(c.getName().equals("org.w3c.dom.ranges.DocumentRange"), "binary name is org.w3c.dom.ranges.DocumentRange");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.ranges.DocumentRange is public");
        check(c.isInterface(), "org.w3c.dom.ranges.DocumentRange is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "createRange"), "declares createRange");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral DocumentRange contract ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.ranges.DocumentRange dr = (org.w3c.dom.ranges.DocumentRange) d;
        check(dr != null, "Document casts to DocumentRange");
        org.w3c.dom.ranges.Range r1 = dr.createRange();
        check(r1 != null, "createRange non-null");
        check(r1.getCollapsed(), "fresh range is collapsed");
        check(r1.getStartContainer() == d && r1.getEndContainer() == d, "fresh range anchored at the document");
        check(r1.getStartOffset() == 0 && r1.getEndOffset() == 0, "fresh range offsets 0/0");
        org.w3c.dom.ranges.Range r2 = dr.createRange();
        check(r1 != r2, "createRange returns independent objects");
        r2.setStart(d, 0);
        check(r1.getCollapsed(), "mutating one range does not affect the other");
        // ranges stay usable after tree growth
        org.w3c.dom.Element e = d.createElement("r");
        d.appendChild(e);
        check(r1.getStartContainer() == d, "range container unchanged by appends");
        // createRange on a second document is independent of the first
        org.w3c.dom.Document d2 = f.newDocumentBuilder().newDocument();
        org.w3c.dom.ranges.DocumentRange dr2 = (org.w3c.dom.ranges.DocumentRange) d2;
        org.w3c.dom.ranges.Range other = dr2.createRange();
        check(other.getStartContainer() == d2, "second document produces its own ranges");
    }
}
